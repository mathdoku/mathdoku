package net.mathdoku.plus.storage.database;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic database adapter. All other database adapters should be extend from
 * this base class.
 */
abstract class DatabaseAdapter {
	private static final String TAG = DatabaseAdapter.class.getName();

	private static final char BACK_TICK = '`';
	private static final char QUOTE = '\'';

	private static final String SQLITE_TRUE = "true";
	private static final String SQLITE_FALSE = "false";

	final SQLiteDatabase mSqliteDatabase;

	/**
	 * Creates a new instance of {@link DatabaseAdapter}.
	 */
	DatabaseAdapter() {
		mSqliteDatabase = DatabaseHelper.getInstance().getWritableDatabase();
	}

	/**
	 * Generates a SQLite column definition. This method should best be used in
	 * conjunction with method createTable.
	 * 
	 * @param column
	 *            Name of column.
	 * @param dataType
	 *            Data type of new column. Must be a valid SQLite data type:
	 *            blob, bool, char, datetime, double, float, integer, numeric,
	 *            real, text or varchar.
	 * @param constraint
	 *            Optional constraint or modifiers for column. For example:
	 *            "not null" or "primary key autoincrement".
	 * @return The definition for one column in a SQLite table.
	 */
	static String createColumn(String column, String dataType, String constraint)
			throws InvalidParameterException {

		if (column == null || column.trim().equals("")) {
			Log.e(TAG,
					"Method createColumn has invalid parameter 'column' with value '"
							+ column + "'.");
			throw new InvalidParameterException();
		}
		if (dataType == null || dataType.trim().equals("")) {
			Log.e(TAG,
					"Method createColumn has invalid parameter 'data type' with value '"
							+ dataType + "'.");
			throw new InvalidParameterException();
		}

		return stringBetweenBackTicks(column)
				+ " "
				+ dataType
				+ (constraint != null && !constraint.equals("") ? " "
						+ constraint.trim() : "");
	}

	/**
	 * Generates a SQLite foreign key constraint. This method should best be
	 * used in conjunction with method createTable.
	 * 
	 * @param column
	 *            Name of column in the table to be created.
	 * @param refersToTable
	 *            The name of the table which is referenced by this foreign key.
	 * @param refersToColumn
	 *            The name of the column in the reference table to which this
	 *            foreign key maps.
	 * @return A SQLite foreign key constraint
	 */
	@SuppressWarnings("SameParameterValue")
	static String createForeignKey(String column, String refersToTable,
			String refersToColumn) throws InvalidParameterException {

		if (column == null || column.trim().equals("")) {
			Log.e(TAG,
					"Method createForeignKey has invalid parameter 'column' with value '"
							+ column + "'.");
			throw new InvalidParameterException();
		}
		if (refersToTable == null || refersToTable.trim().equals("")) {
			Log.e(TAG,
					"Method createForeignKey has invalid parameter 'refersToTable' with value '"
							+ refersToTable + "'.");
			throw new InvalidParameterException();
		}
		if (refersToColumn == null || refersToColumn.trim().equals("")) {
			Log.e(TAG,
					"Method createForeignKey has invalid parameter 'refersToColumn' with value '"
							+ refersToColumn + "'.");
			throw new InvalidParameterException();
		}

		return " FOREIGN KEY(" + stringBetweenBackTicks(column.trim())
				+ ") REFERENCES " + refersToTable.trim() + "("
				+ refersToColumn.trim() + ")";
	}

