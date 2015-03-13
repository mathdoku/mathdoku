package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.gridgenerating.cellcoordinates.CellCoordinates;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MatrixTest {
    private static final int matrixSize = 3;
    private static final Item emptyItem = new Item(-1, -1);
    private Matrix<Item> matrix;

    private static class Item {
        private static int nextId = 0;

        // Contents of the item are not relevant for the test as the Matrix
        // class is generic. For this test class the item only contains a unique
        // id.
        public final int id;
        public final int row;
        public final int column;

        public Item(int row, int column) {
            id = nextId++;
            this.row = row;
            this.column = column;
        }
    }

    @Before
    public void setUp() throws Exception {
        matrix = createMatrixWithEntriesOnDiagonalTopLeftToBottomRightFilledWith(emptyItem);
    }

    private Matrix<Item> createMatrixWithEntriesOnDiagonalTopLeftToBottomRightFilledWith(Item diagonalItem) {
        Matrix<Item> matrix = new Matrix<Item>(matrixSize, emptyItem);
        for (int row = 0; row < matrixSize; row++) {
            for (int col = 0; col < matrixSize; col++) {
                if (row == col) {
                    matrix.setValueToRowColumn(diagonalItem, row, col);
                } else {
                    matrix.setValueToRowColumn(new Item(row, col), row, col);
                }
            }
        }
        return matrix;
    }

    @Test
    public void createTransposedMatrix_OriginalMatrixTransposed_AllItemsTransposed() throws Exception {
        Matrix<Item> transposedMatrix = matrix.createTransposedMatrix();
        for (int row = 0; row < matrixSize; row++) {
            for (int col = 0; col < matrixSize; col++) {
                assertThat(matrix.get(row, col), is(transposedMatrix.get(col, row)));
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void get_RowTooSmall_IndexOutOfBoundsException() throws Exception {
        matrix.get(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void get_RowTooBig_IndexOutOfBoundsException() throws Exception {
        matrix.get(matrixSize, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void get_ColumnTooSmall_IndexOutOfBoundsException() throws Exception {
        matrix.get(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void get_ColumnTooBig_IndexOutOfBoundsException() throws Exception {
        matrix.get(0, matrixSize);
    }

    @Test
    public void containsInvalidCellCoordinates_GivenCoordinatesAreAllValid_NoInvalidCellCoordinatesFound() throws
            Exception {
        CellCoordinates[] cellCoordinates = new CellCoordinates[matrixSize * matrixSize];
        int index = 0;
        for (int row = 0; row < matrixSize; row++) {
            for (int col = 0; col < matrixSize; col++) {
                cellCoordinates[index++] = new CellCoordinates(row, col);
            }
        }
        assertThat(matrix.containsInvalidCellCoordinates(cellCoordinates), is(false));
    }

    @Test
    public void containsInvalidCellCoordinates_GivenCoordinatesHaveRowTooSmall_InvalidCellCoordinatesFound() throws
            Exception {
        assertThat(matrix.containsInvalidCellCoordinates(CellCoordinates.EMPTY), is(true));
    }

    @Test
    public void containsInvalidCellCoordinates_GivenCoordinatesHaveRowTooBig_InvalidCellCoordinatesFound() throws
            Exception {
        assertThat(matrix.containsInvalidCellCoordinates(new CellCoordinates(matrixSize, 0)), is(true));
    }

    @Test
    public void containsInvalidCellCoordinates_GivenCoordinatesHaveColumnTooSmall_InvalidCellCoordinatesFound()
            throws Exception {
        assertThat(matrix.containsInvalidCellCoordinates(CellCoordinates.EMPTY), is(true));
    }

    @Test
    public void containsInvalidCellCoordinates_GivenCoordinatesHaveColumnTooBig_InvalidCellCoordinatesFound() throws
            Exception {
        assertThat(matrix.containsInvalidCellCoordinates(new CellCoordinates(0, matrixSize)), is(true));
    }

    @Test
    public void containsInvalidCellCoordinates_SomeOfGivenCoordinatesAreInvalid_InvalidCellCoordinatesFound() throws
            Exception {
        CellCoordinates validCellCoordinates1 = new CellCoordinates(0, 0);
        CellCoordinates validCellCoordinates2 = new CellCoordinates(matrixSize - 1, matrixSize - 1);
        CellCoordinates invalidCellCoordinates = new CellCoordinates(matrixSize, matrixSize);
        assertThat(matrix.containsInvalidCellCoordinates(validCellCoordinates1, validCellCoordinates2,
                                                         invalidCellCoordinates), is(true));
    }

    @Test
    public void
    setValueToAllCellCoordinates_SetAllEntriesOnDiagonalTopLeftToBottomRight_OnlyEntriesOnDiagonalTopLeftToBottomRightAreChanged() throws Exception {
        CellCoordinates[] diagonalCellCoordinates = new CellCoordinates[matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            diagonalCellCoordinates[i] = new CellCoordinates(i, i);
        }

        Item diagonalItem = new Item(99, 99);
        matrix.setValueToAllCellCoordinates(diagonalItem, diagonalCellCoordinates);

        for (int row = 0; row < matrixSize; row++) {
            for (int col = 0; col < matrixSize; col++) {
                if (row == col) {
                    assertThat(matrix.get(row, col), is(diagonalItem));
                } else {
                    assertThat(matrix.get(row, col), is(not(diagonalItem)));
                }
            }
        }
    }

    @Test
    public void setValueToCellCoordinates_SetASpecificEntry_OnlyThisEntryIsChanged() throws Exception {
        Item someItem = new Item(99, 99);
        int itemRow = 2;
        int itemColumn = 1;

        matrix.setValueToCellCoordinates(someItem, new CellCoordinates(itemRow, itemColumn));
        assertThatValueIsSetToRowColumnOnly(someItem, itemRow, itemColumn);
    }

    private void assertThatValueIsSetToRowColumnOnly(Item someItem, int itemRow, int itemColumn) {
        for (int row = 0; row < matrixSize; row++) {
            for (int col = 0; col < matrixSize; col++) {
                if (row == itemRow && col == itemColumn) {
                    assertThat(matrix.get(row, col), is(someItem));
                } else {
                    assertThat(matrix.get(row, col), is(not(someItem)));
                }
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setValueToRowColumn_RowTooSmall_IndexOutOfBoundsException() throws Exception {
        matrix.setValueToRowColumn(emptyItem, -1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setValueToRowColumn_RowTooBig_IndexOutOfBoundsException() throws Exception {
        matrix.setValueToRowColumn(emptyItem, matrixSize, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setValueToRowColumn_ColumnTooSmall_IndexOutOfBoundsException() throws Exception {
        matrix.setValueToRowColumn(emptyItem, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setValueToRowColumn_ColumnTooBig_IndexOutOfBoundsException() throws Exception {
        matrix.setValueToRowColumn(emptyItem, 0, matrixSize);
    }

    @Test
    public void setValueToRowColumn() throws Exception {
        int itemRow = 2;
        int itemColumn = 1;
        Item someItem = new Item(itemRow, itemColumn);

        matrix.setValueToRowColumn(someItem, itemRow, itemColumn);
        assertThatValueIsSetToRowColumnOnly(someItem, itemRow, itemColumn);
    }

    @Test
    public void getColumn() throws Exception {
        ArrayList<Item> expectedItemsInColumn = new ArrayList<Item>();
        int columnToCheck = 2;
        for (int row = 0; row < matrixSize; row++) {
            for (int col = 0; col < matrixSize; col++) {
                Item item = new Item(row, col);
                matrix.setValueToRowColumn(item, row, col);
                if (col == columnToCheck) {
                    expectedItemsInColumn.add(item);
                }
            }
        }
        assertThat((ArrayList<Item>) matrix.getColumn(columnToCheck), is(expectedItemsInColumn));
    }

    @Test
    public void countValueInColumn_ColumnContainsEmptyValues_CountedTheEmptyCells() throws Exception {
        matrix.setValueToRowColumn(emptyItem, 0, 0);
        matrix.setValueToRowColumn(emptyItem, 2, 0);
        assertThat(matrix.countValueInColumn(emptyItem, 0), is(2));
    }

    @Test
    public void clearValue() throws Exception {
        Item itemToClear = matrix.get(1, 2);
        assertThat(itemToClear, is(notNullValue()));
        assertThat(itemToClear, is(not(emptyItem)));
        matrix.clearValue(itemToClear);
        assertThat(matrix.get(1, 2), is(emptyItem));
    }

    @Test
    public void containsNonEmptyCell_CellCoordinatesToCheckContainEmptyValues_NullValuesFound() throws Exception {
        CellCoordinates[] cellCoordinatesToCheck = new CellCoordinates[]{
                // Containing non empty values
                new CellCoordinates(0, 1), new CellCoordinates(0, 2),
                // Containing empty value
                new CellCoordinates(1, 1), new CellCoordinates(2, 2),};
        assertThat(matrix.containsNonEmptyCell(cellCoordinatesToCheck), is(true));
    }

    @Test
    public void containsNonEmptyCell_MatrixWithNullValues_NullValuesFound() throws Exception {
        CellCoordinates[] cellCoordinatesToCheck = new CellCoordinates[]{
                // Containing non empty values
                new CellCoordinates(0, 1), new CellCoordinates(0, 2), new CellCoordinates(1, 0),
                new CellCoordinates(2, 0),};
        assertThat(matrix.containsNonEmptyCell(cellCoordinatesToCheck), is(true));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isEmpty_RowTooSmall_IndexOutOfBoundsException() throws Exception {
        matrix.isEmpty(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isEmpty_RowTooBig_IndexOutOfBoundsException() throws Exception {
        matrix.isEmpty(matrixSize, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isEmpty_ColumnTooSmall_IndexOutOfBoundsException() throws Exception {
        matrix.isEmpty(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isEmpty_ColumnTooBig_IndexOutOfBoundsException() throws Exception {
        matrix.isEmpty(0, matrixSize);
    }

    @Test
    public void isEmpty_EmptyRowColumn_EmptyDetected() throws Exception {
        assertThat(matrix.isEmpty(1, 1), is(true));
    }

    @Test
    public void isEmpty_NonEmptyRowColumn_NotEmptyDetected() throws Exception {
        assertThat(matrix.isEmpty(1, 0), is(false));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isNotEmpty_RowTooSmall_IndexOutOfBoundsException() throws Exception {
        matrix.isNotEmpty(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isNotEmpty_RowTooBig_IndexOutOfBoundsException() throws Exception {
        matrix.isNotEmpty(matrixSize, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isNotEmpty_ColumnTooSmall_IndexOutOfBoundsException() throws Exception {
        matrix.isNotEmpty(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isNotEmpty_ColumnTooBig_IndexOutOfBoundsException() throws Exception {
        matrix.isNotEmpty(0, matrixSize);
    }

    @Test
    public void isNotEmpty_EmptyRowColumn_EmptyDetected() throws Exception {
        assertThat(matrix.isNotEmpty(1, 1), is(false));
    }

    @Test
    public void isNotEmpty_NonEmptyRowColumn_NotEmptyDetected() throws Exception {
        assertThat(matrix.isNotEmpty(1, 0), is(true));
    }

    @Test
    public void getCellCoordinatesForFirstEmptyCell_FirstCellIsEmpty_CellRow0AndColumn0Found() throws Exception {
        CellCoordinates expectedCellCoordinatesFirstCellEmpty = new CellCoordinates(0, 0);
        assertThat(matrix.getCellCoordinatesForFirstEmptyCell(), is(expectedCellCoordinatesFirstCellEmpty));
    }

    @Test
    public void getCellCoordinatesForFirstEmptyCell_MiddleCellIsEmpty_CellRow0AndColumn0Found() throws Exception {
        matrix.setValueToRowColumn(new Item(0, 0), 0, 0);
        CellCoordinates expectedCellCoordinatesFirstCellEmpty = new CellCoordinates(1, 1);
        assertThat(matrix.getCellCoordinatesForFirstEmptyCell(), is(expectedCellCoordinatesFirstCellEmpty));
    }

    @Test
    public void getCellCoordinatesForFirstEmptyCell_LastCellIsEmpty_CellRow0AndColumn0Found() throws Exception {
        for (int i = 0; i < matrixSize - 1; i++) {
            matrix.setValueToRowColumn(new Item(i, i), i, i);
        }
        CellCoordinates expectedCellCoordinatesFirstCellEmpty = new CellCoordinates(matrixSize - 1, matrixSize - 1);
        assertThat(matrix.getCellCoordinatesForFirstEmptyCell(), is(expectedCellCoordinatesFirstCellEmpty));
    }

    @Test
    public void isTransposed_NormalMatrix_IsNotTransposed() throws Exception {
        assertThat(matrix.isTransposed(), is(false));
    }

    @Test
    public void isTransposed_TransposedMatrix_IsTransposed() throws Exception {
        assertThat(matrix.createTransposedMatrix()
                           .isTransposed(), is(true));
    }

    @Test
    public void isTransposed_DoubleTransposedMatrix_IsNotTransposed() throws Exception {
        assertThat(matrix.createTransposedMatrix()
                           .createTransposedMatrix()
                           .isTransposed(), is(false));
    }
}
