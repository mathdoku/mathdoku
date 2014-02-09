package testHelper;

import net.mathdoku.plus.grid.CellBuilder;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridBuilder;
import net.mathdoku.plus.grid.GridCell;

/**
 * Each Sub class of this class construct a specific a grid object with cells
 * and cages.
 */
public abstract class TestGrid {
	protected final int mGridSize;
	protected final boolean mHideOperator;
	protected final GridBuilder mGridBuilder;
	protected Grid mGrid;

	protected TestGrid(int gridSize, boolean hideOperator) {
		mGridBuilder = new GridBuilder();
		mGridSize = gridSize;
		mHideOperator = hideOperator;
	}

	public TestGrid setEmptyGrid() {
		mGrid = mGridBuilder.build();

		return this;
	}

	protected GridCell createGridCell(int cellNumber, int cellValue, int cageId) {
		return new CellBuilder()
				.setGridSize(mGridSize)
				.setId(cellNumber)
				.setCorrectValue(cellValue)
				.setCageId(cageId)
				.build();
	}

	public Grid getGrid() {
		return mGrid;
	}
}
