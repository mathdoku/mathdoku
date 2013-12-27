package net.mathdoku.plus.grid;

import android.util.Log;

import com.srlee.DLX.MathDokuDLX;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.GridStatistics.StatisticsCounterType;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttemptData;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grid {
	private static final String TAG = "MathDoku.Grid";

	// Each line in the GridFile which contains information about the grid
	// starts with an identifier. This identifier consists of a generic part and
	// the package revision number.
	private static final String SAVE_GAME_GRID_LINE = "GRID";

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

	// Has the solution of the grid been revealed?
	private boolean mRevealed;

	// Puzzle is active as long as it has not been solved.
	private boolean mActive;

	// Which cell is currently be selected? Null if no cell has been selected
	// yet.
	private GridCell mSelectedCell;

	// Statistics for this grid
	private GridStatistics mGridStatistics;

	// ************************************************************************
	// References to other elements of which the grid is constructed.
	// ************************************************************************

	// Cages
	public ArrayList<GridCage> mCages;

	// Cells
	public ArrayList<GridCell> mCells;

	// Keep track of all moves as soon as grid is built or restored.
	private ArrayList<CellChange> mMoves;

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

	// Counters
	private int mClearRedundantPossiblesInSameRowOrColumnCount;

	// The GridInitializer is used by the unit test to initialize the grid with
	// mock objects if needed.
	public static class GridInitializer {
		public GridCell createGridCell(Grid grid, int cell) {
			return new GridCell(grid, cell);
		}

		public CellChange createCellChange() {
			return new CellChange();
		}

		public GridCage createGridCage(Grid grid) {
			return new GridCage(grid);
		}

		public GridStatistics createGridStatistics() {
			return new GridStatistics();
		}

		public GridGeneratingParameters createGridGeneratingParameters() {
			return new GridGeneratingParameters();
		}

		public MathDokuDLX createMathDokuDLX(int gridSize,
				ArrayList<GridCage> cages) {
			return new MathDokuDLX(gridSize, cages);
		}

		public ArrayList<GridCell> createArrayListOfGridCells() {
			return new ArrayList<GridCell>();
		}

		public ArrayList<GridCage> createArrayListOfGridCages() {
			return new ArrayList<GridCage>();
		}

		public ArrayList<CellChange> createArrayListOfCellChanges() {
			return new ArrayList<CellChange>();
		}

		public GridCellSelectorInRowOrColumn createGridCellSelectorInRowOrColumn(
				ArrayList<GridCell> cells, int row, int column) {
			return new GridCellSelectorInRowOrColumn(cells, row, column);
		}

	}

	private final GridInitializer mGridInitializer;

	/**
	 * Creates new instance of {@link net.mathdoku.plus.grid.Grid}.
	 */
	public Grid() {
		mGridInitializer = new GridInitializer();
		initialize();
	}

	/**
	 * Creates new instance of {@link net.mathdoku.plus.grid.Grid}.
	 * 
	 * This method is intended for custom initialization by unit tests.
	 * 
	 * @param gridInitializer
	 *            The initializer to be used for this grid.
	 */
	public Grid(GridInitializer gridInitializer) {
		mGridInitializer = (gridInitializer != null ? gridInitializer
				: new GridInitializer());
		initialize();
	}

	/**
	 * Initializes a new grid object.
	 * 
	 */
	private void initialize() {
		mRowId = -1;
		mSolvingAttemptId = -1;
		mGridSize = 0;
		mCells = mGridInitializer.createArrayListOfGridCells();
		mCages = mGridInitializer.createArrayListOfGridCages();
		mMoves = mGridInitializer.createArrayListOfCellChanges();
		mClearRedundantPossiblesInSameRowOrColumnCount = 0;
		mSolvedListener = null;
		mGridGeneratingParameters = mGridInitializer
				.createGridGeneratingParameters();
		mGridStatistics = mGridInitializer.createGridStatistics();
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
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
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
		mPrefShowDupeDigits = preferences.isDuplicateDigitHighlightVisible();
		mPrefShowMaybesAs3x3Grid = preferences.isMaybesDisplayedInGrid();
		mPrefShowBadCageMaths = preferences.isBadCageMathHighlightVisible();

		// Reset borders of cells as they are affected by the preferences;
		for (GridCell cell : mCells) {
			cell.setBorders();
		}
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

		// Get the cage id of the selected cell and check whether it exists
		int cageIdSelectedCell = mSelectedCell.getCageId();

		// Return cage if cage id exists
		return (cageIdSelectedCell >= 0 && cageIdSelectedCell < mCages.size() ? mCages
				.get(cageIdSelectedCell) : null);
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
				if (cell.getUserValue() != 0 || cell.countPossibles() > 0) {
					updateGridClearCounter = true;
				}
				cell.clear();
				if (replay) {
					cell.clearAllFlags();
				}
			}
			if (updateGridClearCounter) {
				mGridStatistics
						.increaseCounter(StatisticsCounterType.ACTION_CLEAR_GRID);
			}
		}

		// clear cages to remove the border related to bad cage maths.
		setBordersForCagesWithIncorrectMaths();
	}

	/* Fetch the cell at the given row, column */
	public GridCell getCellAt(int row, int column) {
		if (row < 0 || row >= mGridSize)
			return null;
		if (column < 0 || column >= mGridSize)
			return null;

		return this.mCells.get(column + row * this.mGridSize);
	}

	/**
	 * Reveal the solution by setting the user value to the actual value.
	 */
	public void revealSolution() {
		this.mRevealed = true;
		if (mCells != null) {
			for (GridCell cell : this.mCells) {
				if (cell.isUserValueIncorrect()) {
					cell.setRevealed();
					cell.setUserValue(cell.getCorrectValue());
				}
			}
		}
		if (mGridStatistics != null) {
			mGridStatistics.solutionRevealed();
		}
	}

	/**
	 * Unreveal the solution by setting the user value to the actual value. Only
	 * available in DEVELOPMENT mode.
	 */
	public void unrevealSolution() {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			mRevealed = false;
			if (mGridStatistics != null) {
				mGridStatistics.mSolutionRevealed = false;
				mGridStatistics.mSolvedManually = true;
			}
		}
	}

	/**
	 * Checks whether the puzzle is solved.
	 */
	public boolean isSolved() {
		// Check if all cells are filled and do contain the correct value.
		for (GridCell cell : this.mCells) {
			if (cell.isUserValueIncorrect()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Set the puzzle as solved.
	 */
	public void setSolved() {
		// Update the grid statistics
		mGridStatistics.solved();

		// Inform listeners when puzzle is solved.
		if (mSolvedListener != null) {
			mSolvedListener.puzzleSolved();
		}
	}

	// Checks whether the user has made any mistakes

	/**
	 * Checks whether all cells which are filled with a user value are filled
	 * with the correct value.
	 * 
	 * @return True in case the user has made no mistakes so far.
	 */
	public boolean isSolutionValidSoFar() {
		for (GridCell cell : this.mCells) {
			if (cell.isUserValueSet()) {
				// Only check the cells which are filled with a user value.
				if (cell.isUserValueIncorrect()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Adds a cell change to the list of moves played for this grid.
	 * 
	 * @param cellChange
	 *            The cell change to be added to the list of moves.
	 */
	public void addMove(CellChange cellChange) {
		if (mMoves == null) {
			mMoves = mGridInitializer.createArrayListOfCellChanges();
		}

		boolean identicalToLastMove = false;
		int indexLastMove = mMoves.size() - 1;
		if (indexLastMove >= 0) {
			CellChange lastMove = mMoves.get(indexLastMove);
			identicalToLastMove = lastMove.equals(cellChange);
		}
		if (!identicalToLastMove) {
			mMoves.add(cellChange);
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
		// Check if list contains at least one move
		if (mMoves == null || mMoves.size() == 0) {
			// Cannot undo on non existing list.
			return false;
		}

		// Restore the last cell change in the list of moves
		int undoPosition = mMoves.size() - 1;
		GridCell restoredGridCell = mMoves.get(undoPosition).restore();
		if (restoredGridCell == null) {
			// Restore of cell failed.
			return false;
		}

		mMoves.remove(undoPosition);

		mGridStatistics.increaseCounter(StatisticsCounterType.ACTION_UNDO_MOVE);

		// Set the cell to which the cell change applies as selected cell.
		setSelectedCell(restoredGridCell);

		// Each cell in the same column or row as the restored cell, has to be
		// checked for duplicate values.
		GridCellSelectorInRowOrColumn gridCellSelectorInRowOrColumn = mGridInitializer
				.createGridCellSelectorInRowOrColumn(mCells,
						restoredGridCell.getRow(), restoredGridCell.getColumn());
		ArrayList<GridCell> gridCellsInSameRowOrColumn = gridCellSelectorInRowOrColumn
				.find();
		if (gridCellsInSameRowOrColumn != null) {
			for (GridCell gridCellInSameRowOrColumn : gridCellsInSameRowOrColumn) {
				gridCellInSameRowOrColumn
						.markDuplicateValuesInSameRowAndColumn();
			}
		}

		// Check the cage math. Set border in case math is incorrect.
		GridCage gridCage = restoredGridCell.getCage();
		if (gridCage != null && gridCage.isMathsCorrect()) {
			gridCage.setBorders();
		}

		// Undo successful completed.
		return true;
	}

	/**
	 * Deselect the selected cell.
	 */
	public void deselectSelectedCell() {
		setSelectedCell((GridCell) null);
	}

	/**
	 * Selects the given cell.
	 * 
	 * @param cell
	 *            The cell to be selected. If null, then the current selected
	 *            cell will be unselected.
	 * @return The selected cell.
	 */
	public GridCell setSelectedCell(GridCell cell) {
		// Determine currently selected cage
		GridCage oldSelectedCage = getCageForSelectedCell();

		// Unselect current cell
		if (mSelectedCell != null) {
			mSelectedCell.deselect();
		}

		// Select the new cell
		mSelectedCell = cell;
		if (mSelectedCell != null) {
			// A new cell was selected
			mSelectedCell.select();
		}

		// Determine new cage (will return null in case no cell is selected)
		GridCage newSelectedCage = getCageForSelectedCell();

		// Remove borders from old cage if needed
		if ((newSelectedCage == null && oldSelectedCage != null)
				|| (newSelectedCage != null && !newSelectedCage
						.equals(oldSelectedCage))) {
			if (oldSelectedCage != null) {
				oldSelectedCage.setBorders();
			}
		}

		// Select new cage and set borders.
		if (newSelectedCage != null && !newSelectedCage.equals(oldSelectedCage)) {
			newSelectedCage.setBorders();
		}

		return mSelectedCell;
	}

	/**
	 * Set the cell at the given (x,y) coordinates as the selected cell.
	 * 
	 * @param coordinates
	 *            The (x,y) coordinates of the cell.
	 * @return The selected cell.
	 */
	public GridCell setSelectedCell(int coordinates[]) {
		return setSelectedCell(getCellAt(coordinates[1], coordinates[0]));
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
	 * Check if user has revealed the solution of this puzzle.
	 * 
	 * @return True in case the user has solved the puzzle by requesting the
	 *         solution. False otherwise.
	 */
	public boolean isSolutionRevealed() {
		return this.mRevealed;
	}

	/**
	 * Create a string representation of the Grid which can be used to store a
	 * grid.
	 * 
	 * @return A string representation of the grid.
	 */
	public String toStorageString() {
		StringBuilder stringBuilder = new StringBuilder(256);

		// First store data for the grid object itself.
		stringBuilder.append(SAVE_GAME_GRID_LINE)
				.append(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)
				.append(mActive)
				.append(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)
				.append(mRevealed)
				.append(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)
				.append(mClearRedundantPossiblesInSameRowOrColumnCount)
				.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);

		// Store information about the cells. Use one line per single
		// cell.
		for (GridCell cell : mCells) {
			stringBuilder.append(cell.toStorageString()).append(
					SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		// Store information about the cages. Use one line per single
		// cage.
		for (GridCage cage : mCages) {
			stringBuilder.append(cage.toStorageString()).append(
					SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		// Store information about the cell changes. Use one line per single
		// cell change. Note: watch for lengthy line due to recursive cell
		// changes.
		for (CellChange cellChange : mMoves) {
			stringBuilder.append(cellChange.toStorageString()).append(
					SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
		}

		return stringBuilder.toString();
	}

	/**
	 * Converts the definition of this grid to a string. This is a shortcut for
	 * calling
	 * {@link #toGridDefinitionString(java.util.ArrayList, java.util.ArrayList, net.mathdoku.plus.gridGenerating.GridGeneratingParameters)}
	 * .
	 * 
	 * @return A unique string representation of the grid.
	 */
	public String toGridDefinitionString() {
		return toGridDefinitionString(mCells, mCages, mGridGeneratingParameters);
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
			ArrayList<GridCage> cages,
			GridGeneratingParameters gridGeneratingParameters) {
		StringBuilder definitionString = new StringBuilder();

		// Convert puzzle complexity to an integer value. Do not use the ordinal
		// of the enumeration as this value is not persistent.
		int complexity = 0;
		switch (gridGeneratingParameters.mPuzzleComplexity) {
		case RANDOM:
			// Note: puzzles will never be stored with this complexity.
			complexity = 0;
			break;
		case VERY_EASY:
			complexity = 1;
			break;
		case EASY:
			complexity = 2;
			break;
		case NORMAL:
			complexity = 3;
			break;
		case DIFFICULT:
			complexity = 4;
			break;
		case VERY_DIFFICULT:
			complexity = 5;
			break;
		// NO DEFAULT here as we want to be notified at compile time in case a
		// new enum value is added.
		}
		definitionString.append(Integer.toString(complexity)).append(":");

		// Get the cage number (represented as a value of two digits, if needed
		// prefixed with a 0) for each cell. Note: with a maximum of 81 cells in
		// a 9x9 grid we can never have a cage-id > 99.
		for (GridCell cell : cells) {
			definitionString.append(String.format("%02d", cell.getCageId()));
		}
		// Followed by cages
		for (GridCage cage : cages) {
			definitionString
					.append(":")
					.append(cage.mId)
					.append(",")
					.append(cage.mResult)
					.append(",")
					.append(gridGeneratingParameters.mHideOperators ? GridCage.ACTION_NONE
							: cage.mAction);
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
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean fromStorageString(String line, int savedWithRevisionNumber) {
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
		mActive = Boolean.parseBoolean(viewParts[index++]);

		mRevealed = Boolean.parseBoolean(viewParts[index++]);
		// noinspection UnusedAssignment
		mClearRedundantPossiblesInSameRowOrColumnCount = Integer
				.parseInt(viewParts[index++]);

		return true;
	}

	/**
	 * Creates the grid with given information. Returns whether the grid was
	 * successfully inserted into the database (true), or an error occurred in
	 * the process (false).
	 * 
	 * @param gridSize
	 *            The size of grid.
	 * @param cells
	 *            The list of cell used in the grid.
	 * @param cages
	 *            The list of cages used in the grid.
	 * @param gridGeneratingParameters
	 *            The parameters used to create the grid.
	 */
	public boolean create(int gridSize, ArrayList<GridCell> cells,
			ArrayList<GridCage> cages,
			GridGeneratingParameters gridGeneratingParameters) {

		// In case an existing grid object is reused, we have to clean up old
		// data
		if (this.mMoves != null) {
			this.mMoves.clear();
		}
		mSelectedCell = null;
		mRevealed = false;
		mSolvingAttemptId = -1;
		mRowId = -1;
		mClearRedundantPossiblesInSameRowOrColumnCount = 0;
		mSolvedListener = null;
		mGridStatistics = mGridInitializer.createGridStatistics();

		// Set new data in grid
		mGridGeneratingParameters = gridGeneratingParameters;
		mDateCreated = System.currentTimeMillis();
		mGridSize = gridSize;
		mCells = cells;
		mCages = cages;
		mActive = true;

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

	public GridGeneratingParameters getGridGeneratingParameters() {
		return mGridGeneratingParameters;
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
					if (Config.mAppMode == AppMode.DEVELOPMENT) {
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
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
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
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
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
	boolean loadStatistics() {
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
	@SuppressWarnings("UnusedReturnValue")
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
	 * @return True in case everything has been saved. False otherwise.
	 */
	private boolean save(boolean saveDueToUpgrade) {
		boolean saved;

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
			saved = (mGridStatistics != null && mGridStatistics.save());

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
	 * Load a solving attempt and the corresponding grid from the database.
	 * 
	 * @param solvingAttemptId
	 *            The unique id of the solving attempt which has to be loaded.
	 * @return True in case the grid has been loaded successfully. False
	 *         otherwise.
	 */
	public boolean load(int solvingAttemptId) throws InvalidGridException {
		// First load the solving attempt to get the grid id.
		SolvingAttemptData solvingAttemptData = new SolvingAttemptDatabaseAdapter()
				.getData(solvingAttemptId);
		if (solvingAttemptData == null) {
			return false;
		}

		// Load the grid before processing the solving attempt data.
		GridRow gridRow = new GridDatabaseAdapter()
				.get(solvingAttemptData.mGridId);
		if (gridRow != null) {
			mGridSize = gridRow.mGridSize;
			mGridGeneratingParameters = gridRow.mGridGeneratingParameters;
		}

		// Load the data from the solving attempt into the grid object.
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
		// solvingAttemptData.
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
				GridCell cell = mGridInitializer.createGridCell(this, 0);
				if (!cell.fromStorageString(line,
						solvingAttemptData.mSavedWithRevision)) {
					throw new InvalidGridException(
							"Line does not contain cell information while this was expected:"
									+ line);
				}
				mCells.add(cell);
				if (selectedCell == null && cell.isSelected()) {
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
				String invalidList = line
						.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1)[1];
				for (String cellId : invalidList
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
			GridCage cage = mGridInitializer.createGridCage(this);
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
				cage = mGridInitializer.createGridCage(this);
			} while (line != null
					&& cage.fromStorageString(line,
							solvingAttemptData.mSavedWithRevision));

			// Check cage maths after all cages have been read.
			setBordersForCagesWithIncorrectMaths();

			// Set the selected cell (and indirectly the selected cage).
			if (selectedCell != null) {
				setSelectedCell(selectedCell);
			}

			// Remaining lines contain cell changes (zero or more expected)
			CellChange cellChange = mGridInitializer.createCellChange();
			while (line != null
					&& cellChange.fromStorageString(line, mCells,
							solvingAttemptData.mSavedWithRevision)) {
				addMove(cellChange);

				// Read next line. No checking of unexpected end of file might
				// be done here because the last line in a file can contain a
				// cage.
				line = solvingAttemptData.getNextLine();

				// Create a new empty cell change
				cellChange = mGridInitializer.createCellChange();
			}

			// Check if end of file is reached an no information was unread yet.
			if (line != null) {
				throw new InvalidGridException(
						"Unexpected line found while end of file was expected: "
								+ line);
			}

			// Mark cells with duplicate values
			for (GridCell gridCell : mCells) {
				gridCell.markDuplicateValuesInSameRowAndColumn();
			}

			// The solving attempt has been loaded successfully into the grid
			// object
			mSolvingAttemptId = solvingAttemptData.mId;
			mRowId = solvingAttemptData.mGridId;
		} catch (InvalidGridException e) {
			loaded = false;
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
				Log.d(TAG,
						"Invalid file format error when  restoring solving attempt with id '"
								+ solvingAttemptData.mId + "'\n"
								+ e.getMessage());
			}
			initialize();
		} catch (NumberFormatException e) {
			loaded = false;
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
				Log.d(TAG,
						"Number format error when  restoring solving attempt with id '"
								+ solvingAttemptData.mId + "'\n"
								+ e.getMessage());
			}
			initialize();
		} catch (IndexOutOfBoundsException e) {
			loaded = false;
			if (Config.mAppMode == AppMode.DEVELOPMENT) {
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
		deselectSelectedCell();

		// The solving attempt is not yet saved.
		mDateLastSaved = 0;

		// Forget it if the solution was revealed before.
		mRevealed = false;

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
		// 1:00010202000103040506030405060707:0,4,1:1,2,4:2,2,4:3,1,2:4,4,4:5,2,4:6,4,1:7,6,3

		// Split the definition into parts.
		String[] definitionParts = definition.split(":");
		if (definitionParts == null) {
			return false;
		}

		// The definition contains followings parts:
		int ID_PART_COMPLEXITY = 0;
		int ID_PART_CELLS = 1;
		int ID_PART_FIRST_CAGE = 2;
		int ID_PART_LAST_CAGE = definitionParts.length - 1;
		if (ID_PART_LAST_CAGE < ID_PART_FIRST_CAGE) {
			return false;
		}

		// Create an empty cage for each cage part. The cages needs to exists
		// before the cell can be created.
		int cageIndex = 0;
		for (int i = ID_PART_FIRST_CAGE; i <= ID_PART_LAST_CAGE; i++) {
			// Define new cage
			GridCage gridCage = mGridInitializer.createGridCage(this);
			gridCage.setCageId(cageIndex++);

			// Add cage to cages list
			if (mCages.add(gridCage) == false) {
				return false;
			}
		}

		// Retrieve the complexity from the definition. Convert it back to the
		// enumeration. Note that values are not consistent with the ordinal
		// values of the enumeration.
		// The complexity is not needed to rebuild the puzzle, but it is stored
		// as it is a great communicator to the (receiving) user how difficult
		// the puzzle is.
		switch (Integer.parseInt(definitionParts[ID_PART_COMPLEXITY])) {
		case 1:
			mGridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.VERY_EASY;
			break;
		case 2:
			mGridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.EASY;
			break;
		case 3:
			mGridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.NORMAL;
			break;
		case 4:
			mGridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.DIFFICULT;
			break;
		case 5:
			mGridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.VERY_DIFFICULT;
			break;
		default:
			// This value can not be specified in a share url created by the
			// app. But in case it is manipulated by a user before sending to
			// another user, the receiver should not get an exception.
			return false;
		}

		// The first part of the definitions contains the cage number for each
		// individual cell. The cage number always consists of two digits.
		int cellCount = definitionParts[ID_PART_CELLS].length() / 2;
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
		Matcher matcher = pattern.matcher(definitionParts[ID_PART_CELLS]);
		int cellNumber = 0;
		while (matcher.find()) {
			int cageId = Integer.valueOf(matcher.group());

			// Create new cell and add it to the cells list.
			GridCell gridCell = mGridInitializer.createGridCell(this,
					cellNumber++);
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
		for (int i = ID_PART_FIRST_CAGE; i <= ID_PART_LAST_CAGE; i++) {
			// Split the cage part into elements
			String[] cageElements = definitionParts[i].split(",");

			// Get the cage
			GridCage gridCage = mCages.get(i - ID_PART_FIRST_CAGE);
			if (gridCage == null) {
				return false;
			}
			gridCage.setCageResults(Integer.valueOf(cageElements[1]),
					Integer.valueOf(cageElements[2]), false);
		}

		// Check whether a single solution can be found.
		int[][] solution = mGridInitializer
				.createMathDokuDLX(mGridSize, mCages).getSolutionGrid();
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
		setBordersForCagesWithIncorrectMaths();

		return true;
	}

	/**
	 * Get the puzzle complexity which is used to generate this puzzle.
	 * 
	 * @return The puzzle complexity which is used to generate this puzzle.
	 */
	public PuzzleComplexity getPuzzleComplexity() {
		return mGridGeneratingParameters.mPuzzleComplexity;
	}

	/**
	 * Highlight those cells where the user has made a mistake.
	 * 
	 * @return The number of cells which have been marked as invalid. Cells
	 *         which were already marked as invalid will not be counted again.
	 */
	public int markInvalidChoices() {
		int countNewInvalids = 0;

		increaseCounter(StatisticsCounterType.ACTION_CHECK_PROGRESS);
		for (GridCell cell : mCells) {
			// Check all cells having a value and not (yet) marked as invalid.
			if (cell.isUserValueSet() && !cell.hasInvalidUserValueHighlight()) {
				if (cell.getUserValue() != cell.getCorrectValue()) {
					cell.setInvalidHighlight();
					increaseCounter(StatisticsCounterType.CHECK_PROGRESS_INVALIDS_CELLS_FOUND);
					countNewInvalids++;
				}
			}
		}
		if (countNewInvalids > 0) {
			mGridStatistics.increaseCounter(
					StatisticsCounterType.CHECK_PROGRESS_INVALIDS_CELLS_FOUND,
					countNewInvalids);
		}

		return countNewInvalids;
	}

	/**
	 * Checks whether the current grid is being replayed.
	 * 
	 * @return True in case of a replay. False otherwise.
	 */
	public boolean isReplay() {
		return (mGridStatistics != null && (mGridStatistics.getReplayCount() > 0));

	}

	/**
	 * Set borders of all cages having incorrect maths.
	 */
	private void setBordersForCagesWithIncorrectMaths() {
		if (mCages != null) {
			for (GridCage cage : mCages) {
				if (cage.isMathsCorrect()) {
					cage.setBorders();
				}
			}
		}
	}
}