package net.cactii.mathdoku.statistics;

import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;

/**
 * Statistics for a single grid.
 */
public class GridStatistics {
	// Unique row id for the statistics. This value may not be altered.
	public int mId;

	// Row id of the grid on which these statistics apply
	public int mGridId;

	// Identification of the replay of the game. 0 for first attempt. 1 for
	// first replay, 2 for second replay, etc.
	public int mReplayCount;

	// Timestamp of first and last move
	public java.sql.Timestamp mFirstMove;
	public java.sql.Timestamp mLastMove;

	// Time elapsed while playing (e.d. displaying the game)
	public long mElapsedTime;

	// Time added to the elapsed time because of using cheats
	public long mCheatPenaltyTime;

	// ****************
	// Avoidable moves
	// ****************

	// The number of times a user value in cell was replaced by another value
	public int mUserValueReplaced;

	// The number of possible values used
	public int mMaybeValue;

	// The number of times the undo button is used
	public int mActionUndoMove;

	// The number of times the clear button is used to clear a single cell
	public int mActionClearCell;

	// The number of times the action clear grid is used
	public int mActionClearGrid;

	// *******
	// Cheats
	// *******

	// The number of times the action reveal cell is used (a cheat)
	public int mActionRevealCell;

	// The number of times the action operator is used (a cheat)
	public int mActionRevealOperator;

	// The number of times the action "check progress" was used and the total
	// number of invalids values which have been found when using this option (a
	// cheat)
	public int mActionCheckProgress;
	public int mCheckProgressInvalidCellsFound;

	// Has the entire solution been revealed?
	public boolean mSolutionRevealed;

	// ***********************
	// Status for entire grid
	// ***********************

	// Cells filled + revealed + empty == total cells in grid
	public int mCellsFilled;
	public int mCellsEmtpty;
	public int mCellsRevealed; // Note: this value does not need to be identical
								// to mActionRevealCell in case reveal solution
								// is also used.

	// Has the grid been solved manually (i.e. not revealed)?
	public boolean mSolvedManually;

	// Has the grid been finished (either solved or revealed solution)?
	public boolean mFinished;

	// Are those statistics included in the cumulative and historic statistics
	// for this grid?
	public boolean mIncludedInStatistics;

	// Counters available
	public enum StatisticsCounterType {
		CELLS_FILLED, CELLS_EMPTY, CELLS_REVEALED, USER_VALUE_REPLACED, POSSIBLES, ACTION_UNDO_MOVE, ACTION_CLEAR_CELL, ACTION_CLEAR_GRID, ACTION_REVEAL_CELL, ACTION_REVEAL_OPERATOR, ACTION_CHECK_PROGRESS, CHECK_PROGRESS_INVALIDS_CELLS_FOUND
	};

	/**
	 * Save the statistics to the database.
	 * 
	 * @return True in case the statistics have been saved. False otherwise.
	 */
	public boolean save() {
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = new StatisticsDatabaseAdapter();
		return statisticsDatabaseAdapter.update(this);
	}

	/**
	 * Increases the given counter with 1.
	 * 
	 * @param statisticsCounterType
	 *            The counter which has to be increased.
	 */
	public void increaseCounter(StatisticsCounterType statisticsCounterType) {
		increaseCounter(statisticsCounterType, 1);
	}

	/**
	 * Increases the given counter with 1.
	 * 
	 * @param statisticsCounterType
	 *            The counter which has to be increased.
	 */
	public void increaseCounter(StatisticsCounterType statisticsCounterType,
			int occurrences) {
		switch (statisticsCounterType) {
		case CELLS_FILLED:
			mCellsFilled += occurrences;
			break;
		case CELLS_EMPTY:
			mCellsEmtpty += occurrences;
			break;
		case CELLS_REVEALED:
			mCellsRevealed += occurrences;
			break;
		case USER_VALUE_REPLACED:
			mUserValueReplaced += occurrences;
			break;
		case POSSIBLES:
			mMaybeValue += occurrences;
			break;
		case ACTION_UNDO_MOVE:
			mActionUndoMove += occurrences;
			break;
		case ACTION_CLEAR_CELL:
			mActionClearCell += occurrences;
			break;
		case ACTION_CLEAR_GRID:
			mActionClearGrid += occurrences;
			break;
		case ACTION_REVEAL_CELL:
			mActionRevealCell += occurrences;
			break;
		case ACTION_REVEAL_OPERATOR:
			mActionRevealOperator += occurrences;
			break;
		case ACTION_CHECK_PROGRESS:
			mActionCheckProgress += occurrences;
			break;
		case CHECK_PROGRESS_INVALIDS_CELLS_FOUND:
			mCheckProgressInvalidCellsFound += occurrences;
			break;
		}
		setLastMoveToCurrentTime();
	}

