package net.mathdoku.plus.gridgenerating.cellcoordinates;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CellCoordinatesTest {
    private static final int rowValidCellCoordinates = 1;
    private static final int columnValidCellCoordinates = 2;
    private static final CellCoordinates validCellCoordinates1 = new CellCoordinates(rowValidCellCoordinates,
                                                                                     columnValidCellCoordinates);
    private static final CellCoordinates validCellCoordinates2 = new CellCoordinates(rowValidCellCoordinates,
                                                                                     columnValidCellCoordinates);
    private static final CellCoordinates validCellCoordinates3 = new CellCoordinates(rowValidCellCoordinates,
                                                                                     columnValidCellCoordinates);
    private static final CellCoordinates otherValidCellCoordinates = new CellCoordinates(rowValidCellCoordinates + 1,
                                                                                         columnValidCellCoordinates);

    @Test
    public void canBeCreated_ValidCellCoordinates_True() throws Exception {
        assertThat(CellCoordinates.canBeCreated(0, 0), is(true));
    }

    @Test
    public void canBeCreated_InvalidRowInCellCoordinates_False() throws Exception {
        assertThat(CellCoordinates.canBeCreated(-1, 0), is(false));
    }

    @Test
    public void canBeCreated_InvalidColumnInCellCoordinates_False() throws Exception {
        assertThat(CellCoordinates.canBeCreated(0, -1), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_RowTooSmall_throwsIllegalArgumentException() throws Exception {
        new CellCoordinates(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_ColumnTooSmall_throwsIllegalArgumentException() throws Exception {
        new CellCoordinates(0, -1);
    }

    @Test
    public void getRow_ValidCellCoordinates_RowIsCorrect() throws Exception {
        assertThat(validCellCoordinates1.getRow(), is(rowValidCellCoordinates));
    }

    @Test
    public void getRow_EmptyCellCoordinates_RowIsNegative() throws Exception {
        assert CellCoordinates.EMPTY.getRow() < 0;
    }

    @Test
    public void getColumn_ValidCellCoordinates_ColumnIsCorrect() throws Exception {
        assertThat(validCellCoordinates1.getColumn(), is(columnValidCellCoordinates));
    }

    @Test
    public void getRow_EmptyCellCoordinates_ColumnIsNegative() throws Exception {
        assert CellCoordinates.EMPTY.getColumn() < 0;
    }

    @Test
    public void isNull_ValidCellCoordinates_IsFalse() throws Exception {
        assertThat(validCellCoordinates1.isNull(), is(false));
    }

    @Test
    public void isNotNull_ValidCellCoordinates_IsTrue() throws Exception {
        assertThat(validCellCoordinates1.isNotNull(), is(true));
    }

    @Test
    public void equals_Reflexive_True() throws Exception {
        assertThat(validCellCoordinates1.equals(validCellCoordinates1), is(true));
    }

    @Test
    public void equals_WrongType_False() throws Exception {
        class someOtherObject {
        }
        ;

        assertThat(validCellCoordinates1.equals(new someOtherObject()), is(false));
    }

    @Test
    public void equals_Null_False() throws Exception {
        assertThat(validCellCoordinates1.equals(null), is(false));
    }

    @Test
    public void equals_Symmetric_True() throws Exception {
        assertThat(validCellCoordinates1.equals(validCellCoordinates2), is(true));
        assertThat(validCellCoordinates2.equals(validCellCoordinates1), is(true));
    }

    @Test
    public void equals_Transitive_True() throws Exception {
        assertThat(validCellCoordinates1.equals(validCellCoordinates2), is(true));
        assertThat(validCellCoordinates2.equals(validCellCoordinates3), is(true));
        assertThat(validCellCoordinates1.equals(validCellCoordinates3), is(true));
    }

    @Test
    public void equals_ConsistentWhenMultipleTimesCalled_True() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertThat(validCellCoordinates1.equals(validCellCoordinates2), is(true));
            assertThat(validCellCoordinates1.equals(otherValidCellCoordinates), is(false));
        }
    }

    @Test
    public void hashCode_ConsistentWhenMultipleTimesCalled_True() throws Exception {
        int initialHashCode = validCellCoordinates1.hashCode();
        for (int i = 0; i < 10; i++) {
            assertThat(validCellCoordinates1.hashCode(), is(initialHashCode));
        }
    }

    @Test
    public void hashCode_TwoEqualObjects_SameHashCode() throws Exception {
        assertThat(validCellCoordinates1.hashCode(), is(validCellCoordinates2.hashCode()));
    }

    @Test
    public void hashCode_TwoDifferentsObjects_DifferentHashCode() throws Exception {
        assertThat(
                "When failing method hashcode might need further optimizations to calculate different results for different objects.",
                validCellCoordinates1.hashCode(), is(not(otherValidCellCoordinates.hashCode())));
    }
}
