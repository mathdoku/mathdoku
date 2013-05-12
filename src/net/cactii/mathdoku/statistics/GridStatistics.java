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

	// The number of moves reversed via undo
	public int mUndoButton;

	// The number of times the clear button is used to clear a single cell
	public int mCellCleared;

	// The number of times a cage was cleared
	public int mCageCleared;

	// The number of times the entire grid was cleared
	public int mGridCleared;

	// *******
	// Cheats
	// *******

	// The number of cells revealed (a cheat)
	public int mCellsRevealed;

	// The number of cage operators revealed (a cheat)
	public int mOperatorsRevevealed;

	// The number of times "check progress" was used and the total number of
	// invalids values which have been found when using this option (a cheat)
	public int mCheckProgressUsed;
	public int mCheckProgressInvalidsFound;

	// Has the entire solution been revealed?
	public boolean mSolutionRevealed;

	// ***********************
	// Status for entire grid
	// ***********************

	// Cells filled and empty
	public int mCellsUserValueFilled;
	public int mCellsUserValueEmtpty;

	// Has the grid been solved manually (i.e. not revealed)?
	public boolean mSolvedManually;

	// Has the grid been finished (either solved or revealed solution)?
	public boolean mFinished;

	// Counters available
	public enum StatisticsCounterType {
		CELLS_FILLED, CELLS_EMPTY, USER_VALUE_REPLACED, POSSIBLES, UNDOS, CELL_CLEARED, CAGE_CLEARED, GRID_CLEARED, CELLS_REVEALED, OPERATORS_REVEALED, CHECK_PROGRESS_USED, CHECK_PROGRESS_INVALIDS_FOUND
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
			mCellsUserValueFilled += occurrences;
			break;
		case CELLS_EMPTY:
			mCellsUserValueEmtpty += occurrences;
			break;
		case USER_VALUE_REPLACED:
			mUserValueReplaced += occurrences;
			break;
		case POSSIBLES:
			mMaybeValue += occurrences;
			break;
		case UNDOS:
			mUndoButton += occurrences;
			break;
		case CELL_CLEARED:
			mCellCleared += occurrences;
			break;
		case CAGE_CLEARED:
			mCageCleared += occurrences;
			break;
		case GRID_CLEARED:
			mGridCleared += occurrences;
			break;
		case CELLS_REVEALED:
			mCellsRevealed += occurrences;
			break;
		case OPERATORS_REVEALED:
			mOperatorsRevevealed += occurrences;
			break;
		case CHECK_PROGRESS_USED:
			mCheckProgressUsed += occurrences;
			break;
		case CHECK_PROGRESS_INVALIDS_FOUND:
			mCheckProgressInvalidsFound += occurrences;
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
			mCellsUserValueFilled--;
			break;
		case CELLS_EMPTY:
			mCellsUserValueEmtpty--;
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
}