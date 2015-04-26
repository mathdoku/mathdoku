package net.mathdoku.plus.gridsolving;

import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.gridsolving.combogenerator.ComboGenerator;
import net.mathdoku.plus.gridsolving.dancinglinesx.DancingLinesX;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DancingLinesInitializer {
    private static final String TAG = DancingLinesInitializer.class.getName();

    // Replace Config.disabledAlways() on following line with Config.enabledInDevelopmentModeOnly()
    // to show debug information when running in development mode.
    private static final boolean DEBUG = Config.disabledAlways();

    private final int mGridSize;
    private final List<Cage> mCages;
    private boolean uncoverSolution;

    private DancingLinesX dancingLinesX;
    private List<GridSolverMove> gridSolverMoves;

    private int numberOfPermutationsAdded;
    private int cageCount;

    public DancingLinesInitializer(int gridSize, List<Cage> cages) {
        mGridSize = gridSize;
        mCages = sort(cages);
        uncoverSolution = false;
    }

    private List<Cage> sort(List<Cage> cages) {
        List<Cage> sortedCages = new ArrayList<Cage>(cages);

        // Reorder cages based on the number of possible moves for the cage because this has a major impact on the
        // time it will take to find a solution. Cage should be ordered on increasing number of possible moves.
        Collections.sort(sortedCages, new NumberOfMovesCageComparator());
        if (DEBUG) {
            for (Cage cage : sortedCages) {
                Log.i(TAG, "Cage " + cage.getId() + " has " + cage.getNumberOfPossibleCombos() + " permutations with " + cage.getNumberOfCells
                        () + " cells");
            }
        }

        return sortedCages;
    }

    /**
     * Comparator to sort cages based on the number of possible moves, the number of cells in the cage and/or the cage
     * id. This order of the cages determine how efficient the puzzle solving will be.
     */
    private class NumberOfMovesCageComparator implements Comparator<Cage> {
        @Override
        public int compare(Cage cage1, Cage cage2) {
            int difference = cage1.getNumberOfPossibleCombos() - cage2.getNumberOfPossibleCombos();
            if (difference == 0) {
                // Both cages have the same number of possible permutations. Next compare the number of cells in the
                // cage.
                difference = cage1.getNumberOfCells() - cage2.getNumberOfCells();
                if (difference == 0) {
                    // Also the number of cells is equal. Finally compare the id's.
                    difference = cage1.getId() - cage2.getId();
                }
            }
            return difference;
        }
    }

    public DancingLinesInitializer initialize() {
        dancingLinesX = new DancingLinesX(getTotalNumberOfPermutations(), getTotalNumberOfConstraints());

        // In case the solution has to be uncovered, which is needed in case puzzle are shared,
        // register details of all moves. Maybe all relevant data is already stored but I can't find out how.
        gridSolverMoves = uncoverSolution ? new ArrayList<GridSolverMove>() : null;

        numberOfPermutationsAdded = 0;
        cageCount = 0;
        for (Cage cage : mCages) {
            List<int[]> possibleCombos = cage.getPossibleCombos();
            for (int[] possibleCombo : possibleCombos) {
                addPermutations(cage, possibleCombo);
                numberOfPermutationsAdded++;
            }
            cageCount++;
        }

        return this;
    }

    private void addPermutations(Cage cage, int[] possibleCombo) {
        if (DEBUG) {
            Log.i(TAG, "Combo " + numberOfPermutationsAdded + " - Cage " + cage.getId() + " with " + cage.getNumberOfCells() + " " +
                          "cells");
        }

        dancingLinesX.addPermutation(numberOfPermutationsAdded, getCageConstraintIndex(cageCount));

        // Apply the permutation of "possibleCombo" to the cells in the cages
        for (int i = 0; i < cage.getNumberOfCells(); i++) {
            Cell cell = cage.getCell(i);

            dancingLinesX.addPermutation(numberOfPermutationsAdded, getRowConstraintIndex(cell.getRow(), possibleCombo[i]));
            dancingLinesX.addPermutation(numberOfPermutationsAdded,
                                         getColumnConstraintIndex(cell.getColumn(), possibleCombo[i]));

            // Fill data structure for uncovering solution if needed
            if (uncoverSolution) {
                gridSolverMoves.add(
                        new GridSolverMove(cage.getId(), numberOfPermutationsAdded, cell.getRow(), cell.getColumn(), possibleCombo[i]));
            }
            if (DEBUG) {
                Log.i(TAG,
                      "  Cell " + cell.getCellId() + " row =" + cell.getRow() + " col = " + cell.getColumn()
                              + " value = " + possibleCombo[i]);
            }
        }
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
            totalNumberOfPermutations += cage.getPossibleCombos()
                    .size() * (1 + cage.getNumberOfCells() * 2);
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

    public DancingLinesX getInitializedDancingLinesX() {
        if (dancingLinesX == null) {
            throw new IllegalStateException("Call method initialize first.");
        }
        return dancingLinesX;
    }

    public DancingLinesInitializer setUncoverSolution() {
        if (dancingLinesX != null) {
            throw new IllegalStateException("This method should be called before initialize");
        }
        uncoverSolution = true;

        return this;
    }

    public List<GridSolverMove> getGridSolverMoves() {
        return gridSolverMoves;
    }

    public int getNumberOfPermutationsAdded() {
        return numberOfPermutationsAdded;
    }
}
