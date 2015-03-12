package net.mathdoku.plus.leaderboard.ui;

import android.app.ProgressDialog;

import net.mathdoku.plus.R;
import net.mathdoku.plus.leaderboard.LeaderboardConnector;
import net.mathdoku.plus.leaderboard.LeaderboardRankUpdater;
import net.mathdoku.plus.ui.base.AppFragmentActivity;

/**
 * Displays a progress dialog while updating all leaderboards.
 */
public class LeaderboardRankUpdaterProgressDialog extends ProgressDialog implements
        LeaderboardRankUpdater.Listener {
    @SuppressWarnings("unused")
    private static final String TAG = LeaderboardRankUpdaterProgressDialog.class.getName();

    // The updater for the leaderboard rank rows
    private final LeaderboardRankUpdater mLeaderboardRankUpdater;

    /**
     * Creates a new instance of {@link LeaderboardRankUpdaterProgressDialog}.
     *
     * @param appFragmentActivity
     *         The activity which started this {@link LeaderboardRankUpdaterProgressDialog}.
     */
    public LeaderboardRankUpdaterProgressDialog(AppFragmentActivity appFragmentActivity,
                                                LeaderboardConnector leaderboardConnector) {
        super(appFragmentActivity);

        // Initialize the leaderboard rank updater
        mLeaderboardRankUpdater = new LeaderboardRankUpdater(this, leaderboardConnector);

        // Build the dialog
        setTitle(R.string.dialog_leaderboard_rank_update_title);
        setIcon(android.R.drawable.ic_dialog_info);
        setIndeterminate(false);
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
        } else {
            // No leaderboards have to be updated.
            dismiss();
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
    }

    /**
     * Checks whether at least one leaderboard has been updated with the best score of a user.
     *
     * @return True in case no leaderboards have been updated (e.d. the user does not have a score
     * registered on Google Play Services).
     */
    public boolean hasNoLeaderboardUpdated() {
        return mLeaderboardRankUpdater.getCountUpdatedLeaderboardWithScoreCurrentPlayer() == 0;
    }
}