package net.mathdoku.plus.grid;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.database.SolvingAttemptData;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

public class GridLoader {
	private static final String TAG = "MathDoku.GridLoader";

	private final Grid mGrid;

	private int mSavedWithRevision;

	// The Objects Creator is responsible for creating all new objects needed by
	// this class. For unit testing purposes the default create methods can be
	// overridden if needed.
	public static class ObjectsCreator {
		private Grid.ObjectsCreator mGridObjectsCreator;

		public GridCell createGridCell(Grid grid, int cell) {
			return mGridObjectsCreator.createGridCell(grid, cell);
		}

		public CellChange createCellChange() {
			return mGridObjectsCreator.createCellChange();
		}

		public GridCage createGridCage(Grid grid) {
			return mGridObjectsCreator.createGridCage(grid);
		}

		public StatisticsDatabaseAdapter createStatisticsDatabaseAdapter() {
			return new StatisticsDatabaseAdapter();
		}
	}

	private final ObjectsCreator mObjectsCreator;

	/**
	 * Creates new instance of {@link net.mathdoku.plus.grid.GridLoader}.
	 * 
	 * @param grid
	 *            The grid which has to be loaded by the grid loader.
	 */
	public GridLoader(Grid grid) {
		mGrid = grid;

		mObjectsCreator = new ObjectsCreator();
		mObjectsCreator.mGridObjectsCreator = mGrid.getObjectsCreator();
	}

	/**
	 * Creates new instance of {@link net.mathdoku.plus.grid.GridLoader}. All
	 * objects in this class will be created with the given ObjectsCreator. This
	 * method is intended for unit testing.
	 * 
	 * @param grid
	 *            The grid which has to be loaded by the grid loader.
	 * @param objectsCreator
	 *            The ObjectsCreator to be used by this class. Only create
	 *            methods for which the default implementation does not suffice,
	 *            should be overridden.
	 */
	public GridLoader(Grid grid, ObjectsCreator objectsCreator) {
		mGrid = grid;
		mObjectsCreator = (objectsCreator != null ? objectsCreator
				: new ObjectsCreator());
	}

