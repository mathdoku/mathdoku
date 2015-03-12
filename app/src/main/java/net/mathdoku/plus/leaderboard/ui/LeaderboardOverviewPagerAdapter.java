package net.mathdoku.plus.leaderboard.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.leaderboard.LeaderboardType;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the leaderboard
 * overviews.
 */
class LeaderboardOverviewPagerAdapter extends FragmentPagerAdapter {
    @SuppressWarnings("unused")
    private static final String TAG = LeaderboardOverviewPagerAdapter.class.getName();

    private static final GridType[] GRID_SIZES_WITH_LEADERBOARD = LeaderboardType
            .getGridSizeWithLeaderboard();

    // Context
    private final LeaderboardOverviewActivity mLeaderboardOverviewActivity;

    public LeaderboardOverviewPagerAdapter(LeaderboardOverviewActivity
                                                   leaderboardOverviewActivity,
                                           android.support.v4.app.FragmentManager fragmentManager) {
        super(fragmentManager);
        mLeaderboardOverviewActivity = leaderboardOverviewActivity;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt(LeaderboardOverview.ARG_GRID_SIZE,
                      GRID_SIZES_WITH_LEADERBOARD[index].getGridSize());
        bundle.putInt(LeaderboardOverview.ARG_FILTER,
                      mLeaderboardOverviewActivity.getLeaderboardFilter()
                              .ordinal());

        // Create fragment and pass the bundle
        android.support.v4.app.Fragment fragment = new LeaderboardOverview();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() {
        return GRID_SIZES_WITH_LEADERBOARD.length;
    }

    @Override
    public CharSequence getPageTitle(int index) {
        return Integer.toString(GRID_SIZES_WITH_LEADERBOARD[index].getGridSize());
    }

    /**
     * Gets the fragment for the given position in the adapter.
     *
     * @param viewPager
     *         The view pager which contains the fragment.
     * @param position
     *         The position of the fragment to be found.
     * @param fragmentManager
     *         The fragment manager from which the fragment has to be retrieved.
     * @return The fragment corresponding with the given position in the adapter of the view pager.
     */
    public LeaderboardOverview getFragment(ViewPager viewPager, int position,
                                           FragmentManager fragmentManager) {
        return (LeaderboardOverview) fragmentManager.findFragmentByTag(
                makeFragmentName(viewPager.getId(), position));
    }

    /**
     * Gets the fragment name as constructed by Android to identify the fragment.
     *
     * @param viewPagerId
     *         The id of the view pager.
     * @param index
     *         The index in the view pager.
     * @return The fragment name as constructed by Android to identify the fragment.
     */
    private String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }
}
