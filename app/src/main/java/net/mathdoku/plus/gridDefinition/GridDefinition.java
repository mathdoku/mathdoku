package net.mathdoku.plus.griddefinition;

import com.srlee.dlx.MathDokuDLX;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.GridSize;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridBuilder;
import net.mathdoku.plus.util.Util;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class creates the unique definition for a grid. This definition is used
 * to uniquely identify grid stored in the database of share via url's. Also the
 * class is able to recreate a grid based on the grid definition.
 */
public class GridDefinition {
	private static final String TAG = GridDefinition.class.getName();

	private int mGridSize;
	private List<Cage> mCages;
	private List<Cell> mCells;
	private int[] mCageIdPerCell;
	private int[] mCountCellsPerCage;

	public static class ObjectsCreator {
		public GridGeneratingParameters createGridGeneratingParameters() {
			return new GridGeneratingParameters();
		}

		public MathDokuDLX createMathDokuDLX(int gridSize, List<Cage> cages) {
			return new MathDokuDLX(gridSize, cages);
		}

		public List<Cell> createArrayListOfCells() {
			return new ArrayList<Cell>();
		}

		public List<Cage> createArrayListOfCages() {
			return new ArrayList<Cage>();
		}

		public GridBuilder createGridBuilder() {
			return new GridBuilder();
		}
	}

	private GridDefinition.ObjectsCreator mObjectsCreator;

	public GridDefinition() {
		mObjectsCreator = new GridDefinition.ObjectsCreator();
	}

	public void setObjectsCreator(GridDefinition.ObjectsCreator objectsCreator) {
		if (objectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter objectsCreator cannot be null.");
		}
		mObjectsCreator = objectsCreator;
	}

	/**
	 * Converts the definition of this grid to a string. This definitions only
	 * consists of information needed to rebuild the puzzle. It does not include
	 * information about how it was created or about the current status of
	 * solving. This definition is unique regardless of grid size and or the
	 * version of the grid generator used.
	 * 
	 * @return A unique string representation of the grid.
	 */
	public static String getDefinition(List<Cell> cells, List<Cage> cages,
			GridGeneratingParameters gridGeneratingParameters) {
		StringBuilder definitionString = new StringBuilder();

		if (Util.isListNullOrEmpty(cells)) {
			throw new InvalidParameterException(
					"Parameter cells cannot be null or empty list.");
		}
		if (Util.isListNullOrEmpty(cages)) {
			throw new InvalidParameterException(
					"Parameter cages cannot be null or empty list.");
		}
		if (gridGeneratingParameters == null) {
			throw new InvalidParameterException(
					"Parameter gridGeneratingParameters cannot be null.");
		}

		definitionString.append(
				Integer.toString(gridGeneratingParameters.mPuzzleComplexity
						.getId())).append(GridDefinitionDelimiter.LEVEL1);

		// Get the cage number (represented as a value of two digits, if needed
		// prefixed with a 0) for each cell. Note: with a maximum of 81 cells in
		// a 9x9 grid we can never have a cage-id > 99.
		for (Cell cell : cells) {
			definitionString.append(String.format("%02d", cell.getCageId()));
		}
		// Followed by cages
		for (Cage cage : cages) {
			definitionString
					.append(GridDefinitionDelimiter.LEVEL1)
					.append(cage.getId())
					.append(GridDefinitionDelimiter.LEVEL2)
					.append(cage.getResult())
					.append(GridDefinitionDelimiter.LEVEL2)
					.append(gridGeneratingParameters.mHideOperators ? CageOperator.NONE
							.getId() : cage.getOperator().getId());
		}
		return definitionString.toString();
	}

	/**
	 * Convenience method for getting the definition of a grid.
	 * 
	 * @param grid
	 * @return
	 */
	public static String getDefinition(Grid grid) {
		return getDefinition(grid.getCells(), grid.getCages(),
				grid.getGridGeneratingParameters());
	}

