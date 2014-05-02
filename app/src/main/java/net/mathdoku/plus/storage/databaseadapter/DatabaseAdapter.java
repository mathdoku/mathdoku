package net.mathdoku.plus.storage.databaseadapter;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseTableDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;

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

	public boolean createTable() {
		try {
			execAndLogSQL(getDatabaseTableDefinition().getCreateTableSQL());
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(String.format(
					"Table %s already exists. Cannot create table.",
					getTableName()), e);
		}
		return isExistingDatabaseTable();
	}

	protected abstract void upgradeTable(int oldVersion, int newVersion);

	protected abstract DatabaseTableDefinition getDatabaseTableDefinition();

	public String getTableName() {
		return getDatabaseTableDefinition().getTableName();
	}

	/**
	 * Get the definition for the given table as it is currently stored in the
	 * database.
	 * 
	 * @return The definition for the given table as it is currently stored in
	 *         the database. Empty string if the table does not yet exist.
	 */
	public String getCurrentDefinitionOfDatabaseTable() {
		final String columnSql = "sql";
		String[] columns = { columnSql };

		Cursor cursor = sqliteDatabase.query(
				true,
				"sqlite_master",
				columns,
				"name = " + DatabaseUtil.stringBetweenQuotes(getTableName())
						+ " AND type = "
						+ DatabaseUtil.stringBetweenQuotes("table"), null,
				null, null, null, null);
		if (cursor != null) {
			String sql = (cursor.moveToFirst() ? cursor.getString(cursor
					.getColumnIndexOrThrow(columnSql)) : "");
			cursor.close();
			return sql;
		}
		return "";
	}

	/**
	 * Checks whether the given table does exist in the database.
	 * 
	 * @return True if a table with the given name exists. False otherwise.
	 */
	public boolean isExistingDatabaseTable() {
		return !getCurrentDefinitionOfDatabaseTable().isEmpty();
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
		if (getCurrentDefinitionOfDatabaseTable().equalsIgnoreCase(
				getDatabaseTableDefinition().getCreateTableSQL())) {
			return false;
		}

		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			if (isExistingDatabaseTable()) {
				Log.e(TAG, String.format(
						"Change in table '%s' detected. Table has not yet been "
								+ "upgraded.", getTableName()));
				Log.e(TAG, "Database-version: "
						+ getCurrentDefinitionOfDatabaseTable());
			} else {
				Log.e(TAG, String.format("Table '%s' not found in database.",
						getTableName()));
			}

			Log.e(TAG, "Expected table definition: "
					+ getDatabaseTableDefinition()
							.getCreateTableSQL()
							.toUpperCase());
		}

		return true;
	}

	/**
	 * Drop the table and create a table for this database adapter. This method
	 * is only available when running in development way.
	 */
	protected void recreateTableInDevelopmentMode() {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			dropTable();
			createTable();
		}
	}

	public boolean dropTable() {
		if (!isExistingDatabaseTable()) {
			return false;
		}

		try {
			execAndLogSQL("DROP TABLE " + getTableName());
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(String.format(
					"Table %s does not exist. Cannot drop table.",
					getTableName()), e);
		}

		return !isExistingDatabaseTable();
	}

	private void execAndLogSQL(String sql) {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			Log.i(TAG, "Executing SQL: " + sql);
		}
		sqliteDatabase.execSQL(sql);
	}

	/**
	 * Get the column names as used in the table as it is currently stored in
	 * the database.
	 * 
	 * @return The list of columns in the given table.
	 */
	public List<String> getActualTableColumns() {
		// Retrieve columns
		String cmd = "pragma table_info(" + getTableName() + ");";
		Cursor cur = sqliteDatabase.rawQuery(cmd, null);

		// Convert columns to list.
		List<String> columns = new ArrayList<String>();
		while (cur.moveToNext()) {
			columns.add(cur.getString(cur.getColumnIndex("name")));
		}
		cur.close();

		return columns;
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
}