package net.mathdoku.plus.leaderboard;

import net.mathdoku.plus.R;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;

/**
 * This class is used to translate puzzle characteristics (grid size, visibility
 * of operators and puzzle complexity) to Google Play Service leaderboard id's
 * and vice versa.
 */
public class LeaderboardType {
	// All leaderboards as defined in the game service on Google Play. The order
	// of this array should be kept in sync with the computation of the index
	// value in method getIndex.
	private static final int[] mLeaderboardResId = {
			// 4x4 leaderboards
			R.string.leaderboard_4x4__hidden_operators__1_star,
			R.string.leaderboard_4x4__hidden_operators__2_stars,
			R.string.leaderboard_4x4__hidden_operators__3_stars,
			R.string.leaderboard_4x4__hidden_operators__4_stars,
			R.string.leaderboard_4x4__hidden_operators__5_stars,
			R.string.leaderboard_4x4__visible_operators__1_star,
			R.string.leaderboard_4x4__visible_operators__2_stars,
			R.string.leaderboard_4x4__visible_operators__3_stars,
			R.string.leaderboard_4x4__visible_operators__4_stars,
			R.string.leaderboard_4x4__visible_operators__5_stars,
			// 5x5 leaderboards
			R.string.leaderboard_5x5__hidden_operators__1_star,
			R.string.leaderboard_5x5__hidden_operators__2_stars,
			R.string.leaderboard_5x5__hidden_operators__3_stars,
			R.string.leaderboard_5x5__hidden_operators__4_stars,
			R.string.leaderboard_5x5__hidden_operators__5_stars,
			R.string.leaderboard_5x5__visible_operators__1_star,
			R.string.leaderboard_5x5__visible_operators__2_stars,
			R.string.leaderboard_5x5__visible_operators__3_stars,
			R.string.leaderboard_5x5__visible_operators__4_stars,
			R.string.leaderboard_5x5__visible_operators__5_stars,
			// 6x6 leaderboards
			R.string.leaderboard_6x6__hidden_operators__1_star,
			R.string.leaderboard_6x6__hidden_operators__2_stars,
			R.string.leaderboard_6x6__hidden_operators__3_stars,
			R.string.leaderboard_6x6__hidden_operators__4_stars,
			R.string.leaderboard_6x6__hidden_operators__5_stars,
			R.string.leaderboard_6x6__visible_operators__1_star,
			R.string.leaderboard_6x6__visible_operators__2_stars,
			R.string.leaderboard_6x6__visible_operators__3_stars,
			R.string.leaderboard_6x6__visible_operators__4_stars,
			R.string.leaderboard_6x6__visible_operators__5_stars,
			// 7x7 leaderboards
			R.string.leaderboard_7x7__hidden_operators__1_star,
			R.string.leaderboard_7x7__hidden_operators__2_stars,
			R.string.leaderboard_7x7__hidden_operators__3_stars,
			R.string.leaderboard_7x7__hidden_operators__4_stars,
			R.string.leaderboard_7x7__hidden_operators__5_stars,
			R.string.leaderboard_7x7__visible_operators__1_star,
			R.string.leaderboard_7x7__visible_operators__2_stars,
			R.string.leaderboard_7x7__visible_operators__3_stars,
			R.string.leaderboard_7x7__visible_operators__4_stars,
			R.string.leaderboard_7x7__visible_operators__5_stars,
			// 8x8 leaderboards
			R.string.leaderboard_8x8__hidden_operators__1_star,
			R.string.leaderboard_8x8__hidden_operators__2_stars,
			R.string.leaderboard_8x8__hidden_operators__3_stars,
			R.string.leaderboard_8x8__hidden_operators__4_stars,
			R.string.leaderboard_8x8__hidden_operators__5_stars,
			R.string.leaderboard_8x8__visible_operators__1_star,
			R.string.leaderboard_8x8__visible_operators__2_stars,
			R.string.leaderboard_8x8__visible_operators__3_stars,
			R.string.leaderboard_8x8__visible_operators__4_stars,
			R.string.leaderboard_8x8__visible_operators__5_stars,
			// 9x9 leaderboards
			R.string.leaderboard_9x9__hidden_operators__1_star,
			R.string.leaderboard_9x9__hidden_operators__2_stars,
			R.string.leaderboard_9x9__hidden_operators__3_stars,
			R.string.leaderboard_9x9__hidden_operators__4_stars,
			R.string.leaderboard_9x9__hidden_operators__5_stars,
			R.string.leaderboard_9x9__visible_operators__1_star,
			R.string.leaderboard_9x9__visible_operators__2_stars,
			R.string.leaderboard_9x9__visible_operators__3_stars,
			R.string.leaderboard_9x9__visible_operators__4_stars,
			R.string.leaderboard_9x9__visible_operators__5_stars };

