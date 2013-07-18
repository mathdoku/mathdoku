package net.cactii.mathdoku.tip;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import android.content.Context;

public class TipBadCageMath extends TipDialog {

	private static String TIP_NAME = "Tip.TipBadCageMath.DisplayAgain";
	private static TipCategory TIP_CATEGORY = TipCategory.GAME_RULES;

	/**
	 * Creates a new tip dialog which explains that a duplicate value has been
	 * entered.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipBadCageMath(Context context) {
		super(context, TIP_NAME, TIP_CATEGORY);

		build(
				context.getResources().getString(
						R.string.dialog_tip_bad_cage_math_title),
				context.getResources().getString(
						R.string.dialog_tip_bad_cage_math_text), null).show();
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
		// Determine on basis of preferences whether the tip should be shown.
		return preferences.getDisplayTipAgain(TIP_NAME, TIP_CATEGORY);
	}

	/**
	 * Ensure that this tip will not be displayed (again).
	 */
	public static void doNotDisplayAgain(Preferences preferences) {
		// Determine on basis of preferences whether the tip should be shown.
		preferences.doNotDisplayTipAgain(TIP_NAME);
	}
}