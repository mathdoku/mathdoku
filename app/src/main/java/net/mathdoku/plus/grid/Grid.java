package net.mathdoku.plus.grid;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridDefinition.GridDefinition;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.GridStatistics.StatisticsCounterType;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Grid {
	@SuppressWarnings("unused")
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
	private final Grid.ObjectsCreator mObjectsCreator;
	private final List<Cage> mCages;
	private final List<Cell> mCells;

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
	private Cell mSelectedCell;

	// Statistics for this grid
	private GridStatistics mGridStatistics;

	// Keep track of all moves as soon as grid is built or restored.
	private List<CellChange> mMoves;

	// ************************************************************************
	// Miscellaneous
	// ************************************************************************

	// The solving attempt which is merged into this grid.
	private int mSolvingAttemptId;

	// Used to avoid redrawing or saving grid during creation of new grid
	private final Object mLock = new Object();

	// Solved listener
	private OnSolvedListener mSolvedListener;

	public static class ObjectsCreator {
		public GridStatistics createGridStatistics() {
			return new GridStatistics();
		}

		public GridGeneratingParameters createGridGeneratingParameters() {
			return new GridGeneratingParameters();
		}

		public List<Cell> createArrayListOfCells() {
			return new ArrayList<Cell>();
		}

		public List<Cage> createArrayListOfCages() {
			return new ArrayList<Cage>();
		}

		public List<CellChange> createArrayListOfCellChanges() {
			return new ArrayList<CellChange>();
		}

		public CellSelectorInRowOrColumn createCellSelectorInRowOrColumn(
				List<Cell> cells, int row, int column) {
			return new CellSelectorInRowOrColumn(cells, row, column);
		}

		public SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
			return new SolvingAttemptDatabaseAdapter();
		}

		public GridBuilder createGridBuilder() {
			return new GridBuilder();
		}

		public CageBuilder createCageBuilder() {
			return new CageBuilder();
		}

		public GridSaver createGridSaver() {
			return new GridSaver();
		}

		public CellBuilder createCellBuilder() {
			return new CellBuilder();
		}

		public CellChange createCellChange(Cell selectedCell) {
			return new CellChange(selectedCell);
		}
	}

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
		mObjectsCreator = gridBuilder.mGridObjectsCreator == null ? new Grid.ObjectsCreator()
				: gridBuilder.mGridObjectsCreator;
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

		validateGridObjectsCreatorThrowsExceptionOnError();
		validateGridSizeThrowsExceptionOnError();
		validateCellsThrowsExceptionOnError();
		validateCagesThrowsExceptionOnError();
		validateGridGeneratingParametersThrowsExceptionOnError();
		setGridReferences();
		setCageTextToUpperLeftCellAllCages();
		checkUserMathForAllCages();
		markDuplicateValues();
		setSelectedCell(findFirstSelectedCell());

		if (mGridStatistics == null) {
			mGridStatistics = mObjectsCreator.createGridStatistics();
		}
	}

	private Cell findFirstSelectedCell() {
		for (Cell cell : mCells) {
			if (cell.isSelected()) {
				return cell;
			}
		}
		return null;
	}

	private void markDuplicateValues() {
		for (Cell cell : mCells) {
			cell.markDuplicateValuesInSameRowAndColumn();
		}
	}

	private void setGridReferences() {
		for (Cage cage : mCages) {
			cage.setGridReference(this);
		}
		for (Cell cell : mCells) {
			cell.setGridReference(this);
		}
	}

	private void validateGridObjectsCreatorThrowsExceptionOnError() {
		if (mObjectsCreator == null) {
			throw new InvalidGridException(
					"Cannot create a grid if mObjectsCreator is null.");
		}
	}

	private void validateGridSizeThrowsExceptionOnError() {
		if (mGridSize < 1 || mGridSize > 9) {
			throw new InvalidGridException("GridSize " + mGridSize
					+ " is not a valid grid size.");
		}
	}

	private void validateCellsThrowsExceptionOnError() {
		if (Util.isListNullOrEmpty(mCells)) {
			throw new InvalidGridException(
					"Cannot create a grid without a list of cells. mCells is null or empty.");
		}
		if (mCells.size() != mGridSize * mGridSize) {
			throw new InvalidGridException(
					"Cannot create a grid if number of cells does not match with grid size. Expected "
							+ (mGridSize * mGridSize)
							+ " cells, got "
							+ mCells.size() + " cells.");
		}
	}

	private void validateCagesThrowsExceptionOnError() {
		if (Util.isListNullOrEmpty(mCages)) {
			throw new InvalidGridException(
					"Cannot create a grid without a list of cages. mCages is null or empty.");
		}
	}

	private void validateGridGeneratingParametersThrowsExceptionOnError() {
		if (mGridGeneratingParameters == null) {
			throw new InvalidGridException(
					"Cannot create a grid without gridGeneratingParameters.");
		}
	}

	/**
	 * Get the cage of the cell which is currently selected.
	 * 
	 * @return The cage to which the currently selected cell belongs. Null in
	 *         case no cell is selected or no cage exists.
	 */
	public Cage getSelectedCage() {
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
			for (Cell cell : this.mCells) {
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

		// Clear cages to remove the border related to bad cage maths.
		checkUserMathForAllCages();
	}

	/* Fetch the cell with the id. */
	public Cell getCell(int id) {
		if (mCells == null || id < 0 || id >= mCells.size()) {
			return null;
		}

		return this.mCells.get(id);
	}

	/* Fetch the cell at the given row, column */
	public Cell getCellAt(int row, int column) {
		if (row < 0 || row >= mGridSize) {
			return null;
		}
		if (column < 0 || column >= mGridSize) {
			return null;
		}

		return this.mCells.get(column + row * this.mGridSize);
	}

	/**
	 * Reveal the solution by setting the user value to the actual value.
	 */
	public void revealSolution() {
		this.mRevealed = true;
		if (mCells != null) {
			for (Cell cell : this.mCells) {
				if (cell.isUserValueIncorrect()) {
					cell.setRevealed();
					cell.setUserValue(cell.getCorrectValue());
				}
			}
		}
		if (mGridGeneratingParameters != null
				&& mGridGeneratingParameters.mHideOperators && mCages != null) {
			for (Cage cage : mCages) {
				cage.revealOperator();
				setCageTextToUpperLeftCell(cage);
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
		for (Cell cell : this.mCells) {
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
		for (Cell cell : this.mCells) {
			if (cell.isUserValueSet() && cell.isUserValueIncorrect()) {
				return false;
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
			mMoves = mObjectsCreator.createArrayListOfCellChanges();
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
		return mMoves == null ? 0 : mMoves.size();
	}

	public boolean undoLastMove() {
		// Check if list contains at least one move
		if (Util.isListNullOrEmpty(mMoves)) {
			// Cannot undo on non existing list.
			return false;
		}

		// Get the last move
		int undoPosition = mMoves.size() - 1;
		CellChange cellChange = mMoves.get(undoPosition);

		// Remember current situation before restoring the last move
		Cell affectedCell = cellChange.getCell();
		int userValueBeforeUndo = affectedCell.getUserValue();

		// Restore the last cell change in the list of moves
		cellChange.restore();

		mMoves.remove(undoPosition);

		mGridStatistics.increaseCounter(StatisticsCounterType.ACTION_UNDO_MOVE);

		// Set the cell to which the cell change applies as selected cell.
		setSelectedCell(affectedCell);

		if (userValueBeforeUndo != affectedCell.getUserValue()) {
			// Each cell in the same column or row as the restored cell, has to
			// be checked for duplicate values.
			CellSelectorInRowOrColumn cellSelectorInRowOrColumn = mObjectsCreator
					.createCellSelectorInRowOrColumn(mCells,
							affectedCell.getRow(), affectedCell.getColumn());
			List<Cell> cellsInSameRowOrColumn = cellSelectorInRowOrColumn
					.find();
			if (cellsInSameRowOrColumn != null) {
				for (Cell cellInSameRowOrColumn : cellsInSameRowOrColumn) {
					cellInSameRowOrColumn
							.markDuplicateValuesInSameRowAndColumn();
				}
			}

			// Check the cage math
			Cage cage = affectedCell.getCage();
			if (cage != null) {
				cage.checkUserMath();
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
		Cage oldSelectedCage = mSelectedCell.getCage();

		// Deselect the cell itself
		mSelectedCell.deselect();
		mSelectedCell = null;

		// Update borders of cage which was selected before.
		oldSelectedCage.invalidateBordersOfAllCells();
	}

	/**
	 * Selects the given cell.
	 * 
	 * @param cell
	 *            The cell to be selected. If null, then the current selected
	 *            cell will be unselected.
	 * @return The selected cell.
	 */
	public Cell setSelectedCell(Cell cell) {
		if (cell == null) {
			deselectSelectedCell();
			return null;
		}

		Cage oldSelectedCage = null;
		if (mSelectedCell != null) {
			oldSelectedCage = mSelectedCell.getCage();
			mSelectedCell.deselect();
		}
		// Determine new cage
		Cage newSelectedCage = cell.getCage();

		// Select the new cell
		cell.select();
		mSelectedCell = cell;

		// Set borders if another cage is selected.
		if (!newSelectedCage.equals(oldSelectedCage)) {
			if (oldSelectedCage != null) {
				oldSelectedCage.invalidateBordersOfAllCells();
			}
			newSelectedCage.invalidateBordersOfAllCells();
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
	public final Cell setSelectedCell(int[] coordinates) {
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
		if (mSelectedCell == null) {
			return;
		}

		int rowSelectedCell = mSelectedCell.getRow();
		int columnSelectedCell = mSelectedCell.getColumn();
		int valueSelectedCell = mSelectedCell.getUserValue();
		for (Cell cell : mCells) {
			if ((cell.getRow() == rowSelectedCell || cell.getColumn() == columnSelectedCell)
					&& cell.hasPossible(valueSelectedCell)) {
				originalCellChange.addRelatedMove(new CellChange(cell));
				cell.removePossible(valueSelectedCell);
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

	public Cage getCage(int cageId) {
		return mCages.get(cageId);
	}

	public abstract class OnSolvedListener {
		public abstract void puzzleSolved();
	}

	public int getGridSize() {
		return mGridSize;
	}

	public Cell getSelectedCell() {
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
		GridSaver gridSaver = mObjectsCreator.createGridSaver();
		boolean isSaved = gridSaver.save(this);
		if (isSaved) {
			mRowId = gridSaver.getRowId();
			mSolvingAttemptId = gridSaver.getSolvingAttemptId();
			mGridStatistics = gridSaver.getGridStatistics();
			mDateUpdated = gridSaver.getDateUpdated();
		}
		return isSaved;
	}

	/**
	 * Upgrade this grid (solving attempt and statistics) to the current app
	 * version.
	 * 
	 * @return True in case everything has been saved. False otherwise.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean saveOnAppUpgrade() {
		GridSaver gridSaver = mObjectsCreator.createGridSaver();
		boolean isSaved = gridSaver.saveOnAppUpgrade(this);
		if (isSaved) {
			mRowId = gridSaver.getRowId();
			mSolvingAttemptId = gridSaver.getSolvingAttemptId();
			mGridStatistics = gridSaver.getGridStatistics();
			mDateUpdated = gridSaver.getDateUpdated();
		}

		return isSaved;
	}

	/**
	 * Checks if the grid does not contain any user value. It is not relevant
	 * whether the grid does contain maybe values.
	 * 
	 * @return True in case no user values have been filled in. False in case at
	 *         least one user value is filled in.
	 */
	public boolean containsNoUserValues() {
		for (Cell cell : mCells) {
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
		for (Cell cell : mCells) {
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
		List<Cell> cells = new ArrayList<Cell>();
		for (Cell cell : mCells) {
			CellBuilder cellBuilder = mObjectsCreator.createCellBuilder();
			Cell newCell = cellBuilder
					.setGridSize(mGridSize)
					.setId(cell.getCellId())
					.setCorrectValue(cell.getCorrectValue())
					.setCageId(cell.getCageId())
					.build();
			cells.add(newCell);
		}

		// Copy all cages without the play history
		List<Cage> cages = new ArrayList<Cage>();
		for (Cage cage : mCages) {
			CageBuilder cageBuilder = mObjectsCreator.createCageBuilder();
			Cage newCage = cageBuilder
					.setId(cage.getId())
					.setResult(cage.getResult())
					.setCageOperator(cage.getOperator())
					.setHideOperator(mGridGeneratingParameters.mHideOperators)
					.setCells(cage.getCells())
					.build();
			cages.add(newCage);
		}

		// Create a new grid
		return mObjectsCreator
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
		for (Cell cell : mCells) {
			if (cell.isUserValueSet() && !cell.hasInvalidUserValueHighlight()
					&& cell.getUserValue() != cell.getCorrectValue()) {
				cell.setInvalidHighlight();
				countNewInvalids++;
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
		return mGridStatistics != null
				&& (mGridStatistics.getReplayCount() > 0);

	}

	/**
	 * Set borders of all cages having incorrect maths.
	 */
	private void checkUserMathForAllCages() {
		if (mCages != null) {
			for (Cage cage : mCages) {
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
		Cell selectedCell = getSelectedCell();
		if (selectedCell == null) {
			return false;
		}

		// Save current state of selected cell
		CellChange cellChange = mObjectsCreator.createCellChange(selectedCell);

		// Reveal the user value
		selectedCell.setRevealed();
		selectedCell.setUserValue(selectedCell.getCorrectValue());

		if (Preferences.getInstance().isPuzzleSettingClearMaybesEnabled()) {
			// Update possible values for other cells in this row and
			// column.
			clearRedundantPossiblesInSameRowOrColumn(cellChange);
		}

		// Store the cell change (including related cell changed for redundant
		// value which have been cleared.
		addMove(cellChange);

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
		Cage selectedCage = getSelectedCage();
		if (selectedCage == null) {
			return false;
		}
		if (!selectedCage.isOperatorHidden()) {
			// Operator is already visible.
			return false;
		}

		selectedCage.revealOperator();
		setCageTextToUpperLeftCell(selectedCage);

		mGridStatistics
				.increaseCounter(StatisticsCounterType.ACTION_REVEAL_OPERATOR);

		return true;
	}

	public List<CellChange> getCellChanges() {
		// Copy the entire ArrayList to a new instance. The ObjectsCreator
		// should not be used.
		return mMoves == null ? null : new ArrayList(mMoves);
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
	public List<Integer> getUserValuesForCells(int[] cells) {
		List<Integer> userValues = new ArrayList<Integer>();
		if (cells == null) {
			return userValues;
		}

		for (int cell : cells) {
			if (cell >= 0 && cell < mCells.size()) {
				Cell cellCopy = mCells.get(cell);
				if (cellCopy != null && cellCopy.isUserValueSet()) {
					userValues.add(cellCopy.getUserValue());
				}
			}
		}

		return userValues;
	}

	public void invalidateBordersOfAllCells() {
		for (Cell cell : mCells) {
			cell.invalidateBorders();
		}
	}

	public List<Cell> getCells(int[] cells) {
		List<Cell> listOfCells = new ArrayList<Cell>();
		if (cells == null) {
			return listOfCells;
		}

		for (int cell : cells) {
			if (cell >= 0 && cell < mCells.size()) {
				Cell cellCopy = mCells.get(cell);
				if (cellCopy != null) {
					listOfCells.add(cellCopy);
				}
			}
		}

		return listOfCells;
	}

	private final void setCageTextToUpperLeftCellAllCages() {
		for (Cage cage : mCages) {
			setCageTextToUpperLeftCell(cage);
		}
	}

	private final void setCageTextToUpperLeftCell(Cage cage) {
		if (cage != null) {
			int idUpperLeftCell = cage.getIdUpperLeftCell();
			Cell cell = getCell(idUpperLeftCell);
			if (cell != null) {
				cell.setCageText(cage.getCageText());
			}
		}
	}

	public Object getLock() {
		return mLock;
	}

	/**
	 * Gets an unmodifiable list of cages.
	 * 
	 * @return An unmodifiable list of cages.
	 */
	public List<Cage> getCages() {
		return Collections.unmodifiableList(mCages);
	}

	/**
	 * Gets an unmodifiable list of cells.
	 * 
	 * @return An unmodifiable list of cells.
	 */
	public List<Cell> getCells() {
		return Collections.unmodifiableList(mCells);
	}

	public String getDefinition() {
		return GridDefinition.getDefinition(this);
	}
}
