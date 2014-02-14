package net.mathdoku.plus.grid;

import java.util.List;

public class CellSelectorInRowOrColumn extends CellSelector {
	private int mTargetRow;
	private int mTargetColumn;

	/**
	 * Create a new instance of {@link CellSelectorInRowOrColumn}. To be used to
	 * select cells from a list which are either in the given row and or in
	 * given column.
	 * 
	 * @param cells
	 * @param row
	 * @param column
	 */
	public CellSelectorInRowOrColumn(List<Cell> cells, int row, int column) {
		super(cells);
		mTargetRow = row;
		mTargetColumn = column;
	}

	@Override
	public boolean select(Cell cell) {
		// Cell must be in the targeted row and or in the targeted column.
		return (cell.getRow() == mTargetRow || cell.getColumn() == mTargetColumn);
	}
}
