package net.cactii.mathdoku;

import java.util.ArrayList;

import net.cactii.mathdoku.GameFile.GameFileType;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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
 * @author Paul Dingemans
 * 
 */
public class DevelopmentHelper {
	public static String TAG_LOG = "MathDoku.DevelopmentHelper";

	public enum Mode {
		DEVELOPMENT, UNIT_TESTING, PRODUCTION
	};

	public static final Mode mode = Mode.DEVELOPMENT;

	/**
	 * Generate dummy games. A dummy game is not a real game which can be played
	 * as it is not checked on having ###########
	 * 
	 * @param context
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	public static void generateGames(final MainActivity mainActivity) {
		mainActivity.mGridGeneratorTask = new GridGenerator(mainActivity, 4,
				true);

		// Set the options for the grid generator
		GridGenerator.GridGeneratorOptions gridGeneratorOptions = mainActivity.mGridGeneratorTask.new GridGeneratorOptions();
		gridGeneratorOptions.createFakeUserGameFiles = true;
		gridGeneratorOptions.numberOfGamesToGenerate = 10;
		mainActivity.mGridGeneratorTask
				.setGridGeneratorOptions(gridGeneratorOptions);

		// Start the background task to generate the new grids.
		mainActivity.mGridGeneratorTask.execute();
	}

	public static void generateGamesReady(final MainActivity mainActivity,
			int numberOfGamesGenerated) {
		new AlertDialog.Builder(mainActivity)
				.setTitle("Games generated")
				.setMessage(
						Integer.toString(numberOfGamesGenerated)
								+ " games have been generated. Note that it is not "
								+ "guaranteed that those puzzles have unique solutions. "
								+ "Please restart the activity in order to generate "
								+ "preview images for the newly created games.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Remove preference which controls the process
						// of preview image creation
						if (mainActivity.preferences != null
								&& mainActivity.preferences
										.contains(MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED)) {
							Editor prefeditor = mainActivity.preferences.edit();
							prefeditor
									.remove(MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED);
							prefeditor.commit();
						}
						mainActivity.finish();
					}
				}).show();
	}

	/**
	 * Delete all games (including preview images).
	 * 
	 * @param context
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	public static void deleteAllGames(MainActivity mainActivity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setTitle("Delete all?")
				.setMessage("All games and previews will be deleted.")
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Do nothing
							}
						})
				.setPositiveButton("Delete all",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Delete last game file
								GameFile gameFileLastGame = new GameFile(
										GameFileType.LAST_GAME);
								if (gameFileLastGame.exists()) {
									gameFileLastGame.delete();
								}

								// Delete new game file
								GameFile gameFileNewGame = new GameFile(
										GameFileType.NEW_GAME);
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
						});
		AlertDialog dialog = builder.create();
		dialog.show();
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
		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setTitle("Recreate all previews?")
				.setMessage(
						"All previews will be deleted. Also the preference which "
								+ "is used to check whether previews have to be "
								+ "generated is resetted.")
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Do nothing
							}
						})
				.setPositiveButton("Create new previews",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Delete preview last game file
								GameFile gameFileLastGame = new GameFile(
										GameFileType.LAST_GAME);
								if (gameFileLastGame.exists()
										&& gameFileLastGame.hasPreviewImage()) {
									gameFileLastGame.deletePreviewImage();
								}

								// Delete preview new game file
								GameFile gameFileNewGame = new GameFile(
										GameFileType.NEW_GAME);
								if (gameFileNewGame.exists()
										&& gameFileNewGame.hasPreviewImage()) {
									gameFileNewGame.deletePreviewImage();
								}

								// Delete preview all user games
								ArrayList<String> filenames = GameFile
										.getAllGameFilesCreatedByUser(Integer.MAX_VALUE);
								for (String filename : filenames) {
									GameFile gameFile = new GameFile(filename);
									if (gameFile.hasPreviewImage()) {
										gameFile.deletePreviewImage();
									}
								}

								// Remove preference which controls the process
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

								// Restart the activity on confirmation of user
								new AlertDialog.Builder(mainActivity)
										.setMessage(
												"Press OK to close the app and restart the "
														+ "activity yourself to start preview "
														+ "creation process")
										.setPositiveButton(
												"OK",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														mainActivity.finish();
													}
												}).show();
							}
						});
		AlertDialog dialog = builder.create();
		dialog.show();
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
		SharedPreferences preferences = mainActivity.preferences;

		if (preferences != null) {
			String finalDialogMessage = "";
			
			int currentVersion = preferences.getInt("currentversion", -1);
			Editor prefeditor = preferences.edit();

			prefeditor.putInt("currentversion", targetVersion);

			if (targetVersion <= 77
					&& preferences
							.contains(MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED)) {
				prefeditor
						.remove(MainActivity.PREF_CREATE_PREVIEW_IMAGES_COMPLETED);
			}

			if (targetVersion <= 77 && currentVersion > 111) {
				finalDialogMessage += 
								"With upgrade to revision 111 or above, all filenames "
										+ "have been changed. These changes have not been "
										+ "reverted. Those games will be visible again after "
										+ "the upgrade to this version has been completed.\n\n";
			}

			prefeditor.commit();
			
			// Show the final dialog
			finalDialogMessage += "Press OK to close the app. Restart manually. ";
			new AlertDialog.Builder(mainActivity)
					.setMessage(finalDialogMessage)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									mainActivity.finish();
								}
							}).show();

			return true;
		}
		return false;
	}
}