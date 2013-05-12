package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import android.util.Log;

/**
 * The CellChange holds undo information for a GridCell.
 */
public class CellChange {
	private static final String TAG = "MathDoku.CellChange";

	// Remove "&& false" in following line to show debug information about
	// reading information from a storage string
	public static final boolean DEBUG_STORAGE_STRING = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	// Base identifier for different versions of cell information which is
	// stored in
	// saved game. The base should be extended with a integer value
	private final String SAVE_GAME_CELL_CHANGE_LINE = "CELL_CHANGE";

	// The cell for which the undo information is stored.
	private GridCell mGridCell;

	// Properties of the GridCell which can be restored.
	private int mPreviousUserValue;
	private ArrayList<Integer> mPreviousPossibleValues;

	// Undo information for other cell which are changed as a result of changing
	// the cell.
	private ArrayList<CellChange> mRelatedCellChanges;

	/**
	 * Creates a new empty [@link #CellChange] instance.
	 */
	public CellChange() {
		this.mGridCell = null;
		this.mPreviousUserValue = -1;
		this.mPreviousPossibleValues = new ArrayList<Integer>();
		this.mRelatedCellChanges = null;
	}

	/**
	 * Creates a new [@link #CellChange] instance.
	 * 
	 * @param cell
	 *            The cell to which the undo information is related.
	 * @param previousUserValue
	 *            The user value of the cell before it is changed.
	 * @param previousPossibleValues
	 *            The possible values of the cell before it is changed.
	 */
	public CellChange(GridCell cell, int previousUserValue,
			ArrayList<Integer> previousPossibleValues) {
		this.mGridCell = cell;
		this.mPreviousUserValue = previousUserValue;
		this.mPreviousPossibleValues = new ArrayList<Integer>(
				previousPossibleValues);
		this.mRelatedCellChanges = null;
	}

	/**
	 * Restores a GridCell using the undo information.
	 * 
	 * @return The grid cell for which a change was made undone.
	 */
	public GridCell restore() {
		if (this.mRelatedCellChanges != null) {
			// First Undo all related moves.
			for (CellChange relatedMove : this.mRelatedCellChanges) {
				relatedMove.restore();
			}
		}
		mGridCell.undo(this.mPreviousUserValue, this.mPreviousPossibleValues);

		return mGridCell;
	}