	/**
	 * Create a grid from the given definition string.
	 * 
	 * @return The grid created from the definition. Null in case of an error.
	 */
	public Grid createGrid(String definition) {
		GridDefinitionSplitter gridDefinitionSplitter = new GridDefinitionSplitter(
				definition);

		// The complexity is not needed to rebuild the puzzle, but it is stored
		// as it is a great communicator to the (receiving) user how difficult
		// the puzzle is.
		GridGeneratingParameters mGridGeneratingParameters = mObjectsCreator
				.createGridGeneratingParameters();
		mGridGeneratingParameters.mPuzzleComplexity = gridDefinitionSplitter
				.getPuzzleComplexity();

		int cellCount = gridDefinitionSplitter.getNumberOfCells();
		try {
			mGridSize = GridSize.getFromNumberOfCells(cellCount).getGridSize();
		} catch (IllegalArgumentException e) {
			throw new InvalidGridException(String.format(
					"Definition '%s' contains an invalid number of cells.",
					definition), e);
		}

		// Initialize helper variables. Those variables are filled while adding
		// the cells. Later, when adding the cages, the data of those vars is
		// used.
		mCountCellsPerCage = new int[gridDefinitionSplitter.getNumberOfCages()];
		mCageIdPerCell = gridDefinitionSplitter.getCageIdPerCell();

		// Create the cells based on the list of cage numbers for each cell.
		if (!createArrayListOfCells(gridDefinitionSplitter.getNumberOfCages())) {
			throw new InvalidGridException("Cannot create array list of cells.");
		}

		mCages = mObjectsCreator.createArrayListOfCages();
		for (String cageDefinition : gridDefinitionSplitter
				.getCageDefinitions()) {
			if (!createCage(cageDefinition)) {
				return null;
			}
		}

		// Calculate and set the correct values for each cell if a single
		// solution can be determined for the definition.
		if (!setCorrectCellValues()) {
			throw new InvalidGridException(
					"Cannot set the correct values for all cells.");
		}

		// All data is gathered now
		GridBuilder gridBuilder = mObjectsCreator.createGridBuilder();
		return gridBuilder
				.setGridSize(mGridSize)
				.setCells(mCells)
				.setCages(mCages)
				.setGridGeneratingParameters(mGridGeneratingParameters)
				.build();
	}

	private boolean createArrayListOfCells(int expectedNumberOfCages) {
		mCells = mObjectsCreator.createArrayListOfCells();

		for (int cellNumber = 0; cellNumber < mCageIdPerCell.length; cellNumber++) {
			int cageId = mCageIdPerCell[cellNumber];
			Cell cell = new CellBuilder()
					.setGridSize(mGridSize)
					.setId(cellNumber)
					.setCageId(cageId)
					.setLenientCheckCorrectValueOnBuild()
					.build();
			mCells.add(cell);

			if (cageId < 0 || cageId >= expectedNumberOfCages) {
				throw new InvalidGridException(
						"Cell refers to invalid cage id '" + cageId + "'.");
			}
			mCageIdPerCell[cellNumber] = cageId;
			mCountCellsPerCage[cageId]++;
		}

		return true;
	}

	private boolean createCage(String cageDefinition) {
		CageDefinitionSplitter cageDefinitionSplitter = new CageDefinitionSplitter(
				cageDefinition);

		CageBuilder cageBuilder = new CageBuilder();
		int cageId = cageDefinitionSplitter.getCageId();
		if (cageId < 0 || cageId >= mCountCellsPerCage.length) {
			throw new InvalidGridException(String.format(
					"Cage id '%d' in cage definition '%s' is not valid.",
					cageId, cageDefinition));
		}
		cageBuilder.setId(cageId);
		cageBuilder.setResult(cageDefinitionSplitter.getResult());
		cageBuilder.setCageOperator(cageDefinitionSplitter.getCageOperator());

		int[] cells = new int[mCountCellsPerCage[cageId]];
		int cellIndex = 0;
		for (int i = 0; i < mCountCellsPerCage[cageId]; i++) {
			if (mCageIdPerCell[i] == cageId) {
				cells[cellIndex++] = i;
			}
		}

		cageBuilder.setCells(cells);
		cageBuilder.setHideOperator(false);

		Cage cage = cageBuilder.build();
		mCages.add(cage);

		return true;
	}

	private boolean setCorrectCellValues() {
		// Check whether a single solution can be found.
		int[][] solution = mObjectsCreator
				.createMathDokuDLX(mGridSize, mCages)
				.getSolutionGrid();
		if (solution == null) {
			// Either no or multiple solutions can be found. In both case this
			// would mean that the grid definition string was manipulated by the
			// user.
			throw new InvalidGridException(
					"Grid does not have a unique solution.");
		}
		if (solution.length != mGridSize) {
			throw new InvalidGridException("Solution array has "
					+ solution.length + " rows while " + mGridSize
					+ " row were expected.");
		}
		for (int row = 0; row < mGridSize; row++) {
			if (solution[row].length != mGridSize) {
				throw new InvalidGridException("Solution array has "
						+ (solution == null ? 0 : solution[row].length)
						+ " columns in row " + row + " while " + mGridSize
						+ " columns were expected.");
			}
		}

		// Store the solution in the grid cells.
		for (Cell cell : mCells) {
			int row = cell.getRow();
			int column = cell.getColumn();
			if (row >= 0 && row < solution.length && column >= 0
					&& column < solution[row].length) {
				cell.setCorrectValue(solution[row][column]);
			} else {
				return false;
			}
		}

		return true;
	}
}
