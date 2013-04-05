package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.Arrays;

public class GridCage {
	// Identifiers of different versions of cage information which is stored in
	// saved game.
	private static final String SAVE_GAME_CAGE_VERSION_01 = "CAGE";
	private static final String SAVE_GAME_CAGE_VERSION_02 = "CAGE.v2";
	private static final String SAVE_GAME_CAGE_VERSION_03 = "CAGE.v3";

	public static final int ACTION_NONE = 0;
	public static final int ACTION_ADD = 1;
	public static final int ACTION_SUBTRACT = 2;
	public static final int ACTION_MULTIPLY = 3;
	public static final int ACTION_DIVIDE = 4;

	// Action for the cage
	public int mAction;

	// Number the action results in
	public int mResult;
	private String mOperator;
	// Flag to indicate whether operator (+,-,x,/) is hidden.
	private boolean mHideOperator;

	// List of cage's cells
	public ArrayList<GridCell> mCells;
	// Id of the cage
	public int mId;
	// Enclosing context
	public Grid mGrid;

	// User math is correct
	public boolean mUserMathCorrect;
	// Cage (or a cell within) is selected
	public boolean mSelected;

	// Cached list of numbers which satisfy the cage's arithmetic
	private ArrayList<int[]> mPossibles;

	/**
	 * Creates a new instance of {@link GridCage}.
	 * 
	 * @param grid
	 *            The grid in which this cage is defined.
	 */
	public GridCage(Grid grid) {
		initGridCage(grid);
	}

	/**
	 * Creates a new instance of {@link GridCage}.
	 * 
	 * @param grid
	 *            The grid in which the cage is defined.
	 * @param hideOperator
	 *            True in case the grid can be solved without using the
	 *            operators. False otherwise.
	 */
	public GridCage(Grid grid, boolean hideOperator) {
		initGridCage(grid);
		mHideOperator = hideOperator;
	}

