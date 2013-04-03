package net.cactii.mathdoku;

import java.util.ArrayList;

import net.cactii.mathdoku.GridGenerating.GridGeneratingParameters;

public class Grid {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.Grid";

	// Identifiers of different versions of grid view information which is
	// stored in a saved game. In the initial versions, view information was
	// stored on several distinct lines. As from now all view variables are
	// collected on single line as well.
	public static final String SAVE_GAME_GRID_VERSION_01 = "VIEW.v1";
	public static final String SAVE_GAME_GRID_VERSION_02 = "VIEW.v2";
	public static final String SAVE_GAME_GRID_VERSION_03 = "VIEW.v3";
	public static final String SAVE_GAME_GRID_VERSION_04 = "VIEW.v4";
	public static final String SAVE_GAME_GRID_VERSION_05 = "VIEW.v5";

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

	// Date of current game (used for saved games) and elapsed time while
	// playing this game
	private long mDateGenerated;
	private long mDateLastSaved;
	private long mElapsedTime;

	// All parameters that influence the game generation and which are needed to
	// regenerate a specific game again.
	private GridGeneratingParameters mGridGeneratingParameters;

	// Keep track of all moves as soon as grid is built or restored.
	public ArrayList<CellChange> moves;

	// Used to avoid redrawing or saving grid during creation of new grid
	public final Object mLock = new Object();

	// Preferences used when drawing the grid
	private boolean mPrefShowDupeDigits;
	private boolean mPrefShowBadCageMaths;
	private boolean mPrefShowMaybesAs3x3Grid;

	// Solved listener
	private OnSolvedListener mSolvedListener;

	// UsagaeLog counters
	private int mUndoLastMoveCount;
	private int mclearRedundantPossiblesInSameRowOrColumnCount;

