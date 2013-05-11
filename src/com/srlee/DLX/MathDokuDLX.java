package com.srlee.DLX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.cactii.mathdoku.GridCage;
import net.cactii.mathdoku.gridGenerating.GridGenerator;
import android.util.Log;

public class MathDokuDLX extends DLX {
	private static final String TAG = "MathDoku.MathDokuDLX";

	private int BOARD = 0;
	private int BOARD2 = 0;

	public MathDokuDLX(int size, ArrayList<GridCage> cages) {

		BOARD = size;
		BOARD2 = BOARD * BOARD;

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
		int total_moves = 0;
		int total_nodes = 0;
		for (GridCage gc : cages) {
			int possibleMovesInCage = gc.getPossibleNums().size();
			total_moves += possibleMovesInCage;
			total_nodes += possibleMovesInCage * (2 * gc.mCells.size() + 1);
		}
		Init(2 * BOARD2 + cages.size(), total_moves, total_nodes);

		// Reorder cages based on the number of possible moves for the cage
		// because this has a major impact on the time it will take to find a
		// solution. Cage should be ordered on increasing number of possible
		// moves.
		ArrayList<GridCage> sortedCages = new ArrayList<GridCage>(cages);
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
			if (Double.compare(complexityDouble, Long.valueOf(Long.MAX_VALUE).doubleValue()) < 0) {
				// The complexity stills first in a long value.
				Log.i(TAG, "Puzzle complexity: " + complexityLong);
			} else {
				Log.i(TAG, "Puzzle complexity: " + complexityDouble);
			}
		}

		int constraint_num;
		int move_idx = 0;
		for (GridCage gc : sortedCages) {
			ArrayList<int[]> allmoves = gc.getPossibleNums();
			for (int[] onemove : allmoves) {
				for (int i = 0; i < gc.mCells.size(); i++) {
					constraint_num = BOARD * (onemove[i] - 1)
							+ gc.mCells.get(i).getColumn() + 1;
					AddNode(constraint_num, move_idx); // Column constraint
					constraint_num = BOARD2 + BOARD * (onemove[i] - 1)
							+ gc.mCells.get(i).getRow() + 1;
					AddNode(constraint_num, move_idx); // Row constraint
				}
				constraint_num = 2 * BOARD2 + gc.mId + 1;
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
}