	/**
	 * Relates cell changes which belong together. In case a cell change is
	 * restored, all its related cell changes will be restored as well.
	 * 
	 * @param relatedCellChange
	 *            The cell change which will be related to this cell change.
	 */
	public void addRelatedMove(CellChange relatedCellChange) {
		if (this.mRelatedCellChanges == null) {
			this.mRelatedCellChanges = new ArrayList<CellChange>();
		}
		this.mRelatedCellChanges.add(relatedCellChange);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = "<cell:" + this.mGridCell.getCellNumber() + " col:"
				+ this.mGridCell.getColumn() + " row:" + this.mGridCell.getRow()
				+ " previous userval:" + this.mPreviousUserValue
				+ " previous possible values:"
				+ mPreviousPossibleValues.toString() + ">";
		return str;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		// Return true if the objects are identical.
		// (This is just an optimization, not required for correctness.)
		if (this == o) {
			return true;
		}

		// Return false if the other object has the wrong type.
		// This type may be an interface depending on the interface's
		// specification.
		if (!(o instanceof CellChange)) {
			return false;
		}

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access
		// private fields.
		CellChange lhs = (CellChange) o;

		// Check each field. Primitive fields, reference fields, and nullable
		// reference
		// fields are all treated differently.
		return mPreviousUserValue == lhs.mPreviousUserValue
				&& mGridCell.equals(lhs.mGridCell)
				&& (mPreviousPossibleValues == null ? lhs.mPreviousPossibleValues == null
						: mPreviousPossibleValues
								.equals(lhs.mPreviousPossibleValues));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Create a string representation of the Cell Change which can be used to
	 * store a Cell Change in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString() {
		return SAVE_GAME_CELL_CHANGE_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ this.toStorageStringRecursive();
	}

	/**
	 * Create a string representation (recursive) of the Cell Change which can
	 * be used to store a Cell Change in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	private String toStorageStringRecursive() {
		String storageString = "[" + mGridCell.getCellNumber()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + mPreviousUserValue
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		for (int previousPossibleValue : mPreviousPossibleValues) {
			storageString += Integer.toString(previousPossibleValue)
					+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
		}
		storageString += SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		if (mRelatedCellChanges != null) {
			for (CellChange cellChange : mRelatedCellChanges) {
				storageString += cellChange.toStorageStringRecursive()
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
			}
		}
		storageString += "]";

		return storageString;
	}

	/**
	 * Read cell information from or a storage string which was created with @
	 * GridCell#toStorageString()} before.
	 * 
	 * @param line
	 *            The line containing the cell information.
	 * @param cells
	 *            The list of cells to which undo information can be related.
	 * @return True in case the given line contains cell information and is
	 *         processed correctly. False otherwise.
	 */
	public boolean fromStorageString(String line, ArrayList<GridCell> cells, int savedWithRevisionNumber) {
		final String CELL_CHANGE_LINE_REGEXP = "^"
				+ SAVE_GAME_CELL_CHANGE_LINE + "(\\.v\\d+)?"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + "(.*)$";
		final int GROUP_VERSION_NUMBER = 1;
		final int GROUP_CELL_CHANGE = 2;

		Pattern pattern = Pattern.compile(CELL_CHANGE_LINE_REGEXP);
		Matcher matcher = pattern.matcher(line);
		if (!matcher.matches()) {
			// Line does not match the pattern of a line including the object
			// type identifier
			return false;
		}

		if (DEBUG_STORAGE_STRING) {
			Log.i(TAG,
					"---------------------------------------------------------------------------");
			Log.i(TAG, "Line: " + line);
			Log.i(TAG, "Start index: " + matcher.start() + " End index: "
					+ matcher.end() + " #groups: " + matcher.groupCount());
			Log.i(TAG, "Cell change: " + matcher.group(GROUP_CELL_CHANGE));
		}
		
		int revisionNumber = (savedWithRevisionNumber > 0 ? savedWithRevisionNumber :  Integer.valueOf(matcher
				.group(GROUP_VERSION_NUMBER).substring(2)));

		// Recursively process the content of the cell change
		return fromStorageStringRecursive(revisionNumber,
				matcher.group(GROUP_CELL_CHANGE), 1, cells);
	}

	/**
	 * Read cell information from or a storage string which was created with @
	 * GridCell#toStorageString()} before.
	 * 
	 * @param revisionNumber
	 *            The version of the cell change information.
	 * @param line
	 *            The line containing the cell information.
	 * @param level
	 *            The level of recursion. The top level should start at 1.
	 * @param cells
	 *            The list of cells to which undo information can be related.
	 * @return True in case the given line contains cell information and is
	 *         processed correctly. False otherwise.
	 */
	private boolean fromStorageStringRecursive(
			int revisionNumber, String line, int level,
			ArrayList<GridCell> cells) {
		// Regexp and groups inside. Groups 4 - 6 are helper groups which are
		// needed to ensure the validity of the cell information but are not
		// used programmaticly.
		final String CELL_CHANGE_REGEXP = "^\\[(\\d+)"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + "(\\d*)"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + "((\\d*)|((\\d*"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2 + ")+))"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + "(.*)\\]$";
		final int GROUP_CELL_NUMBER = 1;
		final int GROUP_PREVIOUS_USER_VALUE = 2;
		final int GROUP_PREVIOUS_POSSIBLE_VALUES = 3;
		final int GROUP_RELATED_CELL_CHANGED = 7;

		String indent = "";
		if (DEBUG_STORAGE_STRING) {
			for (int i = 0; i < level; i++) {
				indent += "..";
			}
			Log.i(TAG,
					indent
							+ "---------------------------------------------------------------------------");
			Log.i(TAG, indent + "Line: " + line);
		}

		Pattern pattern = Pattern.compile(CELL_CHANGE_REGEXP);
		Matcher matcher = pattern.matcher(line);
		if (!matcher.matches()) {
			if (DEBUG_STORAGE_STRING) {
				// Line does not match the pattern for a cell change
				Log.i(TAG, indent
						+ "Can not process this line. Format is invalid");
			}
			return false;
		}

		if (DEBUG_STORAGE_STRING) {
			Log.i(TAG,
					indent + "Number of groups found: " + matcher.groupCount());
			Log.i(TAG,
					indent + "Cell number: " + matcher.group(GROUP_CELL_NUMBER));
			Log.i(TAG,
					indent + "Previuous user value: "
							+ matcher.group(GROUP_PREVIOUS_USER_VALUE));
			Log.i(TAG,
					indent + "Previous possible values: "
							+ matcher.group(GROUP_PREVIOUS_POSSIBLE_VALUES));
			Log.i(TAG,
					indent + "Related cell changes: "
							+ matcher.group(GROUP_RELATED_CELL_CHANGED));
		}

		this.mGridCell = cells
				.get(Integer.valueOf(matcher.group(GROUP_CELL_NUMBER)));
		mPreviousUserValue = Integer.valueOf(matcher
				.group(GROUP_PREVIOUS_USER_VALUE));
		if (!matcher.group(GROUP_PREVIOUS_POSSIBLE_VALUES).equals("")) {
			for (String possible : matcher
					.group(GROUP_PREVIOUS_POSSIBLE_VALUES).split(
							SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
				mPreviousPossibleValues.add(Integer.valueOf(possible));
			}
		}

		// The related cell changes can not be matched using a regular
		// expression because they recursively can contain other related
		// cell changes.
		String relatedCellChanges = matcher.group(GROUP_RELATED_CELL_CHANGED);
		if (!relatedCellChanges.equals("")) {
			char[] charArray = relatedCellChanges.toCharArray();
			int levelNestedGroup = 0;
			int startPosGroup = 0;
			int index = 0;
			for (char c : charArray) {
				switch (c) {
				case '[': // Start of new groupd
					if (levelNestedGroup == 0) {
						// Remember starting position of outer group only
						startPosGroup = index;
					}
					levelNestedGroup++;
					break;
				case ']':
					levelNestedGroup--;
					if (levelNestedGroup == 0) {
						// Just completed a group.
						String group = relatedCellChanges.substring(
								startPosGroup, index + 1);
						CellChange relatedCellChange = new CellChange();
						if (!relatedCellChange.fromStorageStringRecursive(
								revisionNumber, group, level + 1,
								cells)) {
							return false;
						}
						addRelatedMove(relatedCellChange);
					}
					break;
				default:
					if (levelNestedGroup == 0
							&& !Character.toString(c).equals(
									SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
						Log.i(TAG, indent + "Unexpected character '" + c
								+ "'at position " + index);
						return false;
					}
				}
				index++;
			}
		}

		return true;
	}
}