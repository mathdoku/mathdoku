package net.mathdoku.plus.storage.databaseadapter;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class DatabaseHelperTest {
	private Activity activity;
	private DatabaseHelper databaseHelper;
	private boolean needToTearDownTestRunnerHelper;

	private class DatabaseHelperStub extends DatabaseHelper {
		private final DatabaseAdapter[] databaseAdapters;

		DatabaseHelperStub(Context context, DatabaseAdapter... databaseAdapters) {
			super(context);
			this.databaseAdapters = databaseAdapters;
		}

		@Override
		DatabaseAdapter[] getAllDatabaseAdapters(SQLiteDatabase db) {
			return databaseAdapters;
		}
	}

	@Before
	public void setup() {
		// For this class it is not possible to initialize the TestRunnerHelper
		// for each method as some methods require the database to be closed.
		activity = new Activity();
		needToTearDownTestRunnerHelper = false;
	}

	@After
	public void tearDown() {
		if (needToTearDownTestRunnerHelper) {
			TestRunnerHelper.tearDown();
		} else if (databaseHelper != null) {
			// Database helper was opened directly by this test class.
			databaseHelper.close();
		}
	}

	@Test
	public void getInstance_InstantiateWithContext_Instantiated()
			throws Exception {
		initTestRunnerHelper();
		assertThat(databaseHelper, is(notNullValue()));
	}

	private void initTestRunnerHelper() {
		TestRunnerHelper.setup(this.getClass().getCanonicalName());
		databaseHelper = TestRunnerHelper.getDatabaseHelper();
		activity = TestRunnerHelper.getActivity();
		needToTearDownTestRunnerHelper = true;
	}

	@Test
	public void getInstance_InstantiateTwiceWithDifferentContext_SameInstance()
			throws Exception {
		initTestRunnerHelper();
		assertThat(DatabaseHelper.getInstance(new Activity()),
				is(sameInstance(databaseHelper)));
	}

	@Test
	public void getInstance_InstantiateTwiceWithSameContext_SameInstance()
			throws Exception {
		initTestRunnerHelper();
		assertThat(DatabaseHelper.getInstance(activity),
				is(sameInstance(databaseHelper)));
	}

	public void getInstance_NotInstantiatedBefore_ThrowsSingletonInstanceNotInstantiatedException()
			throws Exception {
		databaseHelper = DatabaseHelper.getInstance();
	}

	public void getInstance_InstantiatedBefore_SameInstance() throws Exception {
		initTestRunnerHelper();
		assertThat(DatabaseHelper.getInstance(),
				is(sameInstance(databaseHelper)));
	}

	@Test
	public void onCreate() throws Exception {
		// Use same mock for multiple tables
		DatabaseAdapter databaseAdapterMock = mock(DatabaseAdapter.class);
		DatabaseAdapter[] databaseAdapters = new DatabaseAdapter[] {
				databaseAdapterMock, databaseAdapterMock };

		databaseHelper = replaceWithNewDatabaseHelperStub(databaseAdapters);

		// When getting the writeable database while the database does not yet
		// exists, results in 1) creating a new writeable database and 2)
		// calling DatabaseHelper.onCreate to instantiate the new database. Each
		// time the new database is created each time the unit test runs, the
		// database does not yet exists.
		databaseHelper.getWritableDatabase();

		verify(databaseAdapterMock, times(databaseAdapters.length))
				.createTable();
	}

	private DatabaseHelperStub replaceWithNewDatabaseHelperStub(
			DatabaseAdapter[] databaseAdapters) {
		// Use a stub for the database helper which uses database adapter mocks
		// instead of real tables.
		return new DatabaseHelperStub(activity, databaseAdapters);
	}

	@Test
	public void onUpgrade() throws Exception {
		// Use same mock for multiple tables
		DatabaseAdapter databaseAdapterMock = mock(DatabaseAdapter.class);
		DatabaseAdapter[] databaseAdapters = new DatabaseAdapter[] {
				databaseAdapterMock, databaseAdapterMock };

		databaseHelper = replaceWithNewDatabaseHelperStub(databaseAdapters);

		databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), 1, 3);
		verify(databaseAdapterMock, times(databaseAdapters.length))
				.upgradeTable(anyInt(), anyInt());
	}

	@Test
	public void hasChangedTableDefinitions_TableDefinitionsHaveNotChanged_False()
			throws Exception {
		// Use same mock for multiple tables
		DatabaseAdapter databaseAdapterMock = mock(DatabaseAdapter.class);
		when(databaseAdapterMock.isTableDefinitionChanged()).thenReturn(false);
		DatabaseAdapter[] databaseAdapters = new DatabaseAdapter[] {
				databaseAdapterMock, databaseAdapterMock };

		databaseHelper = replaceWithNewDatabaseHelperStub(databaseAdapters);

		assertThat(databaseHelper.hasChangedTableDefinitions(), is(false));
		verify(databaseAdapterMock, times(databaseAdapters.length))
				.isTableDefinitionChanged();
	}

	@Test
	public void hasChangedTableDefinitions_TableDefinitionsHaveChanged_True()
			throws Exception {
		// Use different mocks for different tables
		DatabaseAdapter databaseAdapterMock1 = mock(DatabaseAdapter.class);
		when(databaseAdapterMock1.isTableDefinitionChanged()).thenReturn(false);
		DatabaseAdapter databaseAdapterMock2 = mock(DatabaseAdapter.class);
		when(databaseAdapterMock1.isTableDefinitionChanged()).thenReturn(true);
		DatabaseAdapter[] databaseAdapters = new DatabaseAdapter[] {
				databaseAdapterMock1, databaseAdapterMock2 };

		databaseHelper = replaceWithNewDatabaseHelperStub(databaseAdapters);

		assertThat(databaseHelper.hasChangedTableDefinitions(), is(true));
	}
}
