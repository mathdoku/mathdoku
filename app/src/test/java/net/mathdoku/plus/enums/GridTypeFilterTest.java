package net.mathdoku.plus.enums;

import java.util.Arrays;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GridTypeFilterTest {
	GridTypeFilter[] gridTypeFilters = { GridTypeFilter.ALL,
			GridTypeFilter.GRID_2X2, GridTypeFilter.GRID_3X3,
			GridTypeFilter.GRID_4X4, GridTypeFilter.GRID_5X5,
			GridTypeFilter.GRID_6X6, GridTypeFilter.GRID_7X7,
			GridTypeFilter.GRID_8X8, GridTypeFilter.GRID_9X9 };

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
		assertThat("Expected number of grid size filters",
				GridTypeFilter.values().length, is(9));

	}

	private void assertThatGridSizeFilterExists(GridTypeFilter gridTypeFilter) {
		assertThat(
				Arrays.asList(GridTypeFilter.values()).contains(gridTypeFilter),
				is(true));
	}
}
