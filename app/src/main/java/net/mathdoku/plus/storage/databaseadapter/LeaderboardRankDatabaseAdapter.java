package net.mathdoku.plus.storage.databaseadapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.storage.databaseadapter.database.DataType;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseColumnDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseTableDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.ConditionQueryHelper;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.OrderByHelper;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.QueryHelper;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.UpdateQueryHelper;
import net.mathdoku.plus.util.ParameterValidator;

/**
 * The database adapter for the grid table.
 */
public class LeaderboardRankDatabaseAdapter extends DatabaseAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = LeaderboardRankDatabaseAdapter.class
			.getName();

	// Remove "&& false" in following line to show the SQL-statements in the
	// debug information
	@SuppressWarnings("PointlessBooleanExpression")
	public static final boolean DEBUG_SQL = Config.mAppMode == Config.AppMode.DEVELOPMENT && false;

	// Score origins statuses:
	// LOCAL_DATABASE: the score is based on a solving attempt which is stored
	// in the local database.
	// EXTERNAL: the score is based on Google Play Services. This
	// score was achieved by the current player on another device or on this
	// device before reinstalling the app or clearing the database.
	// NONE: the leaderboard has never been played by this user. No score exists
	// in the local database neither is ranked score available on Google Play
	// Services.
	public enum ScoreOrigin {
		LOCAL_DATABASE, EXTERNAL, NONE
	}

	// The ranking information statuses:
	// TO_BE_UPDATED: ranking information is not yet available or needs to be
	// updated.
	// TOP_RANK_UPDATED: ranking information is updated with the top rank for
	// the player as registered on Google Play Services.
	// TOP_RANK_NOT_AVAILABLE: ranking information has been retrieved but was
	// not found for the current player.
	public enum RankStatus {
		TO_BE_UPDATED, TOP_RANK_UPDATED, TOP_RANK_NOT_AVAILABLE
	}

	private static final DatabaseTableDefinition DATABASE_TABLE = defineTable();

	// Columns for table statistics.
	private static final String TABLE_NAME = "leaderboard_rank";
	private static final String KEY_ROWID = "_id";
	private static final String KEY_LEADERBOARD_ID = "leaderboard_id";
	private static final String KEY_GRID_SIZE = "grid_size";
	private static final String KEY_HIDDEN_OPERATORS = "hidden_operators";
	private static final String KEY_PUZZLE_COMPLEXITY = "puzzle_complexity";
	private static final String KEY_SCORE_ORIGIN = "score_origin";
	private static final String KEY_SCORE_STATISTICS_ID = "score_statistics_id";
	private static final String KEY_SCORE_RAW_SCORE = "score_raw_score";
	private static final String KEY_SCORE_DATE_SUBMITTED = "score_date_submitted";
	private static final String KEY_RANK_STATUS = "rank_status";
	private static final String KEY_RANK = "rank";
	private static final String KEY_RANK_DISPLAY = "rank_display";
	private static final String KEY_RANK_DATE_LAST_UPDATED = "rank_date_last_updated";

	public LeaderboardRankDatabaseAdapter() {
		super();
	}

	// Package private access, intended for DatabaseHelper only
	LeaderboardRankDatabaseAdapter(SQLiteDatabase sqLiteDatabase) {
		super(sqLiteDatabase);
	}

	private static DatabaseTableDefinition defineTable() {
		DatabaseTableDefinition databaseTableDefinition = new DatabaseTableDefinition(
				TABLE_NAME);
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ROWID, DataType.INTEGER).setPrimaryKey());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_LEADERBOARD_ID, DataType.STRING)
				.setNotNull()
				.setUniqueKey());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_GRID_SIZE, DataType.INTEGER).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_HIDDEN_OPERATORS, DataType.STRING).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_PUZZLE_COMPLEXITY, DataType.STRING).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_SCORE_ORIGIN, DataType.STRING).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_SCORE_STATISTICS_ID, DataType.INTEGER));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_SCORE_RAW_SCORE, DataType.LONG));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_SCORE_DATE_SUBMITTED, DataType.TIMESTAMP));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_RANK_STATUS, DataType.STRING).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_RANK, DataType.LONG));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_RANK_DISPLAY, DataType.STRING));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_RANK_DATE_LAST_UPDATED, DataType.TIMESTAMP));
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
		if (Config.mAppMode == Config.AppMode.DEVELOPMENT && oldVersion < 587
				&& newVersion >= 587) {
			recreateTableInDevelopmentMode();
		}
	}

	/**
	 * Inserts a leaderboard rank row into the database. The given row id field
	 * is ignored.
	 * 
	 * @param leaderboardRankRow
	 *            The leaderboard rank row to be inserted.
	 * @return The leaderboard rank row which is actually inserted including the
	 *         row id.
	 */
	public LeaderboardRankRow insert(LeaderboardRankRow leaderboardRankRow) {
		ContentValues contentValues = getContentValues(leaderboardRankRow);
		contentValues.remove(KEY_ROWID);

		int id;
		try {
			id = (int) sqliteDatabase.insertOrThrow(TABLE_NAME, null,
					contentValues);
		} catch (SQLiteConstraintException e) {
			throw new DatabaseAdapterException(
					"Cannot insert new initialized leaderboard in database.", e);
		}

		return LeaderboardRankRowBuilder.from(leaderboardRankRow, id).build();
	}

	private ContentValues getContentValues(LeaderboardRankRow leaderboardRankRow) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_ROWID, leaderboardRankRow.getRowId());
		contentValues.put(KEY_LEADERBOARD_ID,
				leaderboardRankRow.getLeaderboardId());
		contentValues.put(KEY_GRID_SIZE, leaderboardRankRow.getGridSize());
		contentValues.put(KEY_HIDDEN_OPERATORS, DatabaseUtil
				.toSQLiteBoolean(leaderboardRankRow.isOperatorsHidden()));
		contentValues.put(KEY_PUZZLE_COMPLEXITY, leaderboardRankRow
				.getPuzzleComplexity()
				.toString());
		contentValues.put(KEY_SCORE_ORIGIN, leaderboardRankRow
				.getScoreOrigin()
				.toString());
		contentValues.put(KEY_SCORE_STATISTICS_ID,
				leaderboardRankRow.getStatisticsId());
		contentValues
				.put(KEY_SCORE_RAW_SCORE, leaderboardRankRow.getRawScore());
		contentValues.put(KEY_SCORE_DATE_SUBMITTED, DatabaseUtil
				.toSQLiteTimestamp(leaderboardRankRow.getDateSubmitted()));
		contentValues.put(KEY_RANK_STATUS, leaderboardRankRow
				.getRankStatus()
				.toString());
		contentValues.put(KEY_RANK, leaderboardRankRow.getRank());
		contentValues
				.put(KEY_RANK_DISPLAY, leaderboardRankRow.getRankDisplay());
		contentValues.put(KEY_RANK_DATE_LAST_UPDATED, DatabaseUtil
				.toSQLiteTimestamp(leaderboardRankRow.getDateLastUpdated()));
		return contentValues;
	}

	/**
	 * Updates a leaderboard rank row into the database.
	 * 
	 * @param leaderboardRankRow
	 *            The leaderboard rank row to be updated.
	 * @return True if updated. False otherwise.
	 */
	public boolean update(LeaderboardRankRow leaderboardRankRow) {
		return sqliteDatabase
				.update(TABLE_NAME,
						getContentValues(leaderboardRankRow),
						KEY_LEADERBOARD_ID
								+ " = "
								+ DatabaseUtil.stringBetweenQuotes(String
										.valueOf(leaderboardRankRow
												.getLeaderboardId())), null) == 1;

	}

	/**
	 * Get the leaderboard rank.
	 * 
	 * @param leaderboardId
	 *            The leaderboard id of the leaderboard which has to be
	 *            retrieved.
	 * @return The leaderboard with the given leaderboard id.
	 */
	public LeaderboardRankRow get(String leaderboardId) {
		LeaderboardRankRow leaderboardRankRow = null;
		Cursor cursor = null;
		try {
			cursor = sqliteDatabase.query(true, TABLE_NAME, DATABASE_TABLE
					.getColumnNames(), QueryHelper.getFieldEqualsValue(
					KEY_LEADERBOARD_ID, leaderboardId), null, null, null, null,
					null);
			leaderboardRankRow = toLeaderboardRankRow(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(String.format(
					"Cannot retrieve leaderboard rank id '%s' from database",
					leaderboardId), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return leaderboardRankRow;
	}

	/**
	 * Convert first record in the given cursor to a LeaderboardRankRow object.
	 * 
	 * @param cursor
	 *            The cursor to be converted.
	 * @return A LeaderboardRankRow object for the first leaderboard rank record
	 *         stored in the given cursor. Null in case of an error.
	 */
	private LeaderboardRankRow toLeaderboardRankRow(Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a leaderboard rank row.
		LeaderboardRankRow leaderboardRankRow = new LeaderboardRankRowBuilder(
				getRowIdFromCursor(cursor), getLeaderboardIdFromCursor(cursor),
				getGridSizeFromCursor(cursor),
				getHideOperatorsFromCursor(cursor),
				getPuzzleComplexityFromCursor(cursor))
				.setScore(getScoreOriginFromCursor(cursor),
						getStatisticsIdFromCursor(cursor),
						getRawScoreFromCursor(cursor),
						getDateScoreSubmittedFromCursor(cursor))
				.setRank(getRankStatusFromCursor(cursor),
						getRankFromCursor(cursor),
						getRankDisplayFromCursor(cursor),
						getDateLastUpdatedFromCursor(cursor))
				.build();

		return leaderboardRankRow;
	}

	private int getRowIdFromCursor(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ROWID));
	}

	private String getLeaderboardIdFromCursor(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_LEADERBOARD_ID));
	}

	private int getGridSizeFromCursor(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GRID_SIZE));
	}

	private boolean getHideOperatorsFromCursor(Cursor cursor) {
		return DatabaseUtil.valueOfSQLiteBoolean(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_HIDDEN_OPERATORS)));
	}

	private PuzzleComplexity getPuzzleComplexityFromCursor(Cursor cursor) {
		return PuzzleComplexity.valueOf(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_PUZZLE_COMPLEXITY)));
	}

	private ScoreOrigin getScoreOriginFromCursor(Cursor cursor) {
		return ScoreOrigin.valueOf(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_SCORE_ORIGIN)));
	}

	private int getStatisticsIdFromCursor(Cursor cursor) {
		return cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_SCORE_STATISTICS_ID));
	}

	private long getRawScoreFromCursor(Cursor cursor) {
		return cursor
				.getLong(cursor.getColumnIndexOrThrow(KEY_SCORE_RAW_SCORE));
	}

	private long getDateScoreSubmittedFromCursor(Cursor cursor) {
		return DatabaseUtil.valueOfSQLiteTimestamp(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_SCORE_DATE_SUBMITTED)));
	}

	private RankStatus getRankStatusFromCursor(Cursor cursor) {
		return RankStatus.valueOf(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_RANK_STATUS)));
	}

	private long getRankFromCursor(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndexOrThrow(KEY_RANK));
	}

	private String getRankDisplayFromCursor(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(KEY_RANK_DISPLAY));
	}

	private long getDateLastUpdatedFromCursor(Cursor cursor) {
		return DatabaseUtil.valueOfSQLiteTimestamp(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_RANK_DATE_LAST_UPDATED)));
	}

	/**
	 * Prefix the given column name with the table name.
	 * 
	 * @param column
	 *            The column name which has to be prefixed.
	 * @return The prefixed column name.
	 */
	public static String getPrefixedColumnName(String column) {
		ParameterValidator.validateNotNullOrEmpty(column);
		return DatabaseUtil.stringBetweenBackTicks(TABLE_NAME) + "."
				+ DatabaseUtil.stringBetweenBackTicks(column);
	}

	/**
	 * Get the most outdated leaderboard for which the ranking information needs
	 * to be updated.
	 * 
	 * @return The (first/next) leaderboard for which the ranking information
	 *         needs to be updated.
	 */
	public LeaderboardRankRow getMostOutdatedLeaderboardRank() {
		LeaderboardRankRow leaderboardRankRow = null;
		Cursor cursor = null;
		try {
			cursor = sqliteDatabase.query(true, TABLE_NAME,
					DATABASE_TABLE.getColumnNames(),
					getSelectionOutdatedLeaderboardRanks(), null, null, null,
					getOrderByMostOutdatedLeaderboardRank(), "1");
			leaderboardRankRow = toLeaderboardRankRow(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					"Cannot retrieve most outdated leaderboard rank from database",
					e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return leaderboardRankRow;
	}

	private String getOrderByMostOutdatedLeaderboardRank() {
		OrderByHelper orderByHelper = new OrderByHelper();
		orderByHelper.sortAscending(KEY_RANK_DATE_LAST_UPDATED);
		// In case the rank date is null or equal, sort on submit date of local
		// score.
		orderByHelper.sortAscending(KEY_SCORE_DATE_SUBMITTED);
		// Add sort on row id to get a deterministic result in the unit tests.
		orderByHelper.sortAscending(KEY_ROWID);
		return orderByHelper.toString();
	}

	/**
	 * Get the number of leaderboards for which the ranking information needs to
	 * be updated.
	 * 
	 * @return The number of leaderboards for which the leaderboards needs to be
	 *         updated again.
	 */
	public int getCountOutdatedLeaderboardRanks() {
		int count = 0;
		Cursor cursor = null;
		try {
			// Build selection and order by clauses
			String[] columns = new String[] { "COUNT(1)" };
			cursor = sqliteDatabase.query(true, TABLE_NAME, columns,
					getSelectionOutdatedLeaderboardRanks(), null, null, null,
					null, null);

			if (cursor == null || !cursor.moveToFirst()) {
				// No record found
				return 0;
			}

			// Convert cursor record to a count of grids
			count = cursor.getInt(0);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					"Cannot count number of outdated leaderboard ranks in database",
					e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}

	/**
	 * Get the selection clause for the queries retrieving the outdated
	 * leaderboard ranks.
	 * 
	 * @return The selection clause for the queries retrieving the outdated
	 *         leaderboard ranks.
	 */
	private String getSelectionOutdatedLeaderboardRanks() {
		ConditionQueryHelper conditionQueryHelper = new ConditionQueryHelper();

		// Include all leaderboards for which the rank status equals
		// TO_BE_UPDATED
		conditionQueryHelper.addOperand(ConditionQueryHelper
				.getFieldEqualsValue(KEY_RANK_STATUS,
						RankStatus.TO_BE_UPDATED.toString()));

		long interval15MinutesInMillis = 15 * 60 * 1000;
		conditionQueryHelper
				.addOperand(getSelectionStringStatusNotUpdatedInIntervalInMillisBeforeSystemTime(
						RankStatus.TOP_RANK_UPDATED, interval15MinutesInMillis));

		long interval24HoursInMillis = 24 * 60 * 60 * 1000;
		conditionQueryHelper
				.addOperand(getSelectionStringStatusNotUpdatedInIntervalInMillisBeforeSystemTime(
						RankStatus.TOP_RANK_NOT_AVAILABLE,
						interval24HoursInMillis));
		conditionQueryHelper.setOrOperator();

		return conditionQueryHelper.toString();
	}

	private String getSelectionStringStatusNotUpdatedInIntervalInMillisBeforeSystemTime(
			RankStatus rankStatus, long intervalInMillis) {
		String intervalInMillisString = DatabaseUtil
				.getCurrentMinusOffsetSQLiteTimestamp(intervalInMillis);

		ConditionQueryHelper conditionQueryHelper = new ConditionQueryHelper();
		conditionQueryHelper.addOperand(ConditionQueryHelper
				.getFieldEqualsValue(KEY_RANK_STATUS, rankStatus.toString()));
		conditionQueryHelper.addOperand(ConditionQueryHelper
				.getFieldLessThanValue(KEY_RANK_DATE_LAST_UPDATED,
						intervalInMillisString));
		conditionQueryHelper.setAndOperator();

		return conditionQueryHelper.toString();
	}

	/**
	 * Clear ranking information for all leaderboards do they will get updated.
	 */
	@SuppressWarnings("StringBufferReplaceableByString")
	public void setAllRanksToBeUpdated() {
		try {
			UpdateQueryHelper updateQueryHelper = new UpdateQueryHelper(
					TABLE_NAME);
			updateQueryHelper.setColumnTo(KEY_RANK_STATUS,
					RankStatus.TO_BE_UPDATED.toString());
			updateQueryHelper.setColumnToNull(KEY_RANK);
			updateQueryHelper.setColumnToNull(KEY_RANK_DISPLAY);
			updateQueryHelper.setColumnToNull(KEY_RANK_DATE_LAST_UPDATED);
			sqliteDatabase.execSQL(updateQueryHelper.toString());
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					"Cannot set ranks to be updated in database", e);
		}
	}
}
