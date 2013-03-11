package net.cactii.mathdoku;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.View;

/**
 * Generates a new grid while displaying a progress dialog.
 */
public class GridGenerator extends AsyncTask<Void, Integer, Void> {
	private GridView mGridView;
	private boolean mHideOperators;
	private MainActivity mActivity;
	private ProgressDialog mProgressDialog;

	/**
	 * Creates a new instance of {@link GridGenerator}.
	 * 
	 * @param activity
	 *            The activity from which this task is started.
	 * @param mGridView
	 *            The
	 * @param hideOperators
	 */
	public GridGenerator(MainActivity activity, int gridSize,
			boolean hideOperators) {
		this.mGridView = new GridView(activity);
		this.mGridView.mGridSize = gridSize;
		this.mHideOperators = hideOperators;

		attachToActivity(activity);
	}

	/**
	 * Attaches the activity to the ASync task.
	 * 
	 * @param activity
	 *            The activity to which results will be sent on completion of
	 *            this task.
	 */
	public void attachToActivity(MainActivity activity) {
		this.mActivity = activity;

		// Build the dialog
		mProgressDialog = new ProgressDialog(activity);
		mProgressDialog.setTitle(R.string.main_ui_building_puzzle_title);
		mProgressDialog.setMessage(activity.getResources().getString(
				R.string.main_ui_building_puzzle_message));
		mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		mGridView.reCreate(mHideOperators);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Void result) {
		if (this.mProgressDialog != null) {
			dismissProgressDialog();
		}
		if (mActivity != null) {
			// The task is still attached to a activity. Send new grid to
			// activity.
			mActivity.onNewGridReady(mGridView);
		} else {
			// The activity is no longer available. Store the game so it can be
			// played next time the activity is started.
			new GameFile("newGame").save(mGridView);
		}

		super.onPostExecute(result);
	}

	/**
	 * Detaches the activity form the ASyn task. The progress dialog which was
	 * shown will be dismissed. The ASync task however still keeps running until
	 * finished.
	 */
	public void detachFromActivity() {
		dismissProgressDialog();
		mActivity = null;
	}

	/**
	 * Dismisses the progress dialog which was shown on start of this ASync
	 * task. The ASync task however still keeps running until finished.
	 */
	public void dismissProgressDialog() {
		mProgressDialog.dismiss();
		mProgressDialog = null;
	}
}
