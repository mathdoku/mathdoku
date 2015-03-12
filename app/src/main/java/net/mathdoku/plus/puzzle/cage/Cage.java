package net.mathdoku.plus.puzzle.cage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.util.Util;

import java.util.Arrays;
import java.util.List;

public class Cage {
    public static final int CAGE_ID_NOT_SET = -1;

    private final int mId;
    private final CageOperator mCageOperator;
    private final int mResult;
    private boolean mHideOperator;
    private final int[] mCells;
    private boolean mMathOnEnteredValuesIsCorrect;

    // Cached list of possible combo's which can be used in this cage if other
    // cages in the grid were not relevant.
    private List<int[]> mPossibleCombos;

    // Enclosing context
    private Grid mGrid;

    public Cage(CageBuilder cageBuilder) {
        mPossibleCombos = null;
        mGrid = null;

        // Defaulting mMathOnEnteredValuesIsCorrect to false result in setting
        // all borders when checking the cage math for the first time.
        mMathOnEnteredValuesIsCorrect = false;

        // Get defaults from builder
        mId = cageBuilder.getId();
        mHideOperator = cageBuilder.getHideOperator();
        mResult = cageBuilder.getResult();
        mCageOperator = cageBuilder.getCageOperator();
        mCells = cageBuilder.getCells();

        validateParametersThrowsExceptionOnError();
    }

    private void validateParametersThrowsExceptionOnError() {
        validateGridIdThrowsExceptionOnError();
        validateResultThrowsExceptionOnError();
        validateCageOperatorThrowsExceptionOnError();
        validateNumberOfCellsThrowsExceptionOnError();
    }

    private void validateGridIdThrowsExceptionOnError() {
        // Check if required parameters are specified
        if (mId < 0) {
            throw new InvalidGridException(String.format("Id of cage %d has not been set.", mId));
        }
    }

    private void validateResultThrowsExceptionOnError() {
        if (mResult <= 0) {
            throw new InvalidGridException("Result of cage not set correctly." + toString());
        }
    }

    private void validateCageOperatorThrowsExceptionOnError() {
        if (mCageOperator == null) {
            throw new InvalidGridException("Cage operator has not been set.");
        }
    }

    private void validateNumberOfCellsThrowsExceptionOnError() {
        if (Util.isArrayNullOrEmpty(mCells)) {
            throw new InvalidGridException(
                    "Cannot create a cage without a list of cell id's. mCells = " + (mCells ==
                            null ? "null" : "empty list"));
        }
        if (hasInvalidNumberOfCellsForOperator()) {
            throw new InvalidGridException(
                    "Cage has an invalid number of cells (" + mCells.length + ") for operator " +
                            mCageOperator.toString() + ".");
        }
    }

    @Override
    public String toString() {
        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder sb = new StringBuilder("Cage{");
        sb.append("mId=")
                .append(mId);
        sb.append(", mCageOperator=")
                .append(mCageOperator);
        sb.append(", mResult=")
                .append(mResult);
        sb.append(", mHideOperator=")
                .append(mHideOperator);
        sb.append(", mCells=")
                .append(Arrays.toString(mCells));
        sb.append(", mMathOnEnteredValuesIsCorrect=")
                .append(mMathOnEnteredValuesIsCorrect);
        sb.append(", mPossibleCombos=")
                .append(mPossibleCombos);
        sb.append(", mGrid=")
                .append(mGrid);
        sb.append('}');
        return sb.toString();
    }

    public boolean isOperatorHidden() {
        return mHideOperator;
    }

    public void revealOperator() {
        mHideOperator = false;
    }

    private boolean hasInvalidNumberOfCellsForOperator() {
        boolean result;
        switch (mCageOperator) {
            case NONE:
                result = mCells.length != 1;
                break;
            case ADD:
            case MULTIPLY:
                result = mCells.length < 2;
                break;
            case DIVIDE:
            case SUBTRACT:
                result = mCells.length != 2;
                break;
            default:
                result = false;
                break;
        }
        return result;
    }

    private boolean isNoneMathsCorrect(List<Integer> values) {
        return values.size() == 1 && values.get(0) == mResult;
    }

    private boolean isAddMathsCorrect(List<Integer> values) {
        if (values.size() >= 2) {
            int total = 0;
            for (int value : values) {
                total += value;
            }
            return total == mResult;
        }
        return false;
    }

    private boolean isMultiplyMathsCorrect(List<Integer> values) {
        if (values.size() >= 2) {
            int total = 1;
            for (int value : values) {
                total *= value;
            }
            return total == mResult;
        }
        return false;
    }

    private boolean isDivideMathsCorrect(List<Integer> values) {
        if (values.size() == 2) {
            int lower = Math.min(values.get(0), values.get(1));
            int higher = Math.max(values.get(0), values.get(1));
            return higher == lower * mResult;
        }
        return false;
    }

    private boolean isSubtractMathsCorrect(List<Integer> values) {
        if (values.size() == 2) {
            int lower = Math.min(values.get(0), values.get(1));
            int higher = Math.max(values.get(0), values.get(1));
            return higher - lower == mResult;
        }
        return false;
    }

