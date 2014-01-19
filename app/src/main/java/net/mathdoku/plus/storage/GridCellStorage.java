package net.mathdoku.plus.storage;

import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * This class converts relevant GridCell data to a string which can be persisted
 * and vice versa.
 */
public class GridCellStorage {
	private static final String TAG = "MathDoku.GridCellStorage";

	/*
	 * Each line in the entire storage string of a Grid contains information
	 * about the type of data stored on the line. Lines containing data for a
	 * Grid Cell starts with following identifier.
	 */
	private static final String SAVE_GAME_CELL_LINE = "CELL";

	private int mId;
	private int mRow;
	private int mColumn;
	private String mCageText;
	private int mCorrectValue;
	private int mUserValue;
	private ArrayList<Integer> mPossibles;
	private boolean mInvalidUserValueHighlight;
	private boolean mRevealed;
	private boolean mSelected;

	/**
	 * Read cell information from a storage string which was created with
	 * {@link #toStorageString(net.mathdoku.plus.grid.GridCell)} before.
	 * 
	 * @param line
	 *            The line containing the cell information.
	 * @return True in case the given line contains cell information and is
	 *         processed correctly. False otherwise.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean fromStorageString(String line, int savedWithRevisionNumber) {
		if (line == null) {
			throw new NullPointerException("Parameter line cannot be null");
		}

		// When upgrading to MathDoku v2 the history is not converted. As of
		// revision 369 all logic for handling games stored with older versions
		// is removed.
		if (savedWithRevisionNumber <= 368) {
			return false;
		}

		String[] cellParts = line
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1);

		int expectedNumberOfElements = 11;
		if (cellParts.length != expectedNumberOfElements) {
			throw new InvalidParameterException(
					"Wrong number of elements in storage string. Got "
							+ cellParts.length + ", expected "
							+ expectedNumberOfElements + ".");
		}

		// Only process the storage string if it starts with the correct
		// identifier.
		if (cellParts[0].equals(SAVE_GAME_CELL_LINE) == false) {
			return false;
		}

		// Process all parts
		int index = 1;
		mId = Integer.parseInt(cellParts[index++]);
		mRow = Integer.parseInt(cellParts[index++]);
		mColumn = Integer.parseInt(cellParts[index++]);
		mCageText = cellParts[index++];
		mCorrectValue = Integer.parseInt(cellParts[index++]);
		mUserValue = Integer.parseInt(cellParts[index++]);

		// Get possible values
		mPossibles = new ArrayList<Integer>();
		if (!cellParts[index].equals("")) {
			for (String possible : cellParts[index]
					.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
				mPossibles.add(Integer.parseInt(possible));
			}
		}
		index++;

		mInvalidUserValueHighlight = Boolean.parseBoolean(cellParts[index++]);
		mRevealed = Boolean.parseBoolean(cellParts[index++]);
		// noinspection UnusedAssignment
		mSelected = Boolean.parseBoolean(cellParts[index++]);

		return true;
	}

	/**
	 * Create a string representation of the Grid Cell which can be used to
	 * store a grid cell in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString(GridCell gridCell) {
		mId = gridCell.getCellId();
		mRow = gridCell.getRow();
		mColumn = gridCell.getColumn();
		mCageText = gridCell.getCageText();
		mCorrectValue = gridCell.getCorrectValue();
		mUserValue = gridCell.getUserValue();
		mPossibles = gridCell.getPossibles();
		mInvalidUserValueHighlight = gridCell.hasInvalidUserValueHighlight();
		mRevealed = gridCell.isRevealed();
		mSelected = gridCell.isSelected();

		String storageString = SAVE_GAME_CELL_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + mId
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + mRow
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mColumn
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mCageText
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mCorrectValue
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mUserValue
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		for (int possible : mPossibles) {
			storageString += possible
					+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
		}
		storageString += SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(mInvalidUserValueHighlight)
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(mRevealed)
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(mSelected);

		return storageString;
	}

	public int getId() {
		return mId;
	}

	public int getRow() {
		return mRow;
	}

	public int getColumn() {
		return mColumn;
	}

	public String getCageText() {
		return mCageText;
	}

	public int getCorrectValue() {
		return mCorrectValue;
	}

	public int getUserValue() {
		return mUserValue;
	}

	public ArrayList<Integer> getPossibles() {
		return mPossibles;
	}

	public boolean isInvalidUserValueHighlight() {
		return mInvalidUserValueHighlight;
	}

	public boolean isRevealed() {
		return mRevealed;
	}

	public boolean isSelected() {
		return mSelected;
	}
}
