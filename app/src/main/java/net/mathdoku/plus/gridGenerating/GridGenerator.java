package net.mathdoku.plus.gridgenerating;

import android.util.Log;

import com.srlee.dlx.MathDokuDLX;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.griddefinition.GridDefinition;
import net.mathdoku.plus.gridgenerating.CellCoordinates.CellCoordinates;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridBuilder;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generate a single grid.
 */
public class GridGenerator {
	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_GRID_GENERATOR = Config.mAppMode == Config.AppMode.DEVELOPMENT && true;
	@SuppressWarnings("PointlessBooleanExpression")
	public static final boolean DEBUG_GRID_GENERATOR_FULL = DEBUG_GRID_GENERATOR && true;
	private static final String TAG = GridGenerator.class.getName();
	private final Listener listener;
	private boolean developmentMode = false;
	private GridGeneratingParameters gridGeneratingParameters;
	private int gridSizeValue;
	private List<Cell> mCells;
	private List<Cage> mCages;
	private Matrix<Integer> correctValueMatrix;
	private Matrix<Integer> cageIdMatrix;
	private int countSingles;
	private Random mRandom;
	private CageTypeGenerator mCageTypeGenerator;

	public GridGenerator(Listener listener) {
		this.listener = listener;
		developmentMode = false;
	}

	public Grid createGrid(GridGeneratingParameters gridGeneratingParameters) {
		this.gridGeneratingParameters = gridGeneratingParameters;
		gridSizeValue = this.gridGeneratingParameters
				.getGridType()
				.getGridSize();

		// Use the game seed to initialize the randomizer which is used to
		// generate the game. Overwrite this game seed with the fixed value of a
		// given game in case you want to recreate the same grid. All you need
		// to ensure is that you the correct revision of this
		// GridGeneratorAsyncTask
		// module. Please be aware that in case the implementation of the random
		// method changes, it will not be possible to recreate the grids!
		mRandom = new Random(this.gridGeneratingParameters.getGameSeed());

		if (DEBUG_GRID_GENERATOR) {
			debugLog(this.gridGeneratingParameters.toString());
		}

		final long mTimeStarted = System.currentTimeMillis();

		Grid grid = null;
		int attemptsToCreateGrid = 0;
		while (grid == null) {
			attemptsToCreateGrid++;

			// Check whether the generating process should be aborted due to
			// cancellation of the grid dialog.
			if (listener.isCancelled()) {
				return null;
			}

			if (developmentMode) {
				listener.updateProgressHighLevel("attempt"
						+ attemptsToCreateGrid);
			}

			grid = attemptToCreateGrid();
		}
		if (DEBUG_GRID_GENERATOR) {
			debugLog("Finished create grid in %d attempts.",
					attemptsToCreateGrid);
		}
		if (Config.mAppMode == Config.AppMode.DEVELOPMENT
				&& System.currentTimeMillis() - mTimeStarted > 30 * 1000) {
			// Sometimes grid generation takes too long. Until I have a game
			// seed which reproduces this problem I can not fix it. If such
			// a game is found in development, an exception will be thrown
			// to investigate it.
			final int elapsedTimeInSeconds = (int) ((System.currentTimeMillis() - mTimeStarted) / 1000);
			signalSlowGridGeneration(elapsedTimeInSeconds);
			return null;
		}

		grid.save();
		return grid;
	}

	private Grid attemptToCreateGrid() {
		randomizeCorrectValueMatrix();

		createCells();

		// Check whether the generating process should be aborted due to
		// cancellation of the grid dialog.
		if (listener.isCancelled()) {
			return null;
		}

		// Create the cages.
		mCages = new ArrayList<Cage>();
		if (!fillGridWithCages()) {
			// For some reason the creation of the cages was not successful.
			// Start over again.
			return null;
		}

		// Check whether the generating process should be aborted due to
		// cancellation of the grid dialog.
		if (listener.isCancelled()) {
			return null;
		}

		// Create the grid object
		GridBuilder mGridBuilder = new GridBuilder();
		mGridBuilder
				.setGridSize(gridSizeValue)
				.setCells(mCells)
				.setCages(mCages)
				.setGridGeneratingParameters(this.gridGeneratingParameters);
		Grid grid = mGridBuilder.build();

		if (hasNonUniqueSolution(grid)) {
			return null;
		}

		return grid;
	}

