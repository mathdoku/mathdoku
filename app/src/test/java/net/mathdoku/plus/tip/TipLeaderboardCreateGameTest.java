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
public class TipLeaderboardCreateGameTest extends TipBaseTest {
    // All fields below are defaulted to a value which result in method
    // toBeDisplayed to return true.
    private int leaderBoardsCreated = 0;
    private int leaderBoardsOverviewViewed = 6;

    @Before
    public void setUp() {
        super.setUp();
        long minTimeIntervalBetweenTwoConsecutiveDisplays = 2 * 24 * 60 * 60 * 1000;
        super.setUpTimeIntervalBetweenTwoConsecutiveDisplays("LeaderboardCreateGame",
                                                             minTimeIntervalBetweenTwoConsecutiveDisplays);
    }

    @Override
    protected void initMocks() {
        super.initMocks();
        when(preferencesMock.getLeaderboardsGamesCreated()).thenReturn(leaderBoardsCreated);
        when(preferencesMock.getLeaderboardsOverviewViewed()).thenReturn(leaderBoardsOverviewViewed);
    }

    @Override
    protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
        assertThat(TipLeaderboardCreateGame.toBeDisplayed(preferencesMock), booleanMatcher);
    }

    @Test
    public void toBeDisplayed_TooManyLeaderboardHaveBeenCreated_DialogIsNotDisplayed() throws Exception {
        leaderBoardsCreated = 1;
        initMocks();
        assertThatDialogToBeDisplayed(is(false));
    }

    @Test
    public void toBeDisplayed_TooFewLeaderboardOverviewsHaveBeenViewed_DialogIsNotDisplayed() throws Exception {
        leaderBoardsOverviewViewed = 5;
        initMocks();
        assertThatDialogToBeDisplayed(is(false));
    }
}
