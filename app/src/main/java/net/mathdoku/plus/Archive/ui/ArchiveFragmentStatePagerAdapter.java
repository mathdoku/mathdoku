package net.mathdoku.plus.archive.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;

import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.storage.selector.ArchiveSolvingAttemptSelector;

import java.util.List;

/**
 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment representing an object in the
 * collection.
 */
class ArchiveFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    // Replace Config.disabledAlways() on following line with Config.enabledInDevelopmentModeOnly()
    // to show debug information when running in development mode.
    private static final boolean DEBUG = Config.disabledAlways();

    private static final int UNKNOWN_GRID_ID = -1;
    private static final int INVALID_POSITION_ID = -2;

    private final ArchiveFragmentActivity mArchiveFragmentActivity;

    // The list of latest solving attempts which will be shown in the archive.
    // In case multiple solving attempts exists for a grid, only the latest
    // solving attempt is displayed.
    private List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid> solvingAttemptsInArchive;

    // Selected filters
    private StatusFilter mStatusFilter;
    private GridTypeFilter mGridTypeFilter;

    // Label used in the pager strip
    private static String mLabelPuzzleNumber;

    public ArchiveFragmentStatePagerAdapter(android.support.v4.app.FragmentManager fragmentManager,
                                            ArchiveFragmentActivity archiveFragmentActivity) {
        super(fragmentManager);

        // Initialize the filters
        mStatusFilter = StatusFilter.ALL;
        mGridTypeFilter = GridTypeFilter.ALL;

        // Initialize the label used in the page titles.
        mArchiveFragmentActivity = archiveFragmentActivity;
        mLabelPuzzleNumber = mArchiveFragmentActivity.getResources()
                .getString(R.string.archive_puzzle_number);

        // Determine id's of grids/solving attempts which are available for
        // display.
        updateSolvingAttemptsInArchive();
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        android.support.v4.app.Fragment fragment = new ArchiveFragment();
        Bundle args = new Bundle();
        args.putInt(ArchiveFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID, getSolvingAttemptId(position));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemPosition(Object item) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return solvingAttemptsInArchive.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (isValidPositionInListOfLatestSolvingAttempts(position)) {
            int relativePosition = position - mArchiveFragmentActivity.getViewPagerCurrentPosition();

            StringBuilder stringBuilder = new StringBuilder();
            if (relativePosition < 0) {
                stringBuilder.append("<");
            } else if (relativePosition == 0) {
                stringBuilder.append(mLabelPuzzleNumber);
            }
            stringBuilder.append(" ");
            stringBuilder.append(getGridId(position));
            if (DEBUG) {
                stringBuilder.append(" (");
                stringBuilder.append(getSolvingAttemptId(position));
                stringBuilder.append(")");
            }
            if (relativePosition > 0) {
                stringBuilder.append(" >");
            }
            return stringBuilder.toString();
        } else {
            return mLabelPuzzleNumber;
        }
    }

    private boolean isValidPositionInListOfLatestSolvingAttempts(int position) {
        return position >= 0 && position < solvingAttemptsInArchive.size();
    }

    /**
     * Set the status filter to the given value.
     *
     * @param statusFilter
     *         The new value of the status filter.
     */
    public void setStatusFilter(StatusFilter statusFilter) {
        mStatusFilter = statusFilter;
        updateSolvingAttemptsInArchive();
    }

    /**
     * Get the currently selected value of the status filter.
     *
     * @return The currently selected value of the status filter.
     */
    public StatusFilter getStatusFilter() {
        return mStatusFilter;
    }

    /**
     * Set the size filter to the given value.
     *
     * @param sizeFilter
     *         The new value of the size filter.
     */
    public void setSizeFilter(GridTypeFilter sizeFilter) {
        mGridTypeFilter = sizeFilter;
        updateSolvingAttemptsInArchive();
    }

    /**
     * Get the currently selected value of the size filter.
     *
     * @return The currently selected value of the size filter.
     */
    public GridTypeFilter getSelectedSizeFilter() {
        return mGridTypeFilter;
    }

    /**
     * Set all grid id's which can be displayed using the adapter.
     */
    private void updateSolvingAttemptsInArchive() {
        solvingAttemptsInArchive = new ArchiveSolvingAttemptSelector(mStatusFilter,
                                                                     mGridTypeFilter)
                .getLatestSolvingAttemptIdPerGrid();
        notifyDataSetChanged();
    }

    /**
     * Get the position in the adapter at which the given grid is placed.
     *
     * @param gridId
     *         The grid id to be found.
     * @return The position in the adapter at which the given grid is placed. {@value #UNKNOWN_GRID_ID} in case the grid
     * id is not known to this adapter.
     */
    public int getPositionOfGridId(int gridId) {
        for (ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid latestSolvingAttemptForGrid :
                solvingAttemptsInArchive) {
            if (latestSolvingAttemptForGrid.getGridId() == gridId) {
                return solvingAttemptsInArchive.indexOf(latestSolvingAttemptForGrid);
            }
        }

        return UNKNOWN_GRID_ID;
    }

    /**
     * Get the grid id at the given position.
     *
     * @param position
     *         Position in adapter for which the grid id has to be returned.
     * @return The grid id at the given position.
     */
    public int getGridId(int position) {
        if (isValidPositionInListOfLatestSolvingAttempts(position)) {
            return solvingAttemptsInArchive.get(position)
                    .getGridId();
        } else {
            return INVALID_POSITION_ID;
        }
    }

    /**
     * Get the solving attempt for the grid at the given position.
     *
     * @param position
     *         Position in adapter for which the solving attempt of the grid has to be returned.
     * @return The grid id at the given position.
     */
    public int getSolvingAttemptId(int position) {
        if (isValidPositionInListOfLatestSolvingAttempts(position)) {
            return solvingAttemptsInArchive.get(position)
                    .getSolvingAttemptId();
        } else {
            return INVALID_POSITION_ID;
        }
    }
}
