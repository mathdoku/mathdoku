package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.RankStatus;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.ScoreOrigin;

/**
 * Mapping for records in database table Grid
 */
public class LeaderboardRankRow {
	private final int id;

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

	public LeaderboardRankRow(
			LeaderboardRankRowBuilder leaderboardRankRowBuilder) {
		id = leaderboardRankRowBuilder.getId();
		leaderboardId = leaderboardRankRowBuilder.getLeaderboardId();
		gridSize = leaderboardRankRowBuilder.getGridSize();
		operatorsHidden = leaderboardRankRowBuilder.isOperatorsHidden();
		puzzleComplexity = leaderboardRankRowBuilder.getPuzzleComplexity();
		scoreOrigin = leaderboardRankRowBuilder.getScoreOrigin();
		statisticsId = leaderboardRankRowBuilder.getStatisticsId();
		rawScore = leaderboardRankRowBuilder.getRawScore();
		dateSubmitted = leaderboardRankRowBuilder.getDateSubmitted();
		rankStatus = leaderboardRankRowBuilder.getRankStatus();
		rank = leaderboardRankRowBuilder.getRank();
		rankDisplay = leaderboardRankRowBuilder.getRankDisplay();
		dateLastUpdated = leaderboardRankRowBuilder.getDateLastUpdated();
	}

	public int getRowId() {
		return id;
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("LeaderboardRankRow{");
		sb.append("id=").append(id);
		sb.append(", leaderboardId='").append(leaderboardId).append('\'');
		sb.append(", gridSize=").append(gridSize);
		sb.append(", operatorsHidden=").append(operatorsHidden);
		sb.append(", puzzleComplexity=").append(puzzleComplexity);
		sb.append(", scoreOrigin=").append(scoreOrigin);
		sb.append(", statisticsId=").append(statisticsId);
		sb.append(", rawScore=").append(rawScore);
		sb.append(", dateSubmitted=").append(dateSubmitted);
		sb.append(", rankStatus=").append(rankStatus);
		sb.append(", rank=").append(rank);
		sb.append(", rankDisplay='").append(rankDisplay).append('\'');
		sb.append(", dateLastUpdated=").append(dateLastUpdated);
		sb.append('}');
		return sb.toString();
	}

	@Override
	@SuppressWarnings("all")
	// Needed to suppress sonar warning on cyclomatic complexity
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof LeaderboardRankRow)) {
			return false;
		}

		LeaderboardRankRow that = (LeaderboardRankRow) o;

		if (id != that.id) {
			return false;
		}
		if (dateLastUpdated != that.dateLastUpdated) {
			return false;
		}
		if (dateSubmitted != that.dateSubmitted) {
			return false;
		}
		if (gridSize != that.gridSize) {
			return false;
		}
		if (operatorsHidden != that.operatorsHidden) {
			return false;
		}
		if (rank != that.rank) {
			return false;
		}
		if (rawScore != that.rawScore) {
			return false;
		}
		if (statisticsId != that.statisticsId) {
			return false;
		}
		if (leaderboardId != null ? !leaderboardId.equals(that.leaderboardId)
				: that.leaderboardId != null) {
			return false;
		}
		if (puzzleComplexity != that.puzzleComplexity) {
			return false;
		}
		if (rankDisplay != null ? !rankDisplay.equals(that.rankDisplay)
				: that.rankDisplay != null) {
			return false;
		}
		if (rankStatus != that.rankStatus) {
			return false;
		}
		if (scoreOrigin != that.scoreOrigin) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result
				+ (leaderboardId != null ? leaderboardId.hashCode() : 0);
		result = 31 * result + gridSize;
		result = 31 * result + (operatorsHidden ? 1 : 0);
		result = 31 * result
				+ (puzzleComplexity != null ? puzzleComplexity.hashCode() : 0);
		result = 31 * result
				+ (scoreOrigin != null ? scoreOrigin.hashCode() : 0);
		result = 31 * result + statisticsId;
		result = 31 * result + (int) (rawScore ^ (rawScore >>> 32));
		result = 31 * result + (int) (dateSubmitted ^ (dateSubmitted >>> 32));
		result = 31 * result + (rankStatus != null ? rankStatus.hashCode() : 0);
		result = 31 * result + (int) (rank ^ (rank >>> 32));
		result = 31 * result
				+ (rankDisplay != null ? rankDisplay.hashCode() : 0);
		result = 31 * result
				+ (int) (dateLastUpdated ^ (dateLastUpdated >>> 32));
		return result;
	}
}
