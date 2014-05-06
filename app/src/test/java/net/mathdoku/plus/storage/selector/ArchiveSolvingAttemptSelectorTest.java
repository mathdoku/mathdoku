package net.mathdoku.plus.storage.selector;

import android.app.Activity;

import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.util.Util;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import testHelper.GridCreator4x4;
import testHelper.GridCreator4x4HiddenOperators;
import testHelper.GridCreator5x5;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class ArchiveSolvingAttemptSelectorTest {
	private ArchiveSolvingAttemptSelector archiveSolvingAttemptSelector;
	private static List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid> latestSolvingAttemptForAllGrids;
	private static List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid> latestSolvingAttemptForAllSolvedGrids;
	private static List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid> latestSolvingAttemptForAllGridsWithSize4;
	private static List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid> latestSolvingAttemptForAllSolvedGridsWithSize4;

	@Before
	public void setup() {
		Activity activity = new Activity();
		new Util(activity);
		DatabaseHelper.getInstance(activity);

		latestSolvingAttemptForAllGrids = new ArrayList<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid>();
		latestSolvingAttemptForAllSolvedGrids = new ArrayList<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid>();
		latestSolvingAttemptForAllGridsWithSize4 = new ArrayList<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid>();
		latestSolvingAttemptForAllSolvedGridsWithSize4 = new ArrayList<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid>();
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

	@After
	public void tearDown() {
		// Close the database helper. This ensure that the next test will use a
		// new DatabaseHelper instance with a new SQLite database connection.
		DatabaseHelper.getInstance().close();
	}

	private Grid createAndSaveGrid(Grid grid) {
		assertThat(grid.save(), is(true));

		ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid latestSolvingAttemptForGrid;
		latestSolvingAttemptForGrid = new ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid(
				grid.getRowId(), grid.getSolvingAttemptId(), -1,
				grid.getGridSize());

		latestSolvingAttemptForAllGrids.add(latestSolvingAttemptForGrid);
		if (grid.isSolved()) {
			latestSolvingAttemptForAllSolvedGrids
					.add(latestSolvingAttemptForGrid);
		}
		if (grid.getGridSize() == 4) {
			latestSolvingAttemptForAllGridsWithSize4
					.add(latestSolvingAttemptForGrid);
		}
		if (grid.isSolved() && grid.getGridSize() == 4) {
			latestSolvingAttemptForAllSolvedGridsWithSize4
					.add(latestSolvingAttemptForGrid);
		}

		return grid;
	}

	@Test
	public void getLatestSolvingAttemptIdPerGrid_AllGrids() throws Exception {
		assertThatLatestSolvingAttemptIdPerGrid(StatusFilter.ALL,
				GridTypeFilter.ALL, is(latestSolvingAttemptForAllGrids));
	}

	private void assertThatLatestSolvingAttemptIdPerGrid(
			StatusFilter statusFilter,
			GridTypeFilter gridTypeFilter,
			Matcher<List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid>> expectedLatestSolvingAttemptForGrids) {
		List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid> resultLatestSolvingAttemptForGrids = getResultLatestSolvingAttemptForGrids(
				statusFilter, gridTypeFilter);
		assertThat(resultLatestSolvingAttemptForGrids,
				is(expectedLatestSolvingAttemptForGrids));
	}

	private List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid> getResultLatestSolvingAttemptForGrids(
			StatusFilter statusFilter, GridTypeFilter gridTypeFilter) {
		archiveSolvingAttemptSelector = new ArchiveSolvingAttemptSelector(
				statusFilter, gridTypeFilter);
		return archiveSolvingAttemptSelector.getLatestSolvingAttemptIdPerGrid();
	}

	@Test
	public void getLatestSolvingAttemptIdPerGrid_AllSolvedGrids()
			throws Exception {
		assertThatLatestSolvingAttemptIdPerGrid(StatusFilter.SOLVED,
				GridTypeFilter.ALL, is(latestSolvingAttemptForAllSolvedGrids));
	}

	@Test
	public void getLatestSolvingAttemptIdPerGrid_AllGridsWithSize4()
			throws Exception {
		assertThatLatestSolvingAttemptIdPerGrid(StatusFilter.ALL,
				GridTypeFilter.GRID_4X4,
				is(latestSolvingAttemptForAllGridsWithSize4));
	}

	@Test
	public void getLatestSolvingAttemptIdPerGrid_AllSolvedGridsWithSize4()
			throws Exception {
		assertThatLatestSolvingAttemptIdPerGrid(StatusFilter.SOLVED,
				GridTypeFilter.GRID_4X4,
				is(latestSolvingAttemptForAllSolvedGridsWithSize4));
	}

	@Test
	public void countGrids_AllGrids() throws Exception {
		List<ArchiveSolvingAttemptSelector.LatestSolvingAttemptForGrid> resultLatestSolvingAttemptForGrids = getResultLatestSolvingAttemptForGrids(
				StatusFilter.ALL, GridTypeFilter.ALL);
		assertThat(resultLatestSolvingAttemptForGrids.size(),
				is(latestSolvingAttemptForAllGrids.size()));
	}
}
