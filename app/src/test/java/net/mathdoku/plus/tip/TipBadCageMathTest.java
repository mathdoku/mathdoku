package net.mathdoku.plus.tip;

import org.hamcrest.Matcher;
import org.junit.Before;

import static org.junit.Assert.assertThat;

public class TipBadCageMathTest extends TipBaseTest {
    // All fields below are defaulted to a value which result in method
    // toBeDisplayed to return true.

    @Before
    public void setUp() {
        super.setUp();
        long minTimeIntervalBetweenTwoConsecutiveDisplays = 5 * 60 * 1000;
        super.setUpTimeIntervalBetweenTwoConsecutiveDisplays("BadCageMath",
                                                             minTimeIntervalBetweenTwoConsecutiveDisplays);
    }

    @Override
    protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
        assertThat(TipBadCageMath.toBeDisplayed(preferencesMock), booleanMatcher);
    }
}
