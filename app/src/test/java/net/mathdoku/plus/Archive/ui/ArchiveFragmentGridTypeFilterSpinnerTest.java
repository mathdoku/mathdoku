package net.mathdoku.plus.archive.ui;

import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.selector.AvailableGridTypeFilterSelector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Arrays;
import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ArchiveFragmentGridTypeFilterSpinnerTest {
	private ArchiveFragmentGridSizeFilterSpinner spinner;
	private ArchiveFragmentActivity archiveFragmentActivity;
	private ArchiveFragmentStatePagerAdapter archiveFragmentStatePagerAdapter;
	private GridDatabaseAdapter gridDatabaseAdapter;
	private static final String EXPECTED_DESCRIPTION_SIZE_FILTER_ALL = "All";
	private static final String EXPECTED_DESCRIPTION_SIZE_FILTER_2 = "2x2";
	private static final String EXPECTED_DESCRIPTION_SIZE_FILTER_4 = "4x4";
	private static final String EXPECTED_DESCRIPTION_SIZE_FILTER_5 = "5x5";

	@Before
	public void setup() {
		TestRunnerHelper.setup(this.getClass().getCanonicalName());

		archiveFragmentActivity = mock(ArchiveFragmentActivity.class);
		archiveFragmentStatePagerAdapter = mock(ArchiveFragmentStatePagerAdapter.class);
		gridDatabaseAdapter = mock(GridDatabaseAdapter.class);
		when(archiveFragmentActivity.getArchiveFragmentStatePagerAdapter())
				.thenReturn(archiveFragmentStatePagerAdapter);
		when(archiveFragmentStatePagerAdapter.getStatusFilter()).thenReturn(
				StatusFilter.ALL);

		when(archiveFragmentActivity.getResources()).thenReturn(
				Robolectric.application.getResources());
	}

	@After
	public void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	@Test
	public void getSpinnerElements_NoGridsInDatabase_GridSizeFilterAllReturnedOnly()
			throws Exception {
		spinner = setSpinnerWithGridSizes();

		String[] gridSizeFilters = { EXPECTED_DESCRIPTION_SIZE_FILTER_ALL };
		assertThat(spinner.getSpinnerElements(),
				is(Arrays.asList(gridSizeFilters)));
	}

	private ArchiveFragmentGridSizeFilterSpinner setSpinnerWithGridSizes(
			final GridTypeFilter... gridTypeFilters) {
		return new ArchiveFragmentGridSizeFilterSpinner(archiveFragmentActivity) {
			private AvailableGridTypeFilterSelector availableGridTypeFilterSelector;

			@Override
			GridDatabaseAdapter createGridDatabaseAdapter() {
				return gridDatabaseAdapter;
			}

			@Override
			AvailableGridTypeFilterSelector createAvailableSizeFilterSelector(
					StatusFilter statusFilter) {
				availableGridTypeFilterSelector = mock(AvailableGridTypeFilterSelector.class);
				when(
						availableGridTypeFilterSelector
								.getAvailableGridTypeFilters()).thenReturn(
						getGridTypeFiltersIncludingAll());
				return availableGridTypeFilterSelector;
			}

			private List<GridTypeFilter> getGridTypeFiltersIncludingAll() {
				List<GridTypeFilter> gridTypeFiltersIncludingAll = new ArrayList<GridTypeFilter>();
				gridTypeFiltersIncludingAll.add(GridTypeFilter.ALL);
				gridTypeFiltersIncludingAll.addAll(Arrays
						.asList(gridTypeFilters));
				return gridTypeFiltersIncludingAll;
			}
		};
	}

	@Test
	public void getSpinnerElements_GridsWithMultipleSizesInDatabase_GridSizeFiltersInclusiveAllReturned()
			throws Exception {
		spinner = setSpinnerWithGridSizes(GridTypeFilter.GRID_2X2,
				GridTypeFilter.GRID_4X4, GridTypeFilter.GRID_5X5);

		String[] gridSizeFilters = { EXPECTED_DESCRIPTION_SIZE_FILTER_ALL,
				EXPECTED_DESCRIPTION_SIZE_FILTER_2,
				EXPECTED_DESCRIPTION_SIZE_FILTER_4,
				EXPECTED_DESCRIPTION_SIZE_FILTER_5 };
		assertThat(spinner.getSpinnerElements(),
				is(Arrays.asList(gridSizeFilters)));
	}

	@Test
	public void indexOfSelectedGridSizeFilter_GridSizeFilterAllSelected_AtIndex0()
			throws Exception {
		spinner = setSpinnerWithGridSizes(GridTypeFilter.GRID_2X2,
				GridTypeFilter.GRID_4X4, GridTypeFilter.GRID_5X5);
		when(archiveFragmentStatePagerAdapter.getSelectedSizeFilter())
				.thenReturn(GridTypeFilter.ALL);

		assertThat(spinner.indexOfSelectedGridSizeFilter(), is(0));
	}

	@Test
	public void indexOfSelectedGridSizeFilter_GridSizeFilterSelected_AtIndex()
			throws Exception {
		GridTypeFilter[] gridTypeFilters = { GridTypeFilter.GRID_2X2,
				GridTypeFilter.GRID_4X4, GridTypeFilter.GRID_5X5 };
		spinner = setSpinnerWithGridSizes(gridTypeFilters);
		int indexLastGridSizeFilter = gridTypeFilters.length - 1;
		GridTypeFilter lastGridTypeFilter = gridTypeFilters[indexLastGridSizeFilter];
		when(archiveFragmentStatePagerAdapter.getSelectedSizeFilter())
				.thenReturn(lastGridTypeFilter);
		int offsetSetForGridSizeFilterAllAtIndex0 = 1;

		assertThat(spinner.indexOfSelectedGridSizeFilter(),
				is(offsetSetForGridSizeFilterAllAtIndex0
						+ indexLastGridSizeFilter));
	}

	@Test
	public void testSize_GridsWithMultipleSizesInDatabase_SizeIncludesGridSizeFilterAll()
			throws Exception {
		GridTypeFilter[] gridTypeFilters = { GridTypeFilter.GRID_2X2,
				GridTypeFilter.GRID_4X4, GridTypeFilter.GRID_5X5 };
		spinner = setSpinnerWithGridSizes(gridTypeFilters);
		int countForGridSizeFilterAll = 1;

		assertThat(spinner.size(), is(countForGridSizeFilterAll
				+ gridTypeFilters.length));
	}

	@Test
	public void get_GridsWithMultipleSizesInDatabase() throws Exception {
		GridTypeFilter[] gridTypeFilters = { GridTypeFilter.GRID_2X2,
				GridTypeFilter.GRID_4X4, GridTypeFilter.GRID_5X5 };
		spinner = setSpinnerWithGridSizes(gridTypeFilters);

		assertThat(spinner.get(0), is(GridTypeFilter.ALL));
		assertThat(spinner.get(1), is(GridTypeFilter.GRID_2X2));
		assertThat(spinner.get(2), is(GridTypeFilter.GRID_4X4));
		assertThat(spinner.get(3), is(GridTypeFilter.GRID_5X5));
	}
}