	// Resource id's of the leaderboards. The items in this array should be kept
	// in sync with array mLeaderboardResId.
	private static final int[] mLeaderboardIconResId = {
			// 4x4 leaderboards
			R.drawable.leaderboard_4x4_operators_hidden_1_star,
			R.drawable.leaderboard_4x4_operators_hidden_2_stars,
			R.drawable.leaderboard_4x4_operators_hidden_3_stars,
			R.drawable.leaderboard_4x4_operators_hidden_4_stars,
			R.drawable.leaderboard_4x4_operators_hidden_5_stars,
			R.drawable.leaderboard_4x4_operators_visible_1_star,
			R.drawable.leaderboard_4x4_operators_visible_2_stars,
			R.drawable.leaderboard_4x4_operators_visible_3_stars,
			R.drawable.leaderboard_4x4_operators_visible_4_stars,
			R.drawable.leaderboard_4x4_operators_visible_5_stars,
			// 5x5 leaderboards
			R.drawable.leaderboard_5x5_operators_hidden_1_star,
			R.drawable.leaderboard_5x5_operators_hidden_2_stars,
			R.drawable.leaderboard_5x5_operators_hidden_3_stars,
			R.drawable.leaderboard_5x5_operators_hidden_4_stars,
			R.drawable.leaderboard_5x5_operators_hidden_5_stars,
			R.drawable.leaderboard_5x5_operators_visible_1_star,
			R.drawable.leaderboard_5x5_operators_visible_2_stars,
			R.drawable.leaderboard_5x5_operators_visible_3_stars,
			R.drawable.leaderboard_5x5_operators_visible_4_stars,
			R.drawable.leaderboard_5x5_operators_visible_5_stars,
			// 6x6 leaderboards
			R.drawable.leaderboard_6x6_operators_hidden_1_star,
			R.drawable.leaderboard_6x6_operators_hidden_2_stars,
			R.drawable.leaderboard_6x6_operators_hidden_3_stars,
			R.drawable.leaderboard_6x6_operators_hidden_4_stars,
			R.drawable.leaderboard_6x6_operators_hidden_5_stars,
			R.drawable.leaderboard_6x6_operators_visible_1_star,
			R.drawable.leaderboard_6x6_operators_visible_2_stars,
			R.drawable.leaderboard_6x6_operators_visible_3_stars,
			R.drawable.leaderboard_6x6_operators_visible_4_stars,
			R.drawable.leaderboard_6x6_operators_visible_5_stars,
			// 7x7 leaderboards
			R.drawable.leaderboard_7x7_operators_hidden_1_star,
			R.drawable.leaderboard_7x7_operators_hidden_2_stars,
			R.drawable.leaderboard_7x7_operators_hidden_3_stars,
			R.drawable.leaderboard_7x7_operators_hidden_4_stars,
			R.drawable.leaderboard_7x7_operators_hidden_5_stars,
			R.drawable.leaderboard_7x7_operators_visible_1_star,
			R.drawable.leaderboard_7x7_operators_visible_2_stars,
			R.drawable.leaderboard_7x7_operators_visible_3_stars,
			R.drawable.leaderboard_7x7_operators_visible_4_stars,
			R.drawable.leaderboard_7x7_operators_visible_5_stars,
			// 8x8 leaderboards
			R.drawable.leaderboard_8x8_operators_hidden_1_star,
			R.drawable.leaderboard_8x8_operators_hidden_2_stars,
			R.drawable.leaderboard_8x8_operators_hidden_3_stars,
			R.drawable.leaderboard_8x8_operators_hidden_4_stars,
			R.drawable.leaderboard_8x8_operators_hidden_5_stars,
			R.drawable.leaderboard_8x8_operators_visible_1_star,
			R.drawable.leaderboard_8x8_operators_visible_2_stars,
			R.drawable.leaderboard_8x8_operators_visible_3_stars,
			R.drawable.leaderboard_8x8_operators_visible_4_stars,
			R.drawable.leaderboard_8x8_operators_visible_5_stars,
			// 9x9 leaderboards
			R.drawable.leaderboard_9x9_operators_hidden_1_star,
			R.drawable.leaderboard_9x9_operators_hidden_2_stars,
			R.drawable.leaderboard_9x9_operators_hidden_3_stars,
			R.drawable.leaderboard_9x9_operators_hidden_4_stars,
			R.drawable.leaderboard_9x9_operators_hidden_5_stars,
			R.drawable.leaderboard_9x9_operators_visible_1_star,
			R.drawable.leaderboard_9x9_operators_visible_2_stars,
			R.drawable.leaderboard_9x9_operators_visible_3_stars,
			R.drawable.leaderboard_9x9_operators_visible_4_stars,
			R.drawable.leaderboard_9x9_operators_visible_5_stars };

	// Maximum number of leaderboards available
	public final static int MAX_LEADERBOARDS = mLeaderboardResId.length;

	// Number of elements for index factor PuzzleComplexity. Note this enum
	// contains a value RANDOM (ordinal value 0) which is not a real complexity
	// factor (no leaderboards exists for it) and therefore needs to be
	// excluded.
	private final static int PUZZLE_COMPLEXITY_OFFSET = (PuzzleComplexity.RANDOM
			.ordinal() == 0 ? 1 : 0);
	private final static int MAX_ELEMENTS_INDEX_FACTOR_PUZZLE_COMPLEXITY = PuzzleComplexity
			.values().length - 1;

