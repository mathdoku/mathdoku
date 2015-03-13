package net.mathdoku.plus.storage.selector;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.storage.databaseadapter.DatabaseAdapterException;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class selects all StatusFilters which are applicable when grids with a given GridType are
 * selected.
 */
public class AvailableStatusFiltersSelector extends SolvingAttemptSelector {
    @SuppressWarnings("unused")
    private static final String TAG = AvailableStatusFiltersSelector.class.getName();

    // Replace Config.DisabledAlways() on following line with Config.EnabledInDevelopmentModeOnly()
    // to show debug information when running in development mode.
    private static final boolean DEBUG = Config.DisabledAlways();

    private static final String KEY_PROJECTION_STATUS_FILTER = "status_filter";
    private final List<StatusFilter> statusFilterList;

    public AvailableStatusFiltersSelector(GridTypeFilter gridTypeFilter) {
        super(StatusFilter.ALL, gridTypeFilter);
        setEnableLogging(DEBUG);
        setOrderByString(KEY_PROJECTION_STATUS_FILTER);
        setGroupByString(KEY_PROJECTION_STATUS_FILTER);
        statusFilterList = retrieveFromDatabase();
    }

    private List<StatusFilter> retrieveFromDatabase() {
        // Convert results in cursor to array of grid id's
        List<StatusFilter> statusFilters = new ArrayList<StatusFilter>();
        statusFilters.add(StatusFilter.ALL);
        Cursor cursor = null;
        try {
            cursor = getCursor();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    statusFilters.add(
                            StatusFilter.valueOf(getProjectionStatusFilterFromCursor(cursor)));
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            throw new DatabaseAdapterException(String.format(
                    "Cannot retrieve used statuses of latest solving attempt per grid from" + " " +
                            "the database (size filter = %s).",
                    gridTypeFilter.toString()), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return statusFilters;
    }

    private String getProjectionStatusFilterFromCursor(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECTION_STATUS_FILTER));
    }

    protected DatabaseProjection getDatabaseProjection() {
        DatabaseProjection databaseProjection = new DatabaseProjection();
        databaseProjection.put(KEY_PROJECTION_STATUS_FILTER, getStatusFilterDerivationSQL());
        return databaseProjection;
    }

    private String getStatusFilterDerivationSQL() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CASE");
        for (SolvingAttemptStatus solvingAttemptStatus : SolvingAttemptStatus.values()) {
            if (solvingAttemptStatus.getAttachedToStatusFilter() != null) {
                stringBuilder.append(" WHEN ");
                stringBuilder.append(DatabaseUtil.stringBetweenBackTicks(
                        SolvingAttemptDatabaseAdapter.KEY_STATUS));
                stringBuilder.append(" = ");
                stringBuilder.append(solvingAttemptStatus.getId());
                stringBuilder.append(" THEN ");
                stringBuilder.append(DatabaseUtil.stringBetweenQuotes(
                        solvingAttemptStatus.getAttachedToStatusFilter()
                                .name()));
            }
        }
        stringBuilder.append(" END");

        return stringBuilder.toString();
    }

    public List<StatusFilter> getAvailableStatusFilters() {
        return statusFilterList;
    }
}
