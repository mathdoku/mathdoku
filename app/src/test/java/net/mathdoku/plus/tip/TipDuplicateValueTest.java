package net.mathdoku.plus.tip;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class TipDuplicateValueTest extends TipBaseTest {
    @Before
    public void setUp() {
        super.setUp();
        long minTimeIntervalBetweenTwoConsecutiveDisplays = 2 * 60 * 1000;
        super.setUpTimeIntervalBetweenTwoConsecutiveDisplays("DuplicateValue",
                                                             minTimeIntervalBetweenTwoConsecutiveDisplays);
    }

    @Override
    protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
        assertThat(TipDuplicateValue.toBeDisplayed(preferencesMock), booleanMatcher);
    }
}
