package net.mathdoku.plus.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of statistics pages.
 */
class StatisticsFragmentPagerAdapter extends FragmentPagerAdapter {

	private static final int FRAGMENT_ID_GRID_SIZE_STATS_4 = 0;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_5 = 1;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_6 = 2;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_7 = 3;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_8 = 4;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_9 = 5;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_ALL = 6;
	private static final int[] fragment_ids = new int[] {
			FRAGMENT_ID_GRID_SIZE_STATS_ALL, FRAGMENT_ID_GRID_SIZE_STATS_4,
			FRAGMENT_ID_GRID_SIZE_STATS_5, FRAGMENT_ID_GRID_SIZE_STATS_6,
			FRAGMENT_ID_GRID_SIZE_STATS_7, FRAGMENT_ID_GRID_SIZE_STATS_8,
			FRAGMENT_ID_GRID_SIZE_STATS_9 };

	public StatisticsFragmentPagerAdapter(
			android.support.v4.app.FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		Bundle bundle = new Bundle();
		switch (i) {
		case FRAGMENT_ID_GRID_SIZE_STATS_4:
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, 4);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, 4);
			break;
		case FRAGMENT_ID_GRID_SIZE_STATS_5:
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, 5);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, 5);
			break;
		case FRAGMENT_ID_GRID_SIZE_STATS_6:
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, 6);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, 6);
			break;
		case FRAGMENT_ID_GRID_SIZE_STATS_7:
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, 7);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, 7);
			break;
		case FRAGMENT_ID_GRID_SIZE_STATS_8:
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, 8);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, 8);
			break;
		case FRAGMENT_ID_GRID_SIZE_STATS_9:
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, 9);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, 9);
			break;
		case FRAGMENT_ID_GRID_SIZE_STATS_ALL:
			// fall through
		default:
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, 4);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, 9);
			break;
		}

		// Create fragment and pass the bundle
		android.support.v4.app.Fragment fragment = new StatisticsLevelFragment();
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
		case FRAGMENT_ID_GRID_SIZE_STATS_4:
			return "4";
		case FRAGMENT_ID_GRID_SIZE_STATS_5:
			return "5";
		case FRAGMENT_ID_GRID_SIZE_STATS_6:
			return "6";
		case FRAGMENT_ID_GRID_SIZE_STATS_7:
			return "7";
		case FRAGMENT_ID_GRID_SIZE_STATS_8:
			return "8";
		case FRAGMENT_ID_GRID_SIZE_STATS_9:
			return "9";
		case FRAGMENT_ID_GRID_SIZE_STATS_ALL:
			// Fall through
		default:
			return "4 - 9";
		}
	}
}