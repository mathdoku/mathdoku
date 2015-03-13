package net.mathdoku.plus.gridconverting;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridLoader;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;

import java.util.List;

/**
 * Converts stored data from a grid to to the latest version.
 */
public class GridConverter extends AsyncTask<Void, Void, Void> {
    @SuppressWarnings("unused")
    private static final String TAG = GridConverter.class.getName();

    // Replace Config.DisabledAlways() on following line with Config.EnabledInDevelopmentModeOnly()
    // to show debug information when running in development mode.
    private static final boolean DEBUG = Config.DisabledAlways();

    // The activity which started this task
    private PuzzleFragmentActivity mActivity;

    // Last used revision number
    private final int mCurrentVersion;

    // New revision number
    private final int mNewVersion;

    // Solving attempts to be converted
    private List<Integer> solvingAttemptIds;

    // The dialog for this task
    private ProgressDialog progressDialog;

    /**
     * Creates a new instance of {@link GridConverter}.
     *
     * @param activity
     *         The activity which started this {@link GridConverter}.
     * @param currentVersion
     *         The last version of the app which was used.
     * @param newVersion
     *         The new version to which will be upgraded.
     */
    public GridConverter(PuzzleFragmentActivity activity, int currentVersion, int newVersion) {
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
     *         The activity to which results will be sent on completion of this task.
     */
    public void attachToActivity(PuzzleFragmentActivity activity) {
        if (((Object) activity).equals(mActivity) && progressDialog != null && progressDialog.isShowing()) {
            // Casting to Object is needed due to bug in Android Studio and/or
            // IntelliJ IDEA Community edition:
            // http://youtrack.jetbrains.com/issue/IDEA-79680
            //
            // The activity is already attached to this task.
            return;
        }

        if (DEBUG) {
            Log.i(TAG, "Attach to activity");
        }

        // Remember the activity that started this task.
        this.mActivity = activity;

        // Determine the solving attempts in the database which have to be
        // converted.
        solvingAttemptIds = createSolvingAttemptDatabaseAdapter().getAllToBeConverted();

        int maxProgressCounter = solvingAttemptIds.size();
        if (maxProgressCounter > 0) {
            progressDialog = createProgressDialog(maxProgressCounter);
            progressDialog.show();
        }
    }

    // package private access for unit testing
    ProgressDialog createProgressDialog(int maxProgressCounter) {
        ProgressDialog dialog = new ProgressDialog(mActivity);

        dialog.setTitle(R.string.dialog_converting_saved_games_title);
        dialog.setMessage(mActivity.getResources()
                                  .getString(R.string.dialog_converting_saved_games_message));
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setMax(maxProgressCounter);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        return dialog;
    }

    // package private access for unit testing
    SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
        return new SolvingAttemptDatabaseAdapter();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Void doInBackground(Void... params) {
        if (mCurrentVersion < mNewVersion && solvingAttemptIds != null) {
            for (int solvingAttemptId : solvingAttemptIds) {
                Grid grid = createGridLoader().load(solvingAttemptId);
                if (grid != null) {
                    grid.saveOnAppUpgrade();

                    publishProgress();
                }
            }
        }

        return null;
    }

    // Package private access for unit testing
    GridLoader createGridLoader() {
        return new GridLoader();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        if (!isCancelled() && progressDialog != null) {
            progressDialog.incrementProgressBy(1);
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
     * Detaches the activity form the ASyn task. The progress dialog which was shown will be dismissed. The ASync task
     * however still keeps running until finished.
     */
    public void detachFromActivity() {
        if (DEBUG) {
            Log.d(TAG, "Detach from activity");
        }

        dismissProgressDialog();
        mActivity = null;
    }

    /**
     * Dismisses the progress dialog which was shown on start of this ASync task. The ASync task however still keeps
     * running until finished.
     */
    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
