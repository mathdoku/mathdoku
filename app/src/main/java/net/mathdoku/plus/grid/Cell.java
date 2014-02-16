package net.mathdoku.plus.grid;

import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.GridStatistics.StatisticsCounterType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cell {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.Cell";

	// Index of the cell (left to right, top to bottom, zero-indexed)
	private int mId;
	// X grid position, zero indexed
	private int mColumn;
	// Y grid position, zero indexed
	private int mRow;
	// Value of the digit in the cell
	private int mCorrectValue;
	// User's entered value
	private int mUserValue;
	// Id of the enclosing cage
	private int mCageId;
	// String of the cage
	private String mCageText;
	// User's candidate digits
	private List<Integer> mPossibles;

	// X pixel position
	private float mPosX;
	// Y pixel position
	private float mPosY;

	private Grid mGrid;

	// Highlight in case a duplicate value is found in row or column
	private boolean mDuplicateValueHighlight;
	// Whether to show cell as selected
	private boolean mSelected;
	// Player revealed this cell
	private boolean mRevealed;
	// Highlight user input isn't correct value
	private boolean mInvalidUserValueHighlight;

	// Indicates whether borders of the cell need to be redrawn
	private boolean mBordersInvalidated;

	public Cell(CellBuilder cellBuilder) {
		// Initialize the variables
		mPosX = 0;
		mPosY = 0;

		// Get values from CellBuilder
		int gridSize = cellBuilder.getGridSize();
		mId = cellBuilder.getId();
		mCorrectValue = cellBuilder.getCorrectValue();
		mUserValue = cellBuilder.getUserValue();
		mCageId = cellBuilder.getCageId();
		mCageText = cellBuilder.getCageText();
		mPossibles = cellBuilder.getPossibles();
		mDuplicateValueHighlight = cellBuilder.isDuplicateValueHighlighted();
		mSelected = cellBuilder.isSelected();
		mRevealed = cellBuilder.isRevealed();
		mInvalidUserValueHighlight = cellBuilder
				.isInvalidUserValueHighlighted();

		// Check if required parameters are specified
		validateCellParametersThrowsExceptionOnError(cellBuilder);
		validateCorrectValueThrowsExceptionOnError(cellBuilder);
		validateCageReferenceThrowsExceptionOnError(cellBuilder);

		// Determine row and column based on
		mColumn = mId % gridSize;
		mRow = mId / gridSize;
	}

	private void validateCellParametersThrowsExceptionOnError(
			CellBuilder cellBuilder) {
		int gridSize = cellBuilder.getGridSize();
		if (gridSize <= 0) {
			throw new InvalidGridException("Parameter gridSize (" + gridSize
					+ ") has an invalid value.");
		}
		if (mId < 0) {
			throw new InvalidGridException("Parameter mId (" + mId
					+ ") has an invalid value.");
		}
		if (mUserValue < 0 || mUserValue > gridSize) {
			throw new InvalidGridException("Parameter mUserValue ("
					+ mUserValue + ") has an invalid value.");
		}
	}

	private void validateCorrectValueThrowsExceptionOnError(
			CellBuilder cellBuilder) {
		if (cellBuilder.performCorrectValueCheck()
				&& (mCorrectValue <= 0 || mCorrectValue > cellBuilder
						.getGridSize())) {
			throw new InvalidGridException("Parameter mCorrectValue ("
					+ mCorrectValue + ") has an invalid value.");
		}
	}

	private void validateCageReferenceThrowsExceptionOnError(
			CellBuilder cellBuilder) {
		if (cellBuilder.performCageReferenceCheck() && mCageId < 0) {
			throw new InvalidGridException("Parameter mCageId (" + mCageId
					+ ") has an invalid value.");
		}
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder("Cell{");
		stringBuilder.append("mId=").append(mId);
		stringBuilder.append(", mColumn=").append(mColumn);
		stringBuilder.append(", mRow=").append(mRow);
		stringBuilder.append(", mCorrectValue=").append(mCorrectValue);
		stringBuilder.append(", mUserValue=").append(mUserValue);
		stringBuilder.append(", mCageId=").append(mCageId);
		stringBuilder.append(", mCageText='").append(mCageText).append('\'');
		stringBuilder.append(", mPossibles=").append(mPossibles);
		stringBuilder.append(", mDuplicateValueHighlight=").append(
				mDuplicateValueHighlight);
		stringBuilder.append(", mSelected=").append(mSelected);
		stringBuilder.append(", mRevealed=").append(mRevealed);
		stringBuilder.append(", mInvalidUserValueHighlight=").append(
				mInvalidUserValueHighlight);
		stringBuilder.append('}');
		return stringBuilder.toString();
	}

	public int countPossibles() {
		return mPossibles.size();
	}

	public void clearPossibles() {
		mPossibles.clear();
	}

	/**
	 * Adds the given digit to the possible values if not yet added before.
	 * 
	 * @param digit
	 *            The digit which has to be added.
	 */
	public void addPossible(int digit) {
		addPossible(digit, true);
	}

	/**
	 * Adds the given digit to the possible values if not yet added before.
	 * 
	 * @param digit
	 *            The digit which has to be added.
	 * @param updateStatistics
	 *            True in case the statistics have to be updated when adding a
	 *            new maybe value. False otherwise.
	 */
	void addPossible(int digit, boolean updateStatistics) {
		if (!hasPossible(digit)) {
			// Add possible value and sort the list of possible values.
			mPossibles.add(digit);
			Collections.sort(mPossibles);

			// Update statistics
			if (updateStatistics && mGrid != null) {
				GridStatistics gridStatistics = mGrid.getGridStatistics();
				if (gridStatistics != null) {
					gridStatistics
							.increaseCounter(StatisticsCounterType.POSSIBLES);
				}
			}
		}
	}

	/**
	 * Removes the given digit from the possible values if it was added before.
	 * 
	 * @param digit
	 *            The digit which has to be removed.
	 */
	public void removePossible(int digit) {
		if (hasPossible(digit)) {
			mPossibles.remove(Integer.valueOf(digit));
		}
	}

	public int getUserValue() {
		return mUserValue;
	}

	public boolean isUserValueSet() {
		return mUserValue != 0;
	}

	/**
	 * Set the user value of the cell to a new value.
	 * 
	 * @param digit
	 *            The new value for the cell. Use 0 to clear the cell.
	 */
	public void setUserValue(int digit) {
		// Update statistics
		if (mGrid != null) {
			GridStatistics gridStatistics = mGrid.getGridStatistics();

			// Only count as replacement as both the original and the new value
			// are not 0 as this is used to indicate an empty cell.
			if (digit != 0 && mUserValue != 0 && digit != mUserValue) {
				gridStatistics
						.increaseCounter(StatisticsCounterType.USER_VALUE_REPLACED);
			}

			// Cell counters are only update if the solution of the cell has
			// not been revealed.
			if (mRevealed == false) {
				gridStatistics
						.decreaseCounter(mUserValue == 0 ? StatisticsCounterType.CELLS_EMPTY
								: StatisticsCounterType.CELLS_FILLED);
				gridStatistics
						.increaseCounter(digit == 0 ? StatisticsCounterType.CELLS_EMPTY
								: StatisticsCounterType.CELLS_FILLED);
			}
		}

		// Remove possibles
		mPossibles.clear();

		// Clear highlight except cheating
		mInvalidUserValueHighlight = false;
		mDuplicateValueHighlight = false;

		// Set new value
		mUserValue = digit;

		// Set borders for this cell and the adjacent cells
		mBordersInvalidated = true;

		// Check if grid is solved.
		if (mGrid != null) {
			if (mGrid.isSolved()) {
				mGrid.setSolved();
			}
		}
	}

	/**
	 * Checks whether the borders of the cell are invalidated since last call to
	 * this method.
	 * 
	 * @return True in case the borders are invalidated. False otherwise.
	 */
	public boolean isBordersInvalidated() {
		boolean bordersInvalidated = mBordersInvalidated;
		mBordersInvalidated = false;
		return bordersInvalidated;
	}

	public void invalidateBorders() {
		mBordersInvalidated = true;
	}

	/**
	 * Clear the user value and/or possible values in a cell.
	 */
	public void clearValue() {
		// Note: setting the userValue to 0 clear the cell but also the possible
		// values!
		setUserValue(0);
	}

	/**
	 * Checks whether the user value is correct.
	 * 
	 * @return True in case the user value is wrong. False otherwise.
	 */
	public boolean isUserValueIncorrect() {
		return mUserValue != mCorrectValue;
	}

	/**
	 * Mark the cell as a cell containing an invalid value.
	 */
	public void setInvalidHighlight() {
		mInvalidUserValueHighlight = true;
	}

	/**
	 * Checks whether the cell is highlighted as invalid. Note: a cell can
	 * contain an invalid value without being marked as invalid. A cell will
	 * only be marked as invalid after using the option "Check Progress".
	 * 
	 * @return True in case the cell has been marked as invalid. False
	 *         otherwise.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean hasInvalidUserValueHighlight() {
		return mInvalidUserValueHighlight;
	}

	public int getCellId() {
		return mId;
	}

	/**
	 * Get the column number (zero based) of the cell.
	 * 
	 * @return The column number (zero based) of the cell.
	 */
	public int getColumn() {
		return mColumn;
	}

	/**
	 * Gets the row number (zero based) of the cell.
	 * 
	 * @return The row number (zero based) of the cell.
	 */
	public int getRow() {
		return mRow;
	}

	public int getCorrectValue() {
		return mCorrectValue;
	}

	public void setCorrectValue(int newValue) {
		mCorrectValue = newValue;
	}

	public int getCageId() {
		return mCageId;
	}

	public Cage getCage() {
		return (mGrid == null ? null : mGrid.getCage(mCageId));
	}

	public void setCageId(int newCageId) {
		mCageId = newCageId;
	}

	public void setCageText(String newCageText) {
		mCageText = newCageText;
	}

	/**
	 * Saves all information needed to undo a user move on this cell.
	 * 
	 * @param originalCellChange
	 *            Use null in case this cell change is a result of a
	 *            modification made by the user itself. In case the cell is
	 *            changed indirectly as a result of changing another cell, use
	 *            the original cell change.
	 * @return The cell change which is created. This value can be used
	 *         optionally to related other cell changes to this cell change.
	 */
	public CellChange saveUndoInformation(CellChange originalCellChange) {
		// Store old values of this cell
		CellChange move = new CellChange(this, mUserValue, mPossibles);
		if (originalCellChange == null) {
			// This move is not a result of another move.
			mGrid.addMove(move);
		} else {
			originalCellChange.addRelatedMove(move);
		}
		return move;
	}

	public void undo(int previousUserValue, List<Integer> previousPossibleValues) {
		setUserValue(previousUserValue);
		if (previousPossibleValues != null) {
			for (int previousPossibleValue : previousPossibleValues) {
				mPossibles.add(previousPossibleValue);
			}
			Collections.sort(mPossibles);
		}
	}

	/**
	 * Check if the given digit is registered as a possible value for this cell.
	 * 
	 * @param digit
	 *            The digit which is to be checked.
	 * @return True in case the digit is registered as a possible value for this
	 *         cell. False otherwise.
	 */
	public boolean hasPossible(int digit) {
		return (mPossibles.indexOf(Integer.valueOf(digit)) >= 0);
	}

	/**
	 * Confirm that the user has revealed the content of the cell.
	 */
	public void setRevealed() {
		// Correct grid statistics
		if (mRevealed == false && mGrid != null) {
			GridStatistics gridStatistics = mGrid.getGridStatistics();
			gridStatistics
					.decreaseCounter(isUserValueSet() ? StatisticsCounterType.CELLS_FILLED
							: StatisticsCounterType.CELLS_EMPTY);
			gridStatistics
					.increaseCounter(StatisticsCounterType.CELLS_REVEALED);
		}

		mRevealed = true;
	}

	/**
	 * Sets the reference to the grid to which this cell belongs.
	 * 
	 * @param grid
	 *            The grid the cell should refer to.
	 */
	public void setGridReference(Grid grid) {
		mGrid = grid;
	}

	/**
	 * Checks whether this cell is part of the currently selected cage.
	 * 
	 * @return True in case this cell is part of the currently selected cage.
	 *         False otherwise.
	 */
	public boolean isCellInSelectedCage() {
		Cell selectedCellInGrid = mGrid.getSelectedCell();
		if (selectedCellInGrid == null) {
			// When no cell is selected, a cage isn't selected as well.
			return false;
		}

		Cage selectedCageInGrid = selectedCellInGrid.getCage();

		return (selectedCageInGrid.getId() == mCageId);
	}

	/**
	 * Checks whether this cell is part of the same cage as the cell at the
	 * given coordinates.
	 * 
	 * @param row
	 *            Row number (zero based) of cell to compare with.
	 * @param column
	 *            Column number (zero based) of cell to compare with.
	 * @return True in case cells are part of same cage. False otherwise.
	 */
	boolean isInSameCageAsCell(int row, int column) {
		Cell cell = mGrid.getCellAt(row, column);
		return (cell != null && cell.getCageId() == mCageId);
	}

	public Cell getCellAbove() {
		return mGrid.getCellAt(mRow - 1, mColumn);
	}

	public Cell getCellOnRight() {
		return mGrid.getCellAt(mRow, mColumn + 1);
	}

	public Cell getCellBelow() {
		return mGrid.getCellAt(mRow + 1, mColumn);
	}

	public Cell getCellOnLeft() {
		return mGrid.getCellAt(mRow, mColumn - 1);
	}

	/**
	 * Checks if this cell is empty, i.e. it does not contain a user value nor
	 * possible values.
	 * 
	 * @return True in case the cell is empty. False otherwise.
	 */
	public boolean isEmpty() {
		return (mUserValue == 0 && mPossibles.size() == 0);
	}

	/**
	 * Set the duplicate highlight of the cell.
	 * 
	 * @param highlight
	 *            True in case the duplicate highlight is visible. False
	 *            otherwise.
	 */
	public void setDuplicateHighlight(boolean highlight) {
		mDuplicateValueHighlight = highlight;
	}

	/**
	 * Check whether the user value of this cell is used in another cell on the
	 * same row or column.
	 * 
	 * @return True in case the user value of this cell is used in another cell
	 *         on the same row or column.
	 */
	public boolean markDuplicateValuesInSameRowAndColumn() {
		if (mGrid == null) {
			// Cannot look for other cells in same row or column as the cell
			// is not used in a grid.
			return false;
		}

		boolean duplicateValue = false;
		if (isUserValueSet()) {
			for (Cell cell : mGrid.getCells()) {
				if (cell.equals(this) == false
						&& cell.getUserValue() == mUserValue) {
					if (cell.getColumn() == mColumn || cell.getRow() == mRow) {
						// Found another cell in the same row or column
						// containing the same user value. Mark this other cell
						// as duplicate.
						duplicateValue = true;
						cell.setDuplicateHighlight(true);
					}
				}
			}
		}
		// Always update this cell as the duplicate highlight must be removed if
		// not applicable anymore.
		setDuplicateHighlight(duplicateValue);

		return duplicateValue;
	}

	/**
	 * Checks whether the cell is the selected cell.
	 * 
	 * @return True if the cell is the selected cell. False otherwise.
	 */
	public boolean isSelected() {
		return mSelected;
	}

	/**
	 * Mark this cell as the selected cell.
	 */
	public void select() {
		mSelected = true;
	}

	/**
	 * Unmark this cell as the selected cell.
	 */
	public void deselect() {
		mSelected = false;
	}

	public String getCageText() {
		return mCageText;
	}

	public List<Integer> getPossibles() {
		// Return a copy of the list of possible values so the original list
		// cannot be manipulated by the requesting object.
		return new ArrayList<Integer>(mPossibles);
	}

	public boolean isRevealed() {
		return mRevealed;
	}

	public Grid getGrid() {
		return mGrid;
	}

	public boolean isDuplicateValueHighlighted() {
		return mDuplicateValueHighlight;
	}
}
