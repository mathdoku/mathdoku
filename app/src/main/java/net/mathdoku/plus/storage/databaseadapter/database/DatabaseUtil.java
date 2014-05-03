package net.mathdoku.plus.storage.databaseadapter.database;

import java.util.Date;

public class DatabaseUtil {
	private static final char BACK_TICK = '`';
	private static final char QUOTE = '\'';

	private static final String SQLITE_TRUE = "true";
	private static final String SQLITE_FALSE = "false";

	// Package private access as for unit testing.
	private static long currentDateTime = -1;

	private DatabaseUtil() {
		// Prevent instantiation of utility class.
	}

	// Package private access as for unit testing.
	DatabaseUtil(long currentDateTime) {
		this.currentDateTime = currentDateTime;
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
	public static String stringBetweenQuotes(String string) {
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
	public static String toSQLiteBoolean(boolean value) {
		return value ? SQLITE_TRUE : SQLITE_FALSE;
	}

	public static String toQuotedSQLiteString(boolean value) {
		return stringBetweenQuotes(toSQLiteBoolean(value));
	}

	/**
	 * Converts a SQLite boolean representation to a boolean.
	 * 
	 * @param value
	 *            The boolean string value to be converted.
	 * @return The boolean value corresponding with the SQLite string value.
	 */
	public static boolean valueOfSQLiteBoolean(String value) {
		if (value.equals(SQLITE_TRUE)) {
			return true;
		}
		if (value.equals(SQLITE_FALSE)) {
			return false;
		}
		throw new IllegalStateException(String.format("Value %s is not a valid SQLite boolean value.", value));
	}

	/**
	 * Creates a SQLite representation of the current date and time.
	 * 
	 * @return The string representation of the current date and time as it is
	 *         stored in SQLite.
	 */
	public static String getCurrentSQLiteTimestamp() {
		return toSQLiteTimestamp(getCurrentDateTime());
	}

	private static long getCurrentDateTime() {
		return currentDateTime < 0 ? new Date().getTime() : currentDateTime;
	}

	/**
	 * Creates a SQLite representation of the current date and time minus a given offset.
	 *
	 * @param offsetInMillisBefore Offset in milliseconds before current date time.
	 * @return The string representation of the current date and time minus the given offset as it is
	 *         stored in SQLite.
	 */
	public static String getCurrentMinusOffsetSQLiteTimestamp(long offsetInMillisBefore) {
		return toSQLiteTimestamp(getCurrentDateTime() - offsetInMillisBefore);
	}

	/**
	 * Converts a datetime value from a long to a SQLite representation.
	 * 
	 * @param value
	 *            The long value to be converted.
	 * @return The string representation of the long value as it is stored in
	 *         SQLite.
	 */
	public static String toSQLiteTimestamp(long value) {
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
	public static java.sql.Timestamp toSQLTimestamp(String value) {
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
	public static long valueOfSQLiteTimestamp(String value) {
		return value == null ? 0 : java.sql.Timestamp.valueOf(value).getTime();
	}
}
