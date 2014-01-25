package net.mathdoku.plus.grid;

import android.app.Activity;
import android.content.Context;

import com.srlee.DLX.MathDokuDLX;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.CageOperator;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
	private GridBuilderStub mGridBuilder;

	private class GridBuilderStub extends GridBuilder {
		public GridBuilderStub setupDefaultWhichDoesNotThrowErrorsOnBuild() {
			int gridSize = 4;
			setGridSize(gridSize);

			// Insert exact number of cells needed with this grid size.
			int numberOfCells = gridSize * gridSize;
			ArrayList<GridCell> gridCells = mGridObjectsCreator
					.createArrayListOfGridCells();
			for (int i = 0; i < numberOfCells; i++) {
				gridCells.add(mGridObjectsCreator.createGridCell(i, gridSize));
			}
			super.setCells(gridCells);

			// Insert an arbitrary number of cages (at least 1)
			int numberOfCages = 3;
			ArrayList<GridCage> gridCages = mGridObjectsCreator
					.createArrayListOfGridCages();
			for (int i = 0; i < numberOfCages; i++) {
				gridCages.add(mGridObjectsCreator.createGridCage());
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
		private GridBuilderStub setCellsInitializedWith(GridCell... gridCell) {
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
		private GridBuilderStub setCagesInitializedWith(GridCage... gridCage) {
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
		private GridBuilderStub setCellChangesInitializedWith(
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

	private GridObjectsCreatorStub mGridObjectsCreatorStub;

	private class GridObjectsCreatorStub extends GridObjectsCreator {
		// Unreveal the array list of cell changes as it is hidden in the Grid
		// Object.
		public ArrayList<CellChange> mArrayListOfCellChanges = null;

		@Override
		public GridCell createGridCell(int id, int gridSize) {
			return mGridCellMock;
		}

		@Override
		public GridCage createGridCage() {
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
		mGridBuilder = new GridBuilderStub();
		mGridBuilder.setObjectsCreator(mGridObjectsCreatorStub);
		mGridBuilder.setupDefaultWhichDoesNotThrowErrorsOnBuild();
	}

	@Test
	public void setPreferences_CreateGrid_PreferencesAreRetrieved()
			throws Exception {
		Grid grid = mGridBuilder.build();
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
		Grid grid = mGridBuilder.build();
		verify(mGridCellMock, times(grid.mCells.size())).setBorders();

		grid.setPreferences();

		verify(mGridCellMock, times(2 * grid.mCells.size())).setBorders();
	}

	@Test
	public void getSelectedCage_SelectedCellIsNull_NullCage() {
		Grid grid = mGridBuilder.build();
		assertThat("Selected cage", grid.getSelectedCage(), is(nullValue()));
	}

	@Test
	public void getSelectedCage_SelectedCellIsNotNull_CageSelected() {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		Grid grid = mGridBuilder.build();
		grid.setSelectedCell(mGridCellMock);

		assertThat("Selected cage", grid.getSelectedCage(), is(mGridCageMock));
	}

	@Test
	public void clearCells_GridWithMultipleMovesCleared_AllMovesCleared()
			throws Exception {
		mGridBuilder.setCellChangesInitializedWith(mCellChangeMock,
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class));
		Grid grid = mGridBuilder.build();
		assertThat("Number of cell changes before clear", grid.countMoves(),
				is(mGridBuilder.mCellChanges.size()));

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
		Grid grid = mGridBuilder.build();

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = false;
		grid.clearCells(replay);

		// Note: currently a cell is always cleared even in case it does not
		// contain a user values nor any maybe values.
		verify(mGridCellMock, times(grid.mCells.size())).clear();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_FlagsOfAllCellsCleared()
			throws Exception {
		Grid grid = mGridBuilder.build();

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = true;
		grid.clearCells(replay);

		verify(mGridCellMock, times(grid.mCells.size())).clearAllFlags();
	}

	@Test
	public void clearCells_GridWithMultipleCellsCleared_GridStatisticsUpdated()
			throws Exception {
		when(mGridCellMock.getUserValue()).thenReturn(0, 1, 2, 0);
		Grid grid = mGridBuilder.build();

		// Clear the cells. Value of variable "replay" is not relevant for this
		// unit test.
		boolean replay = true;
		grid.clearCells(replay);

		// Clearing a second time won't change the statistics as no cells are
		// left to be cleared
		when(mGridCellMock.getUserValue()).thenReturn(0, 0, 0, 0);
		grid.clearCells(replay);

		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_CLEAR_GRID);
	}

	private void assertThatGridCellDoesNotExist(int gridSize, int row, int col) {
		mGridBuilder.setGridSize(gridSize);
		Grid grid = mGridBuilder.build();

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
		Grid grid = mGridBuilder.build();

		assertThat("Cell retrieved from grid", grid.getCellAt(
				mGridBuilder.mGridSize - 1, mGridBuilder.mGridSize - 1),
				is(sameInstance(mGridCellMock)));
	}

	@Test
	public void revealSolution_NonEmptyCellListWith2IncorrectCells_TwoCellsRevealed()
			throws Exception {
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(false, true,
				false, true, false);
		Grid grid = mGridBuilder.build();

		grid.revealSolution();

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridCellMock, times(mGridBuilder.mCells.size()))
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
		Grid grid = mGridBuilder.build();
		grid.revealSolution();

		verify(mGridStatisticsMock).solutionRevealed();
	}

	@Test
	public void unrevealSolution() throws Exception {
		Grid grid = mGridBuilder.build();
		Config.AppMode actualAppMode = Config.mAppMode;
		Config.AppMode expectedAppMode = Config.AppMode.DEVELOPMENT;

		assertThat("Development mode", actualAppMode, is(expectedAppMode));
		assertThat("Grid is unrevealed", grid.isSolutionRevealed(), is(false));
	}

	@Test
	public void isSolved_UnSolvedGridIsChecked_False() throws Exception {
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(false, false,
				true, false);
		mGridBuilder.setGridSize(2);
		mGridBuilder.setCellsInitializedWith(mGridCellMock, mGridCellMock,
				mGridCellMock, mGridCellMock);
		Grid grid = mGridBuilder.build();

		assertThat("Grid is solved", grid.isSolved(), is(false));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridCellMock, atLeast(1)).isUserValueIncorrect();
	}

	@Test
	public void isSolved_SolvedGridIsChecked_True() throws Exception {
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(false);
		Grid grid = mGridBuilder.build();

		assertThat("Grid is solved", grid.isSolved(), is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridCellMock, times(mGridBuilder.mCells.size()))
				.isUserValueIncorrect();
	}

	@Test
	public void setSolved_SolvedGrid_GridStatisticsUpdated() throws Exception {
		Grid grid = mGridBuilder.build();
		grid.setSolved();

		verify(mGridStatisticsMock).solved();
	}

	@Test
	public void setSolved_SolvedGrid_OnSolvedListenerCalled() throws Exception {
		Grid grid = mGridBuilder.build();
		Grid.OnSolvedListener gridOnSolvedListener = mock(Grid.OnSolvedListener.class);
		grid.setSolvedHandler(gridOnSolvedListener);

		// Solve it
		grid.setSolved();

		verify(gridOnSolvedListener).puzzleSolved();
	}

	@Test
	public void isSolutionValidSoFar_AllCellsEmpty_True() throws Exception {
		when(mGridCellMock.isUserValueSet()).thenReturn(false);
		Grid grid = mGridBuilder.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(true));

		// Check if test isn't flawed with incorrect number of cells in list.
		verify(mGridCellMock, times(mGridBuilder.mCells.size()))
				.isUserValueSet();
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsValid_True() throws Exception {
		when(mGridCellMock.isUserValueSet()).thenReturn(true);
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(false);
		Grid grid = mGridBuilder.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(true));
	}

	@Test
	public void isSolutionValidSoFar_FilledCellIsInvalid_False()
			throws Exception {
		when(mGridCellMock.isUserValueSet()).thenReturn(true);
		when(mGridCellMock.isUserValueIncorrect()).thenReturn(true);
		Grid grid = mGridBuilder.build();

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

		mGridBuilder.setGridSize(2);
		mGridBuilder.setCellsInitializedWith(gridCellEmptyStub,
				gridCellWithValidUserValueStub,
				gridCellWithInvalidUserValueStub,
				gridCellWithValidUserValueStub);
		Grid grid = mGridBuilder.build();

		assertThat("Grid is valid so far", grid.isSolutionValidSoFar(),
				is(false));
	}

	@Test
	public void addMove_FirstMoveAddedToNullList_True() throws Exception {
		mGridBuilder.mCellChanges = null;
		Grid grid = mGridBuilder.build();
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreatorStub.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_FirstMoveAddedToEmptyList_True() throws Exception {
		Grid grid = mGridBuilder.build();
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreatorStub.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void addMove_AddMultipleDifferentMoves_True() throws Exception {
		Grid grid = mGridBuilder.build();
		grid.addMove(mCellChangeMock);
		// Add another mock as moves may not be identical for this test
		grid.addMove(mock(CellChange.class));

		assertThat("Number of cell changes",
				mGridObjectsCreatorStub.mArrayListOfCellChanges.size(), is(2));
	}

	@Test
	public void addMove_AddMultipleIdenticalMoves_True() throws Exception {
		Grid grid = mGridBuilder.build();
		// Add same mock twice as we need identical moves for this test
		grid.addMove(mCellChangeMock);
		grid.addMove(mCellChangeMock);

		assertThat("Number of cell changes",
				mGridObjectsCreatorStub.mArrayListOfCellChanges.size(), is(1));
	}

	@Test
	public void countMoves_MovesListIsNull_ZeroMoves() throws Exception {
		mGridBuilder.mCellChanges = null;
		Grid grid = mGridBuilder.build();

		int actualNumberOfCellChanges = grid.countMoves();
		assertThat("Number of moves in a Grid with an null moves list",
				actualNumberOfCellChanges, is(0));
	}

	@Test
	public void countMoves_MovesListIsNotEmpty_MovesCountedCorrectly()
			throws Exception {
		mGridBuilder.setCellChangesInitializedWith(mock(CellChange.class),
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class), mock(CellChange.class),
				mock(CellChange.class));
		Grid grid = mGridBuilder.build();

		assertThat("Number of moves in a Grid with a non-empty moves list",
				grid.countMoves(), is(mGridBuilder.mCellChanges.size()));
	}

	@Test
	public void countMoves_MovesListIsEmpty_ZeroMoves() throws Exception {
		Grid grid = mGridBuilder.build();
		int actualNumberOfCellChanges = grid.countMoves();
		assertThat("Number of moves in a Grid with an empty moves list",
				actualNumberOfCellChanges, is(0));
	}

	@Test
	public void undoLastMove_NullMovesList_False() throws Exception {
		mGridBuilder.setCellChanges(null);
		Grid grid = mGridBuilder.build();

		assertThat("Undo last move", grid.undoLastMove(), is(false));
	}

	@Test
	public void undoLastMove_EmptyMovesList_False() throws Exception {
		Grid grid = mGridBuilder.build();
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
		when(mGridCellMock.getUserValue()).thenReturn( //
				0 /* value before actual undo */, //
				0 /* value after actual undo */);
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		when(mCellChangeMock.getGridCell()).thenReturn(mGridCellMock);
		mGridBuilder.setCellChangesInitializedWith(mCellChangeMock);
		int numberOfMovesBeforeUndo = mGridBuilder.mCellChanges.size();
		Grid grid = mGridBuilder.build();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo));

		grid.undoLastMove();

		verify(mCellChangeMock).restore();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo - 1));
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_UNDO_MOVE);
		assertThat("Selected cell", grid.getSelectedCell(),
				is(sameInstance(mGridCellMock)));
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
		when(mGridCellMock.getUserValue()).thenReturn( //
				0 /* value before actual undo */, //
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
		when(mGridCellMock.getCage()).thenReturn(
				gridCageMockOfGridCellInCellChange);
		when(mCellChangeMock.getGridCell()).thenReturn(mGridCellMock);
		mGridBuilder.setCellChangesInitializedWith(mCellChangeMock);
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
		int numberOfMovesBeforeUndo = mGridBuilder.mCellChanges.size();
		Grid grid = mGridBuilder.build();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo));

		grid.undoLastMove();

		verify(mCellChangeMock).restore();
		assertThat(grid.countMoves(), is(numberOfMovesBeforeUndo - 1));
		verify(mGridStatisticsMock).increaseCounter(
				GridStatistics.StatisticsCounterType.ACTION_UNDO_MOVE);
		assertThat("Selected cell", grid.getSelectedCell(),
				is(sameInstance(mGridCellMock)));
		verify(gridCellInSameRowOrColumn, times(arrayListOfGridCells.size()))
				.markDuplicateValuesInSameRowAndColumn();
		verify(gridCageMockOfGridCellInCellChange).checkUserMath();
	}

	@Test
	public void deselectSelectedCell_CellIsSelected_CellIsDeselected()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		Grid grid = mGridBuilder.build();
		grid.setSelectedCell(mGridCellMock);
		verify(mGridCageMock).setBorders();

		// Deselect this cell
		grid.deselectSelectedCell();

		verify(mGridCellMock).deselect();
		verify(mGridCageMock, times(2)).setBorders();

		assertThat("Selected cell", grid.getSelectedCell(), is(nullValue()));
	}

	@Test
	public void setSelectedCell_SelectNullGridCell_Null() throws Exception {
		Grid grid = mGridBuilder.build();
		assertThat("Selected cell", grid.setSelectedCell((GridCell) null),
				is(nullValue()));
	}

	@Test
	public void setSelectedCell_NoCellCurrentlySelectedInGrid_BordersOfNewCageSetAndSelectedCellReturned()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		Grid grid = mGridBuilder.build();

		assertThat("Selected cell", grid.setSelectedCell(mGridCellMock),
				is(sameInstance(mGridCellMock)));

		verify(mGridCageMock).setBorders();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_OldSelectedCellIsDeselected()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		GridCell otherGridCellMock = mock(GridCell.class);
		when(otherGridCellMock.getCage()).thenReturn(mGridCageMock);
		Grid grid = mGridBuilder.build();

		// Select the cells in given order
		grid.setSelectedCell(mGridCellMock);
		grid.setSelectedCell(otherGridCellMock);

		verify(mGridCellMock).deselect();
	}

	@Test
	public void setSelectedCell_CurrentlySelectedCellInGridIsSelectedAgain_NoBordersReset()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);
		Grid grid = mGridBuilder.build();

		// Select the grid cell
		grid.setSelectedCell(mGridCellMock);
		verify(mGridCageMock, times(1)).setBorders();

		// Select the same cell one more. The borders may not be reset again.
		grid.setSelectedCell(mGridCellMock);
		verify(mGridCageMock, times(1)).setBorders();
	}

	@Test
	public void setSelectedCell_SelectAnotherCellInTheCurrentlySelectedCage_NoBordersReset()
			throws Exception {
		when(mGridCellMock.getCage()).thenReturn(mGridCageMock);

		GridCell otherGridCellMock = mock(GridCell.class);
		when(otherGridCellMock.getCage()).thenReturn(mGridCageMock);
		Grid grid = mGridBuilder.build();

		// Select grid cell stub 1
		grid.setSelectedCell(mGridCellMock);
		verify(mGridCageMock, times(1)).setBorders();

		// Select the other cell in the same cage. The borders may not be reset
		// again.
		grid.setSelectedCell(otherGridCellMock);
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
		Grid grid = mGridBuilder.build();

		// Select grid cell stub 1
		grid.setSelectedCell(gridCellStub1);
		verify(gridCageMock1, times(1)).setBorders();

		// Select the other cell in the same cage. The borders of cage 1 and
		// cage 2 need both to be set.
		grid.setSelectedCell(gridCellStub2);
		verify(gridCageMock1, times(2)).setBorders();
		verify(gridCageMock2, times(1)).setBorders();
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_ArrayListGridCellsIsNull_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = null;
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);
		Grid grid = mGridBuilder.build();

		grid.toGridDefinitionString(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_ArrayListGridCellsIsEmpty_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = new ArrayList<GridCell>();
		ArrayList<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);
		Grid grid = mGridBuilder.build();

		grid.toGridDefinitionString(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_ArrayListGridCagesIsNull_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = mock(ArrayList.class);
		when(gridCells.size()).thenReturn(1);
		ArrayList<GridCage> gridCages = null;
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);
		Grid grid = mGridBuilder.build();

		grid.toGridDefinitionString(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void toGridDefinitionString_ArrayListGridCagesIsEmpty_ThrowsInvalidParameterException()
			throws Exception {
		ArrayList<GridCell> gridCells = mock(ArrayList.class);
		when(gridCells.size()).thenReturn(1);
		ArrayList<GridCage> gridCages = new ArrayList<GridCage>();
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);
		Grid grid = mGridBuilder.build();

		grid.toGridDefinitionString(gridCells, gridCages,
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
		Grid grid = mGridBuilder.build();

		grid.toGridDefinitionString(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test
	public void toGridDefinitionString_WithValidParameters_GridDefinitionCreated()
			throws Exception {

		when(mGridCellMock.getCageId()).thenReturn( //
				0, 1, 2, 2, // Row 1
				0, 1, 2, 2, // Row 2
				1, 1, 1, 2, // Row 3
				3, 3, 3, 2 // Row 4
				);

		GridCage gridCageStub1 = mock(GridCage.class);
		when(gridCageStub1.getId()).thenReturn(0);
		when(gridCageStub1.getResult()).thenReturn(1);
		when(gridCageStub1.getOperator()).thenReturn(CageOperator.NONE);

		GridCage gridCageStub2 = mock(GridCage.class);
		when(gridCageStub2.getId()).thenReturn(1);
		when(gridCageStub2.getResult()).thenReturn(3);
		when(gridCageStub2.getOperator()).thenReturn(CageOperator.ADD);

		GridCage gridCageStub3 = mock(GridCage.class);
		when(gridCageStub3.getId()).thenReturn(2);
		when(gridCageStub3.getResult()).thenReturn(2);
		when(gridCageStub3.getOperator()).thenReturn(CageOperator.NONE);

		mGridBuilder.setCagesInitializedWith(gridCageStub1, gridCageStub2,
				gridCageStub3);

		mGeneratingParametersMock.mPuzzleComplexity = GridGenerator.PuzzleComplexity.NORMAL;
		mGeneratingParametersMock.mHideOperators = false;
		Grid grid = mGridBuilder.build();

		assertThat(
				"Grid definition",
				grid.toGridDefinitionString(mGridBuilder.mCells,
						mGridBuilder.mCages, mGeneratingParametersMock),
				is(equalTo("3:00010202000102020101010203030302:0,1,0:1,3,1:2,2,0")));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCellsListIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilder.setCells(null);
		assertThat(mGridBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCellsListIsEmpty_InvalidParameterException()
			throws Exception {
		mGridBuilder.setCells(new ArrayList<GridCell>());
		assertThat(mGridBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCellsHasTooLittleCells_InvalidParameterException()
			throws Exception {
		mGridBuilder.setGridSize(2);
		mGridBuilder.setCellsInitializedWith(mGridCellMock, mGridCellMock,
				mGridCellMock);
		assertThat(mGridBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCellsHasTooManyCells_InvalidParameterException()
			throws Exception {
		mGridBuilder.setGridSize(1);
		mGridBuilder.setCellsInitializedWith(mGridCellMock, mGridCellMock);
		assertThat(mGridBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCagesListIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilder.setCages(null);
		assertThat(mGridBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridCagesListIsEmpty_InvalidParameterException()
			throws Exception {
		mGridBuilder.setCages(new ArrayList<GridCage>());
		assertThat(mGridBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void gridCreateWithGridBuilder_GridGeneratingParametersIsNull_InvalidParameterException()
			throws Exception {
		mGridBuilder.setGridGeneratingParameters(null);
		assertThat(mGridBuilder.build(), is(nullValue()));
	}

	@Test
	public void gridCreateWithGridBuilder_ValidParameters_GridBuilderParamsUsed()
			throws Exception {
		long dateCreated = System.currentTimeMillis();
		mGridBuilder.setDateCreated(dateCreated);
		mGridBuilder.setActive(false);
		Grid grid = mGridBuilder.build();
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
		assertThat("Grid size", grid.getGridSize(), is(mGridBuilder.mGridSize));
		assertThat("Cells", grid.mCells, is(sameInstance(mGridBuilder.mCells)));
		assertThat("Cells", grid.mCages, is(sameInstance(mGridBuilder.mCages)));
		assertThat("Is active", grid.isActive(), is(mGridBuilder.mActive));
		verify(mGridCageMock, times(mGridBuilder.mCages.size()))
				.setGridReference(any(Grid.class));
		verify(mGridCellMock, times(mGridBuilder.mCells.size()))
				.setGridReference(any(Grid.class));
		verify(mGridCellMock, times(mGridBuilder.mCells.size())).setBorders();
	}

	private void setupForSaveTest() {
		// Instantiate singleton class Util
		new Util(new Activity());

		// Stub method toGridDefinitionString as this methods requires a lot of
		// setup which is not needed to test method save.
		mGridObjectsCreatorStub = new GridObjectsCreatorStub() {
			@Override
			public Grid createGrid(GridBuilder gridBuilder) {
				return new Grid(gridBuilder) {
					@Override
					public String toGridDefinitionString() {
						// This function will be tested in other unit tests.
						return "** SOME GRID DEFINITION STRING **";
					}
				};
			}
		};
		mGridBuilder.setObjectsCreator(mGridObjectsCreatorStub);

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
		Grid grid = mGridBuilder.build();
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
		Grid grid = mGridBuilder.build();
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
		Grid grid = mGridBuilder.build();
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
		mGridBuilder.setGridId(existingGridId);
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
		Grid grid = mGridBuilder.build();
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
		mGridBuilder.setGridId(existingGridId);
		// Prepare for failing insert of new solving attempt
		int mSolvingAttemptIdWhenInsertFails = -1;
		when(
				mSolvingAttemptDatabaseAdapterMock.insert(any(Grid.class),
						anyInt())).thenReturn(mSolvingAttemptIdWhenInsertFails);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilder.build();
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
		mGridBuilder.setSolvingAttemptId(existingGridId,
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
		Grid grid = mGridBuilder.build();
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
		mGridBuilder.setSolvingAttemptId(existingGridId,
				existingSolvingAttemptId);
		// Prepare failing update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock.update(anyInt(),
						any(Grid.class))).thenReturn(false);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilder.build();
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
		mGridBuilder.setSolvingAttemptId(existingGridId,
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
		Grid grid = mGridBuilder.build();
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
		mGridBuilder.setSolvingAttemptId(existingGridId,
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
		Grid grid = mGridBuilder.build();
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
		mGridBuilder.setSolvingAttemptId(existingGridId,
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
		mGridBuilder.setActive(false);
		when(mGridStatisticsMock.getReplayCount()).thenReturn(1);
		when(mGridStatisticsMock.isIncludedInStatistics()).thenReturn(false);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.
		Grid grid = mGridBuilder.build();
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
		Grid grid = mGridBuilder.build();

		assertThat("Grid is saved?", grid.saveOnAppUpgrade(), is(false));
	}

	private void assertThatSaveOnUpgrade(boolean resultSaveGridStatistics,
			Matcher expectedResultSaveOnUpgrade) {
		when(
				mSolvingAttemptDatabaseAdapterMock.updateOnAppUpgrade(anyInt(),
						any(Grid.class))).thenReturn(true);

		when(mGridStatisticsMock.save()).thenReturn(resultSaveGridStatistics);
		Grid grid = mGridBuilder.build();

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
}
