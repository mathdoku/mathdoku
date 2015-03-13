package net.mathdoku.plus.puzzle.cell;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.GridStatistics.StatisticsCounterType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cell {
    @SuppressWarnings("unused")
    private static final String TAG = Cell.class.getName();

    public static final int NO_ENTERED_VALUE = 0;

    // Index of the cell (left to right, top to bottom, zero based)
    private final int mId;

    // Row and column position in grid (zero based)
    private final int mRow;
    private final int mColumn;

    // The correct, the entered (possible) value(s) in cell
    private int mCorrectValue;
    private int mEnteredValue;
    private final List<Integer> mPossibles;
    private final int mMaxValue;

    // String of the cage, only filled in case the cell is the top left cell in
    // the cage.
    private String mCageText;

    private int mCageId;
    private Grid mGrid;

    private boolean mDuplicateValueHighlight;
    private boolean mSelected;
    private boolean mRevealed;
    private boolean mInvalidValueHighlight;

    private boolean mBordersInvalidated;

    public Cell(CellBuilder cellBuilder) {
        // Get values from CellBuilder
        int gridSize = cellBuilder.getGridSize();
        mId = cellBuilder.getId();
        mCorrectValue = cellBuilder.getCorrectValue();
        mEnteredValue = cellBuilder.getEnteredValue();
        mMaxValue = gridSize;
        mCageId = cellBuilder.getCageId();
        mCageText = cellBuilder.getCageText();
        mPossibles = cellBuilder.getPossibles();
        mDuplicateValueHighlight = cellBuilder.isDuplicateValueHighlighted();
        mSelected = cellBuilder.isSelected();
        mRevealed = cellBuilder.isRevealed();
        mInvalidValueHighlight = cellBuilder.isInvalidValueHighlighted();

        // Check if required parameters are specified
        validateCellParametersThrowsExceptionOnError(cellBuilder);
        validatePossiblesThrowsExceptionOnError();
        validateCorrectValueThrowsExceptionOnError(cellBuilder);
        validateCageReferenceThrowsExceptionOnError(cellBuilder);

        // Determine row and column based on
        mColumn = mId % gridSize;
        mRow = mId / gridSize;
    }

    private void validateCellParametersThrowsExceptionOnError(CellBuilder cellBuilder) {
        int gridSize = cellBuilder.getGridSize();
        try {
            GridType.fromInteger(gridSize);
        } catch (IllegalArgumentException e) {
            throw new InvalidGridException(String.format("Parameter gridSize (%d) has an invalid value.", gridSize), e);
        }
        if (mId < 0) {
            throw new InvalidGridException(String.format("Parameter mId (%d) has an invalid value.", mId));
        }
        if (mEnteredValue < 0 || mEnteredValue > mMaxValue) {
            throw new InvalidGridException(
                    String.format("Parameter mEnteredValue (%d) has an invalid value.", mEnteredValue));
        }
    }

    private void validatePossiblesThrowsExceptionOnError() {
        if (mPossibles == null) {
            throw new InvalidGridException("Parameter mPossibles is null.");
        }

        for (int possible : mPossibles) {
            if (possible <= 0 || possible > mMaxValue) {
                throw new InvalidGridException(
                        String.format("Parameter mPossible contains a possible value (%d) with " + "an invalid " +
                                              "" + "value.", possible));
            }
        }
    }

    private void validateCorrectValueThrowsExceptionOnError(CellBuilder cellBuilder) {
        int minimumCorrectValue = cellBuilder.performLenientCorrectValueCheck() ? CellBuilder.CORRECT_VALUE_NOT_SET : 1;
        if (mCorrectValue < minimumCorrectValue || mCorrectValue > mMaxValue) {
            throw new InvalidGridException(
                    String.format("Parameter mCorrectValue (%d) has an invalid value.", mCorrectValue));
        }
    }

    private void validateCageReferenceThrowsExceptionOnError(CellBuilder cellBuilder) {
        if (cellBuilder.performCageReferenceCheck() && mCageId < 0) {
            throw new InvalidGridException(String.format("Parameter mCageId (%d) has an invalid value.", mCageId));
        }
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("Cell{");
        stringBuilder.append("mId=")
                .append(mId);
        stringBuilder.append(", mColumn=")
                .append(mColumn);
        stringBuilder.append(", mRow=")
                .append(mRow);
        stringBuilder.append(", mCorrectValue=")
                .append(mCorrectValue);
        stringBuilder.append(", mEnteredValue=")
                .append(mEnteredValue);
        stringBuilder.append(", mCageId=")
                .append(mCageId);
        stringBuilder.append(", mCageText='")
                .append(mCageText)
                .append('\'');
        stringBuilder.append(", mPossibles=")
                .append(mPossibles);
        stringBuilder.append(", mDuplicateValueHighlight=")
                .append(mDuplicateValueHighlight);
        stringBuilder.append(", mSelected=")
                .append(mSelected);
        stringBuilder.append(", mRevealed=")
                .append(mRevealed);
        stringBuilder.append(", mInvalidValueHighlight=")
                .append(mInvalidValueHighlight);
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
     *         The digit which has to be added.
     * @return True is the value has been added. False otherwise.
     */
    public boolean addPossible(int digit) {
        if (isValueNotValid(digit) || hasPossible(digit)) {
            return false;
        }

        mPossibles.add(digit);
        Collections.sort(mPossibles);

        increasePossiblesCounter();

        return true;
    }

    private void increasePossiblesCounter() {
        if (mGrid != null) {
            GridStatistics gridStatistics = mGrid.getGridStatistics();
            if (gridStatistics != null) {
                gridStatistics.increaseCounter(StatisticsCounterType.POSSIBLES);
            }
        }
    }

    /**
     * Removes the given digit from the possible values if it was added before.
     *
     * @param digit
     *         The digit which has to be removed.
     * @return True in case the given digit has been removed. False otherwise.
     */
    public boolean removePossible(int digit) {
        if (hasPossible(digit)) {
            mPossibles.remove(Integer.valueOf(digit));
            return true;
        } else {
            // Digit was not added as possible.
            return false;
        }
    }

    public int getEnteredValue() {
        return mEnteredValue;
    }

    public boolean hasEnteredValue() {
        return mEnteredValue != NO_ENTERED_VALUE;
    }

    /**
     * Change the entered value of the cell.
     *
     * @param newValue
     *         The new value for the cell. Use 0 to clear the cell.
     * @return True if the new value is set as entered value. False otherwise. Also in case the entered value isn't
     * changed, false is returned.
     */
    public boolean setEnteredValue(int newValue) {
        if (isValueNotValid(newValue) && newValue != NO_ENTERED_VALUE) {
            // New value is invalid. Note: 0 is used to indicate that no user
            // value is set!
            return false;
        }

        // Check if the value is changed.
        int oldValue = mEnteredValue;
        if (newValue == oldValue) {
            return false;
        }

        // Set new value and remove possibles
        mEnteredValue = newValue;
        mPossibles.clear();

        updateStatisticsOnChangeOfEnteredValue(oldValue, newValue);

        // Clear highlight except cheating
        mInvalidValueHighlight = false;
        mDuplicateValueHighlight = false;
        mBordersInvalidated = true;

        // Check if grid is solved.
        if (mGrid != null && mGrid.isSolved()) {
            mGrid.setSolved();
        }

        return true;
    }

    private void updateStatisticsOnChangeOfEnteredValue(int oldEnteredValue, int newEnteredValue) {
        if (mGrid != null && newEnteredValue != oldEnteredValue) {
            GridStatistics gridStatistics = mGrid.getGridStatistics();

            // Only count as replacement as both the original and the new value
            // are not 0 as this is used to indicate an empty cell.
            if (newEnteredValue != NO_ENTERED_VALUE && oldEnteredValue != NO_ENTERED_VALUE) {
                gridStatistics.increaseCounter(StatisticsCounterType.USER_VALUE_REPLACED);
            }

            // Counters for filled and empty cells are only updated if the
            // solution of the cell has not been revealed.
            if (!mRevealed) {
                if (oldEnteredValue == NO_ENTERED_VALUE && newEnteredValue != NO_ENTERED_VALUE) {
                    // Empty cell is filled in
                    gridStatistics.decreaseCounter(StatisticsCounterType.CELLS_EMPTY);
                    gridStatistics.increaseCounter(StatisticsCounterType.CELLS_FILLED);
                } else if (oldEnteredValue != NO_ENTERED_VALUE && newEnteredValue == NO_ENTERED_VALUE) {
                    // Non empty cell is cleared
                    gridStatistics.decreaseCounter(StatisticsCounterType.CELLS_FILLED);
                    gridStatistics.increaseCounter(StatisticsCounterType.CELLS_EMPTY);
                }
            }
        }
    }

    private boolean isValueNotValid(int digit) {
        return digit < 1 || digit > mMaxValue;
    }

    /**
     * Checks whether the borders of the cell are invalidated since last call to this method.
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
     * Clear the entered value and/or possible values in a cell.
     */
    public void clearValue() {
        // Note: setting the EnteredValue to 0 clears and the possible values!
        setEnteredValue(0);
    }

    /**
     * Checks whether the entered value is correct.
     *
     * @return True in case the entered value is wrong. False otherwise.
     */
    public boolean isEnteredValueIncorrect() {
        return mEnteredValue != mCorrectValue;
    }

    /**
     * Mark the cell as a cell containing an invalid value.
     */
    public void setInvalidHighlight() {
        mInvalidValueHighlight = true;
    }

    /**
     * Checks whether the cell is highlighted as invalid. Note: a cell can contain an invalid value without being marked
     * as invalid. A cell will only be marked as invalid after using the option "Check Progress".
     *
     * @return True in case the cell has been marked as invalid. False otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasInvalidValueHighlight() {
        return mInvalidValueHighlight;
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

    public void setCageId(int newCageId) {
        mCageId = newCageId;
    }

    public void setCageText(String newCageText) {
        mCageText = newCageText;
    }

    /**
     * Check if the given digit is registered as a possible value for this cell.
     *
     * @param digit
     *         The digit which is to be checked.
     * @return True in case the digit is registered as a possible value for this cell. False otherwise.
     */
    public boolean hasPossible(int digit) {
        return mPossibles.indexOf(digit) >= 0;
    }

    /**
     * Reveals the correct value of a cell. Note, revealing a cell containing the correct value is handled the same way
     * as revealing the cell which does not contain a correct value. In this way, revealing a cell cannot be abused as a
     * way of using check progress for a single cell without having a penalty to be paid.
     */
    public void revealCorrectValue() {
        // Update the grid statistics only in case the cell is revealed for the
        // first time.
        if (!mRevealed) {
            if (mGrid != null) {
                GridStatistics gridStatistics = mGrid.getGridStatistics();
                gridStatistics.decreaseCounter(
                        hasEnteredValue() ? StatisticsCounterType.CELLS_FILLED : StatisticsCounterType.CELLS_EMPTY);
                gridStatistics.increaseCounter(StatisticsCounterType.CELLS_REVEALED);
            }
            mRevealed = true;
        }

        // Always set the entered value again to the correct value as it might
        // have
        // been changed af the previous time it was revealed.
        mEnteredValue = mCorrectValue;
    }

    /**
     * Sets the reference to the grid to which this cell belongs.
     *
     * @param grid
     *         The grid the cell should refer to.
     */
    public void setGridReference(Grid grid) {
        mGrid = grid;
    }

    /**
     * Checks whether this cell is part of the currently selected cage.
     *
     * @return True in case this cell is part of the currently selected cage. False otherwise.
     */
    public boolean isCellInSelectedCage() {
        Cell selectedCellInGrid = mGrid.getSelectedCell();
        if (selectedCellInGrid == null) {
            // When no cell is selected, a cage isn't selected as well.
            return false;
        }

        return selectedCellInGrid.getCageId() == mCageId;
    }

    /**
     * Checks if this cell is empty, i.e. it does not contain a entered value nor possible values.
     *
     * @return True in case the cell is empty. False otherwise.
     */
    public boolean isEmpty() {
        return mEnteredValue == NO_ENTERED_VALUE && mPossibles.isEmpty();
    }

    /**
     * Set the duplicate highlight of the cell.
     *
     * @param highlight
     *         True in case the duplicate highlight is visible. False otherwise.
     */
    public void setDuplicateHighlight(boolean highlight) {
        mDuplicateValueHighlight = highlight;
    }

    /**
     * Checks whether the cell is the selected cell.
     *
     * @return True if the cell is the selected cell. False otherwise.
     */
    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
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
