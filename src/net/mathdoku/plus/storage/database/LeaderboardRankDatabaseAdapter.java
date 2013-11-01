package net.mathdoku.plus.storage.database;

import java.security.InvalidParameterException;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * The database adapter for the grid table.
 */
public class LeaderboardRankDatabaseAdapter extends DatabaseAdapter {

	private static final String TAG = "MathDoku.LeaderboardRankDatabaseAdapter";

	// Remove "&& false" in following line to show the SQL-statements in the
	// debug information
	public static final boolean DEBUG_SQL = (Config.mAppMode == AppMode.DEVELOPMENT) && true;

	// Columns for table statistics
	protected static final String TABLE = "leaderboard_rank";
	protected static final String KEY_ROWID = "_id";
	protected static final String KEY_LEADERBOARD_ID = "leaderboard_id";
	protected static final String KEY_STATISTICS_ID = "statistics_id";
	protected static final String KEY_RAW_SCORE = "raw_score";
	protected static final String KEY_DATE_SUBMITTED = "date_submitted";
	protected static final String KEY_RANK = "rank";
	protected static final String KEY_RANK_DISPLAY = "rank_display";
	protected static final String KEY_RANK_DATE_LAST_UPDATED = "date_last_updated";

	private static final String[] allColumns = { KEY_ROWID, KEY_LEADERBOARD_ID,
			KEY_STATISTICS_ID, KEY_RAW_SCORE, KEY_DATE_SUBMITTED, KEY_RANK,
			KEY_RANK_DISPLAY, KEY_RANK_DATE_LAST_UPDATED };

	@Override
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
				createColumn(KEY_LEADERBOARD_ID, "text", "not null unique"),
				createColumn(KEY_STATISTICS_ID, "integer", null),
				createColumn(KEY_RAW_SCORE, "long", " not null"),
				createColumn(KEY_DATE_SUBMITTED, "datetime", " not null"),
				createColumn(KEY_RANK, "long", null),
				createColumn(KEY_RANK_DISPLAY, "text", null),
				createColumn(KEY_RANK_DATE_LAST_UPDATED, "datetime", null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.mathdoku.plus.storage.database.DatabaseAdapter#getCreateSQL ()
	 */
	@Override
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
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
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
		if (oldVersion < 587 && newVersion >= 587) {
			// In development revisions the table is simply dropped and
			// recreated.
			try {
				db.execSQL("DROP TABLE " + TABLE);
			} catch (SQLiteException e) {
				// Table does not exist
			}
			create(db);
		}
	}

	/**
	 * Updates the raw score of a leaderboard. In case no leaderboard record yet
	 * exists, it will be created.
	 * 
	 * @param leaderboardId
	 *            The leaderboard is as used by Google Play Services.
	 * @param statisticsId
	 *            The statistics id of the game in which the score was achieved.
	 *            Should be 0 in case the score was achieved on another device
	 *            or before the most recent re-install of the app.
	 * @param rawScore
	 *            The raw score (milliseconds).
	 * @return The rowid of the leaderboard created. -1 in case of an error.
	 * @throws InvalidParameterException
	 *             In case the leaderboard id is empty or null.
	 * @throws SQLException
	 *             In case the leaderboard id is not unique.
	 */
	public int updateOrInsert(String leaderboardId, int statisticsId,
			long rawScore) throws InvalidParameterException, SQLException {
		int id = -1;

		if (leaderboardId == null || leaderboardId.trim().equals("")) {
			throw new InvalidParameterException(
					"Parameter LeaderboardId is not specified.");
		}
		if (statisticsId < 0) {
			throw new InvalidParameterException(
					"Parameter statisticsId is invalid.");
		}
		if (rawScore <= 0) {
			throw new InvalidParameterException(
					"Parameter rawScore Id is invalid.");
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_LEADERBOARD_ID, leaderboardId);
		contentValues.put(KEY_STATISTICS_ID, statisticsId);
		contentValues.put(KEY_RAW_SCORE, rawScore);
		contentValues.put(KEY_DATE_SUBMITTED,
				toSQLiteTimestamp(new java.util.Date().getTime()));

		// Rank information is cleared explicitly as the rank information does
		// not correspond with the score in case an existing leaderboard is
		// updated.
		contentValues.put(KEY_RANK, (String) null);
		contentValues.put(KEY_RANK_DISPLAY, (String) null);
		contentValues.put(KEY_RANK_DATE_LAST_UPDATED, (String) null);

		try {
			id = (int) mSqliteDatabase.replace(TABLE, null, contentValues);
		} catch (SQLiteConstraintException e) {
			InvalidParameterException ipe = new InvalidParameterException(
					e.getLocalizedMessage());
			ipe.initCause(e);
			throw ipe;
		}
		return id;
	}

