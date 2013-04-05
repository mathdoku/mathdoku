package net.cactii.mathdoku;

import android.view.View;

/**
 * The DigitPositionGrid is used to determine which digit is shown at each
 * position in a grid of positions.
 */
public class DigitPositionGrid {
	public final static String TAG = "MathDoku.DigitPositionGrid";

	public enum DigitPositionGridType {
		GRID_3X3, GRID_2X5
	};
	
	private DigitPositionGridType mDigitPositionGridType;

	// Matrices holding information about the visibility and the content of the
	// button positions available.
	private int[][] mVisibility;
	private int[][] mValue;

	// Dimension of matrices
	private int mMaxRows;
	private int mMaxCols;
	
	// The number of rows and columns actually used
	private int mRowsUsed;
	private int mColsUsed;
	
	// The number of positions used in the grid
	private int mPositionsUsed;

	/**
	 * Creates a new instance of {@link DigitPositionGrid}.
	 * 
	 * @param digitPositionGridType
	 *            The type of button grid layout.
	 * @param maxDigit
	 *            The number of digit buttons to put in the grid.
	 */
	public DigitPositionGrid(DigitPositionGridType digitPositionGridType,
			int maxDigit) {
		mDigitPositionGridType = digitPositionGridType;
		mPositionsUsed = maxDigit;

		if (mDigitPositionGridType == DigitPositionGridType.GRID_2X5) {
			// Dimensions of grid in layout xml
			mMaxRows = 2;
			mMaxCols = 5;

			// Buttons positions have to be arranged in a grid of 2
			// rows with maximum of 5 positions each. Depending on the
			// grid size the buttons are arranged as follows
			// size 4: 2 rows of 2 buttons each
			// size 5: 2 rows, first row 3 buttons, second row 2 buttons
			// size 6: 2 rows, 3 buttons
			// size 7: 2 rows, first row 4 buttons, second row 3 buttons
			// size 8: 2 rows, 4 buttons
			// size 9: 2 rows, first row 5 buttons, second row 4 buttons
			mRowsUsed = 2;
		} else {
			// Dimensions of grid in layout xml
			mMaxRows = 3;
			mMaxCols = 3;

			// Buttons positions have to be arranged in a grid of 2 or 3
			// rows with maximum of 3 positions each. Depending on the
			// grid size the buttons are arranged as follows
			// size 4: 2 rows of 2 buttons each
			// size 5: 2 rows, first row 3 buttons, second row 2 buttons
			// size 6: 2 rows, 3 buttons
			// size 7: 3 rows, 2 rows of 3 buttons, last row 1 button
			// size 8: 3 rows, 2 rows of 3 buttons, last row 2 buttons
			// size 9: 3 rows, 2 rows of 3 buttons, last row 3 buttons
			mRowsUsed = (maxDigit <= 6 ? 2 : 3);
		}
		mVisibility = new int[mMaxRows][mMaxCols];
		mValue = new int[mMaxRows][mMaxCols];
		mColsUsed = (int) Math.ceil((double) maxDigit / (double) mRowsUsed);

		// Fill the matrices
		int digit = 1;
		for (int row = 0; row < mMaxRows; row++) {
			for (int col = 0; col < mMaxCols; col++) {
				if (row >= mRowsUsed || col >= mColsUsed) {
					// This entire row or column will not be used.
					mVisibility[row][col] = View.GONE;
					mValue[row][col] = -1;
				} else if (digit <= maxDigit) {
					// This position will be used to store a button.
					mVisibility[row][col] = View.VISIBLE;
					mValue[row][col] = digit++;
				} else {
					// This position will not be user for a button but it is in
					// a column in which a button is placed in another row.
					mVisibility[row][col] = View.INVISIBLE;
					mValue[row][col] = -1;
				}
			}
		}
	}

	/**
	 * Checks if this digit position grid can be reused for the given type and maximum digits.
	 * 
	 * @param digitPositionGridType
	 *            The type of button grid layout.
	 * @param maxDigit
	 *            The number of digit buttons to put in the grid.
	 */
	public boolean isReusable(DigitPositionGridType digitPositionGridType,
			int maxDigit) {
		return (mDigitPositionGridType == digitPositionGridType && mPositionsUsed == maxDigit);
	}

	/**
	 * Get the visibility value for a given index.
	 * 
	 * @param index
	 *            The index for which the visibility has to be retrieved.
	 * @return The visibility for the index.
	 */
	public int getVisibility(int index) {
		return mVisibility[indexToRow(index)][indexToCol(index)];
	}

	/**
	 * Get the value (i.e. the digit) placed at a given index.
	 * 
	 * @param index
	 *            The index for which the value has to be retrieved.
	 * @return The value at the index.
	 */
	public int getValue(int index) {
		return mValue[indexToRow(index)][indexToCol(index)];
	}

	/**
	 * Get the row which corresponds with the given index.
	 * 
	 * @param index
	 *            The index for which the row has to be retrieved.
	 * @return The row corresponding with the index.
	 */
	public int indexToRow(int index) {
		return (index / mMaxCols);
	}

	/**
	 * Get the column which corresponds with the given index.
	 * 
	 * @param index
	 *            The index for which the column has to be retrieved.
	 * @return The column corresponding with the index.
	 */
	public int indexToCol(int index) {
		return (index - (indexToRow(index) * mMaxCols));
	}

	/**
	 * Get the row at which the given digit is used.
	 * 
	 * @param digit
	 *            The digit for which the row has to be retrieved.
	 * @return The row on which the digit is used.
	 */
	public int getRow(int digit) {
		for (int row = 0; row < mMaxRows; row++) {
			for (int col = 0; col < mMaxCols; col++) {
				if (mValue[row][col] == digit) {
					return row;
				}
			}
		}
		return -1;
	}

	/**
	 * Get the col at which the given digit is used.
	 * 
	 * @param digit
	 *            The digit for which the column has to be retrieved.
	 * @return The column in which the digit is used.
	 */
	public int getCol(int digit) {
		for (int row = 0; row < mMaxRows; row++) {
			for (int col = 0; col < mMaxCols; col++) {
				if (mValue[row][col] == digit) {
					return col;
				}
			}
		}
		return -1;
	}

	/**
	 * Checks whether the current digit position grid is a 2x5 grid.
	 * 
	 * @return True in case the current grid is a 2x5 grid. False otherwise.
	 */
	public boolean isGrid2x5() {
		return (mDigitPositionGridType == DigitPositionGridType.GRID_2X5);
	}
	
	/**
	 * Get the number of rows with at least one visible digit.
	 * 
	 * @return The number of rows with at least one visible digit.
	 */
	public int getVisibleDigitRows() {
		return mRowsUsed;
	}

	/**
	 * Get the number of columns with at least one visible digit.
	 * 
	 * @return The number of columns with at least one visible digit.
	 */
	public int getVisibleDigitColumns() {
		return mColsUsed;
	}
}