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
import net.mathdoku.plus.storage.database.SolvingAttemptData;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
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

	// Mocks used by the GridObjectsCreator when creating new objects for the
	// Grid.
	private GridCell mGridCellMock = mock(GridCell.class);
	private GridCage mGridCageMock = mock(GridCage.class);
	private CellChange mCellChangeMock = mock(CellChange.class);
	private GridStatistics mGridStatisticsMock = mock(GridStatistics.class);
	private GridGeneratingParameters mGeneratingParametersMock = mock(GridGeneratingParameters.class);
	private GridDatabaseAdapter mGridDatabaseAdapterMock = mock(GridDatabaseAdapter.class);
	private DatabaseHelper mDatabaseHelperMock = mock(DatabaseHelper.class);
	private SolvingAttemptDatabaseAdapter mSolvingAttemptDatabaseAdapterMock = mock(SolvingAttemptDatabaseAdapter.class);
	private StatisticsDatabaseAdapter mStatisticsDatabaseAdapterMock = mock(StatisticsDatabaseAdapter.class);
	private GridCellSelectorInRowOrColumn mGridCellSelectorInRowOrColumn = mock(GridCellSelectorInRowOrColumn.class);
	private GridLoader mGridLoaderMock = mock(GridLoader.class);

	private GridObjectsCreator mGridObjectsCreator;

	private class GridObjectsCreator extends Grid.ObjectsCreator {
		// Array list for Grid Cells to be used when the Grid Cell list is
		// created.
		private ArrayList<GridCell> mArrayListOfGridCells = null;

		// Array lists for Cell Changes to be used when the Cell Change list is
		// created. By default an empty array list is returned.
		private boolean mArrayListOfCellChangesMockReturnsMockOnNextCall = false;
		private boolean mArrayListOfCellChangesMockReturnsNullOnFirstCall = false;
		private ArrayList<CellChange> mArrayListOfCellChangesInitial = null;

		// Unreveal the array list of cell changes as it is hidden in the Grid
		// Object.
		public ArrayList<CellChange> mArrayListOfCellChanges = null;

		@Override
		public GridCell createGridCell(Grid grid, int cell) {
			return mGridCellMock;
		}

		@Override
		public CellChange createCellChange() {
			return mCellChangeMock;
		}

		@Override
		public GridCage createGridCage(Grid grid) {
			return mGridCageMock;
		}

		@Override
		public GridStatistics createGridStatistics() {
			return mGridStatisticsMock;
		}

		@Override
		public GridGeneratingParameters createGridGeneratingParameters() {
			return mGeneratingParametersMock;
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
		public GridObjectsCreator initializeArrayListOfGridCells(
				GridCell... gridCell) {
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
			mArrayListOfCellChangesInitial = mock(ArrayList.class);

			return this;
		}

		public GridObjectsCreator replaceArrayListOfCellChangesWithNullWhenCreatingGrid() {
			if (mArrayListOfCellChanges != null) {
				throw new RuntimeException(
						"Cannot replace Array List of Cell Changes with Mock after Grid has been initialized.");
			}
			mArrayListOfCellChangesMockReturnsNullOnFirstCall = true;

			return this;
		}

		@Override
		public ArrayList<CellChange> createArrayListOfCellChanges() {
			// As the array list of cell changes is not accessible via the Grid,
			// it is unrevealed via the GridObjectCreator.
			if (mArrayListOfCellChangesMockReturnsNullOnFirstCall) {
				mArrayListOfCellChangesMockReturnsNullOnFirstCall = false;
				mArrayListOfCellChanges = null;
			} else {
				mArrayListOfCellChanges = (mArrayListOfCellChangesInitial != null ? mArrayListOfCellChangesInitial
						: super.createArrayListOfCellChanges());
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

		@Override
		public GridLoader createGridLoader(Grid grid) {
			return mGridLoaderMock;
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
		mGrid.setGridSize(5);
		mGrid.setGridSize(6);
	}

	@Test
	public void setPreferences_CreateGrid_PreferencesAreRetrieved()
			throws Exception {
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
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(mGridCellMock,
				mGridCellMock, mGridCellMock, mGridCellMock);
		int numberOfCells = mGrid.mCells.size();

		mGrid.setPreferences();

		verify(mGridCellMock, times(numberOfCells)).setBorders();
	}

	@Test
	public void getSelectedCage_SelectedCellIsNull_NullCage() {
		assertThat("Selected cage", mGrid.getSelectedCage(), is(nullValue()));
	}

	@Test
	public void getSelectedCage_SelectedCellIsNotNull_CageSelected() {
		mGrid.mCages = createArrayListOfGridCagesWithGridCages(
				mock(GridCage.class), mGridCageMock, mock(GridCage.class));
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		mGrid.setSelectedCell(mGridCellMock);

		assertThat("Selected cage", mGrid.getSelectedCage(), is(mGridCageMock));
	}

	@Test
	public void clearCells_GridWithMultipleMovesCleared_AllMovesCleared()
			throws Exception {
		mGrid.addMove(mCellChangeMock);
		mGrid.addMove(mock(CellChange.class));
		mGrid.addMove(mock(CellChange.class));
		mGrid.addMove(mock(CellChange.class));
		assertThat("Number of cell changes before clear",
				mGridObjectsCreator.mArrayListOfCellChanges.size(), is(4));

		// Clear the cells. Value of variable replace is not relevant for this
		// unit test.
		boolean replay = false;
		mGrid.clearCells(replay);

		assertThat("Number of moves for grid after clear",
				mGridObjectsCreator.mArrayListOfCellChanges.size(), is(0));
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_AllCellsCleared()
			throws Exception {
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(mGridCellMock,
				mGridCellMock, mGridCellMock);

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = false;
		mGrid.clearCells(replay);

		// Note: currently a cell is always cleared even in case it does not
		// contain a user values nor any maybe values.
		int expectedNumberOfCells = mGrid.mCells.size();
		verify(mGridCellMock, times(expectedNumberOfCells)).clear();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_FlagsOfAllCellsCleared()
			throws Exception {
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(mGridCellMock,
				mGridCellMock, mGridCellMock);

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = true;
		mGrid.clearCells(replay);

		int expectedNumberOfCells = mGrid.mCells.size();
		verify(mGridCellMock, times(expectedNumberOfCells)).clearAllFlags();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_GridStatisticsUpdated()
			throws Exception {
		when(mGridCellMock.getUserValue()).thenReturn(0, 1, 2, 0);
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(mGridCellMock,
				mGridCellMock, mGridCellMock);

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = true;
		mGrid.clearCells(replay);

		// Clearing a second time won't change the statistics as no cells are
		// left to be cleared
		when(mGridCellMock.getUserValue()).thenReturn(0, 0, 0, 0);
		mGrid.clearCells(replay);

		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_CLEAR_GRID);
	}

	private void assertThatGridCellDoesNotExist(int gridSize, int row, int col) {
		mGrid.setGridSize(gridSize);

		assertThat("GridCell found for row and column",
				mGrid.getCellAt(row, col), is(nullValue()));
	}

	@Test
	public void getCellAt_NegativeRowNumber_GridCellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = -1;
		int col = 1;

		assertThatGridCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_NegativeColNumber_GridCellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = 1;
		int col = -1;

		assertThatGridCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_RowNumberGreaterOrEqualsToGridSize_GridCellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = gridSize;
		int col = 1;

		assertThatGridCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_ColNumberGreaterOrEqualsToGridSize_GridCellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = 1;
		int col = gridSize;

		assertThatGridCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_ValidRowAndColNumber_NotNull() throws Exception {
		int gridSize = 2;
		mGrid.setGridSize(gridSize);
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(
				mock(GridCell.class), mock(GridCell.class),
				mock(GridCell.class), mGridCellMock);

		assertThat("Number of cells in grid", mGrid.mCells.size(), is(gridSize
				* gridSize));

		// Get cell at row 2 and col 2
		int rowForGridCellMock = 2;
		int colForGridCellMock = 2;
		assertThat(
				"Cell retrieved from grid",
				mGrid.getCellAt(rowForGridCellMock - 1, colForGridCellMock - 1),
				is(sameInstance(mGridCellMock)));
	}

	@Test
	public void revealSolution_NullCellList_True() throws Exception {
		// Override the default (empty) array list with null
		mGrid.mCells = null;

		mGrid.revealSolution();

		assertThat("Solution revealed", mGrid.isSolutionRevealed(), is(true));
	}

	@Test
	public void revealSolution_EmptyCellList_True() throws Exception {
		mGrid.revealSolution();

		assertThat("Solution revealed", mGrid.isSolutionRevealed(), is(true));
	}

	@Test
	public void revealSolution_NonEmptyCellListWith2IncorrectCells_TwoCellsRevealed()
			throws Exception {
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(false, true,
				false, true, false);
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(mGridCellMock,
				mGridCellMock, mGridCellMock, mGridCellMock, mGridCellMock);

		mGrid.revealSolution();

		// Check if test isn't flawed with incorrect number of cells in list.
		int expectedNumberOfCellsInList = 5;
		verify(mGridCellMock, times(expectedNumberOfCellsInList))
				.isUserValueIncorrect();

		// Check whether the correct number of cells has been revealed.
		int expectedNumberOfCellsRevealed = 2; // Number of cells with value
												// false
		verify(mGridCellMock, times(expectedNumberOfCellsRevealed))
				.setRevealed();
		verify(mGridCellMock, times(expectedNumberOfCellsRevealed))
				.setUserValue(anyInt());
	}

	@Test
	public void revealSolution_NonEmptyCellList_GridStatisticsUpdated()
			throws Exception {
		mGrid.revealSolution();

		verify(mGridStatisticsMock).solutionRevealed();
	}

	@Test
	public void unrevealSolution() throws Exception {
		Config.AppMode actualAppMode = Config.mAppMode;
		Config.AppMode expectedAppMode = Config.AppMode.DEVELOPMENT;

		assertThat("Development mode", actualAppMode, is(expectedAppMode));
		assertThat("Grid is unrevealed", mGrid.isSolutionRevealed(), is(false));
	}

	@Test
	public void isSolved_UnSolvedGridIsChecked_False() throws Exception {
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(false, false,
				true, false);
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(mGridCellMock,
				mGridCellMock, mGridCellMock, mGridCellMock);

		assertThat("Grid is solved", mGrid.isSolved(), is(false));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridCellMock, atLeast(1)).isUserValueIncorrect();
	}

	@Test
	public void isSolved_SolvedGridIsChecked_True() throws Exception {
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(false, false,
				false, false);
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(mGridCellMock,
				mGridCellMock, mGridCellMock, mGridCellMock);

		assertThat("Grid is solved", mGrid.isSolved(), is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		int expectedNumberOfCellsInList = 4;
		verify(mGridCellMock, times(expectedNumberOfCellsInList))
				.isUserValueIncorrect();
	}

	@Test
	public void setSolved_SolvedGrid_GridStatisticsUpdated() throws Exception {
		mGrid.setSolved();

		verify(mGridStatisticsMock).solved();
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
		when(mGridCellMock.isUserValueSet()).thenReturn(false, false, false,
				false);
		mGrid.mCells = createArrayListOfGridCellsWithGridCells(mGridCellMock,
				mGridCellMock, mGridCellMock, mGridCellMock);

		assertThat("Grid is valid so far", mGrid.isSolutionValidSoFar(),
				is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		int expectedNumberOfCellsInList = 4;
		verify(mGridCellMock, times(expectedNumberOfCellsInList))
				.isUserValueSet();
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsValid_True() throws Exception {
		when(mGridCellMock.isUserValueSet()).thenReturn(true);
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(false);
		mGrid.mCells.add(mGridCellMock);

		assertThat("Grid is valid so far", mGrid.isSolutionValidSoFar(),
				is(true));
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsInvalid_False()
			throws Exception {
		when(mGridCellMock.isUserValueSet()).thenReturn(true);
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(true);
		mGrid.mCells.add(mGridCellMock);

		assertThat("Grid is valid so far", mGrid.isSolutionValidSoFar(),
				is(false));
	}

	@Test
	public void isSolutionValidSoFar_MultipleCellsIncludingAnInvalid_False()
			throws Exception {
		// Create stub for an empty cell
		GridCell gridCellEmptyStub = mock(GridCell.class);
		when(gridCellEmptyStub.isUserValueSet()).thenReturn(false);

		// Create stub for a cell with a correct value
		GridCell gridCellWithValidUserValueStub = mock(GridCell.class);
		when(gridCellWithValidUserValueStub.isUserValueSet()).thenReturn(true);
		when(gridCellWithValidUserValueStub.isUserValueIncorrect()).thenReturn(
				false);

		// Create stub for a cell with a incorrect value
		GridCell gridCellWithInvalidUserValueStub = mock(GridCell.class);
		when(gridCellWithInvalidUserValueStub.isUserValueSet())
				.thenReturn(true);
		when(gridCellWithInvalidUserValueStub.isUserValueIncorrect())
				.thenReturn(true);

		mGrid.mCells = createArrayListOfGridCellsWithGridCells(
				gridCellEmptyStub, gridCellWithValidUserValueStub,
				gridCellWithInvalidUserValueStub,
				gridCellWithInvalidUserValueStub,
				gridCellWithValidUserValueStub);

		assertThat("Grid is valid so far", mGrid.isSolutionValidSoFar(),
				is(false));
	}

	@Test
	public void addMove_FirstMoveAddedToNullList_True() throws Exception {
		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithNullWhenCreatingGrid();
		mGrid = new Grid(mGridObjectsCreator);
		mGrid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreator.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_FirstMoveAddedToEmptyList_True() throws Exception {
		mGrid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreator.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_AddMultipleDifferentMoves_True() throws Exception {
		mGrid.addMove(mCellChangeMock);
		// Add another mock as moves may not be identical for this test
		mGrid.addMove(mock(CellChange.class));

		assertThat("Number of cell changes",
				mGridObjectsCreator.mArrayListOfCellChanges.size(), is(2));
	}

	@Test
	public void addMove_AddMultipleIdenticalMoves_True() throws Exception {
		// Add same mock twice as we need identical moves for this test
		mGrid.addMove(mCellChangeMock);
		mGrid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreator.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void countMoves_MovesListIsNull_ZeroMoves() throws Exception {
		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithNullWhenCreatingGrid();
		mGrid = new Grid(mGridObjectsCreator);

		int actualNumberOfCellChanges = mGrid.countMoves();
		assertThat("Number of moves in a Grid with an empty moves list",
				actualNumberOfCellChanges, is(0));
	}

	@Test
	public void countMoves_MovesListIsNotEmpty_MovesCountedCorrectly()
			throws Exception {
		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator);
		int expectedNumberOfCellChanges = 99;
		when(mGridObjectsCreator.mArrayListOfCellChanges.size()).thenReturn(
				expectedNumberOfCellChanges);

		int actualNumberOfCellChanges = mGrid.countMoves();
		assertThat("Number of moves in a Grid with an empty moves list",
				actualNumberOfCellChanges, is(expectedNumberOfCellChanges));
	}

	@Test
	public void countMoves_MovesListIsEmpty_ZeroMoves() throws Exception {
		int actualNumberOfCellChanges = mGrid.countMoves();
		assertThat("Number of moves in a Grid with an empty moves list",
				actualNumberOfCellChanges, is(0));
	}

	@Test
	public void undoLastMove_NullMovesList_False() throws Exception {
		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithNullWhenCreatingGrid();
		mGrid = new Grid(mGridObjectsCreator);

		assertThat("Undo last move", mGrid.undoLastMove(), is(false));
	}

	@Test
	public void undoLastMove_EmptyMovesList_False() throws Exception {
		assertThat("Undo last move", mGrid.undoLastMove(), is(false));
	}

	@Test
	public void undoLastMove_MovesListWithOneEntry_MoveIsRestored()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		when(mCellChangeMock.getGridCell()).thenReturn(mGridCellMock);
		mGrid.addMove(mCellChangeMock);

		mGrid.undoLastMove();

		verify(mCellChangeMock).restore();
	}

	private void undoLastMove_SetupGridAndMocks() {
		mGridObjectsCreator = new GridObjectsCreator()
				.initializeArrayListOfGridCells(mGridCellMock)
				.replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator);
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		when(mCellChangeMock.getGridCell()).thenReturn(mGridCellMock);
		when(mGridObjectsCreator.mArrayListOfCellChanges.size()).thenReturn(1);
		when(mGridObjectsCreator.mArrayListOfCellChanges.get(0)).thenReturn(
				mCellChangeMock);
		mGrid.addMove(mCellChangeMock);
	}

	@Test
	public void undoLastMove_UserValueOfCellNotFilledBeforeNorAfterUndo_UndoSucceededAndDuplicateValuesInSameRowAndColumnNotChecked()
			throws Exception {
		undoLastMove_SetupGridAndMocks();
		// This test simulates following:
		// 1) Before undo the cell is empty. After undo the cell is filled with
		// one or more maybe values.
		// 2) Before undo the cell contains on or more maybe values. After undo
		// the cell contains another set of maybe values (including 0 maybe
		// values).
		// In both cases the user value equals 0 before and after the actual
		// undo, indicating that the cell does not contain a user value.
		when(mGridCellMock.getUserValue()).thenReturn( //
				0 /* value before actual undo */, //
				0 /* value after actual undo */);

		mGrid.undoLastMove();

		verify(mCellChangeMock).restore();
		verify(mGridObjectsCreator.mArrayListOfCellChanges).remove(anyInt());
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_UNDO_MOVE);
		assertThat("Selected cell", mGrid.getSelectedCell(),
				is(sameInstance(mGridCellMock)));

		// In case the user value of a cell is not changed, duplicate values are
		// not checked.
		verify(mGridCellMock, times(0)).getRow();
		verify(mGridCellMock, times(0)).markDuplicateValuesInSameRowAndColumn();
		verify(mGridCageMock, times(0)).checkUserMath();
	}

	@Test
	public void undoLastMove_UserValueOfCellChanges_DuplicateValuesInSameRowAndColumnMarkedAndCageMathChecked()
			throws Exception {
		undoLastMove_SetupGridAndMocks();
		// This test simulates following:
		// 1) Before undo the cell is empty. After undo the cell is filled with
		// a user value.
		// 2) Before undo the cell is filled with a user value. After undo the
		// cell is filled with another user value.
		// 3) Before undo the cell is filled with a user value. After undo the
		// cell does not contain a user value; the cell can contain a maybe
		// value.
		when(mGridCellMock.getUserValue()).thenReturn( //
				0 /* value before actual undo */, //
				1 /* value after actual undo */);

		ArrayList<GridCell> arrayListOfGridCells = new ArrayList<GridCell>();
		arrayListOfGridCells.add(mGridCellMock);
		when(mGridCellSelectorInRowOrColumn.find()).thenReturn(
				arrayListOfGridCells);

		mGrid.undoLastMove();

		verify(mGridCellMock).markDuplicateValuesInSameRowAndColumn();
		verify(mGridCageMock).checkUserMath();
	}

	@Test
	public void deselectSelectedCell_CellIsSelected_CellIsDeselected()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		mGrid.setSelectedCell(mGridCellMock);
		verify(mGridCageMock).setBorders();

		// Deselect this cell
		mGrid.deselectSelectedCell();

		verify(mGridCellMock).deselect();
		verify(mGridCageMock, times(2)).setBorders();

		assertThat("Selected cell", mGrid.getSelectedCell(), is(nullValue()));
	}

	@Test
	public void setSelectedCell_SelectNullGridCell_Null() throws Exception {
		assertThat("Selected cell", mGrid.setSelectedCell((GridCell) null),
				is(nullValue()));
	}

	@Test
	public void setSelectedCell_NoCellCurrentlySelectedInGrid_BordersOfNewCageSetAndSelectedCellReturned()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);

		assertThat("Selected cell", mGrid.setSelectedCell(mGridCellMock),
				is(sameInstance(mGridCellMock)));

		verify(mGridCageMock).setBorders();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_OldSelectedCellIsDeselected()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		GridCell otherGridCellMock = mock(GridCell.class);
		when(otherGridCellMock.getCage()).thenReturn(mGridCageMock);

		// Select the cells in given order
		mGrid.setSelectedCell(mGridCellMock);
		mGrid.setSelectedCell(otherGridCellMock);

		verify(mGridCellMock).deselect();
	}

	@Test
	public void setSelectedCell_CurrentlySelectedCellInGridIsSelectedAgain_NoBordersReset()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);

		// Select the grid cell
		mGrid.setSelectedCell(mGridCellMock);
		verify(mGridCageMock, times(1)).setBorders();

		// Select the same cell one more. The borders may not be reset again.
		mGrid.setSelectedCell(mGridCellMock);
		verify(mGridCageMock, times(1)).setBorders();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_NoBordersReset()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);

		GridCell otherGridCellMock = mock(GridCell.class);
		when(otherGridCellMock.getCage()).thenReturn(mGridCageMock);

		// Select grid cell stub 1
		mGrid.setSelectedCell(mGridCellMock);
		verify(mGridCageMock, times(1)).setBorders();

		// Select the other cell in the same cage. The borders may not be reset
		// again.
		mGrid.setSelectedCell(otherGridCellMock);
		verify(mGridCageMock, times(1)).setBorders();
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
		assertThat("Storage string", mGrid.toStorageString(),
				is(equalTo("GRID:false:false" + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCell_StorageStringCreated()
			throws Exception {
		String gridCellStubStorageString = "** A CELL STORAGE STRING **";
		when(mGridCellMock.toStorageString()).thenReturn(
				gridCellStubStorageString);
		mGrid.mCells.add(mGridCellMock);

		assertThat("Storage string", mGrid.toStorageString(),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCellStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCell_StorageStringCreated()
			throws Exception {
		String gridCellStubStorageString1[] = {
				"** FIRST CELL STORAGE STRING **",
				"** SECOND CELL STORAGE STRING **" };
		when(mGridCellMock.toStorageString()).thenReturn(
				gridCellStubStorageString1[0], gridCellStubStorageString1[1]);
		mGrid.mCells.add(mGridCellMock);
		mGrid.mCells.add(mGridCellMock);

		assertThat("Storage string", mGrid.toStorageString(),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCellStubStorageString1[0] + "\n"
						+ gridCellStubStorageString1[1] + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCage_StorageStringCreated()
			throws Exception {
		String gridCageStubStorageString = "** A CAGE STORAGE STRING **";
		when(mGridCageMock.toStorageString()).thenReturn(
				gridCageStubStorageString);
		mGrid.mCages.add(mGridCageMock);

		assertThat("Storage string", mGrid.toStorageString(),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCageStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCage_StorageStringCreated()
			throws Exception {
		String gridCageStubStorageString1[] = {
				"** FIRST CAGE STORAGE STRING **",
				"** SECOND CAGE STORAGE STRING **" };
		when(mGridCageMock.toStorageString()).thenReturn(
				gridCageStubStorageString1[0], gridCageStubStorageString1[1]);
		mGrid.mCages.add(mGridCageMock);
		mGrid.mCages.add(mGridCageMock);

		assertThat("Storage string", mGrid.toStorageString(),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCageStubStorageString1[0] + "\n"
						+ gridCageStubStorageString1[1] + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCellChange_StorageStringCreated()
			throws Exception {
		String mCellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		when(mCellChangeMock.toStorageString()).thenReturn(
				mCellChangeStubStorageString);
		mGrid.addMove(mCellChangeMock);

		assertThat("Storage string", mGrid.toStorageString(),
				is(equalTo("GRID:false:false" + "\n"
						+ mCellChangeStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCellChange_StorageStringCreated()
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

		assertThat("Storage string", mGrid.toStorageString(),
				is(equalTo("GRID:false:false" + "\n"
						+ cellChangeStubStorageString1 + "\n"
						+ cellChangeStubStorageString2 + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithCellAndCageAndCellChange_StorageStringCreated()
			throws Exception {
		String gridCellStubStorageString = "** A CELL STORAGE STRING **";
		when(mGridCellMock.toStorageString()).thenReturn(
				gridCellStubStorageString);
		mGrid.mCells.add(mGridCellMock);

		String gridCageStubStorageString = "** A CAGE STORAGE STRING **";
		when(mGridCageMock.toStorageString()).thenReturn(
				gridCageStubStorageString);
		mGrid.mCages.add(mGridCageMock);

		String mCellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		when(mCellChangeMock.toStorageString()).thenReturn(
				mCellChangeStubStorageString);
		mGrid.addMove(mCellChangeMock);

		assertThat("Storage string", mGrid.toStorageString(),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCellStubStorageString + "\n"
						+ gridCageStubStorageString + "\n"
						+ mCellChangeStubStorageString + "\n")));
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

		when(mGridCellMock.getCageId()).thenReturn(0, 1, 2, 1);
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithGridCells(
				mGridCellMock, mGridCellMock, mGridCellMock, mGridCellMock);

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

		mGeneratingParametersMock.mPuzzleComplexity = GridGenerator.PuzzleComplexity.NORMAL;
		mGeneratingParametersMock.mHideOperators = false;

		assertThat("Grid definition", mGrid.toGridDefinitionString(gridCells,
				gridCages, mGeneratingParametersMock),
				is(equalTo("3:00010201:0,1,0:1,3,1:2,2,0")));
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

		assertThat(mGrid.fromStorageString(storageString, revisionNumber),
				is(false));
	}

	@Test
	public void fromStorageString_RevisionIdTooLow_False() throws Exception {
		String storageString = "GRID:true:true:1";
		int revisionNumber = 368;

		assertThat(mGrid.fromStorageString(storageString, revisionNumber),
				is(false));
	}

	@Test
	public void fromStorageString_ValidStorageStringRevision595_True()
			throws Exception {
		String storageString = "GRID:true:false:1";
		int revisionNumber = 595;

		boolean resultFromStorageString = mGrid.fromStorageString(
				storageString, revisionNumber);

		assertThat("ResultFromStorageString", resultFromStorageString, is(true));
		assertThat("Grid is active", mGrid.isActive(), is(true));
		assertThat("Grid solution is revealed", mGrid.isSolutionRevealed(),
				is(false));
		// The last element of the storage string ("1") is no longer processed
		// by the method and can therefore not be verified.
	}

	@Test
	public void fromStorageString_ValidStorageStringRevision596_True()
			throws Exception {
		String storageString = "GRID:false:true";
		int revisionNumber = 596;

		boolean resultFromStorageString = mGrid.fromStorageString(
				storageString, revisionNumber);

		assertThat("ResultFromStorageString", resultFromStorageString, is(true));
		assertThat("Grid is active", mGrid.isActive(), is(false));
		assertThat("Grid solution is revealed", mGrid.isSolutionRevealed(),
				is(true));
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridCellsListIsNull_InvalidParameterException()
			throws Exception {
		// Do not use the mocking objects which are use by the
		// GridObjectsCreator as method Grid.create() should be executed on the
		// default values as set by the Grid constructor.
		int gridSize = 4;
		ArrayList<GridCell> gridCells = null;
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridCellsListIsEmpty_InvalidParameterException()
			throws Exception {
		// Do not use the mocking objects which are use by the
		// GridObjectsCreator as the Grid.create() should be executed on the
		// default values as set by the Grid constructor.
		int gridSize = 4;
		ArrayList<GridCell> gridCells = new ArrayList<GridCell>();
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridCagesListIsNull_InvalidParameterException()
			throws Exception {
		// Do not use the mocking objects which are use by the
		// GridObjectsCreator as the Grid.create() should be executed on the
		// default values as set by the Grid constructor.
		int gridSize = 4;
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithOneGridCellStub();
		ArrayList<GridCage> gridCages = null;
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridCagesListIsEmpty_InvalidParameterException()
			throws Exception {
		// Do not use the mocking objects which are use by the
		// GridObjectsCreator as the Grid.create() should be executed on the
		// default values as set by the Grid constructor.
		int gridSize = 4;
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithOneGridCellStub();
		ArrayList<GridCage> gridCages = new ArrayList<GridCage>();
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void create_GridGeneratingParametersIsNull_InvalidParameterException()
			throws Exception {
		// Do not use the mocking objects which are use by the
		// GridObjectsCreator as the Grid.create() should be executed on the
		// default values as set by the Grid constructor.
		int gridSize = 4;
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithOneGridCellStub();
		ArrayList<GridCage> gridCages = createArrayListOfGridCagesWithOneGridCagestub();
		GridGeneratingParameters gridGeneratingParameters = null;

		mGrid.create(gridSize, gridCells, gridCages, gridGeneratingParameters);
	}

	@Test
	public void create_ValidParameters_MovesCleared() throws Exception {
		mGridObjectsCreator = new GridObjectsCreator()
				.replaceArrayListOfCellChangesWithMock();
		mGrid = new Grid(mGridObjectsCreator) {
			@Override
			public boolean insertInDatabase() {
				// Method will be tested in another unit test.
				return true;
			}
		};

		// Do not use the mocking objects which are use by the
		// GridObjectsCreator as the Grid.create() should be executed on the
		// default values as set by the Grid constructor.
		int gridSize = 4;
		ArrayList<GridCell> gridCells = createArrayListOfGridCellsWithGridCells(
				mGridCellMock, mGridCellMock);
		ArrayList<GridCage> gridCages = createArrayListOfGridCagesWithGridCages(mGridCageMock);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		long timeBeforeCreate = System.currentTimeMillis();
		assertThat("Creating grid", mGrid.create(gridSize, gridCells,
				gridCages, gridGeneratingParameters), is(true));
		verify(mGridObjectsCreator.mArrayListOfCellChanges).clear();
		assertThat("Selected grid cell", mGrid.getSelectedCell(),
				is(nullValue()));
		assertThat("Solution revealed", mGrid.isSolutionRevealed(), is(false));
		assertThat("Solving attempt id", mGrid.getSolvingAttemptId(),
				is(equalTo(-1)));
		assertThat("Row id", mGrid.getRowId(), is(equalTo(-1)));
		assertThat("Grid statistics", mGrid.getGridStatistics(),
				is(sameInstance(mGridStatisticsMock)));
		assertThat("Date created filled with system time",
				(mGrid.getDateCreated() >= timeBeforeCreate), is(true));
		assertThat("Grid size", mGrid.getGridSize(), is(gridSize));
		assertThat("Cells", mGrid.mCells, is(sameInstance(gridCells)));
		assertThat("Cells", mGrid.mCages, is(sameInstance(gridCages)));
		assertThat("Is active", mGrid.isActive(), is(true));
		verify(mGridCageMock).setGridReference(any(Grid.class));
		verify(mGridCellMock, times(gridCells.size())).setGridReference(
				any(Grid.class));
		verify(mGridCellMock, times(gridCells.size())).setBorders();
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
		when(mGridDatabaseAdapterMock.getByGridDefinition(anyString()))
				.thenReturn(null);
		when(mGridDatabaseAdapterMock.insert(any(Grid.class))).thenReturn(1);
		when(
				mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class),
						anyInt())).thenReturn(1);
		when(mStatisticsDatabaseAdapterMock.insert(any(Grid.class)))
				.thenReturn(mock(GridStatistics.class));

		boolean resultInsertInDatabase = mGrid.insertInDatabase();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(any(Grid.class),
				anyInt());
		verify(mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();

		assertThat("Inserted in database", resultInsertInDatabase, is(true));
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
		when(mGridDatabaseAdapterMock.getByGridDefinition(anyString()))
				.thenReturn(gridRow);
		when(
				mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class),
						anyInt())).thenReturn(1);
		when(mStatisticsDatabaseAdapterMock.insert(any(Grid.class)))
				.thenReturn(mock(GridStatistics.class));

		boolean resultInsertInDatabase = mGrid.insertInDatabase();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(any(Grid.class),
				anyInt());
		verify(mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();

		assertThat("Inserted in database", resultInsertInDatabase, is(true));
	}

	@Test
	public void save_SaveSolvingAttemptFails_False() throws Exception {
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(false);

		assertThat("Grid saved?", mGrid.save(), is(false));
	}

	@Test
	public void save_GridStatisticsIsNull_GridIsSaved() throws Exception {
		mGridObjectsCreator = new GridObjectsCreator();
		mGridStatisticsMock = null;
		mGrid = new Grid(mGridObjectsCreator);
		mGrid.setActive(true);
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);

		assertThat("Grid saved?", mGrid.save(), is(true));
	}

	@Test
	public void save_SaveGridStatisticsFails_False() throws Exception {
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);
		when(mGridStatisticsMock.save()).thenReturn(false);

		boolean resultSave = mGrid.save();

		verify(mGridStatisticsMock).save();
		assertThat("Grid saved?", mGrid.save(), is(false));
	}

	private void assertThatGridIsSaved(
			boolean grid_IsActive,
			int gridStatistics_ReplayCount,
			boolean gridStatistics_IsIncludedInStatistics,
			int expectedNumberOfCallsToUpdateSolvingAttemptToBeIncludedInStatistics) {
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);
		when(mGridStatisticsMock.save()).thenReturn(true);
		when(mGridStatisticsMock.getReplayCount()).thenReturn(
				gridStatistics_ReplayCount);
		when(mGridStatisticsMock.isIncludedInStatistics()).thenReturn(
				gridStatistics_IsIncludedInStatistics);
		when(mStatisticsDatabaseAdapterMock.update(any(GridStatistics.class)))
				.thenReturn(true);

		mGrid.setActive(grid_IsActive);

		assertThat("Grid is saved?", mGrid.save(), is(true));
		verify(mGridStatisticsMock).save();
		verify(
				mStatisticsDatabaseAdapterMock,
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
				mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(false);

		assertThat("Grid is saved?", mGrid.saveOnAppUpgrade(), is(false));
	}

	@Test
	public void saveOnUpgrade_GridStatisticsIsNull_GridIsSaved()
			throws Exception {
		mGridObjectsCreator = new GridObjectsCreator();
		mGridStatisticsMock = null;
		mGrid = new Grid(mGridObjectsCreator);

		when(
				mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(true);

		assertThat("Grid is saved?", mGrid.saveOnAppUpgrade(), is(true));
	}

	private void assertThatSaveOnUpgrade(boolean resultSaveGridStatistics,
			Matcher expectedResultSaveOnUpgrade) {
		when(
				mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(true);

		when(mGridStatisticsMock.save()).thenReturn(resultSaveGridStatistics);

		assertThat("Grid is saved?", mGrid.saveOnAppUpgrade(),
				expectedResultSaveOnUpgrade);
		verify(mGridStatisticsMock).save();
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
	public void load_ViaSolvingAttemptIdWhichDoesNotExist_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 12;
		when(mSolvingAttemptDatabaseAdapterMock.getData(anyInt())).thenReturn(
				null);

		assertThat("Loading solving attempt", mGrid.load(solvingAttemptId),
				is(false));
	}

	@Test
	public void load_ViaSolvingAttemptButGridDoesNotExist_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 12;
		SolvingAttemptData solvingAttemptData = mock(SolvingAttemptData.class);
		when(mSolvingAttemptDatabaseAdapterMock.getData(anyInt())).thenReturn(
				solvingAttemptData);
		when(mGridDatabaseAdapterMock.get(anyInt())).thenReturn(null);

		assertThat("Loading solving attempt", mGrid.load(solvingAttemptId),
				is(false));
		verify(mGridDatabaseAdapterMock).get(anyInt());
	}

	@Test
	public void load_ViaSolvingAttemptButLoadSolvingAttemptDataFailed_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 12;
		SolvingAttemptData solvingAttemptData = mock(SolvingAttemptData.class);
		when(mSolvingAttemptDatabaseAdapterMock.getData(anyInt())).thenReturn(
				solvingAttemptData);
		when(mGridDatabaseAdapterMock.get(anyInt())).thenReturn(
				mock(GridRow.class));
		when(mGridLoaderMock.load(any(SolvingAttemptData.class))).thenReturn(
				false);

		assertThat("Loading solving attempt", mGrid.load(solvingAttemptId),
				is(false));
		verify(mGridDatabaseAdapterMock).get(anyInt());
		verify(mGridLoaderMock).load(any(SolvingAttemptData.class));
	}

	@Test
	public void load_ViaSolvingAttempt_GridLoaded() throws Exception {
		int solvingAttemptId = 12;
		SolvingAttemptData solvingAttemptData = mock(SolvingAttemptData.class);
		when(mSolvingAttemptDatabaseAdapterMock.getData(anyInt())).thenReturn(
				solvingAttemptData);
		when(mGridDatabaseAdapterMock.get(anyInt())).thenReturn(
				mock(GridRow.class));
		when(mGridLoaderMock.load(any(SolvingAttemptData.class))).thenReturn(
				true);

		assertThat("Loading solving attempt", mGrid.load(solvingAttemptId),
				is(true));
		verify(mGridDatabaseAdapterMock).get(anyInt());
		verify(mGridLoaderMock).load(any(SolvingAttemptData.class));
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
