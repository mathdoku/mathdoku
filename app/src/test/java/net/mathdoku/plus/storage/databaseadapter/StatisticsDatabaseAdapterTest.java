package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class StatisticsDatabaseAdapterTest {
    private StatisticsDatabaseAdapter statisticsDatabaseAdapter;

    @Before
    public void setUp() throws Exception {
        TestRunnerHelper.setup(this.getClass()
                                       .getCanonicalName());
        statisticsDatabaseAdapter = new StatisticsDatabaseAdapter();
    }

    @After
    public void tearDown() throws Exception {
        TestRunnerHelper.tearDown();
    }

    @Test
    public void getDatabaseTableDefinition() throws Exception {
        String resultDatabaseCreateSQL = new SolvingAttemptDatabaseAdapter().getDatabaseTableDefinition()
                .getCreateTableSQL();
        StringBuilder expectedDatabaseCreateSQL = new StringBuilder();
        expectedDatabaseCreateSQL.append("CREATE TABLE `solving_attempt` (");
        expectedDatabaseCreateSQL.append("`_id` integer primary key autoincrement, ");
        expectedDatabaseCreateSQL.append("`grid_id` integer not null, ");
        expectedDatabaseCreateSQL.append("`date_created` datetime not null, ");
        expectedDatabaseCreateSQL.append("`date_updated` datetime not null, ");
        expectedDatabaseCreateSQL.append("`revision` integer not null, ");
        expectedDatabaseCreateSQL.append("`data` text not null, ");
        expectedDatabaseCreateSQL.append("`status` integer not null default -1, ");
        expectedDatabaseCreateSQL.append("FOREIGN KEY(`grid_id`) REFERENCES grid(_id))");

        assertThat(resultDatabaseCreateSQL, is(expectedDatabaseCreateSQL.toString()));
    }

    @Test
    public void upgradeTable() throws Exception {
        // Nothing to test currently.
    }

    @Test(expected = IllegalArgumentException.class)
    public void insert_GridStatisticsIsNull_ThrowsIllegalArgumentException() throws Exception {
        statisticsDatabaseAdapter.insert(null);
    }

    @Test
    public void insert_GridStatisticsIsNotNull_ThrowsIllegalArgumentException() throws Exception {
        int idOfFirstGridStatistics = 1;
        int gridId = 987;
        GridStatistics gridStatistics = createGridStatisticsWithAllFieldsHavingAValue(idOfFirstGridStatistics, gridId);

        assertThat(statisticsDatabaseAdapter.insert(gridStatistics), is(idOfFirstGridStatistics));
        assertThat(statisticsDatabaseAdapter.getStatisticsForSolvingAttempt(idOfFirstGridStatistics),
                   is(gridStatistics));
    }

    private GridStatistics createGridStatisticsWithAllFieldsHavingAValue(int id, int gridId) {
        GridStatistics gridStatistics = new GridStatistics();
        gridStatistics.mId = id;
        gridStatistics.mGridId = gridId;
        gridStatistics.mReplayCount = 3;
        gridStatistics.mFirstMove = DatabaseUtil.toSQLTimestamp("2004-04-04 04:04:04.444");
        gridStatistics.mLastMove = DatabaseUtil.toSQLTimestamp("2005-05-05 05:05:05.555");
        gridStatistics.mCellsEmpty = 16;
        gridStatistics.mIncludedInStatistics = true;

        // Fields below will be defaulted by the StatisticsDatabase adapter and
        // should only be changed in case those defaults are changed.
        gridStatistics.mElapsedTime = 0;
        gridStatistics.mCheatPenaltyTime = 0;
        gridStatistics.mEnteredValueReplaced = 0;
        gridStatistics.mMaybeValue = 0;
        gridStatistics.mActionUndoMove = 0;
        gridStatistics.mActionClearCell = 0;
        gridStatistics.mActionClearGrid = 0;
        gridStatistics.mActionRevealCell = 0;
        gridStatistics.mActionRevealOperator = 0;
        gridStatistics.mActionCheckProgress = 0;
        gridStatistics.mCheckProgressInvalidCellsFound = 0;
        gridStatistics.mSolutionRevealed = false;
        gridStatistics.mCellsFilled = 0;
        gridStatistics.mCellsRevealed = 0;
        gridStatistics.mSolvedManually = false;
        gridStatistics.mFinished = false;
        return gridStatistics;
    }

    @Test
    public void getMostRecent() throws Exception {
        int idOfNextGridStatistics = 1;
        int gridId = 987;

        int idGridStatistics1 = idOfNextGridStatistics++;
        statisticsDatabaseAdapter.insert(createGridStatisticsWithAllFieldsHavingAValue(idGridStatistics1, gridId));

        int idGridStatistics2 = idOfNextGridStatistics++;
        GridStatistics gridStatistics2 = createGridStatisticsWithAllFieldsHavingAValue(idGridStatistics2, gridId);
        statisticsDatabaseAdapter.insert(gridStatistics2);

        int otherGridId = 9999;
        int otherGridStatistics = idOfNextGridStatistics++;
        statisticsDatabaseAdapter.insert(
                createGridStatisticsWithAllFieldsHavingAValue(otherGridStatistics, otherGridId));

        assertThat(statisticsDatabaseAdapter.getStatisticsForSolvingAttempt(idGridStatistics2), is(gridStatistics2));
    }

    @Test
    public void update() throws Exception {
        int idOfFirstGridStatistics = 1;
        int gridId = 987;
        GridStatistics gridStatistics = createGridStatisticsWithAllFieldsHavingAValue(idOfFirstGridStatistics, gridId);
        statisticsDatabaseAdapter.insert(gridStatistics);

        // Change all (changeable) fields
        gridStatistics.mLastMove = DatabaseUtil.toSQLTimestamp("2014-02-02 22:22:22.222");
        gridStatistics.mCellsEmpty = 9;
        gridStatistics.mIncludedInStatistics = false;
        gridStatistics.mElapsedTime += 2;
        gridStatistics.mCheatPenaltyTime += 3;
        gridStatistics.mEnteredValueReplaced += 4;
        gridStatistics.mMaybeValue += 5;
        gridStatistics.mActionUndoMove += 6;
        gridStatistics.mActionClearCell += 7;
        gridStatistics.mActionClearGrid += 8;
        gridStatistics.mActionRevealCell += 9;
        gridStatistics.mActionRevealOperator += 10;
        gridStatistics.mActionCheckProgress += 11;
        gridStatistics.mCheckProgressInvalidCellsFound += 12;
        gridStatistics.mSolutionRevealed = true;
        gridStatistics.mCellsFilled += 13;
        gridStatistics.mCellsRevealed += 14;
        gridStatistics.mSolvedManually = true;
        gridStatistics.mFinished = true;

        assertThat(statisticsDatabaseAdapter.update(gridStatistics), is(true));
        assertThat(statisticsDatabaseAdapter.getStatisticsForSolvingAttempt(idOfFirstGridStatistics),
                   is(gridStatistics));
    }

    @Test
    public void getPrefixedColumnName() throws Exception {
        assertThat(statisticsDatabaseAdapter.getPrefixedColumnName("A123bc"), is("`statistics`.`A123bc`"));
    }

    @Test
    public void updateSolvingAttemptToBeIncludedInStatistics() throws Exception {
        int idOfNextGridStatistics = 1;
        int gridId = 987;

        int idGridStatistics1 = idOfNextGridStatistics++;
        GridStatistics gridStatistics1 = createGridStatisticsWithAllFieldsHavingAValue(idGridStatistics1, gridId);
        gridStatistics1.mIncludedInStatistics = true;
        statisticsDatabaseAdapter.insert(gridStatistics1);

        int idGridStatistics2 = idOfNextGridStatistics++;
        GridStatistics gridStatistics2 = createGridStatisticsWithAllFieldsHavingAValue(idGridStatistics2, gridId);
        // Normally, only the newest statistics of a grid will be set to be
        // included in the statistics selection. Just for purpose of this unit
        // test this setting is set incorrect to test the effect of method
        // updateSolvingAttemptToBeIncludedInStatistics.
        gridStatistics1.mIncludedInStatistics = false;
        statisticsDatabaseAdapter.insert(gridStatistics2);

        statisticsDatabaseAdapter.updateSolvingAttemptToBeIncludedInStatistics(gridId, idGridStatistics2);
        gridStatistics1.mIncludedInStatistics = false;
        assertThat(statisticsDatabaseAdapter.getStatisticsForSolvingAttempt(idGridStatistics1), is(gridStatistics1));
        gridStatistics2.mIncludedInStatistics = true;
        assertThat(statisticsDatabaseAdapter.getStatisticsForSolvingAttempt(idGridStatistics2), is(gridStatistics2));
    }
}
