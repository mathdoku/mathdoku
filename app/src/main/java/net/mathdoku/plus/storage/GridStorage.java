package net.mathdoku.plus.storage;

import net.mathdoku.plus.grid.CellChange;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class GridStorage {
	private static final String TAG = "MathDoku.GridStorage";

	// Each line in the GridFile which contains information about the grid
	// starts with an identifier. This identifier consists of a generic part and
	// the package revision number.
	public static final String SAVE_GAME_GRID_LINE = "GRID";

	private boolean mActive;
	private boolean mRevealed;
	private ArrayList<GridCell> mCells;
	private ArrayList<GridCage> mCages;
	private ArrayList<CellChange> mCellChanges;

	private GridStorageObjectsCreator mGridStorageObjectsCreator;

	public GridStorage() {
		mGridStorageObjectsCreator = new GridStorageObjectsCreator();
	}

	public void setObjectsCreator(
			GridStorageObjectsCreator gridStorageObjectsCreator) {
		if (gridStorageObjectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter gridStorageObjectsCreator can not be null.");
		}
		mGridStorageObjectsCreator = gridStorageObjectsCreator;
	}

	/**
	 * Read view information from or a storage string which was created with @
	 * GridView#toStorageString()} before.
	 * 
	 * @param line
	 *            The line containing the view information.
	 * @return True in case the given line contains view information and is
	 *         processed correctly. False otherwise.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean fromStorageString(String line, int savedWithRevisionNumber) {
		if (line == null) {
			throw new NullPointerException("Parameter line cannot be null");
		}

		String[] viewParts = line
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1);

		int expectedNumberOfElements = (savedWithRevisionNumber <= 595 ? 4 : 3);
		if (viewParts.length != expectedNumberOfElements) {
			throw new InvalidParameterException(
					"Wrong number of elements in storage string");
		}

		// Only process the storage string if it starts with the correct
		// identifier.
		if (viewParts[0].equals(SAVE_GAME_GRID_LINE) == false) {
			return false;
		}

		// When upgrading to MathDoku v2 the history is not converted. As of
		// revision 369 all logic for handling games stored with older versions
		// is removed.
		if (savedWithRevisionNumber <= 368) {
			return false;
		}

		// Process all parts
		int index = 1;
		mActive = Boolean.parseBoolean(viewParts[index++]);
		mRevealed = Boolean.parseBoolean(viewParts[index++]);
		if (savedWithRevisionNumber <= 595) {
			// This field is not use starting from version 596.
			index++;
		}

		mCells = null;
		mCages = null;
		mCellChanges = null;

		return true;
	}

	/**
	 * Create a string representation of the Grid which can be used to store a
	 * grid.
	 * 
	 * @return A string representation of the grid.
	 */
	public String toStorageString(Grid grid) {
		mActive = grid.isActive();
		mRevealed = grid.isSolutionRevealed();
		mCells = grid.mCells;
		mCages = grid.mCages;
		mCellChanges = grid.getCellChanges();

		StringBuilder stringBuilder = new StringBuilder(256);

		// First store data for the grid object itself.
		stringBuilder.append(SAVE_GAME_GRID_LINE)
				.append(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)
				.append(mActive)
				.append(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)
				.append(mRevealed)
				.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);

		// Store information about the cells. Use one line per single
		// cell.
		for (GridCell cell : mCells) {
			stringBuilder.append(cell.toStorageString()).append(
					SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		// Store information about the cages. Use one line per single
		// cage.
		if (mCages != null) {
			GridCageStorage gridCageStorage = mGridStorageObjectsCreator
					.createGridCageStorage();
			for (GridCage cage : mCages) {
				stringBuilder.append(gridCageStorage.toStorageString(cage))
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
		}

		// Store information about the cell changes. Use one line per single
		// cell change. Note: watch for lengthy line due to recursive cell
		// changes.
		for (CellChange cellChange : mCellChanges) {
			stringBuilder.append(cellChange.toStorageString()).append(
					SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		return stringBuilder.toString();
	}

	public boolean isActive() {
		return mActive;
	}

	public boolean isRevealed() {
		return mRevealed;
	}
}
