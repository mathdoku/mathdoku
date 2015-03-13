package net.mathdoku.plus.gridsolving;

import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.gridsolving.dancinglinesx.DancingLinesX;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GridSolver {
    @SuppressWarnings("unused")
    private static final String TAG = GridSolver.class.getName();

    // Replace Config.disabledAlways() on following line with Config.enabledInDevelopmentModeOnly()
    // to show debug information when running in development mode.
    public static final boolean DEBUG = Config.disabledAlways();

    private final DancingLinesX dancingLinesX;
    private final int mGridSize;
    private int mTotalMoves;

    // The list of cages for which the solution has to be checked
    private final List<Cage> mCages;

    // Additional data structure in case the solution has to be uncovered.
    private class Move {
        final int mCageId;
        final int mSolutionRow;
        final int mCellRow;
        final int mCellCol;
        final int mCellValue;

        public Move(int cageId, int solutionRow, int cellRow, int cellCol, int cellValue) {
            mCageId = cageId;
            mSolutionRow = solutionRow;
            mCellRow = cellRow;
            mCellCol = cellCol;
            mCellValue = cellValue;
        }
    }

    private List<Move> mMoves;

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
        dancingLinesX = new DancingLinesX();
    }

    private void initialize(boolean uncoverSolution) {
        int gridSizeSquare = mGridSize * mGridSize;
        int totalCages = mCages.size();

        // Number of columns = number of constraints =
        // BOARD * BOARD (for columns) +
        // BOARD * BOARD (for rows) +
        // Num cages (each cage has to be filled once and only once)
        // Number of rows = number of "moves" =
        // Sum of all the possible cage combinations
        // Number of nodes = sum of each move:
        // num_cells column constraints +
        // num_cells row constraints +
        // 1 (cage constraint)
        mTotalMoves = 0;
        int total_nodes = 0;
        ComboGenerator comboGenerator = null;
        for (Cage cage : mCages) {
            if (cage.getPossibleCombos() == null) {
                if (comboGenerator == null) {
                    comboGenerator = new ComboGenerator(mGridSize);
                }
                cage.setPossibleCombos(comboGenerator.getPossibleCombos(cage, cage.getListOfCells()));
            }
            int possibleMovesInCage = cage.getPossibleCombos()
                    .size();
            mTotalMoves += possibleMovesInCage;
            total_nodes += possibleMovesInCage * (2 * cage.getNumberOfCells() + 1);
        }
        dancingLinesX.init(totalCages + 2 * gridSizeSquare, total_nodes);

        // Reorder cages based on the number of possible moves for the cage
        // because this has a major impact on the time it will take to find a
        // solution. Cage should be ordered on increasing number of possible
        // moves.
        List<Cage> sortedCages = new ArrayList<Cage>(mCages);
        Collections.sort(sortedCages, new SortCagesOnNumberOfMoves());
        if (DEBUG) {
            for (Cage cage : sortedCages) {
                Log.i(TAG, "Cage " + cage.getId() + " has " + cage.getPossibleCombos()
                        .size() + " permutations with " + cage.getNumberOfCells() + " cells");
            }
        }

        // In case the solution has to be uncovered, which is needed in case
        // puzzle are shared, register details of all moves. Maybe all relevant
        // data is already stored but I can't find out how.
        if (uncoverSolution) {
            mMoves = new ArrayList<Move>();
        } else {
            mMoves = null;
        }

        int constraintNumber;
        int comboIndex = 0;
        int cageCount = 0;
        for (Cage cage : sortedCages) {
            List<int[]> possibleCombos = cage.getPossibleCombos();
            for (int[] possibleCombo : possibleCombos) {
                if (DEBUG) {
                    Log.i(TAG,
                          "Combo " + comboIndex + " - Cage " + cage.getId() + " with " + cage.getNumberOfCells() + " " +
                                  "cells");
                }

                // Is this permutation used for cage "cageCount"? The cage
                // constraint is put upfront. As the cage have been sorted on
                // the number of possible permutations this has a positive
                // influence on the solving time.
                constraintNumber = cageCount + 1;
                dancingLinesX.addNode(constraintNumber, comboIndex); // Cage
                // constraint

                // Apply the permutation of "possibleCombo" to the cells in the
                // cages
                for (int i = 0; i < cage.getNumberOfCells(); i++) {
                    Cell cell = cage.getCell(i);

                    // Fill data structure for DancingLinesX algorithm

                    // Is digit "possibleCombo[i]" used in column getColumn()?
                    constraintNumber = totalCages + mGridSize * (possibleCombo[i] - 1) + cell.getColumn() + 1;
                    dancingLinesX.addNode(constraintNumber, comboIndex); // Column
                    // constraint

                    // Is digit "possibleCombo[i]" used in row getRow()?
                    constraintNumber = totalCages + gridSizeSquare + mGridSize * (possibleCombo[i] - 1) + cell.getRow
                            () + 1;
                    dancingLinesX.addNode(constraintNumber, comboIndex); // Row
                    // constraint

                    // Fill data structure for uncovering solution if needed
                    if (uncoverSolution) {
                        mMoves.add(
                                new Move(cage.getId(), comboIndex, cell.getRow(), cell.getColumn(), possibleCombo[i]));
                    }
                    if (DEBUG) {
                        Log.i(TAG,
                              "  Cell " + cell.getCellId() + " row =" + cell.getRow() + " col = " + cell.getColumn()
                                      + " value = " + possibleCombo[i]);
                    }
                }

                // Proceed with next permutation for this or for the next cage
                comboIndex++;
            }

            // Proceed with the permutation(s) of the next cage
            cageCount++;
        }
    }

    /**
     * Comparator to sort cages based on the number of possible moves, the number of cells in the cage and/or the cage
     * id. This order of the cages determine how efficient the puzzle solving will be.
     */
    private class SortCagesOnNumberOfMoves implements Comparator<Cage> {
        @Override
        public int compare(Cage cage1, Cage cage2) {
            int difference = cage1.getPossibleCombos()
                    .size() - cage2.getPossibleCombos()
                    .size();
            if (difference == 0) {
                // Both cages have the same number of possible permutation. Next
                // compare the number of cells in the cage.
                difference = cage1.getNumberOfCells() - cage2.getNumberOfCells();
                if (difference == 0) {
                    // Also the number of cells is equal. Finally compare the
                    // id's.
                    difference = cage1.getId() - cage2.getId();
                }
            }
            return difference;
        }
    }

    /**
     * Checks whether a unique solution can be found for this grid.
     *
     * @return True in case exactly one solution exists for this grid.
     */
    public boolean hasUniqueSolution() {
        if (Config.APP_MODE == AppMode.DEVELOPMENT && DEBUG) {
            initialize(true); // Needed to compute complexity in development
            // mode

            // Search for multiple solutions (but stop as soon as the second
            // solution has been found).
            if (dancingLinesX.solve(DancingLinesX.SolveType.MULTIPLE) == 1) {
                // Only one solution has been found. The real complexity of the
                // puzzle is computed based on this solution.

                getPuzzleComplexity();
                return true;
            } else {
                return false;
            }
        } else {
            initialize(false);
            return dancingLinesX.solve(DancingLinesX.SolveType.MULTIPLE) == 1;
        }
    }

    /**
     * Determines the unique solution for this grid.
     *
     * @return The solution of the grid if and oly if the grid has exactly one unique solution. NULL otherwise.
     */
    public int[][] getSolutionGrid() {
        initialize(true);

        // Check if a single unique solution can be determined.
        if (mMoves == null || dancingLinesX.solve(DancingLinesX.SolveType.MULTIPLE) != 1) {
            return null;
        }

        // Determine which rows are included in the solution.
        boolean[] rowInSolution = new boolean[mTotalMoves];
        for (int i = 0; i < mTotalMoves; i++) {
            rowInSolution[i] = false;
        }
        for (int i = 1; i <= dancingLinesX.getRowsInSolution(); i++) {
            rowInSolution[dancingLinesX.getSolutionRow(i)] = true;
        }

        // Now rebuild the solution
        int[][] solutionGrid = new int[mGridSize][mGridSize];
        for (Move move : mMoves) {
            if (rowInSolution[move.mSolutionRow]) {
                solutionGrid[move.mCellRow][move.mCellCol] = move.mCellValue;
            }
        }

        if (DEBUG) {
            for (int row = 0; row < this.mGridSize; row++) {
                String line = "";
                for (int col = 0; col < this.mGridSize; col++) {
                    line += " " + solutionGrid[row][col];
                }
                Log.i(TAG, line);
            }
        }

        return solutionGrid;
    }

    /**
     * Determines the complexity of a grid by analysing its solution.
     *
     * @return The complexity of a grid.
     */
    @SuppressWarnings("UnusedReturnValue")
    private int getPuzzleComplexity() {
        if (Config.APP_MODE == AppMode.DEVELOPMENT) {
            // ///////////////////////////////////////////////////////////////////////
            // NOT READY FOR PRODUCTION MODE YET.
            //
            // Be sure to call initialize(true) before calling this method
            //
            // The method computes a complexity and prints log messages. It is
            // not yet clear whether the computed complexity match with
            // subjective difficulty.
            // ///////////////////////////////////////////////////////////////////////

            if (DEBUG) {
                Log.i(TAG, "Determine puzzle complexity");
            }
            int[][] solutionGrid = new int[mGridSize][mGridSize];
            int moveCount = 1;
            int previousCageId = -1;
            int puzzleComplexity = 1;
            for (int i = 1; i <= dancingLinesX.getRowsInSolution(); i++) {
                // Each solution row corresponds with one cage in the grid. The
                // order in which the cages are resolved is important. As soon a
                // the
                // next cage has to be resolved, it is determined which cage has
                // the
                // least possible number of permutations available at that
                // moment.
                int solutionRow = dancingLinesX.getSolutionRow(i);

                for (Move move : mMoves) {
                    if (move.mSolutionRow == solutionRow) {
                        if (move.mCageId != previousCageId) {
                            // This is the first cell of the next cage to be
                            // filled.
                            // Determine the number of move for this cage which
                            // are
                            // still possible with the partially filled grid.
                            Cage cage = mCages.get(move.mCageId);
                            List<int[]> cageMoves = cage.getPossibleCombos();
                            int possiblePermutations = 0;
                            for (int[] cageMove : cageMoves) {
                                boolean validMove = true;
                                // Test whether this cage move could be applied
                                // to
                                // the cells of the cage.
                                for (int j = 0; j < cage.getNumberOfCells(); j++) {
                                    // Check if value is already used in this
                                    // row
                                    int cellRow = cage.getCell(j)
                                            .getRow();
                                    for (int col = 0; col < mGridSize; col++) {
                                        if (solutionGrid[cellRow][col] == cageMove[j]) {
                                            // The value is already used on this
                                            // row.
                                            validMove = false;
                                            break;
                                        }
                                    }
                                    if (!validMove) {
                                        break;
                                    }

                                    // Check if value is already used in this
                                    // row
                                    int cellColumn = cage.getCell(j)
                                            .getColumn();
                                    for (int row = 0; row < mGridSize; row++) {
                                        if (solutionGrid[row][cellColumn] == cageMove[j]) {
                                            // The value is already used in this
                                            // column.
                                            validMove = false;
                                            break;
                                        }
                                    }
                                    if (!validMove) {
                                        break;
                                    }
                                }
                                if (validMove) {
                                    // All values of the cageMove could be
                                    // placed in
                                    // their respective cells. So this is really
                                    // a
                                    // permutation which still can be place into
                                    // the
                                    // cage.
                                    possiblePermutations++;
                                }
                            }
                            // The complexity of the puzzle has to be multiplied
                            // with the number of possible permutations of this
                            // cage
                            // as their is not deductive way to reduce the
                            // number of
                            // possible combinations any further. At this moment
                            // a
                            // combination has to be chosen at random to check
                            // to
                            // see if it fails.
                            if (DEBUG) {
                                Log.i(TAG, "Select cage " + move.mCageId + " with complexity " + possiblePermutations);
                            }
                            puzzleComplexity *= possiblePermutations;
                            previousCageId = move.mCageId;
                        }

                        // Fill the grid solution with the correct value.
                        solutionGrid[move.mCellRow][move.mCellCol] = move.mCellValue;

                    }
                }
                if (DEBUG) {
                    Log.i(TAG, "*********** MOVE " + moveCount++ + " ***********");
                    for (int row = 0; row < this.mGridSize; row++) {
                        String line = "";
                        for (int col = 0; col < this.mGridSize; col++) {
                            line += " " + solutionGrid[row][col];
                        }
                        Log.i(TAG, line);
                    }
                }
            }
            if (DEBUG) {
                Log.i(TAG,
                      "Total complexity of puzzle " + puzzleComplexity + " (or " + dancingLinesX.complexity + "??)");
            }

            return puzzleComplexity;
        }

        return 0;
    }
}
