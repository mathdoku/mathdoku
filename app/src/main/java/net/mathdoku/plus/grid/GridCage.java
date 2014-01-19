package net.mathdoku.plus.grid;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.gridGenerating.ComboGenerator;
import net.mathdoku.plus.storage.GridCageStorage;

import java.util.ArrayList;

public class GridCage {
	private int mId;
	private CageOperator mCageOperator;
	private int mResult;
	private boolean mHideOperator;
	public ArrayList<GridCell> mCells;

	// Enclosing context
	private Grid mGrid;

	// User math is correct
	private boolean mUserMathCorrect;

	// Cached list of possible combo's which can be used in this cage if other
	// cages in the grid were not relevant.
	private ArrayList<int[]> mPossibleCombos;

	/**
	 * Creates a new instance of {@link GridCage}.
	 */
	public GridCage() {
		initGridCage();
		if (mCells == null) {
			mCells = new ArrayList<GridCell>();
		}
	}

	/**
	 * Creates a new instance of {@link GridCage}.
	 * 
	 * @param hideOperator
	 *            True in case the grid can be solved without using the
	 *            operators. False otherwise.
	 */
	public GridCage(boolean hideOperator) {
		initGridCage();
		mHideOperator = hideOperator;
		if (mCells == null) {
			mCells = new ArrayList<GridCell>();
		}
	}

	/**
	 * Creates a new instance of {@link GridCage}.
	 */
	public GridCage(GridCageStorage gridCageStorage) {
		initGridCage();
		mId = gridCageStorage.getId();
		mHideOperator = gridCageStorage.isHideOperator();
		mResult = gridCageStorage.getResult();
		mCageOperator = gridCageStorage.getCageOperator();
		mCells = gridCageStorage.getCells();
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
		String retStr = "";
		retStr += "Cage id: " + mId + ", Size: "
				+ (mCells == null ? 0 : mCells.size());
		retStr += ", Action: " + mCageOperator.toString();
		retStr += ", Result: " + mResult;
		retStr += ", cells: ";
		if (mCells != null) {
			for (GridCell cell : mCells)
				retStr += cell.getCellId() + ", ";
		}
		return retStr;
	}

	public boolean isOperatorHidden() {
		return mHideOperator;
	}

	public void revealOperator() {
		mHideOperator = false;
		setCageResults(mResult, mCageOperator, false);
	}

	/**
	 * Set the result and operator for this cage.
	 * 
	 * @param resultValue
	 *            The resulting value of the cage when applying the given cageOperator
	 *            on the cell values in the cage.
	 * @param cageOperator
	 *            The cageOperator to be applied on the cell values in this cage.
	 * @param hideOperator
	 *            True in case the operator of this cage can be hidden but the
	 *            puzzle can still be solved.
	 */
	public void setCageResults(int resultValue, CageOperator cageOperator, boolean hideOperator) {
		// Store results in cage object
		mResult = resultValue;
		mCageOperator = cageOperator;
		mHideOperator = hideOperator;

		// Store cage outcome in top left cell of cage
		mCells.get(0).setCageText(mResult + (mHideOperator ? "" : mCageOperator.getSign()));
	}

	/**
	 * Clears the cage result form the cage and the top left cell in the cage.
	 */
	public void clearCageResult() {
		mResult = 0;
		mCageOperator = CageOperator.NONE;

		// Remove outcome from top left cell of cage
		mCells.get(0).setCageText("");
	}

	/*
	 * Sets the cageId of the cage's cells.
	 */
	public void setCageId(int id) {
		mId = id;
		for (GridCell cell : mCells)
			cell.setCageId(mId);
	}

	boolean isAddMathsCorrect() {
		int total = 0;
		for (GridCell cell : mCells) {
			total += cell.getUserValue();
		}
		return (total == mResult);
	}

	boolean isMultiplyMathsCorrect() {
		int total = 1;
		for (GridCell cell : mCells) {
			total *= cell.getUserValue();
		}
		return (total == mResult);
	}

	boolean isDivideMathsCorrect() {
		if (mCells.size() != 2)
			return false;

		if (mCells.get(0).getUserValue() > mCells
				.get(1)
				.getUserValue())
			return mCells.get(0).getUserValue() == (mCells
					.get(1)
					.getUserValue() * mResult);
		else
			return mCells.get(1).getUserValue() == (mCells
					.get(0)
					.getUserValue() * mResult);
	}

	boolean isSubtractMathsCorrect() {
		if (mCells.size() != 2)
			return false;

		if (mCells.get(0).getUserValue() > mCells
				.get(1)
				.getUserValue())
			return (mCells.get(0).getUserValue() - mCells
					.get(1)
					.getUserValue()) == mResult;
		else
			return (mCells.get(1).getUserValue() - mCells
					.get(0)
					.getUserValue()) == mResult;
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
		// If cage has no cells, the maths are not wrong
		if (mCells == null || mCells.size() == 0) {
			return true;
		}

		boolean oldUserMathCorrect = mUserMathCorrect;
		if (allCellsFilledWithUserValue()) {
			if (mHideOperator) {
				mUserMathCorrect = isAddMathsCorrect()
						|| isMultiplyMathsCorrect() || isDivideMathsCorrect()
						|| isSubtractMathsCorrect();
			} else {
				switch (mCageOperator) {
				case ADD:
					mUserMathCorrect = isAddMathsCorrect();
					break;
				case MULTIPLY:
					mUserMathCorrect = isMultiplyMathsCorrect();
					break;
				case DIVIDE:
					mUserMathCorrect = isDivideMathsCorrect();
					break;
				case SUBTRACT:
					mUserMathCorrect = isSubtractMathsCorrect();
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
	public void setBorders() {
		for (GridCell cell2 : mCells) {
			cell2.setBorders();
		}
	}

	/**
	 * Sets the reference to the grid to which this cage belongs.
	 * 
	 * @param grid
	 *            The grid to which the cage belongs.
	 */
	public void setGridReference(Grid grid) {
		mGrid = grid;
	}

	private boolean allCellsFilledWithUserValue() {
		if (mCells != null) {
			for (GridCell gridCell : mCells) {
				if (!gridCell.isUserValueSet()) {
					return false;
				}
			}
		}
		return true;
	}

	public ArrayList<int[]> setPossibleCombos(int gridSize) {
		ComboGenerator comboGenerator = new ComboGenerator(mResult, mCageOperator,
				mHideOperator, mCells, gridSize);
		mPossibleCombos = comboGenerator.getPossibleCombos();

		return mPossibleCombos;
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

	public ArrayList<GridCell> getCells() {
		// Return copy of the cell list so the requesting object cannot
		// manipulate the original list.
		return new ArrayList<GridCell>(mCells);
	}

	public boolean isUserMathCorrect() {
		return mUserMathCorrect;
	}
}
