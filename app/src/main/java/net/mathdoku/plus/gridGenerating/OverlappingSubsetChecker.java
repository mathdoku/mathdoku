package net.mathdoku.plus.gridgenerating;

import android.util.Log;

import net.mathdoku.plus.matrix.SquareMatrix;

import java.util.ArrayList;
import java.util.List;

public class OverlappingSubsetChecker {
    @SuppressWarnings("unused")
    private static final String TAG = OverlappingSubsetChecker.class.getName();

    private final SquareMatrix<Integer> correctValueSquareMatrix;
    private int[] countValues;
    private boolean debugLogging;

    public OverlappingSubsetChecker(SquareMatrix<Integer> correctValueSquareMatrix) {
        this.correctValueSquareMatrix = correctValueSquareMatrix;
        debugLogging = false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public OverlappingSubsetChecker enableLogging(boolean enableLogging) {
        debugLogging = enableLogging;
        return this;
    }

    private void initializeUsedValues() {
        // Array for counting values has 1 additional element so the values do
        // not need to be zero-based.
        countValues = new int[correctValueSquareMatrix.size() + 1];
    }

    private void setUsedValue(int value) {
        countValues[value]++;
    }

    private int countDuplicatesValues() {
        int countDuplicates = 0;
        for (int value = 1; value < countValues.length; value++) {
            if (countValues[value] > 1) {
                countDuplicates++;
            }
        }
        return countDuplicates;
    }

    private List<Integer> getDuplicatesValues() {
        List<Integer> duplicateValues = new ArrayList<Integer>();
        for (int value = 1; value < countValues.length; value++) {
            if (countValues[value] > 1) {
                duplicateValues.add(value);
            }
        }
        return duplicateValues;
    }

    public boolean hasOverlap(SquareMatrix<Integer> cageIdSquareMatrix, SquareMatrix<Boolean>
            usedCellsForNewCageSquareMatrix) {
        return hasOverlap(correctValueSquareMatrix, cageIdSquareMatrix, usedCellsForNewCageSquareMatrix) || hasOverlap(
                correctValueSquareMatrix.createTransposedMatrix(), cageIdSquareMatrix.createTransposedMatrix(),
                usedCellsForNewCageSquareMatrix.createTransposedMatrix());
    }

    private boolean hasOverlap(SquareMatrix<Integer> correctValueSquareMatrix, SquareMatrix<Integer> cageIdSquareMatrix,
                               SquareMatrix<Boolean> usedCellsForNewCageSquareMatrix) {
        for (int newCageCol = 0; newCageCol < correctValueSquareMatrix.size(); newCageCol++) {
            if (newCageHasMultipleCellsInColumn(usedCellsForNewCageSquareMatrix, newCageCol) && hasOverlapWithAnyColumn(
                    correctValueSquareMatrix, cageIdSquareMatrix, usedCellsForNewCageSquareMatrix, newCageCol)) {
                return true;
            }
        }

        // No overlapping subset found
        return false;
    }

    private boolean newCageHasMultipleCellsInColumn(SquareMatrix<Boolean> usedCellsForNewCageSquareMatrix, int newCageCol) {
        return usedCellsForNewCageSquareMatrix.countValueInColumn(true, newCageCol) > 1;
    }

    private boolean hasOverlapWithAnyColumn(SquareMatrix<Integer> correctValueSquareMatrix, SquareMatrix<Integer> cageIdSquareMatrix,
                                            SquareMatrix<Boolean> usedCellsForNewCageSquareMatrix, int sourceColumn) {
        for (int targetColumn = 0; targetColumn < correctValueSquareMatrix.size(); targetColumn++) {
            if (targetColumn != sourceColumn && hasOverlapWithColumn(correctValueSquareMatrix, cageIdSquareMatrix,
                                                                     usedCellsForNewCageSquareMatrix, sourceColumn,
                                                                     targetColumn)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOverlapWithColumn(SquareMatrix<Integer> correctValueSquareMatrix, SquareMatrix<Integer> cageIdSquareMatrix,
                                         SquareMatrix<Boolean> usedCellsForNewCageSquareMatrix, int sourceColumn,
                                         int targetColumn) {
        List<Integer> cagesChecked = new ArrayList<Integer>();

        // Iterate all cells in the column from top to bottom.
        for (int row = 0; row < correctValueSquareMatrix.size(); row++) {
            int otherCageId = cageIdSquareMatrix.get(row, targetColumn);
            if (cageIdSquareMatrix.isNotEmpty(row, targetColumn) && usedCellsForNewCageSquareMatrix.get(row,
                                                                                            sourceColumn) &&
                    !cagesChecked.contains(
                    otherCageId)) {
                // Cell[row][col] is used in a cage which is not
                // yet checked. This is the first row for which
                // the new cage and the other cage has a cell in
                // the columns which are compared.
                cagesChecked.add(otherCageId);
                if (hasOverlapInColumnForCage(correctValueSquareMatrix, cageIdSquareMatrix,
                                              usedCellsForNewCageSquareMatrix, sourceColumn,
                                              targetColumn, row, otherCageId)) {
                    return true;
                }

            }
        }
        return false;
    }

    private boolean hasOverlapInColumnForCage(SquareMatrix<Integer> correctValueSquareMatrix, SquareMatrix<Integer> cageIdSquareMatrix,
                                              SquareMatrix<Boolean> usedCellsForNewCageSquareMatrix, int sourceColumn,
                                              int targetColumn, int startRow, int targetCageId) {
        // Check all remaining rows if the checked
        // columns contain a cell for the new cage and
        // the other cage.
        initializeUsedValues();
        for (int row = startRow; row < correctValueSquareMatrix.size(); row++) {
            if (cageIdSquareMatrix.get(row, targetColumn) == targetCageId && usedCellsForNewCageSquareMatrix.get(row,
                                                                                                     sourceColumn)) {
                // Both cages contain a cell on the same
                // row. Remember values used in those
                // cells.
                setUsedValue(correctValueSquareMatrix.get(row, targetColumn));
                setUsedValue(correctValueSquareMatrix.get(row, sourceColumn));
            }
        }

        // Determine which values are used in both cages
        if (countDuplicatesValues() > 1) {
            if (debugLogging) {
                String dimension = correctValueSquareMatrix.isTransposed() ? "row" : "column";
                logNonUniqueSolution(sourceColumn, targetColumn, dimension, targetCageId, getDuplicatesValues());
            }
            return true;
        }
        return false;
    }

    private void logNonUniqueSolution(int dimension1, int dimension2, String dimension, int otherCageId,
                                      List<Integer> duplicateValues) {
        String nonUniqueSolutionMessage = "         This cage type results in a non-unique " +
                "solution. " + "The new cage contains values %s in %s %d which are also used in " +
                "%s %d within cage %d.";

        Log.i(TAG, String.format(nonUniqueSolutionMessage, duplicateValues.toString(), dimension, dimension1, dimension,
                                 dimension2, otherCageId));
    }
}
