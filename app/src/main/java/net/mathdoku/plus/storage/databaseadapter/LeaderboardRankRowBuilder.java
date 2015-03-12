package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.enums.PuzzleComplexity;

public class LeaderboardRankRowBuilder {
    private final int id;
    private final String leaderboardId;
    private final int gridSize;
    private final boolean operatorsHidden;
    private final PuzzleComplexity puzzleComplexity;

    private LeaderboardRankDatabaseAdapter.ScoreOrigin scoreOrigin =
            LeaderboardRankDatabaseAdapter.ScoreOrigin.NONE;

    // The unique row id of the statistics row which is the best rank for the
    // player
    private int statisticsId;

    // The raw score as stored on the Google Play Services Leaderboard
    private long rawScore;

    // The date on which the leaderboard score was submitted to Google Play
    // Services.
    private long dateSubmitted;

    // The rank on the leaderboard for this score. This is always the rank as it
    // was registered on the last updated date. Over time the rank for the exact
    // same raw score can be raised as other players score better than this
    // score. In case the player improves his best score the rank will stay the
    // same or be decreased.
    private LeaderboardRankDatabaseAdapter.RankStatus rankStatus;
    private long rank;
    private String rankDisplay;

    // Date on which the leaderboard rank was last updated. The rank will be
    // updated in case the player achieves a better score or in case the rank
    // has not been updated for a certain amount of time.
    private long dateLastUpdated;

    /**
     * Constructs a builder for a new leaderboard rank which is not yet stored in the database.
     */
    public LeaderboardRankRowBuilder(String leaderboardId, int gridSize, boolean operatorsHidden,
                                     PuzzleComplexity puzzleComplexity) {
        this(-1, leaderboardId, gridSize, operatorsHidden, puzzleComplexity);
    }

    /**
     * Constructs a builder for a new leaderboard rank which is already stored in the database.
     */
    public LeaderboardRankRowBuilder(int id, String leaderboardId, int gridSize,
                                     boolean operatorsHidden, PuzzleComplexity puzzleComplexity) {
        this.id = id;
        this.leaderboardId = leaderboardId;
        this.gridSize = gridSize;
        this.operatorsHidden = operatorsHidden;
        this.puzzleComplexity = puzzleComplexity;

        setDefaultRankingInformation();
    }

    /**
     * Constructs a builder from a leaderboard rank without changing its id.
     *
     * @param leaderboardRankRow
     *         The leaderboard rank which is to be used as basis.
     */
    public static LeaderboardRankRowBuilder from(LeaderboardRankRow leaderboardRankRow) {
        return from(leaderboardRankRow, leaderboardRankRow.getRowId());
    }

    /**
     * Constructs a builder from a leaderboard rank and sets a new id.
     *
     * @param leaderboardRankRow
     *         The leaderboard rank which is to be used as basis.
     * @param newId
     *         The id to assign to the new builder.
     */
    public static LeaderboardRankRowBuilder from(LeaderboardRankRow leaderboardRankRow, int newId) {
        LeaderboardRankRowBuilder leaderboardRankRowBuilder;

        leaderboardRankRowBuilder = new LeaderboardRankRowBuilder(newId,
                                                                  leaderboardRankRow
                                                                          .getLeaderboardId(),
                                                                  leaderboardRankRow.getGridSize(),
                                                                  leaderboardRankRow
                                                                          .isOperatorsHidden(),
                                                                  leaderboardRankRow
                                                                          .getPuzzleComplexity());
        leaderboardRankRowBuilder.scoreOrigin = leaderboardRankRow.getScoreOrigin();
        leaderboardRankRowBuilder.statisticsId = leaderboardRankRow.getStatisticsId();
        leaderboardRankRowBuilder.rawScore = leaderboardRankRow.getRawScore();
        leaderboardRankRowBuilder.dateSubmitted = leaderboardRankRow.getDateSubmitted();
        leaderboardRankRowBuilder.rankStatus = leaderboardRankRow.getRankStatus();
        leaderboardRankRowBuilder.rank = leaderboardRankRow.getRank();
        leaderboardRankRowBuilder.rankDisplay = leaderboardRankRow.getRankDisplay();
        leaderboardRankRowBuilder.dateLastUpdated = leaderboardRankRow.getDateLastUpdated();

        return leaderboardRankRowBuilder;
    }