	public Grid(int gridSize) {
		mGridSize = gridSize;
		mCells = new ArrayList<GridCell>();
		mCages = new ArrayList<GridCage>();
		moves = new ArrayList<CellChange>();
		mUndoLastMoveCount = 0;
		mclearRedundantPossiblesInSameRowOrColumnCount = 0;
		mSolvedListener = null;
		mGridGeneratingParameters = new GridGeneratingParameters();
		setPreferences();
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
		UsageLog.getInstance().logFunction("ContextMenu.ShowSolution");
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
				mUndoLastMoveCount++;
				GridCell cell = moves.get(undoPosition).restore();
				moves.remove(undoPosition);
				setSelectedCell(cell);
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
			mclearRedundantPossiblesInSameRowOrColumnCount++;
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
	 * Create a string representation of the Grid View which can be used to
	 * store a grid view in a saved game.
	 * 
	 * @param keepOriginalDatetimeLastSaved
	 *            True in case the datetime on which the game was last saved may
	 *            not be altered. When not converting an existing gamefile one
	 *            should use false here.
	 * 
	 * @return A string representation of the grid.
	 */
	public String toStorageString(boolean keepOriginalDatetimeLastSaved) {
		// Update datetime last saved if needed
		if (!keepOriginalDatetimeLastSaved) {
			mDateLastSaved = System.currentTimeMillis();
		}

		// Build storage string
		String storageString = SAVE_GAME_GRID_VERSION_05
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mGameSeed
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mGeneratorRevisionNumber
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mDateLastSaved
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mElapsedTime
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mGridSize
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mActive
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mDateGenerated
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mCheated
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mUndoLastMoveCount
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mclearRedundantPossiblesInSameRowOrColumnCount
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mHideOperators
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mMaxCageResult
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mMaxCageSize;
		return storageString;
	}

	/**
	 * Creates a signature of this grid. The signature is unique for the grid regardless of the version of the grid generator used.
	 * 
	 * @return A unique string representation of the grid.
	 */
	public String getSignatureString() {
		StringBuilder signatureString = new StringBuilder();
		// First append all numbers of the entire grid
		for (GridCell cell : mCells) {
			signatureString.append(cell.getCorrectValue());
		}
		signatureString.append(":");
		// Followed by all cage-id per cell
		for (GridCell cell : mCells) {
			signatureString.append(cell.getCageId());
		}
		// Followed by cages
		for (GridCage cage : mCages) {
			signatureString.append(":" + cage.mId + "," + cage.mResult + "," + cage.mAction);
		}
		return signatureString.toString();
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

		int viewInformationVersion = 0;
		if (viewParts[0].equals(SAVE_GAME_GRID_VERSION_01)) {
			viewInformationVersion = 1;
		} else if (viewParts[0].equals(SAVE_GAME_GRID_VERSION_02)) {
			viewInformationVersion = 2;
		} else if (viewParts[0].equals(SAVE_GAME_GRID_VERSION_03)) {
			viewInformationVersion = 3;
		} else if (viewParts[0].equals(SAVE_GAME_GRID_VERSION_04)) {
			viewInformationVersion = 4;
		} else if (viewParts[0].equals(SAVE_GAME_GRID_VERSION_05)) {
			viewInformationVersion = 5;
		} else {
			return false;
		}

		// Process all parts
		int index = 1;
		mGridGeneratingParameters.mGameSeed = Long
				.parseLong(viewParts[index++]);
		if (viewInformationVersion >= 3) {
			mGridGeneratingParameters.mGeneratorRevisionNumber = Integer
					.parseInt(viewParts[index++]);
		} else {
			mGridGeneratingParameters.mGeneratorRevisionNumber = 0;
		}
		mDateLastSaved = Long.parseLong(viewParts[index++]);
		mElapsedTime = Long.parseLong(viewParts[index++]);
		mGridSize = Integer.parseInt(viewParts[index++]);
		mActive = Boolean.parseBoolean(viewParts[index++]);

		if (viewInformationVersion >= 2) {
			mDateGenerated = Long.parseLong(viewParts[index++]);
		} else {
			// Date generated was not saved prior to version 2.
			mDateGenerated = mDateLastSaved - mElapsedTime;
		}
		if (viewInformationVersion >= 4) {
			mCheated = Boolean.parseBoolean(viewParts[index++]);
		} else {
			// Cheated was not saved prior to version 3.
			mCheated = false;
		}
		if (viewInformationVersion >= 5) {
			mUndoLastMoveCount = Integer.parseInt(viewParts[index++]);
			mclearRedundantPossiblesInSameRowOrColumnCount = Integer
					.parseInt(viewParts[index++]);
			mGridGeneratingParameters.mHideOperators = Boolean
					.parseBoolean(viewParts[index++]);
			mGridGeneratingParameters.mMaxCageResult = Integer
					.parseInt(viewParts[index++]);
			mGridGeneratingParameters.mMaxCageSize = Integer
					.parseInt(viewParts[index++]);
		} else {
			// Cheated was not saved prior to version 3.
			mUndoLastMoveCount = 0;
			mclearRedundantPossiblesInSameRowOrColumnCount = 0;
			mGridGeneratingParameters.mHideOperators = false;
			mGridGeneratingParameters.mMaxCageResult = 0;
			mGridGeneratingParameters.mMaxCageSize = 0;
		}

		return true;
	}

	/**
	 * Creates the grid with given information.
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
	public void create(int gridSize, ArrayList<GridCell> cells,
			ArrayList<GridCage> cages, boolean active,
			GridGeneratingParameters gridGeneratingParameters) {
		clear();
		this.mGridGeneratingParameters = gridGeneratingParameters;
		this.mDateGenerated = System.currentTimeMillis();
		this.mGridSize = gridSize;
		this.mCells = cells;
		this.mCages = cages;
		this.mActive = active;

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
		return mDateGenerated;
	}

	public void setDateCreated(long dateCreated) {
		mDateGenerated = dateCreated;
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

	public int getUndoCount() {
		return mUndoLastMoveCount;
	}

	public int getClearRedundantPossiblesInSameRowOrColumnCount() {
		return mclearRedundantPossiblesInSameRowOrColumnCount;
	}

	public GridGeneratingParameters getGridGeneratingParameters() {
		return mGridGeneratingParameters;
	}

	public boolean getCheated() {
		return mCheated;
	}
}