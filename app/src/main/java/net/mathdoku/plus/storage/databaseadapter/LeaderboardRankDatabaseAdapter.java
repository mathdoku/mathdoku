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
	 * Insert a new initialized leaderboard.
	 * 
	 * @param leaderboardId
	 *            The leaderboard is as used by Google Play Services.
	 * @return The unique rowid of the leaderboard rank created. -1 in case of
	 *         an error.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public int insertInitializedLeaderboard(String leaderboardId, int gridSize,
			boolean operatorsVisible, PuzzleComplexity puzzleComplexity) {
		if (leaderboardId == null || leaderboardId.trim().equals("")) {
			throw new DatabaseAdapterException(
					"Parameter LeaderboardId is not specified.");
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_LEADERBOARD_ID, leaderboardId);
		contentValues.put(KEY_GRID_SIZE, gridSize);
		contentValues.put(KEY_HIDDEN_OPERATORS,
				DatabaseUtil.toSQLiteBoolean(operatorsVisible));
		contentValues.put(KEY_PUZZLE_COMPLEXITY, puzzleComplexity.toString());

		contentValues.put(KEY_SCORE_ORIGIN, ScoreOrigin.NONE.toString());
		contentValues.put(KEY_SCORE_STATISTICS_ID, (Integer) null);
		contentValues.put(KEY_SCORE_RAW_SCORE, (Long) null);
		contentValues.put(KEY_SCORE_DATE_SUBMITTED, (String) null);

		// Rank information is cleared explicitly as the rank information does
		// not correspond with the score in case an existing leaderboard is
		// updated.
		contentValues.put(KEY_RANK_STATUS, RankStatus.TO_BE_UPDATED.toString());
		contentValues.put(KEY_RANK, (String) null);
		contentValues.put(KEY_RANK_DISPLAY, (String) null);
		contentValues.put(KEY_RANK_DATE_LAST_UPDATED, (String) null);

		int id;
		try {
			id = (int) sqliteDatabase.insertOrThrow(TABLE_NAME, null,
					contentValues);
		} catch (SQLiteConstraintException e) {
			throw new DatabaseAdapterException(
					"Cannot insert new initialized leaderboard in database.", e);
		}
		return id;
	}

	/**
	 * Updates the leaderboard with a score which is related to a solving
	 * attempt which is stored in the local database.
	 * 
	 * @param leaderboardId
	 *            The leaderboard is as used by Google Play Services.
	 * @param statisticsId
	 *            The statistics id of the game in which the score was achieved.
	 * @param rawScore
	 *            The raw score (milliseconds).
	 * @return True in case successfully updated. False otherwise.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean updateWithLocalScore(String leaderboardId, int statisticsId,
			long rawScore) {
		if (leaderboardId == null || leaderboardId.trim().equals("")) {
			throw new DatabaseAdapterException(
					"Parameter LeaderboardId is not specified.");
		}
		if (statisticsId <= 0) {
			throw new DatabaseAdapterException(
					"Parameter statisticsId is invalid.");
		}
		if (rawScore <= 0) {
			throw new DatabaseAdapterException(
					"Parameter rawScore Id is invalid.");
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_LEADERBOARD_ID, leaderboardId);

		contentValues.put(KEY_SCORE_ORIGIN,
				ScoreOrigin.LOCAL_DATABASE.toString());
		contentValues.put(KEY_SCORE_STATISTICS_ID, statisticsId);
		contentValues.put(KEY_SCORE_RAW_SCORE, rawScore);
		contentValues.put(KEY_SCORE_DATE_SUBMITTED,
				DatabaseUtil.getCurrentSQLiteTimestamp());

		// Rank information is cleared explicitly as the rank information does
		// not correspond with the score in case an existing leaderboard is
		// updated.
		contentValues.put(KEY_RANK_STATUS, RankStatus.TO_BE_UPDATED.toString());
		contentValues.put(KEY_RANK, (String) null);
		contentValues.put(KEY_RANK_DISPLAY, (String) null);
		contentValues.put(KEY_RANK_DATE_LAST_UPDATED, (String) null);

		return sqliteDatabase
				.update(TABLE_NAME, contentValues, KEY_LEADERBOARD_ID + " = "
						+ DatabaseUtil.stringBetweenQuotes(leaderboardId), null) == 1;
	}

	/**
	 * Updates the leaderboard with a score which is retrieved from Google Play
	 * Services. The score is not related to a solving attempt which is stored
	 * in the local database. This could be a score which was achieved on
	 * another device by or possible on the current device in case the app has
	 * been re-installed or the database was manually removed.
	 * 
	 * @param leaderboardId
	 *            The leaderboard is as used by Google Play Services.
	 * @param rawScore
	 *            The raw score (milliseconds).
	 * @param rank
	 *            The rank for the leaderboard on Google Play Service
	 * @param rankDisplay
	 *            The rank for the leaderboard as displayed on Google Play
	 *            Service
	 * @return True in case successfully updated. False otherwise.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean updateWithGooglePlayScore(String leaderboardId,
			long rawScore, long rank, String rankDisplay) {
		if (leaderboardId == null || leaderboardId.trim().equals("")) {
			throw new DatabaseAdapterException(
					"Parameter LeaderboardId is not specified.");
		}
		if (rawScore <= 0) {
			throw new DatabaseAdapterException(
					"Parameter rawScore Id is invalid.");
		}

		String timestamp = DatabaseUtil.getCurrentSQLiteTimestamp();

		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_LEADERBOARD_ID, leaderboardId);

		contentValues.put(KEY_SCORE_ORIGIN, ScoreOrigin.EXTERNAL.toString());
		contentValues.put(KEY_SCORE_STATISTICS_ID, 0);
		contentValues.put(KEY_SCORE_RAW_SCORE, rawScore);
		contentValues.put(KEY_SCORE_DATE_SUBMITTED, timestamp);

		// Rank information is cleared explicitly as the rank information does
		// not correspond with the score in case an existing leaderboard is
		// updated.
		contentValues.put(KEY_RANK_STATUS,
				RankStatus.TOP_RANK_UPDATED.toString());
		contentValues.put(KEY_RANK, rank);
		contentValues.put(KEY_RANK_DISPLAY, rankDisplay);
		contentValues.put(KEY_RANK_DATE_LAST_UPDATED, timestamp);

		return sqliteDatabase
				.update(TABLE_NAME, contentValues, KEY_LEADERBOARD_ID + " = "
						+ DatabaseUtil.stringBetweenQuotes(leaderboardId), null) == 1;
	}

	/**
	 * Updates the ranking information for a leaderboard.
	 * 
	 * @return True in case successfully updated. False otherwise.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean updateWithGooglePlayRank(String leaderboardId, long rank,
			String rankDisplay) {
		if (leaderboardId == null || leaderboardId.trim().equals("")) {
			throw new DatabaseAdapterException(
					"LeaderboardId is not specified.");
		}

		// Update the ranking fields.
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_RANK_STATUS,
				RankStatus.TOP_RANK_UPDATED.toString());
		contentValues.put(KEY_RANK, rank);
		contentValues.put(KEY_RANK_DISPLAY, rankDisplay);
		contentValues.put(KEY_RANK_DATE_LAST_UPDATED,
				DatabaseUtil.getCurrentSQLiteTimestamp());

		return sqliteDatabase
				.update(TABLE_NAME, contentValues, KEY_LEADERBOARD_ID + " = "
						+ DatabaseUtil.stringBetweenQuotes(leaderboardId), null) == 1;
	}

	/**
	 * Updates the ranking information for a leaderboard.
	 * 
	 * @return True in case successfully updated. False otherwise.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean updateWithGooglePlayRankNotAvailable(String leaderboardId) {
		if (leaderboardId == null || leaderboardId.trim().equals("")) {
			throw new DatabaseAdapterException(
					"LeaderboardId is not specified.");
		}

		// Update the ranking fields.
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_RANK_STATUS,
				RankStatus.TOP_RANK_NOT_AVAILABLE.toString());
		contentValues.put(KEY_RANK, (String) null);
		contentValues.put(KEY_RANK_DISPLAY, (String) null);
		contentValues.put(KEY_RANK_DATE_LAST_UPDATED,
				DatabaseUtil.getCurrentSQLiteTimestamp());

		return sqliteDatabase
				.update(TABLE_NAME, contentValues, KEY_LEADERBOARD_ID + " = "
						+ DatabaseUtil.stringBetweenQuotes(leaderboardId), null) == 1;
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
			cursor = sqliteDatabase.query(true, TABLE_NAME,
					DATABASE_TABLE.getColumnNames(), KEY_LEADERBOARD_ID + "="
							+ DatabaseUtil.stringBetweenQuotes(leaderboardId),
					null, null, null, null, null);
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
		LeaderboardRankRow leaderboardRankRow = new LeaderboardRankRow(
				getLeaderboardIdFromCursor(cursor),
				getGridSizeFromCursor(cursor),
				getHideOperatorsFromCursor(cursor),
				getPuzzleComplexityFromCursor(cursor),
				getScoreOriginFromCursor(cursor),
				getStatisticsIdFromCursor(cursor),
				getRawScoreFromCurssor(cursor),
				getDateScoreSubmittedFromCursor(cursor),
				getRankStatusFromCursor(cursor),
				getRankFromCursor(cursor),
				getRankDisplayFromCursor(cursor),
				getDateLastUpdatedFromCursor(cursor));

		return leaderboardRankRow;
	}

	private String getLeaderboardIdFromCursor(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_LEADERBOARD_ID));
	}

	private int getGridSizeFromCursor(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GRID_SIZE));
	}

	private boolean getHideOperatorsFromCursor(Cursor cursor) {
		return DatabaseUtil.valueOfSQLiteBoolean(
				cursor.getString(cursor.getColumnIndexOrThrow(KEY_HIDDEN_OPERATORS)));
	}

	private PuzzleComplexity getPuzzleComplexityFromCursor(Cursor cursor) {
		return PuzzleComplexity.valueOf(
				cursor.getString(cursor.getColumnIndexOrThrow(KEY_PUZZLE_COMPLEXITY)));
	}

	private ScoreOrigin getScoreOriginFromCursor(Cursor cursor) {
		return ScoreOrigin.valueOf(cursor.getString(cursor.getColumnIndexOrThrow
				(KEY_SCORE_ORIGIN)));
	}

	private int getStatisticsIdFromCursor(Cursor cursor) {
		return cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_SCORE_STATISTICS_ID));
	}

	private long getRawScoreFromCurssor(Cursor cursor) {
		return cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_SCORE_RAW_SCORE));
	}

	private long getDateScoreSubmittedFromCursor(Cursor cursor) {
		return DatabaseUtil.valueOfSQLiteTimestamp(
				cursor.getString(cursor.getColumnIndexOrThrow(KEY_SCORE_DATE_SUBMITTED)));
	}

	private RankStatus getRankStatusFromCursor(Cursor cursor) {
		return RankStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(KEY_RANK_STATUS)));
	}

	private long getRankFromCursor(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndexOrThrow(KEY_RANK));
	}

	private String getRankDisplayFromCursor(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(KEY_RANK_DISPLAY));
	}

	private long getDateLastUpdatedFromCursor(Cursor cursor) {
		return DatabaseUtil.valueOfSQLiteTimestamp(
				cursor.getString(cursor.getColumnIndexOrThrow(KEY_RANK_DATE_LAST_UPDATED)));
	}

	/**
	 * Prefix the given column name with the table name.
	 * 
	 * @param column
	 *            The column name which has to be prefixed.
	 * @return The prefixed column name.
	 */
	public static String getPrefixedColumnName(String column) {
		return TABLE_NAME + "." + column;
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
			String orderBy = "IFNULL(" + KEY_RANK_DATE_LAST_UPDATED + ","
					+ KEY_SCORE_DATE_SUBMITTED + ") ASC";

			cursor = sqliteDatabase.query(true, TABLE_NAME,
					DATABASE_TABLE.getColumnNames(),
					getSelectionOutdatedLeaderboardRanks(), null, null, null,
					orderBy, "1");
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
		// noinspection StringBufferReplaceableByString
		StringBuilder stringBuilder = new StringBuilder();
		long offset15MinutesInMillis = 15 * 60 * 1000;
		long offset24HoursInMillis = 24 * 60 * 60 * 1000;

		// Include all leaderboards for which the rank status equals
		// TO_BE_UPDATED
		// noinspection StringConcatenationInsideStringBufferAppend
		stringBuilder.append(KEY_RANK_STATUS
				+ " = "
				+ DatabaseUtil.stringBetweenQuotes(RankStatus.TO_BE_UPDATED
						.toString()));

		// Include all leaderboards having a score and an updated rank which
		// have not been updated in the last 15 minutes. These are included
		// as the ranking position may have changed as other players
		// achieved a better score than the top score of the current player
		// in the last 15 minutes.
		// noinspection StringConcatenationInsideStringBufferAppend
		stringBuilder
				.append(" OR ("
						+ KEY_RANK_STATUS
						+ " = "
						+ DatabaseUtil
								.stringBetweenQuotes(RankStatus.TOP_RANK_UPDATED
										.toString())
						+ " AND "
						+ KEY_RANK_DATE_LAST_UPDATED
						+ " < "
						+ DatabaseUtil.stringBetweenQuotes(DatabaseUtil
								.getCurrentMinusOffsetSQLiteTimestamp(offset15MinutesInMillis))
						+ ")");

		// Include all leaderboards having no rank and no score which have
		// not been updated in the last 24 hours. These are include as the
		// current as the current player may have played this leaderboard on
		// another device in this interval.
		// noinspection StringConcatenationInsideStringBufferAppend
		stringBuilder
				.append(" OR ("
						+ KEY_RANK_STATUS
						+ " = "
						+ DatabaseUtil
								.stringBetweenQuotes(RankStatus.TOP_RANK_NOT_AVAILABLE
										.toString())
						+ " AND "
						+ KEY_RANK_DATE_LAST_UPDATED
						+ " < "
						+ DatabaseUtil.stringBetweenQuotes(DatabaseUtil
								.getCurrentMinusOffsetSQLiteTimestamp(offset24HoursInMillis))
						+ ")");

		return stringBuilder.toString();
	}

	/**
	 * Clear ranking information for all leaderboards do they will get updated.
	 */
	@SuppressWarnings("StringBufferReplaceableByString")
	public void setAllRanksToBeUpdated() {
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE " + TABLE_NAME + " ");
			// noinspection StringConcatenationInsideStringBufferAppend
			query.append("SET "
					+ KEY_RANK_STATUS
					+ " = "
					+ DatabaseUtil.stringBetweenQuotes(RankStatus.TO_BE_UPDATED
							.toString()));
			query.append(",  " + KEY_RANK + " = null");
			query.append(",  " + KEY_RANK_DISPLAY + " = null");
			query.append(",  " + KEY_RANK_DATE_LAST_UPDATED + " = null");

			sqliteDatabase.execSQL(query.toString());
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(
					"Cannot set ranks to be updated in database", e);
		}
	}
}
