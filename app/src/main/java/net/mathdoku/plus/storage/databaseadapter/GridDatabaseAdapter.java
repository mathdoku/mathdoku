package net.mathdoku.plus.storage.databaseadapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridgenerating.GridGeneratingParametersBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.storage.databaseadapter.database.DataType;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseColumnDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseTableDefinition;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldOperatorIntegerValue;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldOperatorStringValue;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldOperatorValue;
import net.mathdoku.plus.util.ParameterValidator;

/**
 * The database adapter for the grid table.
 */
public class GridDatabaseAdapter extends DatabaseAdapter {
    @SuppressWarnings("unused")
    private static final String TAG = GridDatabaseAdapter.class.getName();

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

    public GridDatabaseAdapter() {
        super();
    }

    // Package private access, intended for DatabaseHelper only
    GridDatabaseAdapter(SQLiteDatabase sqLiteDatabase) {
        super(sqLiteDatabase);
    }

    private static DatabaseTableDefinition defineTable() {
        DatabaseTableDefinition databaseTableDefinition = new DatabaseTableDefinition(TABLE_NAME);
        databaseTableDefinition.addColumn(new DatabaseColumnDefinition(KEY_ROWID, DataType.INTEGER).setPrimaryKey());
        databaseTableDefinition.addColumn(new DatabaseColumnDefinition(KEY_DEFINITION, DataType.STRING).setNotNull()
                                                  .setUniqueKey());
        databaseTableDefinition.addColumn(new DatabaseColumnDefinition(KEY_GRID_SIZE, DataType.INTEGER).setNotNull());
        databaseTableDefinition.addColumn(
                new DatabaseColumnDefinition(KEY_DATE_CREATED, DataType.TIMESTAMP).setNotNull());
        // Grid generating parameters. These values can be null as they are not
        // known for historic games. Neither will they be know when games will
        // be exchanged in the future.
        databaseTableDefinition.addColumn(new DatabaseColumnDefinition(KEY_GAME_SEED, DataType.LONG));
        // changes in tables
        databaseTableDefinition.addColumn(
                new DatabaseColumnDefinition(KEY_GENERATOR_REVISION_NUMBER, DataType.INTEGER));
        databaseTableDefinition.addColumn(new DatabaseColumnDefinition(KEY_PUZZLE_COMPLEXITY, DataType.STRING));
        databaseTableDefinition.addColumn(new DatabaseColumnDefinition(KEY_HIDE_OPERATORS, DataType.STRING));
        databaseTableDefinition.addColumn(new DatabaseColumnDefinition(KEY_MAX_CAGE_RESULT, DataType.INTEGER));
        databaseTableDefinition.addColumn(new DatabaseColumnDefinition(KEY_MAX_CAGE_SIZE, DataType.INTEGER));

        databaseTableDefinition.build();

        return databaseTableDefinition;
    }

    @Override
    public DatabaseTableDefinition getDatabaseTableDefinition() {
        return DATABASE_TABLE;
    }

    /**
     * Upgrades the table to an other version.
     *
     * @param oldVersion
     *         The old version of the database. Use the app revision number to identify the database version.
     * @param newVersion
     *         The new version of the database. Use the app revision number to identify the database version.
     */
    protected void upgradeTable(int oldVersion, int newVersion) {
        if (Config.APP_MODE == AppMode.DEVELOPMENT && oldVersion < 432 && newVersion >= 432) {
            recreateTableInDevelopmentMode();
        }
    }

    /**
     * Inserts a new grid into the database. The grid definition should be unique. The record should be created as soon
     * as the grid is created.
     *
     * @param grid
     *         The grid which has to be inserted into the database.
     * @return The unique row id of the grid created. -1 in case of an error.
     */
    public int insert(Grid grid) {
        ParameterValidator.validateNotNull(grid);

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DEFINITION, grid.getDefinition());
        initialValues.put(KEY_GRID_SIZE, grid.getGridSize());
        initialValues.put(KEY_DATE_CREATED, DatabaseUtil.toSQLiteTimestamp(grid.getDateCreated()));
        GridGeneratingParameters gridGeneratingParameters = grid.getGridGeneratingParameters();
        initialValues.put(KEY_GAME_SEED, gridGeneratingParameters.getGameSeed());
        initialValues.put(KEY_GENERATOR_REVISION_NUMBER, gridGeneratingParameters.getGeneratorVersionNumber());
        initialValues.put(KEY_PUZZLE_COMPLEXITY, gridGeneratingParameters.getPuzzleComplexity()
                .toString());
        initialValues.put(KEY_HIDE_OPERATORS, DatabaseUtil.toSQLiteBoolean(gridGeneratingParameters.isHideOperators()));
        initialValues.put(KEY_MAX_CAGE_RESULT, gridGeneratingParameters.getMaxCageResult());
        initialValues.put(KEY_MAX_CAGE_SIZE, gridGeneratingParameters.getMaxCageSize());

