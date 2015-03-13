package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.puzzle.grid.Grid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;
import testhelper.gridcreator.GridCreator2x2;
import testhelper.gridcreator.GridCreator4x4;
import testhelper.gridcreator.GridCreator5x5;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class GridDatabaseAdapterTest {
    private GridDatabaseAdapter gridDatabaseAdapter;
    private HashMap<Integer, GridRow> expectedGridRowHashMap;

    @Before
    public void setUp() throws Exception {
        TestRunnerHelper.setup(this.getClass()
                                       .getCanonicalName());

        gridDatabaseAdapter = new GridDatabaseAdapter();
    }

    @After
    public void tearDown() throws Exception {
        TestRunnerHelper.tearDown();
    }

    @Test
    public void getDatabaseTableDefinition() throws Exception {
        String resultDatabaseCreateSQL = new GridDatabaseAdapter().getDatabaseTableDefinition()
                .getCreateTableSQL();
        StringBuilder expectedDatabaseCreateSQL = new StringBuilder();
        expectedDatabaseCreateSQL.append("CREATE TABLE `grid` (");
        expectedDatabaseCreateSQL.append("`_id` integer primary key autoincrement, ");
        expectedDatabaseCreateSQL.append("`definition` text not null unique, ");
        expectedDatabaseCreateSQL.append("`grid_size` integer not null, ");
        expectedDatabaseCreateSQL.append("`date_created` datetime not null, ");
        expectedDatabaseCreateSQL.append("`game_seed` long, ");
        expectedDatabaseCreateSQL.append("`generator_revision_number` integer, ");
        expectedDatabaseCreateSQL.append("`puzzle_complexity` text, ");
        expectedDatabaseCreateSQL.append("`hide_operators` text, ");
        expectedDatabaseCreateSQL.append("`max_cage_result` integer, ");
        expectedDatabaseCreateSQL.append("`max_cage_size` integer");
        expectedDatabaseCreateSQL.append(")");
        assertThat(resultDatabaseCreateSQL, is(expectedDatabaseCreateSQL.toString()));
    }

    @Test
    public void upgradeTable() throws Exception {
        // Nothing to test currently.
    }

    @Test(expected = IllegalArgumentException.class)
    public void insert_GridIsNull_ThrowsIllegalArgumentException() throws Exception {
        gridDatabaseAdapter.insert(null);
    }

    @Test
    public void insert_GridIsNotNull() throws Exception {
        int idOfFirstGridInEmptyDatabase = 1;
        assertThat(gridDatabaseAdapter.insert(GridCreator4x4.createEmptyGrid()), is(idOfFirstGridInEmptyDatabase));
    }

    @Test
    public void get() throws Exception {
        insertGridAndAddToHashMap(GridCreator4x4.createEmptyGrid());
        int gridId = insertGridAndAddToHashMap(GridCreator2x2.createEmptyGrid());
        insertGridAndAddToHashMap(GridCreator5x5.createEmptyGrid());

        assertThat(gridDatabaseAdapter.get(gridId), is(getExpectedGridRowFromHashMap(gridId)));
    }

    private GridRow getExpectedGridRowFromHashMap(int gridId) {
        return expectedGridRowHashMap.get(Integer.valueOf(gridId));
    }

    private int insertGridAndAddToHashMap(Grid grid) {
        int gridId = gridDatabaseAdapter.insert(grid);

        GridRow expectedGridRow = new GridRow(gridId, grid.getDefinition(), grid.getGridSize(), grid.getDateCreated(),
                                              grid.getGridGeneratingParameters());

        if (expectedGridRowHashMap == null) {
            expectedGridRowHashMap = new HashMap<Integer, GridRow>();
        }
        expectedGridRowHashMap.put(gridId, expectedGridRow);

        return gridId;
    }

    @Test(expected = IllegalArgumentException.class)
    public void getByGridDefinition_DefinitionIsNull_ThrowsIllegalArgumentException() throws Exception {
        gridDatabaseAdapter.getByGridDefinition(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getByGridDefinition_DefinitionIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        gridDatabaseAdapter.getByGridDefinition("");
    }

    @Test
    public void getByGridDefinition_DefinitionIsNotNullOrEmpty() throws Exception {
        insertGridAndAddToHashMap(GridCreator4x4.createEmptyGrid());
        int gridId = insertGridAndAddToHashMap(GridCreator2x2.createEmptyGrid());
        insertGridAndAddToHashMap(GridCreator5x5.createEmptyGrid());

        String gridDefinition = getExpectedGridRowFromHashMap(gridId).getGridDefinition();
        assertThat(gridDatabaseAdapter.getByGridDefinition(gridDefinition), is(getExpectedGridRowFromHashMap(gridId)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPrefixedColumnName_Null_ThrowsIllegalArgumentException() throws Exception {
        assertThat(GridDatabaseAdapter.getPrefixedColumnName(null), is("`grid`.`TestAbC`"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPrefixedColumnName_Empty_ThrowsIllegalArgumentException() throws Exception {
        assertThat(GridDatabaseAdapter.getPrefixedColumnName(""), is("`grid`.`TestAbC`"));
    }

    @Test
    public void getPrefixedColumnName_NotNullOrEmpty() throws Exception {
        assertThat(GridDatabaseAdapter.getPrefixedColumnName("TestAbC"), is("`grid`.`TestAbC`"));
    }
}
