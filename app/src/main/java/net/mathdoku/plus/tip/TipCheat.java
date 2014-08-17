package net.mathdoku.plus.tip;

import android.content.Context;

import net.mathdoku.plus.Cheat;
import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;

public class TipCheat extends TipDialog {

	/*
	 * Note: this class is used for multiple different cheats.
	 */
	private static final String TIP_NAME_CELL_REVEALED = "Cheat.CellRevealed";
	private static final String TIP_NAME_CHECK_PROGRESS_USED = "Cheat.CheckProgress";
	private static final String TIP_NAME_OPERATOR_REVEALED = "Cheat.OperatorRevealed";
	private static final String TIP_NAME_SOLUTION_REVEALED = "Cheat.SolutionRevealed";
	private static final TipPriority TIP_PRIORITY = TipPriority.LOW;

	/**
	 * Creates a new tip dialog which explains that cheating will increase the
	 * elapsed time.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipCheat(Context context, Cheat cheat) {
		super(context, getTipName(cheat), TIP_PRIORITY);
		build(R.drawable.alert, cheat.getTipTitle(), cheat.getTipText(), null);
	}

	/**
	 * Checks whether this tip has to be displayed. Should be called statically
	 * before creating this object.
	 * 
	 * @param preferences
	 *            Preferences of the activity for which has to be checked
	 *            whether this tip should be shown.
	 * @param cheat
	 *            The cheat for which has to be checked if it might be
	 *            displayed.
	 * @return True in case the tip might be displayed. False otherwise.
	 */
	public static boolean toBeDisplayed(Preferences preferences, Cheat cheat) {
		// Do not display in case it was displayed less than 12 hours ago
		if (preferences.getTipLastDisplayTime(getTipName(cheat)) > System
				.currentTimeMillis() - 12 * 60 * 60 * 1000) {
			return false;
		}

		// Determine on basis of preferences whether the tip should be shown.
		return TipDialog.getDisplayTipAgain(preferences, getTipName(cheat),
				TIP_PRIORITY);
	}

	/**
	 * Get the preference name associated with displaying a tip for the given
	 * cheat.
	 * 
	 * @param cheat
	 *            The cheat for which the tip has to be displayed.
	 * @return The preference name associated with the cheat.
	 */
	private static String getTipName(Cheat cheat) {
		switch (cheat.getType()) {
		case CELL_REVEALED:
			return TIP_NAME_CELL_REVEALED;
		case CHECK_PROGRESS_USED:
			return TIP_NAME_CHECK_PROGRESS_USED;
		case OPERATOR_REVEALED:
			return TIP_NAME_OPERATOR_REVEALED;
		case SOLUTION_REVEALED:
			return TIP_NAME_SOLUTION_REVEALED;
		default:
			throw new IllegalStateException(String.format(
					"No tip implemented for cheat type '%s'", cheat
							.getType()
							.toString()));
		}
	}
}