	/**
	 * Load a mGrid from the given solving attempt.
	 * 
	 * @return True in case the mGrid has been loaded successfully. False
	 *         otherwise.
	 */
	public boolean load(SolvingAttemptData solvingAttemptData) {
		if (solvingAttemptData == null) {
			// Could not retrieve this solving attempt.
			return false;
		}
		if (mGrid.getGridSize() <= 0) {
			// Cannot load solving attempt for grids with an invalid grid size.
			return false;
		}

		mSavedWithRevision = solvingAttemptData.mSavedWithRevision;
		String line;
		try {
			// Read first line
			if ((line = solvingAttemptData.getFirstLine()) == null) {
				throw new InvalidGridException(
						"Unexpected end of solving attempt at first line");
			}

			if (!mGrid.fromStorageString(line, mSavedWithRevision)) {
				throw new InvalidGridException(
						"Line does not contain general grid information while this was expected:"
								+ line);
			}

			if ((line = solvingAttemptData.getNextLine()) == null) {
				throw new InvalidGridException(
						"Unexpected end of solving attempt after processing view information.");
			}

			// Read cells
			while (loadCell(line)) {
				line = solvingAttemptData.getNextLine();
			}
			// Check if expected number of cells is read.
			if (mGrid.mCells.size() != mGrid.getGridSize()
					* mGrid.getGridSize()) {
				throw new InvalidGridException(
						"Unexpected number of cells loaded. Expected: "
								+ (mGrid.getGridSize() * mGrid.getGridSize())
								+ ", actual: " + mGrid.mCells.size());
			}

			// Read cages
			while (loadCage(line)) {
				line = solvingAttemptData.getNextLine();
			}
			// At least one expected is expected, so throw error in case no
			// cages have been loaded.
			if (mGrid.mCages.size() == 0) {
				throw new InvalidGridException(
						"Line does not contain cage information while this was expected:"
								+ line);
			}

			// Remaining lines contain cell changes (zero or more expected)
			while (loadCellChange(line)) {
				line = solvingAttemptData.getNextLine();
			}

			// Check if end of file is reached an not all information was read
			// yet.
			if (line != null) {
				throw new InvalidGridException(
						"Unexpected line found while end of file was expected: "
								+ line);
			}
		} catch (InvalidGridException e) {
			if (Config.mAppMode == Config.AppMode.DEVELOPMENT) {
				throw new InvalidGridException(
						"Invalid format error when restoring solving attempt with id '"
								+ solvingAttemptData.mId + "'\n"
								+ e.getMessage());
			}
			return false;
		} catch (NumberFormatException e) {
			if (Config.mAppMode == Config.AppMode.DEVELOPMENT) {
				throw new InvalidGridException(
						"Invalid Number format error when restoring solving attempt with id '"
								+ solvingAttemptData.mId + "'\n"
								+ e.getMessage());
			}
			return false;
		} catch (IndexOutOfBoundsException e) {
			if (Config.mAppMode == Config.AppMode.DEVELOPMENT) {
				throw new InvalidGridException(
						"Index out of bound error when restoring solving attempt with id '"
								+ solvingAttemptData.mId + "'\n"
								+ e.getMessage());
			}
			return false;
		}

		// All data was read from the solving attempt into the grid. Complete
		// loading of grid by.
		mGrid.setDateCreated(solvingAttemptData.mDateCreated);
		mGrid.setDateLastSaved(solvingAttemptData.mDateUpdated);
		mGrid.checkUserMathForAllCages();
		for (GridCell gridCell : mGrid.mCells) {
			if (gridCell.isSelected()) {
				// The first cell which is marked as selected, is set as
				// selected cell for the grid.
				mGrid.setSelectedCell(gridCell);
				break;
			}
		}
		for (GridCell gridCell : mGrid.mCells) {
			gridCell.markDuplicateValuesInSameRowAndColumn();
		}
		mGrid.setSolvingAttemptId(solvingAttemptData.mId);
		mGrid.setRowId(solvingAttemptData.mGridId);

		GridStatistics gridStatistics = loadStatistics(solvingAttemptData.mGridId);
		mGrid.setGridStatistics(gridStatistics);

		return (gridStatistics != null);
	}

	private boolean loadCell(String line) {
		if (line == null) {
			return false;
		}

		GridCell cell = mObjectsCreator.createGridCell(mGrid, 0);
		if (!cell.fromStorageString(line, mSavedWithRevision)) {
			return false;
		}
		mGrid.mCells.add(cell);

		return true;
	}

	private boolean loadCage(String line) {
		if (line == null) {
			return false;
		}

		GridCage cage = mObjectsCreator.createGridCage(mGrid);
		if (!cage.fromStorageString(line, mSavedWithRevision)) {
			return false;
		}
		mGrid.mCages.add(cage);

		return true;
	}

	private boolean loadCellChange(String line) {
		if (line == null) {
			return false;
		}

		CellChange cellChange = mObjectsCreator.createCellChange();
		if (!cellChange.fromStorageString(line, mGrid.mCells,
				mSavedWithRevision)) {
			return false;
		}
		mGrid.addMove(cellChange);

		return true;
	}

	/**
	 * Load the current statistics for this grid.
	 */
	private GridStatistics loadStatistics(int gridId) {
		// Load most recent statistics for this grid
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = mObjectsCreator
				.createStatisticsDatabaseAdapter();
		GridStatistics gridStatistics = statisticsDatabaseAdapter
				.getMostRecent(gridId);
		if (gridStatistics == null) {
			// No statistics available. Create a new statistics records.
			gridStatistics = statisticsDatabaseAdapter.insert(mGrid);
		}

		return gridStatistics;
	}
}
