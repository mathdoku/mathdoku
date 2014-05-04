package net.mathdoku.plus.storage.selector;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.storage.databaseadapter.DatabaseAdapterException;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class selects all solving attempts which have to be displayed in the
 * Archive.
 */
public class ArchiveSolvingAttemptSelector {
	@SuppressWarnings("unused")
	private static final String TAG = ArchiveSolvingAttemptSelector.class.getName();

	// Remove "&& false" in following line to show the SQL-statements in the
	// debug information
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SQL = Config.mAppMode == Config.AppMode.DEVELOPMENT && false;

	public static final String SQLITE_KEYWORD_AND = " AND ";

	private final GridDatabaseAdapter.StatusFilter statusFilter;
	private final GridTypeFilter gridTypeFilter;
	private final List<LatestSolvingAttemptForGrid> latestSolvingAttemptForGridList;

	private static final String KEY_PROJECTION_GRID_ID = "projection_grid_id";
	private static final String KEY_PROJECTION_SOLVING_ATTEMPT_ID = "projection_solving_attempt_id";
	private static final String KEY_PROJECTION_STATUS = "projection_status";
	private static final String KEY_PROJECTION_SIZE = "projection_size";

	public static class LatestSolvingAttemptForGrid {
		private final int gridId;
		private final int solvingAttemptId;
		private final int statusFilter;
		private final int size;

		public LatestSolvingAttemptForGrid(int gridId, int solvingAttemptId,
				int statusFilter, int size) {
			this.gridId = gridId;
			this.solvingAttemptId = solvingAttemptId;
			this.statusFilter = statusFilter;
			this.size = size;
		}

		public int getGridId() {
			return gridId;
		}

		public int getSolvingAttemptId() {
			return solvingAttemptId;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder(
					"LatestSolvingAttemptForGrid{");
			sb.append("gridId=").append(gridId);
			sb.append(", solvingAttemptId=").append(solvingAttemptId);
			sb.append(", statusFilter=").append(statusFilter);
			sb.append(", size=").append(size);
			sb.append('}');
			return sb.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof LatestSolvingAttemptForGrid)) {
				return false;
			}

			LatestSolvingAttemptForGrid that = (LatestSolvingAttemptForGrid) o;

