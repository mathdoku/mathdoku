package net.mathdoku.plus.storage;

import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class converts relevant Cell data to a string which can be persisted and
 * vice versa.
 */
public class CellStorage {
	private static final String TAG = "MathDoku.CellStorage";

	/*
	 * Each line in the entire storage string of a Grid contains information
	 * about the type of data stored on the line. Lines containing data for a
	 * Grid Cell starts with following identifier.
	 */
	private static final String SAVE_GAME_CELL_LINE = "CELL";

	/**
	 * Read cell information from a storage string which was created with
	 * {@link #toStorageString(net.mathdoku.plus.puzzle.cell.Cell)} before.
	 * 
	 * @param line
	 *            The line containing the cell information.
	 * @return True in case the given line contains cell information and is
	 *         processed correctly. False otherwise.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public CellBuilder getCellBuilderFromStorageString(String line,
			int savedWithRevisionNumber) {
		if (line == null) {
			throw new NullPointerException("Parameter line cannot be null");
		}

		// When upgrading to MathDoku v2 the history is not converted. As of
		// revision 369 all logic for handling games stored with older versions
		// is removed.
		if (savedWithRevisionNumber <= 368) {
			return null;
		}

		String[] cellParts = line
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1);

		// Only process the storage string if it starts with the correct
		// identifier.
		if (cellParts == null
				|| !SAVE_GAME_CELL_LINE.equals(cellParts[0])) {
			return null;
		}

		int expectedNumberOfElements = savedWithRevisionNumber <= 596 ? 11 : 9;
		if (cellParts.length != expectedNumberOfElements) {
			throw new InvalidParameterException(
					"Wrong number of elements in cell storage string. Got "
							+ cellParts.length + ", expected "
							+ expectedNumberOfElements + ".");
		}

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
		cellBuilder.setUserValue(Integer.parseInt(cellParts[index++]));

		// Get possible values
		List<Integer> possibles = new ArrayList<Integer>();
		if (!cellParts[index].equals("")) {
			for (String possible : cellParts[index]
					.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
				possibles.add(Integer.parseInt(possible));
			}
		}
		index++;
		cellBuilder.setPossibles(possibles);

		cellBuilder.setInvalidUserValueHighlight(Boolean
				.parseBoolean(cellParts[index++]));
		cellBuilder.setRevealed(Boolean.parseBoolean(cellParts[index++]));
		cellBuilder.setSelected(Boolean.parseBoolean(cellParts[index++]));

		return cellBuilder;
	}

	/**
	 * Create a string representation of the Grid Cell which can be used to
	 * store a grid cell in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString(Cell cell) {
		String storageString = SAVE_GAME_CELL_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ cell.getCellId()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ cell.getCageText()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ cell.getCorrectValue()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ cell.getUserValue()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		for (int possible : cell.getPossibles()) {
			storageString += possible
					+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
		}
		storageString += SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(cell.hasInvalidUserValueHighlight())
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(cell.isRevealed())
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(cell.isSelected());

		return storageString;
	}
}
