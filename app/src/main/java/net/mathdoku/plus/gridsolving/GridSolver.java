package net.mathdoku.plus.gridsolving;

import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.gridsolving.dancinglinesx.DancingLinesX;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.cage.Cage;

import java.util.List;

public class GridSolver {
    private static final String TAG = GridSolver.class.getName();

    // Replace Config.disabledAlways() on following line with Config.enabledInDevelopmentModeOnly()
    // to show debug information when running in development mode.
    public static final boolean DEBUG = Config.disabledAlways();

    private final int mGridSize;
    private final List<Cage> mCages;
    private DancingLinesX dancingLinesX;

    /**
     * Creates a new instance of {@see GridSolver}.
     *
     * @param gridSize
     *         The size of the grid.
     * @param cages
     *         The cages defined for the grid.
     */
    public GridSolver(int gridSize, List<Cage> cages) {
        mGridSize = gridSize;
        mCages = cages;
    }

    /**
     * Checks whether a unique solution can be found for this grid.
     *
     * @return True in case exactly one solution exists for this grid.
     */
    public boolean hasUniqueSolution() {
        dancingLinesX = new DancingLinesInitializer(mGridSize, mCages).initialize().getInitializedDancingLinesX();

        return dancingLinesX.solve(DancingLinesX.SolveType.MULTIPLE) == 1;
    }

    /**
     * Determines the unique solution for this grid.
     *
     * @return The solution of the grid if and only if the grid has exactly one unique solution.
     */
    public int[][] getSolutionGrid() {
        DancingLinesInitializer dancingLinesInitializer = new DancingLinesInitializer(mGridSize, mCages).setUncoverSolution().initialize();

        dancingLinesX = dancingLinesInitializer.getInitializedDancingLinesX();
        List<GridSolverMove> gridSolverMoves = dancingLinesInitializer.getGridSolverMoves();

        // Check if a single unique solution can be determined.
        if (gridSolverMoves == null || dancingLinesX.solve(DancingLinesX.SolveType.MULTIPLE) != 1) {
            throw new InvalidGridException("Grid does not have a unique solution.");
        }

        int[][] solutionGrid = getSolutionGrid(gridSolverMoves, dancingLinesX.getLastSolutionFound());
        if (DEBUG) {
            logSolutionGrid(solutionGrid);
        }

        return solutionGrid;
    }

    private int[][] getSolutionGrid(List<GridSolverMove> gridSolverMoves, List<Integer> solution) {
        // Now rebuild the solution
        int[][] solutionGrid = new int[mGridSize][mGridSize];
        for (GridSolverMove gridSolverMove : gridSolverMoves) {
            if (solution.contains(gridSolverMove.mSolutionRow)) {
                solutionGrid[gridSolverMove.mCellRow][gridSolverMove.mCellCol] = gridSolverMove.mCellValue;
            }
        }
        return solutionGrid;
    }

    private void logSolutionGrid(int[][] solutionGrid) {
        for (int row = 0; row < this.mGridSize; row++) {
            String line = "";
            for (int col = 0; col < this.mGridSize; col++) {
                line += " " + solutionGrid[row][col];
            }
            Log.i(TAG, line);
        }
    }
}
