package net.mathdoku.plus.storage;

import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cellchange.CellChange;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.security.InvalidParameterException;
import java.util.List;

public class GridStorage {
	private static final String TAG = GridStorage.class.getName();

	/*
	 * Each line in the entire storage string of a Grid contains information
	 * about the type of data stored on the line. The first line of this entire
	 * storage string contains general data of the grid and starts with
	 * following identifier.
	 */
	public static final String SAVE_GAME_GRID_LINE = "GRID";

	private boolean mActive;
	private boolean mRevealed;
	private List<Cell> mCells;
	private List<Cage> mCages;
	private List<CellChange> mCellChanges;

	// The Objects Creator is responsible for creating all new objects needed by
	// this class. For unit testing purposes the default create methods can be
	// overridden if needed.
	public static class ObjectsCreator {
		public CageStorage createCageStorage() {
			return new CageStorage();
		}

		public CellStorage createCellStorage() {
			return new CellStorage();
		}

		public CellChangeStorage createCellChangeStorage() {
			return new CellChangeStorage();
		}
	}

	private GridStorage.ObjectsCreator mObjectsCreator;

	public GridStorage() {
		mObjectsCreator = new GridStorage.ObjectsCreator();
	}

	public void setObjectsCreator(GridStorage.ObjectsCreator objectsCreator) {
		if (objectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter objectsCreator cannot be null.");
		}
		mObjectsCreator = objectsCreator;
	}

	/**
	 * Read view information from or a storage string which was created with @
	 * GridView#getId()} before.
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

		// Only process the storage string if it starts with the correct
		// identifier.
		if (viewParts == null
				|| !SAVE_GAME_GRID_LINE.equals(viewParts[0])) {
			return false;
		}

		int expectedNumberOfElements = (savedWithRevisionNumber <= 595 ? 4 : 3);
		if (viewParts.length != expectedNumberOfElements) {
			throw new InvalidParameterException(
					"Wrong number of elements in grid storage string");
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
		mCells = grid.getCells();
		mCages = grid.getCages();
		mCellChanges = grid.getCellChanges();

		StringBuilder stringBuilder = new StringBuilder(256);

		// First store data for the grid object itself.
		stringBuilder
				.append(SAVE_GAME_GRID_LINE)
				.append(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)
				.append(mActive)
				.append(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)
				.append(mRevealed)
				.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);

		// Store information about the cells. Use one line per single
		// cell.
		CellStorage cellStorage = mObjectsCreator.createCellStorage();
		for (Cell cell : mCells) {
			stringBuilder.append(cellStorage.toStorageString(cell)).append(
					SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		// Store information about the cages. Use one line per single
		// cage.
		if (mCages != null) {
			CageStorage cageStorage = mObjectsCreator.createCageStorage();
			for (Cage cage : mCages) {
				stringBuilder.append(cageStorage.toStorageString(cage)).append(
						SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
		}

		// Store information about the cell changes. Use one line per single
		// cell change. Note: watch for lengthy line due to recursive cell
		// changes.
		CellChangeStorage cellChangeStorage = mObjectsCreator
				.createCellChangeStorage();
		if (mCellChanges != null) {
			for (CellChange cellChange : mCellChanges) {
				stringBuilder.append(
						cellChangeStorage.toStorageString(cellChange)).append(
						SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
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
