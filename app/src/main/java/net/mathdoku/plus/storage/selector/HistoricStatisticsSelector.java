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
	public static final String PROJECTION_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY = "elapsed_time_excluding_cheat_penalty";
	public static final String PROJECTION_CHEAT_PENALTY = "cheat_penalty";
	public static final String PROJECTION_SOLVING_ATTEMPT_STATUS = "series";

	private int minGridSize;
	private int maxGridSize;
	private static final DatabaseProjection DATABASE_PROJECTION = buildDatabaseProjection();
	private List<DataPoint> dataPointList;

	public static class DataPoint {
		private final long elapsedTimeExcludingCheatPenalty;
		private final long cheatPenalty;
		private final SolvingAttemptStatus solvingAttemptStatus;

		public DataPoint(long elapsedTimeExcludingCheatPenalty,
				long cheatPenalty, SolvingAttemptStatus solvingAttemptStatus) {
			this.elapsedTimeExcludingCheatPenalty = elapsedTimeExcludingCheatPenalty;
			this.cheatPenalty = cheatPenalty;
			this.solvingAttemptStatus = solvingAttemptStatus;
		}

		public long getElapsedTimeExcludingCheatPenalty() {
			return elapsedTimeExcludingCheatPenalty;
		}

		public long getCheatPenalty() {
			return cheatPenalty;
		}

		public SolvingAttemptStatus getSolvingAttemptStatus() {
			return solvingAttemptStatus;
		}

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
		@SuppressWarnings("all")
		// Needed to suppress sonar warning on cyclomatic complexity
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
		dataPointList = retrieveFromDatabase();
	}

	private List<DataPoint> retrieveFromDatabase() {
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(DATABASE_PROJECTION);
		sqliteQueryBuilder.setTables(getJoinString());
		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(
					DATABASE_PROJECTION.getAllColumnNames(),
					getSelectionString(), null, null,
					StatisticsDatabaseAdapter.KEY_GRID_ID, null);
			Log.i(TAG, sql);
		}

		Cursor cursor;
		try {
			cursor = sqliteQueryBuilder.query(DatabaseHelper
					.getInstance()
					.getReadableDatabase(), DATABASE_PROJECTION
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
				DataPoint dataPoint = new DataPoint(
						getElapsedTimeExcludingCheatPenaltyFromCursor(cursor),
						getCheatPenaltyFromCursor(cursor),
						SolvingAttemptStatus
								.valueOf(getSolvingAttemptStatusFromCursor(cursor)));

				// Add data point to the list
				dataPoints.add(dataPoint);
			} while (cursor.moveToNext());
		}
		return dataPoints;
	}

	private long getElapsedTimeExcludingCheatPenaltyFromCursor(Cursor cursor) {
		return cursor
				.getLong(cursor
						.getColumnIndexOrThrow(PROJECTION_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY));
	}

	private long getCheatPenaltyFromCursor(Cursor cursor) {
		return cursor.getLong(cursor
				.getColumnIndexOrThrow(PROJECTION_CHEAT_PENALTY));
	}

	private String getSolvingAttemptStatusFromCursor(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndexOrThrow(PROJECTION_SOLVING_ATTEMPT_STATUS));
	}

	private static DatabaseProjection buildDatabaseProjection() {
		DatabaseProjection databaseProjection = new DatabaseProjection();
		databaseProjection.put(DATA_COL_ID,
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_ROWID);
		databaseProjection
				.put(PROJECTION_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY,
						StatisticsDatabaseAdapter
								.getPrefixedColumnName(StatisticsDatabaseAdapter.KEY_ELAPSED_TIME)
								+ " - "
								+ StatisticsDatabaseAdapter
										.getPrefixedColumnName(StatisticsDatabaseAdapter.KEY_CHEAT_PENALTY_TIME));

		databaseProjection.put(PROJECTION_CHEAT_PENALTY,
				StatisticsDatabaseAdapter.TABLE_NAME,
				StatisticsDatabaseAdapter.KEY_CHEAT_PENALTY_TIME);

		databaseProjection.put(PROJECTION_SOLVING_ATTEMPT_STATUS,
				getStatusColumnProjection());

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

	public List<DataPoint> getDataPointList() {
		return dataPointList;
	}
}
