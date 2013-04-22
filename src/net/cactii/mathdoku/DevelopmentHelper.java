package net.cactii.mathdoku;

import java.util.ArrayList;

import net.cactii.mathdoku.MainActivity.InputMode;
import net.cactii.mathdoku.DevelopmentHelpers.DevelopmentHelperHoneycombAndAbove;
import net.cactii.mathdoku.storage.GameFile;
import net.cactii.mathdoku.storage.GameFile.GameFileType;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;
import net.cactii.mathdoku.util.Util;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.os.Build;

/**
 * The Development Helper class is intended to support Development and Unit
 * Testing of this application. Variables and methods should not be used in
 * production code with exception of static variable {@link #mMode}.
 * 
 * Checks on variable {@link #mMode} should always be made in such a way that
 * the result can be determined at compile time. In this way the enclosed block
 * will not be included in the compiled case when the condition for executing
 * the block evaluates to false. Example of intended usage:
 * 
 * <pre class="prettyprint">
 * if (DevelopmentHelper.mode = Mode.UNIT_TESTING) {
 * 	// code which should only be included in case the app is used for unit
 * 	// testing
 * }
 * </pre>
 * 
 * IMPORTANT: Use block above also in all helper function in this class. In this
 * way all development code will not be compiled into the APK as long as the
 * development mode is turned off.
 */
public class DevelopmentHelper {
	public static String TAG_LOG = "MathDoku.DevelopmentHelper";

	public enum Mode {
		DEVELOPMENT, UNIT_TESTING, PRODUCTION
	};

	public static final Mode mMode = Mode.DEVELOPMENT;

	// In development mode the grid generator will show a modified progress
	// dialog. Following types of progress updates are supported. Actual values
	// do not matter as long they are unique strings.
	public static final String GRID_GENERATOR_PROGRESS_UPDATE_TITLE = "Update title";
	public static final String GRID_GENERATOR_PROGRESS_UPDATE_MESSAGE = "Update message";
	public static final String GRID_GENERATOR_PROGRESS_UPDATE_PROGRESS = "Update progress";
	public static final String GRID_GENERATOR_PROGRESS_UPDATE_SOLUTION = "Found a solution";

