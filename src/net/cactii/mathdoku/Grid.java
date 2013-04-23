package net.cactii.mathdoku;

import java.util.ArrayList;

import net.cactii.mathdoku.GridGenerating.GridGeneratingParameters;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.storage.GameFile;
import net.cactii.mathdoku.storage.GameFile.GameFileType;
import net.cactii.mathdoku.storage.GridFile;
import net.cactii.mathdoku.storage.PreviewImage;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.GridRow;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;

public class Grid {
	@SuppressWarnings("unused")
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

	// The file from which the current solving attempt is loaded. Null in case
	// the solving attempt was not yet saved before.
	private GridFile mGridFile;

	public Grid(int gridSize) {
		initialize();
		mGridSize = gridSize;
	}

	/**
	 * Creates a new instance of a {@link Grid} by loading it from the specified
	 * file.
	 * 
	 * @param filename
	 *            The filename (without path) from which the grid has to be
	 *            loaded.
	 */
	public Grid(String filename) {
		initialize();

		mGridFile = new GridFile(filename);
		if (!mGridFile.loadIntoGrid(this)) {
			// Error loading. Initialize again to clean up.
			initialize();
		}
	}

	/**
	 * Initializes a new grid object.
	 */
	private void initialize() {
		mCells = new ArrayList<GridCell>();
		mCages = new ArrayList<GridCage>();
		mMoves = new ArrayList<CellChange>();
		mClearRedundantPossiblesInSameRowOrColumnCount = 0;
		mSolvedListener = null;
		mGridFile = null;
		mGridGeneratingParameters = new GridGeneratingParameters();
		mGridStatistics = new GridStatistics();
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
		String storageString = SAVE_GAME_GRID_LINE
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mGameSeed
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mGeneratorRevisionNumber
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mDateLastSaved
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mGridSize
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mActive
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mDateCreated
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mCheated
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mClearRedundantPossiblesInSameRowOrColumnCount
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mHideOperators
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mMaxCageResult
				+ GameFile.FIELD_DELIMITER_LEVEL1
				+ mGridGeneratingParameters.mMaxCageSize;
		return storageString;
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
		String[] viewParts = line.split(GameFile.FIELD_DELIMITER_LEVEL1);

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
		mDateLastSaved = Long.parseLong(viewParts[index++]);
		if (revisionNumber <= 5) {
			// Field elapsedTime has been removed in version 6 and above.
			mGridStatistics.mElapsedTime = Long.parseLong(viewParts[index++]);
		}
		mGridSize = Integer.parseInt(viewParts[index++]);
		mActive = Boolean.parseBoolean(viewParts[index++]);

		if (revisionNumber >= 2) {
			mDateCreated = Long.parseLong(viewParts[index++]);
		} else {
			// Date generated was not saved prior to version 2.
			mDateCreated = mDateLastSaved - mGridStatistics.mElapsedTime;
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

		// In case an existing grid object is reused, we have to clean up old
		// data
		if (this.mMoves != null) {
			this.mMoves.clear();
		}
		mSelectedCell = null;
		mCheated = false;
		mGridFile = null;

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

		insertInDatabase();
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
		boolean insertedInDatase = false;

		DatabaseHelper.beginTransaction();

		// First insert the grid object.
		GridDatabaseAdapter gridDatabaseAdapter = new GridDatabaseAdapter();
		mRowId = gridDatabaseAdapter.insert(this);
		if (mRowId >= 0) {

			// Insert statistics
			StatisticsDatabaseAdapter statisticsDatabaseAdapter = new StatisticsDatabaseAdapter();
			mGridStatistics = statisticsDatabaseAdapter.insert(this);
			if (mGridStatistics != null) {
				insertedInDatase = DatabaseHelper.setTransactionSuccessful();
			}
		}

		// CLose transaction
		DatabaseHelper.endTransaction();

		return insertedInDatase;
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
	 * Save this grid (game file, statistics and preview). The preview image of
	 * the grid is based upon the given grid view.
	 * 
	 * @param gridView
	 *            The gridView in which the grid is currently displayed.
	 * 
	 * @return True in case everything has been saved. False otherwise.
	 */
	public boolean save(GridView gridView) {
		GameFile gameFile = new GameFile(GameFileType.LAST_GAME);

		// Save to game file first
		GridFile gridFile = new GridFile(gameFile);
		if (!gridFile.save(this, false)) {
			return false;
		}

		// Update statistics
		boolean savedStatistics = (mGridStatistics == null ? false
				: mGridStatistics.save());

		// Save the preview image.
		PreviewImage previewImage = new PreviewImage(gameFile);
		boolean savedPreview = (gridView == null ? false : previewImage
				.save(gridView));

		return (savedStatistics && savedPreview);
	}

	public boolean isLoadedFromFile() {
		return (mGridFile != null);
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
	 * Gets the file name in which the current solving attempt is stored.
	 * 
	 * @return The file name in which the current solving attempt is stored.
	 */
	public String getSolvingAttemptFilename() {
		return (mGridFile == null ? "" : mGridFile.getFilename());
	}
}