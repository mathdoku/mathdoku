package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.enums.PuzzleComplexity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class LeaderboardRankRowTest {
	int idFirstLeaderboardIdInEmptyDatabaseInitialValue = 1;
	String leaderboardIdInitialValue = "*** SOME LEADERBOARD ID ***";
	int gridSizeInitialValue = 7;
	boolean operatorsHiddenInitialValue = true;
	PuzzleComplexity puzzleComplexityInitialValue = PuzzleComplexity.DIFFICULT;
	LeaderboardRankDatabaseAdapter.ScoreOrigin scoreOriginInitialValue = LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE;
	int statisticsIdInitialValue = 67;
	long rawScoreInitialValue = 18293;
	long dateSubmittedInitialValue = 192030434L;
	LeaderboardRankDatabaseAdapter.RankStatus rankStatusInitialValue = LeaderboardRankDatabaseAdapter.RankStatus.TOP_RANK_UPDATED;
	long rankInitialValue = 12;
	String rankDisplayInitialValue = "12th";
	long dateLastUpdatedInitialValue = 199999999L;
	long systemTimeStartOfTest;

	@Before
	public void setUp() {
		systemTimeStartOfTest = System.currentTimeMillis();
	}

	@Test
	public void createInitial() throws Exception {
		LeaderboardRankRow leaderboardRankRow = new LeaderboardRankRowBuilder(
				leaderboardIdInitialValue, gridSizeInitialValue,
				operatorsHiddenInitialValue, puzzleComplexityInitialValue)
				.build();
		assertThatBasicFieldsHaveInitialValues(leaderboardRankRow);
		assertThatScoreOriginHasDefaultValue(leaderboardRankRow);
		assertThatStatisticsIdHasDefaultValue(leaderboardRankRow);
		assertThatRawScoreHasDefaultValue(leaderboardRankRow);
		assertThatDateSubmittedHasDefaultValue(leaderboardRankRow);
		assertThatRankStatusHasDefaultValue(leaderboardRankRow);
		assertThatRankHasDefaultValue(leaderboardRankRow);
		assertThatRankDisplayHasDefaultValue(leaderboardRankRow);
		assertThatDateLastUpdatedHasDefaultValue(leaderboardRankRow);
	}

	private void assertThatBasicFieldsHaveInitialValues(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getLeaderboardId(),
				is(leaderboardIdInitialValue));
		assertThat(leaderboardRankRow.getGridSize(), is(gridSizeInitialValue));
		assertThat(leaderboardRankRow.isOperatorsHidden(),
				is(operatorsHiddenInitialValue));
		assertThat(leaderboardRankRow.getPuzzleComplexity(),
				is(puzzleComplexityInitialValue));
	}

	private void assertThatScoreOriginHasDefaultValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getScoreOrigin(),
				is(LeaderboardRankDatabaseAdapter.ScoreOrigin.NONE));
	}

	private void assertThatStatisticsIdHasDefaultValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getStatisticsId(), is(0));
	}

	private void assertThatRawScoreHasDefaultValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getRawScore(), is(0L));
	}

	private void assertThatDateSubmittedHasDefaultValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getDateSubmitted(), is(0L));
	}

	private void assertThatRankStatusHasDefaultValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getRankStatus(),
				is(LeaderboardRankDatabaseAdapter.RankStatus.TO_BE_UPDATED));
	}

	private void assertThatRankHasDefaultValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getRank(), is(0L));
	}

	private void assertThatRankDisplayHasDefaultValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getRankDisplay(), is(nullValue()));
	}

	private void assertThatDateLastUpdatedHasDefaultValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getDateLastUpdated(), is(0L));
	}

	@Test
	public void createWithLocalScore() throws Exception {
		int updatedStatisticsId = statisticsIdInitialValue + 23;
		long updatedRawScore = rawScoreInitialValue - 17;
		LeaderboardRankRow updatedLeaderboardRank = LeaderboardRankRowBuilder
				.from(createLeaderboardRankRowWithInitialValues())
				.setScoreLocal(updatedStatisticsId, updatedRawScore)
				.build();

		assertThatBasicFieldsHaveInitialValues(updatedLeaderboardRank);
		assertThat(updatedLeaderboardRank.getScoreOrigin(),
				is(LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE));
		assertThat(updatedLeaderboardRank.getStatisticsId(),
				is(updatedStatisticsId));
		assertThat(updatedLeaderboardRank.getRawScore(), is(updatedRawScore));
		assertThatDateSubmittedIsLaterThan(updatedLeaderboardRank,
				systemTimeStartOfTest);
		assertThatRankStatusHasDefaultValue(updatedLeaderboardRank);
		assertThatRankHasDefaultValue(updatedLeaderboardRank);
		assertThatRankDisplayHasDefaultValue(updatedLeaderboardRank);
		assertThatDateLastUpdatedHasDefaultValue(updatedLeaderboardRank);
	}

	private LeaderboardRankRow createLeaderboardRankRowWithInitialValues() {
		return new LeaderboardRankRowBuilder(
				idFirstLeaderboardIdInEmptyDatabaseInitialValue,
				leaderboardIdInitialValue, gridSizeInitialValue,
				operatorsHiddenInitialValue, puzzleComplexityInitialValue)
				.setScore(scoreOriginInitialValue, statisticsIdInitialValue,
						rawScoreInitialValue, dateSubmittedInitialValue)
				.setRank(rankStatusInitialValue, rankInitialValue,
						rankDisplayInitialValue, dateLastUpdatedInitialValue)
				.build();
	}

	private void assertThatDateSubmittedIsLaterThan(
			LeaderboardRankRow leaderboardRankRow, long systemTime) {
		assertThat(leaderboardRankRow.getDateSubmitted() >= systemTime,
				is(true));
	}

	@Test
	public void createWithNewGooglePlayScore() throws Exception {
		long updatedRawScore = rawScoreInitialValue - 17;
		long updatedRank = rankInitialValue - 1;
		String updatedRankDisplay = "11th";
		LeaderboardRankRow updatedLeaderboardRank = LeaderboardRankRowBuilder
				.from(createLeaderboardRankRowWithInitialValues())
				.setScoreAndRank(updatedRawScore, updatedRank,
						updatedRankDisplay)
				.build();

		assertThatBasicFieldsHaveInitialValues(updatedLeaderboardRank);
		assertThat(updatedLeaderboardRank.getScoreOrigin(),
				is(LeaderboardRankDatabaseAdapter.ScoreOrigin.EXTERNAL));
		assertThatStatisticsIdHasDefaultValue(updatedLeaderboardRank);
		assertThat(updatedLeaderboardRank.getRawScore(), is(updatedRawScore));
		assertThatDateSubmittedIsLaterThan(updatedLeaderboardRank,
				systemTimeStartOfTest);
		assertThat(updatedLeaderboardRank.getRankStatus(),
				is(LeaderboardRankDatabaseAdapter.RankStatus.TOP_RANK_UPDATED));
		assertThat(updatedLeaderboardRank.getRank(), is(updatedRank));
		assertThat(updatedLeaderboardRank.getRankDisplay(),
				is(updatedRankDisplay));
		assertThatDateLastUpdatedIsLaterThan(updatedLeaderboardRank,
				systemTimeStartOfTest);
	}

	private void assertThatDateLastUpdatedIsLaterThan(
			LeaderboardRankRow leaderboardRankRow, long systemTime) {
		assertThat(leaderboardRankRow.getDateLastUpdated() >= systemTime,
				is(true));
	}

	@Test
	public void createWithNewGooglePlayRank() throws Exception {
		long updatedRank = rankInitialValue - 1;
		String updatedRankDisplay = "***" + rankDisplayInitialValue + "***";
		LeaderboardRankRow updatedLeaderboardRank = LeaderboardRankRowBuilder
				.from(createLeaderboardRankRowWithInitialValues())
				.setRank(updatedRank, updatedRankDisplay)
				.build();

		assertThatBasicFieldsHaveInitialValues(updatedLeaderboardRank);
		assertThat(updatedLeaderboardRank.getScoreOrigin(),
				is(LeaderboardRankDatabaseAdapter.ScoreOrigin.EXTERNAL));
		assertThatStatisticsIdHasDefaultValue(updatedLeaderboardRank);
		assertThatRawScoreHasInitialValue(updatedLeaderboardRank);
		assertThatDateSubmittedIsLaterThan(updatedLeaderboardRank,
				systemTimeStartOfTest);
		assertThat(updatedLeaderboardRank.getRankStatus(),
				is(LeaderboardRankDatabaseAdapter.RankStatus.TOP_RANK_UPDATED));
		assertThat(updatedLeaderboardRank.getRank(), is(updatedRank));
		assertThat(updatedLeaderboardRank.getRankDisplay(),
				is(updatedRankDisplay));
		assertThatDateLastUpdatedIsLaterThan(updatedLeaderboardRank,
				systemTimeStartOfTest);
	}

	private void assertThatRawScoreHasInitialValue(
			LeaderboardRankRow leaderboardRank) {
		assertThat(leaderboardRank.getRawScore(), is(rawScoreInitialValue));
	}

	@Test
	public void createWithGooglePlayRankNotAvailable() throws Exception {
		LeaderboardRankRow updatedLeaderboardRank = LeaderboardRankRowBuilder
				.from(createLeaderboardRankRowWithInitialValues())
				.setRankNotAvailable()
				.build();

		assertThatBasicFieldsHaveInitialValues(updatedLeaderboardRank);
		assertThatScoreOriginHasInitialValue(updatedLeaderboardRank);
		assertThatStatisticsIdHasInitialValue(updatedLeaderboardRank);
		assertThatRawScoreHasInitialValue(updatedLeaderboardRank);
		assertThatDateSubmittedHasInitialValue(updatedLeaderboardRank);
		assertThat(
				updatedLeaderboardRank.getRankStatus(),
				is(LeaderboardRankDatabaseAdapter.RankStatus.TOP_RANK_NOT_AVAILABLE));
		assertThatRankHasDefaultValue(updatedLeaderboardRank);
		assertThatRankDisplayHasDefaultValue(updatedLeaderboardRank);
		assertThatDateLastUpdatedIsLaterThan(updatedLeaderboardRank,
				systemTimeStartOfTest);
	}

	private void assertThatScoreOriginHasInitialValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getScoreOrigin(),
				is(scoreOriginInitialValue));
	}

	private void assertThatStatisticsIdHasInitialValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getStatisticsId(),
				is(statisticsIdInitialValue));
	}

	private void assertThatDateSubmittedHasInitialValue(
			LeaderboardRankRow leaderboardRankRow) {
		assertThat(leaderboardRankRow.getDateSubmitted(),
				is(dateSubmittedInitialValue));
	}
}
