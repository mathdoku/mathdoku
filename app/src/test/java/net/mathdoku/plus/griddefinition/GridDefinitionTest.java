package net.mathdoku.plus.griddefinition;

import android.app.Activity;

import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridsolving.GridSolver;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.util.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridDefinitionTest {
    private GridDefinition mGridDefinition;
    private String mGridDefinitionString;
    private GridSolver mGridSolverMock = mock(GridSolver.class);

    /**
     * The GridDefinitionString class creates a grid definition string. Use the setXXX methods to change the default
     * string into an invalid grid definition string.
     */
    private class GridDefinitionString {
        // The grid below is constructed in such a way that each operator is
        // used.
        private String complexityString = "5";
        private String[] cellCageIds = {"00", "00", "00", "01", // Row 1
                "02", "00", "03", "01", // Row 2
                "02", "04", "03", "01", // Row 3
                "05", "04", "06", "01" // Row 4
        };
        private String[] cages = {"0,18,3", "1,10,1", "2,8,3", "3,1,2", "4,4,4", "5,1,0", "6,2,0"};
        private final String separator = ":";
        private final int solution[][] = {{3, 2, 1, 4}, // Row 1
                {2, 3, 4, 1}, // Row 2
                {4, 1, 3, 2}, // Row 3
                {1, 4, 2, 3} // Row 4
        };

        public String create() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(complexityString)
                    .append(separator);
            for (String cellCageId : cellCageIds) {
                stringBuilder.append(cellCageId);
            }
            for (String cage : cages) {
                stringBuilder.append(separator)
                        .append(cage);
            }
            return stringBuilder.toString();
        }

        public GridDefinitionString setPuzzleComplexityReference(String puzzleComplexityReference) {
            complexityString = puzzleComplexityReference;

            return this;
        }

        public GridDefinitionString setInvalidCageReferenceInListOfCells(String invalidCageReferenceOfLastCell) {
            if (invalidCageReferenceOfLastCell.length() != 2) {
                throw new IllegalStateException("Invalid size of cage reference.");
            }
            int arbitraryCellWithInvalidCageReference = 7;
            cellCageIds[arbitraryCellWithInvalidCageReference] = invalidCageReferenceOfLastCell;

            return this;
        }

        public GridDefinitionString setInvalidCageDefinition(String cageDefinition) {
            int arbitraryCage = 2;
            cages[arbitraryCage] = cageDefinition;

            return this;
        }

        public int[][] getSolution() {
            return solution;
        }

        public int[][] getSolutionWithTooFewRows() {
            int gridSize = solution.length;
            int invalidSolution[][] = new int[gridSize - 1][gridSize];
            for (int row = 0; row < gridSize - 1; row++) {
                for (int col = 0; col < gridSize; col++) {
                    invalidSolution[row][col] = solution[row][col];
                }
            }
            return invalidSolution;
        }

        public int[][] getSolutionWithTooFewColumns() {
            int gridSize = solution.length;
            int invalidSolution[][] = new int[gridSize][gridSize - 1];
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize - 1; col++) {
                    invalidSolution[row][col] = solution[row][col];
                }
            }
            return invalidSolution;
        }
    }

    private class GridDefinitionTestObjectsCreator extends GridDefinition.ObjectsCreator {
        List<Cell> cells;
        List<Cage> cages;

        @Override
        public List<Cell> createArrayListOfCells() {
            cells = super.createArrayListOfCells();
            return cells;
        }

        @Override
        public List<Cage> createArrayListOfCages() {
            cages = super.createArrayListOfCages();
            return cages;
        }

        @Override
        public GridSolver createGridSolver(int gridSize, List<Cage> cages) {
            return mGridSolverMock;
        }
    }

    private GridDefinitionTestObjectsCreator mGridDefinitionTestObjectsCreator;

    @Before
    public void setup() {
        // Instantiate the singleton classes
        Activity activity = new Activity();
        new Util(activity);

        mGridDefinitionTestObjectsCreator = new GridDefinitionTestObjectsCreator();
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_NullDefinition_NoGridCreated() throws Exception {
        createGridFromGridDefinition(null);
    }

    private Grid createGridFromGridDefinition(String gridDefinition) {
        return new GridDefinition(gridDefinition, mGridDefinitionTestObjectsCreator).createGrid();
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_EmptyDefinition_NoGridCreated() throws Exception {
        createGridFromGridDefinition("");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasTooLittleElements_NoGridCreated() throws Exception {
        createGridFromGridDefinition("1:02");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionWithNonNumericPuzzleComplexity_NoGridCreated() throws Exception {
        createGridFromGridDefinition("INVALID PUZZLE COMPLEXITY ID:00:0,9,1");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionWithTooBigNumericPuzzleComplexity_NoGridCreated() throws Exception {
        createGridFromGridDefinition("10:00:0,9,1");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasEmptyCellCagesPart_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1::0,9,0");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasCellCagesPartWithUnevenNumberOfDigitis_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1:001:0,9,0");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasCellCagesPartWithNonNumericCharacters_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1:0x:0,9,0");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasCagePartWithTooLittleElements_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1:00:0,9");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasCagePartWithTooManyElements_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1:00:0,9,1,2");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasCagePartWithEmptyCageId_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1:00:,9,0");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasCagePartWithEmptyCageResult_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1:00:0,,0");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasCagePartWithEmptyCageOperator_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1:00:0,9,");
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionHasCagePartWithTooBigCageOperator_GridNotCreated() throws Exception {
        createGridFromGridDefinition("1:00:0,9,10");
    }

    @Test
    public void createGrid_DefinitionWithVeryEasyComplexity_GridCreated() throws Exception {
        assertThatGridIsCreatedWithGivenComplexity("1", PuzzleComplexity.VERY_EASY);
    }

    private void assertThatGridIsCreatedWithGivenComplexity(String puzzleComplexityString,
                                                            PuzzleComplexity puzzleComplexity) {
        GridDefinitionString gridDefinitionString = new GridDefinitionString();
        mGridDefinitionString = gridDefinitionString.setPuzzleComplexityReference(puzzleComplexityString)
                .create();
        when(mGridSolverMock.getSolutionGrid()).thenReturn(gridDefinitionString.getSolution());

        assertThat(createGridFromGridDefinition(mGridDefinitionString).getPuzzleComplexity(), is(puzzleComplexity));
    }

    @Test
    public void createGrid_DefinitionWithEasyComplexity_GridCreated() throws Exception {
        assertThatGridIsCreatedWithGivenComplexity("2", PuzzleComplexity.EASY);
    }

    @Test
    public void createGrid_DefinitionWithNormalComplexity_GridCreated() throws Exception {
        assertThatGridIsCreatedWithGivenComplexity("3", PuzzleComplexity.NORMAL);
    }

    @Test
    public void createGrid_DefinitionWithDifficultComplexity_GridCreated() throws Exception {
        assertThatGridIsCreatedWithGivenComplexity("4", PuzzleComplexity.DIFFICULT);
    }

    @Test
    public void createGrid_DefinitionWithVeryDifficultComplexity_GridCreated() throws Exception {
        assertThatGridIsCreatedWithGivenComplexity("5", PuzzleComplexity.VERY_DIFFICULT);
    }

    @Test
    public void createGrid_DefinitionHasCellCagesPartForInvalidNumberOfCells_GridNotCreated() throws Exception {
        for (int i = 1; i <= 82; i++) {
            switch (i) {
                case 16: // fall through
                case 25: // fall through
                case 36: // fall through
                case 49: // fall through
                case 64: // fall through
                case 81: // fall through
                    // The test should not be executed for these number of cells as
                    // these are valid lengths
                    continue;
                default:
                    assertThatExceptionIsThrownWhenCreatingGridFromDefinitionWithInvalidNumberOfCells(i);
            }
        }
    }

    @Test
    public void createGrid_DefinitionHasCellCagesPartForValidNumberOfCells_GridCreated() throws Exception {
        int j = 1;
        StringBuilder cellParts = new StringBuilder();
        for (int gridSize = 4; gridSize <= 9; gridSize++) {
            for (; j <= gridSize * gridSize; j++) {
                cellParts.append("00");
            }
            createGrid_DefinitionHasCellCagesPartForValidNumberOfCells_GridCreated(gridSize,
                                                                                   "1:" + cellParts.toString() + ":0," +
                                                                                           "9,1");
        }
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionCellRefersToNonExistentCage_GridNotCreated() throws Exception {
        mGridDefinitionString = new GridDefinitionString().setInvalidCageReferenceInListOfCells("99")
                .create();

        createGridFromGridDefinition(mGridDefinitionString);
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionCageHasUnexpectedId_GridNotCreated() throws Exception {
        mGridDefinitionString = new GridDefinitionString().setInvalidCageDefinition("999,2,3")
                .create();

        createGridFromGridDefinition(mGridDefinitionString);
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionInvalidCageOperator_GridNotCreated() throws Exception {
        mGridDefinitionString = new GridDefinitionString().setInvalidCageDefinition("2,2,9")
                .create();

        createGridFromGridDefinition(mGridDefinitionString);
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_DefinitionDoesNotHaveAUniqueSolution_GridNotCreated() throws Exception {
        mGridDefinitionString = new GridDefinitionString().create();
        // Although the default grid has one unique solution, let the
        // GridSolver mock return that no unique solution exists.
        when(mGridSolverMock.getSolutionGrid()).thenReturn(null);

        createGridFromGridDefinition(mGridDefinitionString);
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_SolutionHasUnexpectedNumberOfRows_GridNotCreated() throws Exception {
        GridDefinitionString gridDefinitionString = new GridDefinitionString();
        mGridDefinitionString = gridDefinitionString.create();
        when(mGridSolverMock.getSolutionGrid()).thenReturn(gridDefinitionString.getSolutionWithTooFewRows());

        createGridFromGridDefinition(mGridDefinitionString);
    }

    @Test(expected = InvalidGridException.class)
    public void createGrid_SolutionHasUnexpectedNumberOfColumns_GridNotCreated() throws Exception {
        GridDefinitionString gridDefinitionString = new GridDefinitionString();
        mGridDefinitionString = gridDefinitionString.create();
        when(mGridSolverMock.getSolutionGrid()).thenReturn(gridDefinitionString.getSolutionWithTooFewColumns());

        createGridFromGridDefinition(mGridDefinitionString);
    }

    private boolean assertThatExceptionIsThrownWhenCreatingGridFromDefinitionWithInvalidNumberOfCells(int numberOfCellsInCagePart) throws Exception {
        StringBuilder cagesPart = new StringBuilder();
        for (int i = 0; i < numberOfCellsInCagePart; i++) {
            cagesPart.append("00");
        }
        mGridDefinitionString = "1:" + cagesPart + ":0,9,0";

        try {
            createGridFromGridDefinition(mGridDefinitionString);
        } catch (InvalidGridException e) {
            return true;
        }

        // No exception was thrown
        return false;
    }

    private void createGrid_DefinitionHasCellCagesPartForValidNumberOfCells_GridCreated(int gridSize, String definitionString) throws Exception {
        mGridDefinitionString = definitionString;
        // The mGridSolverMock has to return a solution matrix of correct size.
        // The content does not need to be correct for this test.
        int solution[][] = new int[gridSize][gridSize];
        when(mGridSolverMock.getSolutionGrid()).thenReturn(solution);

        assertThat(createGridFromGridDefinition(mGridDefinitionString), is(notNullValue()));
    }
}
