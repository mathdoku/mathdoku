package com.srlee.DLX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.cactii.mathdoku.GridCage;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.gridGenerating.GridGenerator;
import android.util.Log;

public class MathDokuDLX extends DLX {
	private static final String TAG = "MathDoku.MathDokuDLX";

	// Remove "&& false" in following line to show debug information about
	// filling the DLX data structure when running in development mode.
	public static final boolean DEBUG_DLX = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	private int mGridSize;
	private int mTotalMoves;

	// The list of cages for which the solution has to be checked
	private ArrayList<GridCage> mCages; 
	
	// Additional data structure in case the solution has to be uncovered.
	private class Move {
		protected int mSolutionRow;
		protected int mCellRow;
		protected int mCellCol;
		protected int mCellValue;

		public Move(int solutionRow, int cellRow, int cellCol, int cellValue) {
			mSolutionRow = solutionRow;
			mCellRow = cellRow;
			mCellCol = cellCol;
			mCellValue = cellValue;
		}
	}

	ArrayList<Move> mMoves;

	/**
	 * Creates a new instance of {@see MathDokuDLX}.
	 * 
	 * @param gridSize The size of the grid.
	 * @param cages
	 */
	public MathDokuDLX(int gridSize, ArrayList<GridCage> cages) {
		mGridSize = gridSize;
		mCages = cages;
	}
	
	private void initialize(boolean uncoverSolution) {
		int gridSizeSquare = mGridSize * mGridSize;

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
		for (GridCage gc : mCages) {
			int possibleMovesInCage = gc.getPossibleNums().size();
			mTotalMoves += possibleMovesInCage;
			total_nodes += possibleMovesInCage * (2 * gc.mCells.size() + 1);
		}
		Init(2 * gridSizeSquare + mCages.size(), mTotalMoves, total_nodes);

		// Reorder cages based on the number of possible moves for the cage
		// because this has a major impact on the time it will take to find a
		// solution. Cage should be ordered on increasing number of possible
		// moves.
		ArrayList<GridCage> sortedCages = new ArrayList<GridCage>(mCages);
		Collections.sort((List<GridCage>) sortedCages,
				new SortCagesOnNumberOfMoves());
		if (GridGenerator.DEBUG_GRID_GENERATOR) {
			double complexityDouble = 1;
			long complexityLong = 1;
			for (GridCage gc : sortedCages) {
				int possibleMovesInCage = gc.getPossibleNums().size();

				complexityDouble *= possibleMovesInCage;
				complexityLong *= possibleMovesInCage;
				Log.i(TAG, "Cage " + gc.mId + " - cells: " + gc.mCells.size()
						+ " - Possible moves: " + possibleMovesInCage);
			}
			if (Double.compare(complexityDouble, Long.valueOf(Long.MAX_VALUE)
					.doubleValue()) < 0) {
				// The complexity stills first in a long value.
				Log.i(TAG, "Puzzle complexity: " + complexityLong);
			} else {
				Log.i(TAG, "Puzzle complexity: " + complexityDouble);
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
		for (GridCage gc : sortedCages) {
			ArrayList<int[]> allmoves = gc.getPossibleNums();
			for (int[] onemove : allmoves) {
				if (DEBUG_DLX) {
					Log.i(TAG, "Move " + move_idx + " - Cage " + gc.mId);
				}
				for (int i = 0; i < gc.mCells.size(); i++) {
					GridCell gridCell = gc.mCells.get(i);

					// Fill data structure for DLX algoritm
					constraint_num = mGridSize * (onemove[i] - 1)
							+ gridCell.getColumn() + 1;
					AddNode(constraint_num, move_idx); // Column constraint
					constraint_num = gridSizeSquare + mGridSize * (onemove[i] - 1)
							+ gridCell.getRow() + 1;
					AddNode(constraint_num, move_idx); // Row constraint

					// Fill data structure for uncovering solution if needed
					if (uncoverSolution) {
						mMoves.add(new Move(move_idx, gridCell
								.getRow(), gridCell.getColumn(), onemove[i]));
					}
					if (DEBUG_DLX) {
						Log.i(TAG, "  Cell " + gridCell.getCellNumber()
								+ " row =" + gridCell.getRow() + " col = "
								+ gridCell.getColumn() + " value = "
								+ onemove[i]);
					}
				}
				constraint_num = 2 * gridSizeSquare + gc.mId + 1;
				AddNode(constraint_num, move_idx); // Cage constraint
				move_idx++;
			}
		}
	}

	/**
	 * Comparator to sort cages based on the number of possible moves.
	 */
	public class SortCagesOnNumberOfMoves implements Comparator<GridCage> {
		public int compare(GridCage gridCage1, GridCage gridCage2) {
			return gridCage1.getPossibleNums().size()
					- gridCage2.getPossibleNums().size();
		}
	}

	/**
	 * Checks whether a unique solution can be found for this grid.
	 * 
	 * @return True in case exactly one solution exists for this grid.
	 */
	public boolean hasUniqueSolution() {
		initialize(false);
		
		// Search for multiple solutions (but stop as soon as the second
		// solution has been found).
		return (Solve(SolveType.MULTIPLE) == 1);
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
}