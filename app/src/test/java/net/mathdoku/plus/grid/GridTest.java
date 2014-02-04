package net.mathdoku.plus.grid;

import android.app.Activity;
import android.content.Context;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;
import net.mathdoku.plus.util.Util;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
	private GridBuilderStub mGridBuilderStub;

	/**
	 * The GridBuilderStub is used set up a default grid builder for each test
	 * case. It has some additional methods for tweaking the setup for specific
	 * test cases.
	 */
	private class GridBuilderStub extends GridBuilder {
		/*
		 * By default a 4 x 4 Grid is created in which each cell and each cage
		 * is represented with a unique mock.
		 */
		private boolean mUseSameMockForAllGridCells = false;
		private boolean mUseSameMockForAllGridCages = false;

		/*
		 * Variables below refer to the last created grid cell mock and the grid
		 * cage mock. In case the same mock has to be used for all cells, the
		 * variables below are use for each such mock.
		 */
		public GridCell mAnyGridCellMockOfDefaultSetup = null;
		public GridCell mGridCellMockOfDefaultSetup[] = null;
		public GridCage mGridCageOfDefaultSetup = null;

		public GridBuilderStub useSameMockForAllGridCells() {
			mUseSameMockForAllGridCells = true;

			return this;
		}

		public GridBuilderStub useSameMockForAllGridCages() {
			mUseSameMockForAllGridCages = true;
			mAnyGridCellMockOfDefaultSetup = null;
			mGridCellMockOfDefaultSetup = null;
			mGridCageOfDefaultSetup = null;
			return setupDefaultWhichDoesNotThrowErrorsOnBuild();
		}

		public GridBuilderStub setupDefaultWhichDoesNotThrowErrorsOnBuild() {
			int gridSize = 4;
			setGridSize(gridSize);

			// Insert exact number of cells needed with this grid size. A
			// reference to the last created grid cell mock is kept for tests
			// which need just a cell in the default grid.
			int numberOfCells = gridSize * gridSize;
			ArrayList<GridCell> gridCells = mGridObjectsCreator
					.createArrayListOfGridCells();
			mGridCellMockOfDefaultSetup = new GridCell[numberOfCells];
			for (int i = 0; i < numberOfCells; i++) {
				if (mUseSameMockForAllGridCells == false || i == 0) {
					mGridCellMockOfDefaultSetup[i] = mock(GridCell.class);
				} else {
					mGridCellMockOfDefaultSetup[i] = mGridCellMockOfDefaultSetup[0];
				}
				gridCells.add(mGridCellMockOfDefaultSetup[i]);
			}
			super.setCells(gridCells);
			mAnyGridCellMockOfDefaultSetup = mGridCellMockOfDefaultSetup[15];
			// mAnyGridCellMockOfDefaultSetup = mGridCellMockDefaultSetup[5];

			// Insert an arbitrary number of cages (at least 1). A
			// reference to the last created grid cage mock is kept for tests
			// which need just a cage in the default grid.
			int numberOfCages = 3;
			ArrayList<GridCage> gridCages = mGridObjectsCreator
					.createArrayListOfGridCages();
			for (int i = 0; i < numberOfCages; i++) {
				if (mUseSameMockForAllGridCages == false
						|| mGridCageOfDefaultSetup == null) {
					mGridCageOfDefaultSetup = mock(GridCage.class);
				}
				gridCages.add(mGridCageOfDefaultSetup);
			}
			super.setCages(gridCages);

			super.setGridStatistics(mGridObjectsCreator.createGridStatistics());

			super.setGridGeneratingParameters(mGridObjectsCreator
					.createGridGeneratingParameters());

			return this;
		}

		/**
		 * Initializes the list of cells of the GridBuilder with the given grid
		 * cells.
		 */
		public GridBuilderStub setCellsInitializedWith(GridCell... gridCell) {
			// Default setup is no longer valid as the list of cells is
			// replaced.
			mAnyGridCellMockOfDefaultSetup = null;

			ArrayList<GridCell> gridCells = new ArrayList<GridCell>();

			for (int i = 0; i < gridCell.length; i++) {
				gridCells.add(gridCell[i]);
			}
			super.setCells(gridCells);

			return this;
		}

		/**
		 * Initializes the list of cages of the GridBuilder with the given grid
		 * cages.
		 */
		public GridBuilderStub setCagesInitializedWith(GridCage... gridCage) {
			ArrayList<GridCage> gridCages = new ArrayList<GridCage>();

			for (int i = 0; i < gridCage.length; i++) {
				gridCages.add(gridCage[i]);
			}
			super.setCages(gridCages);

			return this;
		}

		/**
		 * Initializes the list of cell changess of the GridBuilder with the
		 * given cell changes.
		 */
		public GridBuilderStub setCellChangesInitializedWith(
				CellChange... cellChange) {
			ArrayList<CellChange> cellChanges = new ArrayList<CellChange>();

			for (int i = 0; i < cellChange.length; i++) {
				cellChanges.add(cellChange[i]);
			}
			super.setCellChanges(cellChanges);

			return this;
		}
	}

	// Mocks used by the GridObjectsCreatorStub when creating new objects for
	// the Grid.
	private CellChange mCellChangeMock = mock(CellChange.class);
	private GridStatistics mGridStatisticsMock = mock(GridStatistics.class);
	private GridGeneratingParameters mGridGeneratingParametersMock = mock(GridGeneratingParameters.class);
	private GridDatabaseAdapter mGridDatabaseAdapterMock = mock(GridDatabaseAdapter.class);
	private DatabaseHelper mDatabaseHelperMock = mock(DatabaseHelper.class);
	private SolvingAttemptDatabaseAdapter mSolvingAttemptDatabaseAdapterMock = mock(SolvingAttemptDatabaseAdapter.class);
	private StatisticsDatabaseAdapter mStatisticsDatabaseAdapterMock = mock(StatisticsDatabaseAdapter.class);
	private GridCellSelectorInRowOrColumn mGridCellSelectorInRowOrColumn = mock(GridCellSelectorInRowOrColumn.class);

	private GridObjectsCreatorStub mGridObjectsCreatorStub;

	private class GridObjectsCreatorStub extends GridObjectsCreator {
		// Unreveal the array list of cell changes as it is hidden in the Grid
		// Object.
		public ArrayList<CellChange> mArrayListOfCellChanges = null;

		@Override
		public GridStatistics createGridStatistics() {
			return mGridStatisticsMock;
		}

		@Override
		public GridGeneratingParameters createGridGeneratingParameters() {
			return mGridGeneratingParametersMock;
		}

		@Override
		public ArrayList<CellChange> createArrayListOfCellChanges() {
			mArrayListOfCellChanges = super.createArrayListOfCellChanges();
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

		mGridObjectsCreatorStub = new GridObjectsCreatorStub();
		mGridBuilderStub = new GridBuilderStub();
		mGridBuilderStub.setObjectsCreator(mGridObjectsCreatorStub);
		mGridBuilderStub.setupDefaultWhichDoesNotThrowErrorsOnBuild();
	}

	@Test
	public void setPreferences_CreateGrid_PreferencesAreRetrieved()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.setPreferences();

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
		Grid grid = mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild()
				.build();
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(grid.mCells.size())).setBorders();

		grid.setPreferences();

		// While building the grid, the borders were already set (see verify
		// above). Now check if it has been called again while setting the
		// preferences.
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(2 * grid.mCells.size())).setBorders();
	}

	@Test
	public void getSelectedCage_SelectedCellIsNull_NullCage() {
		Grid grid = mGridBuilderStub.build();
		assertThat("Selected cage", grid.getSelectedCage(), is(nullValue()));
	}

	@Test
	public void getSelectedCage_SelectedCellIsNotNull_CageSelected() {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();
		grid.setSelectedCell(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);

		assertThat("Selected cage", grid.getSelectedCage(),
				is(mGridBuilderStub.mGridCageOfDefaultSetup));
	}

	@Test
	public void clearCells_GridWithMultipleMovesCleared_AllMovesCleared()
			throws Exception {
		mGridBuilderStub.setCellChangesInitializedWith(mCellChangeMock,
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class));
		Grid grid = mGridBuilderStub.build();
		assertThat("Number of cell changes before clear", grid.countMoves(),
				is(mGridBuilderStub.mCellChanges.size()));

		// Clear the cells. Value of variable replace is not relevant for this
		// unit test.
		boolean replay = false;
		grid.clearCells(replay);

		assertThat("Number of moves for grid after clear", grid.countMoves(),
				is(0));
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_AllCellsCleared()
			throws Exception {
		Grid grid = mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild()
				.build();

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = false;
		grid.clearCells(replay);

		// Note: currently a cell is always cleared even in case it does not
		// contain a user values nor any maybe values.
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(grid.mCells.size())).clear();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_FlagsOfAllCellsCleared()
			throws Exception {
		Grid grid = mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild()
				.build();

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = true;
		grid.clearCells(replay);

		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(grid.mCells.size())).clearAllFlags();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_GridStatisticsUpdated()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getUserValue())
				.thenReturn(0, 1, 2, 0);
		Grid grid = mGridBuilderStub.build();

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = true;
		grid.clearCells(replay);

		// Clearing a second time won't change the statistics as no cells are
		// left to be cleared
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getUserValue())
				.thenReturn(0, 0, 0, 0);
		grid.clearCells(replay);

		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_CLEAR_GRID);
	}

	private void assertThatGridCellDoesNotExist(int gridSize, int row, int col) {
		mGridBuilderStub.setGridSize(gridSize);
		Grid grid = mGridBuilderStub.build();

		assertThat("GridCell found for row and column",
				grid.getCellAt(row, col), is(nullValue()));
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
		Grid grid = mGridBuilderStub.build();

		assertThat(
				"Cell retrieved from grid",
				grid.getCellAt(mGridBuilderStub.mGridSize - 1,
						mGridBuilderStub.mGridSize - 1),
				is(sameInstance(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup)));
	}

	@Test
	public void revealSolution_NonEmptyCellListWith2IncorrectCells_TwoCellsRevealed()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(false, true, false, true, false);
		Grid grid = mGridBuilderStub.build();

		grid.revealSolution();

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).isUserValueIncorrect();

		// Check whether the correct number of cells has been revealed.
		int expectedNumberOfCellsRevealed = 2; // Number of cells with value
												// false
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(expectedNumberOfCellsRevealed)).setRevealed();
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(expectedNumberOfCellsRevealed)).setUserValue(anyInt());
	}

	@Test
	public void revealSolution_GridWithHiddenOperators_OperatorsRevealed()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllGridCells()
				.useSameMockForAllGridCages()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		mGridBuilderStub.mGridGeneratingParametersDefaultSetup.mHideOperators = true;
		Grid grid = mGridBuilderStub.build();
		// During setup of default grid the method setCageText is already called once for each cage.
		verify(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup, times(mGridBuilderStub.mCages.size()))
				.setCageText(anyString());

		grid.revealSolution();

		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup, times(mGridBuilderStub.mCages.size())).revealOperator();
		// Check if setCageText is called a second time for each cage
		verify(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup, times(2 * mGridBuilderStub.mCages.size()))
				.setCageText(anyString());

	}

	@Test
	public void revealSolution_NonEmptyCellList_GridStatisticsUpdated()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.revealSolution();

		verify(mGridStatisticsMock).solutionRevealed();
	}

	@Test
	public void unrevealSolution() throws Exception {
		Grid grid = mGridBuilderStub.build();
		Config.AppMode actualAppMode = Config.mAppMode;
		Config.AppMode expectedAppMode = Config.AppMode.DEVELOPMENT;

		assertThat("Development mode", actualAppMode, is(expectedAppMode));
		assertThat("Grid is unrevealed", grid.isSolutionRevealed(), is(false));
	}

	@Test
	public void isSolved_UnSolvedGridIsChecked_False() throws Exception {
		mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(false, false, true, false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is solved", grid.isSolved(), is(false));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup, atLeast(1))
				.isUserValueIncorrect();
	}

	@Test
	public void isSolved_SolvedGridIsChecked_True() throws Exception {
		mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is solved", grid.isSolved(), is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).isUserValueIncorrect();
	}

	@Test
	public void setSolved_SolvedGrid_GridStatisticsUpdated() throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.setSolved();

		verify(mGridStatisticsMock).solved();
	}

	@Test
	public void setSolved_SolvedGrid_OnSolvedListenerCalled() throws Exception {
		Grid grid = mGridBuilderStub.build();
		Grid.OnSolvedListener gridOnSolvedListener = mock(Grid.OnSolvedListener.class);
		grid.setSolvedHandler(gridOnSolvedListener);

		// Solve it
		grid.setSolved();

		verify(gridOnSolvedListener).puzzleSolved();
	}

	@Test
	public void isSolutionValidSoFar_AllCellsEmpty_True() throws Exception {
		mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).isUserValueSet();
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsValid_True() throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(true);
		when(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(true));
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsInvalid_False()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(true);
		when(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(true);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
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

		mGridBuilderStub.setGridSize(2);
		mGridBuilderStub.setCellsInitializedWith(gridCellEmptyStub,
				gridCellWithValidUserValueStub,
				gridCellWithInvalidUserValueStub,
				gridCellWithValidUserValueStub);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(false));
	}

	@Test
	public void addMove_FirstMoveAddedToNullList_True() throws Exception {
		mGridBuilderStub.mCellChanges = null;
		Grid grid = mGridBuilderStub.build();
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreatorStub.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_FirstMoveAddedToEmptyList_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreatorStub.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_AddMultipleDifferentMoves_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.addMove(mCellChangeMock);
		// Add another mock as moves may not be identical for this test
		grid.addMove(mock(CellChange.class));

		assertThat("Number of cell changes",
				mGridObjectsCreatorStub.mArrayListOfCellChanges.size(), is(2));
	}

	@Test
	public void addMove_AddMultipleIdenticalMoves_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		// Add same mock twice as we need identical moves for this test
		grid.addMove(mCellChangeMock);
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreatorStub.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void countMoves_MovesListIsNull_ZeroMoves() throws Exception {
		mGridBuilderStub.mCellChanges = null;
		Grid grid = mGridBuilderStub.build();

		int actualNumberOfCellChanges = grid.countMoves();
		assertThat("Number of moves in a Grid with an null moves list",
				actualNumberOfCellChanges, is(0));
	}

	@Test
	public void countMoves_MovesListIsNotEmpty_MovesCountedCorrectly()
			throws Exception {
		mGridBuilderStub.setCellChangesInitializedWith(mock(CellChange.class),
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class));
		Grid grid = mGridBuilderStub.build();

		assertThat("Number of moves in a Grid with a non-empty moves list",
				grid.countMoves(), is(mGridBuilderStub.mCellChanges.size()));
	}

	@Test
	public void countMoves_MovesListIsEmpty_ZeroMoves() throws Exception {
		Grid grid = mGridBuilderStub.build();
		int actualNumberOfCellChanges = grid.countMoves();
		assertThat("Number of moves in a Grid with an empty moves list",
				actualNumberOfCellChanges, is(0));
	}

	@Test
	public void undoLastMove_NullMovesList_False() throws Exception {
		mGridBuilderStub.setCellChanges(null);
		Grid grid = mGridBuilderStub.build();

		assertThat("Undo last move", grid.undoLastMove(), is(false));
	}

	@Test
	public void undoLastMove_EmptyMovesList_False() throws Exception {
		Grid grid = mGridBuilderStub.build();
		assertThat("Undo last move", grid.undoLastMove(), is(false));
	}

	@Test
	public void undoLastMove_RestoreCellAndCellChangeAreEmptyOrContainMaybes_MoveIsRestored()
			throws Exception {
		// This test simulates following situations:
		// 1) Before undo the cell is empty. After undo the cell is filled with
		// one or more maybe values.
		// 2) Before undo the cell contains one or more maybe values. After undo
		// the cell contains another set of maybe values (including 0 maybe
		// values).
		// In both cases the user value equals 0 before and after the actual
		// undo, indicating that the cell does not contain a user value.
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getUserValue())
				.thenReturn( //
						0 /*
						 * value before actual undo
						 */,
						//
						0 /*
						 * value after actual undo
						 */);
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);
		when(mCellChangeMock.getGridCell()).thenReturn(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		mGridBuilderStub.setCellChangesInitializedWith(mCellChangeMock);
		int numberOfMovesBeforeUndo = mGridBuilderStub.mCellChanges.size();
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo));

		grid.undoLastMove();

		verify(mCellChangeMock).restore();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo - 1));
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_UNDO_MOVE);
		assertThat(
				"Selected cell",
				grid.getSelectedCell(),
				is(sameInstance(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup)));
	}

	@Test
	public void undoLastMove_RestoreCellOrCellChangeAreEmptyOrContainUserValue_MoveIsRestored()
			throws Exception {
		// This test simulates following:
		// 1) Before undo the cell is empty. After undo the cell is filled with
		// a user value.
		// 2) Before undo the cell is filled with a user value. After undo the
		// cell is filled with another user value.
		// 3) Before undo the cell is filled with a user value. After undo the
		// cell does not contain a user value; the cell can contain a maybe
		// value.
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getUserValue())
				.thenReturn( //
						0 /* value before actual undo */,
						//
						1 /* value after actual undo */);
		// This unit test checks whether UndoLastMove correctly calls methods
		// markDuplicateValuesInSameRowAndColumn and checkUserMath. Both those
		// methods are called when instantiating the new grid via the
		// GridBuilder.build(). To isolate the effects caused by UndoLastMove,
		// some additional mocks are used which are not related to the mocks
		// used by the default grid stub:
		// The cage of the cell being restored refers to an entire new grid cage
		// mock. For this unit test it is not relevant that the grid cell of the
		// cell change refers to another mock.
		GridCage gridCageMockOfGridCellInCellChange = mock(GridCage.class);
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(gridCageMockOfGridCellInCellChange);
		when(mCellChangeMock.getGridCell()).thenReturn(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		mGridBuilderStub.setCellChangesInitializedWith(mCellChangeMock);
		// The selector GridCellSelectorInSameRowOrColumn returns an array
		// list of new grid cell mocks. For this unit test it is not relevant
		// that those mocks are not related to "real" cells of the default grid
		// stub.
		GridCell gridCellInSameRowOrColumn = mock(GridCell.class);
		ArrayList<GridCell> arrayListOfGridCells = new ArrayList<GridCell>();
		arrayListOfGridCells.add(gridCellInSameRowOrColumn);
		arrayListOfGridCells.add(gridCellInSameRowOrColumn);
		arrayListOfGridCells.add(gridCellInSameRowOrColumn);
		arrayListOfGridCells.add(gridCellInSameRowOrColumn);
		when(mGridCellSelectorInRowOrColumn.find()).thenReturn(
				arrayListOfGridCells);
		// Check if setup is correct
		int numberOfMovesBeforeUndo = mGridBuilderStub.mCellChanges.size();
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo));

		grid.undoLastMove();

		verify(mCellChangeMock).restore();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo - 1));
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_UNDO_MOVE);
		assertThat(
				"Selected cell",
				grid.getSelectedCell(),
				is(sameInstance(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup)));
		verify(gridCellInSameRowOrColumn, times(arrayListOfGridCells.size()))
				.markDuplicateValuesInSameRowAndColumn();
		verify(gridCageMockOfGridCellInCellChange).checkUserMath();
	}

	@Test
	public void deselectSelectedCell_CellIsSelected_CellIsDeselected()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();
		grid.setSelectedCell(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mGridCageOfDefaultSetup).setBorders();

		// Deselect this cell
		grid.deselectSelectedCell();

		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup).deselect();
		verify(mGridBuilderStub.mGridCageOfDefaultSetup, times(2)).setBorders();

		assertThat("Selected cell", grid.getSelectedCell(), is(nullValue()));
	}

	@Test
	public void setSelectedCell_SelectNullGridCell_Null() throws Exception {
		Grid grid = mGridBuilderStub.build();
		assertThat("Selected cell", grid.setSelectedCell((GridCell) null),
				is(nullValue()));
	}

	@Test
	public void setSelectedCell_NoCellCurrentlySelectedInGrid_BordersOfNewCageSetAndSelectedCellReturned()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();

		assertThat(
				"Selected cell",
				grid
						.setSelectedCell(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup),
				is(sameInstance(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup)));

		verify(mGridBuilderStub.mGridCageOfDefaultSetup).setBorders();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_OldSelectedCellIsDeselected()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);
		GridCell otherGridCellMock = mock(GridCell.class);
		when(otherGridCellMock.getCage()).thenReturn(
				mGridBuilderStub.mGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();

		// Select the cells in given order
		grid.setSelectedCell(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		grid.setSelectedCell(otherGridCellMock);

		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup).deselect();
	}

	@Test
	public void setSelectedCell_CurrentlySelectedCellInGridIsSelectedAgain_NoBordersReset()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();

		// Select the grid cell
		grid.setSelectedCell(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mGridCageOfDefaultSetup, times(1)).setBorders();

		// Select the same cell one more. The borders may not be reset again.
		grid.setSelectedCell(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mGridCageOfDefaultSetup, times(1)).setBorders();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_NoBordersReset()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);

		GridCell otherGridCellMock = mock(GridCell.class);
		when(otherGridCellMock.getCage()).thenReturn(
				mGridBuilderStub.mGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();

		// Select grid cell stub 1
		grid.setSelectedCell(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mGridCageOfDefaultSetup, times(1)).setBorders();

		// Select the other cell in the same cage. The borders may not be reset
		// again.
		grid.setSelectedCell(otherGridCellMock);
		verify(mGridBuilderStub.mGridCageOfDefaultSetup, times(1)).setBorders();
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
		Grid grid = mGridBuilderStub.build();

		// Select grid cell stub 1
		grid.setSelectedCell(gridCellStub1);
		verify(gridCageMock1, times(1)).setBorders();

		// Select the other cell in the same cage. The borders of cage 1 and
		// cage 2 need both to be set.
		grid.setSelectedCell(gridCellStub2);
		verify(gridCageMock1, times(2)).setBorders();
		verify(gridCageMock2, times(1)).setBorders();
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCellsListIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCells(null);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCellsListIsEmpty_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCells(new ArrayList<GridCell>());
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCellsHasTooLittleCells_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setGridSize(2);
		mGridBuilderStub.setCellsInitializedWith(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCellsHasTooManyCells_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setGridSize(1);
		mGridBuilderStub.setCellsInitializedWith(
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyGridCellMockOfDefaultSetup);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCagesListIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCages(null);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCagesListIsEmpty_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCages(new ArrayList<GridCage>());
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridGeneratingParametersIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setGridGeneratingParameters(null);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test
	public void gridCreateWithGridBuilder_ValidParameters_GridBuilderParamsUsed()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllGridCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		long dateCreated = System.currentTimeMillis();
		mGridBuilderStub.setDateCreated(dateCreated);
		mGridBuilderStub.setActive(false);
		Grid grid = mGridBuilderStub.build();
		assertThat("Selected grid cell", grid.getSelectedCell(),
				is(nullValue()));
		assertThat("Solution revealed", grid.isSolutionRevealed(), is(false));
		assertThat("Solving attempt id", grid.getSolvingAttemptId(),
				is(equalTo(-1)));
		assertThat("Row id", grid.getRowId(), is(equalTo(-1)));
		assertThat("Grid statistics", grid.getGridStatistics(),
				is(sameInstance(mGridStatisticsMock)));
		assertThat("Date created filled with system time",
				grid.getDateCreated(), is(dateCreated));
		assertThat("Date updated filled with system time", grid.getDateSaved(),
				is(dateCreated));
		assertThat("Grid size", grid.getGridSize(),
				is(mGridBuilderStub.mGridSize));
		assertThat("Cells", grid.mCells,
				is(sameInstance(mGridBuilderStub.mCells)));
		assertThat("Cells", grid.mCages,
				is(sameInstance(mGridBuilderStub.mCages)));
		assertThat("Is active", grid.isActive(), is(mGridBuilderStub.mActive));
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).setGridReference(
				any(Grid.class));
		verify(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).setBorders();
	}

	private void setupForSaveTest() {
		// Instantiate singleton class Util
		new Util(new Activity());

		// Explicitly set the grid generating parameters and cage methods which
		// are used when getting the grid definition to avoid a null pointer
		// exceptions.
		mGridGeneratingParametersMock.mPuzzleComplexity = PuzzleComplexity.NORMAL;
		mGridGeneratingParametersMock.mHideOperators = false;
		mGridBuilderStub.useSameMockForAllGridCages();
		when(mGridBuilderStub.mGridCageOfDefaultSetup.getOperator())
				.thenReturn(CageOperator.ADD);

		// Initialize the mock as was it a real GridStatistics object which was
		// not yet saved.
		mGridStatisticsMock.mId = -1;
	}

	@Test
	public void save_InsertGridWithUniqueGridDefinition_Saved()
			throws Exception {
		setupForSaveTest();
		// Prepare for not finding an existing grid row with the same definition
		when(mGridDatabaseAdapterMock.getByGridDefinition(anyString()))
				.thenReturn(null);
		// Prepare for insert of new grid row
		int mGridRowIdAfterSuccessfulInsert = 34;
		when(mGridDatabaseAdapterMock.insert(any(Grid.class))).thenReturn(
				mGridRowIdAfterSuccessfulInsert);
		// Prepare insert of new solving attempt
		int mSolvingAttemptIdAfterSuccessfulInsert = 51;
		when(
				mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class),
						anyInt())).thenReturn(
				mSolvingAttemptIdAfterSuccessfulInsert);
		// Prepare insert of new statistics
		GridStatistics mGridStatisticsAfterSuccessfulInsert = mock(GridStatistics.class);
		mGridStatisticsAfterSuccessfulInsert.mId = 52;
		when(mStatisticsDatabaseAdapterMock.insert(any(Grid.class)))
				.thenReturn(mGridStatisticsAfterSuccessfulInsert);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(-1));
		assertThat(grid.getSolvingAttemptId(), is(-1));
		assertThat(grid.getGridStatistics().mId, is(-1));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(any(Grid.class),
				anyInt());
		verify(mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId() >= 0, is(true));
		assertThat(grid.getSolvingAttemptId() >= 0, is(true));
		assertThat(grid.getGridStatistics().mId >= 0, is(true));
		assertThat(saveResult, is(true));
	}

	@Test
	public void save_InsertGridWithUniqueGridDefinitionButInsertGridFails_NotSaved()
			throws Exception {
		setupForSaveTest();
		// Prepare for not finding an existing grid row with the same definition
		when(mGridDatabaseAdapterMock.getByGridDefinition(anyString()))
				.thenReturn(null);
		// Prepare for failing insert of new grid row
		int mGridRowIdWhenInsertFails = -1;
		when(mGridDatabaseAdapterMock.insert(any(Grid.class))).thenReturn(
				mGridRowIdWhenInsertFails);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(-1));
		assertThat(grid.getSolvingAttemptId(), is(-1));
		assertThat(grid.getGridStatistics().mId, is(-1));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId(), is(-1));
		assertThat(grid.getSolvingAttemptId(), is(-1));
		assertThat(grid.getGridStatistics().mId, is(-1));
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_InsertGridWithExistingGridDefinition_Saved()
			throws Exception {
		setupForSaveTest();
		// Prepare for finding existing grid row when inserting a new grid
		GridRow gridRow = mock(GridRow.class);
		gridRow.mId = 25;
		when(mGridDatabaseAdapterMock.getByGridDefinition(anyString()))
				.thenReturn(gridRow);
		// Prepare insert of new solving attempt
		int mSolvingAttemptIdAfterSuccessfulInsert = 51;
		when(
				mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class),
						anyInt())).thenReturn(
				mSolvingAttemptIdAfterSuccessfulInsert);
		// Prepare insert of new statistics
		GridStatistics mGridStatisticsAfterSuccessfulInsert = mock(GridStatistics.class);
		mGridStatisticsAfterSuccessfulInsert.mId = 52;
		when(mStatisticsDatabaseAdapterMock.insert(any(Grid.class)))
				.thenReturn(mGridStatisticsAfterSuccessfulInsert);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(-1));
		assertThat(grid.getSolvingAttemptId(), is(-1));
		assertThat(grid.getGridStatistics().mId, is(-1));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(any(Grid.class),
				anyInt());
		verify(mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId() >= 0, is(true));
		assertThat(grid.getSolvingAttemptId() >= 0, is(true));
		assertThat(grid.getGridStatistics().mId >= 0, is(true));
		assertThat(saveResult, is(true));
	}

	@Test
	public void save_SavedGridButMissingSolvingAttemptAndStatistics_Saved()
			throws Exception {
		setupForSaveTest();
		// Grid was already saved
		int existingGridId = 12;
		mGridBuilderStub.setGridId(existingGridId);
		// Prepare insert of new solving attempt
		int mSolvingAttemptIdAfterSuccessfulInsert = 51;
		when(
				mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class),
						anyInt())).thenReturn(
				mSolvingAttemptIdAfterSuccessfulInsert);
		// Prepare insert of new statistics
		GridStatistics mGridStatisticsAfterSuccessfulInsert = mock(GridStatistics.class);
		mGridStatisticsAfterSuccessfulInsert.mId = 52;
		when(mStatisticsDatabaseAdapterMock.insert(any(Grid.class)))
				.thenReturn(mGridStatisticsAfterSuccessfulInsert);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(-1));
		assertThat(grid.getGridStatistics().mId, is(-1));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(any(Grid.class),
				anyInt());
		verify(mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId() >= 0, is(true));
		assertThat(grid.getGridStatistics().mId >= 0, is(true));
		assertThat(saveResult, is(true));
	}

	@Test
	public void save_SavedGridButInsertSolvingAttemptFails_NotSaved()
			throws Exception {
		setupForSaveTest();
		// Grid was already saved
		int existingGridId = 12;
		mGridBuilderStub.setGridId(existingGridId);
		// Prepare for failing insert of new solving attempt
		int mSolvingAttemptIdWhenInsertFails = -1;
		when(
				mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class),
						anyInt())).thenReturn(mSolvingAttemptIdWhenInsertFails);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(-1));
		assertThat(grid.getGridStatistics().mId, is(-1));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(any(Grid.class),
				anyInt());
		verify(mStatisticsDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(-1));
		assertThat(grid.getGridStatistics().mId, is(-1));
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptUpdateSolvingAttemptAndInsertStatistics_Saved()
			throws Exception {
		setupForSaveTest();
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		int existingSolvingAttemptId = 15;
		mGridBuilderStub.setSolvingAttemptId(existingGridId,
				existingSolvingAttemptId);
		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);
		// Prepare insert of new statistics
		GridStatistics mGridStatisticsAfterSuccessfulInsert = mock(GridStatistics.class);
		mGridStatisticsAfterSuccessfulInsert.mId = 52;
		when(mStatisticsDatabaseAdapterMock.insert(any(Grid.class)))
				.thenReturn(mGridStatisticsAfterSuccessfulInsert);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId, is(-1));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(Grid.class), anyInt());
		verify(mSolvingAttemptDatabaseAdapterMock).update(anyInt(),
				any(Grid.class));
		verify(mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId >= 0, is(true));
		assertThat(saveResult, is(true));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptButUpdateSolvingAttemptFails_NotSaved()
			throws Exception {
		setupForSaveTest();
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		int existingSolvingAttemptId = 15;
		mGridBuilderStub.setSolvingAttemptId(existingGridId,
				existingSolvingAttemptId);
		// Prepare failing update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(false);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId, is(-1));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(Grid.class), anyInt());
		verify(mSolvingAttemptDatabaseAdapterMock).update(anyInt(),
				any(Grid.class));
		verify(mStatisticsDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId, is(-1));
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptButInsertStatisticsFails_NotSaved()
			throws Exception {
		setupForSaveTest();
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		int existingSolvingAttemptId = 15;
		mGridBuilderStub.setSolvingAttemptId(existingGridId,
				existingSolvingAttemptId);
		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);
		// Prepare failing insert of new statistics
		when(mStatisticsDatabaseAdapterMock.insert(any(Grid.class)))
				.thenReturn(null);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId, is(-1));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(Grid.class), anyInt());
		verify(mSolvingAttemptDatabaseAdapterMock).update(anyInt(),
				any(Grid.class));
		verify(mStatisticsDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptAndStatisticsUpdateStatisticsFails_NotSaved()
			throws Exception {
		setupForSaveTest();
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		int existingSolvingAttemptId = 15;
		mGridBuilderStub.setSolvingAttemptId(existingGridId,
				existingSolvingAttemptId);
		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);
		// Prepare failing update of statistics
		int existingStatisticsId = 16;
		mGridStatisticsMock.mId = existingStatisticsId;
		when(mGridStatisticsMock.save()).thenReturn(false);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId, is(existingStatisticsId));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(Grid.class), anyInt());
		verify(mSolvingAttemptDatabaseAdapterMock).update(anyInt(),
				any(Grid.class));
		verify(mStatisticsDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mGridStatisticsMock).save();
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId, is(existingStatisticsId));
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptAndStatisticsUpdateAfterReplayIsFinished_Saved()
			throws Exception {
		setupForSaveTest();
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		int existingSolvingAttemptId = 15;
		mGridBuilderStub.setSolvingAttemptId(existingGridId,
				existingSolvingAttemptId);
		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(true);
		// Prepare successful update of statistics
		int existingStatisticsId = 16;
		mGridStatisticsMock.mId = existingStatisticsId;
		when(mGridStatisticsMock.save()).thenReturn(true);
		// Prepare that this is a finished replay of a grid which is not yet
		// included in the (overall) statistics
		mGridBuilderStub.setActive(false);
		when(mGridStatisticsMock.getReplayCount()).thenReturn(1);
		when(mGridStatisticsMock.isIncludedInStatistics()).thenReturn(false);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilderStub.build();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId, is(existingStatisticsId));

		boolean saveResult = grid.save();

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(Grid.class), anyInt());
		verify(mSolvingAttemptDatabaseAdapterMock).update(anyInt(),
				any(Grid.class));
		verify(mStatisticsDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mGridStatisticsMock).save();
		verify(mStatisticsDatabaseAdapterMock)
				.updateSolvingAttemptToBeIncludedInStatistics(anyInt(),
						anyInt());
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(grid.getRowId(), is(existingGridId));
		assertThat(grid.getSolvingAttemptId(), is(existingSolvingAttemptId));
		assertThat(grid.getGridStatistics().mId, is(existingStatisticsId));
		assertThat(saveResult, is(true));
	}

	@Test
	public void saveOnUpgrade_SaveSolvingAttemptFails_GridNotSaved()
			throws Exception {
		when(
				mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is saved?", grid.saveOnAppUpgrade(), is(false));
	}

	private void assertThatSaveOnUpgrade(boolean resultSaveGridStatistics,
			Matcher expectedResultSaveOnUpgrade) {
		when(
				mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(true);

		when(mGridStatisticsMock.save()).thenReturn(resultSaveGridStatistics);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is saved?", grid.saveOnAppUpgrade(),
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
	public void containsNoUserValues_GridHasNoUserValuesAndNoMaybeValues_True()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.containsNoUserValues(), is(true));
	}

	@Test
	public void containsNoUserValues_GridHasOneUserValueButNoMaybeValues_False()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(true);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.containsNoUserValues(), is(false));
	}

	@Test
	public void containsNoUserValues_GridHasOneMaybeValueButNoUserValues_True()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.countPossibles())
				.thenReturn(1);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.containsNoUserValues(), is(true));
	}

	@Test
	public void isEmpty_GridHasNoUserValuesAndNoMaybeValues_True()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.isEmpty(), is(true));
	}

	@Test
	public void isEmpty_GridHasOneUserValueButNoMaybeValues_False()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(true);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.isEmpty(), is(false));
	}

	@Test
	public void isEmpty_GridHasOneMaybeValueButNoUserValues_False()
			throws Exception {
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.countPossibles())
				.thenReturn(1);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.isEmpty(), is(false));
	}

	@Test
	public void replay() throws Exception {

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

	@Test
	public void checkUserMathForAllCages() throws Exception {

	}

	@Test
	public void revealSelectedCell() throws Exception {

	}

	@Test
	public void revealOperatorSelectedCage_CageSelectedForCageWithHiddenOperator_OperatorRevealed() throws Exception {
		// Set a cage as selected by selecting a cell before building the grid.
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.isSelected())
				.thenReturn(true);
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);
		when(mGridBuilderStub.mGridCageOfDefaultSetup.isOperatorHidden()).thenReturn(true);
		Grid grid = mGridBuilderStub.build();
		// Remainder of setup after the grid is build.
		int idOfUpperLeftCellOfSelectedCage = 13;
		when(mGridBuilderStub.mGridCageOfDefaultSetup.getIdUpperLeftCell())
				.thenReturn(idOfUpperLeftCellOfSelectedCage);
		String newCageText = "** SOME NEW CAGE TEXT **";
		when(mGridBuilderStub.mGridCageOfDefaultSetup.getCageText())
				.thenReturn(newCageText);

		assertThat(grid.revealOperatorSelectedCage(), is(true));
		verify(mGridBuilderStub.mGridCageOfDefaultSetup).revealOperator();
		verify(
				mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfUpperLeftCellOfSelectedCage])
				.setCageText(newCageText);
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_REVEAL_OPERATOR);
	}

	@Test
	public void revealOperatorSelectedCage_CageSelectedForCageWithVisibleOperator_OperatorRevealed() throws Exception {
		// Set a cage as selected by selecting a cell before building the grid.
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.isSelected())
				.thenReturn(true);
		when(mGridBuilderStub.mAnyGridCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mGridCageOfDefaultSetup);
		when(mGridBuilderStub.mGridCageOfDefaultSetup.isOperatorHidden()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.revealOperatorSelectedCage(), is(false));
		verify(mGridBuilderStub.mGridCageOfDefaultSetup, never()).revealOperator();
	}

	@Test
	public void revealOperatorSelectedCage_NoCageSelectedForCageWithVisibleOperator_OperatorRevealed() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.revealOperatorSelectedCage(), is(false));
		verify(mGridBuilderStub.mGridCageOfDefaultSetup, never()).revealOperator();
	}

	@Test
	public void getUserValuesForCells_CellsIsNull_Null() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getUserValuesForCells(null), is(nullValue()));
	}

	@Test
	public void getUserValuesForCells_CellWithInvalidId_Null() throws Exception {
		int idOfCell = -1; // Value should not be in range 0 to gridsize * gridsize
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getUserValuesForCells(new int[] {idOfCell}), is(nullValue()));
	}

	@Test
	public void getUserValuesForCells_GetValueForOneCellHavingAUserValue_UserValueReturned() throws Exception {
		int idOfCell = 4;
		int valueOfCell = 3;
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell].isUserValueSet()).thenReturn(true);
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell].getUserValue()).thenReturn(valueOfCell);
		Grid grid = mGridBuilderStub.build();

		ArrayList<Integer> expectedUserValues = new ArrayList<Integer>();
		expectedUserValues.add(valueOfCell);
		assertThat(grid.getUserValuesForCells(new int[] {idOfCell}), is(expectedUserValues));
	}

	@Test
	public void getUserValuesForCells_GetValueForOneCellNotHavingAUserValue_NoUserValuesReturned() throws Exception {
		int idOfCell = 4;
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell].isUserValueSet()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		ArrayList<Integer> expectedUserValues = new ArrayList<Integer>();
		assertThat(grid.getUserValuesForCells(new int[] {idOfCell}), is(expectedUserValues));
	}

	@Test
	public void getUserValuesForCells_GetValueForMultipleCellsAllHavingAUserValue_UserValuesReturned() throws Exception {
		int idOfCell_1 = 4;
		int valueOfCell_1 = 3;
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_1].isUserValueSet()).thenReturn(true);
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_1].getUserValue()).thenReturn(valueOfCell_1);
		int idOfCell_2 = 8;
		int valueOfCell_2 = 2;
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_2].isUserValueSet()).thenReturn(true);
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_2].getUserValue()).thenReturn(valueOfCell_2);
		Grid grid = mGridBuilderStub.build();

		ArrayList<Integer> expectedUserValues = new ArrayList<Integer>();
		expectedUserValues.add(valueOfCell_1);
		expectedUserValues.add(valueOfCell_2);
		assertThat(grid.getUserValuesForCells(new int[] {idOfCell_1, idOfCell_2}), is(expectedUserValues));
	}

	@Test
	public void getUserValuesForCells_GetValueForMultipleCellsNotAllHavingAUserValue_UserValuesReturned() throws Exception {
		int idOfCell_1 = 4;
		int valueOfCell_1 = 3;
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_1].isUserValueSet()).thenReturn(true);
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_1].getUserValue()).thenReturn(valueOfCell_1);
		int idOfCell_2 = 8;
		int valueOfCell_2 = 2;
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_2].isUserValueSet()).thenReturn(true);
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_2].getUserValue()).thenReturn(valueOfCell_2);
		int idOfCell_3 = 9;
		when(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_3].isUserValueSet()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		ArrayList<Integer> expectedUserValues = new ArrayList<Integer>();
		expectedUserValues.add(valueOfCell_1);
		expectedUserValues.add(valueOfCell_2);
		assertThat(grid.getUserValuesForCells(new int[] {idOfCell_1, idOfCell_2, idOfCell_3}), is(expectedUserValues));
	}

	@Test
	public void setBorderForCells_CellsIsNull_NoBorderAreSet() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.setBorderForCells(null), is(false));
	}

	@Test
	public void setBorderForCells_CellWithInvalidId_BorderAreSet() throws Exception {
		int idOfCell = -1; // Value should not be in range 0 to gridsize * gridsize
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.setBorderForCells(new int[] {idOfCell}), is(false));
	}

	@Test
	public void setBorderForCells_SingleCell_BorderAreSet() throws Exception {
		int idOfCell = 4;
		Grid grid = mGridBuilderStub.build();
		verify(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell]).setBorders();

		assertThat(grid.setBorderForCells(new int[] {idOfCell}), is(true));
		verify(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell], times(2)).setBorders();
	}

	@Test
	public void setBorderForCells_MultipleCells_BorderAreSet() throws Exception {
		int idOfCell_1 = 4;
		int idOfCell_2 = 8;
		Grid grid = mGridBuilderStub.build();
		verify(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_1]).setBorders();
		verify(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_2]).setBorders();

		assertThat(grid.setBorderForCells(new int[] {idOfCell_1, idOfCell_2}), is(true));
		verify(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_1], times(2)).setBorders();
		verify(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_2], times(2)).setBorders();
	}

	@Test
	public void getGridCells_CellsIsNull_NullReturned() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getGridCells(null), is(nullValue()));
	}

	@Test
	public void getGridCells_InvalidCellId_CellReturned() throws Exception {
		int idOfCell = -1; // Value should not be in range 0 to gridsize * gridsize
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getGridCells(new int[] {idOfCell}), is(nullValue()));
	}

	@Test
	public void getGridCells_GetSingleCell_CellReturned() throws Exception {
		int idOfCell = 4;
		Grid grid = mGridBuilderStub.build();

		ArrayList<GridCell> expectedGridCells = new ArrayList<GridCell>();
		expectedGridCells.add(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell]);
		assertThat(grid.getGridCells(new int[] {idOfCell}), is(expectedGridCells));
	}

	@Test
	public void getGridCells_GetMultipleCells_CellsReturned() throws Exception {
		int idOfCell_1 = 4;
		int idOfCell_2 = 8;
		Grid grid = mGridBuilderStub.build();

		ArrayList<GridCell> expectedGridCells = new ArrayList<GridCell>();
		expectedGridCells.add(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_1]);
		expectedGridCells.add(mGridBuilderStub.mGridCellMockOfDefaultSetup[idOfCell_2]);
		assertThat(grid.getGridCells(new int[] {idOfCell_1, idOfCell_2}), is(expectedGridCells));
	}
}
