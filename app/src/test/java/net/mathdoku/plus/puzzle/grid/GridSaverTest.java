package net.mathdoku.plus.puzzle.grid;

import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.puzzle.cellchange.CellChange;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.GridRow;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptRow;
import net.mathdoku.plus.storage.databaseadapter.StatisticsDatabaseAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridSaverTest {
	private GridSaver mGridSaver;
	private Grid mGridMock = mock(Grid.class);
	private GridStatistics mGridStatisticsMock = mock(GridStatistics.class);
	private GridGeneratingParameters mGridGeneratingParametersMock = mock(GridGeneratingParameters.class);
	private GridDatabaseAdapter mGridDatabaseAdapterMock = mock(GridDatabaseAdapter.class);
	private DatabaseHelper mDatabaseHelperMock = mock(DatabaseHelper.class);
	private SolvingAttemptDatabaseAdapter mSolvingAttemptDatabaseAdapterMock = mock(SolvingAttemptDatabaseAdapter.class);
	private StatisticsDatabaseAdapter mStatisticsDatabaseAdapterMock = mock(StatisticsDatabaseAdapter.class);

	private GridSaverTestObjectsCreator mGridSaverTestGridSaverTestObjectsCreator;

	private class GridSaverTestObjectsCreator extends GridSaver.ObjectsCreator {
		// Unreveal the array list of cell changes as it is hidden in the Grid
		// Object.
		public List<CellChange> mArrayListOfCellChanges = null;

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
	public void setup() {
		TestRunnerHelper.setup(this.getClass().getCanonicalName());

		// Initialize the grid mock as if it is a new Grid which has not
		// been saved.
		when(mGridMock.getRowId()).thenReturn(-1);
		when(mGridMock.getSolvingAttemptId()).thenReturn(-1);
		mGridStatisticsMock.mId = -1;
		when(mGridMock.getGridStatistics()).thenReturn(mGridStatisticsMock);
		long dateCreated = new java.util.GregorianCalendar(2014,
				Calendar.FEBRUARY, 8, 7, 6).getTimeInMillis(); // 2014, february
																// 8th at 07:06
		when(mGridMock.getDateCreated()).thenReturn(dateCreated);
		when(mGridMock.getDateSaved()).thenReturn(dateCreated);

		mGridSaverTestGridSaverTestObjectsCreator = new GridSaverTestObjectsCreator();
		mGridSaver = new GridSaver()
				.setObjectsCreator(mGridSaverTestGridSaverTestObjectsCreator);
	}

	@After
	public void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	@Test
	public void save_InsertGridWithUniqueGridDefinition_Saved()
			throws Exception {
		// Prepare for not finding an existing grid row with the same definition
		when(mGridDatabaseAdapterMock.getByGridDefinition(anyString()))
				.thenReturn(null);
		// Prepare for insert of new grid row
		int mGridRowIdAfterSuccessfulInsert = 34;
		when(mGridDatabaseAdapterMock.insert(any(Grid.class))).thenReturn(
				mGridRowIdAfterSuccessfulInsert);
		// Prepare insert of new solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.insert(any(SolvingAttemptRow.class))).thenReturn(
				mock(SolvingAttemptRow.class));
		// Prepare insert of new statistics
		int mGridStatisticsIdAfterSuccessfulInsert = 52;
		when(mStatisticsDatabaseAdapterMock.insert(any(GridStatistics.class)))
				.thenReturn(mGridStatisticsIdAfterSuccessfulInsert);

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock)
				.insert(any(GridStatistics.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(mGridSaver.getRowId() >= 0, is(true));
		assertThat(mGridSaver.getSolvingAttemptId() >= 0, is(true));
		assertThat(mGridSaver.getGridStatistics().mId >= 0, is(true));
		assertThat(mGridSaver.getDateUpdated(), is(mGridMock.getDateSaved()));
		assertThat(saveResult, is(true));
	}

	@Test
	public void save_InsertGridWithUniqueGridDefinitionButInsertGridFails_NotSaved()
			throws Exception {
		// Prepare for not finding an existing grid row with the same definition
		when(mGridDatabaseAdapterMock.getByGridDefinition(anyString()))
				.thenReturn(null);
		// Prepare for failing insert of new grid row
		int mGridRowIdWhenInsertFails = -1;
		when(mGridDatabaseAdapterMock.insert(any(Grid.class))).thenReturn(
				mGridRowIdWhenInsertFails);

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock).insert(any(Grid.class));
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_InsertGridWithExistingGridDefinition_Saved()
			throws Exception {
		// Prepare for finding existing grid row when inserting a new grid
		GridRow gridRow = mock(GridRow.class);
		when(gridRow.getGridId()).thenReturn(25);
		when(mGridDatabaseAdapterMock.getByGridDefinition(anyString()))
				.thenReturn(gridRow);
		// Prepare insert of new solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.insert(any(SolvingAttemptRow.class))).thenReturn(
				mock(SolvingAttemptRow.class));
		// Prepare insert of new statistics
		int mGridStatisticsIdAfterSuccessfulInsert = 52;
		when(mStatisticsDatabaseAdapterMock.insert(any(GridStatistics.class)))
				.thenReturn(mGridStatisticsIdAfterSuccessfulInsert);

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock)
				.insert(any(GridStatistics.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(mGridSaver.getRowId(), is(gridRow.getGridId()));
		assertThat(mGridSaver.getSolvingAttemptId() >= 0, is(true));
		assertThat(mGridSaver.getGridStatistics().mId >= 0, is(true));
		assertThat(mGridSaver.getDateUpdated(), is(mGridMock.getDateSaved()));
		assertThat(saveResult, is(true));
	}

	@Test
	public void save_SavedGridButMissingSolvingAttemptAndStatistics_Saved()
			throws Exception {
		// Grid was already saved
		int existingGridId = 12;
		when(mGridMock.getRowId()).thenReturn(existingGridId);
		// Prepare insert of new solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.insert(any(SolvingAttemptRow.class))).thenReturn(
				mock(SolvingAttemptRow.class));
		// Prepare insert of new statistics
		int mGridStatisticsIdAfterSuccessfulInsert = 52;
		when(mStatisticsDatabaseAdapterMock.insert(any(GridStatistics.class)))
				.thenReturn(mGridStatisticsIdAfterSuccessfulInsert);

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock)
				.insert(any(GridStatistics.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(mGridSaver.getRowId(), is(existingGridId));
		assertThat(mGridSaver.getSolvingAttemptId() >= 0, is(true));
		assertThat(mGridSaver.getGridStatistics().mId >= 0, is(true));
		assertThat(mGridSaver.getDateUpdated(), is(mGridMock.getDateSaved()));
		assertThat(saveResult, is(true));
	}

	@Test
	public void save_SavedGridButInsertSolvingAttemptFails_NotSaved()
			throws Exception {
		// Grid was already saved
		int existingGridId = 12;
		when(mGridMock.getRowId()).thenReturn(existingGridId);
		// Prepare for failing insert of new solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.insert(any(SolvingAttemptRow.class))).thenReturn(null);
		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock).insert(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock, never()).insert(
				any(GridStatistics.class));
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptUpdateSolvingAttemptAndInsertStatistics_Saved()
			throws Exception {
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		when(mGridMock.getRowId()).thenReturn(existingGridId);
		int existingSolvingAttemptId = 15;
		when(mGridMock.getSolvingAttemptId()).thenReturn(
				existingSolvingAttemptId);

		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.update(any(SolvingAttemptRow.class))).thenReturn(true);
		// Prepare insert of new statistics
		int mGridStatisticsIdAfterSuccessfulInsert = 52;
		when(mStatisticsDatabaseAdapterMock.insert(any(GridStatistics.class)))
				.thenReturn(mGridStatisticsIdAfterSuccessfulInsert);

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(SolvingAttemptRow.class));
		verify(mSolvingAttemptDatabaseAdapterMock).update(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock)
				.insert(any(GridStatistics.class));
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(mGridSaver.getRowId(), is(existingGridId));
		assertThat(mGridSaver.getSolvingAttemptId(),
				is(existingSolvingAttemptId));
		assertThat(mGridSaver.getGridStatistics().mId >= 0, is(true));
		assertThat(mGridSaver.getDateUpdated() > mGridMock.getDateSaved(),
				is(true));
		assertThat(saveResult, is(true));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptButUpdateSolvingAttemptFails_NotSaved()
			throws Exception {
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		when(mGridMock.getRowId()).thenReturn(existingGridId);
		int existingSolvingAttemptId = 15;
		when(mGridMock.getSolvingAttemptId()).thenReturn(
				existingSolvingAttemptId);
		// Prepare failing update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.update(any(SolvingAttemptRow.class)))
				.thenReturn(false);

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(SolvingAttemptRow.class));
		verify(mSolvingAttemptDatabaseAdapterMock).update(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock, never()).insert(
				any(GridStatistics.class));
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptButInsertStatisticsFails_NotSaved()
			throws Exception {
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		when(mGridMock.getRowId()).thenReturn(existingGridId);
		int existingSolvingAttemptId = 15;
		when(mGridMock.getSolvingAttemptId()).thenReturn(
				existingSolvingAttemptId);
		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.update(any(SolvingAttemptRow.class))).thenReturn(true);
		// Prepare failing insert of new statistics
		when(mStatisticsDatabaseAdapterMock.insert(any(GridStatistics.class)))
				.thenReturn(-1);

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(SolvingAttemptRow.class));
		verify(mSolvingAttemptDatabaseAdapterMock).update(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock)
				.insert(any(GridStatistics.class));
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptAndStatisticsUpdateStatisticsFails_NotSaved()
			throws Exception {
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		when(mGridMock.getRowId()).thenReturn(existingGridId);
		int existingSolvingAttemptId = 15;
		when(mGridMock.getSolvingAttemptId()).thenReturn(
				existingSolvingAttemptId);
		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.update(any(SolvingAttemptRow.class))).thenReturn(true);
		// Prepare failing update of statistics
		int existingStatisticsId = 16;
		mGridStatisticsMock.mId = existingStatisticsId;
		when(mGridStatisticsMock.save()).thenReturn(false);
		// Build grid and check that grid, solving attempt and statistics are
		// not yet saved.

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(SolvingAttemptRow.class));
		verify(mSolvingAttemptDatabaseAdapterMock).update(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock, never()).insert(
				any(GridStatistics.class));
		verify(mGridStatisticsMock).save();
		verify(mDatabaseHelperMock, never()).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(saveResult, is(false));
	}

	@Test
	public void save_ExistingGridAndSolvingAttemptAndStatisticsUpdateAfterReplayIsFinished_Saved()
			throws Exception {
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		when(mGridMock.getRowId()).thenReturn(existingGridId);
		int existingSolvingAttemptId = 15;
		when(mGridMock.getSolvingAttemptId()).thenReturn(
				existingSolvingAttemptId);
		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.update(any(SolvingAttemptRow.class))).thenReturn(true);
		// Prepare successful update of statistics
		int existingStatisticsId = 16;
		mGridStatisticsMock.mId = existingStatisticsId;
		when(mGridStatisticsMock.save()).thenReturn(true);
		// Prepare that this is a finished replay of a grid which is not yet
		// included in the (overall) statistics
		when(mGridMock.isActive()).thenReturn(false);
		when(mGridStatisticsMock.getReplayCount()).thenReturn(1);
		when(mGridStatisticsMock.isIncludedInStatistics()).thenReturn(false);

		boolean saveResult = mGridSaver.save(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(SolvingAttemptRow.class));
		verify(mSolvingAttemptDatabaseAdapterMock).update(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock, never()).insert(
				any(GridStatistics.class));
		verify(mGridStatisticsMock).save();
		verify(mStatisticsDatabaseAdapterMock)
				.updateSolvingAttemptToBeIncludedInStatistics(anyInt(),
						anyInt());
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(mGridSaver.getRowId(), is(existingGridId));
		assertThat(mGridSaver.getSolvingAttemptId(),
				is(existingSolvingAttemptId));
		assertThat(mGridSaver.getGridStatistics().mId, is(existingStatisticsId));
		assertThat(mGridSaver.getDateUpdated() > mGridMock.getDateSaved(),
				is(true));
		assertThat(saveResult, is(true));
	}

	@Test
	public void saveOnAppUpgrade_GridSaved_DateUpdatedNotChanged() {
		// Grid and solving attempt were already saved
		int existingGridId = 12;
		when(mGridMock.getRowId()).thenReturn(existingGridId);
		int existingSolvingAttemptId = 15;
		when(mGridMock.getSolvingAttemptId()).thenReturn(
				existingSolvingAttemptId);
		// Prepare successful update of solving attempt
		when(
				mSolvingAttemptDatabaseAdapterMock
						.update(any(SolvingAttemptRow.class))).thenReturn(true);
		// Prepare successful update of statistics
		int existingStatisticsId = 16;
		mGridStatisticsMock.mId = existingStatisticsId;
		when(mGridStatisticsMock.save()).thenReturn(true);
		// Prepare that this is a finished replay of a grid which is not yet
		// included in the (overall) statistics
		when(mGridMock.isActive()).thenReturn(false);
		when(mGridStatisticsMock.getReplayCount()).thenReturn(1);
		when(mGridStatisticsMock.isIncludedInStatistics()).thenReturn(false);

		boolean saveResult = mGridSaver.saveOnAppUpgrade(mGridMock);

		verify(mDatabaseHelperMock).beginTransaction();
		verify(mGridDatabaseAdapterMock, never()).insert(any(Grid.class));
		verify(mSolvingAttemptDatabaseAdapterMock, never()).insert(
				any(SolvingAttemptRow.class));
		verify(mSolvingAttemptDatabaseAdapterMock).update(
				any(SolvingAttemptRow.class));
		verify(mStatisticsDatabaseAdapterMock, never()).insert(
				any(GridStatistics.class));
		verify(mGridStatisticsMock).save();
		verify(mStatisticsDatabaseAdapterMock)
				.updateSolvingAttemptToBeIncludedInStatistics(anyInt(),
						anyInt());
		verify(mDatabaseHelperMock).setTransactionSuccessful();
		verify(mDatabaseHelperMock).endTransaction();
		assertThat(mGridSaver.getRowId(), is(existingGridId));
		assertThat(mGridSaver.getSolvingAttemptId(),
				is(existingSolvingAttemptId));
		assertThat(mGridSaver.getGridStatistics().mId, is(existingStatisticsId));
		assertThat(mGridSaver.getDateUpdated(), is(mGridMock.getDateSaved()));
		assertThat(saveResult, is(true));
	}

	@Test(expected = UnexpectedMethodInvocationException.class)
	public void getRowId_MethodIsCalledBeforeCallingSave_ThrowUnexpectedMethodInvocationException()
			throws Exception {
		mGridSaver.getRowId();
	}

	@Test(expected = UnexpectedMethodInvocationException.class)
	public void getSolvingAttemptId_MethodIsCalledBeforeCallingSave_ThrowUnexpectedMethodInvocationException()
			throws Exception {
		mGridSaver.getSolvingAttemptId();
	}

	@Test(expected = UnexpectedMethodInvocationException.class)
	public void getGridStatistics_MethodIsCalledBeforeCallingSave_ThrowUnexpectedMethodInvocationException()
			throws Exception {
		mGridSaver.getGridStatistics();
	}
}
