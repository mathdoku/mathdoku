package net.cactii.mathdoku;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
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
	private static final boolean DEBUG_GRID_GAME_FILE_CONVERTER = (DevelopmentHelper.mode == Mode.DEVELOPMENT) && false;

	// The activity which started this task
	private MainActivity mActivity;

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
	private String[] mFilenamesR77;

	// File which need to be upgraded
	private ArrayList<String> mFilenames;

	// The dialog for this task
	private ProgressDialog mProgressDialog;

	// Conversion results
	private ArrayList<String> mGridSignatures;
	private int mTotalGrids;
	private int mTotalGridsSolved;
	
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
	public GameFileConverter(MainActivity activity, int currentVersion,
			int newVersion) {
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
	public void attachToActivity(MainActivity activity) {
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

		// Determine how much work must be done
		int maxProgressCounter = 0;
		if (mCurrentVersion <= 111) {
			// When converting from revision 110, both a rename and a conversion
			// has to be done on files folling R110 naming convention.
			mFilenamesR77 = getFiles(PATH_R110, GAMEFILE_PREFIX_R110);
			maxProgressCounter += 2 * mFilenamesR77.length;
		}
		// For files following current naming convention only a single pass
		// conversion is needed.
		mFilenames = GameFile.getAllGameFiles(Integer.MAX_VALUE);
		maxProgressCounter += mFilenames.size();

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
		mGridSignatures = new ArrayList<String>();
		mTotalGrids = 0;
		mTotalGridsSolved = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		if (mCurrentVersion < mNewVersion) {
			// Rename files
			if (mCurrentVersion <= 111 && mFilenamesR77.length > 0) {
				// Rename files
				for (String filename : mFilenamesR77) {
					String newFilename = null;
					if (filename.startsWith(GAMEFILE_PREFIX_R110 + "_")) {
						newFilename = PATH_R110 + FILENAME_SAVED_GAME_R111
								+ filename.substring(10)
								+ GAMEFILE_EXTENSION_R111;
					} else if (filename.startsWith("savedgame")) {
						newFilename = PATH_R110 + FILENAME_LAST_GAME_R111
								+ GAMEFILE_EXTENSION_R111;
					}

					// Rename if applicable.
					if (newFilename != null) {
						// Rename the file
						if (new File(PATH_R110 + filename).renameTo(new File(
								newFilename))) {
							// File is renamed.
							if (new File(PATH_R110 + filename).exists()) {
								new File(PATH_R110 + filename).delete();
							}
						}
					}
					publishProgress();
				}

				// Now the files have been renamed, the file list has to be
				// determined again.
				mFilenames = GameFile.getAllGameFiles(Integer.MAX_VALUE);

				// Update current version to enforce content conversion as well.
				mCurrentVersion = 111;
			}

			// Update game file contents
			if (mFilenames.size() > 0) {
				for (String filename : mFilenames) {
					// Load grid
					GameFile gameFile = new GameFile(filename);
					Grid grid = gameFile.load();
					
					// Get signature for grid. Update the number of occurrences for this signature.
					mTotalGrids++;
					String signature = grid.getSignatureString();
					if (!mGridSignatures.contains(signature)) {
						// New signature found
						mGridSignatures.add(signature);
					}
					if (grid.checkIfSolved()) {
						mTotalGridsSolved++;
					}
					
					// Save grid and publish progress
					gameFile.save(grid, true);
					publishProgress();
				}
			}
		}

		return null;
	}

	protected void onProgressUpdate(Void... values) {
		mProgressDialog.incrementProgressBy(1);
	}

	@Override
	protected void onPostExecute(Void result) {
		UsageLog.getInstance().logGameFileConversion(mCurrentVersion, mNewVersion, mTotalGrids, mGridSignatures.size());

		// We assume the user knows the rules as soon as two game have been solved.
		Preferences.getInstance().setUserIsFamiliarWithRules(mTotalGridsSolved > 1);
		
		// Phase 1 of upgrade has been completed. Start next phase.
		if (mActivity != null) {
			mActivity.upgradePhase2_createPreviewImages(mCurrentVersion,
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
	 * Retrieve all files with a give prefix from a directory with given path.
	 * 
	 * @param path
	 *            Path to a directory.
	 * @param filePrefix
	 *            Prefix of files to be returned.
	 * @return An array of filenames with given prefix in the given directory.
	 */
	private String[] getFiles(String path, final String filePrefix) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(filePrefix);
			}
		};
		File dir = new File(path);
		return (dir == null ? null : dir.list(filter));
	}
}