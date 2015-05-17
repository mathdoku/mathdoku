package net.mathdoku.plus.matrix;

import net.mathdoku.plus.gridgenerating.cellcoordinates.CellCoordinates;

import java.util.ArrayList;
import java.util.List;

public class UniqueValuePerRowAndColumnChecker {
    private Matrix<Integer> matrix;
    private int maxValue;
    private Matrix<CellCoordinates> rowConstraints;
    private Matrix<CellCoordinates> columnConstraints;
    private ArrayList<CellCoordinates> cellCoordinatesHavingDuplicateValues;
    private enum FindMode {FIND_FIRST, FIND_ALL}

    public static class InvalidMatrixValue extends RuntimeException {
    }

    public static UniqueValuePerRowAndColumnChecker create(Matrix<Integer> matrix) {
        checkMatrixValues(matrix);
        return new UniqueValuePerRowAndColumnChecker(matrix);
    }

    private static void checkMatrixValues(Matrix<Integer> matrix) {
        for (int row = 0; row < matrix.getRows(); row++) {
            for (int column = 0; column < matrix.getColumns(); column++) {
                if (matrix.isNotEmpty(row, column) && matrix.get(row, column) <= 0) {
                    throw new InvalidMatrixValue();
                }
            }
        }
    }

    private UniqueValuePerRowAndColumnChecker(Matrix<Integer> matrix) {
        this.matrix = matrix;
    }

    public boolean hasNoDuplicateValues() {
        cellCoordinatesHavingDuplicateValues = new ArrayList<CellCoordinates>();
        if (!matrix.isEmpty()) {
            checkUniqueValuesForEachRowAndColumn(FindMode.FIND_FIRST);
        }
        return cellCoordinatesHavingDuplicateValues.isEmpty();
    }

    private boolean checkUniqueValuesForEachRowAndColumn(FindMode findMode) {
        initializeToFindDuplicateCellCoordinates();
        checkUniqueValuesForEachRow(findMode);
        if (isFirstDuplicateFoundInFindModeFirst(findMode)) {
            return true;
        }
        checkUniqueValuesForEachColumn(findMode);
        return false;
    }

    private boolean isFirstDuplicateFoundInFindModeFirst(FindMode findMode) {
        return !cellCoordinatesHavingDuplicateValues.isEmpty() && FindMode.FIND_FIRST.equals((findMode));
    }

    private void initializeToFindDuplicateCellCoordinates() {
        maxValue = getMaxValue();
        rowConstraints = new Matrix<CellCoordinates>(matrix.getRows(), maxValue, CellCoordinates.EMPTY);
        columnConstraints = new Matrix<CellCoordinates>(matrix.getColumns(), maxValue, CellCoordinates.EMPTY);
    }

    private int getMaxValue() {
        int maxValue = Integer.MIN_VALUE;
        for (int row = 0; row < matrix.getRows(); row++) {
            for (int column = 0; column < matrix.getColumns(); column++) {
                if (matrix.isNotEmpty(row, column)) {
                    maxValue = Math.max(maxValue, matrix.get(row, column));
                }
            }
        }
        return maxValue;
    }

    private void checkUniqueValuesForEachRow(FindMode findMode) {
        for (int row = 0; row < matrix.getRows(); row++) {
            for (int column = 0; column < matrix.getColumns(); column++) {
                checkRowConstraintForCell(row, column);
                if (isFirstDuplicateFoundInFindModeFirst(findMode)) {
                    return;
                }
            }
        }
    }

    private void checkRowConstraintForCell(int row, int column) {
        if (matrix.isNotEmpty(row, column)) {
            int valueDimension = matrix.get(row, column) - 1;
            if (CellCoordinates.EMPTY.equals(rowConstraints.get(row, valueDimension))) {
                // This is the first cell in this row that is using this value.
                rowConstraints.setValueToRowColumn(new CellCoordinates(row, column), row, valueDimension);
            } else {
                // The value is already used on this row of the matrix
                cellCoordinatesHavingDuplicateValues.add(rowConstraints.get(row, valueDimension));
                cellCoordinatesHavingDuplicateValues.add(new CellCoordinates(row, column));
            }
        }
    }

    private void checkUniqueValuesForEachColumn(FindMode findMode) {
        for (int row = 0; row < matrix.getRows(); row++) {
            for (int column = 0; column < matrix.getColumns(); column++) {
                checkColumnConstraintForCell(row, column);
                if (isFirstDuplicateFoundInFindModeFirst(findMode)) {
                    return;
                }
            }
        }
    }

    private void checkColumnConstraintForCell(int row, int column) {
        if (matrix.isNotEmpty(row, column)) {
            int valueDimension = matrix.get(row, column) - 1;
            if (CellCoordinates.EMPTY.equals(columnConstraints.get(column, valueDimension))) {
                // This is the first cell in this column that is using this value.
                columnConstraints.setValueToRowColumn(new CellCoordinates(row, column), column, valueDimension);
            } else {
                // The value is already used on this column of the matrix
                cellCoordinatesHavingDuplicateValues.add(columnConstraints.get(column, valueDimension));
                cellCoordinatesHavingDuplicateValues.add(new CellCoordinates(row, column));
            }
        }
    }

    public List<CellCoordinates> findCellCoordinatesWithDuplicateValues() {
        cellCoordinatesHavingDuplicateValues = new ArrayList<CellCoordinates>();
        if (!matrix.isEmpty()) {
            checkUniqueValuesForEachRowAndColumn(FindMode.FIND_ALL);
        }
        return cellCoordinatesHavingDuplicateValues;
    }
}
