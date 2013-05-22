package net.cactii.mathdoku;

import java.util.ArrayList;

import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.gridGenerating.GridGeneratingParameters;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.GridRow;
import net.cactii.mathdoku.storage.database.SolvingAttemptData;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;
import net.cactii.mathdoku.util.UsageLog;
import net.cactii.mathdoku.util.Util;
import android.util.Log;

public class Grid {
	private static final String TAG = "MathDoku.Grid";

	// Each line in the GridFile which contains information about the grid
	// starts with an identifier. This identifier consists of a generic part and
	// the package revision number.
	public static final String SAVE_GAME_GRID_LINE = "GRID";

	// Converting from old version name to new.
	private static final String SAVE_GAME_GRID_VERSION_READONLY = "VIEW.v";

	// ************************************************************************
	// Grid variables which are determined when generating the grid and which do
	// not alter anymore.
	// ************************************************************************

	// Unique row id of grid in database
	private int mRowId;

	// Size of the grid
	private int mGridSize;

	// All parameters that influence the game generation and which are needed to
	// regenerate a specific game again.
	private long mDateCreated;
	private GridGeneratingParameters mGridGeneratingParameters;

	// ************************************************************************
	// Grid elements and references which do change while solving the game.
	// ************************************************************************

	private long mDateLastSaved;

	// Has the solutiuon of the grid been revealed?
	private boolean mCheated;

	// Puzzle is active as long as it has not been solved.
	private boolean mActive;

	// Which cell is currently be selected? Null if no cell has been selected
	// yet.
	private GridCell mSelectedCell;

	// Statistics for this grid
	GridStatistics mGridStatistics;

	// ************************************************************************
	// References to other elements of which the grid is constructed.
	// ************************************************************************

	// Cages
	public ArrayList<GridCage> mCages;

	// Cells
	public ArrayList<GridCell> mCells;

	// Keep track of all moves as soon as grid is built or restored.
	public ArrayList<CellChange> mMoves;

	// ************************************************************************
	// Miscelleaneous
	// ************************************************************************

	// The solving attempt which is merged into this grid.
	private int mSolvingAttemptId;

	// Used to avoid redrawing or saving grid during creation of new grid
	public final Object mLock = new Object();

	// Preferences used when drawing the grid
	private boolean mPrefShowDupeDigits;
	private boolean mPrefShowBadCageMaths;
	private boolean mPrefShowMaybesAs3x3Grid;

	// Solved listener
	private OnSolvedListener mSolvedListener;

	// UsagaeLog counters
	private int mClearRedundantPossiblesInSameRowOrColumnCount;

	public Grid() {
		initialize();
	}

	/**
	 * Initializes a new grid object.
	 */
	private void initialize() {
		mRowId = -1;
		mSolvingAttemptId = -1;
		mGridSize = 0;
		mCells = new ArrayList<GridCell>();
		mCages = new ArrayList<GridCage>();
		mMoves = new ArrayList<CellChange>();
		mClearRedundantPossiblesInSameRowOrColumnCount = 0;
		mSolvedListener = null;
		mGridGeneratingParameters = new GridGeneratingParameters();
		mGridStatistics = new GridStatistics();
		setPreferences();
	}

