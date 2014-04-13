package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.gridgenerating.CellCoordinates.CellCoordinates;

public class CageType {
	@SuppressWarnings("unused")
	private static final String TAG = CageType.class.getName();

	// Number or cells used in this cage type.
	private int mSize;

	// Dimensions of matrix needed to store the cage type shape.
	private int mRows;
	private int mCols;

	// Each cage type has an origin cell which is defined as the left most
	// occupied cell in the top row of the shape. As empty top rows can not
	// occur by default, there is no rowOriginOffset.
	private int mColOriginOffset;

	// Matrix to store the cage type. True in case the cell is part of the cage
	// type. False in case the cell is unused.
	private boolean[][] mUsedCells;

	/**
	 * Creates a new instance of {@link CageType}.
	 */
	public CageType() {
	}

	/**
	 * Get the size, i.e. the number of cells, used by this cage type.
	 * 
	 * @return The size, i.e. the number of cells, used by this cage type.
	 */
	public int cellsUsed() {
		return mSize;
	}

	/**
	 * Get the (maximum) width in number of cells of the cage type.
	 * 
	 * @return The (maximum) width in number of cells of the cage type.
	 */
	public int getWidth() {
		return mCols;
	}

	/**
	 * Get the (maximum) height in number of cells of the cage type.
	 * 
	 * @return The (maximum) height in number of cells of the cage type.
	 */
	public int getHeight() {
		return mRows;
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
		boolean[][] extendedUsedCells = new boolean[mRows + 2][mCols + 2];

		// Shift cage type one row down and 1 column to the right.
		for (int row = 0; row < mRows; row++) {
			System.arraycopy(mUsedCells[row], 0, extendedUsedCells[row + 1], 1,
					mCols);
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
		if (canNotBeCreatedAtOrigin(originCell)) {
			return new CellCoordinates[] { CellCoordinates.EMPTY };
		}

		// Get cage type matrix. If not defined, return the given origin cell as
		// a single cell cage.
		if (mUsedCells == null) {
			return new CellCoordinates[] { originCell };
		}

		// Calculate coordinates of cells involved in case this cage type starts
		// at the given origin cell.
		CellCoordinates[] cellCoordinates = new CellCoordinates[mSize];
		int coordinatesIndex = 0;
		for (int row = 0; row < mRows; row++) {
			for (int col = 0; col < mCols; col++) {
				if (mUsedCells[row][col]) {
					cellCoordinates[coordinatesIndex++] = new CellCoordinates(
							originCell.getRow() + row, originCell.getColumn()
									+ col - mColOriginOffset);
				}
			}
		}

		return cellCoordinates;
	}

	private boolean canNotBeCreatedAtOrigin(
			CellCoordinates originCellCoordinates) {
		return (originCellCoordinates.getColumn() < mColOriginOffset);
	}

	/**
	 * Return the coordinates of the origin cell within the cage type in case
	 * the top left cell of the cage type mask is placed at given offsets.
	 * 
	 * @return The coordinates of the most top left cell used in the cage type.
	 */
	public CellCoordinates getOriginCoordinates(CellCoordinates offset) {
		return new CellCoordinates(offset.getRow(), offset.getColumn()
				+ mColOriginOffset);
	}

	/**
	 * Sets a cage type matrix. Empty rows and columns will be removed from the
	 * given matrix.
	 * 
	 * @param newCageTypeMatrix
	 *            The matrix defining the cage type. Used cells have value true.
	 *            Unused cells have value false.
	 */
	public void setMatrix(boolean[][] newCageTypeMatrix) {
		// Determine top-left and bottom-right coordinates of the rectangle in
		// which the shape is placed.
		int top = Integer.MAX_VALUE;
		int bottom = -1;
		int left = Integer.MAX_VALUE;
		int right = -1;
		for (int row = 0; row < newCageTypeMatrix.length; row++) {
			for (int col = 0; col < newCageTypeMatrix[row].length; col++) {
				if (newCageTypeMatrix[row][col]) {
					if (row < top) {
						top = row;
					}
					if (col < left) {
						left = col;
					}
					if (row > bottom) {
						bottom = row;
					}
					if (col > right) {
						right = col;
					}
				}
			}
		}

		// Create a new cage type matrix by stripping all unused rows and
		// columns
		this.mRows = bottom - top + 1;
		this.mCols = right - left + 1;
		this.mUsedCells = new boolean[this.mRows][this.mCols];
		boolean originFound = false;
		this.mSize = 0;
		for (int row = top; row <= bottom; row++) {
			for (int col = left; col <= right; col++) {
				this.mUsedCells[row - top][col - left] = newCageTypeMatrix[row][col];
				if (newCageTypeMatrix[row][col]) {
					this.mSize++;
					if (!originFound) {
						originFound = true;
						this.mColOriginOffset = col - left;
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "";
		for (int row = 0; row < mRows; row++) {
			result += "  ";
			for (int col = 0; col < mCols; col++) {
				result += mUsedCells[row][col] ? " X" : " -";
			}
			result += "\n";
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CageType)) {
			return false;
		}

		CageType cageType = (CageType) o;

		if (mColOriginOffset != cageType.mColOriginOffset) {
			return false;
		}
		if (mCols != cageType.mCols) {
			return false;
		}
		if (mRows != cageType.mRows) {
			return false;
		}
		if (mSize != cageType.mSize) {
			return false;
		}
		return getUsedCellsAsString().equals(cageType.getUsedCellsAsString());

	}

	@Override
	public int hashCode() {
		int result = mSize;
		result = 31 * result + mRows;
		result = 31 * result + mCols;
		result = 31 * result + mColOriginOffset;
		result = 31 * result + getUsedCellsAsString().hashCode();
		return result;
	}

	private String getUsedCellsAsString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int row = 0; row < mRows; row++) {
			for (int col = 0; col < mCols; col++) {
				stringBuilder.append(mUsedCells[row][col] ? "X" : "-");
			}
		}
		return stringBuilder.toString();
	}
}