	// Number of elements for index factor operator visibility
	private final static int MAX_ELEMENTS_INDEX_FACTOR_OPERATORS = 2; // boolean

	// Number of elements for index factor grid size
	private final static int MIN_GRID_SIZE = 4;
	private final static int MAX_GRID_SIZE = 9;

	// When computing the index it is computed using following index factors.
	// Each index factor is the product of all possible combinations of previous
	// factors.
	private final static int PUZZLE_COMPLEXITY_INDEX_FACTOR = 1;
	private final static int HIDE_OPERATOR_INDEX_FACTOR = (1 * MAX_ELEMENTS_INDEX_FACTOR_PUZZLE_COMPLEXITY);
	private final static int GRID_SIZE_INDEX_FACTOR = (HIDE_OPERATOR_INDEX_FACTOR * MAX_ELEMENTS_INDEX_FACTOR_OPERATORS);

	/**
	 * Get the leaderboard index for the given combination of grid size,
	 * operator visibility and puzzle complexity.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 * @param hideOperators
	 *            True in case operator are visible. False otherwise.
	 * @param puzzleComplexity
	 *            The complexity of the grid (currently not yet in use).
	 * @return The index associated with the leaderboard. -1 in case of an
	 *         error.
	 */
	public static int getIndex(int gridSize, boolean hideOperators,
			PuzzleComplexity puzzleComplexity) {

		// Test whether grid size is within known size.
		assert (gridSize >= MIN_GRID_SIZE && gridSize <= MAX_GRID_SIZE);

		// Determine the leaderboard index to use.
		int index = (puzzleComplexity.ordinal() - PUZZLE_COMPLEXITY_OFFSET)
				* PUZZLE_COMPLEXITY_INDEX_FACTOR;
		index += (hideOperators ? 0 : 1) * HIDE_OPERATOR_INDEX_FACTOR;
		index += (gridSize - MIN_GRID_SIZE) * GRID_SIZE_INDEX_FACTOR;

		// Update leaderboard if an valid index was determined.
		if (index >= 0 && index < mLeaderboardResId.length) {
			return index;
		}

		return -1;
	}

	/**
	 * Get the leaderboard resource id for the given combination of grid size,
	 * operator visibility and puzzle complexity.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 * @param hideOperators
	 *            True in case operator are visible. False otherwise.
	 * @param puzzleComplexity
	 *            The complexity of the grid (currently not yet in use).
	 * @return The resource id associated with the leaderboard. -1 in case of an
	 *         error.
	 */
	public static int getResId(int gridSize, boolean hideOperators,
			PuzzleComplexity puzzleComplexity) {
		int leaderboardIndex = getIndex(gridSize, hideOperators,
				puzzleComplexity);
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

		// As grid size is the biggest factor it is not needed to strip other
		// factors from index.
		int gridSize = index / GRID_SIZE_INDEX_FACTOR + MIN_GRID_SIZE;

		// Test whether a valid grid size has been computed.
		assert (gridSize >= MIN_GRID_SIZE && gridSize <= MAX_GRID_SIZE);

		return gridSize;
	}

	/**
	 * Checks whether the operators for the given index of leaderboard types are
	 * hidden.
	 * 
	 * @param index
	 *            The index for which the visibility of the operators of the
	 *            leaderboard type has to be determined.
	 * @return True in case the operators for this leaderboard type are hidden.
	 *         False otherwise.
	 */
	public static boolean hasHiddenOperator(int index) {
		// Strip bigger factors from the index.
		int remainder = index % GRID_SIZE_INDEX_FACTOR;

		// Get operator visibility
		remainder /= HIDE_OPERATOR_INDEX_FACTOR;

		// Test whether a valid remainder has been computed.
		assert (remainder == 0 || remainder == 1);

		return (remainder == 0);
	}

	/**
	 * Gets the puzzle complexity for the given index of leaderboard types.
	 * 
	 * @param index
	 *            The index for which the puzzle complexity of the leaderboard
	 *            type has to be determined.
	 * @return The puzzle complexity of the leaderboard type.
	 */
	public static PuzzleComplexity getPuzzleComplexity(int index) {
		// Strip bigger factors from the index.
		int remainder = index % HIDE_OPERATOR_INDEX_FACTOR;

		// Get puzzle complexity
		remainder /= PUZZLE_COMPLEXITY_INDEX_FACTOR;
		assert (remainder >= 0 && remainder <= MAX_ELEMENTS_INDEX_FACTOR_PUZZLE_COMPLEXITY);
		remainder += PUZZLE_COMPLEXITY_OFFSET;

		return PuzzleComplexity.values()[remainder];
	}

	/**
	 * Get the leaderboard icon resource id for the given index.
	 * 
	 * @param leaderboardIndex
	 *            The index of the leaderboard icon which has to be returned.
	 * @return The icon resource id associated with the leaderboard. -1 in case
	 *         of an error.
	 */
	public static int getIconResId(int leaderboardIndex) {

		if (leaderboardIndex >= 0
				&& leaderboardIndex < mLeaderboardIconResId.length) {
			return mLeaderboardIconResId[leaderboardIndex];
		}

		return -1;
	}
}