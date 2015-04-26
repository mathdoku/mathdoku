package net.mathdoku.plus.gridsolving;

import junit.framework.Assert;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.grid.Grid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;
import testhelper.gridcreator.GridCreator2x2NoUniqueSolution;
import testhelper.gridcreator.GridCreator4x4;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class GridSolverTest {

    @Before
    public void setUp() throws Exception {
        TestRunnerHelper.setup(this.getClass()
                                       .getCanonicalName());
        Preferences.getInstance(TestRunnerHelper.getActivity());
    }

    @After
    public void tearDown() throws Exception {
        TestRunnerHelper.tearDown();
    }

    @Test
    public void getSolutionGrid_GridWithAUniqueSolution_SolutionIsFound() throws Exception {
        Grid grid = GridCreator4x4.createEmptyGrid();
        GridSolver gridSolver = new GridSolver(4, grid.getCages());
        assertThatGridSolutionEquals(gridSolver.getSolutionGrid(), grid);
    }

    private void assertThatGridSolutionEquals(int[][] solutionGrid, Grid grid) {
        if (solutionGrid == null || grid == null) {
            Assert.fail("grid should not be null");
        }
        // Assert size of solution grid
        assertThat(solutionGrid.length, is(grid.getGridSize()));
        for (int row = 0; row < grid.getGridSize(); row++) {
            assertThat(solutionGrid[row].length, is(grid.getGridSize()));
        }
        for (int row = 0; row < grid.getGridSize(); row++) {
            for (int column = 0; column < grid.getGridSize(); column++) {
                assertThat(solutionGrid[row][column], is(grid.getCellAt(row, column).getCorrectValue()));
            }
        }
    }

    @Test(expected = InvalidGridException.class)
    public void getSolutionGrid_GridWithoutUniqueSolution_InvalidGridExceptionIsThrown() throws Exception {
        Grid grid = GridCreator2x2NoUniqueSolution.createEmptyGrid();
        GridSolver gridSolver = new GridSolver(2, grid.getCages());
        assertThat(gridSolver.getSolutionGrid(), is(nullValue()));
    }
}