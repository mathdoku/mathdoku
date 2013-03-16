package net.cactii.mathdoku;

import java.util.ArrayList;

public class Grid {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.Grid";

	// Identifiers of different versions of grid view information which is
	// stored in a saved game. In the initial versions, view information was
	// stored on several distinct lines. As from now all view variables are
	// collected on single line as well.
	public static final String SAVE_GAME_GRID_VERSION_01 = "VIEW.v1";

	// Size of the grid
	private int mGridSize;

	// Cages
	public ArrayList<GridCage> mCages;

	// Cell and solution
	public ArrayList<GridCell> mCells;

	private boolean mCheated;

	// Puzzle is active as long as it has not been solved.
	private boolean mActive;

	private GridCell mSelectedCell;

	// Date of current game (used for saved games) and elapsed time while playing this game
	private long mDateCreated;
	private long mElapsedTime;

	// The seed used to create the game. In case the seed equals 0 it can not be
	// trusted as a valid seed in case the grid was restored from a game file
	// prior to version 1 as the game seed was not stored until then.
	private long mGameSeed;

	// Keep track of all moves as soon as grid is built or restored.
	public ArrayList<CellChange> moves;

	// Used to avoid redrawing or saving grid during creation of new grid
	public final Object mLock = new Object();

	// Solved listener
	private OnSolvedListener mSolvedListener;

	public Grid(int gridSize) {
		mGridSize = gridSize;
		mCells = new ArrayList<GridCell>();
		mCages = new ArrayList<GridCage>();
		moves = new ArrayList<CellChange>();
		this.mSolvedListener = null;
	}

