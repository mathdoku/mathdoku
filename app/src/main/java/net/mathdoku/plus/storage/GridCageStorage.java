package net.mathdoku.plus.storage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * This class converts relevant GridCage data to a string which can be persisted
 * and vice versa.
 */
public class GridCageStorage {
	/*
	 * Each line in the entire storage string of a Grid contains information
	 * about the type of data stored on the line. Lines containing data for a
	 * Grid Cage starts with following identifier.
	 */
	private static final String SAVE_GAME_CAGE_LINE = "CAGE";

	private int mId;
	private CageOperator mCageOperator;
	private int mResult;
	private ArrayList<GridCell> mCells;
	private boolean mHideOperator;

	/**
	 * Read cage information from a storage string which was created with
	 * {@link #toStorageString(net.mathdoku.plus.grid.GridCage)} before.
	 * 
	 * @param line
	 *            The line containing the cage information.
	 * @param savedWithRevisionNumber
	 *            The revision number of the app which was used to created the
	 *            storage string.
	 * @param gridCells
	 *            The cells to which the cell id's in the storage string refer.
	 * @return True in case the given line contains cage information and is
	 *         processed correctly. False otherwise.
	 */
	public boolean fromStorageString(String line, int savedWithRevisionNumber,
			ArrayList<GridCell> gridCells) {
		if (line == null) {
			throw new NullPointerException("Parameter line cannot be null");
		}

		// When upgrading to MathDoku v2 the history is not converted. As of
		// revision 369 all logic for handling games stored with older versions
		// is removed.
		if (savedWithRevisionNumber <= 368) {
			return false;
		}

		String[] cageParts = line
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1);

		int expectedNumberOfElements = 6;
		if (cageParts.length != expectedNumberOfElements) {
			throw new InvalidParameterException(
					"Wrong number of elements in storage string. Got "
							+ cageParts.length + ", expected "
							+ expectedNumberOfElements + ".");
		}

		// Only process the storage string if it starts with the correct
		// identifier.
		if (cageParts[0].equals(SAVE_GAME_CAGE_LINE) == false) {
			return false;
		}

		// Process all parts
		int index = 1;
		mId = Integer.parseInt(cageParts[index++]);
		mCageOperator = CageOperator.fromId(cageParts[index++]);
		mResult = Integer.parseInt(cageParts[index++]);
		mCells = new ArrayList<GridCell>();
		if (!cageParts[index].equals("")) {
			for (String cellId : cageParts[index]
					.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
				GridCell gridCell = gridCells.get(Integer.parseInt(cellId));
				gridCell.setCageId(mId);
				mCells.add(gridCell);
			}
		}
		index++;
		// noinspection UnusedAssignment
		mHideOperator = Boolean.parseBoolean(cageParts[index++]);

		return true;
	}

	/**
	 * Creates a string representation of the given Grid Cage which can be
	 * persisted. Use
	 * {@link #fromStorageString(String, int, java.util.ArrayList)} to parse the
	 * storage string.
	 * 
	 * @param gridCage
	 *            The grid cage which has to be converted to a storage string.
	 * 
	 * @return A string representation of the grid cage.
	 */
	public String toStorageString(GridCage gridCage) {
		mId = gridCage.getId();
		mCageOperator = gridCage.getOperator();
		mResult = gridCage.getResult();
		mCells = gridCage.getCells();
		mHideOperator = gridCage.isOperatorHidden();

		String storageString = SAVE_GAME_CAGE_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + mId
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mCageOperator.getId()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mResult
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		if (mCells != null) {
			for (GridCell cell : mCells) {
				storageString += cell.getCellId()
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
			}
		}
		storageString += SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(mHideOperator);

		return storageString;
	}

	public int getId() {
		return mId;
	}

	public CageOperator getCageOperator() {
		return mCageOperator;
	}

	public int getResult() {
		return mResult;
	}

	public ArrayList<GridCell> getCells() {
		return mCells;
	}

	public boolean isHideOperator() {
		return mHideOperator;
	}
}
