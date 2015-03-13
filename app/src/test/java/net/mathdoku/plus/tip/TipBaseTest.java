package net.mathdoku.plus.tip;

import net.mathdoku.plus.Preferences;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TipBaseTest {
    protected Preferences preferencesMock = mock(Preferences.class);
    private boolean instantiatedBySubclass = false;
    private long minTimeIntervalBetweenTwoConsecutiveDisplays;
    private long tipLastDisplayTime;
    private String preferenceName;

    public void setUp() {
        // Reset static singleton for consecutive unit tests
        TipDialog.resetDisplayedDialogs();
    }

    protected void setUpTimeIntervalBetweenTwoConsecutiveDisplays(String preferenceName,
                                                                  long minTimeIntervalBetweenTwoConsecutiveDisplays) {
        instantiatedBySubclass = true;
        this.preferenceName = preferenceName;
        this.minTimeIntervalBetweenTwoConsecutiveDisplays = minTimeIntervalBetweenTwoConsecutiveDisplays;

        setIsDisplayedLongEnoughAgo();
    }

    /**
     * Sets the interval between the current invocation and the last time the dialog was actually displayed to the bare
     * minimum period. As a result the dialog should be displayed again (if all other conditions are matched as well).
     */
    private void setIsDisplayedLongEnoughAgo() {
        tipLastDisplayTime = System.currentTimeMillis() - minTimeIntervalBetweenTwoConsecutiveDisplays;
    }

    @Test
    public void toBeDisplayed_NoConditionsViolated_DialogIsDisplayed() throws Exception {
        if (instantiatedBySubclass) {
            initMocks();
            assertThatDialogToBeDisplayed(is(true));
        }
    }

    /**
     * Subclass should override and invoke this method in case additional mocks have to be instantiated.
     */
    protected void initMocks() {
        // Value below is not flexible in this unit test as it deals with
        // behavior of the super class.
        when(preferencesMock.getTipDisplayAgain(preferenceName)).thenReturn(true, true);
        when(preferencesMock.getTipLastDisplayTime(preferenceName)).thenReturn(tipLastDisplayTime);
    }

    protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
        // Unit test does not allow abstract class. Throw exception if method
        // not overridden.
        throw new IllegalStateException("Subclass should override method assertThatDialogToBeDisplayed.");
    }

    @Test
    public void toBeDisplayed_DisplayTooShortAgo_DialogIsNotDisplayed() throws Exception {
        if (instantiatedBySubclass) {
            setIsDisplayedNotLongEnoughAgo();
            initMocks();
            assertThatDialogToBeDisplayed(is(false));
        }
    }

    /**
     * Sets the interval between the current invocation and the last time the dialog was actually displayed to the
     * maximum period which is just too short for the dialog to be displayed again.
     */
    protected void setIsDisplayedNotLongEnoughAgo() {
        // Use a margin of 2 milliseconds to prevent false negative results.
        tipLastDisplayTime = System.currentTimeMillis() - minTimeIntervalBetweenTwoConsecutiveDisplays + 2;
    }
}
