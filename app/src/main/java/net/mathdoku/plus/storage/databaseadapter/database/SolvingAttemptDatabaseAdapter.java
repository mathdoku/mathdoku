package net.mathdoku.plus.storage.databaseadapter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.storage.databaseadapter.database.database.DataType;
import net.mathdoku.plus.storage.databaseadapter.database.database.DatabaseColumnDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.database.DatabaseForeignKeyDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.database.DatabaseTableDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.database.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The database adapter for the solving attempt table. For each grid one or more
 * solving attempt records can exists in the database. For grid created with
 * version 2 of this app, a statistics record will exist. For grids created with
 * an older version, statistics data is not available.
 */
public class SolvingAttemptDatabaseAdapter extends DatabaseAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = SolvingAttemptDatabaseAdapter.class
			.getName();

	// Remove "&& false" in following line to show the SQL-statements in the
	// debug information
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SQL = Config.mAppMode == AppMode.DEVELOPMENT && false;

	private static final DatabaseTableDefinition DATABASE_TABLE = defineTable();

	// Columns for table
	static final String TABLE_NAME = "solving_attempt";
	static final String KEY_ROWID = "_id";
	static final String KEY_GRID_ID = "grid_id";
	private static final String KEY_DATE_CREATED = "date_created";
	private static final String KEY_DATE_UPDATED = "date_updated";
	private static final String KEY_SAVED_WITH_REVISION = "revision";
	private static final String KEY_DATA = "data";
	static final String KEY_STATUS = "status";

	// Delimiters used in the data field to separate objects, fields and values
	// in a field which can hold multiple values.
	public static final String EOL_DELIMITER = "\n"; // Separate objects
	public static final String FIELD_DELIMITER_LEVEL1 = ":"; // Separate fields
	public static final String FIELD_DELIMITER_LEVEL2 = ","; // Separate values

	public SolvingAttemptDatabaseAdapter() {
		super();
	}

	// Package private access, intended for DatabaseHelper only
	SolvingAttemptDatabaseAdapter(SQLiteDatabase sqLiteDatabase) {
		super(sqLiteDatabase);
	}

	private static DatabaseTableDefinition defineTable() {
		DatabaseTableDefinition databaseTableDefinition = new DatabaseTableDefinition(
				TABLE_NAME);
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ROWID, DataType.INTEGER).setPrimaryKey());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_GRID_ID, DataType.INTEGER).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_DATE_CREATED, DataType.TIMESTAMP).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_DATE_UPDATED, DataType.TIMESTAMP).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_SAVED_WITH_REVISION, DataType.INTEGER).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_DATA, DataType.STRING).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_STATUS, DataType.INTEGER).setNotNull().setDefaultValue(
				SolvingAttemptStatus.UNDETERMINED.getId()));
		databaseTableDefinition.setForeignKey(new DatabaseForeignKeyDefinition(
				KEY_GRID_ID, GridDatabaseAdapter.TABLE_NAME,
				GridDatabaseAdapter.KEY_ROWID));

		databaseTableDefinition.build();

		return databaseTableDefinition;
	}

	@Override
	protected DatabaseTableDefinition getDatabaseTableDefinition() {
		return DATABASE_TABLE;
	}

	/**
	 * Upgrades the table to an other version.
	 * 
	 * @param oldVersion
	 *            The old version of the database. Use the app revision number
	 *            to identify the database version.
	 * @param newVersion
	 *            The new version of the database. Use the app revision number
	 *            to identify the database version.
	 */
	protected void upgradeTable(int oldVersion, int newVersion) {
		if (Config.mAppMode == AppMode.DEVELOPMENT && oldVersion < 433
				&& newVersion >= 433) {
			recreateTableInDevelopmentMode();
		}
	}

	/**
	 * Inserts a new solving attempt record for a grid into the database.
	 * 
	 * @param solvingAttemptRow
	 *            The solving attempt to be inserted.
	 * @return The row id of the row created. -1 in case of an error.
	 */
	public int insert(SolvingAttemptRow solvingAttemptRow) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_GRID_ID, solvingAttemptRow.mGridId);
		initialValues.put(KEY_DATE_CREATED,
				DatabaseUtil.toSQLiteTimestamp(solvingAttemptRow.mDateCreated));
		initialValues.put(KEY_DATE_UPDATED,
				DatabaseUtil.toSQLiteTimestamp(solvingAttemptRow.mDateUpdated));
		initialValues.put(KEY_SAVED_WITH_REVISION,
				solvingAttemptRow.mSavedWithRevision);
		initialValues.put(KEY_DATA, solvingAttemptRow.mStorageString);
		initialValues.put(KEY_STATUS,
				solvingAttemptRow.mSolvingAttemptStatus.getId());

		long id = -1;
		try {
			id = sqliteDatabase.insertOrThrow(TABLE_NAME, null, initialValues);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					"Cannot insert new solving attempt in database.", e);
		}

		if (id < 0) {
			throw new DatabaseAdapterException(
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
	public SolvingAttemptRow getData(int solvingAttemptId) {
		SolvingAttemptRow solvingAttemptRow = null;
		Cursor cursor = null;
		try {
			cursor = sqliteDatabase.query(true, TABLE_NAME,
					DATABASE_TABLE.getColumnNames(), KEY_ROWID + "="
							+ solvingAttemptId, null, null, null, null, null);

			if (cursor == null || !cursor.moveToFirst()) {
				// No record found for this grid.
				return null;
			}

			// Convert cursor record to a SolvingAttemptRow row
			solvingAttemptRow = new SolvingAttemptRow();
			solvingAttemptRow.mId = cursor.getInt(cursor
					.getColumnIndexOrThrow(KEY_ROWID));
			solvingAttemptRow.mGridId = cursor.getInt(cursor
					.getColumnIndexOrThrow(KEY_GRID_ID));
			solvingAttemptRow.mDateCreated = DatabaseUtil
					.valueOfSQLiteTimestamp(cursor.getString(cursor
							.getColumnIndexOrThrow(KEY_DATE_CREATED)));
			solvingAttemptRow.mDateUpdated = DatabaseUtil
					.valueOfSQLiteTimestamp(cursor.getString(cursor
							.getColumnIndexOrThrow(KEY_DATE_UPDATED)));
			solvingAttemptRow.mSavedWithRevision = cursor.getInt(cursor
					.getColumnIndexOrThrow(KEY_SAVED_WITH_REVISION));
			solvingAttemptRow.mStorageString = cursor.getString(cursor
					.getColumnIndexOrThrow(KEY_DATA));
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					String.format(
							"Cannot retrieve solving attempt with id '%d' from database.",
							solvingAttemptId), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return solvingAttemptRow;
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
			cursor = sqliteDatabase.query(true, TABLE_NAME,
					new String[] { KEY_ROWID }, null, null, null, null,
					KEY_DATE_UPDATED + " DESC", "1");

			if (cursor == null || !cursor.moveToFirst()) {
				// No record found
				return -1;
			}

			// Convert cursor record to a SolvingAttemptRow row
			id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ROWID));
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
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
	 * @param solvingAttemptRow
	 *            The solving attempt to be updated.
	 * @return True in case the statistics have been updated. False otherwise.
	 */
	public boolean update(SolvingAttemptRow solvingAttemptRow) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_DATE_UPDATED,
				DatabaseUtil.toSQLiteTimestamp(solvingAttemptRow.mDateUpdated));
		contentValues.put(KEY_SAVED_WITH_REVISION,
				solvingAttemptRow.mSavedWithRevision);
		contentValues.put(KEY_DATA, solvingAttemptRow.mStorageString);
		contentValues.put(KEY_STATUS,
				solvingAttemptRow.mSolvingAttemptStatus.getId());

		return sqliteDatabase.update(TABLE_NAME, contentValues, KEY_ROWID
				+ " = " + solvingAttemptRow.mId, null) == 1;
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
			cursor = sqliteDatabase.query(true, TABLE_NAME, columns, null,
					null, null, null, null, null);

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
			throw new DatabaseAdapterException(
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
		return DatabaseUtil.stringBetweenBackTicks(TABLE_NAME) + "."
				+ DatabaseUtil.stringBetweenBackTicks(column);
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
			cursor = sqliteDatabase.query(true, TABLE_NAME,
					new String[] { "COUNT(1)" }, KEY_GRID_ID + "=" + gridId,
					null, null, null, null, null);

			if (cursor == null || !cursor.moveToFirst()) {
				// No record found for this grid.
				return 0;
			}

			// Convert cursor record to a SolvingAttemptRow row
			count = cursor.getInt(0);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
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
