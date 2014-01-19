package net.mathdoku.plus.grid;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttempt;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import java.security.InvalidParameterException;

/**
 * The GridLoad is responsible for loading a grid from the database into a new
 * {@link net.mathdoku.plus.grid.Grid} object.
 */
public class GridLoader {
	private static final String TAG = "MathDoku.GridLoader";

	private int mSavedWithRevision;

	private GridLoaderData mGridLoaderData;

	private GridStorage mGridStorage;

	// By default this module throws exceptions on error when running in
	// development mode only.
	private boolean mThrowExceptionOnError;

	private GridLoaderObjectsCreator mGridLoaderObjectsCreator;

	/**
	 * Creates new instance of {@link net.mathdoku.plus.grid.GridLoader}.
	 */
	public GridLoader() {
		mGridLoaderObjectsCreator = new GridLoaderObjectsCreator();

		setThrowExceptionOnError(Config.mAppMode == Config.AppMode.DEVELOPMENT);
	}

	public void setObjectsCreator(
			GridLoaderObjectsCreator gridLoaderObjectsCreator) {
		if (gridLoaderObjectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter GridLoadObjectsCreator can not be null.");
		}
		mGridLoaderObjectsCreator = gridLoaderObjectsCreator;
	}

	/**
	 * Loads a solving attempt into a new {@link net.mathdoku.plus.grid.Grid}.
	 * 
	 * @param solvingAttemptId
	 *            The id of the solving attempt to be loaded.
	 * @return The Grid if load is successfully completed. Null in case of an
	 *         error.
	 */
	public Grid load(int solvingAttemptId) {
		SolvingAttempt solvingAttempt = loadSolvingAttempt(solvingAttemptId);
		if (solvingAttempt == null) {
			return null;
		}

		mGridLoaderData = mGridLoaderObjectsCreator.createGridLoaderData();
		mGridLoaderData.mDateCreated = solvingAttempt.mDateCreated;
		mGridLoaderData.mDateUpdated = solvingAttempt.mDateUpdated;
		mGridLoaderData.mSolvingAttemptId = solvingAttempt.mId;

		GridRow gridRow = mGridLoaderObjectsCreator.createGridDatabaseAdapter()
				.get(solvingAttempt.mGridId);
		if (gridRow == null) {
			return null;
		}
		if (gridRow.mGridSize <= 0) {
			// Cannot load solving attempt for grids with an invalid grid size.
			return null;
		}

		mGridLoaderData.mGridSize = gridRow.mGridSize;
		mGridLoaderData.mGridGeneratingParameters = gridRow.mGridGeneratingParameters;
		mSavedWithRevision = solvingAttempt.mSavedWithRevision;

		// SolvingAttemptData can only be processed after the grid size and
		// revision number is known.
		if (loadFromStorageStrings(solvingAttempt) == false) {
			return null;
		}

		if (loadStatistics(solvingAttempt.mGridId) == false) {
			return null;
		}

		return mGridLoaderObjectsCreator.createGrid(mGridLoaderData);
	}

	private SolvingAttempt loadSolvingAttempt(int solvingAttemptId) {
		SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = mGridLoaderObjectsCreator
				.createSolvingAttemptDatabaseAdapter();
		return solvingAttemptDatabaseAdapter.getData(solvingAttemptId);
	}

	private GridRow loadGridRow(int gridId) {
		GridDatabaseAdapter gridDatabaseAdapter = mGridLoaderObjectsCreator
				.createGridDatabaseAdapter();
		return gridDatabaseAdapter.get(gridId);
	}

