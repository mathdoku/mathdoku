package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderByHelperTest {
    OrderByHelper orderByHelper;

    @Before
    public void setUp() {
        orderByHelper = new OrderByHelper();
    }

    @Test
    public void sortAscending_1ColumnSet_Success() throws Exception {
        orderByHelper.sortAscending("COLUMN1");
        assertThat(orderByHelper.toString(), is("`COLUMN1` ASC"));
    }

    @Test
    public void sortAscending_2ColumnsSet_Success() throws Exception {
        orderByHelper.sortAscending("COLUMN1");
        orderByHelper.sortAscending("COLUMN2");
        assertThat(orderByHelper.toString(), is("`COLUMN1` ASC, `COLUMN2` ASC"));
    }

    @Test
    public void sortAscending_3ColumnsSet_Success() throws Exception {
        orderByHelper.sortAscending("COLUMN1");
        orderByHelper.sortAscending("COLUMN2");
        orderByHelper.sortAscending("COLUMN3");
        assertThat(orderByHelper.toString(), is("`COLUMN1` ASC, `COLUMN2` ASC, `COLUMN3` ASC"));
    }

    @Test
    public void sortDescending_1ColumnSet_Success() throws Exception {
        orderByHelper.sortDescending("COLUMN1");
        assertThat(orderByHelper.toString(), is("`COLUMN1` DESC"));
    }

    @Test
    public void sortDescending_2ColumnsSet_Success() throws Exception {
        orderByHelper.sortDescending("COLUMN1");
        orderByHelper.sortDescending("COLUMN2");
        assertThat(orderByHelper.toString(), is("`COLUMN1` DESC, `COLUMN2` DESC"));
    }

    @Test
    public void sortDescending_3ColumnsSet_Success() throws Exception {
        orderByHelper.sortDescending("COLUMN1");
        orderByHelper.sortDescending("COLUMN2");
        orderByHelper.sortDescending("COLUMN3");
        assertThat(orderByHelper.toString(), is("`COLUMN1` DESC, `COLUMN2` DESC, `COLUMN3` DESC"));
    }

    @Test(expected = IllegalStateException.class)
    public void toString_NoColumnsSet_ThrowsIllegalStateException() throws Exception {
        orderByHelper.toString();
    }

    @Test
    public void toString_3ColumnsSet_ThrowsIllegalStateException() throws Exception {
        orderByHelper.sortDescending("COLUMN1");
        orderByHelper.sortAscending("COLUMN2");
        orderByHelper.sortDescending("COLUMN3");
        assertThat(orderByHelper.toString(), is("`COLUMN1` DESC, `COLUMN2` ASC, `COLUMN3` DESC"));
    }
}
