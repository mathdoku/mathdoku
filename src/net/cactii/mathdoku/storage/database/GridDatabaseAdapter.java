package net.cactii.mathdoku.storage.database;

import java.security.InvalidParameterException;

import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.gridGenerating.GridGeneratingParameters;
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
public class GridDatabaseAdapter extends DatabaseAdapter {

	private static final String TAG = "MathDoku.GridDatabaseAdapter";

	public static final boolean DEBUG_SQL = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && true;

	// Columns for table statistics
	protected static final String TABLE = "grid";
	protected static final String KEY_ROWID = "_id";
	protected static final String KEY_DEFINITION = "definition";
	protected static final String KEY_GRID_SIZE = "grid_size";
	protected static final String KEY_DATE_CREATED = "date_created";
	protected static final String KEY_GAME_SEED = "game_seed";
	protected static final String KEY_GENERATOR_REVISION_NUMBER = "generator_revision_number";
	protected static final String KEY_HIDE_OPERATORS = "hide_operators";
	protected static final String KEY_MAX_CAGE_RESULT = "max_cage_result";
	protected static final String KEY_MAX_CAGE_SIZE = "max_cage_size";

	private static final String[] allColumns = { KEY_ROWID, KEY_DEFINITION,
			KEY_GRID_SIZE, KEY_DATE_CREATED, KEY_GAME_SEED,
			KEY_GENERATOR_REVISION_NUMBER, KEY_HIDE_OPERATORS,
			KEY_MAX_CAGE_RESULT, KEY_MAX_CAGE_SIZE };

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
				createColumn(KEY_HIDE_OPERATORS, "string", null),
				createColumn(KEY_MAX_CAGE_RESULT, "integer", null),
				createColumn(KEY_MAX_CAGE_SIZE, "integer", null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cactii.mathdoku.storage.database.DatabaseAdapter#getCreateSQL()
	 */
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
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
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
		if (oldVersion < 265) {
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
	 * Inserts a new grid into the database. The grid definition should be
	 * unique. The record should be created as soon as the grid is created.
	 * 
	 * @param grid
	 *            The grid which has to be inserted into the database.
	 * @return The unique rowid of the grid created. -1 in case of an error.
	 * @throws InvalidParameterException
	 *             In case the definition is empty or null.
	 * @throws SQLException
	 *             In case the definition is not unique.
	 */
	public int insert(Grid grid) throws InvalidParameterException, SQLException {
		int id = -1;

		String gridDefinition = grid.toGridDefinitionString();
		if (gridDefinition == null || gridDefinition.trim().equals("")) {
			// TODO: better handling of situation in which a grid definition was
			// added before. It is a very rare situation but it can occur.
			throw new InvalidParameterException(
					"Definition of grid is not unique.");
		}
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DEFINITION, gridDefinition);
		initialValues.put(KEY_GRID_SIZE, grid.getGridSize());
		initialValues.put(KEY_DATE_CREATED,
				toSQLiteTimestamp(grid.getDateCreated()));
		GridGeneratingParameters gridGeneratingParameters = grid
				.getGridGeneratingParameters();
		initialValues.put(KEY_GAME_SEED, gridGeneratingParameters.mGameSeed);
		initialValues.put(KEY_GENERATOR_REVISION_NUMBER,
				gridGeneratingParameters.mGeneratorRevisionNumber);
		initialValues.put(KEY_HIDE_OPERATORS,
				gridGeneratingParameters.mHideOperators);
		initialValues.put(KEY_MAX_CAGE_RESULT,
				gridGeneratingParameters.mMaxCageResult);
		initialValues.put(KEY_MAX_CAGE_SIZE,
				gridGeneratingParameters.mMaxCageSize);

		try {
			id = (int) mSqliteDatabase
					.insertOrThrow(TABLE, null, initialValues);
		} catch (SQLiteConstraintException e) {
			InvalidParameterException ipe = new InvalidParameterException(
					e.getLocalizedMessage());
			ipe.initCause(e);
			throw ipe;
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
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
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
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
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
	 * 
	 * @return A GridRow object for the first grid record stored in the given
	 *         cursor. Null in case of an error.
	 */
	private GridRow toGridRow(Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst()) {
			// Record can not be processed.
			return null;
		}

		// Convert cursor record to a grid statics object.
		GridRow gridStatistics = new GridRow();
		gridStatistics.mId = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_ROWID));
		gridStatistics.mDefinition = cursor.getString(cursor
				.getColumnIndexOrThrow(KEY_DEFINITION));
		gridStatistics.mGridSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_GRID_SIZE));
		gridStatistics.mDateCreated = valueOfSQLiteTimestamp(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_DATE_CREATED)));

		GridGeneratingParameters gridGeneratingParameters = new GridGeneratingParameters();
		gridGeneratingParameters.mGameSeed = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_GAME_SEED));
		gridGeneratingParameters.mGeneratorRevisionNumber = cursor
				.getInt(cursor
						.getColumnIndexOrThrow(KEY_GENERATOR_REVISION_NUMBER));
		gridGeneratingParameters.mHideOperators = valueOfSQLiteBoolean(cursor
				.getString(cursor.getColumnIndexOrThrow(KEY_HIDE_OPERATORS)));
		gridGeneratingParameters.mMaxCageResult = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_MAX_CAGE_RESULT));
		gridGeneratingParameters.mMaxCageSize = cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_MAX_CAGE_SIZE));

		return gridStatistics;
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
	 * Get a list of all grid id's.
	 * 
	 * @return The list of all grid id's.
	 */
	public int[] getAllGridIds() {
		int[] gridIds = null;
		Cursor cursor = null;
		try {
			cursor = mSqliteDatabase.query(true, TABLE, new String[] {KEY_ROWID}, null, null, null, null, KEY_ROWID, null);
			if (cursor.moveToFirst()) {
				gridIds = new int[cursor.getCount()];
				int i = 0;
				int columnIndex = cursor.getColumnIndexOrThrow(KEY_ROWID);
				do {
					gridIds[i++] = cursor.getInt(columnIndex);
				} while (cursor.moveToNext());
			}
		} catch (SQLiteException e) {
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				e.printStackTrace();
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return gridIds;
	}
}