	/**
	 * Generates a SQLite table definition. This method should best be used in
	 * conjunction with method createColumn.
	 * 
	 * @param table
	 *            Name of table.
	 * @param elements
	 *            1 or more column definitions and or foreign keys which are
	 *            preferably generated with method createColumn and
	 *            createForeignKey. Foreign keys should be ordered after the
	 *            columns.
	 * @return Definition for a SQLite table.
	 * @throws InvalidParameterException
	 */
	static String createTable(String table, String... elements)
			throws InvalidParameterException {
		StringBuilder query = new StringBuilder();

		if (table == null || table.trim().equals("")) {
			Log.e(TAG,
					"Method createTable has invalid parameter 'table' with value '"
							+ table + "'.");
			throw new InvalidParameterException();
		}
		if (elements == null || elements.length < 1) {
			Log.e(TAG, "Method createTable expects at least 1 column.");
			throw new InvalidParameterException();
		}

		// noinspection StringConcatenationInsideStringBufferAppend
		query.append("CREATE TABLE " + stringBetweenBackTicks(table) + " (");
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] == null || elements[i].trim().equals("")) {
				Log.e(TAG, "Method createTable has invalid parameter 'columns["
						+ i + "]' with value '" + elements[i] + "'.");
				throw new InvalidParameterException();
			}
			// noinspection StringConcatenationInsideStringBufferAppend
			query.append(elements[i] + (i < elements.length - 1 ? ", " : ")"));
		}

		return query.toString();
	}

	/**
	 * Encloses a string with back ticks (`). To be used for SQLite table and
	 * column names.
	 * 
	 * @param string
	 *            The string which needs to be prefixed and suffixed with back
	 *            ticks.
	 * @return The string properly enclosed with back ticks.
	 */
	public static String stringBetweenBackTicks(String string) {
		return BACK_TICK + string + BACK_TICK;
	}

	/**
	 * Encloses a string with quotes ('). To be used for string values in
	 * SQlite.
	 * 
	 * @param string
	 *            The string which needs to be prefixed and suffixed with
	 *            quotes.
	 * @return The string properly enclosed with quotes.
	 */
	static String stringBetweenQuotes(String string) {
		return QUOTE + string + QUOTE;
	}

	/**
	 * Converts a boolean value to a SQLite representation.
	 * 
	 * @param value
	 *            : The boolean value to be converted.
	 * @return The string representation of the boolean value as it is stored in
	 *         SQLite.
	 */
	static String toSQLiteBoolean(boolean value) {
		return value ? SQLITE_TRUE : SQLITE_FALSE;
	}

	/**
	 * Converts a SQLite boolean representation to a boolean.
	 * 
	 * @param value
	 *            The boolean string value to be converted.
	 * @return The boolean value corresponding with the SQLite string value.
	 */
	static boolean valueOfSQLiteBoolean(String value) {
		return value.equals(SQLITE_TRUE);
	}

	/**
	 * Converts a datetime value from a long to a SQLite representation.
	 * 
	 * @param value
	 *            The long value to be converted.
	 * @return The string representation of the long value as it is stored in
	 *         SQLite.
	 */
	static String toSQLiteTimestamp(long value) {
		return new java.sql.Timestamp(value).toString();
	}

	/**
	 * Converts a datetime value from a string to a SQL timestamp
	 * representation.
	 * 
	 * @param value
	 *            The string value to be converted.
	 * @return The SQL timestamp representation of the string value.
	 */
	static java.sql.Timestamp toSQLTimestamp(String value) {
		if (value == null) {
			return new java.sql.Timestamp(0);
		} else {
			return java.sql.Timestamp.valueOf(value);
		}
	}

	/**
	 * Converts a string value from the SQLite representation to a long.
	 * 
	 * @param value
	 *            The string value to be converted.
	 * @return The long value representing the given string value. 0 in case a
	 *         null value was passed.
	 */
	static long valueOfSQLiteTimestamp(String value) {
		return value == null ? 0 : java.sql.Timestamp.valueOf(value).getTime();
	}

	/**
	 * Get the table name.
	 * 
	 * @return The name of the table.
	 */
	protected abstract String getTableName();

	/**
	 * Get the table definition.
	 * 
	 * @return The definition of the table.
	 */
	protected abstract String getCreateSQL();

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

		Cursor cursor = mSqliteDatabase.query(true, "sqlite_master", columns,
				"name = " + stringBetweenQuotes(getTableName())
						+ " AND type = " + stringBetweenQuotes("table"), null,
				null, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				// Table exists. Check if definition matches with expected
				// definition.
				// noinspection ConstantConditions
				String sql = cursor.getString(
						cursor.getColumnIndexOrThrow(columnSql)).toUpperCase();
				String expectedSql = getCreateSQL().toUpperCase();
				tableDefinitionChanged = !sql.equals(expectedSql);

				if (tableDefinitionChanged) {
					Log
							.e(TAG, String.format(
									"Change in table '%s' detected. Table has not yet been " +
											"upgraded.",
									getTableName()));
					Log.e(TAG, "Database-version: " + sql);
					Log.e(TAG, "Expected version: " + expectedSql);
				}
			}
			cursor.close();
		} else {
			// Table does not exist.
			Log.e(TAG, "Table '" + getTableName() + "' not found in database.");
			Log.e(TAG, "Expected version: " + getCreateSQL());
			tableDefinitionChanged = true;
		}

		return tableDefinitionChanged;
	}

	/**
	 * Drop the given table and create a new table with the given SQL. This
	 * method is only available when running in development way.
	 * 
	 * @param table
	 *            The table to be dropped.
	 * @param createSql
	 *            The SQL statement to recreate the table.
	 */
	protected static void recreateTableInDevelopmentMode(SQLiteDatabase db,
			String table, String createSql) {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			try {
				String dropSql = "DROP TABLE " + table;
				execAndLogSQL(db, dropSql);
			} catch (SQLiteException e) {
				Log
						.i(TAG,
								String
										.format("Table %s does not exist. Cannot drop table (not an error.",
												table), e);
			}
			execAndLogSQL(db, createSql);
		}
	}

	private static void execAndLogSQL(SQLiteDatabase db, String sql) {
		Log.i(TAG, "Executing SQL: " + sql);
		db.execSQL(sql);
	}

	/**
	 * Drops one or more columns from the table.
	 * 
	 * @param sqliteDatabase
	 *            The database to be used by the adapter.
	 * @param tableName
	 *            The table name.
	 * @param columnsToDropped
	 *            The array of columns to be dropped.
	 * @param createSQL
	 *            The SQL statement to create the new version of the table.
	 * @return True in case the table is recreated. False otherwise.
	 */
	protected static boolean dropColumn(SQLiteDatabase sqliteDatabase,
			String tableName, String[] columnsToDropped, String createSQL) {
		// Check if columns to be dropped has been specified.
		if (columnsToDropped == null || columnsToDropped.length == 0) {
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
				throw new DatabaseException(TAG
						+ ".dropColumn has invalid parameter.'");
			} else {
				return false;
			}
		}

		// Build the list of columns for the new version of the table.
		List<String> currentColumnList = getTableColumns(sqliteDatabase,
				tableName);
		List<String> newColumnList = new ArrayList<String>(currentColumnList);
		if (!newColumnList.removeAll(Arrays.asList(columnsToDropped))
				|| newColumnList.isEmpty()
				|| newColumnList.equals(currentColumnList)) {
			// Can not delete.
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
				throw new DatabaseException(TAG
						+ ".dropColumn can not drop columns '"
						+ Arrays.toString(columnsToDropped) + "'.");
			} else {
				return false;
			}
		}

		// Start a new transaction.
		sqliteDatabase.beginTransaction();

		try {
			// Rename current table to temporary table.
			sqliteDatabase.execSQL("ALTER TABLE " + tableName + " RENAME TO "
					+ tableName + "_old;");

			// Create the (new version of the) table on its new format (no
			// redundant
			// columns).
			sqliteDatabase.execSQL(createSQL);

			String newColumnsString = TextUtils.join(",", newColumnList);

			// Populating the (new version of the) table with the data from the
			// temporary table.
			sqliteDatabase.execSQL("INSERT INTO " + tableName + "("
					+ newColumnsString + ") SELECT " + newColumnsString
					+ " FROM " + tableName + "_old;");

			// Drop the temporary table.
			sqliteDatabase.execSQL("DROP TABLE " + tableName + "_old;");

			// Commit changes.
			sqliteDatabase.setTransactionSuccessful();

		} catch (SQLException e) {
			Log.wtf(TAG, String.format(
					"Error while dropping column(s) from table %s", tableName),
					e);
			return false;
		} finally {
			sqliteDatabase.endTransaction();
		}
		return true;
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
}