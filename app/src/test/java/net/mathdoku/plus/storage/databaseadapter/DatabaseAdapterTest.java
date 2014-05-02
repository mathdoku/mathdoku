package net.mathdoku.plus.storage.databaseadapter;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.storage.databaseadapter.database.DataType;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseColumnDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseTableDefinition;
import net.mathdoku.plus.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class DatabaseAdapterTest {
	DatabaseAdapterStub databaseAdapterStub;
	private static SQLiteDatabase sqLiteDatabase;
	private DatabaseTableDefinition databaseTableDefinition;
	private static final String TABLE_NAME = "UNIT_TEST";

	private class DatabaseAdapterStub extends DatabaseAdapter {
		private boolean hasAdditionalColumn = false;

		public DatabaseAdapterStub() {
			DatabaseAdapterTest.sqLiteDatabase = this.sqliteDatabase;
		}

		public void setAdditionalColumn() {
			hasAdditionalColumn = true;
		}

		@Override
		protected void upgradeTable(int oldVersion, int newVersion) {
		}

		@Override
		protected DatabaseTableDefinition getDatabaseTableDefinition() {
			databaseTableDefinition = new DatabaseTableDefinition(TABLE_NAME);
			databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
					"column", DataType.STRING));
			if (hasAdditionalColumn) {
				databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
						"additional_column", DataType.STRING));
			}
			databaseTableDefinition.build();

			return databaseTableDefinition;
		}
	}

	@Before
	public void setup() {
		// Instantiate singleton classes
		Activity activity = new Activity();
		new Util(activity);
		DatabaseHelper.getInstance(activity);

		databaseAdapterStub = new DatabaseAdapterStub();
	}

	@After
	public void tearDown() {
		// Close the database helper. This ensure that the next test will use a
		// new DatabaseHelper instance with a new SQLite database connection.
		DatabaseHelper.getInstance().close();
	}

	@Test
	public void getAllDatabaseAdapters() throws Exception {
		DatabaseAdapter[] databaseAdapters = DatabaseAdapterStub
				.getAllDatabaseAdapters(sqLiteDatabase);

		List<String> databaseAdapterClassNames = new ArrayList<String>();
		for (DatabaseAdapter databaseAdapter : databaseAdapters) {
			databaseAdapterClassNames.add(databaseAdapter
					.getClass()
					.getSimpleName());
		}
		Collections.sort(databaseAdapterClassNames);

		List<String> expectedDatabaseAdapterClassNames = Arrays
				.asList(new String[] {
						GridDatabaseAdapter.class.getSimpleName(),
						LeaderboardRankDatabaseAdapter.class.getSimpleName(),
						SolvingAttemptDatabaseAdapter.class.getSimpleName(),
						StatisticsDatabaseAdapter.class.getSimpleName() });
		Collections.sort(expectedDatabaseAdapterClassNames);

		assertThat(databaseAdapterClassNames,
				is(equalTo(expectedDatabaseAdapterClassNames)));
	}

	@Test
	public void createTable_TableDoesNotYetExist_TableIsCreated()
			throws Exception {
		assertThat(databaseAdapterStub.createTable(), is(true));
	}

	@Test(expected = DatabaseAdapterException.class)
	public void createTable_TableAlreadyExists_ThrowException()
			throws Exception {
		// First create will succeed as table does not yet exist.
		assertThat(databaseAdapterStub.createTable(), is(true));

		// On second create of same table an exception is thrown.
		assertThat(databaseAdapterStub.createTable(), is(false));
	}

	@Test
	public void upgradeTable() throws Exception {
		// Has to be tested for sub classes as it is an abstract function.
	}

	@Test
	public void getDatabaseTableDefinition() throws Exception {
		// Has to be tested for sub classes as it is an abstract function.
	}

	@Test
	public void getTableName() throws Exception {
		assertThat(databaseAdapterStub.getTableName(), is(TABLE_NAME));
	}

	@Test
	public void getCurrentDefinitionOfDatabaseTable() throws Exception {
		assertThat(databaseAdapterStub.getCurrentDefinitionOfDatabaseTable(),
				is(""));
		databaseAdapterStub.createTable();
		assertThat(databaseAdapterStub
				.getCurrentDefinitionOfDatabaseTable()
				.toUpperCase(), is(databaseTableDefinition
				.getCreateTableSQL()
				.toUpperCase()));
	}

	@Test
	public void isExistingDatabaseTable_TableDoesNotYetExist_False()
			throws Exception {
		assertThat(databaseAdapterStub.isExistingDatabaseTable(), is(false));
	}

	@Test
	public void isExistingDatabaseTable_TableDoesExist_True() throws Exception {
		databaseAdapterStub.createTable();
		assertThat(databaseAdapterStub.isExistingDatabaseTable(), is(true));
	}

	@Test
	public void isTableDefinitionChanged_TableExistsAndIsNotChanged_False()
			throws Exception {
		databaseAdapterStub.createTable();
		assertThat(databaseAdapterStub.isTableDefinitionChanged(), is(false));
	}

	@Test
	public void isTableDefinitionChanged_TableExistsAndIsChanged_True()
			throws Exception {
		assertThat(databaseAdapterStub.createTable(), is(true));

		DatabaseAdapterStub databaseAdapterStubWithAdditionalColumn = new DatabaseAdapterStub();
		databaseAdapterStubWithAdditionalColumn.setAdditionalColumn();
		assertThat(
				databaseAdapterStubWithAdditionalColumn
						.isTableDefinitionChanged(),
				is(true));
	}

	@Test
	public void recreateTableInDevelopmentMode() throws Exception {
		if (Config.mAppMode == Config.AppMode.DEVELOPMENT) {
			assertThat(databaseAdapterStub.createTable(), is(true));

			DatabaseAdapterStub databaseAdapterStubWithAdditionalColumn = new DatabaseAdapterStub();
			databaseAdapterStubWithAdditionalColumn.setAdditionalColumn();
			assertThat(
					databaseAdapterStubWithAdditionalColumn
							.isTableDefinitionChanged(),
					is(true));

			databaseAdapterStubWithAdditionalColumn
					.recreateTableInDevelopmentMode();

			assertThat(
					databaseAdapterStubWithAdditionalColumn
							.isTableDefinitionChanged(),
					is(false));
		}
	}

	@Test
	public void getActualTableColumns() throws Exception {
		assertThat(databaseAdapterStub.createTable(), is(true));
		assertThat(databaseAdapterStub.getActualTableColumns(),
				is(Arrays.asList(databaseTableDefinition.getColumnNames())));
	}
}
