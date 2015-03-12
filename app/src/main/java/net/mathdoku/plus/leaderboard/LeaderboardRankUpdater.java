package net.mathdoku.plus.leaderboard;

import android.util.Log;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.ScoreOrigin;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRow;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRowBuilder;

import java.text.DateFormat;

/**
 * This class updates one single leaderboard rank in the database with the latest ranking
 * information from Google Play Services.
 */
public class LeaderboardRankUpdater {
    @SuppressWarnings("unused")
    private static final String TAG = LeaderboardRankUpdater.class.getName();

    // Remove "&& false" in following line to show debug information about
    // converting game files when running in development mode.
    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG = Config.APP_MODE == AppMode.DEVELOPMENT && false;

    // The leaderboard connector
    private final LeaderboardConnector mLeaderboardConnector;

    // Connector to database table for leaderboard ranks.
    private final LeaderboardRankDatabaseAdapter mLeaderboardRankDatabaseAdapter;

    // The next leaderboard rank to be updated
    private LeaderboardRankRow mLeaderboardRankRow;

    // The listener to be called upon completing the update of a leaderboard
    // rank.
    private final Listener mListener;

    // Status whether update has already started
    private boolean mIsUpdateStarted;

    // Counters for the number of leaderboards which have been updated.
    private int mCountLeaderboardsUpdatedWithScoreForPlayer;

    public interface Listener {
        // Called as soon as a leaderboard rank has been processed.
        void onLeaderboardRankUpdated();

        // Called as soon as all leaderboard ranks have been updated.
        void onLeaderboardRankUpdateFinished();
    }

    /**
     * Creates a new instance of {@link LeaderboardRankUpdater}.
     */
    public LeaderboardRankUpdater(LeaderboardConnector leaderboardConnector) {
        mLeaderboardConnector = leaderboardConnector;
        mListener = null;
        mLeaderboardRankDatabaseAdapter = new LeaderboardRankDatabaseAdapter();

        mLeaderboardRankRow = null;
        mIsUpdateStarted = false;
    }

    /**
     * Creates a new instance of {@link LeaderboardRankUpdater}.
     */
    public LeaderboardRankUpdater(Listener listener, LeaderboardConnector leaderboardConnector) {
        mLeaderboardConnector = leaderboardConnector;
        mListener = listener;
        mLeaderboardRankDatabaseAdapter = new LeaderboardRankDatabaseAdapter();

        mLeaderboardRankRow = null;
        mIsUpdateStarted = false;
    }

    /**
     * Updates the current leaderboard rank. Note that the updates are processed asynchronously.
     * Upon completing updating all leaderboard, the listener will be called.
     */
    public void update() {
        // In case the update is not yet started then get the first leaderboard
        // to be updated.
        if (!mIsUpdateStarted) {
            mLeaderboardRankRow = mLeaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank();
            mIsUpdateStarted = true;
        }

        if (mLeaderboardRankRow == null) {
            // Inform listener if no more leaderboard ranks need to be updated.
            if (mListener != null) {
                mListener.onLeaderboardRankUpdateFinished();
            }
        } else {
            // Update this leaderboard rank.
            if (mLeaderboardRankRow.getScoreOrigin() == ScoreOrigin.LOCAL_DATABASE &&
                    mLeaderboardRankRow.getRawScore() > 0) {
                // A local top score was already registered for this
                // leaderboard. This score is submitted which will also result
                // in updating the ranking information.
                submitLocalTopScore();

            } else {
                // For this leaderboard no local top score exists. Only the
                // ranking information needs to be updated for following
                // reasons:
                // - The user might have achieved as new top score using another
                // device.
                // - The user might have re-installed the app or manually
                // deleted all app data.
                // - Another user has achieved a score which is better than the
                // users local top score which results in another ranking
                // position of the current player. NOT IMPLEMENTED YET.
                updateRankingInformation();
            }
        }
    }

