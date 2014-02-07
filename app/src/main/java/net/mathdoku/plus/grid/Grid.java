package net.mathdoku.plus.grid;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridDefinition.GridDefinition;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.GridStatistics.StatisticsCounterType;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import java.util.ArrayList;

public class Grid {
	private static final String TAG = "MathDoku.Grid";

	// Unique row id of grid in database
	private int mRowId;

	/**
	 * Grid variables below are determined when generating and should not be
	 * altered after being set. These variables can only be set via
	 * {@link net.mathdoku.plus.grid.Grid#Grid(GridBuilder)}.
	 */
	private final int mGridSize;
	private final long mDateCreated;
	private final GridGeneratingParameters mGridGeneratingParameters;
	private final GridObjectsCreator mGridObjectsCreator;

	// Be careful: although mCages and mCell are final variables, the content of
	// those list can still be altered!
	public final ArrayList<GridCage> mCages;
	public final ArrayList<GridCell> mCells;

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

	/**
	 * Prevent the Grid from being instantiated directly. To create a new
	 * instance of {@link net.mathdoku.plus.grid.Grid} the GridBuilder has to be
	 * used.
	 */
	private Grid() {
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

		for (GridCage gridCage : mCages) {
			setCageTextToUpperLeftCell(gridCage);
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
	 */
	public void clearCells() {
		if (this.mMoves != null) {
			this.mMoves.clear();
		}
		if (mCells != null) {
			boolean updateGridClearCounter = false;
			for (GridCell cell : this.mCells) {
				if (cell.getUserValue() != 0 || cell.countPossibles() > 0) {
					updateGridClearCounter = true;
				}
				cell.clearValue();
			}
			if (updateGridClearCounter) {
				mGridStatistics
						.increaseCounter(StatisticsCounterType.ACTION_CLEAR_GRID);
			}
		}

		// clear cages to remove the border related to bad cage maths.
		checkUserMathForAllCages();
	}

	/* Fetch the cell with the id. */
	public GridCell getCell(int id) {
		if (mCells == null || id < 0 || id >= mCells.size()) {
			return null;
		}

		return this.mCells.get(id);
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
		if (mGridGeneratingParameters != null
				&& mGridGeneratingParameters.mHideOperators && mCages != null) {
			for (GridCage gridCage : mCages) {
				gridCage.revealOperator();
				setCageTextToUpperLeftCell(gridCage);
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

	/**
	 * Check if user has revealed the solution of this puzzle.
	 * 
	 * @return True in case the user has solved the puzzle by requesting the
	 *         solution. False otherwise.
	 */
	public boolean isSolutionRevealed() {
		return this.mRevealed;
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
			String gridDefinition = GridDefinition.getDefinition(this);
			GridDatabaseAdapter gridDatabaseAdapter = mGridObjectsCreator
					.createGridDatabaseAdapter();
			GridRow gridRow = gridDatabaseAdapter
					.getByGridDefinition(gridDefinition);
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
	 * Checks if the grid does not contain any user value. It is not relevant
	 * whether the grid does contain maybe values.
	 * 
	 * @return True in case no user values have been filled in. False in case at
	 *         least one user value is filled in.
	 */
	public boolean containsNoUserValues() {
		for (GridCell cell : mCells) {
			if (cell.isUserValueSet()) {
				return false;
			}
		}

		// No user values found
		return true;
	}

	/**
	 * Checks if the grid is empty (i.e. cells do not contain a user value nor a
	 * possible value).
	 * 
	 * @return True in case the grid is empty. False otherwise
	 */
	public boolean isEmpty() {
		for (GridCell cell : mCells) {
			if (cell.isUserValueSet() || cell.countPossibles() > 0) {
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
	public Grid createNewGridForReplay() {
		// Copy all cells without the play history
		ArrayList<GridCell> cells = new ArrayList<GridCell>();
		for (GridCell cell : mCells) {
			GridCell gridCell = new GridCell(cell.getCellId(), mGridSize);
			gridCell.setCorrectValue(cell.getCorrectValue());
			gridCell.setCageId(cell.getCageId());
			cells.add(gridCell);
		}

		// Copy all cages without the play history
		ArrayList<GridCage> cages = new ArrayList<GridCage>();
		for (GridCage cage : mCages) {
			CageBuilder cageBuilder = mGridObjectsCreator.createCageBuilder();
			GridCage gridCage = cageBuilder
					.setId(cage.getId())
					.setResult(cage.getResult())
					.setCageOperator(cage.getOperator())
					.setHideOperator(mGridGeneratingParameters.mHideOperators)
					.setCells(cage.getCells())
					.build();
			cages.add(gridCage);
		}

		// Create a new grid
		return mGridObjectsCreator
				.createGridBuilder()
				.setGridSize(mGridSize)
				.setGridId(mRowId)
				.setGridGeneratingParameters(mGridGeneratingParameters)
				.setCells(cells)
				.setCages(cages)
				.build();
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
	private void checkUserMathForAllCages() {
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
		if (selectedGridCage.isOperatorHidden() == false) {
			// Operator is already visible.
			return false;
		}

		selectedGridCage.revealOperator();
		setCageTextToUpperLeftCell(selectedGridCage);

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

	/**
	 * Gets the user values for a given list of cells.
	 * 
	 * @param cells
	 *            The list of cells for which the user values have to be
	 *            returned.
	 * @return The user values which are filled in for the given cells. Null in
	 *         case of an error. In case the returned arrays contains less
	 *         elements than the number of cells given this indicates that not
	 *         all cells have a user value.
	 */
	public ArrayList<Integer> getUserValuesForCells(int[] cells) {
		if (Util.isArrayNullOrEmpty(cells)) {
			return null;
		}

		ArrayList<Integer> userValues = new ArrayList<Integer>();
		for (int i = 0; i < cells.length; i++) {
			int cell = cells[i];
			if (cell < 0 || cell > mCells.size()) {
				return null;
			}
			GridCell gridCell = mCells.get(cell);
			if (gridCell == null) {
				return null;
			}
			if (gridCell.isUserValueSet()) {
				userValues.add(gridCell.getUserValue());
			}
		}

		return userValues;
	}

	public boolean setBorderForCells(int[] cells) {
		if (Util.isArrayNullOrEmpty(cells)) {
			return false;
		}

		for (int i = 0; i < cells.length; i++) {
			int cell = cells[i];
			if (cell < 0 || cell > mCells.size()) {
				return false;
			}
			GridCell gridCell = mCells.get(cell);
			if (gridCell == null) {
				return false;
			}
			gridCell.setBorders();
		}

		return true;
	}

	public ArrayList<GridCell> getGridCells(int[] cells) {
		if (Util.isArrayNullOrEmpty(cells)) {
			return null;
		}

		ArrayList<GridCell> gridCells = new ArrayList<GridCell>();
		for (int i = 0; i < cells.length; i++) {
			int cell = cells[i];
			if (cell < 0 || cell > mCells.size()) {
				return null;
			}
			GridCell gridCell = mCells.get(cell);
			if (gridCell == null) {
				return null;
			}
			gridCells.add(gridCell);
		}

		return gridCells;
	}

	private void setCageTextToUpperLeftCell(GridCage gridCage) {
		if (gridCage != null) {
			int idUpperLeftCell = gridCage.getIdUpperLeftCell();
			GridCell gridCell = getCell(idUpperLeftCell);
			if (gridCell != null) {
				gridCell.setCageText(gridCage.getCageText());
			}
		}
	}
}
