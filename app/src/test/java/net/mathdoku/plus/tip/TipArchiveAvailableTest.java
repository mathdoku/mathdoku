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
public class TipArchiveAvailableTest extends TipBaseTest {
    // All fields below are defaulted to a value which result in method
    // toBeDisplayed to return true.
    private boolean archiveAvailable = true;

    @Before
    public void setUp() {
        super.setUp();
        long minTimeIntervalBetweenTwoConsecutiveDisplays = 12 * 60 * 60 * 1000;
        super.setUpTimeIntervalBetweenTwoConsecutiveDisplays("ArchiveAvailable",
                                                             minTimeIntervalBetweenTwoConsecutiveDisplays);
    }

    @Override
    protected void initMocks() {
        super.initMocks();
        when(preferencesMock.isArchiveAvailable()).thenReturn(archiveAvailable);
    }

    @Override
    protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
        assertThat(TipArchiveAvailable.toBeDisplayed(preferencesMock), booleanMatcher);
    }

    @Test
    public void toBeDisplayed_ArchiveIsNotYetAvailable_DialogIsNotDisplayed() throws Exception {
        archiveAvailable = false;
        initMocks();
        assertThatDialogToBeDisplayed(is(false));
    }
}
