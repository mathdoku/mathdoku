package net.mathdoku.plus.leaderboard;

import net.mathdoku.plus.R;
import net.mathdoku.plus.leaderboard.ui.LeaderboardFragmentActivity;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;
import android.app.ProgressDialog;
import android.content.Intent;

/**
 * Displays a progress dialog while updating all leaderboards.
 */
public class LeaderboardRankUpdaterProgressDialog extends ProgressDialog
		implements LeaderboardRankUpdater.Listener {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.LeaderboardRankUpdaterProgressDialog";

	// The activity which started this task
	private final PuzzleFragmentActivity mActivity;

	// The leaderboard connector
	private final LeaderboardConnector mLeaderboardConnector;

	// The updater for the leaderboard rank rows
	private final LeaderboardRankUpdater mLeaderboardRankUpdater;

	/**
	 * Creates a new instance of {@link LeaderboardRankUpdaterProgressDialog}.
	 * 
	 * @param activity
	 *            The activity which started this
	 *            {@link LeaderboardRankUpdaterProgressDialog}.
	 */
	public LeaderboardRankUpdaterProgressDialog(
			PuzzleFragmentActivity activity,
			LeaderboardConnector leaderboardConnector) {
		super(activity);

		mActivity = activity;
		mLeaderboardConnector = leaderboardConnector;

		// Initialize new leaderboards (only after an update to a new app
		// version or after a clean install).
		initializeNewLeaderboards(activity);

		// Initialize the leaderboard rank updater
		mLeaderboardRankUpdater = new LeaderboardRankUpdater(this,
				mLeaderboardConnector);

		// Build the dialog
		setTitle(R.string.dialog_leaderboard_rank_update_title);
		setMessage(mActivity.getResources().getString(
				R.string.dialog_leaderboard_rank_update_message));
		setIcon(android.R.drawable.ic_dialog_info);
		setIndeterminate(false);
		setCancelable(true);
		setMax(mLeaderboardRankUpdater.getCount());
		setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}

	@Override
	public void show() {
		// Only display the dialog in case a leaderboard has to be updated.
		if (getMax() > 0) {
			// Display the dialog
			super.show();

			// Start updating the leaderboards.
			mLeaderboardRankUpdater.update();
		}
	}

	private void initializeNewLeaderboards(
			PuzzleFragmentActivity mPuzzleFragmentActivity) {
		// Initialize the leaderboards if needed.
		if (mPuzzleFragmentActivity.mMathDokuPreferences
				.isLeaderboardsInitialized() == false) {
			LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapter = new LeaderboardRankDatabaseAdapter();
			for (String leaderboardId : mLeaderboardConnector
					.getLeaderboardIds()) {
				// Create a leaderboard record if currently does not yet
				// exist.
				if (leaderboardRankDatabaseAdapter.get(leaderboardId) == null) {
					leaderboardRankDatabaseAdapter
							.insertInitializedLeaderboard(leaderboardId);
				}
			}
			mPuzzleFragmentActivity.mMathDokuPreferences
					.setLeaderboardsInitialized();
		}
	}

	@Override
	public void onLeaderboardRankUpdated() {
		// A leaderboard rank has been updated.
		incrementProgressBy(1);
	}

	@Override
	public void onLeaderboardRankUpdateFinished() {
		// All leaderboards have been updated. Dismiss the dialog.
		dismiss();

		// Start the leaderboards overview
		Intent intentLeaderboards = new Intent(mActivity,
				LeaderboardFragmentActivity.class);
		mActivity.startActivity(intentLeaderboards);
	}
}