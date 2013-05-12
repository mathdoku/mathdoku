package net.cactii.mathdoku.storage.database;

import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.statistics.CumulativeStatistics;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.statistics.HistoricStatistics;
import net.cactii.mathdoku.statistics.HistoricStatistics.Serie;
import net.cactii.mathdoku.storage.database.Projection.Aggregation;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Build;
import android.util.Log;

/**
 * The database adapter for the statistics table. For each grid zero or more
 * statistics records can exists in the database. Only for historic games no
 * statistics will exist. Multiple statistics will only exist in case a game is
 * replayed in order to try to improve the statistics for the grid.
 */
public class StatisticsDatabaseAdapter extends DatabaseAdapter {
	private static final String TAG = "MathDoku.StatisticsDatabaseAdapter";

	public static final boolean DEBUG_SQL = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	// Columns for table statistics
	private static final String TABLE = "statistics";
	private static final String KEY_ROWID = "_id";
	private static final String KEY_GRID_ID = "grid_id";
	private static final String KEY_FIRST_MOVE = "first_move";
	private static final String KEY_LAST_MOVE = "last_move";
	public static final String KEY_ELAPSED_TIME = "elapsed_time";
	public static final String KEY_CHEAT_PENALTY_TIME = "cheat_penalty_time";
	public static final String KEY_CELLS_USER_VALUE_FILLED = "cells_user_value_filled";
	public static final String KEY_CELLS_USER_VALUES_EMPTY = "cells_user_value_empty";
	public static final String KEY_CELLS_USER_VALUES_REPLACED = "cells_user_value_replaced";
	public static final String KEY_POSSIBLES = "possibles";
	public static final String KEY_UNDOS = "undos";
	public static final String KEY_CELLS_CLEARED = "cells_cleared";
	public static final String KEY_CAGE_CLEARED = "cage_cleared";
	public static final String KEY_GRID_CLEARED = "grid_cleared";
	public static final String KEY_CELLS_REVEALED = "cells_revealed";
	public static final String KEY_OPERATORS_REVEALED = "operators_revealed";
	public static final String KEY_CHECK_PROGRESS_USED = "check_progress_used";
	public static final String KEY_CHECK_PROGRESS_INVALIDS_FOUND = "check_progress_invalids_found";
	private static final String KEY_SOLUTION_REVEALED = "solution_revealed";
	private static final String KEY_SOLVED_MANUALLY = "solved_manually";
	private static final String KEY_FINISHED = "finished";

	private static final String[] allColumns = { KEY_ROWID, KEY_GRID_ID,
			KEY_FIRST_MOVE, KEY_LAST_MOVE, KEY_ELAPSED_TIME,
			KEY_CHEAT_PENALTY_TIME, KEY_CELLS_USER_VALUE_FILLED,
			KEY_CELLS_USER_VALUES_EMPTY, KEY_CELLS_USER_VALUES_REPLACED,
			KEY_POSSIBLES, KEY_UNDOS, KEY_CELLS_CLEARED, KEY_CAGE_CLEARED,
			KEY_GRID_CLEARED, KEY_CELLS_REVEALED, KEY_OPERATORS_REVEALED,
			KEY_CHECK_PROGRESS_USED, KEY_CHECK_PROGRESS_INVALIDS_FOUND,
			KEY_SOLUTION_REVEALED, KEY_SOLVED_MANUALLY, KEY_FINISHED };

	// Projection for retrieve the cumulative and historic statistics
	private static Projection mCumulativeStatisticsProjection = null;
	private static Projection mHistoricStatisticsProjection = null;

	protected String getTableName() {
		return TABLE;
	}

