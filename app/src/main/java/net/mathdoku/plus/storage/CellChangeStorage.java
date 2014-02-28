package net.mathdoku.plus.storage;

import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cellchange.CellChange;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class converts relevant CellChange data to a string which can be
 * persisted and vice versa.
 */
public class CellChangeStorage {
	private static final String TAG = "MathDoku.CellChangeStorage";

	// Remove "&& false" in following line to show debug information about
	// reading information from a storage string
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_STORAGE_STRING = (Config.mAppMode == Config.AppMode.DEVELOPMENT) && false;

	/*
	 * Each line in the entire storage string of a Grid contains information
	 * about the type of data stored on the line. Lines containing data for a
	 * Cell Change starts with following identifier.
	 */
	private final String SAVE_GAME_CELL_CHANGE_LINE = "CELL_CHANGE";

	private Cell mCell;
	private Integer mPreviousUserValue;
	private List<Integer> mPreviousPossibleValues;
	private List<CellChange> mRelatedCellChanges;

	/**
	 * Read cell information from or a storage string which was created with @
	 * Cell#getId()} before.
	 * 
	 * @param line
	 *            The line containing the cell information.
	 * @param cells
	 *            The list of cells to which undo information can be related.
	 * @return True in case the given line contains cell information and is
	 *         processed correctly. False otherwise.
	 */
	public boolean fromStorageString(String line, List<Cell> cells,
			int savedWithRevisionNumber) {
		if (line == null) {
			throw new NullPointerException("Parameter line cannot be null");
		}

		// When upgrading to MathDoku v2 the history is not converted. As of
		// revision 369 all logic for handling games stored with older versions
		// is removed.
		if (savedWithRevisionNumber <= 368) {
			return false;
		}

		final String CELL_CHANGE_LINE_REGEXP = "^" + SAVE_GAME_CELL_CHANGE_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ "(.*)$";
		final int GROUP_CELL_CHANGE = 1;

		Pattern pattern = Pattern.compile(CELL_CHANGE_LINE_REGEXP);
		Matcher matcher = pattern.matcher(line);
		if (!matcher.matches()) {
			// Line does not match the pattern of a line including the object
			// type identifier
			return false;
		}

		if (DEBUG_STORAGE_STRING) {
			Log
					.i(TAG,
							"---------------------------------------------------------------------------");
			Log.i(TAG, "Line: " + line);
			Log.i(TAG, "Start index: " + matcher.start() + " End index: "
					+ matcher.end() + " #groups: " + matcher.groupCount());
			Log.i(TAG, "Cell change: " + matcher.group(GROUP_CELL_CHANGE));
		}

		// Recursively process the content of the cell change
		return fromStorageStringRecursive(savedWithRevisionNumber,
				matcher.group(GROUP_CELL_CHANGE), 1, cells);
	}

	/**
	 * Read cell information from or a storage string which was created with @
	 * Cell#getId()} before.
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
	private boolean fromStorageStringRecursive(int revisionNumber, String line,
			int level, List<Cell> cells) {
		// Regexp and groups inside. Groups 4 - 6 are helper groups which are
		// needed to ensure the validity of the cell information but are not
		// used programmatic.
		final String CELL_CHANGE_REGEXP = "^\\[(\\d+)"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ "(\\d*)"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ "((\\d*)|((\\d*"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2 + ")+))"
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ "(.*)\\]$";
		final int GROUP_CELL_NUMBER = 1;
		final int GROUP_PREVIOUS_USER_VALUE = 2;
		final int GROUP_PREVIOUS_POSSIBLE_VALUES = 3;
		final int GROUP_RELATED_CELL_CHANGED = 7;

		String indent = "";
		if (DEBUG_STORAGE_STRING) {
			for (int i = 0; i < level; i++) {
				indent += "..";
			}
			Log
					.i(TAG,
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
			Log
					.i(TAG,
							indent + "Cell number: "
									+ matcher.group(GROUP_CELL_NUMBER));
			Log.i(TAG,
					indent + "Previous user value: "
							+ matcher.group(GROUP_PREVIOUS_USER_VALUE));
			Log.i(TAG,
					indent + "Previous possible values: "
							+ matcher.group(GROUP_PREVIOUS_POSSIBLE_VALUES));
			Log.i(TAG,
					indent + "Related cell changes: "
							+ matcher.group(GROUP_RELATED_CELL_CHANGED));
		}

		mCell = cells.get(Integer.valueOf(matcher.group(GROUP_CELL_NUMBER)));
		mPreviousUserValue = Integer.valueOf(matcher
				.group(GROUP_PREVIOUS_USER_VALUE));
		mPreviousPossibleValues = new ArrayList<Integer>();
		if (!matcher.group(GROUP_PREVIOUS_POSSIBLE_VALUES).equals("")) {
			for (String possible : matcher
					.group(GROUP_PREVIOUS_POSSIBLE_VALUES)
					.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
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
				case '[': // Start of new group
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
						CellChangeStorage relatedCellChangeStorage = new CellChangeStorage();
						if (!relatedCellChangeStorage
								.fromStorageStringRecursive(revisionNumber,
										group, level + 1, cells)) {
							return false;
						}
						CellChange relatedCellChange = new CellChange(
								relatedCellChangeStorage);
						if (mRelatedCellChanges == null) {
							mRelatedCellChanges = new ArrayList<CellChange>();
						}
						mRelatedCellChanges.add(relatedCellChange);
					}
					break;
				default:
					if (levelNestedGroup == 0
							&& !Character
									.toString(c)
									.equals(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
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

	/**
	 * Create a string representation of the Cell Change which can be used to
	 * store a Cell Change in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString(CellChange cellChange) {
		return SAVE_GAME_CELL_CHANGE_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ toStorageStringRecursive(cellChange);
	}

	/**
	 * Create a string representation (recursive) of the Cell Change which can
	 * be used to store a Cell Change in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	private String toStorageStringRecursive(CellChange rootCellChange) {
		mCell = rootCellChange.getCell();
		mPreviousUserValue = rootCellChange.getPreviousUserValue();
		mPreviousPossibleValues = rootCellChange.getPreviousPossibleValues();
		mRelatedCellChanges = rootCellChange.getRelatedCellChanges();

		String storageString = "[" + mCell.getCellId()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mPreviousUserValue
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		for (int previousPossibleValue : mPreviousPossibleValues) {
			storageString += Integer.toString(previousPossibleValue)
					+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
		}
		storageString += SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		if (mRelatedCellChanges != null) {
			CellChangeStorage cellChangeStorage = new CellChangeStorage();
			for (CellChange relatedCellChange : mRelatedCellChanges) {
				storageString += cellChangeStorage
						.toStorageStringRecursive(relatedCellChange)
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
			}
		}
		storageString += "]";

		return storageString;
	}

	public Cell getCell() {
		return mCell;
	}

	public Integer getPreviousUserValue() {
		return mPreviousUserValue;
	}

	public List<Integer> getPreviousPossibleValues() {
		return mPreviousPossibleValues;
	}

	public List<CellChange> getRelatedCellChanges() {
		return mRelatedCellChanges;
	}
}
