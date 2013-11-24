package net.mathdoku.plus.tip;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;

import android.content.Context;

public class TipStatistics extends TipDialog {

	private static final String TIP_NAME = "Tip.TipStatistics.DisplayAgain";
	private static final TipPriority TIP_PRIORITY = TipPriority.LOW;

	/**
	 * Creates a new tip dialog which explains that the statistics have been
	 * made available.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipStatistics(Context context) {
		super(context, TIP_NAME, TIP_PRIORITY);

		build(
				R.drawable.lightbulb,
				context.getResources().getString(
						R.string.dialog_tip_statistics_title),
				context.getResources().getString(
						R.string.dialog_tip_statistics_text), null).show();
	}

	/**
	 * Checks whether this tip has to be displayed. Should be called statically
	 * before creating this object.
	 * 
	 * @param preferences
	 *            Preferences of the activity for which has to be checked
	 *            whether this tip should be shown.
	 * @return
	 */
	public static boolean toBeDisplayed(Preferences preferences) {
		// Do not display in case statistics are not yet available.
		if (preferences.isStatisticsAvailable() == false) {
			return false;
		}

		// Do not display in case it was displayed less than 12 hours ago
		if (preferences.getTipLastDisplayTime(TIP_NAME) > System
				.currentTimeMillis() - (12 * 60 * 60 * 1000)) {
			return false;
		}

		// Determine on basis of preferences whether the tip should be shown.
		return TipDialog
				.getDisplayTipAgain(preferences, TIP_NAME, TIP_PRIORITY);
	}
}