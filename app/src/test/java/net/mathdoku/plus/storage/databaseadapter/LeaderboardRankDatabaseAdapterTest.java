package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.enums.PuzzleComplexity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class LeaderboardRankDatabaseAdapterTest {
	LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapter;

	@Before
	public void setUp() throws Exception {
		TestRunnerHelper.setup(this.getClass().getCanonicalName());
		leaderboardRankDatabaseAdapter = new LeaderboardRankDatabaseAdapter();
	}

	@After
	public void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	@Test
	public void getDatabaseTableDefinition() throws Exception {
		String resultDatabaseCreateSQL = leaderboardRankDatabaseAdapter
				.getDatabaseTableDefinition()
				.getCreateTableSQL();
		StringBuilder expectedDatabaseCreateSQL = new StringBuilder();
		expectedDatabaseCreateSQL.append("CREATE TABLE `leaderboard_rank` (");
		expectedDatabaseCreateSQL
				.append("`_id` integer primary key autoincrement, ");
		expectedDatabaseCreateSQL
				.append("`leaderboard_id` text not null unique, ");
		expectedDatabaseCreateSQL.append("`grid_size` integer not null, ");
		expectedDatabaseCreateSQL.append("`hidden_operators` text not null, ");
		expectedDatabaseCreateSQL.append("`puzzle_complexity` text not null, ");
		expectedDatabaseCreateSQL.append("`score_origin` text not null, ");
		expectedDatabaseCreateSQL.append("`score_statistics_id` integer, ");
		expectedDatabaseCreateSQL.append("`score_raw_score` long, ");
		expectedDatabaseCreateSQL.append("`score_date_submitted` datetime, ");
		expectedDatabaseCreateSQL.append("`rank_status` text not null, ");
		expectedDatabaseCreateSQL.append("`rank` long, ");
		expectedDatabaseCreateSQL.append("`rank_display` text, ");
		expectedDatabaseCreateSQL.append("`rank_date_last_updated` datetime");
		expectedDatabaseCreateSQL.append(")");
		assertThat(resultDatabaseCreateSQL,
				is(expectedDatabaseCreateSQL.toString()));
	}

	@Test
	public void upgradeTable() throws Exception {
		// Nothing to test currently.
	}

	@Test
	public void insert() throws Exception {
		LeaderboardRankRow newLeaderboardRankRow = createLeaderboardRankRowAllFieldsFilled();

		assertThat(
				leaderboardRankDatabaseAdapter.insert(newLeaderboardRankRow),
				is(newLeaderboardRankRow));
	}

	private LeaderboardRankRow createLeaderboardRankRowAllFieldsFilled() {
		int idFirstLeaderboardIdInEmptyDatabase = 1;
		String leaderboardId = "*** SOME LEADERBOARD ID ***";
		int gridSize = 7;
		boolean operatorsHidden = true;
		PuzzleComplexity puzzleComplexity = PuzzleComplexity.DIFFICULT;
		LeaderboardRankDatabaseAdapter.ScoreOrigin scoreOrigin = LeaderboardRankDatabaseAdapter.ScoreOrigin.EXTERNAL;
		int statisticsId = 67;
		long rawScore = 18293;
		long dateSubmitted = 192030434L;
		LeaderboardRankDatabaseAdapter.RankStatus rankStatus = LeaderboardRankDatabaseAdapter.RankStatus.TOP_RANK_UPDATED;
		long rank = 12;
		String rankDisplay = "12th";
		long dateLastUpdated = 199999999L;
		return new LeaderboardRankRow(idFirstLeaderboardIdInEmptyDatabase,
				leaderboardId, gridSize, operatorsHidden, puzzleComplexity,
				scoreOrigin, statisticsId, rawScore, dateSubmitted, rankStatus,
				rank, rankDisplay, dateLastUpdated);
	}

	@Test
	public void update() throws Exception {
		LeaderboardRankRow originalLeaderboardRankRow = createLeaderboardRankRowAllFieldsFilled();
		leaderboardRankDatabaseAdapter.insert(originalLeaderboardRankRow);

		int updatedStatisticsId = originalLeaderboardRankRow.getStatisticsId() + 23;
		long updatedRawScore = originalLeaderboardRankRow.getRawScore() - 51;
		LeaderboardRankRow updatedLeaderboardRankRow = originalLeaderboardRankRow
				.createWithNewLocalScore(updatedStatisticsId, updatedRawScore);
		assertThat(
				leaderboardRankDatabaseAdapter
						.update(updatedLeaderboardRankRow),
				is(true));
		assertThat(
				leaderboardRankDatabaseAdapter.get(originalLeaderboardRankRow
						.getLeaderboardId()), is(updatedLeaderboardRankRow));
	}

	@Test
	public void get() throws Exception {
		createAndInsertInitialLeaderboard("*** LEADERBOARD 1 ***");
		String leaderboardId2 = "*** LEADERBOARD 2 ***";
		LeaderboardRankRow leaderboardRankRow2 = createAndInsertInitialLeaderboard(leaderboardId2);
		createAndInsertInitialLeaderboard("*** LEADERBOARD 3 ***");

		assertThat(leaderboardRankDatabaseAdapter.get(leaderboardId2),
				is(leaderboardRankRow2));
	}

	private LeaderboardRankRow createAndInsertInitialLeaderboard(
			String leaderboardId) {
		int gridSize = 4;
		boolean operatorsHidden = true;
		PuzzleComplexity puzzleComplexity = PuzzleComplexity.NORMAL;
		return leaderboardRankDatabaseAdapter.insert(LeaderboardRankRow
				.createInitial(leaderboardId, gridSize, operatorsHidden,
						puzzleComplexity));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getPrefixedColumnName_Null_ThrowsIllegalArgumentException()
			throws Exception {
		assertThat(LeaderboardRankDatabaseAdapter.getPrefixedColumnName(null),
				is("`leaderboard_rank`.`TestAbC`"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getPrefixedColumnName_Empty_ThrowsIllegalArgumentException()
			throws Exception {
		assertThat(LeaderboardRankDatabaseAdapter.getPrefixedColumnName(""),
				is("`leaderboard_rank`.`TestAbC`"));
	}

	@Test
	public void getPrefixedColumnName_NotNullOrEmpty() throws Exception {
		assertThat(
				LeaderboardRankDatabaseAdapter.getPrefixedColumnName("TestAbC"),
				is("`leaderboard_rank`.`TestAbC`"));
	}

	@Test
	public void getMostOutdatedLeaderboardRank_OnlyLeaderboardsWithStatusToBeUpdatedAndEqualRankDateUpdatedExists_LeaderboardWithSmallestIdIsSelected()
			throws Exception {
		LeaderboardRankRow expectedLeaderboardRank = createAndInsertInitialLeaderboard("*** LEADERBOARD 1 ***");
		createAndInsertInitialLeaderboard("*** LEADERBOARD 2 ***");

		assertThat(
				leaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank(),
				is(expectedLeaderboardRank));
	}

	@Test
	public void getMostOutdatedLeaderboardRank_OnlyLeaderboardsWithLocalScoreExists_LeaderboardWithSmallestIdIsSelected()
			throws Exception {
		long systemTime = System.currentTimeMillis();
		LeaderboardRankRow leaderboardRank1 = createAndInsertLeaderboardWithLocalScoreAtDateTime(
				"*** LEADERBOARD 1 ***", systemTime);
		// Leaderboard 2 will be selected as it is older than leaderboard 1.
		LeaderboardRankRow leaderboardRank2 = createAndInsertLeaderboardWithLocalScoreAtDateTime(
				"*** LEADERBOARD 2 ***",
				leaderboardRank1.getDateSubmitted() - 1);
		// Leaderboard 3 has same update time as leaderboard 2 but will not be
		// selected because it has an higher row id.
		createAndInsertLeaderboardWithLocalScoreAtDateTime(
				"*** LEADERBOARD 3 ***", leaderboardRank2.getDateSubmitted());

		assertThat(
				leaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank(),
				is(leaderboardRank2));
	}

	private LeaderboardRankRow createAndInsertLeaderboardWithLocalScoreAtDateTime(
			String leaderboardId, long datetime) {
		int gridSize = 4;
		boolean operatorsHidden = true;
		PuzzleComplexity puzzleComplexity = PuzzleComplexity.NORMAL;
		int statisticsId = 78;
		long rawScore = 12033;
		long rank = 12;
		String rankDisplay = "12th";

		// Create the leaderboard rank with normal methods
		LeaderboardRankRow leaderboardRankRow = LeaderboardRankRow
				.createInitial(leaderboardId, gridSize, operatorsHidden,
						puzzleComplexity)
				.createWithNewGooglePlayScore(rawScore, rank, rankDisplay)
				.createWithNewLocalScore(statisticsId, rawScore);

		// Manipulate the date last update
		leaderboardRankRow = getLeaderboardRankRowWithManipulatedDateLastSubmitted(
				leaderboardRankRow, datetime);

		return leaderboardRankDatabaseAdapter.insert(leaderboardRankRow);
	}

	private LeaderboardRankRow getLeaderboardRankRowWithManipulatedDateLastSubmitted(
			LeaderboardRankRow leaderboardRankRow, long datetime) {
		return new LeaderboardRankRow(leaderboardRankRow.getRowId(),
				leaderboardRankRow.getLeaderboardId(),
				leaderboardRankRow.getGridSize(),
				leaderboardRankRow.isOperatorsHidden(),
				leaderboardRankRow.getPuzzleComplexity(),
				leaderboardRankRow.getScoreOrigin(),
				leaderboardRankRow.getStatisticsId(),
				leaderboardRankRow.getRawScore(), datetime,
				leaderboardRankRow.getRankStatus(),
				leaderboardRankRow.getRank(),
				leaderboardRankRow.getRankDisplay(),
				leaderboardRankRow.getDateLastUpdated());
	}

	@Test
	public void getMostOutdatedLeaderboardRank_OnlyLeaderboardsWithStatusUpdatedInLast15MinutesExists_NoLeaderboardSelected()
			throws Exception {
		long systemTime = System.currentTimeMillis();
		createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 1 ***", systemTime);
		// The second leaderboard is just inside the 15 minutes time interval
		// before the current system time. Do not use a difference of just a few
		// milliseconds as minor delays in executing this test should not result
		// in false negative results.
		long safetyOffsetInMillis = 5000;
		createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 2 ***", systemTime - 15 * 60 * 1000
						+ safetyOffsetInMillis);

		assertThat(
				leaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank(),
				is(nullValue()));
	}

	private LeaderboardRankRow createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
			String leaderboardId, long datetime) {
		int gridSize = 4;
		boolean operatorsHidden = true;
		PuzzleComplexity puzzleComplexity = PuzzleComplexity.NORMAL;
		long rawScore = 12033;
		long rank = 12;
		String rankDisplay = "12th";

		// Create the leaderboard rank with normal methods
		LeaderboardRankRow leaderboardRankRow = LeaderboardRankRow
				.createInitial(leaderboardId, gridSize, operatorsHidden,
						puzzleComplexity)
				.createWithNewGooglePlayScore(rawScore, rank, rankDisplay);

		// Manipulate the date last update
		leaderboardRankRow = getLeaderboardRankRowWithManipulatedDateLastUpdateRank(
				leaderboardRankRow, datetime);

		return leaderboardRankDatabaseAdapter.insert(leaderboardRankRow);
	}

	private LeaderboardRankRow getLeaderboardRankRowWithManipulatedDateLastUpdateRank(
			LeaderboardRankRow leaderboardRankRow, long datetime) {
		return new LeaderboardRankRow(leaderboardRankRow.getRowId(),
				leaderboardRankRow.getLeaderboardId(),
				leaderboardRankRow.getGridSize(),
				leaderboardRankRow.isOperatorsHidden(),
				leaderboardRankRow.getPuzzleComplexity(),
				leaderboardRankRow.getScoreOrigin(),
				leaderboardRankRow.getStatisticsId(),
				leaderboardRankRow.getRawScore(),
				leaderboardRankRow.getDateSubmitted(),
				leaderboardRankRow.getRankStatus(),
				leaderboardRankRow.getRank(),
				leaderboardRankRow.getRankDisplay(), datetime);
	}

	@Test
	public void getMostOutdatedLeaderboardRank_LeaderboardsWithStatusUpdatedMoreThan15MinutesAgoExists_MostOutdatedLeaderboardWithSmallestIdSelected()
			throws Exception {
		long systemTime = System.currentTimeMillis();
		createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 1 ***", systemTime);
		// The second leaderboard is just outside the 15 minutes time interval
		// before the current system time. It is safe to use an additional delay
		// of 1 millisecond as execution of remainder of the unit test will also
		// take a few milliseconds.
		long safetyOffsetInMillis = 1;
		LeaderboardRankRow leaderboardRank2 = createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 2 ***", systemTime - 15 * 60 * 1000
						- safetyOffsetInMillis);
		// The third leaderboard is more outdated than the second leaderboard
		// and will be selected first.
		LeaderboardRankRow leaderboardRank3 = createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 3 ***",
				leaderboardRank2.getDateLastUpdated() - 1);
		// Leaderboard 4 has same update time as leaderboard 3 but will not be
		// selected because it has an higher row id.
		createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 4 ***", leaderboardRank3.getDateLastUpdated());

		assertThat(
				leaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank(),
				is(leaderboardRank3));
	}

	@Test
	public void getMostOutdatedLeaderboardRank_OnlyLeaderboardsWithTopRankNotAvailableInLast24HoursExists_NoLeaderboardSelected()
			throws Exception {
		long systemTime = System.currentTimeMillis();
		createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
				"*** LEADERBOARD 1 ***", systemTime);
		// The second leaderboard is just inside the 24 hours time interval
		// before the current system time. Do not use a difference of just a few
		// milliseconds as minor delays in executing this test should not result
		// in false negative results.
		long safetyOffsetInMillis = 5000;
		createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
				"*** LEADERBOARD 2 ***", systemTime - 24 * 60 * 60 * 1000
						+ safetyOffsetInMillis);

		assertThat(
				leaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank(),
				is(nullValue()));
	}

	private LeaderboardRankRow createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
			String leaderboardId, long datetime) {
		int gridSize = 4;
		boolean operatorsHidden = true;
		PuzzleComplexity puzzleComplexity = PuzzleComplexity.NORMAL;
		long rawScore = 12033;
		long rank = 12;
		String rankDisplay = "12th";

		// Create the leaderboard rank with normal methods
		LeaderboardRankRow leaderboardRankRow = LeaderboardRankRow
				.createInitial(leaderboardId, gridSize, operatorsHidden,
						puzzleComplexity)
				.createWithNewGooglePlayScore(rawScore, rank, rankDisplay)
				.createWithGooglePlayRankNotAvailable();

		// Manipulate the date last update
		leaderboardRankRow = getLeaderboardRankRowWithManipulatedDateLastUpdateRank(
				leaderboardRankRow, datetime);

		return leaderboardRankDatabaseAdapter.insert(leaderboardRankRow);
	}

	@Test
	public void getMostOutdatedLeaderboardRank_OnlyLeaderboardsWithTopRankNotAvailableInLast24HoursExists_MostOutdatedLeaderboardWithSmallestIdSelected()
			throws Exception {
		long systemTime = System.currentTimeMillis();
		createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
				"*** LEADERBOARD 1 ***", systemTime);
		// The second leaderboard is just outside the 24 hours time interval
		// before the current system time. It is safe to use an additional delay
		// of 1 millisecond as execution of remainder of the unit test will also
		// take a few milliseconds.
		long safetyOffsetInMillis = 1;
		LeaderboardRankRow leaderboardRank2 = createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
				"*** LEADERBOARD 2 ***", systemTime - 24 * 60 * 60 * 1000
						- safetyOffsetInMillis);
		// The third leaderboard is more outdated than the second leaderboard
		// and will be selected first.
		LeaderboardRankRow leaderboardRank3 = createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
				"*** LEADERBOARD 3 ***",
				leaderboardRank2.getDateLastUpdated() - 1);
		// Leaderboard 4 has same update time as leaderboard 3 but will not be
		// selected because it
		// has an higher row id.
		createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
				"*** LEADERBOARD 4 ***", leaderboardRank3.getDateLastUpdated());

		assertThat(
				leaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank(),
				is(leaderboardRank3));
	}

	@Test
	public void getCountOutdatedLeaderboardRanks() throws Exception {
		int expectedCountOutdatedLeaderboardRanks = 0;
		long systemTime = System.currentTimeMillis();

		// Leaderboard which has status TO_BE_UPDATED should be counted.
		LeaderboardRankRow leaderboardRank1 = createAndInsertLeaderboardWithLocalScoreAtDateTime(
				"*** LEADERBOARD 1 ***", systemTime);
		expectedCountOutdatedLeaderboardRanks++;

		// Leaderboard which has status TOP_RANK_UPDATED and is less than 15
		// minutes old should not be counted.
		createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 2 ***", systemTime);

		// Leaderboard which has status TOP_RANK_UPDATED and is less than 15
		// minutes old should be counted.
		long safetyOffsetInMillis = 1;
		LeaderboardRankRow leaderboardRank2 = createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 3 ***", systemTime - 15 * 60 * 1000
						- safetyOffsetInMillis);
		expectedCountOutdatedLeaderboardRanks++;

		// Leaderboard which has status TOP_RANK_NOT_AVAILABLE and is less than
		// 24 hours old should not be counted.
		createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
				"*** LEADERBOARD 4 ***", systemTime);

		// Leaderboard which has status TOP_RANK_NOT_AVAILABLE and is less than
		// 24 hours old should be counted.
		createAndInsertLeaderboardWithGooglePlayRankNotAvailableAtDatetime(
				"*** LEADERBOARD 5 ***", systemTime - 24 * 60 * 60 * 1000
						+ safetyOffsetInMillis);
		expectedCountOutdatedLeaderboardRanks++;

		assertThat(
				leaderboardRankDatabaseAdapter
						.getCountOutdatedLeaderboardRanks(),
				is(expectedCountOutdatedLeaderboardRanks));
	}

	@Test
	public void setAllRanksToBeUpdated() throws Exception {
		long systemTime = System.currentTimeMillis();

		// Insert some leaderboard which do not need to be updated.
		createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 1 ***", systemTime);
		createAndInsertLeaderboardWithGooglePlayScoreAtDatetime(
				"*** LEADERBOARD 2 ***", systemTime);
		assertThat(
				leaderboardRankDatabaseAdapter.getMostOutdatedLeaderboardRank(),
				is(nullValue()));
		leaderboardRankDatabaseAdapter.setAllRanksToBeUpdated();

		assertThat(
				leaderboardRankDatabaseAdapter
						.getCountOutdatedLeaderboardRanks(),
				is(2));
	}
}
