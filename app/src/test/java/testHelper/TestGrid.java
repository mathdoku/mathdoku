package testHelper;

import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridCell;

/**
 * Each Sub class of this class construct a specific a grid object with cells and cages.
 */
public abstract  class TestGrid {
	protected final int mGridSize;
	protected final boolean mHideOperator;

	protected TestGrid(int gridSize, boolean hideOperator) {
		mGridSize = gridSize;
		mHideOperator = hideOperator;
	}

	public abstract Grid createNewGridWithAllEmptyCells();

	protected GridCell createGridCell(int cellNumber, int cellValue,
										   int cageId) {
		GridCell gridCell = new GridCell(cellNumber, mGridSize);
		gridCell.setCorrectValue(cellValue);
		gridCell.setCageId(cageId);
		return gridCell;
	}
}