	/**
	 * Checks if given menu item id can be processed by the development helper.
	 * 
	 * @param mainActivity
	 *            The main activity in which context the menu item was selected.
	 * @param menuId
	 *            The selected menu item.
	 * @return True in case the menu item is processed succesfully. False
	 *         otherwise.
	 */
	public static boolean onDevelopmentHelperOption(MainActivity mainActivity,
			int menuId) {
		if (mMode == Mode.DEVELOPMENT) {
			switch (menuId) {
			case R.id.development_mode_delete_database:
				executeDeleteDatabase(mainActivity);
				break;
			case R.id.development_mode_generate_games:
				// Cancel old timer
				mainActivity.stopTimer();

				// Generate games
				generateGames(mainActivity);
				return true;
			case R.id.development_mode_recreate_previews:
				recreateAllPreviews(mainActivity);
				return true;
			case R.id.development_mode_delete_games:
				deleteAllGames(mainActivity);
				return true;
			case R.id.development_mode_reset_preferences:
				resetPreferences(mainActivity);
				return true;
			case R.id.development_mode_clear_data:
				deleteGamesAndPreferences(mainActivity);
				return true;
			case R.id.development_mode_reset_log:
				// Delete old log
				UsageLog.getInstance().delete();

				// Reset preferences
				Preferences.getInstance().resetUsageLogDisabled();

				// Re-enable usage log
				UsageLog.getInstance(mainActivity);
				return true;
			case R.id.development_mode_send_log:
				UsageLog.getInstance().askConsentForSendingLog(mainActivity);
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	/**
	 * Delete database.
	 * 
	 * @param context
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	public static void deleteDatabase(final MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
			builder.setTitle("Delete database?")
					.setMessage(
							"The database will be deleted. All statistic "
									+ "information will be lost permanently.")
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Do nothing
								}
							})
					.setPositiveButton("Delete database",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									executeDeleteDatabase(mainActivity);
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	/**
	 * Generate dummy games. A dummy game is not a real game which can be played
	 * as it is not checked on having a unique solution.
	 * 
	 * @param context
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	public static void generateGames(final MainActivity mainActivity) {
		int maxCageResult = mainActivity.getResources().getInteger(
				R.integer.maximum_cage_value);
		mainActivity.mGridGeneratorTask = new GridGenerator(mainActivity, 8, 4,
				maxCageResult, false,
				new Util(mainActivity).getPackageVersionNumber());
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			// Set the options for the grid generator
			GridGenerator.GridGeneratorOptions gridGeneratorOptions = mainActivity.mGridGeneratorTask.new GridGeneratorOptions();
			gridGeneratorOptions.createFakeUserGameFiles = true;
			gridGeneratorOptions.numberOfGamesToGenerate = 40;

			// Set to false to generate grids with same size and hideOperators
			// value as initial grid.
			gridGeneratorOptions.randomGridSize = false;
			gridGeneratorOptions.randomHideOperators = false;

			// Start the grid generator
			mainActivity.mGridGeneratorTask
					.setGridGeneratorOptions(gridGeneratorOptions);

			// Start the background task to generate the new grids.
			mainActivity.mGridGeneratorTask.execute();
		}
	}

	public static void generateGamesReady(final MainActivity mainActivity,
			int numberOfGamesGenerated) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			new AlertDialog.Builder(mainActivity)
					.setTitle("Games generated")
					.setMessage(
							Integer.toString(numberOfGamesGenerated)
									+ " games have been generated. Note that it is not "
									+ "guaranteed that those puzzles have unique solutions. "
									+ "After restart of the activity the preview images "
									+ "will be created for the newly created games.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Preferences.getInstance(mainActivity)
											.initCreatePreviewImagesCompleted();
									restartActivity(mainActivity);
								}
							}).show();
		}
	}

	/**
	 * Delete all games (including preview images).
	 * 
	 * @param context
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	public static void deleteAllGames(final MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
			builder.setTitle("Delete all?")
					.setMessage("All games and previews will be deleted.")
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Do nothing
								}
							})
					.setPositiveButton("Delete all",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									executeDeleteAllGames();
									mainActivity.mGrid = null;
									mainActivity
											.setInputMode(InputMode.NO_INPUT__HIDE_GRID);
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	/**
	 * Delete all preview images and resets the preferences at the default which
	 * is used to check whether preview images have to be generated.
	 * 
	 * @param mainActivity
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	public static void recreateAllPreviews(final MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
			builder.setTitle("Recreate all previews?")
					.setMessage(
							"All previews will be deleted. Also the preference which "
									+ "is used to check whether previews have to be "
									+ "generated is resetted.")
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Do nothing
								}
							})
					.setPositiveButton("Create new previews",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Delete preview last game file
									GameFile gameFileLastGame = new GameFile(
											GameFileType.LAST_GAME);
									if (gameFileLastGame.exists()
											&& gameFileLastGame
													.hasPreviewImage()) {
										gameFileLastGame.deletePreviewImage();
									}

									// Delete preview new game file
									GameFile gameFileNewGame = new GameFile(
											GameFileType.NEW_GAME);
									if (gameFileNewGame.exists()
											&& gameFileNewGame
													.hasPreviewImage()) {
										gameFileNewGame.deletePreviewImage();
									}

									// Delete preview all user games
									ArrayList<String> filenames = GameFile
											.getAllGameFilesCreatedByUser(Integer.MAX_VALUE);
									for (String filename : filenames) {
										GameFile gameFile = new GameFile(
												filename);
										if (gameFile.hasPreviewImage()) {
											gameFile.deletePreviewImage();
										}
									}

									Preferences.getInstance(mainActivity)
											.initCreatePreviewImagesCompleted();
									restartActivity(mainActivity);
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	/**
	 * Removes all preferences. After restart of the app the preferences will be
	 * initalised with default values. Saved games will not be deleted!
	 * 
	 * @param mainActivity
	 *            The activity in which context the preferences are resetted.
	 * @return
	 */
	public static boolean resetPreferences(final MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			executeDeleteAllPreferences();

			// Show dialog
			new AlertDialog.Builder(mainActivity)
					.setMessage(
							"All preferences have been removed. After restart "
									+ "of the app the preferences will be "
									+ "initialized with default values.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									restartActivity(mainActivity);
								}
							}).show();

			return true;
		}
		return false;
	}

