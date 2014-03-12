package net.mathdoku.plus.storage.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.enums.SolvingAttemptStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * The database adapter for the solving attempt table. For each grid one or more
 * solving attempt records can exists in the database. For grid created with
 * version 2 of this app, a statistics record will exist. For grids created with
 * an older version, statistics data is not available.
 */
public class SolvingAttemptDatabaseAdapter extends DatabaseAdapter {
	private static final String TAG = SolvingAttemptDatabaseAdapter.class
			.getName();

	// Remove "&& false" in following line to show the SQL-statements in the
	// debug information
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SQL = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// Columns for table
	static final String TABLE = "solving_attempt";
	static final String KEY_ROWID = "_id";
	static final String KEY_GRID_ID = "grid_id";
	private static final String KEY_DATE_CREATED = "date_created";
	private static final String KEY_DATE_UPDATED = "date_updated";
	private static final String KEY_SAVED_WITH_REVISION = "revision";
	private static final String KEY_DATA = "data";
	static final String KEY_STATUS = "status";

	private static final String[] dataColumns = { KEY_ROWID, KEY_GRID_ID,
			KEY_DATE_CREATED, KEY_DATE_UPDATED, KEY_SAVED_WITH_REVISION,
			KEY_DATA, KEY_STATUS };

	// Delimiters used in the data field to separate objects, fields and values
	// in a field which can hold multiple values.
	public static final String EOL_DELIMITER = "\n"; // Separate objects
	public static final String FIELD_DELIMITER_LEVEL1 = ":"; // Separate fields
	public static final String FIELD_DELIMITER_LEVEL2 = ","; // Separate values

	// in fields

	/**
	 * Get the table name.
	 * 
	 * @return The table name;
	 */
	@Override
	protected String getTableName() {
		return TABLE;
	}

	/**
	 * Builds the SQL create statement for this table.
	 * 
	 * @return The SQL create statement for this table.
	 */
	private static String buildCreateSQL() {
		return createTable(
				TABLE,
				createColumn(KEY_ROWID, "integer", "primary key autoincrement"),
				createColumn(KEY_GRID_ID, "integer", " not null"),
				createColumn(KEY_DATE_CREATED, "datetime", "not null"),
				createColumn(KEY_DATE_UPDATED, "datetime", "not null"),
				createColumn(KEY_SAVED_WITH_REVISION, "integer", " not null"),
				createColumn(KEY_DATA, "string", "not null"),
				createColumn(KEY_STATUS, "integer", "not null default "
						+ SolvingAttemptStatus.UNDETERMINED.getId()),
				createForeignKey(KEY_GRID_ID, GridDatabaseAdapter.TABLE,
						GridDatabaseAdapter.KEY_ROWID));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.mathdoku.plus.storage.database.DatabaseAdapter#getCreateSQL ()
	 */
	@Override
	protected String getCreateSQL() {
		return buildCreateSQL();
	}

	/**
	 * Creates the table.
	 * 
	 * @param db
	 *            The database in which the table has to be created.
	 */
	static void create(SQLiteDatabase db) {
		String sql = buildCreateSQL();
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			Log.i(TAG, sql);
		}

		// Execute create statement
		db.execSQL(sql);
	}

