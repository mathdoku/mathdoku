package net.mathdoku.plus.matrix;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SquareMatrixTest {
    private static final int squareMatrixSize = 3;
    private static final Item emptyItem = new Item(-1, -1);
    private SquareMatrix<Item> squareMatrix;

    private static class Item {
        private static int nextId = 0;

        // Contents of the item are not relevant for the test as the SquareMatrix
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
        squareMatrix = createSquareMatrixWithEntriesOnDiagonalTopLeftToBottomRightFilledWith(emptyItem);
    }

    private SquareMatrix<Item> createSquareMatrixWithEntriesOnDiagonalTopLeftToBottomRightFilledWith(Item diagonalItem) {
        SquareMatrix<Item> squareMatrix = new SquareMatrix<Item>(squareMatrixSize, emptyItem);
        for (int row = 0; row < squareMatrixSize; row++) {
            for (int col = 0; col < squareMatrixSize; col++) {
                if (row == col) {
                    squareMatrix.setValueToRowColumn(diagonalItem, row, col);
                } else {
                    squareMatrix.setValueToRowColumn(new Item(row, col), row, col);
                }
            }
        }
        return squareMatrix;
    }

    @Test
    public void createTransposedSquareMatrix_OriginalSquareMatrixTransposed_AllItemsTransposed() throws Exception {
        SquareMatrix<Item> transposedSquareMatrix = squareMatrix.createTransposedMatrix();
        for (int row = 0; row < squareMatrixSize; row++) {
            for (int col = 0; col < squareMatrixSize; col++) {
                assertThat(squareMatrix.get(row, col), is(transposedSquareMatrix.get(col, row)));
            }
        }
    }

    @Test
    public void isTransposed_NormalSquareMatrix_IsNotTransposed() throws Exception {
        assertThat(squareMatrix.isTransposed(), is(false));
    }

    @Test
    public void isTransposed_TransposedSquareMatrix_IsTransposed() throws Exception {
        assertThat(squareMatrix.createTransposedMatrix()
                           .isTransposed(), is(true));
    }

    @Test
    public void isTransposed_DoubleTransposedSquareMatrix_IsNotTransposed() throws Exception {
        assertThat(squareMatrix.createTransposedMatrix()
                           .createTransposedMatrix()
                           .isTransposed(), is(false));
    }
}
