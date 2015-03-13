package csvimporter;

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
public class DatabaseCsvImporterTest {
    @Before
    public void setUp() throws Exception {
        TestRunnerHelper.setup(this.getClass()
                                       .getCanonicalName());
    }

    @After
    public void tearDown() throws Exception {
        TestRunnerHelper.tearDown();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FilenameIsNull_ThrowsCsvImporterException() throws Exception {
        new DatabaseCsvImporter(null, new GridDatabaseAdapter()).importFile();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FilenameIsEmpty_ThrowsCsvImporterException() throws Exception {
        new DatabaseCsvImporter("", new GridDatabaseAdapter()).importFile();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_DatabaseAdapterIsNull_ThrowsCsvImporterException() throws Exception {
        new DatabaseCsvImporter(getPathToFile("CsvImporterTest_grid.csv"), null).importFile();
    }

    private String getPathToFile(String filename) {
        return "csvimporter/databasecsvimporter/" + filename;
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FileDoesNotExist_ThrowsCsvImporterException() throws Exception {
        new DatabaseCsvImporter(getPathToFile("some non existing file.csv"), new GridDatabaseAdapter()).importFile();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FileContainsAStringWhereAnIntegerWasExpected_ThrowsCsvImporterException() throws Exception {
        new DatabaseCsvImporter(getPathToFile("grid_with_error.csv"), new GridDatabaseAdapter()).importFile();
    }

    @Test
    public void importFile_Grid_ImportedWithoutError() throws Exception {
        new DatabaseCsvImporter(getPathToFile("grid.csv"), new GridDatabaseAdapter()).importFile();
    }

    @Test
    public void importFile_LeaderboardRank_ImportedWithoutError() throws Exception {
        new DatabaseCsvImporter(getPathToFile("leaderboard_rank.csv"),
                                new LeaderboardRankDatabaseAdapter()).importFile();
    }

    @Test
    public void importFile_SolvingAttempt_ImportedWithoutError() throws Exception {
        new DatabaseCsvImporter(getPathToFile("solving_attempt.csv"), new SolvingAttemptDatabaseAdapter()).importFile();
    }

    @Test
    public void importFile_Statistics_ImportedWithoutError() throws Exception {
        new DatabaseCsvImporter(getPathToFile("statistics.csv"), new StatisticsDatabaseAdapter()).importFile();
    }
}