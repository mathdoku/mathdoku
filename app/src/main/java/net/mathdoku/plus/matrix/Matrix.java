package net.mathdoku.plus.matrix;

import net.mathdoku.plus.gridgenerating.cellcoordinates.CellCoordinates;

import java.util.ArrayList;
import java.util.List;

public class Matrix<T> {
    private final T[][] cellValue;
    private final T emptyValue;
    private int rows;
    private int columns;

    public Matrix(int rows, int columns, T emptyValue) {
        this.rows = rows;
        this.columns = columns;
        this.emptyValue = emptyValue;
        cellValue = (T[][]) new Object[this.rows][this.columns];
        initializeMatrix();
    }

    private void initializeMatrix() {
        for (int row = 0; row < this.rows; row++) {
            for (int column = 0; column < this.columns; column++) {
                cellValue[row][column] = emptyValue;
            }
        }
    }

    public T get(int row, int column) {
        if (isInvalidPosition(row, column)) {
            throw indexOutOfBoundsException(row, column);
        }
        return cellValue[row][column];
    }

    public boolean containsInvalidCellCoordinates(CellCoordinates... cellCoordinatesArray) {
        for (CellCoordinates cellCoordinates : cellCoordinatesArray) {
            if (cellCoordinates.isNull() || isInvalidPosition(cellCoordinates.getRow(), cellCoordinates.getColumn())) {
                return true;
            }
        }
        return false;
    }

    private boolean isInvalidPosition(int row, int column) {
        return isInvalidRowIndex(row) || isInvalidColumnIndex(column);
    }

    private boolean isInvalidRowIndex(int index) {
        return index < 0 || index >= rows;
    }

    private boolean isInvalidColumnIndex(int index) {
        return index < 0 || index >= columns;
    }

    private IndexOutOfBoundsException indexOutOfBoundsException(int row, int column) {
        return new IndexOutOfBoundsException(
                String.format("Invalid position (%d,%d), size of matrix is (%d, %d)", row, column, rows, columns));
    }

    public void setValueToAllCellCoordinates(@SuppressWarnings(
            "SameParameterValue") T value, CellCoordinates... cellCoordinatesArray) {
        for (CellCoordinates cellCoordinates : cellCoordinatesArray) {
            setValueToCellCoordinates(value, cellCoordinates);
        }
    }

    public void setValueToCellCoordinates(T value, CellCoordinates cellCoordinates) {
        setValueToRowColumn(value, cellCoordinates.getRow(), cellCoordinates.getColumn());
    }

    public void setValueToRowColumn(T value, int row, int column) {
        if (isInvalidPosition(row, column)) {
            throw indexOutOfBoundsException(row, column);
        }
        cellValue[row][column] = value;
    }

    public List<T> getColumn(int column) {
        List<T> valuesInColumn = new ArrayList<T>();
        for (int row = 0; row < rows; row++) {
            valuesInColumn.add(cellValue[row][column]);
        }
        return valuesInColumn;
    }

    public int countValueInColumn(Object value, int column) {
        int count = 0;
        for (int row = 0; row < rows; row++) {
            if (cellValue[row][column] == value) {
                count++;
            }
        }
        return count;
    }

    /**
     * Clears the given value from all positions in the solution matrix.
     */
    public void clearValue(Object value) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (cellValue[row][column] == value) {
                    cellValue[row][column] = emptyValue;
                }
            }
        }
    }

    public boolean containsNonEmptyCell(CellCoordinates... cellCoordinatesArray) {
        for (CellCoordinates cellCoordinates : cellCoordinatesArray) {
            if (!isEmpty(cellCoordinates.getRow(), cellCoordinates.getColumn())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (isNotEmpty(row, column)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isEmpty(int row, int column) {
        if (isInvalidPosition(row, column)) {
            throw indexOutOfBoundsException(row, column);
        }
        return cellValue[row][column] == emptyValue;
    }

    public boolean isNotEmpty(int row, int column) {
        return !isEmpty(row, column);
    }

    public CellCoordinates getCellCoordinatesForFirstEmptyCell() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (isEmpty(row, column)) {
                    return new CellCoordinates(row, column);
                }
            }
        }
        return CellCoordinates.EMPTY;
    }

    public T getEmptyValue() {
        return emptyValue;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
