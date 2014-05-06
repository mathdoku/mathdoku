package net.mathdoku.plus.puzzle.grid;

import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.GridRow;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptRow;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import java.security.InvalidParameterException;

public class GridSaver {
	private int mRowId;
	private int mSolvingAttemptId;
	private GridStatistics mGridStatistics;
	private long mDateUpdated;
	private boolean mSetDateUpdatedOnUpdateOfSolvingAttempt;
	private boolean mIsSaved;

	public static class ObjectsCreator {
		public DatabaseHelper createDatabaseHelper() {
			return DatabaseHelper.getInstance();
		}

		public GridDatabaseAdapter createGridDatabaseAdapter() {
			return new GridDatabaseAdapter();
		}

		public SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
			return new SolvingAttemptDatabaseAdapter();
		}

		public StatisticsDatabaseAdapter createStatisticsDatabaseAdapter() {
			return new StatisticsDatabaseAdapter();
		}

		public SolvingAttemptRow createSolvingAttempt() {
			return new SolvingAttemptRow();
		}

		public GridStorage createGridStorage() {
			return new GridStorage();
		}
	}

	GridSaver.ObjectsCreator mObjectsCreator;

	public GridSaver() {
		mObjectsCreator = new GridSaver.ObjectsCreator();
		mSetDateUpdatedOnUpdateOfSolvingAttempt = true;
		mIsSaved = false;
	}

	public GridSaver setObjectsCreator(GridSaver.ObjectsCreator objectsCreator) {
		if (objectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter objectsCreator cannot be null.");
		}
		mObjectsCreator = objectsCreator;

		return this;
	}

	public boolean saveOnAppUpgrade(Grid grid) {
		// When save due to App Upgrade the DateUpdate is not changed to the
		// current time as the user has not changed the game by playing it.
		mSetDateUpdatedOnUpdateOfSolvingAttempt = false;
		return save(grid);
	}

	/**
	 * Save this grid (solving attempt and statistics).
	 * 
	 * @return True in case everything has been saved. False otherwise.
	 */
	public boolean save(Grid grid) {
		mRowId = grid.getRowId();
		mSolvingAttemptId = grid.getSolvingAttemptId();
		mGridStatistics = grid.getGridStatistics();

		DatabaseHelper databaseHelper = mObjectsCreator.createDatabaseHelper();
		databaseHelper.beginTransaction();

		mIsSaved = saveGridRow(grid) && saveSolvingAttempt(grid)
				&& saveStatistics(grid);
		if (mIsSaved) {
			databaseHelper.setTransactionSuccessful();
		}

		databaseHelper.endTransaction();

		return mIsSaved;
	}

	private boolean saveGridRow(Grid grid) {
		// Insert grid record if it does not yet exists. The grid record never
		// needs to be updated as the definition is immutable.
		if (mRowId < 0) {
			// Before insert first check if already a grid record exists for the
			// grid definition. If so, then reuse the existing grid definition.
			String gridDefinition = grid.getDefinition();
			GridDatabaseAdapter gridDatabaseAdapter = mObjectsCreator
					.createGridDatabaseAdapter();
			GridRow gridRow = gridDatabaseAdapter
					.getByGridDefinition(gridDefinition);
			mRowId = gridRow == null ? gridDatabaseAdapter.insert(grid)
					: gridRow.getGridId();
		}

		return mRowId >= 0;
	}

	private boolean saveSolvingAttempt(Grid grid) {
		SolvingAttemptRow solvingAttemptRow = mObjectsCreator
				.createSolvingAttempt();
		solvingAttemptRow.mId = mSolvingAttemptId;
		solvingAttemptRow.mGridId = mRowId;
		solvingAttemptRow.mDateCreated = grid.getDateCreated();
		solvingAttemptRow.mDateUpdated = grid.getDateSaved();
		solvingAttemptRow.mSavedWithRevision = Util.getPackageVersionNumber();
		solvingAttemptRow.mStorageString = mObjectsCreator
				.createGridStorage()
				.toStorageString(grid);
		solvingAttemptRow.mSolvingAttemptStatus = SolvingAttemptStatus
				.getDerivedStatus(grid.isSolutionRevealed(), grid.isActive(),
						grid.isEmpty());

		// Insert or update the solving attempt.
		SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = mObjectsCreator
				.createSolvingAttemptDatabaseAdapter();
		if (mSolvingAttemptId < 0) {
			mDateUpdated = solvingAttemptRow.mDateUpdated;
			mSolvingAttemptId = solvingAttemptDatabaseAdapter
					.insert(solvingAttemptRow);
			return mSolvingAttemptId >= 0;
		} else {
			if (mSetDateUpdatedOnUpdateOfSolvingAttempt) {
				solvingAttemptRow.mDateUpdated = System.currentTimeMillis();
			}
			mDateUpdated = solvingAttemptRow.mDateUpdated;
			return solvingAttemptDatabaseAdapter.update(solvingAttemptRow);
		}
	}

	private boolean saveStatistics(Grid grid) {
		// Insert or update the grid statistics.
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = mObjectsCreator
				.createStatisticsDatabaseAdapter();
		if (mGridStatistics.mId < 0) {
			mGridStatistics.mGridId = mRowId;
			mGridStatistics.mCellsEmpty = grid.getGridSize()
					* grid.getGridSize();
			mGridStatistics.setFirstMove(mDateUpdated);
			mGridStatistics.setLastMove(mDateUpdated);

			// Count all solving attempts for this grid. Note taht the solving
			// attempt is already inserted before the statistics are inserted.
			int countSolvingAttemptsForGrid = mObjectsCreator
					.createSolvingAttemptDatabaseAdapter()
					.countSolvingAttemptForGrid(mRowId);
			mGridStatistics.mReplayCount = countSolvingAttemptsForGrid - 1;
			mGridStatistics.mIncludedInStatistics = countSolvingAttemptsForGrid == 1;
			mGridStatistics.mId = statisticsDatabaseAdapter
					.insert(mGridStatistics);

			return mGridStatistics.mId >= 0;
		} else {
			if (!mGridStatistics.save()) {
				return false;
			}

			// In case a replay of the grid is finished the statistics which
			// have to included in the cumulative and the historic statistics
			// should be changed to the current solving attempt.
			if (!grid.isActive() && mGridStatistics.getReplayCount() > 0
					&& !mGridStatistics.isIncludedInStatistics()) {
				// Note: do not return false in case following fails as it is
				// not relevant to the user.
				statisticsDatabaseAdapter
						.updateSolvingAttemptToBeIncludedInStatistics(
								grid.getRowId(), grid.getSolvingAttemptId());
			}

			return true;
		}
	}

	public int getRowId() {
		if (!mIsSaved) {
			throw new UnexpectedMethodInvocationException(
					"Method 'getRowId' can only be called after successful save.");
		}
		return mRowId;
	}

	public int getSolvingAttemptId() {
		if (!mIsSaved) {
			throw new UnexpectedMethodInvocationException(
					"Method 'getSolvingAttemptId' can only be called after successful save.");
		}
		return mSolvingAttemptId;
	}

	public GridStatistics getGridStatistics() {
		if (!mIsSaved) {
			throw new UnexpectedMethodInvocationException(
					"Method 'getGridStatistics' can only be called after successful save.");
		}
		return mGridStatistics;
	}

	public long getDateUpdated() {
		if (!mIsSaved) {
			throw new UnexpectedMethodInvocationException(
					"Method 'getDateUpdated' can only be called after successful save.");
		}
		return mDateUpdated;
	}
}
