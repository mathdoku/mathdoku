package net.mathdoku.plus.grid;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.CellChangeStorage;
import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.GridCellStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttempt;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * The GridLoad is responsible for loading a grid from the database into a new
 * {@link net.mathdoku.plus.grid.Grid} object.
 */
public class GridLoader {
	private static final String TAG = "MathDoku.GridLoader";

	private int mSavedWithRevision;

	private GridBuilder mGridBuilder;
	private List<GridCell> mGridCells;
	private List<GridCage> mGridCages;
	private List<CellChange> mCellChanges;

	private GridStorage mGridStorage;

	// By default this module throws exceptions on error when running in
	// development mode only.
	private boolean mThrowExceptionOnError;

	private GridObjectsCreator mGridObjectsCreator;

	/**
	 * Creates new instance of {@link net.mathdoku.plus.grid.GridLoader}.
	 */
	public GridLoader() {
		mGridObjectsCreator = new GridObjectsCreator();

		setThrowExceptionOnError(Config.mAppMode == Config.AppMode.DEVELOPMENT);
	}

	public void setObjectsCreator(
GridObjectsCreator gridObjectsCreator) {
		if (gridObjectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter GridLoadObjectsCreator can not be null.");
		}
		mGridObjectsCreator = gridObjectsCreator;
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

		mGridBuilder = mGridObjectsCreator
	.createGridBuilder()
				.setDateCreated(solvingAttempt.mDateCreated)
				.setDateUpdated(solvingAttempt.mDateUpdated)
				.setSolvingAttemptId(solvingAttempt.mGridId,solvingAttempt.mId);
		GridRow gridRow = mGridObjectsCreator.createGridDatabaseAdapter()
				.get(solvingAttempt.mGridId);
		if (gridRow == null) {
			return null;
		}
		if (gridRow.mGridSize <= 0) {
			// Cannot load solving attempt for grids with an invalid grid size.
			return null;
		}

		mGridBuilder
				.setGridSize(gridRow.mGridSize)
				.setGridGeneratingParameters(gridRow.mGridGeneratingParameters);
		mSavedWithRevision = solvingAttempt.mSavedWithRevision;

		// SolvingAttemptData can only be processed after the grid size and
		// revision number is known.
		if (loadFromStorageStrings(solvingAttempt) == false) {
			return null;
		}

		if (loadStatistics(solvingAttempt.mGridId) == false) {
			return null;
		}
		return mGridBuilder.build();
	}

	private SolvingAttempt loadSolvingAttempt(int solvingAttemptId) {
		SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = mGridObjectsCreator
				.createSolvingAttemptDatabaseAdapter();
		return solvingAttemptDatabaseAdapter.getData(solvingAttemptId);
	}

	private GridRow loadGridRow(int gridId) {
		GridDatabaseAdapter gridDatabaseAdapter = mGridObjectsCreator
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

			mGridStorage = mGridObjectsCreator.createGridStorage();
			if (mGridStorage.fromStorageString(line, mSavedWithRevision) == false) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Line does not contain general grid information while this was "
									+ "expected:" + line);
				}
				return false;
			}
			mGridBuilder.setActive(mGridStorage.isActive());
			mGridBuilder.setRevealed(mGridStorage.isRevealed());

			if ((line = solvingAttempt.mData.getNextLine()) == null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt after processing view information"
									+ ".");
				}
				return false;
			}

			// Read cells
			mGridCells = mGridObjectsCreator.createArrayListOfGridCells();
			while (loadCell(line)) {
				line = solvingAttempt.mData.getNextLine();
			}
			// Check if expected number of cells is read.
			if (mGridCells.size() != mGridBuilder.mGridSize
					* mGridBuilder.mGridSize) {
				throw new InvalidGridException(
						"Unexpected number of cells loaded. Expected: "
								+ (mGridBuilder.mGridSize * mGridBuilder.mGridSize)
								+ ", actual: " + mGridCells.size());
			}
			mGridBuilder.setCells(mGridCells);

			// Read cages
			mGridCages = mGridObjectsCreator.createArrayListOfGridCages();
			while (loadCage(line)) {
				line = solvingAttempt.mData.getNextLine();
			}
			// At least one expected is expected, so throw error in case no
			// cages have been loaded.
			if (mGridCages.size() == 0) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Line does not contain cage information while this was expected:"
									+ line);
				}
				return false;
			}
			mGridBuilder.setCages(mGridCages);

			// Remaining lines contain cell changes (zero or more expected)
			mCellChanges = mGridObjectsCreator.createArrayListOfCellChanges();
			while (loadCellChange(line)) {
				line = solvingAttempt.mData.getNextLine();
			}
			mGridBuilder.setCellChanges(mCellChanges);

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

		GridCellStorage mGridCellStorage = mGridObjectsCreator
				.createGridCellStorage();
		if (mGridCellStorage.fromStorageString(line, mSavedWithRevision) == false) {
			return false;
		}
		GridCell cell = mGridObjectsCreator.createGridCell(mGridCellStorage);
		mGridCells.add(cell);

		return true;
	}

	private boolean loadCage(String line) {
		if (line == null) {
			return false;
		}

		GridCageStorage gridCageStorage = mGridObjectsCreator
				.createGridCageStorage();
		CageBuilder cageBuilder = gridCageStorage.getCageBuilderFromStorageString(line, mSavedWithRevision,
				mGridCells);
		if (cageBuilder == null) {
			return false;
		}
		GridCage cage = mGridObjectsCreator.createGridCage(cageBuilder);
		mGridCages.add(cage);

		return true;
	}

	private boolean loadCellChange(String line) {
		if (line == null) {
			return false;
		}

		CellChangeStorage cellChangeStorage = mGridObjectsCreator
				.createCellChangeStorage();
		if (!cellChangeStorage.fromStorageString(line, mGridCells,
				mSavedWithRevision)) {
			return false;
		}
		CellChange cellChange = mGridObjectsCreator
				.createCellChange(cellChangeStorage);
		mCellChanges.add(cellChange);

		return true;
	}

	/**
	 * Load the most recent statistics for this grid.
	 */
	private boolean loadStatistics(int gridId) {
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = mGridObjectsCreator
				.createStatisticsDatabaseAdapter();
		GridStatistics gridStatistics = statisticsDatabaseAdapter
				.getMostRecent(gridId);
		mGridBuilder.setGridStatistics(gridStatistics);
		return (gridStatistics != null);
	}

	public void setThrowExceptionOnError(boolean throwExceptionOnError) {
		mThrowExceptionOnError = throwExceptionOnError;
	}
}
