package net.cactii.mathdoku;


public class GridCageType {
	public static final String TAG = "MathDoku.GridCageType";

	// Number or cells used in this cage type.
	private int mSize;

	// Dimensions of matrix needed to store the cage type shape.
	private int rows;
	private int cols;

	// Each cage type has an origin cell which is defined as the left most
	// occupied cell in the top row of the shape. As empty top rows can not
	// occur by default, there is no rowOriginOffset.
	private int colOriginOffset;

	// Matrix to store the cage type. True in case the cell is part of the cage
	// type. False in case the cell is unused.
	public boolean[][] usedCells;

	/**
	 * Creates a new instance of {@link CageType}.
	 */
	public GridCageType() {
	}

	/**
	 * Get the size, i.e. the number of cells, used by this cage type.
	 * 
	 * @return The size, i.e. the number of cells, used by this cage type.
	 */
	public int size() {
		return mSize;
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
		boolean[][] extendedUsedCells = new boolean[rows + 2][cols + 2];

		// Shift cage type one row down and 1 column to the right.
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				extendedUsedCells[row + 1][col + 1] = usedCells[row][col];
			}
		}

		return extendedUsedCells;
	}

	/**
	 * Get the cell coordinates if this cage type starts at the given origin.
	 * 
	 * @param origin
	 *            The origin cell where the cage type does start.
	 * @return An array of coordinates (row,col) of cells involved. The caller
	 *         needs to check whether all returned coordinates are valid.
	 */
	public int[][] getCellCoordinates(GridCell origin) {
		int rowOrigin = origin.getRow();
		int colOrigin = origin.getColumn();

		// Get cage type matrix
		if (usedCells == null) {
			return new int[][] { { rowOrigin, colOrigin } };
		}

		// Calculate coordinates of cells involved in case this cage type starts
		// at the given origin cell.
		int[][] coordinates = new int[mSize][2];
		int coordinatesIndex = 0;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				if (usedCells[row][col]) {
					coordinates[coordinatesIndex++] = new int[] {
							rowOrigin + row, colOrigin + col - colOriginOffset };
				}
			}
		}

		return coordinates;
	}

	/**
	 * Sets a cage type matrix. Empty rows and columns will be removed from the
	 * given matrix.
	 * 
	 * @param newCageTypeMatrix
	 *            The matric defining the cage type. Used cells have value true.
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
		this.rows = bottom - top + 1;
		this.cols = right - left + 1;
		this.usedCells = new boolean[this.rows][this.cols];
		boolean originFound = false;
		this.mSize = 0;
		for (int row = top; row <= bottom; row++) {
			for (int col = left; col <= right; col++) {
				this.usedCells[row - top][col - left] = newCageTypeMatrix[row][col];
				if (newCageTypeMatrix[row][col]) {
					this.mSize++;
					if (!originFound) {
						originFound = true;
						this.colOriginOffset = col - left;
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
	public String toString() {
		String result = "";
		for (int row = 0; row < rows; row++) {
			result += "  ";
			for (int col = 0; col < cols; col++) {
				result += (usedCells[row][col] ? " X" : " -");
			}
			result += "\n";
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		// Return true if the objects are identical.
		// (This is just an optimization, not required for correctness.)
		if (this == o) {
			return true;
		}

		// Return false if the other object has the wrong type.
		// This type may be an interface depending on the interface's
		// specification.
		if (!(o instanceof GridCageType)) {
			return false;
		}

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access
		// private fields.
		GridCageType lhs = (GridCageType) o;

		// Return false in case dimensions are not the same.
		if (rows != lhs.rows || cols != lhs.cols) {
			return false;
		}

		// Return false in case content of shape matrixes are not the same.
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				if (usedCells[row][col] != lhs.usedCells[row][col]) {
					return false;
				}
			}
		}

		// Objects are equal.
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}