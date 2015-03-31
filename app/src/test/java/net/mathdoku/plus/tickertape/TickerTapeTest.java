package net.mathdoku.plus.tickertape;

import android.app.Activity;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static matcher.MathdokuMatcher.notSameInstance;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class TickerTapeTest {
    private static final String ITEM_1 = "*** THE FIRST ITEM ***";
    private static final String ITEM_2 = "*** THE SECOND ITEM ***";
    private Activity activity;
    private TickerTape tickerTape;
    private TickerTapeUpdater tickerTapeUpdaterMock;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(Activity.class)
                .create()
                .get();

        tickerTape = new TickerTape(activity, null) {
            @Override
            TickerTapeUpdater createTickerTapeUpdaterRunnable() {
                tickerTapeUpdaterMock = mock(TickerTapeUpdater.class);
                return tickerTapeUpdaterMock;
            }
        };
        tickerTape.addItem(ITEM_1);
        tickerTape.addItem(ITEM_2);
    }

    @Test
    public void constructor_TickerTapeIsInvisible_True() throws Exception {
        assertThat(tickerTape.getVisibility(), is(View.INVISIBLE));
    }

    @Test
    public void addItem() throws Exception {
        String item = "*** ANOTHER ITEM ***";
        tickerTape.addItem(item);

        assertThatTickerTapeContainsItem(item);
    }

    private void assertThatTickerTapeContainsItem(String item) {
        View view = tickerTape.findViewWithTag(TickerTape.TAG_VIEW_TICKER_TAPE_LINEAR_LAYOUT);
        assertThat(view, is(notNullValue()));

        ArrayList<View> views = new ArrayList<View>();
        view.findViewsWithText(views, item, View.FIND_VIEWS_WITH_TEXT);
        assertThat(views.size(), is(1));
    }

    @Test
    public void disable_Success() throws Exception {
        assertThat(tickerTape.getVisibility(), is(View.INVISIBLE));
        tickerTape.disable();
        assertThat(tickerTape.getVisibility(), is(View.GONE));
    }

    @Test
    public void show_TickerTapeIsNotDisabled_TickerTapeUpdatedIsStarted() throws Exception {
        tickerTape.show();
        assertThat(tickerTapeUpdaterMock, is(notNullValue()));
        verify(tickerTapeUpdaterMock).startMoving();
    }

    @Test
    public void show_TickerTapeIsDisabled_TickerTapeUpdatedIsNotStarted() throws Exception {
        tickerTape.disable();
        tickerTape.show();
        assertThat(tickerTapeUpdaterMock, is(nullValue()));
    }

    @Test
    public void moveToNextPosition() throws Exception {
        // This method cannot be unit tested with Robolectric as it uses
        // computations based on the view width which are always 0 when using
        // current version of Robolectric.
    }

    @Test
    public void cancel() throws Exception {
        tickerTape.show();
        TickerTapeUpdater initialTickerTapeUpdater = tickerTapeUpdaterMock;
        tickerTape.cancel();
        tickerTape.show();
        assertThat(tickerTapeUpdaterMock, is(notSameInstance(initialTickerTapeUpdater)));
    }

    @Test
    public void isEraseConditionFulfilled_NoEraseConditionSet_False() throws Exception {
        int countItemsDisplayedCompletely = 123;
        long actualTimeDisplayed = 456;
        assertThat(tickerTape.isEraseConditionFulfilled(countItemsDisplayedCompletely, actualTimeDisplayed), is(false));
    }

    @Test
    public void isEraseConditionFulfilled_EraseConditionSetButTooFewItemsDisplayedCompletely_False() throws Exception {
        int numberOfItemsAddedDuringSetup = 2;
        int minDisplayCycles = 3;
        long minDisplayTime = 456;
        tickerTape.setEraseConditions(minDisplayCycles, minDisplayTime);

        int countItemsDisplayedCompletely = minDisplayCycles * numberOfItemsAddedDuringSetup - 1;
        long actualTimeDisplayed = minDisplayTime;
        assertThat(tickerTape.isEraseConditionFulfilled(countItemsDisplayedCompletely, actualTimeDisplayed), is(false));
    }

    @Test
    public void isEraseConditionFulfilled_EraseConditionSetButDisplayedTooShortly_False() throws Exception {
        int numberOfItemsAddedDuringSetup = 2;
        int minDisplayCycles = 3;
        long minDisplayTime = 456;
        tickerTape.setEraseConditions(minDisplayCycles, minDisplayTime);

        int countItemsDisplayedCompletely = minDisplayCycles * numberOfItemsAddedDuringSetup;
        long actualTimeDisplayed = minDisplayTime - 1;
        assertThat(tickerTape.isEraseConditionFulfilled(countItemsDisplayedCompletely, actualTimeDisplayed), is(false));
    }

    @Test
    public void isEraseConditionFulfilled_AllConditionsAreMatched_True() throws Exception {
        int numberOfItemsAddedDuringSetup = 2;
        int minDisplayCycles = 3;
        long minDisplayTime = 456;
        tickerTape.setEraseConditions(minDisplayCycles, minDisplayTime);

        int countItemsDisplayedCompletely = minDisplayCycles * numberOfItemsAddedDuringSetup;
        long actualTimeDisplayed = minDisplayTime;
        assertThat(tickerTape.isEraseConditionFulfilled(countItemsDisplayedCompletely, actualTimeDisplayed), is(true));
    }

    @Test
    public void reset() throws Exception {
        tickerTape.show();
        assertThat(tickerTape.getVisibility(), is(not(View.GONE)));
        tickerTape.reset();
        assertThat(tickerTape.getVisibility(), is(View.INVISIBLE));
        assertThat(tickerTape.getCurrentItem(), is(nullValue()));
    }
}
