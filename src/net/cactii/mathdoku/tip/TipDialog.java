package net.cactii.mathdoku.tip;

import java.util.ArrayList;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import android.app.AlertDialog;
import android.content.Context;
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
		GAME_RULES, APP_USAGE_V1_9, APP_USAGE_V2
	};

	// Context in which the tip is created.
	private Context mContext;

	// Preferences defined for the current context.
	Preferences mPreferences;

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
	 * @param context
	 *            The activity in which context the tip is used.
	 */
	public TipDialog(Context context, String preference,
			TipCategory tipCategory) {
		super(context);

		// Store reference to activity and preferences
		mContext = context;
		mPreferences = Preferences.getInstance();
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
	 *            The image to be shown with this tip. It is preferred to have
	 *            an image in each tip. In case the tip can no be clarified with
	 *            an image use value null.
	 * @return
	 */
	protected TipDialog build(String tipTitle, String tipText, Drawable tipImage) {
		// Check if dialog should be built.
		if (!mDisplayAgain) {
			return this;
		}

		// Fill the fields for this dialog
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View tipView = inflater.inflate(R.layout.tip_dialog, null);

		TextView textView = (TextView) tipView
				.findViewById(R.id.dialog_tip_text);
		textView.setText(tipText);

		ImageView imageView = (ImageView) tipView
				.findViewById(R.id.dialog_tip_image);
		if (tipImage != null) {
			imageView.setImageDrawable(tipImage);
			imageView.requestLayout();
		} else {
			imageView.setVisibility(View.GONE);
		}

		final CheckBox checkBoxView = (CheckBox) tipView
				.findViewById(R.id.dialog_tip_do_not_show_again);

		setIcon(R.drawable.about); // TODO: replace with tip icon
		setTitle(tipTitle);
		setView(tipView);
		setCancelable(true);

		setButton(DialogInterface.BUTTON_POSITIVE, mContext.getResources()
				.getString(R.string.dialog_general_button_close),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Check if do not show again checkbox is
						// checked
						if (checkBoxView.isChecked()) {
							mPreferences.setDoNotDisplayTipAgain(mTip);
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
	 * Resets the list of displayed dialogs.
	 */
	public static void resetDisplayedDialogs() {
		if (mDisplayedDialogs != null) {
			mDisplayedDialogs.clear();
		}
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
		return mPreferences.getDisplayTipAgain(mTip, mTipCategory);
	}

}