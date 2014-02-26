package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridBuilder;
import net.mathdoku.plus.puzzle.grid.UnexpectedMethodInvocationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Each Sub class of this class construct a specific a grid object with cells
 * and cages.
 */
public class TestGrid {
	private final boolean mHideOperator;
	private final GridBuilder mGridBuilder;
	private Grid mGrid;
	private boolean mSetCorrectUserValue;
	private static final int ID_NO_CELL_SELECTED = -1;
	private int mSelectedCellId = ID_NO_CELL_SELECTED;

	// The arrays belows define a grid which can be solved with hidden
	// operators. As a result the grid can also use with visible operators.
	private final int mGridSize = 4;
	private int mCorrectValuePerCell[/* cell id */] = {
			// Row 1
			3, 1, 4, 2,
			// Row 2
			4, 3, 2, 1,
			// Row 3
			2, 4, 1, 3,
			// Row 4
			1, 2, 3, 4 };
	private int cageIdPerCell[/* cell id */] = {
			// Row 1
			0, 1, 2, 2,
			// Row 2
			0, 1, 3, 2,
			// Row 3
			4, 1, 3, 5,
			// Row 4
			4, 6, 3, 5 };
	private int resultPerCage[/* cage id */] = {
			// Cage 1
			7,
			// Cage 2
			12,
			// Cage 3
			8,
			// Cage 4
			6,
			// Cage 5
			2,
			// Cage 6
			1,
			// Cage 7
			2 };
	private CageOperator cageOperatorPerCage[/* cage id */] = {
			// Cage 1
			CageOperator.ADD,
			// Cage 2
			CageOperator.MULTIPLY,
			// Cage 3
			CageOperator.MULTIPLY,
			// Cage 4
			CageOperator.ADD,
			// Cage 5
			CageOperator.DIVIDE,
			// Cage 6
			CageOperator.SUBTRACT,
			// Cage 7
			CageOperator.NONE };

	protected TestGrid(boolean hideOperator) {
		mGridBuilder = new GridBuilder();
		mHideOperator = hideOperator;
	}

	public static TestGrid WithVisibleOperators() {
		return new TestGrid(false);
	}

	public static TestGrid WithHiddenOperators() {
		return new TestGridHiddenOperators();
	}

	/**
	 * Set a cell as selected cell. By default no cell is selected.
	 */
	public TestGrid setSelectedCell(int cellId) {
		if (mGrid != null) {
			throw new UnexpectedMethodInvocationException(
					"Method should be called before the grid is build.");
		}
		mSelectedCellId = cellId;

		return this;
	}

	public TestGrid setEmptyGrid() {
		if (mGrid != null) {
			throw new UnexpectedMethodInvocationException(
					"Grid has already been build by other method invocation.");
		}
		mSetCorrectUserValue = false;
		createGrid();

		return this;
	}

	public TestGrid setCorrectUserValueToAllCells() {
		if (mGrid != null) {
			throw new UnexpectedMethodInvocationException(
					"Grid has already been build by other method invocation.");
		}
		mSetCorrectUserValue = true;
		createGrid();

		return this;
	}

	private void createGrid() {
		List<Cage> cages = getCages();
		List<Cell> cells = getCells();
		GridGeneratingParameters gridGeneratingParameters = getGridGeneratingParameters();

		mGridBuilder
				.setGridSize(mGridSize)
				.setGridGeneratingParameters(gridGeneratingParameters)
				.setCells(cells)
				.setCages(cages);

		mGrid = mGridBuilder.build();
	}

	private List<Cage> getCages() {
		List<Cage> cages = new ArrayList<Cage>();

		for (int cageId = 0; cageId < resultPerCage.length; cageId++) {
			Cage cage = new CageBuilder()
					.setId(cageId)
					.setHideOperator(mHideOperator)
					.setCells(getCells(cageId))
					.setResult(resultPerCage[cageId])
					.setCageOperator(cageOperatorPerCage[cageId])
					.build();
			cages.add(cage);
		}

		return cages;
	}

	private int[] getCells(int cageId) {
		int[] cells = new int[getNumberOfCellsForCage(cageId)];
		int count = 0;
		for (int i = 0; i < cageIdPerCell.length; i++) {
			if (cageIdPerCell[i] == cageId) {
				cells[count++] = i;
			}
		}

		return cells;
	}

	private int getNumberOfCellsForCage(int cageId) {
		int count = 0;
		for (int id : cageIdPerCell) {
			if (id == cageId) {
				count++;
			}
		}
		return count;
	}

	protected List<Cell> getCells() {
		List<Cell> cells = new ArrayList<Cell>();
		for (int cellNumber = 0; cellNumber < mCorrectValuePerCell.length; cellNumber++) {
			cells.add(createCell(cellNumber, mCorrectValuePerCell[cellNumber],
								 cageIdPerCell[cellNumber]));
		}

		return cells;
	}

	protected Cell createCell(int cellNumber, int cellValue, int cageId) {
		CellBuilder cellBuilder = new CellBuilder()
				.setGridSize(mGridSize)
				.setId(cellNumber)
				.setCorrectValue(cellValue)
				.setCageId(cageId);
		if (mSetCorrectUserValue) {
			cellBuilder.setUserValue(cellValue);
		}
		if (cellNumber == mSelectedCellId) {
			cellBuilder.setSelected(true);
		}

		return cellBuilder.build();
	}

	private GridGeneratingParameters getGridGeneratingParameters() {
		GridGeneratingParameters gridGeneratingParameters = new GridGeneratingParameters();
		gridGeneratingParameters.mHideOperators = mHideOperator;
		gridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.NORMAL;
		gridGeneratingParameters.mGameSeed = 0;
		gridGeneratingParameters.mGeneratorRevisionNumber = 596;
		gridGeneratingParameters.mMaxCageResult = 999999;
		gridGeneratingParameters.mMaxCageSize = 4;
		return gridGeneratingParameters;
	}

	public Grid getGrid() {
		return mGrid;
	}

	public int getIdTopLeftCellOfCageContainingCellWithId(int cellId) {
		int cageId = cageIdPerCell[cellId];
		return getIdTopLeftCelOfCage(cageId);
	}

	private int getIdTopLeftCelOfCage(int cageId) {
		for (int i = 0; i < cageIdPerCell.length; i++) {
			if (cageIdPerCell[i] == cageId) {
				return i;
			}
		}
		return -1;
	}

	public TestGrid setIncorrectUserValueInCell(int cellId) {
		Cell cell = mGrid.getCell(cellId);
		int correctValue = cell.getCorrectValue();
		cell.setUserValue(correctValue == 1 ? mGridSize : 1);

		return this;
	}

	public int getCageIdOfCell(int cellId) {
		return cageIdPerCell[cellId];
	}
}
