package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.puzzle.grid.Grid;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
public class GridGeneratorAsyncTaskTest {
	public static final String DETAIL_LEVEL_PROGRESS_UPDATE = "*** DETAIL LEVEL PROGRESS UPDATE ***";
	private GridGeneratorAsyncTask gridGeneratorAsyncTask;
	private GridGeneratorAsyncTaskListener gridGeneratorAsyncTaskListener;

	private class GridGeneratorStub extends GridGenerator {
		protected Listener listener;

		public GridGeneratorStub(Listener listener) {
			super(listener);
			this.listener = listener;
		}

		@Override
		public Grid createGrid(GridGeneratingParameters gridGeneratingParameters) {
			return mock(Grid.class);
		}

		@Override
		public Grid createGridInDevelopmentMode(
				GridGeneratingParameters gridGeneratingParameters) {
			return createGrid(gridGeneratingParameters);
		}
	}

	private class GridGeneratorAsyncTaskListener implements
			GridGeneratorAsyncTask.Listener {
		private boolean onGeneratedIsCalled = false;
		private boolean onFinishGridGeneratorIsCalled = false;
		private int onFinishGridGeneratorGridCount = 0;
		private boolean onCancelGridGeneratorAsyncTaskIsCalled = false;
		private String onHighLevelProgressUpdateText = "";
		private String onDetailLevelProgressUpdateText = "";

		@Override
		public void onGridGenerated() {
			onGeneratedIsCalled = true;
		}

		@Override
		public void onFinishGridGenerator(List<Grid> grids) {
			onFinishGridGeneratorIsCalled = true;
			onFinishGridGeneratorGridCount = grids.size();
		}

		@Override
		public void onCancelGridGeneratorAsyncTask() {
			onCancelGridGeneratorAsyncTaskIsCalled = true;
		}

		@Override
		public void onHighLevelProgressUpdate(String text) {
			onHighLevelProgressUpdateText = text;
		}

		@Override
		public void onDetailLevelProgressDetail(String text) {
			onDetailLevelProgressUpdateText = text;
		}
	}

	@Before
	public void setUp() throws Exception {
		gridGeneratorAsyncTaskListener = new GridGeneratorAsyncTaskListener();
		gridGeneratorAsyncTask = new GridGeneratorAsyncTask(
				gridGeneratorAsyncTaskListener,
				new GridGeneratorAsyncTask.ObjectsCreator() {
					@Override
					public GridGeneratorInterface createGridGenerator(
							GridGeneratorAsyncTask gridGeneratorAsyncTask) {
						return new GridGeneratorStub(gridGeneratorAsyncTask);
					}
				});
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_NullListener_ThrowsIllegalArgumentException()
			throws Exception {
		new GridGeneratorAsyncTask(null);
	}

	// AsyncTask rethrows exceptions always a runtime exception.
	@Test(expected = RuntimeException.class)
	public void execute_NoGridGeneratingParameters_ThrowsRuntimeException()
			throws Exception {
		gridGeneratorAsyncTask.execute();
	}

	// AsyncTask rethrows exceptions always a runtime exception.
	@Test(expected = RuntimeException.class)
	public void execute_EmptyGridGeneratingParametersArray_ThrowsGridGeneratingException()
			throws Exception {
		GridGeneratingParameters[] gridGeneratingParametersArray = new GridGeneratingParameters[0];
		gridGeneratorAsyncTask.execute(gridGeneratingParametersArray);
	}

	@Test
	public void updateProgressHighLevel_AnyText_AsyncTaskListenerIsCalled()
			throws Exception {
		final String HIGH_LEVEL_PROGRESS_UPDATE = "*** HIGH LEVEL PROGRESS UPDATE ***";
		gridGeneratorAsyncTask
				.updateProgressHighLevel(HIGH_LEVEL_PROGRESS_UPDATE);
		assertThat(
				gridGeneratorAsyncTaskListener.onHighLevelProgressUpdateText,
				is(HIGH_LEVEL_PROGRESS_UPDATE));
	}

	@Test
	public void updateProgressDetailLevel_AnyText_AsyncTaskListenerIsCalled()
			throws Exception {
		final String DETAILS_LEVEL_PROGRESS_UPDATE = "*** DETAILS LEVEL PROGRESS UPDATE ***";
		gridGeneratorAsyncTask
				.updateProgressDetailLevel(DETAILS_LEVEL_PROGRESS_UPDATE);
		assertThat(
				gridGeneratorAsyncTaskListener.onDetailLevelProgressUpdateText,
				is(DETAILS_LEVEL_PROGRESS_UPDATE));
	}

	@Test
	public void signalSlowGridGeneration_OnAnyCall_AsyncTaskIsCancelled()
			throws Exception {
		assertThat(gridGeneratorAsyncTask.isCancelled(), is(false));
		gridGeneratorAsyncTask.signalSlowGridGeneration();
		assertThat(gridGeneratorAsyncTask.isCancelled(), is(true));
	}

	@Test
	public void onPostExecute_OneSetOfGridGeneratingParameters_TaskIsExecuted()
			throws Exception {
		gridGeneratorAsyncTask.execute(mock(GridGeneratingParameters.class));
		assertThat(
				gridGeneratorAsyncTaskListener.onFinishGridGeneratorIsCalled,
				is(true));
		assertThat(
				gridGeneratorAsyncTaskListener.onFinishGridGeneratorGridCount,
				is(1));
	}

	@Test
	public void onPostExecute_TwoSetsOfGridGeneratingParameters_TaskIsExecuted()
			throws Exception {
		if (Config.mAppMode == Config.AppMode.DEVELOPMENT) {
			gridGeneratorAsyncTask.execute(
					mock(GridGeneratingParameters.class),
					mock(GridGeneratingParameters.class));
			assertThat(
					gridGeneratorAsyncTaskListener.onFinishGridGeneratorIsCalled,
					is(true));
			assertThat(
					gridGeneratorAsyncTaskListener.onFinishGridGeneratorGridCount,
					is(2));
		}
	}

	@Test
	public void testOnCancelled() throws Exception {
		gridGeneratorAsyncTask
				.onCancelled(null);
		assertThat(
				gridGeneratorAsyncTaskListener.onCancelGridGeneratorAsyncTaskIsCalled,
				is(true));
	}
}
