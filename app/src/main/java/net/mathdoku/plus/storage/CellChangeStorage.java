package net.mathdoku.plus.storage;

import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cellchange.CellChange;
import net.mathdoku.plus.storage.databaseadapter.database.SolvingAttemptDatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class converts relevant CellChange data to a string which can be
 * persisted and vice versa.
 */
public class CellChangeStorage {
	@SuppressWarnings("unused")
	private static final String TAG = CellChangeStorage.class.getName();

	// Remove "&& false" in following line to show debug information about
	// reading information from a storage string
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_STORAGE_STRING = Config.mAppMode == Config.AppMode.DEVELOPMENT && false;

	private Cell mCell;
	private Integer mPreviousEnteredValue;
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

		CellChangeStoragePatternMatcher cellChangeStoragePatternMatcher = new CellChangeStoragePatternMatcher();
		if (!cellChangeStoragePatternMatcher.matchesOuter(line)) {
			// Line does not match the pattern of a line including the object
			// type identifier
			return false;
		}

		if (DEBUG_STORAGE_STRING) {
			Log
					.d(TAG,
							"---------------------------------------------------------------------------");
			Log.d(TAG, "Line: " + line);
			cellChangeStoragePatternMatcher.debugLogOuter();
		}

		// Recursively process the content of the cell change
		return fromStorageStringRecursive(
				cellChangeStoragePatternMatcher.getDataOuter(), 1, cells);
	}

	/**
	 * Read cell information from or a storage string which was created with @
	 * Cell#getId()} before.
	 * 
	 * @param line
	 *            The line containing the cell information.
	 * @param level
	 *            The level of recursion. The top level should start at 1.
	 * @param cells
	 *            The list of cells to which undo information can be related.
	 * @return True in case the given line contains cell information and is
	 *         processed correctly. False otherwise.
	 */
	private boolean fromStorageStringRecursive(String line, int level,
			List<Cell> cells) {
		if (DEBUG_STORAGE_STRING) {
			String indent = getDebugLogIndent(level);
			Log.i(TAG, indent + "Line: " + line);
		}

		CellChangeStoragePatternMatcher cellChangeStoragePatternMatcher = new CellChangeStoragePatternMatcher();
		if (!cellChangeStoragePatternMatcher.matchesInner(line)) {
			if (DEBUG_STORAGE_STRING) {
				String indent = getDebugLogIndent(level);
				Log.i(TAG, indent
						+ "Can not process this line. Format is invalid");
			}
			return false;
		}

		if (DEBUG_STORAGE_STRING) {
			String indent = getDebugLogIndent(level);
			cellChangeStoragePatternMatcher.debugLogInner(indent);
		}

		mCell = cells.get(cellChangeStoragePatternMatcher.getCellNumber());
		mPreviousEnteredValue = cellChangeStoragePatternMatcher
				.getPreviousEnteredValue();
		mPreviousPossibleValues = cellChangeStoragePatternMatcher
				.getPreviousPossibleValues();

		for (String relatedCellChangeString : cellChangeStoragePatternMatcher
				.getRelatedCellChanges()) {
			if (!relatedCellChangeString.equals("")) {
				CellChangeStorage relatedCellChangeStorage = new CellChangeStorage();
				if (!relatedCellChangeStorage.fromStorageStringRecursive(
						relatedCellChangeString, level + 1, cells)) {
					return false;
				}
				CellChange relatedCellChange = new CellChange(
						relatedCellChangeStorage);
				if (mRelatedCellChanges == null) {
					mRelatedCellChanges = new ArrayList<CellChange>();
				}
				mRelatedCellChanges.add(relatedCellChange);
			}
		}

		return true;
	}

	private String getDebugLogIndent(int level) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < level; i++) {
			stringBuilder.append("..");
		}
		return stringBuilder.toString();
	}

	/**
	 * Create a string representation of the Cell Change which can be used to
	 * store a Cell Change in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString(CellChange cellChange) {
		return CellChangeStoragePatternMatcher.SAVE_GAME_CELL_CHANGE_LINE
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
		mPreviousEnteredValue = rootCellChange.getPreviousEnteredValue();
		mPreviousPossibleValues = rootCellChange.getPreviousPossibleValues();
		mRelatedCellChanges = rootCellChange.getRelatedCellChanges();

		String storageString = "[" + mCell.getCellId()
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mPreviousEnteredValue
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

	public Integer getPreviousEnteredValue() {
		return mPreviousEnteredValue;
	}

	public List<Integer> getPreviousPossibleValues() {
		return mPreviousPossibleValues;
	}

	public List<CellChange> getRelatedCellChanges() {
		return mRelatedCellChanges;
	}
}
