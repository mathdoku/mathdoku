package net.mathdoku.plus.storage;

import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cellchange.CellChange;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridBuilder;
import net.mathdoku.plus.statistics.GridStatistics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridStorageTest {
    private GridStorage gridStorage;
    private CellStorage mCellStorageMock = mock(CellStorage.class);
    private CellChangeStorage mCellChangeStorageMock = mock(CellChangeStorage.class);
    private CageStorage mCageStorageMock = mock(CageStorage.class);
    private List<String> cageStorageStrings = new ArrayList<String>();

    private class GridBuilderStub extends GridBuilder {
        private final int mGridSize = 4;
        private String cellStorageConcatenated = "";
        private String cageStorageConcatenated = "";
        private String cellChangesStorageConcatenated = "";

        public GridBuilderStub() {
            super();
            super.setGridSize(mGridSize);

            super.setGridStatistics(mock(GridStatistics.class));

            super.setGridGeneratingParameters(mock(GridGeneratingParameters.class));
        }

        private GridBuilderStub addCellMocksWithStorageString() {
            List<Cell> cells = new ArrayList<Cell>();
            StringBuilder stringBuilder = new StringBuilder();

            int numberOfCells = mGridSize * mGridSize;
            for (int i = 1; i <= numberOfCells; i++) {
                Cell cell = mock(Cell.class);
                String storageString = String.format("*** CELL %d storage string ***", i);
                when(mCellStorageMock.toStorageString(cell)).thenReturn(storageString);
                stringBuilder.append(storageString)
                        .append("\n");
                cells.add(cell);
            }
            super.setCells(cells);
            cellStorageConcatenated = stringBuilder.toString();

            return this;
        }

        private GridBuilderStub addCageMocksWithStorageString(int numberOfCages) {
            List<Cage> cages = new ArrayList<Cage>();
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 1; i <= numberOfCages; i++) {
                Cage cage = mock(Cage.class);
                String storageString = String.format("*** CAGE %d storage string ***", i);
                cageStorageStrings.add(storageString);
                cages.add(cage);
                stringBuilder.append(storageString)
                        .append("\n");
            }
            super.setCages(cages);
            cageStorageConcatenated = stringBuilder.toString();

            return this;
        }

        private GridBuilderStub addCellChangeMocksWithStorageString(int numberOfCellChanges) {
            List<CellChange> cellChanges = new ArrayList<CellChange>();
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 1; i <= numberOfCellChanges; i++) {
                CellChange cellChange = mock(CellChange.class);
                String storageString = String.format("*** CELL CHANGE %d storage string ***", i);
                when(mCellChangeStorageMock.toStorageString(cellChange)).thenReturn(storageString);
                cellChanges.add(cellChange);
                stringBuilder.append(storageString)
                        .append("\n");
            }
            super.setCellChanges(cellChanges);
            cellChangesStorageConcatenated = stringBuilder.toString();

            return this;
        }

        public String getStorageString(String prefix) {
            return prefix + cellStorageConcatenated + cageStorageConcatenated + cellChangesStorageConcatenated;
        }

    }

    private class GridStorageTestObjectsCreator extends GridStorage.ObjectsCreator {
        @Override
        public String createCageStorageString(Cage cage) {
            if (cageStorageStrings == null || cageStorageStrings.isEmpty()) {
                return null;
            }

            String cageStorageString = cageStorageStrings.get(0)
                    .toString();
            cageStorageStrings.remove(0);
            return cageStorageString;
        }

        @Override
        public CellStorage createCellStorage() {
            return mCellStorageMock;
        }

        @Override
        public CellChangeStorage createCellChangeStorage() {
            return mCellChangeStorageMock;
        }
    }

    ;

    private GridStorageTestObjectsCreator mGridStorageTestObjectsCreator;

    @Before
    public void setup() {
        gridStorage = new GridStorage();
        mGridStorageTestObjectsCreator = new GridStorageTestObjectsCreator();
        gridStorage.setObjectsCreator(mGridStorageTestObjectsCreator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromStorageString_StorageStringIsNull_NullPointerException() throws Exception {
        String storageString = null;
        int revisionNumber = 596;

        gridStorage.fromStorageString(storageString, revisionNumber);
    }

    @Test(expected = StorageException.class)
    public void fromStorageString_InvalidLineId_False() throws Exception {
        String storageString = "WRONG:this is not a valid grid storage string";
        int revisionNumber = 596;

        assertThat(gridStorage.fromStorageString(storageString, revisionNumber), is(false));
    }

    @Test(expected = InvalidParameterException.class)
    public void
    fromStorageString_StorageStringHasTooLittleElementsForRevisionLessOrEqualTo595_InvalidParameterException() throws
            Exception {
        String storageString = "GRID:2:3";
        int revisionNumber = 595;

        gridStorage.fromStorageString(storageString, revisionNumber);
    }

    @Test(expected = InvalidParameterException.class)
    public void
    fromStorageString_StorageStringHasTooManyElementsForRevisionLessOrEqualTo595_InvalidParameterException() throws
            Exception {
        String storageString = "GRID:2:3:4:5";
        int revisionNumber = 595;

        gridStorage.fromStorageString(storageString, revisionNumber);
    }

    @Test(expected = InvalidParameterException.class)
    public void
    fromStorageString_StorageStringHasTooLittleElementsForRevisionLessOrEqualTo596_InvalidParameterException() throws
            Exception {
        String storageString = "GRID:2";
        int revisionNumber = 596;

        gridStorage.fromStorageString(storageString, revisionNumber);
    }

    @Test(expected = InvalidParameterException.class)
    public void
    fromStorageString_StorageStringHasTooManyElementsForRevisionLessOrEqualTo596_InvalidParameterException() throws
            Exception {
        String storageString = "GRID:2:3:4";
        int revisionNumber = 596;

        gridStorage.fromStorageString(storageString, revisionNumber);
    }

    @Test(expected = StorageException.class)
    public void fromStorageString_RevisionIdTooLow_False() throws Exception {
        String storageString = "GRID:true:true:1";
        int revisionNumber = 368;

        assertThat(gridStorage.fromStorageString(storageString, revisionNumber), is(false));
    }

    @Test
    public void fromStorageString_ValidStorageStringRevision595_True() throws Exception {
        String storageString = "GRID:true:false:1";
        int revisionNumber = 595;

        assertThat(gridStorage.fromStorageString(storageString, revisionNumber), is(true));
        assertThat(gridStorage.isActive(), is(true));
        assertThat(gridStorage.isRevealed(), is(false));
        // The last element of the storage string ("1") is no longer processed
        // by the method and can therefore not be verified.
    }

    @Test
    public void fromStorageString_ValidStorageStringRevision596_True() throws Exception {
        String storageString = "GRID:false:true";
        int revisionNumber = 596;

        assertThat(gridStorage.fromStorageString(storageString, revisionNumber), is(true));
        assertThat(gridStorage.isActive(), is(false));
        assertThat(gridStorage.isRevealed(), is(true));
    }

    @Test
    public void toStorageString_SaveNewGridWithMultipleCellsAndOneCage_StorageStringCreated() throws Exception {
        GridBuilderStub gridBuilderStub = new GridBuilderStub().addCellMocksWithStorageString()
                .addCageMocksWithStorageString(1);
        gridBuilderStub.setActive(false);
        gridBuilderStub.setRevealed(true);
        Grid grid = gridBuilderStub.build();

        assertThat("Storage string", gridStorage.toStorageString(grid),
                   is(equalTo(gridBuilderStub.getStorageString("GRID:false:true" + "\n"))));
    }

    @Test
    public void toStorageString_SaveNewGridWithMultipleCellsAndMultipleCages_StorageStringCreated() throws Exception {
        GridBuilderStub gridBuilderStub = new GridBuilderStub().addCellMocksWithStorageString()
                .addCageMocksWithStorageString(3);
        gridBuilderStub.setActive(false);
        gridBuilderStub.setRevealed(true);
        Grid grid = gridBuilderStub.build();

        assertThat("Storage string", gridStorage.toStorageString(grid),
                   is(equalTo(gridBuilderStub.getStorageString("GRID:false:true" + "\n"))));
    }

    @Test
    public void toStorageString_SaveNewGridWithMultipleCellsAndOneCageAndOneCellChange_StorageStringCreated() throws Exception {
        GridBuilderStub gridBuilderStub = new GridBuilderStub().addCellMocksWithStorageString()
                .addCageMocksWithStorageString(1)
                .addCellChangeMocksWithStorageString(1);
        gridBuilderStub.setActive(true);
        gridBuilderStub.setRevealed(false);
        Grid grid = gridBuilderStub.build();

        assertThat("Storage string", gridStorage.toStorageString(grid),
                   is(equalTo(gridBuilderStub.getStorageString("GRID:true:false" + "\n"))));
    }

    @Test
    public void toStorageString_SaveNewGridWithMultipleCellsAndMultipleCagesAndMultipleCellChanges_StorageStringCreated() throws Exception {
        GridBuilderStub gridBuilderStub = new GridBuilderStub().addCellMocksWithStorageString()
                .addCageMocksWithStorageString(3)
                .addCellChangeMocksWithStorageString(4);
        gridBuilderStub.setActive(true);
        gridBuilderStub.setRevealed(false);
        Grid grid = gridBuilderStub.build();

        assertThat("Storage string", gridStorage.toStorageString(grid),
                   is(equalTo(gridBuilderStub.getStorageString("GRID:true:false" + "\n"))));
    }
}
