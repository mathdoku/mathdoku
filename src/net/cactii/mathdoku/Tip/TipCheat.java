package net.cactii.mathdoku.Tip;

import net.cactii.mathdoku.Cheat;
import net.cactii.mathdoku.MainActivity;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;

public class TipCheat extends TipDialog {
	
	/*
	 * Note: this class is used for multiple different cheats. 
	 */
	private static TipCategory TIP_CATEGORY = TipCategory.APP_USAGE;

	/**
	 * Creates a new tip dialog which explains that cheating will increase the
	 * elapsed time.
	 * 
	 * @param mainActivity
	 *            The activity in which this tip has to be shown.
	 */
	public TipCheat(MainActivity mainActivity, Cheat cheat) {
		super(mainActivity, getTipName(cheat), TIP_CATEGORY);

		// Set the title
		String mTitle = cheat.getTipTitle();

		// Determine body text
		String mText = cheat.getTipText();

		build(mTitle, mText, null);
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
	public static boolean toBeDisplayed(Preferences preferences, Cheat cheat) {
		// Determine on basis of preferences whether the tip should be shown.
		return preferences.getDisplayTipAgain(getTipName(cheat), TIP_CATEGORY);
	}

	/**
	 * Get the preference name associated with displaying a tip for the given
	 * cheat.
	 * 
	 * @param cheat
	 *            The cheat for which the tip has to be displayed.
	 * @return
	 */
	public static String getTipName(Cheat cheat) {
		return "Tip.TipCheat." + cheat.getName() + ".DisplayAgain";
	}
}
