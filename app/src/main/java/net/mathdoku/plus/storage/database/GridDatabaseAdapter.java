package net.mathdoku.plus.storage.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridgenerating.GridGeneratingParametersBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;

/**
 * The database adapter for the grid table.
 */
public class GridDatabaseAdapter extends DatabaseAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = GridDatabaseAdapter.class.getName();

	// Remove "&& false" in following line to show the SQL-statements in the
	// debug information
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SQL = Config.mAppMode == AppMode.DEVELOPMENT && false;

	// Columns for table statistics
	static final String TABLE = "grid";
	static final String KEY_ROWID = "_id";
	private static final String KEY_DEFINITION = "definition";
	static final String KEY_GRID_SIZE = "grid_size";
	private static final String KEY_DATE_CREATED = "date_created";
	private static final String KEY_GAME_SEED = "game_seed";
	private static final String KEY_GENERATOR_REVISION_NUMBER = "generator_revision_number";
	private static final String KEY_PUZZLE_COMPLEXITY = "puzzle_complexity";
	private static final String KEY_HIDE_OPERATORS = "hide_operators";
	private static final String KEY_MAX_CAGE_RESULT = "max_cage_result";
	private static final String KEY_MAX_CAGE_SIZE = "max_cage_size";

	private static final String[] allColumns = { KEY_ROWID, KEY_DEFINITION,
			KEY_GRID_SIZE, KEY_DATE_CREATED, KEY_GAME_SEED,
			KEY_GENERATOR_REVISION_NUMBER, KEY_PUZZLE_COMPLEXITY,
			KEY_HIDE_OPERATORS, KEY_MAX_CAGE_RESULT, KEY_MAX_CAGE_SIZE };

	// Columns used in result of function getLatestSolvingAttemptsPerGrid
	public static final int LATEST_SOLVING_ATTEMPT_PER_GRID__GRID_ID = 0;
	public static final int LATEST_SOLVING_ATTEMPT_PER_GRID__SOLVING_ATTEMPT_ID = 1;

	// Allowed values for the status filter
	public enum StatusFilter {
		ALL, UNFINISHED, SOLVED, REVEALED
	}

	@Override
	protected String getTableName() {
		return TABLE;
	}

	/**
	 * Builds the SQL create statement for this table.
	 * 
	 * @return The SQL create statement for this table.
	 */
	private static String buildCreateSQL() {
		return createTable(
				TABLE,
				createColumn(KEY_ROWID, "integer", "primary key autoincrement"),
				createColumn(KEY_DEFINITION, "text", "not null unique"),
				createColumn(KEY_GRID_SIZE, "integer", " not null"),
				createColumn(KEY_DATE_CREATED, "datetime", "not null"),
				// Grid generating parameters.
				// These values can be null as they are not known for
				// historic games. Neither will they be know when games
				// will be exchanged in the future.
				createColumn(KEY_GAME_SEED, "long", null),
				// changes in tables
				createColumn(KEY_GENERATOR_REVISION_NUMBER, "integer", null),
				createColumn(KEY_PUZZLE_COMPLEXITY, "string", null),
				createColumn(KEY_HIDE_OPERATORS, "string", null),
				createColumn(KEY_MAX_CAGE_RESULT, "integer", null),
				createColumn(KEY_MAX_CAGE_SIZE, "integer", null));
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
	static void create(SQLiteDatabase db) {
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
	static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (Config.mAppMode == AppMode.DEVELOPMENT && oldVersion < 432
				&& newVersion >= 432) {
			recreateTableInDevelopmentMode(db, TABLE, buildCreateSQL());
		}
	}

	/**
	 * Inserts a new grid into the database. The grid definition should be
	 * unique. The record should be created as soon as the grid is created.
	 * 
	 * @param grid
	 *            The grid which has to be inserted into the database.
	 * @return The unique row id of the grid created. -1 in case of an error.
	 */
	public int insert(Grid grid) {
		String gridDefinition = grid.getDefinition();
		if (gridDefinition == null || gridDefinition.trim().equals("")) {
			// TODO: better handling of situation in which a grid definition was
			// added before. It is a very rare situation but it can occur.
			throw new DatabaseException("Definition of grid is not unique.");
		}
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DEFINITION, gridDefinition);
		initialValues.put(KEY_GRID_SIZE, grid.getGridSize());
		initialValues.put(KEY_DATE_CREATED,
				toSQLiteTimestamp(grid.getDateCreated()));
		GridGeneratingParameters gridGeneratingParameters = grid
				.getGridGeneratingParameters();
		initialValues
				.put(KEY_GAME_SEED, gridGeneratingParameters.getGameSeed());
		initialValues.put(KEY_GENERATOR_REVISION_NUMBER,
				gridGeneratingParameters.getGeneratorVersionNumber());
		initialValues.put(KEY_PUZZLE_COMPLEXITY, gridGeneratingParameters
				.getPuzzleComplexity()
				.toString());
		initialValues.put(KEY_HIDE_OPERATORS,
				toSQLiteBoolean(gridGeneratingParameters.isHideOperators()));
		initialValues.put(KEY_MAX_CAGE_RESULT,
				gridGeneratingParameters.getMaxCageResult());
		initialValues.put(KEY_MAX_CAGE_SIZE,
				gridGeneratingParameters.getMaxCageSize());

		int id;
		try {
			id = (int) mSqliteDatabase
					.insertOrThrow(TABLE, null, initialValues);
		} catch (SQLiteConstraintException e) {
			throw new DatabaseException("Cannot insert new grid in database.",
					e);
		}

		if (id < 0) {
			throw new DatabaseException(
					"Insert of new puzzle failed when inserting the grid into the database.");
		}

		return id;
	}

	/**
	 * Get a grid by searching on (row) id.
	 * 
	 * @param id
	 *            The unique row id of the grid to be found.
	 * @return The grid with the given id.
	 */
	public GridRow get(int id) {
		GridRow gridRow = null;
		Cursor cursor = null;
		try {
			cursor = mSqliteDatabase.query(true, TABLE, allColumns, KEY_ROWID
					+ "=" + id, null, null, null, null, null);
			gridRow = toGridRow(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseException(String.format(
					"Cannot retrieve gridRow with id '%d' from database", id),
					e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return gridRow;
	}

	/**
	 * Get a grid by searching for its definition.
	 * 
	 * @param definition
	 *            The unique grid definition of the grid to be found.
	 * @return The grid with the given definition. Null in case of an error.
	 */
	public GridRow getByGridDefinition(String definition) {
		GridRow gridRow = null;
		Cursor cursor = null;
		try {
			cursor = mSqliteDatabase.query(true, TABLE, allColumns,
					KEY_DEFINITION + "=" + stringBetweenQuotes(definition),
					null, null, null, null, null);
			gridRow = toGridRow(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseException(String.format(
					"Cannot retrieve grid with definition '%s' from database",
					definition), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return gridRow;
	}

	/**
	 * Convert first record in the given cursor to a GridRow object.
	 * 
	 * @param cursor
	 *            The cursor to be converted.
	 * @return A GridRow object for the first grid record stored in the given
	 *         cursor. Null in case of an error.
	 */
	private GridRow toGridRow(Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a grid statics object.
		GridRow gridRow = new GridRow();
		gridRow.mId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ROWID));
		gridRow.mDefinition = cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_DEFINITION));
		gridRow.mGridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_GRID_SIZE));
		gridRow.mDateCreated = valueOfSQLiteTimestamp(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_DATE_CREATED)));

		gridRow.mGridGeneratingParameters = new GridGeneratingParametersBuilder()
				.setGridType(getGridTypeFromCursor(cursor))
				.setHideOperators(getHideOperatorFromCursor(cursor))
				.setPuzzleComplexity(getPuzzleComplexityFromCursor(cursor))
				.setGeneratorVersionNumber(
						getGeneratorRevisionNumberFromCursor(cursor))
				.setGameSeed(getGameSeedFromCursor(cursor))
				.setMaxCageResult(getMaxCageResultFromCursor(cursor))
				.setMaxCageSize(getMaxCageSizeFromCursor(cursor))
				.createGridGeneratingParameters();

		return gridRow;
	}

	private GridType getGridTypeFromCursor(Cursor cursor) {
		return GridType.fromInteger(cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_GRID_SIZE)));
	}

	private boolean getHideOperatorFromCursor(Cursor cursor) {
		return valueOfSQLiteBoolean(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_HIDE_OPERATORS)));
	}

	private PuzzleComplexity getPuzzleComplexityFromCursor(Cursor cursor) {
		return PuzzleComplexity.valueOf(cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_PUZZLE_COMPLEXITY)));
	}

	private int getGeneratorRevisionNumberFromCursor(Cursor cursor) {
		return cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_GENERATOR_REVISION_NUMBER));
	}

	private long getGameSeedFromCursor(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GAME_SEED));
	}

	private int getMaxCageResultFromCursor(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MAX_CAGE_RESULT));
	}

	private int getMaxCageSizeFromCursor(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MAX_CAGE_SIZE));
	}

	/**
	 * Prefix the given column name with the table name.
	 * 
	 * @param column
	 *            The column name which has to be prefixed.
	 * @return The prefixed column name.
	 */
	public static String getPrefixedColumnName(String column) {
		return TABLE + "." + column;
	}

	/**
	 * Get a list of all grid id's and the latest solving attempt per grid.
	 * 
	 * @return The list of all grid id's and the latest solving attempt per
	 *         grid.
	 */
	public int[][] getLatestSolvingAttemptsPerGrid(StatusFilter statusFilter,
			GridTypeFilter gridTypeFilter) {
		String keySolvingAttemptId = "solving_attempt_id";

		// Build projection
		Projection projection = new Projection();
		projection.put(KEY_ROWID, TABLE, KEY_ROWID);
		projection.put(keySolvingAttemptId,
				SolvingAttemptDatabaseAdapter.TABLE,
				SolvingAttemptDatabaseAdapter.KEY_ROWID);

		// Build query
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(projection);
		sqliteQueryBuilder
				.setTables(TABLE
						+ " INNER JOIN "
						+ SolvingAttemptDatabaseAdapter.TABLE
						+ " ON "
						+ SolvingAttemptDatabaseAdapter
								.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_GRID_ID)
						+ " = " + getPrefixedColumnName(KEY_ROWID));

		// Determine where clause elements for the filters.
		String selectionStatus = getStatusSelectionString(statusFilter);
		String selectionSize = getSizeSelectionString(gridTypeFilter);

		// Build where clause. Note: it is not possible to use an aggregation
		// function like MAX(solving_attempt_id) as the selection on status
		// should only be based on the status of the last solving attempt. Using
		// an aggregate function results in wrong data when a status filter is
		// applied as the aggregation would only be based on the solving attempt
		// which do match the status while ignore the fact that a more recent
		// solving attempt exists with another status.
		String selection = "not exists (select 1 from "
				+ SolvingAttemptDatabaseAdapter.TABLE
				+ " as sa2 where sa2."
				+ SolvingAttemptDatabaseAdapter.KEY_GRID_ID
				+ " = "
				+ SolvingAttemptDatabaseAdapter
						.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_GRID_ID)
				+ " and sa2."
				+ SolvingAttemptDatabaseAdapter.KEY_ROWID
				+ " > "
				+ SolvingAttemptDatabaseAdapter
						.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_ROWID)
				+ ") "
				+ (!selectionStatus.isEmpty() ? " AND " + selectionStatus : "")
				+ (!selectionSize.isEmpty() ? " AND " + selectionSize : "");

		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(
					projection.getAllColumnNames(), selection, KEY_ROWID, null,
					null, null);
			Log.i(TAG, sql);
		}

		// Convert results in cursor to array of grid id's
		int[][] gridIds = null;
		Cursor cursor = null;
		try {
			cursor = sqliteQueryBuilder.query(mSqliteDatabase,
					projection.getAllColumnNames(), selection, null, KEY_ROWID,
					null, null);
			if (cursor != null && cursor.moveToFirst()) {
				gridIds = new int[cursor.getCount()][2];
				int i = 0;
				int gridIdColumnIndex = cursor.getColumnIndexOrThrow(KEY_ROWID);
				int maxSolvingAttemptColumnIndex = cursor
						.getColumnIndexOrThrow(keySolvingAttemptId);
				do {
					gridIds[i][LATEST_SOLVING_ATTEMPT_PER_GRID__GRID_ID] = cursor
							.getInt(gridIdColumnIndex);
					gridIds[i][LATEST_SOLVING_ATTEMPT_PER_GRID__SOLVING_ATTEMPT_ID] = cursor
							.getInt(maxSolvingAttemptColumnIndex);

					i++;
				} while (cursor.moveToNext());
			}
		} catch (SQLiteException e) {
			throw new DatabaseException(String.format(
					"Cannot retrieve latest solving attempt per grid from the database "
							+ "(status filter = %s, size filter = %s).",
					statusFilter.toString(), gridTypeFilter.toString()), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return gridIds;
	}

	/**
	 * Get a list of statuses used by grids which matches with the given size
	 * filter.
	 * 
	 * @param sizeFilter
	 *            The size filter which has to be matched by the grids.
	 * @return The list of statuses used by grids which matches with the given
	 *         size filter.
	 */
	public StatusFilter[] getUsedStatuses(GridTypeFilter sizeFilter) {
		// Build the projection
		Projection projection = new Projection();
		final String keyStatusFilter = "status_filter";
		projection
				.put(keyStatusFilter,
						"CASE WHEN "
								+ stringBetweenBackTicks(SolvingAttemptDatabaseAdapter.KEY_STATUS)
								+ " = "
								+ SolvingAttemptStatus.REVEALED_SOLUTION
										.getId()
								+ " THEN "
								+ StatusFilter.REVEALED.ordinal()
								+ " WHEN "
								+ stringBetweenBackTicks(SolvingAttemptDatabaseAdapter.KEY_STATUS)
								+ " = "
								+ SolvingAttemptStatus.FINISHED_SOLVED.getId()
								+ " THEN " + StatusFilter.SOLVED.ordinal()
								+ " ELSE " + StatusFilter.UNFINISHED.ordinal()
								+ " END");

		// Build query
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(projection);
		sqliteQueryBuilder
				.setTables(TABLE
						+ " INNER JOIN "
						+ SolvingAttemptDatabaseAdapter.TABLE
						+ " as sa1 "
						+ " ON sa1."
						+ stringBetweenBackTicks(SolvingAttemptDatabaseAdapter.KEY_GRID_ID)
						+ " = " + getPrefixedColumnName(KEY_ROWID));

		// Retrieve all data. Note: in case column is not added to the
		// projection, no data will be retrieved!
		String[] columnsData = { keyStatusFilter };

		// Build where clause. Be sure only to retrieve the status of the last
		// solving attempt of a grid as the archive will only display the last
		// solving attempt.
		String sizeSelection = getSizeSelectionString(sizeFilter);
		String selection = sizeSelection
				+ (sizeSelection.isEmpty() ? "" : " AND ")
				+ " NOT EXISTS ( "
				+ "SELECT 1 "
				+ " FROM "
				+ SolvingAttemptDatabaseAdapter.TABLE
				+ " as sa2"
				+ " WHERE sa2."
				+ stringBetweenBackTicks(SolvingAttemptDatabaseAdapter.KEY_GRID_ID)
				+ " = sa1."
				+ stringBetweenBackTicks(SolvingAttemptDatabaseAdapter.KEY_GRID_ID)
				+ " AND sa2."
				+ stringBetweenBackTicks(SolvingAttemptDatabaseAdapter.KEY_ROWID)
				+ " > sa1."
				+ stringBetweenBackTicks(SolvingAttemptDatabaseAdapter.KEY_ROWID)
				+ ")";

		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(columnsData, selection,
					keyStatusFilter, null, keyStatusFilter, null);
			Log.i(TAG, sql);
		}

		// Convert results in cursor to array of grid id's
		StatusFilter[] statuses = null;
		Cursor cursor = null;
		try {
			cursor = sqliteQueryBuilder.query(mSqliteDatabase, columnsData,
					selection, null, keyStatusFilter, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				statuses = new StatusFilter[cursor.getCount() + 1];
				statuses[0] = StatusFilter.ALL;
				int i = 1;
				int columnIndex = cursor.getColumnIndexOrThrow(keyStatusFilter);
				do {
					int status = cursor.getInt(columnIndex);
					if (status == StatusFilter.UNFINISHED.ordinal()) {
						statuses[i++] = StatusFilter.UNFINISHED;
					} else if (status == StatusFilter.SOLVED.ordinal()) {
						statuses[i++] = StatusFilter.SOLVED;
					} else if (status == StatusFilter.REVEALED.ordinal()) {
						statuses[i++] = StatusFilter.REVEALED;
					}
				} while (cursor.moveToNext());
			}
		} catch (SQLiteException e) {
			throw new DatabaseException(
					String.format(
							"Cannot retrieve used statuses of latest solving attempt per grid from the database (size filter = %s).",
							sizeFilter.toString()), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return statuses;
	}

	/**
	 * Get a list of sized used by grids which matches with the given status
	 * filter.
	 * 
	 * @param statusFilter
	 *            The status filter which has to be matched by the grids.
	 * @return The list of sizes used by grids which matches with the given
	 *         status filter.
	 */
	public GridType[] getUsedSizes(StatusFilter statusFilter) {
		// Build the projection
		Projection projection = new Projection();
		projection.put(KEY_GRID_SIZE, TABLE, KEY_GRID_SIZE);

		// Build query
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(projection);
		sqliteQueryBuilder
				.setTables(TABLE
						+ " INNER JOIN "
						+ SolvingAttemptDatabaseAdapter.TABLE
						+ " ON "
						+ SolvingAttemptDatabaseAdapter
								.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_GRID_ID)
						+ " = " + getPrefixedColumnName(KEY_ROWID));

		// Retrieve all data. Note: in case column is not added to the
		// projection, no data will be retrieved!
		String[] columnsData = { stringBetweenBackTicks(KEY_GRID_SIZE) };

		// Build where clause
		String selection = getStatusSelectionString(statusFilter);

		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(columnsData, selection,
					KEY_GRID_SIZE, null, KEY_GRID_SIZE, null);
			Log.i(TAG, sql);
		}

		// Convert results in cursor to array of grid id's
		GridType[] sizes = null;
		Cursor cursor = null;
		try {
			cursor = sqliteQueryBuilder.query(mSqliteDatabase, columnsData,
					selection, null, KEY_GRID_SIZE, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				sizes = new GridType[cursor.getCount()];
				int i = 0;
				int columnIndex = cursor.getColumnIndexOrThrow(KEY_GRID_SIZE);
				do {
					sizes[i++] = GridType.fromInteger(cursor
							.getInt(columnIndex));
				} while (cursor.moveToNext());
			}
		} catch (SQLiteException e) {
			throw new DatabaseException(
					String.format(
							"Cannot retrieve used sizes of latest solving attempt per grid from the database (status filter = %s).",
							statusFilter.toString()), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return sizes;
	}

	/**
	 * Get the SQL where clause to select solving attempts for which the status
	 * matches the given status filter.
	 * 
	 * @param statusFilter
	 *            The status filter to be matched.
	 * @return The SQL where clause which matches solving attempts with the
	 *         given status filter.
	 */
	private String getStatusSelectionString(StatusFilter statusFilter) {
		// Determine selection for status filter
		switch (statusFilter) {
		case ALL:
			// no filter on status
			return "";
		case REVEALED:
			return SolvingAttemptDatabaseAdapter
					.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_STATUS)
					+ " = " + SolvingAttemptStatus.REVEALED_SOLUTION.getId();
		case SOLVED:
			return SolvingAttemptDatabaseAdapter
					.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_STATUS)
					+ " = " + SolvingAttemptStatus.FINISHED_SOLVED.getId();
		case UNFINISHED:
			return SolvingAttemptDatabaseAdapter
					.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_STATUS)
					+ " IN ("
					+ SolvingAttemptStatus.NOT_STARTED.getId()
					+ ","
					+ SolvingAttemptStatus.UNFINISHED.getId() + ")";
		}
		return null;
	}

	/**
	 * Get the SQL where clause to select solving attempts for which the size
	 * matches the given size filter.
	 * 
	 * @param gridTypeFilter
	 *            The size filter to be matched.
	 * @return The SQL where clause which matches solving attempts with the
	 *         given size filter.
	 */
	private String getSizeSelectionString(GridTypeFilter gridTypeFilter) {
		return gridTypeFilter == GridTypeFilter.ALL ? ""
				: getPrefixedColumnName(KEY_GRID_SIZE) + " = "
						+ gridTypeFilter.getGridType();
	}

	/**
	 * Counts the number of grids having a specific status and or size.
	 * 
	 * @param statusFilter
	 *            The status to be matched.
	 * @param sizeFilter
	 *            The size to be matched.
	 * @return The number of grids having a specific status and or size.
	 */
	@SuppressWarnings("SameParameterValue")
	public int countGrids(StatusFilter statusFilter, GridTypeFilter sizeFilter) {
		int[][] gridIds = getLatestSolvingAttemptsPerGrid(statusFilter,
				sizeFilter);
		return gridIds == null ? 0 : gridIds.length;
	}

}
