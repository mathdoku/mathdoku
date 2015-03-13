package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.gridgenerating.cellcoordinates.CellCoordinates;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CageTypeTest {
    private CageType cageTypeExample;

    private final boolean[][] cageTypeMatrixExampleWithNoUnusedBorders = new boolean[][]{
            // row 1
            {false, false, true, false, false},
            // row 2
            {false, true, true, true, false},
            // row 3
            {true, true, false, true, true},};
    private final boolean[][] cageTypeMatrixExampleWithUnusedBorders = new boolean[][]{
            // row 1
            {false, false, false, false, false, false, false},
            // row 3
            {false, false, false, true, false, false, false},
            // row 4
            {false, false, true, true, true, false, false},
            // row 5
            {false, true, true, false, true, true, false},
            // row 6
            {false, false, false, false, false, false, false},};
    private final int cageTypeMatrixExampleHeight = 3;
    private final int cageTypeMatrixExampleWidth = 5;
    private final int cageTypeMatrixExampleSize = 8;

    @Before
    public void setup() {
        cageTypeExample = new CageType(cageTypeMatrixExampleWithUnusedBorders);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_CageTypeMatrixNull_CageTypeNotCreated() throws Exception {
        new CageType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_CageTypeMatrixNoRows_CageTypeNotCreated() throws Exception {
        new CageType(new boolean[][]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_CageTypeMatrixOneRowButNoColumns_CageTypeNotCreated() throws Exception {
        new CageType(new boolean[][]{{}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_CageTypeMatrixRowsOfDifferentLength_CageTypeNotCreated() throws Exception {
        new CageType(new boolean[][]{{true, false}, {true}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_CageTypeMatrixEmpty_CageTypeNotCreated() throws Exception {
        new CageType(new boolean[][]{{false, false}, {false, false}});
    }

    @Test
    public void new_CreateCageTypeSingleCell_CageTypeCreated() throws Exception {
        assertThat(new CageType(new boolean[][]{{true}}), is(notNullValue()));
    }

    @Test
    public void new_CreateCageTypeExample_CageTypeCreated() throws Exception {
        assertThat(cageTypeExample, is(notNullValue()));
    }

    @Test
    public void new_CreateCageTypeExampleWithUnusedBorders_EqualsCageTypeExampleWithNoUnusedBorders() throws Exception {
        CageType cageTypeExampleUnusedBorder = new CageType(cageTypeMatrixExampleWithNoUnusedBorders);
        assertThat(cageTypeExampleUnusedBorder, is(notNullValue()));
        assertThat(cageTypeExampleUnusedBorder, equalTo(cageTypeExample));
    }

    @Test
    public void new_CreateCageTypeWithBigUnusedBorders_CageTypeCreated() throws Exception {
        // Define a single cell cage type inside a big matrix with unused cells.
        boolean[][] cageTypeMatrixWithBigUnusedBorders = new boolean[11][11];
        cageTypeMatrixWithBigUnusedBorders[4][7] = true;

        CageType cageTypeWithBigUnusedBorders = new CageType(cageTypeMatrixWithBigUnusedBorders);
        assertThat(cageTypeWithBigUnusedBorders, is(notNullValue()));
        assertThat(cageTypeWithBigUnusedBorders.size(), is(1));
        assertThat(cageTypeWithBigUnusedBorders.getHeight(), is(1));
        assertThat(cageTypeWithBigUnusedBorders.getWidth(), is(1));
    }

    @Test
    public void size() throws Exception {
        assertThat(cageTypeExample.size(), is(cageTypeMatrixExampleSize));
    }

    @Test
    public void getWidth() throws Exception {
        assertThat(cageTypeExample.getWidth(), is(cageTypeMatrixExampleWidth));
    }

    @Test
    public void getHeight() throws Exception {
        assertThat(cageTypeExample.getHeight(), is(cageTypeMatrixExampleHeight));
    }

    @Test
    public void getExtendedCageTypeMatrix() throws Exception {
        boolean[][] extendedCageType = cageTypeExample.getExtendedCageTypeMatrix();
        assertThat(extendedCageType, equalTo(cageTypeMatrixExampleWithUnusedBorders));
    }

    @Test
    public void getCellCoordinatesOfAllCellsInCage_CageTypeCannotBeShiftedToOriginLocation_EmptyCellCoordinates()
            throws Exception {
        assertThat(cageTypeExample.getCellCoordinatesOfAllCellsInCage(new CellCoordinates(0, 0)),
                   equalTo(new CellCoordinates[]{CellCoordinates.EMPTY}));

    }

    @Test
    public void getCellCoordinatesOfAllCellsInCage_CageTypeCanBeShiftedToOriginLocation_EmptyCellCoordinates() throws
            Exception {
        // Use an origin position to lower right of the cage type example, so we
        // are sure that it can be shifted.
        int originRow = cageTypeMatrixExampleHeight + 1;
        int originColumn = cageTypeMatrixExampleWidth + 1;
        assertThat(cageTypeExample.getCellCoordinatesOfAllCellsInCage(new CellCoordinates(originRow, originColumn)),
                   equalTo(new CellCoordinates[]{
                           // row 1
                           new CellCoordinates(originRow, originColumn),
                           // row 2
                           new CellCoordinates(originRow + 1, originColumn - 1),
                           new CellCoordinates(originRow + 1, originColumn),
                           new CellCoordinates(originRow + 1, originColumn + 1),
                           // row 3
                           new CellCoordinates(originRow + 2, originColumn - 2),
                           new CellCoordinates(originRow + 2, originColumn - 1),
                           new CellCoordinates(originRow + 2, originColumn + 1),
                           new CellCoordinates(originRow + 2, originColumn + 2),}));
    }
}
