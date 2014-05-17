package databasehelper.CsvImporter;

import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

@RunWith(RobolectricGradleTestRunner.class)
public class CsvImporterTest {
	@Before
	public void setUp() throws Exception {
		TestRunnerHelper.setup(this.getClass().getCanonicalName());
	}

	@After
	public void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	@Test(expected = CsvImportException.class)
	public void importIntoDatabase_FilenameIsNull_ThrowsCsvImporterException()
			throws Exception {
		new CsvImporter(null, new GridDatabaseAdapter()).importIntoDatabase();
	}

	@Test(expected = CsvImportException.class)
	public void importIntoDatabase_FilenameIsEmpty_ThrowsCsvImporterException()
			throws Exception {
		new CsvImporter("", new GridDatabaseAdapter()).importIntoDatabase();
	}

	@Test(expected = CsvImportException.class)
	public void importIntoDatabase_DatabaseAdapterIsNull_ThrowsCsvImporterException()
			throws Exception {
		new CsvImporter("databasehelper/CsvImporter/CsvImporterTest_grid.csv",
				null).importIntoDatabase();
	}

	@Test(expected = CsvImportException.class)
	public void importIntoDatabase_FileDoesNotExist_ThrowsCsvImporterException()
			throws Exception {
		new CsvImporter("some non existing file.csv", new GridDatabaseAdapter())
				.importIntoDatabase();
	}

	@Test(expected = CsvImportException.class)
	public void importIntoDatabase_FileContainsAStringWhereAnIntegerWasExpected_ThrowsCsvImporterException()
			throws Exception {
		new CsvImporter("databasehelper/CsvImporter/grid_with_error.csv",
				new GridDatabaseAdapter()).importIntoDatabase();
	}

	@Test
	public void importIntoDatabase_Grid_ImportedWithoutError() throws Exception {
		new CsvImporter("databasehelper/CsvImporter/grid.csv",
				new GridDatabaseAdapter()).importIntoDatabase();
	}

	@Test
	public void importIntoDatabase_LeaderboardRank_ImportedWithoutError()
			throws Exception {
		new CsvImporter("databasehelper/CsvImporter/leaderboard_rank.csv",
				new LeaderboardRankDatabaseAdapter()).importIntoDatabase();
	}

	@Test
	public void importIntoDatabase_SolvingAttempt_ImportedWithoutError()
			throws Exception {
		new CsvImporter("databasehelper/CsvImporter/solving_attempt.csv",
				new SolvingAttemptDatabaseAdapter()).importIntoDatabase();
	}

	@Test
	public void importIntoDatabase_Statistics_ImportedWithoutError()
			throws Exception {
		new CsvImporter("databasehelper/CsvImporter/statistics.csv",
				new StatisticsDatabaseAdapter()).importIntoDatabase();
	}
}
