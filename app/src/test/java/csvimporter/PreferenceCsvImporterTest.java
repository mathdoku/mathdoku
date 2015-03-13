package csvimporter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

@RunWith(RobolectricGradleTestRunner.class)
public class PreferenceCsvImporterTest {
    SharedPreferences sharedPreferences;

    @Before
    public void setUp() throws Exception {
        TestRunnerHelper.setup(this.getClass()
                                       .getCanonicalName());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TestRunnerHelper.getActivity());
    }

    @After
    public void tearDown() throws Exception {
        TestRunnerHelper.tearDown();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FilenameIsNull_ThrowsCsvImporterException() throws Exception {
        new PreferenceCsvImporter(null, sharedPreferences).importFile();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FilenameIsEmpty_ThrowsCsvImporterException() throws Exception {
        new PreferenceCsvImporter("", sharedPreferences).importFile();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_SharedPreferencesIsNull_ThrowsCsvImporterException() throws Exception {
        new PreferenceCsvImporter(getPathToFile("csvimportertest.csv"), null).importFile();
    }

    private String getPathToFile(String filename) {
        return "csvimporter/preferencecsvimporter/" + filename;
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FileDoesNotExist_ThrowsCsvImporterException() throws Exception {
        new PreferenceCsvImporter(getPathToFile("some non existing file.csv"), sharedPreferences).importFile();
    }

    @Test
    public void importFile_FileContainsNoErrors_Success() throws Exception {
        new PreferenceCsvImporter(getPathToFile("csvimportertest.csv"), sharedPreferences).importFile();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FileContainsLineWithInvalidDatatype_ThrowsCsvImporterException() throws Exception {
        new PreferenceCsvImporter(getPathToFile("csvimportertest_invalid_datatype.csv"),
                                  sharedPreferences).importFile();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FileContainsLineWithInvalidValue_ThrowsCsvImporterException() throws Exception {
        new PreferenceCsvImporter(getPathToFile("csvimportertest_invalid_value.csv"), sharedPreferences).importFile();
    }

    @Test(expected = CsvImporterException.class)
    public void importFile_FileContainsLineWithTooFewColumns_ThrowsCsvImporterException() throws Exception {
        new PreferenceCsvImporter(getPathToFile("csvimportertest_too_few_columns.csv"), sharedPreferences).importFile();
    }
}
