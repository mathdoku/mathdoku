package net.mathdoku.plus.statistics.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;

import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.GridTypeFilter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of statistics pages.
 */
class StatisticsFragmentPagerAdapter extends FragmentPagerAdapter {
	private static final GridTypeFilter[] GRID_TYPE_FILTERS_IN_PAGER_ADAPTER = {
			GridTypeFilter.ALL, GridTypeFilter.GRID_4X4,
			GridTypeFilter.GRID_5X5, GridTypeFilter.GRID_6X6,
			GridTypeFilter.GRID_7X7, GridTypeFilter.GRID_8X8,
			GridTypeFilter.GRID_9X9 };
	private static final int MIN_GRID_SIZE_IN_PAGER_ADAPTER = GridType
			.fromGridTypeFilter(GridTypeFilter.GRID_4X4)
			.getGridSize();
	private static final int MAX_GRID_SIZE_IN_PAGER_ADAPTER = GridType
			.fromGridTypeFilter(GridTypeFilter.GRID_9X9)
			.getGridSize();

	private final Context context;

	public StatisticsFragmentPagerAdapter(
			android.support.v4.app.FragmentManager fragmentManager,
			Context context) {
		super(fragmentManager);
		this.context = context;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int index) {
		Bundle bundle = new Bundle();
		if (GRID_TYPE_FILTERS_IN_PAGER_ADAPTER[index] == GridTypeFilter.ALL) {
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN,
					MIN_GRID_SIZE_IN_PAGER_ADAPTER);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX,
					MAX_GRID_SIZE_IN_PAGER_ADAPTER);
		} else {
			int gridSize = GridType.fromGridTypeFilter(
					GRID_TYPE_FILTERS_IN_PAGER_ADAPTER[index]).getGridSize();
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MIN, gridSize);
			bundle.putInt(StatisticsLevelFragment.ARG_GRID_SIZE_MAX, gridSize);
		}

		android.support.v4.app.Fragment fragment = new StatisticsLevelFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public int getCount() {
		return GRID_TYPE_FILTERS_IN_PAGER_ADAPTER.length;
	}

	@Override
	public CharSequence getPageTitle(int index) {
		if (GRID_TYPE_FILTERS_IN_PAGER_ADAPTER[index] == GridTypeFilter.ALL) {
			return context.getResources().getString(R.string.grid_range,
					MIN_GRID_SIZE_IN_PAGER_ADAPTER,
					MAX_GRID_SIZE_IN_PAGER_ADAPTER);
		} else {
			return Integer.toString(GridType.fromGridTypeFilter(
					GRID_TYPE_FILTERS_IN_PAGER_ADAPTER[index]).getGridSize());
		}
	}
}
