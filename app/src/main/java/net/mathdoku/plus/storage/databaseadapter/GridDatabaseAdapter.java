package net.mathdoku.plus.storage.databaseadapter;

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
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridgenerating.GridGeneratingParametersBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.storage.databaseadapter.database.DataType;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseColumnDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseTableDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.storage.selector.AvailableStatusFiltersSelector;

import java.util.List;

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

	private static final DatabaseTableDefinition DATABASE_TABLE = defineTable();

	// Columns for table statistics
	public static final String TABLE_NAME = "grid";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_DEFINITION = "definition";
	public static final String KEY_GRID_SIZE = "grid_size";
	public static final String KEY_DATE_CREATED = "date_created";
	public static final String KEY_GAME_SEED = "game_seed";
	public static final String KEY_GENERATOR_REVISION_NUMBER = "generator_revision_number";
	public static final String KEY_PUZZLE_COMPLEXITY = "puzzle_complexity";
	public static final String KEY_HIDE_OPERATORS = "hide_operators";
	public static final String KEY_MAX_CAGE_RESULT = "max_cage_result";
	public static final String KEY_MAX_CAGE_SIZE = "max_cage_size";

	// Allowed values for the status filter
	public enum StatusFilter { ALL, UNFINISHED, SOLVED, REVEALED }

	public GridDatabaseAdapter() {
		super();
	}

	private static DatabaseTableDefinition defineTable() {
		DatabaseTableDefinition databaseTableDefinition = new DatabaseTableDefinition(
				TABLE_NAME);
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_ROWID, DataType.INTEGER).setPrimaryKey());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_DEFINITION, DataType.STRING).setNotNull().setUniqueKey());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_GRID_SIZE, DataType.INTEGER).setNotNull());
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_DATE_CREATED, DataType.TIMESTAMP).setNotNull());
		// Grid generating parameters. These values can be null as they are not
		// known for historic games. Neither will they be know when games will
		// be exchanged in the future.
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_GAME_SEED, DataType.LONG));
		// changes in tables
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_GENERATOR_REVISION_NUMBER, DataType.INTEGER));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_PUZZLE_COMPLEXITY, DataType.STRING));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_HIDE_OPERATORS, DataType.STRING));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_MAX_CAGE_RESULT, DataType.INTEGER));
		databaseTableDefinition.addColumn(new DatabaseColumnDefinition(
				KEY_MAX_CAGE_SIZE, DataType.INTEGER));

		databaseTableDefinition.build();

		return databaseTableDefinition;
	}

	// Package private access, intended for DatabaseHelper only
	GridDatabaseAdapter(SQLiteDatabase sqLiteDatabase) {
		super(sqLiteDatabase);
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
		if (Config.mAppMode == AppMode.DEVELOPMENT && oldVersion < 432
				&& newVersion >= 432) {
			recreateTableInDevelopmentMode();
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
			throw new DatabaseAdapterException(
					"Definition of grid is not unique.");
		}
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DEFINITION, gridDefinition);
		initialValues.put(KEY_GRID_SIZE, grid.getGridSize());
		initialValues.put(KEY_DATE_CREATED,
				DatabaseUtil.toSQLiteTimestamp(grid.getDateCreated()));
		GridGeneratingParameters gridGeneratingParameters = grid
				.getGridGeneratingParameters();
		initialValues
				.put(KEY_GAME_SEED, gridGeneratingParameters.getGameSeed());
		initialValues.put(KEY_GENERATOR_REVISION_NUMBER,
				gridGeneratingParameters.getGeneratorVersionNumber());
		initialValues.put(KEY_PUZZLE_COMPLEXITY, gridGeneratingParameters
				.getPuzzleComplexity()
				.toString());
		initialValues.put(KEY_HIDE_OPERATORS, DatabaseUtil
				.toSQLiteBoolean(gridGeneratingParameters.isHideOperators()));
		initialValues.put(KEY_MAX_CAGE_RESULT,
				gridGeneratingParameters.getMaxCageResult());
		initialValues.put(KEY_MAX_CAGE_SIZE,
				gridGeneratingParameters.getMaxCageSize());

		int id;
		try {
			id = (int) sqliteDatabase.insertOrThrow(TABLE_NAME, null,
					initialValues);
		} catch (SQLiteConstraintException e) {
			throw new DatabaseAdapterException(
					"Cannot insert new grid in database.", e);
		}

		if (id < 0) {
			throw new DatabaseAdapterException(
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
			cursor = sqliteDatabase.query(true, TABLE_NAME,
					DATABASE_TABLE.getColumnNames(), KEY_ROWID + "=" + id,
					null, null, null, null, null);
			gridRow = toGridRow(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(String.format(
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
			cursor = sqliteDatabase.query(true, TABLE_NAME,
					DATABASE_TABLE.getColumnNames(), KEY_DEFINITION + "="
							+ DatabaseUtil.stringBetweenQuotes(definition),
					null, null, null, null, null);
			gridRow = toGridRow(cursor);
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(String.format(
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
		gridRow.mDateCreated = DatabaseUtil.valueOfSQLiteTimestamp(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_DATE_CREATED)));

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
		return DatabaseUtil.valueOfSQLiteBoolean(cursor.getString(cursor
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
		return DatabaseUtil.stringBetweenBackTicks(TABLE_NAME) + "."
				+ DatabaseUtil.stringBetweenBackTicks(column);
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
		// Build the databaseProjection
		DatabaseProjection databaseProjection = new DatabaseProjection();
		databaseProjection.put(KEY_GRID_SIZE, TABLE_NAME, KEY_GRID_SIZE);

		// Build query
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(databaseProjection);
		sqliteQueryBuilder
				.setTables(TABLE_NAME
						+ " INNER JOIN "
						+ SolvingAttemptDatabaseAdapter.TABLE_NAME
						+ " ON "
						+ SolvingAttemptDatabaseAdapter
								.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_GRID_ID)
						+ " = " + getPrefixedColumnName(KEY_ROWID));

		// Retrieve all data. Note: in case column is not added to the
		// databaseProjection, no data will be retrieved!
		String[] columnsData = { DatabaseUtil
				.stringBetweenBackTicks(KEY_GRID_SIZE) };

		// Build where clause
		String selection = SolvingAttemptDatabaseAdapter
				.getStatusSelectionString(statusFilter);

		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(columnsData, selection,
					KEY_GRID_SIZE, null, KEY_GRID_SIZE, null);
			Log.i(TAG, sql);
		}

		// Convert results in cursor to array of grid id's
		GridType[] sizes = null;
		Cursor cursor = null;
		try {
			cursor = sqliteQueryBuilder.query(sqliteDatabase, columnsData,
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
			throw new DatabaseAdapterException(
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
	 * Get the SQL where clause to select solving attempts for which the size
	 * matches the given size filter.
	 * 
	 * @param gridTypeFilter
	 *            The size filter to be matched.
	 * @return The SQL where clause which matches solving attempts with the
	 *         given size filter.
	 */
	public static String getSizeSelectionString(GridTypeFilter gridTypeFilter) {
		return gridTypeFilter == GridTypeFilter.ALL ? ""
				: getPrefixedColumnName(KEY_GRID_SIZE) + " = "
						+ gridTypeFilter.getGridType();
	}

}
