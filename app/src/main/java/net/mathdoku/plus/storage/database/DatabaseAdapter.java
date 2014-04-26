package net.mathdoku.plus.storage.database;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic database adapter. All other database adapters should be extend from
 * this base class.
 */
abstract class DatabaseAdapter {
	@SuppressWarnings("unused")
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
	static String createColumn(String column, String dataType, String constraint) {
		validateStringParameterNotNullOrEmpty(column);
		validateStringParameterNotNullOrEmpty(dataType);

		StringBuilder columnDefinition = new StringBuilder();
		String space = " ";
		columnDefinition.append(stringBetweenBackTicks(column.trim()));
		columnDefinition.append(space);
		columnDefinition.append(dataType.trim());
		if (constraint != null) {
			columnDefinition.append(space);
			columnDefinition.append(constraint.trim());
		}
		return columnDefinition.toString();
	}

	private static void validateStringParameterNotNullOrEmpty(
			String parameterValue) {
		if (parameterValue == null || parameterValue.trim().isEmpty()) {
			throw new InvalidParameterException(String.format(
					"Parameter has invalid value '%s'.", parameterValue));
		}
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
	 * @return The definition for one column in a SQLite table.
	 */
	static String createColumn(String column, String dataType) {
		return createColumn(column, dataType, "");
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
			String refersToColumn) {
		validateStringParameterNotNullOrEmpty(column);
		validateStringParameterNotNullOrEmpty(refersToTable);
		validateStringParameterNotNullOrEmpty(refersToColumn);

		StringBuilder foreignKey = new StringBuilder();
		foreignKey.append(" FOREIGN KEY(");
		foreignKey.append(stringBetweenBackTicks(column.trim()));
		foreignKey.append(") REFERENCES ");
		foreignKey.append(refersToTable.trim());
		foreignKey.append("(");
		foreignKey.append(refersToColumn.trim());
		foreignKey.append(")");
		return foreignKey.toString();
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
	 */
	static String createTable(String table, String... elements) {
		validateStringParameterNotNullOrEmpty(table);
		if (elements == null || elements.length < 1) {
			throw new InvalidParameterException(
					"At least one element has to be specified.");
		}

		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE ");
		query.append(stringBetweenBackTicks(table));
		query.append(" (");
		for (int i = 0; i < elements.length; i++) {
			validateStringParameterNotNullOrEmpty(elements[i]);
			query.append(elements[i]);
			query.append(i < elements.length - 1 ? ", " : ")");
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
				String dropSql = getDropTableSQL(table);
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

	static String getDropTableSQL(String table) {
		return "DROP TABLE " + table;
	}

	public String getDropTableSQL() {
		return getDropTableSQL(getTableName());
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
	static List<String> getTableColumns(SQLiteDatabase sqliteDatabase,
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
		return getTableColumns(mSqliteDatabase, getTableName());
	}
}