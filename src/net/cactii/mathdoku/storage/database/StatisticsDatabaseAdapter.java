package net.cactii.mathdoku.storage.database;

import java.security.InvalidParameterException;

import net.cactii.mathdoku.statistics.GridStatistics;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

/**
 * The database adapter for the statistics table.
 */
public class StatisticsDatabaseAdapter extends DatabaseAdapter {

	static final String TAG = "MathDoku.StatisticsDatabaseAdapter";

	// Columns for table statistics
	static final String TABLE = "statistics";
	static final String KEY_ROWID = "_id";
	static final String KEY_GRID_SIGNATURE = "grid_signature";
	static final String KEY_GRID_SIZE = "grid_size";
	static final String KEY_FIRST_MOVE = "first_move";
	static final String KEY_LAST_MOVE = "last_move";
	static final String KEY_ELAPSED_TIME = "elapsed_time";
	static final String KEY_CHEAT_PENALTY_TIME = "cheat_penalty_time";
	static final String KEY_MOVES = "moves";
	static final String KEY_POSSIBLES = "possibles";
	static final String KEY_UNDOS = "undos";
	static final String KEY_CELLS_REVEALED = "cells_revealed";
	static final String KEY_OPERATORS_REVEALED = "operators_revealed";
	static final String KEY_CHECK_PROGRESS_USED = "check_progress_used";
	static final String KEY_CHECK_PROGRESS_INVALIDS_FOUND = "check_progress_invalids_found";
	static final String KEY_SOLUTION_REVEALED = "solution_revealed";
	static final String KEY_SOLVED_MANUALLY = "solved_manually";
	static final String KEY_FINISHED = "finished";

	static final String[] allColumns = { KEY_ROWID, KEY_GRID_SIGNATURE,
			KEY_GRID_SIZE, KEY_FIRST_MOVE, KEY_LAST_MOVE, KEY_ELAPSED_TIME,
			KEY_CHEAT_PENALTY_TIME, KEY_MOVES, KEY_POSSIBLES, KEY_UNDOS,
			KEY_CELLS_REVEALED, KEY_OPERATORS_REVEALED,
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
						createColumn(KEY_MOVES, "integer",
								" not null default 0"),
						createColumn(KEY_POSSIBLES, "integer",
								" not null default 0"),
						createColumn(KEY_UNDOS, "integer",
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
	 * record should be created as soon as the user makes his very first move.
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

		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a grid statics object.
		GridStatistics gridStatistics = new GridStatistics();
		gridStatistics._id = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_ROWID));
		gridStatistics.gridSignature = cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_GRID_SIGNATURE));
		gridStatistics.gridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_GRID_SIZE));
		gridStatistics.firstMove = java.sql.Timestamp.valueOf(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_MOVE)));
		gridStatistics.lastMove = java.sql.Timestamp.valueOf(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_LAST_MOVE)));
		gridStatistics.elapsedTime = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_ELAPSED_TIME));
		gridStatistics.cheatPenaltyTime = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_CHEAT_PENALTY_TIME));
		gridStatistics.moves = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_MOVES));
		gridStatistics.possibles = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_POSSIBLES));
		gridStatistics.undos = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_UNDOS));
		gridStatistics.cellsRevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CELLS_REVEALED));
		gridStatistics.operatorsRevevealed = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_OPERATORS_REVEALED));
		gridStatistics.checkProgressUsed = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CHECK_PROGRESS_USED));
		gridStatistics.checkProgressInvalidsFound = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_CHECK_PROGRESS_INVALIDS_FOUND));
		gridStatistics.solutionRevealed = Boolean
				.valueOf(cursor.getString(cursor
						.getColumnIndexOrThrow(KEY_SOLUTION_REVEALED)));
		gridStatistics.solvedManually = Boolean.valueOf(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_SOLVED_MANUALLY)));
		gridStatistics.finished = Boolean.valueOf(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_FINISHED)));

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
		newValues.put(KEY_ROWID, gridStatistics._id);
		newValues.put(KEY_GRID_SIGNATURE, gridStatistics.gridSignature);
		newValues.put(KEY_GRID_SIZE, gridStatistics.gridSize);
		newValues.put(KEY_FIRST_MOVE, gridStatistics.firstMove.toString());
		newValues.put(KEY_LAST_MOVE, gridStatistics.lastMove.toString());
		newValues.put(KEY_ELAPSED_TIME, gridStatistics.elapsedTime);
		newValues.put(KEY_CHEAT_PENALTY_TIME, gridStatistics.cheatPenaltyTime);
		newValues.put(KEY_MOVES, gridStatistics.moves);
		newValues.put(KEY_POSSIBLES, gridStatistics.possibles);
		newValues.put(KEY_UNDOS, gridStatistics.undos);
		newValues.put(KEY_CELLS_REVEALED, gridStatistics.cellsRevealed);
		newValues.put(KEY_OPERATORS_REVEALED,
				gridStatistics.operatorsRevevealed);
		newValues
				.put(KEY_CHECK_PROGRESS_USED, gridStatistics.checkProgressUsed);
		newValues.put(KEY_CHECK_PROGRESS_INVALIDS_FOUND,
				gridStatistics.checkProgressInvalidsFound);
		newValues.put(KEY_SOLUTION_REVEALED, gridStatistics.solutionRevealed);
		newValues.put(KEY_SOLVED_MANUALLY, gridStatistics.solvedManually);
		newValues.put(KEY_FINISHED, gridStatistics.finished);

		return (db
				.update(TABLE,
						newValues,
						KEY_ROWID
								+ " = "
								+ gridStatistics._id
								+ " AND "
								+ KEY_GRID_SIGNATURE
								+ " = "
								+ DatabaseAdapter
										.stringBetweenQuotes(gridStatistics.gridSignature),
						null) == 1);

	}
}
