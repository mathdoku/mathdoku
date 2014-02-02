package net.mathdoku.plus.gridGenerating;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComboGenerator {
	private int mResult;
	private CageOperator mCageOperator;
	private boolean mHideOperator;
	private List<GridCell> mCageCells;
	private int mGridSize;

	public ComboGenerator(int gridSize) {
		mGridSize = gridSize;
	}

	/**
	 * Get all possible combinations for the given cage.
	 *
	 * @param gridCage
	 *            The cage for which all possible combo's have to be determined.
	 * @param cells The list of cells for this cage.
	 * @return The list of all possible combinations. Null in case no
	 *         combinations or too many permutations have been found.
	 */
	public ArrayList<int[]> getPossibleCombos(GridCage gridCage, List<GridCell> cells) {
		mResult = gridCage.getResult();
		mCageOperator = gridCage.getOperator();
		mHideOperator = gridCage.isOperatorHidden();
		mCageCells = cells;

		if (mHideOperator) {
			return getPossibleCombosHiddenOperator();
		} else {
			return getPossibleCombosVisibleOperator();
		}
	}

	/**
	 * Get all permutations of cell values for this cage.
	 *
	 * @return The list of all permutations of cell values which can be used for
	 *         this cage.
	 */
	private ArrayList<int[]> getPossibleCombosHiddenOperator() {
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
	private ArrayList<int[]> getPossibleCombosVisibleOperator() {
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

	/**
	 * Checks if the given permutation can be filled in in the cells of the
	 * cages without violating the rule that a digit can be used only once on
	 * each row and each column.
	 *
	 * @param possibles
	 *            The permutation which has to be checked.
	 */
	private boolean satisfiesConstraints(int[] possibles) {
		// The first dimension for rowConstraints holds the different rows of
		// the grid. The second dimension indicates whether digit (columnIndex +
		// 1) is used in this row.
		boolean[][] rowConstraints = new boolean[mGridSize][mGridSize];

		// The first dimension for columnConstraints holds the different columns
		// of the grid. The second dimension indicates whether digit
		// (columnIndex + 1) is used in this column.
		boolean[][] columnConstraints = new boolean[mGridSize][mGridSize];

		// The values of the given permutation are copied in the specified order
		// to the cells of the cages.
		int rowConstraintsDimension1;
		int columnConstraintsDimension1;
		int constraintsDimension2;
		for (int i = 0; i < this.mCageCells.size(); i++) {
			// The actual position of i-th cell in the grid determines the first dimension of the constraint arrays.
			rowConstraintsDimension1 = mCageCells.get(i).getRow();
			columnConstraintsDimension1 = mCageCells.get(i).getColumn();

			// The value of the i-th position of the permutation determines the second dimension for both constraint arrays.
			constraintsDimension2 = possibles[i] - 1;

			if (rowConstraints[rowConstraintsDimension1][constraintsDimension2]) {
				// The value is already used on this row of the grid
				return false;
			}
			rowConstraints[rowConstraintsDimension1][constraintsDimension2] = true;


			if (columnConstraints[columnConstraintsDimension1][constraintsDimension2]) {
				// The value is already used on this column of the grid.
				return false;
			}
			columnConstraints[columnConstraintsDimension1][constraintsDimension2] = true;
		}

		// This permutation can be used to fill the cells of the cage without violation the rules.
		return true;
	}
}
