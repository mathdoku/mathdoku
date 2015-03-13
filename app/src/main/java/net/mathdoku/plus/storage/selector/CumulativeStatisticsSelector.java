package net.mathdoku.plus.storage.selector;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.statistics.CumulativeStatistics;
import net.mathdoku.plus.storage.databaseadapter.DatabaseAdapterException;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.ConditionList;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldBetweenIntegerValues;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldOperatorBooleanValue;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.FieldOperatorValue;
import net.mathdoku.plus.storage.databaseadapter.queryhelper.JoinHelper;

public class CumulativeStatisticsSelector {
    @SuppressWarnings("unused")
    private static final String TAG = CumulativeStatisticsSelector.class.getName();

    // Replace Config.DisabledAlways() on following line with Config.EnabledInDevelopmentModeOnly()
    // to show debug information when running in development mode.
    private static final boolean DEBUG = Config.DisabledAlways();

    private final int minGridSize;
    private final int maxGridSize;
    private static final DatabaseProjection DATABASE_PROJECTION = buildDatabaseProjection();
    private final CumulativeStatistics cumulativeStatistics;

    public CumulativeStatisticsSelector(int minGridSize, int maxGridSize) {
        this.minGridSize = minGridSize;
        this.maxGridSize = maxGridSize;
        cumulativeStatistics = retrieveFromDatabase();
    }

    private static DatabaseProjection buildDatabaseProjection() {
        DatabaseProjection databaseProjection = new DatabaseProjection();

        // Grid size minimum and maximum
        databaseProjection.put(DatabaseProjection.Aggregation.MIN, GridDatabaseAdapter.TABLE_NAME,
                               GridDatabaseAdapter.KEY_GRID_SIZE);
        databaseProjection.put(DatabaseProjection.Aggregation.MAX, GridDatabaseAdapter.TABLE_NAME,
                               GridDatabaseAdapter.KEY_GRID_SIZE);

        // Totals per status of game
        databaseProjection.put(DatabaseProjection.Aggregation.COUNTIF_TRUE,
                               StatisticsDatabaseAdapter.TABLE_NAME,
                               StatisticsDatabaseAdapter.KEY_ACTION_REVEAL_SOLUTION);
        databaseProjection.put(DatabaseProjection.Aggregation.COUNTIF_TRUE,
                               StatisticsDatabaseAdapter.TABLE_NAME,
                               StatisticsDatabaseAdapter.KEY_SOLVED_MANUALLY);
        databaseProjection.put(DatabaseProjection.Aggregation.COUNTIF_TRUE,
                               StatisticsDatabaseAdapter.TABLE_NAME,
                               StatisticsDatabaseAdapter.KEY_FINISHED);

        // Total games
        databaseProjection.put(DatabaseProjection.Aggregation.COUNT,
                               StatisticsDatabaseAdapter.TABLE_NAME,
                               StatisticsDatabaseAdapter.KEY_ROWID);

        return databaseProjection;
    }

    private CumulativeStatistics retrieveFromDatabase() {
        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setProjectionMap(DATABASE_PROJECTION);
        sqliteQueryBuilder.setTables(new JoinHelper(GridDatabaseAdapter.TABLE_NAME,
                                                    GridDatabaseAdapter.KEY_ROWID).innerJoinWith(
                StatisticsDatabaseAdapter.TABLE_NAME, StatisticsDatabaseAdapter.KEY_GRID_ID)
                                             .toString());

        if (DEBUG) {
            String sql = sqliteQueryBuilder.buildQuery(DATABASE_PROJECTION.getAllColumnNames(),
                                                       getSelectionString(), null, null, null,
                                                       null);
            Log.i(TAG, sql);
        }

        Cursor cursor;
        try {
            cursor = sqliteQueryBuilder.query(DatabaseHelper.getInstance()
                                                      .getReadableDatabase(),
                                              DATABASE_PROJECTION.getAllColumnNames(),
                                              getSelectionString(), null, null, null, null);
        } catch (SQLiteException e) {
            throw new DatabaseAdapterException(String.format(
                    "Cannot retrieve the cumulative statistics for grids with sizes '%d-%d' from " +
                            "database.",
                    minGridSize, maxGridSize), e);
        }

        if (cursor == null || !cursor.moveToFirst()) {
            // Record can not be processed.
            return null;
        }

        return getCumulativeStatisticsFromCursor(cursor);
    }

