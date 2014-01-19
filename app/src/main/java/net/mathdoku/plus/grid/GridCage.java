package net.mathdoku.plus.grid;

import net.mathdoku.plus.gridGenerating.ComboGenerator;
import net.mathdoku.plus.storage.GridCageStorage;

import java.util.ArrayList;

public class GridCage {
	public static final int ACTION_NONE = 0;
	public static final int ACTION_ADD = 1;
	public static final int ACTION_SUBTRACT = 2;
	public static final int ACTION_MULTIPLY = 3;
	public static final int ACTION_DIVIDE = 4;

	public int mId;

	public int mAction;
	public int mResult;
	private boolean mHideOperator;
	public ArrayList<GridCell> mCells;

	// Enclosing context
	private Grid mGrid;

	// User math is correct
	public boolean mUserMathCorrect;

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
		mAction = gridCageStorage.getAction();
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
		retStr += "Cage id: " + this.mId + ", Size: "
				+ (this.mCells == null ? 0 : this.mCells.size());
		retStr += ", Action: ";
		switch (this.mAction) {
		case ACTION_NONE:
			retStr += "None";
			break;
		case ACTION_ADD:
			retStr += "Add";
			break;
		case ACTION_SUBTRACT:
			retStr += "Subtract";
			break;
		case ACTION_MULTIPLY:
			retStr += "Multiply";
			break;
		case ACTION_DIVIDE:
			retStr += "Divide";
			break;
		}
		retStr += ", Result: " + this.mResult;
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
		setCageResults(mResult, mAction, false);
	}

	/**
	 * Set the result and operator for this cage.
	 * 
	 * @param resultValue
	 *            The resulting value of the cage when applying the given action
	 *            on the cell values in the cage.
	 * @param action
	 *            The action to be applied on the cell values in this cage.
	 * @param hideOperator
	 *            True in case the operator of this cage can be hidden but the
	 *            puzzle can still be solved.
	 */
	public void setCageResults(int resultValue, int action, boolean hideOperator) {
		// Store results in cage object
		mResult = resultValue;
		mAction = action;
		String operator = "";
		switch (mAction) {
		case ACTION_NONE:
			operator = "";
			break;
		case ACTION_ADD:
			operator = "+";
			break;
		case ACTION_SUBTRACT:
			operator = "-";
			break;
		case ACTION_MULTIPLY:
			operator = "x";
			break;
		case ACTION_DIVIDE:
			operator = "/";
			break;
		}
		mHideOperator = hideOperator;

		// Store cage outcome in top left cell of cage
		mCells.get(0).setCageText(mResult + (mHideOperator ? "" : operator));
	}

	/**
	 * Clears the cage result form the cage and the top left cell in the cage.
	 */
	public void clearCageResult() {
		mResult = 0;
		mAction = ACTION_NONE;

		// Remove outcome from top left cell of cage
		mCells.get(0).setCageText("");
	}

	/*
	 * Sets the cageId of the cage's cells.
	 */
	public void setCageId(int id) {
		this.mId = id;
		for (GridCell cell : this.mCells)
			cell.setCageId(this.mId);
	}

	boolean isAddMathsCorrect() {
		int total = 0;
		for (GridCell cell : this.mCells) {
			total += cell.getUserValue();
		}
		return (total == this.mResult);
	}

	boolean isMultiplyMathsCorrect() {
		int total = 1;
		for (GridCell cell : this.mCells) {
			total *= cell.getUserValue();
		}
		return (total == this.mResult);
	}

	boolean isDivideMathsCorrect() {
		if (this.mCells.size() != 2)
			return false;

		if (this.mCells.get(0).getUserValue() > this.mCells
				.get(1)
				.getUserValue())
			return this.mCells.get(0).getUserValue() == (this.mCells
					.get(1)
					.getUserValue() * this.mResult);
		else
			return this.mCells.get(1).getUserValue() == (this.mCells
					.get(0)
					.getUserValue() * this.mResult);
	}

	boolean isSubtractMathsCorrect() {
		if (this.mCells.size() != 2)
			return false;

		if (this.mCells.get(0).getUserValue() > this.mCells
				.get(1)
				.getUserValue())
			return (this.mCells.get(0).getUserValue() - this.mCells
					.get(1)
					.getUserValue()) == this.mResult;
		else
			return (this.mCells.get(1).getUserValue() - this.mCells
					.get(0)
					.getUserValue()) == this.mResult;
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
			if (this.mHideOperator) {
				mUserMathCorrect = isAddMathsCorrect()
						|| isMultiplyMathsCorrect() || isDivideMathsCorrect()
						|| isSubtractMathsCorrect();
			} else {
				switch (this.mAction) {
				case ACTION_ADD:
					mUserMathCorrect = isAddMathsCorrect();
					break;
				case ACTION_MULTIPLY:
					mUserMathCorrect = isMultiplyMathsCorrect();
					break;
				case ACTION_DIVIDE:
					mUserMathCorrect = isDivideMathsCorrect();
					break;
				case ACTION_SUBTRACT:
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
		ComboGenerator.Operator operator;
		switch (mAction) {
		case ACTION_NONE:
			operator = ComboGenerator.Operator.NONE;
			break;
		case ACTION_ADD:
			operator = ComboGenerator.Operator.ADD;
			break;
		case ACTION_SUBTRACT:
			operator = ComboGenerator.Operator.SUBTRACT;
			break;
		case ACTION_MULTIPLY:
			operator = ComboGenerator.Operator.MULTIPLY;
			break;
		case ACTION_DIVIDE:
			operator = ComboGenerator.Operator.DIVIDE;
			break;
		}
		ComboGenerator comboGenerator = new ComboGenerator(mResult, mAction,
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

	public int getAction() {
		return mAction;
	}

	public int getResult() {
		return mResult;
	}

	public ArrayList<GridCell> getCells() {
		// Return copy of the cell list so the requesting object cannot
		// manipulate the original list.
		return new ArrayList<GridCell>(mCells);
	}
}
