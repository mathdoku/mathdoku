package net.mathdoku.plus.tip;

import android.content.Context;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;

public class TipArchiveAvailable extends TipDialog {

	private static final String TIP_NAME = "ArchiveAvailable";
	private static final TipPriority TIP_PRIORITY = TipPriority.LOW;

	/**
	 * Creates a new tip dialog which explains that the statistics have been
	 * made available.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipArchiveAvailable(Context context) {
		super(context, TIP_NAME, TIP_PRIORITY);

		build(
				R.drawable.lightbulb,
				context.getResources().getString(
						R.string.dialog_tip_archive_available_title),
				context.getResources().getString(
						R.string.dialog_tip_archive_available_text), null)
				.show();
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
		// Do not display in case archive is not yet available.
		if (!preferences.isArchiveAvailable()) {
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