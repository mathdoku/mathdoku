package net.mathdoku.plus.matrix;

import net.mathdoku.plus.gridgenerating.cellcoordinates.CellCoordinates;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UniqueValuePerRowAndColumnCheckerTest {
    private Matrix<Integer> matrix;
    private final static int EMPTY = -1;
    private final static int GRID_SIZE = 3;
    private static final Integer DUPLICATE_VALUE = 1;
    private static final int FIRST_ROW = 0;
    private static final int LAST_ROW = GRID_SIZE - 1;
    private static final int FIRST_COLUMN = 0;
    private static final int LAST_COLUMN = GRID_SIZE - 1;

    @Before
    public void setUp() throws Exception {
        matrix = new Matrix<Integer>(GRID_SIZE, GRID_SIZE, EMPTY);
    }

    @Test(expected = UniqueValuePerRowAndColumnChecker.InvalidMatrixValue.class)
    public void create_MatrixWith0Value_ThrowsInvalidMatrixValueException() throws Exception {
        matrix.setValueToRowColumn(0,FIRST_ROW,FIRST_COLUMN);
        UniqueValuePerRowAndColumnChecker.create(matrix);
    }

    @Test
    public void check_EmptyMatrix_IsTrue() throws Exception {
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).hasNoDuplicateValues(), is(true));
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).findCellCoordinatesWithDuplicateValues().isEmpty(),
                   is(true));
    }

    @Test
    public void check_MatrixWithUniqueAndEmptyValues_IsTrue() throws Exception {
        matrix.setValueToRowColumn(1,FIRST_ROW,FIRST_COLUMN);
        matrix.setValueToRowColumn(2,FIRST_ROW,LAST_COLUMN);
        matrix.setValueToRowColumn(3,LAST_ROW,LAST_COLUMN);
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).hasNoDuplicateValues(), is(true));
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).findCellCoordinatesWithDuplicateValues().isEmpty(),
                   is(true));
    }

    @Test
    public void check_MatrixWithDuplicateValueInFirstRow_IsFalse() throws Exception {
        matrix.setValueToRowColumn(DUPLICATE_VALUE,FIRST_ROW,FIRST_COLUMN);
        matrix.setValueToRowColumn(DUPLICATE_VALUE,FIRST_ROW,LAST_COLUMN);
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).hasNoDuplicateValues(), is(false));

        List<CellCoordinates> cellCoordinates = new ArrayList<CellCoordinates>();
        cellCoordinates.add(new CellCoordinates(FIRST_ROW, FIRST_COLUMN));
        cellCoordinates.add(new CellCoordinates(FIRST_ROW, LAST_COLUMN));
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).findCellCoordinatesWithDuplicateValues(), is(cellCoordinates));
    }

    @Test
    public void check_MatrixWithDuplicateValueInLastRow_IsFalse() throws Exception {
        matrix.setValueToRowColumn(DUPLICATE_VALUE,LAST_ROW,FIRST_COLUMN);
        matrix.setValueToRowColumn(DUPLICATE_VALUE,LAST_ROW,LAST_COLUMN);
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).hasNoDuplicateValues(), is(false));
    }

    @Test
    public void check_MatrixWithDuplicateValueInFirstColumn_IsFalse() throws Exception {
        matrix.setValueToRowColumn(DUPLICATE_VALUE,FIRST_ROW,FIRST_COLUMN);
        matrix.setValueToRowColumn(DUPLICATE_VALUE,LAST_ROW,FIRST_COLUMN);
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).hasNoDuplicateValues(), is(false));
    }

    @Test
    public void check_MatrixWithDuplicateValueInLastColumn_IsFalse() throws Exception {
        matrix.setValueToRowColumn(DUPLICATE_VALUE,FIRST_ROW,LAST_COLUMN);
        matrix.setValueToRowColumn(DUPLICATE_VALUE,LAST_ROW,LAST_COLUMN);
        assertThat(UniqueValuePerRowAndColumnChecker.create(matrix).hasNoDuplicateValues(), is(false));
    }
}