package net.mathdoku.plus.storage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * This class converts relevant Cage data to a string which can be persisted and
 * vice versa.
 */
public class CageStorage {
	/*
	 * Each line in the entire storage string of a Grid contains information
	 * about the type of data stored on the line. Lines containing data for a
	 * Grid Cage starts with following identifier.
	 */
	private static final String CAGE_LINE_ID = "CAGE";

	/**
	 * Read cage information from a storage string which was created with
	 * {@link #toStorageString(net.mathdoku.plus.puzzle.cage.Cage)} before.
	 * 
	 * @param line
	 *            The line containing the cage information.
	 * @param savedWithRevisionNumber
	 *            The revision number of the app which was used to created the
	 *            storage string.
	 * @param cells
	 *            The cells to which the cell id's in the storage string refer.
	 * @return True in case the given line contains cage information and is
	 *         processed correctly. False otherwise.
	 */
	public CageBuilder getCageBuilderFromStorageString(String line,
			int savedWithRevisionNumber, List<Cell> cells) {
		if (line == null) {
			throw new NullPointerException("Parameter line cannot be null");
		}

		// When upgrading to MathDoku v2 the history is not converted. As of
		// revision 369 all logic for handling games stored with older versions
		// is removed.
		if (savedWithRevisionNumber <= 368) {
			return null;
		}

		String[] cageParts = line
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1);

		// Only process the storage string if it starts with the correct
		// identifier.
		if (cageParts == null || CAGE_LINE_ID.equals(cageParts[0]) == false) {
			return null;
		}

		int expectedNumberOfElements = 6;
		if (cageParts.length != expectedNumberOfElements) {
			throw new InvalidParameterException(
					"Wrong number of elements in cage storage string. Got "
							+ cageParts.length + ", expected "
							+ expectedNumberOfElements + ".");
		}

		CageBuilder cageBuilder = new CageBuilder();

		// Process all parts
		int index = 1;
		int cageId = Integer.parseInt(cageParts[index++]);
		cageBuilder.setId(cageId);
		cageBuilder.setCageOperator(CageOperator.fromId(cageParts[index++]));
		cageBuilder.setResult(Integer.parseInt(cageParts[index++]));

		int[] mCells = null;
		if (!cageParts[index].equals("")) {
			String[] cellParts = cageParts[index]
					.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2);
			mCells = new int[cellParts.length];
			for (int i = 0; i < cellParts.length; i++) {
				int cellId = Integer.parseInt(cellParts[i]);
				Cell cell = cells.get(cellId);
				cell.setCageId(cageId);
				mCells[i] = cellId;
			}
		}
		index++;
		cageBuilder.setCells(mCells);

		// noinspection UnusedAssignment
		cageBuilder.setHideOperator(Boolean.parseBoolean(cageParts[index++]));

		return cageBuilder;
	}

	/**
	 * Creates a string representation of the given Grid Cage which can be
	 * persisted. Use
	 * {@link #getCageBuilderFromStorageString(String, int, java.util.List)} to
	 * parse the storage string.
	 * 
	 * @param cage
	 *            The grid cage which has to be converted to a storage string.
	 * 
	 * @return A string representation of the grid cage.
	 */
	public String toStorageString(Cage cage) {
		String storageString = CAGE_LINE_ID
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ cage.getId()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ cage.getOperator().getId()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ cage.getResult()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		List<Cell> cells = cage.getListOfCells();
		if (cells != null) {
			for (Cell cell : cells) {
				storageString += cell.getCellId()
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
			}
		}
		storageString += SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(cage.isOperatorHidden());

		return storageString;
	}
}
