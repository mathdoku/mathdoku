package net.mathdoku.plus.tip;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class TipStatisticsTest extends TipBaseTest {
    // All fields below are defaulted to a value which result in method
    // toBeDisplayed to return true.
    private boolean statisticsAvailable = true;

    @Before
    public void setUp() {
        super.setUp();
        long minTimeIntervalBetweenTwoConsecutiveDisplays = 12 * 60 * 60 * 1000;
        super.setUpTimeIntervalBetweenTwoConsecutiveDisplays("Statistics",
                                                             minTimeIntervalBetweenTwoConsecutiveDisplays);
    }

    @Override
    protected void initMocks() {
        super.initMocks();
        when(preferencesMock.isStatisticsAvailable()).thenReturn(statisticsAvailable);
    }

    @Override
    protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
        assertThat(TipStatistics.toBeDisplayed(preferencesMock), booleanMatcher);
    }

    @Test
    public void toBeDisplayed_StatisticsNotAvailable_DialogIsNotDisplayed() throws Exception {
        statisticsAvailable = false;
        initMocks();
        assertThatDialogToBeDisplayed(is(false));
    }
}