	/**
	 * Updates the ranking information for a leaderboard.
	 * 
	 * @return The rowid of the leaderboard created. -1 in case of an error.
	 * @throws InvalidParameterException
	 *             In case the leaderboard id is empty or null.
	 */
	public boolean updateRank(String leaderboardId, long rank,
			String rankDisplay) throws InvalidParameterException, SQLException {
		if (leaderboardId == null || leaderboardId.trim().equals("")) {
			throw new InvalidParameterException(
					"LeaderboardId is not specified.");
		}

		// Update the ranking fields.
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_RANK, (String) null);
		contentValues.put(KEY_RANK_DISPLAY, (String) null);
		contentValues.put(KEY_RANK_DATE_LAST_UPDATED,
				toSQLiteTimestamp(new java.util.Date().getTime()));

		return (mSqliteDatabase.update(TABLE, contentValues, KEY_LEADERBOARD_ID
				+ " = " + stringBetweenQuotes(leaderboardId), null) == 1);
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
			cursor = mSqliteDatabase.query(true, TABLE, allColumns,
					KEY_LEADERBOARD_ID + "="
							+ stringBetweenQuotes(leaderboardId), null, null,
					null, null, null);
			leaderboardRankRow = toLeaderboardRankRow(cursor);
		} catch (SQLiteException e) {
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
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
	 * 
	 * @return A LeaderboardRankRow object for the first leaderboard rank record
	 *         stored in the given cursor. Null in case of an error.
	 */
	private LeaderboardRankRow toLeaderboardRankRow(Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a leaderboard rank row.
		LeaderboardRankRow leaderboardRankRow = new LeaderboardRankRow();
		leaderboardRankRow.mLeaderboardId = cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_LEADERBOARD_ID));
		leaderboardRankRow.mStatisticsId = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_STATISTICS_ID));
		leaderboardRankRow.mRawScore = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_RAW_SCORE));
		leaderboardRankRow.mDateSubmitted = valueOfSQLiteTimestamp(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_DATE_SUBMITTED)));
		leaderboardRankRow.mRank = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_RANK));
		leaderboardRankRow.mRankDisplay = cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_RANK_DISPLAY));
		leaderboardRankRow.mDateLastUpdated = valueOfSQLiteTimestamp(cursor
				.getString(cursor
						.getColumnIndexOrThrow(KEY_RANK_DATE_LAST_UPDATED)));

		return leaderboardRankRow;
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

	/**
	 * Get the leaderboard for which the score has been submitted the longest
	 * ago but for which no ranking information has been registered.
	 * 
	 * @return The leaderboard which needs to be submitted again.
	 */
	public LeaderboardRankRow getOldestLeaderboardWithoutRank() {
		LeaderboardRankRow leaderboardRankRow = null;
		Cursor cursor = null;
		try {
			cursor = mSqliteDatabase.query(true, TABLE, allColumns,
					KEY_RANK_DATE_LAST_UPDATED + " is null", null, null, null,
					KEY_DATE_SUBMITTED + " ASC", "1");
			leaderboardRankRow = toLeaderboardRankRow(cursor);
		} catch (SQLiteException e) {
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return leaderboardRankRow;
	}
}