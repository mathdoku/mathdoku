package net.mathdoku.plus.grid;

import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;

import java.util.List;

/**
 * This class holds all data needed to build a new
 * {@link net.mathdoku.plus.grid.Grid} instance.
 */
public class GridBuilder {
	Grid.ObjectsCreator mGridObjectsCreator; // Optional
	int mGridSize; // Required
	GridGeneratingParameters mGridGeneratingParameters; // Required
	GridStatistics mGridStatistics; // Optional
	long mDateCreated; // Optional
	long mDateUpdated; // Optional
	int mGridId; // Optional
	int mSolvingAttemptId; // Optional
	List<GridCell> mCells; // Required
	List<GridCage> mCages; // Required
	List<CellChange> mCellChanges; // Optional
	boolean mActive; // Optional
	boolean mRevealed; // Optional

	public GridBuilder() {
		// Set default values for all optionals
		mGridStatistics = null;
		mDateCreated = System.currentTimeMillis();
		mDateUpdated = mDateCreated;
		mGridId = -1; // 0 is a valid id in the database and should not be used
						// as default.
		mSolvingAttemptId = -1; // 0 is a valid id in the database and should
								// not be used as default.
		mCellChanges = null;
		mActive = true;
		mRevealed = false;
	}

	/**
	 * Sets the Grids Object Creator which has to be passed to the Grid
	 * constructor. This is not an ObjectCreator for class GridBuilder itself.
	 * 
	 * This method is intended to be called in unit tests only in case the
	 * Grid.ObjectsCreator of class Grid needs to be overridden.
	 * 
	 * @param gridObjectsCreator
	 *            The Grid.ObjectCreator to be passed to the Grid constructor.
	 * @return the GridBuilder object so it can be chained.
	 */
	public GridBuilder setGridObjectsCreator(
			Grid.ObjectsCreator gridObjectsCreator) {
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
		// Solving attempt should not be set without setting its corresponding
		// grid id.
		mGridId = gridId;
		mSolvingAttemptId = solvingAttemptId;
		return this;
	}

	public GridBuilder setCells(List<GridCell> cells) {
		mCells = cells;
		return this;
	}

	public GridBuilder setCages(List<GridCage> cages) {
		mCages = cages;
		return this;
	}

	public GridBuilder setCellChanges(List<CellChange> cellChanges) {
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
		// The GridBuilder class does not use the ObjectsCreator pattern to
		// create the new grid object.
		return new Grid(this);
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
