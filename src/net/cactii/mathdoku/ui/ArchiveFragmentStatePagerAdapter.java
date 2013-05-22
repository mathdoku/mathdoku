package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
 * fragment representing an object in the collection.
 */
public class ArchiveFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

	ArchiveFragmentActivity mArchiveFragmentActivity;

	// The list of grid id's which have to be shown.
	private int[] mGridIds;

	// The grid id which is currently shown
	private int mCurrentGridId;

	// Selected filter on status
	private StatusFilter mStatusFilter;

	private String mLabelPuzzleNumber;

	public enum StatusFilter {
		ALL, UNFINISHED, SOLVED, CHEATED
	};

	// Selected filter on size
	private SizeFilter mSizeFilter;

	public enum SizeFilter {
		ALL, SIZE_4, SIZE_5, SIZE_6, SIZE_7, SIZE_8, SIZE_9
	};

	public ArchiveFragmentStatePagerAdapter(
			android.support.v4.app.FragmentManager fragmentManager,
			ArchiveFragmentActivity archiveFragmentActivity) {
		super(fragmentManager);
		mArchiveFragmentActivity = archiveFragmentActivity;

		mStatusFilter = StatusFilter.ALL;
		mSizeFilter = SizeFilter.ALL;

		mLabelPuzzleNumber = mArchiveFragmentActivity.getResources().getString(
				R.string.archive_pager_puzzle_number);

		// Determine id's of grids/solving attempts which are available for
		// display.
		setGridIds();
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		mCurrentGridId = mGridIds[i];

		android.support.v4.app.Fragment fragment = new ArchiveFragment();
		Bundle args = new Bundle();
		args.putInt(ArchiveFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID,
				mCurrentGridId);
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
			return mLabelPuzzleNumber + " " + mGridIds[position];
		} else {
			return mLabelPuzzleNumber;
		}
	}

	public void setStatusFilter(StatusFilter statusFilter) {
		mStatusFilter = statusFilter;
		setGridIds();
	}

	public StatusFilter getStatusFilter() {
		return mStatusFilter;
	}

	public void setSizeFilter(SizeFilter sizeFilter) {
		mSizeFilter = sizeFilter;
		setGridIds();
	}

	public SizeFilter getSizeFilter() {
		return mSizeFilter;
	}

	private void setGridIds() {
		// Determine which grid should be shown
		GridDatabaseAdapter gridDatabaseAdapter = new GridDatabaseAdapter();
		mGridIds = gridDatabaseAdapter
				.getAllGridIds(mStatusFilter, mSizeFilter);
		notifyDataSetChanged();
	}

	/**
	 * Get the position in the adapter at which the given grid is placed.
	 * 
	 * @param gridId
	 *            The grid id to be found.
	 * @returns The position in the adapter at which the given grid is placed.
	 *          -1 in case the grid id is not known to this adapter.
	 */
	public int getPositionOfGridId(int gridId) {
		// Check position of given solving attempt id.
		for (int i = 0; i < mGridIds.length; i++) {
			if (mGridIds[i] == gridId) {
				return i;
			}
		}

		return -1;
	}
}