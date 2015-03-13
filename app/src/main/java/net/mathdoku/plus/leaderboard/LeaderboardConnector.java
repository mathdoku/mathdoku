package net.mathdoku.plus.leaderboard;

import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.ui.TopScoreDialog;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.ScoreOrigin;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRow;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRowBuilder;
import net.mathdoku.plus.ui.base.AppFragmentActivity;
import net.mathdoku.plus.util.Util;

public class LeaderboardConnector {
    @SuppressWarnings("unused")
    private static final String TAG = LeaderboardConnector.class.getName();

    // Replace Config.disabledAlways() on following line with Config.enabledInDevelopmentModeOnly()
    // to show debug information when running in development mode.
    public static final boolean DEBUG = Config.disabledAlways();

    // Reference to the games client of google play services.
    private final GamesClient mGamesClient;

    // Reference to the context
    private final AppFragmentActivity mAppFragmentActivity;

    // The Google+ leaderboard id's. The items in this array are kept in sync
    // with array mLeaderboardResId. The array is only initialized in DEBUG
    // mode.
    private static String[] mLeaderboardId = null;

    /**
     * Create a new instance of the leaderboard.
     *
     * @param appFragmentActivity
     *         The app fragment activity in which context the leaderboard connector is created.
     * @param gamesClient
     *         The games client which has to be used by the leaderboard connector.
     */
    public LeaderboardConnector(AppFragmentActivity appFragmentActivity, final GamesClient gamesClient) {
        mGamesClient = gamesClient;
        mAppFragmentActivity = appFragmentActivity;

        if (DEBUG) {
            // Fill the array of Google+ leaderboard id's based on the resource
            // id's. This facilitates searching for a resource id based on the
            // Google+ leaderboard id.
            Resources resources = appFragmentActivity.getResources();
            mLeaderboardId = new String[LeaderboardType.MAX_LEADERBOARDS];
            for (int i = 0; i < mLeaderboardId.length; i++) {
                mLeaderboardId[i] = resources.getString(LeaderboardType.getResId(i));
            }
        }
    }

    /**
     * Checks if the user is signed in on Google Play Services.
     *
     * @return True if the user is signed in on Google Play Services. False otherwise.
     */
    public boolean isSignedIn() {
        return mGamesClient != null && mGamesClient.isConnected();
    }

    /**
     * Submits a score to a leaderboard.
     *
     * @param gridSize
     *         The size of the grid to which the score applies.
     * @param puzzleComplexity
     *         The complexity of the grid to which the score applies.
     * @param hideOperators
     *         True in case of the grid to which the score applies had hidden operators. False otherwise.
     * @param timePlayed
     *         The elapsed time for the grid.
     */
    public void submitScore(final int gridSize, final PuzzleComplexity puzzleComplexity, final boolean hideOperators,
                            long timePlayed) {
        // Check boundaries of time played
        if (timePlayed <= 0 || timePlayed == Long.MAX_VALUE) {
            return;
        }

        // Check if already signed in
        if (!isSignedIn()) {
            return;
        }

        if (LeaderboardType.notDefinedForGridSize(gridSize)) {
            return;
        }

        // Determine the leaderboardId to which the score has to be submitted.
        String leaderboardId = mAppFragmentActivity.getResources()
                .getString(
                        LeaderboardType.getResId(LeaderboardType.getIndex(gridSize, hideOperators, puzzleComplexity)));

        if (DEBUG) {
            Log.i(TAG, "Submit new score " + timePlayed + " for leaderboard" +
                          getLeaderboardNameForLogging(leaderboardId) + " with callback listener");
        }

        // Submit the score as immediate. Upon receiving confirmation that the
        // score was processed by Google Play Services, the leaderboard rank
        // database can be updated.
        mGamesClient.submitScoreImmediate(new OnScoreSubmittedListenerImpl(gridSize, puzzleComplexity, hideOperators),
                                          leaderboardId, timePlayed);
    }

    /**
     * Updates the leaderboard rank information based on score received.
     *
     * @param leaderboard
     *         The leaderboard for which a leaderboard score is received.
     * @param leaderboardScore
     *         The leaderboard score containing the rank for the current player or if such rank does not exists, the
     *         rank of the first player. Null in case no player has played this leaderboard.
     * @return True in case a new high score was achieved. False otherwise.
     */
    boolean updateLeaderboardRankInformation(Leaderboard leaderboard, LeaderboardScore leaderboardScore) {
        if (leaderboard == null || leaderboardScore == null) {
            return false;
        }
        boolean newHighScoreAchieved = true;

        // Get the id of the leaderboard as used by Google Play Services
        String leaderboardId = leaderboard.getLeaderboardId();

        if (DEBUG) {
            Log.i(TAG, "Received rank for current Player on leaderboard" + getLeaderboardNameForLogging(leaderboardId));
        }

        LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapter = new LeaderboardRankDatabaseAdapter();

        // Get the current score registered for the leaderboard.
        LeaderboardRankRow leaderboardRankRow = leaderboardRankDatabaseAdapter.get(leaderboardId);
        if (leaderboardRankRow.getScoreOrigin() == ScoreOrigin.NONE || leaderboardRankRow.getRawScore() >
                leaderboardScore.getRawScore()) {
            // The score which was registered on Google Play Services is better
            // than the local top score. This can only happen in case the user
            // has achieved that score using another device or in case the app
            // is re-installed or the database was removed manually.
            newHighScoreAchieved = false;
            if (DEBUG) {
                if (leaderboardRankRow.getScoreOrigin() == ScoreOrigin.NONE) {
                    Log.i(TAG, "No local score does yet exist for leaderboard " +
                                  getLeaderboardNameForLogging(
                                          leaderboardId) + ". The top score as registered on Google " +
                                  "Play Services (" + leaderboardScore.getRawScore() + ") " +
                                  "will be set as the best score for this leaderboard.");
                } else {
                    Log.i(TAG, "The local top score (" + leaderboardRankRow.getRawScore() + ") is not " +
                                  "" + "as good as the top score as registered on Google Play " +
                                  "Services (" + leaderboardScore.getRawScore() + ") for " +
                                  "leaderboard" + getLeaderboardNameForLogging(
                                  leaderboardId) + ". The local top score will be updated to " +
                                  "this score.");
                }
            }

            // Update both the score and ranking information for this
            // leaderboard.
            leaderboardRankRow = LeaderboardRankRowBuilder.from(leaderboardRankRow)
                    .setScoreAndRank(leaderboardScore.getRawScore(), leaderboardScore.getRank(),
                                     leaderboardScore.getDisplayRank())
                    .build();
        } else {
            leaderboardRankRow = LeaderboardRankRowBuilder.from(leaderboardRankRow)
                    .setRank(leaderboardScore.getRank(), leaderboardScore.getDisplayRank())
                    .build();
        }
        new LeaderboardRankDatabaseAdapter().update(leaderboardRankRow);

        return newHighScoreAchieved;
    }

