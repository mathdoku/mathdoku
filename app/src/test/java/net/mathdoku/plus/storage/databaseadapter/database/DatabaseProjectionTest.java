package net.mathdoku.plus.storage.databaseadapter.database;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DatabaseProjectionTest {
    DatabaseProjection databaseProjection;
    private static final String TARGET_STRING = "*** TARGET STRING ***";
    private static final String SOURCE_STRING = "*** SOURCE STRING ***";
    private static final String TARGET_COLUMN = "targetColumn";
    private static final String SOURCE_TABLE = "sourceTable";
    private static final String SOURCE_COLUMN = "sourceColumn";

    @Before
    public void setup() {
        databaseProjection = new DatabaseProjection();
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetStringToSourceString_TargetIsNull_ThrowsIllegalArgumentException() throws Exception {
        databaseProjection.put(null, SOURCE_STRING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetStringToSourceString_TargetIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        databaseProjection.put("", SOURCE_STRING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetStringToSourceString_SourceIsNull_ThrowsIllegalArgumentException() throws Exception {
        databaseProjection.put(TARGET_STRING, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetStringToSourceString_SourceIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        databaseProjection.put(TARGET_STRING, "");
    }

    @Test
    public void put_MapTargetStringToSourceString_ProjectionCanBeRetrieved() throws Exception {
        String expectedProjection = SOURCE_STRING + " AS `" + TARGET_STRING + "`";

        // When first adding the target_string, a null value is returned.
        assertThat(databaseProjection.put(TARGET_STRING, SOURCE_STRING), is(nullValue()));
        assertThat(databaseProjection.size(), is(1));
        assertThatDatabaseProjectionContains(TARGET_STRING, expectedProjection);

        // When same value is added again, the original value should be
        // returned.
        assertThat(databaseProjection.put(TARGET_STRING, SOURCE_STRING), is(expectedProjection));
        assertThat(databaseProjection.size(), is(1));
    }

    private void assertThatDatabaseProjectionContains(String key, String value) {
        assertThat(databaseProjection.get(key), is(value));
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetColumnToSourceTableColumn_TargetIsNull_ThrowsIllegalArgumentException() throws Exception {
        databaseProjection.put((String) null, SOURCE_TABLE, SOURCE_COLUMN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetColumnToSourceTableColumn_TargetIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        databaseProjection.put("", SOURCE_TABLE, SOURCE_COLUMN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetColumnToSourceTableColumn_SourceTableIsNull_ThrowsIllegalArgumentException() throws
            Exception {
        databaseProjection.put(TARGET_COLUMN, null, SOURCE_COLUMN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetColumnToSourceTableColumn_SourceTableIsEmpty_ThrowsIllegalArgumentException() throws
            Exception {
        databaseProjection.put(TARGET_COLUMN, "", SOURCE_COLUMN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetColumnToSourceTableColumn_SourceColumnIsNull_ThrowsIllegalArgumentException() throws
            Exception {
        databaseProjection.put(TARGET_COLUMN, SOURCE_TABLE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapTargetColumnToSourceTableColumn_SourceColumnIsEmpty_ThrowsIllegalArgumentException() throws
            Exception {
        databaseProjection.put(TARGET_COLUMN, SOURCE_TABLE, "");
    }

    @Test
    public void put_MapTargetColumnToSourceTableColumn_ProjectionCanBeRetrieved() throws Exception {
        String back_tick = "`";
        String expectedProjection = back_tick + SOURCE_TABLE + back_tick + "." + back_tick + SOURCE_COLUMN +
                back_tick + " AS " + back_tick + TARGET_COLUMN + back_tick;

        // When first adding the target_string, a null value is returned.
        assertThat(databaseProjection.put(TARGET_COLUMN, SOURCE_TABLE, SOURCE_COLUMN), is(nullValue()));
        assertThat(databaseProjection.size(), is(1));
        assertThatDatabaseProjectionContains(TARGET_COLUMN, expectedProjection);

        // When same value is added again, the original value should be
        // returned.
        assertThat(databaseProjection.put(TARGET_COLUMN, SOURCE_TABLE, SOURCE_COLUMN), is(expectedProjection));
        assertThat(databaseProjection.size(), is(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapAggregationToSourceTableColumn_TargetIsNull_ThrowsIllegalArgumentException() throws Exception {
        databaseProjection.put((DatabaseProjection.Aggregation) null, SOURCE_TABLE, SOURCE_COLUMN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapAggregationToSourceTableColumn_SourceTableIsNull_ThrowsIllegalArgumentException() throws
            Exception {
        databaseProjection.put(DatabaseProjection.Aggregation.MAX, null, SOURCE_COLUMN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapAggregationToSourceTableColumn_SourceTableIsEmpty_ThrowsIllegalArgumentException() throws
            Exception {
        databaseProjection.put(DatabaseProjection.Aggregation.MAX, "", SOURCE_COLUMN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapAggregationToSourceTableColumn_SourceColumnIsNull_ThrowsIllegalArgumentException() throws
            Exception {
        databaseProjection.put(DatabaseProjection.Aggregation.MAX, SOURCE_TABLE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_MapAggregationToSourceTableColumn_SourceColumnIsEmpty_ThrowsIllegalArgumentException() throws
            Exception {
        databaseProjection.put(DatabaseProjection.Aggregation.MAX, SOURCE_TABLE, "");
    }

    @Test
    public void put_MapDefaultAggregationToSourceTableColumn_ProjectionCanBeRetrieved() throws Exception {
        String back_tick = "`";
        DatabaseProjection.Aggregation databaseProjectAggregation = DatabaseProjection.Aggregation.MAX;
        String expectedAggregationProjection = "MAX(" + back_tick + SOURCE_TABLE + back_tick + "." + back_tick +
                SOURCE_COLUMN + back_tick + ") AS " + back_tick + databaseProjectAggregation
                .getAggregationColumnNameForColumn(
                SOURCE_COLUMN) + back_tick;

        assertThatDatabaseProjectionContainsAggregation(databaseProjectAggregation, expectedAggregationProjection);
    }

    private void assertThatDatabaseProjectionContainsAggregation(DatabaseProjection.Aggregation
                                                                         databaseProjectAggregation,
                                                                 String expectedAggregationProjection) {
        // When first adding the target_string, a null value is returned.
        assertThat(databaseProjection.put(databaseProjectAggregation, SOURCE_TABLE, SOURCE_COLUMN), is(nullValue()));
        assertThat(databaseProjection.size(), is(1));
        assertThatDatabaseProjectionContains(
                databaseProjectAggregation.getAggregationColumnNameForColumn(SOURCE_COLUMN),
                expectedAggregationProjection);

        // When same value is added again, the original value should be
        // returned.
        assertThat(databaseProjection.put(databaseProjectAggregation, SOURCE_TABLE, SOURCE_COLUMN),
                   is(expectedAggregationProjection));
        assertThat(databaseProjection.size(), is(1));
    }

    @Test
    public void put_MapCountAggregationToSourceTableColumn_ProjectionCanBeRetrieved() throws Exception {
        String back_tick = "`";
        DatabaseProjection.Aggregation databaseProjectAggregation = DatabaseProjection.Aggregation.COUNT;
        String expectedAggregationProjection = "COUNT(1) AS " + back_tick + databaseProjectAggregation
                .getAggregationColumnNameForColumn(
                SOURCE_COLUMN) + back_tick;

        assertThatDatabaseProjectionContainsAggregation(databaseProjectAggregation, expectedAggregationProjection);
    }

    @Test
    public void put_MapCountIfTrueAggregationToSourceTableColumn_ProjectionCanBeRetrieved() throws Exception {
        String back_tick = "`";
        DatabaseProjection.Aggregation databaseProjectAggregation = DatabaseProjection.Aggregation.COUNTIF_TRUE;
        String expectedAggregationProjection = "SUM(CASE WHEN " + back_tick + SOURCE_TABLE + back_tick + "." +
                back_tick + SOURCE_COLUMN + back_tick + " = 'true' THEN 1 ELSE 0 END) AS " + back_tick +
                databaseProjectAggregation.getAggregationColumnNameForColumn(
                SOURCE_COLUMN) + back_tick;

        assertThatDatabaseProjectionContainsAggregation(databaseProjectAggregation, expectedAggregationProjection);
    }

    @Test
    public void getAllColumnNames() throws Exception {
        databaseProjection.put(TARGET_STRING, SOURCE_STRING);
        databaseProjection.put(TARGET_COLUMN, SOURCE_TABLE, SOURCE_COLUMN);
        databaseProjection.put(DatabaseProjection.Aggregation.MAX, SOURCE_TABLE, SOURCE_COLUMN);
        databaseProjection.put(DatabaseProjection.Aggregation.COUNT, SOURCE_TABLE, SOURCE_COLUMN);
        databaseProjection.put(DatabaseProjection.Aggregation.COUNTIF_TRUE, SOURCE_TABLE, SOURCE_COLUMN);

        List<String> expectedColumnNames = Arrays.asList(
                new String[]{TARGET_STRING, TARGET_COLUMN, "MAX_" + SOURCE_COLUMN, "COUNT_" + SOURCE_COLUMN,
                        "COUNTIF_TRUE_" + SOURCE_COLUMN});
        Collections.sort(expectedColumnNames);

        List<String> resultColumnNames = Arrays.asList(databaseProjection.getAllColumnNames());
        Collections.sort(resultColumnNames);

        assertThat(resultColumnNames, is(equalTo(expectedColumnNames)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAggregatedKey_SourceIsNull_ThrowsIllegalArgumentException() throws Exception {
        DatabaseProjection.Aggregation.COUNT.getAggregationColumnNameForColumn(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAggregatedKey_SourceIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        DatabaseProjection.Aggregation.COUNT.getAggregationColumnNameForColumn("");
    }
}
