package net.mathdoku.plus.storage.selector;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.storage.databaseadapter.DatabaseAdapterException;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.ConditionQueryHelper;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.JoinHelper;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.QueryHelper;

import java.util.ArrayList;
import java.util.List;

public class HistoricStatisticsSelector {
	@SuppressWarnings("unused")
	private static final String TAG = HistoricStatisticsSelector.class
			.getName();

	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SQL = Config.mAppMode == Config.AppMode.DEVELOPMENT && false;

	// Columns in the DatabaseProjection
	public static final String DATA_COL_ID = "id";
	public static final String DATA_COL_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY = "elapsed_time_excluding_cheat_penalty";
	public static final String DATA_COL_CHEAT_PENALTY = "cheat_penalty";
	public static final String DATA_COL_SERIES = "series";

	private int minGridSize;
	private int maxGridSize;
	private final static DatabaseProjection databaseProjection = buildDatabaseProjection();
	private List<DataPoint> dataPoints;

	public static class DataPoint {
		public long elapsedTimeExcludingCheatPenalty;
		public long cheatPenalty;
		public SolvingAttemptStatus solvingAttemptStatus;

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("DataPoint{");
			sb.append("elapsedTimeExcludingCheatPenalty=").append(
					elapsedTimeExcludingCheatPenalty);
			sb.append(", cheatPenalty=").append(cheatPenalty);
			sb.append(", solvingAttemptStatus=").append(solvingAttemptStatus);
			sb.append('}');
			return sb.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof DataPoint)) {
				return false;
			}

			DataPoint dataPoint = (DataPoint) o;

			if (cheatPenalty != dataPoint.cheatPenalty) {
				return false;
			}
			if (elapsedTimeExcludingCheatPenalty != dataPoint.elapsedTimeExcludingCheatPenalty) {
				return false;
			}
			if (solvingAttemptStatus != dataPoint.solvingAttemptStatus) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = (int) (elapsedTimeExcludingCheatPenalty ^ (elapsedTimeExcludingCheatPenalty >>> 32));
			result = 31 * result + (int) (cheatPenalty ^ (cheatPenalty >>> 32));
			result = 31
					* result
					+ (solvingAttemptStatus != null ? solvingAttemptStatus
							.hashCode() : 0);
			return result;
		}
	}

	public HistoricStatisticsSelector(int minGridSize, int maxGridSize) {
		this.minGridSize = minGridSize;
		this.maxGridSize = maxGridSize;
		dataPoints = retrieveFromDatabase();
	}

	private List<DataPoint> retrieveFromDatabase() {
		// Build query
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(databaseProjection);
		sqliteQueryBuilder.setTables(getJoinString());
		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(
					databaseProjection.getAllColumnNames(),
					getSelectionString(), null, null,
					StatisticsDatabaseAdapter.KEY_GRID_ID, null);
			Log.i(TAG, sql);
		}

		Cursor cursor;
		try {
			cursor = sqliteQueryBuilder.query(DatabaseHelper
					.getInstance()
					.getReadableDatabase(), databaseProjection
					.getAllColumnNames(), getSelectionString(), null, null,
					null, StatisticsDatabaseAdapter.KEY_GRID_ID);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					String.format(
							"Cannot retrieve the historic statistics for grids with sizes '%d-%d' from database.",
							minGridSize, maxGridSize), e);
		}

		List<DataPoint> dataPoints = getDataPointsFromCursor(cursor);
		if (cursor != null) {
			cursor.close();
		}

		return dataPoints;
	}

	private String getJoinString() {
		return new JoinHelper(GridDatabaseAdapter.TABLE_NAME,
				GridDatabaseAdapter.KEY_ROWID).innerJoinWith(
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_GRID_ID).toString();
	}

	private String getSelectionString() {
		ConditionQueryHelper conditionQueryHelper = new ConditionQueryHelper();
		conditionQueryHelper.addOperand(ConditionQueryHelper
				.getFieldBetweenValues(GridDatabaseAdapter.KEY_GRID_SIZE,
						minGridSize, maxGridSize));
		conditionQueryHelper.addOperand(ConditionQueryHelper
				.getFieldEqualsValue(
						StatisticsDatabaseAdapter.KEY_INCLUDE_IN_STATISTICS,
						true));
		conditionQueryHelper.setAndOperator();
		return conditionQueryHelper.toString();
	}

	private List<DataPoint> getDataPointsFromCursor(Cursor cursor) {
		List<DataPoint> dataPoints = new ArrayList<DataPoint>();

		// Get historic data from cursor
		if (cursor != null && cursor.moveToFirst()) {
			do {
				// Fill new data point
				DataPoint dataPoint = new DataPoint();
				dataPoint.elapsedTimeExcludingCheatPenalty = cursor
						.getLong(cursor
								.getColumnIndexOrThrow(DATA_COL_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY));
				dataPoint.cheatPenalty = cursor.getLong(cursor
						.getColumnIndexOrThrow(DATA_COL_CHEAT_PENALTY));
				dataPoint.solvingAttemptStatus = SolvingAttemptStatus
						.valueOf(cursor.getString(cursor
								.getColumnIndexOrThrow(DATA_COL_SERIES)));

				// Add data point to the list
				dataPoints.add(dataPoint);
			} while (cursor.moveToNext());
		}
		return dataPoints;
	}

	private static DatabaseProjection buildDatabaseProjection() {
		DatabaseProjection databaseProjection = new DatabaseProjection();
		databaseProjection.put(DATA_COL_ID,
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_ROWID);
		databaseProjection
				.put(DATA_COL_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY,
						StatisticsDatabaseAdapter
								.getPrefixedColumnName(StatisticsDatabaseAdapter.KEY_ELAPSED_TIME)
								+ " - "
								+ StatisticsDatabaseAdapter
										.getPrefixedColumnName(StatisticsDatabaseAdapter.KEY_CHEAT_PENALTY_TIME));

		databaseProjection.put(DATA_COL_CHEAT_PENALTY,
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_CHEAT_PENALTY_TIME);

		databaseProjection.put(DATA_COL_SERIES, getStatusColumnProjection());

		return databaseProjection;
	}

	private static String getStatusColumnProjection() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("CASE WHEN ");
		stringBuilder.append(QueryHelper.getFieldNotEqualsValue(
				StatisticsDatabaseAdapter.KEY_FINISHED, true));
		stringBuilder.append(" THEN ");
		stringBuilder
				.append(DatabaseUtil
						.stringBetweenQuotes(SolvingAttemptStatus.UNFINISHED
								.toString()));
		stringBuilder.append(" WHEN ");
		stringBuilder.append(QueryHelper.getFieldEqualsValue(
				StatisticsDatabaseAdapter.KEY_ACTION_REVEAL_SOLUTION, true));
		stringBuilder.append(" THEN ");
		stringBuilder.append(DatabaseUtil
				.stringBetweenQuotes(SolvingAttemptStatus.REVEALED_SOLUTION
						.toString()));
		stringBuilder.append(" ELSE ");
		stringBuilder.append(DatabaseUtil
				.stringBetweenQuotes(SolvingAttemptStatus.FINISHED_SOLVED
						.toString()));
		stringBuilder.append(" END");

		return stringBuilder.toString();
	}

	public List<DataPoint> getDataPoints() {
		return dataPoints;
	}

}
