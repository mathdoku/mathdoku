package net.mathdoku.plus.griddefinition;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridgenerating.GridGeneratingParametersBuilder;
import net.mathdoku.plus.gridsolving.GridSolver;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridBuilder;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class creates the unique definition for a grid. This definition is used to uniquely identify
 * grid stored in the database of share via url's. Also the class is able to recreate a grid based
 * on the grid definition.
 */
public class GridDefinition {
    @SuppressWarnings("unused")
    private static final String TAG = GridDefinition.class.getName();

    private final String gridDefinition;
    private GridType gridType;
    private int gridSize;
    private List<Cage> mCages;
    private List<Cell> mCells;
    private PuzzleComplexity puzzleComplexity;
    private int[] mCageIdPerCell;
    private int[] mCountCellsPerCage;

    public static class ObjectsCreator {
        public GridGeneratingParametersBuilder createGridGeneratingParametersBuilder() {
            return new GridGeneratingParametersBuilder();
        }

        public GridSolver createGridSolver(int gridSize, List<Cage> cages) {
            return new GridSolver(gridSize, cages);
        }

        public List<Cell> createArrayListOfCells() {
            return new ArrayList<Cell>();
        }

        public List<Cage> createArrayListOfCages() {
            return new ArrayList<Cage>();
        }

        public GridBuilder createGridBuilder() {
            return new GridBuilder();
        }
    }

    private GridDefinition.ObjectsCreator objectsCreator;

    // package private access for unit testing.
    GridDefinition(String gridDefinition, GridDefinition.ObjectsCreator objectsCreator) {
        if (objectsCreator == null) {
            throw new InvalidParameterException("Parameter objectsCreator cannot be null.");
        }
        this.objectsCreator = objectsCreator;

        this.gridDefinition = gridDefinition;

        parse();
    }

    public GridDefinition(String gridDefinition) {
        this(gridDefinition, new ObjectsCreator());
    }

    private void parse() {
        GridDefinitionSplitter gridDefinitionSplitter = new GridDefinitionSplitter(gridDefinition);

        parseGridSize(gridDefinitionSplitter);
        parsePuzzleComplexity(gridDefinitionSplitter);
        parseCells(gridDefinitionSplitter);
        parseCages(gridDefinitionSplitter);

        // Calculate and set the correct values for each cell if a single
        // solution can be determined for the definition.
        if (!setCorrectCellValues(getSolution())) {
            throw new InvalidGridException("Cannot set the correct values for all cells.");
        }
    }

    private void parseGridSize(GridDefinitionSplitter gridDefinitionSplitter) {
        int cellCount = gridDefinitionSplitter.getNumberOfCells();
        try {
            gridType = GridType.getFromNumberOfCells(cellCount);
        } catch (IllegalArgumentException e) {
            throw new InvalidGridException(
                    String.format("Definition '%s' contains an invalid number of cells.",
                                  gridDefinition), e);
        }
        gridSize = gridType.getGridSize();
    }

    private void parsePuzzleComplexity(GridDefinitionSplitter gridDefinitionSplitter) {
        puzzleComplexity = gridDefinitionSplitter.getPuzzleComplexity();
    }

    private void parseCells(GridDefinitionSplitter gridDefinitionSplitter) {
        mCells = objectsCreator.createArrayListOfCells();

        int expectedNumberOfCages = gridDefinitionSplitter.getNumberOfCages();
        mCountCellsPerCage = new int[expectedNumberOfCages];

        mCageIdPerCell = gridDefinitionSplitter.getCageIdPerCell();
        for (int cellNumber = 0; cellNumber < mCageIdPerCell.length; cellNumber++) {
            addCell(cellNumber, mCageIdPerCell[cellNumber]);
        }
    }

    private void addCell(int cellNumber, int cageId) {
        if (cageId < 0 || cageId >= mCountCellsPerCage.length) {
            throw new InvalidGridException("Cell refers to invalid cage id '" + cageId + "'.");
        }
        Cell cell = new CellBuilder().setGridSize(gridSize)
                .setId(cellNumber)
                .setCageId(cageId)
                .setLenientCheckCorrectValueOnBuild()
                .build();
        mCells.add(cell);
        mCountCellsPerCage[cageId]++;
    }

