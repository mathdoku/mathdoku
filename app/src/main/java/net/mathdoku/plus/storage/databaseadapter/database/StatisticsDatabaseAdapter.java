package net.mathdoku.plus.storage.databaseadapter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.statistics.CumulativeStatistics;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.HistoricStatistics;
import net.mathdoku.plus.statistics.HistoricStatistics.Series;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection.Aggregation;

import static net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil.stringBetweenBackTicks;
import static net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil.stringBetweenQuotes;
import static net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil.toSQLTimestamp;
import static net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil.toSQLiteBoolean;

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
	private static final String TABLE_NAME = "statistics";
	private static final String KEY_ROWID = "_id";
	private static final String KEY_GRID_ID = "grid_id";
	private static final String KEY_REPLAY = "replay";
	private static final String KEY_FIRST_MOVE = "first_move";
	private static final String KEY_LAST_MOVE = "last_move";
	private static final String KEY_ELAPSED_TIME = "elapsed_time";
	private static final String KEY_CHEAT_PENALTY_TIME = "cheat_penalty_time";
	private static final String KEY_CELLS_FILLED = "cells_filled";
	private static final String KEY_CELLS_EMPTY = "cells_empty";
	private static final String KEY_CELLS_REVEALED = "cells_revealed";
	private static final String KEY_USER_VALUES_REPLACED = "user_value_replaced";
	private static final String KEY_POSSIBLES = "possibles";
	private static final String KEY_ACTION_UNDOS = "action_undos";
	private static final String KEY_ACTION_CLEAR_CELL = "action_clear_cells";
	private static final String KEY_ACTION_CLEAR_GRID = "action_clear_grid";
	private static final String KEY_ACTION_REVEAL_CELL = "action_reveal_cell";
	private static final String KEY_ACTION_REVEAL_OPERATOR = "action_reveal_operators";
	private static final String KEY_ACTION_CHECK_PROGRESS = "action_check_progress";
	private static final String KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND = "check_progress_invalid_cells_found";
	private static final String KEY_ACTION_REVEAL_SOLUTION = "action_reveal_solution";
	private static final String KEY_SOLVED_MANUALLY = "solved_manually";
	private static final String KEY_FINISHED = "finished";

	// For each grid only the latest completed solving attempt should be
	// included in the statistics. Only in case no finished solving attempt
	// exists for a grid, the latest unfinished solving attempt should be used.
	// For ease and speed of retrieving it is stored whether this solving
	// attempt should be included or excluded from the statistics.
	private static final String KEY_INCLUDE_IN_STATISTICS = "include_in_statistics";

	// DatabaseProjection for retrieve the cumulative and historic statistics
	private static DatabaseProjection mCumulativeStatisticsDatabaseProjection = null;
	private static DatabaseProjection mHistoricStatisticsDatabaseProjection = null;

	private static DatabaseTableDefinition defineTable() {
		DatabaseTableDefinition databaseTableDefinition = new DatabaseTableDefinition(
				TABLE_NAME);
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ROWID, DataType.INTEGER).setPrimaryKey());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_GRID_ID, DataType.INTEGER).setNotNull());
		databaseTableDefinition.addColumn(
				new DatabaseColumnDefinition(KEY_REPLAY, DataType.INTEGER).setNotNull()
						.setDefaultValue(0));
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
				gridStatistics.mIncludedInStatistics);

		long id;
		try {
			id = sqliteDatabase.insertOrThrow(TABLE_NAME, null, contentValues);
		} catch (SQLiteException e) {
			throw new DatabaseException(
					"Cannot insert new grid statistics in database.", e);
		}

		if (id < 0) {
			throw new DatabaseException(
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
			cursor = sqliteDatabase.query(true, TABLE_NAME,
					DATABASE_TABLE.getColumnNames(),
					KEY_GRID_ID + "=" + gridId, null, null, null, KEY_ROWID
							+ " DESC", "1");
			gridStatistics = toGridStatistics(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseException(
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
		gridStatistics.mFirstMove = toSQLTimestamp(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_FIRST_MOVE)));
		gridStatistics.mLastMove = toSQLTimestamp(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_LAST_MOVE)));
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
	 * Get cumulative statistics for all grids with a given grid size.
	 * 
	 * @param minGridSize
	 *            The minimum size of the grid for which the cumulative
	 *            statistics have to be determined.
	 * @param maxGridSize
	 *            The maximum size of the grid for which the cumulative
	 *            statistics have to be determined. Use same value as minimum
	 *            grid size to retrieve statistics for 1 specific grid size.
	 * @return The cumulative statistics for the given grid size.
	 */
	public CumulativeStatistics getCumulativeStatistics(int minGridSize,
			int maxGridSize) {
		// Build projection if not yet done
		if (mCumulativeStatisticsDatabaseProjection == null) {
			mCumulativeStatisticsDatabaseProjection = new DatabaseProjection();

			// Grid size minimum and maximum
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.MIN,
					GridDatabaseAdapter.TABLE_NAME,
					GridDatabaseAdapter.KEY_GRID_SIZE);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.MAX,
					GridDatabaseAdapter.TABLE_NAME,
					GridDatabaseAdapter.KEY_GRID_SIZE);

			// First and last move
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.MIN,
					TABLE_NAME, KEY_FIRST_MOVE);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.MAX,
					TABLE_NAME, KEY_LAST_MOVE);

			// Total, minimum, average, and maximum elapsed time
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_ELAPSED_TIME);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.MIN,
					TABLE_NAME, KEY_ELAPSED_TIME);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.AVG,
					TABLE_NAME, KEY_ELAPSED_TIME);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.MAX,
					TABLE_NAME, KEY_ELAPSED_TIME);

			// Total, minimum, average, and maximum penalty time
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_CHEAT_PENALTY_TIME);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.MIN,
					TABLE_NAME, KEY_CHEAT_PENALTY_TIME);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.AVG,
					TABLE_NAME, KEY_CHEAT_PENALTY_TIME);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.MAX,
					TABLE_NAME, KEY_CHEAT_PENALTY_TIME);

			// not (yet) used KEY_CELLS_USER_VALUE_FILLED,
			// not (yet) used KEY_CELLS_USER_VALUES_EMPTY
			// not (yet) used KEY_CELLS_USER_VALUES_REPLACED,

			// Totals of avoidable moves
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_POSSIBLES);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_ACTION_UNDOS);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_ACTION_CLEAR_CELL);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_ACTION_CLEAR_GRID);

			// Totals per cheat
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_ACTION_REVEAL_CELL);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_ACTION_REVEAL_OPERATOR);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_ACTION_CHECK_PROGRESS);
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.SUM,
					TABLE_NAME, KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND);

			// Totals per status of game'
			mCumulativeStatisticsDatabaseProjection.put(
					Aggregation.COUNTIF_TRUE, TABLE_NAME,
					KEY_ACTION_REVEAL_SOLUTION);
			mCumulativeStatisticsDatabaseProjection.put(
					Aggregation.COUNTIF_TRUE, TABLE_NAME, KEY_SOLVED_MANUALLY);
			mCumulativeStatisticsDatabaseProjection.put(
					Aggregation.COUNTIF_TRUE, TABLE_NAME, KEY_FINISHED);

			// Total games
			mCumulativeStatisticsDatabaseProjection.put(Aggregation.COUNT,
					TABLE_NAME, KEY_ROWID);
		}

		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder
				.setProjectionMap(mCumulativeStatisticsDatabaseProjection);
		sqliteQueryBuilder.setTables(GridDatabaseAdapter.TABLE_NAME
				+ " INNER JOIN "
				+ TABLE_NAME
				+ " ON "
				+ GridDatabaseAdapter
						.getPrefixedColumnName(GridDatabaseAdapter.KEY_ROWID)
				+ " = " + getPrefixedColumnName(KEY_GRID_ID));
		String selection = GridDatabaseAdapter
				.getPrefixedColumnName(GridDatabaseAdapter.KEY_GRID_SIZE)
				+ " BETWEEN "
				+ minGridSize
				+ " AND "
				+ maxGridSize
				+ " AND "
				+ KEY_INCLUDE_IN_STATISTICS + " = 'true'";

		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder
					.buildQuery(mCumulativeStatisticsDatabaseProjection.getAllColumnNames(),
								selection, null, null, null, null);
			Log.i(TAG, sql);
		}

		Cursor cursor;
		try {
			cursor = sqliteQueryBuilder
					.query(sqliteDatabase,
						   mCumulativeStatisticsDatabaseProjection.getAllColumnNames(), selection,
						   null, null, null, null);
		} catch (SQLiteException e) {
			throw new DatabaseException(
					String.format(
							"Cannot retrieve the cumulative statistics for grids with sizes '%d-%d' from database.",
							minGridSize, maxGridSize), e);
		}

		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a grid statics object.
		CumulativeStatistics cumulativeStatistics = new CumulativeStatistics();

		// Grid size minimum and maximum
		cumulativeStatistics.mMinGridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.MIN,
								GridDatabaseAdapter.KEY_GRID_SIZE)));
		cumulativeStatistics.mMaxGridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.MAX,
								GridDatabaseAdapter.KEY_GRID_SIZE)));

		// First and last move
		cumulativeStatistics.mMinFirstMove = toSQLTimestamp(cursor
				.getString(cursor
						.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
								.getAggregatedKey(Aggregation.MIN,
										KEY_FIRST_MOVE))));
		cumulativeStatistics.mMaxLastMove = toSQLTimestamp(cursor
				.getString(cursor
						.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
								.getAggregatedKey(Aggregation.MAX,
										KEY_LAST_MOVE))));

		// Total, minimum, average, and maximum elapsed time
		cumulativeStatistics.mSumElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM, KEY_ELAPSED_TIME)));
		cumulativeStatistics.mAvgElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.AVG, KEY_ELAPSED_TIME)));
		cumulativeStatistics.mMinElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.MIN, KEY_ELAPSED_TIME)));
		cumulativeStatistics.mMaxElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.MAX, KEY_ELAPSED_TIME)));

		// Total, minimum, average, and maximum penalty time
		cumulativeStatistics.mSumCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mAvgCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.AVG,
								KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mMinCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.MIN,
								KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mMaxCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.MAX,
								KEY_CHEAT_PENALTY_TIME)));

		// not (yet) used KEY_CELLS_USER_VALUE_FILLED,
		// not (yet) used KEY_CELLS_USER_VALUES_EMPTY
		// not (yet) used KEY_CELLS_USER_VALUES_REPLACED,

		// Totals of avoidable moves
		cumulativeStatistics.mSumMaybeValue = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM, KEY_POSSIBLES)));
		cumulativeStatistics.mSumActionUndoMove = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM, KEY_ACTION_UNDOS)));
		cumulativeStatistics.mSumActionClearCell = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_ACTION_CLEAR_CELL)));
		cumulativeStatistics.mSumActionClearGrid = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_ACTION_CLEAR_GRID)));

		// Totals per cheat
		cumulativeStatistics.mSumActionRevealCell = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_ACTION_REVEAL_CELL)));
		cumulativeStatistics.mSumActionRevealOperator = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_ACTION_REVEAL_OPERATOR)));
		cumulativeStatistics.mSumActionCheckProgress = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_ACTION_CHECK_PROGRESS)));
		cumulativeStatistics.mSumCheckProgressInvalidCellsFound = cursor
				.getInt(cursor
						.getColumnIndexOrThrow(
								mCumulativeStatisticsDatabaseProjection.getAggregatedKey(
										Aggregation.SUM, KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND)));

		// Totals per status of game
		cumulativeStatistics.mCountSolutionRevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.COUNTIF_TRUE,
								KEY_ACTION_REVEAL_SOLUTION)));
		cumulativeStatistics.mCountSolvedManually = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.COUNTIF_TRUE,
								KEY_SOLVED_MANUALLY)));
		cumulativeStatistics.mCountFinished = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.COUNTIF_TRUE,
								KEY_FINISHED)));
		cumulativeStatistics.mCountStarted = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsDatabaseProjection
						.getAggregatedKey(Aggregation.COUNT, KEY_ROWID)));

		cursor.close();
		return cumulativeStatistics;
	}

	/**
	 * Get the historic statistics for the given column for all grids with a
	 * given grid size.
	 * 
	 * @param minGridSize
	 *            The minimum size of the grid for which the cumulative
	 *            statistics have to be determined.
	 * @param maxGridSize
	 *            The maximum size of the grid for which the cumulative
	 *            statistics have to be determined. Use same value as minimum
	 *            grid size to retrieve statistics for 1 specific grid size.
	 * @return The cumulative statistics for the given grid size.
	 */
	public HistoricStatistics getHistoricData(int minGridSize, int maxGridSize) {

		// Build projection if not yet done. As this projection is only build
		// once, it has to contain all base columns and all columns for which
		// the historic data can be retrieved.
		if (mHistoricStatisticsDatabaseProjection == null) {
			mHistoricStatisticsDatabaseProjection = new DatabaseProjection();

			// Add base columns to the projection
			mHistoricStatisticsDatabaseProjection.put(
					HistoricStatistics.DATA_COL_ID, TABLE_NAME, KEY_ROWID);
			mHistoricStatisticsDatabaseProjection.put(
					stringBetweenBackTicks(HistoricStatistics.DATA_COL_SERIES), /*
																				 * Explicit
																				 * back
																				 * ticks
																				 * needed
																				 * here
																				 * !
																				 */
					"CASE WHEN "
							+ stringBetweenBackTicks(KEY_FINISHED)
							+ " <> "
							+ stringBetweenQuotes("true")
							+ " THEN "
							+ stringBetweenQuotes(Series.UNFINISHED.toString())
							+ " WHEN "
							+ KEY_ACTION_REVEAL_SOLUTION
							+ " = "
							+ stringBetweenQuotes("true")
							+ " THEN "
							+ stringBetweenQuotes(Series.SOLUTION_REVEALED
									.toString()) + " ELSE "
							+ stringBetweenQuotes(Series.SOLVED.toString())
							+ " END");

			// Add data columns to the projection.
			mHistoricStatisticsDatabaseProjection.put(KEY_ELAPSED_TIME,
					TABLE_NAME, KEY_ELAPSED_TIME);
			mHistoricStatisticsDatabaseProjection.put(KEY_CHEAT_PENALTY_TIME,
					TABLE_NAME, KEY_CHEAT_PENALTY_TIME);
			mHistoricStatisticsDatabaseProjection.put(KEY_CELLS_FILLED,
					TABLE_NAME, KEY_CELLS_FILLED);
			mHistoricStatisticsDatabaseProjection.put(KEY_CELLS_EMPTY,
					TABLE_NAME, KEY_CELLS_EMPTY);
			mHistoricStatisticsDatabaseProjection.put(KEY_CELLS_REVEALED,
					TABLE_NAME, KEY_CELLS_REVEALED);
			mHistoricStatisticsDatabaseProjection.put(KEY_USER_VALUES_REPLACED,
					TABLE_NAME, KEY_USER_VALUES_REPLACED);
			mHistoricStatisticsDatabaseProjection.put(KEY_POSSIBLES,
					TABLE_NAME, KEY_POSSIBLES);
			mHistoricStatisticsDatabaseProjection.put(KEY_ACTION_UNDOS,
					TABLE_NAME, KEY_ACTION_UNDOS);
			mHistoricStatisticsDatabaseProjection.put(KEY_ACTION_CLEAR_CELL,
					TABLE_NAME, KEY_ACTION_CLEAR_CELL);
			mHistoricStatisticsDatabaseProjection.put(KEY_ACTION_CLEAR_GRID,
					TABLE_NAME, KEY_ACTION_CLEAR_GRID);
			mHistoricStatisticsDatabaseProjection.put(KEY_ACTION_REVEAL_CELL,
					TABLE_NAME, KEY_ACTION_REVEAL_CELL);
			mHistoricStatisticsDatabaseProjection.put(
					KEY_ACTION_REVEAL_OPERATOR, TABLE_NAME,
					KEY_ACTION_REVEAL_OPERATOR);
			mHistoricStatisticsDatabaseProjection.put(
					KEY_ACTION_CHECK_PROGRESS, TABLE_NAME,
					KEY_ACTION_CHECK_PROGRESS);
			mHistoricStatisticsDatabaseProjection.put(
					KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND, TABLE_NAME,
					KEY_CHECK_PROGRESS_INVALID_CELLS_FOUND);
		}

		// Build query
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder
				.setProjectionMap(mHistoricStatisticsDatabaseProjection);
		sqliteQueryBuilder.setTables(GridDatabaseAdapter.TABLE_NAME
				+ " INNER JOIN "
				+ TABLE_NAME
				+ " ON "
				+ GridDatabaseAdapter
						.getPrefixedColumnName(GridDatabaseAdapter.KEY_ROWID)
				+ " = " + getPrefixedColumnName(KEY_GRID_ID));

		// Retrieve all data. Note: in case column is not added to the
		// projection, no data will be retrieved!
		String[] columnsData = {
				// Statistics id
				stringBetweenBackTicks(HistoricStatistics.DATA_COL_ID),

				// Elapsed time excluding the cheat penalty
				stringBetweenBackTicks(KEY_ELAPSED_TIME)
						+ " - "
						+ stringBetweenBackTicks(KEY_CHEAT_PENALTY_TIME)
						+ " AS "
						+ HistoricStatistics.DATA_COL_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY,

				// Cheat penalty
				stringBetweenBackTicks(KEY_CHEAT_PENALTY_TIME) + " AS "
						+ HistoricStatistics.DATA_COL_CHEAT_PENALTY,

				// Series
				stringBetweenBackTicks(HistoricStatistics.DATA_COL_SERIES) };

		String selection = GridDatabaseAdapter
				.getPrefixedColumnName(GridDatabaseAdapter.KEY_GRID_SIZE)
				+ " BETWEEN "
				+ minGridSize
				+ " AND "
				+ maxGridSize
				+ " AND "
				+ KEY_INCLUDE_IN_STATISTICS + " = 'true'";

		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(columnsData, selection,
					null, null, KEY_GRID_ID, null);
			Log.i(TAG, sql);
		}

		Cursor cursor;
		try {
			cursor = sqliteQueryBuilder.query(sqliteDatabase, columnsData,
					selection, null, null, null, KEY_GRID_ID);
		} catch (SQLiteException e) {
			throw new DatabaseException(
					String.format(
							"Cannot retrieve the historic statistics for grids with sizes '%d-%d' from database.",
							minGridSize, maxGridSize), e);
		}

		HistoricStatistics historicStatistics = new HistoricStatistics(cursor);
		if (cursor != null) {
			cursor.close();
		}

		return historicStatistics;
	}

	/**
	 * Prefix the given column name with the table name.
	 * 
	 * @param column
	 *            The column name which has to be prefixed.
	 * @return The prefixed column name.
	 */
	@SuppressWarnings("SameParameterValue")
	private static String getPrefixedColumnName(String column) {
		return TABLE_NAME + "." + column;
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
				+ stringBetweenQuotes(toSQLiteBoolean(true)) + " ELSE "
				+ stringBetweenQuotes(toSQLiteBoolean(false)) + " END "
				+ " WHERE " + KEY_GRID_ID + " = " + gridId + " AND ("
				+ KEY_ROWID + " = " + solvingAttemptId + " OR "
				+ KEY_INCLUDE_IN_STATISTICS + " = "
				+ stringBetweenQuotes(toSQLiteBoolean(true)) + ")";
		if (DEBUG_SQL) {
			Log.i(TAG, sql);
		}
		try {
			sqliteDatabase.execSQL(sql);
		} catch (SQLiteException e) {
			throw new DatabaseException(
					String.format(
							"Cannot update the grid statistics in database for grid with id '%d'.",
							gridId), e);
		}
	}
}