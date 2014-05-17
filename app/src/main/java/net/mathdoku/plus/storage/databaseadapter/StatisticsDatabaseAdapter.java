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
import net.mathdoku.plus.storage.databaseadapter.queryhelper.ConditionList;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldOperatorBooleanValue;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldOperatorIntegerValue;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldOperatorValue;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.OrderByHelper;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.UpdateQueryHelper;
import net.mathdoku.plus.util.ParameterValidator;

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

	public StatisticsDatabaseAdapter() {
		super();
	}

	// Package private access, intended for DatabaseHelper only
	StatisticsDatabaseAdapter(SQLiteDatabase sqLiteDatabase) {
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
		ParameterValidator.validateNotNull(gridStatistics);
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
	 * Get the statistics for the given solving attempt id.
	 * 
	 * @param solvingAttemptId
	 *            The solving attempt Id for which the statistics have to be
	 *            retrieved.
	 * @return The most statistics for the solving attempt.
	 */
	public GridStatistics getStatisticsForSolvingAttempt(int solvingAttemptId) {
		GridStatistics gridStatistics = null;
		Cursor cursor = null;
		try {
			cursor = sqliteDatabase.query(true,
					//
					TABLE_NAME,
					//
					DATABASE_TABLE.getColumnNames(),
					// Note: statistics id equals solving attempt id
					getStatisticsIdSelectionString(solvingAttemptId), null,
					null,
					//
					null,
					//
					new OrderByHelper().sortDescending(KEY_ROWID).toString(),
					//
					null);
			gridStatistics = getGridStatisticsFromCursor(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					String.format(
							"Cannot retrieve statistics for grid with id '%d' from database.",
							solvingAttemptId), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return gridStatistics;
	}

	private String getStatisticsIdSelectionString(int statisticsRowId) {
		return new FieldOperatorIntegerValue(KEY_ROWID,
				FieldOperatorValue.Operator.EQUALS, statisticsRowId).toString();
	}

	/**
	 * Convert first record in the given cursor to a GridStatistics object.
	 * 
	 * @param cursor
	 *            The cursor to be converted.
	 * @return A GridStatistics object for the first statistics record stored in
	 *         the given cursor. Null in case of an error.
	 */
	private GridStatistics getGridStatisticsFromCursor(Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst()) {
			// No statistics records found for this grid.
			return null;
		}

		// Convert cursor record to a grid statics object.
		GridStatistics gridStatistics = new GridStatistics();
		gridStatistics.mId = cursor.getInt(getRowIdColumnFromCursor(cursor));
		gridStatistics.mGridId = cursor
				.getInt(getGridIdColumnFromCursor(cursor));
		gridStatistics.mReplayCount = cursor
				.getInt(getReplayCountColumnFromCursor(cursor));
		gridStatistics.mFirstMove = DatabaseUtil.toSQLTimestamp(cursor
				.getString(getFirstMoveColumnFromCursor(cursor)));
		gridStatistics.mLastMove = DatabaseUtil.toSQLTimestamp(cursor
				.getString(getLastMoveColumnFromCursor(cursor)));
		gridStatistics.mElapsedTime = cursor
				.getLong(getElapsedTimeColumnFromCursor(cursor));
		gridStatistics.mCheatPenaltyTime = cursor
				.getLong(getCheatPenaltyTimeColumnFromCursor(cursor));
		gridStatistics.mCellsFilled = cursor
				.getInt(getCellFilledColumnFromCursor(cursor));
		gridStatistics.mCellsEmpty = cursor
				.getInt(getCellsEmptyColumnFromCursor(cursor));
		gridStatistics.mCellsRevealed = cursor
				.getInt(getCellRevealedColumnFromCursor(cursor));
		gridStatistics.mEnteredValueReplaced = cursor
				.getInt(getEnteredValueReplacedColumnFromCursor(cursor));
		gridStatistics.mMaybeValue = cursor
				.getInt(getMaybeValueColumnFromCursor(cursor));
		gridStatistics.mActionUndoMove = cursor
				.getInt(getActionUndoMoveColumnFromCursor(cursor));
		gridStatistics.mActionClearCell = cursor
				.getInt(getActionClearCellColumnFromCursor(cursor));
		gridStatistics.mActionClearGrid = cursor
				.getInt(getActionClearGridColumnFromCursor(cursor));
		gridStatistics.mActionRevealCell = cursor
				.getInt(getActionRevealCellColumnFromCursor(cursor));
		gridStatistics.mActionRevealOperator = cursor
				.getInt(getActionRevealOperatorColumnFromCursor(cursor));
		gridStatistics.mActionCheckProgress = cursor
				.getInt(getActionCheckProgressColumnFromCursor(cursor));
		gridStatistics.mCheckProgressInvalidCellsFound = cursor
				.getInt(getCheckProgressInvalidCellsFoundColumnFromCursor(cursor));
		gridStatistics.mSolutionRevealed = Boolean
				.valueOf(getSolutionRevealedStatusColumnFromCursor(cursor));
		gridStatistics.mSolvedManually = Boolean
				.valueOf(getSolvedManuallyStatusColumnFromCursor(cursor));
		gridStatistics.mFinished = Boolean
				.valueOf(getFinishedStatusColumnFromCursor(cursor));
		gridStatistics.mIncludedInStatistics = Boolean
				.valueOf(getIncludeInStatisticsColumnFromCursor(cursor));

		return gridStatistics;
	}

	private int getRowIdColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_ROWID);
	}

	private int getGridIdColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_GRID_ID);
	}

	private int getReplayCountColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_REPLAY);
	}

	private int getFirstMoveColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_FIRST_MOVE);
	}

	private int getLastMoveColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_LAST_MOVE);
	}

	private int getElapsedTimeColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_ELAPSED_TIME);
	}

	private int getCheatPenaltyTimeColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_CHEAT_PENALTY_TIME);
	}

	private int getCellFilledColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_CELLS_FILLED);
	}

	private int getCellsEmptyColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_CELLS_EMPTY);
	}

	private int getCellRevealedColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_CELLS_REVEALED);
	}

	private int getEnteredValueReplacedColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_USER_VALUES_REPLACED);
	}

	private int getMaybeValueColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_POSSIBLES);
	}

	private int getActionUndoMoveColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_ACTION_UNDOS);
	}

	private int getActionClearCellColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_ACTION_CLEAR_CELL);
	}

	private int getActionClearGridColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_ACTION_CLEAR_GRID);
	}

	private int getActionRevealCellColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_ACTION_REVEAL_CELL);
	}

	private int getActionRevealOperatorColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_ACTION_REVEAL_OPERATOR);
	}

	private int getActionCheckProgressColumnFromCursor(Cursor cursor) {
		return cursor.getColumnIndexOrThrow(KEY_ACTION_CHECK_PROGRESS);
	}

	private int getCheckProgressInvalidCellsFoundColumnFromCursor(Cursor cursor) {
		return cursor
				.getColumnIndexOrThrow(KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND);
	}

	private String getSolutionRevealedStatusColumnFromCursor(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_ACTION_REVEAL_SOLUTION));
	}

	private String getSolvedManuallyStatusColumnFromCursor(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_SOLVED_MANUALLY));
	}

	private String getFinishedStatusColumnFromCursor(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(KEY_FINISHED));
	}

	private String getIncludeInStatisticsColumnFromCursor(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_INCLUDE_IN_STATISTICS));
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
	 * @param statisticsIdToBeIncluded
	 *            The row id of the statistics record which has to be included
	 *            as *the* statistics of the grid when retrieving the cumulative
	 *            or historic statistics are retrieved.
	 */
	public void updateSolvingAttemptToBeIncludedInStatistics(int gridId,
			int statisticsIdToBeIncluded) {
		UpdateQueryHelper updateQueryHelper = new UpdateQueryHelper(TABLE_NAME);
		updateQueryHelper
				.setColumnToStatement(
						KEY_INCLUDE_IN_STATISTICS,
						getDerivationNewValueIncludeInStatistics(statisticsIdToBeIncluded));

		ConditionList conditionListInner = new ConditionList();
		conditionListInner.addOperand(new FieldOperatorIntegerValue(KEY_ROWID,
				FieldOperatorValue.Operator.EQUALS, statisticsIdToBeIncluded));
		conditionListInner.addOperand(new FieldOperatorBooleanValue(
				KEY_INCLUDE_IN_STATISTICS, FieldOperatorValue.Operator.EQUALS,
				true));
		conditionListInner.setOrOperator();

		ConditionList conditionListOuter = new ConditionList();
		conditionListOuter.addOperand(new FieldOperatorIntegerValue(
				KEY_GRID_ID, FieldOperatorValue.Operator.EQUALS, gridId));
		conditionListOuter.addOperand(conditionListInner);
		conditionListOuter.setAndOperator();

		updateQueryHelper.setWhereCondition(conditionListOuter);

		if (DEBUG_SQL) {
			Log.i(TAG, updateQueryHelper.toString());
		}
		try {
			sqliteDatabase.execSQL(updateQueryHelper.toString());
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					String.format(
							"Cannot update the grid statistics in database for grid with id '%d'.",
							gridId), e);
		}
	}

	private String getDerivationNewValueIncludeInStatistics(
			int statisticsIdToBeIncluded) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(" CASE WHEN ");
		stringBuilder
				.append(getStatisticsIdSelectionString(statisticsIdToBeIncluded));
		stringBuilder.append(" THEN ");
		stringBuilder.append(DatabaseUtil.toQuotedSQLiteString(true));
		stringBuilder.append(" ELSE ");
		stringBuilder.append(DatabaseUtil.toQuotedSQLiteString(false));
		stringBuilder.append(" END ");
		return stringBuilder.toString();
	}
}