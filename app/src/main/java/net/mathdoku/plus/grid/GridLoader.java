package net.mathdoku.plus.grid;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.database.SolvingAttempt;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import java.security.InvalidParameterException;

public class GridLoader {
	private static final String TAG = "MathDoku.GridLoader";

	private final Grid mGrid;

	private int mSavedWithRevision;
	private GridStorage mGridStorage;
	private GridCageStorage mGridCageStorage;

	// By default this module throws exceptions on error when running in development mode only.
	private boolean mThrowExceptionOnError;

	private GridLoaderObjectsCreator mObjectsCreator;

	/**
	 * Creates new instance of {@link net.mathdoku.plus.grid.GridLoader}.
	 *
	 * @param grid
	 * 		The grid which has to be loaded by the grid loader.
	 */
	public GridLoader(Grid grid) {
		mGrid = grid;

		mObjectsCreator = new GridLoaderObjectsCreator();

		setThrowExceptionOnError(Config.mAppMode == Config.AppMode.DEVELOPMENT);
	}

	public void setObjectsCreator(GridLoaderObjectsCreator gridLoaderObjectsCreator) {
		if (gridLoaderObjectsCreator == null) {
			throw new InvalidParameterException("Parameter GridLoadObjectsCreator can not be null.");
		}
		mObjectsCreator = gridLoaderObjectsCreator;
	}

	/**
	 * Load a mGrid from the given solving attempt.
	 *
	 * @return True in case the mGrid has been loaded successfully. False otherwise.
	 */
	public boolean load(SolvingAttempt solvingAttempt) {
		if (solvingAttempt == null) {
			// Could not retrieve this solving attempt.
			return false;
		}
		if (solvingAttempt.mData == null) {
			// Could not retrieve this solving attempt.
			return false;
		}
		if (mGrid.getGridSize() <= 0) {
			// Cannot load solving attempt for grids with an invalid grid size.
			return false;
		}

		mSavedWithRevision = solvingAttempt.mSavedWithRevision;
		String line;
		try {
			// Read first line
			if ((line = solvingAttempt.mData.getFirstLine()) == null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt at first line");
				}
				return false;
			}

			mGridStorage = mObjectsCreator.createGridStorage();
			if (mGridStorage.fromStorageString(line, mSavedWithRevision) == false) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Line does not contain general grid information while this was " +
									"expected:" + line);
				}
				return false;
			}
			mGrid.setActive(mGridStorage.isActive());
			mGrid.setRevealed(mGridStorage.isRevealed());

			if ((line = solvingAttempt.mData.getNextLine()) == null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt after processing view information"
									+ ".");
				}
				return false;
			}

			// Read cells
			while (loadCell(line)) {
				line = solvingAttempt.mData.getNextLine();
			}
			// Check if expected number of cells is read.
			if (mGrid.mCells.size() != mGrid.getGridSize() * mGrid.getGridSize()) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected number of cells loaded. Expected: " + (mGrid.getGridSize()
									* mGrid.getGridSize()) + ", actual: " + mGrid.mCells.size());
				}
				return false;
			}

			// Read cages
			while (loadCage(line)) {
				line = solvingAttempt.mData.getNextLine();
			}
			// At least one expected is expected, so throw error in case no
			// cages have been loaded.
			if (mGrid.mCages.size() == 0) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Line does not contain cage information while this was expected:" +
									line);
				}
				return false;
			}

			// Remaining lines contain cell changes (zero or more expected)
			while (loadCellChange(line)) {
				line = solvingAttempt.mData.getNextLine();
			}

			// Check if end of file is reached an not all information was read
			// yet.
			if (line != null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected line found while end of file was expected: " + line);
				}
				return false;
			}
		} catch (InvalidGridException e) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Invalid format error when restoring solving attempt with id '" +
								solvingAttempt.mId + "'\n" + e.getMessage());
			}
			return false;
		} catch (NumberFormatException e) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Invalid Number format error when restoring solving attempt with id '" +
								solvingAttempt.mId + "'\n" + e.getMessage());
			}
			return false;
		} catch (IndexOutOfBoundsException e) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Index out of bound error when restoring solving attempt with id '" +
								solvingAttempt.mId + "'\n" + e.getMessage());
			}
			return false;
		}

		// All data was read from the solving attempt into the grid. Complete
		// loading of grid by.
		mGrid.setDateCreated(solvingAttempt.mDateCreated);
		mGrid.setDateLastSaved(solvingAttempt.mDateUpdated);

		for (GridCage gridCage : mGrid.mCages) {
			gridCage.setGridReference(mGrid);
		}
		for (GridCell gridCell : mGrid.mCells) {
			gridCell.setGridReference(mGrid);
		}
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
		mGrid.setSolvingAttemptId(solvingAttempt.mId);
		mGrid.setRowId(solvingAttempt.mGridId);

		GridStatistics gridStatistics = loadStatistics(solvingAttempt.mGridId);
		mGrid.setGridStatistics(gridStatistics);

		return (gridStatistics != null);
	}

	private boolean loadCell(String line) {
		if (line == null) {
			return false;
		}

		GridCell cell = mObjectsCreator.createGridCell(0, mGrid.getGridSize());
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

		if (mGridCageStorage == null) {
			mGridCageStorage = mObjectsCreator.createGridCageStorage();
		}
		if (!mGridCageStorage.fromStorageString(line, mSavedWithRevision, mGrid.mCells)) {
			return false;
		}
		GridCage cage = mObjectsCreator.createGridCage(mGridCageStorage.getId(),
													   mGridCageStorage.isHideOperator(),
													   mGridCageStorage.getResult(),
													   mGridCageStorage.getAction(),
													   mGridCageStorage.getCells());
		mGrid.mCages.add(cage);

		return true;
	}

	private boolean loadCellChange(String line) {
		if (line == null) {
			return false;
		}

		CellChange cellChange = mObjectsCreator.createCellChange();
		if (!cellChange.fromStorageString(line, mGrid.mCells, mSavedWithRevision)) {
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
		GridStatistics gridStatistics = statisticsDatabaseAdapter.getMostRecent(gridId);
		if (gridStatistics == null) {
			// No statistics available. Create a new statistics records.
			gridStatistics = statisticsDatabaseAdapter.insert(mGrid);
		}

		return gridStatistics;
	}

	public void setThrowExceptionOnError(boolean throwExceptionOnError) {
		mThrowExceptionOnError = throwExceptionOnError;
	}
}
