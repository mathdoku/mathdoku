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
	public final static String TAG = "MathDoku.LeaderboardFragmentPagerAdapter";

	private static final int FRAGMENT_ID_GRID_SIZE_4 = 0;
	private static final int FRAGMENT_ID_GRID_SIZE_5 = 1;
	private static final int FRAGMENT_ID_GRID_SIZE_6 = 2;
	private static final int FRAGMENT_ID_GRID_SIZE_7 = 3;
	private static final int FRAGMENT_ID_GRID_SIZE_8 = 4;
	private static final int FRAGMENT_ID_GRID_SIZE_9 = 5;
	private static int[] fragment_ids = new int[] { FRAGMENT_ID_GRID_SIZE_4,
			FRAGMENT_ID_GRID_SIZE_5, FRAGMENT_ID_GRID_SIZE_6,
			FRAGMENT_ID_GRID_SIZE_7, FRAGMENT_ID_GRID_SIZE_8,
			FRAGMENT_ID_GRID_SIZE_9 };

	// Context
	private LeaderboardFragmentActivity mLeaderboardFragmentActivity;

	public LeaderboardFragmentPagerAdapter(
			LeaderboardFragmentActivity leaderboardFragmentActivity,
			android.support.v4.app.FragmentManager fragmentManager) {
		super(fragmentManager);
		mLeaderboardFragmentActivity = leaderboardFragmentActivity;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		android.support.v4.app.Fragment fragment = null;
		fragment = new LeaderboardFragment();

		int gridSize = 0;
		switch (i) {
		case FRAGMENT_ID_GRID_SIZE_4:
			gridSize = 4;
			break;
		case FRAGMENT_ID_GRID_SIZE_5:
			gridSize = 5;
			break;
		case FRAGMENT_ID_GRID_SIZE_6:
			gridSize = 6;
			break;
		case FRAGMENT_ID_GRID_SIZE_7:
			gridSize = 7;
			break;
		case FRAGMENT_ID_GRID_SIZE_8:
			gridSize = 8;
			break;
		case FRAGMENT_ID_GRID_SIZE_9:
			gridSize = 9;
			break;
		default:
			gridSize = 4;
			break;
		}

		Bundle args = new Bundle();
		args.putInt(LeaderboardFragment.ARG_GRID_SIZE, gridSize);
		args.putInt(LeaderboardFragment.ARG_FILTER,
				mLeaderboardFragmentActivity.getLeaderboardFilter().ordinal());
		fragment.setArguments(args);

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
	 * Gets the fragment for the given position in the adapater.
	 * 
	 * @param container
	 * @param position
	 * @param fm
	 * @return
	 */
	public LeaderboardFragment getFragment(ViewPager container, int position,
			FragmentManager fm) {
		String name = makeFragmentName(container.getId(), position);
		return (LeaderboardFragment) (fm.findFragmentByTag(name));
	}

	/**
	 * Gets the fragment name as constructed by Android to identify the
	 * fragment.
	 * 
	 * @param viewId
	 * @param index
	 * @return
	 */
	private String makeFragmentName(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}
}