    /**
     * Get the name for a leaderboard based on the leaderboard id as used by Google Play Services. Should only be used
     * in DEBUG mode.
     *
     * @param leaderboardId
     *         The leaderboard id as used by Google Play Services.
     * @return The description of the leaderboard name. Null if not in DEBUG mode.
     */
    String getLeaderboardNameForLogging(String leaderboardId) {
        if (DEBUG) {
            // Check if reference list of leaderboard id is initialized.
            if (mLeaderboardId == null) {
                return "<<leaderboardId is not initialized>>";
            }

            // Search for the leaderboard id
            for (int i = 0; i < mLeaderboardId.length; i++) {
                if (mLeaderboardId[i].equals(leaderboardId)) {
                    return "[size " + LeaderboardType.getGridSize(i) + ", " + (LeaderboardType.hasHiddenOperator(
                            i) ? "hidden operators" : "visible operators") + ", " +
                            "" + LeaderboardType.getPuzzleComplexity(i)
                            .toString() + "]";
                }
            }

            // The leaderboard type was not found
            return "<<leaderboardId " + leaderboardId + " is invalid>>";
        } else {
            return null;
        }
    }

    /**
     * Get the games client used by the leaderboard connector.
     *
     * @return The games client used by the leaderboard connector.
     */
    GamesClient getGamesClient() {
        return mGamesClient;
    }

    private class OnScoreSubmittedListenerImpl implements OnScoreSubmittedListener {
        private final int gridSize;
        private final PuzzleComplexity puzzleComplexity;
        private final boolean hideOperators;

        public OnScoreSubmittedListenerImpl(int gridSize, PuzzleComplexity puzzleComplexity, boolean hideOperators) {
            this.gridSize = gridSize;
            this.puzzleComplexity = puzzleComplexity;
            this.hideOperators = hideOperators;
        }

        @Override
        public void onScoreSubmitted(int statusCode, SubmitScoreResult submitScoreResult) {
            if (statusCode == GamesClient.STATUS_OK && submitScoreResult != null) {
                // The score was submitted and processed by Google Play
                // Services.
                if (DEBUG) {
                    Log.i(TAG, "The onScoreSubmitted listener for method submitScore has " + "been " +
                                  "called successfully for leaderboard " +
                                  getLeaderboardNameForLogging(submitScoreResult.getLeaderboardId()));
                }

                // Retrieve the current rank of the player. This rank
                // information is needed to update the leaderboard rank
                // information.
                new LeaderboardRankPlayer(LeaderboardConnector.this,
                                          new OnScoreSubmittedLeaderboardRankPlayerListener()).loadCurrentPlayerRank(
                        submitScoreResult.getLeaderboardId());
            }
        }

        private class OnScoreSubmittedLeaderboardRankPlayerListener implements LeaderboardRankPlayer.Listener {
            @Override
            public void onLeaderboardRankLoaded(Leaderboard leaderboard, LeaderboardScore leaderboardScore) {
                // The leaderboard rank for the current player has been received.
                boolean newHighScoreAchieved = updateLeaderboardRankInformation(leaderboard, leaderboardScore);
                if (newHighScoreAchieved) {
                    new TopScoreDialog(mAppFragmentActivity, LeaderboardType.getIconResId(
                            LeaderboardType.getIndex(gridSize, hideOperators, puzzleComplexity)),
                                       Util.durationTimeToString(leaderboardScore.getRawScore()),
                                       leaderboardScore.getDisplayRank()).show();
                    if (DEBUG) {
                        Log.i(TAG,
                              "Leaderboard: " + leaderboard.getDisplayName() + "\n" + "Score: " + leaderboardScore
                                      .getDisplayScore() + "\n" + "Rank: " + leaderboardScore.getDisplayRank());
                    }
                }
            }

            @Override
            public void onNoRankFound(Leaderboard leaderboard) {
                // Nothing to do here. It should not be possible
                // that the player rank is not found after it
                // was just successfully submitted and received.
            }
        }
    }
}
