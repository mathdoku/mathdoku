package net.cactii.mathdoku.ui;

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
		
		// Determine number of grid available.
		setGridIds();
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		mCurrentGridId = mGridIds[i];

		android.support.v4.app.Fragment fragment = new ArchiveFragment();
		Bundle args = new Bundle();
		args.putInt(ArchiveFragment.ARG_OBJECT, mCurrentGridId);
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
		return "Puzzle " + (position + 1);
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
		mGridIds = gridDatabaseAdapter.getAllGridIds(mStatusFilter, mSizeFilter);
		notifyDataSetChanged();
	}
}