	/**
	 * Decreases the given counter with 1.
	 * 
	 * @param statisticsCounterType
	 *            The counter which has to be increased.
	 */
	public void decreaseCounter(StatisticsCounterType statisticsCounterType) {
		switch (statisticsCounterType) {
		case CELLS_FILLED:
			mCellsFilled--;
			break;
		case CELLS_EMPTY:
			mCellsEmtpty--;
			break;
		case CELLS_REVEALED:
			mCellsRevealed--;
			break;
		default:
			// Not available for other counters.
		}
		setLastMoveToCurrentTime();
	}

	/**
	 * Update the statistics in case a grid is solved.
	 */
	public void solved() {
		if (!mSolutionRevealed) {
			mSolvedManually = true;
		}
		mFinished = true;
		setLastMoveToCurrentTime();
	}

	/**
	 * Update the statistics in case the solution of a grid has been revealed.
	 */
	public void solutionRevealed() {
		mSolutionRevealed = true;
		mSolvedManually = false;
		mFinished = true;
		setLastMoveToCurrentTime();
	}

	/**
	 * Updates the date time of the last move.
	 */
	private void setLastMoveToCurrentTime() {
		mLastMove = new java.sql.Timestamp(System.currentTimeMillis());
	}

	/**
	 * Get the id for these statistics.
	 * 
	 * @return The id for these statistics.
	 */
	public int getId() {
		return mId;
	}

	/**
	 * Get the grid id for these statistics.
	 * 
	 * @return The grid id for these statistics.
	 */
	public int getGridId() {
		return mGridId;
	}

	/**
	 * If the grid is replayed, get the number of times the grid is replayed so
	 * far.
	 * 
	 * @return 0 in case the grid is not replayed. 1 in case this grid is
	 *         replayed once, 2 in case it is replayed twice, etc.
	 */
	public int getReplayCount() {
		return mReplayCount;
	}

	/**
	 * Get the timestamp of the first move.
	 * 
	 * @return The timestamp of the first move.
	 */
	public java.sql.Timestamp getFirstMove() {
		return mFirstMove;
	}

	/**
	 * Get the timestamp of the last move.
	 * 
	 * @return The timestamp of the first move.
	 */
	public java.sql.Timestamp getLastMove() {
		return mLastMove;
	}

	/**
	 * Get the elapsed time (including penalty time).
	 * 
	 * @return The elapsed time (including penalty time).
	 */
	public long getElapsedTime() {
		return mElapsedTime;
	}

	/**
	 * Gets the elapsed time (including penalty time).
	 * 
	 * @return The elapsed time (including penalty time).
	 */
	public long getCheatPenaltyTime() {
		return mCheatPenaltyTime;
	}

	/**
	 * Check whether the solution was revealed.
	 * 
	 * @return True in case the solution was revealed. False otherwise.
	 */
	public boolean isSolutionRevealed() {
		return mSolutionRevealed;
	}

	/**
	 * Check whether the grid was solved manually.
	 * 
	 * @return True in case the solution was solved manually. False otherwise.
	 */
	public boolean isSolvedManually() {
		return mSolvedManually;
	}

	/**
	 * Check whether the grid is finished (either solved manually or solution
	 * revelead).
	 * 
	 * @return True in case the grid is finished. False otherwise.
	 */
	public boolean isFinished() {
		return mFinished;
	}

	/**
	 * Checks whether these statistics are included in the cumulative and
	 * historic statistics.
	 * 
	 * @return True in case included in the cumulative and historic statistics.
	 *         False otherwise.
	 */
	public boolean isIncludedInStatistics() {
		return mIncludedInStatistics;
	}
}