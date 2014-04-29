package net.mathdoku.plus.storage.databaseadapter.database;

import android.app.Activity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class DatabaseColumnDropperTest {
	private DatabaseColumnDropper databaseColumnDropper;
	private TestTableDatabaseAdapter testTableDatabaseAdapter;

	private static final String KEY_TO_BE_KEPT_1 = "to_be_kept_column_1";
	private static final String KEY_TO_BE_KEPT_2 = "to_be_kept_column_2";
	private static final String KEY_TO_BE_DELETED_1 = "to_be_deleted_column_1";
	private static final String KEY_TO_BE_KEPT_3 = "to_be_kept_column_3";
	private static final String KEY_TO_BE_DELETED_2 = "to_be_deleted_column_2";

	private class TestTableDatabaseAdapter extends DatabaseAdapter {
		public TestTableDatabaseAdapter() {
			execSQL(getCreateSQL());
		}

		@Override
		protected void upgradeTable(int oldVersion, int newVersion) {
		}

		@Override
		protected DatabaseTableDefinition getDatabaseTableDefinition() {
			DatabaseTableDefinition databaseTableDefinition = new DatabaseTableDefinition(
					"TestTable");
			databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
					KEY_TO_BE_KEPT_1, DataType.STRING));
			databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
					KEY_TO_BE_KEPT_2, DataType.STRING));
			databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
					KEY_TO_BE_DELETED_1, DataType.STRING));
			databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
					KEY_TO_BE_KEPT_3, DataType.STRING));
			databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
					KEY_TO_BE_DELETED_2, DataType.STRING));
			return databaseTableDefinition;
		}
	}

	@Before
	public void setup() {
		Activity activity = new Activity();
		DatabaseHelper.getInstance(activity);

		testTableDatabaseAdapter = new TestTableDatabaseAdapter();
		databaseColumnDropper = new DatabaseColumnDropper(
				testTableDatabaseAdapter);
	}

	@After
	public void tearDown() {
		// Close the database helper. This ensure that the next test will use a
		// new DatabaseHelper instance with a new SQLite database connection.
		DatabaseHelper.getInstance().close();
	}

	@Test(expected = DatabaseException.class)
	public void dropColumns_NullColumnArray_ThrowsDatabaseException()
			throws Exception {
		assertThat(databaseColumnDropper.dropColumns(null), is(false));
	}

	@Test(expected = DatabaseException.class)
	public void dropColumns_EmptyColumnArray_ThrowsDatabaseException()
			throws Exception {
		assertThat(databaseColumnDropper.dropColumns(new String[] {}),
				is(false));
	}

	@Test(expected = DatabaseException.class)
	public void dropColumns_NullColumn_ThrowsDatabaseException()
			throws Exception {
		assertThat(databaseColumnDropper.dropColumns(new String[] { null }),
				is(false));
	}

	@Test(expected = DatabaseException.class)
	public void dropColumns_DropNonExistingColumn_ThrowsDatabaseException()
			throws Exception {
		assertThat(
				databaseColumnDropper
						.dropColumns(new String[] { "*** NON EXISTENT COLUMN ***" }),
				is(false));
	}

	@Test
	public void dropColumns_DropOneExistingColumn_Success() throws Exception {
		assertThat(
				databaseColumnDropper
						.dropColumns(new String[] { KEY_TO_BE_DELETED_2 }),
				is(true));
	}

	@Test
	public void dropColumns_DropTwoExistingColumns_Success() throws Exception {
		assertThat(
				databaseColumnDropper.dropColumns(new String[] {
						KEY_TO_BE_DELETED_1, KEY_TO_BE_DELETED_2 }), is(true));
	}

	@Test(expected = DatabaseException.class)
	public void dropColumns_DropAllColumns_ThrowsDatabaseException()
			throws Exception {
		assertThat(
				databaseColumnDropper.dropColumns(new String[] {
						KEY_TO_BE_KEPT_1, KEY_TO_BE_KEPT_2,
						KEY_TO_BE_DELETED_1, KEY_TO_BE_KEPT_3,
						KEY_TO_BE_DELETED_2 }), is(true));
	}
}
