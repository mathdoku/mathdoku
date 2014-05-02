package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.RankStatus;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.ScoreOrigin;

/**
 * Mapping for records in database table Grid
 */
public class LeaderboardRankRow {
	// Leaderboard id
	public String mLeaderboardId;

	// Size of the grid
	public int mGridSize;

	// Visibility of the operators
	public boolean mOperatorsHidden;

	// Complexity of the puzzle
	public PuzzleComplexity mPuzzleComplexity;

	// Origin of the score
	public ScoreOrigin mScoreOrigin;

	// The unique row id of the statistics row which is the best rank for the
	// player
	public int mStatisticsId;

	// The raw score as stored on the Google Play Services Leaderboard
	public long mRawScore;

	// The date on which the leaderboard score was submitted to Google Play
	// Services.
	public long mDateSubmitted;

	// The rank on the leaderboard for this score. This is always the rank as it
	// was registered on the last updated date. Over time the rank for the exact
	// same raw score can be raised as other players score better than this
	// score. In case the player improves his best score the rank will stay the
	// same or be decreased.
	public RankStatus mRankStatus;
	public long mRank;
	public String mRankDisplay;

	// Date on which the leaderboard rank was last updated. The rank will be
	// updated in case the player achieves a better score or in case the rank
	// has not been updated for a certain amount of time.
	public long mDateLastUpdated;
}
