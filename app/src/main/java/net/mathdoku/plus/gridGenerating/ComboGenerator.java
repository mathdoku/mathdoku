package net.mathdoku.plus.gridGenerating;

import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;

import java.util.ArrayList;
import java.util.Arrays;

public class ComboGenerator {
	public enum Operator {
		NONE, ADD, SUBTRACT, MULTIPLY, DIVIDE
	}

	private int mResult;
	private Operator mCageOperator;
	private boolean mHideOperator;
	private ArrayList<GridCell> mCageCells;
	private int mGridSize;

	public ComboGenerator(int result, int action, boolean hideOperator,
			ArrayList<GridCell> cageCells, int gridSize) {
		mResult = result;
		// TODO: change GridCage.ACTION* to Operator-enum
		switch (action) {
		case GridCage.ACTION_NONE:
			mCageOperator = Operator.NONE;
			break;
		case GridCage.ACTION_ADD:
			mCageOperator = Operator.ADD;
			break;
		case GridCage.ACTION_SUBTRACT:
			mCageOperator = Operator.SUBTRACT;
			break;
		case GridCage.ACTION_MULTIPLY:
			mCageOperator = Operator.MULTIPLY;
			break;
		case GridCage.ACTION_DIVIDE:
			mCageOperator = Operator.DIVIDE;
			break;
		}
		mHideOperator = hideOperator;
		mCageCells = cageCells;
		mGridSize = gridSize;
	}

	/**
	 * Get all possible combinations for a specific cage.
	 * 
	 * @return The list of all possible combinations. Null in case no
	 *         combinations or too many permutations have been found.
	 */
	public ArrayList<int[]> getPossibleCombos() {
		if (mHideOperator
				|| (mCageOperator == Operator.NONE && mCageCells.size() > 1)) {
			return setPossibleCombosHiddenOperator();
		} else {
			return setPossibleCombosVisibleOperator();
		}
	}

	/**
	 * Get all permutations of cell values for this cage.
	 * 
	 * @return The list of all permutations of cell values which can be used for
	 *         this cage.
	 */
	private ArrayList<int[]> setPossibleCombosHiddenOperator() {
		ArrayList<int[]> resultCombos = new ArrayList<int[]>();

		// Single cell cages can only contain the value of the single cell.
		if (mCageCells.size() == 1) {
			int number[] = { mResult };
			resultCombos.add(number);
			return resultCombos;
		}

		// Cages of size two can contain any operation
		if (mCageCells.size() == 2) {
			for (int i1 = 1; i1 <= mGridSize; i1++) {
				for (int i2 = i1 + 1; i2 <= mGridSize; i2++) {
					if (i2 - i1 == mResult || i1 - i2 == mResult
							|| mResult * i1 == i2 || mResult * i2 == i1
							|| i1 + i2 == mResult || i1 * i2 == mResult) {
						int numbers[] = { i1, i2 };
						resultCombos.add(numbers);
						numbers = new int[] { i2, i1 };
						resultCombos.add(numbers);
					}
				}
			}
			return resultCombos;
		}

		// Cages of size two and above can only contain an add or a multiply
		// operation
		resultCombos = getAllAddCombos(mGridSize, mResult, mCageCells.size());
		ArrayList<int[]> multiplyCombos = getAllMultiplyCombos(mGridSize,
				mResult, mCageCells.size());

		// Combine Add & Multiply result sets
		for (int[] multiplyCombo : multiplyCombos) {
			boolean newCombo = true;
			for (int[] resultCombo : resultCombos) {
				if (Arrays.equals(multiplyCombo, resultCombo)) {
					newCombo = false;
					break;
				}
			}
			if (newCombo) {
				resultCombos.add(multiplyCombo);
			}
		}

		return resultCombos;
	}

	/*
	 * Generates all combinations of numbers which satisfy the cage's arithmetic
	 * and MathDoku constraints i.e. a digit can only appear once in a
	 * column/row
	 */
	private ArrayList<int[]> setPossibleCombosVisibleOperator() {
		ArrayList<int[]> AllResults = new ArrayList<int[]>();

		switch (mCageOperator) {
		case NONE:
			assert (mCageCells.size() == 1);
			int number[] = { mResult };
			AllResults.add(number);
			break;
		case SUBTRACT:
			assert (mCageCells.size() == 2);
			for (int i1 = 1; i1 <= mGridSize; i1++)
				for (int i2 = i1 + 1; i2 <= mGridSize; i2++)
					if (i2 - i1 == mResult || i1 - i2 == mResult) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			break;
		case DIVIDE:
			assert (mCageCells.size() == 2);
			for (int i1 = 1; i1 <= mGridSize; i1++)
				for (int i2 = i1 + 1; i2 <= mGridSize; i2++)
					if (mResult * i1 == i2 || mResult * i2 == i1) {
						int numbers[] = { i1, i2 };
						AllResults.add(numbers);
						numbers = new int[] { i2, i1 };
						AllResults.add(numbers);
					}
			break;
		case ADD:
			AllResults = getAllAddCombos(mGridSize, mResult, mCageCells.size());
			break;
		case MULTIPLY:
			AllResults = getAllMultiplyCombos(mGridSize, mResult,
					mCageCells.size());
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
						getAllCombos_ResultSet
								.add(getAllCombos_Numbers.clone());
				}
			} else {
				getAllCombos_Numbers[n_cells - 1] = n;
				getAddCombos(max_val, target_sum - n, n_cells - 1);
			}
		}
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
						getAllCombos_ResultSet
								.add(getAllCombos_Numbers.clone());
				}
			} else {
				getAllCombos_Numbers[n_cells - 1] = n;
				getMultiplyCombos(max_val, target_sum / n, n_cells - 1);
			}
		}
	}

	/*
	 * Check whether the set of numbers satisfies all constraints Looking for
	 * cases where a digit appears more than once in a column/row Constraints: 0
	 * -> (mGridSize * mGridSize)-1 = column constraints (each column must
	 * contain each digit) mGridSize * mGridSize -> 2*(mGridSize * mGridSize)-1
	 * = row constraints (each row must contain each digit)
	 */
	private boolean satisfiesConstraints(int[] possibles) {
		boolean constraints[] = new boolean[mGridSize * mGridSize * 2];
		int constraint_num;
		for (int i = 0; i < this.mCageCells.size(); i++) {
			constraint_num = mGridSize * (possibles[i] - 1)
					+ mCageCells.get(i).getColumn();
			if (constraints[constraint_num]) {
				return false;
			} else {
				constraints[constraint_num] = true;
				constraint_num = mGridSize * mGridSize + mGridSize
						* (possibles[i] - 1) + mCageCells.get(i).getRow();
			}
			if (constraints[constraint_num]) {
				return false;
			} else {
				constraints[constraint_num] = true;
			}
		}
		return true;
	}
}
