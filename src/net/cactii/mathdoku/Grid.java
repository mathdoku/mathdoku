package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import net.cactii.mathdoku.util.Util;
import android.util.Log;

import com.srlee.DLX.MathDokuDLX;

public class Grid {
	private static final String TAG = "MathDoku.Grid";

	// Each line in the GridFile which contains information about the grid
	// starts with an identifier. This identifier consists of a generic part and
	// the package revision number.
	public static final String SAVE_GAME_GRID_LINE = "GRID";

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
	// Miscellaneous
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
	 * Clears all cells and cages in the entire grid.
	 * 
	 * @param replay
	 *            True if clear is needed for replay of a finished grid. False
	 *            otherwise.
	 */
	public void clearCells(boolean replay) {
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
				if (replay) {
					cell.clearAllFlags();
				}
			}
			if (updateGridClearCounter) {
				mGridStatistics
						.increaseCounter(StatisticsCounterType.GRID_CLEARED);
			}
		}
		
		// clear cages to remove the border related to bad cage maths.
		if (mCages != null) {
			for (GridCage cage : mCages) {
				cage.checkCageMathsCorrect(false);
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

	// Solve the puzzle by setting the Uservalue to the actual value
	public void solve() {
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
		return toGridDefinitionString(mCells, mCages,
				mGridGeneratingParameters.mHideOperators);
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
			ArrayList<GridCage> cages, boolean hideOperators) {
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
					+ (hideOperators ? GridCage.ACTION_NONE : cage.mAction));
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
		mGridGeneratingParameters.mGameSeed = Long
				.parseLong(viewParts[index++]);
		mGridGeneratingParameters.mGeneratorRevisionNumber = Integer
				.parseInt(viewParts[index++]);
		mGridSize = Integer.parseInt(viewParts[index++]);
		mActive = Boolean.parseBoolean(viewParts[index++]);

		mCheated = Boolean.parseBoolean(viewParts[index++]);
		mClearRedundantPossiblesInSameRowOrColumnCount = Integer
				.parseInt(viewParts[index++]);
		mGridGeneratingParameters.mHideOperators = Boolean
				.parseBoolean(viewParts[index++]);
		mGridGeneratingParameters.mMaxCageResult = Integer
				.parseInt(viewParts[index++]);
		mGridGeneratingParameters.mMaxCageSize = Integer
				.parseInt(viewParts[index++]);

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

			// Update statistics.
			saved = (mGridStatistics == null ? false : mGridStatistics.save());

			// In case a replay of the grid is finished the statistics which
			// have to included in the cumulative and the historic statistics
			// should be changed to the current solving attempt.
			if (saved && mActive == false
					&& mGridStatistics.getReplayCount() > 0
					&& mGridStatistics.isIncludedInStatistics() == false
					&& saveDueToUpgrade == false) {
				new StatisticsDatabaseAdapter()
						.updateSolvingAttemptToBeIncludedInStatistics(mRowId,
								mSolvingAttemptId);
			}

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
	private boolean load(SolvingAttemptData solvingAttemptData) {
		boolean loaded = true;

		if (solvingAttemptData == null) {
			// Could not retrieve this solving attempt.
			return false;
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
				throw new InvalidGridException(
						"Line does not contain grid information while this was expected:"
								+ line);
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
					c.setInvalidHighlight();
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
			
			// Mark cells with duplicate values
			for (GridCell gridCell : mCells) {
				markDuplicateValuesInRowAndColumn(gridCell);
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

	/**
	 * Replay a grid as if the grid was just created.
	 */
	public void replay() {
		// Clear the cells and the moves list.
		clearCells(true);
		mClearRedundantPossiblesInSameRowOrColumnCount = 0;

		// No cell may be selected.
		setSelectedCell(null);

		// The solving attempt is not yet saved.
		mDateLastSaved = 0;

		// Forget it if we have cheated before.
		mCheated = false;

		// Make the grid active again.
		mActive = true;

		// mGridStatistics ???
		// mSolvingAttemptId

		// Insert new solving attempt and statistics.
		insertInDatabase();

		// Save the grid
		save();
	}

	/**
	 * Load a grid from the given definition string.
	 * 
	 * @return True in case the grid has been loaded successfully. False
	 *         otherwise.
	 */
	public boolean load(String definition) {
		// Be sure to start with an empty grid when load from a definition.
		initialize();

		// Example of a grid definition:
		// 00010202000103040506030405060707:0,4,1:1,2,4:2,2,4:3,1,2:4,4,4:5,2,4:6,4,1:7,6,3

		// Split the definition into parts.
		String[] definitionParts = definition.split(":");

		// The definition should contain at least two parts.
		if (definitionParts == null || definitionParts.length < 2) {
			return false;
		}

		// Create empty cages. Except the first part of the definition, each
		// part represents a single cage.
		for (int i = 1; i < definitionParts.length; i++) {
			// Define new cage
			GridCage gridCage = new GridCage(this);
			gridCage.setCageId(i - 1);

			// Add cage to cages list
			if (mCages.add(gridCage) == false) {
				return false;
			}
		}

		// The first part of the definitions contains the cage number for each
		// individual cell. The cage number always consists of two digits.
		int cellCount = definitionParts[0].length() / 2;
		switch (cellCount) {
		case 16:
			mGridSize = 4;
			break;
		case 25:
			mGridSize = 5;
			break;
		case 36:
			mGridSize = 6;
			break;
		case 49:
			mGridSize = 7;
			break;
		case 64:
			mGridSize = 8;
			break;
		case 81:
			mGridSize = 9;
			break;
		default:
			// Invalid number of cells.
			return false;
		}
		Pattern pattern = Pattern.compile("\\d\\d");
		Matcher matcher = pattern.matcher(definitionParts[0]);
		int cellNumber = 0;
		while (matcher.find()) {
			int cageId = Integer.valueOf(matcher.group());

			// Create new cell and add it to the cells list.
			GridCell gridCell = new GridCell(this, cellNumber++);
			gridCell.setCageId(cageId);
			mCells.add(gridCell);

			// Determine the cage to which the cell has to be added.
			GridCage gridCage = mCages.get(cageId);
			if (gridCage == null) {
				return false;
			}
			gridCage.mCells.add(gridCell);
		}

		// Finalize the grid cages which only can be done after the cell have
		// been attached to the cages.
		for (int i = 1; i < definitionParts.length; i++) {
			// Split the cage part into elements
			String[] cageElements = definitionParts[i].split(",");

			// Define new cage
			GridCage gridCage = mCages.get(i - 1);
			if (gridCage == null) {
				return false;
			}
			gridCage.setCageResults(Integer.valueOf(cageElements[1]),
					Integer.valueOf(cageElements[2]), false);
		}

		// Check whether a single solution can be found.
		int[][] solution = new MathDokuDLX(mGridSize, mCages).getSolutionGrid();
		if (solution == null) {
			// Either no or multiple solutions can be found. In both case this
			// would mean that the grid definition string was manipulated by the
			// user.
			return false;
		}

		// Store the solution in the grid cells.
		for (int row = 0; row < this.mGridSize; row++) {
			for (int col = 0; col < this.mGridSize; col++) {
				getCellAt(row, col).setCorrectValue(solution[row][col]);
			}
		}

		// Finally set all cage borders by checking their math
		for (GridCage gridCage : mCages) {
			gridCage.checkCageMathsCorrect(true);
		}

		return true;
	}

	/**
	 * Check whether the user value of the given cell is used in another cell on
	 * the same row or column.
	 * 
	 * @param gridCell
	 *            The grid cell for which it has to be checked whether its value
	 *            is used in another cell on the same row or column.
	 * @return True in case the user value of the given cell is used in another
	 *         cell on the same row or column.
	 */
	public boolean markDuplicateValuesInRowAndColumn(GridCell gridCell) {
		boolean duplicateValue = false;

		if (gridCell.isUserValueSet()) {
			int userValue = gridCell.getUserValue();
			for (GridCell cell : mCells) {
				// For each cell containing the user value it is checked whether
				// the cell is in the same row or column as the given cell.
				if (cell.getUserValue() == userValue) {
					if ((cell.getColumn() == gridCell.getColumn() && cell
							.getRow() != gridCell.getRow())
							|| (cell.getColumn() != gridCell.getColumn() && cell
									.getRow() == gridCell.getRow())) {
						// Mark this cell as duplicate.
						duplicateValue = true;
						cell.setDuplicateHighlight(true);
					}
				}
			}
		}
		gridCell.setDuplicateHighlight(duplicateValue);
		return duplicateValue;
	}
}