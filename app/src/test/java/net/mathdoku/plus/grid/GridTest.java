package net.mathdoku.plus.grid;

import android.app.Activity;
import android.content.Context;

import com.srlee.DLX.MathDokuDLX;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridGenerating.GridGenerator;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridTest {
	private Activity mActivity;
	private Preferences preferences;
	private Grid mGrid;

	private GridObjectsCreator mGridObjectsCreator;

	private class GridObjectsCreator extends Grid.ObjectsCreator {
		// Init mockable objects
		public GridStatistics mGridStatisticsMock = mock(GridStatistics.class);
		public GridDatabaseAdapter mGridDatabaseAdapterMock = mock(GridDatabaseAdapter.class);
		public DatabaseHelper mDatabaseHelperMock = mock(DatabaseHelper.class);
		public SolvingAttemptDatabaseAdapter mSolvingAttemptDatabaseAdapterMock = mock(SolvingAttemptDatabaseAdapter.class);
		public StatisticsDatabaseAdapter mStatisticsDatabaseAdapterMock = mock(StatisticsDatabaseAdapter.class);
		public GridCellSelectorInRowOrColumn mGridCellSelectorInRowOrColumn  = mock(GridCellSelectorInRowOrColumn.class);

		// Array lists will not be initialized until the grid is actually created with this GridObjectsCreator.
		private ArrayList<GridCell> mArrayListOfGridCells = null;
		public ArrayList<CellChange> mArrayListOfCellChanges = null;

		@Override
		public GridCell createGridCell(Grid grid, int cell) {
			return mock(GridCell.class);
		}

		@Override
		public CellChange createCellChange() {
			return mock(CellChange.class);
		}

		@Override
		public GridCage createGridCage(Grid grid) {
			return mock(GridCage.class);
		}

		@Override
		public GridStatistics createGridStatistics() {
			return mGridStatisticsMock;
		}

		@Override
		public GridGeneratingParameters createGridGeneratingParameters() {
			return mock(GridGeneratingParameters.class);
		}

		@Override
		public MathDokuDLX createMathDokuDLX(int gridSize,
				ArrayList<GridCage> cages) {
			return mock(MathDokuDLX.class);
		}

		/**
		 * Initializes the ArrayList of Grid Cells with an ArrayList filled with
		 * given (mocked) GridCells.
		 */
		public GridObjectsCreator initializeArrayListOfGridCells(GridCell... gridCell) {
			if (mArrayListOfGridCells != null) {
				throw new RuntimeException(
						"Cannot initialize Array List of Grid Cells after Grid has been initialized.");
			}
			mArrayListOfGridCells = createArrayListOfGridCellsWithGridCells(gridCell);

			return this;
		}

		@Override
		public ArrayList<GridCell> createArrayListOfGridCells() {
			if (mArrayListOfGridCells == null) {
				mArrayListOfGridCells = super.createArrayListOfGridCells();
			}
			return mArrayListOfGridCells;
		}

		@Override
		public ArrayList<GridCage> createArrayListOfGridCages() {
			return super.createArrayListOfGridCages();
		}

		public GridObjectsCreator replaceArrayListOfCellChangesWithMock() {
			if (mArrayListOfCellChanges != null) {
				throw new RuntimeException(
						"Cannot replace Array List of Cell Changes with Mock after Grid has been initialized.");
			}
			mArrayListOfCellChanges = mock(ArrayList.class);

			return this;
		}

		@Override
		public ArrayList<CellChange> createArrayListOfCellChanges() {
			if (mArrayListOfCellChanges == null) {
				mArrayListOfCellChanges = super.createArrayListOfCellChanges();
			}
			return mArrayListOfCellChanges;
		}

		@Override
		public GridCellSelectorInRowOrColumn createGridCellSelectorInRowOrColumn(
				ArrayList<GridCell> cells, int row, int column) {
			return mGridCellSelectorInRowOrColumn;
		}

		@Override
		public DatabaseHelper createDatabaseHelper() {
			return mDatabaseHelperMock;
		}

		@Override
		public GridDatabaseAdapter createGridDatabaseAdapter() {
			return mGridDatabaseAdapterMock;
		}

		@Override
		public SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
			return mSolvingAttemptDatabaseAdapterMock;
		}

		@Override
		public StatisticsDatabaseAdapter createStatisticsDatabaseAdapter() {
			return mStatisticsDatabaseAdapterMock;
		}
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		mActivity = new Activity();

		Preferences.ObjectsCreator preferencesObjectsCreator = new Preferences.ObjectsCreator() {
			@Override
			public Preferences createPreferencesSingletonInstance(
					Context context) {
				return mock(Preferences.class);
			}
		};

		// Create the Preference Instance with the Singleton Creator which uses
		// a mocked Preferences object
		preferences = Preferences.getInstance(mActivity,
				preferencesObjectsCreator);

		mGridObjectsCreator = new GridObjectsCreator();
		mGrid = new Grid(mGridObjectsCreator);
	}

	@Test(expected = RuntimeException.class)
	public void SetGridSize_ChangeGridSize_RuntimeExceptionIsThrown()
			throws Exception {
		// Init the grid
		mGrid.setGridSize(5);

		// Following will throw an exception as the grid size can not be changed
		// after being set
		mGrid.setGridSize(6);
	}

	@Test
	public void setPreferences_CreateGrid_PreferencesAreRetrieved()
			throws Exception {
		// Set preferences
		mGrid.setPreferences();

		// Check that preferences are retrieved when setting the preferences.
		// Note: method setPreference is also called when creating the grid. So
		// each method is invoked more than once.
		verify(preferences, atLeastOnce()).isDuplicateDigitHighlightVisible();
		verify(preferences, atLeastOnce()).isMaybesDisplayedInGrid();
		verify(preferences, atLeastOnce()).isBadCageMathHighlightVisible();
	}

	@Test
	public void setPreferences_CreateGridOfSize4_CellBordersAreSet()
			throws Exception {
		// Create a GridCell mock
		GridCell gridCellMock = mock(GridCell.class);
		doNothing().when(gridCellMock).setBorders();

		// Add the GridCell mock for each cell in the grid
		int gridSize = 4;
		mGrid.mCells.add(gridCellMock);
		mGrid.mCells.add(gridCellMock);
		mGrid.mCells.add(gridCellMock);
		mGrid.mCells.add(gridCellMock);

		// Set preferences
		mGrid.setPreferences();

		// Check that preferences are set when Grid is created.
		verify(gridCellMock, times(4)).setBorders();
	}

	@Test
	public void getSelectedCage_SelectedCellIsNull_NullCage() {
		// Get the actual result
		GridCage expectedGridCage = null;
		GridCage resultGridCage = mGrid.getSelectedCage();
		assertEquals("Selected cage", expectedGridCage, resultGridCage);

	}

	@Test
	public void getSelectedCage_SelectedCellIsNotNull_CageSelected() {
		// Create stubs for grid cages.
		GridCage gridCageStub1 = mock(GridCage.class);
		mGrid.mCages.add(gridCageStub1);

		// Stub the cage for the cell which is selected
		GridCage gridCageStub2 = mock(GridCage.class);
		GridCell gridCellStub2 = mock(GridCell.class);
		when(gridCellStub2.getCage()).thenReturn(gridCageStub2);
		mGrid.mCages.add(gridCageStub2);
		mGrid.setSelectedCell(gridCellStub2);

		GridCage gridCageStub3 = mock(GridCage.class);
		mGrid.mCages.add(gridCageStub3);

		// Get the actual result
		GridCage expectedGridCage = gridCageStub2;
		GridCage resultGridCage = mGrid.getSelectedCage();
		assertEquals("Selected cage", expectedGridCage, resultGridCage);
	}

	@Test
	public void clearCells_GridWithMultipleMovesCleared_AllMovesCleared()
			throws Exception {
		// Create stubs for each cell change and add those stubs to the list of
		// cells. Note: the same cell change cannot be added more than once as
		// method Grid.addMove only will add a move when it is not identical to
		// the last move which was added.
		CellChange cellChangeStub1 = mock(CellChange.class);
		mGrid.addMove(cellChangeStub1);

		CellChange cellChangeStub2 = mock(CellChange.class);
		mGrid.addMove(cellChangeStub2);

		CellChange cellChangeStub3 = mock(CellChange.class);
		mGrid.addMove(cellChangeStub3);

		CellChange cellChangeStub4 = mock(CellChange.class);
		mGrid.addMove(cellChangeStub4);

		// Verify the number of cell changes added
		int expectedNumberOfCellChangesBeforeClear = 4;
		int resultNumberOfCellChangesBeforeClear = mGrid.countMoves();
		assertEquals("Number of moves for grid before clear",
				expectedNumberOfCellChangesBeforeClear,
				resultNumberOfCellChangesBeforeClear);

		// Clear the cells. Value of variable replace is not relevant for this
		// unit test.
		boolean replay = false;
		mGrid.clearCells(replay);
		int expectedNumberOfCellChangesAfterClear = 0;
		int resultNumberOfCellChangesAfterClear = mGrid.countMoves();
		assertEquals("Number of moves for grid after clear",
				expectedNumberOfCellChangesAfterClear,
				resultNumberOfCellChangesAfterClear);
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_AllCellsCleared()
			throws Exception {
		// Create stub for empty grid cell
		GridCell emptyGridCellMock = mock(GridCell.class);

		// Add this stub multiple times to the list of cells
		mGrid.mCells.add(emptyGridCellMock);
		mGrid.mCells.add(emptyGridCellMock);
		mGrid.mCells.add(emptyGridCellMock);
		mGrid.mCells.add(emptyGridCellMock);

		// Verify the number of cell changes added
		int expectedNumberOfCells = 4;
		int resultNumberOfCells = mGrid.mCells.size();
		assertEquals("Number of moves for grid before clear",
				expectedNumberOfCells, resultNumberOfCells);

		// Clear the cells. Value of variable replace is not relevant for this
		// unit test.
		boolean replay = false;
		mGrid.clearCells(replay);

		// Note: currently a cell is always cleared even in case it does not
		// contain a user values nor any maybe values.
		verify(emptyGridCellMock, times(expectedNumberOfCells)).clear();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_FlagsOfAllCellsCleared()
			throws Exception {
		// Create stub for empty grid cell
		GridCell emptyGridCellStub = mock(GridCell.class);

		// Add this stub multiple times to the list of cells
		mGrid.mCells.add(emptyGridCellStub);
		mGrid.mCells.add(emptyGridCellStub);
		mGrid.mCells.add(emptyGridCellStub);
		mGrid.mCells.add(emptyGridCellStub);

		// Verify the number of cell changes added
		int expectedNumberOfCells = 4;
		int resultNumberOfCells = mGrid.mCells.size();
		assertEquals("Number of moves for grid before clear",
				expectedNumberOfCells, resultNumberOfCells);

		// Clear the cells.
		boolean replay = true;
		mGrid.clearCells(replay);

		verify(emptyGridCellStub, times(expectedNumberOfCells)).clearAllFlags();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_GridStatisticsUpdated()
			throws Exception {
		// Create stub for empty grid cell.
		GridCell emptyGridCellStub = mock(GridCell.class);
		when(emptyGridCellStub.getUserValue()).thenReturn(0, 1, 2, 0);

		// Add this stub multiple times to the list of cells
		mGrid.mCells.add(emptyGridCellStub);
		mGrid.mCells.add(emptyGridCellStub);
		mGrid.mCells.add(emptyGridCellStub);
		mGrid.mCells.add(emptyGridCellStub);

		// Clear the cells.
		boolean replay = true;
		mGrid.clearCells(replay);

		// Clearing a second time won't change the statistics as no cells are
		// left to be cleared
		when(emptyGridCellStub.getUserValue()).thenReturn(0, 0, 0, 0);
		mGrid.clearCells(replay);

		verify(mGridObjectsCreator.mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_CLEAR_GRID);
	}

	private GridCell getCellAt(int gridSize, int row, int col) {
		mGrid.setGridSize(gridSize);

		return mGrid.getCellAt(row, col);
	}

	@Test
	public void getCellAt_NegativeRowNumber_Null() throws Exception {
		int gridSize = 4;
		int row = -1;
		int col = 1;

		GridCell expectedGridCell = getCellAt(gridSize, row, col);
		assertNull(expectedGridCell);
	}

	@Test
	public void getCellAt_NegativeColNumber_Null() throws Exception {
		int gridSize = 4;
		int row = 1;
		int col = -1;

		GridCell expectedGridCell = getCellAt(gridSize, row, col);
		assertNull(expectedGridCell);
	}

	@Test
	public void getCellAt_RowNumberGreaterOrEqualsToGridSize_Null()
			throws Exception {
		int gridSize = 4;
		int row = gridSize;
		int col = 1;

		GridCell expectedGridCell = getCellAt(gridSize, row, col);
		assertNull(expectedGridCell);
	}

	@Test
	public void getCellAt_ColNumberGreaterOrEqualsToGridSize_Null()
			throws Exception {
		int gridSize = 4;
		int row = 1;
		int col = gridSize;

		GridCell expectedGridCell = getCellAt(gridSize, row, col);
		assertNull(expectedGridCell);
	}

	@Test
	public void getCellAt_ValidRowAndColNumber_NotNull() throws Exception {
		int gridSize = 2;
		mGrid.setGridSize(gridSize);

		// Add cells to the grid.
		GridCell gridCellMock1 = mock(GridCell.class);
		mGrid.mCells.add(gridCellMock1);

		GridCell gridCellMock2 = mock(GridCell.class);
		mGrid.mCells.add(gridCellMock2);

		GridCell gridCellMock3 = mock(GridCell.class);
		mGrid.mCells.add(gridCellMock3);

		GridCell gridCellMock4 = mock(GridCell.class);
		mGrid.mCells.add(gridCellMock4);

		int expectedNumberOfCells = gridSize * gridSize;
		int resultNumberOfCells = mGrid.mCells.size();
		assertEquals("Number of cells in grid", expectedNumberOfCells,
				resultNumberOfCells);

		// Get cell at row 2 and col 2
		int rowIndex = 2 - 1;
		int colIndex = 2 - 1;
		GridCell expectedGridCell = gridCellMock4;
		GridCell resultGridCell = mGrid.getCellAt(rowIndex, colIndex);
		assertEquals(expectedGridCell, resultGridCell);
	}

	@Test
	public void revealSolution_NullCellList_True() throws Exception {
		// Override the default (empty) array list with null
		mGrid.mCells = null;

		mGrid.revealSolution();

		boolean expectedSolutionRevealed = true;
		boolean resultSolutionRevealed = mGrid.isSolutionRevealed();
		assertEquals("Solution revealed", expectedSolutionRevealed,
				resultSolutionRevealed);
	}

	@Test
	public void revealSolution_EmptyCellList_True() throws Exception {
		mGrid.revealSolution();

		boolean expectedSolutionRevealed = true;
		boolean resultSolutionRevealed = mGrid.isSolutionRevealed();
		assertEquals("Solution revealed", expectedSolutionRevealed,
				resultSolutionRevealed);
	}

	@Test
	public void revealSolution_NonEmptyCellListWith2IncorrectCells_TwoCellsRevealed()
			throws Exception {
		// Add mock cells
		GridCell gridCellMock = mock(GridCell.class);
		when(gridCellMock.isUserValueIncorrect()).thenReturn(false, true,
				false, true, false);
		mGrid.mCells.add(gridCellMock);
		mGrid.mCells.add(gridCellMock);
		mGrid.mCells.add(gridCellMock);
		mGrid.mCells.add(gridCellMock);
		mGrid.mCells.add(gridCellMock);

		mGrid.revealSolution();

		// Check if test isn't flawed with incorrect number of cells in list.
		int expectedNumberOfCellsInList = 5;
		verify(gridCellMock, times(expectedNumberOfCellsInList))
				.isUserValueIncorrect();

		// Ceckh whether the correct number of cells has been revealed.
		int expectedNumberOfCellsRevealed = 2;
		verify(gridCellMock, times(expectedNumberOfCellsRevealed))
				.setRevealed();
		verify(gridCellMock, times(expectedNumberOfCellsRevealed))
				.setUserValue(anyInt());
	}

	@Test
	public void revealSolution_NonEmptyCellList_GridStatisticsUpdated()
			throws Exception {
		mGrid.revealSolution();

		verify(mGridObjectsCreator.mGridStatisticsMock).solutionRevealed();
	}

	@Test
	public void unrevealSolution() throws Exception {
		// Nothing to do here. Solely used for testing purposes
		assertTrue(Config.mAppMode == Config.AppMode.DEVELOPMENT);
	}

	@Test
	public void isSolved_UnSolvedGridIsChecked_False() throws Exception {
		// Add cells to the grid
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.isUserValueIncorrect()).thenReturn(false, false,
				true, false);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);

		boolean expectedIsSolved = false;
		boolean resultedIsSolved = mGrid.isSolved();
		assertEquals("Grid is solved", expectedIsSolved, resultedIsSolved);
	}

	@Test
	public void isSolved_SolvedGridIsChecked_True() throws Exception {
		// Add cells to the grid
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.isUserValueIncorrect()).thenReturn(false, false,
				false, false);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);

		boolean expectedIsSolved = true;
		boolean resultedIsSolved = mGrid.isSolved();
		assertEquals("Grid is solved", expectedIsSolved, resultedIsSolved);
	}

	@Test
	public void setSolved_SolvedGrid_GridStatisticsUpdated() throws Exception {
		mGrid.setSolved();

		verify(mGridObjectsCreator.mGridStatisticsMock).solved();
	}

	@Test
	public void setSolved_SolvedGrid_OnSolvedListenerCalled() throws Exception {
		Grid.OnSolvedListener gridOnSolvedListener = mock(Grid.OnSolvedListener.class);
		mGrid.setSolvedHandler(gridOnSolvedListener);

		// Solve it
		mGrid.setSolved();

		verify(gridOnSolvedListener).puzzleSolved();
	}

	@Test
	public void isSolutionValidSoFar_AllCellsEmpty_True() throws Exception {
		// Add cells to the grid
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.isUserValueSet()).thenReturn(false, false, false,
				false);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);
		mGrid.mCells.add(gridCellStub);

		boolean expectedIsSolutionValidSoFar = true;
		boolean resultedIsSolutionValidSoFar = mGrid.isSolutionValidSoFar();
		assertEquals("Grid is valid so far", expectedIsSolutionValidSoFar,
				resultedIsSolutionValidSoFar);
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsValid_True() throws Exception {
		// Add cells to the grid
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.isUserValueSet()).thenReturn(true);
		when(gridCellStub.isUserValueIncorrect()).thenReturn(false);
		mGrid.mCells.add(gridCellStub);

		boolean expectedIsSolutionValidSoFar = true;
		boolean resultedIsSolutionValidSoFar = mGrid.isSolutionValidSoFar();
		assertEquals("Grid is valid so far", expectedIsSolutionValidSoFar,
				resultedIsSolutionValidSoFar);
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsInvalid_False()
			throws Exception {
		// Add cells to the grid
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.isUserValueSet()).thenReturn(true);
		when(gridCellStub.isUserValueIncorrect()).thenReturn(true);
		mGrid.mCells.add(gridCellStub);

		boolean expectedIsSolutionValidSoFar = false;
		boolean resultedIsSolutionValidSoFar = mGrid.isSolutionValidSoFar();
		assertEquals("Grid is valid so far", expectedIsSolutionValidSoFar,
				resultedIsSolutionValidSoFar);
	}

	@Test
	public void isSolutionValidSoFar_MultipleCellsIncludingAnInvalid_False()
			throws Exception {
		// Add an empty cell
		GridCell gridCellEmptyStub = mock(GridCell.class);
		when(gridCellEmptyStub.isUserValueSet()).thenReturn(false);
		mGrid.mCells.add(gridCellEmptyStub);

		// Add two cells with a correct value to the grid
		GridCell gridCellWithValidUserValueStub = mock(GridCell.class);
		when(gridCellWithValidUserValueStub.isUserValueSet()).thenReturn(true);
		when(gridCellWithValidUserValueStub.isUserValueIncorrect()).thenReturn(
				false);
		mGrid.mCells.add(gridCellWithValidUserValueStub);
		mGrid.mCells.add(gridCellWithValidUserValueStub);

		// Add two cells with an invalid value
		GridCell gridCellWithInvalidUserValueStub = mock(GridCell.class);
		when(gridCellWithInvalidUserValueStub.isUserValueSet())
				.thenReturn(true);
		when(gridCellWithInvalidUserValueStub.isUserValueIncorrect())
				.thenReturn(true);
		mGrid.mCells.add(gridCellWithInvalidUserValueStub);
		mGrid.mCells.add(gridCellWithInvalidUserValueStub);

		boolean expectedIsSolutionValidSoFar = false;
		boolean resultedIsSolutionValidSoFar = mGrid.isSolutionValidSoFar();
		assertEquals("Grid is valid so far", expectedIsSolutionValidSoFar,
				resultedIsSolutionValidSoFar);
	}

	private Grid createGridWithInitialMovesListIsNull() {
		// The default ObjectsCreator always starts with an empty moves list
		// which is not null.
		// The returned grid is created with an initialized which returns a null
		// value for the moves list when it is called for the first time. As the
		// first create call for the moves list is initiated by the
		// Grid-constructor this results in a Grid for which the moves list is
		// null.
		return new Grid(new Grid.ObjectsCreator() {
			boolean mReturnNullOnNextCall = true;

			@Override
			public ArrayList<CellChange> createArrayListOfCellChanges() {
				ArrayList<CellChange> arrayListOfCellChanges = (mReturnNullOnNextCall ? null
						: super.createArrayListOfCellChanges());
				mReturnNullOnNextCall = false;
				return arrayListOfCellChanges;
			}
		});
	}

	@Test
	public void addMove_FirstMoveAddedToNullList_True() throws Exception {
		mGrid = createGridWithInitialMovesListIsNull();

		// Create stub for move to be added.
		CellChange cellChangeStub = mock(CellChange.class);
		mGrid.addMove(cellChangeStub);

		int expectedNumberOfMoves = 1;
		int resultNumberOfMoves = mGrid.countMoves();
		assertEquals("Number of moves", expectedNumberOfMoves,
				resultNumberOfMoves);
	}

	@Test
	public void addMove_FirstMoveAddedToEmptyList_True() throws Exception {
		// Create stub to be added to list of moves
		CellChange cellChangeStub = mock(CellChange.class);

		// Simulate two moves
		mGrid.addMove(cellChangeStub);

		int expectedNumberOfMoves = 1;
		int resultNumberOfMoves = mGrid.countMoves();
		assertEquals("Number of moves", expectedNumberOfMoves,
				resultNumberOfMoves);
	}

	@Test
	public void addMove_AddMultipleDifferentMoves_True() throws Exception {
		// Create stub for first move to be added. Separate stubs are needed per
		// move as a move which is identical to the last move is ignored.
		CellChange cellChangeStub1 = mock(CellChange.class);
		mGrid.addMove(cellChangeStub1);

		// Create stub for first move to be added. Separate stubs are needed per
		// move as a move which is identical to the last move is ignored.
		CellChange cellChangeStub2 = mock(CellChange.class);
		mGrid.addMove(cellChangeStub2);

		int expectedNumberOfMoves = 2;
		int resultNumberOfMoves = mGrid.countMoves();
		assertEquals("Number of moves", expectedNumberOfMoves,
				resultNumberOfMoves);
	}

	@Test
	public void addMove_AddMultipleIdenticalMoves_True() throws Exception {
		// Create stub for moves to be added.
		CellChange cellChangeStub = mock(CellChange.class);

		// Add the same stub multiple times to simulate identical moves
		mGrid.addMove(cellChangeStub);
		mGrid.addMove(cellChangeStub);
		mGrid.addMove(cellChangeStub);

		int expectedNumberOfMoves = 1;
		int resultNumberOfMoves = mGrid.countMoves();
		assertEquals("Number of moves", expectedNumberOfMoves,
				resultNumberOfMoves);
	}

	@Test
	public void countMoves() throws Exception {
		// See tests for addMove
	}

	@Test
	public void undoLastMove_NullMovesList_False() throws Exception {
		mGrid = createGridWithInitialMovesListIsNull();

		boolean resultUndoLastMove = mGrid.undoLastMove();

		boolean expectedUndoLastMove = false;
		assertEquals("Undo last move", expectedUndoLastMove, resultUndoLastMove);
	}

	@Test
	public void undoLastMove_EmptyMovesList_False() throws Exception {
		boolean resultUndoLastMove = mGrid.undoLastMove();

		boolean expectedUndoLastMove = false;
		assertEquals("Undo last move", expectedUndoLastMove, resultUndoLastMove);
	}

	@Test
	public void undoLastMove_MovesListWithOneEntry_MoveIsRestored()
			throws Exception {
		final GridCage gridCageStub = mock(GridCage.class);

		final GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCage()).thenReturn(gridCageStub);

		final CellChange cellChangeMock = mock(CellChange.class);
		when(cellChangeMock.getGridCell()).thenReturn(gridCellStub);

		mGrid.addMove(cellChangeMock);

		mGrid.undoLastMove();

		verify(cellChangeMock).restore();
	}

	@Test
	public void undoLastMove_MovesListWithOneEntry_MoveIsRemoved()
			throws Exception {
		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator);

		final GridCage gridCageStub = mock(GridCage.class);

		final GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCage()).thenReturn(gridCageStub);

		final CellChange cellChangeStub = mock(CellChange.class);
		when(cellChangeStub.getGridCell()).thenReturn(gridCellStub);

		when(mGridObjectsCreator.mArrayListOfCellChanges.size()).thenReturn(1);
		when(mGridObjectsCreator.mArrayListOfCellChanges.get(0)).thenReturn(
				cellChangeStub);

		mGrid.addMove(cellChangeStub);

		mGrid.undoLastMove();

		verify(mGridObjectsCreator.mArrayListOfCellChanges).remove(anyInt());
	}

	@Test
	public void undoLastMove_MovesListWithOneEntry_GridStatisticsUpdated()
			throws Exception {
		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator);

		final GridCage gridCageStub = mock(GridCage.class);

		final GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCage()).thenReturn(gridCageStub);

		final CellChange cellChangeStub = mock(CellChange.class);
		when(cellChangeStub.getGridCell()).thenReturn(gridCellStub);

		when(mGridObjectsCreator.mArrayListOfCellChanges.size()).thenReturn(1);
		when(mGridObjectsCreator.mArrayListOfCellChanges.get(0)).thenReturn(
				cellChangeStub);

		mGrid.addMove(cellChangeStub);

		mGrid.undoLastMove();

		verify(mGridObjectsCreator.mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_UNDO_MOVE);
	}

	@Test
	public void undoLastMove_MovesListWithOneEntry_CellSelected()
			throws Exception {
		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator);

		final GridCage gridCageStub = mock(GridCage.class);

		final GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCage()).thenReturn(gridCageStub);

		final CellChange cellChangeStub = mock(CellChange.class);
		when(cellChangeStub.getGridCell()).thenReturn(gridCellStub);

		when(mGridObjectsCreator.mArrayListOfCellChanges.size()).thenReturn(1);
		when(mGridObjectsCreator.mArrayListOfCellChanges.get(0)).thenReturn(
				cellChangeStub);

		mGrid.addMove(cellChangeStub);

		mGrid.undoLastMove();

		GridCell expectedSelectedCell = gridCellStub;
		GridCell resultSelectedCell = mGrid.getSelectedCell();
		assertEquals("Selected cell", expectedSelectedCell, resultSelectedCell);
	}

	@Test
	public void undoLastMove_MovesListWithOneEntryChangeInPossibleValuesOnly_NoDuplicateValuesInSameRowAndColumnMarked()
			throws Exception {
		final GridCage gridCageStub = mock(GridCage.class);

		int actualUserValueBeforeUndo = 0; // Cell is empty or has maybe values
											// only.
		int expectedUserValueAfterUndo = actualUserValueBeforeUndo;
		final GridCell gridCellMock = mock(GridCell.class);
		when(gridCellMock.getCage()).thenReturn(gridCageStub);
		when(gridCellMock.getUserValue()).thenReturn(actualUserValueBeforeUndo,
				expectedUserValueAfterUndo);

		final CellChange cellChangeStub = mock(CellChange.class);
		when(cellChangeStub.getGridCell()).thenReturn(gridCellMock);

		mGridObjectsCreator = new GridObjectsCreator()
				.initializeArrayListOfGridCells(gridCellMock)
				.replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator);

		when(mGridObjectsCreator.mArrayListOfCellChanges.size()).thenReturn(1);
		when(mGridObjectsCreator.mArrayListOfCellChanges.get(0)).thenReturn(
				cellChangeStub);

		mGrid.addMove(cellChangeStub);

		mGrid.undoLastMove();

		verify(gridCellMock, times(0)).markDuplicateValuesInSameRowAndColumn();
	}

	@Test
	public void undoLastMove_MovesListWithOneEntryChangeInPossibleValuesOnly_NoCageBordersSet()
			throws Exception {
		final GridCage gridCageMock = mock(GridCage.class);

		int actualUserValueBeforeUndo = 0; // Cell is empty or has maybe values
											// only.
		int expectedUserValueAfterUndo = actualUserValueBeforeUndo;
		final GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCage()).thenReturn(gridCageMock);
		when(gridCellStub.getUserValue()).thenReturn(actualUserValueBeforeUndo,
				expectedUserValueAfterUndo);

		final CellChange cellChangeStub = mock(CellChange.class);
		when(cellChangeStub.getGridCell()).thenReturn(gridCellStub);

		mGridObjectsCreator = new GridObjectsCreator()
				.initializeArrayListOfGridCells(gridCellStub);
		mGrid = new Grid(mGridObjectsCreator);

		mGrid.addMove(cellChangeStub);

		mGrid.undoLastMove();

		// Note: the borders are set once as a result of selecting the cell.
		verify(gridCageMock, times(1)).setBorders();
	}

	@Test
	public void undoLastMove_ChangePossibleValueToUserValue_DuplicateValuesInSameRowAndColumnMarked()
			throws Exception {
		final GridCage gridCageStub = mock(GridCage.class);

		int actualUserValueBeforeUndo = 0; // Cell is empty or has maybe values
											// only.
		int expectedUserValueAfterUndo = 1;
		int row = 1;
		int column = 2;
		final GridCell gridCellMock = mock(GridCell.class);
		when(gridCellMock.getCage()).thenReturn(gridCageStub);
		when(gridCellMock.getUserValue()).thenReturn(actualUserValueBeforeUndo,
				expectedUserValueAfterUndo);

		final CellChange cellChangeStub = mock(CellChange.class);
		when(cellChangeStub.getGridCell()).thenReturn(gridCellMock);

		ArrayList<GridCell> mArrayListOfGridCells = new ArrayList
				<GridCell>();
		mArrayListOfGridCells.add(gridCellMock);
		when(mGridObjectsCreator.mGridCellSelectorInRowOrColumn.find()).thenReturn(mArrayListOfGridCells);

		mGrid.addMove(cellChangeStub);

		mGrid.undoLastMove();

		verify(gridCellMock).markDuplicateValuesInSameRowAndColumn();
	}

	@Test
	public void undoLastMove_ChangePossibleValueToUserValue_CheckUserMath()
			throws Exception {
		final GridCage gridCageMock = mock(GridCage.class);

		int actualUserValueBeforeUndo = 0; // Cell is empty or has maybe values
											// only.
		int expectedUserValueAfterUndo = 1;
		final GridCell gridCellMock = mock(GridCell.class);
		when(gridCellMock.getCage()).thenReturn(gridCageMock);
		when(gridCellMock.getUserValue()).thenReturn(actualUserValueBeforeUndo,
				expectedUserValueAfterUndo);

		final CellChange cellChangeStub = mock(CellChange.class);
		when(cellChangeStub.getGridCell()).thenReturn(gridCellMock);

		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator);

		when(mGridObjectsCreator.mArrayListOfCellChanges.size()).thenReturn(1);
		when(mGridObjectsCreator.mArrayListOfCellChanges.get(0)).thenReturn(cellChangeStub);

		mGrid.addMove(cellChangeStub);

		mGrid.undoLastMove();

		verify(gridCageMock).checkUserMath();
	}

	@Test
	public void deselectSelectedCell_CellIsSelected_CellIsDeselected()
			throws Exception {
		// Select a cell
		GridCage gridCageMock = mock(GridCage.class);
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCage()).thenReturn(gridCageMock);
		mGrid.setSelectedCell(gridCellStub);
		verify(gridCageMock).setBorders();

		// Deselect this cell
		mGrid.deselectSelectedCell();

		verify(gridCellStub).deselect();
		verify(gridCageMock, times(2)).setBorders();

		GridCell expectedSelectedCell = null;
		GridCell resultSelectedCell = mGrid.getSelectedCell();
		assertEquals("Selected cell", expectedSelectedCell, resultSelectedCell);
	}

	@Test
	public void setSelectedCell_SelectNullGridCell_Null() throws Exception {
		GridCell resultGridCell = mGrid.setSelectedCell((GridCell) null);
		assertNull(resultGridCell);
	}

	@Test
	public void setSelectedCell_NoCellCurrentlySelectedInGrid_BordersOfNewCageSetAndSelectedCellReturned()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);

		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCage()).thenReturn(gridCageMock);
		mGrid.setSelectedCell(gridCellStub);

		GridCell expectedGridCell = gridCellStub;
		GridCell resultGridCell = mGrid.setSelectedCell(gridCellStub);
		assertEquals("Selected cell", expectedGridCell, resultGridCell);

		verify(gridCageMock).setBorders();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_OldSelectedCellIsDeselected()
			throws Exception {
		GridCage gridCageStub = mock(GridCage.class);

		GridCell gridCellStub1 = mock(GridCell.class);
		when(gridCellStub1.getCage()).thenReturn(gridCageStub);

		GridCell gridCellStub2 = mock(GridCell.class);
		when(gridCellStub2.getCage()).thenReturn(gridCageStub);

		// Select the cells in given order
		mGrid.setSelectedCell(gridCellStub1);
		mGrid.setSelectedCell(gridCellStub2);

		verify(gridCellStub1).deselect();
	}

	@Test
	public void setSelectedCell_CurrentlySelectedCellInGridIsSelectedAgain_NoBordersReset()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);

		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCage()).thenReturn(gridCageMock);

		// Select the grid cell
		mGrid.setSelectedCell(gridCellStub);
		verify(gridCageMock, times(1)).setBorders();

		// Select the same cell one more. The borders may not be reset again.
		mGrid.setSelectedCell(gridCellStub);
		verify(gridCageMock, times(1)).setBorders();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_NoBordersReset()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);

		GridCell gridCellStub1 = mock(GridCell.class);
		when(gridCellStub1.getCage()).thenReturn(gridCageMock);

		GridCell gridCellStub2 = mock(GridCell.class);
		when(gridCellStub2.getCage()).thenReturn(gridCageMock);

		// Select grid cell stub 1
		mGrid.setSelectedCell(gridCellStub1);
		verify(gridCageMock, times(1)).setBorders();

		// Select the other cell in the same cage. The borders may not be reset
		// again.
		mGrid.setSelectedCell(gridCellStub2);
		verify(gridCageMock, times(1)).setBorders();
	}

	@Test
	public void setSelectedCell_SelectCellInAnotherCage_NoBordersReset()
			throws Exception {
		GridCage gridCageMock1 = mock(GridCage.class);

		GridCell gridCellStub1 = mock(GridCell.class);
		when(gridCellStub1.getCage()).thenReturn(gridCageMock1);

		GridCage gridCageMock2 = mock(GridCage.class);

		GridCell gridCellStub2 = mock(GridCell.class);
		when(gridCellStub2.getCage()).thenReturn(gridCageMock2);

		// Select grid cell stub 1
		mGrid.setSelectedCell(gridCellStub1);
		verify(gridCageMock1, times(1)).setBorders();

		// Select the other cell in the same cage. The borders of cage 1 and
		// cage 2 need both to be set.
		mGrid.setSelectedCell(gridCellStub2);
		verify(gridCageMock1, times(2)).setBorders();
		verify(gridCageMock2, times(1)).setBorders();
	}

	@Test
	public void toStorageString_SaveNewGrid_StorageStringCreated()
			throws Exception {
		String resultStorageString = mGrid.toStorageString();

		String expectedStorageString = "GRID:false:false" + "\n";
		Assert.assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCell_StorageStringCreated()
			throws Exception {
		GridCell gridCellStub = mock(GridCell.class);
		String gridCellStubStorageString = "** A CELL STORAGE STRING **";
		when(gridCellStub.toStorageString()).thenReturn(
				gridCellStubStorageString);
		mGrid.mCells.add(gridCellStub);

		String resultStorageString = mGrid.toStorageString();

		String expectedStorageString = "GRID:false:false" + "\n"
				+ gridCellStubStorageString + "\n";
		Assert.assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCell_StorageStringCreated()
			throws Exception {
		GridCell gridCellStub1 = mock(GridCell.class);
		String gridCellStubStorageString1[] = {
				"** FIRST CELL STORAGE STRING **",
				"** SECOND CELL STORAGE STRING **" };
		when(gridCellStub1.toStorageString()).thenReturn(
				gridCellStubStorageString1[0], gridCellStubStorageString1[1]);
		mGrid.mCells.add(gridCellStub1);
		mGrid.mCells.add(gridCellStub1);

		String resultStorageString = mGrid.toStorageString();

		String expectedStorageString = "GRID:false:false" + "\n"
				+ gridCellStubStorageString1[0] + "\n"
				+ gridCellStubStorageString1[1] + "\n";
		Assert.assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCage_StorageStringCreated()
			throws Exception {
		GridCage gridCageStub = mock(GridCage.class);
		String gridCageStubStorageString = "** A CAGE STORAGE STRING **";
		when(gridCageStub.toStorageString()).thenReturn(
				gridCageStubStorageString);
		mGrid.mCages.add(gridCageStub);

		String resultStorageString = mGrid.toStorageString();

		String expectedStorageString = "GRID:false:false" + "\n"
				+ gridCageStubStorageString + "\n";
		Assert.assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCage_StorageStringCreated()
			throws Exception {
		GridCage gridCageStub1 = mock(GridCage.class);
		String gridCageStubStorageString1[] = {
				"** FIRST CAGE STORAGE STRING **",
				"** SECOND CAGE STORAGE STRING **" };
		when(gridCageStub1.toStorageString()).thenReturn(
				gridCageStubStorageString1[0], gridCageStubStorageString1[1]);
		mGrid.mCages.add(gridCageStub1);
		mGrid.mCages.add(gridCageStub1);

		String resultStorageString = mGrid.toStorageString();

		String expectedStorageString = "GRID:false:false" + "\n"
				+ gridCageStubStorageString1[0] + "\n"
				+ gridCageStubStorageString1[1] + "\n";
		Assert.assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCellChange_StorageStringCreated()
			throws Exception {
		CellChange cellChangeStub = mock(CellChange.class);
		String cellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		when(cellChangeStub.toStorageString()).thenReturn(
				cellChangeStubStorageString);
		mGrid.addMove(cellChangeStub);

		String resultStorageString = mGrid.toStorageString();

		String expectedStorageString = "GRID:false:false" + "\n"
				+ cellChangeStubStorageString + "\n";
		Assert.assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCellchange_StorageStringCreated()
			throws Exception {
		CellChange cellChangeStub1 = mock(CellChange.class);
		String cellChangeStubStorageString1 = "** FIRST CELL CHANGE STORAGE STRING **";
		when(cellChangeStub1.toStorageString()).thenReturn(
				cellChangeStubStorageString1);
		mGrid.addMove(cellChangeStub1);

		CellChange cellChangeStub2 = mock(CellChange.class);
		String cellChangeStubStorageString2 = "** SECOND CELL CHANGE STORAGE STRING **";
		when(cellChangeStub2.toStorageString()).thenReturn(
				cellChangeStubStorageString2);
		mGrid.addMove(cellChangeStub2);

		String resultStorageString = mGrid.toStorageString();

		String expectedStorageString = "GRID:false:false" + "\n"
				+ cellChangeStubStorageString1 + "\n"
				+ cellChangeStubStorageString2 + "\n";
		Assert.assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_SaveNewGridWithCellAndCageAndCellChange_StorageStringCreated()
			throws Exception {
		GridCell gridCellStub = mock(GridCell.class);
		String gridCellStubStorageString = "** A CELL STORAGE STRING **";
		when(gridCellStub.toStorageString()).thenReturn(
				gridCellStubStorageString);
		mGrid.mCells.add(gridCellStub);

		GridCage gridCageStub = mock(GridCage.class);
		String gridCageStubStorageString = "** A CAGE STORAGE STRING **";
		when(gridCageStub.toStorageString()).thenReturn(
				gridCageStubStorageString);
		mGrid.mCages.add(gridCageStub);

		CellChange cellChangeStub = mock(CellChange.class);
		String cellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		when(cellChangeStub.toStorageString()).thenReturn(
				cellChangeStubStorageString);
		mGrid.addMove(cellChangeStub);

		String resultStorageString = mGrid.toStorageString();

		String expectedStorageString = "GRID:false:false" + "\n"
				+ gridCellStubStorageString + "\n" + gridCageStubStorageString
				+ "\n" + cellChangeStubStorageString + "\n";
		Assert.assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_ArrayListGridCellsIsNull_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = null;
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.toGridDefinitionString(gridCells, gridCages,
														gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_ArrayListGridCellsIsEmpty_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = new ArrayList<GridCell>();
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.toGridDefinitionString(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_ArrayListGridCagesIsNull_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = mock(ArrayList.class);
		when(gridCells.size()).thenReturn(1);
		ArrayList<GridCage> gridCages = null;
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.toGridDefinitionString(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_ArrayListGridCagesIsEmpty_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = mock(ArrayList.class);
		when(gridCells.size()).thenReturn(1);
		ArrayList<GridCage> gridCages = new ArrayList<GridCage>();
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.toGridDefinitionString(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_GridGeneratingParametersIsNull_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = mock(ArrayList.class);
		when(gridCells.size()).thenReturn(1);
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		when(gridCages.size()).thenReturn(1);
		GridGeneratingParameters gridGeneratingParameters = null;

		mGrid.toGridDefinitionString(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test
	public void toGridDefinitionString_WithValidParameters_GridDefinitionCreated()
			throws Exception {

		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getCageId()).thenReturn(0, 1, 2, 1);
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithGridCells(
				gridCellStub, gridCellStub, gridCellStub, gridCellStub);

		GridCage gridCageStub1 = mock(GridCage.class);
		gridCageStub1.mId = 0;
		gridCageStub1.mResult = 1;
		gridCageStub1.mAction = GridCage.ACTION_NONE;

		GridCage gridCageStub2 = mock(GridCage.class);
		gridCageStub2.mId = 1;
		gridCageStub2.mResult = 3;
		gridCageStub2.mAction = GridCage.ACTION_ADD;

		GridCage gridCageStub3 = mock(GridCage.class);
		gridCageStub3.mId = 2;
		gridCageStub3.mResult = 2;
		gridCageStub3.mAction = GridCage.ACTION_NONE;

		ArrayList<GridCage> gridCages = createArrayListOfGridCagesWithGridCages(
				gridCageStub1, gridCageStub2, gridCageStub3);

		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);
		gridGeneratingParameters.mPuzzleComplexity = GridGenerator.PuzzleComplexity.NORMAL;
		gridGeneratingParameters.mHideOperators = false;

		String resultGridDefinition = mGrid.toGridDefinitionString(gridCells,
				gridCages, gridGeneratingParameters);
		String expectedGridDefinition = "3:00010201:0,1,0:1,3,1:2,2,0";
		assertEquals("Grid definition", expectedGridDefinition,
				resultGridDefinition);
	}

	@Test(expected = NullPointerException.class)
	public void fromStorageString_StorageStringIsNull_NullPointerException()
			throws Exception {
		String storageString = null;
		int revisionNumber = 596;

		mGrid.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringIsEmpty_InvalidParameterException()
			throws Exception {
		String storageString = "";
		int revisionNumber = 596;

		mGrid.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasIncorrectNumberOfElementsForRevisionLessOrEqualTo595_InvalidParameterException()
			throws Exception {
		String storageString = "GRID:too little arguments";
		int revisionNumber = 595;

		mGrid.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasIncorrectNumberOfElementsForRevisionGreaterOrEqualTo596_InvalidParameterException()
			throws Exception {
		String storageString = "GRID:too:many:arguments";
		int revisionNumber = 596;

		mGrid.fromStorageString(storageString, revisionNumber);
	}

	@Test
	public void fromStorageString_InvalidLineId_False() throws Exception {
		String storageString = "WRONG:true:true";
		int revisionNumber = 596;

		boolean resultFromStorageString = mGrid.fromStorageString(storageString,
				revisionNumber);

		assertFalse(resultFromStorageString);
	}

	@Test
	public void fromStorageString_RevisionIdTooLow_False() throws Exception {
		String storageString = "GRID:true:true:1";
		int revisionNumber = 368;

		boolean resultFromStorageString = mGrid.fromStorageString(storageString,
				revisionNumber);

		assertFalse(resultFromStorageString);
	}

	@Test
	public void fromStorageString_ValidStorageStringRevision595_True()
			throws Exception {
		String storageString = "GRID:true:false:1";
		int revisionNumber = 595;

		boolean resultFromStorageString = mGrid.fromStorageString(storageString,
				revisionNumber);

		assertTrue("ResultFromStorageString", resultFromStorageString);
		assertTrue("Grid is active", mGrid.isActive());
		assertFalse("Grid solution is revealed", mGrid.isSolutionRevealed());
		// The last element "1" of the storage string is no longer processed by
		// the method and can therefore not be verified.
	}

	@Test
	public void fromStorageString_ValidStorageStringRevision596_True()
			throws Exception {
		String storageString = "GRID:false:true";
		int revisionNumber = 596;

		boolean resultFromStorageString = mGrid.fromStorageString(storageString,
				revisionNumber);

		assertTrue("ResultFromStorageString", resultFromStorageString);
		assertFalse("Grid is active", mGrid.isActive());
		assertTrue("Grid solution is revealed", mGrid.isSolutionRevealed());
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridCellsListIsNull_InvalidParameterException()
			throws Exception {
		int gridSize = 4;
		ArrayList<GridCell> gridCells = null;
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridCellsListIsEmpty_InvalidParameterException()
			throws Exception {
		int gridSize = 4;
		ArrayList<GridCell> gridCells = new ArrayList<GridCell>();
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridCagesListIsNull_InvalidParameterException()
			throws Exception {
		int gridSize = 4;
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithOneGridCellStub();
		ArrayList<GridCage> gridCages = null;
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridCagesListIsEmpty_InvalidParameterException()
			throws Exception {
		int gridSize = 4;
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithOneGridCellStub();
		ArrayList<GridCage> gridCages = new ArrayList<GridCage>();
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridGeneratingParametersIsNull_InvalidParameterException()
			throws Exception {
		int gridSize = 4;
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithOneGridCellStub();
		ArrayList<GridCage> gridCages = createArrayListOfGridCagesWithOneGridCagestub();
		GridGeneratingParameters gridGeneratingParameters = null;

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test
	public void create_ValidParameters_MovesCleared() throws Exception {
		int gridSize = 4;

		GridCell gridCellMock = mock(GridCell.class);
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithGridCells(
				gridCellMock, gridCellMock);

		GridCage gridCageMock = mock(GridCage.class);
		ArrayList<GridCage> gridCages = createArrayListOfGridCagesWithGridCages(gridCageMock);

		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGridObjectsCreator = new GridObjectsCreator().replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator) {
			@Override
			public boolean insertInDatabase() {
				// Method will be tested in another unit test.
				return true;
			}
		};

		long timeBeforeCreate = System.currentTimeMillis();
		boolean resultCreate = mGrid.create(gridSize, gridCells, gridCages,
				gridGeneratingParameters);
		assertTrue("Creating grid", resultCreate);

		verify(mGridObjectsCreator.mArrayListOfCellChanges).clear();

		GridCell resultGridCell = mGrid.getSelectedCell();
		assertNull("Selected grid cell", resultGridCell);

		boolean resultIsSolutionRevealed = mGrid.isSolutionRevealed();
		assertFalse("Solution revealed", resultIsSolutionRevealed);

		int resultSolvingAttemptId = mGrid.getSolvingAttemptId();
		int expectedSolvingAttemptId = -1;
		assertEquals("Solving attempt id", expectedSolvingAttemptId,
				resultSolvingAttemptId);

		int resultRowId = mGrid.getRowId();
		int expectedRowId = -1;
		assertEquals("Row id", expectedRowId, resultRowId);

		GridStatistics resultGridStatistics = mGrid.getGridStatistics();
		GridStatistics expectedGridStatistics = mGridObjectsCreator.mGridStatisticsMock;
		assertEquals("Grid statistics", expectedGridStatistics,
				resultGridStatistics);

		boolean dateCreateFilledWithCurrentTime = (mGrid.getDateCreated() >= timeBeforeCreate);
		assertTrue("Date created filled with system time",
				dateCreateFilledWithCurrentTime);

		int resultGridSize = mGrid.getGridSize();
		int expectedGridSize = gridSize;
		assertEquals("Grid size", expectedGridSize, resultGridSize);

		ArrayList<GridCell> resultArrayListGridCells = mGrid.mCells;
		ArrayList<GridCell> expectedArrayListGridCells = gridCells;
		assertEquals("Cells", expectedArrayListGridCells,
				resultArrayListGridCells);

		ArrayList<GridCage> resultArrayListGridCages = mGrid.mCages;
		ArrayList<GridCage> expectedArrayListGridCages = gridCages;
		assertEquals("Cells", expectedArrayListGridCages,
				resultArrayListGridCages);

		boolean resultIsActive = mGrid.isActive();
		assertTrue("Is active", resultIsActive);

		verify(gridCageMock).setGridReference(any(Grid.class));
		verify(gridCellMock, times(gridCells.size())).setGridReference(
				any(Grid.class));
		verify(gridCellMock, times(gridCells.size())).setBorders();
	}

	@Test
	public void insertInDatabase_GridWithUniqueGridDefinition_GridAndSolvingAttemptAndStatisticsAreInserted()
			throws Exception {
		// Call the Util class once to instantiate the statics which are used by
		// the insertInDatabase method.
		new Util(mActivity);

		mGridObjectsCreator = new GridObjectsCreator();
		mGrid = new Grid(mGridObjectsCreator) {
			@Override
			public String toGridDefinitionString() {
				return "** A Grid definition string **";
			}
		};
		when(mGridObjectsCreator.mGridDatabaseAdapterMock.getByGridDefinition(anyString())).thenReturn(null);
		when(mGridObjectsCreator.mGridDatabaseAdapterMock.insert(any(Grid.class))).thenReturn(1);
		when(mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class), anyInt()))
				.thenReturn(1);
		when(mGridObjectsCreator.mStatisticsDatabaseAdapterMock.insert(any(Grid.class))).thenReturn(
				mock(GridStatistics.class));

		boolean resultInsertInDatabase = mGrid.insertInDatabase();

		verify(mGridObjectsCreator.mDatabaseHelperMock).beginTransaction();
		verify(mGridObjectsCreator.mGridDatabaseAdapterMock).insert(any(Grid.class));
		verify(mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock).insert(any(Grid.class), anyInt());
		verify(mGridObjectsCreator.mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mGridObjectsCreator.mDatabaseHelperMock).setTransactionSuccessful();
		verify(mGridObjectsCreator.mDatabaseHelperMock).endTransaction();

		assertTrue("Inserted in database", resultInsertInDatabase);
	}

	@Test
	public void insertInDatabase_GridWithExistingGridDefinition_SolvingAttemptAndStatisticsAreInserted()
			throws Exception {
		// Call the Util class once to instantiate the statics which are used by
		// the insertInDatabase method.
		new Util(mActivity);

		mGridObjectsCreator = new GridObjectsCreator();
		mGrid = new Grid(mGridObjectsCreator) {
			@Override
			public String toGridDefinitionString() {
				return "** A Grid definition string **";
			}
		};

		final GridRow gridRow = mock(GridRow.class);
		gridRow.mId = 123;
		when(mGridObjectsCreator.mGridDatabaseAdapterMock.getByGridDefinition(anyString())).thenReturn(
				gridRow);
		when(mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class), anyInt()))
				.thenReturn(1);
		when(mGridObjectsCreator.mStatisticsDatabaseAdapterMock.insert(any(Grid.class))).thenReturn(
				mock(GridStatistics.class));

		boolean resultInsertInDatabase = mGrid.insertInDatabase();

		verify(mGridObjectsCreator.mDatabaseHelperMock).beginTransaction();
		verify(mGridObjectsCreator.mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock).insert(any(Grid.class), anyInt());
		verify(mGridObjectsCreator.mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mGridObjectsCreator.mDatabaseHelperMock).setTransactionSuccessful();
		verify(mGridObjectsCreator.mDatabaseHelperMock).endTransaction();

		assertTrue("Inserted in database", resultInsertInDatabase);
	}

	@Test
	public void save_SaveSolvingAttemptFails_False() throws Exception {
		when(
				mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(false);

		boolean resultSave = mGrid.save();
		assertFalse("Grid save", resultSave);
	}

	@Test
	public void save_GridStatisticsIsNull_GridIsSaved() throws Exception {
		mGridObjectsCreator = new GridObjectsCreator();
		mGridObjectsCreator.mGridStatisticsMock = null;
		mGrid = new Grid(mGridObjectsCreator);

		when(
				mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);

		mGrid.setActive(true);

		assertThat(mGrid.save(), is(true));
	}

	@Test
	public void save_SaveGridStatisticsFails_False() throws Exception {
		when(
				mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);
		when(mGridObjectsCreator.mGridStatisticsMock.save()).thenReturn(false);

		boolean resultSave = mGrid.save();

		verify(mGridObjectsCreator.mGridStatisticsMock).save();
		assertFalse("Grid save", resultSave);
	}

	private void assertThatGridIsSaved(
			boolean grid_IsActive,
			int gridStatistics_ReplayCount,
			boolean gridStatistics_IsIncludedInStatistics,
			int expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics) {
		when(
				mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);
		when(mGridObjectsCreator.mGridStatisticsMock.save()).thenReturn(true);
		when(mGridObjectsCreator.mGridStatisticsMock.getReplayCount()).thenReturn(gridStatistics_ReplayCount);
		when(mGridObjectsCreator.mGridStatisticsMock.isIncludedInStatistics()).thenReturn(
				gridStatistics_IsIncludedInStatistics);
		when(mGridObjectsCreator.mStatisticsDatabaseAdapterMock.update(any(GridStatistics.class)))
				.thenReturn(true);

		mGrid.setActive(grid_IsActive);

		assertThat(mGrid.save(), is(true));
		verify(mGridObjectsCreator.mGridStatisticsMock).save();
		verify(
				mGridObjectsCreator.mStatisticsDatabaseAdapterMock,
				times(expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics))
				.updateSolvingAttemptToBeIncludedInStatistics(anyInt(),
						anyInt());
	}

	@Test
	public void save_GridIsActive_GridSavedButSolvingAttemptsNotUpdated()
			throws Exception {
		boolean grid_IsActive = true;
		int gridStatistics_ReplayCount = 1;
		boolean gridStatistics_IsIncludedInStatistics = false;
		int expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics = 0;

		assertThatGridIsSaved(grid_IsActive, gridStatistics_ReplayCount,
				gridStatistics_IsIncludedInStatistics,
				expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics);
	}

	@Test
	public void save_GridIsNotReplayed_GridSavedButSolvingAttemptsNotUpdated()
			throws Exception {
		boolean grid_IsActive = false;
		int gridStatistics_ReplayCount = 0;
		boolean gridStatistics_IsIncludedInStatistics = false;
		int expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics = 0;

		assertThatGridIsSaved(grid_IsActive, gridStatistics_ReplayCount,
				gridStatistics_IsIncludedInStatistics,
				expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics);
	}

	@Test
	public void save_GridIsNotIncludedInStatistics_GridSavedButSolvingAttemptsNotUpdated()
			throws Exception {
		boolean grid_IsActive = false;
		int gridStatistics_ReplayCount = 1;
		boolean gridStatistics_IsIncludedInStatistics = true;
		int expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics = 0;

		assertThatGridIsSaved(grid_IsActive, gridStatistics_ReplayCount,
				gridStatistics_IsIncludedInStatistics,
				expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics);
	}

	@Test
	public void save_Grid_GridSavedAndSolvingAttemptsNotUpdated()
			throws Exception {
		boolean grid_IsActive = false;
		int gridStatistics_ReplayCount = 1;
		boolean gridStatistics_IsIncludedInStatistics = false;
		int expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics = 1;

		assertThatGridIsSaved(grid_IsActive, gridStatistics_ReplayCount,
				gridStatistics_IsIncludedInStatistics,
				expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics);
	}

	@Test
	public void saveOnUpgrade_SaveSolvingAttemptFails_GridNotSaved()
			throws Exception {
		when(
				mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(false);

		assertThat(mGrid.saveOnAppUpgrade(), is(false));
	}

	@Test
	public void saveOnUpgrade_GridStatisticsIsNull_GridIsSaved()
			throws Exception {
		mGridObjectsCreator = new GridObjectsCreator();
		mGridObjectsCreator.mGridStatisticsMock = null;
		mGrid = new Grid(mGridObjectsCreator);

		when(
				mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(true);

		assertThat(mGrid.saveOnAppUpgrade(), is(true));
	}

	private void assertThatSaveOnUpgrade(boolean resultSaveGridStatistics,
			Matcher expectedResultSaveOnUpgrade) {
		when(
				mGridObjectsCreator.mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(true);

		when(mGridObjectsCreator.mGridStatisticsMock.save()).thenReturn(resultSaveGridStatistics);

		assertThat(mGrid.saveOnAppUpgrade(), expectedResultSaveOnUpgrade);
		verify(mGridObjectsCreator.mGridStatisticsMock).save();
	}

	@Test
	public void saveOnUpgrade_SaveGridStatisticsFails_GridNotSaved()
			throws Exception {
		boolean resultSaveGridStatistics = false;

		assertThatSaveOnUpgrade(resultSaveGridStatistics, is(false));
	}

	@Test
	public void saveOnUpgrade_SaveSolvingAttemptAndGridStatisticsSucceeds_GridIsSaved()
			throws Exception {
		boolean resultSaveGridStatistics = true;

		assertThatSaveOnUpgrade(resultSaveGridStatistics, is(true));
	}

	@Test
	public void load() throws Exception {

	}

	@Test
	public void isEmpty() throws Exception {

	}

	@Test
	public void replay() throws Exception {

	}

	@Test
	public void load1() throws Exception {

	}

	@Test
	public void markDuplicateValuesInRowAndColumn() throws Exception {

	}

	@Test
	public void getPuzzleComplexity() throws Exception {

	}

	@Test
	public void markInvalidChoices() throws Exception {

	}

	@Test
	public void isReplay() throws Exception {

	}

	/**
	 * Creates a new array list and add one Grid Cell stub as list entry.
	 */
	private ArrayList<GridCell> createArrayListOfGridCellsWithOneGridCellStub() {
		ArrayList<GridCell> gridCells = new ArrayList<GridCell>();
		GridCell gridCellMock = mock(GridCell.class);
		gridCells.add(gridCellMock);

		return gridCells;
	}

	/**
	 * Creates a new array list and adds all given grid cells (possibly fakes)
	 * as list entries.
	 */
	private ArrayList<GridCell> createArrayListOfGridCellsWithGridCells(
			GridCell... gridCell) {
		ArrayList<GridCell> gridCells = new ArrayList<GridCell>();

		for (int i = 0; i < gridCell.length; i++) {
			gridCells.add(gridCell[i]);
		}

		return gridCells;
	}

	/**
	 * Creates a new array list and add one Grid Cage stub as list entry.
	 */
	private ArrayList<GridCage> createArrayListOfGridCagesWithOneGridCagestub() {
		ArrayList<GridCage> gridCages = new ArrayList<GridCage>();
		GridCage gridCageMock = mock(GridCage.class);
		gridCages.add(gridCageMock);

		return gridCages;
	}

	/**
	 * Creates a new array list and adds all given grid cages (possibly fakes)
	 * as list entries.
	 */
	private ArrayList<GridCage> createArrayListOfGridCagesWithGridCages(
			GridCage... gridCage) {
		ArrayList<GridCage> gridCages = new ArrayList<GridCage>();

		for (int i = 0; i < gridCage.length; i++) {
			gridCages.add(gridCage[i]);
		}

		return gridCages;
	}
}
