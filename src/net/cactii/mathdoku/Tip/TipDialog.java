package net.cactii.mathdoku.Tip;

import net.cactii.mathdoku.MainActivity;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * For each tip this class has to be extended. The constructor of the subclass
 * determine the preference, title, text and image to be used for this tip.
 */
public class TipDialog extends AlertDialog {

	// Category of tips
	public enum TipCategory {
		GAME_RULES, APP_USAGE
	};

	// Context in which the tip is created.
	private MainActivity mMainActivity;

	// Preferences defined for the current context.
	Preferences mPreferences;

	// Name of the preference used to determine whether it should be shown again
	// or not.
	private String mPreferenceDisplayAgain; // TODO: rename
	private boolean mDisplayAgain;

	// The category the tip falls in.
	private TipCategory mTipCategory;

	/**
	 * Creates a new instance of {@link TipDialog}.
	 * 
	 * @param mainActivity
	 *            The activity in which context the tip is used.
	 */
	public TipDialog(MainActivity mainActivity, String preference,
			TipCategory tipCategory) {
		super(mainActivity);

		// Store reference to activity and preferences
		mMainActivity = mainActivity;
		mPreferences = Preferences.getInstance();
		mPreferenceDisplayAgain = preference;
		mTipCategory = tipCategory;

		// Check if this tip should be shown (again)
		mDisplayAgain = displayTip();
	}

	/**
	 * Build the dialog.

	 * @param tipTitle The title of the dialog.
	 * @param tipText The body text of the tip.
	 * @param tipImage The image to be shown with this tip.
	 * @return
	 */
	protected TipDialog build(String tipTitle, String tipText, Drawable tipImage) {
		// Check if dialog should be built.
		if (!mDisplayAgain) {
			return this;
		}

		// Fill the fields for this dialog
		LayoutInflater inflater = LayoutInflater.from(mMainActivity);
		View tipView = inflater.inflate(R.layout.tip_dialog, null);

		TextView textView = (TextView) tipView
				.findViewById(R.id.dialog_tip_text);
		textView.setText(tipText);
		
		ImageView imageView = (ImageView) tipView
				.findViewById(R.id.dialog_tip_image);
		imageView.setImageDrawable(tipImage);
		imageView.requestLayout();

		final CheckBox checkBoxView = (CheckBox) tipView
				.findViewById(R.id.dialog_tip_do_not_show_again);

		setIcon(R.drawable.about); // TODO: replace with tip icon
		setTitle(tipTitle);
		setView(tipView);
		setCancelable(true);

		setButton(DialogInterface.BUTTON_POSITIVE, mMainActivity.getResources()
				.getString(R.string.dialog_general_button_close),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Check if do not show again checkbox is
						// checked
						if (checkBoxView.isChecked()) {
							mPreferences.setDoNotDisplayTipAgain(mPreferenceDisplayAgain);
						}
					}
				});

		return this;
	}

	/**
	 * Checks if this tips needs to be displayed. If so, it is displayed as
	 * well. If not, nothing will happen.
	 */
	@Override
	public void show() {
		// Check if dialog should be show.
		if (!mDisplayAgain) {
			return;
		}

		super.show();
	}

	/**
	 * Check whether this tip will be shown.
	 * 
	 * @return True in case the tip has to be shown. False otherwise.
	 */
	public boolean displayTip() {
		return mPreferences.getDisplayTipAgain(mPreferenceDisplayAgain, mTipCategory);
	}

}