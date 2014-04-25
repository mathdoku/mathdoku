package net.mathdoku.plus.leaderboard.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.leaderboard.LeaderboardType;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of leaderboard pages.
 */
class LeaderboardFragmentPagerAdapter extends FragmentPagerAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = LeaderboardFragmentPagerAdapter.class
			.getName();

	private static final GridType[] GRID_SIZES_WITH_LEADERBOARD = LeaderboardType
			.getGridSizeWithLeaderboard();

	// Context
	private final LeaderboardFragmentActivity mLeaderboardFragmentActivity;

	public LeaderboardFragmentPagerAdapter(
			LeaderboardFragmentActivity leaderboardFragmentActivity,
			android.support.v4.app.FragmentManager fragmentManager) {
		super(fragmentManager);
		mLeaderboardFragmentActivity = leaderboardFragmentActivity;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int index) {
		Bundle bundle = new Bundle();
		bundle.putInt(LeaderboardFragment.ARG_GRID_SIZE,
				GRID_SIZES_WITH_LEADERBOARD[index].getGridSize());
		bundle.putInt(LeaderboardFragment.ARG_FILTER,
				mLeaderboardFragmentActivity.getLeaderboardFilter().ordinal());

		// Create fragment and pass the bundle
		android.support.v4.app.Fragment fragment = new LeaderboardFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public int getCount() {
		return GRID_SIZES_WITH_LEADERBOARD.length;
	}

	@Override
	public CharSequence getPageTitle(int index) {
		return Integer.toString(GRID_SIZES_WITH_LEADERBOARD[index]
				.getGridSize());
	}

	/**
	 * Gets the fragment for the given position in the adapter.
	 * 
	 * @param viewPager
	 *            The view pager which contains the fragment.
	 * @param position
	 *            The position of the fragment to be found.
	 * @param fragmentManager
	 *            The fragment manager from which the fragment has to be
	 *            retrieved.
	 * @return The fragment corresponding with the given position in the adapter
	 *         of the view pager.
	 */
	public LeaderboardFragment getFragment(ViewPager viewPager, int position,
			FragmentManager fragmentManager) {
		String name = makeFragmentName(viewPager.getId(), position);
		return (LeaderboardFragment) fragmentManager.findFragmentByTag(name);
	}

	/**
	 * Gets the fragment name as constructed by Android to identify the
	 * fragment.
	 * 
	 * @param viewPagerId
	 *            The id of the view pager.
	 * @param index
	 *            The index in the view pager.
	 * @return The fragment name as constructed by Android to identify the
	 *         fragment.
	 */
	private String makeFragmentName(int viewPagerId, int index) {
		return "android:switcher:" + viewPagerId + ":" + index;
	}
}
