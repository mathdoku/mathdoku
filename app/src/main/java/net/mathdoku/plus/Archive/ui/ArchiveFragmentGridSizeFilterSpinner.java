package net.mathdoku.plus.archive.ui;

import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.storage.databaseadapter.database.GridDatabaseAdapter;

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

	public ArchiveFragmentGridSizeFilterSpinner(
			ArchiveFragmentActivity archiveFragmentActivity) {
		objectsCreator = new ObjectsCreator();
		this.archiveFragmentActivity = archiveFragmentActivity;
		archiveFragmentStatePagerAdapter = archiveFragmentActivity
				.getArchiveFragmentStatePagerAdapter();
		gridTypeFilters = getGridTypeFilters();
	}

	public ArchiveFragmentGridSizeFilterSpinner setObjectsCreator(
			ArchiveFragmentGridSizeFilterSpinner.ObjectsCreator objectsCreator) {
		if (this.objectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter objectsCreator cannot be null.");
		}
		this.objectsCreator = objectsCreator;

		return this;
	}

	private List<GridTypeFilter> getGridTypeFilters() {
		List<GridTypeFilter> gridTypeFilters = new ArrayList<GridTypeFilter>();
		gridTypeFilters.add(GridTypeFilter.ALL);
		for (GridType gridType : getUsedSizesForCurrentStatusFilter()) {
			gridTypeFilters.add(GridTypeFilter.fromGridSize(gridType));
		}

		return gridTypeFilters;
	}

	private GridType[] getUsedSizesForCurrentStatusFilter() {
		GridDatabaseAdapter gridDatabaseAdapter = createGridDatabaseAdapter();
		return gridDatabaseAdapter
				.getUsedSizes(archiveFragmentStatePagerAdapter
						.getStatusFilter());
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
			return archiveFragmentActivity.getResources().getString(
					R.string.all);
		} else {
			return archiveFragmentActivity.getResources().getString(
					R.string.grid_description_short,
					gridTypeFilter.getGridType(), gridTypeFilter.getGridType());
		}
	}

	public int indexOfSelectedGridSizeFilter() {
		GridTypeFilter selectedSizeFilter = archiveFragmentStatePagerAdapter
				.getSelectedSizeFilter();
		return gridTypeFilters.indexOf(selectedSizeFilter);
	}

	public int size() {
		return gridTypeFilters.size();
	}

	public GridTypeFilter get(int i) {
		return gridTypeFilters.get(i);
	}
}
