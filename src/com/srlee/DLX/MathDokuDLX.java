package com.srlee.DLX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.cactii.mathdoku.GridCage;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import android.util.Log;

public class MathDokuDLX extends DLX {
	private static final String TAG = "MathDoku.MathDokuDLX";

	// Remove "&& false" in following line to show debug information about
	// filling the DLX data structure when running in development mode.
	public static final boolean DEBUG_DLX = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	private final int mGridSize;
	private int mTotalMoves;

	// The list of cages for which the solution has to be checked
	private final ArrayList<GridCage> mCages;

	// Additional data structure in case the solution has to be uncovered.
	private class Move {
		protected int mCageId;
		protected int mSolutionRow;
		protected int mCellRow;
		protected int mCellCol;
		protected int mCellValue;

		public Move(int cageId, int solutionRow, int cellRow, int cellCol,
				int cellValue) {
			mCageId = cageId;
			mSolutionRow = solutionRow;
			mCellRow = cellRow;
			mCellCol = cellCol;
			mCellValue = cellValue;
		}
	}

	private ArrayList<Move> mMoves;

	/**
	 * Creates a new instance of {@see MathDokuDLX}.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 * @param cages
	 */
	public MathDokuDLX(int gridSize, ArrayList<GridCage> cages) {
		mGridSize = gridSize;
		mCages = cages;
	}

	private void initialize(boolean uncoverSolution) {
		int gridSizeSquare = mGridSize * mGridSize;
		int totalCages = mCages.size();

		// Number of columns = number of constraints =
		// BOARD * BOARD (for columns) +
		// BOARD * BOARD (for rows) +
		// Num cages (each cage has to be filled once and only once)
		// Number of rows = number of "moves" =
		// Sum of all the possible cage combinations
		// Number of nodes = sum of each move:
		// num_cells column constraints +
		// num_cells row constraints +
		// 1 (cage constraint)
		mTotalMoves = 0;
		int total_nodes = 0;
		for (GridCage gridCage : mCages) {
			int possibleMovesInCage = gridCage.getPossibleNums().size();
			mTotalMoves += possibleMovesInCage;
			total_nodes += possibleMovesInCage
					* (2 * gridCage.mCells.size() + 1);
		}
		Init(totalCages + 2 * gridSizeSquare, mTotalMoves, total_nodes);

		// Reorder cages based on the number of possible moves for the cage
		// because this has a major impact on the time it will take to find a
		// solution. Cage should be ordered on increasing number of possible
		// moves.
		ArrayList<GridCage> sortedCages = new ArrayList<GridCage>(mCages);
		Collections.sort(sortedCages, new SortCagesOnNumberOfMoves());
		if (DEBUG_DLX) {
			for (GridCage gridCage : sortedCages) {
				Log.i(TAG, "Cage " + gridCage.mId + " has "
						+ gridCage.getPossibleNums().size()
						+ " permutations with " + gridCage.mCells.size()
						+ " cells");
			}
		}

		// In case the solution has to be uncovered, which is needed in case
		// puzzle are shared, register details of all moves. Maybe all relevant
		// data is already stored but I can't find out how.
		if (uncoverSolution) {
			mMoves = new ArrayList<Move>();
		} else {
			mMoves = null;
		}

		int constraint_num;
		int move_idx = 0;
		int cage_count = 0;
		for (GridCage gridCage : sortedCages) {
			ArrayList<int[]> allmoves = gridCage.getPossibleNums();
			for (int[] onemove : allmoves) {
				if (DEBUG_DLX) {
					Log.i(TAG, "Move " + move_idx + " - Cage " + gridCage.mId
							+ " with " + gridCage.mCells.size() + " cells");
				}

				// Is this permutation used for cage "cage_count"? The cage
				// constraint is put upfront. As the cage have been sorted on
				// the number of possible permutations this has a positive
				// influence on the solving time.
				constraint_num = cage_count + 1;
				AddNode(constraint_num, move_idx); // Cage constraint

				// Apply the permutation of "onemove" to the cells in the cages
				for (int i = 0; i < gridCage.mCells.size(); i++) {
					GridCell gridCell = gridCage.mCells.get(i);

					// Fill data structure for DLX algorithm

					// Is digit "onemove[i]" used in column getColumn()?
					constraint_num = totalCages + mGridSize * (onemove[i] - 1)
							+ gridCell.getColumn() + 1;
					AddNode(constraint_num, move_idx); // Column constraint

					// Is digit "onemove[i]" used in row getRow()?
					constraint_num = totalCages + gridSizeSquare + mGridSize
							* (onemove[i] - 1) + gridCell.getRow() + 1;
					AddNode(constraint_num, move_idx); // Row constraint

					// Fill data structure for uncovering solution if needed
					if (uncoverSolution) {
						mMoves.add(new Move(gridCage.mId, move_idx, gridCell
								.getRow(), gridCell.getColumn(), onemove[i]));
					}
					if (DEBUG_DLX) {
						Log.i(TAG, "  Cell " + gridCell.getCellNumber()
								+ " row =" + gridCell.getRow() + " col = "
								+ gridCell.getColumn() + " value = "
								+ onemove[i]);
					}
				}

				// Proceed with next permutation for this or for the next cage
				move_idx++;
			}

			// Proceed with the permutation(s) of the next cage
			cage_count++;
		}
	}

