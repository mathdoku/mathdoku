package net.mathdoku.plus.grid;

import java.util.List;

public class GridCellSelectorInRowOrColumn extends GridCellSelector {
	private int mTargetRow;
	private int mTargetColumn;

	/**
	 * Create a new instance of
	 * {@link net.mathdoku.plus.grid.GridCellSelectorInRowOrColumn}. To be used
	 * to select cells from a list which are either in the given row and or in
	 * given column.
	 * 
	 * @param cells
	 * @param row
	 * @param column
	 */
	public GridCellSelectorInRowOrColumn(List<GridCell> cells, int row,
			int column) {
		super(cells);
		mTargetRow = row;
		mTargetColumn = column;
	}

	@Override
	public boolean select(GridCell gridCell) {
		// Cell must be in the targeted row and or in the targeted column.
		return (gridCell.getRow() == mTargetRow || gridCell.getColumn() == mTargetColumn);
	}
}
