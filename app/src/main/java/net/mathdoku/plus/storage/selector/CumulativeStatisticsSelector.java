package net.mathdoku.plus.storage.selector;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.statistics.CumulativeStatistics;
import net.mathdoku.plus.storage.databaseadapter.DatabaseAdapterException;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.ConditionQueryHelper;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.JoinHelper;

public class CumulativeStatisticsSelector {
	@SuppressWarnings("unused")
	private static final String TAG = CumulativeStatisticsSelector.class.getName();

	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SQL = Config.mAppMode == Config.AppMode.DEVELOPMENT && false;

	private final int minGridSize;
	private final int maxGridSize;
	private final static DatabaseProjection databaseProjection = buildDatabaseProjection();
	private final CumulativeStatistics cumulativeStatistics;

	public CumulativeStatisticsSelector(int minGridSize, int maxGridSize) {
		this.minGridSize = minGridSize;
		this.maxGridSize = maxGridSize;
		cumulativeStatistics = retrieveFromDatabase();
	}

	private static DatabaseProjection buildDatabaseProjection() {
		DatabaseProjection databaseProjection = new DatabaseProjection();

		// Grid size minimum and maximum
		databaseProjection.put(DatabaseProjection.Aggregation.MIN,
				GridDatabaseAdapter.TABLE_NAME,
				GridDatabaseAdapter.KEY_GRID_SIZE);
		databaseProjection.put(DatabaseProjection.Aggregation.MAX,
				GridDatabaseAdapter.TABLE_NAME,
				GridDatabaseAdapter.KEY_GRID_SIZE);

		// Totals per status of game
		databaseProjection.put(DatabaseProjection.Aggregation.COUNTIF_TRUE,
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_ACTION_REVEAL_SOLUTION);
		databaseProjection.put(DatabaseProjection.Aggregation.COUNTIF_TRUE,
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_SOLVED_MANUALLY);
		databaseProjection.put(DatabaseProjection.Aggregation.COUNTIF_TRUE,
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_FINISHED);

		// Total games
		databaseProjection.put(DatabaseProjection.Aggregation.COUNT,
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_ROWID);

		return databaseProjection;
	}

	private CumulativeStatistics retrieveFromDatabase() {
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(databaseProjection);
		sqliteQueryBuilder.setTables(new JoinHelper(
				GridDatabaseAdapter.TABLE_NAME, GridDatabaseAdapter.KEY_ROWID)
				.innerJoinWith(StatisticsDatabaseAdapter.TABLE_NAME,
						StatisticsDatabaseAdapter.KEY_GRID_ID)
				.toString());

		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(
					databaseProjection.getAllColumnNames(),
					getSelectionString(), null, null, null, null);
			Log.i(TAG, sql);
		}

		Cursor cursor;
		try {
			cursor = sqliteQueryBuilder.query(DatabaseHelper
					.getInstance()
					.getReadableDatabase(), databaseProjection
					.getAllColumnNames(), getSelectionString(), null, null,
					null, null);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					String.format(
							"Cannot retrieve the cumulative statistics for grids with sizes '%d-%d' from database.",
							minGridSize, maxGridSize), e);
		}

		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		return getCumulativeStatisticsFromCursor(cursor);
	}

	private String getSelectionString() {
		ConditionQueryHelper conditionQueryHelper = new ConditionQueryHelper();
		conditionQueryHelper
				.addOperand(ConditionQueryHelper.getFieldBetweenValues(
						GridDatabaseAdapter
								.getPrefixedColumnName(GridDatabaseAdapter.KEY_GRID_SIZE),
						minGridSize, maxGridSize));
		conditionQueryHelper.addOperand(ConditionQueryHelper
				.getFieldEqualsValue(
						StatisticsDatabaseAdapter.KEY_INCLUDE_IN_STATISTICS,
						true));
		conditionQueryHelper.setAndOperator();
		return conditionQueryHelper.toString();
	}

	private CumulativeStatistics getCumulativeStatisticsFromCursor(Cursor cursor) {
		// Convert cursor record to a grid statics object.
		CumulativeStatistics cumulativeStatistics = new CumulativeStatistics();

		// Grid size minimum and maximum
		cumulativeStatistics.mMinGridSize = cursor
				.getInt(getColumnIndexMinGridSizeFromCursor(cursor));
		cumulativeStatistics.mMaxGridSize = cursor
				.getInt(getColumnIndexMaxGridSizeFromCursor(cursor));

		// Totals per status of game
		cumulativeStatistics.mCountSolutionRevealed = cursor
				.getInt(getColumnIndexCountSolutionRevealedFromCursor(cursor));
		cumulativeStatistics.mCountSolvedManually = cursor
				.getInt(getColumnIndexCountSolvedManuallyFromCursor(cursor));
		cumulativeStatistics.mCountFinished = cursor
				.getInt(getColumnIndexCountFinishedFromCursor(cursor));

		// Total games
		cumulativeStatistics.mCountStarted = cursor
				.getInt(getColumnIndexRowIdFromCursor(cursor));

		cursor.close();
		return cumulativeStatistics;
	}

	private int getColumnIndexMinGridSizeFromCursor(Cursor cursor) {
		return cursor
				.getColumnIndexOrThrow(DatabaseProjection.Aggregation.MIN
						.getAggregationColumnNameForColumn(GridDatabaseAdapter.KEY_GRID_SIZE));
	}

	private int getColumnIndexMaxGridSizeFromCursor(Cursor cursor) {
		return cursor
				.getColumnIndexOrThrow(DatabaseProjection.Aggregation.MAX
						.getAggregationColumnNameForColumn(GridDatabaseAdapter.KEY_GRID_SIZE));
	}

	private int getColumnIndexCountSolutionRevealedFromCursor(Cursor cursor) {
		return cursor
				.getColumnIndexOrThrow(DatabaseProjection.Aggregation.COUNTIF_TRUE
						.getAggregationColumnNameForColumn(StatisticsDatabaseAdapter.KEY_ACTION_REVEAL_SOLUTION));
	}

	private int getColumnIndexCountSolvedManuallyFromCursor(Cursor cursor) {
		return cursor
				.getColumnIndexOrThrow(DatabaseProjection.Aggregation.COUNTIF_TRUE
						.getAggregationColumnNameForColumn(StatisticsDatabaseAdapter.KEY_SOLVED_MANUALLY));
	}

	private int getColumnIndexCountFinishedFromCursor(Cursor cursor) {
		return cursor
				.getColumnIndexOrThrow(DatabaseProjection.Aggregation.COUNTIF_TRUE
						.getAggregationColumnNameForColumn(StatisticsDatabaseAdapter.KEY_FINISHED));
	}

	private int getColumnIndexRowIdFromCursor(Cursor cursor) {
		return cursor
				.getColumnIndexOrThrow(DatabaseProjection.Aggregation.COUNT
						.getAggregationColumnNameForColumn(StatisticsDatabaseAdapter.KEY_ROWID));
	}

	public CumulativeStatistics getCumulativeStatistics() {
		return cumulativeStatistics;
	}
}
