package net.mathdoku.plus.griddefinition;

import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.puzzle.InvalidGridException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GridDefinitionSplitter {
    // Example of a grid definition:
    // 1:00010202000103040506030405060707:0,4,1:1,2,4:2,2,4:3,1,2:4,4,4:5,2,4:6,4,1:7,6,3
    private static final String REGEXP_EXACTLY_ONE_DIGIT = "\\d";
    private static final String REGEXP_EXACTLY_TWO_DIGITS = "\\d\\d";
    private static final String REGEXP_AT_LEAST_ONE_DIGIT = "\\d+";
    private static final String REGEXP_GRID_DEFINITION = new StringBuilder()
            // Part for puzzle complexity
            .append(REGEXP_EXACTLY_ONE_DIGIT)
                    // Part for cage id's per cell
            .append(GridDefinitionDelimiter.LEVEL1.toString()).append("(")
                    // Cage id for a cell
            .append(REGEXP_EXACTLY_TWO_DIGITS)
                    // At least one cell needed
            .append(")+")
                    // Part for cage definitions
            .append("(")
                    // Start of new cage part
            .append(GridDefinitionDelimiter.LEVEL1.toString())
                    // Cage id
            .append(REGEXP_AT_LEAST_ONE_DIGIT).append(GridDefinitionDelimiter.LEVEL2.toString())
                    // Result value of cage
            .append(REGEXP_AT_LEAST_ONE_DIGIT).append(GridDefinitionDelimiter.LEVEL2.toString())
                    // Cage operator
            .append(REGEXP_EXACTLY_ONE_DIGIT)
                    // At least one cage needed
            .append(")+")
            .toString();

    private final String gridDefinition;
    private final String[] gridDefinitionElements;
    private static final int ID_PART_COMPLEXITY = 0;
    private static final int ID_PART_CELLS = 1;
    private static final int ID_PART_FIRST_CAGE = 2;

    public GridDefinitionSplitter(String gridDefinition) {
        if (gridDefinition == null) {
            throw new InvalidGridException("Grid definition cannot be null.");
        }

        this.gridDefinition = gridDefinition;
        if (!gridDefinition.matches(REGEXP_GRID_DEFINITION)) {
            throw new InvalidGridException("Grid definition has invalid format.");
        }

        gridDefinitionElements = gridDefinition.split(GridDefinitionDelimiter.LEVEL1.toString());
        if (gridDefinitionElements == null || gridDefinitionElements.length < 3) {
            throw new InvalidGridException("Grid definition has too little elements.");
        }
    }

    public PuzzleComplexity getPuzzleComplexity() {
        PuzzleComplexity puzzleComplexity = null;
        try {
            puzzleComplexity = PuzzleComplexity.fromId(gridDefinitionElements[ID_PART_COMPLEXITY]);
        } catch (IllegalArgumentException e) {
            throw new InvalidGridException(
                    String.format("Grid definition '%s' has invalid complexity '%s'.", gridDefinition,
                                  gridDefinitionElements[ID_PART_COMPLEXITY]), e);
        }

        return puzzleComplexity;
    }

    public int getNumberOfCells() {
        // The first part of the definition contains the cage number for each
        // individual cell. The cage number always consists of two digits. So
        // the number of cells is equal to 50% of the length of this string.
        return gridDefinitionElements[ID_PART_CELLS].length() / 2;
    }

    public int getNumberOfCages() {
        return gridDefinitionElements.length - ID_PART_FIRST_CAGE;
    }

    public int[] getCageIdPerCell() {
        // The cagesString contains the cage number for each individual cell.
        // The cage number always consists of two digits.
        Pattern pattern = Pattern.compile(REGEXP_EXACTLY_TWO_DIGITS);
        Matcher matcher = pattern.matcher(gridDefinitionElements[ID_PART_CELLS]);
        int[] cageIdPerCell = new int[getNumberOfCells()];
        int cellNumber = 0;
        while (matcher.find()) {
            cageIdPerCell[cellNumber++] = Integer.valueOf(matcher.group());
        }
        return cageIdPerCell;
    }

    public String[] getCageDefinitions() {
        String[] cages = new String[getNumberOfCages()];
        for (int i = ID_PART_FIRST_CAGE; i < gridDefinitionElements.length; i++) {
            cages[i - ID_PART_FIRST_CAGE] = gridDefinitionElements[i];
        }
        return cages;
    }
}
