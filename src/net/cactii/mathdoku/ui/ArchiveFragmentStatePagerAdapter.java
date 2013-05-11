package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
 * fragment representing an object in the collection.
 */
public class ArchiveFragmentStatePagerAdapter extends
		FragmentStatePagerAdapter {

	ArchiveFragmentActivity mArchiveFragmentActivity;
	
	private int[] mGridIds;

	public ArchiveFragmentStatePagerAdapter(
			android.support.v4.app.FragmentManager fragmentManager,
			ArchiveFragmentActivity archiveFragmentActivity) {
		super(fragmentManager);
		mArchiveFragmentActivity = archiveFragmentActivity;

		// Determine number of grid available.
		GridDatabaseAdapter gridDatabaseAdapter = new GridDatabaseAdapter();
		mGridIds = gridDatabaseAdapter.getAllGridIds();
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		android.support.v4.app.Fragment fragment = new ArchiveFragment();
		Bundle args = new Bundle();
		args.putInt(ArchiveFragment.ARG_OBJECT, mGridIds[i]);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getCount() {
		return (mGridIds == null ? 0 : mGridIds.length);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return "Puzzle " + (position + 1);
	}
}