	/**
	 * Sets the size for the grid. The size can only be set once.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 */
	public void setGridSize(int gridSize) {
		if (mGridSize == 0) {
			mGridSize = gridSize;
		} else if (gridSize != mGridSize) {
			// Ignore changes to grid size.
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				throw new RuntimeException(
						"GridSize can not be changed after it has been set.");
			}
		}
	}

	/**
	 * Set preferences which are used for drawing the grid.
	 */
	public void setPreferences() {
		Preferences preferences = Preferences.getInstance();
		mPrefShowDupeDigits = preferences.showDuplicateDigits();
		mPrefShowMaybesAs3x3Grid = preferences.showMaybesAsGrid();
		mPrefShowBadCageMaths = preferences.showBadCageMaths();

		// Reset borders of cells as they are affected by the preferences;
		for (GridCell cell : mCells) {
			cell.setBorders();
		}
	}

	// Returns cage id of cell at row, column
	// Returns -1 if not a valid cell or cage
	public int cageIdAt(int row, int column) {
		if (row < 0 || row >= mGridSize || column < 0 || column >= mGridSize)
			return -1;
		return this.mCells.get(column + row * this.mGridSize).getCageId();
	}

	/**
	 * Get the cage of the cell which is currently selected.
	 * 
	 * @return The cage to which the currently selected cell belongs. Null in
	 *         case no cell is selected or no cage exists.
	 */
	public GridCage getCageForSelectedCell() {
		if (mCages == null || mSelectedCell == null) {
			return null;
		}

		return mCages.get(mSelectedCell.getCageId());
	}

	/**
	 * Clears all cells in the entire grid.
	 */
	public void clearCells() {
		if (this.mMoves != null) {
			this.mMoves.clear();
		}
		if (mCells != null) {
			boolean updateGridClearCounter = false;
			for (GridCell cell : this.mCells) {
				if (cell.getUserValue() != 0) {
					mGridStatistics
							.increaseCounter(StatisticsCounterType.CELLS_EMPTY);
					mGridStatistics
							.decreaseCounter(StatisticsCounterType.CELLS_FILLED);
					updateGridClearCounter = true;
				} else if (cell.countPossibles() > 0) {
					updateGridClearCounter = true;
				}
				cell.clear();
			}
			if (updateGridClearCounter) {
				mGridStatistics
						.increaseCounter(StatisticsCounterType.GRID_CLEARED);
			}
		}
	}

	/* Fetch the cell at the given row, column */
	public GridCell getCellAt(int row, int column) {
		if (row < 0 || row >= mGridSize)
			return null;
		if (column < 0 || column >= mGridSize)
			return null;

		return this.mCells.get(column + row * this.mGridSize);
	}

	// Return the number of times a given user value is in a row
	public int getNumValueInRow(GridCell ocell) {
		int count = 0;
		for (GridCell cell : this.mCells) {
			if (cell.getRow() == ocell.getRow()
					&& cell.getUserValue() == ocell.getUserValue())
				count++;
		}
		return count;
	}

	// Return the number of times a given user value is in a column
	public int getNumValueInCol(GridCell ocell) {
		int count = 0;
		for (GridCell cell : this.mCells) {
			if (cell.getColumn() == ocell.getColumn()
					&& cell.getUserValue() == ocell.getUserValue())
				count++;
		}
		return count;
	}

	// Solve the puzzle by setting the Uservalue to the actual value
	public void solve() {
		UsageLog.getInstance().logFunction("ContextMenu.ShowSolution");
		this.mCheated = true;
		if (this.mMoves != null) {
			this.mMoves.clear();
		}
		for (GridCell cell : this.mCells) {
			if (!cell.isUserValueCorrect()) {
				cell.setCheated();
			}
			cell.setUserValue(cell.getCorrectValue());
		}
		mGridStatistics.solutionRevealed();
	}

	// Returns whether the puzzle is solved.
	public boolean checkIfSolved() {
		// Check if alls cell contain correct value.
		for (GridCell cell : this.mCells) {
			if (!cell.isUserValueCorrect()) {
				return false;
			}
		}

		// All values are correct. Puzzle is solved.
		if (this.mSolvedListener != null) {
			this.mSolvedListener.puzzleSolved();
		}

		// Deslect cell (and cage)
		if (mSelectedCell != null) {
			mSelectedCell.mSelected = false;
		}

		// Deactivate grid
		mActive = false;

		mGridStatistics.solved();

		return true;
	}

	// Checks whether the user has made any mistakes
	public boolean isSolutionValidSoFar() {
		for (GridCell cell : this.mCells)
			if (cell.isUserValueSet())
				if (cell.getUserValue() != cell.getCorrectValue())
					return false;

		return true;
	}

	// Return the list of cells that are highlighted as invalid
	public ArrayList<GridCell> invalidsHighlighted() {
		ArrayList<GridCell> invalids = new ArrayList<GridCell>();
		for (GridCell cell : this.mCells)
			if (cell.hasInvalidUserValueHighlight())
				invalids.add(cell);

		return invalids;
	}

	public void addMove(CellChange move) {
		if (mMoves == null) {
			mMoves = new ArrayList<CellChange>();
		}

		boolean identicalToLastMove = false;
		int indexLastMove = mMoves.size() - 1;
		if (indexLastMove >= 0) {
			CellChange lastMove = mMoves.get(indexLastMove);
			identicalToLastMove = lastMove.equals(move);
		}
		if (!identicalToLastMove) {
			mMoves.add(move);
		}
	}

	/**
	 * Get the number of moves made by the user.
	 * 
	 * @return The number of moves made by the user.
	 */
	public int countMoves() {
		return (mMoves == null ? 0 : mMoves.size());
	}

	public boolean undoLastMove() {
		if (mMoves != null) {
			int undoPosition = mMoves.size() - 1;

			if (undoPosition >= 0) {
				GridCell cell = mMoves.get(undoPosition).restore();
				mMoves.remove(undoPosition);
				setSelectedCell(cell);
				mGridStatistics.increaseCounter(StatisticsCounterType.UNDOS);
				return true;
			}
		}
		return false;
	}

	/**
	 * Selects the given cell.
	 * 
	 * @param cell
	 *            The cell to be selected. If null, then the current selected
	 *            cell will be unselected.
	 */
	public void setSelectedCell(GridCell cell) {
		// Unselect current cage
		GridCage oldSelectedCage = getCageForSelectedCell();
		if (oldSelectedCage != null) {
			oldSelectedCage.mSelected = false;
		}

		// Unselect current cell
		if (mSelectedCell != null) {
			mSelectedCell.mSelected = false;
		}

		// Select the new cell
		mSelectedCell = cell;
		if (mSelectedCell != null) {
			// A new cell was selected
			mSelectedCell.mSelected = true;
		}

		// Determine new cage (will return null in case no cell is selected)
		GridCage newSelectedCage = getCageForSelectedCell();

		// Remove borders form old cage if needed
		if ((newSelectedCage == null && oldSelectedCage != null)
				|| (newSelectedCage != null && !newSelectedCage
						.equals(oldSelectedCage))) {
			if (oldSelectedCage != null) {
				oldSelectedCage.setBorders();
			}
		}

		// Select new cage
		if (newSelectedCage != null) {
			newSelectedCage.mSelected = true;

			// Add border for new cage if needed
			if (!newSelectedCage.equals(oldSelectedCage)) {
				newSelectedCage.setBorders();
			}
		}
	}

	/**
	 * Clear the user value of the selected cell from the list of possible
	 * values in all other cells in the same row or in the same column as the
	 * selected cell.
	 * 
	 * @param originalCellChange
	 *            The cell which was originally changed.
	 */
	public void clearRedundantPossiblesInSameRowOrColumn(
			CellChange originalCellChange) {
		if (mSelectedCell != null) {
			mClearRedundantPossiblesInSameRowOrColumnCount++;
			int rowSelectedCell = this.mSelectedCell.getRow();
			int columnSelectedCell = this.mSelectedCell.getColumn();
			int valueSelectedCell = this.mSelectedCell.getUserValue();
			if (this.mSelectedCell != null) {
				for (GridCell cell : this.mCells) {
					if (cell.getRow() == rowSelectedCell
							|| cell.getColumn() == columnSelectedCell) {
						if (cell.hasPossible(valueSelectedCell)) {
							cell.saveUndoInformation(originalCellChange);
							cell.removePossible(valueSelectedCell);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the seed which is used to generate this puzzle.
	 * 
	 * @return The seed which can be used to generate this puzzle.
	 */
	public long getGameSeed() {
		return mGridGeneratingParameters.mGameSeed;
	}

	/**
	 * Check is user has cheated with solving this puzzle by requesting the
	 * solution.
	 * 
	 * @return True in case the user has solved the puzzle by requesting the
	 *         solution. False otherwise.
	 */
	public boolean isSolvedByCheating() {
		return this.mCheated;
	}

	/**
	 * Create a string representation of the Grid which can be used to store a
	 * grid.
	 * 
	 * @return A string representation of the grid.
	 */
	public String toStorageString() {
		StringBuffer stringBuffer = new StringBuffer(256);

		// First store data for the grid object itself.
		stringBuffer.append(SAVE_GAME_GRID_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mGameSeed
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mGeneratorRevisionNumber
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mGridSize
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mActive
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mCheated
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mClearRedundantPossiblesInSameRowOrColumnCount
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mHideOperators
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mMaxCageResult
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mMaxCageSize
				+ SolvingAttemptDatabaseAdapter.EOL_DELIMITER);

		// Store information about the cells. Use one line per single
		// cell.
		for (GridCell cell : mCells) {
			stringBuffer.append(cell.toStorageString()
					+ SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		// Store information about the cages. Use one line per single
		// cage.
		for (GridCage cage : mCages) {
			stringBuffer.append(cage.toStorageString()
					+ SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		// Store information about the cell changes. Use one line per single
		// cell change. Note: watch for lengthy line due to recursive cell
		// changes.
		for (CellChange cellChange : mMoves) {
			stringBuffer.append(cellChange.toStorageString()
					+ SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		return stringBuffer.toString();
	}

	/**
	 * Converts the definition of this grid to a string. This is a shortcut for
	 * calling {@link #toGridDefinitionString(ArrayList, ArrayList)}.
	 * 
	 * @return A unique string representation of the grid.
	 */
	public String toGridDefinitionString() {
		return toGridDefinitionString(mCells, mCages);
	}

	/**
	 * Converts the definition of this grid to a string. This definitions only
	 * consists of information needed to rebuild the puzzle. It does not include
	 * information about how it was created or about the current status of
	 * solving. This definition is unique regardless of grid size and or the
	 * version of the grid generator used.
	 * 
	 * @return A unique string representation of the grid.
	 */
	public static String toGridDefinitionString(ArrayList<GridCell> cells,
			ArrayList<GridCage> cages) {
		StringBuilder definitionString = new StringBuilder();

		// Get the cage number (represented as a value of two digits, if needed
		// prefixed with a 0) for each cell. Note: with a maximum of 81 cells in
		// a 9x9 grid we can never have a cage-id > 99.
		for (GridCell cell : cells) {
			definitionString.append(String.format("%02d", cell.getCageId()));
		}
		// Followed by cages
		for (GridCage cage : cages) {
			definitionString.append(":" + cage.mId + "," + cage.mResult + ","
					+ cage.mAction);
		}
		return definitionString.toString();
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
	public boolean fromStorageString(String line, int savedWithRevisionNumber) {
		String[] viewParts = line
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1);

		// Determine if this line contains Grid-information. If so, also
		// determine with which revision number the information was stored.
		int revisionNumber = 0;
		if (viewParts[0].equals(SAVE_GAME_GRID_LINE)
				&& savedWithRevisionNumber > 0) {
			revisionNumber = savedWithRevisionNumber;
		} else if (viewParts[0].startsWith(SAVE_GAME_GRID_VERSION_READONLY)) {
			// Extract version number from the view information itself.
			String revisionNumberString = viewParts[0]
					.substring(SAVE_GAME_GRID_VERSION_READONLY.length());
			revisionNumber = Integer.valueOf(revisionNumberString);
		} else {
			return false;
		}

		// Process all parts
		int index = 1;
		mGridGeneratingParameters.mGameSeed = Long
				.parseLong(viewParts[index++]);
		if (revisionNumber >= 3) {
			mGridGeneratingParameters.mGeneratorRevisionNumber = Integer
					.parseInt(viewParts[index++]);
		} else {
			mGridGeneratingParameters.mGeneratorRevisionNumber = 0;
		}
		if (revisionNumber < 271) {
			mDateLastSaved = Long.parseLong(viewParts[index++]);
		}
		if (revisionNumber <= 5) {
			// Field elapsedTime has been removed in version 6 and above.
			mGridStatistics.mElapsedTime = Long.parseLong(viewParts[index++]);
		}
		mGridSize = Integer.parseInt(viewParts[index++]);
		mActive = Boolean.parseBoolean(viewParts[index++]);

		if (revisionNumber < 271) {
			if (revisionNumber >= 2) {
				mDateCreated = Long.parseLong(viewParts[index++]);
			} else {
				// Date generated was not saved prior to version 2.
				mDateCreated = mDateLastSaved - mGridStatistics.mElapsedTime;
			}
		}
		if (revisionNumber >= 4) {
			mCheated = Boolean.parseBoolean(viewParts[index++]);
		} else {
			// Cheated was not saved prior to version 3.
			mCheated = false;
		}
		if (revisionNumber == 5) {
			// UndoCounter was only stored in version 5 in the game file.
			// Starting form version 6 it has been moved to the statistics
			// database.
			mGridStatistics.mUndoButton = Integer.parseInt(viewParts[index++]);
		}
		if (revisionNumber >= 5) {
			mClearRedundantPossiblesInSameRowOrColumnCount = Integer
					.parseInt(viewParts[index++]);
			mGridGeneratingParameters.mHideOperators = Boolean
					.parseBoolean(viewParts[index++]);
			mGridGeneratingParameters.mMaxCageResult = Integer
					.parseInt(viewParts[index++]);
			mGridGeneratingParameters.mMaxCageSize = Integer
					.parseInt(viewParts[index++]);
		} else {
			// Cheated was not saved prior to version 3.
			mClearRedundantPossiblesInSameRowOrColumnCount = 0;
			mGridGeneratingParameters.mHideOperators = false;
			mGridGeneratingParameters.mMaxCageResult = 0;
			mGridGeneratingParameters.mMaxCageSize = 0;
		}

		return true;
	}

	/**
	 * Creates the grid with given information. Returns whether the grid was
	 * successfully inserted into the database (true), or an error occurred in
	 * the process (false).
	 * 
	 * @param gameSeed
	 *            The game seed used to generate this grid.
	 * @param generatorRevisionNumber
	 *            The revision number of the generator used to create the grid.
	 * @param gridSize
	 *            The size of grid.
	 * @param cells
	 *            The list of cell used in the grid.
	 * @param cages
	 *            The list of cages used in the grid.
	 * @param active
	 *            The status of the grid.
	 */
	public boolean create(int gridSize, ArrayList<GridCell> cells,
			ArrayList<GridCage> cages, boolean active,
			GridGeneratingParameters gridGeneratingParameters) {

		// In case an existing grid object is reused, we have to clean up old
		// data
		if (this.mMoves != null) {
			this.mMoves.clear();
		}
		mSelectedCell = null;
		mCheated = false;
		mSolvingAttemptId = -1;

		// Set new data in grid
		mGridGeneratingParameters = gridGeneratingParameters;
		mDateCreated = System.currentTimeMillis();
		mGridSize = gridSize;
		mCells = cells;
		mCages = cages;
		mActive = active;

		// Cages keep a reference to the grid view to which they belong.
		for (GridCage cage : cages) {
			cage.setGridReference(this);
		}

		// Cells keep a reference to the grid view to which they belong.
		for (GridCell cell : cells) {
			cell.setGridReference(this);
		}

		// Set cell border for all cell
		for (GridCell cell : cells) {
			cell.setBorders();
		}

		return insertInDatabase();
	}

	public void setSolvedHandler(OnSolvedListener listener) {
		this.mSolvedListener = listener;
	}

	public abstract class OnSolvedListener {
		public abstract void puzzleSolved();
	}

	public int getGridSize() {
		return mGridSize;
	}

	public GridCell getSelectedCell() {
		return mSelectedCell;
	}

	public boolean isActive() {
		return mActive;
	}

	public void setActive(boolean active) {
		mActive = active;
	}

	public long getElapsedTime() {
		return mGridStatistics.mElapsedTime;
	}

	public long getCheatPenaltyTime() {
		return mGridStatistics.mCheatPenaltyTime;
	}

	public void setElapsedTime(long elapsedTime, long cheatPenaltyTime) {
		mGridStatistics.mElapsedTime = elapsedTime;
		mGridStatistics.mCheatPenaltyTime = cheatPenaltyTime;
	}

	public long getDateCreated() {
		return mDateCreated;
	}

	public void setDateCreated(long dateCreated) {
		mDateCreated = dateCreated;
	}

	public long getDateSaved() {
		return mDateLastSaved;
	}

	public boolean hasPrefShowDupeDigits() {
		return mPrefShowDupeDigits;
	}

	public boolean hasPrefShowBadCageMaths() {
		return mPrefShowBadCageMaths;
	}

	public boolean hasPrefShowMaybesAs3x3Grid() {
		return mPrefShowMaybesAs3x3Grid;
	}

	public int getClearRedundantPossiblesInSameRowOrColumnCount() {
		return mClearRedundantPossiblesInSameRowOrColumnCount;
	}

	public GridGeneratingParameters getGridGeneratingParameters() {
		return mGridGeneratingParameters;
	}

	public boolean getCheated() {
		return mCheated;
	}

	/**
	 * Create new objects in the databases for this grid.
	 * 
	 * @return True in case the gird has been inserted. False otherwise.
	 */
	public boolean insertInDatabase() {
		DatabaseHelper.beginTransaction();

		// Insert grid record if it does not yet exists.
		if (mRowId < 0) {
			GridDatabaseAdapter gridDatabaseAdapter = new GridDatabaseAdapter();
			GridRow gridRow = gridDatabaseAdapter
					.getByGridDefinition(toGridDefinitionString());
			if (gridRow == null) {
				// Insert grid into database.
				mRowId = gridDatabaseAdapter.insert(this);
				if (mRowId < 0) {
					if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
						throw new RuntimeException(
								"Error while inserting a new grid into database.");
					} else {
						DatabaseHelper.endTransaction();
						return false;
					}
				}
			} else {
				mRowId = gridRow.mId;
			}
		}

		// Insert new solving attempt.
		SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = new SolvingAttemptDatabaseAdapter();
		mSolvingAttemptId = solvingAttemptDatabaseAdapter.insert(this,
				Util.getPackageVersionNumber());
		if (mSolvingAttemptId < 0) {
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				throw new RuntimeException(
						"Error while inserting a new grid into database.");
			} else {
				DatabaseHelper.endTransaction();
				return false;
			}
		}

		// Insert new statistics.
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = new StatisticsDatabaseAdapter();
		mGridStatistics = statisticsDatabaseAdapter.insert(this);
		if (mGridStatistics == null) {
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				throw new RuntimeException(
						"Error while inserting a new grid into database.");
			} else {
				DatabaseHelper.endTransaction();
				return false;
			}
		}

		// Commit and close transaction
		DatabaseHelper.setTransactionSuccessful();
		DatabaseHelper.endTransaction();

		return true;
	}

	/**
	 * Load the current statistics for this grid.
	 */
	public boolean loadStatistics() {
		// Determine definition
		String definition = toGridDefinitionString();

		// First load grid.
		GridDatabaseAdapter gridDatabaseAdapter = new GridDatabaseAdapter();
		GridRow gridRow = gridDatabaseAdapter.getByGridDefinition(definition);
		if (gridRow == null) {
			// Insert grid into database.
			mRowId = gridDatabaseAdapter.insert(this);
		} else {
			mRowId = gridRow.mId;
		}

		// Load most recent statistics for this grid
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = new StatisticsDatabaseAdapter();
		mGridStatistics = statisticsDatabaseAdapter.getMostRecent(mRowId);
		if (mGridStatistics == null) {
			// No statistics available. Create a new statistics records.
			mGridStatistics = statisticsDatabaseAdapter.insert(this);
		}

		return (mGridStatistics != null);
	}

	/**
	 * Increase given counter with 1.
	 * 
	 * @param statisticsCounterType
	 *            The counter which has to be increased.
	 */
	public void increaseCounter(StatisticsCounterType statisticsCounterType) {
		mGridStatistics.increaseCounter(statisticsCounterType);
	}

	/**
	 * Get the grid statistics related to this grid.
	 * 
	 * @return The grid statistics related to this grid.
	 */
	public GridStatistics getGridStatistics() {
		return mGridStatistics;
	}

	/**
	 * Get the row id of this grid.
	 * 
	 * @return The row id of this grid.
	 */
	public int getRowId() {
		return mRowId;
	}

	/**
	 * Gets the id of the solving attempt which is merged into this grid.
	 * 
	 * @return The id of the solving attempt which is merged into this grid.
	 */
	public int getSolvingAttemptId() {
		return mSolvingAttemptId;
	}

	/**
	 * Save this grid (solving attempt and statistics).
	 * 
	 * @return True in case everything has been saved. False otherwise.
	 */
	public boolean save() {
		return save(false);
	}

	/**
	 * Upgrade this grid (solving attempt and statistics) to the current app
	 * version.
	 * 
	 * @return True in case everything has been saved. False otherwise.
	 */
	public boolean saveOnUpgrade() {
		return save(true);
	}

	/**
	 * Save this grid (solving attempt and statistics).
	 * 
	 * @param saveDueToUpgrade
	 *            False (default) in case of normal save. True in case saving is
	 *            done while upgrading the grid to the current version of the
	 *            app.
	 * 
	 * @return True in case everything has been saved. False otherwise.
	 */
	private boolean save(boolean saveDueToUpgrade) {
		boolean saved = true;

		synchronized (mLock) { // Avoid saving game at the same time as
								// creating puzzle

			// The solving attempt was already created as soon as the grid was
			// created first. So only an update is needed.
			SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = new SolvingAttemptDatabaseAdapter();
			if (!solvingAttemptDatabaseAdapter.update(mSolvingAttemptId, this,
					saveDueToUpgrade)) {
				return false;
			}

			// Update statistics. Do not abort in case statistics could not be
			// saved.
			saved = (mGridStatistics == null ? false : mGridStatistics.save());
		} // End of synchronised block

		return saved;
	}

	/**
	 * Load a grid and corresponding solving attempt from the database.
	 * 
	 * @param solvingAttemptId
	 *            The unique id of the solving attempt which has to be loaded.
	 * @return True in case the grid has been loaded successfully. False
	 *         otherwise.
	 */
	public boolean load(int solvingAttemptId) throws InvalidGridException {
		SolvingAttemptData solvingAttemptData = new SolvingAttemptDatabaseAdapter()
				.getData(solvingAttemptId);
		if (load(solvingAttemptData)) {
			// Load the statistics of the grid
			return loadStatistics();
		} else {
			return false;
		}
	}

	/**
	 * Load a grid from the given solving attempt.
	 * 
	 * @return True in case the grid has been loaded successfully. False
	 *         otherwise.
	 */
	public boolean load(SolvingAttemptData solvingAttemptData) {
		boolean loaded = true;

		if (solvingAttemptData == null) {
			// Could not retrieve this solving attempt.
			loaded = false;
			throw new InvalidGridException();
		}

		// Get date created and date last saved from the solving attempt record.
		// When converting from game file to database the fields will be 0. They
		// will be overwritten with dates stored in the VIEW-line of the
		// solvingAttempData.
		mDateCreated = solvingAttemptData.mDateCreated;
		mDateLastSaved = solvingAttemptData.mDateUpdated;

		String line;
		try {
			// Read first line
			if ((line = solvingAttemptData.getFirstLine()) == null) {
				throw new InvalidGridException(
						"Unexpected end of solving attempt at first line");
			}

			// Read view information
			if (!fromStorageString(line, solvingAttemptData.mSavedWithRevision)) {
				// The initial version of the saved games stored view
				// information on 4 different lines. Rewrite to valid view
				// storage information (version 1).
				// Do not remove as long as backward compatibility with old save
				// file should be remained.
				line = Grid.SAVE_GAME_GRID_LINE
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
						+ 0 // game seed. Use
							// 0 as it was
							// not stored in
							// initial
							// files
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
						+ line // date created
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
						+ solvingAttemptData.getNextLine() // elapsed time
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
						+ solvingAttemptData.getNextLine() // grid size
						+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
						+ solvingAttemptData.getNextLine(); // active

				// Retry to process this line as if it was saved with revision
				// 1.
				if (!fromStorageString(line, 1)) {
					throw new InvalidGridException(
							"View information can not be processed: " + line);
				}
			}
			if ((line = solvingAttemptData.getNextLine()) == null) {
				throw new InvalidGridException(
						"Unexpected end of solving attempt after processing view information.");
			}

			// Read cell information
			int countCellsToRead = mGridSize * mGridSize;
			GridCell selectedCell = null;
			while (countCellsToRead > 0) {
				GridCell cell = new GridCell(this, 0);
				if (!cell.fromStorageString(line,
						solvingAttemptData.mSavedWithRevision)) {
					throw new InvalidGridException(
							"Line does not contain cell information while this was expected:"
									+ line);
				}
				mCells.add(cell);
				if (cell.mSelected && selectedCell == null) {
					// Remember first cell which is marked as selected. Note the
					// cell can not be selected until the cages are loaded as
					// well.
					selectedCell = cell;
				}
				countCellsToRead--;

				// Read next line
				if ((line = solvingAttemptData.getNextLine()) == null) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt when processing cell information.");
				}
			}

			if (line.startsWith("SELECTED:")) {
				// Do not remove as long as backward compatibility with old save
				// file should be remained. In new save files this information
				// is stored as part of the cell information.
				if (selectedCell == null) {
					// No cell is selected yet.
					int selected = Integer
							.parseInt(line
									.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)[1]);
					selectedCell = mCells.get(selected);
				}

				// Read next line
				if ((line = solvingAttemptData.getNextLine()) == null) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt after processing SELECTED line.");
				}
			}
			if (line.startsWith("INVALID:")) {
				// Do not remove as long as backward compatibility with old save
				// file should be remained. In new save files this information
				// is stored as part of the cell information.
				String invalidlist = line
						.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)[1];
				for (String cellId : invalidlist
						.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
					int cellNum = Integer.parseInt(cellId);
					GridCell c = mCells.get(cellNum);
					c.setInvalidHighlight(true);
				}

				// Read next line
				if ((line = solvingAttemptData.getNextLine()) == null) {
					throw new InvalidGridException(
							"Unexpected end of solving attempt after processing INVALID line.");
				}
			}

			// Cages (at least one expected)
			GridCage cage = new GridCage(this);
			if (!cage.fromStorageString(line,
					solvingAttemptData.mSavedWithRevision)) {
				throw new InvalidGridException(
						"Line does not contain cage  information while this was expected:"
								+ line);
			}
			do {
				mCages.add(cage);

				// Read next line. No checking of unexpected end of file might
				// be done here because the last line in a file can contain a
				// cage.
				line = solvingAttemptData.getNextLine();

				// Create a new empty cage
				cage = new GridCage(this);
			} while (line != null
					&& cage.fromStorageString(line,
							solvingAttemptData.mSavedWithRevision));

			// Check cage maths after all cages have been read.
			for (GridCage cage2 : mCages) {
				cage2.checkCageMathsCorrect(true);
			}

			// Set the selected cell (and indirectly the selected cage).
			if (selectedCell != null) {
				setSelectedCell(selectedCell);
			}

			// Remaining lines contain cell changes (zero or more expected)
			CellChange cellChange = new CellChange();
			while (line != null
					&& cellChange.fromStorageString(line, mCells,
							solvingAttemptData.mSavedWithRevision)) {
				addMove(cellChange);

				// Read next line. No checking of unexpected end of file might
				// be done here because the last line in a file can contain a
				// cage.
				line = solvingAttemptData.getNextLine();

				// Create a new empty cell change
				cellChange = new CellChange();
			}

			// Check if end of file is reached an no information was unread yet.
			if (line != null) {
				throw new InvalidGridException(
						"Unexpected line found while end of file was expected: "
								+ line);
			}

			// The solving attempt has been loaded succesfully into the grid
			// object
			mSolvingAttemptId = solvingAttemptData.mId;
			mRowId = solvingAttemptData.mGridId;
		} catch (InvalidGridException e) {
			loaded = false;
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				Log.d(TAG,
						"Invalid file format error when  restoring solving attempt with id '"
								+ solvingAttemptData.mId + "'\n"
								+ e.getMessage());
			}
			initialize();
		} catch (NumberFormatException e) {
			loaded = false;
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				Log.d(TAG,
						"Number format error when  restoring solving attempt with id '"
								+ solvingAttemptData.mId + "'\n"
								+ e.getMessage());
			}
			initialize();
		} catch (IndexOutOfBoundsException e) {
			loaded = false;
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				Log.d(TAG,
						"Index out of bound error when  restoring solving attempt with id '"
								+ solvingAttemptData.mId + "'\n"
								+ e.getMessage());
			}
			initialize();
		}
		return loaded;
	}

	/**
	 * Checks if the grid is empty (i.e. cells do not contain a user value nor a
	 * possible value).
	 * 
	 * @param checkPossibles
	 *            Also check possible values to determine whether grid is empty.
	 *            If false it is only checked whether user values have been
	 *            entered.
	 * @return True in case the grid is empty. False otherwise
	 */
	public boolean isEmpty(boolean checkPossibles) {
		for (GridCell cell : mCells) {
			if (cell.isUserValueSet()
					|| (checkPossibles && cell.countPossibles() > 0)) {
				// Not empty as this cell contains a user value or a possible
				// value
				return false;
			}
		}

		// All cells are empty
		return true;
	}
}