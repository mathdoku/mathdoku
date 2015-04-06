package net.mathdoku.plus.gridsolving;

import android.util.Log;

import net.mathdoku.plus.config.Config;
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

    private final int mGridSize;
    private final List<Cage> mCages;
    private final DancingLinesX dancingLinesX;

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

        dancingLinesX = new DancingLinesX(getTotalNumberOfPermutations(), getTotalNumberOfConstraints());
    }

    private int getTotalNumberOfPermutations() {
        int totalNumberOfPermutations = 0;
        ComboGenerator comboGenerator = null;
        for (Cage cage : mCages) {
            if (cage.getPossibleCombos() == null) {
                if (comboGenerator == null) {
                    comboGenerator = new ComboGenerator(mGridSize);
                }
                cage.setPossibleCombos(comboGenerator.getPossibleCombos(cage, cage.getListOfCells()));
            }
            // For each possible permutation the following constraint will be created:
            // - One cage constraint
            // - One row constraint per cell in the cage
            // - One column constraint per cell in the cage
            totalNumberOfPermutations += cage.getPossibleCombos().size() * (1 + cage.getNumberOfCells() * 2);
        }
        return totalNumberOfPermutations;
    }

    private int getTotalNumberOfConstraints() {
        // Constraints for MathDoku consists of:
        // - cage constraints (the calculation of the cage). A cage constraint should be interpreted as: "is digit
        //   <d> used in column <c> of the cage?" or "is digit <d> used in row <r> of the cage?". For each cage one
        //   constraint is to be defined.
        //  - column constraints (each digit is used once in each column). A column constraint should be interpreted
        //    as: "is digit <d> used in column <c>?". For each cell in the grid a column constraint has to be defined.
        // - row constraints (each digit is used once in each row). A row constraint should be interpreted as: "is
        //   digit <d> used in row <r>?". For each cell in the grid a row constraint has to be defined.
        return mCages.size() + (2 * mGridSize * mGridSize);
    }

    private void initialize(boolean uncoverSolution) {
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

                dancingLinesX.addPermutation(comboIndex, getCageConstraintIndex(cageCount));

                // Apply the permutation of "possibleCombo" to the cells in the cages
                for (int i = 0; i < cage.getNumberOfCells(); i++) {
                    Cell cell = cage.getCell(i);

                    dancingLinesX.addPermutation(comboIndex, getRowConstraintIndex(cell.getRow(), possibleCombo[i]));
                    dancingLinesX.addPermutation(comboIndex,
                                                 getColumnConstraintIndex(cell.getColumn(), possibleCombo[i]));

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

    private int getCageConstraintIndex(int index) {
        // The dancing lines algorithm regularly searches for the constraint having the least number of permutations.
        // The cage constraint are therefore stored at beginning of the list of the constraints. For further
        // optimization, the cages also have been sorted (ascending) on the number of permutations.
        return index;
    }

    private int getRowConstraintIndex(int rowIndex, int cellValue) {
        return getOffsetToSkipAllCageConstraints() + getOffsetToSkipAllColumnConstraints() +
                getOffsetToFirstConstraintForCellValue(cellValue) + rowIndex;
    }

    private int getOffsetToSkipAllColumnConstraints() {
        // A column constraint exists for each cell in the grid.
        return mGridSize * mGridSize;
    }

    private int getOffsetToSkipAllCageConstraints() {
        return mCages.size();
    }

    private int getOffsetToFirstConstraintForCellValue(int cellValue) {
        return mGridSize * (cellValue - 1);
    }

    private int getColumnConstraintIndex(int columnIndex, int cellValue) {
        return getOffsetToSkipAllCageConstraints() + getOffsetToFirstConstraintForCellValue(cellValue) + columnIndex;
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
        initialize(false);
        return dancingLinesX.solve(DancingLinesX.SolveType.MULTIPLE) == 1;
    }

    /**
     * Determines the unique solution for this grid.
     *
     * @return The solution of the grid if and only if the grid has exactly one unique solution. NULL otherwise.
     */
    public int[][] getSolutionGrid() {
        initialize(true);

        // Check if a single unique solution can be determined.
        if (mMoves == null || dancingLinesX.solve(DancingLinesX.SolveType.MULTIPLE) != 1) {
            return null;
        }

        // Determine which rows are included in the solution.
        boolean[] rowInSolution = new boolean[getTotalNumberOfMoves()];
        for (int i = 0; i < rowInSolution.length; i++) {
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

    private int getTotalNumberOfMoves() {
        int totalMoves = 0;
        for (Cage cage : mCages) {
            totalMoves += cage.getPossibleCombos()
                    .size();
        }
        return totalMoves;
    }
}