	/**
	 * Builds the SQL create statement for this table.
	 * 
	 * @return The SQL create statement for this table.
	 */
	protected static String buildCreateSQL() {
		return createTable(
				TABLE,
				createColumn(KEY_ROWID, "integer", "primary key autoincrement"),
				createColumn(KEY_GRID_ID, "integer", " not null"),
				createColumn(KEY_FIRST_MOVE, "datetime", "not null"),
				createColumn(KEY_LAST_MOVE, "datetime", "not null"),
				createColumn(KEY_ELAPSED_TIME, "long", "not null default 0"),
				createColumn(KEY_CHEAT_PENALTY_TIME, "long",
						"not null default 0"),
				createColumn(KEY_CELLS_USER_VALUE_FILLED, "integer",
						" not null default 0"),
				createColumn(KEY_CELLS_USER_VALUES_EMPTY, "integer",
						" not null default 0"),
				createColumn(KEY_CELLS_USER_VALUES_REPLACED, "integer",
						" not null default 0"),
				createColumn(KEY_POSSIBLES, "integer", " not null default 0"),
				createColumn(KEY_UNDOS, "integer", " not null default 0"),
				createColumn(KEY_CELLS_CLEARED, "integer",
						" not null default 0"),
				createColumn(KEY_CAGE_CLEARED, "integer", " not null default 0"),
				createColumn(KEY_GRID_CLEARED, "integer", " not null default 0"),
				createColumn(KEY_CELLS_REVEALED, "integer",
						" not null default 0"),
				createColumn(KEY_OPERATORS_REVEALED, "integer",
						" not null default 0"),
				createColumn(KEY_CHECK_PROGRESS_USED, "integer",
						" not null default 0"),
				createColumn(KEY_CHECK_PROGRESS_INVALIDS_FOUND, "integer",
						" not null default 0"),
				createColumn(KEY_SOLUTION_REVEALED, "string",
						" not null default `false`"),
				createColumn(KEY_SOLVED_MANUALLY, "string",
						" not null default `false`"),
				createColumn(KEY_FINISHED, "string",
						" not null default `false`"),
				createForeignKey(KEY_GRID_ID, GridDatabaseAdapter.TABLE,
						GridDatabaseAdapter.KEY_ROWID));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cactii.mathdoku.storage.database.DatabaseAdapter#getCreateSQL()
	 */
	protected String getCreateSQL() {
		return buildCreateSQL();
	}

	/**
	 * Creates the table.
	 * 
	 * @param db
	 *            The database in which the table has to be created.
	 */
	protected static void create(SQLiteDatabase db) {
		String sql = buildCreateSQL();
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
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
	protected static void upgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		if (oldVersion < 265) {
			// In development revisions the table is simply dropped and
			// recreated.
			try {
				String sql = "DROP TABLE " + TABLE;
				if (DEBUG_SQL) {
					Log.i(TAG, sql);
				}
				db.execSQL(sql);
			} catch (SQLiteException e) {
				if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
					e.printStackTrace();
				}
			}
			create(db);
		}
		if (oldVersion >= 268 && oldVersion < 299 && newVersion >= 299) {
			dropColumn(db, TABLE, new String[] { "filename_solving_attempt" },
					buildCreateSQL());
		}
	}

	/**
	 * Inserts a new statistics record for a grid into the database.
	 * 
	 * @param grid
	 *            The grid for which a new statistics record has to be inserted.
	 * @return The grid statistics created. Null in case of an error.
	 */
	public GridStatistics insert(Grid grid) {
		java.sql.Timestamp now = new java.sql.Timestamp(
				new java.util.Date().getTime());
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_GRID_ID, grid.getRowId());
		initialValues.put(KEY_CELLS_USER_VALUES_EMPTY, grid.getGridSize()
				* grid.getGridSize());
		initialValues.put(KEY_FIRST_MOVE, now.toString());
		initialValues.put(KEY_LAST_MOVE, now.toString());

		long id = -1;
		try {
			id = mSqliteDatabase.insertOrThrow(TABLE, null, initialValues);
		} catch (SQLiteException e) {
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
		}

