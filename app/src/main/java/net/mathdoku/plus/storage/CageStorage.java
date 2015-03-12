package net.mathdoku.plus.storage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.storage.selector.StorageDelimiter;
import net.mathdoku.plus.util.Util;

import java.util.List;

/**
 * This class converts relevant Cage data to a string which can be persisted and vice versa.
 */
public class CageStorage {
    /*
     * Each line in the entire storage string of a Grid contains information
     * about the type of data stored on the line. Lines containing data for a
     * Grid Cage starts with following identifier.
     */
    private static final String CAGE_LINE_ID = "CAGE";

    private CageStorage() {
        // Prevent instantiation of utility class.
    }

    /**
     * Checks quickly whether the given line contains CellChangeStorage data.
     */
    public static boolean containsCageStorageData(String line) {
        return line != null && line.startsWith(
                CAGE_LINE_ID + StorageDelimiter.FIELD_DELIMITER_LEVEL1);
    }

    /**
     * Read cage information from a storage string which was created with {@link
     * #toStorageString(net.mathdoku.plus.puzzle.cage.Cage)} before.
     *
     * @param line
     *         The line containing the cage information.
     * @param savedWithRevisionNumber
     *         The revision number of the app which was used to created the storage string.
     * @param cells
     *         The cells to which the cell id's in the storage string refer. Be aware that the
     *         contents (i.e. the cage id) of the cells is being set.
     * @return True in case the given line contains cage information and is processed correctly.
     * False otherwise.
     */
    public static CageBuilder getCageBuilderFromStorageString(String line,
                                                              int savedWithRevisionNumber,
                                                              List<Cell> cells) {
        validateParametersGetCageBuilderFromStorageString(line, savedWithRevisionNumber, cells);

        String[] cageParts = getCagePartsFromLine(line);

        CageBuilder cageBuilder = new CageBuilder();

        // Process all parts
        int index = 1;
        int cageId = Integer.parseInt(cageParts[index++]);
        cageBuilder.setId(cageId);
        cageBuilder.setCageOperator(CageOperator.fromId(cageParts[index++]));
        cageBuilder.setResult(Integer.parseInt(cageParts[index++]));

        if (!cageParts[index].isEmpty()) {
            String[] cellParts = cageParts[index].split(StorageDelimiter.FIELD_DELIMITER_LEVEL2);
            for (int i = 0; i < cellParts.length; i++) {
                int cellId = Integer.parseInt(cellParts[i]);
                cells.get(cellId)
                        .setCageId(cageId);
            }
        }
        cageBuilder.setCells(getIdsOfCellsInCage(cells, cageId));
        index++;

        cageBuilder.setHideOperator(Boolean.parseBoolean(cageParts[index++]));

        return cageBuilder;
    }

    private static void validateParametersGetCageBuilderFromStorageString(String line,
                                                                          int savedWithRevisionNumber, List<Cell> cells) {
        if (line == null) {
            throw new IllegalArgumentException("Parameter line cannot be null");
        }

        // When upgrading to MathDoku v2 the history is not converted. As of
        // revision 369 all logic for handling games stored with older versions
        // is removed.
        if (savedWithRevisionNumber <= 368) {
            throw new StorageException(String.format(
                    "Cannot process storage strings of cages created with revision" + " %d or " +
                            "before.",
                    savedWithRevisionNumber));
        }

        if (Util.isListNullOrEmpty(cells)) {
            throw new StorageException("List cannot be null or empty.");
        }
    }

    private static int[] getIdsOfCellsInCage(List<Cell> cells, int cageId) {
        int[] cellIds = new int[countCellsInCage(cells, cageId)];
        int index = 0;
        for (Cell cell : cells) {
            if (cell.getCageId() == cageId) {
                cellIds[index++] = cell.getCellId();
            }
        }
        return cellIds;
    }

    private static int countCellsInCage(List<Cell> cells, int cageId) {
        int countCellsInCage = 0;
        for (Cell cell : cells) {
            if (cell.getCageId() == cageId) {
                countCellsInCage++;
            }
        }
        return countCellsInCage;
    }

    private static String[] getCagePartsFromLine(String line) {
        String[] cageParts = line.split(StorageDelimiter.FIELD_DELIMITER_LEVEL1);

        // Only process the storage string if it starts with the correct
        // identifier.
        if (cageParts == null || !CAGE_LINE_ID.equals(cageParts[0])) {
            throw new StorageException(String.format("Invalid cage storage string '%s'.", line));
        }

        int expectedNumberOfElements = 6;
        if (cageParts.length != expectedNumberOfElements) {
            throw new StorageException(
                    "Wrong number of elements in cage storage string. Got " + cageParts.length +
                            ", expected " + expectedNumberOfElements + ".");
        }
        return cageParts;
    }

    /**
     * Creates a string representation of the given Grid Cage which can be persisted. Use {@link
     * #getCageBuilderFromStorageString(String, int, java.util.List)} to parse the storage string.
     *
     * @param cage
     *         The grid cage which has to be converted to a storage string.
     * @return A string representation of the grid cage.
     */
    public static String toStorageString(Cage cage) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(CAGE_LINE_ID);
        stringBuilder.append(StorageDelimiter.FIELD_DELIMITER_LEVEL1);
        stringBuilder.append(cage.getId());
        stringBuilder.append(StorageDelimiter.FIELD_DELIMITER_LEVEL1);
        stringBuilder.append(cage.getOperator()
                                     .getId());
        stringBuilder.append(StorageDelimiter.FIELD_DELIMITER_LEVEL1);
        stringBuilder.append(cage.getResult());
        stringBuilder.append(StorageDelimiter.FIELD_DELIMITER_LEVEL1);
        for (int cellId : cage.getCells()) {
            stringBuilder.append(cellId);
            stringBuilder.append(StorageDelimiter.FIELD_DELIMITER_LEVEL2);
        }
        stringBuilder.append(StorageDelimiter.FIELD_DELIMITER_LEVEL1);
        stringBuilder.append(Boolean.toString(cage.isOperatorHidden()));

        return stringBuilder.toString();
    }
}
