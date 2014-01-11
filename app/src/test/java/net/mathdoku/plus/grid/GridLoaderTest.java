package net.mathdoku.plus.grid;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.database.SolvingAttempt;
import net.mathdoku.plus.storage.database.SolvingAttemptData;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridLoaderTest {
	private GridLoader mGridLoader;
	private GridLoader.ObjectsCreator mGridLoaderObjectsCreator;

	private int mGridMockSize;
	private int mGridMockExpectedNumberOfCells;
	private int mGridMockExpectedNumberOfCages;

	private Grid mGridMock = mock(Grid.class);
	private GridCell mGridCellMock = mock(GridCell.class);
	private GridCage mGridCageMock = mock(GridCage.class);
	private CellChange mCellChangeMock = mock(CellChange.class);
	private StatisticsDatabaseAdapter mStatisticsDatabaseAdapterMock = mock(StatisticsDatabaseAdapter.class);
	private GridStorage mGridStorageMock = mock(GridStorage.class);
	private GridCageStorage mGridCageStorageMock = mock(GridCageStorage.class);

	private class GridLoaderObjectsCreator extends GridLoader.ObjectsCreator {
		@Override
		public GridCell createGridCell(int id, int gridSize) {
			return mGridCellMock;
		}

		@Override
		public GridCage createGridCage(int id, boolean hideOperator,
				int result, int action, ArrayList<GridCell> cells) {
			return mGridCageMock;
		}

		@Override
		public StatisticsDatabaseAdapter createStatisticsDatabaseAdapter() {
			return mStatisticsDatabaseAdapterMock;
		}

		@Override
		public GridStorage createGridStorage() {
			return mGridStorageMock;
		}

		@Override
		public GridCageStorage createGridCageStorage() {
			return mGridCageStorageMock;
		}
	}

	private class SolvingAttemptStub extends SolvingAttempt {
		private boolean mIncludeGridInformation = false;
		private boolean mIncludeInvalidLineBetweenGridInformationAndCell = false;
		private int mNumberOfCells = 0;
		private boolean mIncludeInvalidLineBetweenCellAndCages = false;
		private int mNumberOfCages = 0;
		private boolean mIncludeInvalidLineBetweenCagesAndCellChanges = false;
		private int mNumberOfCellChanges = 0;
		private boolean mIncludeInvalidLineAfterCellChanges = false;

		public SolvingAttemptStub() {
			mSavedWithRevision = 596;
			setData();
		}

		public SolvingAttemptStub setHasInvalidLineBetweenGridInformationAndCell() {
			mIncludeInvalidLineBetweenGridInformationAndCell = true;
			setData();

			return this;
		}

		public SolvingAttemptStub setHasGeneralGridInformation() {
			mIncludeGridInformation = true;
			setData();

			return this;
		}

		public SolvingAttemptStub setNumberOfCells(int numberOfCells) {
			mNumberOfCells = numberOfCells;
			setData();

			return this;
		}

		public SolvingAttemptStub setHasInvalidLineBetweenCellAndCages() {
			mIncludeInvalidLineBetweenCellAndCages = true;
			setData();

			return this;
		}

		public SolvingAttemptStub setNumberOfCages(int numberOfCages) {
			mNumberOfCages = numberOfCages;
			setData();

			return this;
		}

		public SolvingAttemptStub setHasInvalidLineBetweenCagesAndCellChanges() {
			mIncludeInvalidLineBetweenCagesAndCellChanges = true;
			setData();

			return this;
		}

		public SolvingAttemptStub setNumberOfCellChanges(int numberOfCellChanges) {
			mNumberOfCellChanges = numberOfCellChanges;
			setData();

			return this;
		}

		public SolvingAttemptStub setHasInvalidLineAfterCellChanges() {
			mIncludeInvalidLineAfterCellChanges = true;
			setData();

			return this;
		}

		private void setData() {
			StringBuilder stringBuilder = new StringBuilder();

			if (mIncludeGridInformation) {
				stringBuilder.append("** GRID INFORMATION **");
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
			if (mIncludeInvalidLineBetweenGridInformationAndCell) {
				stringBuilder
						.append("** INVALID DATA BETWEEN GRID INFORMATION AND CELLS **");
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
			for (int i = 1; i <= mNumberOfCells; i++) {
				stringBuilder.append("** CELL " + i + " DATA **");
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
			if (mIncludeInvalidLineBetweenCellAndCages) {
				stringBuilder
						.append("** INVALID DATA BETWEEN CELLS AND CAGES **");
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
			for (int i = 1; i <= mNumberOfCages; i++) {
				stringBuilder.append("** CAGE " + i + " DATA **");
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
			if (mIncludeInvalidLineBetweenCagesAndCellChanges) {
				stringBuilder
						.append("** INVALID DATA BETWEEN CAGES AND CELL CHANGES **");
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
			for (int i = 1; i <= mNumberOfCellChanges; i++) {
				stringBuilder.append("** CELL CHANGE " + i + " DATA **");
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}
			if (mIncludeInvalidLineAfterCellChanges) {
				stringBuilder.append("** INVALID DATA AFTER CELL CHANGES **");
				stringBuilder
						.append(SolvingAttemptDatabaseAdapter.EOL_DELIMITER);
			}

			mData = new SolvingAttemptData(stringBuilder.toString());
		}

		public void setNullData() {
			mData = null;
		}
	}

	@Before
	public void Setup() {
		mGridLoaderObjectsCreator = new GridLoaderObjectsCreator();
		mGridLoader = new GridLoader(mGridMock, mGridLoaderObjectsCreator);

		// Even when running the unit test in the debug variant,
		// the grid loader production code should be tested only.
		if (Config.mAppMode == Config.AppMode.DEVELOPMENT) {
			mGridLoader.setThrowExceptionOnError(false);
		}

		// Use a real array lists to store created cells, cages and cell changes
		// instead of mock.
		mGridMock.mCells = new ArrayList<GridCell>();
		mGridMock.mCages = new ArrayList<GridCage>();
	}

	public void load_SolvingAttemptNull_NotLoaded() throws Exception {
		SolvingAttempt solvingAttempt = null;
		assertThat("Grid load", mGridLoader.load(solvingAttempt), is(false));
	}

	public void load_SolvingAttemptWithInvalidGridSize_NotLoaded()
			throws Exception {
		gridMockSetGridSize(0);
		SolvingAttempt solvingAttempt = mock(SolvingAttempt.class);
		assertThat("Grid load", mGridLoader.load(solvingAttempt), is(false));
	}

	@Test
	public void load_SolvingAttemptWithNullData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub();
		solvingAttemptStub.setNullData();

		assertThat("Grid load", mGridLoader.load(solvingAttemptStub), is(false));
	}

	@Test
	public void load_SolvingAttemptWithWithInvalidGridData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasInvalidLineBetweenGridInformationAndCell();
		when(mGridStorageMock.fromStorageString(anyString(), anyInt()))
				.thenReturn(false);

		assertThat(mGridLoader.load(solvingAttemptStub), is(false));
	}

	@Test
	public void load_SolvingAttemptMissingGridCellData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation();
		gridMockCanReadFromStorageString();

		assertThat(mGridLoader.load(solvingAttemptStub), is(false));
	}

	@Test
	public void load_SolvingAttemptCellDataWithNumberErrorInGridCellData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setHasInvalidLineBetweenGridInformationAndCell();
		gridMockCanReadFromStorageString();
		when(mGridCellMock.fromStorageString(anyString(), anyInt())).thenThrow(
				new NumberFormatException("** INVALID NUMBER IN CELL DATA **"));

		mGridLoader.load(solvingAttemptStub);
	}

	@Test
	public void load_SolvingAttemptGridCellLoadFailedDueToTooLittleCells_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		int tooLittleCells = mGridMockExpectedNumberOfCells - 1;
		gridCellMockCanReadGridCellsFromStorageString(tooLittleCells);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(tooLittleCells);

		assertThat(mGridLoader.load(solvingAttemptStub), is(false));
	}

	@Test
	public void load_SolvingAttemptGridCellLoadFailedDueToTooManyCells_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		int tooManyCells = mGridMockExpectedNumberOfCells + 1;
		gridCellMockCanReadGridCellsFromStorageString(tooManyCells);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(tooManyCells);

		assertThat(mGridLoader.load(solvingAttemptStub), is(false));
	}

	@Test
	public void load_SolvingAttemptGridCellsNotSucceededWithAnyOtherData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells);

		assertThat(mGridLoader.load(solvingAttemptStub), is(false));
	}

	@Test
	public void load_SolvingAttemptGridCellsSucceededWithUnexpectedData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setHasInvalidLineBetweenCagesAndCellChanges();

		assertThat(mGridLoader.load(solvingAttemptStub), is(false));
	}

	@Test
	public void load_SolvingAttemptDoesNotContainCellChanges_GridLoaded()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		mGridMockExpectedNumberOfCages = 1;
		gridCageMockLoadsStorageStringIntoGridStorageForGivenNumberOfCages(mGridMockExpectedNumberOfCages);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setNumberOfCages(mGridMockExpectedNumberOfCages);
		statisticsDatabaseAdapterCanLoadStatistics(true);

		assertThat("Grid loaded", mGridLoader.load(solvingAttemptStub),
				is(true));
		verifyGridLoaded();
	}

	@Test
	public void load_SolvingAttemptDoesContainCellChanges_GridLoaded()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		mGridMockExpectedNumberOfCages = 1;
		gridCageMockLoadsStorageStringIntoGridStorageForGivenNumberOfCages(mGridMockExpectedNumberOfCages);
		// Add a trailing element with value false to indicate a storage sting
		// which can not be loaded into a cell. This prevents the grid loader
		// from entering an endless loop.
		when(
				mCellChangeMock.fromStorageString(anyString(),
						any(ArrayList.class), anyInt())).thenReturn( //
				true, // At least 1 times
				false // Do NOT remove
				);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setNumberOfCages(mGridMockExpectedNumberOfCages);
		statisticsDatabaseAdapterCanLoadStatistics(true);

		assertThat("Grid loaded", mGridLoader.load(solvingAttemptStub),
				is(true));
		verifyGridLoaded();
	}

	private void verifyGridLoaded() {
		verify(mGridMock).setDateCreated(anyLong());
		verify(mGridMock).setDateLastSaved(anyLong());
		verify(mGridMock).checkUserMathForAllCages();
		verify(mGridCellMock, atLeast(1)).isSelected();
		verify(mGridMock).setSelectedCell(any(GridCell.class));
		verify(mGridCellMock, times(mGridMockExpectedNumberOfCells))
				.markDuplicateValuesInSameRowAndColumn();
		verify(mGridMock).setSolvingAttemptId(anyInt());
		verify(mGridMock).setRowId(anyInt());
		verify(mStatisticsDatabaseAdapterMock).getMostRecent(anyInt());
	}

	@Test
	public void load_SolvingAttemptInsertsStatisticsIfNotYetExists_GridLoaded()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		mGridMockExpectedNumberOfCages = 1;
		gridCageMockLoadsStorageStringIntoGridStorageForGivenNumberOfCages(mGridMockExpectedNumberOfCages);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setNumberOfCages(mGridMockExpectedNumberOfCages);
		statisticsDatabaseAdapterCanLoadStatistics(false);
		statisticsDatabaseAdapterCanInsertStatistics(true);

		assertThat("Grid loaded", mGridLoader.load(solvingAttemptStub),
				is(true));
	}

	@Test
	public void load_SolvingAttemptInsertStatisticsFailed_GridNotLoaded()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		mGridMockExpectedNumberOfCages = 1;
		gridCageMockLoadsStorageStringIntoGridStorageForGivenNumberOfCages(mGridMockExpectedNumberOfCages);
		SolvingAttemptStub solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setNumberOfCages(mGridMockExpectedNumberOfCages);
		statisticsDatabaseAdapterCanLoadStatistics(false);
		statisticsDatabaseAdapterCanInsertStatistics(false);

		assertThat("Grid loaded", mGridLoader.load(solvingAttemptStub),
				is(false));
	}

	@Test
	public void testLoadStatistics() throws Exception {

	}

	private void gridMockSetGridSize(int gridSize) {
		mGridMockSize = gridSize;
		when(mGridMock.getGridSize()).thenReturn(mGridMockSize);

		mGridMockExpectedNumberOfCells = mGridMockSize * mGridMockSize;
	}

	private void gridMockCanReadFromStorageString() {
		when(mGridStorageMock.fromStorageString(anyString(), anyInt()))
				.thenReturn(true);
	}

	private void gridCellMockCanReadGridCellsFromStorageString(int numberOfCells) {
		// Add a trailing element with value false to indicate a storage sting
		// which can not be loaded into a cell. This prevents the grid loader
		// from entering an endless loop.
		switch (numberOfCells) {
		case 3:
			when(mGridCellMock.fromStorageString(anyString(), anyInt()))
					.thenReturn( //
							true, true, true, // read 3 cells
							false // Do NOT remove
					);
			when(mGridCellMock.isSelected()).thenReturn(false, false, true);
			break;
		case 4:
			when(mGridCellMock.fromStorageString(anyString(), anyInt()))
					.thenReturn( //
							true, true, true, true, // read 4 cells
							false // Do NOT remove
					);
			when(mGridCellMock.isSelected()).thenReturn(false, false, true,
					false);
			break;
		case 5:
			when(mGridCellMock.fromStorageString(anyString(), anyInt()))
					.thenReturn( //
							true, true, true, true, true, // read 5 cells
							false // Do NOT remove
					);
			when(mGridCellMock.isSelected()).thenReturn(false, true, true,
					false, false);
			break;
		default:
			throw new InvalidParameterException(
					"Invalid value '"
							+ numberOfCells
							+ "' for helper method gridCellMockCanReadGridCellsFromStorageString");
		}
	}

	private void gridCageMockLoadsStorageStringIntoGridStorageForGivenNumberOfCages(
			int numberOfCages) {
		// Add a trailing element with value false to indicate a storage sting
		// which can not be loaded into a cage. This prevents the grid loader
		// from entering an endless loop.
		switch (numberOfCages) {
		case 1:
			when(
					mGridCageStorageMock.fromStorageString(anyString(),
							anyInt(), any(ArrayList.class))).thenReturn( //
					true, // read 1 cage
					false // Do NOT remove
					);
			break;
		case 2:
			when(
					mGridCageStorageMock.fromStorageString(anyString(),
							anyInt(), any(ArrayList.class))).thenReturn( //
					true, true, // Read 2 cages
					false // Do NOT remove
					);
			break;
		default:
			throw new InvalidParameterException(
					"Invalid value '"
							+ numberOfCages
							+ "' for helper method gridCageMockLoadsStorageStringIntoGridStorageForGivenNumberOfCages");
		}

	}

	private void statisticsDatabaseAdapterCanLoadStatistics(boolean loadSucceeds) {
		when(mStatisticsDatabaseAdapterMock.getMostRecent(anyInt()))
				.thenReturn((loadSucceeds ? mock(GridStatistics.class) : null));
	}

	private void statisticsDatabaseAdapterCanInsertStatistics(
			boolean insertSucceeds) {
		when(mStatisticsDatabaseAdapterMock.insert(any(Grid.class)))
				.thenReturn(
						(insertSucceeds ? mock(GridStatistics.class) : null));
	}
}
