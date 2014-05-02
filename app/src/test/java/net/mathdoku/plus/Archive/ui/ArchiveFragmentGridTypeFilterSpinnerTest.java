package net.mathdoku.plus.archive.ui;

import android.app.Activity;
import android.content.res.Resources;

import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.emory.mathcs.backport.java.util.Arrays;
import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ArchiveFragmentGridTypeFilterSpinnerTest {
	private ArchiveFragmentGridSizeFilterSpinner spinner;
	private ArchiveFragmentActivity archiveFragmentActivity;
	private ArchiveFragmentStatePagerAdapter archiveFragmentStatePagerAdapter;
	private GridDatabaseAdapter gridDatabaseAdapter;
	private Resources resources;
	private final String FILTER_ALL = "**ALL**";
	private final String FILTER_2 = "2x2";
	private final String FILTER_4 = "4 by 4";
	private final String FILTER_5 = "5";

	@Before
	public void setup() {
		// Instantiate singleton classes
		Activity activity = new Activity();
		DatabaseHelper.getInstance(activity);

		archiveFragmentActivity = mock(ArchiveFragmentActivity.class);
		archiveFragmentStatePagerAdapter = mock(ArchiveFragmentStatePagerAdapter.class);
		gridDatabaseAdapter = mock(GridDatabaseAdapter.class);
		when(archiveFragmentActivity.getArchiveFragmentStatePagerAdapter())
				.thenReturn(archiveFragmentStatePagerAdapter);
		when(archiveFragmentStatePagerAdapter.getStatusFilter()).thenReturn(
				GridDatabaseAdapter.StatusFilter.ALL);

		resources = mock(Resources.class);
		when(archiveFragmentActivity.getResources()).thenReturn(resources);
		when(resources.getString(R.string.all)).thenReturn(FILTER_ALL);
		when(resources.getString(R.string.grid_description_short, 2, 2))
				.thenReturn(FILTER_2);
		when(resources.getString(R.string.grid_description_short, 4, 4))
				.thenReturn(FILTER_4);
		when(resources.getString(R.string.grid_description_short, 5, 5))
				.thenReturn(FILTER_5);
	}

	@Test
	public void getSpinnerElements_NoGridsInDatabase_GridSizeFilterAllReturnedOnly()
			throws Exception {
		spinner = setSpinnerWithGridSizes();

		String[] gridSizeFilters = { FILTER_ALL };
		assertThat(spinner.getSpinnerElements(),
				is(Arrays.asList(gridSizeFilters)));
	}

	private ArchiveFragmentGridSizeFilterSpinner setSpinnerWithGridSizes(
			GridType... gridTypes) {
		when(
				gridDatabaseAdapter
						.getUsedSizes(any(GridDatabaseAdapter.StatusFilter.class)))
				.thenReturn(gridTypes);
		return new ArchiveFragmentGridSizeFilterSpinner(archiveFragmentActivity) {
			@Override
			GridDatabaseAdapter createGridDatabaseAdapter() {
				return gridDatabaseAdapter;
			}
		};
	}

	@Test
	public void getSpinnerElements_GridsWithMultipleSizesInDatabase_GridSizeFiltersInclusiveAllReturned()
			throws Exception {
		spinner = setSpinnerWithGridSizes(GridType.GRID_2X2, GridType.GRID_4x4,
				GridType.GRID_5X5);

		String[] gridSizeFilters = { FILTER_ALL, FILTER_2, FILTER_4, FILTER_5 };
		assertThat(spinner.getSpinnerElements(),
				is(Arrays.asList(gridSizeFilters)));
	}

	@Test
	public void indexOfSelectedGridSizeFilter_GridSizeFilterAllSelected_AtIndex0()
			throws Exception {
		spinner = setSpinnerWithGridSizes(GridType.GRID_2X2, GridType.GRID_4x4,
				GridType.GRID_5X5);
		when(archiveFragmentStatePagerAdapter.getSelectedSizeFilter())
				.thenReturn(GridTypeFilter.ALL);

		assertThat(spinner.indexOfSelectedGridSizeFilter(), is(0));
	}

	@Test
	public void indexOfSelectedGridSizeFilter_GridSizeFilterSelected_AtIndex()
			throws Exception {
		GridType[] gridTypes = { GridType.GRID_2X2, GridType.GRID_4x4,
				GridType.GRID_5X5 };
		spinner = setSpinnerWithGridSizes(gridTypes);
		int indexLastGridSize = gridTypes.length - 1;
		GridType lastGridType = gridTypes[indexLastGridSize];
		GridTypeFilter lastGridTypeFilter = GridTypeFilter
				.fromGridSize(lastGridType);
		when(archiveFragmentStatePagerAdapter.getSelectedSizeFilter())
				.thenReturn(lastGridTypeFilter);
		int offsetSetForGridSizeFilterAllAtIndex0 = 1;

		assertThat(spinner.indexOfSelectedGridSizeFilter(),
				is(offsetSetForGridSizeFilterAllAtIndex0 + indexLastGridSize));
	}

	@Test
	public void testSize_GridsWithMultipleSizesInDatabase_SizeIncludesGridSizeFilterAll()
			throws Exception {
		GridType[] gridTypes = { GridType.GRID_2X2, GridType.GRID_4x4,
				GridType.GRID_5X5 };
		spinner = setSpinnerWithGridSizes(gridTypes);
		int countForGridSizeFilterAll = 1;

		assertThat(spinner.size(), is(countForGridSizeFilterAll
				+ gridTypes.length));
	}

	@Test
	public void get_GridsWithMultipleSizesInDatabase() throws Exception {
		GridType[] gridTypes = { GridType.GRID_2X2, GridType.GRID_4x4,
				GridType.GRID_5X5 };
		spinner = setSpinnerWithGridSizes(gridTypes);

		assertThat(spinner.get(0), is(GridTypeFilter.ALL));
		assertThat(spinner.get(1), is(GridTypeFilter.GRID_2X2));
		assertThat(spinner.get(2), is(GridTypeFilter.GRID_4X4));
		assertThat(spinner.get(3), is(GridTypeFilter.GRID_5X5));
	}
}
