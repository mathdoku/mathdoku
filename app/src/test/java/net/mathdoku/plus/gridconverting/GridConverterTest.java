package net.mathdoku.plus.gridconverting;

import android.app.ProgressDialog;

import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridLoader;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static matcher.MathdokuMatcher.notSameInstance;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridConverterTest {
    private PuzzleFragmentActivity puzzleFragmentActivity;
    private ProgressDialog progressDialogInitialMock = mock(ProgressDialog.class);
    private SolvingAttemptDatabaseAdapter solvingAttemptDatabaseAdapterMock = mock(SolvingAttemptDatabaseAdapter.class);
    private GridLoader gridLoaderMock = mock(GridLoader.class);
    public static final int CURRENT_VERSION_598 = 598;
    public static final int NEW_VERSION_600 = 600;
    public boolean activityUpgradePhase2IsCalled = false;

    private class GridConverterWithMockedDialog extends GridConverter {
        public boolean progressDialogCreated;
        public ProgressDialog currentProgressDialog;

        public GridConverterWithMockedDialog(PuzzleFragmentActivity activity, int currentVersion, int newVersion) {
            super(activity, currentVersion, newVersion);
        }

        @Override
        ProgressDialog createProgressDialog(int maxProgressCounter) {
            if (progressDialogCreated) {
                // On second and subsequent calls return another mock to
                // simulate that the puzzle fragment activity and the dialog
                // were destroyed.
                currentProgressDialog = mock(ProgressDialog.class);
            } else {
                progressDialogCreated = true;
                currentProgressDialog = progressDialogInitialMock;
            }
            return currentProgressDialog;
        }

        @Override
        SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
            return solvingAttemptDatabaseAdapterMock;
        }

        @Override
        GridLoader createGridLoader() {
            return gridLoaderMock;
        }
    }

    @Before
    public void setUp() throws Exception {
        TestRunnerHelper.setup(this.getClass()
                                       .getCanonicalName());
        puzzleFragmentActivity = new PuzzleFragmentActivity() {
            @Override
            public void upgradePhase2(int previousInstalledVersion, int currentVersion) {
                activityUpgradePhase2IsCalled = true;
                // swallow
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        TestRunnerHelper.tearDown();
    }

    @Test
    public void constructor_VersionNotIncreased_NoDialogPresented() throws Exception {
        GridConverterWithMockedDialog gridConverterWithMockedDialog = new GridConverterWithMockedDialog(
                puzzleFragmentActivity, CURRENT_VERSION_598, CURRENT_VERSION_598);

        assertThat(gridConverterWithMockedDialog.progressDialogCreated, is(false));
    }

    @Test
    public void constructor_VersionIncreased_NoDialogPresented() throws Exception {
        GridConverterWithMockedDialog gridConverterWithMockedDialog = new GridConverterWithMockedDialog(
                puzzleFragmentActivity, CURRENT_VERSION_598, NEW_VERSION_600);

        assertThat(gridConverterWithMockedDialog.progressDialogCreated, is(notNullValue()));
    }

    @Test
    public void attachToActivity_AttachActivityAgainWhileDialogStillVisible_SameInstanceReturned() throws Exception {
        setSolvingAttemptDatabaseAdapterMockToReturnIds(1, 2, 3);
        when(progressDialogInitialMock.isShowing()).thenReturn(true);
        GridConverterWithMockedDialog gridConverterWithMockedDialog = new GridConverterWithMockedDialog(
                puzzleFragmentActivity, CURRENT_VERSION_598, NEW_VERSION_600);

        ProgressDialog initialProgressDialog = progressDialogInitialMock;
        gridConverterWithMockedDialog.attachToActivity(puzzleFragmentActivity);
        assertThat(gridConverterWithMockedDialog.progressDialogCreated, is(true));
        assertThat(gridConverterWithMockedDialog.currentProgressDialog, is(sameInstance(initialProgressDialog)));
    }

    private List<Integer> setSolvingAttemptDatabaseAdapterMockToReturnIds(int... solvingAttemptIds) {
        List<Integer> solvingAttemptsToBeConverted = new ArrayList<Integer>();
        for (int solvingAttemptId : solvingAttemptIds) {
            solvingAttemptsToBeConverted.add(solvingAttemptId);
        }
        when(solvingAttemptDatabaseAdapterMock.getAllToBeConverted()).thenReturn(solvingAttemptsToBeConverted);

        return solvingAttemptsToBeConverted;
    }

    @Test
    public void attachToActivity_AttachActivityAgainWhileDialogNotVisible_OtherInstanceReturned() throws Exception {
        setSolvingAttemptDatabaseAdapterMockToReturnIds(1);
        GridConverterWithMockedDialog gridConverterWithMockedDialog = new GridConverterWithMockedDialog(
                puzzleFragmentActivity, CURRENT_VERSION_598, NEW_VERSION_600);
        assertThat(gridConverterWithMockedDialog.progressDialogCreated, is(true));

        // In setup above the progress dialog is already attached to the puzzle
        // fragment and a dialog was created. After re-attaching the same
        // activity, the dialog should not have been rebuild.
        ProgressDialog initialProgressDialog = progressDialogInitialMock;
        when(progressDialogInitialMock.isShowing()).thenReturn(false);
        setSolvingAttemptDatabaseAdapterMockToReturnIds(2, 3);

        gridConverterWithMockedDialog.attachToActivity(puzzleFragmentActivity);
        assertThat(gridConverterWithMockedDialog.currentProgressDialog, is(notSameInstance(initialProgressDialog)));
    }

    @Test
    public void attachToActivity_AttachOtherActivity_OtherInstanceReturned() throws Exception {
        setSolvingAttemptDatabaseAdapterMockToReturnIds(1, 2, 3);
        GridConverterWithMockedDialog gridConverterWithMockedDialog = new GridConverterWithMockedDialog(
                puzzleFragmentActivity, CURRENT_VERSION_598, NEW_VERSION_600);

        // In setup above the progress dialog is already attached to the puzzle
        // fragment and a dialog was created. After re-attaching the same
        // activity, the dialog should not have been rebuild.
        ProgressDialog initialProgressDialog = progressDialogInitialMock;
        when(progressDialogInitialMock.isShowing()).thenReturn(true);
        PuzzleFragmentActivity otherPuzzleFragmentActivity = new PuzzleFragmentActivity();
        gridConverterWithMockedDialog.attachToActivity(otherPuzzleFragmentActivity);
        assertThat(gridConverterWithMockedDialog.progressDialogCreated, is(true));
        assertThat(gridConverterWithMockedDialog.currentProgressDialog, is(notSameInstance(initialProgressDialog)));
    }

    @Test
    // Test method execute tests several protected methods of class GridConverter:
    // - Void doInBackground(Void... params)
    // - void onProgressUpdate(Void... values)
    // - void onPostExecute(Void result)
    // -
    public void execute() throws Exception {
        List<Integer> idsToBeReturned = setSolvingAttemptDatabaseAdapterMockToReturnIds(1, 2, 3, 12);
        GridConverterWithMockedDialog gridConverterWithMockedDialog = new GridConverterWithMockedDialog(
                puzzleFragmentActivity, CURRENT_VERSION_598, NEW_VERSION_600);
        when(gridLoaderMock.load(anyInt())).thenReturn(mock(Grid.class));
        gridConverterWithMockedDialog.execute();
        verify(gridConverterWithMockedDialog.currentProgressDialog, times(idsToBeReturned.size())).incrementProgressBy(
                1);
        assertThat(activityUpgradePhase2IsCalled, is(true));
    }
}