	// Returns cage id of cell at row, column
	// Returns -1 if not a valid cell or cage
	public int CageIdAt(int row, int column) {
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

	public void clearUserValues() {
		if (this.moves != null) {
			this.moves.clear();
		}
		if (mCells != null) {
			for (GridCell cell : this.mCells) {
				cell.clearUserValue();
			}
		}
	}

	/**
	 * Clear this view so a new game can be restored.
	 */
	public void clear() {
		if (this.moves != null) {
			this.moves.clear();
		}
		if (this.mCells != null) {
			this.mCells.clear();
		}
		if (this.mCages != null) {
			this.mCages.clear();
		}
		mSelectedCell = null;
		mActive = false;
		mCheated = false;
		mElapsedTime = 0;
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
	public void Solve() {
		this.mCheated = true;
		if (this.moves != null) {
			this.moves.clear();
		}
		for (GridCell cell : this.mCells) {
			if (!cell.isUserValueCorrect()) {
				cell.setCheated();
			}
			cell.setUserValue(cell.getCorrectValue());
		}
	}

	// Returns whether the puzzle is solved.
	public void checkIfSolved() {
		// Check if alls cell contain correct value.
		for (GridCell cell : this.mCells) {
			if (!cell.isUserValueCorrect()) {
				return;
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
			if (cell.getInvalidHighlight())
				invalids.add(cell);

		return invalids;
	}

	public void AddMove(CellChange move) {
		if (moves == null) {
			moves = new ArrayList<CellChange>();
		}

		boolean identicalToLastMove = false;
		int indexLastMove = moves.size() - 1;
		if (indexLastMove >= 0) {
			CellChange lastMove = moves.get(indexLastMove);
			identicalToLastMove = lastMove.equals(move);
		}
		if (!identicalToLastMove) {
			moves.add(move);
		}
	}

	/**
	 * Get the number of moves made by the user.
	 * 
	 * @return The number of moves made by the user.
	 */
	public int countMoves() {
		return (moves == null ? 0 : moves.size());
	}

	public boolean UndoLastMove() {
		if (moves != null) {
			int undoPosition = moves.size() - 1;

			if (undoPosition >= 0) {
				GridCell cell = moves.get(undoPosition).restore();
				moves.remove(undoPosition);
				setSelectedCell(cell);
				return true;
			}
		}
		return false;
	}

	public void setSelectedCell(GridCell cell) {
		// Unselect current cage
		GridCage selectedCage = getCageForSelectedCell();
		if (selectedCage != null) {
			selectedCage.mSelected = false;
		}

		// Unselect current cell
		if (mSelectedCell != null) {
			mSelectedCell.mSelected = false;
		}

		// Select new cell
		mSelectedCell = cell;
		mSelectedCell.mSelected = true;

		// Select new cage
		getCageForSelectedCell().mSelected = true;
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
		int rowSelectedCell = this.mSelectedCell.getRow();
		int columnSelectedCell = this.mSelectedCell.getColumn();
		int valueSelectedCell = this.mSelectedCell.getUserValue();
		if (this.mSelectedCell != null) {
			for (GridCell cell : this.mCells) {
				if (cell.getRow() == rowSelectedCell
						|| cell.getColumn() == columnSelectedCell) {
					if (cell.hasPossible(valueSelectedCell)) {
						cell.saveUndoInformation(originalCellChange);
						cell.togglePossible(valueSelectedCell);
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
		return this.mGameSeed;
	}

	/**
	 * Check is user has cheated with solving this puzzle by requesting the
	 * solution.
	 * 
	 * @return True in case the user has solved the puzzle by requesting the
	 *         solution. False otherwise.
	 */
	public boolean isSolvedByCheating() {
		// TODO: store mCheated in game file so for a restored game this
		// variable is restored as well
		return this.mCheated;
	}

	/**
	 * Create a string representation of the Grid View which can be used to
	 * store a grid view in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString() {
		String storageString = SAVE_GAME_GRID_VERSION_01
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mGameSeed
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mDateCreated
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mElapsedTime
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mGridSize
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mActive;
		return storageString;
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
	public boolean fromStorageString(String line) {
		String[] viewParts = line.split(GameFile.FIELD_DELIMITER_LEVEL1);

		@SuppressWarnings("unused")
		int viewInformationVersion = 0;
		if (viewParts[0].equals(SAVE_GAME_GRID_VERSION_01)) {
			viewInformationVersion = 1;
		} else {
			return false;
		}

		// Process all parts
		int index = 1;
		mGameSeed = Long.parseLong(viewParts[index++]);
		mDateCreated = Long.parseLong(viewParts[index++]);
		mElapsedTime = Long.parseLong(viewParts[index++]);
		mGridSize = Integer.parseInt(viewParts[index++]);
		mActive = Boolean.parseBoolean(viewParts[index++]);

		return true;
	}

	/**
	 * Creates the grid with given information.
	 * 
	 * @param mGameSeed
	 *            : The game seed used to generate this grid.
	 * @param mGridSize
	 *            The size of grid.
	 * @param mCells
	 *            The list of cell used in the grid.
	 * @param mCages
	 *            The list of cages used in the grid.
	 * @param mActive
	 *            The status of the grid.
	 */
	public void create(long mGameSeed, int mGridSize,
			ArrayList<GridCell> mCells, ArrayList<GridCage> mCages,
			boolean mActive) {
		clear();
		this.mGameSeed = mGameSeed;
		this.mDateCreated = System.currentTimeMillis();
		this.mGridSize = mGridSize;
		this.mCells = mCells;
		this.mCages = mCages;
		this.mActive = mActive;

		// Cages keep a reference to the grid view to which they belong.
		for (GridCage cage : mCages) {
			cage.setGridReference(this);
		}

		// Cells keep a reference to the grid view to which they belong.
		for (GridCell cell : mCells) {
			cell.setGridReference(this);
		}
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
		return mElapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		mElapsedTime = elapsedTime;
	}

	public long getDateCreated() {
		return mDateCreated;
	}

	public void setDateCreated(long dateCreated) {
		mDateCreated = dateCreated;
	}
}