package net.mathdoku.plus.gridgenerating.cellcoordinates;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NullCellCoordinatesTest {
    private NullCellCoordinates nullCellCoordinates = NullCellCoordinates.create();

    @Test
    public void create_NullCellCoordinatesTest_IsNotNull() throws Exception {
        assertThat(nullCellCoordinates, is(notNullValue()));
    }

    @Test
    public void create_ConsecutiveCalls_SameInstance() throws Exception {
        assertThat(NullCellCoordinates.create(), is(sameInstance(nullCellCoordinates)));
    }

    @Test
    public void isNull_EmptyCellCoordinates_IsTrue() throws Exception {
        assertThat(nullCellCoordinates.isNull(), is(true));
    }

    @Test
    public void isNotNull_EmptyCellCoordinates_IsFalse() throws Exception {
        assertThat(nullCellCoordinates.isNotNull(), is(false));
    }
}
