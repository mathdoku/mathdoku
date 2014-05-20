package net.mathdoku.plus.storage.selector;

import net.mathdoku.plus.statistics.CumulativeStatistics;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import csvimporter.DatabaseCsvImporter;
import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class CumulativeStatisticsSelectorTest {
	private static boolean setupAtFirstTestIsExecuted = false;

	@Before
	public void setUp() throws Exception {
		if (!setupAtFirstTestIsExecuted) {
			/*
			 * Ideally this would have been executed in the @BeforeClass setup.
			 * This is however not possible as the Robolectric.application is
			 * not yet set when executing a @BeforeClass method. As a result it
			 * is not possible to open the database helper correctly.
			 */
			TestRunnerHelper.setup(CumulativeStatisticsSelectorTest.class
					.getCanonicalName());
			String pathToImportFile = "net/mathdoku/plus/storage/databaseadapter/selector/";
			new DatabaseCsvImporter(pathToImportFile
					+ "CumulativeStatisticsSelectorTest_grid.csv",
					new GridDatabaseAdapter()).importFile();
			new DatabaseCsvImporter(pathToImportFile
					+ "CumulativeStatisticsSelectorTest_statistics.csv",
					new StatisticsDatabaseAdapter()).importFile();
			setupAtFirstTestIsExecuted = true;
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	@Test
	public void invoke_GetAllGridsWithSize4() throws Exception {
		int minGridSize = 4;
		int maxGridSize = 4;
		CumulativeStatistics cumulativeStatistics = new CumulativeStatisticsSelector(
				minGridSize, maxGridSize).getCumulativeStatistics();

		assertThat(cumulativeStatistics, is(notNullValue()));
		assertThat("Minimum grid size", cumulativeStatistics.mMinGridSize,
				is(minGridSize));
		assertThat("Maximum grid size", cumulativeStatistics.mMaxGridSize,
				is(maxGridSize));
		assertThat("Grids started", cumulativeStatistics.mCountStarted, is(2));
		assertThat("Grids revealed",
				cumulativeStatistics.mCountSolutionRevealed, is(0));
		assertThat("Grids solved manually",
				cumulativeStatistics.mCountSolvedManually, is(1));
		assertThat("Grids finished", cumulativeStatistics.mCountFinished, is(1));
	}

	@Test
	public void invoke_GetAllGridsWithSize5() throws Exception {
		int minGridSize = 5;
		int maxGridSize = 5;
		CumulativeStatistics cumulativeStatistics = new CumulativeStatisticsSelector(
				minGridSize, maxGridSize).getCumulativeStatistics();

		assertThat(cumulativeStatistics, is(notNullValue()));
		assertThat("Minimum grid size", cumulativeStatistics.mMinGridSize,
				is(minGridSize));
		assertThat("Maximum grid size", cumulativeStatistics.mMaxGridSize,
				is(maxGridSize));
		assertThat("Grids started", cumulativeStatistics.mCountStarted, is(4));
		assertThat("Grids revealed",
				cumulativeStatistics.mCountSolutionRevealed, is(1));
		assertThat("Grids solved manually",
				cumulativeStatistics.mCountSolvedManually, is(2));
		assertThat("Grids finished", cumulativeStatistics.mCountFinished, is(3));
	}

	@Test
	public void invoke_GetAllGridsWithSize4Or5() throws Exception {
		int minGridSize = 4;
		int maxGridSize = 5;
		CumulativeStatistics cumulativeStatistics = new CumulativeStatisticsSelector(
				minGridSize, maxGridSize).getCumulativeStatistics();

		assertThat(cumulativeStatistics, is(notNullValue()));
		assertThat("Minimum grid size", cumulativeStatistics.mMinGridSize,
				is(minGridSize));
		assertThat("Maximum grid size", cumulativeStatistics.mMaxGridSize,
				is(maxGridSize));
		assertThat("Grids started", cumulativeStatistics.mCountStarted, is(6));
		assertThat("Grids revealed",
				cumulativeStatistics.mCountSolutionRevealed, is(1));
		assertThat("Grids solved manually",
				cumulativeStatistics.mCountSolvedManually, is(3));
		assertThat("Grids finished", cumulativeStatistics.mCountFinished, is(4));
	}
}
