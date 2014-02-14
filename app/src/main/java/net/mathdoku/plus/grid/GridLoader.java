package net.mathdoku.plus.grid;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.CellChangeStorage;
import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.CellStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.SolvingAttemptStorage;
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
	private List<Cell> mCells;
	private List<GridCage> mGridCages;
	private List<CellChange> mCellChanges;

	public static class ObjectsCreator {

		public GridBuilder createGridBuilder() {
			return new GridBuilder();
		}

		public GridDatabaseAdapter createGridDatabaseAdapter() {
			return new GridDatabaseAdapter();
		}

		public SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
			return new SolvingAttemptDatabaseAdapter();
		}

		public SolvingAttemptStorage createSolvingAttemptStorage(
				String storageString) {
			return new SolvingAttemptStorage(storageString);
		}

		public GridStorage createGridStorage() {
			return new GridStorage();
		}

		public List<Cell> createArrayListOfCells() {
			return new ArrayList<Cell>();
		}

		public List<GridCage> createArrayListOfGridCages() {
			return new ArrayList<GridCage>();
		}

		public List<CellChange> createArrayListOfCellChanges() {
			return new ArrayList<CellChange>();
		}

		public CellStorage createCellStorage() {
			return new CellStorage();
		}

		public GridCageStorage createGridCageStorage() {
			return new GridCageStorage();
		}

		public GridCage createGridCage(CageBuilder cageBuilder) {
			return new GridCage(cageBuilder);
		}

		public CellChangeStorage createCellChangeStorage() {
			return new CellChangeStorage();
		}

		public CellChange createCellChange(CellChangeStorage cellChangeStorage) {
			return new CellChange(cellChangeStorage);
		}

		public StatisticsDatabaseAdapter createStatisticsDatabaseAdapter() {
			return new StatisticsDatabaseAdapter();
		}

		public Cell createCell(CellBuilder cellBuilder) {
			return new Cell(cellBuilder);
		}
	}

	private GridLoader.ObjectsCreator mObjectsCreator;

	// By default this module throws exceptions on error when running in
	// development mode only.
	private boolean mThrowExceptionOnError;

	/**
	 * Creates new instance of {@link net.mathdoku.plus.grid.GridLoader}.
	 */
	public GridLoader() {
		mObjectsCreator = new GridLoader.ObjectsCreator();

		setThrowExceptionOnError(Config.mAppMode == Config.AppMode.DEVELOPMENT);
	}

	public void setObjectsCreator(GridLoader.ObjectsCreator objectsCreator) {
		if (objectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter objectsCreator cannot be null.");
		}
		mObjectsCreator = objectsCreator;
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

		mGridBuilder = mObjectsCreator
				.createGridBuilder()
				.setDateCreated(solvingAttempt.mDateCreated)
				.setDateUpdated(solvingAttempt.mDateUpdated)
				.setSolvingAttemptId(solvingAttempt.mGridId, solvingAttempt.mId);
		GridRow gridRow = mObjectsCreator.createGridDatabaseAdapter().get(
				solvingAttempt.mGridId);
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

		// SolvingAttemptStorage can only be processed after the grid size and
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
		SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = mObjectsCreator
				.createSolvingAttemptDatabaseAdapter();
		return solvingAttemptDatabaseAdapter.getData(solvingAttemptId);
	}

	private GridRow loadGridRow(int gridId) {
		GridDatabaseAdapter gridDatabaseAdapter = mObjectsCreator
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
		String line = "";
		try {
			if (solvingAttempt.mStorageString == null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Solving attempt contains no storage string.");
				}
				return false;
			}

			SolvingAttemptStorage solvingAttemptStorage = mObjectsCreator
					.createSolvingAttemptStorage(solvingAttempt.mStorageString);
			if ((line = solvingAttemptStorage.getFirstLine()) == null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt at first line");
				}
				return false;
			}

			GridStorage mGridStorage = mObjectsCreator.createGridStorage();
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

			if ((line = solvingAttemptStorage.getNextLine()) == null) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt after processing view information"
									+ ".");
				}
				return false;
			}

			// Read cells
			mCells = mObjectsCreator.createArrayListOfCells();
			while (loadCell(line)) {
				line = solvingAttemptStorage.getNextLine();
			}
			// Check if expected number of cells is read.
			if (mCells.size() != mGridBuilder.mGridSize
					* mGridBuilder.mGridSize) {
				throw new InvalidGridException(
						"Unexpected number of cells loaded. Expected: "
								+ (mGridBuilder.mGridSize * mGridBuilder.mGridSize)
								+ ", actual: " + mCells.size());
			}
			mGridBuilder.setCells(mCells);

			// Read cages
			mGridCages = mObjectsCreator.createArrayListOfGridCages();
			while (loadCage(line)) {
				line = solvingAttemptStorage.getNextLine();
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
			mCellChanges = mObjectsCreator.createArrayListOfCellChanges();
			while (loadCellChange(line)) {
				line = solvingAttemptStorage.getNextLine();
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
				throw new InvalidGridException("Invalid format error in line ("
						+ line + ") when restoring solving attempt with id '"
						+ solvingAttempt.mId + "'\n" + e.getMessage());
			}
			return false;
		} catch (NumberFormatException e) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Invalid Number format error in line (\" + line + \") when restoring solving attempt with id '"
								+ solvingAttempt.mId + "'\n" + e.getMessage());
			}
			return false;
		} catch (IndexOutOfBoundsException e) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Index out of bound error in line (\" + line + \") when restoring solving attempt with id '"
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

		CellStorage mCellStorage = mObjectsCreator.createCellStorage();
		CellBuilder cellBuilder = mCellStorage.getCellBuilderFromStorageString(
				line, mSavedWithRevision);
		if (cellBuilder == null) {
			return false;
		}
		cellBuilder.setGridSize(mGridBuilder.mGridSize);
		cellBuilder.setSkipCheckCageReferenceOnBuild();
		Cell cell = mObjectsCreator.createCell(cellBuilder);
		mCells.add(cell);

		return true;
	}

	private boolean loadCage(String line) {
		if (line == null) {
			return false;
		}

		GridCageStorage gridCageStorage = mObjectsCreator
				.createGridCageStorage();
		CageBuilder cageBuilder = gridCageStorage
				.getCageBuilderFromStorageString(line, mSavedWithRevision,
						mCells);
		if (cageBuilder == null) {
			return false;
		}
		GridCage cage = mObjectsCreator.createGridCage(cageBuilder);
		mGridCages.add(cage);

		return true;
	}

	private boolean loadCellChange(String line) {
		if (line == null) {
			return false;
		}

		CellChangeStorage cellChangeStorage = mObjectsCreator
				.createCellChangeStorage();
		if (!cellChangeStorage.fromStorageString(line, mCells,
				mSavedWithRevision)) {
			return false;
		}
		CellChange cellChange = mObjectsCreator
				.createCellChange(cellChangeStorage);
		mCellChanges.add(cellChange);

		return true;
	}

	/**
	 * Load the most recent statistics for this grid.
	 */
	private boolean loadStatistics(int gridId) {
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = mObjectsCreator
				.createStatisticsDatabaseAdapter();
		// TODO: statistics should be loaded for a specific solving attempt
		GridStatistics gridStatistics = statisticsDatabaseAdapter
				.getMostRecent(gridId);
		mGridBuilder.setGridStatistics(gridStatistics);

		return gridStatistics != null;
	}

	public void setThrowExceptionOnError(boolean throwExceptionOnError) {
		mThrowExceptionOnError = throwExceptionOnError;
	}
}
