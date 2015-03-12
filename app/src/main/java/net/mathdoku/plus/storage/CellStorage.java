package net.mathdoku.plus.storage;

import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.storage.selector.StorageDelimiter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class converts relevant Cell data to a string which can be persisted and vice versa.
 */
public class CellStorage {
    @SuppressWarnings("unused")
    private static final String TAG = CellStorage.class.getName();

    private static final String LINE_IDENTIFIER = "CELL";

    /**
     * Checks quickly whether the given line contains CellStorage data.
     */
    public static boolean containsCellStorageData(String line) {
        return line != null && line.startsWith(
                LINE_IDENTIFIER + StorageDelimiter.FIELD_DELIMITER_LEVEL1);
    }

    /**
     * Read cell information from a storage string which was created with {@link
     * #toStorageString(net.mathdoku.plus.puzzle.cell.Cell)} before.
     *
     * @param line
     *         The line containing the cell information.
     * @return True in case the given line contains cell information and is processed correctly.
     * False otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public CellBuilder getCellBuilderFromStorageString(String line, int savedWithRevisionNumber) {
        validateGetCellBuilderFromStorageString(line, savedWithRevisionNumber);

        String[] cellParts = getCellParts(line, savedWithRevisionNumber);

        CellBuilder cellBuilder = new CellBuilder();

        // Process all parts
        int index = 1;
        cellBuilder.setId(Integer.parseInt(cellParts[index++]));
        if (savedWithRevisionNumber <= 596) {
            // Skip fields row and column. These will be derived from the cell
            // number.
            index += 2;
        }
        cellBuilder.setCageText(cellParts[index++]);
        cellBuilder.setCorrectValue(Integer.parseInt(cellParts[index++]));
        cellBuilder.setEnteredValue(Integer.parseInt(cellParts[index++]));
        cellBuilder.setPossibles(getPossibleValuesFromCellPart(cellParts[index++]));
        cellBuilder.setInvalidValueHighlight(Boolean.parseBoolean(cellParts[index++]));
        cellBuilder.setRevealed(Boolean.parseBoolean(cellParts[index++]));
        cellBuilder.setSelected(Boolean.parseBoolean(cellParts[index++]));

        return cellBuilder;
    }

    private void validateGetCellBuilderFromStorageString(String line, int savedWithRevisionNumber) {
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
    }

    private String[] getCellParts(String line, int savedWithRevisionNumber) {
        String[] cellParts = line.split(StorageDelimiter.FIELD_DELIMITER_LEVEL1);
        // Only process the storage string if it starts with the correct
        // identifier.
        if (cellParts == null || !LINE_IDENTIFIER.equals(cellParts[0])) {
            throw new StorageException(String.format("Invalid cell storage string '%s'.", line));
        }

        int expectedNumberOfElements = savedWithRevisionNumber <= 596 ? 11 : 9;
        if (cellParts.length != expectedNumberOfElements) {
            throw new InvalidParameterException(
                    "Wrong number of elements in cell storage string. Got " + cellParts.length +
                            ", expected " + expectedNumberOfElements + ".");
        }
        return cellParts;
    }

    private List<Integer> getPossibleValuesFromCellPart(String cellPart) {
        List<Integer> possibles = new ArrayList<Integer>();
        if (!cellPart.isEmpty()) {
            for (String possible : cellPart.split(StorageDelimiter.FIELD_DELIMITER_LEVEL2)) {
                possibles.add(Integer.parseInt(possible));
            }
        }
        return possibles;
    }

    /**
     * Create a string representation of the Grid Cell which can be used to store a grid cell in a
     * saved game.
     *
     * @return A string representation of the grid cell.
     */
    public String toStorageString(Cell cell) {
        String storageString = LINE_IDENTIFIER + StorageDelimiter.FIELD_DELIMITER_LEVEL1 + cell
                .getCellId() + StorageDelimiter.FIELD_DELIMITER_LEVEL1 + cell.getCageText() +
                StorageDelimiter.FIELD_DELIMITER_LEVEL1 + cell.getCorrectValue() +
                StorageDelimiter.FIELD_DELIMITER_LEVEL1 + cell.getEnteredValue() +
                StorageDelimiter.FIELD_DELIMITER_LEVEL1;
        for (int possible : cell.getPossibles()) {
            storageString += possible + StorageDelimiter.FIELD_DELIMITER_LEVEL2;
        }
        storageString += StorageDelimiter.FIELD_DELIMITER_LEVEL1 + Boolean.toString(
                cell.hasInvalidValueHighlight()) + StorageDelimiter.FIELD_DELIMITER_LEVEL1 + Boolean.toString(
                cell.isRevealed()) + StorageDelimiter.FIELD_DELIMITER_LEVEL1 + Boolean.toString(
                cell.isSelected());

        return storageString;
    }
}
