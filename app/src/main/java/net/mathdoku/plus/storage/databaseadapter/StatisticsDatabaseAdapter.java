package net.mathdoku.plus.storage.databaseadapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.databaseadapter.database.DataType;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseColumnDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseForeignKeyDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseTableDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.OrderByHelper;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.QueryHelper;

/**
 * The database adapter for the statistics table. For each grid zero or more
 * statistics records can exists in the database. Only for historic games no
 * statistics will exist. Multiple statistics will only exist in case a game is
 * replayed in order to try to improve the statistics for the grid.
 */
public class StatisticsDatabaseAdapter extends DatabaseAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = StatisticsDatabaseAdapter.class.getName();

	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SQL = Config.mAppMode == AppMode.DEVELOPMENT && false;

	private static final DatabaseTableDefinition DATABASE_TABLE = defineTable();

	// Columns for table statistics
	public static final String TABLE_NAME = "statistics";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_GRID_ID = "grid_id";
	public static final String KEY_REPLAY = "replay";
	public static final String KEY_FIRST_MOVE = "first_move";
	public static final String KEY_LAST_MOVE = "last_move";
	public static final String KEY_ELAPSED_TIME = "elapsed_time";
	public static final String KEY_CHEAT_PENALTY_TIME = "cheat_penalty_time";
	public static final String KEY_CELLS_FILLED = "cells_filled";
	public static final String KEY_CELLS_EMPTY = "cells_empty";
	public static final String KEY_CELLS_REVEALED = "cells_revealed";
	public static final String KEY_USER_VALUES_REPLACED = "user_value_replaced";
	public static final String KEY_POSSIBLES = "possibles";
	public static final String KEY_ACTION_UNDOS = "action_undos";
	public static final String KEY_ACTION_CLEAR_CELL = "action_clear_cells";
	public static final String KEY_ACTION_CLEAR_GRID = "action_clear_grid";
	public static final String KEY_ACTION_REVEAL_CELL = "action_reveal_cell";
	public static final String KEY_ACTION_REVEAL_OPERATOR = "action_reveal_operators";
	public static final String KEY_ACTION_CHECK_PROGRESS = "action_check_progress";
	public static final String KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND = "check_progress_invalid_cells_found";
	public static final String KEY_ACTION_REVEAL_SOLUTION = "action_reveal_solution";
	public static final String KEY_SOLVED_MANUALLY = "solved_manually";
	public static final String KEY_FINISHED = "finished";

	// For each grid only the latest completed solving attempt should be
	// included in the statistics. Only in case no finished solving attempt
	// exists for a grid, the latest unfinished solving attempt should be used.
	// For ease and speed of retrieving it is stored whether this solving
	// attempt should be included or excluded from the statistics.
	public static final String KEY_INCLUDE_IN_STATISTICS = "include_in_statistics";

	private static DatabaseTableDefinition defineTable() {
		DatabaseTableDefinition databaseTableDefinition = new DatabaseTableDefinition(
				TABLE_NAME);
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ROWID, DataType.INTEGER).setPrimaryKey());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_GRID_ID, DataType.INTEGER).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_REPLAY, DataType.INTEGER).setNotNull().setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_FIRST_MOVE, DataType.TIMESTAMP).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_LAST_MOVE, DataType.TIMESTAMP).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ELAPSED_TIME, DataType.LONG)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_CHEAT_PENALTY_TIME, DataType.LONG)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_CELLS_FILLED, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_CELLS_EMPTY, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_CELLS_REVEALED, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_USER_VALUES_REPLACED, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_POSSIBLES, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ACTION_UNDOS, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ACTION_CLEAR_CELL, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ACTION_CLEAR_GRID, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ACTION_REVEAL_CELL, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ACTION_REVEAL_OPERATOR, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ACTION_CHECK_PROGRESS, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND, DataType.INTEGER)
				.setNotNull()
				.setDefaultValue(0));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ACTION_REVEAL_SOLUTION, DataType.BOOLEAN)
				.setNotNull()
				.setDefaultValue(false));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_SOLVED_MANUALLY, DataType.BOOLEAN)
				.setNotNull()
				.setDefaultValue(false));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_FINISHED, DataType.BOOLEAN).setNotNull().setDefaultValue(
				false));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_INCLUDE_IN_STATISTICS, DataType.BOOLEAN)
				.setNotNull()
				.setDefaultValue(false));
		databaseTableDefinition.setForeignKey(new DatabaseForeignKeyDefinition(
				KEY_GRID_ID, GridDatabaseAdapter.TABLE_NAME,
				GridDatabaseAdapter.KEY_ROWID));
		databaseTableDefinition.build();

		return databaseTableDefinition;
	}

	public StatisticsDatabaseAdapter() {
		super();
	}

	// Package private access, intended for DatabaseHelper only
	StatisticsDatabaseAdapter(SQLiteDatabase sqLiteDatabase) {
		super(sqLiteDatabase);
	}

	@Override
	public DatabaseTableDefinition getDatabaseTableDefinition() {
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
		if (Config.mAppMode == AppMode.DEVELOPMENT && oldVersion < 438
				&& newVersion >= 438) {
			recreateTableInDevelopmentMode();
		}
	}

	/**
	 * Inserts a new statistics record into the database.
	 * 
	 * @param gridStatistics
	 *            The grid new statistics to be inserted.
	 * @return The row id of statistics record created. -1 in case of an error.
	 */
	public int insert(GridStatistics gridStatistics) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_GRID_ID, gridStatistics.mGridId);
		contentValues.put(KEY_REPLAY, gridStatistics.mReplayCount);
		contentValues.put(KEY_CELLS_EMPTY, gridStatistics.mCellsEmpty);
		contentValues.put(KEY_FIRST_MOVE, gridStatistics.mFirstMove.toString());
		contentValues.put(KEY_LAST_MOVE, gridStatistics.mLastMove.toString());
		contentValues.put(KEY_INCLUDE_IN_STATISTICS,
				Boolean.toString(gridStatistics.mIncludedInStatistics));

		long id;
		try {
			id = sqliteDatabase.insertOrThrow(TABLE_NAME, null, contentValues);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					"Cannot insert new grid statistics in database.", e);
		}

		if (id < 0) {
			throw new DatabaseAdapterException(
					"Insert of new puzzle failed when inserting the statistics into the database.");
		}

		// Retrieve the record created.
		return (int) id;
	}

	/**
	 * Get most recent statistics for a given grid id.
	 * 
	 * @param gridId
	 *            The grid id for which the most recent statistics have to be
	 *            determined.
	 * @return The most recent grid statistics for the grid.
	 */
	public GridStatistics getMostRecent(int gridId) {
		GridStatistics gridStatistics = null;
		Cursor cursor = null;
		try {
			cursor = sqliteDatabase.query(true,
					//
					TABLE_NAME,
					//
					DATABASE_TABLE.getColumnNames(),
					//
					QueryHelper.getFieldEqualsValue(KEY_GRID_ID, gridId), null,
					null,
					//
					null,
					//
					new OrderByHelper().sortDescending(KEY_ROWID).toString(),
					//
					"1");
			gridStatistics = toGridStatistics(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					String.format(
							"Cannot retrieve statistics for grid with id '%d' from database.",
							gridId), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return gridStatistics;
	}

	/**
	 * Convert first record in the given cursor to a GridStatistics object.
	 * 
	 * @param cursor
	 *            The cursor to be converted.
	 * @return A GridStatistics object for the first statistics record stored in
	 *         the given cursor. Null in case of an error.
	 */
	private GridStatistics toGridStatistics(Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst()) {
			// No statistics records found for this grid.
			return null;
		}

		// Convert cursor record to a grid statics object.
		GridStatistics gridStatistics = new GridStatistics();
		gridStatistics.mId = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ROWID));
		gridStatistics.mGridId = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_GRID_ID));
		gridStatistics.mReplayCount = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_REPLAY));
		gridStatistics.mFirstMove = DatabaseUtil.toSQLTimestamp(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_MOVE)));
		gridStatistics.mLastMove = DatabaseUtil.toSQLTimestamp(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_LAST_MOVE)));
		gridStatistics.mElapsedTime = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_ELAPSED_TIME));
		gridStatistics.mCheatPenaltyTime = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_CHEAT_PENALTY_TIME));
		gridStatistics.mCellsFilled = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_FILLED));
		gridStatistics.mCellsEmpty = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_EMPTY));
		gridStatistics.mCellsRevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_REVEALED));
		gridStatistics.mEnteredValueReplaced = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_USER_VALUES_REPLACED));
		gridStatistics.mMaybeValue = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_POSSIBLES));
		gridStatistics.mActionUndoMove = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ACTION_UNDOS));
		gridStatistics.mActionClearCell = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ACTION_CLEAR_CELL));
		gridStatistics.mActionClearGrid = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ACTION_CLEAR_GRID));
		gridStatistics.mActionRevealCell = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ACTION_REVEAL_CELL));
		gridStatistics.mActionRevealOperator = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ACTION_REVEAL_OPERATOR));
		gridStatistics.mActionCheckProgress = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ACTION_CHECK_PROGRESS));
		gridStatistics.mCheckProgressInvalidCellsFound = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND));
		gridStatistics.mSolutionRevealed = Boolean.valueOf(cursor
				.getString(cursor
						.getColumnIndexOrThrow(KEY_ACTION_REVEAL_SOLUTION)));
		gridStatistics.mSolvedManually = Boolean.valueOf(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_SOLVED_MANUALLY)));
		gridStatistics.mFinished = Boolean.valueOf(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_FINISHED)));
		gridStatistics.mIncludedInStatistics = Boolean.valueOf(cursor
				.getString(cursor
						.getColumnIndexOrThrow(KEY_INCLUDE_IN_STATISTICS)));

		return gridStatistics;
	}

	/**
	 * Update the given statistics. It is required that the record already
	 * exists. The id should never be changed.
	 * 
	 * @param gridStatistics
	 *            The statistics to be updated.
	 * @return True in case the statistics have been updated. False otherwise.
	 */
	public boolean update(GridStatistics gridStatistics) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_ROWID, gridStatistics.mId);
		newValues.put(KEY_FIRST_MOVE, gridStatistics.mFirstMove.toString());
		newValues.put(KEY_LAST_MOVE, gridStatistics.mLastMove.toString());
		newValues.put(KEY_ELAPSED_TIME, gridStatistics.mElapsedTime);
		newValues.put(KEY_CHEAT_PENALTY_TIME, gridStatistics.mCheatPenaltyTime);
		newValues.put(KEY_CELLS_FILLED, gridStatistics.mCellsFilled);
		newValues.put(KEY_CELLS_EMPTY, gridStatistics.mCellsEmpty);
		newValues.put(KEY_CELLS_REVEALED, gridStatistics.mCellsRevealed);
		newValues.put(KEY_USER_VALUES_REPLACED,
				gridStatistics.mEnteredValueReplaced);
		newValues.put(KEY_POSSIBLES, gridStatistics.mMaybeValue);
		newValues.put(KEY_ACTION_UNDOS, gridStatistics.mActionUndoMove);
		newValues.put(KEY_ACTION_CLEAR_CELL, gridStatistics.mActionClearCell);
		newValues.put(KEY_ACTION_CLEAR_GRID, gridStatistics.mActionClearGrid);
		newValues.put(KEY_ACTION_REVEAL_CELL, gridStatistics.mActionRevealCell);
		newValues.put(KEY_ACTION_REVEAL_OPERATOR,
				gridStatistics.mActionRevealOperator);
		newValues.put(KEY_ACTION_CHECK_PROGRESS,
				gridStatistics.mActionCheckProgress);
		newValues.put(KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND,
				gridStatistics.mCheckProgressInvalidCellsFound);
		newValues.put(KEY_ACTION_REVEAL_SOLUTION,
				Boolean.toString(gridStatistics.mSolutionRevealed));
		newValues.put(KEY_SOLVED_MANUALLY,
				Boolean.toString(gridStatistics.mSolvedManually));
		newValues.put(KEY_FINISHED, Boolean.toString(gridStatistics.mFinished));
		newValues.put(KEY_INCLUDE_IN_STATISTICS,
				Boolean.toString(gridStatistics.mIncludedInStatistics));

		return sqliteDatabase.update(TABLE_NAME, newValues, KEY_ROWID + " = "
				+ gridStatistics.mId, null) == 1;
	}

	/**
	 * Prefix the given column name with the table name.
	 * 
	 * @param column
	 *            The column name which has to be prefixed.
	 * @return The prefixed column name.
	 */
	@SuppressWarnings("SameParameterValue")
	public static String getPrefixedColumnName(String column) {
		return DatabaseUtil.tableAndColumnBetweenBackTicks(TABLE_NAME, column);
	}

	/**
	 * Set the new solving attempt which has to be included for a specific grid
	 * in case the cumulative or historic statistics are retrieved.
	 * 
	 * @param gridId
	 *            The grid id for which the solving attempts have to changed.
	 * @param solvingAttemptId
	 *            The solving attempt which has to be included for the grid when
	 *            retrieving the cumulative or historic statistics are
	 *            retrieved.
	 */
	public void updateSolvingAttemptToBeIncludedInStatistics(int gridId,
			int solvingAttemptId) {
		String sql = "UPDATE " + TABLE_NAME + " SET "
				+ KEY_INCLUDE_IN_STATISTICS + " = " + " CASE WHEN " + KEY_ROWID
				+ " = " + solvingAttemptId + " THEN "
				+ DatabaseUtil.toQuotedSQLiteString(true) + " ELSE "
				+ DatabaseUtil.toQuotedSQLiteString(false) + " END "
				+ " WHERE " + KEY_GRID_ID + " = " + gridId + " AND ("
				+ KEY_ROWID + " = " + solvingAttemptId + " OR "
				+ KEY_INCLUDE_IN_STATISTICS + " = "
				+ DatabaseUtil.toQuotedSQLiteString(true) + ")";
		if (DEBUG_SQL) {
			Log.i(TAG, sql);
		}
		try {
			sqliteDatabase.execSQL(sql);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					String.format(
							"Cannot update the grid statistics in database for grid with id '%d'.",
							gridId), e);
		}
	}

}