    /**
     * Updates the score. Preferred usage is {@link LeaderboardRankRowBuilder#setScoreLocal(int,
     * long)} .
     *
     * @param scoreOrigin
     *         Origin of the score.
     * @param statisticsId
     *         The id of the statistics rows on which the score is based.
     * @param rawScore
     *         The raw score (in milliseconds) to be registered.
     * @param timestamp
     *         The date and time on which the score is achieved.
     * @return The updated leaderboard rank row builder.
     */
    public LeaderboardRankRowBuilder setScore(LeaderboardRankDatabaseAdapter.ScoreOrigin
                                                      scoreOrigin, int statisticsId,
                                              long rawScore, long timestamp) {
        validateRawScore(scoreOrigin, rawScore);
        validateScoreOrigin(scoreOrigin, statisticsId);
        this.scoreOrigin = scoreOrigin;
        this.statisticsId = statisticsId;
        this.rawScore = rawScore;
        this.dateSubmitted = timestamp;

        return this;
    }

    private void validateRawScore(LeaderboardRankDatabaseAdapter.ScoreOrigin scoreOrigin,
                                  long rawScore) {
        if (scoreOrigin != LeaderboardRankDatabaseAdapter.ScoreOrigin.NONE && rawScore <= 0) {
            throwRawScoreIllegalArgumentException();
        }
        if (scoreOrigin == LeaderboardRankDatabaseAdapter.ScoreOrigin.NONE && rawScore != 0) {
            throwRawScoreIllegalArgumentException();
        }
    }

    private void throwRawScoreIllegalArgumentException() {
        throw new IllegalArgumentException("Parameter rawScore is invalid.");
    }

    private void validateScoreOrigin(LeaderboardRankDatabaseAdapter.ScoreOrigin scoreOrigin,
                                     int statisticsId) {
        if (scoreOrigin == LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE &&
                statisticsId <= 0) {
            throwStatisticsIdIllegalArgumentException();
        }
        if (scoreOrigin != LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE &&
                statisticsId != 0) {
            throwStatisticsIdIllegalArgumentException();
        }
    }

    private void throwStatisticsIdIllegalArgumentException() {
        throw new IllegalArgumentException("Parameter statisticsId is invalid.");
    }

    private void setDefaultRankingInformation() {
        rankStatus = LeaderboardRankDatabaseAdapter.RankStatus.TO_BE_UPDATED;
        rank = 0;
        rankDisplay = null;
        dateLastUpdated = 0;
    }

    /**
     * Updates the local score.
     *
     * @param statisticsId
     *         The id of the statistics rows on which the score is based.
     * @param rawScore
     *         The raw score (in milliseconds) to be registered.
     * @return The updated leaderboard rank row builder.
     */
    public LeaderboardRankRowBuilder setScoreLocal(int statisticsId, long rawScore) {
        setScore(LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE, statisticsId, rawScore,
                 System.currentTimeMillis());
        setDefaultRankingInformation();

        return this;
    }

    /**
     * Updates with a score and rank from Google Play Services.
     *
     * @param rawScore
     *         The raw score (in milliseconds) to be registered.
     * @param rank
     *         The rank on Google Play for the score.
     * @param rankDisplay
     *         The rank description as displayed on Google Play.
     * @return The updated leaderboard rank row builder.
     */
    public LeaderboardRankRowBuilder setScoreAndRank(long rawScore, long rank, String rankDisplay) {
        long timestamp = System.currentTimeMillis();
        setScore(LeaderboardRankDatabaseAdapter.ScoreOrigin.EXTERNAL, 0, rawScore, timestamp);
        setRank(LeaderboardRankDatabaseAdapter.RankStatus.TOP_RANK_UPDATED, rank, rankDisplay,
                timestamp);

        return this;
    }

    /**
     * Updates with a rank from Google Play Services.
     *
     * @param rank
     *         The rank on Google Play for the score.
     * @param rankDisplay
     *         The rank description as displayed on Google Play.
     * @return The updated leaderboard rank row builder.
     */
    public LeaderboardRankRowBuilder setRank(long rank, String rankDisplay) {
        return setScoreAndRank(rawScore, rank, rankDisplay);
    }

    public LeaderboardRankRowBuilder setRank(LeaderboardRankDatabaseAdapter.RankStatus
                                                     rankStatus, long rank, String rankDisplay,
                                             long timestamp) {
        this.rankStatus = rankStatus;
        this.rank = rank;
        this.rankDisplay = rankDisplay;
        this.dateLastUpdated = timestamp;

        return this;
    }

    /**
     * Resets ranking information when top rank is not available.
     *
     * @return The updated leaderboard rank row builder.
     */
    public LeaderboardRankRowBuilder setRankNotAvailable() {
        setRank(LeaderboardRankDatabaseAdapter.RankStatus.TOP_RANK_NOT_AVAILABLE, 0, null,
                System.currentTimeMillis());

        return this;
    }

    public LeaderboardRankRow build() {
        return new LeaderboardRankRow(this);
    }

    public int getId() {
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

    public LeaderboardRankDatabaseAdapter.ScoreOrigin getScoreOrigin() {
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

    public LeaderboardRankDatabaseAdapter.RankStatus getRankStatus() {
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
