package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.gridgenerating.CellCoordinates.CellCoordinates;

public class CageType {
	@SuppressWarnings("unused")
	private static final String TAG = CageType.class.getName();

	// Smallest possible rectangle of cells, used to define the cage type. True
	// in case the cell is part of the cage type. False in case the cell is
	// unused.
	private final boolean[][] usedCells;

	// Number or cells used in this cage type.
	private final int size;

	// Dimensions of matrix needed to store the cage type shape. These
	// dimensions can easily be derived from the matrix itself but are kept for
	// readability of source code.
	private final int rows;
	private final int columns;

	/**
	 * Creates a new instance of {@link CageType}.
	 */
	public CageType(boolean[][] cageTypeMatrix) {
		validateCageTypeMatrix(cageTypeMatrix);
		usedCells = removeUnusedBorders(cageTypeMatrix);
		rows = usedCells.length;
		columns = usedCells[0].length;
		size = initSize();
	}

	private void validateCageTypeMatrix(boolean[][] cageTypeMatrix) {
		if (cageTypeMatrix == null) {
			throw new IllegalArgumentException("Cage type matrix should not be null.");
		}
		if (hasNoRowsOrColumns(cageTypeMatrix)) {
			throw new IllegalArgumentException("Cage type matrix should not be empty.");
		}
		if (hasRowsOfDifferentLength(cageTypeMatrix)) {
			throw new IllegalArgumentException("All rows in cage type matrix should have same length.");
		}
		if (isEmpty(cageTypeMatrix)) {
			throw new IllegalArgumentException("Cage type matrix can not be empty.");
		}
	}

	private boolean hasNoRowsOrColumns(boolean[][] cageTypeMatrix) {
		return cageTypeMatrix.length == 0 || cageTypeMatrix[0].length == 0;
	}

	private boolean hasRowsOfDifferentLength(boolean[][] cageTypeMatrix) {
		for (int row = 1; row < cageTypeMatrix.length; row++) {
			if (cageTypeMatrix[row].length != cageTypeMatrix[0].length) {
				return true;
			}
		}
		return false;
	}

