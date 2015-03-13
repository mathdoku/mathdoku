package net.mathdoku.plus.gridgenerating;

import android.app.ProgressDialog;

import net.mathdoku.plus.gridgenerating.iface.GridGeneratorAsyncTaskIface;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static matcher.MathdokuMatcher.notSameInstance;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GeneratePuzzleProgressDialogTest {
    private GeneratePuzzleProgressDialog generatePuzzleProgressDialog;
    private PuzzleFragmentActivity puzzleFragmentActivity;
    private GridGeneratorAsyncTaskStub gridGeneratorAsyncTaskStub;
    private ProgressDialog progressDialogMock;
    private boolean onNewGridReadyIsCalled = false;
    private boolean onCancelGridGenerationIsCalled = false;

    private class GridGeneratorAsyncTaskStub implements GridGeneratorAsyncTaskIface {
        boolean createGridsIsRunning = false;

        public GridGeneratorAsyncTaskStub() {

        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void updateProgressHighLevel(String text) {

        }

        @Override
        public void updateProgressDetailLevel(String text) {

        }

        @Override
        public void signalSlowGridGeneration() {

        }

        @Override
        public void createGrids(GridGeneratingParameters... gridGeneratingParametersArray) {
            createGridsIsRunning = true;
        }

        @Override
        public void cancel() {

        }
    }

    @Before
    public void setup() {
        // Instantiate singleton classes
        puzzleFragmentActivity = new PuzzleFragmentActivity() {
            @Override
            public void onNewGridReady(Grid newGrid) {
                onNewGridReadyIsCalled = true;
            }

            @Override
            public void onCancelGridGeneration() {
                onCancelGridGenerationIsCalled = true;
            }
        };
        generatePuzzleProgressDialog = createGeneratePuzzleProgressDialogForASingleGrid();
    }

    private GeneratePuzzleProgressDialog createGeneratePuzzleProgressDialogForASingleGrid() {
        return createGeneratePuzzleProgressDialog(new GridGeneratingParameters[]{mock(GridGeneratingParameters.class)});
    }

    private GeneratePuzzleProgressDialog createGeneratePuzzleProgressDialog(final GridGeneratingParameters[]
                                                                                    gridGeneratingParametersArray) {
        return new GeneratePuzzleProgressDialog(puzzleFragmentActivity, gridGeneratingParametersArray) {
            @Override
            public GridGeneratorAsyncTaskIface createGridGeneratorAsyncTask() {
                gridGeneratorAsyncTaskStub = new GridGeneratorAsyncTaskStub();
                return gridGeneratorAsyncTaskStub;
            }

            @Override
            ProgressDialog createProgressDialog() {
                progressDialogMock = mock(ProgressDialog.class);
                return progressDialogMock;
            }
        };
    }

    @Test
    public void constructor_Default_CreatingGridIsStarted() throws Exception {
        assertThat(gridGeneratorAsyncTaskStub.createGridsIsRunning, is(true));
    }

    @Test
    public void constructor_Default_ProgressDialogIsCreated() throws Exception {
        assertThat(progressDialogMock, is(notNullValue()));
    }

    @Test
    public void constructor_Default_SpinnerStyle() throws Exception {
        verify(progressDialogMock).setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Test
    public void constructor_MultipleGridGeneratingParameterSets_ProgressBarStyle() throws Exception {
        generatePuzzleProgressDialog = createGeneratePuzzleProgressDialogForMultipleGrids();
        verify(progressDialogMock).setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    private GeneratePuzzleProgressDialog createGeneratePuzzleProgressDialogForMultipleGrids() {
        return createGeneratePuzzleProgressDialog(new GridGeneratingParameters[]{mock(GridGeneratingParameters.class),
                mock(GridGeneratingParameters.class)});
    }

    @Test
    public void attachToActivity_AttachActivityAgainWhileDialogStillVisible_SameInstanceReturned() throws Exception {
        when(progressDialogMock.isShowing()).thenReturn(true);
        ProgressDialog initialProgressDialog = progressDialogMock;
        assertThat(generatePuzzleProgressDialog.attachToActivity(puzzleFragmentActivity),
                   is(sameInstance(generatePuzzleProgressDialog)));
        assertThat(progressDialogMock, is(sameInstance(initialProgressDialog)));
    }

    @Test
    public void attachToActivity_AttachActivityAgainWhileDialogNotVisible_OtherInstanceReturned() throws Exception {
        // In setup the progress dialog is already attached to the puzzle
        // fragment and a dialog was created. After re-attaching the same
        // activity, the dialog should not have been rebuild.
        ProgressDialog initialProgressDialog = progressDialogMock;
        assertThat(generatePuzzleProgressDialog.attachToActivity(puzzleFragmentActivity),
                   is(sameInstance(generatePuzzleProgressDialog)));
        assertThat(progressDialogMock, is(notSameInstance(initialProgressDialog)));
    }

    @Test
    public void attachToActivity_AttachOtherActivity_OtherInstanceReturned() throws Exception {
        // In setup the progress dialog is already attached to the puzzle
        // fragment and a dialog was created. After re-attaching the same
        // activity, the dialog should not have been rebuild.
        ProgressDialog initialProgressDialog = progressDialogMock;
        when(progressDialogMock.isShowing()).thenReturn(true);
        PuzzleFragmentActivity otherPuzzleFragmentActivity = new PuzzleFragmentActivity();
        assertThat(generatePuzzleProgressDialog.attachToActivity(otherPuzzleFragmentActivity),
                   is(sameInstance(generatePuzzleProgressDialog)));
        assertThat(progressDialogMock, is(notSameInstance(initialProgressDialog)));
    }

    @Test
    public void show() throws Exception {
        generatePuzzleProgressDialog.show();
        verify(progressDialogMock).show();
    }

    @Test
    public void detachFromActivity() throws Exception {
        generatePuzzleProgressDialog.detachFromActivity();
        verify(progressDialogMock).dismiss();
    }

    @Test
    public void onFinishGridGenerator_GridListNull_DismissDialogOnly() throws Exception {
        generatePuzzleProgressDialog.onFinishGridGenerator(null);
        verify(progressDialogMock).dismiss();
    }

    @Test
    public void onFinishGridGenerator_GridListEmpty_DismissDialogOnly() throws Exception {
        generatePuzzleProgressDialog.onFinishGridGenerator(new ArrayList<Grid>());
        verify(progressDialogMock).dismiss();
    }

    @Test
    public void onFinishGridGenerator_GridListContainsSingleGrid_DismissDialogOnly() throws Exception {
        List<Grid> gridListWithSingleGrid = new ArrayList<Grid>();
        gridListWithSingleGrid.add(mock(Grid.class));
        generatePuzzleProgressDialog.onFinishGridGenerator(gridListWithSingleGrid);
        verify(progressDialogMock).dismiss();
        assertThat(onNewGridReadyIsCalled, is(true));
    }

    // Todo: add test onFinishGridGenerator with multiple result grids.
    // onFinishGridGenerator_GridListContainsMultipleGrids_DismissDialogOnly()
    // This test however fails on a null pointer exception when calling a static
    // function. As this call will only be used in development mode it is not a
    // problem that it is not yet unit tested.

    @Test
    public void onCancelGridGeneratorAsyncTask() throws Exception {
        generatePuzzleProgressDialog.onCancelGridGeneratorAsyncTask();
        assertThat(onCancelGridGenerationIsCalled, is(true));
        verify(progressDialogMock).dismiss();
    }

    @Test
    public void onHighLevelProgressUpdate() throws Exception {
        String progressUpdate = "*** HIGH LEVEL PROGRESS UPDATE ***";
        generatePuzzleProgressDialog.onHighLevelProgressUpdate(progressUpdate);
        verify(progressDialogMock).setTitle(progressUpdate);
    }

    @Test
    public void onDetailLevelProgressDetail() throws Exception {
        String progressUpdate = "*** DETAIL LEVEL PROGRESS UPDATE ***";
        generatePuzzleProgressDialog.onDetailLevelProgressDetail(progressUpdate);
        verify(progressDialogMock).setMessage(progressUpdate);
    }
}
