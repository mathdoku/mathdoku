package net.mathdoku.plus.leaderboard;

import net.mathdoku.plus.R;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;

/**
 * This class is used to translate puzzle characteristics (grid size, visibility
 * of operators and puzzle complexity) to Google Play Service leaderboard id's
 * and vice versa.
 * 
 */
public class LeaderboardType {
	// All leaderboards as defined in the game service on Google Play
	private static final int[] mLeaderboardResId = {
			R.string.leaderboard_fastest_4x4_visibile_operators,
			R.string.leaderboard_fastest_5x5_visibile_operators,
			R.string.leaderboard_fastest_6x6_visibile_operators,
			R.string.leaderboard_fastest_7x7_visibile_operators,
			R.string.leaderboard_fastest_8x8_visibile_operators,
			R.string.leaderboard_fastest_9x9_visibile_operators,
			R.string.leaderboard_fastest_4x4_hidden_operators,
			R.string.leaderboard_fastest_5x5_hidden_operators,
			R.string.leaderboard_fastest_6x6_hidden_operators,
			R.string.leaderboard_fastest_7x7_hidden_operators,
			R.string.leaderboard_fastest_8x8_hidden_operators,
			R.string.leaderboard_fastest_9x9_hidden_operators };

	// Maximum number of leaderboards available
	public final static int MAX_LEADERBOARDS = mLeaderboardResId.length;

	// Minimum and maximum allowed grid size
	private final static int MIN_GRID_SIZE = 4;
	private final static int MAX_GRID_SIZE = 9;

	// The index is computed with following index factors (each must be number
	// uniquely)
	private final static int MAX_ELEMENTS_INDEX_FACTOR_GRID_SIZE = MAX_GRID_SIZE
			- MIN_GRID_SIZE + 1;
	private final static int GRID_SIZE_INDEX_FACTOR = 0;
	private final static int HIDE_OPERATOR_INDEX_FACTOR = 1;

	/**
	 * Get the leaderboard index for the given combination of grid size, puzzle
	 * complexity and hide operators.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 * @param puzzleComplexity
	 *            The complexity of the grid (currently not yet in use).
	 * @param hideOperators
	 *            True in case operator are hidden. False otherwise.
	 * @return The index associated with the leaderboard. -1 in case of an
	 *         error.
	 */
	public static int getIndex(int gridSize, PuzzleComplexity puzzleComplexity,
			boolean hideOperators) {
		// Test whether grid size is within known size.
		assert (gridSize >= MIN_GRID_SIZE && gridSize <= MAX_GRID_SIZE);

		// Determine the leaderboard index to use. Note: the puzzle complexity
		// is not yet used to compute the index.
		int index = (gridSize - MIN_GRID_SIZE)
				* (int) Math.pow(MAX_ELEMENTS_INDEX_FACTOR_GRID_SIZE,
						GRID_SIZE_INDEX_FACTOR);
		index += (hideOperators ? 1 : 0)
				* Math.pow(MAX_ELEMENTS_INDEX_FACTOR_GRID_SIZE,
						HIDE_OPERATOR_INDEX_FACTOR);

		// Update leaderboard if an valid index was determined.
		if (index >= 0 && index < mLeaderboardResId.length) {
			return index;
		}

		return -1;
	}

	/**
	 * Get the leaderboard resource id for the given combination of grid size,
	 * puzzle complexity and hide operators.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 * @param puzzleComplexity
	 *            The complexity of the grid (currently not yet in use).
	 * @param hideOperators
	 *            True in case operator are hidden. False otherwise.
	 * @return The resource id associated with the leaderboard. -1 in case of an
	 *         error.
	 */
	public static int getResId(int gridSize, PuzzleComplexity puzzleComplexity,
			boolean hideOperators) {
		int leaderboardIndex = getIndex(gridSize, puzzleComplexity,
				hideOperators);
		if (leaderboardIndex >= 0) {
			return mLeaderboardResId[leaderboardIndex];
		}

		return -1;
	}

	/**
	 * Get the leaderboard resource id for the given index.
	 * 
	 * @param leaderboardIndex
	 *            The index of the leaderboard which has to be returned.
	 * @return The resource id associated with the leaderboard. -1 in case of an
	 *         error.
	 */
	public static int getResId(int leaderboardIndex) {

		if (leaderboardIndex >= 0
				&& leaderboardIndex < mLeaderboardResId.length) {
			return mLeaderboardResId[leaderboardIndex];
		}

		return -1;
	}

	/**
	 * Gets the grid size for the given index of leaderboard types.
	 * 
	 * @param index
	 *            The index for which the grid size of the leaderboard type has
	 *            to be determined.
	 * @return The grid size of the leaderboard type. -1 in case of an error.
	 */
	public static int getGridSize(int index) {
		if (index < 0 || index >= MAX_LEADERBOARDS) {
			return -1;
		}

		int gridSize = (index % MAX_ELEMENTS_INDEX_FACTOR_GRID_SIZE)
				+ MIN_GRID_SIZE;

		// Test whether a valid grid size has been computed.
		assert (gridSize >= MIN_GRID_SIZE && gridSize <= MAX_GRID_SIZE);

		return gridSize;
	}

	/**
	 * Gets the visibility of the operators for the given index of leaderboard
	 * types.
	 * 
	 * @param index
	 *            The index for which the visibility of the operators of the
	 *            leaderboard type has to be determined.
	 * @return True in case the operators for this leaderboard type are hidden.
	 *         False otherwise.
	 */
	public static boolean getHideOperator(int id) {
		int remainder = id
				/ (int) Math.pow(MAX_ELEMENTS_INDEX_FACTOR_GRID_SIZE,
						HIDE_OPERATOR_INDEX_FACTOR);

		// Test whether a valid remainder has been computed.
		assert (remainder == 0 || remainder == 1);

		return (remainder == 1 ? true : false);
	}
}