package net.cactii.mathdoku.statistics;

import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;

/**
 * Statistics for a single grid.
 */
public class GridStatistics {
	// Unique row id for the signature in database.
	public int mId;

	// Unique string representation of this grid
	public String mGridSignature;

	// Size of the grid
	public int gridSize;

	// Timestamp of first and last move
	public java.sql.Timestamp mFirstMove;
	public java.sql.Timestamp mLastMove;

	// Time elapsed while playing (e.d. displaying the game)
	public long mElapsedTime;

	// Time added to the elapsed time because of using cheats
	public long mCheatPenaltyTime; // TODO: implement cheat penalty time

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
		DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = new StatisticsDatabaseAdapter(
				databaseHelper);
		return statisticsDatabaseAdapter.update(this);
	}

	/**
	 * Increases the given counter with 1.
	 * 
	 * @param statisticsCounterType
	 *            The counter which has to be increased.
	 */
	public void increaseCounter(StatisticsCounterType statisticsCounterType) {
		switch (statisticsCounterType) {
		case CELLS_FILLED:
			mCellsUserValueFilled++;
			break;
		case CELLS_EMPTY:
			mCellsUserValueEmtpty++;
			break;
		case USER_VALUE_REPLACED:
			mUserValueReplaced++;
			break;
		case POSSIBLES:
			mMaybeValue++;
			break;
		case UNDOS:
			mUndoButton++;
			break;
		case CELL_CLEARED:
			mCellCleared++;
			break;
		case CAGE_CLEARED:
			mCageCleared++;
			break;
		case GRID_CLEARED:
			mGridCleared++;
			break;
		case CELLS_REVEALED:
			mCellsRevealed++;
			break;
		case OPERATORS_REVEALED:
			mOperatorsRevevealed++;
			break;
		case CHECK_PROGRESS_USED:
			mCheckProgressUsed++;
			break;
		case CHECK_PROGRESS_INVALIDS_FOUND:
			mCheckProgressInvalidsFound++;
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
	 * Get the signature id for these statistics.
	 * 
	 * @return The signature id for these statistics.
	 */
	public int getSignatureId() {
		return mId;
	}

	/**
	 * Get the full grid signature.
	 * 
	 * @return The full grid signature.
	 */
	public String getGridSignature() {
		return mGridSignature;
	}

	/**
	 * Get the grid size.
	 * 
	 * @return The grid size.
	 */
	public int getGridSize() {
		return gridSize;
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