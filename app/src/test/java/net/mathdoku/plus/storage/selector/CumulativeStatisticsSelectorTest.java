package net.mathdoku.plus.storage.selector;

import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import databasehelper.CsvImporter;
import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class CumulativeStatisticsSelectorTest {
	@Before
	public void setUp() throws Exception {
		TestRunnerHelper.setup(this.getClass().getCanonicalName());

		new CsvImporter("grid.csv", new GridDatabaseAdapter()).importIntoDatabase();
	}

	@After
	public void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	@Test
	public void invoke() throws Exception {
		assertThat(new GridDatabaseAdapter().get(1), is(nullValue()));
	}
}
