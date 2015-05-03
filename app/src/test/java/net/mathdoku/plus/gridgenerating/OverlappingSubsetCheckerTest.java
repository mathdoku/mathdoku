package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.matrix.SquareMatrix;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OverlappingSubsetCheckerTest {
    private Integer[] correctValuesPerCell = new Integer[/* cell id */]{
            // row 1
            1, 2, 3, 4,
            // row 2
            2, 1, 4, 3,
            // row 3
            4, 3, 2, 1,
            // row 4
            3, 4, 1, 2};
    private Integer[] cageIds = new Integer[/* cell id */]{
            // row 1
            1, 2, 2, 2,
            // row 2
            1, -1, -1, -1,
            // row 3
            1, -1, -1, -1,
            // row 4
            1, -1, -1, -1};

    @Test
    public void hasOverlap_NewCageHasOverlappingRowWithOtherCage_OverlapDetected() throws Exception {
        Boolean[] usedForNewCage = new Boolean[/* cell id */]{
                // row 1
                false, false, false, false,
                // row 2
                false, false, false, false,
                // row 3
                false, true, true, false,
                // row 4
                false, false, false, false};

        assertOverlap(correctValuesPerCell, cageIds, usedForNewCage, is(true));
    }

    @Test
    public void hasOverlap_NewCageHasOverlappingColumnWithOtherCage_OverlapDetected() throws Exception {
        Boolean[] usedForNewCage = new Boolean[/* cell id */]{
                // row 1
                false, false, false, false,
                // row 2
                false, true, false, false,
                // row 3
                false, true, false, false,
                // row 4
                false, true, false, false};

        assertOverlap(correctValuesPerCell, cageIds, usedForNewCage, is(true));
    }

    @Test
    public void hasOverlap_NewCageHasNoOverlappingSubsetWithOtherCage_NoOverlapDetected() throws Exception {
        Boolean[] usedForNewCage = new Boolean[/* cell id */]{
                // row 1
                false, false, false, false,
                // row 2
                false, false, false, true,
                // row 3
                false, false, true, true,
                // row 4
                false, false, true, false};

        assertOverlap(correctValuesPerCell, cageIds, usedForNewCage, is(false));
    }

    private void assertOverlap(Integer[] correctValuesPerCell, Integer[] cageIds, Boolean[] usedForNewCage,
                               Matcher<Boolean> overlapMatcher) {
        int size = 4;
        assertThat(size * size, is(correctValuesPerCell.length));

        SquareMatrix<Integer> correctValuesSquareMatrix = toIntegerMatrix(correctValuesPerCell, size);
        SquareMatrix<Integer> cageIdSquareMatrix = toIntegerMatrix(cageIds, size);
        SquareMatrix<Boolean> newCageSquareMatrix = toBooleanMatrix(usedForNewCage, 4);
        assertThat(new OverlappingSubsetChecker(correctValuesSquareMatrix).hasOverlap(cageIdSquareMatrix,
                                                                                      newCageSquareMatrix),
                   overlapMatcher);
    }

    private SquareMatrix<Integer> toIntegerMatrix(Integer[] array, int size) {
        SquareMatrix<Integer> integerSquareMatrix = new SquareMatrix<Integer>(size, -1);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                integerSquareMatrix.setValueToRowColumn(array[row * size + col], row, col);
            }
        }
        return integerSquareMatrix;
    }

    private SquareMatrix<Boolean> toBooleanMatrix(Boolean[] array, int size) {
        SquareMatrix<Boolean> booleanSquareMatrix = new SquareMatrix<Boolean>(size, false);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                booleanSquareMatrix.setValueToRowColumn(array[row * size + col], row, col);
            }
        }
        return booleanSquareMatrix;
    }
}
