package net.mathdoku.plus.storage.selector;

import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import databasehelper.CsvImporter.CsvImporter;
import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class HistoricStatisticsSelectorTest {
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
			TestRunnerHelper.setup(HistoricStatisticsSelectorTest.class
					.getCanonicalName());
			String pathToImportFile = "net/mathdoku/plus/storage/databaseadapter/selector/";
			new CsvImporter(pathToImportFile
					+ "HistoricStatisticsSelectorTest_grid.csv",
					new GridDatabaseAdapter()).importIntoDatabase();
			new CsvImporter(pathToImportFile
					+ "HistoricStatisticsSelectorTest_statistics.csv",
					new StatisticsDatabaseAdapter()).importIntoDatabase();
			setupAtFirstTestIsExecuted = true;
		}
	}

	@Test
	public void placeholder() throws Exception {
	}

	@AfterClass
	public static void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	@Test
	public void invoke_GetAllGridsWithSize4() throws Exception {
		int minGridSize = 4;
		int maxGridSize = 4;
		assertThat(
				new HistoricStatisticsSelector(minGridSize, maxGridSize)
						.getDataPointList(),
				is(getDataPoints(minGridSize, maxGridSize)));
	}

	private List<HistoricStatisticsSelector.DataPoint> getDataPoints(
			int minGridSize, int maxGridSize) {
		List<HistoricStatisticsSelector.DataPoint> expectedDataPoints = new ArrayList<HistoricStatisticsSelector.DataPoint>();
		if (minGridSize <= 4) {
			expectedDataPoints.add(createDataPoint(78512, 40000,
					SolvingAttemptStatus.FINISHED_SOLVED));
			expectedDataPoints.add(createDataPoint(128038, 100000,
					SolvingAttemptStatus.UNFINISHED));
		}
		if (maxGridSize >= 5) {
			expectedDataPoints.add(createDataPoint(153219, 80000,
					SolvingAttemptStatus.REVEALED_SOLUTION));
			expectedDataPoints.add(createDataPoint(1337031, 0,
					SolvingAttemptStatus.FINISHED_SOLVED));
			expectedDataPoints.add(createDataPoint(116916, 0,
					SolvingAttemptStatus.FINISHED_SOLVED));
			expectedDataPoints.add(createDataPoint(0, 0,
					SolvingAttemptStatus.UNFINISHED));
		}
		return expectedDataPoints;
	}

	private HistoricStatisticsSelector.DataPoint createDataPoint(
			long elapsedTimeExcludingCheatPenalty, long cheatPenalty,
			SolvingAttemptStatus solvingAttemptStatus) {
		HistoricStatisticsSelector.DataPoint dataPoint = new HistoricStatisticsSelector.DataPoint(
				elapsedTimeExcludingCheatPenalty - cheatPenalty, cheatPenalty,
				solvingAttemptStatus);
		return dataPoint;
	}

	// @Test
	public void invoke_GetAllGridsWithSize5() throws Exception {
		int minGridSize = 5;
		int maxGridSize = 5;
		assertThat(
				new HistoricStatisticsSelector(minGridSize, maxGridSize)
						.getDataPointList(),
				is(getDataPoints(minGridSize, maxGridSize)));
	}

	// @Test
	public void invoke_GetAllGridsWithSize4Or5() throws Exception {
		int minGridSize = 4;
		int maxGridSize = 5;
		assertThat(
				new HistoricStatisticsSelector(minGridSize, maxGridSize)
						.getDataPointList(),
				is(getDataPoints(minGridSize, maxGridSize)));
	}
}
