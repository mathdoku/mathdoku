package net.cactii.mathdoku.storage.database;

import java.security.InvalidParameterException;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Basic database adapter. All other database adapters should be extend from
 * this base class.
 */
public class DatabaseAdapter {
	static final String TAG = "MathDoku.DatabaseAdapter";

	static final char BACK_TICK = '`';
	static final char QUOTE = '\'';
	
	static final String SQLITE_TRUE = "true";
	static final String SQLITE_FALSE = "false";

	/**
	 * Generates a SQLite column definition. This method should best be used in
	 * conjunction with method createTable.
	 * 
	 * @param column
	 *            Name of column.
	 * @param datatype
	 *            Datatype of new column. Must be a valid SQLite datatype: blob,
	 *            bool, char, datetime, double, float, integer, numeric, real,
	 *            text or varchar.
	 * @param constraint
	 *            Optional constraint or modifiers for column. For example:
	 *            "not null" or "primary key autoincrement".
	 * @return The definition for one column in a SQLite table.
	 */
	public static String createColumn(String column, String datatype,
			String constraint) throws InvalidParameterException {

		if (column == null || column.trim().equals("")) {
			Log.e(TAG,"Method createColumn has invalid parameter 'column' with value '" + column + "'.");
			throw new InvalidParameterException();
		}
		if (datatype == null || datatype.trim().equals("")) {
			Log.e(TAG,"Method createColumn has invalid parameter 'datatype' with value '" + datatype + "'.");
			throw new InvalidParameterException();
		}
		
		return stringBetweenBackTicks(column) + " " + datatype
				+ ((constraint != null && !constraint.equals("")) ? " " + constraint.trim() : "");
	}

	/**
	 * Generates a SQLite table definition. This method should best be used in
	 * conjunction with method createColumn.
	 * 
	 * @param table
	 *            Name of table.
	 * @param columns
	 *            1 or more column definitions which are preferably generated
	 *            with method createColumn.
	 * @return Definition for a SQLite table.
	 * @throws InvalidParameterException
	 */
	public static String createTable(String table, String... columns)
			throws InvalidParameterException {
		StringBuilder query = new StringBuilder();

		if (table == null || table.trim().equals("")) {
			Log.e(TAG,"Method createTable has invalid parameter 'table' with value '" + table + "'.");
			throw new InvalidParameterException();
		}
		if (columns == null || columns.length < 1) {
			Log.e(TAG, "Method createTable expects at least 1 column.");
			throw new InvalidParameterException();
		}

		query.append("CREATE TABLE IF NOT EXISTS "
				+ stringBetweenBackTicks(table) + " (");
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == null || columns[i].trim().equals("")) {
				Log.e(TAG,"Method createTable has invalid parameter 'columns[" + i + "]' with value '" + columns[i] + "'.");
				throw new InvalidParameterException();
			}
			query.append(columns[i] + (i < columns.length - 1 ? ", " : ");"));
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
	 * 
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
	 * 
	 * @return The string properly enclosed with quotes.
	 */
	public static String stringBetweenQuotes(String string) {
		return QUOTE + string + QUOTE;
	}

	public DatabaseHelper databaseHelper;

	public SQLiteDatabase db;

	/**
	 * @param context
	 *            The context in which the database adapter will be used.
	 */
	public DatabaseAdapter(DatabaseHelper databaseHelper) throws SQLException {
		db = databaseHelper.getWritableDatabase();
	}

	/**
	 * Converts a boolean value to a SQLite representation.
	 * 
	 * @param value: The boolean value to be converted.
	 * @return The string representation of the boolean value as it is stored in SQLite.
	 */
	public String booleanToSQLiteString(boolean value) {
		return (value ? SQLITE_TRUE : SQLITE_FALSE);
	}
}
