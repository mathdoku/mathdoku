package net.mathdoku.plus.statistics;

import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;

/**
 * Statistics for a single grid.
 */
public class GridStatistics {
	// Unique row id for the statistics. This value may not be altered.
	public int mId = -1; // -1 if not saved

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
	public int mEnteredValueReplaced;

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
	public int mCellsEmpty;

	// Cells revealed. This value does not need to be identical to
	// mActionRevealCell in case reveal solution is also used.
	public int mCellsRevealed;

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
	}

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
			mCellsEmpty += occurrences;
			break;
		case CELLS_REVEALED:
			mCellsRevealed += occurrences;
			break;
		case USER_VALUE_REPLACED:
			mEnteredValueReplaced += occurrences;
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
			mCellsEmpty--;
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
	 * Check whether the grid is finished (either solved manually or solution
	 * revealed).
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

	public void setFirstMove(long datetime) {
		mFirstMove = new java.sql.Timestamp(datetime);
	}

	public void setLastMove(long datetime) {
		mLastMove = new java.sql.Timestamp(datetime);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("GridStatistics{");
		sb.append("mId=")
				.append(mId);
		sb.append(", mGridId=")
				.append(mGridId);
		sb.append(", mReplayCount=")
				.append(mReplayCount);
		sb.append(", mFirstMove=")
				.append(mFirstMove);
		sb.append(", mLastMove=")
				.append(mLastMove);
		sb.append(", mElapsedTime=")
				.append(mElapsedTime);
		sb.append(", mCheatPenaltyTime=")
				.append(mCheatPenaltyTime);
		sb.append(", mEnteredValueReplaced=")
				.append(mEnteredValueReplaced);
		sb.append(", mMaybeValue=")
				.append(mMaybeValue);
		sb.append(", mActionUndoMove=")
				.append(mActionUndoMove);
		sb.append(", mActionClearCell=")
				.append(mActionClearCell);
		sb.append(", mActionClearGrid=")
				.append(mActionClearGrid);
		sb.append(", mActionRevealCell=")
				.append(mActionRevealCell);
		sb.append(", mActionRevealOperator=")
				.append(mActionRevealOperator);
		sb.append(", mActionCheckProgress=")
				.append(mActionCheckProgress);
		sb.append(", mCheckProgressInvalidCellsFound=")
				.append(mCheckProgressInvalidCellsFound);
		sb.append(", mSolutionRevealed=")
				.append(mSolutionRevealed);
		sb.append(", mCellsFilled=")
				.append(mCellsFilled);
		sb.append(", mCellsEmpty=")
				.append(mCellsEmpty);
		sb.append(", mCellsRevealed=")
				.append(mCellsRevealed);
		sb.append(", mSolvedManually=")
				.append(mSolvedManually);
		sb.append(", mFinished=")
				.append(mFinished);
		sb.append(", mIncludedInStatistics=")
				.append(mIncludedInStatistics);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GridStatistics)) {
			return false;
		}

		GridStatistics that = (GridStatistics) o;

		if (mActionCheckProgress != that.mActionCheckProgress) {
			return false;
		}
		if (mActionClearCell != that.mActionClearCell) {
			return false;
		}
		if (mActionClearGrid != that.mActionClearGrid) {
			return false;
		}
		if (mActionRevealCell != that.mActionRevealCell) {
			return false;
		}
		if (mActionRevealOperator != that.mActionRevealOperator) {
			return false;
		}
		if (mActionUndoMove != that.mActionUndoMove) {
			return false;
		}
		if (mCellsEmpty != that.mCellsEmpty) {
			return false;
		}
		if (mCellsFilled != that.mCellsFilled) {
			return false;
		}
		if (mCellsRevealed != that.mCellsRevealed) {
			return false;
		}
		if (mCheatPenaltyTime != that.mCheatPenaltyTime) {
			return false;
		}
		if (mCheckProgressInvalidCellsFound != that.mCheckProgressInvalidCellsFound) {
			return false;
		}
		if (mElapsedTime != that.mElapsedTime) {
			return false;
		}
		if (mEnteredValueReplaced != that.mEnteredValueReplaced) {
			return false;
		}
		if (mFinished != that.mFinished) {
			return false;
		}
		if (mGridId != that.mGridId) {
			return false;
		}
		if (mId != that.mId) {
			return false;
		}
		if (mIncludedInStatistics != that.mIncludedInStatistics) {
			return false;
		}
		if (mMaybeValue != that.mMaybeValue) {
			return false;
		}
		if (mReplayCount != that.mReplayCount) {
			return false;
		}
		if (mSolutionRevealed != that.mSolutionRevealed) {
			return false;
		}
		if (mSolvedManually != that.mSolvedManually) {
			return false;
		}
		if (mFirstMove != null ? !mFirstMove.equals(that.mFirstMove) : that.mFirstMove != null) {
			return false;
		}
		if (mLastMove != null ? !mLastMove.equals(that.mLastMove) : that.mLastMove != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = mId;
		result = 31 * result + mGridId;
		result = 31 * result + mReplayCount;
		result = 31 * result + (mFirstMove != null ? mFirstMove.hashCode() : 0);
		result = 31 * result + (mLastMove != null ? mLastMove.hashCode() : 0);
		result = 31 * result + (int) (mElapsedTime ^ (mElapsedTime >>> 32));
		result = 31 * result + (int) (mCheatPenaltyTime ^ (mCheatPenaltyTime >>> 32));
		result = 31 * result + mEnteredValueReplaced;
		result = 31 * result + mMaybeValue;
		result = 31 * result + mActionUndoMove;
		result = 31 * result + mActionClearCell;
		result = 31 * result + mActionClearGrid;
		result = 31 * result + mActionRevealCell;
		result = 31 * result + mActionRevealOperator;
		result = 31 * result + mActionCheckProgress;
		result = 31 * result + mCheckProgressInvalidCellsFound;
		result = 31 * result + (mSolutionRevealed ? 1 : 0);
		result = 31 * result + mCellsFilled;
		result = 31 * result + mCellsEmpty;
		result = 31 * result + mCellsRevealed;
		result = 31 * result + (mSolvedManually ? 1 : 0);
		result = 31 * result + (mFinished ? 1 : 0);
		result = 31 * result + (mIncludedInStatistics ? 1 : 0);
		return result;
	}
}