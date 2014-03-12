package net.mathdoku.plus.leaderboard.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of leaderboard pages.
 */
class LeaderboardFragmentPagerAdapter extends FragmentPagerAdapter {
	private static final String TAG = LeaderboardFragmentPagerAdapter.class
			.getName();

	private static final int FRAGMENT_ID_GRID_SIZE_4 = 0;
	private static final int FRAGMENT_ID_GRID_SIZE_5 = 1;
	private static final int FRAGMENT_ID_GRID_SIZE_6 = 2;
	private static final int FRAGMENT_ID_GRID_SIZE_7 = 3;
	private static final int FRAGMENT_ID_GRID_SIZE_8 = 4;
	private static final int FRAGMENT_ID_GRID_SIZE_9 = 5;
	private static final int[] fragment_ids = new int[] {
			FRAGMENT_ID_GRID_SIZE_4, FRAGMENT_ID_GRID_SIZE_5,
			FRAGMENT_ID_GRID_SIZE_6, FRAGMENT_ID_GRID_SIZE_7,
			FRAGMENT_ID_GRID_SIZE_8, FRAGMENT_ID_GRID_SIZE_9 };

	// Context
	private final LeaderboardFragmentActivity mLeaderboardFragmentActivity;

	public LeaderboardFragmentPagerAdapter(
			LeaderboardFragmentActivity leaderboardFragmentActivity,
			android.support.v4.app.FragmentManager fragmentManager) {
		super(fragmentManager);
		mLeaderboardFragmentActivity = leaderboardFragmentActivity;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		// Create bundle
		Bundle bundle = new Bundle();
		switch (i) {
		case FRAGMENT_ID_GRID_SIZE_4:
			bundle.putInt(LeaderboardFragment.ARG_GRID_SIZE, 4);
			break;
		case FRAGMENT_ID_GRID_SIZE_5:
			bundle.putInt(LeaderboardFragment.ARG_GRID_SIZE, 5);
			break;
		case FRAGMENT_ID_GRID_SIZE_6:
			bundle.putInt(LeaderboardFragment.ARG_GRID_SIZE, 6);
			break;
		case FRAGMENT_ID_GRID_SIZE_7:
			bundle.putInt(LeaderboardFragment.ARG_GRID_SIZE, 7);
			break;
		case FRAGMENT_ID_GRID_SIZE_8:
			bundle.putInt(LeaderboardFragment.ARG_GRID_SIZE, 8);
			break;
		case FRAGMENT_ID_GRID_SIZE_9:
			bundle.putInt(LeaderboardFragment.ARG_GRID_SIZE, 9);
			break;
		default:
			bundle.putInt(LeaderboardFragment.ARG_GRID_SIZE, 4);
			break;
		}
		bundle.putInt(LeaderboardFragment.ARG_FILTER,
				mLeaderboardFragmentActivity.getLeaderboardFilter().ordinal());

		// Create fragment and pass the bundle
		android.support.v4.app.Fragment fragment = new LeaderboardFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public int getCount() {
		return fragment_ids.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case FRAGMENT_ID_GRID_SIZE_4:
			return "4";
		case FRAGMENT_ID_GRID_SIZE_5:
			return "5";
		case FRAGMENT_ID_GRID_SIZE_6:
			return "6";
		case FRAGMENT_ID_GRID_SIZE_7:
			return "7";
		case FRAGMENT_ID_GRID_SIZE_8:
			return "8";
		case FRAGMENT_ID_GRID_SIZE_9:
			return "9";
		default:
			return null;
		}
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
		return (LeaderboardFragment) (fragmentManager.findFragmentByTag(name));
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