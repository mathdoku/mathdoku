package net.cactii.mathdoku;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import net.cactii.mathdoku.GridCell.BorderType;

import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;

public class GridCage {
	// Identifiers of different versions of cage information which is stored in
	// saved game.
	private static final String SAVE_GAME_CAGE_VERSION_01 = "CAGE";
	private static final String SAVE_GAME_CAGE_VERSION_02 = "CAGE.v2";

	public static final int ACTION_NONE = 0;
	public static final int ACTION_ADD = 1;
	public static final int ACTION_SUBTRACT = 2;
	public static final int ACTION_MULTIPLY = 3;
	public static final int ACTION_DIVIDE = 4;

	// Action for the cage
	public int mAction;
	// Number the action results in
	public int mResult;
	// List of cage's cells
	public ArrayList<GridCell> mCells;
	// Id of the cage
	public int mId;
	// Enclosing context
	public GridView mContext;
	// User math is correct
	public boolean mUserMathCorrect;
	// Cage (or a cell within) is selected
	public boolean mSelected;
	// Flag to indicate whether operator (+,-,x,/) is hidden
	private boolean mOperatorHidden;
	// Cached list of numbers which satisfy the cage's arithmetic
	private ArrayList<int[]> mPossibles;

	/**
	 * Creates a new instance of {@link GridCage}.
	 * 
	 * @param gridView
	 *            The grid view in which context this cage is defined.
	 */
	public GridCage(GridView gridView) {
		initGridCage(gridView);
	}

	/**
	 * Creates a new instance of {@link GridCage}.
	 * 
	 * @param gridView
	 *            The grid view in which context this cage is defined.
	 * @param hiddenoperator
	 *            True in case the operator should be hidden. False otherwise.
	 */
	public GridCage(GridView gridView, boolean hiddenoperator) {
		initGridCage(gridView);
		mOperatorHidden = hiddenoperator;
	}

	/**
	 * Initializes the grid view variables.
	 * 
	 * @param gridView
	 *            The grid view in which context this cage is defined.
	 */
	private void initGridCage(GridView gridView) {
		this.mContext = gridView;
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
		return mOperatorHidden;
	}

	public void setOperatorHidden(boolean operatorHidden) {
		this.mOperatorHidden = operatorHidden;
		this.mPossibles = null; // Clear cached list of possible numbers
	}

