package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.RankStatus;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.ScoreOrigin;

/**
 * Mapping for records in database table Grid
 */
public class LeaderboardRankRow {
	private final String leaderboardId;
	private final int gridSize;
	private final boolean operatorsHidden;
	private final PuzzleComplexity puzzleComplexity;
	private final ScoreOrigin scoreOrigin;

	// The unique row id of the statistics row which is the best rank for the
	// player
	private final int statisticsId;

	// The raw score as stored on the Google Play Services Leaderboard
	private final long rawScore;

	// The date on which the leaderboard score was submitted to Google Play
	// Services.
	private final long dateSubmitted;

	// The rank on the leaderboard for this score. This is always the rank as it
	// was registered on the last updated date. Over time the rank for the exact
	// same raw score can be raised as other players score better than this
	// score. In case the player improves his best score the rank will stay the
	// same or be decreased.
	private final RankStatus rankStatus;
	private final long rank;
	private final String rankDisplay;

	// Date on which the leaderboard rank was last updated. The rank will be
	// updated in case the player achieves a better score or in case the rank
	// has not been updated for a certain amount of time.
	private final long dateLastUpdated;

	public LeaderboardRankRow(String leaderboardId, int gridSize,
			boolean operatorsHidden, PuzzleComplexity puzzleComplexity,
			ScoreOrigin scoreOrigin, int statisticsId, long rawScore,
			long dateSubmitted, RankStatus rankStatus, long rank,
			String rankDisplay, long dateLastUpdated) {
		this.leaderboardId = leaderboardId;
		this.gridSize = gridSize;
		this.operatorsHidden = operatorsHidden;
		this.puzzleComplexity = puzzleComplexity;
		this.scoreOrigin = scoreOrigin;
		this.statisticsId = statisticsId;
		this.rawScore = rawScore;
		this.dateSubmitted = dateSubmitted;
		this.rankStatus = rankStatus;
		this.rank = rank;
		this.rankDisplay = rankDisplay;
		this.dateLastUpdated = dateLastUpdated;
	}

	public String getLeaderboardId() {
		return leaderboardId;
	}

	public int getGridSize() {
		return gridSize;
	}

	public boolean isOperatorsHidden() {
		return operatorsHidden;
	}

	public PuzzleComplexity getPuzzleComplexity() {
		return puzzleComplexity;
	}

	public ScoreOrigin getScoreOrigin() {
		return scoreOrigin;
	}

	public int getStatisticsId() {
		return statisticsId;
	}

	public long getRawScore() {
		return rawScore;
	}

	public long getDateSubmitted() {
		return dateSubmitted;
	}

	public RankStatus getRankStatus() {
		return rankStatus;
	}

	public long getRank() {
		return rank;
	}

	public String getRankDisplay() {
		return rankDisplay;
	}

	public long getDateLastUpdated() {
		return dateLastUpdated;
	}
}
