package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter.SizeFilter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter.StatusFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
 * fragment representing an object in the collection.
 */
public class ArchiveFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

	// Remove "&& false" in following line to show the solving attempt id in the
	// pager title of the archive.
	public static final boolean DEBUG_SHOW_SOLVING_ATTEMPT_ID = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	public static final int UNKNOWN_GRID_ID = -1;
	public static final int INVALID_POSITION_ID = -2;

	// The list of grids which can be shown with the adapter. Per grid the
	// latest solving attempt is also retrieved.
	private int[][] mGridIds;

	// Selected filters
	private StatusFilter mStatusFilter;
	private SizeFilter mSizeFilter;

	// Label used in the pager strip
	private static String mLabelPuzzleNumber;

	public ArchiveFragmentStatePagerAdapter(
			android.support.v4.app.FragmentManager fragmentManager,
			ArchiveFragmentActivity archiveFragmentActivity) {
		super(fragmentManager);

		// Initialize the filters
		mStatusFilter = StatusFilter.ALL;
		mSizeFilter = SizeFilter.ALL;

		// Initialize the label used in the page titles.
		mLabelPuzzleNumber = archiveFragmentActivity.getResources().getString(
				R.string.archive_pager_puzzle_number);

		// Determine id's of grids/solving attempts which are available for
		// display.
		setGridIds();
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		android.support.v4.app.Fragment fragment = new ArchiveFragment();
		Bundle args = new Bundle();
		args.putInt(
				ArchiveFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID,
				mGridIds[i][GridDatabaseAdapter.LATEST_SOLVING_ATTEMPT_PER_GRID__SOLVING_ATTEMP_ID]);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getItemPosition(Object item) {
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		return (mGridIds == null ? 0 : mGridIds.length);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (mGridIds != null && position >= 0 && position < mGridIds.length) {
			if (DEBUG_SHOW_SOLVING_ATTEMPT_ID) {
				return mLabelPuzzleNumber
						+ " "
						+ mGridIds[position][GridDatabaseAdapter.LATEST_SOLVING_ATTEMPT_PER_GRID__GRID_ID]
						+ "("
						+ mGridIds[position][GridDatabaseAdapter.LATEST_SOLVING_ATTEMPT_PER_GRID__SOLVING_ATTEMP_ID]
						+ ")";
			} else {
				return mLabelPuzzleNumber
						+ " "
						+ mGridIds[position][GridDatabaseAdapter.LATEST_SOLVING_ATTEMPT_PER_GRID__GRID_ID];
			}
		} else {
			return mLabelPuzzleNumber;
		}
	}

	/**
	 * Set the status filter to the given value.
	 * 
	 * @param statusFilter
	 *            The new value of the status filter.
	 */
	public void setStatusFilter(StatusFilter statusFilter) {
		if (statusFilter != mStatusFilter) {
			mStatusFilter = statusFilter;
			setGridIds();
		}
	}

	/**
	 * Get the currently selected value of the status filter.
	 * 
	 * @return The currently selected value of the status filter.
	 */
	public StatusFilter getStatusFilter() {
		return mStatusFilter;
	}

	/**
	 * Set the size filter to the given value.
	 * 
	 * @param statusFilter
	 *            The new value of the size filter.
	 */
	public void setSizeFilter(SizeFilter sizeFilter) {
		if (sizeFilter != mSizeFilter) {
			mSizeFilter = sizeFilter;
			setGridIds();
		}
	}

	/**
	 * Get the currently selected value of the size filter.
	 * 
	 * @return The currently selected value of the size filter.
	 */
	public SizeFilter getSizeFilter() {
		return mSizeFilter;
	}

	/**
	 * Set all grid id's which can be displayed using the adapter.
	 */
	private void setGridIds() {
		// Determine which grid should be shown
		GridDatabaseAdapter gridDatabaseAdapter = new GridDatabaseAdapter();
		mGridIds = gridDatabaseAdapter.getLatestSolvingAttemptsPerGrid(
				mStatusFilter, mSizeFilter);
		notifyDataSetChanged();
	}

	/**
	 * Get the position in the adapter at which the given grid is placed.
	 * 
	 * @param gridId
	 *            The grid id to be found.
	 * @returns The position in the adapter at which the given grid is placed.
	 *          {@value #UNKNOWN_GRID_ID} in case the grid id is not known to
	 *          this adapter.
	 */
	public int getPositionOfGridId(int gridId) {
		// Check position of given solving attempt id.
		for (int i = 0; i < mGridIds.length; i++) {
			if (mGridIds[i][GridDatabaseAdapter.LATEST_SOLVING_ATTEMPT_PER_GRID__GRID_ID] == gridId) {
				return i;
			}
		}

		return UNKNOWN_GRID_ID;
	}

	/**
	 * Get the grid id at the given position.
	 * 
	 * @param position
	 *            Position in adapter for which the grid id has to be returned.
	 * 
	 * @return The grid id at the given position.
	 */
	public int getGridId(int position) {
		if (mGridIds != null && position >= 0 && position < mGridIds.length) {
			return mGridIds[position][GridDatabaseAdapter.LATEST_SOLVING_ATTEMPT_PER_GRID__GRID_ID];
		} else {
			return INVALID_POSITION_ID;
		}
	}

	/**
	 * Get the solving attempt for the grid at the given position.
	 * 
	 * @param position
	 *            Position in adapter for which the solving attempt of the grid
	 *            has to be returned.
	 * 
	 * @return The grid id at the given position.
	 */
	public int getSolvingAttemptId(int position) {
		if (mGridIds != null && position >= 0 && position < mGridIds.length) {
			return mGridIds[position][GridDatabaseAdapter.LATEST_SOLVING_ATTEMPT_PER_GRID__SOLVING_ATTEMP_ID];
		} else {
			return INVALID_POSITION_ID;
		}
	}
}