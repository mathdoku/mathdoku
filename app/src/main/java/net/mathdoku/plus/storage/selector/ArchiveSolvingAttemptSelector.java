package net.mathdoku.plus.storage.selector;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.storage.databaseadapter.DatabaseAdapterException;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.database.DatabaseProjection;

import java.util.ArrayList;
import java.util.List;

/**
 * This class gathers data about grids and solving attempts with a given status
 * and/or size.
 */
public class ArchiveSolvingAttemptSelector extends SolvingAttemptSelector {
	@SuppressWarnings("unused")
	private static final String TAG = ArchiveSolvingAttemptSelector.class
			.getName();

	// Remove "&& false" in following line to show the SQL-statements in the
	// debug information
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SQL = Config.mAppMode == Config.AppMode.DEVELOPMENT && false;

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

	public ArchiveSolvingAttemptSelector(StatusFilter statusFilter,
			GridTypeFilter gridTypeFilter) {
		super(statusFilter, gridTypeFilter);
		setEnableLogging(DEBUG_SQL);
		setOrderByString(KEY_PROJECTION_GRID_ID);
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

	protected DatabaseProjection getDatabaseProjection() {
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
