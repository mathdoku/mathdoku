package net.mathdoku.plus.tip;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import android.content.Context;

public class TipLeaderboardViewDetails extends TipDialog {

	private static final String TIP_NAME = "Tip.LeaderboardViewDetails.DisplayAgain";
	private static final TipPriority TIP_PRIORITY = TipPriority.LOW;

	/**
	 * Creates a new tip dialog which explains that leaderboards can be tapped
	 * to view more details.</br>
	 * <p/>
	 * For performance reasons this method should only be called in case the
	 * static call to method {@link #toBeDisplayed} returned true.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipLeaderboardViewDetails(Context context) {
		super(context, TIP_NAME, TIP_PRIORITY);

		build(
				R.drawable.lightbulb,
				context.getResources().getString(
						R.string.dialog_tip_leaderboard_view_details_title),
				context.getResources().getString(
						R.string.dialog_tip_leaderboard_view_details_text),
				null).show();
	}

	/**
	 * Checks whether this tip has to be displayed. Should be called statically
	 * before creating this object.
	 * 
	 * @param preferences
	 *            Preferences of the activity for which has to be checked
	 *            whether this tip should be shown.
	 * @return True in case the tip might be displayed. False otherwise.
	 */
	public static boolean toBeDisplayed(Preferences preferences) {
		// Do not display in case the view leaderboards function has been used
		if (preferences.getLeaderboardsDetailsViewed() >= 1) {
			return false;
		}

		// Do not display in case this is the first time the leaderboard
		// overview is displayed.
		if (preferences.getLeaderboardsOverviewViewed() <= 1) {
			return false;
		}

		// Do not display in case it was displayed less than 2 days ago.
		if (preferences.getTipLastDisplayTime(TIP_NAME) > System
				.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)) {
			return false;
		}

		// Determine on basis of preferences whether the tip should be shown.
		return TipDialog
				.getDisplayTipAgain(preferences, TIP_NAME, TIP_PRIORITY);
	}
}
