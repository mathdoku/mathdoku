package net.mathdoku.plus.tip;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;

import android.content.Context;

public class TipDuplicateValue extends TipDialog {

	private static final String TIP_NAME = "Tip.TipDuplicateValue.DisplayAgain";
	private static final TipPriority TIP_PRIORITY = TipPriority.MEDIUM;

	/**
	 * Creates a new tip dialog which explains that a duplicate value has been
	 * entered.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipDuplicateValue(Context context) {
		super(context, TIP_NAME, TIP_PRIORITY);

		build(
				R.drawable.alert,
				context.getResources().getString(
						R.string.dialog_tip_duplicate_value_title),
				context.getResources().getString(
						R.string.dialog_tip_duplicate_value_text), null).show();
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
		// Do not display in case it was displayed less than 2 minutes ago.
		if (preferences.getTipLastDisplayTime(TIP_NAME) > System
				.currentTimeMillis() - (2 * 60 * 1000)) {
			return false;
		}

		// Determine on basis of preferences whether the tip should be shown.
		return TipDialog
				.getDisplayTipAgain(preferences, TIP_NAME, TIP_PRIORITY);
	}
}