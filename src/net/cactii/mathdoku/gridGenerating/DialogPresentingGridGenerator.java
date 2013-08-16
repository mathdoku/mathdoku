package net.cactii.mathdoku.gridGenerating;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.ui.PuzzleFragmentActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;

/**
 * An asynchronous task that generates a grid, displays a dialog in the process
 * and ultimately calls the onNewGridReady method of the passed activity, if the
 * task and the activity aren't detached before this may happen.
 */
public final class DialogPresentingGridGenerator extends GridGenerator {
	private static final String TAG = "MathDoku.DialogPresentingGridGenerator";

	/**
	 * The activity used to display the dialog, and to forward the generated
	 * grid to. Access level is not private, to prevent the an extra access
	 * method (see
	 * http://developer.android.com/training/articles/perf-tips.html#
	 * PackageInner).
	 */
	/* package */PuzzleFragmentActivity mPuzzleFragmentActivity;

	// The dialog for this task
	private ProgressDialog mProgressDialog;

	private static final class GridForwarder implements GridUser {
		public DialogPresentingGridGenerator mDialogPresentingGridGenerator;

		@Override
		public final void useCreatedGrid(Grid grid) {
			if (mDialogPresentingGridGenerator.mPuzzleFragmentActivity != null) {
				if (DEBUG_GRID_GENERATOR) {
					Log.d(TAG, "Send results to activity.");
				}
				// The task is still attached to a activity. Inform activity
				// about completing the new game generation. The activity will
				// deal with showing the new grid directly.
				mDialogPresentingGridGenerator.mPuzzleFragmentActivity
						.onNewGridReady(grid);
			}
		}

		@Override
		public final void onCancelGridGeneration() {
			if (mDialogPresentingGridGenerator.mPuzzleFragmentActivity != null) {
				if (DEBUG_GRID_GENERATOR) {
					Log.d(TAG,
							"Inform activity about cancelation of the grid generation.");
				}
				// The task is still attached to a activity. Inform activity
				// about completing the new game generation. The activity will
				// deal with showing the new grid directly.
				mDialogPresentingGridGenerator.mPuzzleFragmentActivity
						.onCancelGridGeneration();
			}
		}
	}

	/**
	 * Creates a new instance of {@link DialogPresentingGridGenerator}.
	 * 
	 * @param activity
	 *            The activity from which this task is started.
	 * @param gridSize
	 *            The size of the gird to be created.
	 * @param hideOperators
	 *            True in case should be solvable without using operators.
	 */
	public DialogPresentingGridGenerator(PuzzleFragmentActivity activity,
			int gridSize, boolean hideOperators,
			PuzzleComplexity puzzleComplexity, int packageVersionNumber) {
		super(gridSize, hideOperators, puzzleComplexity, packageVersionNumber,
				new GridForwarder());
		((GridForwarder) mUser).mDialogPresentingGridGenerator = this;

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
	public void attachToActivity(PuzzleFragmentActivity activity) {
		if (activity.equals(this.mPuzzleFragmentActivity)
				&& mProgressDialog != null && mProgressDialog.isShowing()) {
			// The activity is already attached to this task.
			return;
		}

		if (DEBUG_GRID_GENERATOR) {
			Log.i(TAG, "Attach to activity");
		}

		// Remember the activity that started this task.
		this.mPuzzleFragmentActivity = activity;

		buildDialog();
	}

	/**
	 * Builds and shows the dialog.
	 */
	private void buildDialog() {
		// Build the dialog
		mProgressDialog = new ProgressDialog(mPuzzleFragmentActivity);
		mProgressDialog.setTitle(R.string.dialog_building_puzzle_title);
		mProgressDialog.setMessage(mPuzzleFragmentActivity.getResources()
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
				cancel(true);
			}
		});

		// Set style of dialog.
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (mGridGeneratorOptions.numberOfGamesToGenerate > 1) {
				mProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog
						.setMax(mGridGeneratorOptions.numberOfGamesToGenerate);
			}
		}

		// Show the dialog
		mProgressDialog.show();
	}

	/**
	 * Detaches the activity form the task. The progress dialog that was shown
	 * by this task will be dismissed. The task, however, still keeps running
	 * until the grid is generated.
	 */
	public void detachFromActivity() {
		if (DEBUG_GRID_GENERATOR) {
			Log.d(TAG, "Detach from activity");
		}

		dismissProgressDialog();
		mPuzzleFragmentActivity = null;
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

	@Override
	protected void handleNewAttemptStarted(int attemptCount) {
		if (DEBUG_GRID_GENERATOR) {
			Log.i(TAG, "Puzzle generation attempt: " + attemptCount);
			publishProgress(
					DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_TITLE,
					mPuzzleFragmentActivity.getResources().getString(
							R.string.dialog_building_puzzle_title)
							+ " (attempt " + attemptCount + ")");
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (mGridGeneratorOptions.createFakeUserGameFiles) {
				mPuzzleFragmentActivity.mDialogPresentingGridGenerator = null;
				// Grids are already saved.
				DevelopmentHelper.generateGamesReady(mPuzzleFragmentActivity,
						mGridGeneratorOptions.numberOfGamesToGenerate);
				if (this.mProgressDialog != null) {
					dismissProgressDialog();
				}
				return;
			}
		}
		// Call the super implementation, which - if everything works as it
		// should - will call handleGridGenerated.
		super.onPostExecute(result);
		// Dismiss the dialog if still visible
		if (this.mProgressDialog != null) {
			dismissProgressDialog();
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		if (DEBUG_GRID_GENERATOR) {
			if (values.length >= 2 && values[0] != null && values[1] != null) {
				if (values[0]
						.equals(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_TITLE)) {
					mProgressDialog.setTitle(values[1]);
				} else if (values[0]
						.equals(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_MESSAGE)) {
					mProgressDialog.setMessage(values[1]);
				}
			}
		}
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (values.length > 0
					&& values[0] != null
					&& values[0]
							.equals(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_PROGRESS)) {
				mProgressDialog.incrementProgressBy(1);
			}
		}
		super.onProgressUpdate(values);
	}

	@Override
	public void setGridGeneratorOptions(
			GridGeneratorOptions gridGeneratorOptions) {
		super.setGridGeneratorOptions(gridGeneratorOptions);

		// Rebuild the dialog using the grid generator options.
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT
				&& mProgressDialog != null) {
			mProgressDialog.dismiss();
			buildDialog();
		}
	}
}