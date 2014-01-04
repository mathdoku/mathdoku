package net.mathdoku.plus.grid;

import net.mathdoku.plus.statistics.GridStatistics;
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

	private class GridLoaderObjectsCreator extends GridLoader.ObjectsCreator {
		@Override
		public GridCell createGridCell(Grid grid, int cell) {
			return mGridCellMock;
		}

		@Override
		public GridCage createGridCage(Grid grid) {
			return mGridCageMock;
		}

		@Override
		public StatisticsDatabaseAdapter createStatisticsDatabaseAdapter() {
			return mStatisticsDatabaseAdapterMock;
		}
	}

	private class SolvingAttemptDataCreator extends SolvingAttemptData {
		private boolean mIncludeGridInformation = false;
		private boolean mIncludeInvalidLineBetweenGridInformationAndCell = false;
		private int mNumberOfCells = 0;
		private boolean mIncludeInvalidLineBetweenCellAndCages = false;
		private int mNumberOfCages = 0;
		private boolean mIncludeInvalidLineBetweenCagesAndCellChanges = false;
		private int mNumberOfCellChanges = 0;
		private boolean mIncludeInvalidLineAfterCellChanges = false;

		public SolvingAttemptDataCreator() {
			mSavedWithRevision = 596;
			buildData();
		}

		public SolvingAttemptDataCreator setHasInvalidLineBetweenGridInformationAndCell() {
			mIncludeInvalidLineBetweenGridInformationAndCell = true;
			buildData();

			return this;
		}

		public SolvingAttemptDataCreator setHasGeneralGridInformation() {
			mIncludeGridInformation = true;
			buildData();

			return this;
		}

		public SolvingAttemptDataCreator setNumberOfCells(int numberOfCells) {
			mNumberOfCells = numberOfCells;
			buildData();

			return this;
		}

		public SolvingAttemptDataCreator setHasInvalidLineBetweenCellAndCages() {
			mIncludeInvalidLineBetweenCellAndCages = true;
			buildData();

			return this;
		}

		public SolvingAttemptDataCreator setNumberOfCages(int numberOfCages) {
			mNumberOfCages = numberOfCages;
			buildData();

			return this;
		}

		public SolvingAttemptDataCreator setHasInvalidLineBetweenCagesAndCellChanges() {
			mIncludeInvalidLineBetweenCagesAndCellChanges = true;
			buildData();

			return this;
		}

		public SolvingAttemptDataCreator setNumberOfCellChanges(
				int numberOfCellChanges) {
			mNumberOfCellChanges = numberOfCellChanges;
			buildData();

			return this;
		}

		public SolvingAttemptDataCreator setHasInvalidLineAfterCellChanges() {
			mIncludeInvalidLineAfterCellChanges = true;
			buildData();

			return this;
		}

		private void buildData() {
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

			setData(stringBuilder.toString());
		}
	}

	@Before
	public void Setup() {
		mGridLoaderObjectsCreator = new GridLoaderObjectsCreator();
		mGridLoader = new GridLoader(mGridMock, mGridLoaderObjectsCreator);

		// Use a real array lists to store created cells, cages and cell changes
		// instead of mock.
		mGridMock.mCells = new ArrayList<GridCell>();
		mGridMock.mCages = new ArrayList<GridCage>();

	}

	public void load_SolvingAttemptDataNull_NotLoaded() throws Exception {
		SolvingAttemptData solvingAttemptData = null;
		assertThat("Grid load", mGridLoader.load(solvingAttemptData), is(false));
	}

	public void load_SolvingAttemptWithInvalidGridSize_NotLoaded()
			throws Exception {
		gridMockSetGridSize(0);
		SolvingAttemptData solvingAttemptData = mock(SolvingAttemptData.class);
		assertThat("Grid load", mGridLoader.load(solvingAttemptData), is(false));
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataWithNullData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptData solvingAttemptData = new SolvingAttemptData();
		solvingAttemptData.setData(null);

		mGridLoader.load(solvingAttemptData);
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataIsEmpty_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptData mSolvingAttemptDataMock = mock(SolvingAttemptData.class);
		when(mSolvingAttemptDataMock.getFirstLine()).thenReturn(null);

		mGridLoader.load(mSolvingAttemptDataMock);
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataWithWithInvalidGridData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasInvalidLineBetweenGridInformationAndCell();
		when(mGridMock.fromStorageString(anyString(), anyInt())).thenReturn(
				false);

		mGridLoader.load(solvingAttemptData);
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataMissingGridCellData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation();
		gridMockCanReadFromStorageString();

		mGridLoader.load(solvingAttemptData);
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataCellDataWithNumberErrorInGridCellData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setHasInvalidLineBetweenGridInformationAndCell();
		gridMockCanReadFromStorageString();
		when(mGridCellMock.fromStorageString(anyString(), anyInt())).thenThrow(
				new NumberFormatException("** INVALID NUMBER IN CELL DATA **"));

		mGridLoader.load(solvingAttemptData);
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataGridCellLoadFailedDueToTooLittleCells_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		int tooLittleCells = mGridMockExpectedNumberOfCells - 1;
		gridCellMockCanReadGridCellsFromStorageString(tooLittleCells);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(tooLittleCells);

		mGridLoader.load(solvingAttemptData);
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataGridCellLoadFailedDueToTooManyCells_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		int tooManyCells = mGridMockExpectedNumberOfCells + 1;
		gridCellMockCanReadGridCellsFromStorageString(tooManyCells);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(tooManyCells);

		mGridLoader.load(solvingAttemptData);
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataGridCellsNotSucceededWithAnyOtherData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells);

		mGridLoader.load(solvingAttemptData);
	}

	@Test(expected = InvalidGridException.class)
	public void load_SolvingAttemptDataGridCellsSucceededWithUnexpectedData_ThrowsInvalidGridException()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setHasInvalidLineBetweenCagesAndCellChanges();

		mGridLoader.load(solvingAttemptData);
	}

	@Test
	public void load_SolvingAttemptDataDoesNotContainCellChanges_GridLoaded()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		mGridMockExpectedNumberOfCages = 1;
		gridCageMockCanReadGridCageFromStorageString(mGridMockExpectedNumberOfCages);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setNumberOfCages(mGridMockExpectedNumberOfCages);
		statisticsDatabaseAdapterCanLoadStatistics(true);

		assertThat("Grid loaded", mGridLoader.load(solvingAttemptData),
				is(true));
		verifyGridLoaded();
	}

	@Test
	public void load_SolvingAttemptDataDoesContainCellChanges_GridLoaded()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		mGridMockExpectedNumberOfCages = 1;
		gridCageMockCanReadGridCageFromStorageString(mGridMockExpectedNumberOfCages);
		// Add a trailing element with value false to indicate a storage sting
		// which can not be loaded into a cell. This prevents the grid loader
		// from entering an endless loop.
		when(
				mCellChangeMock.fromStorageString(anyString(),
						any(ArrayList.class), anyInt())).thenReturn( //
				true, // At least 1 times
				false // Do NOT remove
				);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setNumberOfCages(mGridMockExpectedNumberOfCages);
		statisticsDatabaseAdapterCanLoadStatistics(true);

		assertThat("Grid loaded", mGridLoader.load(solvingAttemptData),
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
		gridCageMockCanReadGridCageFromStorageString(mGridMockExpectedNumberOfCages);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setNumberOfCages(mGridMockExpectedNumberOfCages);
		statisticsDatabaseAdapterCanLoadStatistics(false);
		statisticsDatabaseAdapterCanInsertStatistics(true);

		assertThat("Grid loaded", mGridLoader.load(solvingAttemptData),
				is(true));
	}

	@Test
	public void load_SolvingAttemptInsertStatisticsFailed_GridNotLoaded()
			throws Exception {
		gridMockSetGridSize(2);
		gridMockCanReadFromStorageString();
		gridCellMockCanReadGridCellsFromStorageString(mGridMockExpectedNumberOfCells);
		mGridMockExpectedNumberOfCages = 1;
		gridCageMockCanReadGridCageFromStorageString(mGridMockExpectedNumberOfCages);
		SolvingAttemptDataCreator solvingAttemptData = new SolvingAttemptDataCreator()
				.setHasGeneralGridInformation() //
				.setNumberOfCells(mGridMockExpectedNumberOfCells) //
				.setNumberOfCages(mGridMockExpectedNumberOfCages);
		statisticsDatabaseAdapterCanLoadStatistics(false);
		statisticsDatabaseAdapterCanInsertStatistics(false);

		assertThat("Grid loaded", mGridLoader.load(solvingAttemptData),
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
		when(mGridMock.fromStorageString(anyString(), anyInt())).thenReturn(
				true);
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

	private void gridCageMockCanReadGridCageFromStorageString(int numberOfCages) {
		// Add a trailing element with value false to indicate a storage sting
		// which can not be loaded into a cage. This prevents the grid loader
		// from entering an endless loop.
		switch (numberOfCages) {
		case 1:
			when(mGridCageMock.fromStorageString(anyString(), anyInt()))
					.thenReturn( //
							true, // read 1 cage
							false // Do NOT remove
					);
			break;
		case 2:
			when(mGridCageMock.fromStorageString(anyString(), anyInt()))
					.thenReturn( //
							true, true, // Read 2 cages
							false // Do NOT remove
					);
			break;
		default:
			throw new InvalidParameterException(
					"Invalid value '"
							+ numberOfCages
							+ "' for helper method gridCageMockCanReadGridCageFromStorageString");
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
