package net.mathdoku.plus.grid;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GridCage {
	private final int mId;
	private final CageOperator mCageOperator;
	private final int mResult;
	private boolean mHideOperator;
	private int[] mCells;

	// User math is correct
	private boolean mUserMathCorrect;

	// Cached list of possible combo's which can be used in this cage if other
	// cages in the grid were not relevant.
	private ArrayList<int[]> mPossibleCombos;

	// Enclosing context
	private Grid mGrid;

	/**
	 * Creates a new instance of {@link GridCage}.
	 */
	public GridCage(CageBuilder cageBuilder) {
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

		// Check if required parameters are specified
		if (mId < 0) {
			throw new InvalidGridException("Id of cage " + mId
					+ " has not been set.");
		}
		if (Util.isArrayNullOrEmpty(mCells)) {
			throw new InvalidGridException(
					"Cannot create a cage without a list of cell id's. mCells = "
							+ (mCells == null ? "null" : "empty list"));
		}
		if (mResult <= 0) {
			throw new InvalidGridException("Result of cage " + mResult
					+ " not set correctly.");
		}
		if (mCageOperator == null) {
			throw new InvalidGridException("Cage operator has not been set.");
		}
		if (hasValidNumberOfCellsForOperator() == false) {
			throw new InvalidGridException(
					"Cage has an invalid number of cells (" + mCells.length
							+ ") for operator " + mCageOperator.toString()
							+ ".");
		}
	}

	/**
	 * Initializes the cage variables.
	 */
	private void initGridCage() {
		mPossibleCombos = null;

		// Defaulting mUserMathCorrect to false result in setting all borders
		// when checking the cage math for the first time.
		mUserMathCorrect = false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("GridCage{");
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
		sb.append(", mUserMathCorrect=")
				.append(mUserMathCorrect);
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

	private boolean hasValidNumberOfCellsForOperator() {
		switch (mCageOperator) {
		case NONE:
			return (mCells.length == 1);
		case ADD:
			return (mCells.length >= 2);
		case MULTIPLY:
			return (mCells.length >= 2);
		case DIVIDE:
			return (mCells.length == 2);
		case SUBTRACT:
			return (mCells.length == 2);
		}
		return false;
	}

	private boolean isNoneMathsCorrect(List<Integer> userValues) {
		if (userValues.size() == 1) {
			return (userValues.get(0) == mResult);
		}
		return false;
	}

	private boolean isAddMathsCorrect(List<Integer> userValues) {
		if (userValues.size() >= 2) {
			int total = 0;
			for (int userValue : userValues) {
				total += userValue;
			}
			return (total == mResult);
		}
		return false;
	}

	private boolean isMultiplyMathsCorrect(List<Integer> userValues) {
		if (userValues.size() >= 2) {
			int total = 1;
			for (int userValue : userValues) {
				total *= userValue;
			}
			return (total == mResult);
		}
		return false;
	}

	private boolean isDivideMathsCorrect(List<Integer> userValues) {
		if (userValues.size() == 2) {
			int lower = Math.min(userValues.get(0),userValues.get(1));
			int higher = Math.max(userValues.get(0), userValues.get(1));
			return higher == (lower * mResult);
		}
		return false;
	}

	private boolean isSubtractMathsCorrect(List<Integer> userValues) {
		if (userValues.size() == 2) {
			int lower = Math.min(userValues.get(0),userValues.get(1));
			int higher = Math.max(userValues.get(0),userValues.get(1));
			return (higher - lower == mResult);
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

		List userValues = mGrid.getUserValuesForCells(mCells);
		if (userValues != null && userValues.size() == mCells.length) {
			if (mHideOperator) {
				mUserMathCorrect = isNoneMathsCorrect(userValues)
						|| isAddMathsCorrect(userValues)
						|| isMultiplyMathsCorrect(userValues)
						|| isDivideMathsCorrect(userValues)
						|| isSubtractMathsCorrect(userValues);
			} else {
				switch (mCageOperator) {
				case NONE:
					mUserMathCorrect = isNoneMathsCorrect(userValues);
					break;
				case ADD:
					mUserMathCorrect = isAddMathsCorrect(userValues);
					break;
				case MULTIPLY:
					mUserMathCorrect = isMultiplyMathsCorrect(userValues);
					break;
				case DIVIDE:
					mUserMathCorrect = isDivideMathsCorrect(userValues);
					break;
				case SUBTRACT:
					mUserMathCorrect = isSubtractMathsCorrect(userValues);
					break;
				}
			}
		} else {
			// At least one cell has no user value. So math is not incorrect.
			mUserMathCorrect = true;
		}

		if (oldUserMathCorrect != mUserMathCorrect) {
			setBorders();
		}

		return mUserMathCorrect;
	}

	/**
	 * Set borders for all cells in this cage.
	 */
	public boolean setBorders() {
		return (mGrid == null ? false : mGrid.setBorderForCells(mCells));
	}

	public int getIdUpperLeftCell() {
		return mCells[0];
	}

	public String getCageText() {
		return mResult
					+ (mHideOperator ? "" : mCageOperator.getSign());
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
		return (userValues == null || userValues.size() < mCells.length);
	}

	public int getNumberOfCells() {
		return mCells.length;
	}

	public void setPossibleCombos(ArrayList<int[]> possibleCombos) {
		mPossibleCombos = possibleCombos;
	}

	public ArrayList<int[]> getPossibleCombos() {
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
	public ArrayList<GridCell> getGridCells() {
		return (mGrid == null ? null : mGrid.getGridCells(mCells));
	}

	public int[] getCells() {
		return mCells;
	}

	public GridCell getCell(int position) {
		if (mGrid == null || position < 0 || position >= mCells.length) {
			return null;
		}
		return mGrid.getCell(mCells[position]);
	}

	public boolean isUserMathCorrect() {
		return mUserMathCorrect;
	}
}
