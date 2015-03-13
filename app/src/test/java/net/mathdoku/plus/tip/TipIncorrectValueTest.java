package net.mathdoku.plus.tip;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class TipIncorrectValueTest extends TipBaseTest {
    @Before
    public void setUp() {
        super.setUp();
        long minTimeIntervalBetweenTwoConsecutiveDisplays = 0;
        super.setUpTimeIntervalBetweenTwoConsecutiveDisplays("IncorrectValue",
                                                             minTimeIntervalBetweenTwoConsecutiveDisplays);
    }

    @Override
    protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
        assertThat(TipIncorrectValue.toBeDisplayed(preferencesMock), booleanMatcher);
    }

    @Override
    public void toBeDisplayed_DisplayTooShortAgo_DialogIsNotDisplayed() {
        // Ignore this test as this tip will not be checked on time since last
        // being displayed.
    }

    @Test
    public void doNotDisplayAgain() throws Exception {
        TipIncorrectValue.doNotDisplayAgain(preferencesMock);
        verify(preferencesMock).setTipDoNotDisplayAgain("IncorrectValue");
    }
}