	private boolean hasNonUniqueSolution(Grid grid) {
		if (developmentMode) {
			if (DEBUG_GRID_GENERATOR) {
				debugLog("The uniqueness of the solution of this grid has not been verified.");
			}
			// In development mode, the uniqueness of the grid is not relevant
			// compared to the speed of generating. Each solution has to be
			// accepted regardless whether it is unique.
			return false;
		}

		listener.updateProgressDetailLevel("Verify unique solution");
		if (new MathDokuDLX(gridSizeValue, grid.getCages()).hasUniqueSolution()) {
			if (DEBUG_GRID_GENERATOR) {
				debugLog("This grid has a unique solution.");
			}
			return false;
		} else {
			if (DEBUG_GRID_GENERATOR) {
				debugLog("This grid does not have a unique solution.");
			}
			return true;
		}
	}

	private void createCells() {
		mCells = new ArrayList<Cell>();
		int cellNumber = 0;
		for (int column = 0; column < gridSizeValue; column++) {
			for (int row = 0; row < gridSizeValue; row++) {
				Cell cell = new CellBuilder()
						.setGridSize(gridSizeValue)
						.setId(cellNumber++)
						.setCorrectValue(correctValueMatrix.get(row, column))
						.setSkipCheckCageReferenceOnBuild()
						.build();
				mCells.add(cell);
			}
		}
	}

	private void signalSlowGridGeneration(int elapsedTimeInSeconds) {
		String message = String.format(
				"Game generation took too long (%d) secs.",
				elapsedTimeInSeconds);
		listener.updateProgressDetailLevel(message);
		listener.signalSlowGridGeneration();
		Log.i(TAG, message);
		Log.i(TAG, gridGeneratingParameters.toString());
	}

	public Grid createGridInDevelopmentMode(
			GridGeneratingParameters gridGeneratingParameters) {
		developmentMode = true;
		return createGrid(gridGeneratingParameters);
	}

	/*
	 * Fills the grid with random numbers. Each digit (1 .. grid size) is used
	 * once per row and once per column.
	 */
	private void randomizeCorrectValueMatrix() {
		if (developmentMode) {
			listener.updateProgressDetailLevel("Randomize grid.");
		}
		correctValueMatrix = new RandomIntegerMatrixGenerator(gridSizeValue, mRandom).getMatrix();
	}

