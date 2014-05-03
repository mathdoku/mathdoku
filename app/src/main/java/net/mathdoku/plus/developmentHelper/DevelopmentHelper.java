package net.mathdoku.plus.developmenthelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.archive.ui.ArchivePreferenceActivity;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.gridgenerating.GeneratePuzzleProgressDialog;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridgenerating.GridGeneratingParametersBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;

import java.util.Random;

/**
 * The Development Helper class is intended to support Development and Unit
 * Testing of this application. Variables and methods should not be used in
 * production code.
 * <p/>
 * Checks on variable {@link net.mathdoku.plus.config.Config#mAppMode} should
 * always be made in such a way that the result can be determined at compile
 * time. In this way the enclosed block will not be included in the compiled
 * case when the condition for executing the block evaluates to false. Example
 * of intended usage:
 * <p/>
 * 
 * <code>
 *     if (DevelopmentHelper.mode == AppMode.UNIT_TESTING) {
 * 	       // code which should only be included in case the app is used for unit
 * 	       // testing
 *     }
 * </code>
 * <p/>
 * IMPORTANT: Use block above also in all helper function in this class. In this
 * way all development code will not be compiled into the APK as long as the
 * development mode is turned off.
 */
public class DevelopmentHelper {
	@SuppressWarnings("unused")
	private static final String TAG = DevelopmentHelper.class.getName();

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
			PuzzleFragmentActivity puzzleFragmentActivity, int menuId, Grid grid) {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			switch (menuId) {
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
			case R.id.development_mode_delete_database_and_preferences:
				deleteDatabaseAndPreferences(puzzleFragmentActivity);
				return true;
			case R.id.development_mode_submit_manual_score:
				if (grid != null && !grid.isActive()) {
					submitManualScore(puzzleFragmentActivity, grid);
				}
				return true;
			case R.id.development_mode_generate_test_helper:
				if (grid != null) {
					new TestHelperCodeGenerator(grid).logCode();
				}
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	/**
	 * Generate dummy games. A dummy game is not a real game which can be played
	 * as it is not checked on having a unique solution.
	 * 
	 * @param puzzleFragmentActivity
	 *            The activity in which context the confirmation dialog will be
	 *            shown.
	 */
	private static void generateGames(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			puzzleFragmentActivity.mGeneratePuzzleProgressDialog = new GeneratePuzzleProgressDialog(
					puzzleFragmentActivity,
					createArrayOfRandomGridGeneratingParameters(10));
			puzzleFragmentActivity.mGeneratePuzzleProgressDialog.show();
		}
	}

	private static GridGeneratingParameters[] createArrayOfRandomGridGeneratingParameters(
			int numberOfGridsToBeCreated) {
		GridGeneratingParameters[] arrayOfGridGeneratingParameters = new GridGeneratingParameters[numberOfGridsToBeCreated];
		for (int i = 0; i < numberOfGridsToBeCreated; i++) {
			arrayOfGridGeneratingParameters[i] = createRandomGridGeneratingParameters();
		}

		return arrayOfGridGeneratingParameters;
	}

	private static GridGeneratingParameters createRandomGridGeneratingParameters() {
		return new GridGeneratingParametersBuilder()
				.setGridType(getRandomGridType())
				.setHideOperators(new Random().nextBoolean())
				.setRandomPuzzleComplexity()
				.createGridGeneratingParameters();
	}

	private static GridType getRandomGridType() {
		GridType[] gridTypes = GridType.values();
		int randomIndex = new Random().nextInt(gridTypes.length);
		return gridTypes[randomIndex];
	}

	public static void generateGamesReady(
			final PuzzleFragmentActivity puzzleFragmentActivity,
			int numberOfGamesGenerated) {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			new AlertDialog.Builder(puzzleFragmentActivity)
					.setTitle("Games generated")
					.setMessage(
							Integer.toString(numberOfGamesGenerated)
									+ " games have been generated. Note that it is not "
									+ "guaranteed that those puzzles have unique solutions.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// Do nothing
								}
							})
					.show();
		}
	}

	/**
	 * Removes all preferences. After restart of the app the preferences will be
	 * initialized with default values. Saved games will not be deleted!
	 * 
	 * @param puzzleFragmentActivity
	 *            The activity in which context the preferences are have to be
	 *            reset.
	 */
	private static void resetPreferences(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			executeDeleteAllPreferences();

			// Show dialog
			new AlertDialog.Builder(puzzleFragmentActivity)
					.setMessage(
							"All preferences have been removed. After restart " + "of the app the " +
									"preferences will be " + "initialized with default values.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// Restart the activity
									//puzzleFragmentActivity.recreate();
									Intent intent = new Intent(puzzleFragmentActivity, ArchivePreferenceActivity.class);
									puzzleFragmentActivity.getBaseContext().startActivity(intent);
									puzzleFragmentActivity.finish();
								}
							})
					.show();
		}
	}

	/**
	 * Delete all data (games, database and preferences). It is provided as an
	 * easy access instead of using the button in the AppInfo dialog which
	 * involves opening the application manager.
	 * 
	 * @param puzzleFragmentActivity
	 *            The activity in which context the database and preferences are
	 *            deleted.
	 */
	private static void deleteDatabaseAndPreferences(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					puzzleFragmentActivity);
			builder.setTitle("Delete all data and preferences?")
					.setMessage(
							"All data and preferences for MathDoku+ will be deleted.")
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// Do nothing
								}
							})
					.setPositiveButton("Delete all",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									executeDeleteAllPreferences();
									executeDeleteDatabase(puzzleFragmentActivity);
									restartApp(puzzleFragmentActivity);


								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	private static void restartApp(PuzzleFragmentActivity puzzleFragmentActivity) {
		Intent intent = new Intent(puzzleFragmentActivity, PuzzleFragmentActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		puzzleFragmentActivity.getBaseContext().startActivity(intent);
	}

	/**
	 * Deletes all preferences.
	 */
	private static void executeDeleteAllPreferences() {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			Editor editor = Preferences.getInstance().mSharedPreferences.edit();
			editor.clear();
			editor.commit();
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
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			// Close database helper (this will also close the open databases).
			DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
			databaseHelper.close();
			databaseHelper.deleteDatabase(puzzleFragmentActivity.getBaseContext());

			// Also delete all preferences as some preferences relate to content
			// in database.
			executeDeleteAllPreferences();

			// Reopen the database helper.
			DatabaseHelper.getInstance(puzzleFragmentActivity);
		}
	}

	/**
	 * Checks the consistency of the database when running in development mode.
	 * 
	 * @param puzzleFragmentActivity
	 *            The activity in which context the database is checked.
	 * @return False in case the database is not consistent. True otherwise.
	 */
	public static boolean checkDatabaseConsistency(
			final PuzzleFragmentActivity puzzleFragmentActivity) {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			// While developing it regularly occurs that table definitions have
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

			if (databaseHelper.hasChangedTableDefinitions()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						puzzleFragmentActivity);
				builder.setTitle("Database is inconsistent?")
						.setMessage(
								"The database is not consistent. This is probably due "
										+ "to a table alteration (see log messages) "
										+ "without changing the revision number in "
										+ "the manifest. Either update the revision "
										+ "number in the manifest or delete the "
										+ "database.\n"
										+ "If you continue to use this this might "
										+ "result in (unhandled) exceptions.")
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										// Do nothing
									}
								})
						.setPositiveButton("Delete database",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										executeDeleteDatabase(puzzleFragmentActivity);
										restartApp(puzzleFragmentActivity);
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
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			Editor editor = Preferences.getInstance().mSharedPreferences.edit();
			editor.putBoolean(Preferences.ARCHIVE_AVAILABLE, true);
			editor.putBoolean(Preferences.STATISTICS_AVAILABLE, true);
			editor.commit();
		}
	}

	/**
	 * Ask the user to enter a score manually for the given puzzle. This dialog
	 * is intended for testing the leaderboards.
	 * 
	 * @param puzzleFragmentActivity
	 *            The puzzle fragment activity which is use to submit a score
	 *            manually.
	 * @param grid
	 *            The grid for which a score is submitted.
	 */
	public static void submitManualScore(
			final PuzzleFragmentActivity puzzleFragmentActivity, final Grid grid) {
		if (Config.mAppMode == AppMode.DEVELOPMENT
				&& puzzleFragmentActivity
						.getResources()
						.getString(R.string.app_id)
						.equals("282401107486")) {
			LayoutInflater li = LayoutInflater.from(puzzleFragmentActivity);
			View view = li.inflate(R.layout.leaderboard_score, null);
			assert view != null;

			final TextView manualLeaderboardScore = (TextView) view
					.findViewById(R.id.manual_leaderboard_score);
			assert manualLeaderboardScore != null;

			AlertDialog.Builder builder = new AlertDialog.Builder(
					puzzleFragmentActivity);
			builder.setTitle("Manually submit a score to the leaderboard")
					.setView(view)
					.setPositiveButton("Submit",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// Change real score of puzzle with manually
									// entered score
									// noinspection ConstantConditions
									long score = Long
											.valueOf(manualLeaderboardScore
													.getText()
													.toString());
									if (score > 0) {
										// Manipulate grid and statistics so it
										// can be re-submitted again.
										if (grid.isSolutionRevealed()) {
											grid.unrevealSolution();
										}
										GridStatistics gridStatistics = grid
												.getGridStatistics();
										gridStatistics.mElapsedTime = score;
										gridStatistics.mCheatPenaltyTime = 0;
										gridStatistics.mReplayCount = 0;
										puzzleFragmentActivity
												.onPuzzleFinishedListener(grid);
									}
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}
}
