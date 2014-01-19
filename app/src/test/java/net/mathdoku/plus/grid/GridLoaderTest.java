package net.mathdoku.plus.grid;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.GridCellStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttempt;
import net.mathdoku.plus.storage.database.SolvingAttemptData;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridLoaderTest {
	private GridLoader mGridLoader;

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
			mId = 1;
			mGridId = 1;
			mDateCreated = 12345678;
			mDateUpdated = 123456789;
			mSavedWithRevision = 596;
			setData();
		}

		public SolvingAttemptStub setId(int id) {
			mId = id;

			return this;
		}

		public SolvingAttemptStub setGridId(int gridId) {
			mGridId = gridId;

			return this;
		}

		public SolvingAttemptStub setDateCreated(long dateCreated) {
			mDateCreated = dateCreated;

			return this;
		}

		public SolvingAttemptStub setDateUpdated(long dateUpdated) {
			mDateUpdated = dateUpdated;

			return this;
		}

		public SolvingAttemptStub setSavedWithRevision(int savedWithRevision) {
			mSavedWithRevision = savedWithRevision;

			return this;
		}

		public SolvingAttemptStub setHasInvalidLineBetweenGridInformationAndCell() {
			mIncludeInvalidLineBetweenGridInformationAndCell = true;
			mGridLoaderObjectsCreatorStub.setHasUnExpectedDataBeforeGridCells();
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
			mGridLoaderObjectsCreatorStub.setHasUnExpectedDataBeforeGridCages();
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

		public SolvingAttemptStub setNullData() {
			mData = null;

			return this;
		}
	}

	private class GridLoaderObjectsCreatorStub extends GridLoaderObjectsCreator {
		private int mNumberOfGridCellMocksReturningAValidStorageString = 0;
		private int mNumberOfGridCageStorageMocksReturningAValidStorageString = 0;
		private int mNumberOfCellChangeMocksReturningAValidStorageString = 0;
		private SolvingAttemptDatabaseAdapter mSolvingAttemptDatabaseAdapterMock = mock(SolvingAttemptDatabaseAdapter.class);
		private GridDatabaseAdapter mGridDatabaseAdapterMock = mock(GridDatabaseAdapter.class);
		private StatisticsDatabaseAdapter mStatisticsDatabaseAdapterMock = mock(StatisticsDatabaseAdapter.class);
		private GridStorage mGridStorageMock = mock(GridStorage.class);
		private Grid mGridMock = mock(Grid.class);
		private int mCellNumberOnWhichAnNumberFormatExceptionIsThrown = -1;
		private boolean mHasUnExpectedDataBeforeGridCells = false;
		private boolean mHasUnExpectedDataBeforeGridCages = false;

		private GridLoaderData mGridLoaderData = new GridLoaderData();

		public void setGridMockReturningAValidStorageString() {
			when(mGridStorageMock.fromStorageString(anyString(), anyInt()))
					.thenReturn(true);
		}

		public void setGridMockReturningAnInvalidStorageString() {
			when(mGridStorageMock.fromStorageString(anyString(), anyInt()))
					.thenReturn(false);
		}

		public void setGridMockIsActive(boolean isActive) {
			when(mGridStorageMock.isActive()).thenReturn(isActive);
		}

		public void setGridMockIsRevealed(boolean isRevealed) {
			when(mGridStorageMock.isRevealed()).thenReturn(isRevealed);
		}

		public void setCellNumberOnWhichAnNumberFormatExceptionIsThrown(
				int cellNumberOnWhichAnInvalidNumberExceptionIsThrown) {
			mCellNumberOnWhichAnNumberFormatExceptionIsThrown = cellNumberOnWhichAnInvalidNumberExceptionIsThrown;
		}

		public void setHasUnExpectedDataBeforeGridCells() {
			mHasUnExpectedDataBeforeGridCells = true;
		}

		public void setHasUnExpectedDataBeforeGridCages() {
			mHasUnExpectedDataBeforeGridCages = true;
		}

		/**
		 * Initializes the GridLoaderObjectsCreatorStub to return a grid cell
		 * mock which returns a valid storage string for the given number of
		 * times. In case more objects than the given number are created, the
		 * corresponding grid cell mocks will return an invalid storage string.
		 * 
		 * @param numberOfGridCellMocksReturningAValidStorageString
		 */
		public void setNumberOfGridCellMocksReturningAValidStorageString(
				int numberOfGridCellMocksReturningAValidStorageString) {
			mNumberOfGridCellMocksReturningAValidStorageString = numberOfGridCellMocksReturningAValidStorageString;
		}

		/**
		 * Initializes the GridLoaderObjectsCreatorStub to return a grid cage
		 * mock which returns a valid storage string for the given number of
		 * times. In case more objects than the given number are created, the
		 * corresponding grid cage mocks will return an invalid storage string.
		 * 
		 * @param numberOfGridCageStorageMocksReturningAValidStorageString
		 */
		public void setNumberOfGridCageStorageMocksReturningAValidStorageString(
				int numberOfGridCageStorageMocksReturningAValidStorageString) {
			mNumberOfGridCageStorageMocksReturningAValidStorageString = numberOfGridCageStorageMocksReturningAValidStorageString;
		}

		/**
		 * Initializes the GridLoaderObjectsCreatorStub to return a cell change
		 * mock which returns a valid storage string for the given number of
		 * times. In case more objects than the given number are created, the
		 * corresponding cell change mocks will return an invalid storage
		 * string.
		 * 
		 * @param numberOfCellChangeMocksReturningAValidStorageString
		 */
		public void setNumberOfGridCellChangeMocksReturningAValidStorageString(
				int numberOfCellChangeMocksReturningAValidStorageString) {
			mNumberOfCellChangeMocksReturningAValidStorageString = numberOfCellChangeMocksReturningAValidStorageString;
		}

		public void returnsSolvingAttempt(SolvingAttempt solvingAttempt) {
			when(mSolvingAttemptDatabaseAdapterMock.getData(anyInt()))
					.thenReturn(solvingAttempt);
		}

		public void returnsGridRow(GridRow gridRow) {
			when(mGridDatabaseAdapterMock.get(anyInt())).thenReturn(gridRow);
		}

		public void returnsGridStatistics(GridStatistics gridStatistics) {
			when(mStatisticsDatabaseAdapterMock.getMostRecent(anyInt()))
					.thenReturn(gridStatistics);
		}

		public SolvingAttemptDatabaseAdapter getSolvingAttemptDatabaseAdapter() {
			return mSolvingAttemptDatabaseAdapterMock;
		}

		public GridDatabaseAdapter getGridDatabaseAdapterMock() {
			return mGridDatabaseAdapterMock;
		}

		public GridLoaderData getGridLoaderData() {
			return mGridLoaderData;
		}

		@Override
		public GridCellStorage createGridCellStorage() {
			GridCellStorage gridCellStorage = mock(GridCellStorage.class);

			// Determine whether this mock should return a valid or invalid
			// storage string.
			boolean validStorageString = (mHasUnExpectedDataBeforeGridCells == false && mNumberOfGridCellMocksReturningAValidStorageString > 0);
			if (mHasUnExpectedDataBeforeGridCells) {
				mHasUnExpectedDataBeforeGridCells = false;
			} else {
				mNumberOfGridCellMocksReturningAValidStorageString--;
			}
			when(gridCellStorage.fromStorageString(anyString(), anyInt())).thenReturn(validStorageString);

			// Check if a InvalidNumberException will be thrown for this cell
			if (mCellNumberOnWhichAnNumberFormatExceptionIsThrown >= 0) {
				if (mCellNumberOnWhichAnNumberFormatExceptionIsThrown == 0) {
					when(gridCellStorage.fromStorageString(anyString(), anyInt()))
							.thenThrow(new NumberFormatException(
									"** INVALID NUMBER IN CELL DATA " + "**"));
				}
				mCellNumberOnWhichAnNumberFormatExceptionIsThrown--;
			}

			return gridCellStorage;
		}

		@Override
		public GridCageStorage createGridCageStorage() {
			GridCageStorage gridCageStorage = mock(GridCageStorage.class);

			// Determine whether this mock should return a valid or invalid
			// storage string.
			boolean validStorageString = (mHasUnExpectedDataBeforeGridCages == false && mNumberOfGridCageStorageMocksReturningAValidStorageString > 0);
			if (mHasUnExpectedDataBeforeGridCages) {
				mHasUnExpectedDataBeforeGridCages = false;
			} else {
				mNumberOfGridCageStorageMocksReturningAValidStorageString--;
			}
			when(
					gridCageStorage.fromStorageString(anyString(), anyInt(),
							any(ArrayList.class))).thenReturn(
					validStorageString);

			return gridCageStorage;
		}

		@Override
		public CellChange createCellChange() {
			CellChange cellChange = mock(CellChange.class);

			// Determine whether this mock should return a valid or invalid
			// storage string.
			when(
					cellChange.fromStorageString(anyString(),
							any(ArrayList.class), anyInt())).thenReturn(
					mNumberOfCellChangeMocksReturningAValidStorageString > 0);
			mNumberOfCellChangeMocksReturningAValidStorageString--;

			return cellChange;
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
		public SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
			return mSolvingAttemptDatabaseAdapterMock;
		}

		@Override
		public GridDatabaseAdapter createGridDatabaseAdapter() {
			return mGridDatabaseAdapterMock;
		}

		@Override
		public GridLoaderData createGridLoaderData() {
			return mGridLoaderData;
		}

		@Override
		public Grid createGrid(GridLoaderData gridLoaderData) {
			return mGridMock;
		}
	}

	private GridLoaderObjectsCreatorStub mGridLoaderObjectsCreatorStub;

	@Before
	public void Setup() {
		mGridLoader = new GridLoader();
		mGridLoaderObjectsCreatorStub = new GridLoaderObjectsCreatorStub();
		mGridLoader.setObjectsCreator(mGridLoaderObjectsCreatorStub);

		// Even when running the unit test in the debug variant, the grid loader
		// should not throw development exceptions as the tests below only test
		// the release variant in which no such exceptions are thrown.
		if (Config.mAppMode == Config.AppMode.DEVELOPMENT) {
			// Disable this until all unit tests succeed in development mode!
			mGridLoader.setThrowExceptionOnError(false);
		}
	}

	@Test
	public void load_SolvingAttemptNull_NotLoaded() throws Exception {
		mGridLoaderObjectsCreatorStub.returnsSolvingAttempt(null);

		int mSolvingAttemptId = 1;
		assertThat("Grid load", mGridLoader.load(mSolvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_GridRowNull_NotLoaded() throws Exception {
		mGridLoaderObjectsCreatorStub.returnsGridRow(null);

		int mSolvingAttemptId = 1;
		assertThat("Grid load", mGridLoader.load(mSolvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptWithInvalidGridSize_NotLoaded()
			throws Exception {
		GridRow gridRow = mock(GridRow.class);
		gridRow.mGridSize = 0;
		mGridLoaderObjectsCreatorStub.returnsGridRow(gridRow);

		int mSolvingAttemptId = 1;
		assertThat("Grid load", mGridLoader.load(mSolvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptWithNullData_GridNotLoaded()
			throws Exception {
		int mSolvingAttemptId = 1;
		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setNullData();

		assertThat("Grid load", mGridLoader.load(mSolvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptWithWithInvalidGridData_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
		// Missing general info
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);
		mGridLoaderObjectsCreatorStub
				.setGridMockReturningAnInvalidStorageString();

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptGridInformationIsSucceededWithUnexpectedData_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = 0;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setHasInvalidLineBetweenGridInformationAndCell()
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptMissingGridCellData_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = 0;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptCellDataWithNumberErrorInGridCellData_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);
		// About half way of the cells throw an invalid number exception
		mGridLoaderObjectsCreatorStub
				.setCellNumberOnWhichAnNumberFormatExceptionIsThrown(numberOfCells / 2);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptGridCellLoadFailedDueToTooLittleCells_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int tooLittleCells = numberOfCells - 1;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(tooLittleCells)
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, tooLittleCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptGridCellLoadFailedDueToTooManyCells_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int tooManyCells = numberOfCells + 1;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(tooManyCells)
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, tooManyCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptGridCellsNotSucceededWithAnyOtherData_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 0;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptGridCellsSucceededWithUnexpectedData_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setHasInvalidLineBetweenCellAndCages()
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptDoesNotContainCellChanges_GridLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 5;
		int numberOfCellChanges = 0;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages)
				.setNumberOfCellChanges(numberOfCellChanges);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(notNullValue()));
		GridLoaderData gridLoaderData = mGridLoaderObjectsCreatorStub
				.getGridLoaderData();
		assertThat("Grid has number of cell changes",
				gridLoaderData.mCellChanges.size(), is(numberOfCellChanges));
	}

	@Test
	public void load_SolvingAttemptRetrievedFromDatabase_DataCopiedToGridLoaderData()
			throws Exception {
		int solvingAttemptId = 134;
		int mGridId = 1235;
		long dateCreated = 123456789;
		long dateUpdated = 123455555;
		int mSavedWithRevision = 597;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;
		boolean isActive = true;
		boolean isRevealed = false;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setId(solvingAttemptId)
				.setGridId(mGridId)
				.setDateCreated(dateCreated)
				.setDateUpdated(dateUpdated)
				.setSavedWithRevision(mSavedWithRevision)
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages)
				.setNumberOfCellChanges(numberOfCellChanges);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		mGridLoaderObjectsCreatorStub.returnsSolvingAttempt(solvingAttemptStub);
		GridRow gridRow = mock(GridRow.class);
		gridRow.mGridSize = gridSize;
		gridRow.mGridGeneratingParameters = mock(GridGeneratingParameters.class);
		mGridLoaderObjectsCreatorStub.returnsGridRow(gridRow);
		mGridLoaderObjectsCreatorStub.setGridMockReturningAValidStorageString();
		mGridLoaderObjectsCreatorStub.setGridMockIsActive(isActive);
		mGridLoaderObjectsCreatorStub.setGridMockIsRevealed(isRevealed);
		mGridLoaderObjectsCreatorStub
				.setNumberOfGridCellMocksReturningAValidStorageString(numberOfCells);
		mGridLoaderObjectsCreatorStub
				.setNumberOfGridCageStorageMocksReturningAValidStorageString(numberOfCages);
		mGridLoaderObjectsCreatorStub
				.setNumberOfGridCellChangeMocksReturningAValidStorageString(numberOfCellChanges);
		GridStatistics gridStatistics = mock(GridStatistics.class);
		mGridLoaderObjectsCreatorStub.returnsGridStatistics(gridStatistics);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(notNullValue()));
		GridLoaderData gridLoaderData = mGridLoaderObjectsCreatorStub
				.getGridLoaderData();
		assertThat("Grid has size", gridLoaderData.mGridSize, is(gridSize));
		assertThat("Grid has generating parameters",
				gridLoaderData.mGridGeneratingParameters,
				is(sameInstance(gridRow.mGridGeneratingParameters)));
		assertThat("Grid has statistics", gridLoaderData.mGridStatistics,
				is(sameInstance(gridStatistics)));
		assertThat("Grid has date created", gridLoaderData.mDateCreated,
				is(dateCreated));
		assertThat("Grid has date updated", gridLoaderData.mDateUpdated,
				is(dateUpdated));
		assertThat("Grid has solving attempt id",
				gridLoaderData.mSolvingAttemptId, is(solvingAttemptId));
		assertThat("Grid has number of cells", gridLoaderData.mCells.size(),
				is(numberOfCells));
		assertThat("Grid has number of cages", gridLoaderData.mCages.size(),
				is(numberOfCages));
		assertThat("Grid has number of cell changes",
				gridLoaderData.mCellChanges.size(), is(numberOfCellChanges));
		assertThat("Grid is active", gridLoaderData.mActive, is(isActive));
		assertThat("Grid is revealed", gridLoaderData.mRevealed, is(isRevealed));
	}

	@Test
	public void load_StatisticsNotLoaded_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setHasInvalidLineBetweenCellAndCages()
				.setNumberOfCages(numberOfCages);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);
		mGridLoaderObjectsCreatorStub.returnsGridStatistics(null);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	/**
	 * Setup the mocks needed to test the loading of solving attempt data.
	 */
	private void setupForParsingSolvingAttemptData(int gridSize,
			int numberOfCells, int numberOfCages, int numberOfCellChanges,
			SolvingAttempt solvingAttempt) {
		boolean isActive = true;
		boolean isRevealed = false;

		// Beware: not all mock methods defined below will be invoked in each
		// test!
		GridRow gridRow = mock(GridRow.class);
		gridRow.mGridSize = gridSize;
		gridRow.mGridGeneratingParameters = mock(GridGeneratingParameters.class);
		mGridLoaderObjectsCreatorStub.returnsGridRow(gridRow);
		mGridLoaderObjectsCreatorStub.setGridMockReturningAValidStorageString();
		mGridLoaderObjectsCreatorStub.setGridMockIsActive(isActive);
		mGridLoaderObjectsCreatorStub.setGridMockIsRevealed(isRevealed);
		mGridLoaderObjectsCreatorStub
				.setNumberOfGridCellMocksReturningAValidStorageString(numberOfCells);
		mGridLoaderObjectsCreatorStub
				.setNumberOfGridCageStorageMocksReturningAValidStorageString(numberOfCages);
		mGridLoaderObjectsCreatorStub
				.setNumberOfGridCellChangeMocksReturningAValidStorageString(numberOfCellChanges);
		mGridLoaderObjectsCreatorStub.returnsSolvingAttempt(solvingAttempt);
		GridStatistics gridStatistics = mock(GridStatistics.class);
		mGridLoaderObjectsCreatorStub.returnsGridStatistics(gridStatistics);
	}

}
