package net.mathdoku.plus.puzzle.grid;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.puzzle.cellchange.CellChange;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.CellChangeStorage;
import net.mathdoku.plus.storage.CageStorage;
import net.mathdoku.plus.storage.CellStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.SolvingAttemptStorage;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.GridRow;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptRow;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * The GridLoad is responsible for loading a grid from the database into a new
 * {@link Grid} object.
 */
public class GridLoader {
	@SuppressWarnings("unused")
	private static final String TAG = GridLoader.class.getName();

	private int mSavedWithRevision;

	private GridBuilder mGridBuilder;
	private List<Cell> mCells;
	private List<Cage> mCages;
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

		public List<Cage> createArrayListOfCages() {
			return new ArrayList<Cage>();
		}

		public List<CellChange> createArrayListOfCellChanges() {
			return new ArrayList<CellChange>();
		}

		public CellStorage createCellStorage() {
			return new CellStorage();
		}

		public CageStorage createCageStorage() {
			return new CageStorage();
		}

		public Cage createCage(CageBuilder cageBuilder) {
			return new Cage(cageBuilder);
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
	 * Creates new instance of {@link GridLoader}.
	 */
	public GridLoader() {
		mObjectsCreator = new GridLoader.ObjectsCreator();

		// Only throw exceptions when running in development mode. In production
		// mode the grid load just fails without an error.
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
	 * Loads a solving attempt into a new {@link Grid}.
	 * 
	 * @param solvingAttemptId
	 *            The id of the solving attempt to be loaded.
	 * @return The Grid if load is successfully completed. Null in case of an
	 *         error.
	 */
	public Grid load(int solvingAttemptId) {
		SolvingAttemptRow solvingAttemptRow = loadSolvingAttempt(solvingAttemptId);
		if (solvingAttemptRow == null) {
			return null;
		}

		mGridBuilder = mObjectsCreator
				.createGridBuilder()
				.setDateCreated(solvingAttemptRow.getSolvingAttemptDateCreated())
				.setDateUpdated(solvingAttemptRow.getSolvingAttemptDateUpdated())
				.setSolvingAttemptId(solvingAttemptRow.getGridId(),
									 solvingAttemptRow.getSolvingAttemptId());
		GridRow gridRow = mObjectsCreator.createGridDatabaseAdapter().get(
				solvingAttemptRow.getGridId());
		if (gridRow == null || gridRow.getGridSize() <= 0) {
			return null;
		}

		mGridBuilder
				.setGridSize(gridRow.getGridSize())
				.setGridGeneratingParameters(gridRow.getGridGeneratingParameters());
		mSavedWithRevision = solvingAttemptRow.getSavedWithRevision();

		// SolvingAttemptStorage can only be processed after the grid size and
		// revision number is known.
		if (!loadFromStorageStrings(solvingAttemptRow)
				|| !loadStatistics(solvingAttemptRow.getGridId())) {
			return null;
		}

		return mGridBuilder.build();
	}

	private SolvingAttemptRow loadSolvingAttempt(int solvingAttemptId) {
		SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = mObjectsCreator
				.createSolvingAttemptDatabaseAdapter();
		return solvingAttemptDatabaseAdapter.getData(solvingAttemptId);
	}

	/**
	 * Loads data from the concatenated storage strings.
	 * 
	 * @return True in case the entire string is processed successful. False
	 *         otherwise.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean loadFromStorageStrings(SolvingAttemptRow solvingAttemptRow) {
		if (solvingAttemptRow.getStorageString() == null) {
			return errorOnLoadStorageString(
					"Solving attempt contains no storage string.", null);
		}

		try {
			SolvingAttemptStorage solvingAttemptStorage = mObjectsCreator
					.createSolvingAttemptStorage(solvingAttemptRow.getStorageString());
			loadGridStorage(solvingAttemptStorage);
			loadCells(solvingAttemptStorage);
			loadCages(solvingAttemptStorage);
			loadCellChanges(solvingAttemptStorage);
		} catch (NumberFormatException e) {
			return errorOnLoadStorageString(
					String.format(
							"Invalid Number format error when restoring solving attempt with id %d.",
							solvingAttemptRow.getSolvingAttemptId()), e);
		} catch (InvalidGridException e) {
			return errorOnLoadStorageString(
					String.format(
							"Loading of solving attempt data with id %d to grid builder failed.'",
							solvingAttemptRow.getSolvingAttemptId()), e);
		}

		return true;
	}

	private boolean loadGridStorage(SolvingAttemptStorage solvingAttemptStorage) {
		String line = solvingAttemptStorage.getNextLine();
		if (line == null) {
			throw new InvalidGridException(
					"Unexpected end of solving attempt at first line");
		}

		GridStorage mGridStorage = mObjectsCreator.createGridStorage();
		if (!mGridStorage.fromStorageString(line, mSavedWithRevision)) {
			throw new InvalidGridException(
					"Line below does not contain general grid information while this was expected."
							+ "\nLine: " + line);
		}
		mGridBuilder.setActive(mGridStorage.isActive());
		mGridBuilder.setRevealed(mGridStorage.isRevealed());

		line = solvingAttemptStorage.getNextLine();
		if (line == null) {
			throw new InvalidGridException(
					"Unexpected end of solving attempt after processing view information.");
		}

		return true;
	}

	private boolean loadCells(SolvingAttemptStorage solvingAttemptStorage) {
		mCells = mObjectsCreator.createArrayListOfCells();

		String line = solvingAttemptStorage.getLine();
		while (loadCell(line)) {
			line = solvingAttemptStorage.getNextLine();
		}
		// Check if expected number of cells is read.
		if (mCells.size() != mGridBuilder.mGridSize * mGridBuilder.mGridSize) {

			throw new InvalidGridException(
					String
							.format("Unexpected number of cells loaded. Expected: %d, actual %d.",
									mGridBuilder.mGridSize
											* mGridBuilder.mGridSize,
									mCells.size()));
		}
		mGridBuilder.setCells(mCells);

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

	private boolean loadCages(SolvingAttemptStorage solvingAttemptStorage) {
		mCages = mObjectsCreator.createArrayListOfCages();

		String line = solvingAttemptStorage.getLine();
		while (loadCage(line)) {
			line = solvingAttemptStorage.getNextLine();
		}

		// At least one cage expected is expected, so throw error in case no
		// cages have been loaded.
		if (mCages.isEmpty()) {
			throw new InvalidGridException(
					"Line below does not contain cage information while this was expected.\nLine: "
							+ line);
		}
		mGridBuilder.setCages(mCages);

		return true;
	}

	private boolean loadCage(String line) {
		if (line == null) {
			return false;
		}

		CageStorage cageStorage = mObjectsCreator.createCageStorage();
		CageBuilder cageBuilder = cageStorage.getCageBuilderFromStorageString(
				line, mSavedWithRevision, mCells);
		if (cageBuilder == null) {
			return false;
		}
		Cage cage = mObjectsCreator.createCage(cageBuilder);
		mCages.add(cage);

		return true;
	}

	private boolean loadCellChanges(SolvingAttemptStorage solvingAttemptStorage) {
		mCellChanges = mObjectsCreator.createArrayListOfCellChanges();

		// Start with the last line which was read but which was not yet
		// processed.
		String line = solvingAttemptStorage.getLine();
		while (loadCellChange(line)) {
			line = solvingAttemptStorage.getNextLine();
		}
		// Zero or more cell changes expected.
		mGridBuilder.setCellChanges(mCellChanges);

		// Check if end of file is reached and not all information was read
		// yet.
		if (line != null) {
			throw new InvalidGridException(
					"Unexpected line below found while end of file was expected.\nLine: "
							+ line);
		}

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
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

	private boolean errorOnLoadStorageString(String error, Throwable throwable) {
		if (mThrowExceptionOnError) {
			throw new InvalidGridException(error, throwable);
		}
		return false;
	}
}
