package net.mathdoku.plus.storage.databaseadapter.database;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DatabaseForeignKeyDefinitionTest {
    private static final String COLUMN_NAME = "*** COLUMN NAME ***";
    private static final String REFERS_TO_TABLE_NAME = "*** REFER TO TABLE NAME ***";
    private static final String REFERS_TO_COLUMN_NAME = "*** REFER TO COLUMN NAME ***";

    @Test(expected = IllegalArgumentException.class)
    public void constructor_ColumnIsNull_ThrowsIllegalArgumentException() throws Exception {
        new DatabaseForeignKeyDefinition(null, REFERS_TO_TABLE_NAME, REFERS_TO_COLUMN_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_ColumnIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        new DatabaseForeignKeyDefinition("", REFERS_TO_TABLE_NAME, REFERS_TO_COLUMN_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_RefersToTableIsNull_ThrowsIllegalArgumentException() throws Exception {
        new DatabaseForeignKeyDefinition(COLUMN_NAME, null, REFERS_TO_COLUMN_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_RefersToTableIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        new DatabaseForeignKeyDefinition(COLUMN_NAME, "", REFERS_TO_COLUMN_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_RefersToColumnIsNull_ThrowsIllegalArgumentException() throws Exception {
        new DatabaseForeignKeyDefinition(COLUMN_NAME, REFERS_TO_TABLE_NAME, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_RefersToColumnIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        new DatabaseForeignKeyDefinition(COLUMN_NAME, REFERS_TO_TABLE_NAME, "");
    }

    @Test
    public void getForeignKeyClause_Success() throws Exception {
        DatabaseForeignKeyDefinition databaseForeignKeyDefinition = new DatabaseForeignKeyDefinition(COLUMN_NAME,
                                                                                                     REFERS_TO_TABLE_NAME,
                                                                                                     REFERS_TO_COLUMN_NAME);
        assertThat(databaseForeignKeyDefinition.getForeignKeyClause(),
                   is("FOREIGN KEY(`" + COLUMN_NAME + "`) REFERENCES " + REFERS_TO_TABLE_NAME + "" + "(" + REFERS_TO_COLUMN_NAME + ")"));
    }
}
