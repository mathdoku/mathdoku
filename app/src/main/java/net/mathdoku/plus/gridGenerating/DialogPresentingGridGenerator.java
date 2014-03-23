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
public final class DialogPresentingGridGenerator {
	private static final String TAG = DialogPresentingGridGenerator.class
			.getName();

	private int numberOfGamesToGenerate;
	private GridGenerator gridGenerator;
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
			GridGenerator.Listener {
		public DialogPresentingGridGenerator mDialogPresentingGridGenerator;

		public GridGeneratorListener(
				DialogPresentingGridGenerator dialogPresentingGridGenerator) {
			this.mDialogPresentingGridGenerator = dialogPresentingGridGenerator;
		}

		@Override
		public void onFinishGridGenerator(List<Grid> grids) {
			mDialogPresentingGridGenerator.dismissProgressDialog();
			if (grids == null || grids.isEmpty()) {
				return;
			}
			if (grids.size() == 1
					&& mDialogPresentingGridGenerator.mPuzzleFragmentActivity != null) {
				if (GridGenerator.DEBUG_GRID_GENERATOR) {
					Log.d(TAG, "Send results to activity.");
				}
				// The task is still attached to a activity. Inform
				// activity
				// about completing the new game generation. The
				// activity will
				// deal with showing the new grid directly.
				mDialogPresentingGridGenerator.mPuzzleFragmentActivity
						.onNewGridReady(grids.get(0));
			} else if (grids.size() > 1) {
				DevelopmentHelper.generateGamesReady(
						mDialogPresentingGridGenerator.mPuzzleFragmentActivity,
						grids.size());
			}
		}

		@Override
		public final void onCancelGridGenerator() {
			if (mDialogPresentingGridGenerator.mPuzzleFragmentActivity != null) {
				if (GridGenerator.DEBUG_GRID_GENERATOR) {
					Log
							.d(TAG,
									"Inform activity about cancellation of the grid generation.");
				}
				// The task is still attached to a activity. Inform activity
				// about completing the new game generation. The activity will
				// deal with showing the new grid directly.
				mDialogPresentingGridGenerator.mPuzzleFragmentActivity
						.onCancelGridGeneration();
			}
		}

		@Override
		public void updateProgressHighLevel(String text) {
			mDialogPresentingGridGenerator.setTitle(text);
		}

		@Override
		public void updateProgressDetailLevel(String text) {
			mDialogPresentingGridGenerator.setMessage(text);
		}

		@Override
		public void onGridGenerated() {
			mDialogPresentingGridGenerator.onGridGenerated();
		}
	}

	/**
	 * Creates a new instance of {@link DialogPresentingGridGenerator}.
	 * 
	 * @param activity
	 *            The activity from which this task is started.
	 * @param gridGeneratingParameters
	 *            The parameters to be used to create the new grid. Only in
	 *            development mode an array of grid generating parameters is
	 *            accepted as well.
	 */
	public DialogPresentingGridGenerator(PuzzleFragmentActivity activity,
			GridGeneratingParameters... gridGeneratingParameters) {
		gridGeneratorListener = new GridGeneratorListener(this);
		gridGenerator = new GridGenerator(gridGeneratorListener,
				gridGeneratingParameters);
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
	public void attachToActivity(PuzzleFragmentActivity activity) {
		if (((Object) activity).equals(mPuzzleFragmentActivity)
				&& mProgressDialog != null && mProgressDialog.isShowing()) {
			// Casting to Object is needed due to bug in Android Studio and/or
			// IntelliJ IDEA Community edition:
			// http://youtrack.jetbrains.com/issue/IDEA-79680
			//
			// The activity is already attached to this task.
			return;
		}

		if (GridGenerator.DEBUG_GRID_GENERATOR) {
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
				gridGenerator.cancel(true);
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

		// Show the dialog
		mProgressDialog.show();
	}

	public void generate() {
		if (gridGenerator != null) {
			gridGenerator.execute();
		}
	}

	/**
	 * Detaches the activity form the task. The progress dialog that was shown
	 * by this task will be dismissed. The task, however, still keeps running
	 * until the grid is generated.
	 */
	public void detachFromActivity() {
		if (GridGenerator.DEBUG_GRID_GENERATOR) {
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
