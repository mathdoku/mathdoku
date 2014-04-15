package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridgenerating.GridGeneratingParametersBuilder;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridBuilder;
import net.mathdoku.plus.puzzle.grid.UnexpectedMethodInvocationException;

import java.util.ArrayList;
import java.util.List;

public abstract class GridCreator {
	public static final String FIELD_SEPARATOR_GRID_DEFINITION_PART = ":";
	private final GridBuilder mGridBuilder;
	private Grid mGrid;
	private boolean mSetCorrectEnteredValue;
	private static final int ID_NO_CELL_SELECTED = -1;
	private int mSelectedCellId = ID_NO_CELL_SELECTED;
	public static final long DO_NOT_USE_TO_REGENERATE_GRID = 0;

	protected GridCreator() {
		mGridBuilder = new GridBuilder();
	}

	/**
	 * Set a cell as selected cell. By default no cell is selected.
	 */
	public GridCreator setSelectedCell(int cellId) {
		if (mGrid != null) {
			throw new UnexpectedMethodInvocationException(
					"Method should be called before the grid is build.");
		}
		mSelectedCellId = cellId;

		return this;
	}

	public GridCreator setEmptyGrid() {
		if (mGrid != null) {
			throw new UnexpectedMethodInvocationException(
					"Grid has already been build by other method invocation.");
		}
		mSetCorrectEnteredValue = false;
		createGrid();

		return this;
	}

	public GridCreator setCorrectEnteredValueToAllCells() {
		if (mGrid != null) {
			throw new UnexpectedMethodInvocationException(
					"Grid has already been build by other method invocation.");
		}
		mSetCorrectEnteredValue = true;
		createGrid();

		return this;
	}

	private void createGrid() {
		GridGeneratingParameters gridGeneratingParameters = getGridGeneratingParameters();
		List<Cage> cages = getCages();
		List<Cell> cells = getCells();

		mGridBuilder
				.setGridSize(getGridType().getGridSize())
				.setGridGeneratingParameters(gridGeneratingParameters)
				.setCells(cells)
				.setCages(cages);

		mGrid = mGridBuilder.build();
	}

	public GridGeneratingParameters getGridGeneratingParameters() {
		return new GridGeneratingParametersBuilder()
				.setGridType(getGridType())
				.setHideOperators(getHideOperator())
				.setPuzzleComplexity(getPuzzleComplexity())
				.setGameSeed(getGameSeed())
				.setGeneratorVersionNumber(getGeneratorVersionNumber())
				.setMaxCageResult(getMaxCageResult())
				.setMaxCageSize(getMaxCageSize())
				.createGridGeneratingParameters();
	}

	public List<Cage> getCages() {
		List<Cage> cages = new ArrayList<Cage>();

		for (int cageId = 0; cageId < getResultPerCage().length; cageId++) {
			Cage cage = new CageBuilder()
					.setId(cageId)
					.setHideOperator(getHideOperator())
					.setCells(getCells(cageId))
					.setResult(getResultPerCage()[cageId])
					.setCageOperator(getCageOperatorPerCage()[cageId])
					.build();
			cages.add(cage);
		}

		return cages;
	}

	protected int[] getCells(int cageId) {
		int[] cells = new int[getNumberOfCellsForCage(cageId)];
		int count = 0;
		for (int i = 0; i < getCageIdPerCell().length; i++) {
			if (getCageIdPerCell()[i] == cageId) {
				cells[count++] = i;
			}
		}

		return cells;
	}

	private int getNumberOfCellsForCage(int cageId) {
		int count = 0;
		for (int id : getCageIdPerCell()) {
			if (id == cageId) {
				count++;
			}
		}
		return count;
	}

	public List<Cell> getCells() {
		List<Cell> cells = new ArrayList<Cell>();
		for (int cellNumber = 0; cellNumber < getCorrectValuePerCell().length; cellNumber++) {
			cells.add(createCell(cellNumber,
					getCorrectValuePerCell()[cellNumber],
					getCageIdPerCell()[cellNumber]));
		}

		return cells;
	}

	protected Cell createCell(int cellNumber, int cellValue, int cageId) {
		CellBuilder cellBuilder = new CellBuilder()
				.setGridSize(getGridType().getGridSize())
				.setId(cellNumber)
				.setCorrectValue(cellValue)
				.setCageId(cageId);
		if (mSetCorrectEnteredValue) {
			cellBuilder.setEnteredValue(cellValue);
		}
		if (cellNumber == mSelectedCellId) {
			cellBuilder.setSelected(true);
		}

		return cellBuilder.build();
	}

	public Grid getGrid() {
		if (mGrid == null) {
			throw new UnexpectedMethodInvocationException(
					"Method should not be called before the grid is build with setEmptyGrid or setCorrectEnteredValueToAllCells.");
		}
		return mGrid;
	}

	public int getIdTopLeftCellOfCageContainingCellWithId(int cellId) {
		int cageId = getCageIdPerCell()[cellId];
		return getIdTopLeftCelOfCage(cageId);
	}

	private int getIdTopLeftCelOfCage(int cageId) {
		for (int i = 0; i < getCageIdPerCell().length; i++) {
			if (getCageIdPerCell()[i] == cageId) {
				return i;
			}
		}
		return -1;
	}

	public GridCreator setCorrectEnteredValueInCell(int cellId) {
		Cell cell = mGrid.getCell(cellId);
		int correctValue = cell.getCorrectValue();
		cell.setEnteredValue(correctValue);

		return this;
	}

	public GridCreator setIncorrectEnteredValueInCell(int cellId) {
		Cell cell = mGrid.getCell(cellId);
		int correctValue = cell.getCorrectValue();
		cell.setEnteredValue(correctValue == 1 ? getGridType().getGridSize()
				: 1);

		return this;
	}

	public int getCageIdOfCell(int cellId) {
		return getCageIdPerCell()[cellId];
	}

	public final boolean canBeRegenerated() {
		return getGameSeed() != DO_NOT_USE_TO_REGENERATE_GRID;
	}

	/**
	 * Override in subclass in case the grid can be regenerated using its game
	 * seed.
	 */
	protected long getGameSeed() {
		return DO_NOT_USE_TO_REGENERATE_GRID;
	}

	protected abstract GridType getGridType();

	protected abstract boolean getHideOperator();

	protected abstract PuzzleComplexity getPuzzleComplexity();

	protected abstract int getGeneratorVersionNumber();

	protected abstract int getMaxCageResult();

	protected abstract int getMaxCageSize();

	protected abstract int[] getCorrectValuePerCell();

	protected abstract int[] getCageIdPerCell();

	protected abstract int[] getResultPerCage();

	protected abstract CageOperator[] getCageOperatorPerCage();

	public abstract String getGridDefinition();

	public int getIdOfUpperLeftCellOfCageWithMultipleCellsAndAnUnrevealedCageOperator() {
		for (Cage cage : mGrid.getCages()) {
			if (cage.isOperatorHidden() && cage.getNumberOfCells() > 1) {
				return cage.getIdUpperLeftCell();
			}
		}
		throw new IllegalStateException("This grid has no cage having multiple cells for which the cage operator is not yet unrevealed.");
	}
}