	/**
	 * Initializes the cage variables.
	 * 
	 * @param grid
	 *            The grid in which this cage is defined.
	 */
	private void initGridCage(Grid grid) {
		this.mGrid = grid;
		mPossibles = null;
		mUserMathCorrect = true;
		mSelected = false;
		mCells = new ArrayList<GridCell>();
	}

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
		for (GridCell cell : this.mCells)
			retStr += cell.getCellNumber() + ", ";
		return retStr;
	}

	public boolean isOperatorHidden() {
		return mHideOperator;
	}

	public void revealOperator() {
		mHideOperator = false;
		setCageResults(mResult, mAction, mHideOperator);
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
		switch (mAction) {
		case ACTION_NONE:
			mOperator = "";
			break;
		case ACTION_ADD:
			mOperator = "+";
			break;
		case ACTION_SUBTRACT:
			mOperator = "-";
			break;
		case ACTION_MULTIPLY:
			mOperator = "x";
			break;
		case ACTION_DIVIDE:
			mOperator = "/";
			break;
		}
		mHideOperator = hideOperator;

		// Store cage outcome in top left cell of cage
		mCells.get(0).setCageText(mResult + (mHideOperator ? "" : mOperator));
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

	public boolean isAddMathsCorrect() {
		int total = 0;
		for (GridCell cell : this.mCells) {
			total += cell.getUserValue();
		}
		return (total == this.mResult);
	}

	public boolean isMultiplyMathsCorrect() {
		int total = 1;
		for (GridCell cell : this.mCells) {
			total *= cell.getUserValue();
		}
		return (total == this.mResult);
	}

	public boolean isDivideMathsCorrect() {
		if (this.mCells.size() != 2)
			return false;

		if (this.mCells.get(0).getUserValue() > this.mCells.get(1)
				.getUserValue())
			return this.mCells.get(0).getUserValue() == (this.mCells.get(1)
					.getUserValue() * this.mResult);
		else
			return this.mCells.get(1).getUserValue() == (this.mCells.get(0)
					.getUserValue() * this.mResult);
	}

	public boolean isSubtractMathsCorrect() {
		if (this.mCells.size() != 2)
			return false;

		if (this.mCells.get(0).getUserValue() > this.mCells.get(1)
				.getUserValue())
			return (this.mCells.get(0).getUserValue() - this.mCells.get(1)
					.getUserValue()) == this.mResult;
		else
			return (this.mCells.get(1).getUserValue() - this.mCells.get(0)
					.getUserValue()) == this.mResult;
	}

	/**
	 * Checks whether the cage arithmetic is correct using the values the user
	 * has filled in.
	 * 
	 * @param forceBorderReset
	 *            True if borders should always be reset. False in case borders
	 *            only need to be reset in cage the status of the cage math has
	 *            changed.
	 */
	public void checkCageMathsCorrect(boolean forceBorderReset) {
		boolean oldUserMathCorrect = mUserMathCorrect;

		// If not all cells in the cage are filled, the maths are not wrong.
		boolean allCellsFilledIn = true;
		for (GridCell cell : this.mCells) {
			if (!cell.isUserValueSet()) {
				mUserMathCorrect = true;
				allCellsFilledIn = false;
				break;
			}
		}

		if (allCellsFilledIn) {
			if (this.mCells.size() == 1) {
				// A single cell cage is correct in case its user value is
				// correct.
				mUserMathCorrect = mCells.get(0).isUserValueCorrect();
			} else {
				if (this.mHideOperator) {
					if (isAddMathsCorrect() || isMultiplyMathsCorrect()
							|| isDivideMathsCorrect()
							|| isSubtractMathsCorrect()) {
						mUserMathCorrect = true;
					} else {
						mUserMathCorrect = false;
					}
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
			}
		}

		if (oldUserMathCorrect != mUserMathCorrect || forceBorderReset) {
			// Reset borders in all cells of this cage
			setBorders();
		}
	}

	/**
	 * Set borders for all cells in this cage.
	 */
	public void setBorders() {
		for (GridCell cell2 : mCells) {
			cell2.setBorders();
		}
	}

	// Returns whether the user values in the cage match the cage text
	public boolean showBadCageMath(boolean mPrefShowBadCageMaths) {
		if (!mPrefShowBadCageMaths) {
			// Bad cage math should not be shown.
			return false;
		}

		// Warning will not be shown if not all cells in cage are filled
		for (GridCell cell : this.mCells)
			if (!cell.isUserValueSet()) {
				// This cell has no value
				return false;
			}

		if (this.mCells.size() == 1) {
			return !this.mCells.get(0).isUserValueCorrect();
		}

		if (this.mHideOperator) {
			if (isAddMathsCorrect() || isMultiplyMathsCorrect()
					|| isDivideMathsCorrect() || isSubtractMathsCorrect()) {
				// At least one of the operators has a correct result with
				// current cell values
				return false;
			} else {
				// None of the operators has a correct result with current cell
				// values
				return true;
			}
		} else {
			switch (this.mAction) {
			case ACTION_ADD:
				return !isAddMathsCorrect();
			case ACTION_MULTIPLY:
				return !isMultiplyMathsCorrect();
			case ACTION_DIVIDE:
				return !isDivideMathsCorrect();
			case ACTION_SUBTRACT:
				return !isSubtractMathsCorrect();
			}
		}
		throw new RuntimeException("isSolved() got to an unreachable point "
				+ this.mAction + ": " + this.toString());
	}

	public ArrayList<int[]> getPossibleNums() {
		if (mPossibles == null) {
			if (mHideOperator)
				mPossibles = setPossibleNumsNoOperator();
			else
				mPossibles = setPossibleNums();
		}
		return mPossibles;
	}

	private ArrayList<int[]> setPossibleNumsNoOperator() {
		ArrayList<int[]> AllResults = new ArrayList<int[]>();

		if (this.mAction == ACTION_NONE) {
			assert (mCells.size() == 1);
			int number[] = { mResult };
			AllResults.add(number);
			return AllResults;
		}

		int gridSize = mGrid.getGridSize();
		if (mCells.size() == 2) {
			for (int i1 = 1; i1 <= gridSize; i1++)
				for (int i2 = i1 + 1; i2 <= gridSize; i2++)
					if (i2 - i1 == mResult || i1 - i2 == mResult
							|| mResult * i1 == i2 || mResult * i2 == i1
							|| i1 + i2 == mResult || i1 * i2 == mResult) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			return AllResults;
		}

		// ACTION_ADD:
		AllResults = getAllAddCombos(gridSize, mResult, mCells.size());

		// ACTION_MULTIPLY:
		ArrayList<int[]> multResults = getAllMultiplyCombos(gridSize, mResult,
				mCells.size());

		// Combine Add & Multiply result sets
		for (int[] possibleset : multResults) {
			boolean foundset = false;
			for (int[] currentset : AllResults) {
				if (Arrays.equals(possibleset, currentset)) {
					foundset = true;
					break;
				}
			}
			if (!foundset)
				AllResults.add(possibleset);
		}

		return AllResults;
	}

	/*
	 * Generates all combinations of numbers which satisfy the cage's arithmetic
	 * and MathDoku constraints i.e. a digit can only appear once in a
	 * column/row
	 */
	private ArrayList<int[]> setPossibleNums() {
		ArrayList<int[]> AllResults = new ArrayList<int[]>();

		int gridSize = mGrid.getGridSize();

		switch (this.mAction) {
		case ACTION_NONE:
			assert (mCells.size() == 1);
			int number[] = { mResult };
			AllResults.add(number);
			break;
		case ACTION_SUBTRACT:
			assert (mCells.size() == 2);
			for (int i1 = 1; i1 <= gridSize; i1++)
				for (int i2 = i1 + 1; i2 <= gridSize; i2++)
					if (i2 - i1 == mResult || i1 - i2 == mResult) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			break;
		case ACTION_DIVIDE:
			assert (mCells.size() == 2);
			for (int i1 = 1; i1 <= gridSize; i1++)
				for (int i2 = i1 + 1; i2 <= gridSize; i2++)
					if (mResult * i1 == i2 || mResult * i2 == i1) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			break;
		case ACTION_ADD:
			AllResults = getAllAddCombos(gridSize, mResult, mCells.size());
			break;
		case ACTION_MULTIPLY:
			AllResults = getAllMultiplyCombos(gridSize, mResult, mCells.size());
			break;
		}
		return AllResults;
	}

	// The following two variables are required by the recursive methods below.
	// They could be passed as parameters of the recursive methods, but this
	// reduces performance.
	private int[] getAllCombos_Numbers;
	private ArrayList<int[]> getAllCombos_ResultSet;

	private ArrayList<int[]> getAllAddCombos(int max_val, int target_sum,
			int n_cells) {
		getAllCombos_Numbers = new int[n_cells];
		getAllCombos_ResultSet = new ArrayList<int[]>();
		getAddCombos(max_val, target_sum, n_cells);
		return getAllCombos_ResultSet;
	}

	/*
	 * Recursive method to calculate all combinations of digits which add up to
	 * target
	 * 
	 * @param max_val maximum permitted value of digit (= dimension of grid)
	 * 
	 * @param target_sum the value which all the digits should add up to
	 * 
	 * @param n_cells number of digits still to select
	 */
	private void getAddCombos(int max_val, int target_sum, int n_cells) {
		for (int n = 1; n <= max_val; n++) {
			if (n_cells == 1) {
				if (n == target_sum) {
					getAllCombos_Numbers[0] = n;
					if (satisfiesConstraints(getAllCombos_Numbers))
						getAllCombos_ResultSet.add(getAllCombos_Numbers.clone());
				}
			} else {
				getAllCombos_Numbers[n_cells - 1] = n;
				getAddCombos(max_val, target_sum - n, n_cells - 1);
			}
		}
		return;
	}

	private ArrayList<int[]> getAllMultiplyCombos(int max_val, int target_sum,
			int n_cells) {
		getAllCombos_Numbers = new int[n_cells];
		getAllCombos_ResultSet = new ArrayList<int[]>();
		getMultiplyCombos(max_val, target_sum, n_cells);

		return getAllCombos_ResultSet;
	}

	/*
	 * Recursive method to calculate all combinations of digits which multiply
	 * up to target
	 * 
	 * @param max_val maximum permitted value of digit (= dimension of grid)
	 * 
	 * @param target_sum the value which all the digits should multiply up to
	 * 
	 * @param n_cells number of digits still to select
	 */
	private void getMultiplyCombos(int max_val, int target_sum, int n_cells) {
		for (int n = 1; n <= max_val; n++) {
			if (target_sum % n != 0)
				continue;

			if (n_cells == 1) {
				if (n == target_sum) {
					getAllCombos_Numbers[0] = n;
					if (satisfiesConstraints(getAllCombos_Numbers))
						getAllCombos_ResultSet.add(getAllCombos_Numbers.clone());
				}
			} else {
				getAllCombos_Numbers[n_cells - 1] = n;
				getMultiplyCombos(max_val, target_sum / n, n_cells - 1);
			}
		}
		return;
	}

	/*
	 * Check whether the set of numbers satisfies all constraints Looking for
	 * cases where a digit appears more than once in a column/row Constraints: 0
	 * -> (mGridSize * mGridSize)-1 = column constraints (each column must
	 * contain each digit) mGridSize * mGridSize -> 2*(mGridSize * mGridSize)-1
	 * = row constraints (each row must contain each digit)
	 */
	private boolean satisfiesConstraints(int[] test_nums) {

		int gridSize = mGrid.getGridSize();

		boolean constraints[] = new boolean[gridSize * gridSize * 2];
		int constraint_num;
		for (int i = 0; i < this.mCells.size(); i++) {
			constraint_num = gridSize * (test_nums[i] - 1)
					+ mCells.get(i).getColumn();
			if (constraints[constraint_num])
				return false;
			else
				constraints[constraint_num] = true;
			constraint_num = gridSize * gridSize + gridSize
					* (test_nums[i] - 1) + mCells.get(i).getRow();
			if (constraints[constraint_num])
				return false;
			else
				constraints[constraint_num] = true;
		}
		return true;
	}

	/**
	 * Create a string representation of the Grid Cage which can be used to
	 * store a grid cage in a saved game.
	 * 
	 * @return A string representation of the grid cage.
	 */
	public String toStorageString() {
		String storageString = SAVE_GAME_CAGE_VERSION_03
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mId
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mAction
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mResult
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mOperator
				+ GameFile.FIELD_DELIMITER_LEVEL1;
		for (GridCell cell : mCells) {
			storageString += cell.getCellNumber()
					+ GameFile.FIELD_DELIMITER_LEVEL2;
		}
		storageString += GameFile.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(isOperatorHidden());

		return storageString;
	}

	/**
	 * Read cage information from or a storage string which was created with @
	 * GridCage#toStorageString()} before.
	 * 
	 * @param line
	 *            The line containing the cage information.
	 * @return True in case the given line contains cage information and is
	 *         processed correctly. False otherwise.
	 */
	public boolean fromStorageString(String line) {
		String[] cageParts = line.split(GameFile.FIELD_DELIMITER_LEVEL1);

		// Check version of stored cage information
		int cageInformationVersion = 0;
		if (cageParts[0].equals(SAVE_GAME_CAGE_VERSION_01)) {
			cageInformationVersion = 1;
		} else if (cageParts[0].equals(SAVE_GAME_CAGE_VERSION_02)) {
			cageInformationVersion = 2;
		} else if (cageParts[0].equals(SAVE_GAME_CAGE_VERSION_03)) {
			cageInformationVersion = 3;
		} else {
			return false;
		}

		// Process all parts
		int index = 1;
		mId = Integer.parseInt(cageParts[index++]);
		mAction = Integer.parseInt(cageParts[index++]);
		mResult = Integer.parseInt(cageParts[index++]);
		if (cageInformationVersion == 1) {
			// Version 1 contained the cage type at this position but this field
			// is not needed anymore to restore a cage. So skip this field.
			index++;
		}
		if (cageInformationVersion >= 3) {
			mOperator = cageParts[index++];
		}
		for (String cellId : cageParts[index++]
				.split(GameFile.FIELD_DELIMITER_LEVEL2)) {
			GridCell c = mGrid.mCells.get(Integer.parseInt(cellId));
			c.setCageId(mId);
			mCells.add(c);
		}
		if (cageInformationVersion == 1 && cageParts.length == 6) {
			// Version 1 with 6 cage parts does not contain the mOperatorHidden
			// part while the version with 7 parts does contain this field.
			mHideOperator = false;
		} else {
			mHideOperator = Boolean.parseBoolean(cageParts[index++]);
		}

		return true;
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
}