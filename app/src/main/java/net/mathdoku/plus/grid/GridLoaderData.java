package net.mathdoku.plus.grid;

import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;

import java.util.ArrayList;

/**
 * This class holds parsed data as retrieved by the
 * {@link net.mathdoku.plus.grid.GridLoader} from the database which is needed
 * to create a new {@link net.mathdoku.plus.grid.Grid}.
 */
public class GridLoaderData {
	public int mGridSize;
	public GridGeneratingParameters mGridGeneratingParameters;
	public GridStatistics mGridStatistics;
	public long mDateCreated;
	public long mDateUpdated;
	public int mSolvingAttemptId;
	public ArrayList<GridCell> mCells;
	public ArrayList<GridCage> mCages;
	public ArrayList<CellChange> mCellChanges;
	public boolean mActive;
	public boolean mRevealed;


	@Override
	public String toString() {
		return "GridLoaderData{\n" +
				"\tmGridSize=" + mGridSize + "\n" +
				"\tmGridGeneratingParameters=" + mGridGeneratingParameters +"\n" +
				"\tmGridStatistics=" + mGridStatistics +"\n" +
				"\tmDateCreated=" + mDateCreated +"\n" +
				"\tmDateUpdated=" + mDateUpdated +"\n" +
				"\tmSolvingAttemptId=" + mSolvingAttemptId +"\n" +
				"\tmCells count=" + (mCells == null ? "null" : mCells.size()) +"\n" +
				"\tmCages count=" + (mCages == null ? "null" : mCages.size()) +"\n" +
				"\tmCellChanges count=" +(mCellChanges == null ? "null" : mCellChanges.size()) +"\n" +
				"\tmActive=" + mActive +"\n" +
				"\tmRevealed=" + mRevealed + "\n" +
				'}';
	}
}