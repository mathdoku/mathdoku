package net.mathdoku.plus.gridgenerating;

public class CellCoordinates {
	private final int row;
	private final int column;

	CellCoordinates(int row, int column) {
		this.row = row;
		this.column = column;
	}

	boolean isInvalidForGridSize(int gridSize) {
		return isInvalidIndexForGridSize(row, gridSize) || isInvalidIndexForGridSize(column, gridSize);
	}

	private boolean isInvalidIndexForGridSize(int index, int gridSize) {
		return index < 0 || index >= gridSize;
	}

	public String toCellString() {
		return String.format("cell[%d,%d]", row, column);
	}

	int getRow() {
		return row;
	}

	int getColumn() {
		return column;
	}
}
