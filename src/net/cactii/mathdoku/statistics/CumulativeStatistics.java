package net.cactii.mathdoku.statistics;

public class CumulativeStatistics {
	// Minimum and maximum size of grids cumulated 
	public int mMinGridSize;
	public int mMaxGridSize;

	// Timestamp of earliest first and the latest last move
	public java.sql.Timestamp mMinFirstMove;
	public java.sql.Timestamp mMaxLastMove;

	// Time elapsed while playing (e.d. displaying the game)
	public long mSumElapsedTime;
	public long mAvgElapsedTime;
	public long mMinElapsedTime;
	public long mMaxElapsedTime;

	// Time added to the elapsed time because of using cheats
	public long mSumCheatPenaltyTime;
	public long mAvgCheatPenaltyTime;
	public long mMinCheatPenaltyTime;
	public long mMaxCheatPenaltyTime;

	// ****************
	// Avoidable moves
	// ****************

	// The number of times a user value in cell was replaced by another value
	public int mSumUserValueReplaced;

	// The number of possible values used
	public int mSumMaybeValue;

	// The number of moves reversed via undo
	public int mSumUndoButton;

	// The number of times the clear button is used to clear a single cell
	public int mSumCellCleared;

	// The number of times a cage was cleared
	public int mSumCageCleared;

	// The number of times the entire grid was cleared
	public int mSumGridCleared;

	// *******
	// Cheats
	// *******

	// The number of cells revealed (a cheat)
	public int mSumCellsRevealed;

	// The number of cage operators revealed (a cheat)
	public int mSumOperatorsRevevealed;

	// The number of times "check progress" was used and the total number of
	// invalids values which have been found when using this option (a cheat)
	public int mSumCheckProgressUsed;
	public int mSumcheckProgressInvalidsFound;

	// ***********************
	// Total grids per status
	// ***********************
	public int mCountStarted;
	public int mCountSolutionRevealed;
	public int mCountSolvedManually;
	public int mCountFinished;


	// Cells filled and empty
	public int mSumCellsUserValueFilled;
	public int mSumCellsUserValueEmtpty;
	
	/**
	 * Checks whether the statistics applies to grids of the same grid size.
	 * @return True in case the statistics refer to one single level. 
	 */
	public boolean isSingleLevelStatistics() {
		return (mMinGridSize == mMaxGridSize);
	}

}