    private String getSelectionString() {
        ConditionList conditionList = new ConditionList();
        conditionList.addOperand(
                new FieldBetweenIntegerValues(GridDatabaseAdapter.KEY_GRID_SIZE, minGridSize,
                                              maxGridSize));
        conditionList.addOperand(
                new FieldOperatorBooleanValue(StatisticsDatabaseAdapter.KEY_INCLUDE_IN_STATISTICS,
                                              FieldOperatorValue.Operator.EQUALS, true));
        conditionList.setAndOperator();
        return conditionList.toString();
    }

    private CumulativeStatistics getCumulativeStatisticsFromCursor(Cursor cursor) {
        CumulativeStatistics statistics = new CumulativeStatistics();

        // Grid size minimum and maximum
        statistics.mMinGridSize = cursor.getInt(getColumnIndexMinGridSizeFromCursor(cursor));
        statistics.mMaxGridSize = cursor.getInt(getColumnIndexMaxGridSizeFromCursor(cursor));

        // Totals per status of game
        statistics.mCountSolutionRevealed = cursor.getInt(
                getColumnIndexCountSolutionRevealedFromCursor(cursor));
        statistics.mCountSolvedManually = cursor.getInt(
                getColumnIndexCountSolvedManuallyFromCursor(cursor));
        statistics.mCountFinished = cursor.getInt(getColumnIndexCountFinishedFromCursor(cursor));

        // Total games
        statistics.mCountStarted = cursor.getInt(getColumnIndexRowIdFromCursor(cursor));

        cursor.close();
        return statistics;
    }

    private int getColumnIndexMinGridSizeFromCursor(Cursor cursor) {
        return cursor.getColumnIndexOrThrow(
                DatabaseProjection.Aggregation.MIN.getAggregationColumnNameForColumn(
                        GridDatabaseAdapter.KEY_GRID_SIZE));
    }

    private int getColumnIndexMaxGridSizeFromCursor(Cursor cursor) {
        return cursor.getColumnIndexOrThrow(
                DatabaseProjection.Aggregation.MAX.getAggregationColumnNameForColumn(
                        GridDatabaseAdapter.KEY_GRID_SIZE));
    }

    private int getColumnIndexCountSolutionRevealedFromCursor(Cursor cursor) {
        return cursor.getColumnIndexOrThrow(
                DatabaseProjection.Aggregation.COUNTIF_TRUE.getAggregationColumnNameForColumn(
                        StatisticsDatabaseAdapter.KEY_ACTION_REVEAL_SOLUTION));
    }

    private int getColumnIndexCountSolvedManuallyFromCursor(Cursor cursor) {
        return cursor.getColumnIndexOrThrow(
                DatabaseProjection.Aggregation.COUNTIF_TRUE.getAggregationColumnNameForColumn(
                        StatisticsDatabaseAdapter.KEY_SOLVED_MANUALLY));
    }

    private int getColumnIndexCountFinishedFromCursor(Cursor cursor) {
        return cursor.getColumnIndexOrThrow(
                DatabaseProjection.Aggregation.COUNTIF_TRUE.getAggregationColumnNameForColumn(
                        StatisticsDatabaseAdapter.KEY_FINISHED));
    }

    private int getColumnIndexRowIdFromCursor(Cursor cursor) {
        return cursor.getColumnIndexOrThrow(
                DatabaseProjection.Aggregation.COUNT.getAggregationColumnNameForColumn(
                        StatisticsDatabaseAdapter.KEY_ROWID));
    }

    public CumulativeStatistics getCumulativeStatistics() {
        return cumulativeStatistics;
    }
}
