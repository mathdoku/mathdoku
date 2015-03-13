package net.mathdoku.plus.storage.databaseadapter.database;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DataTypeTest {
    @Test
    public void getSqliteDataType_Boolean() throws Exception {
        assertThat(DataType.BOOLEAN.getSqliteDataType(), is("text"));

    }

    @Test
    public void getSqliteDataType_Integer() throws Exception {
        assertThat(DataType.INTEGER.getSqliteDataType(), is("integer"));
    }

    @Test
    public void getSqliteDataType_Long() throws Exception {
        assertThat(DataType.LONG.getSqliteDataType(), is("long"));

    }

    @Test
    public void getSqliteDataType_String() throws Exception {
        assertThat(DataType.STRING.getSqliteDataType(), is("text"));
    }

    @Test
    public void getSqliteDataType_Timestamp() throws Exception {
        assertThat(DataType.TIMESTAMP.getSqliteDataType(), is("datetime"));
    }
}