	/**
	 * Delete all data (games, database and preferences). It is provided as an
	 * easy access instead of using the button in the AppInfo dialog which
	 * involves opening the application manager.
	 * 
	 * @param mainActivity
	 *            The activity in which context the preferences are resetted.
	 */
	public static void deleteGamesAndPreferences(final MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
			builder.setTitle("Delete all data and preferences?")
					.setMessage(
							"All data and preferences for MathDoku will be deleted.")
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Do nothing
								}
							})
					.setPositiveButton("Delete all",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									executeDeleteAllGames();
									executeDeleteAllPreferences();
									executeDeleteDatabase(mainActivity);
									mainActivity.mGrid = null;
									mainActivity
											.setInputMode(InputMode.NO_INPUT__HIDE_GRID);

									restartActivity(mainActivity);
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	/**
	 * Delete all game files (including previews) without warning.
	 */
	private static void executeDeleteAllGames() {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			// Delete last game file
			GameFile gameFileLastGame = new GameFile(GameFileType.LAST_GAME);
			if (gameFileLastGame.exists()) {
				gameFileLastGame.delete();
			}

			// Delete new game file
			GameFile gameFileNewGame = new GameFile(GameFileType.NEW_GAME);
			if (gameFileNewGame.exists()) {
				gameFileNewGame.delete();
			}

			// Delete all user games
			ArrayList<String> filenames = GameFile
					.getAllGameFilesCreatedByUser(Integer.MAX_VALUE);
			for (String filename : filenames) {
				new GameFile(filename).delete();
			}
		}
	}

	/**
	 * Deletes all preferences.
	 */
	private static void executeDeleteAllPreferences() {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			Editor prefeditor = Preferences.getInstance().mSharedPreferences
					.edit();
			prefeditor.clear();
			prefeditor.commit();
		}
	}

	/**
	 * Deletes the database.
	 * 
	 * @param mainActivity
	 *            The activity for which the database has to be deleted.
	 */
	private static void executeDeleteDatabase(MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			// Close database helper (this will also close the open databases).
			DatabaseHelper.getInstance().close();

			// Delete the database.
			mainActivity.deleteDatabase(DatabaseHelper.DATABASE_NAME);

			// Reopen the database helper.
			DatabaseHelper.getInstance(mainActivity);
		}
	}

	/**
	 * Restart the activity automatically. If not possible due to OS version,
	 * show a dialog and ask user to restat manually.
	 * 
	 * @param mainActivity
	 *            The activity to be restarted.
	 */
	private static void restartActivity(final MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// The activity can be restarted automatically. No dialog
				// needed. Note: we can not call mainActivity.recreate here as
				// the app can't be run anymore on Android 1.6.
				DevelopmentHelperHoneycombAndAbove
						.restartActivity(mainActivity);
				return;
			}

			// Can restart activity automatically on pre honeycomb. Show dialog
			// an
			// let user restart manually.
			new AlertDialog.Builder(mainActivity)
					.setMessage(
							"Press OK to close the app and restart the "
									+ "activity yourself")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									mainActivity.finish();
								}
							}).show();
		}
	}

	public static void checkDatabaseConsistency(final MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			// While developping it regularly occurs that table definitions have
			// been altered without creating separate database versions. As the
			// database are accessed when the last game is restarted this
			// results in a force close without having the ability to delete the
			// databases via menu "DevelopmentTools". As this is very
			// inconvenient, the validity of the table is checked right here.
			DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

			// Explicitly get a writeable database first. In case the database
			// does not yet exists it will be created. Of course the database
			// will be consistent just after it has been created.
			databaseHelper.getWritableDatabase();

			if (new GridDatabaseAdapter(databaseHelper)
					.isTableDefinitionChanged()
					|| new StatisticsDatabaseAdapter(databaseHelper)
							.isTableDefinitionChanged()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						mainActivity);
				builder.setTitle("Database is inconsistent?")
						.setMessage(
								"The database is not consistent. This is probably due "
										+ "to a table alteration (see logmessages) "
										+ "without changing the revision number in "
										+ "the manifest. Either update the revision "
										+ "number in the manifest or delete the "
										+ "database.\n"
										+ "If you continue to use this this might "
										+ "result in (unhandeld) exceptions.")
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// Do nothing
									}
								})
						.setPositiveButton("Delete database",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										executeDeleteDatabase(mainActivity);
										restartActivity(mainActivity);
									}
								});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		}
	}
}