	/**
	 * Creates cages for the current grid which is already filled with numbers.
	 *
	 * @return True in case the cages have been created successfully.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean fillGridWithCages() {
		if (developmentMode) {
			listener.updateProgressDetailLevel("Create cages.");
		}

		mCageTypeGenerator = CageTypeGenerator.getInstance();

		for (int attempts = 1; attempts <= 20; attempts++) {
			if (listener.isCancelled()) {
				return false;
			}
			if (attemptToFillGridWithCages()) {
				if (DEBUG_GRID_GENERATOR) {
					printCageCreationDebugInformation();
				}

				return true;
			}
		}

		return false;
	}

	private boolean attemptToFillGridWithCages() {
		cageIdMatrix = new Matrix<Integer>(gridSizeValue, Cage.CAGE_ID_NOT_SET);

		// TODO: actually the cage list should be cleared when starting a new
		// cycle of generating a cage. This however breaks the current unit test
		// because the entire list of cages is given to MathDokuDLX-class for
		// checking for a valid solution. If previously a grid was found with a
		// non unique solution, this will also fail all solutions in this
		// do-while loop as well and as a result affects the randomizer!!
		//
		// After refactoring of the GridGenerator, the GridCreators-classes and
		// their parameters need to be refreshed. Also the GeneratorVersion has
		// to be increased!
		for (int i = 0; i <= 15; i++) {
			Log.d(TAG, "CAGES NEED TO BE CLEARED!"); // Todo: remove
		}
		// mCages.clear();

		// Todo: remove the variable countSingles when refactoring of
		// GridGenerator is completed.
		countSingles = 0;

		if (gridGeneratingParameters.getMaxCageSize() >= CageTypeGenerator.MAX_CAGE_SIZE) {
			createFirstCageWithBiggerSize();
			if (listener.isCancelled()) {
				return false;
			}
		}

		// Fill remainder of grid
		CellCoordinates coordinatesCellNotInAnyCage = cageIdMatrix
				.getCellCoordinatesForFirstEmptyCell();
		while (coordinatesCellNotInAnyCage.isNotNull()) {
			if (listener.isCancelled()) {
				return false;
			}

			if (!createCageWithNormalSize(coordinatesCellNotInAnyCage)) {
				return false;
			}

			coordinatesCellNotInAnyCage = cageIdMatrix
					.getCellCoordinatesForFirstEmptyCell();
		}

		return gridHasUniqueGridDefinition();
	}

	private boolean gridHasUniqueGridDefinition() {
		String gridDefinition = GridDefinition.getDefinition(mCells, mCages,
				gridGeneratingParameters);
		if (new GridDatabaseAdapter().getByGridDefinition(gridDefinition) != null) {
			if (developmentMode) {
				listener
						.updateProgressDetailLevel("Grid has been generated before.");
			}
			return false;
		}
		return true;
	}

	private void createFirstCageWithBiggerSize() {
		for (int attempts = 1; attempts <= 10; attempts++) {
			if (listener.isCancelled()) {
				return;
			}
			if (attemptToCreateFirstCageWithBiggerSize()) {
				return;
			}
		}
	}

	private boolean attemptToCreateFirstCageWithBiggerSize() {
		CageType cageType = mCageTypeGenerator.getRandomCageType(
				gridGeneratingParameters.getMaxCageSize(), gridSizeValue,
				gridSizeValue, mRandom);
		if (cageType != null) {
			CellCoordinates cellCoordinatesForFirstCage = getCellCoordinatesForFirstCage(cageType);

			CellCoordinates coordinatesTopLeft = cageType
					.getOriginCoordinates(cellCoordinatesForFirstCage);

			// TODO: replace the two lines above with the single
			// line below. As the cell coordinates
			// for the first cell are determined at random, it has
			// no value to shift the cage type
			// in such a way that the top left cell of the cage type
			// is positioned at the origin cell.
			// This however breaks the unit test and should not be
			// done until refactor of this class
			// has been completed.
			// CellCoordinates coordinatesTopLeft =
			// getCellCoordinatesForFirstCage(cageType);

			if (createCageAtCoordinates(cageType, coordinatesTopLeft,
					4 * gridGeneratingParameters.getMaxCagePermutations())) {
				return true;
			}
		}

		return false;
	}

	private boolean noMoreSingleCellCagesAllowed() {
		return countSingleCellCages() >= gridGeneratingParameters
				.getMaximumSingleCellCages();
	}

	private int countSingleCellCages() {
		// TODO: replace with for loop below when refactoring of GridGenerator
		// is completed. Currently the list of cages is not cleared when
		// starting a new attempt. As a result it is not yet possible to count
		// the number of single cell cages.
		return countSingles;

		// int countSingleCellCages = 0;
		// for (Cage cage : mCages) {
		// if (cage.isSingleCellCage()) {
		// countSingleCellCages++;
		// }
		// }
		// return countSingleCellCages;
	}

	private CellCoordinates getCellCoordinatesForFirstCage(CageType cageType) {
		/*
		 * // TODO: enable when mCages.clear is enabled if (mCages.size() != 0)
		 * { throw new IllegalStateException(
		 * "Only to be used if not other cells are placed in the grid."); }
		 */
		// Use +1 in calls to randomizer to prevent exceptions in case the
		// entire height and/or width is needed for the cage type.
		int startRow = mRandom
				.nextInt(gridSizeValue - cageType.getHeight() + 1);
		int startCol = mRandom.nextInt(gridSizeValue - cageType.getWidth() + 1);
		return new CellCoordinates(startRow, startCol);
	}

	private List<Cell> getAllCells(
			CellCoordinates[] cellCoordinatesOfAllCellsInCage) {
		List<Cell> cells = new ArrayList<Cell>();
		for (CellCoordinates cellCoordinates : cellCoordinatesOfAllCellsInCage) {
			cells.add(getCellAt(cellCoordinates));
		}
		return cells;
	}

	private Cell getCellAt(CellCoordinates cellCoordinates) {
		int cellId = getCellId(cellCoordinates.getRow(),
				cellCoordinates.getColumn());
		if (cellId < 0) {
			return null;
		}

		return mCells.get(cellId);
	}

	private int getCellId(int row, int column) {
		if (row < 0 || row >= gridSizeValue) {
			return -1;
		}
		if (column < 0 || column >= gridSizeValue) {
			return -1;
		}
		return row * gridSizeValue + column;
	}

	/**
	 * Create the cage at the given coordinates.
	 *
	 * @param cageType
	 *            The type of cage to be created.
	 * @param originCell
	 *            the cell at which the cage will be placed. The coordinates of
	 *            the cells to be used for the cage.
	 * @param maxPermutations
	 *            The maximum permutations allowed to create the cage. Use 0 in
	 *            case no checking on the number of permutations needs to be
	 *            done.
	 * @return True if the cage is created. False otherwise.
	 */
	private boolean createCageAtCoordinates(CageType cageType,
			CellCoordinates originCell, int maxPermutations) {
		if (cageType == null || originCell == null) {
			return false;
		}

		CandidateCageCreator candidateCageCreator = createCandidateCageCreator();
		if (candidateCageCreator.cageTypeDoesNotFitAtCellCoordinates(cageType,
				originCell)) {
			return false;
		}

		List<Cell> cells = getAllCells(candidateCageCreator.getCellsCoordinates());

		Cage candidateCage = candidateCageCreator.create(getIdNewCage(), cells);
		if (candidateCageHasTooManyPermutations(candidateCage, maxPermutations)) {
			return false;
		}

		if (candidateCage.isValid()) {
			CellCoordinates[] cellCoordinatesOfAllCellsInCage = cageType
					.getCellCoordinatesOfAllCellsInCage(originCell);
			addCageToSolution(candidateCage, cellCoordinatesOfAllCellsInCage);

			mCages.add(candidateCage);
			return true;
		}

		return false;
	}

	private boolean candidateCageHasTooManyPermutations(Cage candidateCage,
			int maxPermutations) {
		if (candidateCage.getPossibleCombos().size() > maxPermutations) {
			// If a cage has many permutations it reduces the chance to find a
			// unique solution for the puzzle.
			if (DEBUG_GRID_GENERATOR_FULL) {
				debugLog(
						"This cage type has been rejected as it has more than %d initial permutations which fulfill the cage requirement.",
						maxPermutations);
			}
			return true;
		}
		return false;
	}

	private CandidateCageCreator createCandidateCageCreator() {
		CandidateCageCreatorParameters candidateCageCreatorParameters;
		candidateCageCreatorParameters = new CandidateCageCreatorParameters()
				.setGridGeneratingParameters(gridGeneratingParameters)
				.setRandom(mRandom)
				.setCorrectValueMatrix(correctValueMatrix)
				.setCageIdMatrix(cageIdMatrix);
		return new CandidateCageCreator(candidateCageCreatorParameters)
				.enableLogging(DEBUG_GRID_GENERATOR_FULL);
	}

	private void addCageToSolution(Cage cage,
			CellCoordinates[] cellCoordinatesOfAllCellsInCage) {
		List<Cell> cells = getAllCells(cellCoordinatesOfAllCellsInCage);

		// Set cage id in all cells used by this cage.
		for (Cell cell : cells) {
			cell.setCageId(cage.getId());
		}

		// Update the cage matrix
		for (Cell cell : cells) {
			cageIdMatrix.setValueToRowColumn(cage.getId(), cell.getRow(),
					cell.getColumn());
		}
	}

	private int getIdNewCage() {
		return mCages.size();
	}

	private void debugLog(String format, Object... args) {
		Log.d(TAG, String.format(format, args));
	}

	/**
	 * Create a new cage which originates at the given cell. The cage type for
	 * this cage will be randomly determined.
	 * 
	 * @param originCell
	 *            The cell at which a randomly selected cage has to be placed.
	 * 
	 * @return The selected grid cage type.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean createCageWithNormalSize(CellCoordinates originCell) {
		if (DEBUG_GRID_GENERATOR_FULL) {
			Log.d(TAG, "Determine valid cages for " + originCell);
		}

		RandomListItemSelector<CageType> randomCageTypeSelector = new RandomListItemSelector<CageType>(
				mRandom,
				mCageTypeGenerator
						.getCageTypesWithSizeEqualOrLessThan(gridGeneratingParameters
								.getMaxCageSize()));
		randomCageTypeSelector.setLastItemToBeSelected(mCageTypeGenerator
				.getSingleCellCageType());
		while (!randomCageTypeSelector.isEmpty()) {
			// Check whether the generating process should be aborted
			// due to cancellation of the grid dialog.
			if (listener.isCancelled()) {
				return false;
			}

			CageType selectedCageType = randomCageTypeSelector.next();
			if (hasTooManySingleCellCages(selectedCageType)) {
				return false;
			}
			if (createCageAtCoordinates(selectedCageType, originCell,
					gridGeneratingParameters.getMaxCagePermutations())) {
				return true;
			}
		}

		throw new IllegalStateException(
				"Should not reach this point as the single cell cage will be selected above!");
	}

	private boolean hasTooManySingleCellCages(CageType selectedCageType) {
		if (!selectedCageType.equals(mCageTypeGenerator
				.getSingleCellCageType())) {
			return false;
		}
		if (noMoreSingleCellCagesAllowed()) {
			if (developmentMode) {
				listener.updateProgressDetailLevel(String.format(
						"Found more single cell cages than allowed (%d).",
						gridGeneratingParameters
								.getMaximumSingleCellCages()));
			}
			return true;
		}

		// Todo: remove after refactoring GridGenerator is completed. Single
		// cell cages have to be counted using the list mCages instead of
		// using this var.
		countSingles++;

		return false;
	}

	/**
	 * Print debug information for create cage process to logging.
	 */
	private void printCageCreationDebugInformation() {
		debugLog("   Checking cage type");
		String cageIdFormat = "%d";
		String emptyCell = ".";
		if (mCages.size() > 100) {
			cageIdFormat = "%03d";
			emptyCell = "  .";
		} else if (mCages.size() > 10) {
			cageIdFormat = "%02d";
			emptyCell = " .";
		}
		for (int row = 0; row < gridSizeValue; row++) {
			String line = "      ";
			for (int col = 0; col < gridSizeValue; col++) {
				line += " " + correctValueMatrix.get(row, col);
			}
			line += "   ";
			for (int col = 0; col < gridSizeValue; col++) {
				line += " "
						+ (cageIdMatrix.isEmpty(row, col) ? emptyCell : String
								.format(cageIdFormat,
										cageIdMatrix.get(row, col)));
			}
			debugLog(line);
		}
	}

	interface Listener {
		// Requests the listener whether the generating of this grid has to be
		// cancelled.
		boolean isCancelled();

		// Send a high level progress update to the listener.
		void updateProgressHighLevel(String text);

		// Send a detail level progress update to the listener.
		void updateProgressDetailLevel(String text);

		// Send signal about slow grid generating
		void signalSlowGridGeneration();
	}
}