		if (id < 0) {
			return null;
		}

		// Retrieve the record created.
		return get((int) id);
	}

	/**
	 * Get the statistics for the given (row) id.
	 * 
	 * @param id
	 *            The unique row id of the statistics to be found.
	 * @return The grid statistics for the given id. Null in case of an error.
	 */
	public GridStatistics get(int id) {
		GridStatistics gridStatistics = null;
		Cursor cursor = null;
		try {
			cursor = mSqliteDatabase.query(true, TABLE, allColumns, KEY_ROWID
					+ "=" + id, null, null, null, null, null);
			gridStatistics = toGridStatistics(cursor);
		} catch (SQLiteException e) {
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return gridStatistics;
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
			cursor = mSqliteDatabase.query(true, TABLE, allColumns, KEY_GRID_ID
					+ "=" + gridId, null, null, null, KEY_ROWID + " DESC", "1");
			gridStatistics = toGridStatistics(cursor);
		} catch (SQLiteException e) {
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
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
	 * 
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
		gridStatistics.mFirstMove = toSQLTimestamp(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_FIRST_MOVE)));
		gridStatistics.mLastMove = toSQLTimestamp(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_LAST_MOVE)));
		gridStatistics.mElapsedTime = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_ELAPSED_TIME));
		gridStatistics.mCheatPenaltyTime = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_CHEAT_PENALTY_TIME));
		gridStatistics.mCellsUserValueFilled = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_USER_VALUE_FILLED));
		gridStatistics.mCellsUserValueEmtpty = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_USER_VALUES_EMPTY));
		gridStatistics.mUserValueReplaced = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_USER_VALUES_REPLACED));
		gridStatistics.mMaybeValue = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_POSSIBLES));
		gridStatistics.mUndoButton = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_UNDOS));
		gridStatistics.mCellCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_CLEARED));
		gridStatistics.mCageCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CAGE_CLEARED));
		gridStatistics.mGridCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_GRID_CLEARED));
		gridStatistics.mCellsRevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_REVEALED));
		gridStatistics.mOperatorsRevevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_OPERATORS_REVEALED));
		gridStatistics.mCheckProgressUsed = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CHECK_PROGRESS_USED));
		gridStatistics.mCheckProgressInvalidsFound = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CHECK_PROGRESS_INVALIDS_FOUND));
		gridStatistics.mSolutionRevealed = Boolean
				.valueOf(cursor.getString(cursor
						.getColumnIndexOrThrow(KEY_SOLUTION_REVEALED)));
		gridStatistics.mSolvedManually = Boolean.valueOf(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_SOLVED_MANUALLY)));
		gridStatistics.mFinished = Boolean.valueOf(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_FINISHED)));

		return gridStatistics;
	}

	/**
	 * Update the given statistics. It is required that the record already
	 * exists. The id should never be changed.
	 * 
	 * @param gridStatistics
	 *            The statistics to be updated.
	 * 
	 * @return True in case the statistics have been updated. False otherwise.
	 */
	public boolean update(GridStatistics gridStatistics) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_ROWID, gridStatistics.mId);
		newValues.put(KEY_FIRST_MOVE, gridStatistics.mFirstMove.toString());
		newValues.put(KEY_LAST_MOVE, gridStatistics.mLastMove.toString());
		newValues.put(KEY_ELAPSED_TIME, gridStatistics.mElapsedTime);
		newValues.put(KEY_CHEAT_PENALTY_TIME, gridStatistics.mCheatPenaltyTime);
		newValues.put(KEY_CELLS_USER_VALUE_FILLED,
				gridStatistics.mCellsUserValueFilled);
		newValues.put(KEY_CELLS_USER_VALUES_EMPTY,
				gridStatistics.mCellsUserValueEmtpty);
		newValues.put(KEY_CELLS_USER_VALUES_REPLACED,
				gridStatistics.mUserValueReplaced);
		newValues.put(KEY_POSSIBLES, gridStatistics.mMaybeValue);
		newValues.put(KEY_UNDOS, gridStatistics.mUndoButton);
		newValues.put(KEY_CELLS_CLEARED, gridStatistics.mCellCleared);
		newValues.put(KEY_CAGE_CLEARED, gridStatistics.mCageCleared);
		newValues.put(KEY_GRID_CLEARED, gridStatistics.mGridCleared);
		newValues.put(KEY_CELLS_REVEALED, gridStatistics.mCellsRevealed);
		newValues.put(KEY_OPERATORS_REVEALED,
				gridStatistics.mOperatorsRevevealed);
		newValues.put(KEY_CHECK_PROGRESS_USED,
				gridStatistics.mCheckProgressUsed);
		newValues.put(KEY_CHECK_PROGRESS_INVALIDS_FOUND,
				gridStatistics.mCheckProgressInvalidsFound);
		newValues.put(KEY_SOLUTION_REVEALED,
				Boolean.toString(gridStatistics.mSolutionRevealed));
		newValues.put(KEY_SOLVED_MANUALLY,
				Boolean.toString(gridStatistics.mSolvedManually));
		newValues.put(KEY_FINISHED, Boolean.toString(gridStatistics.mFinished));

		return (mSqliteDatabase.update(TABLE, newValues, KEY_ROWID + " = "
				+ gridStatistics.mId, null) == 1);
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
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public CumulativeStatistics getCumulativeStatistics(int minGridSize,
			int maxGridSize) {
		// Build projection if not yet done
		if (mCumulativeStatisticsProjection == null) {
			mCumulativeStatisticsProjection = new Projection();

			// Grid size minimum and maximum
			mCumulativeStatisticsProjection.put(Aggregation.MIN,
					GridDatabaseAdapter.TABLE,
					GridDatabaseAdapter.KEY_GRID_SIZE);
			mCumulativeStatisticsProjection.put(Aggregation.MAX,
					GridDatabaseAdapter.TABLE,
					GridDatabaseAdapter.KEY_GRID_SIZE);

			// First and last move
			mCumulativeStatisticsProjection.put(Aggregation.MIN, TABLE,
					KEY_FIRST_MOVE);
			mCumulativeStatisticsProjection.put(Aggregation.MAX, TABLE,
					KEY_LAST_MOVE);

			// Total, minimum, average, and maximum elapsed time
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_ELAPSED_TIME);
			mCumulativeStatisticsProjection.put(Aggregation.MIN, TABLE,
					KEY_ELAPSED_TIME);
			mCumulativeStatisticsProjection.put(Aggregation.AVG, TABLE,
					KEY_ELAPSED_TIME);
			mCumulativeStatisticsProjection.put(Aggregation.MAX, TABLE,
					KEY_ELAPSED_TIME);

			// Total, minimum, average, and maximum penalty time
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_CHEAT_PENALTY_TIME);
			mCumulativeStatisticsProjection.put(Aggregation.MIN, TABLE,
					KEY_CHEAT_PENALTY_TIME);
			mCumulativeStatisticsProjection.put(Aggregation.AVG, TABLE,
					KEY_CHEAT_PENALTY_TIME);
			mCumulativeStatisticsProjection.put(Aggregation.MAX, TABLE,
					KEY_CHEAT_PENALTY_TIME);

			// not (yet) used KEY_CELLS_USER_VALUE_FILLED,
			// not (yet) used KEY_CELLS_USER_VALUES_EMPTY
			// not (yet) used KEY_CELLS_USER_VALUES_REPLACED,

			// Totals of avoidable moves
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_POSSIBLES);
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_UNDOS);
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_CELLS_CLEARED);
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_CAGE_CLEARED);
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_GRID_CLEARED);

			// Totals per cheat
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_CELLS_REVEALED);
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_OPERATORS_REVEALED);
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_CHECK_PROGRESS_USED);
			mCumulativeStatisticsProjection.put(Aggregation.SUM, TABLE,
					KEY_CHECK_PROGRESS_INVALIDS_FOUND);

			// Totals per status of game'
			mCumulativeStatisticsProjection.put(Aggregation.COUNTIF_TRUE,
					TABLE, KEY_SOLUTION_REVEALED);
			mCumulativeStatisticsProjection.put(Aggregation.COUNTIF_TRUE,
					TABLE, KEY_SOLVED_MANUALLY);
			mCumulativeStatisticsProjection.put(Aggregation.COUNTIF_TRUE,
					TABLE, KEY_FINISHED);

			// Total games
			mCumulativeStatisticsProjection.put(Aggregation.COUNT, TABLE,
					KEY_ROWID);
		}

		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(mCumulativeStatisticsProjection);
		sqliteQueryBuilder.setTables(GridDatabaseAdapter.TABLE
				+ " INNER JOIN "
				+ TABLE
				+ " ON "
				+ GridDatabaseAdapter
						.getPrefixedColumnName(GridDatabaseAdapter.KEY_ROWID)
				+ " = " + getPrefixedColumnName(KEY_GRID_ID));

		if (DEBUG_SQL) {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				String sql = sqliteQueryBuilder
						.buildQuery(
								mCumulativeStatisticsProjection
										.getAllColumnNames(),
								GridDatabaseAdapter
										.getPrefixedColumnName(GridDatabaseAdapter.KEY_GRID_SIZE)
										+ " BETWEEN "
										+ minGridSize
										+ " AND "
										+ maxGridSize, null, null, null, null);
				Log.i(TAG, sql);
			}
		}

		Cursor cursor = null;
		try {
			cursor = sqliteQueryBuilder
					.query(mSqliteDatabase,
							mCumulativeStatisticsProjection.getAllColumnNames(),
							GridDatabaseAdapter
									.getPrefixedColumnName(GridDatabaseAdapter.KEY_GRID_SIZE)
									+ " BETWEEN "
									+ minGridSize
									+ " AND "
									+ maxGridSize, null, null, null, null);
		} catch (SQLiteException e) {
			if (cursor != null) {
				cursor.close();
			}
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
		}

		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a grid statics object.
		CumulativeStatistics cumulativeStatistics = new CumulativeStatistics();

		// Grid size minimum and maximum
		cumulativeStatistics.mMinGridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.MIN,
								GridDatabaseAdapter.KEY_GRID_SIZE)));
		cumulativeStatistics.mMaxGridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.MAX,
								GridDatabaseAdapter.KEY_GRID_SIZE)));

		// First and last move
		cumulativeStatistics.mMinFirstMove = toSQLTimestamp(cursor
				.getString(cursor
						.getColumnIndexOrThrow(mCumulativeStatisticsProjection
								.getAggregatedKey(Aggregation.MIN,
										KEY_FIRST_MOVE))));
		cumulativeStatistics.mMaxLastMove = toSQLTimestamp(cursor
				.getString(cursor
						.getColumnIndexOrThrow(mCumulativeStatisticsProjection
								.getAggregatedKey(Aggregation.MAX,
										KEY_LAST_MOVE))));

		// Total, minimum, average, and maximum elapsed time
		cumulativeStatistics.mSumElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM, KEY_ELAPSED_TIME)));
		cumulativeStatistics.mAvgElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.AVG, KEY_ELAPSED_TIME)));
		cumulativeStatistics.mMinElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.MIN, KEY_ELAPSED_TIME)));
		cumulativeStatistics.mMaxElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.MAX, KEY_ELAPSED_TIME)));

		// Total, minimum, average, and maximum penalty time
		cumulativeStatistics.mSumCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mAvgCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.AVG,
								KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mMinCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.MIN,
								KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mMaxCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.MAX,
								KEY_CHEAT_PENALTY_TIME)));

		// not (yet) used KEY_CELLS_USER_VALUE_FILLED,
		// not (yet) used KEY_CELLS_USER_VALUES_EMPTY
		// not (yet) used KEY_CELLS_USER_VALUES_REPLACED,

		// Totals of avoidable moves
		cumulativeStatistics.mSumMaybeValue = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM, KEY_POSSIBLES)));
		cumulativeStatistics.mSumUndoButton = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM, KEY_UNDOS)));
		cumulativeStatistics.mSumCellCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM, KEY_CELLS_CLEARED)));
		cumulativeStatistics.mSumCageCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM, KEY_CAGE_CLEARED)));
		cumulativeStatistics.mSumGridCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM, KEY_GRID_CLEARED)));

		// Totals per cheat
		cumulativeStatistics.mSumCellsRevealed = cursor
				.getInt(cursor
						.getColumnIndexOrThrow(mCumulativeStatisticsProjection
								.getAggregatedKey(Aggregation.SUM,
										KEY_CELLS_REVEALED)));
		cumulativeStatistics.mSumOperatorsRevevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_OPERATORS_REVEALED)));
		cumulativeStatistics.mSumCheckProgressUsed = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.SUM,
								KEY_CHECK_PROGRESS_USED)));
		cumulativeStatistics.mSumcheckProgressInvalidsFound = cursor
				.getInt(cursor
						.getColumnIndexOrThrow(mCumulativeStatisticsProjection
								.getAggregatedKey(Aggregation.SUM,
										KEY_CHECK_PROGRESS_INVALIDS_FOUND)));

		// Totals per status of game
		cumulativeStatistics.mCountSolutionRevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.COUNTIF_TRUE,
								KEY_SOLUTION_REVEALED)));
		cumulativeStatistics.mCountSolvedManually = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.COUNTIF_TRUE,
								KEY_SOLVED_MANUALLY)));
		cumulativeStatistics.mCountFinished = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
						.getAggregatedKey(Aggregation.COUNTIF_TRUE,
								KEY_FINISHED)));
		cumulativeStatistics.mCountStarted = cursor.getInt(cursor
				.getColumnIndexOrThrow(mCumulativeStatisticsProjection
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
	 *            grid size to retireve statistics for 1 specific grid size.
	 * @return The cumulative statistics for the given grid size.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public HistoricStatistics getHistoricData(String column, int minGridSize,
			int maxGridSize) {

		// Build projection if not yet done. As this projection is only build
		// once, it has to contain all base columns and all columns for which
		// the historic data can be retrieved.
		if (mHistoricStatisticsProjection == null) {
			mHistoricStatisticsProjection = new Projection();

			// Add base columns to the projection
			mHistoricStatisticsProjection.put(HistoricStatistics.DATA_COL_ID,
					TABLE, KEY_ROWID);
			mHistoricStatisticsProjection.put(
					stringBetweenBackTicks(HistoricStatistics.DATA_COL_SERIES), // Explicit
																				// back
																				// ticks
																				// needed
																				// here!
					"CASE WHEN "
							+ stringBetweenBackTicks(KEY_FINISHED)
							+ " <> "
							+ stringBetweenQuotes("true")
							+ " THEN "
							+ stringBetweenQuotes(Serie.UNFINISHED.toString())
							+ " WHEN "
							+ KEY_SOLUTION_REVEALED
							+ " = "
							+ stringBetweenQuotes("true")
							+ " THEN "
							+ stringBetweenQuotes(Serie.SOLUTION_REVEALED
									.toString()) + " ELSE "
							+ stringBetweenQuotes(Serie.SOLVED.toString())
							+ " END");

			// Add data columns to the projection.
			mHistoricStatisticsProjection.put(KEY_ELAPSED_TIME, TABLE,
					KEY_ELAPSED_TIME);
			mHistoricStatisticsProjection.put(KEY_CHEAT_PENALTY_TIME, TABLE,
					KEY_CHEAT_PENALTY_TIME);
			mHistoricStatisticsProjection.put(KEY_CELLS_USER_VALUE_FILLED,
					TABLE, KEY_CELLS_USER_VALUE_FILLED);
			mHistoricStatisticsProjection.put(KEY_CELLS_USER_VALUES_EMPTY,
					TABLE, KEY_CELLS_USER_VALUES_EMPTY);
			mHistoricStatisticsProjection.put(KEY_CELLS_USER_VALUES_REPLACED,
					TABLE, KEY_CELLS_USER_VALUES_REPLACED);
			mHistoricStatisticsProjection.put(KEY_POSSIBLES, TABLE,
					KEY_POSSIBLES);
			mHistoricStatisticsProjection.put(KEY_UNDOS, TABLE, KEY_UNDOS);
			mHistoricStatisticsProjection.put(KEY_CELLS_CLEARED, TABLE,
					KEY_CELLS_CLEARED);
			mHistoricStatisticsProjection.put(KEY_CAGE_CLEARED, TABLE,
					KEY_CAGE_CLEARED);
			mHistoricStatisticsProjection.put(KEY_GRID_CLEARED, TABLE,
					KEY_GRID_CLEARED);
			mHistoricStatisticsProjection.put(KEY_CELLS_REVEALED, TABLE,
					KEY_CELLS_REVEALED);
			mHistoricStatisticsProjection.put(KEY_OPERATORS_REVEALED, TABLE,
					KEY_OPERATORS_REVEALED);
			mHistoricStatisticsProjection.put(KEY_CHECK_PROGRESS_USED, TABLE,
					KEY_CHECK_PROGRESS_USED);
			mHistoricStatisticsProjection.put(
					KEY_CHECK_PROGRESS_INVALIDS_FOUND, TABLE,
					KEY_CHECK_PROGRESS_INVALIDS_FOUND);
		}

		// Build query
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(mHistoricStatisticsProjection);
		sqliteQueryBuilder.setTables(GridDatabaseAdapter.TABLE
				+ " INNER JOIN "
				+ TABLE
				+ " ON "
				+ GridDatabaseAdapter
						.getPrefixedColumnName(GridDatabaseAdapter.KEY_ROWID)
				+ " = " + getPrefixedColumnName(KEY_GRID_ID));

		// Retrieve all data. Note: in case column is not added to the
		// projection, no data will be retrieved!
		String[] columnsData = {
				stringBetweenBackTicks(HistoricStatistics.DATA_COL_ID),
				stringBetweenBackTicks(column) + " AS "
						+ HistoricStatistics.DATA_COL_VALUE,
				stringBetweenBackTicks(HistoricStatistics.DATA_COL_SERIES) };

		if (DEBUG_SQL) {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				String sql = sqliteQueryBuilder
						.buildQuery(
								columnsData,
								GridDatabaseAdapter
										.getPrefixedColumnName(GridDatabaseAdapter.KEY_GRID_SIZE)
										+ " BETWEEN "
										+ minGridSize
										+ " AND "
										+ maxGridSize, null, null, null, null);
				Log.i(TAG, sql);
			}
		}

		Cursor cursor = null;
		try {
			cursor = sqliteQueryBuilder
					.query(mSqliteDatabase,
							columnsData,
							GridDatabaseAdapter
									.getPrefixedColumnName(GridDatabaseAdapter.KEY_GRID_SIZE)
									+ " BETWEEN "
									+ minGridSize
									+ " AND "
									+ maxGridSize, null, null, null, null);
		} catch (SQLiteException e) {
			if (cursor != null) {
				cursor.close();
			}
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
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
	 * 
	 * @return The prefixed column name.
	 */
	public static String getPrefixedColumnName(String column) {
		return TABLE + "." + column;
	}
}