	/**
	 * Loads data from the concatenated storage strings.
	 * 
	 * @return True in case the entire string is processed successful. False
	 *         otherwise.
	 */
	private boolean loadFromStorageStrings(SolvingAttempt solvingAttempt) {
		String line;
		try {
			if (solvingAttempt.mData == null
					|| (line = solvingAttempt.mData.getFirstLine()) == null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt at first line");
				}
				return false;
			}

			mGridStorage = mGridLoaderObjectsCreator.createGridStorage();
			if (mGridStorage.fromStorageString(line, mSavedWithRevision) == false) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Line does not contain general grid information while this was "
									+ "expected:" + line);
				}
				return false;
			}
			mGridLoaderData.mActive = mGridStorage.isActive();
			mGridLoaderData.mRevealed = mGridStorage.isRevealed();

			if ((line = solvingAttempt.mData.getNextLine()) == null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt after processing view information"
									+ ".");
				}
				return false;
			}

			// Read cells
			mGridLoaderData.mCells = mGridLoaderObjectsCreator
					.createArrayListOfGridCells();
			while (loadCell(line)) {
				line = solvingAttempt.mData.getNextLine();
			}
			// Check if expected number of cells is read.
			if (mGridLoaderData.mCells.size() != mGridLoaderData.mGridSize
					* mGridLoaderData.mGridSize) {
				throw new InvalidGridException(
						"Unexpected number of cells loaded. Expected: "
								+ (mGridLoaderData.mGridSize * mGridLoaderData.mGridSize)
								+ ", actual: " + mGridLoaderData.mCells.size());
			}

			// Read cages
			mGridLoaderData.mCages = mGridLoaderObjectsCreator
					.createArrayListOfGridCages();
			while (loadCage(line)) {
				line = solvingAttempt.mData.getNextLine();
			}
			// At least one expected is expected, so throw error in case no
			// cages have been loaded.
			if (mGridLoaderData.mCages.size() == 0) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Line does not contain cage information while this was expected:"
									+ line);
				}
				return false;
			}

			// Remaining lines contain cell changes (zero or more expected)
			mGridLoaderData.mCellChanges = mGridLoaderObjectsCreator
					.createArrayListOfCellChanges();
			while (loadCellChange(line)) {
				line = solvingAttempt.mData.getNextLine();
			}

			// Check if end of file is reached an not all information was read
			// yet.
			if (line != null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected line found while end of file was expected: "
									+ line);
				}
				return false;
			}
		} catch (InvalidGridException e) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Invalid format error when restoring solving attempt with id '"
								+ solvingAttempt.mId + "'\n" + e.getMessage());
			}
			return false;
		} catch (NumberFormatException e) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Invalid Number format error when restoring solving attempt with id '"
								+ solvingAttempt.mId + "'\n" + e.getMessage());
			}
			return false;
		} catch (IndexOutOfBoundsException e) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Index out of bound error when restoring solving attempt with id '"
								+ solvingAttempt.mId + "'\n" + e.getMessage());
			}
			return false;
		}
		return true;
	}

	private boolean loadCell(String line) {
		if (line == null) {
			return false;
		}

		GridCell cell = mGridLoaderObjectsCreator.createGridCell(0,
				mGridLoaderData.mGridSize);
		if (!cell.fromStorageString(line, mSavedWithRevision)) {
			return false;
		}
		mGridLoaderData.mCells.add(cell);

		return true;
	}

	private boolean loadCage(String line) {
		if (line == null) {
			return false;
		}

		GridCageStorage mGridCageStorage = mGridLoaderObjectsCreator.createGridCageStorage();
		if (mGridCageStorage.fromStorageString(line, mSavedWithRevision,
				mGridLoaderData.mCells) == false) {
			return false;
		}
		GridCage cage = mGridLoaderObjectsCreator.createGridCage(mGridCageStorage);
		mGridLoaderData.mCages.add(cage);

		return true;
	}

	private boolean loadCellChange(String line) {
		if (line == null) {
			return false;
		}

		CellChange cellChange = mGridLoaderObjectsCreator.createCellChange();
		if (!cellChange.fromStorageString(line, mGridLoaderData.mCells,
				mSavedWithRevision)) {
			return false;
		}
		mGridLoaderData.mCellChanges.add(cellChange);

		return true;
	}

	/**
	 * Load the most recent statistics for this grid.
	 */
	private boolean loadStatistics(int gridId) {
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = mGridLoaderObjectsCreator
				.createStatisticsDatabaseAdapter();
		mGridLoaderData.mGridStatistics = statisticsDatabaseAdapter.getMostRecent(gridId);
		return (mGridLoaderData.mGridStatistics != null);
	}

	public void setThrowExceptionOnError(boolean throwExceptionOnError) {
		mThrowExceptionOnError = throwExceptionOnError;
	}
}