			if (gridId != that.gridId) {
				return false;
			}
			if (solvingAttemptId != that.solvingAttemptId) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = gridId;
			result = 31 * result + solvingAttemptId;
			return result;
		}
	}

	public ArchiveSolvingAttemptSelector(
			GridDatabaseAdapter.StatusFilter statusFilter,
			GridTypeFilter gridTypeFilter) {
		this.statusFilter = statusFilter;
		this.gridTypeFilter = gridTypeFilter;
		latestSolvingAttemptForGridList = retrieveFromDatabase();
	}

	private List<LatestSolvingAttemptForGrid> retrieveFromDatabase() {
		List<LatestSolvingAttemptForGrid> latestSolvingAttemptForGrids = new ArrayList<LatestSolvingAttemptForGrid>();
		Cursor cursor = null;
		try {
			cursor = getCursor();
			if (cursor != null && cursor.moveToFirst()) {
				do {
					latestSolvingAttemptForGrids
							.add(new LatestSolvingAttemptForGrid(
									getGridIdFromCursor(cursor),
									getSolvingAttemptIdFromCursor(cursor),
									getSolvingAttemptStatusFromCursor(cursor),
									getGridSizeFromCursor(cursor)));

				} while (cursor.moveToNext());
			}
		} catch (SQLiteException e) {
			throw new DatabaseAdapterException(String.format(
					"Cannot retrieve latest solving attempt per grid from the database "
							+ "(status filter = %s, size filter = %s).",
					statusFilter.toString(), gridTypeFilter.toString()), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return latestSolvingAttemptForGrids;
	}

	private Cursor getCursor() {
		DatabaseProjection databaseProjection = getDatabaseProjection();

		SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
		sqliteQueryBuilder.setProjectionMap(databaseProjection);
		sqliteQueryBuilder.setTables(getJoinString());

		if (DEBUG_SQL) {
			String sql = sqliteQueryBuilder.buildQuery(
					databaseProjection.getAllColumnNames(),
					getSelectionString(), KEY_PROJECTION_GRID_ID, null, null,
					null);
			Log.i(TAG, sql);
		}

		return sqliteQueryBuilder.query(DatabaseHelper
				.getInstance()
				.getWritableDatabase(), databaseProjection.getAllColumnNames(),
				getSelectionString(), null, KEY_PROJECTION_GRID_ID, null, null);
	}

	private DatabaseProjection getDatabaseProjection() {
		DatabaseProjection databaseProjection = new DatabaseProjection();
		databaseProjection.put(KEY_PROJECTION_GRID_ID,
				GridDatabaseAdapter.TABLE_NAME, GridDatabaseAdapter.KEY_ROWID);
		databaseProjection.put(KEY_PROJECTION_SOLVING_ATTEMPT_ID,
				SolvingAttemptDatabaseAdapter.TABLE_NAME,
				SolvingAttemptDatabaseAdapter.KEY_ROWID);
		databaseProjection.put(KEY_PROJECTION_STATUS,
				SolvingAttemptDatabaseAdapter.TABLE_NAME,
				SolvingAttemptDatabaseAdapter.KEY_STATUS);
		databaseProjection.put(KEY_PROJECTION_SIZE,
				GridDatabaseAdapter.TABLE_NAME,
				GridDatabaseAdapter.KEY_GRID_SIZE);
		return databaseProjection;
	}

	private String getJoinString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(DatabaseUtil
				.stringBetweenBackTicks(GridDatabaseAdapter.TABLE_NAME));
		stringBuilder.append(" INNER JOIN ");
		stringBuilder.append(SolvingAttemptDatabaseAdapter.TABLE_NAME);
		stringBuilder.append(" ON ");
		stringBuilder
				.append(SolvingAttemptDatabaseAdapter
						.getPrefixedColumnName(SolvingAttemptDatabaseAdapter.KEY_GRID_ID));
		stringBuilder.append(" = ");
		stringBuilder.append(GridDatabaseAdapter
				.getPrefixedColumnName(GridDatabaseAdapter.KEY_ROWID));

		return stringBuilder.toString();
	}

	private String getSelectionString() {
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
		stringBuilder.append(" = ");
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
		String selectionStatus = SolvingAttemptDatabaseAdapter
				.getStatusSelectionString(statusFilter);
		if (!selectionStatus.isEmpty()) {
			stringBuilder.append(SQLITE_KEYWORD_AND);
			stringBuilder.append(selectionStatus);
		}
		return stringBuilder.toString();
	}

	private String getSizeSelectionString() {
		StringBuilder stringBuilder = new StringBuilder();
		String selectionSize = GridDatabaseAdapter
				.getSizeSelectionString(gridTypeFilter);
		if (!selectionSize.isEmpty()) {
			stringBuilder.append(SQLITE_KEYWORD_AND);
			stringBuilder.append(selectionSize);
		}

		return stringBuilder.toString();
	}

	private int getGridIdFromCursor(Cursor cursor) {
		return cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_PROJECTION_GRID_ID));
	}

	private int getSolvingAttemptIdFromCursor(Cursor cursor) {
		return cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_PROJECTION_SOLVING_ATTEMPT_ID));
	}

	private int getSolvingAttemptStatusFromCursor(Cursor cursor) {
		return cursor.getInt(cursor
				.getColumnIndexOrThrow(KEY_PROJECTION_STATUS));
	}

	private int getGridSizeFromCursor(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROJECTION_SIZE));
	}

	/**
	 * Get the complete list of solving attempts. In case multiple solving
	 * attempts exists for a single grid, only the most recent solving attempt
	 * of such a grid is included in the list.
	 * 
	 * @return The list of the latest solving attempt per grid.
	 */
	public List<LatestSolvingAttemptForGrid> getLatestSolvingAttemptIdPerGrid() {
		return latestSolvingAttemptForGridList;
	}

	/**
	 * Counts the number of solving attempts which will be retrieved by this
	 * selector. *
	 * 
	 * @return The number of solving attempts which will be retrieved by this
	 *         selector.
	 */
	@SuppressWarnings("SameParameterValue")
	public int countGrids() {
		return latestSolvingAttemptForGridList.size();
	}
}
