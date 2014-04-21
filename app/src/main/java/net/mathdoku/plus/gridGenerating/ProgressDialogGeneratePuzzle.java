package net.mathdoku.plus.gridgenerating;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;

import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.developmenthelper.DevelopmentHelper;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;

import java.util.List;

/**
 * This class presents a progress dialog while the grid is generated
 * asynchronously. The onNewGridReady method of the passed activity will be
 * called as the grid has been generated as long as the activity is still
 * attached to this class.
 */
public final class ProgressDialogGeneratePuzzle {
	private static final String TAG = ProgressDialogGeneratePuzzle.class
			.getName();

	// Remove "&& false" in following line to show debug information.
	@SuppressWarnings("PointlessBooleanExpression")
	static final boolean DEBUG = Config.mAppMode == Config.AppMode.DEVELOPMENT && false;

	private int numberOfGamesToGenerate;
	private GridGeneratorAsyncTask gridGeneratorAsyncTask;
	private GridGeneratorListener gridGeneratorListener;

	/**
	 * The activity used to display the dialog, and to forward the generated
	 * grid to. Access level is not private, to prevent the an extra access
	 * method (see
	 * http://developer.android.com/training/articles/perf-tips.html#
	 * PackageInner).
	 */
	/* package */
	@SuppressWarnings("WeakerAccess")
	PuzzleFragmentActivity mPuzzleFragmentActivity;

	// The dialog for this task
	private ProgressDialog mProgressDialog;

	private static final class GridGeneratorListener implements
			GridGeneratorAsyncTask.Listener {
		public ProgressDialogGeneratePuzzle mProgressDialogGeneratePuzzle;

		public GridGeneratorListener(
				ProgressDialogGeneratePuzzle progressDialogGeneratePuzzle) {
			this.mProgressDialogGeneratePuzzle = progressDialogGeneratePuzzle;
		}

		@Override
		public void onFinishGridGenerator(List<Grid> grids) {
			mProgressDialogGeneratePuzzle.dismissProgressDialog();
			if (grids == null || grids.isEmpty()) {
				return;
			}
			if (grids.size() == 1
					&& mProgressDialogGeneratePuzzle.mPuzzleFragmentActivity != null) {
				if (DEBUG) {
					Log.d(TAG, "Send results to activity.");
				}
				// The task is still attached to a activity. Inform
				// activity
				// about completing the new game generation. The
				// activity will
				// deal with showing the new grid directly.
				mProgressDialogGeneratePuzzle.mPuzzleFragmentActivity
						.onNewGridReady(grids.get(0));
			} else if (grids.size() > 1) {
				DevelopmentHelper.generateGamesReady(
						mProgressDialogGeneratePuzzle.mPuzzleFragmentActivity,
						grids.size());
			}
		}

		@Override
		public final void onCancelGridGeneratorAsyncTask() {
			if (mProgressDialogGeneratePuzzle.mPuzzleFragmentActivity != null) {
				if (DEBUG) {
					Log
							.d(TAG,
									"Inform activity about cancellation of the grid generation.");
				}
				// The task is still attached to a activity. Inform activity
				// about completing the new game generation. The activity will
				// deal with showing the new grid directly.
				mProgressDialogGeneratePuzzle.mPuzzleFragmentActivity
						.onCancelGridGeneration();
			}
			this.mProgressDialogGeneratePuzzle.dismissProgressDialog();
		}

		@Override
		public void onHighLevelProgressUpdate(String text) {
			mProgressDialogGeneratePuzzle.setTitle(text);
		}

		@Override
		public void onDetailLevelProgressDetail(String text) {
			mProgressDialogGeneratePuzzle.setMessage(text);
		}

		@Override
		public void onGridGenerated() {
			mProgressDialogGeneratePuzzle.onGridGenerated();
		}
	}

	/**
	 * Creates a new instance of {@link ProgressDialogGeneratePuzzle}.
	 * 
	 * @param activity
	 *            The activity from which this task is started.
	 * @param gridGeneratingParameters
	 *            The parameters to be used to create the new grid. Only in
	 *            development mode an array of grid generating parameters is
	 *            accepted as well.
	 */
	public ProgressDialogGeneratePuzzle(PuzzleFragmentActivity activity,
			GridGeneratingParameters... gridGeneratingParameters) {
		gridGeneratorListener = new GridGeneratorListener(this);
		gridGeneratorAsyncTask = new GridGeneratorAsyncTask(
				gridGeneratorListener);
		gridGeneratorAsyncTask.execute(gridGeneratingParameters);
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
	public ProgressDialogGeneratePuzzle attachToActivity(
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

		buildDialog();

		return this;
	}

	public void show() {
		if (mProgressDialog != null) {
			mProgressDialog.show();
		}
	}

	/**
	 * Builds and shows the dialog.
	 */
	private void buildDialog() {
		// Build the dialog
		mProgressDialog = new ProgressDialog(mPuzzleFragmentActivity);
		mProgressDialog.setTitle(R.string.dialog_building_puzzle_title);
		mProgressDialog.setMessage(mPuzzleFragmentActivity
				.getResources()
				.getString(R.string.dialog_building_puzzle_message));
		mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
		mProgressDialog.setIndeterminate(false);

		// Allow canceling via back button but not by touching outside the
		// dialog
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// Cancel the async task which generates the grid
				gridGeneratorAsyncTask.cancel(true);
			}
		});

		// Set style of dialog.
		if (Config.mAppMode == AppMode.DEVELOPMENT
				&& numberOfGamesToGenerate > 1) {
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setMax(numberOfGamesToGenerate);
		} else {
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
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

	void setTitle(final String title) {
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

	void setMessage(final String message) {
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

	void onGridGenerated() {
		mProgressDialog.incrementProgressBy(1);
	}
}
