package net.cactii.mathdoku.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of statistics pages.
 */
public class StatisticsFragmentPagerAdapter extends FragmentPagerAdapter {

	private static final int FRAGMENT_ID_GAME_STATS = 0;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_4 = 1;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_5 = 2;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_6 = 3;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_7 = 4;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_8 = 5;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_9 = 6;
	private static final int FRAGMENT_ID_GRID_SIZE_STATS_ALL = 7;
	private static int[] fragment_ids;

	private int mGameStatisticsId;

	public StatisticsFragmentPagerAdapter(
			android.support.v4.app.FragmentManager fragmentManager,
			int gameStatisticsId) {
		super(fragmentManager);
		mGameStatisticsId = gameStatisticsId;
		if (mGameStatisticsId >= 0) {
			fragment_ids = new int[] { FRAGMENT_ID_GAME_STATS,
					FRAGMENT_ID_GRID_SIZE_STATS_ALL,
					FRAGMENT_ID_GRID_SIZE_STATS_4,
					FRAGMENT_ID_GRID_SIZE_STATS_5,
					FRAGMENT_ID_GRID_SIZE_STATS_6,
					FRAGMENT_ID_GRID_SIZE_STATS_7,
					FRAGMENT_ID_GRID_SIZE_STATS_8,
					FRAGMENT_ID_GRID_SIZE_STATS_9 };
		} else {
			fragment_ids = new int[] { FRAGMENT_ID_GRID_SIZE_STATS_ALL,
					FRAGMENT_ID_GRID_SIZE_STATS_4,
					FRAGMENT_ID_GRID_SIZE_STATS_5,
					FRAGMENT_ID_GRID_SIZE_STATS_6,
					FRAGMENT_ID_GRID_SIZE_STATS_7,
					FRAGMENT_ID_GRID_SIZE_STATS_8,
					FRAGMENT_ID_GRID_SIZE_STATS_9 };
		}
	}

	@Override
	public android.support.v4.app.Fragment getItem(int i) {
		android.support.v4.app.Fragment fragment = null;
		if (i == FRAGMENT_ID_GAME_STATS) {
			fragment = new StatisticsGameFragment();
			Bundle args = new Bundle();
			args.putInt(StatisticsGameFragment.BUNDLE_KEY_STATISTICS_ID,
					mGameStatisticsId);
			fragment.setArguments(args);
		} else {
			fragment = new StatisticsLevelFragment();

			int min = 0;
			int max = 0;
			switch (i) {
			case FRAGMENT_ID_GRID_SIZE_STATS_4:
				min = max = 4;
				break;
			case FRAGMENT_ID_GRID_SIZE_STATS_5:
				min = max = 5;
				break;
			case FRAGMENT_ID_GRID_SIZE_STATS_6:
				min = max = 6;
				break;
			case FRAGMENT_ID_GRID_SIZE_STATS_7:
				min = max = 7;
				break;
			case FRAGMENT_ID_GRID_SIZE_STATS_8:
				min = max = 8;
				break;
			case FRAGMENT_ID_GRID_SIZE_STATS_9:
				min = max = 9;
				break;
			case FRAGMENT_ID_GRID_SIZE_STATS_ALL:
				// fall through
			default:
				min = 4;
				max = 9;
				break;
			}

			Bundle args = new Bundle();
			args.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, min);
			args.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, max);
			fragment.setArguments(args);
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return fragment_ids.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case FRAGMENT_ID_GAME_STATS:
			return "G";
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