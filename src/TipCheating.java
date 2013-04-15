import net.cactii.mathdoku.MainActivity;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.Tip.TipDialog;

public class TipCheating extends TipDialog {

	private static String TIP_NAME = "Tip.TipCheating.DisplayAgain";
	private static TipCategory TIP_CATEGORY = TipCategory.APP_USAGE;

	/**
	 * Creates a new tip dialog which explains that cheating will increase the
	 * elapsed time.
	 * 
	 * @param mainActivity
	 *            The activity in which this tip has to be shown.
	 */
	public TipCheating(MainActivity mainActivity) {
		super(mainActivity, TIP_NAME, TIP_CATEGORY);

		// Set the title
		String mTitle = mainActivity.getResources().getString(
				R.string.dialog_tip_cheat_reveal_value_title);

		// Determine body text
		String mText = mainActivity.getResources().getString(
				R.string.dialog_tip_cheat_reveal_value_text);

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
	public static boolean toBeDisplayed(Preferences preferences) {
		// Determine on basis of preferences whether the tip should be shown.
		return preferences.getDisplayTipAgain(TIP_NAME, TIP_CATEGORY);
	}
}
