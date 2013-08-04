package net.cactii.mathdoku.tip;

import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import android.content.Context;

public class TipSwipeDigit5 extends TipDialog {

	public static String TIP_NAME = "Tip.TipSwipeDigit5.DisplayAgain";
	private static TipPriority TIP_PRIORITY = TipPriority.LOW;

	/**
	 * Creates a new tip dialog which explains that how swipe the digit 5.
	 * 
	 * For performance reasons this method should only be called in case the
	 * static call to method {@link #toBeDisplayed} returned true.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipSwipeDigit5(Context context) {
		super(context, TIP_NAME, TIP_PRIORITY);

		build(
				context.getResources().getString(
						R.string.dialog_tip_swipe_digit_5_title),
				context.getResources().getString(
						R.string.dialog_tip_swipe_digit_5_text),
				context.getResources()
						.getDrawable(R.drawable.tip_swipe_digit_5)).show();
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
	public static boolean toBeDisplayed(Preferences preferences, Grid grid) {
		// No tip to be displayed for grid with size smaller than 5
		if (grid == null || grid.getGridSize() < 5) {
			return false;
		}
		
		// Do not display in case it was displayed less than 1 minute ago.
		if (preferences.getTipLastDisplayTime(TIP_NAME) > System.currentTimeMillis() - (60 * 1000)) {
			return false;
		}

		// Do not display in case already some swipe motions for digit 5 have
		// been completed. As the swipe-5 could have been accidently completed,
		// the tip will not be ignored if less than 3 swipe-5 have been
		// completed.
		if (preferences.getSwipeMotionCounter(5) > 3) {
			return false;
		}

		// Do not display until the user has completed at least 10 swipes for
		// other digits.
		if (preferences.getSwipeMotionCounter(1)
				+ preferences.getSwipeMotionCounter(2)
				+ preferences.getSwipeMotionCounter(3)
				+ preferences.getSwipeMotionCounter(4)
				+ preferences.getSwipeMotionCounter(6)
				+ preferences.getSwipeMotionCounter(7)
				+ preferences.getSwipeMotionCounter(8)
				+ preferences.getSwipeMotionCounter(9) <= 10) {
			return false;
		}

		// Only display in case no other dialog is registered yet.
		if (TipDialog.isAvailable() == false) {
			return false;
		}

		// Determine on basis of preferences whether the tip should be shown.
		return (TipDialog.isAvailable() && TipDialog.getDisplayTipAgain(
				preferences, TIP_NAME, TIP_PRIORITY));
	}
}