    /**
     * Checks whether the cage arithmetic is correct using the values the user has filled in. If
     * needed the border of the cage will be updated to reflect a change of state. For single cell
     * cages the math will never be incorrect.
     *
     * @return True in case the math on the entered values in the cage does not contain an error or
     * in case not all cells in the cage have been filled in.
     */
    public boolean checkMathOnEnteredValues() {
        if (mGrid == null) {
            return false;
        }

        boolean oldMathOnEnteredValuesIsCorrect = mMathOnEnteredValuesIsCorrect;

        List<Integer> enteredValues = mGrid.getEnteredValuesForCells(mCells);
        if (enteredValues != null && enteredValues.size() == mCells.length) {
            mMathOnEnteredValuesIsCorrect = mHideOperator ?
                    checkMathOnEnteredValuesWithHiddenOperators(
                    enteredValues) : checkMathOnEnteredValuesWithVisibleOperators(enteredValues);
        } else {
            // At least one cell is empty. So math is not incorrect.
            mMathOnEnteredValuesIsCorrect = true;
        }

        if (oldMathOnEnteredValuesIsCorrect != mMathOnEnteredValuesIsCorrect) {
            invalidateBordersOfAllCells();
        }

        return mMathOnEnteredValuesIsCorrect;
    }

    private boolean checkMathOnEnteredValuesWithHiddenOperators(List<Integer> enteredValues) {
        boolean mathCorrect = isNoneMathsCorrect(enteredValues);
        mathCorrect = mathCorrect || isAddMathsCorrect(enteredValues);
        mathCorrect = mathCorrect || isMultiplyMathsCorrect(enteredValues);
        mathCorrect = mathCorrect || isDivideMathsCorrect(enteredValues);
        mathCorrect = mathCorrect || isSubtractMathsCorrect(enteredValues);

        return mathCorrect;
    }

    private boolean checkMathOnEnteredValuesWithVisibleOperators(List<Integer> enteredValues) {
        boolean mathCorrect;

        switch (mCageOperator) {
            case NONE:
                mathCorrect = isNoneMathsCorrect(enteredValues);
                break;
            case ADD:
                mathCorrect = isAddMathsCorrect(enteredValues);
                break;
            case MULTIPLY:
                mathCorrect = isMultiplyMathsCorrect(enteredValues);
                break;
            case DIVIDE:
                mathCorrect = isDivideMathsCorrect(enteredValues);
                break;
            case SUBTRACT:
                mathCorrect = isSubtractMathsCorrect(enteredValues);
                break;
            default:
                mathCorrect = false;
                break;
        }

        return mathCorrect;
    }

    /**
     * Set borders for all cells in this cage.
     */
    public boolean invalidateBordersOfAllCells() {
        if (mGrid == null) {
            return false;
        }
        List<Cell> cells = mGrid.getCells(mCells);
        if (cells != null) {
            for (Cell cell : cells) {
                cell.invalidateBorders();
            }
        }
        return true;
    }

    public int getIdUpperLeftCell() {
        return mCells[0];
    }

    public String getCageText() {
        return mResult + (mHideOperator ? "" : mCageOperator.getSign());
    }

    /**
     * Sets the reference to the grid to which this cage belongs.
     *
     * @param grid
     *         The grid to which the cage belongs.
     */
    public void setGridReference(Grid grid) {
        mGrid = grid;

        // Don't needs to set the reference to the grid cells in mCells as they
        // will be set directly via the list of grid cells of mGrid.
    }

    public boolean hasEmptyCells() {
        if (mGrid == null) {
            return true;
        }

        List enteredValues = mGrid.getEnteredValuesForCells(mCells);
        return enteredValues == null || enteredValues.size() < mCells.length;
    }

    public int getNumberOfCells() {
        return mCells.length;
    }

    public void setPossibleCombos(List<int[]> possibleCombos) {
        mPossibleCombos = possibleCombos;
    }

    public List<int[]> getPossibleCombos() {
        return mPossibleCombos;
    }

    public int getId() {
        return mId;
    }

    public CageOperator getOperator() {
        return mCageOperator;
    }

    public int getResult() {
        return mResult;
    }

    // TODO: method should be removed after refactor GridSolver.java
    public List<Cell> getListOfCells() {
        return mGrid == null ? null : mGrid.getCells(mCells);
    }

    public int[] getCells() {
        return mCells;
    }

    public Cell getCell(int position) {
        if (mGrid == null || position < 0 || position >= mCells.length) {
            return null;
        }
        return mGrid.getCell(mCells[position]);
    }

    public boolean isMathOnEnteredValuesCorrect() {
        return mMathOnEnteredValuesIsCorrect;
    }

    public boolean isNull() {
        return false;
    }

    public boolean isValid() {
        return !isNull();
    }

    public boolean isSingleCellCage() {
        return mCells != null && mCells.length == 1;
    }
}
