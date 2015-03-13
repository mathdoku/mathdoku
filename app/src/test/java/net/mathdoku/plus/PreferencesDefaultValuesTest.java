package net.mathdoku.plus;

import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.leaderboard.ui.LeaderboardOverviewActivity;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.puzzle.ui.GridInputMode;
import net.mathdoku.plus.util.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * This class tests the default values of the preferences after a clean install of the latest version of the app.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class PreferencesDefaultValuesTest {
    private Preferences preferences;

    @Before
    public void setUp() throws Exception {
        TestRunnerHelper.setup(this.getClass()
                                       .getCanonicalName());
        preferences = Preferences.getInstance(TestRunnerHelper.getActivity());
        setDefaultPreferences();
    }

    protected void setDefaultPreferences() {
        // todo: Preference class should determine current version id instead of
        // using a passed value.
        preferences.upgrade(-1, Util.getPackageVersionNumber());
    }

    @After
    public void tearDown() throws Exception {
        TestRunnerHelper.tearDown();
    }

    @Test
    public void getCurrentInstalledVersion() throws Exception {
        assertThat(preferences.getCurrentInstalledVersion(), is(599));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isArchiveAvailable() throws Exception {
        assertThat(preferences.isArchiveAvailable(), is(false));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void getArchiveGridIdLastShowed_GetSetGet() throws Exception {
        assertThat(preferences.getArchiveGridIdLastShowed(), is(-1));
        preferences.setArchiveGridIdLastShowed(156);
        assertThat(preferences.getArchiveGridIdLastShowed(), is(156));
    }

    @Test
    public void getArchiveSizeFilterLastValueUsed_GetSetGet() throws Exception {
        assertThat(preferences.getArchiveSizeFilterLastValueUsed(), is(GridTypeFilter.ALL));
        preferences.setArchiveSizeFilterLastValueUsed(GridTypeFilter.GRID_7X7);
        assertThat(preferences.getArchiveSizeFilterLastValueUsed(), is(GridTypeFilter.GRID_7X7));
    }

    @Test
    public void getArchiveStatusFilterLastValueUsed_GetSetGet() throws Exception {
        assertThat(preferences.getArchiveStatusFilterLastValueUsed(), is(StatusFilter.ALL));
        preferences.setArchiveStatusFilterLastValueUsed(StatusFilter.UNFINISHED);
        assertThat(preferences.getArchiveStatusFilterLastValueUsed(), is(StatusFilter.UNFINISHED));
    }

    @Test
    public void isArchiveSizeFilterVisible() throws Exception {
        assertThat(preferences.isArchiveSizeFilterVisible(), is(false));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isArchiveStatusFilterVisible() throws Exception {
        assertThat(preferences.isArchiveStatusFilterVisible(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isArchiveChartDescriptionVisible() throws Exception {
        assertThat(preferences.isArchiveChartDescriptionVisible(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isLeaderboardsInitialized_GetSetGet() throws Exception {
        assertThat(preferences.isLeaderboardsInitialized(), is(false));
        preferences.setLeaderboardsInitialized();
        assertThat(preferences.isLeaderboardsInitialized(), is(true));
    }

    @Test
    public void getLeaderboardFilterLastValueUsed_GetSetGet() throws Exception {
        assertThat(preferences.getLeaderboardFilterLastValueUsed(),
                   is(LeaderboardOverviewActivity.LeaderboardFilter.ALL_LEADERBOARDS));
        preferences.setLeaderboardFilterLastValueUsed(LeaderboardOverviewActivity.LeaderboardFilter.MY_LEADERBOARDS);
        assertThat(preferences.getLeaderboardFilterLastValueUsed(),
                   is(LeaderboardOverviewActivity.LeaderboardFilter.MY_LEADERBOARDS));
    }

    @Test
    public void getLeaderboardsTabLastDisplayed_GetSetGet() throws Exception {
        assertThat(preferences.getLeaderboardsTabLastDisplayed(), is(0));
        preferences.setLeaderboardsTabLastDisplayed(456);
        assertThat(preferences.getLeaderboardsTabLastDisplayed(), is(456));
    }

    @Test
    public void getLeaderboardsDetailsViewed_GetSetGet() throws Exception {
        assertThat(preferences.getLeaderboardsDetailsViewed(), is(0));
        preferences.increaseLeaderboardsDetailsViewed();
        preferences.increaseLeaderboardsDetailsViewed();
        assertThat(preferences.getLeaderboardsDetailsViewed(), is(2));
    }

    @Test
    public void getLeaderboardsGamesCreated_GetSetGet() throws Exception {
        assertThat(preferences.getLeaderboardsGamesCreated(), is(0));
        preferences.increaseLeaderboardsGamesCreated();
        preferences.increaseLeaderboardsGamesCreated();
        assertThat(preferences.getLeaderboardsGamesCreated(), is(2));
    }

    @Test
    public void getLeaderboardsOverviewViewed_GetSetGet() throws Exception {
        assertThat(preferences.getLeaderboardsOverviewViewed(), is(0));
        preferences.increaseLeaderboardsOverviewViewed();
        preferences.increaseLeaderboardsOverviewViewed();
        assertThat(preferences.getLeaderboardsOverviewViewed(), is(2));
    }

    @Test
    public void isHideTillNextTopScoreAchievedChecked_GetSetGet() throws Exception {
        assertThat(preferences.isHideTillNextTopScoreAchievedChecked(), is(false));
        preferences.setHideTillNextTopScoreAchievedChecked(true);
        assertThat(preferences.isHideTillNextTopScoreAchievedChecked(), is(true));
    }

    @Test
    public void getInputModeChangedCounter_GetSetGet() throws Exception {
        assertThat(preferences.getInputModeChangedCounter(), is(0));
        preferences.increaseInputModeChangedCounter();
        preferences.increaseInputModeChangedCounter();
        assertThat(preferences.getInputModeChangedCounter(), is(2));
    }

    @Test
    public void getInputModeCopyCounter_GetSetGet() throws Exception {
        assertThat(preferences.getInputModeCopyCounter(), is(0));
        preferences.increaseInputModeCopyCounter();
        preferences.increaseInputModeCopyCounter();
        assertThat(preferences.getInputModeCopyCounter(), is(2));
    }

    @Test
    public void getGridInputMode_GetSetGet() throws Exception {
        assertThat(preferences.getGridInputMode(), is(GridInputMode.NORMAL));
        preferences.setGridInputMode(true, GridInputMode.MAYBE);
        assertThat(preferences.getGridInputMode(), is(GridInputMode.MAYBE));
    }

    @Test
    public void isGridInputModeCopyEnabled_GetSetGet() throws Exception {
        assertThat(preferences.isGridInputModeCopyEnabled(), is(false));
        preferences.setGridInputMode(true, GridInputMode.MAYBE);
        assertThat(preferences.isGridInputModeCopyEnabled(), is(true));
    }

    @Test
    public void getPuzzleParameterComplexity_GetSetGet() throws Exception {
        assertThat(preferences.getPuzzleParameterComplexity(), is(PuzzleComplexity.VERY_EASY));
        preferences.setPuzzleParameterComplexity(PuzzleComplexity.DIFFICULT);
        assertThat(preferences.getPuzzleParameterComplexity(), is(PuzzleComplexity.DIFFICULT));
    }

    @Test
    public void getPuzzleParameterOperatorsVisible_GetSetGet() throws Exception {
        assertThat(preferences.getPuzzleParameterOperatorsVisible(), is(true));
        preferences.setPuzzleParameterOperatorsVisible(false);
        assertThat(preferences.getPuzzleParameterOperatorsVisible(), is(false));
    }

    @Test
    public void getPuzzleParameterGridSize_GetSetGet() throws Exception {
        assertThat(preferences.getPuzzleParameterGridSize(), is(GridType.GRID_4X4));
        preferences.setPuzzleParameterGridSize(GridType.GRID_3X3);
        assertThat(preferences.getPuzzleParameterGridSize(), is(GridType.GRID_3X3));
    }

    @Test
    public void isBadCageMathHighlightVisible() throws Exception {
        assertThat(preferences.isBadCageMathHighlightVisible(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isPuzzleSettingClearMaybesEnabled() throws Exception {
        assertThat(preferences.isPuzzleSettingClearMaybesEnabled(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isColoredDigitsVisible() throws Exception {
        assertThat(preferences.isColoredDigitsVisible(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isFullScreenEnabled() throws Exception {
        assertThat(preferences.isFullScreenEnabled(), is(false));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void getDigitInputMethod_GetSetGet() throws Exception {
        assertThat(preferences.getDigitInputMethod(), is(Preferences.PuzzleSettingInputMethod.SWIPE_ONLY));
        preferences.setDigitInputMethod(false, true);
        assertThat(preferences.getDigitInputMethod(), is(Preferences.PuzzleSettingInputMethod.BUTTONS_ONLY));
    }

    @Test
    public void getDigitInputMethod_EnableSwipeAndButtons_SetGet() throws Exception {
        preferences.setDigitInputMethod(true, true);
        assertThat(preferences.getDigitInputMethod(), is(Preferences.PuzzleSettingInputMethod.SWIPE_AND_BUTTONS));
    }

    @Test
    public void getDigitInputMethod_DisableSwipeAndButtons_DigitInputModeNotChanged() throws Exception {
        preferences.setDigitInputMethod(false, false);
        assertThat(preferences.getDigitInputMethod(), is(Preferences.PuzzleSettingInputMethod.SWIPE_AND_BUTTONS));
    }

    @Test
    public void isMaybesDisplayedInGrid() throws Exception {
        assertThat(preferences.isMaybesDisplayedInGrid(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void getOuterSwipeCircleVisibility() throws Exception {
        assertThat(preferences.getOuterSwipeCircleVisibility(), is("4"));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isOuterSwipeCircleNeverVisible_GetSetGet() throws Exception {
        assertThat(preferences.isOuterSwipeCircleNeverVisible(), is(false));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isPlaySoundEffectEnabled() throws Exception {
        assertThat(preferences.isPlaySoundEffectEnabled(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void getTheme() throws Exception {
        assertThat(preferences.getTheme(), is(Painter.GridTheme.LIGHT));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isTimerVisible() throws Exception {
        assertThat(preferences.isTimerVisible(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isWakeLockEnabled() throws Exception {
        assertThat(preferences.isWakeLockEnabled(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void isStatisticsAvailable_GetSetGet() throws Exception {
        assertThat(preferences.isStatisticsAvailable(), is(false));
        preferences.setStatisticsAvailable();
        assertThat(preferences.isStatisticsAvailable(), is(true));
    }

    @Test
    public void isStatisticsChartDescriptionVisible_GetSetGet() throws Exception {
        assertThat(preferences.isStatisticsChartDescriptionVisible(), is(true));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void getStatisticsSettingElapsedTimeChartMaximumGames_GetSetGet() throws Exception {
        assertThat(preferences.getStatisticsSettingElapsedTimeChartMaximumGames(), is(100));
        // Class preferences does not contain a setter method for this preference.
    }

    @Test
    public void getStatisticsTabLastDisplayed_GetSetGet() throws Exception {
        assertThat(preferences.getStatisticsTabLastDisplayed(), is(-1));
        preferences.setStatisticsTabLastDisplayed(985);
        assertThat(preferences.getStatisticsTabLastDisplayed(), is(985));
    }

    @Test
    public void increaseSwipeInvalidMotionCounter_GetSetGet() throws Exception {
        assertThat(getSwipeInvalidMotionCounter(), is(0));
        preferences.increaseSwipeInvalidMotionCounter();
        preferences.increaseSwipeInvalidMotionCounter();
        preferences.commitCounters();
        assertThat(getSwipeInvalidMotionCounter(), is(2));
    }

    private int getSwipeInvalidMotionCounter() {
        // Class preferences does not contain a getter method for this preference.
        return preferences.mSharedPreferences.getInt("swipe_invalid_motion_counter", -999);
    }

    @Test
    public void increaseSwipeValidMotionCounter_GetSetGet() throws Exception {
        assertThat(getSwipeValidMotionCounter("swipe_digit_3_counter"), is(0));
        preferences.increaseSwipeValidMotionCounter(3);
        preferences.increaseSwipeValidMotionCounter(3);
        preferences.commitCounters();
        assertThat(getSwipeValidMotionCounter("swipe_digit_3_counter"), is(2));
    }

    private int getSwipeValidMotionCounter(String swipeCounter) {
        // Class preferences does not contain a getter method for this preference.
        return preferences.mSharedPreferences.getInt(swipeCounter, -999);
    }
}
