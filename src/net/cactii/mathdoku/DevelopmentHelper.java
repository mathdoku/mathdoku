package net.cactii.mathdoku;

import java.util.ArrayList;

import net.cactii.mathdoku.GameFile.GameFileType;
import net.cactii.mathdoku.MainActivity.InputMode;
import net.cactii.mathdoku.DevelopmentHelpers.DevelopmentHelperHoneycombAndAbove;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

/**
 * The Development Helper class is intended to support Development and Unit
 * Testing of this application. Variables and methods should not be used in
 * production code with exception of static variable {@link #mode}.
 * 
 * Checks on variable {@link #mode} should always be made in such a way that the
 * result can be determined at compile time. In this way the enclosed block will
 * not be included in the compiled case when the condition for executing the
 * block evaluates to false. Example of intended usage:
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

	public static final Mode mode = Mode.DEVELOPMENT;

	// In development mode the grid generator will show a modified progress
	// dialog. Following types of progress updates are supported. Actual values
	// do not matter as long they are unique strings.
	public static final String GRID_GENERATOR_PROGRESS_UPDATE_TITLE = "Update title";
	public static final String GRID_GENERATOR_PROGRESS_UPDATE_MESSAGE = "Update message";
	public static final String GRID_GENERATOR_PROGRESS_UPDATE_PROGRESS = "Update progress";
	public static final String GRID_GENERATOR_PROGRESS_UPDATE_SOLUTION = "Found a solution";

	/**
	 * Generate dummy games. A dummy game is not a real game which can be played
	 * as it is not checked on having ###########
	 * 
	 * @param context
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	public static void generateGames(final MainActivity mainActivity) {
		int maxCageResult = mainActivity.getResources().getInteger(
				R.integer.maximum_cage_value);
		mainActivity.mGridGeneratorTask = new GridGenerator(mainActivity, 6, 4,
				maxCageResult, false);
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
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
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
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
									// Remove preference which controls the
									// process
									// of preview image creation
									if (mainActivity.preferences != null
											&& mainActivity.preferences
													.contains(MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED)) {
										Editor prefeditor = mainActivity.preferences
												.edit();
										prefeditor
												.remove(MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED);
										prefeditor.commit();
									}
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
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
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
	 * Delete all preview images and resets the preference which is used to
	 * check whether preview images have to be generated.
	 * 
	 * @param mainActivity
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	public static void recreateAllPreviews(final MainActivity mainActivity) {
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
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

									// Reset preference which controls the
									// process of preview image creation
									if (mainActivity.preferences != null) {
										Editor prefeditor = mainActivity.preferences
												.edit();
										prefeditor
												.putBoolean(
														MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
														MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT);
										prefeditor.commit();
									}

									restartActivity(mainActivity);
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	/**
	 * Reset the preferences to a given version of the game.
	 * 
	 * @param mainActivity
	 *            The activity in which context the preferences are resetted.
	 * @param targetVersion
	 *            The version of the game to which preferences will be resetted.
	 * @return
	 */
	public static boolean resetPreferences(final MainActivity mainActivity,
			int targetVersion) {
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
			SharedPreferences preferences = mainActivity.preferences;

			if (preferences != null) {
				String finalDialogMessage = "";

				int currentVersion = preferences.getInt(
						MainActivity.PREF_CURRENT_VERSION,
						MainActivity.PREF_CURRENT_VERSION_DEFAULT);
				Editor prefeditor = preferences.edit();

				prefeditor.putInt(MainActivity.PREF_CURRENT_VERSION,
						targetVersion);

				if (targetVersion <= 77
						&& preferences
								.contains(MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED)) {
					prefeditor
							.remove(MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED);
				}

				if (targetVersion <= 77 && currentVersion > 111) {
					finalDialogMessage += "With upgrade to revision 111 or above, all filenames "
							+ "have been changed. These changes have not been "
							+ "reverted. Those games will be visible again after "
							+ "the upgrade to this version has been completed.\n\n";
				}

				prefeditor.commit();

				// Show the final dialog
				new AlertDialog.Builder(mainActivity)
						.setMessage(finalDialogMessage)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										restartActivity(mainActivity);
									}
								}).show();

				return true;
			}
		}
		return false;
	}

	/**
	 * Delete all data (games and preferences). It is provided as an easy access
	 * instead of using the button in the AppInfo dialog which involves opening
	 * the application manager.
	 * 
	 * @param mainActivity
	 *            The activity in which context the preferences are resetted.
	 */
	public static void deleteGamesAndPreferences(final MainActivity mainActivity) {
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
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
									Editor prefeditor = mainActivity.preferences
											.edit();
									prefeditor.clear();
									prefeditor.commit();

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
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
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
	 * Restart the activity automatically. If not possible due to OS version,
	 * show a dialog and ask user to restat manually.
	 * 
	 * @param mainActivity
	 *            The activity to be restarted.
	 */
	@TargetApi(Build.VERSION_CODES.DONUT)
	private static void restartActivity(final MainActivity mainActivity) {
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
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
}