package net.mathdoku.plus.grid;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.GridStatistics.StatisticsCounterType;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grid {
	private static final String TAG = "MathDoku.Grid";

	// ************************************************************************
	// Grid variables which are determined when generating the grid and which do
	// not alter anymore.
	// ************************************************************************

	// Unique row id of grid in database
	private int mRowId;

	// Size of the grid
	// TODO: make final after methods create and load have been altered to
	// builder pattern.
	private int mGridSize;

	// All parameters that influence the game generation and which are needed to
	// regenerate a specific game again.
	private long mDateCreated;
	private GridGeneratingParameters mGridGeneratingParameters;

	// ************************************************************************
	// Grid elements and references which do change while solving the game.
	// ************************************************************************

	private long mDateUpdated;

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

	private GridObjectsCreator mGridObjectsCreator;

	/**
	 * Prevent the Grid from being instantiated directly. To create a new
	 * instance of {@link net.mathdoku.plus.grid.Grid} the GridBuilder has to be
	 * used.
	 */
	// TODO: change accessor to private as method load has been rewritten to use
	// the GridBuilder
	public Grid() {
		throw new InvalidGridException(
				"Grid can only be instantiated via the GridBuilder.");
	}

	public Grid(GridBuilder gridBuilder) {
		// Set default for variables which can not be set via the builder
		mSelectedCell = null;
		mSolvedListener = null;

		// Get defaults from builder
		mGridObjectsCreator = gridBuilder.mGridObjectsCreator;
		mGridSize = gridBuilder.mGridSize;
		mGridGeneratingParameters = gridBuilder.mGridGeneratingParameters;
		mGridStatistics = gridBuilder.mGridStatistics;
		mDateCreated = gridBuilder.mDateCreated;
		mDateUpdated = gridBuilder.mDateUpdated;
		mRowId = gridBuilder.mGridId;
		mSolvingAttemptId = gridBuilder.mSolvingAttemptId;
		mCells = gridBuilder.mCells;
		mCages = gridBuilder.mCages;
		mMoves = gridBuilder.mCellChanges;
		mActive = gridBuilder.mActive;
		mRevealed = gridBuilder.mRevealed;

		// Check if required parameters are specified
		if (mGridObjectsCreator == null) {
			throw new InvalidGridException(
					"Cannot create a grid if GridObjectsCreator is null.");
		}
		if (mGridSize < 1 || mGridSize > 9) {
			throw new InvalidGridException("GridSize " + mGridSize
					+ " is not a valid grid size.");
		}
		if (Util.isArrayListNullOrEmpty(mCells)) {
			throw new InvalidGridException(
					"Cannot create a grid without a list of cells. mCells = "
							+ (mCells == null ? "null" : "empty list"));
		}
		if (mCells.size() != mGridSize * mGridSize) {
			throw new InvalidGridException(
					"Cannot create a grid if number of cells does not match with grid size. Expected "
							+ (mGridSize * mGridSize)
							+ " cells, got "
							+ mCells.size() + " cells.");
		}
		if (Util.isArrayListNullOrEmpty(mCages)) {
			throw new InvalidGridException(
					"Cannot create a grid without a list of cages. mCages = "
							+ (mCages == null ? "null" : "empty list"));
		}
		if (mGridGeneratingParameters == null) {
			throw new InvalidGridException(
					"Cannot create a grid without gridGeneratingParameters.");
		}

		for (GridCage gridCage : mCages) {
			gridCage.setGridReference(this);
		}
		for (GridCell gridCell : mCells) {
			gridCell.setGridReference(this);
		}

		checkUserMathForAllCages();
		for (GridCell gridCell : mCells) {
			gridCell.markDuplicateValuesInSameRowAndColumn();
		}
		for (GridCell gridCell : mCells) {
			if (gridCell.isSelected()) {
				// The first cell which is marked as selected, is set as
				// selected cell for the grid.
				setSelectedCell(gridCell);
				break;
			}
		}

		if (mGridStatistics == null) {
			mGridStatistics = mGridObjectsCreator.createGridStatistics();
		}

		setPreferences();
	}

	/**
	 * Initializes a new grid object.
	 * 
	 */
	// TODO: remove when GridBuild patter has been implemented on methods which
	// currently invoke this method.
	void initialize() {
		mRowId = -1;
		mSolvingAttemptId = -1;
		mGridSize = 0;
		mCells = mGridObjectsCreator.createArrayListOfGridCells();
		mCages = mGridObjectsCreator.createArrayListOfGridCages();
		mMoves = mGridObjectsCreator.createArrayListOfCellChanges();
		mSolvedListener = null;
		mGridGeneratingParameters = mGridObjectsCreator
				.createGridGeneratingParameters();
		mGridStatistics = mGridObjectsCreator.createGridStatistics();
		setPreferences();
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
	public GridCage getSelectedCage() {
		if (mSelectedCell == null) {
			return null;
		}

		return mSelectedCell.getCage();
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
		checkUserMathForAllCages();
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
	public boolean unrevealSolution() {
		if (Config.mAppMode == AppMode.DEVELOPMENT) {
			mRevealed = false;
			if (mGridStatistics != null) {
				mGridStatistics.mSolutionRevealed = false;
				mGridStatistics.mSolvedManually = true;
			}
			return true;
		}
		return false;
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
			mMoves = mGridObjectsCreator.createArrayListOfCellChanges();
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

		// Get the last move
		int undoPosition = mMoves.size() - 1;
		CellChange cellChange = mMoves.get(undoPosition);

		// Remember current situation before restoring the last move
		GridCell cellChangeGridCell = cellChange.getGridCell();
		int userValueBeforeUndo = cellChangeGridCell.getUserValue();

		// Restore the last cell change in the list of moves
		cellChange.restore();

		mMoves.remove(undoPosition);

		mGridStatistics.increaseCounter(StatisticsCounterType.ACTION_UNDO_MOVE);

		// Set the cell to which the cell change applies as selected cell.
		setSelectedCell(cellChangeGridCell);

		if (userValueBeforeUndo != cellChangeGridCell.getUserValue()) {
			// Each cell in the same column or row as the restored cell, has to
			// be checked for duplicate values.
			GridCellSelectorInRowOrColumn gridCellSelectorInRowOrColumn = mGridObjectsCreator
					.createGridCellSelectorInRowOrColumn(mCells,
							cellChangeGridCell.getRow(),
							cellChangeGridCell.getColumn());
			ArrayList<GridCell> gridCellsInSameRowOrColumn = gridCellSelectorInRowOrColumn
					.find();
			if (gridCellsInSameRowOrColumn != null) {
				for (GridCell gridCellInSameRowOrColumn : gridCellsInSameRowOrColumn) {
					gridCellInSameRowOrColumn
							.markDuplicateValuesInSameRowAndColumn();
				}
			}

			// Check the cage math
			GridCage gridCage = cellChangeGridCell.getCage();
			if (gridCage != null) {
				gridCage.checkUserMath();
			}
		}

		// Undo successful completed.
		return true;
	}

	/**
	 * Deselect the selected cell.
	 */
	public void deselectSelectedCell() {
		if (mSelectedCell == null) {
			// Nothing to do in case no cell is selected.
			return;
		}

		// Remember cage which is currently selected.
		GridCage oldSelectedCage = mSelectedCell.getCage();

		// Deselect the cell itself
		mSelectedCell.deselect();
		mSelectedCell = null;

		// Update borders of cage which was selected before.
		oldSelectedCage.setBorders();
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
		if (cell == null) {
			deselectSelectedCell();
			return null;
		}

		GridCage oldSelectedCage = null;
		if (mSelectedCell != null) {
			oldSelectedCage = mSelectedCell.getCage();
			mSelectedCell.deselect();
		}
		// Determine new cage
		GridCage newSelectedCage = cell.getCage();

		// Select the new cell
		cell.select();
		mSelectedCell = cell;

		// Set borders if another cage is selected.
		if (newSelectedCage.equals(oldSelectedCage) == false) {
			if (oldSelectedCage != null) {
				oldSelectedCage.setBorders();
			}
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

		if (Util.isArrayListNullOrEmpty(cells)) {
			throw new InvalidParameterException(
					"Parameter cells cannot be null or empty list.");
		}
		if (Util.isArrayListNullOrEmpty(cages)) {
			throw new InvalidParameterException(
					"Parameter cages cannot be null or empty list.");
		}
		if (gridGeneratingParameters == null) {
			throw new InvalidParameterException(
					"Parameter gridGeneratingParameters cannot be null.");
		}

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
					.append(cage.getId())
					.append(",")
					.append(cage.getResult())
					.append(",")
					.append(gridGeneratingParameters.mHideOperators ? CageOperator.NONE
							.getId() : cage.getOperator().getId());
		}
		return definitionString.toString();
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
		return mDateUpdated;
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
		DatabaseHelper databaseHelper = mGridObjectsCreator
				.createDatabaseHelper();
		databaseHelper.beginTransaction();

		// Insert grid record if it does not yet exists. The grid record never
		// needs to be updated as the definition is immutable.
		if (mRowId < 0) {
			// Before insert first check if already a grid record exists for the
			// grid definition. If so, then reuse the existing grid definition.
			GridDatabaseAdapter gridDatabaseAdapter = mGridObjectsCreator
					.createGridDatabaseAdapter();
			GridRow gridRow = gridDatabaseAdapter
					.getByGridDefinition(toGridDefinitionString());
			mRowId = (gridRow == null ? gridDatabaseAdapter.insert(this)
					: gridRow.mId);
			if (mRowId < 0) {
				// Insert of new Grid record failed.
				databaseHelper.endTransaction();
				return false;
			}
		}

		// Insert or update the solving attempt.
		SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = mGridObjectsCreator
				.createSolvingAttemptDatabaseAdapter();
		if (mSolvingAttemptId < 0) {
			mSolvingAttemptId = solvingAttemptDatabaseAdapter.insert(this,
					Util.getPackageVersionNumber());
			if (mSolvingAttemptId < 0) {
				// Insert of new solving attempt failed.
				databaseHelper.endTransaction();
				return false;
			}
		} else if (solvingAttemptDatabaseAdapter
				.update(mSolvingAttemptId, this) == false) {
			// Update of solving attempt failed.
			databaseHelper.endTransaction();
			return false;
		}

		// Insert or update the grid statistics.
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = mGridObjectsCreator
				.createStatisticsDatabaseAdapter();
		if (mGridStatistics.mId < 0) {
			mGridStatistics = statisticsDatabaseAdapter.insert(this);
			if (mGridStatistics == null || mGridStatistics.mId < 0) {
				// Insert of new grid statistics failed.
				databaseHelper.endTransaction();
				return false;
			}
		} else {
			if (mGridStatistics.save() == false) {
				// Update of grid statistics failed.
				databaseHelper.endTransaction();
				return false;
			}

			// In case a replay of the grid is finished the statistics which
			// have to included in the cumulative and the historic statistics
			// should be changed to the current solving attempt.
			if (mActive == false && mGridStatistics.getReplayCount() > 0
					&& mGridStatistics.isIncludedInStatistics() == false) {
				// Note: do not return false in case following fails as it is
				// not relevant to the user.
				statisticsDatabaseAdapter
						.updateSolvingAttemptToBeIncludedInStatistics(mRowId,
								mSolvingAttemptId);
			}
		}

		// Commit and close transaction
		databaseHelper.setTransactionSuccessful();
		databaseHelper.endTransaction();

		return true;
	}

	/**
	 * Upgrade this grid (solving attempt and statistics) to the current app
	 * version.
	 * 
	 * @return True in case everything has been saved. False otherwise.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean saveOnAppUpgrade() {
		synchronized (mLock) {
			// The solving attempt was already created as soon as the grid was
			// created first. So only an update is needed.
			SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter = mGridObjectsCreator
					.createSolvingAttemptDatabaseAdapter();
			if (solvingAttemptDatabaseAdapter.updateOnAppUpgrade(
					mSolvingAttemptId, this) == false) {
				return false;
			}

			if (mGridStatistics != null && mGridStatistics.save() == false) {
				return false;
			}

		}

		return true;
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

		// No cell may be selected.
		deselectSelectedCell();

		// The solving attempt is not yet saved.
		mDateUpdated = 0;

		// Forget it if the solution was revealed before.
		mRevealed = false;

		// Make the grid active again.
		mActive = true;

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
			GridCage gridCage = mGridObjectsCreator.createGridCage();
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
			GridCell gridCell = mGridObjectsCreator.createGridCell(
					cellNumber++, mGridSize);
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
					CageOperator.fromId(cageElements[2]), false);
		}

		// Check whether a single solution can be found.
		int[][] solution = mGridObjectsCreator.createMathDokuDLX(mGridSize,
				mCages).getSolutionGrid();
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
		checkUserMathForAllCages();

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

		mGridStatistics
				.increaseCounter(StatisticsCounterType.ACTION_CHECK_PROGRESS);
		for (GridCell cell : mCells) {
			// Check all cells having a value and not (yet) marked as invalid.
			if (cell.isUserValueSet() && !cell.hasInvalidUserValueHighlight()) {
				if (cell.getUserValue() != cell.getCorrectValue()) {
					cell.setInvalidHighlight();
					mGridStatistics
							.increaseCounter(StatisticsCounterType.CHECK_PROGRESS_INVALIDS_CELLS_FOUND);
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
	/* package private */void checkUserMathForAllCages() {
		if (mCages != null) {
			for (GridCage cage : mCages) {
				cage.checkUserMath();
			}
		}
	}

	/**
	 * Reveals the user value of the selected cell.
	 * 
	 * @return True if the user value is revealed. False otherwise.
	 */
	public boolean revealSelectedCell() {
		GridCell selectedCell = getSelectedCell();
		if (selectedCell == null) {
			return false;
		}

		// Save old cell info
		CellChange originalUserMove = selectedCell.saveUndoInformation(null);

		// Reveal the user value
		selectedCell.setRevealed();
		selectedCell.setUserValue(selectedCell.getCorrectValue());

		if (Preferences.getInstance().isPuzzleSettingClearMaybesEnabled()) {
			// Update possible values for other cells in this row and
			// column.
			clearRedundantPossiblesInSameRowOrColumn(originalUserMove);
		}

		mGridStatistics
				.increaseCounter(StatisticsCounterType.ACTION_REVEAL_CELL);

		return true;
	}

	/**
	 * Reveals the operator of cage for the selected cell.
	 * 
	 * @return True in case the operator of the cage is revealed. False
	 *         otherwise.
	 */
	public boolean revealOperatorSelectedCage() {
		GridCage selectedGridCage = getSelectedCage();
		if (selectedGridCage == null) {
			return false;
		}

		selectedGridCage.revealOperator();

		mGridStatistics
				.increaseCounter(StatisticsCounterType.ACTION_REVEAL_OPERATOR);

		return true;
	}

	/* package private */void setSolvingAttemptId(int solvingAttemptId) {
		mSolvingAttemptId = solvingAttemptId;
	}

	/* package private */void setRevealed(boolean revealed) {
		mRevealed = revealed;
	}

	public ArrayList<CellChange> getCellChanges() {
		// Copy the entire ArrayList to a new instance. The ObjectsCreator
		// should not be used.
		return (mMoves == null ? null : new ArrayList(mMoves));
	}
}
