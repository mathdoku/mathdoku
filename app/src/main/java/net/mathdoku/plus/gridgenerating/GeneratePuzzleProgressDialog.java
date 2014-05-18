package net.mathdoku.plus.gridgenerating;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;

import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.developmenthelper.DevelopmentHelper;
import net.mathdoku.plus.gridgenerating.iface.GridGeneratorAsyncTaskIface;
import net.mathdoku.plus.gridgenerating.iface.GridGeneratorAsyncTaskListenerIface;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;

import java.util.List;

/**
 * This class presents a progress dialog while the grid is generated
 * asynchronously. The onNewGridReady method of the passed activity will be
 * called as the grid has been generated as long as the activity is still
 * attached to this class.
 */
public class GeneratePuzzleProgressDialog implements
		GridGeneratorAsyncTaskListenerIface {
	@SuppressWarnings("unused")
	private static final String TAG = GeneratePuzzleProgressDialog.class
			.getName();

	// Remove "&& false" in following line to show debug information.
	@SuppressWarnings("PointlessBooleanExpression")
	static final boolean DEBUG = Config.APP_MODE == Config.AppMode.DEVELOPMENT && false;

	private int numberOfGamesToGenerate;
	private GridGeneratorAsyncTaskIface gridGeneratorAsyncTask;

	/**
	 * The activity used to display the dialog, and to forward the generated
	 * grid to.
	 */
	private PuzzleFragmentActivity mPuzzleFragmentActivity;

	// The dialog for this task. This class cannot simply extend class
	// ProgressDialog. In case of a configuration change (e.g. device rotates)
	// the Activity which creates this dialog is killed and a new activity is
	// created.
	private ProgressDialog mProgressDialog;

	/**
	 * Creates a new instance of {@link GeneratePuzzleProgressDialog}.
	 * 
	 * @param activity
	 *            The activity from which this task is started.
	 * @param gridGeneratingParameters
	 *            The parameters to be used to create the new grid. Only in
	 *            development mode an array of grid generating parameters is
	 *            accepted as well.
	 */
	public GeneratePuzzleProgressDialog(PuzzleFragmentActivity activity,
			GridGeneratingParameters... gridGeneratingParameters) {
		gridGeneratorAsyncTask = createGridGeneratorAsyncTask();
		gridGeneratorAsyncTask.createGrids(gridGeneratingParameters);
		numberOfGamesToGenerate = gridGeneratingParameters.length;

		// Attach the task to the activity activity and show progress dialog if
		// needed.
		attachToActivity(activity);
	}

	/**
	 * Attaches the activity to the ASync task.
	 * 
	 * @param activity
	 *            The activity to which results will be sent on completion of
	 *            this task.
	 */
	public GeneratePuzzleProgressDialog attachToActivity(
			PuzzleFragmentActivity activity) {
		if (((Object) activity).equals(mPuzzleFragmentActivity)
				&& mProgressDialog != null && mProgressDialog.isShowing()) {
			// Casting to Object is needed due to bug in Android Studio and/or
			// IntelliJ IDEA Community edition:
			// http://youtrack.jetbrains.com/issue/IDEA-79680
			//
			// The activity is already attached to this task.
			return this;
		}

		if (DEBUG) {
			Log.i(TAG, "Attach to activity");
		}

		// Remember the activity that started this task.
		mPuzzleFragmentActivity = activity;

		mProgressDialog = buildDialog();

		return this;
	}

	public void show() {
		if (mProgressDialog != null) {
			mProgressDialog.show();
		}
	}

	private ProgressDialog buildDialog() {
		ProgressDialog progressDialog = createProgressDialog();
		progressDialog.setTitle(R.string.dialog_building_puzzle_title);
		progressDialog.setMessage(mPuzzleFragmentActivity
				.getResources()
				.getString(R.string.dialog_building_puzzle_message));
		progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		progressDialog.setIndeterminate(false);

		// Allow canceling via back button but not by touching outside the
		// dialog
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				gridGeneratorAsyncTask.cancel();
			}
		});

		// Set style of dialog.
		if (Config.APP_MODE == AppMode.DEVELOPMENT
				&& numberOfGamesToGenerate > 1) {
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(numberOfGamesToGenerate);
		} else {
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}

		return progressDialog;
	}

	/**
	 * Detaches the activity form the task. The progress dialog that was shown
	 * by this task will be dismissed. The task, however, still keeps running
	 * until the grid is generated.
	 */
	public void detachFromActivity() {
		if (DEBUG) {
			Log.d(TAG, "Detach from activity");
		}

		dismissProgressDialog();
		mPuzzleFragmentActivity = null;
	}

	private void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	private void setTitle(final String title) {
		if (mPuzzleFragmentActivity != null) {
			mPuzzleFragmentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mProgressDialog != null) {
						mProgressDialog.setTitle(title);
					}

				}
			});
		}
	}

	private void setMessage(final String message) {
		if (mPuzzleFragmentActivity != null) {
			mPuzzleFragmentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mProgressDialog != null) {
						mProgressDialog.setMessage(message);
					}

				}
			});
		}
	}

	@Override
	public void onFinishGridGenerator(List<Grid> grids) {
		dismissProgressDialog();
		if (grids == null || grids.isEmpty()) {
			return;
		}
		if (grids.size() == 1 && mPuzzleFragmentActivity != null) {
			if (DEBUG) {
				Log.d(TAG, "Send results to activity.");
			}
			// The task is still attached to a activity. Inform
			// activity
			// about completing the new game generation. The
			// activity will
			// deal with showing the new grid directly.
			mPuzzleFragmentActivity.onNewGridReady(grids.get(0));
		} else if (grids.size() > 1) {
			DevelopmentHelper.generateGamesReady(mPuzzleFragmentActivity,
					grids.size());
		}
	}

	@Override
	public void onCancelGridGeneratorAsyncTask() {
		if (mPuzzleFragmentActivity != null) {
			if (DEBUG) {
				Log
						.d(TAG,
								"Inform activity about cancellation of the grid generation.");
			}
			// The task is still attached to a activity. Inform activity
			// about completing the new game generation. The activity will
			// deal with showing the new grid directly.
			mPuzzleFragmentActivity.onCancelGridGeneration();
		}
		dismissProgressDialog();
	}

	@Override
	public void onHighLevelProgressUpdate(String text) {
		setTitle(text);
	}

	@Override
	public void onDetailLevelProgressDetail(String text) {
		setMessage(text);
	}

	@Override
	public void onGridGenerated() {
		mProgressDialog.incrementProgressBy(1);
	}

	GridGeneratorAsyncTaskIface createGridGeneratorAsyncTask() {
		return new GridGeneratorAsyncTask(this);
	}

	ProgressDialog createProgressDialog() {
		return new ProgressDialog(mPuzzleFragmentActivity);
	}
}
