package net.mathdoku.plus.archive.ui;

import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.selector.AvailableGridTypeFilterSelector;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class ArchiveFragmentGridSizeFilterSpinner {
    private final ArchiveFragmentActivity archiveFragmentActivity;
    private final ArchiveFragmentStatePagerAdapter archiveFragmentStatePagerAdapter;
    private final List<GridTypeFilter> gridTypeFilters;

    public static class ObjectsCreator {
        public GridDatabaseAdapter createGridDatabaseAdapter() {
            return new GridDatabaseAdapter();
        }
    }

    private ArchiveFragmentGridSizeFilterSpinner.ObjectsCreator objectsCreator;

    public ArchiveFragmentGridSizeFilterSpinner(ArchiveFragmentActivity archiveFragmentActivity) {
        objectsCreator = new ObjectsCreator();
        this.archiveFragmentActivity = archiveFragmentActivity;
        archiveFragmentStatePagerAdapter = archiveFragmentActivity.getArchiveFragmentStatePagerAdapter();
        gridTypeFilters = getGridTypeFilters();
    }

    private List<GridTypeFilter> getGridTypeFilters() {
        return createAvailableSizeFilterSelector(
                archiveFragmentStatePagerAdapter.getStatusFilter()).getAvailableGridTypeFilters();
    }

    /*
     * Package private method which allows the creation of a new
     * AvailableGridTypeFilterSelector to be overwritten by the test class for
     * this package.
     */
    AvailableGridTypeFilterSelector createAvailableSizeFilterSelector(StatusFilter statusFilter) {
        return new AvailableGridTypeFilterSelector(statusFilter);
    }

    public ArchiveFragmentGridSizeFilterSpinner setObjectsCreator(ArchiveFragmentGridSizeFilterSpinner.ObjectsCreator
                                                                          objectsCreator) {
        if (this.objectsCreator == null) {
            throw new InvalidParameterException("Parameter objectsCreator cannot be null.");
        }
        this.objectsCreator = objectsCreator;

        return this;
    }

    /*
     * Package private method which allows the creation of a new
     * GridDatabaseAdapter to be overwritten by the test class for this package.
     */
    GridDatabaseAdapter createGridDatabaseAdapter() {
        return new GridDatabaseAdapter();
    }

    public List<String> getSpinnerElements() {
        List<String> spinnerElements = new ArrayList<String>();
        for (GridTypeFilter gridTypeFilter : gridTypeFilters) {
            spinnerElements.add(getSpinnerElementDescription(gridTypeFilter));
        }
        return spinnerElements;
    }

    private String getSpinnerElementDescription(GridTypeFilter gridTypeFilter) {
        if (gridTypeFilter == GridTypeFilter.ALL) {
            return archiveFragmentActivity.getResources()
                    .getString(R.string.all);
        } else {
            return archiveFragmentActivity.getResources()
                    .getStringArray(R.array.archive_size_filter)[gridTypeFilter.ordinal()];
        }
    }

    public int indexOfSelectedGridSizeFilter() {
        GridTypeFilter selectedSizeFilter = archiveFragmentStatePagerAdapter.getSelectedSizeFilter();
        return gridTypeFilters.indexOf(selectedSizeFilter);
    }

    public int size() {
        return gridTypeFilters.size();
    }

    public GridTypeFilter get(int i) {
        return gridTypeFilters.get(i);
    }
}
