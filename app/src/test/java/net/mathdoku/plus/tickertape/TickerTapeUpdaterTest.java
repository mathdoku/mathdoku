package net.mathdoku.plus.tickertape;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import robolectric.RobolectricGradleTestRunner;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class TickerTapeUpdaterTest {
    private static final int NUMBER_OF_RUNS_BEFORE_ERASE_CONDITION_FULFILLED = 3;
    private TickerTapeUpdater tickerTapeUpdater;
    private TickerTape tickerTapeMock = mock(TickerTape.class);

    @Before
    public void setUp() {
        tickerTapeUpdater = new TickerTapeUpdater(tickerTapeMock);
    }

    @Test
    public void startMoving_InitialOrReset_TickerTapeIsVisible() throws Exception {
        tickerTapeUpdater.startMoving();
        verify(tickerTapeMock).setVisibility(View.VISIBLE);
    }

    @Test
    public void run_MultipleTimesBeforeEraseConditionIsFulfilled_TickerTapeHidden() throws Exception {
        boolean doNotCancelBeforeEraseConditionIsFulfilled = false;
        runMultipleTimes(doNotCancelBeforeEraseConditionIsFulfilled);

        verify(tickerTapeMock).setVisibility(View.VISIBLE);
        verify(tickerTapeMock, times(NUMBER_OF_RUNS_BEFORE_ERASE_CONDITION_FULFILLED)).isEraseConditionFulfilled(
                anyInt(), anyLong());
        verify(tickerTapeMock).hide();
    }

    private void runMultipleTimes(boolean cancelBeforeEraseConditionIsFulfilled) {
        // Set number of times the isEraseConditionFulfilled until it finally
        // returns true.
        when(tickerTapeMock.isEraseConditionFulfilled(anyInt(), anyLong())).thenReturn(false, false, true);

        when(tickerTapeMock.getCurrentItem()).thenReturn("*** SOME ITEM ***");

        // When startMoving is called, the run method of
        // TickerTapeUpdater is called once using a post delayed
        // message. After that it consecutively calls itself again using post
        // delayed messages. Robolectric has to be instructed to process each
        // such delayed message.
        tickerTapeUpdater.startMoving();
        for (int i = 0; i < NUMBER_OF_RUNS_BEFORE_ERASE_CONDITION_FULFILLED + 1; i++) {
            if (cancelBeforeEraseConditionIsFulfilled && i == getNumberOfRunsBeforeCancellation()) {
                // Before all runs are completed to fulfill the erase condition,
                // the Ticker Tape Runnable is cancelled.
                tickerTapeUpdater.cancel();
            }
            Robolectric.runUiThreadTasksIncludingDelayedTasks();
        }
    }

    private int getNumberOfRunsBeforeCancellation() {
        return NUMBER_OF_RUNS_BEFORE_ERASE_CONDITION_FULFILLED - 1;
    }

    @Test
    public void run_MultipleTimeBeforeCancellation_TickerTapeNotHidden() throws Exception {
        boolean cancelBeforeEraseConditionIsFulfilled = true;
        runMultipleTimes(cancelBeforeEraseConditionIsFulfilled);

        verify(tickerTapeMock).setVisibility(View.VISIBLE);
        verify(tickerTapeMock, times(getNumberOfRunsBeforeCancellation())).isEraseConditionFulfilled(anyInt(),
                                                                                                     anyLong());
        verify(tickerTapeMock, never()).hide();
    }
}