    private void parseCages(GridDefinitionSplitter gridDefinitionSplitter) {
        mCages = objectsCreator.createArrayListOfCages();
        for (String cageDefinition : gridDefinitionSplitter.getCageDefinitions()) {
            if (!createCage(cageDefinition)) {
                throw new InvalidGridException(
                        String.format("Cannot create cage with definition '%s'.", cageDefinition));
            }
        }
    }

    private boolean createCage(String cageDefinition) {
        CageDefinitionSplitter cageDefinitionSplitter = new CageDefinitionSplitter(cageDefinition);

        CageBuilder cageBuilder = new CageBuilder();
        int cageId = cageDefinitionSplitter.getCageId();
        if (cageId < 0 || cageId >= mCountCellsPerCage.length) {
            throw new InvalidGridException(
                    String.format("Cage id '%d' in cage definition '%s' is not valid.", cageId,
                                  cageDefinition));
        }
        cageBuilder.setId(cageId);
        cageBuilder.setResult(cageDefinitionSplitter.getResult());
        cageBuilder.setCageOperator(cageDefinitionSplitter.getCageOperator());
        cageBuilder.setCells(getCellsWithCageId(cageId));
        cageBuilder.setHideOperator(false);

        Cage cage = cageBuilder.build();
        mCages.add(cage);

        return true;
    }

    private int[] getCellsWithCageId(int cageId) {
        int[] cells = new int[mCountCellsPerCage[cageId]];

        int cellIndex = 0;
        for (int i = 0; i < mCountCellsPerCage[cageId]; i++) {
            if (mCageIdPerCell[i] == cageId) {
                cells[cellIndex++] = i;
            }
        }

        return cells;
    }

    private boolean setCorrectCellValues(int[][] solution) {
        for (Cell cell : mCells) {
            int row = cell.getRow();
            int column = cell.getColumn();
            if (row >= 0 && row < solution.length && column >= 0 && column < solution[row].length) {
                cell.setCorrectValue(solution[row][column]);
            } else {
                return false;
            }
        }

        return true;
    }

    private int[][] getSolution() {
        int[][] solution = objectsCreator.createGridSolver(gridSize, mCages)
                .getSolutionGrid();

        if (solution == null) {
            throw new InvalidGridException("Grid does not have a unique solution.");
        }
        if (solution.length != gridSize) {
            throw new InvalidGridException(
                    String.format("Solution array has %d rows while %d rows were expected.",
                                  solution.length, gridSize));
        }
        for (int row = 0; row < gridSize; row++) {
            if (solution[row].length != gridSize) {
                throw new InvalidGridException(String.format(
                        "Solution array has %d columns in row %d while %d columns " + "were " +
                                "expected.",
                        solution == null ? 0 : solution[row].length, row, gridSize));
            }
        }

        return solution;
    }

    /**
     * Create a grid from the given definition string.
     *
     * @return The grid created from the definition. Null in case of an error.
     */
    public Grid createGrid() {
        GridGeneratingParametersBuilder mGridGeneratingParametersBuilder = objectsCreator.createGridGeneratingParametersBuilder();
        mGridGeneratingParametersBuilder.setGridType(gridType);
        // The complexity is not needed to rebuild the puzzle, but it is stored
        // as it is a great communicator to the (receiving) user how difficult
        // the puzzle is.
        mGridGeneratingParametersBuilder.setPuzzleComplexity(puzzleComplexity);
        mGridGeneratingParametersBuilder.setHideOperators(allCagesHaveCageOperatorNone(mCages));

        GridBuilder gridBuilder = objectsCreator.createGridBuilder();
        return gridBuilder.setGridSize(gridSize)
                .setCells(mCells)
                .setCages(mCages)
                .setGridGeneratingParameters(
                        mGridGeneratingParametersBuilder.createGridGeneratingParameters())
                .build();
    }

    private boolean allCagesHaveCageOperatorNone(List<Cage> cages) {
        for (Cage cage : cages) {
            if (cage.getOperator() != CageOperator.NONE) {
                return false;
            }
        }
        return true;
    }
}
