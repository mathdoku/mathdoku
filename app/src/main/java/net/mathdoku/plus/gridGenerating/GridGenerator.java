package net.mathdoku.plus.gridgenerating;

import android.util.Log;

import com.srlee.dlx.MathDokuDLX;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.griddefinition.GridDefinition;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
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
	private static final String TAG = GridGenerator.class.getName();

	private static final int CELL_NOT_IN_CAGE = -1;

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	@SuppressWarnings("PointlessBooleanExpression")
	static final boolean DEBUG_GRID_GENERATOR = Config.mAppMode == Config.AppMode.DEVELOPMENT && false;
	@SuppressWarnings("PointlessBooleanExpression")
	public static final boolean DEBUG_GRID_GENERATOR_FULL = DEBUG_GRID_GENERATOR && false;
	private boolean developmentMode = false;

	private GridGeneratingParameters mCurrentGridGeneratingParameters;
	private int mCurrentGridSizeValue;
	private List<Cell> mCells;
	private int[][] mSolutionMatrix;
	private List<Cage> mCages;
	private int[][] mCageMatrix;

	// Random generator
	private Random mRandom;

	private CageTypeGenerator mCageTypeGenerator;

	interface Listener {
		// Requests the listener whether the generating of this grid has to be
		// cancelled.
		boolean isCancelled();

		// Send a high level progress update to the listener.
		void updateProgressHighLevel(String text);

		// Send a detail level progress update to the listener.
		void updateProgressDetailLevel(String text);

		// Send signal about slow grid generating
		void signalSlowGridGeneration(String text);
	}

	Listener listener;

	public GridGenerator(Listener listener) {
		this.listener = listener;
		developmentMode = false;
	}

	public Grid createGrid(GridGeneratingParameters gridGeneratingParameters) {
		mCurrentGridGeneratingParameters = gridGeneratingParameters;
		mCurrentGridSizeValue = mCurrentGridGeneratingParameters
				.getGridType()
				.getGridSize();

		if (DEBUG_GRID_GENERATOR) {
			Log.d(TAG,
					"Game seed: "
							+ mCurrentGridGeneratingParameters.getGameSeed());
		}

		final long mTimeStarted = System.currentTimeMillis();

		// Use the game seed to initialize the randomizer which is used to
		// generate the game. Overwrite this game seed with the fixed value of a
		// given game in case you want to recreate the same grid. All you need
		// to ensure is that you the correct revision of this
		// GridGeneratorAsyncTask
		// module. Please be aware that in case the implementation of the random
		// method changes, it will not be possible to recreate the grids!
		mRandom = new Random(mCurrentGridGeneratingParameters.getGameSeed());

		Grid grid;
		boolean hasUniqueSolution;
		boolean checkUniquenessOfSolution = !developmentMode;
		int attemptsToCreateGrid = 0;
		do {
			hasUniqueSolution = false;
			grid = null;

			// Check whether the generating process should be aborted due to
			// cancellation of the grid dialog.
			if (listener.isCancelled()) {
				return null;
			}

			attemptsToCreateGrid++;
			if (developmentMode) {
				listener.updateProgressHighLevel("attempt"
						+ attemptsToCreateGrid);
			}

			randomiseGrid();

			mCells = new ArrayList<Cell>();
			int cellNumber = 0;
			for (int column = 0; column < mCurrentGridSizeValue; column++) {
				for (int row = 0; row < mCurrentGridSizeValue; row++) {
					Cell cell = new CellBuilder()
							.setGridSize(mCurrentGridSizeValue)
							.setId(cellNumber++)
							.setCorrectValue(mSolutionMatrix[row][column])
							.setSkipCheckCageReferenceOnBuild()
							.build();
					mCells.add(cell);
				}
			}

			// Check whether the generating process should be aborted due to
			// cancellation of the grid dialog.
			if (listener.isCancelled()) {
				return null;
			}

			// Create the cages.
			mCages = new ArrayList<Cage>();
			if (!createCages() && !listener.isCancelled()) {
				// For some reason the creation of the cages was not successful.
				// Start over again.
				continue;
			}

			// Check whether the generating process should be aborted due to
			// cancellation of the grid dialog.
			if (listener.isCancelled()) {
				return null;
			}

			// Create the grid object
			GridBuilder mGridBuilder = new GridBuilder();
			mGridBuilder
					.setGridSize(mCurrentGridSizeValue)
					.setCells(mCells)
					.setCages(mCages)
					.setGridGeneratingParameters(
							mCurrentGridGeneratingParameters);
			grid = mGridBuilder.build();

			// Determine whether grid has a unique solution.
			if (checkUniquenessOfSolution) {
				if (developmentMode) {
					listener
							.updateProgressDetailLevel("Verify unique solution");
				}
				hasUniqueSolution = new MathDokuDLX(mCurrentGridSizeValue,
						grid.getCages()).hasUniqueSolution();
			}

			if (DEBUG_GRID_GENERATOR) {
				if (!checkUniquenessOfSolution) {
					Log
							.d(TAG,
									"The uniqueness of the solution of this grid has not been verified.");
				} else if (hasUniqueSolution) {
					Log.d(TAG, "This grid has a unique solution.");
				} else {
					Log.d(TAG, "This grid does not have a unique solution.");
				}
			}

			if (Config.mAppMode == Config.AppMode.DEVELOPMENT
					&& System.currentTimeMillis() - mTimeStarted > 30 * 1000) {
				// Sometimes grid generation takes too long. Until I have a game
				// seed which reproduces this problem I can not fix it. If such
				// a game is found in development, an exception will be thrown
				// to investigate it.
				final int elapsedTimeInSeconds = (int) ((System
						.currentTimeMillis() - mTimeStarted) / 1000);
				signalSlowGridGeneration(elapsedTimeInSeconds);
				return null;
			}
		} while (!hasUniqueSolution && checkUniquenessOfSolution);
		if (DEBUG_GRID_GENERATOR) {
			Log.d(TAG, String.format("Finished create grid in %d attempts.",
					attemptsToCreateGrid));
		}
		if (grid != null) {
			grid.save();
		}

		return grid;
	}

	private void signalSlowGridGeneration(int elapsedTimeInSeconds) {
		Log
				.i(TAG,
						String
								.format("Game generation takes too long (%d) secs). Please investigate.",
										elapsedTimeInSeconds));
		Log.i(TAG, mCurrentGridGeneratingParameters.toString());
		listener
				.signalSlowGridGeneration("Slow game generation. See LogCat for grid generating parameters which might help "
						+ "to reproduce the problem. Game seed = "
						+ mCurrentGridGeneratingParameters.getGameSeed());
	}

	public Grid createGridInDevelopmentMode(
			GridGeneratingParameters gridGeneratingParameters) {
		developmentMode = true;
		return createGrid(gridGeneratingParameters);
	}

	/*
	 * Fills the grid with random numbers, per therules:
	 * 
	 * - 1 to <row size> on every row and column - No duplicates in any row or
	 * column.
	 */
	private void randomiseGrid() {
		if (developmentMode) {
			listener.updateProgressDetailLevel("Randomise grid.");
		}

		int attempts;
		mSolutionMatrix = new int[this.mCurrentGridSizeValue][this.mCurrentGridSizeValue];
		for (int value = 1; value < this.mCurrentGridSizeValue + 1; value++) {
			for (int row = 0; row < this.mCurrentGridSizeValue; row++) {
				attempts = 20;
				int column;
				while (true) {
					column = this.mRandom.nextInt(this.mCurrentGridSizeValue);
					if (--attempts == 0) {
						// Too many attempts needed to fill grid.
						break;
					}
					if (mSolutionMatrix[row][column] > 0) {
						// Position already occupied.
						continue;
					}
					if (valueInColumn(column, value)) {
						// Value already used in this column.
						continue;
					}
					break;
				}
				if (attempts == 0) {
					// No attempts left to sey the current value in each column.
					// Clear the value from all positions in the solution matrix
					// and try next value.
					clearValue(value--);
					break;
				}
				mSolutionMatrix[row][column] = value;
			}
		}
	}

	/* Determine if the given value is in the given column */
	private boolean valueInColumn(int column, int value) {
		for (int row = 0; row < mCurrentGridSizeValue; row++) {
			if (mSolutionMatrix[row][column] == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Clears the given value from all positions in the solution matrix.
	 */
	private void clearValue(int value) {
		for (int row = 0; row < mCurrentGridSizeValue; row++) {
			for (int column = 0; column < mCurrentGridSizeValue; column++) {
				if (mSolutionMatrix[row][column] == value) {
					mSolutionMatrix[row][column] = 0;
				}
			}
		}
	}

	/**
	 * Creates cages for the current grid which is already filled with numbers.
	 * 
	 * @return True in case the cages have been created successfully.
	 */
	private boolean createCages() {
		if (developmentMode) {
			listener.updateProgressDetailLevel("Create cages.");
		}

		mCageTypeGenerator = CageTypeGenerator.getInstance();

		boolean restart;
		int attempts = 0;
		do {
			restart = false;
			attempts++;
			mCageMatrix = new int[this.mCurrentGridSizeValue][this.mCurrentGridSizeValue];
			for (int row = 0; row < this.mCurrentGridSizeValue; row++) {
				for (int col = 0; col < this.mCurrentGridSizeValue; col++) {
					mCageMatrix[row][col] = CELL_NOT_IN_CAGE;
				}
			}

			if (mCurrentGridGeneratingParameters.getMaxCageSize() >= CageTypeGenerator.MAX_CAGE_SIZE) {
				// Drop a first (bigger) cage type somewhere in the grid.
				int remainingAttemptsToPlaceBigCageType = 10;
				while (remainingAttemptsToPlaceBigCageType > 0) {
					// Check whether the generating process should be aborted
					// due to cancellation of the grid dialog.
					if (listener.isCancelled()) {
						return false;
					}

					CageType cageType = mCageTypeGenerator.getRandomCageType(
							mCurrentGridGeneratingParameters.getMaxCageSize(),
							mCurrentGridSizeValue, mCurrentGridSizeValue,
							mRandom);
					if (cageType != null) {
						// Determine a random row and column at which the mask
						// will be placed. Use +1 in calls to randomizer to
						// prevent exceptions in case the entire height and/or
						// width is needed for the cage type.
						int startRow = mRandom.nextInt(mCurrentGridSizeValue
								- cageType.getHeight() + 1);
						int startCol = mRandom.nextInt(mCurrentGridSizeValue
								- cageType.getWidth() + 1);
						CellCoordinates randomStartCellCoordinates = new CellCoordinates(
								startRow, startCol);

						// Determine the origin cell of the cage type in case
						// the cage type mask is put at the randomly determined
						// position.
						CellCoordinates coordinatesTopLeft = cageType
								.getOriginCoordinates(randomStartCellCoordinates);

						// Get coordinates of all cells in the cage cells and
						// add the cage.
						// Note: no checking is done on the maximum permutations
						// for the first cage.
						List<Cell> cells = getAllCells(cageType
								.getCellCoordinatesOfAllCellsInCage(coordinatesTopLeft));
						Cage firstCage = createCage(cells,
								4 * mCurrentGridGeneratingParameters
										.getMaxCagePermutations());
						if (firstCage != null) {
							mCages.add(firstCage);
							break;
						}
					}

					// Try another time to drop a big cage type unless maximum
					// number of tries has been reached.
					remainingAttemptsToPlaceBigCageType--;
				}
			}

			// Fill remainder of grid
			int countSingles = 0;
			CellCoordinates coordinatesCellNotInAnyCage = getCoordinatesOfNextCellNotInAnyCage();
			while (coordinatesCellNotInAnyCage != null) {
				// Determine a random cage which will start at this cell.
				Cage cage = selectRandomCageType(coordinatesCellNotInAnyCage);
				if (cage.getNumberOfCells() == 1) {
					countSingles++;
					if (countSingles > mCurrentGridGeneratingParameters
							.getMaximumSingleCellCages()) {
						if (developmentMode) {
							listener.updateProgressDetailLevel(String.format(
									"Found more single cell cages than allowed (%d) in attempt"
											+ " " + "%d.",
									mCurrentGridGeneratingParameters
											.getMaximumSingleCellCages(),
									attempts));
						}
						restart = true;
						break;
					}
				}

				// Add the cage to the grid
				this.mCages.add(cage);

				coordinatesCellNotInAnyCage = getCoordinatesOfNextCellNotInAnyCage();
			}

			// A valid grid has been created. Check if grid was never created
			// before.
			if (!restart) {
				String gridDefinition = GridDefinition.getDefinition(mCells,
						mCages, mCurrentGridGeneratingParameters);
				if (new GridDatabaseAdapter()
						.getByGridDefinition(gridDefinition) != null) {
					// The exact same grid has been created before. Create
					// another grid.
					restart = true;
					if (developmentMode) {
						listener
								.updateProgressDetailLevel("Grid has been generated before.");
					}
					continue;
				}
			}

			// If not succeeded in 20 attempts then stop and try all over again.
			if (attempts >= 20) {
				return false;
			}
		} while (restart);

		if (DEBUG_GRID_GENERATOR) {
			printCageCreationDebugInformation(null);
		}

		return true;
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
		if (row < 0 || row >= mCurrentGridSizeValue) {
			return -1;
		}
		if (column < 0 || column >= mCurrentGridSizeValue) {
			return -1;
		}
		return row * mCurrentGridSizeValue + column;
	}

	/**
	 * Create the cage at the given coordinates.
	 * 
	 * @param cells
	 *            The cells to be used for the cage.
	 * @param maxPermutations
	 *            The maximum permutations allowed to create the cage. Use 0 in
	 *            case no checking on the number of permutations needs to be
	 *            done.
	 * @return The grid cage which is created. Null in case the cage has too
	 *         many permutations.
	 */
	private Cage createCage(List<Cell> cells, int maxPermutations) {
		CageBuilder cageBuilder = new CageBuilder();
		int newCageId = mCages.size();
		cageBuilder.setId(newCageId);

		cageBuilder.setCells(getAllCellIds(cells));

		CageOperator cageOperator = generateCageOperator(getAllCorrectValues(cells));
		cageBuilder.setCageOperator(cageOperator);
		cageBuilder.setHideOperator(mCurrentGridGeneratingParameters
				.isHideOperators());
		cageBuilder.setResult(getCageResult(getAllCorrectValues(cells),
				cageOperator));

		// All data is gathered which is needed to build the cage.
		Cage cage = cageBuilder.build();

		// Finally check whether the number of permutations of possible
		// solutions for the cage is not to big.
		ComboGenerator comboGenerator = new ComboGenerator(
				mCurrentGridSizeValue);
		List<int[]> possibleCombos = comboGenerator.getPossibleCombos(cage,
				cells);
		if (maxPermutations > 0 && possibleCombos.size() > maxPermutations) {
			// This cage has too many permutations which fulfill the
			// cage requirements. As this reduces the chance to find a
			// solution for the puzzle too much, the cage type will not
			// returned.
			if (DEBUG_GRID_GENERATOR_FULL) {
				Log.d(TAG, "This cage type has been rejected as it has more "
						+ "than " + maxPermutations + " initial "
						+ "permutations which fulfill the cage requirement.");
			}
			return null;
		}
		cage.setPossibleCombos(possibleCombos);

		// Set cage id in all cells used by this cage.
		for (Cell cell : cells) {
			cell.setCageId(newCageId);
		}

		// Update the cage matrix
		for (Cell cell : cells) {
			mCageMatrix[cell.getRow()][cell.getColumn()] = newCageId;
		}

		return cage;
	}

	private int[] getAllCellIds(List<Cell> cells) {
		int[] ids = new int[cells.size()];
		int index = 0;
		for (Cell cell : cells) {
			ids[index++] = cell.getCellId();
		}
		return ids;
	}

	/**
	 * Generates the operator to be used for the cage, semi-randomly.
	 * 
	 * @param cellValues
	 *            An array contain the correct value of each cell in the cage
	 */
	private CageOperator generateCageOperator(int[] cellValues) {
		// A cage consisting of one single cell has no operator.
		if (cellValues.length == 1) {
			return CageOperator.NONE;
		}

		// For cages of size 2 and bigger a weight (i.e. the chance on choosing
		// that operator) will be determined.
		int divisionWeight;
		int subtractionWeight;
		int addWeight;
		int multiplyWeight;

		if (cellValues.length == 2) {
			// A cage consisting of two cells can have any operator. Divide and
			// subtraction get extra weight because those operators are not used
			// in bigger cages.
			// As division can only be used if the remainder after division
			// equals 0 it gets even more weight.
			int lower = Math.min(cellValues[0], cellValues[1]);
			int higher = Math.max(cellValues[0], cellValues[1]);
			divisionWeight = higher % lower == 0 ? 50 : 0;
			subtractionWeight = 30;
			addWeight = 15;
			multiplyWeight = 15;
		} else {
			// Cage has three or more cells. Division and subtraction are not
			// allowed as operators.
			divisionWeight = 0;
			subtractionWeight = 0;
			addWeight = 50;
			multiplyWeight = 50;
		}

		// Determine a random number in the range of the total weight of the
		// operator available.
		int totalWeight = divisionWeight + subtractionWeight + addWeight
				+ multiplyWeight;
		double index = mRandom.nextInt(totalWeight);

		// Check whether the division operator has to be applied
		if (index < divisionWeight) {
			return CageOperator.DIVIDE;
		}
		index -= divisionWeight;

		// Check whether the subtraction operator has to be applied
		if (index < subtractionWeight) {
			return CageOperator.SUBTRACT;
		}
		index -= subtractionWeight;

		// Check whether the multiply operator has to be applied. If it value is
		// greater than the maximum cage result, the add operator will be used
		// instead.
		if (index < multiplyWeight) {
			int cageResultMultiplication = getCageResult(cellValues,
					CageOperator.MULTIPLY);
			if (cageResultMultiplication <= mCurrentGridGeneratingParameters
					.getMaxCageResult()) {
				return CageOperator.MULTIPLY;
			}
			// Multiplication leads to a cage value that is too big to be
			// displayed on this device. Instead of multiplication the add
			// operator will be used for this cage which leads to a small cage
			// outcome.
			Log.d(TAG, String.format("GameSeed: %d cage result %d is rejected",
					mCurrentGridGeneratingParameters.getGameSeed(),
					cageResultMultiplication));
		}

		// Use ADD in all other cases.
		return CageOperator.ADD;
	}

	private int getCageResult(int[] cellValues, CageOperator cageOperator) {
		if (cageOperator == CageOperator.NONE && cellValues.length == 1) {
			return cellValues[0];
		}

		if (cageOperator == CageOperator.DIVIDE && cellValues.length == 2) {
			int lower = Math.min(cellValues[0], cellValues[1]);
			int higher = Math.max(cellValues[0], cellValues[1]);
			return higher / lower;
		}

		if (cageOperator == CageOperator.SUBTRACT && cellValues.length == 2) {
			int lower = Math.min(cellValues[0], cellValues[1]);
			int higher = Math.max(cellValues[0], cellValues[1]);
			return higher - lower;
		}

		if (cageOperator == CageOperator.ADD && cellValues.length >= 2) {
			int total = 0;
			for (int cellValue : cellValues) {
				total += cellValue;
			}
			return total;
		}

		if (cageOperator == CageOperator.MULTIPLY && cellValues.length >= 2) {
			int total = 1;
			for (int cellValue : cellValues) {
				total *= cellValue;
			}
			return total;
		}

		// No valid result was calculated.
		return -1;
	}

	private int[] getAllCorrectValues(List<Cell> cells) {
		int[] correctValues = new int[cells.size()];
		int index = 0;
		for (Cell cell : cells) {
			correctValues[index++] = cell.getCorrectValue();
		}
		return correctValues;
	}

	/**
	 * Get the coordinates of the next cell which is not yet contained in a
	 * cage.
	 * 
	 * @return The coordinates of the next cell which is not yet contained in a
	 *         cage. Null in case all cells are contained in a cage.
	 */
	private CellCoordinates getCoordinatesOfNextCellNotInAnyCage() {
		for (int row = 0; row < mCurrentGridSizeValue; row++) {
			for (int column = 0; column < mCurrentGridSizeValue; column++) {
				if (mCageMatrix[row][column] == CELL_NOT_IN_CAGE) {
					return new CellCoordinates(row, column);
				}
			}
		}
		return null;
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
	private Cage selectRandomCageType(CellCoordinates originCell) {
		if (DEBUG_GRID_GENERATOR_FULL) {
			Log
					.d(TAG,
							"Determine valid cages for "
									+ originCell.toCellString());
		}

		// Store indexes of all defined cages types, except cage type 0 which is
		// a single cell, in a temporary list of available cages.
		List<Integer> availableCages = new ArrayList<Integer>();
		for (int i = 1; i < mCageTypeGenerator
				.size(mCurrentGridGeneratingParameters.getMaxCageSize()); i++) {
			availableCages.add(i);
		}

		CageType selectedCageType;
		do {
			// Check whether the generating process should be aborted
			// due to cancellation of the grid dialog.
			if (listener.isCancelled()) {
				return null;
			}

			// Randomly select any cage from the list of available cages. As
			// soon as a cage type is selected, it is removed from the list of
			// available cage types so it will not be selected again.
			int randomIndex = this.mRandom.nextInt(availableCages.size());
			int cageTypeToBeChecked = availableCages.get(randomIndex);
			availableCages.remove(randomIndex);
			selectedCageType = mCageTypeGenerator
					.getCageType(cageTypeToBeChecked);

			// Get coordinates of all cells involved when this cage type is
			// placed at this origin.
			CellCoordinates[] cellCoordinatesOfAllCellsInCage = selectedCageType
					.getCellCoordinatesOfAllCellsInCage(originCell);

			// Build mask for this cage
			boolean[][] maskNewCage = new boolean[this.mCurrentGridSizeValue][this.mCurrentGridSizeValue];
			int[] maskNewCageRowCount = new int[this.mCurrentGridSizeValue];
			int[] maskNewCageColCount = new int[this.mCurrentGridSizeValue];
			boolean cageIsValid = true;
			for (CellCoordinates cellCoordinates : cellCoordinatesOfAllCellsInCage) {
				if (cellCoordinates.isInvalidForGridSize(mCurrentGridSizeValue)) {
					// Coordinates of this cell in cage falls outside the
					// grid.
					cageIsValid = false;
					break;
				} else if (mCageMatrix[cellCoordinates.getRow()][cellCoordinates
						.getColumn()] >= 0) {
					// Cell is already used in another cage
					cageIsValid = false;
					break;
				} else {
					// Cell can be used for this new cage.
					maskNewCage[cellCoordinates.getRow()][cellCoordinates
							.getColumn()] = true;
					maskNewCageRowCount[cellCoordinates.getRow()]++;
					maskNewCageColCount[cellCoordinates.getColumn()]++;
				}
			}
			if (!cageIsValid) {
				continue;
			}

			if (DEBUG_GRID_GENERATOR_FULL) {
				// Print solution, cage matrix and maskNewCage
				printCageCreationDebugInformation(maskNewCage);
			}

			// Check next cage in case an overlapping subset is found in the
			// columns
			if (hasOverlappingSubsetOfValuesInColumns(maskNewCage,
					maskNewCageColCount)) {
				continue;
			}

			// Check next cage in case an overlapping subset is found in the
			// rows
			if (hasOverlappingSubsetOfValuesInRows(maskNewCage,
					maskNewCageRowCount)) {
				continue;
			}

			// So far the cage is still valid
			List<Cell> cells = getAllCells(cellCoordinatesOfAllCellsInCage);
			Cage cage = createCage(cells,
					mCurrentGridGeneratingParameters.getMaxCagePermutations());
			if (cage != null) {
				// As we randomly check available cages, we can stop as soon as
				// a valid cage is found which does fit on this position.
				return cage;
			}

			// No cage created due to too many permutations. Check next cage.
		} while (!availableCages.isEmpty());

		// No cage, other than a single cell, does fit on this position in the
		// grid.
		if (DEBUG_GRID_GENERATOR_FULL) {
			// Print solution, cage matrix and maskNewCage
			boolean[][] maskNewCage = new boolean[this.mCurrentGridSizeValue][this.mCurrentGridSizeValue];
			maskNewCage[originCell.getRow()][originCell.getColumn()] = true;
			printCageCreationDebugInformation(maskNewCage);
		}

		// Create the new cage for a single cell cage.
		List<Cell> cells = getAllCells(new CellCoordinates[] { originCell });
		return createCage(cells, 0);
	}

	/**
	 * Print debug information for create cage process to logging.
	 * 
	 * @param maskNewCage
	 *            Mask of cage type which is currently processed.
	 */
	private void printCageCreationDebugInformation(boolean[][] maskNewCage) {
		Log.d(TAG, "   Checking cage type");
		String cageIdFormat = "%d";
		String emptyCell = ".";
		String usedCell = "X";
		if (this.mCages.size() > 100) {
			cageIdFormat = "%03d";
			emptyCell = "  .";
			usedCell = "  X";
		} else if (this.mCages.size() > 10) {
			cageIdFormat = "%02d";
			emptyCell = " .";
			usedCell = " X";
		}
		for (int row = 0; row < this.mCurrentGridSizeValue; row++) {
			String line = "      ";
			for (int col = 0; col < this.mCurrentGridSizeValue; col++) {
				line += " " + mSolutionMatrix[row][col];
			}
			line += "   ";
			for (int col = 0; col < this.mCurrentGridSizeValue; col++) {
				line += " "
						+ (mCageMatrix[row][col] == CELL_NOT_IN_CAGE ? emptyCell
								: String.format(cageIdFormat,
										mCageMatrix[row][col]));
			}
			if (maskNewCage != null) {
				line += "   ";
				for (int col = 0; col < this.mCurrentGridSizeValue; col++) {
					line += " "
							+ (maskNewCage[row][col] ? usedCell : emptyCell);
				}
			}
			Log.d(TAG, line);
		}
	}

	/**
	 * Determines whether the given new cage contains a subset of values in its
	 * columns which is also used in the columns of another cage.
	 * 
	 * @param maskNewCage
	 *            A mask of the new cage. Cells which are in use by this cage
	 *            have value 1. Cells not used have a value -1.
	 * @param maskNewCageColCount
	 *            The number of rows per column in use by this new cage.
	 * @return True in case an overlapping subset of values is found. False
	 *         otherwise.
	 */
	private boolean hasOverlappingSubsetOfValuesInColumns(
			boolean[][] maskNewCage, int[] maskNewCageColCount) {
		for (int newCageCol = 0; newCageCol < this.mCurrentGridSizeValue; newCageCol++) {
			if (maskNewCageColCount[newCageCol] > 1) {
				// This column in the new cage has more than one row and
				// therefore needs to be checked with columns of other cages.

				// Compare the column in which the new cage is placed with cages
				// in other columns of the grid.
				for (int col = 0; col < this.mCurrentGridSizeValue; col++) {
					if (col != newCageCol) {

						// Cages which are already checked during processing of
						// this column of the new cage, can be skipped.
						List<Integer> cagesChecked = new ArrayList<Integer>();

						// Iterate all cells in the column from top to bottom.
						for (int row = 0; row < this.mCurrentGridSizeValue; row++) {
							int otherCageId = mCageMatrix[row][col];
							if (otherCageId >= 0
									&& maskNewCage[row][newCageCol]
									&& !cagesChecked.contains(otherCageId)) {
								// Cell[row][col] is used in a cage which is not
								// yet checked. This is the first row for which
								// the new cage and the other cage has a cell in
								// the columns which are compared.
								cagesChecked.add(otherCageId);

								// Check all remaining rows if the checked
								// columns contain a cell for the new cage and
								// the other cage.
								int[] valuesUsed = new int[this.mCurrentGridSizeValue];
								for (int row2 = row; row2 < this.mCurrentGridSizeValue; row2++) {
									if (mCageMatrix[row2][col] == otherCageId
											&& maskNewCage[row2][newCageCol]) {
										// Both cages contain a cell on the same
										// row. Remember values used in those
										// cells.
										valuesUsed[mSolutionMatrix[row2][col] - 1]++;
										valuesUsed[mSolutionMatrix[row2][newCageCol] - 1]++;
									}
								}

								// Determine which values are used in both cages
								List<Integer> duplicateValues = new ArrayList<Integer>();
								for (int i = 0; i < this.mCurrentGridSizeValue; i++) {
									if (valuesUsed[i] > 1) {
										// Value (i+1) has been used in both
										// columns of the new cage and the other
										// cage.
										duplicateValues.add(i + 1);
									}
								}
								if (duplicateValues.size() > 1) {
									// At least two values have been found which
									// are used in both columns of the new cage
									// and the other cage. As this would result
									// in a non-unique solution, the cage is not
									// valid.
									if (DEBUG_GRID_GENERATOR_FULL) {
										Log
												.i(TAG,
														String
																.format("         This cage type "
																		+ "will result in a "
																		+ ""
																		+ "non-unique "
																		+ "solution. The new"
																		+ " cage "
																		+ "contains values "
																		+ "%s "
																		+ " in "
																		+ "column %d "
																		+ " "
																		+ "which are also "
																		+ "used in "
																		+ "column %d "
																		+ " "
																		+ "within cage %d.",
																		duplicateValues
																				.toString(),
																		newCageCol,
																		col,
																		otherCageId));
									}
									return true;
								}
							}
						}
					}
				}
			}
		}

		// No overlapping subset found
		return false;
	}

	/**
	 * Determines whether the given new cage contains a subset of values in its
	 * rows which is also used in the rows of another cage.
	 * 
	 * @param maskNewCage
	 *            A mask of the new cage. Cells which are in use by this cage
	 *            have value 1. Cells not used have a value -1.
	 * @param maskNewCageRowCount
	 *            The number of columns per row in use by this new cage.
	 * @return True in case an overlapping subset of values is found. False
	 *         otherwise.
	 */
	private boolean hasOverlappingSubsetOfValuesInRows(boolean[][] maskNewCage,
			int[] maskNewCageRowCount) {
		for (int newCageRow = 0; newCageRow < this.mCurrentGridSizeValue; newCageRow++) {
			if (maskNewCageRowCount[newCageRow] > 1) {
				// This row in the new cage has more than one column and
				// therefore needs to be checked with rows of other cages.

				// Compare the row in which the new cage is placed with cages
				// in other rows of the grid.
				for (int row = 0; row < this.mCurrentGridSizeValue; row++) {
					if (row != newCageRow) {

						// Cages which are already checked during processing of
						// this row of the new cage, can be skipped.
						List<Integer> cagesChecked = new ArrayList<Integer>();

						// Iterate all cells in the row from left to right.
						for (int col = 0; col < this.mCurrentGridSizeValue; col++) {
							int otherCageId = mCageMatrix[row][col];
							if (otherCageId >= 0
									&& maskNewCage[newCageRow][col]
									&& !cagesChecked.contains(otherCageId)) {
								// Cell[row][col] is used in a cage which is not
								// yet checked. This is the first column for
								// which
								// the new cage and the other cage has a cell in
								// the rows which are compared.
								cagesChecked.add(otherCageId);

								// Check all remaining columns if the checked
								// rows contain a cell for the new cage and
								// the other cage.
								int[] valuesUsed = new int[this.mCurrentGridSizeValue];
								for (int cols2 = col; cols2 < this.mCurrentGridSizeValue; cols2++) {
									if (mCageMatrix[row][cols2] == otherCageId
											&& maskNewCage[newCageRow][cols2]) {
										// Both cages contain a cell on the same
										// columns. Remember values used in
										// those
										// cells.
										valuesUsed[mSolutionMatrix[row][cols2] - 1]++;
										valuesUsed[mSolutionMatrix[newCageRow][cols2] - 1]++;
									}
								}

								// Determine which values are used in both cages
								List<Integer> duplicateValues = new ArrayList<Integer>();
								for (int i = 0; i < this.mCurrentGridSizeValue; i++) {
									if (valuesUsed[i] > 1) {
										// Value (i+1) has been used in both
										// columns of the new cage and the other
										// cage.
										duplicateValues.add(i + 1);
									}
								}
								if (duplicateValues.size() > 1) {
									// At least two values have been found which
									// are used in both rows of the new cage
									// and the other cage. As this would result
									// in a non-unique solution, the cage is not
									// valid.
									if (DEBUG_GRID_GENERATOR_FULL) {
										Log
												.i(TAG,
														String
																.format("         This cage type "
																		+ "will result in a "
																		+ "non-unique "
																		+ "solution. The "
																		+ "new"
																		+ " cage "
																		+ "contains values "
																		+ "%s "
																		+ " in row "
																		+ "%d "
																		+ " which "
																		+ "are also "
																		+ "used in "
																		+ "row "
																		+ "%d "
																		+ " within "
																		+ "cage %d.",
																		duplicateValues
																				.toString(),
																		newCageRow,
																		row,
																		otherCageId));
									}
									return true;
								}
							}
						}
					}
				}
			}
		}

		// No overlapping subset found
		return false;
	}

}
