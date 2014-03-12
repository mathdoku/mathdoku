package net.mathdoku.plus.puzzle.cage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.util.Util;

import java.util.Arrays;
import java.util.List;

public class Cage {
	private final int mId;
	private final CageOperator mCageOperator;
	private final int mResult;
	private boolean mHideOperator;
	private final int[] mCells;

	// User math is correct
	private boolean mUserMathCorrect;

	// Cached list of possible combo's which can be used in this cage if other
	// cages in the grid were not relevant.
	private List<int[]> mPossibleCombos;

	// Enclosing context
	private Grid mGrid;

	/**
	 * Creates a new instance of {@link Cage}.
	 */
	public Cage(CageBuilder cageBuilder) {
		// Set default for variables which can not be set via the builder
		mPossibleCombos = null;
		// Reference to grid will only be set after the entire grid is created
		// which cannot be done until all cages are created.
		mGrid = null;
		// Defaulting mUserMathCorrect to false result in setting all borders
		// when checking the cage math for the first time.
		mUserMathCorrect = false;

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
			throw new InvalidGridException(String.format(
					"Id of cage %d has not been set.", mId));
		}
	}

	private void validateResultThrowsExceptionOnError() {
		if (mResult <= 0) {
			throw new InvalidGridException("Result of cage " + mResult
					+ " not set correctly.");
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
					"Cannot create a cage without a list of cell id's. mCells = "
							+ (mCells == null ? "null" : "empty list"));
		}
		if (hasInvalidNumberOfCellsForOperator()) {
			throw new InvalidGridException(
					"Cage has an invalid number of cells (" + mCells.length
							+ ") for operator " + mCageOperator.toString()
							+ ".");
		}
	}

	@Override
	public String toString() {
		@SuppressWarnings("StringBufferReplaceableByString")
		final StringBuilder sb = new StringBuilder("Cage{");
		sb.append("mId=").append(mId);
		sb.append(", mCageOperator=").append(mCageOperator);
		sb.append(", mResult=").append(mResult);
		sb.append(", mHideOperator=").append(mHideOperator);
		sb.append(", mCells=").append(Arrays.toString(mCells));
		sb.append(", mUserMathCorrect=").append(mUserMathCorrect);
		sb.append(", mPossibleCombos=").append(mPossibleCombos);
		sb.append(", mGrid=").append(mGrid);
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

	private boolean isNoneMathsCorrect(List<Integer> userValues) {
		return userValues.size() == 1 && (userValues.get(0) == mResult);
	}

	private boolean isAddMathsCorrect(List<Integer> userValues) {
		if (userValues.size() >= 2) {
			int total = 0;
			for (int userValue : userValues) {
				total += userValue;
			}
			return total == mResult;
		}
		return false;
	}

	private boolean isMultiplyMathsCorrect(List<Integer> userValues) {
		if (userValues.size() >= 2) {
			int total = 1;
			for (int userValue : userValues) {
				total *= userValue;
			}
			return total == mResult;
		}
		return false;
	}

	private boolean isDivideMathsCorrect(List<Integer> userValues) {
		if (userValues.size() == 2) {
			int lower = Math.min(userValues.get(0), userValues.get(1));
			int higher = Math.max(userValues.get(0), userValues.get(1));
			return higher == (lower * mResult);
		}
		return false;
	}

	private boolean isSubtractMathsCorrect(List<Integer> userValues) {
		if (userValues.size() == 2) {
			int lower = Math.min(userValues.get(0), userValues.get(1));
			int higher = Math.max(userValues.get(0), userValues.get(1));
			return higher - lower == mResult;
		}
		return false;
	}

	/**
	 * Checks whether the cage arithmetic is correct using the values the user
	 * has filled in. If needed the border of the cage will be updated to
	 * reflect a change of state. For single cell cages the math will never be
	 * incorrect.
	 * 
	 * @return True in case the user math does not contain an error or in case
	 *         not all cells in the cage have been filled in.
	 */
	public boolean checkUserMath() {
		if (mGrid == null) {
			return false;
		}

		boolean oldUserMathCorrect = mUserMathCorrect;

		List<Integer> userValues = mGrid.getUserValuesForCells(mCells);
		if (userValues != null && userValues.size() == mCells.length) {
			mUserMathCorrect = mHideOperator ? checkUserMathHiddenOperators(userValues)
					: checkUserMathVisibleOperators(userValues);
		} else {
			// At least one cell has no user value. So math is not incorrect.
			mUserMathCorrect = true;
		}

		if (oldUserMathCorrect != mUserMathCorrect) {
			invalidateBordersOfAllCells();
		}

		return mUserMathCorrect;
	}

	private boolean checkUserMathHiddenOperators(List<Integer> userValues) {
		boolean userMathCorrect = isNoneMathsCorrect(userValues);
		userMathCorrect = userMathCorrect || isAddMathsCorrect(userValues);
		userMathCorrect = userMathCorrect || isMultiplyMathsCorrect(userValues);
		userMathCorrect = userMathCorrect || isDivideMathsCorrect(userValues);
		userMathCorrect = userMathCorrect || isSubtractMathsCorrect(userValues);

		return userMathCorrect;
	}

	private boolean checkUserMathVisibleOperators(List<Integer> userValues) {
		boolean userMathCorrect;

		switch (mCageOperator) {
		case NONE:
			userMathCorrect = isNoneMathsCorrect(userValues);
			break;
		case ADD:
			userMathCorrect = isAddMathsCorrect(userValues);
			break;
		case MULTIPLY:
			userMathCorrect = isMultiplyMathsCorrect(userValues);
			break;
		case DIVIDE:
			userMathCorrect = isDivideMathsCorrect(userValues);
			break;
		case SUBTRACT:
			userMathCorrect = isSubtractMathsCorrect(userValues);
			break;
		default:
			userMathCorrect = false;
			break;
		}

		return userMathCorrect;
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
	 *            The grid to which the cage belongs.
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

		List userValues = mGrid.getUserValuesForCells(mCells);
		return userValues == null || userValues.size() < mCells.length;
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

	// TODO: method should be removed after refactor MathDokuDLX.java
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

	public boolean isUserMathCorrect() {
		return mUserMathCorrect;
	}
}
