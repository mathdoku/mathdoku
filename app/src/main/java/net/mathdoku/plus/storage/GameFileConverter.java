package net.mathdoku.plus.storage;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridLoader;
import net.mathdoku.plus.griddefinition.GridDefinition;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts all game files to the latest version.
 */
public class GameFileConverter extends AsyncTask<Void, Void, Void> {
	private static final String TAG = GameFileConverter.class.getName();

	// Remove "&& false" in following line to show debug information about
	// converting game files when running in development mode.
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_GRID_GAME_FILE_CONVERTER = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// The activity which started this task
	private PuzzleFragmentActivity mActivity;

	// Last used revision number
	private final int mCurrentVersion;

	// New revision number
	private final int mNewVersion;

	// Solving attempts to be converted
	private List<Integer> solvingAttemptIds;

	// The dialog for this task
	private ProgressDialog mProgressDialog;

	// Conversion results
	private List<String> mGridDefinitions;

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
		if ((((Object) activity).equals(mActivity)) && mProgressDialog != null
				&& mProgressDialog.isShowing()) {
			// Casting to Object is needed due to bug in Android Studio and/or
			// IntelliJ IDEA Community edition:
			// http://youtrack.jetbrains.com/issue/IDEA-79680
			//
			// The activity is already attached to this task.
			return;
		}

		if (DEBUG_GRID_GAME_FILE_CONVERTER) {
			Log.i(TAG, "Attach to activity");
		}

		// Remember the activity that started this task.
		this.mActivity = activity;

		// Determine how many solving attempts in the database have to be
		// converted.
		int maxProgressCounter = 0;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		if (mCurrentVersion < mNewVersion) {
			// Convert data in SolvingAttempt to newest structure.
			if (solvingAttemptIds != null) {
				for (int solvingAttemptId : solvingAttemptIds) {
					Grid grid = new GridLoader().load(solvingAttemptId);
					if (grid != null) {
						// Get definition for the grid.
						String gridDefinition = GridDefinition
								.getDefinition(grid);
						if (!mGridDefinitions.contains(gridDefinition)) {
							// New definition found
							mGridDefinitions.add(gridDefinition);
						}

						// Save grid.
						grid.saveOnAppUpgrade();

						// Update progress
						publishProgress();
					}
				}
			}
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		if (!isCancelled() && mProgressDialog != null) {
			mProgressDialog.incrementProgressBy(1);
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		// Phase 1 of upgrade has been completed. Start next phase.
		if (mActivity != null) {
			mActivity.upgradePhase2(mCurrentVersion, mNewVersion);
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
	void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
}
