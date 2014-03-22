package net.mathdoku.plus.enums;

import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GridTypeFilterTest {
	GridTypeFilter[] gridTypeFilters = {GridTypeFilter.ALL, GridTypeFilter.GRID_2X2, GridTypeFilter.GRID_3X3, GridTypeFilter.GRID_4X4, GridTypeFilter.GRID_5X5, GridTypeFilter.GRID_6X6, GridTypeFilter.GRID_7X7, GridTypeFilter.GRID_8X8, GridTypeFilter.GRID_9X9};

	@Test
	public void CheckIfAllGridSizeFiltersButNoMoreExists() throws Exception {
		assertThatGridSizeFilterExists(GridTypeFilter.ALL);
		assertThatGridSizeFilterExists(GridTypeFilter.GRID_2X2);
		assertThatGridSizeFilterExists(GridTypeFilter.GRID_3X3);
		assertThatGridSizeFilterExists(GridTypeFilter.GRID_4X4);
		assertThatGridSizeFilterExists(GridTypeFilter.GRID_5X5);
		assertThatGridSizeFilterExists(GridTypeFilter.GRID_6X6);
		assertThatGridSizeFilterExists(GridTypeFilter.GRID_7X7);
		assertThatGridSizeFilterExists(GridTypeFilter.GRID_8X8);
		assertThatGridSizeFilterExists(GridTypeFilter.GRID_9X9);
		assertThat("Expected number of grid size filters", GridTypeFilter.values().length, is(9));

	}

	private void assertThatGridSizeFilterExists(GridTypeFilter gridTypeFilter) {
		assertThat(Arrays.asList(GridTypeFilter.values()).contains(gridTypeFilter), is(true));
	}

	@Test
	public void fromGridSize_AllGridSizeFilters_CorrectlyMappedToGridSize() throws Exception {
		assertThat(GridTypeFilter.fromGridSize(GridType.GRID_2X2), is(GridTypeFilter.GRID_2X2));
		assertThat(GridTypeFilter.fromGridSize(GridType.GRID_3X3), is(GridTypeFilter.GRID_3X3));
		assertThat(GridTypeFilter.fromGridSize(GridType.GRID_4x4), is(GridTypeFilter.GRID_4X4));
		assertThat(GridTypeFilter.fromGridSize(GridType.GRID_5X5), is(GridTypeFilter.GRID_5X5));
		assertThat(GridTypeFilter.fromGridSize(GridType.GRID_6X6), is(GridTypeFilter.GRID_6X6));
		assertThat(GridTypeFilter.fromGridSize(GridType.GRID_7X7), is(GridTypeFilter.GRID_7X7));
		assertThat(GridTypeFilter.fromGridSize(GridType.GRID_8X8), is(GridTypeFilter.GRID_8X8));
		assertThat(GridTypeFilter.fromGridSize(GridType.GRID_9X9), is(GridTypeFilter.GRID_9X9));
	}

	@Test
	public void getGridSize_AllGridSizeFilters_CorrectlyMappedToGridSize() throws Exception {
		assertThat(GridTypeFilter.GRID_2X2.getGridType(), is(2));
		assertThat(GridTypeFilter.GRID_3X3.getGridType(), is(3));
		assertThat(GridTypeFilter.GRID_4X4.getGridType(), is(4));
		assertThat(GridTypeFilter.GRID_5X5.getGridType(), is(5));
		assertThat(GridTypeFilter.GRID_6X6.getGridType(), is(6));
		assertThat(GridTypeFilter.GRID_7X7.getGridType(), is(7));
		assertThat(GridTypeFilter.GRID_8X8.getGridType(), is(8));
		assertThat(GridTypeFilter.GRID_9X9.getGridType(), is(9));
	}

	@Test(expected = IllegalStateException.class)
	public void getGridSize_GridSizeFilterAll_ThrowsError() throws Exception {
		int gridSize = GridTypeFilter.ALL.getGridType();
	}
}
