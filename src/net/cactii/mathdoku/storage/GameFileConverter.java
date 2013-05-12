package net.cactii.mathdoku.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.storage.database.SolvingAttemptData;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;
import net.cactii.mathdoku.ui.PuzzleFragmentActivity;
import net.cactii.mathdoku.util.UsageLog;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Converts all game files to the latest version.
 */
public class GameFileConverter extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "MathDoku.GameFileConverter";

	// Remove "&& false" in following line to show debug information about
	// converting game files when running in development mode.
	private static final boolean DEBUG_GRID_GAME_FILE_CONVERTER = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	// The activity which started this task
	private PuzzleFragmentActivity mActivity;

	// Last used revision number
	private int mCurrentVersion;

	// New revision number
	private int mNewVersion;

	// Path and file prefix for revision 111 and prior
	@SuppressLint("SdCardPath")
	private static final String PATH_R110 = "/data/data/net.cactii.mathdoku/";
	private static final String GAMEFILE_PREFIX_R110 = "savedgame";
	private static final String FILENAME_LAST_GAME_R111 = "last_game";
	private static final String FILENAME_SAVED_GAME_R111 = "saved_game_";
	private static final String GAMEFILE_EXTENSION_R111 = ".mgf";
	private static final String PREVIEW_EXTENSION_R111 = ".png";
	private String[] mFilenamesToBeConverted;
	private String[] mFilenamesToBeDeleted;

	// Solving attempts to be converted
	ArrayList<Integer> solvingAttemptIds;

	// The dialog for this task
	private ProgressDialog mProgressDialog;

	// Conversion results
	private ArrayList<String> mGridDefinitions;
	private int mTotalGrids;
	private int mTotalGridsSolved;
	private int mTotalGridsNotConverted;

	// Database adapter for the statistics
	StatisticsDatabaseAdapter mStatisticsDatabaseAdapter;

	/**
	 * Creates a new instance of {@link GameFileConverter}.
	 * 
	 * @param activity
	 *            The activity which started this {@link GameFileConverter}.
	 * @param currentVersion
	 *            The last version of the app which was used.
	 * @param newVersion
	 *            The new version to which will be upgraded.
	 */
	public GameFileConverter(PuzzleFragmentActivity activity,
			int currentVersion, int newVersion) {
		mActivity = activity;
		mCurrentVersion = currentVersion;
		mNewVersion = newVersion;

		// Check if we need to upgrade
		if (mCurrentVersion < mNewVersion) {
			// Attach the task to the activity activity and show progress dialog
			// if needed.
			attachToActivity(activity);
		}
	}

	/**
	 * Attaches the activity to the ASync task.
	 * 
	 * @param activity
	 *            The activity to which results will be sent on completion of
	 *            this task.
	 */
	public void attachToActivity(PuzzleFragmentActivity activity) {
		if (activity.equals(this.mActivity) && mProgressDialog != null
				&& mProgressDialog.isShowing()) {
			// The activity is already attached to this task.
			return;
		}

		if (DEBUG_GRID_GAME_FILE_CONVERTER) {
			Log.i(TAG, "Attach to activity");
		}

		// Remember the activity that started this task.
		this.mActivity = activity;

		// Determine how much (old) game files have to be moved from file to
		// database.
		mFilenamesToBeConverted = getFilesToBeConverted();
		int maxProgressCounter = mFilenamesToBeConverted.length;

		// Determine how much (old) preview files have to be deleted.
		mFilenamesToBeDeleted = getFilesToBeDeleted();
		maxProgressCounter += mFilenamesToBeDeleted.length;
		
		// Determine how many solving attempts in the database have to be
		// converted.
		if ((solvingAttemptIds = new SolvingAttemptDatabaseAdapter()
				.getAllToBeConverted()) != null) {
			maxProgressCounter += solvingAttemptIds.size();
		}

		if (maxProgressCounter > 0) {
			// Build the dialog
			mProgressDialog = new ProgressDialog(mActivity);
			mProgressDialog
					.setTitle(R.string.dialog_converting_saved_games_title);
			mProgressDialog.setMessage(mActivity.getResources().getString(
					R.string.dialog_converting_saved_games_message));
			mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setMax(maxProgressCounter);

			// Set style of dialog.
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

			// Show the dialog
			mProgressDialog.show();
		}

		// Initialize conversion results.
		mGridDefinitions = new ArrayList<String>();
		mTotalGrids = 0;
		mTotalGridsSolved = 0;
		mTotalGridsNotConverted = 0;
	}

	@Override
	protected void onPreExecute() {
		// Open database adapter
		super.onPreExecute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		if (mCurrentVersion < mNewVersion) {
			// Convert from game file to database
			if (mCurrentVersion < 271 && mFilenamesToBeConverted.length > 0) {
				for (String filename : mFilenamesToBeConverted) {
					// Load data from file and store it in a fake
					// solvingAttemptData object.
					SolvingAttemptData solvingAttemptData = new SolvingAttemptData();
					solvingAttemptData.mId = -1;
					solvingAttemptData.mGridId = -1;
					solvingAttemptData.mSavedWithRevision = -1;
					solvingAttemptData.setData(readFile(PATH_R110 + filename));

					// Load the fake solving attempt into a grid object.
					Grid grid = new Grid();
					if (grid.load(solvingAttemptData)) {
						grid.insertInDatabase();
						new File(PATH_R110 + filename).delete();
					}
					publishProgress();
				}
			}
			// Delete preview images
			if (mCurrentVersion < 299 && mFilenamesToBeDeleted.length > 0) {
				for (String filename : mFilenamesToBeDeleted) {
					new File(PATH_R110 + filename).delete();
					publishProgress();
				}
			}

			// Convert data in SolvingAttempt to newest structure.
			if (solvingAttemptIds != null) {
				for (int solvingAttemptId : solvingAttemptIds) {
					Grid grid = new Grid();
					if (grid.load(solvingAttemptId)) {
						mTotalGrids++;

						// Get definition for the grid.
						// REMOVE: stats for converted games.
						String definition = grid.toGridDefinitionString();
						if (!mGridDefinitions.contains(definition)) {
							// New definition found
							mGridDefinitions.add(definition);
						}
						if (grid.checkIfSolved()) {
							mTotalGridsSolved++;
						}

						// Save grid.
						grid.save();

						// Update progress
						publishProgress();
					} else {
						mTotalGridsNotConverted++;
					}
				}
			}
		}

		return null;
	}

	protected void onProgressUpdate(Void... values) {
		if (!isCancelled() && mProgressDialog != null) {
			mProgressDialog.incrementProgressBy(1);
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		UsageLog.getInstance().logGameFileConversion(mCurrentVersion,
				mNewVersion, mTotalGrids, mGridDefinitions.size(),
				mTotalGridsNotConverted);

		// We assume the user knows the rules as soon as two game have been
		// solved.
		Preferences.getInstance().setUserIsFamiliarWithRules(
				mTotalGridsSolved > 1);

		// Phase 1 of upgrade has been completed. Start next phase.
		if (mActivity != null) {
			mActivity.upgradePhase2_UpdatePreferences(mCurrentVersion,
					mNewVersion);
		}
		detachFromActivity();
		super.onPostExecute(result);
	}

	/**
	 * Detaches the activity form the ASyn task. The progress dialog which was
	 * shown will be dismissed. The ASync task however still keeps running until
	 * finished.
	 */
	public void detachFromActivity() {
		if (DEBUG_GRID_GAME_FILE_CONVERTER) {
			Log.d(TAG, "Detach from activity");
		}

		dismissProgressDialog();
		mActivity = null;
	}

	/**
	 * Dismisses the progress dialog which was shown on start of this ASync
	 * task. The ASync task however still keeps running until finished.
	 */
	public void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	/**
	 * Retrieve all files which have to be converted
	 * 
	 * @param path
	 *            Path to a directory.
	 * @return An array of filenames with given prefix in the given directory.
	 */
	private String[] getFilesToBeConverted() {
		String path = PATH_R110;
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.startsWith(GAMEFILE_PREFIX_R110)) {
					return true;
				} else if (name.endsWith(GAMEFILE_EXTENSION_R111)) {
					return name.startsWith(FILENAME_SAVED_GAME_R111)
							|| name.startsWith(FILENAME_LAST_GAME_R111);
				} else {
					return false;
				}
			}
		};
		File dir = new File(path);
		return (dir == null ? null : dir.list(filter));
	}

	/**
	 * Retrieve all preview images which have to be deleted
	 * 
	 * @param path
	 *            Path to a directory.
	 * @return An array of filenames in the given directory which have to be
	 *         deleted.
	 */
	private String[] getFilesToBeDeleted() {
		String path = PATH_R110;
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(PREVIEW_EXTENSION_R111)) {
					return true;
				} else {
					return false;
				}
			}
		};
		File dir = new File(path);
		return (dir == null ? null : dir.list(filter));
	}

	/**
	 * Get the (entire) content of a file.
	 * 
	 * @param filename
	 *            The name of the file to be read.
	 * @return The content of the file.
	 */
	private String readFile(String filename) {
		StringBuffer stringBuffer = new StringBuffer(256);
		String line;
		BufferedReader bufferedReader = null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(filename));
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream), 8192);

			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line + SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File '" + filename + "' not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "IO Exception when reading file '" + filename + "': "
					+ e.getMessage());
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				// Do nothing
			}
		}
		return stringBuffer.toString();
	}
}