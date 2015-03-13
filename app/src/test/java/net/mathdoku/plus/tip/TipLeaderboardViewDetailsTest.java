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
public class TipLeaderboardViewDetailsTest extends TipBaseTest {
    // All fields below are defaulted to a value which result in method
    // toBeDisplayed to return true.
    private int leaderboardDetailsViewed = 0;
    private int leaderBoardsOverviewViewed = 2;

    @Before
    public void setUp() {
        super.setUp();
        long minTimeIntervalBetweenTwoConsecutiveDisplays = 2 * 24 * 60 * 60 * 1000;
        super.setUpTimeIntervalBetweenTwoConsecutiveDisplays("LeaderboardViewDetails",
                                                             minTimeIntervalBetweenTwoConsecutiveDisplays);
    }

    @Override
    protected void initMocks() {
        super.initMocks();
        when(preferencesMock.getLeaderboardsDetailsViewed()).thenReturn(leaderboardDetailsViewed);
        when(preferencesMock.getLeaderboardsOverviewViewed()).thenReturn(leaderBoardsOverviewViewed);
    }

    @Override
    protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
        assertThat(TipLeaderboardViewDetails.toBeDisplayed(preferencesMock), booleanMatcher);
    }

    @Test
    public void toBeDisplayed_TooManyLeaderboardHaveBeenCreated_DialogIsNotDisplayed() throws Exception {
        leaderboardDetailsViewed = 1;
        initMocks();
        assertThatDialogToBeDisplayed(is(false));
    }

    @Test
    public void toBeDisplayed_TooFewLeaderboardOverviewsHaveBeenViewed_DialogIsNotDisplayed() throws Exception {
        leaderBoardsOverviewViewed = 1;
        initMocks();
        assertThatDialogToBeDisplayed(is(false));
    }
}
