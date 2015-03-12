package net.mathdoku.plus.tip;

import android.content.Context;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;

public class TipLeaderboardCreateGame extends TipDialog {

    private static final String TIP_NAME = "LeaderboardCreateGame";
    private static final TipPriority TIP_PRIORITY = TipPriority.LOW;

    /**
     * Creates a new tip dialog which explains that leaderboards can be long tapped to create a new
     * game for the leaderboard.</br>
     * <p/>
     * For performance reasons this method should only be called in case the static call to method
     * {@link #toBeDisplayed} returned true.
     *
     * @param context
     *         The activity in which this tip has to be shown.
     */
    public TipLeaderboardCreateGame(Context context) {
        super(context, TIP_NAME, TIP_PRIORITY);

        build(R.drawable.lightbulb, context.getResources()
                      .getString(R.string.dialog_tip_leaderboard_create_game_details_title),
              context.getResources()
                      .getString(R.string.dialog_tip_leaderboard_create_game_text), null);
    }

    /**
     * Checks whether this tip has to be displayed. Should be called statically before creating this
     * object.
     *
     * @param preferences
     *         Preferences of the activity for which has to be checked whether this tip should be
     *         shown.
     * @return True in case the tip might be displayed. False otherwise.
     */
    public static boolean toBeDisplayed(Preferences preferences) {
        // Do not display in case the create game function has been used
        if (preferences.getLeaderboardsGamesCreated() >= 1) {
            return false;
        }

        // Do not display in case the leaderboard overview has been used a few
        // times only.
        if (preferences.getLeaderboardsOverviewViewed() <= 5) {
            return false;
        }

        // Do not display in case it was displayed less than 2 days ago.
        if (preferences.getTipLastDisplayTime(
                TIP_NAME) > System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000) {
            return false;
        }

        // Determine on basis of preferences whether the tip should be shown.
        return TipDialog.getDisplayTipAgain(preferences, TIP_NAME, TIP_PRIORITY);
    }
}
