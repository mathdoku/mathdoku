package net.cactii.mathdoku.Tip;

import java.util.ArrayList;

import net.cactii.mathdoku.MainActivity;
import net.cactii.mathdoku.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
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

	private static String TIP_CATEGORY_FAMILIAR_WITH_APP = "Tip.Category.FamiliarWithApp";
	private static boolean TIP_CATEGORY_FAMILIAR_WITH_APP_DEFAULT = false;

	private static String TIP_CATEGORY_FAMILIAR_WITH_RULES = "Tip.Category.FamiliarWithRules";
	private static boolean TIP_CATEGORY_FAMILIAR_WITH_RULES_DEFAULT = false;

	// Category of tips
	public enum TipCategory {
		GAME_RULES, APP_USAGE
	};

	// Context in which the tip is created.
	private MainActivity mMainActivity;

	// Preferences defined for the current context.
	final SharedPreferences mPreferences;

	// Name of the preference used to determine whether it should be shown again
	// or not.
	private String mTip;
	private boolean mDisplayAgain;

	// The category the tip falls in.
	private TipCategory mTipCategory;

	// Show only one dialog per tip type
	private static ArrayList<String> mDisplayedDialogs = null;

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
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(mainActivity);
		mTip = preference;
		mTipCategory = tipCategory;

		// Initializes the displayed dialogs list on first call
		if (mDisplayedDialogs == null) {
			mDisplayedDialogs = new ArrayList<String>();
		}

		// Check if this tip should be shown (again)
		mDisplayAgain = displayTip();
	}

	/**
	 * Build the dialog.
	 * 
	 * @param tipTitle
	 *            The title of the dialog.
	 * @param tipText
	 *            The body text of the tip.
	 * @param tipImage
	 *            The image to be shown with this tip.
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
							Editor prefeditor = mPreferences.edit();
							prefeditor.putBoolean(mTip, false);
							prefeditor.commit();
						}
					}
				});

		// In case the dialog is shown, it will be added to a list of displayed
		// tip dialogs. On dismissal of the dialog it has to be removed from
		// this list.
		setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// Remove the dialog from the list of displayed dialogs.
				mDisplayedDialogs.remove(mTip);
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

		// Check if dialog is already shown
		if (mDisplayedDialogs.contains(mTip)) {
			// This tip is already be shown currently. This can happen in case
			// the user very quickly triggers the same tip.
			return;
		}

		// Add tip to list of displayed dialogs
		mDisplayedDialogs.add(mTip);

		// Display dialog
		super.show();
	}

	/**
	 * Check whether this tip will be shown.
	 * 
	 * @return True in case the tip has to be shown. False otherwise.
	 */
	public boolean displayTip() {
		return displayTip(mPreferences, mTip, mTipCategory);
	}

	/**
	 * Check whether this tip will be shown. Tips which are checked frequently
	 * should always call the static displayTip method of the corresponding
	 * subclass before actually call method show as this always creates a dialog
	 * while not knowing whether the tip has to be displayed.
	 * 
	 * @return True in case the tip has to be shown. False otherwise.
	 */
	protected static boolean displayTip(SharedPreferences preferences,
			String preference, TipCategory tipCategory) {
		// Tip will not be displayed in case its checkbox was checked before.
		if (!preferences.getBoolean(preference, true)) {
			return false;
		}

		switch (tipCategory) {
		case APP_USAGE:
			// Do not display this tip in case the user is already familiar
			// with the app
			return !preferences.getBoolean(TIP_CATEGORY_FAMILIAR_WITH_APP,
					TIP_CATEGORY_FAMILIAR_WITH_APP_DEFAULT);
		case GAME_RULES:
			// Do not display this tip in case the user is already familiar
			// with the game rules
			return !preferences.getBoolean(TIP_CATEGORY_FAMILIAR_WITH_RULES,
					TIP_CATEGORY_FAMILIAR_WITH_RULES_DEFAULT);
		}

		return true;
	}

	/**
	 * Initializes the tip category preferences.
	 */
	public static void initializeCategoryPreferences(
			SharedPreferences preferences, boolean newInstall) {
		Editor prefeditor = preferences.edit();
		if (newInstall) {
			if (!preferences.contains(TIP_CATEGORY_FAMILIAR_WITH_APP)) {
				prefeditor.putBoolean(TIP_CATEGORY_FAMILIAR_WITH_APP,
						TIP_CATEGORY_FAMILIAR_WITH_APP_DEFAULT);
			}
		}
		if (!preferences.contains(TIP_CATEGORY_FAMILIAR_WITH_RULES)) {
			prefeditor.putBoolean(TIP_CATEGORY_FAMILIAR_WITH_RULES,
					TIP_CATEGORY_FAMILIAR_WITH_RULES_DEFAULT);
		}
		prefeditor.commit();
	}

	/**
	 * Initializes the preference "familiar with rules".
	 * 
	 * @param mainActivity
	 *            The activity for which the preference has to set.
	 * 
	 * @param familiarWithRules
	 *            The new value for this preference.
	 */
	public static void setUserIsFamiliarWithRules(MainActivity mainActivity,
			boolean familiarWithRules) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(mainActivity);
		Editor prefeditor = preferences.edit();
		if (!preferences.contains(TIP_CATEGORY_FAMILIAR_WITH_RULES)) {
			prefeditor.putBoolean(TIP_CATEGORY_FAMILIAR_WITH_RULES,
					TIP_CATEGORY_FAMILIAR_WITH_RULES_DEFAULT);
		}
		prefeditor.commit();
	}
}