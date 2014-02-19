package testHelper;

import net.mathdoku.plus.grid.Cell;
import net.mathdoku.plus.grid.CellBuilder;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridBuilder;
import net.mathdoku.plus.grid.UnexpectedMethodInvocationException;

/**
 * Each Sub class of this class construct a specific a grid object with cells
 * and cages.
 */
public abstract class TestGrid {
	protected final int mGridSize;
	protected final boolean mHideOperator;
	protected final GridBuilder mGridBuilder;
	protected Grid mGrid;
	private boolean mSetCorrectUserValue;

	protected TestGrid(int gridSize, boolean hideOperator) {
		mGridBuilder = new GridBuilder();
		mGridSize = gridSize;
		mHideOperator = hideOperator;
	}

	protected abstract void createTestGrid();

	public TestGrid setEmptyGrid() {
		if (mGrid != null) {
			throw new UnexpectedMethodInvocationException("Grid has already been build by other method invocation.");
		}
		mSetCorrectUserValue = true;
		createTestGrid();
		mGrid = mGridBuilder.build();

		return this;
	}

	public TestGrid setCorrectUserValueToAllCells() {
		if (mGrid != null) {
			throw new UnexpectedMethodInvocationException("Grid has already been build by other method invocation.");
		}
		mSetCorrectUserValue = true;
		createTestGrid();
		mGrid = mGridBuilder.build();

		return this;
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
		return cellBuilder.build();
	}


	public Grid getGrid() {
		return mGrid;
	}
}
