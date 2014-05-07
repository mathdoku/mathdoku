package net.mathdoku.plus.storage.selector;

import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;

/**
 * This class builds a cursor for selecting the latest solving attempt per grid.
 * If applicable the grids are filter based on a given GridType. If applicable
 * the solving attempts are filtered on a given Status.
 */

public abstract class SolvingAttemptSelector {
	@SuppressWarnings("unused")
	private static final String TAG = SolvingAttemptSelector.class.getName();

	private static final String SQLITE_KEYWORD_AND = " AND ";
	private static final String SQLITE_KEYWORD_EQUALS = " = ";

	protected final StatusFilter statusFilter;
	protected final GridTypeFilter gridTypeFilter;
	private boolean enableLogging;
	private String groupByString;
	private String orderByString;

	public SolvingAttemptSelector(StatusFilter statusFilter,
			GridTypeFilter gridTypeFilter) {
		this.statusFilter = statusFilter;
		this.gridTypeFilter = gridTypeFilter;
	}

	public void setEnableLogging(boolean enableLogging) {
		this.enableLogging = enableLogging;
	}

	public void setGroupByString(String groupByString) {
		this.groupByString = groupByString;
	}

	public void setOrderByString(String orderByString) {
		this.orderByString = orderByString;
	}

	protected Cursor getCursor() {
		DatabaseProjection databaseProjection = getDatabaseProjection();
		SQLiteQueryBuilder sqliteQueryBuilder = getSqLiteQueryBuilder(databaseProjection);

		if (enableLogging) {
			String sql = sqliteQueryBuilder.buildQuery(
					databaseProjection.getAllColumnNames(),
					getSelectionString(), groupByString, null, orderByString,
					null);
			Log.i(TAG, sql);
		}

		return sqliteQueryBuilder.query(DatabaseHelper
				.getInstance()
				.getReadableDatabase(), databaseProjection.getAllColumnNames(),
				getSelectionString(), null, groupByString, null, orderByString);
	}

	/**
	 * Gets the database projection which is used to feed the cursor with the
	 * columns to be retrieved.
	 * 
	 * @return The database projection which is used to feed the cursor with the
	 *         columns to be retrieved.
	 */
	protected abstract DatabaseProjection getDatabaseProjection();

	private SQLiteQueryBuilder getSqLiteQueryBuilder(
			DatabaseProjection databaseProjection) {
		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(databaseProjection);
		sqliteQueryBuilder.setTables(getJoinString());
		return sqliteQueryBuilder;
	}

	protected String getJoinString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(DatabaseUtil
				.stringBetweenBackTicks(GridDatabaseAdapter.TABLE_NAME));
		stringBuilder.append(" INNER JOIN ");
		stringBuilder.append(SolvingAttemptDatabaseAdapter.TABLE_NAME);
		stringBuilder.append(" ON ");
		stringBuilder
				.append(SolvingAttemptDatabaseAdapter
						.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_GRID_ID));
		stringBuilder.append(SQLITE_KEYWORD_EQUALS);
		stringBuilder.append(GridDatabaseAdapter
				.getPrefixedColumnName(GridDatabaseAdapter.KEY_ROWID));

		return stringBuilder.toString();
	}

	protected String getSelectionString() {
		// Note: it is not possible to use an aggregation function like
		// MAX(solving_attempt_id) as the selection on status should only be
		// based on the status of the last solving attempt. Using an aggregate
		// function results in wrong data when a status filter is applied as the
		// aggregation would only be based on the solving attempt which do match
		// the status while ignore the fact that a more recent solving attempt
		// exists with another status.
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("not exists (select 1 from ");
		stringBuilder.append(SolvingAttemptDatabaseAdapter.TABLE_NAME);
		stringBuilder.append(" as sa2 where sa2.");
		stringBuilder.append(SolvingAttemptDatabaseAdapter.KEY_GRID_ID);
		stringBuilder.append(SQLITE_KEYWORD_EQUALS);
		stringBuilder
				.append(SolvingAttemptDatabaseAdapter
						.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_GRID_ID));
		stringBuilder.append(" and sa2.");
		stringBuilder.append(SolvingAttemptDatabaseAdapter.KEY_ROWID);
		stringBuilder.append(" > ");
		stringBuilder
				.append(SolvingAttemptDatabaseAdapter
						.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_ROWID));
		stringBuilder.append(") ");
		stringBuilder.append(getStatusSelectionString());
		stringBuilder.append(getSizeSelectionString());

		return stringBuilder.toString();
	}

	private String getStatusSelectionString() {
		StringBuilder stringBuilder = new StringBuilder();
		if (statusFilter != StatusFilter.ALL) {
			stringBuilder.append(SQLITE_KEYWORD_AND);
			stringBuilder.append(" (");
			int countSolvingAttemptStatuses = 0;
			for (SolvingAttemptStatus solvingAttemptStatus : statusFilter
					.getAllAttachedSolvingAttemptStatuses()) {
				if (countSolvingAttemptStatuses > 0) {
					stringBuilder.append(" OR ");
				}
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter
								.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_STATUS));
				stringBuilder.append(SQLITE_KEYWORD_EQUALS);
				stringBuilder.append(solvingAttemptStatus.getId());
				countSolvingAttemptStatuses++;
			}
			stringBuilder.append(") ");
		}
		return stringBuilder.toString();
	}

	private String getSizeSelectionString() {
		StringBuilder stringBuilder = new StringBuilder();
		if (gridTypeFilter != GridTypeFilter.ALL) {
			stringBuilder.append(SQLITE_KEYWORD_AND);
			stringBuilder.append(GridDatabaseAdapter
					.getPrefixedColumnName(GridDatabaseAdapter.KEY_GRID_SIZE));
			stringBuilder.append(SQLITE_KEYWORD_EQUALS);
			stringBuilder.append(GridType
					.fromGridTypeFilter(gridTypeFilter)
					.getGridSize());
		}

		return stringBuilder.toString();
	}
}