        int id;
        try {
            id = (int) sqliteDatabase.insertOrThrow(TABLE_NAME, null, initialValues);
        } catch (SQLiteConstraintException e) {
            throw new DatabaseAdapterException("Cannot insert new grid in database.", e);
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
     *         The unique row id of the grid to be found.
     * @return The grid with the given id.
     */
    public GridRow get(int id) {
        GridRow gridRow = null;
        Cursor cursor = null;
        try {
            cursor = sqliteDatabase.query(true, TABLE_NAME, DATABASE_TABLE.getColumnNames(),
                                          getRowIdSelectionString(id), null, null, null, null, null);
            gridRow = toGridRow(cursor);
        } catch (SQLiteException e) {
            throw new DatabaseAdapterException(String.format("Cannot retrieve gridRow with id '%d' from database", id),
                                               e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return gridRow;
    }

    private String getRowIdSelectionString(int id) {
        return new FieldOperatorIntegerValue(KEY_ROWID, FieldOperatorValue.Operator.EQUALS, id).toString();
    }

    /**
     * Get a grid by searching for its definition.
     *
     * @param definition
     *         The unique grid definition of the grid to be found.
     * @return The grid with the given definition. Null in case of an error.
     */
    public GridRow getByGridDefinition(String definition) {
        ParameterValidator.validateNotNullOrEmpty(definition);

        GridRow gridRow = null;
        Cursor cursor = null;
        try {
            cursor = sqliteDatabase.query(true, TABLE_NAME, DATABASE_TABLE.getColumnNames(),
                                          getDefinitionSelectionString(definition), null, null, null, null, null);
            gridRow = toGridRow(cursor);
        } catch (SQLiteException e) {
            throw new DatabaseAdapterException(
                    String.format("Cannot retrieve grid with definition '%s' from database", definition), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return gridRow;
    }

    private String getDefinitionSelectionString(String definition) {
        return new FieldOperatorStringValue(KEY_DEFINITION, FieldOperatorValue.Operator.EQUALS, definition).toString();
    }

    /**
     * Convert first record in the given cursor to a GridRow object.
     *
     * @param cursor
     *         The cursor to be converted.
     * @return A GridRow object for the first grid record stored in the given cursor. Null in case of an error.
     */
    private GridRow toGridRow(Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            // Record can not be processed.
            return null;
        }

        return new GridRow(getGridIdFromCursor(cursor), getGridDefinitionFromCursor(cursor),
                           getGridSizeFromCursor(cursor), getGridDateCreatedFromCursor(cursor),
                           new GridGeneratingParametersBuilder().setGridType(getGridTypeFromCursor(cursor))
                                   .setHideOperators(getHideOperatorFromCursor(cursor))
                                   .setPuzzleComplexity(getPuzzleComplexityFromCursor(cursor))
                                   .setGeneratorVersionNumber(getGeneratorRevisionNumberFromCursor(cursor))
                                   .setGameSeed(getGameSeedFromCursor(cursor))
                                   .setMaxCageResult(getMaxCageResultFromCursor(cursor))
                                   .setMaxCageSize(getMaxCageSizeFromCursor(cursor))
                                   .createGridGeneratingParameters());
    }

    private int getGridIdFromCursor(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ROWID));
    }

    private String getGridDefinitionFromCursor(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEFINITION));
    }

    private int getGridSizeFromCursor(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GRID_SIZE));
    }

    private long getGridDateCreatedFromCursor(Cursor cursor) {
        return DatabaseUtil.valueOfSQLiteTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_CREATED)));
    }

    private GridType getGridTypeFromCursor(Cursor cursor) {
        return GridType.fromInteger(getGridSizeFromCursor(cursor));
    }

    private boolean getHideOperatorFromCursor(Cursor cursor) {
        return DatabaseUtil.valueOfSQLiteBoolean(cursor.getString(cursor.getColumnIndexOrThrow(KEY_HIDE_OPERATORS)));
    }

    private PuzzleComplexity getPuzzleComplexityFromCursor(Cursor cursor) {
        return PuzzleComplexity.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PUZZLE_COMPLEXITY)));
    }

    private int getGeneratorRevisionNumberFromCursor(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GENERATOR_REVISION_NUMBER));
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
     *         The column name which has to be prefixed.
     * @return The prefixed column name.
     */
    public static String getPrefixedColumnName(String column) {
        ParameterValidator.validateNotNullOrEmpty(column);
        return DatabaseUtil.stringBetweenBackTicks(TABLE_NAME) + "." + DatabaseUtil.stringBetweenBackTicks(column);
    }
}
