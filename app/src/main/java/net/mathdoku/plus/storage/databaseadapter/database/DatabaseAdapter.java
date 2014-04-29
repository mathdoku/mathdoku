package net.mathdoku.plus.storage.databaseadapter.database;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.storage.databaseadapter.database.database.DatabaseTableDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.database.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic database adapter. All other database adapters should be extend from
 * this base class.
 */
public abstract class DatabaseAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = DatabaseAdapter.class.getName();

	protected final SQLiteDatabase sqliteDatabase;

	public DatabaseAdapter() {
		sqliteDatabase = DatabaseHelper.getInstance().getWritableDatabase();
	}

	// Package private access, intended for DatabaseHelper only
	DatabaseAdapter(SQLiteDatabase sqLiteDatabase) {
		sqliteDatabase = sqLiteDatabase;
	}

	// Package private access, intended for DatabaseHelper only
	static final DatabaseAdapter[] getAllDatabaseAdapters(
			SQLiteDatabase sqLiteDatabase) {
		return new DatabaseAdapter[] { new GridDatabaseAdapter(sqLiteDatabase),
				new SolvingAttemptDatabaseAdapter(sqLiteDatabase),
				new StatisticsDatabaseAdapter(sqLiteDatabase),
				new LeaderboardRankDatabaseAdapter(sqLiteDatabase) };
	}

	void createTable() {
		String sql = getCreateSQL();
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			Log.i(TAG, sql);
		}

		// Execute create statement
		sqliteDatabase.execSQL(sql);
	}

	protected abstract void upgradeTable(int oldVersion, int newVersion);

	protected abstract DatabaseTableDefinition getDatabaseTableDefinition();

	public String getTableName() {
		return getDatabaseTableDefinition().getTableName();
	}

	public String getCreateSQL() {
		return getDatabaseTableDefinition().getCreateTableSQL();
	}

	/**
	 * Checks the table definition, of a table which has been created in the
	 * database before, with the expected table definition as defined in the
	 * software.
	 * 
	 * @return True in case the definition of an already existing table does not
	 *         match with the expected definition. False otherwise.
	 */
	@SuppressLint("DefaultLocale")
	public boolean isTableDefinitionChanged() {
		boolean tableDefinitionChanged = false;

		final String columnSql = "sql";
		String[] columns = { columnSql };

		Cursor cursor = sqliteDatabase.query(true, "sqlite_master", columns,
				"name = " + stringBetweenQuotes(getTableName())
						+ " AND type = " + stringBetweenQuotes("table"), null,
				null, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				// Table exists. Check if definition matches with expected
				// definition.
				String sql = cursor.getString(
						cursor.getColumnIndexOrThrow(columnSql)).toUpperCase();
				String expectedSql = getCreateSQL().toUpperCase();
				tableDefinitionChanged = !sql.equals(expectedSql);
				if (Config.mAppMode == AppMode.DEVELOPMENT
						&& tableDefinitionChanged) {
					Log.e(TAG, String.format(
							"Change in table '%s' detected. Table has not yet been "
									+ "upgraded.", getTableName()));
					Log.e(TAG, "Database-version: " + sql);
				}
			}
			cursor.close();
		} else {
			// Table does not exist.
			Log.e(TAG, String.format("Table '%s' not found in database.",
					getTableName()));
			tableDefinitionChanged = true;
		}
		if (Config.mAppMode == AppMode.DEVELOPMENT && tableDefinitionChanged) {
			Log.e(TAG, "Expected table definition: " + getCreateSQL());
		}

		return tableDefinitionChanged;
	}

	/**
	 * Drop the table and create a table for this database adapter. This method
	 * is only available when running in development way.
	 */
	protected void recreateTableInDevelopmentMode() {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			try {
				execAndLogSQL(sqliteDatabase, getDropTableSQL());
			} catch (SQLiteException e) {
				Log
						.i(TAG,
								String
										.format("Table %s does not exist. Cannot drop table (not necessarily an error).",
												getTableName()), e);
			}
			execAndLogSQL(sqliteDatabase, getCreateSQL());
		}
	}

	public String getDropTableSQL() {
		return "DROP TABLE " + getTableName();
	}

	private static void execAndLogSQL(SQLiteDatabase db, String sql) {
		Log.i(TAG, "Executing SQL: " + sql);
		db.execSQL(sql);
	}

	/**
	 * Get the actual column names for the given table.
	 * 
	 * @param sqliteDatabase
	 *            The database to be used by the adapter.
	 * @param tableName
	 *            The table for which the actual columns have to be determined.
	 * @return The list of columns in the given table.
	 */
	private static List<String> getTableColumns(SQLiteDatabase sqliteDatabase,
			String tableName) {
		// Retrieve columns
		String cmd = "pragma table_info(" + tableName + ");";
		Cursor cur = sqliteDatabase.rawQuery(cmd, null);

		// Convert columns to list.
		List<String> columns = new ArrayList<String>();
		while (cur.moveToNext()) {
			columns.add(cur.getString(cur.getColumnIndex("name")));
		}
		cur.close();

		return columns;
	}

	public List<String> getTableColumns() {
		return getTableColumns(sqliteDatabase, getTableName());
	}

	public void beginTransaction() {
		sqliteDatabase.beginTransaction();
	}

	public void endTransaction() {
		sqliteDatabase.endTransaction();
	}

	public void setTransactionSuccessful() {
		sqliteDatabase.setTransactionSuccessful();
	}

	public void execSQL(String sql) {
		sqliteDatabase.execSQL(sql);
	}

	// Convenience method
	protected static String stringBetweenBackTicks(String string) {
		return DatabaseUtil.stringBetweenBackTicks(string);
	}

	// Convenience method
	protected static String stringBetweenQuotes(String string) {
		return DatabaseUtil.stringBetweenQuotes(string);
	}

	// Convenience method
	protected static String toSQLiteBoolean(boolean value) {
		return DatabaseUtil.toSQLiteBoolean(value);
	}

	// Convenience method
	protected static boolean valueOfSQLiteBoolean(String value) {
		return DatabaseUtil.valueOfSQLiteBoolean(value);
	}

	// Convenience method
	protected static String toSQLiteTimestamp(long value) {
		return DatabaseUtil.toSQLiteTimestamp(value);
	}

	// Convenience method
	protected static java.sql.Timestamp toSQLTimestamp(String value) {
		return DatabaseUtil.toSQLTimestamp(value);
	}

	// Convenience method
	protected static long valueOfSQLiteTimestamp(String value) {
		return DatabaseUtil.valueOfSQLiteTimestamp(value);
	}
}