package net.cactii.mathdoku.storage.database;

import java.security.InvalidParameterException;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Basic database adapter. All other database adapters should be extend from
 * this base class.
 */
public abstract class DatabaseAdapter {
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
			Log.e(TAG,
					"Method createColumn has invalid parameter 'column' with value '"
							+ column + "'.");
			throw new InvalidParameterException();
		}
		if (datatype == null || datatype.trim().equals("")) {
			Log.e(TAG,
					"Method createColumn has invalid parameter 'datatype' with value '"
							+ datatype + "'.");
			throw new InvalidParameterException();
		}

		return stringBetweenBackTicks(column)
				+ " "
				+ datatype
				+ ((constraint != null && !constraint.equals("")) ? " "
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
	public static String createForeignKey(String column, String refersToTable,
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
	public static String createTable(String table, String... elements)
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

		query.append("CREATE TABLE " + stringBetweenBackTicks(table) + " (");
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] == null || elements[i].trim().equals("")) {
				Log.e(TAG, "Method createTable has invalid parameter 'columns["
						+ i + "]' with value '" + elements[i] + "'.");
				throw new InvalidParameterException();
			}
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
	 * @param value
	 *            : The boolean value to be converted.
	 * @return The string representation of the boolean value as it is stored in
	 *         SQLite.
	 */
	public String toSQLiteBoolean(boolean value) {
		return (value ? SQLITE_TRUE : SQLITE_FALSE);
	}

	/**
	 * Converts a SQLite boolean representation to a boolean.
	 * 
	 * @param value
	 *            The boolean string value to be converted.
	 * @return The boolean value corresponding with the SQLite string value.
	 */
	public boolean valueOfSQLiteBoolean(String value) {
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
	public String toSQLiteTimestamp(long value) {
		return new java.sql.Timestamp(value).toString();
	}

	/**
	 * Converts a string value from the SQLite representation to a long.
	 * 
	 * @param value
	 *            The string value to be converted.
	 * @return The long value representing the given string value.
	 */
	public long valueOfSQLiteTimestamp(String value) {
		return java.sql.Timestamp.valueOf(value).getTime();
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
	public boolean isTableDefinitionChanged() {
		boolean tableDefinitionChanged = false;

		final String KEY_SQL = "sql";
		String columns[] = { KEY_SQL };

		Cursor cursor = db.query(true, "sqlite_master", columns, "name = "
				+ stringBetweenQuotes(getTableName()) + " AND type = "
				+ stringBetweenQuotes("table"), null, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String sql = cursor
					.getString(cursor.getColumnIndexOrThrow(KEY_SQL));
			tableDefinitionChanged = !sql.equals(getCreateSQL());

			if (tableDefinitionChanged) {
				Log.e(TAG, "Change in table '" + getTableName()
						+ "' detected. Table has not yet been upgraded.");
				Log.e(TAG, "Database-version: " + sql);
				Log.e(TAG, "Expected version: " + getCreateSQL());
			}
		}
		cursor.close();

		return tableDefinitionChanged;
	}
}