    /**
     * Submits the current top score of the leaderboard rank as a new score to Google Play Services.
     * As a result the ranking information will be updated as well.
     */
    private void submitLocalTopScore() {
        if (DEBUG) {
            Log.i(TAG,
                  "Submit score (" + mLeaderboardRankRow.getRawScore() + ") for existing " +
                          "leaderboard" + mLeaderboardConnector.getLeaderboardNameForLogging(
                          mLeaderboardRankRow.getLeaderboardId()) + " which was last submitted on" +
                          " " + DateFormat.getDateTimeInstance()
                          .format(mLeaderboardRankRow.getDateSubmitted()) + " with callback " +
                          "listener");
        }
        mLeaderboardConnector.getGamesClient()
                .submitScoreImmediate(new OnSubmitScoreImmediateListener(),
                                      mLeaderboardRankRow.getLeaderboardId(),
                                      mLeaderboardRankRow.getRawScore());
    }

    private void clearLeaderboardRank(Leaderboard leaderboard) {
        LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapter = new
                LeaderboardRankDatabaseAdapter();
        LeaderboardRankRow leaderboardRankRow = LeaderboardRankRowBuilder.from(
                leaderboardRankDatabaseAdapter.get(leaderboard.getLeaderboardId()))
                .setRankNotAvailable()
                .build();
        leaderboardRankDatabaseAdapter.update(leaderboardRankRow);
    }

    /**
     * Updates the ranking information of the current leaderboard.
     */
    private void updateRankingInformation() {
        new LeaderboardRankPlayer(mLeaderboardConnector,
                                  new UpdateRankingInformationListener()).loadCurrentPlayerRank(
                mLeaderboardRankRow.getLeaderboardId());
    }

    /**
     * Get the number of leaderboard ranks that needs to be updated.
     *
     * @return The number of leaderboard ranks that needs to be updated.
     */
    public int getCount() {
        return mLeaderboardRankDatabaseAdapter.getCountOutdatedLeaderboardRanks();
    }

    /**
     * Wrap up after finishing the update of a leaderboard rank. Listener will be informed. Next
     * leaderboard will be updated.
     */
    private void setUpdateFinished() {
        // Inform listener if no more leaderboard ranks need to be updated.
        if (mListener != null) {
            mListener.onLeaderboardRankUpdated();
        }

        // Determine next leaderboard to be updated.
        mLeaderboardRankRow = mLeaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank();

        // Start update of the next the leaderboard rank.
        update();
    }

    /**
     * Get the number of leaderboards which have been updated with the best score of the current
     * player as known by Google Play Services.
     *
     * @return Get the number of leaderboards which have been updated with the best score of the
     * current player as known by Google Play Services.
     */
    public int getCountUpdatedLeaderboardWithScoreCurrentPlayer() {
        return mCountLeaderboardsUpdatedWithScoreForPlayer;
    }

    private class UpdateRankingInformationListener implements LeaderboardRankPlayer.Listener {
        @Override
        public void onLeaderboardRankLoaded(Leaderboard leaderboard,
                                            LeaderboardScore leaderboardScore) {
            mLeaderboardConnector.updateLeaderboardRankInformation(leaderboard, leaderboardScore);
            mCountLeaderboardsUpdatedWithScoreForPlayer++;
            setUpdateFinished();
        }

        @Override
        public void onNoRankFound(Leaderboard leaderboard) {
            // The current player has never played this leaderboard
            // as no rank for this player was found on Google Play
            // Services.
            if (DEBUG) {
                Log.i(TAG,
                      "No local top score and no ranking information " + "was found for the " +
                              "current user for leaderboard " + mLeaderboardConnector
                              .getLeaderboardNameForLogging(
                              leaderboard.getLeaderboardId()) + ".");
            }
            clearLeaderboardRank(leaderboard);
            setUpdateFinished();
        }
    }

    private class OnSubmitScoreImmediateListener implements OnScoreSubmittedListener {
        @Override
        public void onScoreSubmitted(int statusCode, SubmitScoreResult submitScoreResult) {
            if (statusCode == GamesClient.STATUS_OK && submitScoreResult != null) {
                // The score was submitted and processed by Google
                // Play Services.
                if (DEBUG) {
                    Log.i(TAG,
                          "Score for leaderboard " + mLeaderboardConnector.getLeaderboardNameForLogging(
                                  submitScoreResult.getLeaderboardId()) + " has been processed by Google Play Services.");
                }

                updateRankingInformation();
            }
        }
    }
}