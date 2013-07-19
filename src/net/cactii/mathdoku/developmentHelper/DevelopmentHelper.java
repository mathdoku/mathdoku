package net.cactii.mathdoku.developmentHelper;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.gridGenerating.DialogPresentingGridGenerator;
import net.cactii.mathdoku.gridGenerating.GridGenerator;
import net.cactii.mathdoku.gridGenerating.GridGenerator.PuzzleComplexity;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.ui.PuzzleFragmentActivity;
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
	 * @param puzzleFragmentActivity
	 *            The main activity in which context the menu item was selected.
	 * @param menuId
	 *            The selected menu item.
	 * @return True in case the menu item is processed successfully. False
	 *         otherwise.
	 */
	public static boolean onDevelopmentHelperOption(
			PuzzleFragmentActivity puzzleFragmentActivity, int menuId) {
		if (mMode == Mode.DEVELOPMENT) {
			switch (menuId) {
			case R.id.development_mode_delete_database:
				executeDeleteDatabase(puzzleFragmentActivity);
				break;
			case R.id.development_mode_generate_games:
				// Generate games
				generateGames(puzzleFragmentActivity);
				return true;
			case R.id.development_mode_reset_preferences:
				resetPreferences(puzzleFragmentActivity);
				return true;
			case R.id.development_mode_unlock_archive:
				unlockArchiveAndStatistics();
				return true;
			case R.id.development_mode_clear_data:
				deleteGamesAndPreferences(puzzleFragmentActivity);
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
	public static void deleteDatabase(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					puzzleFragmentActivity);
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
									executeDeleteDatabase(puzzleFragmentActivity);
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
	public static void generateGames(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		puzzleFragmentActivity.mDialogPresentingGridGenerator = new DialogPresentingGridGenerator(
				puzzleFragmentActivity, 6, false, PuzzleComplexity.NORMAL,
				Util.getPackageVersionNumber());
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			// Set the options for the grid generator
			GridGenerator.GridGeneratorOptions gridGeneratorOptions = puzzleFragmentActivity.mDialogPresentingGridGenerator.new GridGeneratorOptions();
			gridGeneratorOptions.createFakeUserGameFiles = true;
			gridGeneratorOptions.numberOfGamesToGenerate = 8;

			// Set to false to generate grids with same size and hideOperators
			// value as initial grid.
			gridGeneratorOptions.randomGridSize = false;
			gridGeneratorOptions.randomHideOperators = false;

			// Start the grid generator
			puzzleFragmentActivity.mDialogPresentingGridGenerator
					.setGridGeneratorOptions(gridGeneratorOptions);

			// Start the background task to generate the new grids.
			puzzleFragmentActivity.mDialogPresentingGridGenerator.execute();
		}
	}

	public static void generateGamesReady(
			final PuzzleFragmentActivity puzzleFragmentActivity,
			int numberOfGamesGenerated) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			new AlertDialog.Builder(puzzleFragmentActivity)
					.setTitle("Games generated")
					.setMessage(
							Integer.toString(numberOfGamesGenerated)
									+ " games have been generated. Note that it is not "
									+ "guaranteed that those puzzles have unique solutions.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Do nothing
								}
							}).show();
		}
	}

	/**
	 * Removes all preferences. After restart of the app the preferences will be
	 * initalised with default values. Saved games will not be deleted!
	 * 
	 * @param puzzleFragmentActivity
	 *            The activity in which context the preferences are resetted.
	 * @return
	 */
	public static boolean resetPreferences(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			executeDeleteAllPreferences();

			// Show dialog
			new AlertDialog.Builder(puzzleFragmentActivity)
					.setMessage(
							"All preferences have been removed. After restart "
									+ "of the app the preferences will be "
									+ "initialized with default values.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									restartActivity(puzzleFragmentActivity);
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
	 * @param puzzleFragmentActivity
	 *            The activity in which context the preferences are reseted.
	 */
	public static void deleteGamesAndPreferences(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					puzzleFragmentActivity);
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
									executeDeleteAllPreferences();
									executeDeleteDatabase(puzzleFragmentActivity);
									restartActivity(puzzleFragmentActivity);
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
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
	 * @param puzzleFragmentActivity
	 *            The activity for which the database has to be deleted.
	 */
	private static void executeDeleteDatabase(
			PuzzleFragmentActivity puzzleFragmentActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			// Close database helper (this will also close the open databases).
			DatabaseHelper.getInstance().close();

			// Delete the database.
			puzzleFragmentActivity.deleteDatabase(DatabaseHelper.DATABASE_NAME);

			// Reopen the database helper.
			DatabaseHelper.getInstance(puzzleFragmentActivity);
		}
	}

	/**
	 * Restart the activity automatically. If not possible due to OS version,
	 * show a dialog and ask user to restat manually.
	 * 
	 * @param puzzleFragmentActivity
	 *            The activity to be restarted.
	 */
	private static void restartActivity(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// The activity can be restarted automatically. No dialog
				// needed. Note: we can not call puzzleFragmentActivity.recreate
				// here as
				// the app can't be run anymore on Android 1.6.
				DevelopmentHelperHoneycombAndAbove
						.restartActivity(puzzleFragmentActivity);
				return;
			}

			// Can restart activity automatically on pre honeycomb. Show dialog
			// an
			// let user restart manually.
			new AlertDialog.Builder(puzzleFragmentActivity)
					.setMessage(
							"Press OK to close the app and restart the "
									+ "activity yourself")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									puzzleFragmentActivity.finish();
								}
							}).show();
		}
	}

	/**
	 * Checks the consistency of the database when running in development mode.
	 * 
	 * @param puzzleFragmentActivity
	 *            The activitity in which context the database is checked.
	 * @return False in case the database is not consistent. True otherwise.
	 */
	public static boolean checkDatabaseConsistency(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
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

			if (DatabaseHelper.hasChangedTableDefinitions()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						puzzleFragmentActivity);
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
										executeDeleteDatabase(puzzleFragmentActivity);
										restartActivity(puzzleFragmentActivity);
									}
								});
				AlertDialog dialog = builder.create();
				dialog.show();
				return false;
			}
		}

		return true;
	}

	/**
	 * Make options Archive and Statistics visible in the main menu.
	 */
	private static void unlockArchiveAndStatistics() {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			Editor prefeditor = Preferences.getInstance().mSharedPreferences
					.edit();
			prefeditor.putBoolean(Preferences.SHOW_ARCHIVE, true);
			prefeditor.putBoolean(Preferences.SHOW_STATISTICS, true);
			prefeditor.commit();
		}
	}
}