	/*
	 * Generates the arithmetic for the cage, semi-randomly.
	 * 
	 * - If a cage has 3 or more cells, it can only be an add or multiply. -
	 * else if the cells are evenly divisible, division is used, else
	 * subtraction.
	 */
	public void setArithmetic() {
		this.mAction = -1;
		if (this.mCells.size() == 1) {
			this.mAction = ACTION_NONE;
			this.mResult = this.mCells.get(0).getCorrectValue();
			this.mCells.get(0).setCageText("" + this.mResult);
			return;
		}
		double rand = this.mContext.mRandom.nextDouble();
		double addChance = 0.25;
		double multChance = 0.5;
		if (this.mCells.size() > 2) {
			addChance = 0.5;
			multChance = 1.0;
		}
		if (rand <= addChance)
			this.mAction = ACTION_ADD;
		else if (rand <= multChance)
			this.mAction = ACTION_MULTIPLY;

		if (this.mAction == ACTION_ADD) {
			int total = 0;
			for (GridCell cell : this.mCells) {
				total += cell.getCorrectValue();
			}
			this.mResult = total;
			if (mOperatorHidden)
				this.mCells.get(0).setCageText(this.mResult + "");
			else
				this.mCells.get(0).setCageText(this.mResult + "+");
		}
		if (this.mAction == ACTION_MULTIPLY) {
			int total = 1;
			for (GridCell cell : this.mCells) {
				total *= cell.getCorrectValue();
			}
			this.mResult = total;
			if (mOperatorHidden)
				this.mCells.get(0).setCageText(this.mResult + "");
			else
				this.mCells.get(0).setCageText(this.mResult + "x");
		}
		if (this.mAction > -1) {
			return;
		}

		if (this.mCells.size() < 2) {
			Log.d("KenKen", "Why only length 1? Type: " + this);
		}
		int cell1Value = this.mCells.get(0).getCorrectValue();
		int cell2Value = this.mCells.get(1).getCorrectValue();
		int higher = cell1Value;
		int lower = cell2Value;
		boolean canDivide = false;
		if (cell1Value < cell2Value) {
			higher = cell2Value;
			lower = cell1Value;
		}
		if (higher % lower == 0)
			canDivide = true;
		if (canDivide) {
			this.mResult = higher / lower;
			this.mAction = ACTION_DIVIDE;
			// this.mCells.get(0).mCageText = this.mResult + "\367";
			if (mOperatorHidden)
				this.mCells.get(0).setCageText(this.mResult + "");
			else
				this.mCells.get(0).setCageText(this.mResult + "/");
		} else {
			this.mResult = higher - lower;
			this.mAction = ACTION_SUBTRACT;
			if (mOperatorHidden)
				this.mCells.get(0).setCageText(this.mResult + "");
			else
				this.mCells.get(0).setCageText(this.mResult + "-");
		}
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

	// Returns whether the user values in the cage match the cage text
	public boolean isMathsCorrect() {
		if (this.mCells.size() == 1)
			return this.mCells.get(0).isUserValueCorrect();

		if (this.mOperatorHidden) {
			if (isAddMathsCorrect() || isMultiplyMathsCorrect()
					|| isDivideMathsCorrect() || isSubtractMathsCorrect())
				return true;
			else
				return false;
		} else {
			switch (this.mAction) {
			case ACTION_ADD:
				return isAddMathsCorrect();
			case ACTION_MULTIPLY:
				return isMultiplyMathsCorrect();
			case ACTION_DIVIDE:
				return isDivideMathsCorrect();
			case ACTION_SUBTRACT:
				return isSubtractMathsCorrect();
			}
		}
		throw new RuntimeException("isSolved() got to an unreachable point "
				+ this.mAction + ": " + this.toString());
	}

	// Determine whether user entered values match the arithmetic.
	//
	// Only marks cells bad if all cells have a uservalue, and they dont
	// match the arithmetic hint.
	public void userValuesCorrect() {
		this.mUserMathCorrect = true;
		for (GridCell cell : this.mCells)
			if (!cell.isUserValueSet()) {
				this.setBorders();
				return;
			}

		this.mUserMathCorrect = this.isMathsCorrect();
		this.setBorders();
	}

	/*
	 * Sets the borders of the cage's cells.
	 */
	public void setBorders() {
		for (GridCell cell : this.mCells) {
			cell.borderTypeTop = BorderType.NONE;
			cell.borderTypeRight = BorderType.NONE;
			cell.borderTypeBottom = BorderType.NONE;
			cell.borderTypeLeft = BorderType.NONE;
			if (this.mContext.CageIdAt(cell.getRow() - 1, cell.getColumn()) != this.mId) {
				if (!this.mUserMathCorrect && this.mContext.mBadMaths) {
					cell.borderTypeTop = BorderType.CELL_WARNING;
				} else if (this.mSelected) {
					cell.borderTypeTop = BorderType.OUTER_OF_CAGE_SELECTED;
				} else {
					cell.borderTypeTop = BorderType.OUTER_OF_CAGE_NOT_SELECTED;
				}
			}

			if (this.mContext.CageIdAt(cell.getRow(), cell.getColumn() + 1) != this.mId) {
				if (!this.mUserMathCorrect && this.mContext.mBadMaths) {
					cell.borderTypeRight = BorderType.CELL_WARNING;
				} else if (this.mSelected) {
					cell.borderTypeRight = BorderType.OUTER_OF_CAGE_SELECTED;
				} else {
					cell.borderTypeRight = BorderType.OUTER_OF_CAGE_NOT_SELECTED;
				}
			}

			if (this.mContext.CageIdAt(cell.getRow() + 1, cell.getColumn()) != this.mId) {
				if (!this.mUserMathCorrect && this.mContext.mBadMaths) {
					cell.borderTypeBottom = BorderType.CELL_WARNING;
				} else if (this.mSelected) {
					cell.borderTypeBottom = BorderType.OUTER_OF_CAGE_SELECTED;
				} else {
					cell.borderTypeBottom = BorderType.OUTER_OF_CAGE_NOT_SELECTED;
				}
			}

			if (this.mContext.CageIdAt(cell.getRow(), cell.getColumn() - 1) != this.mId) {
				if (!this.mUserMathCorrect && this.mContext.mBadMaths) {
					cell.borderTypeLeft = BorderType.CELL_WARNING;
				} else if (this.mSelected) {
					cell.borderTypeLeft = BorderType.OUTER_OF_CAGE_SELECTED;
				} else {
					cell.borderTypeLeft = BorderType.OUTER_OF_CAGE_NOT_SELECTED;
				}
			}
		}
	}

	public ArrayList<int[]> getPossibleNums() {
		if (mPossibles == null) {
			if (mOperatorHidden)
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

		if (mCells.size() == 2) {
			for (int i1 = 1; i1 <= this.mContext.mGridSize; i1++)
				for (int i2 = i1 + 1; i2 <= this.mContext.mGridSize; i2++)
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
		AllResults = getalladdcombos(this.mContext.mGridSize, mResult,
				mCells.size());

		// ACTION_MULTIPLY:
		ArrayList<int[]> multResults = getallmultcombos(
				this.mContext.mGridSize, mResult, mCells.size());

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

		switch (this.mAction) {
		case ACTION_NONE:
			assert (mCells.size() == 1);
			int number[] = { mResult };
			AllResults.add(number);
			break;
		case ACTION_SUBTRACT:
			assert (mCells.size() == 2);
			for (int i1 = 1; i1 <= this.mContext.mGridSize; i1++)
				for (int i2 = i1 + 1; i2 <= this.mContext.mGridSize; i2++)
					if (i2 - i1 == mResult || i1 - i2 == mResult) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			break;
		case ACTION_DIVIDE:
			assert (mCells.size() == 2);
			for (int i1 = 1; i1 <= this.mContext.mGridSize; i1++)
				for (int i2 = i1 + 1; i2 <= this.mContext.mGridSize; i2++)
					if (mResult * i1 == i2 || mResult * i2 == i1) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			break;
		case ACTION_ADD:
			AllResults = getalladdcombos(this.mContext.mGridSize, mResult,
					mCells.size());
			break;
		case ACTION_MULTIPLY:
			AllResults = getallmultcombos(this.mContext.mGridSize, mResult,
					mCells.size());
			break;
		}
		return AllResults;
	}

	// The following two variables are required by the recursive methods below.
	// They could be passed as parameters of the recursive methods, but this
	// reduces performance.
	private int[] numbers;
	private ArrayList<int[]> result_set;

	private ArrayList<int[]> getalladdcombos(int max_val, int target_sum,
			int n_cells) {
		numbers = new int[n_cells];
		result_set = new ArrayList<int[]>();
		getaddcombos(max_val, target_sum, n_cells);
		return result_set;
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
	private void getaddcombos(int max_val, int target_sum, int n_cells) {
		for (int n = 1; n <= max_val; n++) {
			if (n_cells == 1) {
				if (n == target_sum) {
					numbers[0] = n;
					if (satisfiesConstraints(numbers))
						result_set.add(numbers.clone());
				}
			} else {
				numbers[n_cells - 1] = n;
				getaddcombos(max_val, target_sum - n, n_cells - 1);
			}
		}
		return;
	}

	private ArrayList<int[]> getallmultcombos(int max_val, int target_sum,
			int n_cells) {
		numbers = new int[n_cells];
		result_set = new ArrayList<int[]>();
		getmultcombos(max_val, target_sum, n_cells);

		return result_set;
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
	private void getmultcombos(int max_val, int target_sum, int n_cells) {
		for (int n = 1; n <= max_val; n++) {
			if (target_sum % n != 0)
				continue;

			if (n_cells == 1) {
				if (n == target_sum) {
					numbers[0] = n;
					if (satisfiesConstraints(numbers))
						result_set.add(numbers.clone());
				}
			} else {
				numbers[n_cells - 1] = n;
				getmultcombos(max_val, target_sum / n, n_cells - 1);
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

		boolean constraints[] = new boolean[mContext.mGridSize
				* mContext.mGridSize * 2];
		int constraint_num;
		for (int i = 0; i < this.mCells.size(); i++) {
			constraint_num = mContext.mGridSize * (test_nums[i] - 1)
					+ mCells.get(i).getColumn();
			if (constraints[constraint_num])
				return false;
			else
				constraints[constraint_num] = true;
			constraint_num = mContext.mGridSize * mContext.mGridSize
					+ mContext.mGridSize * (test_nums[i] - 1)
					+ mCells.get(i).getRow();
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
		String storageString = SAVE_GAME_CAGE_VERSION_02
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mId
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mAction
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mResult
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
		for (String cellId : cageParts[index]
				.split(GameFile.FIELD_DELIMITER_LEVEL2)) {
			GridCell c = mContext.mCells.get(Integer.parseInt(cellId));
			c.setCageId(mId);
			mCells.add(c);
		}
		if (cageInformationVersion == 1 && cageParts.length == 6) {
			// Version 1 with 6 cage parts does not contain the mOperatorHidden
			// part while the version with 7 parts does contain this field.
			mOperatorHidden = false;
		} else {
			mOperatorHidden = Boolean.parseBoolean(cageParts[index++]);
		}

		return true;
	}
}