package net.mathdoku.plus.grid;

import android.app.Activity;
import android.content.Context;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import testHelper.TestGridHiddenOperators;
import testHelper.TestGridVisibleOperators;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridTest {
	private Activity mActivity;
	private Preferences mPreferencesMock;
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
		private boolean mUseSameMockForAllCells = false;
		private boolean mUseSameMockForAllGridCages = false;

		/*
		 * Variables below refer to the last created grid cell mock and the grid
		 * cage mock. In case the same mock has to be used for all cells, the
		 * variables below are use for each such mock.
		 */
		public Cell mAnyCellMockOfDefaultSetup = null;
		public Cell mCellMockOfDefaultSetup[] = null;
		public GridCage mAnyGridCageOfDefaultSetup = null;

		public GridGeneratingParameters mGridGeneratingParametersDefaultSetup;

		public GridBuilderStub useSameMockForAllCells() {
			mUseSameMockForAllCells = true;

			return this;
		}

		public GridBuilderStub useSameMockForAllGridCages() {
			mUseSameMockForAllGridCages = true;

			return this;
		}

		public GridBuilderStub setupDefaultWhichDoesNotThrowErrorsOnBuild() {
			int gridSize = 4;
			setGridSize(gridSize);

			// Insert exact number of cells needed with this grid size. A
			// reference to the last created grid cell mock is kept for tests
			// which need just a cell in the default grid.
			int numberOfCells = gridSize * gridSize;
			List<Cell> cells = mGridObjectsCreator
					.createArrayListOfCells();
			mCellMockOfDefaultSetup = new Cell[numberOfCells];
			for (int i = 0; i < numberOfCells; i++) {
				if (mUseSameMockForAllCells == false || i == 0) {
					mCellMockOfDefaultSetup[i] = mock(Cell.class);
				} else {
					mCellMockOfDefaultSetup[i] = mCellMockOfDefaultSetup[0];
				}
				cells.add(mCellMockOfDefaultSetup[i]);
			}
			super.setCells(cells);
			mAnyCellMockOfDefaultSetup = mCellMockOfDefaultSetup[15];
			// mAnyCellMockOfDefaultSetup = mCellMockDefaultSetup[5];

			// Insert an arbitrary number of cages (at least 1). A
			// reference to the last created grid cage mock is kept for tests
			// which need just a cage in the default grid.
			int numberOfCages = 3;
			List<GridCage> gridCages = mGridObjectsCreator
					.createArrayListOfGridCages();
			for (int i = 0; i < numberOfCages; i++) {
				if (mUseSameMockForAllGridCages == false
						|| mAnyGridCageOfDefaultSetup == null || i == 0) {
					mAnyGridCageOfDefaultSetup = mock(GridCage.class);
				}
				gridCages.add(mAnyGridCageOfDefaultSetup);
			}
			super.setCages(gridCages);

			super.setGridStatistics(mGridObjectsCreator.createGridStatistics());

			mGridGeneratingParametersDefaultSetup = mGridObjectsCreator
					.createGridGeneratingParameters();
			super
					.setGridGeneratingParameters(mGridGeneratingParametersDefaultSetup);

			return this;
		}

		/**
		 * Initializes the list of cells of the GridBuilder with the given grid
		 * cells.
		 */
		public GridBuilderStub setCellsInitializedWith(Cell... cell) {
			// Default setup is no longer valid as the list of cells is
			// replaced.
			mAnyCellMockOfDefaultSetup = null;

			List<Cell> cells = new ArrayList<Cell>();

			for (int i = 0; i < cell.length; i++) {
				cells.add(cell[i]);
			}
			super.setCells(cells);

			return this;
		}

		/**
		 * Initializes the list of cages of the GridBuilder with the given grid
		 * cages.
		 */
		public GridBuilderStub setCagesInitializedWith(GridCage... gridCage) {
			List<GridCage> gridCages = new ArrayList<GridCage>();

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
			List<CellChange> cellChanges = new ArrayList<CellChange>();

			for (int i = 0; i < cellChange.length; i++) {
				cellChanges.add(cellChange[i]);
			}
			super.setCellChanges(cellChanges);

			return this;
		}

		public GridBuilderStub setCellMockAsSelectedCell(int idSelectedCell) {
			// Set the selected cell before building the grid
			when(mCellMockOfDefaultSetup[idSelectedCell].isSelected())
					.thenReturn(true);
			when(mCellMockOfDefaultSetup[idSelectedCell].getCage())
					.thenReturn(mAnyGridCageOfDefaultSetup);

			return this;
		}

		private GridBuilderStub setCellMockWithUserValue(int id, int row, int column,
														 int userValue) {
			// Define row and column of the selected cell
			when(mCellMockOfDefaultSetup[id].getRow()).thenReturn(row);
			when(mCellMockOfDefaultSetup[id].getColumn())
					.thenReturn(column);
			// Set user value
			when(mCellMockOfDefaultSetup[id].getUserValue()).thenReturn(
					userValue);

			return this;
		}

		private GridBuilderStub setCellMockWithMaybeValues(int id, int row, int column,
														   int maybeValues[]) {
			when(mCellMockOfDefaultSetup[id].getRow()).thenReturn(row);
			when(mCellMockOfDefaultSetup[id].getColumn())
					.thenReturn(column);
			for (int maybeValue : maybeValues) {
				when(mCellMockOfDefaultSetup[id].hasPossible(maybeValue))
						.thenReturn(true);
			}

			return this;
		}
	}

	// Mocks used by the GridObjectsCreatorStub when creating new objects for
	// the Grid.
	private CellChange mCellChangeMock = mock(CellChange.class);
	private GridStatistics mGridStatisticsMock = mock(GridStatistics.class);
	private SolvingAttemptDatabaseAdapter mSolvingAttemptDatabaseAdapterMock = mock(SolvingAttemptDatabaseAdapter.class);
	private CellSelectorInRowOrColumn mCellSelectorInRowOrColumn = mock(CellSelectorInRowOrColumn.class);
	private GridSaver mGridSaverMock = mock(GridSaver.class);

	private class GridTestObjectsCreator extends Grid.ObjectsCreator {
		// Unreveal the array list of cell changes as it is hidden in the Grid
		// Object.
		public List<CellChange> mArrayListOfCellChanges = null;

		@Override
		public GridStatistics createGridStatistics() {
			return mGridStatisticsMock;
		}

		@Override
		public List<CellChange> createArrayListOfCellChanges() {
			mArrayListOfCellChanges = super.createArrayListOfCellChanges();
			return mArrayListOfCellChanges;
		}

		@Override
		public CellSelectorInRowOrColumn createCellSelectorInRowOrColumn(List<Cell> cells,
																		 int row, int column) {
			return mCellSelectorInRowOrColumn;
		}

		@Override
		public SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
			return mSolvingAttemptDatabaseAdapterMock;
		}

		@Override
		public GridSaver createGridSaver() {
			return mGridSaverMock;
		}
	}
	private GridTestObjectsCreator mGridTestObjectsCreator;

	@Before
	public void setUp() {
		mActivity = new Activity();

		Preferences.ObjectsCreator preferencesObjectsCreator = new Preferences.ObjectsCreator() {
			@Override
			public Preferences createPreferences(Context context) {
				return mock(Preferences.class);
			}
		};

		// Create the Preference Instance with the Singleton Creator which uses
		// a mocked Preferences object
		mPreferencesMock = Preferences.getInstance(mActivity,
				preferencesObjectsCreator);

		mGridBuilderStub = new GridBuilderStub();
		mGridTestObjectsCreator = new GridTestObjectsCreator();
		mGridBuilderStub.setGridObjectsCreator(mGridTestObjectsCreator);
		mGridBuilderStub.setupDefaultWhichDoesNotThrowErrorsOnBuild();
	}

	@Test
	public void getSelectedCage_SelectedCellIsNull_NullCage() {
		Grid grid = mGridBuilderStub.build();
		assertThat("Selected cage", grid.getSelectedCage(), is(nullValue()));
	}

	@Test
	public void getSelectedCage_SelectedCellIsNotNull_CageSelected() {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mAnyGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);

		assertThat("Selected cage", grid.getSelectedCage(),
				is(mGridBuilderStub.mAnyGridCageOfDefaultSetup));
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

		grid.clearCells();

		assertThat("Number of moves for grid after clear", grid.countMoves(),
				is(0));
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_AllCellsCleared()
			throws Exception {
		Grid grid = mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild()
				.build();

		grid.clearCells();

		// Note: currently a cell is always cleared even in case it does not
		// contain a user values nor any maybe values.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(grid.getCells().size())).clearValue();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_GridStatisticsUpdated()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getUserValue())
				.thenReturn(0, 1, 2, 0);
		Grid grid = mGridBuilderStub.build();

		grid.clearCells();

		// Clearing a second time won't change the statistics as no cells are
		// left to be cleared
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getUserValue())
				.thenReturn(0, 0, 0, 0);
		grid.clearCells();

		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_CLEAR_GRID);
	}

	@Test
	public void clearCells_AnyGrid_UserMathOfAllCagesIsChecked()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllGridCages()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		Grid grid = mGridBuilderStub.build();
		// Method checkUserMath is already call while instantiating the new
		// grid.
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).checkUserMath();

		grid.clearCells();

		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup,
				times(2 * mGridBuilderStub.mCages.size())).checkUserMath();
	}

	private void assertThatCellDoesNotExist(int gridSize, int row, int col) {
		mGridBuilderStub.setGridSize(gridSize);
		Grid grid = mGridBuilderStub.build();

		assertThat("Cell found for row and column",
				grid.getCellAt(row, col), is(nullValue()));
	}

	@Test
	public void getCellAt_NegativeRowNumber_CellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = -1;
		int col = 1;

		assertThatCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_NegativeColNumber_CellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = 1;
		int col = -1;

		assertThatCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_RowNumberGreaterOrEqualsToGridSize_CellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = gridSize;
		int col = 1;

		assertThatCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_ColNumberGreaterOrEqualsToGridSize_CellDoesNotExist()
			throws Exception {
		int gridSize = 4;
		int row = 1;
		int col = gridSize;

		assertThatCellDoesNotExist(gridSize, row, col);
	}

	@Test
	public void getCellAt_ValidRowAndColNumber_NotNull() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(
				"Cell retrieved from grid",
				grid.getCellAt(mGridBuilderStub.mGridSize - 1,
						mGridBuilderStub.mGridSize - 1),
				is(sameInstance(mGridBuilderStub.mAnyCellMockOfDefaultSetup)));
	}

	@Test
	public void revealSolution_NonEmptyCellListWith2IncorrectCells_TwoCellsRevealed()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(false, true, false,
				true, false);
		Grid grid = mGridBuilderStub.build();

		grid.revealSolution();

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).isUserValueIncorrect();

		// Check whether the correct number of cells has been revealed.
		int expectedNumberOfCellsRevealed = 2; // Number of cells with value
												// false
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(expectedNumberOfCellsRevealed)).setRevealed();
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(expectedNumberOfCellsRevealed)).setUserValue(anyInt());
	}

	@Test
	public void revealSolution_GridWithHiddenOperators_OperatorsRevealed()
			throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.useSameMockForAllGridCages()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		mGridBuilderStub.mGridGeneratingParametersDefaultSetup.mHideOperators = true;
		Grid grid = mGridBuilderStub.build();
		// During setup of default grid the method setCageText is already called
		// once for each cage.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).setCageText(anyString());

		grid.revealSolution();

		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).revealOperator();
		// Check if setCageText is called a second time for each cage
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(2 * mGridBuilderStub.mCages.size())).setCageText(
				anyString());

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
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(false, false, true,
				false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is solved", grid.isSolved(), is(false));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup, atLeast(1))
				.isUserValueIncorrect();
	}

	@Test
	public void isSolved_SolvedGridIsChecked_True() throws Exception {
		mGridBuilderStub
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is solved", grid.isSolved(), is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
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
				.useSameMockForAllCells()
				.setupDefaultWhichDoesNotThrowErrorsOnBuild();
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).isUserValueSet();
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsValid_True() throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(true);
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(true));
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsInvalid_False()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(true);
		when(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup
						.isUserValueIncorrect()).thenReturn(true);
		Grid grid = mGridBuilderStub.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(false));
	}

	@Test
	public void isSolutionValidSoFar_MultipleCellsIncludingAnInvalid_False()
			throws Exception {
		// Create stub for an empty cell
		Cell cellEmptyStub = mock(Cell.class);
		when(cellEmptyStub.isUserValueSet()).thenReturn(false);

		// Create stub for a cell with a correct value
		Cell cellWithValidUserValueStub = mock(Cell.class);
		when(cellWithValidUserValueStub.isUserValueSet()).thenReturn(true);
		when(cellWithValidUserValueStub.isUserValueIncorrect()).thenReturn(
				false);

		// Create stub for a cell with a incorrect value
		Cell cellWithInvalidUserValueStub = mock(Cell.class);
		when(cellWithInvalidUserValueStub.isUserValueSet())
				.thenReturn(true);
		when(cellWithInvalidUserValueStub.isUserValueIncorrect())
				.thenReturn(true);

		mGridBuilderStub.setGridSize(2);
		mGridBuilderStub.setCellsInitializedWith(cellEmptyStub, cellWithValidUserValueStub,
												 cellWithInvalidUserValueStub,
												 cellWithValidUserValueStub);
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
				mGridTestObjectsCreator.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_FirstMoveAddedToEmptyList_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridTestObjectsCreator.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_AddMultipleDifferentMoves_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		grid.addMove(mCellChangeMock);
		// Add another mock as moves may not be identical for this test
		grid.addMove(mock(CellChange.class));

		assertThat("Number of cell changes",
				mGridTestObjectsCreator.mArrayListOfCellChanges.size(), is(2));
	}

	@Test
	public void addMove_AddMultipleIdenticalMoves_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		// Add same mock twice as we need identical moves for this test
		grid.addMove(mCellChangeMock);
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridTestObjectsCreator.mArrayListOfCellChanges.size(), is(1));
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
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getUserValue())
				.thenReturn( //
						0 /*
						 * value before actual undo
						 */,
						//
						0 /*
						 * value after actual undo
						 */);
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mAnyGridCageOfDefaultSetup);
		when(mCellChangeMock.getCell()).thenReturn(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup);
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
				is(sameInstance(mGridBuilderStub.mAnyCellMockOfDefaultSetup)));
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
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getUserValue())
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
		GridCage gridCageMockOfAffectedCellInCellChange = mock(GridCage.class);
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCage())
				.thenReturn(gridCageMockOfAffectedCellInCellChange);
		when(mCellChangeMock.getCell()).thenReturn(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		mGridBuilderStub.setCellChangesInitializedWith(mCellChangeMock);
		// The selector CellSelectorInSameRowOrColumn returns an array
		// list of new grid cell mocks. For this unit test it is not relevant
		// that those mocks are not related to "real" cells of the default grid
		// stub.
		Cell cellInSameRowOrColumn = mock(Cell.class);
		List<Cell> arrayListOfCells = new ArrayList<Cell>();
		arrayListOfCells.add(cellInSameRowOrColumn);
		arrayListOfCells.add(cellInSameRowOrColumn);
		arrayListOfCells.add(cellInSameRowOrColumn);
		arrayListOfCells.add(cellInSameRowOrColumn);
		when(mCellSelectorInRowOrColumn.find()).thenReturn(arrayListOfCells);
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
				is(sameInstance(mGridBuilderStub.mAnyCellMockOfDefaultSetup)));
		verify(cellInSameRowOrColumn, times(arrayListOfCells.size()))
				.markDuplicateValuesInSameRowAndColumn();
		verify(gridCageMockOfAffectedCellInCellChange).checkUserMath();
	}

	@Test
	public void deselectSelectedCell_CellIsSelected_CellIsDeselected()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mAnyGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup).invalidateBordersOfAllCells();

		// Deselect this cell
		grid.deselectSelectedCell();

		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup).deselect();
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup, times(2))
				.invalidateBordersOfAllCells();

		assertThat("Selected cell", grid.getSelectedCell(), is(nullValue()));
	}

	@Test
	public void setSelectedCell_SelectNullCell_Null() throws Exception {
		Grid grid = mGridBuilderStub.build();
		assertThat("Selected cell", grid.setSelectedCell((Cell) null),
				is(nullValue()));
	}

	@Test
	public void setSelectedCell_NoCellCurrentlySelectedInGrid_BordersOfNewCageSetAndSelectedCellReturned()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mAnyGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();

		assertThat(
				"Selected cell",
				grid
						.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup),
				is(sameInstance(mGridBuilderStub.mAnyCellMockOfDefaultSetup)));

		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup).invalidateBordersOfAllCells();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_OldSelectedCellIsDeselected()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mAnyGridCageOfDefaultSetup);
		Cell otherCellMock = mock(Cell.class);
		when(otherCellMock.getCage()).thenReturn(
				mGridBuilderStub.mAnyGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();

		// Select the cells in given order
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		grid.setSelectedCell(otherCellMock);

		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup).deselect();
	}

	@Test
	public void setSelectedCell_CurrentlySelectedCellInGridIsSelectedAgain_NoBordersReset()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mAnyGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();

		// Select the grid cell
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup, times(1))
				.invalidateBordersOfAllCells();

		// Select the same cell one more. The borders may not be reset again.
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup, times(1))
				.invalidateBordersOfAllCells();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_NoBordersReset()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.getCage())
				.thenReturn(mGridBuilderStub.mAnyGridCageOfDefaultSetup);

		Cell otherCellMock = mock(Cell.class);
		when(otherCellMock.getCage()).thenReturn(
				mGridBuilderStub.mAnyGridCageOfDefaultSetup);
		Grid grid = mGridBuilderStub.build();

		// Select grid cell stub 1
		grid.setSelectedCell(mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup, times(1))
				.invalidateBordersOfAllCells();

		// Select the other cell in the same cage. The borders may not be reset
		// again.
		grid.setSelectedCell(otherCellMock);
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup, times(1))
				.invalidateBordersOfAllCells();
	}

	@Test
	public void setSelectedCell_SelectCellInAnotherCage_NoBordersReset()
			throws Exception {
		GridCage gridCageMock1 = mock(GridCage.class);

		Cell cellStub1 = mock(Cell.class);
		when(cellStub1.getCage()).thenReturn(gridCageMock1);

		GridCage gridCageMock2 = mock(GridCage.class);

		Cell cellStub2 = mock(Cell.class);
		when(cellStub2.getCage()).thenReturn(gridCageMock2);
		Grid grid = mGridBuilderStub.build();

		// Select grid cell stub 1
		grid.setSelectedCell(cellStub1);
		verify(gridCageMock1, times(1)).invalidateBordersOfAllCells();

		// Select the other cell in the same cage. The borders of cage 1 and
		// cage 2 need both to be set.
		grid.setSelectedCell(cellStub2);
		verify(gridCageMock1, times(2)).invalidateBordersOfAllCells();
		verify(gridCageMock2, times(1)).invalidateBordersOfAllCells();
	}

	@Test
	public void clearRedundantPossiblesInSameRowOrColumn_AnotherCellInSameRowContainsTheRedundantPossibleValueButNoOtherPossibleValues_TheRedundantPossibleValueIsCleared()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value os also used as a possible
		// value in another cell on the same row as the selected cell.
		int idSelectedCell = 14;
		int valueSelectedCell = 1;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellOnSameRow = idSelectedCell - 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithUserValue(idSelectedCell, rowSelectedCell, columnSelectedCell,
										  valueSelectedCell)
				.setCellMockWithMaybeValues(idOtherCellOnSameRow, rowSelectedCell,
											columnSelectedCell - 1, new int[]{valueSelectedCell})
				.build();

		grid.clearRedundantPossiblesInSameRowOrColumn(mock(CellChange.class));

		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.hasPossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.removePossible(valueSelectedCell);
	}

	@Test
	public void clearRedundantPossiblesInSameRowOrColumn_AnotherCellInSameRowContainsMultiplePossibleValuesIncludingTheRedundantValue_TheRedundantPossibleValueIsCleared()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value os also used as a possible
		// value in another cell on the same row as the selected cell.
		int idSelectedCell = 14;
		int valueSelectedCell = 2;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellOnSameRow = idSelectedCell - 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithUserValue(idSelectedCell, rowSelectedCell, columnSelectedCell,
										  valueSelectedCell)
				.setCellMockWithMaybeValues(idOtherCellOnSameRow, rowSelectedCell,
											columnSelectedCell - 1,
											new int[]{valueSelectedCell - 1, valueSelectedCell,
													valueSelectedCell + 1})
				.build();

		grid.clearRedundantPossiblesInSameRowOrColumn(mock(CellChange.class));

		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.hasPossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.removePossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow],
				never()).removePossible(valueSelectedCell - 1);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow],
				never()).removePossible(valueSelectedCell + 1);
	}

	@Test
	public void clearRedundantPossiblesInSameRowOrColumn_AnotherCellInSameColumnContainsTheRedundantPossibleValueButNoOtherPossibleValues_TheRedundantPossibleValueIsCleared()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value is also used as a possible
		// value in another cell in the same column as the selected cell.
		int idSelectedCell = 14;
		int valueSelectedCell = 1;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellInSameColumn = idSelectedCell - 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithUserValue(idSelectedCell, rowSelectedCell, columnSelectedCell,
										  valueSelectedCell)
				.setCellMockWithMaybeValues(idOtherCellInSameColumn, rowSelectedCell - 1,
											columnSelectedCell, new int[]{valueSelectedCell})
				.build();

		grid.clearRedundantPossiblesInSameRowOrColumn(mock(CellChange.class));

		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn])
				.hasPossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn])
				.removePossible(valueSelectedCell);
	}

	@Test
	public void clearRedundantPossiblesInSameRowOrColumn_AnotherCellInSameColumnContainsMultiplePossibleValuesIncludingTheRedundantValue_TheRedundantPossibleValueIsCleared()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value os also used as a possible
		// value in another cell on the same row as the selected cell.
		int idSelectedCell = 14;
		int valueSelectedCell = 1;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellInSameColumn = idSelectedCell - 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithUserValue(idSelectedCell, rowSelectedCell, columnSelectedCell,
										  valueSelectedCell)
				.setCellMockWithMaybeValues(idOtherCellInSameColumn, rowSelectedCell - 1,
											columnSelectedCell, new int[]{valueSelectedCell})
				.build();

		grid.clearRedundantPossiblesInSameRowOrColumn(mock(CellChange.class));

		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn])
				.hasPossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn])
				.removePossible(valueSelectedCell);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn],
				never()).removePossible(valueSelectedCell - 1);
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellInSameColumn],
				never()).removePossible(valueSelectedCell + 1);
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CellsListIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCells(null);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CellsListIsEmpty_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setCells(new ArrayList<Cell>());
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CellsHasTooLittleCells_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setGridSize(2);
		mGridBuilderStub.setCellsInitializedWith(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyCellMockOfDefaultSetup);
		assertThat(mGridBuilderStub.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_CellsHasTooManyCells_InvalidParameterException()
			throws Exception {
		mGridBuilderStub.setGridSize(1);
		mGridBuilderStub.setCellsInitializedWith(
				mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				mGridBuilderStub.mAnyCellMockOfDefaultSetup);
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
				.useSameMockForAllCells()
				.useSameMockForAllGridCages()
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
		assertThat("Cells", grid.getCells(), is(mGridBuilderStub.mCells));
		assertThat("Cages", grid.getCages(), is(mGridBuilderStub.mCages));
		assertThat("Is active", grid.isActive(), is(mGridBuilderStub.mActive));
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).setGridReference(
				any(Grid.class));
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size())).setGridReference(
				any(Grid.class));
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).setCageText(anyString());
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup,
				times(mGridBuilderStub.mCages.size())).checkUserMath();
		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup,
				times(mGridBuilderStub.mCells.size()))
				.markDuplicateValuesInSameRowAndColumn();
	}

	@Test
	public void save_NoErrorsOnSave_GridIdSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		int rowId = 34;
		when(mGridSaverMock.save(grid)).thenReturn(true);
		when(mGridSaverMock.getRowId()).thenReturn(rowId);

		assertThat(grid.save(), is(true));
		assertThat(grid.getRowId(), is(rowId));
	}

	@Test
	public void save_NoErrorsOnSave_SolvingAttemptIdSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		int solvingAttemptId = 35;
		when(mGridSaverMock.save(grid)).thenReturn(true);
		when(mGridSaverMock.getSolvingAttemptId()).thenReturn(solvingAttemptId);

		assertThat(grid.save(), is(true));
		assertThat(grid.getSolvingAttemptId(), is(solvingAttemptId));
	}

	@Test
	public void save_NoErrorsOnSave_StatisticsSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		GridStatistics gridStatistics = mock(GridStatistics.class);
		when(mGridSaverMock.save(grid)).thenReturn(true);
		when(mGridSaverMock.getGridStatistics()).thenReturn(gridStatistics);

		assertThat(grid.save(), is(true));
		assertThat(grid.getGridStatistics(), is(gridStatistics));
	}

	@Test
	public void save_ErrorOnSave_False() throws Exception {
		Grid grid = mGridBuilderStub.build();
		when(mGridSaverMock.save(grid)).thenReturn(false);

		assertThat(grid.save(), is(false));
		verify(mGridSaverMock, never()).getRowId();
		verify(mGridSaverMock, never()).getSolvingAttemptId();
		verify(mGridSaverMock, never()).getGridStatistics();
	}

	@Test
	public void saveOnAppUpgrade_NoErrorsOnSaveOnAppUpgrade_GridIdSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		int rowId = 34;
		when(mGridSaverMock.saveOnAppUpgrade(grid)).thenReturn(true);
		when(mGridSaverMock.getRowId()).thenReturn(rowId);

		assertThat(grid.saveOnAppUpgrade(), is(true));
		assertThat(grid.getRowId(), is(rowId));
	}

	@Test
	public void saveOnAppUpgrade_NoErrorsOnSaveOnAppUpgrade_SolvingAttemptIdSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		int solvingAttemptId = 35;
		when(mGridSaverMock.saveOnAppUpgrade(grid)).thenReturn(true);
		when(mGridSaverMock.getSolvingAttemptId()).thenReturn(solvingAttemptId);

		assertThat(grid.saveOnAppUpgrade(), is(true));
		assertThat(grid.getSolvingAttemptId(), is(solvingAttemptId));
	}

	@Test
	public void saveOnAppUpgrade_NoErrorsOnSaveOnAppUpgrade_StatisticsSet() throws Exception {
		Grid grid = mGridBuilderStub.build();
		GridStatistics gridStatistics = mock(GridStatistics.class);
		when(mGridSaverMock.saveOnAppUpgrade(grid)).thenReturn(true);
		when(mGridSaverMock.getGridStatistics()).thenReturn(gridStatistics);

		assertThat(grid.saveOnAppUpgrade(), is(true));
		assertThat(grid.getGridStatistics(), is(gridStatistics));
	}

	@Test
	public void saveOnAppUpgrade_ErrorOnSaveOnAppUpgrade_False() throws Exception {
		Grid grid = mGridBuilderStub.build();
		when(mGridSaverMock.saveOnAppUpgrade(grid)).thenReturn(false);

		assertThat(grid.saveOnAppUpgrade(), is(false));
		verify(mGridSaverMock, never()).getRowId();
		verify(mGridSaverMock, never()).getSolvingAttemptId();
		verify(mGridSaverMock, never()).getGridStatistics();
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
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(true);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.containsNoUserValues(), is(false));
	}

	@Test
	public void containsNoUserValues_GridHasOneMaybeValueButNoUserValues_True()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.countPossibles())
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
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.isUserValueSet())
				.thenReturn(true);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.isEmpty(), is(false));
	}

	@Test
	public void isEmpty_GridHasOneMaybeValueButNoUserValues_False()
			throws Exception {
		when(mGridBuilderStub.mAnyCellMockOfDefaultSetup.countPossibles())
				.thenReturn(1);
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.isEmpty(), is(false));
	}

	@Test
	public void createNewGridForReplay_ContainsCellWithDuplicateValueHighlight_DuplicateHighLightIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = new TestGridVisibleOperators()
				.setEmptyGrid()
				.getGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).setDuplicateHighlight(true);

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1),
				is(not(originalGrid.getCell(idOfCell_1))));
	}

	@Test
	public void createNewGridForReplay_ContainsCellWithUserValue_UserValueIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = new TestGridVisibleOperators()
				.setEmptyGrid()
				.getGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).setUserValue(3);

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).getUserValue(),
				is(not(originalGrid.getCell(idOfCell_1).getUserValue())));
	}

	@Test
	public void createNewGridForReplay_ContainsCellWithMaybeValue_PossiblesIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = new TestGridVisibleOperators()
				.setEmptyGrid()
				.getGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).addPossible(3);

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).getPossibles(),
				is(not(originalGrid.getCell(idOfCell_1).getPossibles())));
	}

	@Test
	public void createNewGridForReplay_ContainsRevealedCell_IsRevealedIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = new TestGridVisibleOperators()
				.setEmptyGrid()
				.getGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).setRevealed();

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).isRevealed(),
				is(not(originalGrid.getCell(idOfCell_1).isRevealed())));
	}

	@Test
	public void createNewGridForReplay_InvalidUserHighlight_InvalidHighlightIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = new TestGridVisibleOperators()
				.setEmptyGrid()
				.getGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).setInvalidHighlight();

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1),
				is(not(originalGrid.getCell(idOfCell_1))));
	}

	@Test
	public void createNewGridForReplay_HasSelectedCell_SelectedCellIsCleared()
			throws Exception {
		// Use a grid without mocks for this test.
		Grid originalGrid = new TestGridVisibleOperators()
				.setEmptyGrid()
				.getGrid();
		// Set only one property of one cell which should not be copied to the
		// new grid.
		int idOfCell_1 = 5;
		originalGrid.getCell(idOfCell_1).select();

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).isSelected(),
				is(not(originalGrid.getCell(idOfCell_1).isSelected())));
	}

	@Test
	public void createNewGridForReplay_RevealedCageOperator_RevealedCageOperatorIsHidden()
			throws Exception {
		// Use a grid without mocks for this test.
		TestGridHiddenOperators testGridHiddenOperators = new TestGridHiddenOperators()
				.setEmptyGrid();
		Grid originalGrid = testGridHiddenOperators.getGrid();
		// Reveal the operator of a cage with an unrevealed cage operator. For
		// this a cell in such a cage has to be selected after which the cage
		// operator can be unrevealed. Deselect the cell again as after creating
		// the new grid, no cell is selected.
		int idOfCell_1 = testGridHiddenOperators
				.getIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator();
		Cell cell_1 = originalGrid.getCell(idOfCell_1);
		originalGrid.setSelectedCell(cell_1);
		originalGrid.revealOperatorSelectedCage();
		originalGrid.deselectSelectedCell();

		Grid newGrid = originalGrid.createNewGridForReplay();

		assertThat(newGrid, is(notNullValue()));
		assertThat(newGrid.getCell(idOfCell_1).getCageText(),
				is(not(originalGrid.getCell(idOfCell_1).getCageText())));
	}

	@Test
	public void markInvalidChoices_GridHasNoInvalidValues_GridStatisticsAreUpdated()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.markInvalidChoices(), is(0));

		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_CHECK_PROGRESS);
	}

	@Test
	public void markInvalidChoices_GridHasOneCellWithInvalidValue_CellWithInvalidValueIsHighlighted()
			throws Exception {
		int cellId = 7;
		TestGridVisibleOperators testGridCreator = new TestGridVisibleOperators()
				.setEmptyGrid()
				.setIncorrectUserValueInCell(cellId);
		Grid grid = testGridCreator.getGrid();

		assertThat(grid.markInvalidChoices(), is(1));
		assertThat(grid.getCell(7).hasInvalidUserValueHighlight(), is(true));
	}

	@Test
	public void markInvalidChoices_GridHasTwoCellsWithInvalidValue_StatisticsOfInvalidCellsFoundUpdated()
			throws Exception {
		int cellId_1 = 7;
		int cellId_2 = 0;
		TestGridVisibleOperators testGridCreator = new TestGridVisibleOperators()
				.setEmptyGrid()
				.setIncorrectUserValueInCell(cellId_1)
				.setIncorrectUserValueInCell(cellId_2);
		Grid grid = testGridCreator.getGrid();

		assertThat(grid.markInvalidChoices(), is(2));
		assertThat(grid.getGridStatistics().mCheckProgressInvalidCellsFound,
				is(2));
	}

	@Test
	public void isReplay_GridStatisticsReplayCounterIs0_False()
			throws Exception {
		Grid grid = mGridBuilderStub.build();
		when(mGridStatisticsMock.getReplayCount()).thenReturn(0);

		assertThat(grid.isReplay(), is(false));
	}

	@Test
	public void isReplay_GridStatisticsReplayCounterIs1_True() throws Exception {
		Grid grid = mGridBuilderStub.build();
		when(mGridStatisticsMock.getReplayCount()).thenReturn(1);

		assertThat(grid.isReplay(), is(true));
	}

	@Test
	public void revealSelectedCell_CellIsSelected_UndoInformationForSelectedCellIsSaved()
			throws Exception {
		int idSelectedCell = 14;
		Grid grid = mGridBuilderStub.setCellMockAsSelectedCell(idSelectedCell).build();

		assertThat(grid.revealSelectedCell(), is(true));
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idSelectedCell])
				.saveUndoInformation(any(CellChange.class));
	}

	@Test
	public void revealSelectedCell_CellIsSelected_SelectedCellIsRevealed()
			throws Exception {
		int idSelectedCell = 14;
		Grid grid = mGridBuilderStub.setCellMockAsSelectedCell(idSelectedCell).build();

		assertThat(grid.revealSelectedCell(), is(true));
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idSelectedCell])
				.setRevealed();
	}

	@Test
	public void revealSelectedCell_CellIsSelected_SelectedCellContainsCorrectUserValue()
			throws Exception {
		int idSelectedCell = 14;
		Grid grid = mGridBuilderStub.setCellMockAsSelectedCell(idSelectedCell).build();
		int correctValue = 3;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idSelectedCell]
						.getCorrectValue()).thenReturn(correctValue);

		assertThat(grid.revealSelectedCell(), is(true));
		verify(mGridBuilderStub.mCellMockOfDefaultSetup[idSelectedCell])
				.setUserValue(correctValue);
	}

	@Test
	public void revealSelectedCell_RevealedValueIsUsedAsPossibleInOtherCellOnSameRow_RedundantPossibleValueIsRemoved()
			throws Exception {
		// Set up a grid with a for which the user value of the selected is
		// revealed and for which the same value os also used as a possible
		// value in another cell on the same row as the selected cell.
		int idSelectedCell = 14;
		int rowSelectedCell = 3;
		int columnSelectedCell = 2;
		int idOtherCellOnSameRow = idSelectedCell - 1;
		int valueRevealed = 1;
		Grid grid = mGridBuilderStub
				.setCellMockAsSelectedCell(idSelectedCell)
				.setCellMockWithUserValue(idSelectedCell, rowSelectedCell, columnSelectedCell,
										  valueRevealed)
				.setCellMockWithMaybeValues(idOtherCellOnSameRow, rowSelectedCell,
											columnSelectedCell - 1, new int[]{valueRevealed})
				.build();
		// Enable puzzle setting to clear maybes.
		when(mPreferencesMock.isPuzzleSettingClearMaybesEnabled()).thenReturn(
				true);

		// See unit tests for method clearRedundantPossiblesInSameRowOrColumn
		// for other scenario's. Just one scenario is tested for method
		// revealSelectedCell in order to test whether this method is invoked.
		assertThat(grid.revealSelectedCell(), is(true));
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOtherCellOnSameRow])
				.removePossible(valueRevealed);
	}

	@Test
	public void revealSelectedCell_CellIsSelected_StatisticsUpdated()
			throws Exception {
		int idSelectedCell = 14;
		Grid grid = mGridBuilderStub.setCellMockAsSelectedCell(idSelectedCell).build();

		assertThat(grid.revealSelectedCell(), is(true));
		verify(mGridBuilderStub.mGridStatistics).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_REVEAL_CELL);
	}

	@Test
	public void revealSelectedCell_NoCellIsSelected_False() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.revealSelectedCell(), is(false));
	}

	@Test
	public void revealOperatorSelectedCage_CageSelectedForCageWithHiddenOperator_OperatorRevealed()
			throws Exception {
		int idOfSelectedCell = 14;
		Grid grid = mGridBuilderStub.setCellMockAsSelectedCell(idOfSelectedCell).build();
		int idOfUpperLeftCellOfSelectedCage = 13;
		when(mGridBuilderStub.mAnyGridCageOfDefaultSetup.isOperatorHidden())
				.thenReturn(true);
		when(mGridBuilderStub.mAnyGridCageOfDefaultSetup.getIdUpperLeftCell())
				.thenReturn(idOfUpperLeftCellOfSelectedCage);
		String newCageText = "** SOME NEW CAGE TEXT **";
		when(mGridBuilderStub.mAnyGridCageOfDefaultSetup.getCageText())
				.thenReturn(newCageText);

		assertThat(grid.revealOperatorSelectedCage(), is(true));
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup).revealOperator();
		verify(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfUpperLeftCellOfSelectedCage])
				.setCageText(newCageText);
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_REVEAL_OPERATOR);
	}

	@Test
	public void revealOperatorSelectedCage_CageSelectedForCageWithVisibleOperator_OperatorRevealed()
			throws Exception {
		int idOfSelectedCell = 14;
		Grid grid = mGridBuilderStub.setCellMockAsSelectedCell(idOfSelectedCell).build();
		when(mGridBuilderStub.mAnyGridCageOfDefaultSetup.isOperatorHidden())
				.thenReturn(false);

		assertThat(grid.revealOperatorSelectedCage(), is(false));
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup, never())
				.revealOperator();
	}

	@Test
	public void revealOperatorSelectedCage_NoCageSelectedForCageWithVisibleOperator_OperatorRevealed()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.revealOperatorSelectedCage(), is(false));
		verify(mGridBuilderStub.mAnyGridCageOfDefaultSetup, never())
				.revealOperator();
	}

	@Test
	public void getUserValuesForCells_CellsIsNull_EmptyListReturned()
			throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getUserValuesForCells(null).size(), is(0));
	}

	@Test
	public void getUserValuesForCells_CellWithInvalidId_EmptyListReturned()
			throws Exception {
		int idOfCell = -1; // Value should not be in range 0 to gridsize *
							// gridsize
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getUserValuesForCells(new int[] { idOfCell }).size(),
				is(0));
	}

	@Test
	public void getUserValuesForCells_GetValueForOneCellHavingAUserValue_UserValueReturned()
			throws Exception {
		int idOfCell = 4;
		int valueOfCell = 3;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell]
						.isUserValueSet()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell]
						.getUserValue()).thenReturn(valueOfCell);
		Grid grid = mGridBuilderStub.build();

		List<Integer> expectedUserValues = new ArrayList<Integer>();
		expectedUserValues.add(valueOfCell);
		assertThat(grid.getUserValuesForCells(new int[] { idOfCell }),
				is(expectedUserValues));
	}

	@Test
	public void getUserValuesForCells_GetValueForOneCellNotHavingAUserValue_NoUserValuesReturned()
			throws Exception {
		int idOfCell = 4;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell]
						.isUserValueSet()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		List<Integer> expectedUserValues = new ArrayList<Integer>();
		assertThat(grid.getUserValuesForCells(new int[] { idOfCell }),
				is(expectedUserValues));
	}

	@Test
	public void getUserValuesForCells_GetValueForMultipleCellsAllHavingAUserValue_UserValuesReturned()
			throws Exception {
		int idOfCell_1 = 4;
		int valueOfCell_1 = 3;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]
						.isUserValueSet()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]
						.getUserValue()).thenReturn(valueOfCell_1);
		int idOfCell_2 = 8;
		int valueOfCell_2 = 2;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]
						.isUserValueSet()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]
						.getUserValue()).thenReturn(valueOfCell_2);
		Grid grid = mGridBuilderStub.build();

		List<Integer> expectedUserValues = new ArrayList<Integer>();
		expectedUserValues.add(valueOfCell_1);
		expectedUserValues.add(valueOfCell_2);
		assertThat(grid.getUserValuesForCells(new int[] { idOfCell_1,
				idOfCell_2 }), is(expectedUserValues));
	}

	@Test
	public void getUserValuesForCells_GetValueForMultipleCellsNotAllHavingAUserValue_UserValuesReturned()
			throws Exception {
		int idOfCell_1 = 4;
		int valueOfCell_1 = 3;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]
						.isUserValueSet()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]
						.getUserValue()).thenReturn(valueOfCell_1);
		int idOfCell_2 = 8;
		int valueOfCell_2 = 2;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]
						.isUserValueSet()).thenReturn(true);
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]
						.getUserValue()).thenReturn(valueOfCell_2);
		int idOfCell_3 = 9;
		when(
				mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_3]
						.isUserValueSet()).thenReturn(false);
		Grid grid = mGridBuilderStub.build();

		List<Integer> expectedUserValues = new ArrayList<Integer>();
		expectedUserValues.add(valueOfCell_1);
		expectedUserValues.add(valueOfCell_2);
		assertThat(
				grid.getUserValuesForCells(new int[] { idOfCell_1, idOfCell_2,
						idOfCell_3 }), is(expectedUserValues));
	}

	@Test
	public void invalidateBordersOfAllCells_MultipleCells_BorderAreSet() throws Exception {
		Grid grid = mGridBuilderStub.useSameMockForAllCells().build();

		grid.invalidateBordersOfAllCells();

		verify(mGridBuilderStub.mAnyCellMockOfDefaultSetup).invalidateBorders();
	}

	@Test
	public void getCells_CellsIsNull_EmptyListReturned() throws Exception {
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getCells(null).size(), is(0));
	}

	@Test
	public void getCells_InvalidCellId_EmptyListReturned() throws Exception {
		int idOfCell = -1; // Value should not be in range 0 to gridsize *
							// gridsize
		Grid grid = mGridBuilderStub.build();

		assertThat(grid.getCells(new int[]{idOfCell}).size(), is(0));
	}

	@Test
	public void getCells_GetSingleCell_CellReturned() throws Exception {
		int idOfCell = 4;
		Grid grid = mGridBuilderStub.build();

		List<Cell> expectedCells = new ArrayList<Cell>();
		expectedCells
				.add(mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell]);
		assertThat(grid.getCells(new int[]{idOfCell}),
				is(expectedCells));
	}

	@Test
	public void getCells_GetMultipleCells_CellsReturned() throws Exception {
		int idOfCell_1 = 4;
		int idOfCell_2 = 8;
		Grid grid = mGridBuilderStub.build();

		List<Cell> expectedCells = new ArrayList<Cell>();
		expectedCells
				.add(mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_1]);
		expectedCells
				.add(mGridBuilderStub.mCellMockOfDefaultSetup[idOfCell_2]);
		assertThat(grid.getCells(new int[]{idOfCell_1, idOfCell_2}),
				is(expectedCells));
	}
}
