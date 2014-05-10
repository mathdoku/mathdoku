package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.enums.SolvingAttemptStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class SolvingAttemptDatabaseAdapterTest {
	SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapter;
	private HashMap</* solving attempt id */Integer, /* grid id */Integer> insertedSolvingAttempts;

	private static class SolvingAttemptRowStub extends SolvingAttemptRow {
		// The date created will be increased with 1 each time a new solving
		// attempt
		// is created by this class.
		private static long systemCurrentTimeInMillis = 123456789L;

		private static int solvingAttemptIdWhichIsIgnoredDuringInsert = -1;
		private static int gridId = 198;
		private static long dateCreated = systemCurrentTimeInMillis;
		private static long dateUpdated = systemCurrentTimeInMillis;
		private static int revision = 598;

		private SolvingAttemptRowStub(int solvingAttemptId, int gridId,
				long solvingAttemptDateCreated, long solvingAttemptDateUpdated,
				SolvingAttemptStatus solvingAttemptStatus,
				int savedWithRevision, String storageString) {
			super(solvingAttemptId, gridId, solvingAttemptDateCreated,
					solvingAttemptDateUpdated, solvingAttemptStatus,
					savedWithRevision, storageString);
		}

		public SolvingAttemptRowStub(String storageString) {
			super(solvingAttemptIdWhichIsIgnoredDuringInsert, gridId,
					dateCreated, dateUpdated, SolvingAttemptStatus.UNFINISHED,
					revision, storageString);
			increaseCurrentTimeInMillis();
		}

		public SolvingAttemptRowStub(int gridId, String storageString) {
			super(solvingAttemptIdWhichIsIgnoredDuringInsert, gridId,
					dateCreated, dateUpdated, SolvingAttemptStatus.UNFINISHED,
					revision, storageString);
			increaseCurrentTimeInMillis();
		}

		public static void increaseCurrentTimeInMillis() {
			systemCurrentTimeInMillis++;
			dateCreated = systemCurrentTimeInMillis;
			dateUpdated = systemCurrentTimeInMillis;
		}

		public static SolvingAttemptRowStub createUpdatedSolvingAttemptRowStub(
				SolvingAttemptRow source) {
			systemCurrentTimeInMillis++;
			return new SolvingAttemptRowStub(source.getSolvingAttemptId(),
					source.getGridId(), source.getSolvingAttemptDateCreated(),
					systemCurrentTimeInMillis,
					source.getSolvingAttemptStatus(),
					source.getSavedWithRevision(), source.getStorageString());
		}
	}

	@Before
	public void setUp() throws Exception {
		TestRunnerHelper.setup(this.getClass().getCanonicalName());
		solvingAttemptDatabaseAdapter = new SolvingAttemptDatabaseAdapter();
	}

	@After
	public void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	@Test
	public void getDatabaseTableDefinition() throws Exception {
		String resultDatabaseCreateSQL = new SolvingAttemptDatabaseAdapter()
				.getDatabaseTableDefinition()
				.getCreateTableSQL();
		StringBuilder expectedDatabaseCreateSQL = new StringBuilder();
		expectedDatabaseCreateSQL.append("CREATE TABLE `solving_attempt` (");
		expectedDatabaseCreateSQL
				.append("`_id` integer primary key autoincrement, ");
		expectedDatabaseCreateSQL.append("`grid_id` integer not null, ");
		expectedDatabaseCreateSQL.append("`date_created` datetime not null, ");
		expectedDatabaseCreateSQL.append("`date_updated` datetime not null, ");
		expectedDatabaseCreateSQL.append("`revision` integer not null, ");
		expectedDatabaseCreateSQL.append("`data` text not null, ");
		expectedDatabaseCreateSQL
				.append("`status` integer not null default -1, ");
		expectedDatabaseCreateSQL
				.append("FOREIGN KEY(`grid_id`) REFERENCES grid(_id)");
		expectedDatabaseCreateSQL.append(")");
		assertThat(resultDatabaseCreateSQL,
				is(expectedDatabaseCreateSQL.toString()));
	}

	@Test
	public void upgradeTable() throws Exception {
		// Nothing to test currently.
	}

	@Test(expected = IllegalArgumentException.class)
	public void insert_SolvingAttemptRowIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		solvingAttemptDatabaseAdapter.insert(null);
	}

	@Test
	public void insert_SolvingAttemptRowIsNotNull() throws Exception {
		int idOfFirstSolvingAttemptIdInEmptyDatabase = 1;
		SolvingAttemptRow solvingAttemptRow = createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 1 ***");
		assertThat(solvingAttemptRow, is(notNullValue()));
		assertThat(solvingAttemptRow.getSolvingAttemptId(),
				is(idOfFirstSolvingAttemptIdInEmptyDatabase));
	}

	private SolvingAttemptRow createAndInsertSolvingAttemptRow(
			String storageString) {
		return solvingAttemptDatabaseAdapter.insert(new SolvingAttemptRowStub(
				storageString));
	}

	@Test
	public void getSolvingAttemptRow() throws Exception {
		createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 1 ***");
		SolvingAttemptRow solvingAttemptRow = createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 2 ***");
		createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 3 ***");

		int solvingAttemptId = solvingAttemptRow.getSolvingAttemptId();
		assertThat(
				solvingAttemptDatabaseAdapter
						.getSolvingAttemptRow(solvingAttemptId),
				is(solvingAttemptRow));
	}

	@Test
	public void getMostRecentPlayedId_WhenInsertWasLastSaveAction()
			throws Exception {
		createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 1 ***");
		SolvingAttemptRow lastSolvingAttemptRowInserted = createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 2 ***");

		assertThat(solvingAttemptDatabaseAdapter.getMostRecentPlayedId(),
				is(lastSolvingAttemptRowInserted.getSolvingAttemptId()));
	}

	@Test
	public void getMostRecentPlayedId_WhenUpdateWasLastSaveAction()
			throws Exception {
		SolvingAttemptRow solvingAttemptRow1 = createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 1 ***");
		createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 2 ***");
		solvingAttemptDatabaseAdapter.update(SolvingAttemptRowStub
				.createUpdatedSolvingAttemptRowStub(solvingAttemptRow1));

		assertThat(solvingAttemptDatabaseAdapter.getMostRecentPlayedId(),
				is(solvingAttemptRow1.getSolvingAttemptId()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void update_SolvingAttemptRowIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		solvingAttemptDatabaseAdapter.update(null);
	}

	@Test
	public void updateSolvingAttemptRowIsNotNull() throws Exception {
		SolvingAttemptRow solvingAttemptRow = createAndInsertSolvingAttemptRow("*** SOME STORAGE STRING 1 ***");
		SolvingAttemptRow updatedSolvingAttemptRow = SolvingAttemptRowStub
				.createUpdatedSolvingAttemptRowStub(solvingAttemptRow);
		assertThat(
				solvingAttemptDatabaseAdapter.update(updatedSolvingAttemptRow),
				is(true));
		assertThat(
				solvingAttemptDatabaseAdapter.getSolvingAttemptRow(solvingAttemptRow
						.getSolvingAttemptId()), is(updatedSolvingAttemptRow));
	}

	@Test
	public void getAllToBeConverted() throws Exception {
		int gridId1 = 198;
		int gridId2 = 199;
		createSolvingAttemptsForMultipleGrids(gridId1, gridId2);

		List<Integer> resultGridIds = solvingAttemptDatabaseAdapter
				.getAllToBeConverted();
		Collections.sort(resultGridIds);

		List<Integer> expectedGridIds = new ArrayList<Integer>();
		for (Map.Entry<Integer, Integer> entry : insertedSolvingAttempts
				.entrySet()) {
			if (!expectedGridIds.contains(entry.getKey())) {
				expectedGridIds.add((entry.getKey()));
			}
		}

		Collections.sort(expectedGridIds);

		assertThat(resultGridIds, is(expectedGridIds));
	}

	private void createSolvingAttemptsForMultipleGrids(int gridId1, int gridId2) {
		createAndInsertSolvingAttemptRowAndSaveCreatedIds(gridId1,
				"*** SOME STORAGE STRING 1 FOR GRID 1 ***");
		createAndInsertSolvingAttemptRowAndSaveCreatedIds(gridId2,
				"*** SOME STORAGE STRING 2 FOR GRID 2 ***");
		createAndInsertSolvingAttemptRowAndSaveCreatedIds(gridId1,
				"*** SOME STORAGE STRING 3 FOR GRID 1 ***");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getPrefixedColumnName_ColumnNameIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		assertThat(SolvingAttemptDatabaseAdapter.getPrefixedColumnName(null),
				is("`solving_attempt`.`TestAbC`"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getPrefixedColumnName_ColumnNamIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		assertThat(SolvingAttemptDatabaseAdapter.getPrefixedColumnName(""),
				is("`solving_attempt`.`TestAbC`"));
	}

	@Test
	public void getPrefixedColumnName_ColumnNameIsNotNull() throws Exception {
		assertThat(
				SolvingAttemptDatabaseAdapter.getPrefixedColumnName("TestAbC"),
				is("`solving_attempt`.`TestAbC`"));
	}

	@Test
	public void countSolvingAttemptForGrid() throws Exception {
		int gridId1 = 198;
		int gridId2 = 199;
		createSolvingAttemptsForMultipleGrids(gridId1, gridId2);

		int expectedCountGridId1 = 0;
		for (Map.Entry<Integer, Integer> entry : insertedSolvingAttempts
				.entrySet()) {
			if (entry.getValue() == gridId1) {
				expectedCountGridId1++;
			}
		}
		assertThat(
				solvingAttemptDatabaseAdapter
						.countSolvingAttemptForGrid(gridId1),
				is(expectedCountGridId1));
	}

	private SolvingAttemptRow createAndInsertSolvingAttemptRowAndSaveCreatedIds(
			int gridId, String storageString) {
		SolvingAttemptRow solvingAttemptRow = solvingAttemptDatabaseAdapter
				.insert(new SolvingAttemptRowStub(gridId, storageString));

		if (insertedSolvingAttempts == null) {
			insertedSolvingAttempts = new HashMap<Integer, Integer>();
		}
		insertedSolvingAttempts.put(solvingAttemptRow.getSolvingAttemptId(),
				solvingAttemptRow.getGridId());

		return solvingAttemptRow;
	}
}