	private boolean isEmpty(boolean[][] cageTypeMatrix) {
		for (int row = 0; row < cageTypeMatrix.length; row++) {
			for (int col = 0; col < cageTypeMatrix[row].length; col++) {
				if (cageTypeMatrix[row][col]) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean[][] removeUnusedBorders(boolean[][] cageTypeMatrix) {
		int top = getTopRowOfUsedArea(cageTypeMatrix);
		int left = getLeftColumnOfUsedArea(cageTypeMatrix);
		int bottom = getBottomRowOfUsedArea(cageTypeMatrix);
		int right = getRightMostColumnOfUserArea(cageTypeMatrix);

		int height = bottom - top + 1;
		int width = right - left + 1;
		if (height == cageTypeMatrix.length
				&& width == cageTypeMatrix[0].length) {
			return cageTypeMatrix;
		} else {
			boolean[][] strippedCageTypeMatrix = new boolean[height][width];
			for (int row = top; row <= bottom; row++) {
				System.arraycopy(cageTypeMatrix[row], left,
						strippedCageTypeMatrix[row - top], 0, width);
			}
			return strippedCageTypeMatrix;
		}
	}

	private int getTopRowOfUsedArea(boolean[][] cageTypeMatrix) {
		for (int row = 0; row < cageTypeMatrix.length; row++) {
			for (int col = 0; col < cageTypeMatrix[row].length; col++) {
				if (cageTypeMatrix[row][col]) {
					return row;
				}
			}
		}
		throw new IllegalStateException(
				"Cannot determine top row of used area when matrix is empty.");
	}

	private int getLeftColumnOfUsedArea(boolean[][] cageTypeMatrix) {
		for (int col = 0; col < cageTypeMatrix[0].length; col++) {
			for (int row = 0; row < cageTypeMatrix.length; row++) {
				if (cageTypeMatrix[row][col]) {
					return col;
				}
			}
		}
		throw new IllegalStateException(
				"Cannot determine left column of used area when matrix is empty.");
	}

	private int getBottomRowOfUsedArea(boolean[][] cageTypeMatrix) {
		for (int row = cageTypeMatrix.length - 1; row >= 0; row--) {
			for (int col = 0; col < cageTypeMatrix[row].length; col++) {
				if (cageTypeMatrix[row][col]) {
					return row;
				}
			}
		}
		throw new IllegalStateException(
				"Cannot determine bottom row of used area when matrix is empty.");
	}

	private int getRightMostColumnOfUserArea(boolean[][] cageTypeMatrix) {
		for (int col = cageTypeMatrix[0].length - 1; col >= 0; col--) {
			for (int row = 0; row < cageTypeMatrix.length; row++) {
				if (cageTypeMatrix[row][col]) {
					return col;
				}
			}
		}
		throw new IllegalStateException(
				"Cannot determine right column of used area when matrix is empty.");
	}

	private int initSize() {
		int countUsedCells = 0;
		for (int row = 0; row < usedCells.length; row++) {
			for (int col = 0; col < usedCells[row].length; col++) {
				if (usedCells[row][col]) {
					countUsedCells++;
				}
			}
		}
		return countUsedCells;
	}

	/**
	 * Get the size, i.e. the number of cells, used by this cage type.
	 * 
	 * @return The size, i.e. the number of cells, used by this cage type.
	 */
	public int size() {
		return size;
	}

	/**
	 * Get the width of the smallest rectangle in which the cage type fits.
	 * 
	 * @return The width of the smallest rectangle in which the cage type fits.
	 */
	public int getWidth() {
		return columns;
	}

	/**
	 * Get the height of the smallest rectangle in which the cage type fits.
	 * 
	 * @return The height of the smallest rectangle in which the cage type fits.
	 */
	public int getHeight() {
		return rows;
	}

	/**
	 * Get a cage type matrix with a margin of 1 row/column with unused cells
	 * around the entire shape. So a shape with dimensions 2x3 will be extended
	 * to 4x5. The original shape will be shifted by one position to the bottom
	 * and one position to the right.
	 * 
	 * @return An extended cage type matrix with unused cells around the entire
	 *         shape.
	 */
	public boolean[][] getExtendedCageTypeMatrix() {
		boolean[][] extendedUsedCells = new boolean[rows + 2][columns + 2];

		// Shift cage type one row down and 1 column to the right.
		for (int row = 0; row < rows; row++) {
			System.arraycopy(usedCells[row], 0, extendedUsedCells[row + 1], 1, columns);
		}

		return extendedUsedCells;
	}

	/**
	 * Get the cell coordinates if this cage type starts at the given origin.
	 * 
	 * @param originCell
	 *            The cell (0 based) at which the origin cell of the cage type
	 *            will be placed.
	 * @return An array of coordinates (row,col) of cells involved. The caller
	 *         needs to check whether all returned coordinates are valid.
	 */
	public CellCoordinates[] getCellCoordinatesOfAllCellsInCage(
			CellCoordinates originCell) {
		int columnOffset = getColumnOffsetToFirstUsedCellOnTheFirstRow();
		if (canNotBeShiftedToOriginCell(originCell, columnOffset)) {
			return new CellCoordinates[] { CellCoordinates.EMPTY };
		}

		// Calculate coordinates of cells involved in case this cage type starts
		// at the given origin cell.
		CellCoordinates[] cellCoordinates = new CellCoordinates[size];
		int coordinatesIndex = 0;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				if (usedCells[row][col]) {
					cellCoordinates[coordinatesIndex++] = new CellCoordinates(
							originCell.getRow() + row, originCell.getColumn()
									+ col - columnOffset);
				}
			}
		}

		return cellCoordinates;
	}

	private boolean canNotBeShiftedToOriginCell(CellCoordinates originCell,
			int columnOffset) {
		return originCell.getColumn() < columnOffset;
	}

	private int getColumnOffsetToFirstUsedCellOnTheFirstRow() {
		for (int col = 0; col < columns; col++) {
			if (usedCells[0][col]) {
				return col;
			}
		}
		throw new IllegalStateException(
				"Cannot determine offset to first used cell if top row is empty");
	}

	public CellCoordinates getCellCoordinatesTopLeftCell() {
		return new CellCoordinates(0, getColumnOffsetToFirstUsedCellOnTheFirstRow());
	}

	@Override
	public String toString() {
		String result = "";
		for (int row = 0; row < rows; row++) {
			result += "  ";
			for (int col = 0; col < columns; col++) {
				result += usedCells[row][col] ? " X" : " -";
			}
			result += "\n";
		}
		return result;
	}

	@Override
	@SuppressWarnings("all") // Needed to suppress sonar warning on cyclomatic complexity
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CageType)) {
			return false;
		}

		CageType cageType = (CageType) o;

		if (columns != cageType.columns) {
			return false;
		}
		if (rows != cageType.rows) {
			return false;
		}
		if (size != cageType.size) {
			return false;
		}
		return getUsedCellsAsString().equals(cageType.getUsedCellsAsString());

	}

	@Override
	public int hashCode() {
		int result = size;
		result = 31 * result + rows;
		result = 31 * result + columns;
		result = 31 * result + getUsedCellsAsString().hashCode();
		return result;
	}

	private String getUsedCellsAsString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				stringBuilder.append(usedCells[row][col] ? "X" : "-");
			}
		}
		return stringBuilder.toString();
	}
}
