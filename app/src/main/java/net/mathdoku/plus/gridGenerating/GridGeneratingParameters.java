package net.mathdoku.plus.gridGenerating;

import net.mathdoku.plus.enums.PuzzleComplexity;

/**
 * All parameters which have influenced the grid generating process.
 */
public class GridGeneratingParameters {
	public int mGeneratorRevisionNumber;
	public PuzzleComplexity mPuzzleComplexity;
	public long mGameSeed;
	public int mMaxCageSize;
	public int mMaxCageResult;
	public boolean mHideOperators;
}
