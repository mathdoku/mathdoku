package net.mathdoku.plus.leaderboard.ui;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class LeaderboardOverviewListItemTest {
    // The value of the leaderboard ids below should be equal to the value of
    // corresponding resource in the google_play_services.xml. Note that the
    // content of this file differs in DEVELOPMENT versus PRODUCTION MODE.
    public static final String LEADERBOARD_ID_GRID6X6_VISIBLE_OPERATORS_DIFFICULT_DEVELOPMENT_MODE =
            "CgkIntyrg5wIEAIQNA";
    public static final String LEADERBOARD_ID_GRID6X6_VISIBLE_OPERATORS_DIFFICULT_PRODUCTION_MODE =
            "CgkIhIjt-ZwMEAIQLw";
    public static final String LEADERBOARD_ID_GRID9X9_HIDDEN_OPERATORS_DIFFICULT_DEVELOPMENT_MODE =
            "CgkIntyrg5wIEAIQTQ";
    public static final String LEADERBOARD_ID_GRID9X9_HIDDEN_OPERATORS_DIFFICULT_PRODUCTION_MODE = "CgkIhIjt-ZwMEAIQSA";

    private Activity activity;
    private LeaderboardOverviewListItem leaderboardOverviewListItem;
    private LeaderboardOverview leaderboardOverview;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class)
                .create()
                .get();
        leaderboardOverview = mock(LeaderboardOverview.class);
        //when(leaderboardOverview.getActivity()).thenReturn(activity.getApplicationContext());
        when(leaderboardOverview.getContext()).thenReturn(activity);//.getApplicationContext());
        when(leaderboardOverview.getLayoutInflater()).thenReturn(activity.getLayoutInflater());
        when(leaderboardOverview.getResources()).thenReturn(activity.getResources());
    }

    @Test
    public void constructor_LeaderboardWithRankingInformationInLocalDatabase_VisibilityOfChildViewsIsCorrect() throws
            Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndVisibleOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE);
        assertThatFieldIsVisible(leaderboardOverviewListItem, R.id.leaderboard_score_label);
        assertThatFieldIsVisible(leaderboardOverviewListItem, R.id.leaderboard_score_display);
        assertThatFieldIsGone(leaderboardOverviewListItem, R.id.leaderboard_not_played);
        assertThatFieldIsVisible(leaderboardOverviewListItem, R.id.leaderboard_rank_display);
        assertThat(leaderboardOverviewListItem.hasNoScore(), is(false));
    }

    private LeaderboardOverviewListItem createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndVisibleOperators
            (LeaderboardRankDatabaseAdapter.ScoreOrigin scoreOrigin) {
        LeaderboardRankRow leaderboardRankRowMock = mock(LeaderboardRankRow.class);
        when(leaderboardRankRowMock.getScoreOrigin()).thenReturn(scoreOrigin);

        final LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapterMock = mock(
                LeaderboardRankDatabaseAdapter.class);
        String leaderboardIdGrid6x6OperatorsVisibleAndComplexityDifficult = (Config.APP_MODE == Config.AppMode
                .DEVELOPMENT ? LEADERBOARD_ID_GRID6X6_VISIBLE_OPERATORS_DIFFICULT_DEVELOPMENT_MODE :
                LEADERBOARD_ID_GRID6X6_VISIBLE_OPERATORS_DIFFICULT_PRODUCTION_MODE);
        when(leaderboardRankDatabaseAdapterMock.get(
                     leaderboardIdGrid6x6OperatorsVisibleAndComplexityDifficult)).thenReturn(leaderboardRankRowMock);

        return new LeaderboardOverviewListItem(leaderboardOverview, GridType.GRID_6X6.getGridSize(), false,
                                               PuzzleComplexity.DIFFICULT) {
            @Override
            LeaderboardRankDatabaseAdapter createLeaderboardRankDatabaseAdapter() {
                return leaderboardRankDatabaseAdapterMock;
            }
        };
    }

    private void assertThatFieldIsGone(LeaderboardOverviewListItem leaderboardOverviewListItem, int resId) {
        assertThat(leaderboardOverviewListItem.getView()
                           .findViewById(resId)
                           .getVisibility(), is(View.GONE));
    }

    private void assertThatFieldIsVisible(LeaderboardOverviewListItem leaderboardOverviewListItem, int resId) {
        assertThat(leaderboardOverviewListItem.getView()
                           .findViewById(resId)
                           .getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void constructor_LeaderboardWithRankingInformationOnGooglePlayServices_VisibilityOfChildViewsIsSetCorrect
            () throws Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndVisibleOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.EXTERNAL);
        assertThatFieldIsVisible(leaderboardOverviewListItem, R.id.leaderboard_score_label);
        assertThatFieldIsVisible(leaderboardOverviewListItem, R.id.leaderboard_score_display);
        assertThatFieldIsGone(leaderboardOverviewListItem, R.id.leaderboard_not_played);
        assertThatFieldIsVisible(leaderboardOverviewListItem, R.id.leaderboard_rank_display);
    }

    @Test
    public void constructor_LeaderboardWithoutRankingInformation_ThrowsExceptionOnHasNoScore() throws Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndVisibleOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.NONE);
        assertThatFieldIsGone(leaderboardOverviewListItem, R.id.leaderboard_score_label);
        assertThatFieldIsGone(leaderboardOverviewListItem, R.id.leaderboard_score_display);
        assertThatFieldIsVisible(leaderboardOverviewListItem, R.id.leaderboard_not_played);
        assertThatFieldIsGone(leaderboardOverviewListItem, R.id.leaderboard_rank_display);
        assertThat(leaderboardOverviewListItem.hasNoScore(), is(true));
    }

    @Test
    public void constructor_LeaderboardPlaceHolder_PlaceHolderContainsCorrectGridSize() throws Exception {
        leaderboardOverviewListItem = new LeaderboardOverviewListItem(leaderboardOverview,
                                                                      GridType.GRID_6X6.getGridSize());
        assertThat(((TextView) leaderboardOverviewListItem.getView()).getText()
                           .toString(),
                   is("You have not yet played any puzzles of size 6. Change the filter above to view the " +
                              "leaderboards for other users."));
    }

    @Test
    public void setVisibility_SetToGone_ViewIsGone() throws Exception {
        leaderboardOverviewListItem = new LeaderboardOverviewListItem(leaderboardOverview,
                                                                      GridType.GRID_6X6.getGridSize());
        leaderboardOverviewListItem.setVisibility(View.GONE);
        assertThat(leaderboardOverviewListItem.getView()
                           .getVisibility(), is(View.GONE));
    }

    @Test
    public void setVisibility_SetToVisible_ViewIsVisible() throws Exception {
        leaderboardOverviewListItem = new LeaderboardOverviewListItem(leaderboardOverview,
                                                                      GridType.GRID_6X6.getGridSize());
        leaderboardOverviewListItem.setVisibility(View.VISIBLE);
        assertThat(leaderboardOverviewListItem.getView()
                           .getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void hasNoScore_LeaderboardWithRankingInformationInLocalDatabase_False() throws Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndVisibleOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE);
        assertThat(leaderboardOverviewListItem.hasNoScore(), is(false));
    }

    @Test
    public void hasNoScore_LeaderboardWithRankingInformationOnGooglePlayServices_True() throws Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndVisibleOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.EXTERNAL);
        assertThat(leaderboardOverviewListItem.hasNoScore(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void hasNoScore_LeaderboardPlaceHolder_ThrowsExceptionOnHasNoScore() throws Exception {
        leaderboardOverviewListItem = new LeaderboardOverviewListItem(leaderboardOverview,
                                                                      GridType.GRID_6X6.getGridSize());
        leaderboardOverviewListItem.hasNoScore();
    }

    @Test
    public void hasHiddenOperators_LeaderboardWithVisibleOperatorsAndRankingInformationInLocalDatabase_False() throws
            Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndVisibleOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE);
        assertThat(leaderboardOverviewListItem.hasHiddenOperators(), is(false));
    }

    @Test
    public void hasHiddenOperators_LeaderboardWithHiddenOperatorsAndRankingInformationInLocalDatabase_False() throws
            Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndHiddenOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.LOCAL_DATABASE);
        assertThat(leaderboardOverviewListItem.hasHiddenOperators(), is(true));
    }

    private LeaderboardOverviewListItem createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndHiddenOperators
            (LeaderboardRankDatabaseAdapter.ScoreOrigin scoreOrigin) {
        LeaderboardRankRow leaderboardRankRowMock = mock(LeaderboardRankRow.class);
        when(leaderboardRankRowMock.getScoreOrigin()).thenReturn(scoreOrigin);

        final LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapterMock = mock(
                LeaderboardRankDatabaseAdapter.class);
        String leaderboardIdGrid9x9OperatorsHiddenAndComplexityDifficult = (Config.APP_MODE == Config.AppMode
                .DEVELOPMENT ? LEADERBOARD_ID_GRID9X9_HIDDEN_OPERATORS_DIFFICULT_DEVELOPMENT_MODE :
                LEADERBOARD_ID_GRID9X9_HIDDEN_OPERATORS_DIFFICULT_PRODUCTION_MODE);
        when(leaderboardRankDatabaseAdapterMock.get(
                     leaderboardIdGrid9x9OperatorsHiddenAndComplexityDifficult)).thenReturn(leaderboardRankRowMock);

        return new LeaderboardOverviewListItem(leaderboardOverview, GridType.GRID_9X9.getGridSize(), true,
                                               PuzzleComplexity.DIFFICULT) {
            @Override
            LeaderboardRankDatabaseAdapter createLeaderboardRankDatabaseAdapter() {
                return leaderboardRankDatabaseAdapterMock;
            }
        };
    }

    @Test
    public void hasHiddenOperators_LeaderboardWithWithVisibleOperatorsAndRankingInformationOnGooglePlayServices_True() throws Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndVisibleOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.EXTERNAL);
        assertThat(leaderboardOverviewListItem.hasHiddenOperators(), is(false));
    }

    @Test
    public void hasHiddenOperators_LeaderboardWithWithHiddenOperatorsAndRankingInformationOnGooglePlayServices_True() throws Exception {
        leaderboardOverviewListItem = createLeaderboardOverviewListItemBasedOnLeaderboardRankRowAndHiddenOperators(
                LeaderboardRankDatabaseAdapter.ScoreOrigin.EXTERNAL);
        assertThat(leaderboardOverviewListItem.hasHiddenOperators(), is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void hasHiddenOperators_LeaderboardPlaceHolder_ThrowsExceptionOnHasNoScore() throws Exception {
        leaderboardOverviewListItem = new LeaderboardOverviewListItem(leaderboardOverview,
                                                                      GridType.GRID_9X9.getGridSize());
        leaderboardOverviewListItem.hasHiddenOperators();
    }
}
