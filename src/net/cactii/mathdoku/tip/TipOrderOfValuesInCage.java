package net.cactii.mathdoku.tip;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.grid.GridCage;
import android.content.Context;

public class TipOrderOfValuesInCage extends TipDialog {

	public static String TIP_NAME = "Tip.OrderOfValuesInCage.DisplayAgain";
	private static TipPriority TIP_PRIORITY = TipPriority.LOW;

	/**
	 * Creates a new tip dialog which explains that the order of values in the
	 * cell of the cage is not relevant for solving the cage arithmetic. </br>
	 * 
	 * For performance reasons this method should only be called in case the
	 * static call to method {@link #toBeDisplayed} returned true.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipOrderOfValuesInCage(Context context) {
		super(context, TIP_NAME, TIP_PRIORITY);

		build(
				context.getResources().getString(
						R.string.dialog_tip_order_of_values_in_cage_title),
				context.getResources().getString(
						R.string.dialog_tip_order_of_values_in_cage_text),
				context.getResources().getDrawable(
						R.drawable.tip_order_of_values_in_cage)).show();
	}

	/**
	 * Checks whether this tip has to be displayed. Should be called statically
	 * before creating this object.
	 * 
	 * @param preferences
	 *            Preferences of the activity for which has to be checked
	 *            whether this tip should be shown.
	 * @param cage
	 * @return
	 */
	public static boolean toBeDisplayed(Preferences preferences, GridCage cage) {
		// No tip to be displayed for non existing cages or single cell cages
		if (cage == null || cage.mAction == GridCage.ACTION_NONE) {
			return false;
		}

		// No tip to be displayed in case operators are visible and values have
		// to be added or multiplied.
		if (!cage.isOperatorHidden()
				&& (cage.mAction == GridCage.ACTION_ADD || cage.mAction == GridCage.ACTION_MULTIPLY)) {
			return false;
		}

		// Do not display in case it was displayed less than 2 minutes ago.
		if (preferences.getTipLastDisplayTime(TIP_NAME) > System.currentTimeMillis() - (5 * 60 * 1000)) {
			return false;
		}

		// Determine on basis of preferences whether the tip should be shown.
		return TipDialog
				.getDisplayTipAgain(preferences, TIP_NAME, TIP_PRIORITY);
	}
}