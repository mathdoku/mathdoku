package net.cactii.mathdoku.tip;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.ui.PuzzleFragmentActivity;
import net.cactii.mathdoku.ui.PuzzleFragmentActivity.InputMode;
import android.graphics.drawable.Drawable;

public class TipInputModeChanged extends TipDialog {

	private static String TIP_NAME = "Tip.InputModeMaybeDiscovered.DisplayAgain";
	private static TipCategory TIP_CATEGORY = TipCategory.APP_USAGE;

	/**
	 * Creates a new tip dialog which explains that the input mode has been
	 * changed.</br>
	 * 
	 * For performance reasons this method should only be called in case the
	 * static call to method {@link #toBeDisplayed} returned true.
	 * 
	 * @param mainActivity
	 *            The activity in which this tip has to be shown.
	 */
	public TipInputModeChanged(PuzzleFragmentActivity mainActivity, InputMode newInputMode) {
		super(mainActivity, TIP_NAME, TIP_CATEGORY);

			// Set the title
			String mTitle = mainActivity.getResources().getString(
					R.string.dialog_tip_input_mode_changed_title);

			// Determine body text
			String mText;
			String normalInputMode = mainActivity.getResources().getString(
					R.string.input_mode_normal_short);
			String maybeInputMode = mainActivity.getResources().getString(
					R.string.input_mode_maybe_short);
			if (newInputMode == InputMode.MAYBE) {
				mText = mainActivity.getResources().getString(
						R.string.dialog_tip_input_mode_changed_text,
						normalInputMode, maybeInputMode);
			} else {
				mText = mainActivity.getResources().getString(
						R.string.dialog_tip_input_mode_changed_text,
						maybeInputMode, normalInputMode);
			}

			// Determine image
			Drawable mImage = mainActivity.getResources().getDrawable(
					R.drawable.tip_input_mode_changed);

			build(mTitle, mText, mImage);
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