	/**
	 * Upgrades the table to an other version.
	 * 
	 * @param db
	 *            The database in which the table has to be updated.
	 * @param oldVersion
	 *            The old version of the database. Use the app revision number
	 *            to identify the database version.
	 * @param newVersion
	 *            The new version of the database. Use the app revision number
	 *            to identify the database version.
	 */
	static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (Config.mAppMode == AppMode.DEVELOPMENT && oldVersion < 433
				&& newVersion >= 433) {
			recreateTableInDevelopmentMode(db, TABLE, buildCreateSQL());
		}
	}

	/**
	 * Inserts a new solving attempt record for a grid into the database.
	 * 
	 * @param solvingAttempt
	 *            The solving attempt to be inserted.
	 * @return The row id of the row created. -1 in case of an error.
	 */
	public int insert(SolvingAttempt solvingAttempt) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_GRID_ID, solvingAttempt.mGridId);
		initialValues.put(KEY_DATE_CREATED,
				toSQLiteTimestamp(solvingAttempt.mDateCreated));
		initialValues.put(KEY_DATE_UPDATED,
				toSQLiteTimestamp(solvingAttempt.mDateUpdated));
		initialValues.put(KEY_SAVED_WITH_REVISION,
				solvingAttempt.mSavedWithRevision);
		initialValues.put(KEY_DATA, solvingAttempt.mStorageString);
		initialValues.put(KEY_STATUS,
				solvingAttempt.mSolvingAttemptStatus.getId());

		long id = -1;
		try {
			id = mSqliteDatabase.insertOrThrow(TABLE, null, initialValues);
		} catch (SQLiteException e) {
			throw new DatabaseException(
					"Cannot insert new solving attempt in database.", e);
		}

		if (id < 0) {
			throw new DatabaseException(
					"Insert of new puzzle failed when inserting the solving attempt into the database.");
		}

		return (int) id;
	}

	/**
	 * Gets the solving attempt for the given solving attempt id.
	 * 
	 * @param solvingAttemptId
	 *            The solving attempt id for which the data has to be retrieved.
	 * @return The data of the solving attempt.
	 */
	public SolvingAttempt getData(int solvingAttemptId) {
		SolvingAttempt solvingAttempt = null;
		Cursor cursor = null;
		try {
			cursor = mSqliteDatabase.query(true, TABLE, dataColumns, KEY_ROWID
					+ "=" + solvingAttemptId, null, null, null, null, null);

			if (cursor == null || !cursor.moveToFirst()) {
				// No record found for this grid.
				return null;
			}

			// Convert cursor record to a SolvingAttempt row
			solvingAttempt = new SolvingAttempt();
			solvingAttempt.mId = cursor.getInt(cursor
					.getColumnIndexOrThrow(KEY_ROWID));
			solvingAttempt.mGridId = cursor.getInt(cursor
					.getColumnIndexOrThrow(KEY_GRID_ID));
			solvingAttempt.mDateCreated = valueOfSQLiteTimestamp(cursor
					.getString(cursor.getColumnIndexOrThrow(KEY_DATE_CREATED)));
			solvingAttempt.mDateUpdated = valueOfSQLiteTimestamp(cursor
					.getString(cursor.getColumnIndexOrThrow(KEY_DATE_UPDATED)));
			solvingAttempt.mSavedWithRevision = cursor.getInt(cursor
					.getColumnIndexOrThrow(KEY_SAVED_WITH_REVISION));
			solvingAttempt.mStorageString = cursor.getString(cursor
					.getColumnIndexOrThrow(KEY_DATA));
		} catch (SQLiteException e) {
			throw new DatabaseException(
					String.format(
							"Cannot retrieve solving attempt with id '%d' from database.",
							solvingAttemptId), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return solvingAttempt;
	}

	/**
	 * Get the most recently played solving attempt
	 * 
	 * @return The id of the solving attempt which was last played. -1 in case
	 *         of an error.
	 */
	public int getMostRecentPlayedId() {
		int id = -1;
		Cursor cursor = null;
		try {
			cursor = mSqliteDatabase.query(true, TABLE,
					new String[] { KEY_ROWID }, null, null, null, null,
					KEY_DATE_UPDATED + " DESC", "1");

			if (cursor == null || !cursor.moveToFirst()) {
				// No record found
				return -1;
			}

			// Convert cursor record to a SolvingAttempt row
			id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ROWID));
		} catch (SQLiteException e) {
			throw new DatabaseException(
					"Cannot retrieve the most recent played solving attempt id in database.",
					e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return id;
	}

	/**
	 * Update the solving attempt.
	 * 
	 * @param solvingAttempt
	 *            The solving attempt to be updated.
	 * @return True in case the statistics have been updated. False otherwise.
	 */
	public boolean update(SolvingAttempt solvingAttempt) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_DATE_UPDATED,
				toSQLiteTimestamp(solvingAttempt.mDateUpdated));
		contentValues.put(KEY_SAVED_WITH_REVISION,
				solvingAttempt.mSavedWithRevision);
		contentValues.put(KEY_DATA, solvingAttempt.mStorageString);
		contentValues.put(KEY_STATUS,
				solvingAttempt.mSolvingAttemptStatus.getId());

		return (mSqliteDatabase.update(TABLE, contentValues, KEY_ROWID + " = "
				+ solvingAttempt.mId, null) == 1);
	}

	/**
	 * Gets a list of id's for all solving attempts which need to be converted.
	 * 
	 * @return The list of id's for all solving attempts which need to be
	 *         converted.
	 */
	public List<Integer> getAllToBeConverted() {
		List<Integer> idArrayList = null;
		Cursor cursor = null;
		String[] columns = { KEY_ROWID };
		try {
			// Currently all solving attempts are returned. In future this can
			// be restricted to games which are not solved.
			cursor = mSqliteDatabase.query(true, TABLE, columns, null, null,
					null, null, null, null);

			if (cursor == null || !cursor.moveToFirst()) {
				// No record found for this grid.
				return null;
			}

			// Convert cursor records to an array list of id's.
			idArrayList = new ArrayList<Integer>();
			do {
				idArrayList.add(cursor.getInt(cursor
						.getColumnIndexOrThrow(KEY_ROWID)));
			} while (cursor.moveToNext());
		} catch (SQLiteException e) {
			throw new DatabaseException(
					"Cannot retrieve all solving attempt id's from database which have to be converted.",
					e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return idArrayList;
	}

	/**
	 * Prefix the given column name with the table name.
	 * 
	 * @param column
	 *            The column name which has to be prefixed.
	 * @return The prefixed column name.
	 */
	public static String getPrefixedColumnName(String column) {
		return stringBetweenBackTicks(TABLE) + "."
				+ stringBetweenBackTicks(column);
	}

	/**
	 * Count the number of solving attempt for the given grid id.
	 * 
	 * @param gridId
	 *            The grid id for which the number of solving attempts has to be
	 *            determined.
	 * @return The number of solving attempt for the given grid id.
	 */
	public int countSolvingAttemptForGrid(int gridId) {
		int count = 0;
		Cursor cursor = null;
		try {
			cursor = mSqliteDatabase.query(true, TABLE,
					new String[] { "COUNT(1)" }, KEY_GRID_ID + "=" + gridId,
					null, null, null, null, null);

			if (cursor == null || !cursor.moveToFirst()) {
				// No record found for this grid.
				return 0;
			}

			// Convert cursor record to a SolvingAttempt row
			count = cursor.getInt(0);
		} catch (SQLiteException e) {
			throw new DatabaseException(
					String.format(
							"Cannot count the number of solving attempts for grid with id '%d' from database.",
							gridId), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}
}
