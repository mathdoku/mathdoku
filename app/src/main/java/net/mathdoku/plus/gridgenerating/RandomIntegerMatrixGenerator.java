package net.mathdoku.plus.gridgenerating;

import java.util.Random;

/**
 * Creates an Integer Matrix filled with random values. Each number from 1 to the maximum value is used exactly once in
 * each row and exactly once in each column.
 */
public class RandomIntegerMatrixGenerator {
    static final int CORRECT_VALUE_NOT_SET = -1;

    // Maximum number of tries to execute. Changing these values effectively
    // changes the grid generating algorithm.
    private static final int MAX_ATTEMPTS_TO_RANDOMIZE_VALUE = 35;
    private static final int MAX_ATTEMPTS_TO_GET_AN_ELIGIBLE_COLUMN_FOR_A_ROW = 20;

    private final int size;
    private final Random random;
    Matrix<Integer> integerMatrix;

    public RandomIntegerMatrixGenerator(int size, Random random) {
        this.size = size;
        this.random = random;

        integerMatrix = new Matrix<Integer>(size, CORRECT_VALUE_NOT_SET);
        randomizeIntegerMatrix();
    }

    private void randomizeIntegerMatrix() {
        for (int value = 1; value <= size; value++) {
            randomizeValue(value);
        }
    }

    private void randomizeValue(int value) {
        for (int attempts = 1; attempts <= MAX_ATTEMPTS_TO_RANDOMIZE_VALUE; attempts++) {
            if (tryToRandomizeValue(value)) {
                return;
            }
        }
        throw new GridGeneratingException(String.format("Too many tries to randomize value %d.", value));
    }

    private boolean tryToRandomizeValue(int value) {
        for (int row = 0; row < size; row++) {
            int column = getRandomEligibleColumnOnRowForValue(row, value);
            if (column >= 0) {
                integerMatrix.setValueToRowColumn(value, row, column);
            } else {
                // Can not put this value in any empty column on this row.
                // Clear the value from all positions in the solution matrix
                // and try to place this value again.
                integerMatrix.clearValue(value);
                return false;
            }
        }
        return true;
    }

    /**
     * Finds a random column in the given row in which the given value can be place without violation the constraint
     * that the value is already used in the column.
     */
    private int getRandomEligibleColumnOnRowForValue(int row, int value) {
        // todo: probably this is not the most efficient way to determine the
        // next position. An alternative would be to keep a list of positions
        // which still can be chosen and select a random position from this
        // list. For this moment this is not the most important performance
        // bottleneck.
        for (int attempts = 1; attempts <= MAX_ATTEMPTS_TO_GET_AN_ELIGIBLE_COLUMN_FOR_A_ROW; attempts++) {
            int column = random.nextInt(size);
            if (integerMatrix.isEmpty(row, column) && !integerMatrix.getColumn(column)
                    .contains(value)) {
                return column;
            }
        }
        return -1;
    }

    public Matrix<Integer> getMatrix() {
        return integerMatrix;
    }
}
