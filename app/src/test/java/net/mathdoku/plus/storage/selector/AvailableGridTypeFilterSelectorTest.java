package net.mathdoku.plus.storage.selector;

import android.app.Activity;

import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import testHelper.GridCreator4x4;
import testHelper.GridCreator4x4HiddenOperators;
import testHelper.GridCreator5x5;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class AvailableGridTypeFilterSelectorTest {
	private static List<GridTypeFilter> gridTypeFilterListAllStatuses;
	private static List<GridTypeFilter> gridTypeFilterListWithStatusSolved;
	private static List<GridTypeFilter> gridTypeFilterListWithStatusUnfinished;

	@Before
	public void setup() {
		Activity activity = new Activity();
		new Util(activity);
		DatabaseHelper.getInstance(activity);

		gridTypeFilterListAllStatuses = createAndInitializeNewGridTypeFilterList();
		gridTypeFilterListWithStatusSolved = createAndInitializeNewGridTypeFilterList();
		gridTypeFilterListWithStatusUnfinished = createAndInitializeNewGridTypeFilterList();
		createAndSaveGrid(GridCreator4x4
				.create()
				.setCorrectEnteredValueToAllCells()
				.getGrid());
		createAndSaveGrid(GridCreator5x5
				.create()
				.setCorrectEnteredValueToAllCells()
				.getGrid());
		createAndSaveGrid(GridCreator4x4HiddenOperators
				.create()
				.setEmptyGrid()
				.getGrid());
	}

	private List<GridTypeFilter> createAndInitializeNewGridTypeFilterList() {
		List<GridTypeFilter> gridTypeFilterList = new ArrayList<GridTypeFilter>();

		gridTypeFilterList.add(GridTypeFilter.ALL);

		return gridTypeFilterList;
	}

	@After
	public void tearDown() {
		// Close the database helper. This ensure that the next test will use a
		// new DatabaseHelper instance with a new SQLite database connection.
		DatabaseHelper.getInstance().close();
	}

	private void createAndSaveGrid(Grid grid) {
		assertThat(grid.save(), is(true));

		GridTypeFilter gridTypeFilter = grid
				.getGridGeneratingParameters()
				.getGridType()
				.getAttachedToGridTypeFilter();
		if (!gridTypeFilterListAllStatuses.contains(gridTypeFilter)) {
			gridTypeFilterListAllStatuses.add(gridTypeFilter);
		}

		StatusFilter statusFilter = SolvingAttemptStatus
				.getDerivedStatus(grid.isSolutionRevealed(), grid.isActive(),
						grid.isEmpty())
				.getAttachedToStatusFilter();
		if (statusFilter == StatusFilter.SOLVED
				&& !gridTypeFilterListWithStatusSolved.contains(gridTypeFilter)) {
			gridTypeFilterListWithStatusSolved.add(gridTypeFilter);
		}
		if (statusFilter == StatusFilter.UNFINISHED
				&& !gridTypeFilterListWithStatusUnfinished
						.contains(gridTypeFilter)) {
			gridTypeFilterListWithStatusUnfinished.add(gridTypeFilter);
		}
	}

	@Test
	public void getAvailableGridTypeFilters_GridAllStatuses() throws Exception {
		assertThatGridTypeFilterList(StatusFilter.ALL,
				gridTypeFilterListAllStatuses);
	}

	private void assertThatGridTypeFilterList(StatusFilter statusFilter,
			List<GridTypeFilter> expectedGridTypeFilterList) {
		List<GridTypeFilter> resultGridTypeFilters = new AvailableGridTypeFilterSelector(
				statusFilter).getAvailableGridTypeFilters();
		Collections.sort(resultGridTypeFilters);
		Collections.sort(expectedGridTypeFilterList);
		assertThat(resultGridTypeFilters, is(expectedGridTypeFilterList));
	}

	@Test
	public void getAvailableGridTypeFilters_AllSolvedGrids() throws Exception {
		assertThatGridTypeFilterList(StatusFilter.SOLVED,
				gridTypeFilterListWithStatusSolved);
	}

	@Test
	public void getAvailableGridTypeFilters_AllUnfinishedGrids()
			throws Exception {
		assertThatGridTypeFilterList(StatusFilter.UNFINISHED,
				gridTypeFilterListWithStatusUnfinished);
	}
}
