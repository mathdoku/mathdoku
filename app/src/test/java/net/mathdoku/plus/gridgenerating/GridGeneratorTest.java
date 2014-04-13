package net.mathdoku.plus.gridgenerating;

import android.app.Activity;

import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import testHelper.GridCreator;
import testHelper.GridCreator2x2;
import testHelper.GridCreator4x4HiddenOperators;
import testHelper.GridCreator9x9;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * NOTE: the first test which is executed will take significant longer than the
 * other tests as the database structure will be created during this test.
 */

@RunWith(RobolectricGradleTestRunner.class)
public class GridGeneratorTest {
	GridGeneratorListener gridGeneratorListener;

	private static class GridGeneratorListener implements
			GridGenerator.Listener {
		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public void updateProgressHighLevel(String text) {
			// Swallow
		}

		@Override
		public void updateProgressDetailLevel(String text) {
			// Swallow
		}

		@Override
		public void signalSlowGridGeneration() {
			// Swallow
		}
	}

	@Before
	public void setup() {
		Activity activity = new Activity();
		new Util(activity);
		DatabaseHelper.getInstance(activity);

		gridGeneratorListener = new GridGeneratorListener();
	}

	@After
	public void tearDown() {
		// Close the database helper. This ensure that the next test will use a
		// new DatabaseHelper instance with a new SQLite database connection. In
		// this way it is possible to generate the same grid multiple times in
		// one test cyclus. Without this tear down, a grid can only be generated
		// once because the grid generator checks whether the generated grid
		// definition is unique.
		DatabaseHelper.getInstance().close();
	}

	@Test
	public void createGrid_Grid2x2_RegeneratedGridHasSameGridDefinition()
			throws Exception {
		assertRegenerateGrid(GridCreator2x2.createEmpty());
	}

	@Test
	public void createGrid_Grid4x4HiddenOperators_RegeneratedGridHasSameGridDefinition()
			throws Exception {
		assertRegenerateGrid(GridCreator4x4HiddenOperators.createEmpty());
	}

	@Test
	public void createGrid_Grid9x9_RegeneratedGridHasSameGridDefinition()
			throws Exception {
		assertRegenerateGrid(GridCreator9x9.createEmpty());
	}

	private void assertRegenerateGrid(GridCreator gridCreator) {
		assertThat(
				"This GridCreator subclass should not be used to test whether the grid can be regenerated.",
				gridCreator.canBeRegenerated(), is(true));

		Grid generatedGrid = new GridGenerator(gridGeneratorListener)
				.createGrid(gridCreator.getGridGeneratingParameters());
		assertThat(generatedGrid.getGridGeneratingParameters(),
				is(gridCreator.getGridGeneratingParameters()));
		assertThat(generatedGrid.getDefinition(), is(gridCreator
				.getGrid()
				.getDefinition()));
	}
}
