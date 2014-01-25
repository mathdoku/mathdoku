package net.mathdoku.plus.grid;

import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;

import java.util.ArrayList;

/**
 * This class holds parsed data as retrieved by the
 * {@link net.mathdoku.plus.grid.GridLoader} from the database which is needed
 * to create a new {@link net.mathdoku.plus.grid.Grid}.
 */
public class GridBuilder {
	GridObjectsCreator mGridObjectsCreator; // Optional
	int mGridSize; // Required
	GridGeneratingParameters mGridGeneratingParameters; // Required
	GridStatistics mGridStatistics; // Optional
	long mDateCreated; // Optional
	long mDateUpdated; // Optional
	int mGridId; // Optional
	int mSolvingAttemptId; // Optional
	ArrayList<GridCell> mCells; // Required
	ArrayList<GridCage> mCages; // Required
	ArrayList<CellChange> mCellChanges; // Optional
	boolean mActive; // Optional
	boolean mRevealed; // Optional

	public GridBuilder() {
		// Set default values for all optionals
		mGridObjectsCreator = new GridObjectsCreator();
		mGridStatistics = null;
		mDateCreated = System.currentTimeMillis();
		mDateUpdated = mDateCreated;
		mGridId = -1; // 0 is a valid id in the database and should not be used as default.
		mSolvingAttemptId = -1; // 0 is a valid id in the database and should not be used as default.
		mCellChanges = null;
		mActive = true;
		mRevealed = false;
	}

	/**
	 * Only for Unit test purposes the objects creator should be overwritten if
	 * needed.
	 */
	public GridBuilder setObjectsCreator(GridObjectsCreator gridObjectsCreator) {
		mGridObjectsCreator = gridObjectsCreator;
		return this;
	}

	public GridBuilder setGridSize(int gridSize) {
		mGridSize = gridSize;
		return this;
	}

	public GridBuilder setGridGeneratingParameters(
			GridGeneratingParameters gridGeneratingParameters) {
		mGridGeneratingParameters = gridGeneratingParameters;
		return this;
	}

	public GridBuilder setGridStatistics(GridStatistics gridStatistics) {
		mGridStatistics = gridStatistics;
		return this;
	}

	public GridBuilder setDateCreated(long dateCreated) {
		mDateCreated = dateCreated;
		if (mDateCreated > mDateUpdated) {
			mDateUpdated = mDateCreated;
		}
		return this;
	}

	public GridBuilder setDateUpdated(long dateUpdated) {
		mDateUpdated = dateUpdated;
		if (mDateUpdated < mDateCreated) {
			mDateCreated = mDateUpdated;
		}
		return this;
	}

	public GridBuilder setGridId(int gridId) {
		mGridId = gridId;
		return this;
	}

	public GridBuilder setSolvingAttemptId(int gridId, int solvingAttemptId) {
		// Solving attempt should not be set without setting its corresponding grid id.
		mGridId = gridId;
		mSolvingAttemptId = solvingAttemptId;
		return this;
	}

	public GridBuilder setCells(ArrayList<GridCell> cells) {
		mCells = cells;
		return this;
	}

	public GridBuilder setCages(ArrayList<GridCage> cages) {
		mCages = cages;
		return this;
	}

	public GridBuilder setCellChanges(ArrayList<CellChange> cellChanges) {
		mCellChanges = cellChanges;
		return this;
	}

	public GridBuilder setActive(boolean active) {
		mActive = active;
		return this;
	}

	public GridBuilder setRevealed(boolean revealed) {
		mRevealed = revealed;
		return this;
	}

	public Grid build() {
		return mGridObjectsCreator.createGrid(this);
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("GridBuilder{\n");
		stringBuilder.append("\tmGridSize=" + mGridSize + "\n");
		stringBuilder.append("\tmGridGeneratingParameters="
				+ mGridGeneratingParameters + "\n");
		stringBuilder.append("\tmGridStatistics=" + mGridStatistics + "\n");
		stringBuilder.append("\tmDateCreated=" + mDateCreated + "\n");
		stringBuilder.append("\tmDateUpdated=" + mDateUpdated + "\n");
		stringBuilder.append("\tmSolvingAttemptId=" + mSolvingAttemptId + "\n");
		stringBuilder.append("\tmCells count="
				+ (mCells == null ? "null" : mCells.size()) + "\n");
		stringBuilder.append("\tmCages count="
				+ (mCages == null ? "null" : mCages.size()) + "\n");
		stringBuilder.append("\tmCellChanges count="
				+ (mCellChanges == null ? "null" : mCellChanges.size()) + "\n");
		stringBuilder.append("\tmActive=" + mActive + "\n");
		stringBuilder.append("\tmRevealed=" + mRevealed + "\n");
		stringBuilder.append('}');
		return stringBuilder.toString();
	}
}