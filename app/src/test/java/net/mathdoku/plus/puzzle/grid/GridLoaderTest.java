package net.mathdoku.plus.puzzle.grid;

import android.app.Activity;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.CellBuilder;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.CellChangeStorage;
import net.mathdoku.plus.storage.CageStorage;
import net.mathdoku.plus.storage.CellStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.GridRow;
import net.mathdoku.plus.storage.database.SolvingAttempt;
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
			setStorageString();
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
			mGridLoaderTestObjectsCreator.setHasUnExpectedDataBeforeCells();
			setStorageString();

			return this;
		}

		public SolvingAttemptStub setHasGeneralGridInformation() {
			mIncludeGridInformation = true;
			setStorageString();

			return this;
		}

		public SolvingAttemptStub setNumberOfCells(int numberOfCells) {
			mNumberOfCells = numberOfCells;
			setStorageString();

			return this;
		}

		public SolvingAttemptStub setHasInvalidLineBetweenCellAndCages() {
			mIncludeInvalidLineBetweenCellAndCages = true;
			mGridLoaderTestObjectsCreator.setHasUnExpectedDataBeforeCages();
			setStorageString();

			return this;
		}

		public SolvingAttemptStub setNumberOfCages(int numberOfCages) {
			mNumberOfCages = numberOfCages;
			setStorageString();

			return this;
		}

		public SolvingAttemptStub setHasInvalidLineBetweenCagesAndCellChanges() {
			mIncludeInvalidLineBetweenCagesAndCellChanges = true;
			mGridLoaderTestObjectsCreator
					.setHasUnExpectedDataBeforeCellChanges();
			setStorageString();

			return this;
		}

		public SolvingAttemptStub setNumberOfCellChanges(int numberOfCellChanges) {
			mNumberOfCellChanges = numberOfCellChanges;
			setStorageString();

			return this;
		}

		public SolvingAttemptStub setHasInvalidLineAfterCellChanges() {
			mIncludeInvalidLineAfterCellChanges = true;
			setStorageString();

			return this;
		}

		private void setStorageString() {
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

			mStorageString = stringBuilder.toString();
		}

		public SolvingAttemptStub setNullData() {
			mStorageString = null;

			return this;
		}
	}

	public class GridLoaderTestObjectsCreator extends GridLoader.ObjectsCreator {
		private int mGridSize;
		private int mNumberOfCellMocksReturningAValidStorageString = 0;
		private int mNumberOfCageStorageMocksReturningAValidStorageString = 0;
		private int mNumberOfCellChangeStorageMocksReturningAValidStorageString = 0;
		private SolvingAttemptDatabaseAdapter mSolvingAttemptDatabaseAdapterMock = mock(SolvingAttemptDatabaseAdapter.class);
		private GridDatabaseAdapter mGridDatabaseAdapterMock = mock(GridDatabaseAdapter.class);
		private StatisticsDatabaseAdapter mStatisticsDatabaseAdapterMock = mock(StatisticsDatabaseAdapter.class);
		private GridStorage mGridStorageMock = mock(GridStorage.class);
		private int mCellNumberOnWhichAnNumberFormatExceptionIsThrown = -1;
		private boolean mHasUnExpectedDataBeforeCells = false;
		private boolean mHasUnExpectedDataBeforeCages = false;
		private boolean mHasUnExpectedDataBeforeCellChanges = false;
		private boolean mHasUnExpectedDataAfterCellChanges = false;
		private GridBuilder mGridBuilderMock = new GridBuilder() {
			@Override
			public Grid build() {
				/*
				 * As the stub does not contain real data, building the grid
				 * would always fail. As unit testing of the Grid constructor is
				 * not inside the scope of these unit tests, a mock is returned
				 * whenever the build is called.
				 */
				return mock(Grid.class);
			}
		};
		private Cage mCageMock = mock(Cage.class);

		private void setGridSize(int gridSize) {
			mGridSize = gridSize;
		}

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

		public void setHasUnExpectedDataBeforeCells() {
			mHasUnExpectedDataBeforeCells = true;
		}

		public void setHasUnExpectedDataBeforeCages() {
			mHasUnExpectedDataBeforeCages = true;
		}

		public void setHasUnExpectedDataBeforeCellChanges() {
			mHasUnExpectedDataBeforeCellChanges = true;
		}

		/**
		 * Initializes the GridObjectsCreatorStub to return a grid cell mock
		 * which returns a valid storage string for the given number of times.
		 * In case more objects than the given number are created, the
		 * corresponding grid cell mocks will return an invalid storage string.
		 * 
		 * @param numberOfCellMocksReturningAValidStorageString
		 */
		public void setNumberOfCellMocksReturningAValidStorageString(
				int numberOfCellMocksReturningAValidStorageString) {
			mNumberOfCellMocksReturningAValidStorageString = numberOfCellMocksReturningAValidStorageString;
		}

		/**
		 * Initializes the GridObjectsCreatorStub to return a grid cage mock
		 * which returns a valid storage string for the given number of times.
		 * In case more objects than the given number are created, the
		 * corresponding grid cage mocks will return an invalid storage string.
		 * 
		 * @param numberOfCageStorageMocksReturningAValidStorageString
		 */
		public void setNumberOfCageStorageMocksReturningAValidStorageString(
				int numberOfCageStorageMocksReturningAValidStorageString) {
			mNumberOfCageStorageMocksReturningAValidStorageString = numberOfCageStorageMocksReturningAValidStorageString;
		}

		/**
		 * Initializes the GridObjectsCreatorStub to return a cell change mock
		 * which returns a valid storage string for the given number of times.
		 * In case more objects than the given number are created, the
		 * corresponding cell change mocks will return an invalid storage
		 * string.
		 * 
		 * @param numberOfCellChangeMocksReturningAValidStorageString
		 */
		public void setNumberOfCellChangeMocksReturningAValidStorageString(
				int numberOfCellChangeMocksReturningAValidStorageString) {
			mNumberOfCellChangeStorageMocksReturningAValidStorageString = numberOfCellChangeMocksReturningAValidStorageString;
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

		public GridBuilder getGridBuilder() {
			return mGridBuilderMock;
		}

		@Override
		public CellStorage createCellStorage() {
			CellStorage cellStorage = mock(CellStorage.class);

			// Determine whether this mock should return a valid or invalid
			// storage string.
			boolean validStorageString = !mHasUnExpectedDataBeforeCells && mNumberOfCellMocksReturningAValidStorageString > 0;
			if (mHasUnExpectedDataBeforeCells) {
				mHasUnExpectedDataBeforeCells = false;
			} else {
				mNumberOfCellMocksReturningAValidStorageString--;
			}
			if (validStorageString) {
				CellBuilder cellBuilder = validStorageString ? mock(CellBuilder.class)
						: null;
				when(
						cellStorage.getCellBuilderFromStorageString(
								anyString(), anyInt())).thenReturn(
						validStorageString ? cellBuilder : null);
				when(cellBuilder.getGridSize()).thenReturn(mGridSize);
				when(cellBuilder.getCorrectValue()).thenReturn(1);

			} else {
				when(
						cellStorage.getCellBuilderFromStorageString(
								anyString(), anyInt())).thenReturn(null);
			}

			// Check if a InvalidNumberException will be thrown for this cell
			if (mCellNumberOnWhichAnNumberFormatExceptionIsThrown >= 0) {
				if (mCellNumberOnWhichAnNumberFormatExceptionIsThrown == 0) {
					when(
							cellStorage.getCellBuilderFromStorageString(
									anyString(), anyInt())).thenThrow(
							new NumberFormatException(
									"** INVALID NUMBER IN CELL DATA " + "**"));
				}
				mCellNumberOnWhichAnNumberFormatExceptionIsThrown--;
			}

			return cellStorage;
		}

		@Override
		public CageStorage createCageStorage() {
			CageStorage cageStorage = mock(CageStorage.class);

			// Determine what result will be returned when
			// getCageBuilderFromStorageString is called.
			boolean isValidStorageString = !mHasUnExpectedDataBeforeCages && mNumberOfCageStorageMocksReturningAValidStorageString > 0;
			if (mHasUnExpectedDataBeforeCages) {
				mHasUnExpectedDataBeforeCages = false;
			} else {
				mNumberOfCageStorageMocksReturningAValidStorageString--;
			}
			when(
					cageStorage.getCageBuilderFromStorageString(anyString(),
							anyInt(), any(ArrayList.class))).thenReturn(
					isValidStorageString ? mock(CageBuilder.class) : null);

			return cageStorage;
		}

		@Override
		public CellChangeStorage createCellChangeStorage() {
			CellChangeStorage cellChangeStorage = mock(CellChangeStorage.class);

			// Determine whether this mock should return a valid or invalid
			// storage string.
			boolean validStorageString = !mHasUnExpectedDataBeforeCellChanges && mNumberOfCellChangeStorageMocksReturningAValidStorageString > 0;
			if (mHasUnExpectedDataBeforeCellChanges) {
				mHasUnExpectedDataBeforeCellChanges = false;
			} else {
				mNumberOfCellChangeStorageMocksReturningAValidStorageString--;
			}
			when(
					cellChangeStorage.fromStorageString(anyString(),
							any(ArrayList.class), anyInt())).thenReturn(
					validStorageString);

			return cellChangeStorage;
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
		public Cage createCage(CageBuilder cageBuilder) {
			return mCageMock;
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
		public GridBuilder createGridBuilder() {
			return mGridBuilderMock;
		}
	}

	private GridLoaderTestObjectsCreator mGridLoaderTestObjectsCreator;

	@Before
	public void Setup() {
		// Instantiate Singleton
		Preferences.getInstance(new Activity());

		mGridLoader = new GridLoader();
		mGridLoaderTestObjectsCreator = new GridLoaderTestObjectsCreator();
		mGridLoader.setObjectsCreator(mGridLoaderTestObjectsCreator);

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
		mGridLoaderTestObjectsCreator.returnsSolvingAttempt(null);

		int mSolvingAttemptId = 1;
		assertThat("Grid load", mGridLoader.load(mSolvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_GridRowNull_NotLoaded() throws Exception {
		mGridLoaderTestObjectsCreator.returnsGridRow(null);

		int mSolvingAttemptId = 1;
		assertThat("Grid load", mGridLoader.load(mSolvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptWithInvalidGridSize_NotLoaded()
			throws Exception {
		GridRow gridRow = mock(GridRow.class);
		gridRow.mGridSize = 0;
		mGridLoaderTestObjectsCreator.returnsGridRow(gridRow);

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
		mGridLoaderTestObjectsCreator
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
	public void load_SolvingAttemptMissingCellData_GridNotLoaded()
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
	public void load_SolvingAttemptCellDataWithNumberErrorInCellData_GridNotLoaded()
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
		mGridLoaderTestObjectsCreator
				.setCellNumberOnWhichAnNumberFormatExceptionIsThrown(numberOfCells / 2);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptCellLoadFailedDueToTooLittleCells_GridNotLoaded()
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
	public void load_SolvingAttemptCellLoadFailedDueToTooManyCells_GridNotLoaded()
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
	public void load_SolvingAttemptCellsNotSucceededWithAnyOtherData_GridNotLoaded()
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
	public void load_SolvingAttemptCellsSucceededWithUnexpectedData_GridNotLoaded()
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
		mGridLoader.setThrowExceptionOnError(true);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(notNullValue()));
		GridBuilder gridBuilder = mGridLoaderTestObjectsCreator
				.getGridBuilder();
		assertThat("Grid has number of cell changes",
				gridBuilder.mCellChanges.size(), is(numberOfCellChanges));
	}

	@Test
	public void load_SolvingAttemptCagesSucceededWithUnexpectedData_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages)
				.setHasInvalidLineBetweenCagesAndCellChanges()
				.setNumberOfCellChanges(numberOfCellChanges);
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptCellChangesSucceededWithUnexpectedData_GridNotLoaded()
			throws Exception {
		int solvingAttemptId = 56;
		int gridSize = 4;
		int numberOfCells = gridSize * gridSize;
		int numberOfCages = 5;
		int numberOfCellChanges = 12;

		SolvingAttempt solvingAttemptStub = new SolvingAttemptStub()
				.setHasGeneralGridInformation()
				.setNumberOfCells(numberOfCells)
				.setNumberOfCages(numberOfCages)
				.setNumberOfCellChanges(numberOfCellChanges)
				.setHasInvalidLineAfterCellChanges();
		setupForParsingSolvingAttemptData(gridSize, numberOfCells,
				numberOfCages, numberOfCellChanges, solvingAttemptStub);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(nullValue()));
	}

	@Test
	public void load_SolvingAttemptRetrievedFromDatabase_DataCopiedToGridLoaderData()
			throws Exception {
		int solvingAttemptId = 134;
		int mGridId = 1235;
		long dateCreated = 123456789;
		long dateUpdated = 123457777;
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

		mGridLoaderTestObjectsCreator.returnsSolvingAttempt(solvingAttemptStub);
		GridRow gridRow = mock(GridRow.class);
		gridRow.mGridSize = gridSize;
		gridRow.mGridGeneratingParameters = mock(GridGeneratingParameters.class);
		mGridLoaderTestObjectsCreator.returnsGridRow(gridRow);
		mGridLoaderTestObjectsCreator.setGridMockReturningAValidStorageString();
		mGridLoaderTestObjectsCreator.setGridMockIsActive(isActive);
		mGridLoaderTestObjectsCreator.setGridMockIsRevealed(isRevealed);
		mGridLoaderTestObjectsCreator
				.setNumberOfCellMocksReturningAValidStorageString(numberOfCells);
		mGridLoaderTestObjectsCreator
				.setNumberOfCageStorageMocksReturningAValidStorageString(numberOfCages);
		mGridLoaderTestObjectsCreator
				.setNumberOfCellChangeMocksReturningAValidStorageString(numberOfCellChanges);
		GridStatistics gridStatistics = mock(GridStatistics.class);
		mGridLoaderTestObjectsCreator.returnsGridStatistics(gridStatistics);
		mGridLoader.setThrowExceptionOnError(true);

		assertThat("Grid load", mGridLoader.load(solvingAttemptId),
				is(notNullValue()));
		GridBuilder gridBuilder = mGridLoaderTestObjectsCreator
				.getGridBuilder();
		assertThat("Grid has size", gridBuilder.mGridSize, is(gridSize));
		assertThat("Grid has generating parameters",
				gridBuilder.mGridGeneratingParameters,
				is(sameInstance(gridRow.mGridGeneratingParameters)));
		assertThat("Grid has statistics", gridBuilder.mGridStatistics,
				is(sameInstance(gridStatistics)));
		assertThat("Grid has date created", gridBuilder.mDateCreated,
				is(dateCreated));
		assertThat("Grid has date updated", gridBuilder.mDateUpdated,
				is(dateUpdated));
		assertThat("Grid has solving attempt id",
				gridBuilder.mSolvingAttemptId, is(solvingAttemptId));
		assertThat("Grid has number of cells", gridBuilder.mCells.size(),
				is(numberOfCells));
		assertThat("Grid has number of cages", gridBuilder.mCages.size(),
				is(numberOfCages));
		assertThat("Grid has number of cell changes",
				gridBuilder.mCellChanges.size(), is(numberOfCellChanges));
		assertThat("Grid is active", gridBuilder.mActive, is(isActive));
		assertThat("Grid is revealed", gridBuilder.mRevealed, is(isRevealed));
	}

	@Test
	public void load_StatisticsNotLoaded_GridNotLoaded() throws Exception {
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
		mGridLoaderTestObjectsCreator.returnsGridStatistics(null);

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
		mGridLoaderTestObjectsCreator.returnsGridRow(gridRow);
		mGridLoaderTestObjectsCreator.setGridMockReturningAValidStorageString();
		mGridLoaderTestObjectsCreator.setGridMockIsActive(isActive);
		mGridLoaderTestObjectsCreator.setGridMockIsRevealed(isRevealed);
		mGridLoaderTestObjectsCreator.setGridSize(gridSize);
		mGridLoaderTestObjectsCreator
				.setNumberOfCellMocksReturningAValidStorageString(numberOfCells);
		mGridLoaderTestObjectsCreator
				.setNumberOfCageStorageMocksReturningAValidStorageString(numberOfCages);
		mGridLoaderTestObjectsCreator
				.setNumberOfCellChangeMocksReturningAValidStorageString(numberOfCellChanges);
		mGridLoaderTestObjectsCreator.returnsSolvingAttempt(solvingAttempt);
		GridStatistics gridStatistics = mock(GridStatistics.class);
		mGridLoaderTestObjectsCreator.returnsGridStatistics(gridStatistics);
	}
}
