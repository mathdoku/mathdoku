package net.cactii.mathdoku.storage.database;

import java.security.InvalidParameterException;

import net.cactii.mathdoku.statistics.CumulativeStatistics;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.statistics.HistoricStatistics;
import net.cactii.mathdoku.statistics.HistoricStatistics.Serie;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

/**
 * The database adapter for the statistics table.
 */
public class StatisticsDatabaseAdapter extends DatabaseAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.StatisticsDatabaseAdapter";

	// Columns for table statistics
	private static final String TABLE = "statistics";
	private static final String KEY_ROWID = "_id";
	private static final String KEY_GRID_SIGNATURE = "grid_signature";
	private static final String KEY_GRID_SIZE = "grid_size";
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

	private static final String[] allColumns = { KEY_ROWID, KEY_GRID_SIGNATURE,
			KEY_GRID_SIZE, KEY_FIRST_MOVE, KEY_LAST_MOVE, KEY_ELAPSED_TIME,
			KEY_CHEAT_PENALTY_TIME, KEY_CELLS_USER_VALUE_FILLED,
			KEY_CELLS_USER_VALUES_EMPTY, KEY_CELLS_USER_VALUES_REPLACED,
			KEY_POSSIBLES, KEY_UNDOS, KEY_CELLS_CLEARED, KEY_CAGE_CLEARED,
			KEY_GRID_CLEARED, KEY_CELLS_REVEALED, KEY_OPERATORS_REVEALED,
			KEY_CHECK_PROGRESS_USED, KEY_CHECK_PROGRESS_INVALIDS_FOUND,
			KEY_SOLUTION_REVEALED, KEY_SOLVED_MANUALLY, KEY_FINISHED };

	/**
	 * Constructs a new instance of the statistics database adapter.
	 * 
	 * @param databaseHelper
	 *            The database helper needed to open the adapter.
	 */
	public StatisticsDatabaseAdapter(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}

	/**
	 * Creates the table.
	 * 
	 * @param db
	 *            The database in which the table has to be created.
	 */
	protected static void create(SQLiteDatabase db) {
		// Build create statement
		String createSQL = DatabaseAdapter
				.createTable(
						TABLE,
						createColumn(KEY_ROWID, "integer",
								"primary key autoincrement"),
						createColumn(KEY_GRID_SIGNATURE, "text",
								"not null unique"),
						createColumn(KEY_GRID_SIZE, "integer", " not null"),
						createColumn(KEY_FIRST_MOVE, "datetime", "not null"),
						createColumn(KEY_LAST_MOVE, "datetime", "not null"),
						createColumn(KEY_ELAPSED_TIME, "long",
								"not null default 0"),
						createColumn(KEY_CHEAT_PENALTY_TIME, "long",
								"not null default 0"),
						createColumn(KEY_CELLS_USER_VALUE_FILLED, "integer",
								" not null default 0"),
						createColumn(KEY_CELLS_USER_VALUES_EMPTY, "integer",
								" not null default 0"),
						createColumn(KEY_CELLS_USER_VALUES_REPLACED, "integer",
								" not null default 0"),
						createColumn(KEY_POSSIBLES, "integer",
								" not null default 0"),
						createColumn(KEY_UNDOS, "integer",
								" not null default 0"),
						createColumn(KEY_CELLS_CLEARED, "integer",
								" not null default 0"),
						createColumn(KEY_CAGE_CLEARED, "integer",
								" not null default 0"),
						createColumn(KEY_GRID_CLEARED, "integer",
								" not null default 0"),
						createColumn(KEY_CELLS_REVEALED, "integer",
								" not null default 0"),
						createColumn(KEY_OPERATORS_REVEALED, "integer",
								" not null default 0"),
						createColumn(KEY_CHECK_PROGRESS_USED, "integer",
								" not null default 0"),
						createColumn(KEY_CHECK_PROGRESS_INVALIDS_FOUND,
								"integer", " not null default 0"),
						createColumn(KEY_SOLUTION_REVEALED, "string",
								" not null default `false`"),
						createColumn(KEY_SOLVED_MANUALLY, "string",
								" not null default `false`"),
						createColumn(KEY_FINISHED, "string",
								" not null default `false`"));

		// Execute create statement
		db.execSQL(createSQL);

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
		if (oldVersion > 0 && newVersion > oldVersion) {
			// In development revisions the table is simply dropped and
			// recreated.
			db.execSQL("DROP TABLE " + TABLE);
			create(db);
		}
	}

	/**
	 * Inserts a new grid into the database. The signature should be unique. The
	 * record should be created as soon as the grid is created.
	 * 
	 * @param signature
	 *            The unique signature of the game.
	 * @param gridSize
	 *            The size of the grid.
	 * @return The grid statistics created. Null in case of an error.
	 * @throws InvalidParameterException
	 *             In case the signature is empty or null.
	 * @throws SQLException
	 *             In case the signature is not unique.
	 */
	public GridStatistics insertGrid(String signature, int gridSize)
			throws InvalidParameterException, SQLException {
		java.sql.Timestamp now = new java.sql.Timestamp(
				new java.util.Date().getTime());
		if (signature == null || signature.trim().equals("")) {
			throw new InvalidParameterException(
					"Signature of grid is not unique");
		}
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_GRID_SIGNATURE, signature.trim());
		initialValues.put(KEY_GRID_SIZE, gridSize);
		initialValues.put(KEY_CELLS_USER_VALUES_EMPTY, gridSize * gridSize);
		initialValues.put(KEY_FIRST_MOVE, now.toString());
		initialValues.put(KEY_LAST_MOVE, now.toString());

		long id;
		try {
			id = db.insertOrThrow(TABLE, null, initialValues);
		} catch (SQLiteConstraintException e) {
			InvalidParameterException ipe = new InvalidParameterException(
					e.getLocalizedMessage());
			ipe.initCause(e);
			throw ipe;
		}
		if (id < 0) {
			return null;
		}

		// Retrieve the record created.
		return getByGridSignature(signature);
	}

	/**
	 * Get a grid by searching on (row) id.
	 * 
	 * @param id
	 *            The unique row id of the grid to be found.
	 * @return The grid statistics for the grid with the given signature id.
	 */
	public GridStatistics get(int signatureId) {
		Cursor cursor = db.query(true, TABLE, allColumns, KEY_ROWID + "="
				+ signatureId, null, null, null, null, null);
		GridStatistics gridStatistics = toGridStatistics(cursor);
		cursor.close();
		return gridStatistics;
	}

	/**
	 * Get a grid by searching on the signature.
	 * 
	 * @param signature
	 *            The unique grid signature of the grid to be found.
	 * @return The grid with the given signature. Null in case of an error.
	 */
	public GridStatistics getByGridSignature(String signature) {
		Cursor cursor = db.query(true, TABLE, allColumns, KEY_GRID_SIGNATURE
				+ "=" + DatabaseAdapter.stringBetweenQuotes(signature), null,
				null, null, null, null);
		GridStatistics gridStatistics = toGridStatistics(cursor);
		cursor.close();
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
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a grid statics object.
		GridStatistics gridStatistics = new GridStatistics();
		gridStatistics.mId = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ROWID));
		gridStatistics.mGridSignature = cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_GRID_SIGNATURE));
		gridStatistics.gridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_GRID_SIZE));
		gridStatistics.mFirstMove = java.sql.Timestamp.valueOf(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_MOVE)));
		gridStatistics.mLastMove = java.sql.Timestamp.valueOf(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_LAST_MOVE)));
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

		cursor.close();
		return gridStatistics;
	}

	/**
	 * Update the given statistics. It is required that the record already
	 * exists. The id and signature should never be changed.
	 * 
	 * @param gridStatistics
	 *            The statistics to be updated.
	 * 
	 * @return True in case the statistics have been updated. False otherwise.
	 */
	public boolean update(GridStatistics gridStatistics) {
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_ROWID, gridStatistics.mId);
		newValues.put(KEY_GRID_SIGNATURE, gridStatistics.mGridSignature);
		newValues.put(KEY_GRID_SIZE, gridStatistics.gridSize);
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

		return (db
				.update(TABLE,
						newValues,
						KEY_ROWID
								+ " = "
								+ gridStatistics.mId
								+ " AND "
								+ KEY_GRID_SIGNATURE
								+ " = "
								+ DatabaseAdapter
										.stringBetweenQuotes(gridStatistics.mGridSignature),
						null) == 1);

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
	 *            grid size to retireve statistics for 1 specific grid size.
	 * @return The cumulative statistics for the given grid size.
	 */
	public CumulativeStatistics getByGridSize(int minGridSize, int maxGridSize) {

		String[] columns = {
				// Grid size minimum and maximum
				min(KEY_GRID_SIZE),
				max(KEY_GRID_SIZE),
				// First and last move
				min(KEY_FIRST_MOVE),
				max(KEY_LAST_MOVE),
				// Total, average, minimum and maximum elapsed time
				sum(KEY_ELAPSED_TIME),
				avg(KEY_ELAPSED_TIME),
				min(KEY_ELAPSED_TIME),
				max(KEY_ELAPSED_TIME),
				// Average, minimum and maximum penalty time
				sum(KEY_CHEAT_PENALTY_TIME),
				avg(KEY_CHEAT_PENALTY_TIME),
				min(KEY_CHEAT_PENALTY_TIME),
				max(KEY_CHEAT_PENALTY_TIME),
				// not (yet) used KEY_CELLS_USER_VALUE_FILLED,
				// not (yet) used KEY_CELLS_USER_VALUES_EMPTY
				// not (yet) used KEY_CELLS_USER_VALUES_REPLACED,
				// Totals of avoidable moves
				sum(KEY_POSSIBLES),
				sum(KEY_UNDOS),
				sum(KEY_CELLS_CLEARED),
				sum(KEY_CAGE_CLEARED),
				sum(KEY_GRID_CLEARED),
				// Totals per cheat
				sum(KEY_CELLS_REVEALED),
				sum(KEY_OPERATORS_REVEALED),
				sum(KEY_CHECK_PROGRESS_USED),
				sum(KEY_CHECK_PROGRESS_INVALIDS_FOUND),
				// Totals per status of game
				countIf(KEY_SOLUTION_REVEALED, "true"),
				countIf(KEY_SOLVED_MANUALLY, "true"),
				countIf(KEY_FINISHED, "true"),
				// Total games
				"COUNT(1)" };

		Cursor cursor = db.query(true, TABLE, columns, KEY_GRID_SIZE
				+ " BETWEEN " + minGridSize + " AND " + maxGridSize, null,
				null, null, null, null);

		CumulativeStatistics cumulativeStatistics = toCumulativeStatistics(cursor);
		cursor.close();
		return cumulativeStatistics;
	}

	/**
	 * Convert first record in the given cursor to a CumulativeStatistics
	 * object.
	 * 
	 * @param cursor
	 *            The cursor to be converted.
	 * 
	 * @return A CumulativeStatistics object for the first statistics record
	 *         stored in the given cursor. Null in case of an error.
	 */
	private CumulativeStatistics toCumulativeStatistics(Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a grid statics object.
		CumulativeStatistics cumulativeStatistics = new CumulativeStatistics();

		cumulativeStatistics.mMinGridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(min(KEY_GRID_SIZE)));
		cumulativeStatistics.mMaxGridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(max(KEY_GRID_SIZE)));

		cumulativeStatistics.mMinFirstMove = java.sql.Timestamp.valueOf(cursor
				.getString(cursor.getColumnIndexOrThrow(min(KEY_FIRST_MOVE))));
		cumulativeStatistics.mMaxLastMove = java.sql.Timestamp.valueOf(cursor
				.getString(cursor.getColumnIndexOrThrow(max(KEY_LAST_MOVE))));

		cumulativeStatistics.mSumElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_ELAPSED_TIME)));
		cumulativeStatistics.mAvgElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(avg(KEY_ELAPSED_TIME)));
		cumulativeStatistics.mMinElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(min(KEY_ELAPSED_TIME)));
		cumulativeStatistics.mMaxElapsedTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(max(KEY_ELAPSED_TIME)));

		cumulativeStatistics.mSumCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mAvgCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(avg(KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mMinCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(min(KEY_CHEAT_PENALTY_TIME)));
		cumulativeStatistics.mMaxCheatPenaltyTime = cursor.getInt(cursor
				.getColumnIndexOrThrow(max(KEY_CHEAT_PENALTY_TIME)));

		// not (yet) used KEY_CELLS_USER_VALUE_FILLED,
		// not (yet) used KEY_CELLS_USER_VALUES_EMPTY
		// not (yet) used KEY_CELLS_USER_VALUES_REPLACED,

		cumulativeStatistics.mSumMaybeValue = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_POSSIBLES)));
		cumulativeStatistics.mSumUndoButton = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_UNDOS)));
		cumulativeStatistics.mSumCellCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_CELLS_CLEARED)));
		cumulativeStatistics.mSumCageCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_CAGE_CLEARED)));
		cumulativeStatistics.mSumGridCleared = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_GRID_CLEARED)));
		cumulativeStatistics.mSumCellsRevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_CELLS_REVEALED)));
		cumulativeStatistics.mSumOperatorsRevevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_OPERATORS_REVEALED)));
		cumulativeStatistics.mSumCheckProgressUsed = cursor.getInt(cursor
				.getColumnIndexOrThrow(sum(KEY_CHECK_PROGRESS_USED)));
		cumulativeStatistics.mSumcheckProgressInvalidsFound = cursor
				.getInt(cursor
						.getColumnIndexOrThrow(sum(KEY_CHECK_PROGRESS_INVALIDS_FOUND)));
		cumulativeStatistics.mCountSolutionRevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(countIf(KEY_SOLUTION_REVEALED, "true")));
		cumulativeStatistics.mCountSolvedManually = cursor.getInt(cursor
				.getColumnIndexOrThrow(countIf(KEY_SOLVED_MANUALLY, "true")));
		cumulativeStatistics.mCountFinished = cursor.getInt(cursor
				.getColumnIndexOrThrow(countIf(KEY_FINISHED, "true")));
		cumulativeStatistics.mCountStarted = cursor.getInt(cursor
				.getColumnIndexOrThrow("COUNT(1)"));

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
	public HistoricStatistics getHistoricData(String column, int minGridSize,
			int maxGridSize) {

		// Check if historic data is available for given column
		String[] validColumns = { KEY_ELAPSED_TIME, KEY_CHEAT_PENALTY_TIME,
				KEY_CELLS_USER_VALUE_FILLED, KEY_CELLS_USER_VALUES_EMPTY,
				KEY_CELLS_USER_VALUES_REPLACED, KEY_POSSIBLES, KEY_UNDOS,
				KEY_CELLS_CLEARED, KEY_CAGE_CLEARED, KEY_GRID_CLEARED,
				KEY_CELLS_REVEALED, KEY_OPERATORS_REVEALED,
				KEY_CHECK_PROGRESS_USED, KEY_CHECK_PROGRESS_INVALIDS_FOUND };
		boolean valid = false;
		for (String validColumn : validColumns) {
			if (validColumn.equals(column)) {
				valid = true;
				break;
			}
		}
		if (!valid) {
			return null;
		}

		// Retrieve all data
		String[] columnsData = {
				KEY_ROWID + " AS " + HistoricStatistics.DATA_COL_ID,
				column + " AS " + HistoricStatistics.DATA_COL_VALUE,
				"CASE WHEN "
						+ KEY_FINISHED
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
						+ " END AS " + HistoricStatistics.DATA_COL_SERIES };
		Cursor cursorData = db.query(true, TABLE, columnsData, KEY_GRID_SIZE
				+ " BETWEEN " + minGridSize + " AND " + maxGridSize, null,
				null, null, KEY_FIRST_MOVE, null);

		HistoricStatistics historicStatistics = new HistoricStatistics(
				cursorData);
		cursorData.close();

		return historicStatistics;
	}

}