	/**
	 * Comparator to sort cages based on the number of possible moves, the
	 * number of cells in the cage and/or the cage id. This order of the cages
	 * determine how efficient the puzzle solving will be.
	 */
	public class SortCagesOnNumberOfMoves implements Comparator<GridCage> {
		@Override
		public int compare(GridCage gridCage1, GridCage gridCage2) {
			int difference = gridCage1.getPossibleNums().size()
					- gridCage2.getPossibleNums().size();
			if (difference == 0) {
				// Both cages have the same number of possible permutation. Next
				// compare the number of cells in the cage.
				difference = gridCage1.mCells.size() - gridCage2.mCells.size();
				if (difference == 0) {
					// Also the number of cells is equal. Finally compare the
					// id's.
					difference = gridCage1.mId - gridCage2.mId;
				}
			}
			return difference;
		}
	}

	/**
	 * Checks whether a unique solution can be found for this grid.
	 * 
	 * @return True in case exactly one solution exists for this grid.
	 */
	public boolean hasUniqueSolution() {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT && DEBUG_DLX) {
			initialize(true); // Needed to compute complexity in development
								// mode

			// Search for multiple solutions (but stop as soon as the second
			// solution has been found).
			if (Solve(SolveType.MULTIPLE) == 1) {
				// Only one solution has been found. The real complexity of the
				// puzzle is computed based on this solution.

				getPuzzleComplexity();
				return true;
			} else {
				return false;
			}
		} else {
			initialize(false);
			return (Solve(SolveType.MULTIPLE) == 1);
		}
	}

	/**
	 * Determines the unique solution for this grid.
	 * 
	 * @return The solution of the grid if and oly if the grid has exactly one
	 *         unique solution. NULL otherwise.
	 */
	public int[][] getSolutionGrid() {
		initialize(true);

		// Check if a single unique solution can be determined.
		if (mMoves == null || Solve(SolveType.MULTIPLE) != 1) {
			return null;
		}

		// Determine which rows are included in the solution.
		boolean[] rowInSolution = new boolean[mTotalMoves];
		for (int i = 0; i < mTotalMoves; i++) {
			rowInSolution[i] = false;
		}
		for (int i = 1; i <= GetRowsInSolution(); i++) {
			rowInSolution[GetSolutionRow(i)] = true;
		}

		// Now rebuild the solution
		int[][] solutionGrid = new int[mGridSize][mGridSize];
		for (Move move : mMoves) {
			if (rowInSolution[move.mSolutionRow]) {
				solutionGrid[move.mCellRow][move.mCellCol] = move.mCellValue;
			}
		}

		if (DEBUG_DLX) {
			for (int row = 0; row < this.mGridSize; row++) {
				String line = "";
				for (int col = 0; col < this.mGridSize; col++) {
					line += " " + solutionGrid[row][col];
				}
				Log.i(TAG, line);
			}
		}

		return solutionGrid;
	}

	/**
	 * Determines the complexity of a grid by analysing its solution.
	 * 
	 * @return The complexity of a grid.
	 */
	private int getPuzzleComplexity() {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT && DEBUG_DLX) {
			// ///////////////////////////////////////////////////////////////////////
			// NOT READY FOR PRODUCTION MODE YET.
			//
			// Be sure to call initialize(true) before calling this method
			//
			// The method computes a complexity and prints log messages. It is
			// not yet clear whether the computed complexity match with
			// subjective difficulty.
			// ///////////////////////////////////////////////////////////////////////

			if (DEBUG_DLX) {
				Log.i(TAG, "Determine puzzle complexitiy");
			}
			int[][] solutionGrid = new int[mGridSize][mGridSize];
			int moveCount = 1;
			int previousCageId = -1;
			int puzzleComplexity = 1;
			for (int i = 1; i <= GetRowsInSolution(); i++) {
				// Each solution row corresponds with one cage in the grid. The
				// order in which the cages are resolved is important. As soon a
				// the
				// next cage has to be resolved, it is determined which cage has
				// the
				// least possible number of permutations available at that
				// moment.
				int solutionRow = GetSolutionRow(i);

				for (Move move : mMoves) {
					if (move.mSolutionRow == solutionRow) {
						if (move.mCageId != previousCageId) {
							// This is the first cell of the next cage to be
							// filled.
							// Determine the number of move for this cage which
							// are
							// still possible with the partially filled grid.
							GridCage gridCage = mCages.get(move.mCageId);
							ArrayList<int[]> cageMoves = gridCage
									.getPossibleNums();
							int possiblePermutations = 0;
							for (int[] cageMove : cageMoves) {
								boolean validMove = true;
								// Test whether this cage move could be applied
								// to
								// the cells of the cage.
								for (int j = 0; j < gridCage.mCells.size(); j++) {
									// Check if value is already used in this
									// row
									int cellRow = gridCage.mCells.get(j)
											.getRow();
									for (int col = 0; col < mGridSize; col++) {
										if (solutionGrid[cellRow][col] == cageMove[j]) {
											// The value is already used on this
											// row.
											validMove = false;
											break;
										}
									}
									if (validMove == false) {
										break;
									}

									// Check if value is already used in this
									// row
									int cellColumn = gridCage.mCells.get(j)
											.getColumn();
									for (int row = 0; row < mGridSize; row++) {
										if (solutionGrid[row][cellColumn] == cageMove[j]) {
											// The value is already used in this
											// column.
											validMove = false;
											break;
										}
									}
									if (validMove == false) {
										break;
									}
								}
								if (validMove) {
									// All values of the cageMove could be
									// placed in
									// their respective cells. So this is really
									// a
									// permutation which still can be place into
									// the
									// cage.
									possiblePermutations++;
								}
							}
							// The complexity of the puzzle has to be multiplied
							// with the number of possible permutations of this
							// cage
							// as their is not deductive way to reduce the
							// number of
							// possible combinations any further. At this moment
							// a
							// combination has to be chosen at random to check
							// to
							// see if it fails.
							if (DEBUG_DLX) {
								Log.i(TAG, "Select cage " + move.mCageId
										+ " with complexity "
										+ possiblePermutations);
							}
							puzzleComplexity *= possiblePermutations;
							previousCageId = move.mCageId;
						}

						// Fill the grid solution with the correct value.
						solutionGrid[move.mCellRow][move.mCellCol] = move.mCellValue;

					}
				}
				if (DEBUG_DLX) {
					Log.i(TAG, "*********** MOVE " + (moveCount++)
							+ " ***********");
					for (int row = 0; row < this.mGridSize; row++) {
						String line = "";
						for (int col = 0; col < this.mGridSize; col++) {
							line += " " + solutionGrid[row][col];
						}
						Log.i(TAG, line);
					}
				}
			}
			if (DEBUG_DLX) {
				Log.i(TAG, "Total complexity of puzzle " + puzzleComplexity
						+ " (or " + complexity + "??)");
			}

			return puzzleComplexity;
		}

		return 0;
	}
}