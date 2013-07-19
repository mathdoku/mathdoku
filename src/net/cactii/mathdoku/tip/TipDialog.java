package net.cactii.mathdoku.tip;

import java.util.Random;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
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
	public static String TAG = "Tip.TipDialog";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG_TIP_DIALOG = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	// Context in which the tip is created.
	private Context mContext;

	// Preferences defined for the current context.
	Preferences mPreferences;

	// Name of the preference used to determine whether it should be shown again
	// or not.
	private String mTip;
	private TipPriority mPriority;

	public enum TipPriority {
		LOW, MEDIUM, HIGH
	};

	private boolean mDisplayAgain;

	// No more than one tip dialog should be showed at the same time.
	private static TipDialog mDisplayedDialog = null;

	/**
	 * Creates a new instance of {@link TipDialog}.
	 * 
	 * @param context
	 *            The activity in which context the tip is used.
	 */
	public TipDialog(Context context, String preference, TipPriority priority) {
		super(context);

		// Store reference to activity and preferences
		mContext = context;
		mPreferences = Preferences.getInstance();
		mTip = preference;
		mPriority = priority;

		// In case the dialog is created, the previous dialog should be
		// canceled. Priority checking should already be done before
		// instantiating the dialog.
		if (mDisplayedDialog != null) {
			mDisplayedDialog.dismiss();
		}

		// Register this tip as the one and only displayed dialog
		mDisplayedDialog = this;
		if (DEBUG_TIP_DIALOG) {
			Log.i(TAG, "Added dialog " + mTip);
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

		// In case the dialog is shown, it is registered as the displayed
		// tip dialog. On dismissal of the dialog it has to be unregistered.
		setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (DEBUG_TIP_DIALOG) {
					Log.i(TAG, "Removed dialog " + mTip);
				}
				mDisplayedDialog = null;
			}
		});

		return this;
	}

	/**
	 * Resets the list of displayed dialogs.
	 */
	public static void resetDisplayedDialogs() {
		if (mDisplayedDialog != null) {
			mDisplayedDialog.dismiss();
			mDisplayedDialog = null;
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

		// Display dialog
		super.show();
	}

	/**
	 * Check whether this tip will be shown.
	 * 
	 * @return True in case the tip has to be shown. False otherwise.
	 */
	public boolean displayTip() {
		return mPreferences.getDisplayTipAgain(mTip);
	}

	/**
	 * Check whether this tip will be shown.
	 * 
	 * @return True in case the tip has to be shown. False otherwise.
	 */
	public static boolean getDisplayTipAgain(Preferences preferences,
			String tip, TipPriority priority) {
		// Check do-not-show-again-preference for this tip first.
		if (preferences.getDisplayTipAgain(tip) == false) {
			if (DEBUG_TIP_DIALOG) {
				Log.i(TAG, tip + ": do-not-show-again enabled");
			}
			return false;
		}

		// If already a dialog is showed, check whether it has to be replaced
		// with a higher priority dialog.
		if (mDisplayedDialog != null) {
			if (DEBUG_TIP_DIALOG) {
				Log.i(TAG, tip + ": priority (" + priority.ordinal()
						+ ") compared with priority of "
						+ mDisplayedDialog.mTip + "("
						+ mDisplayedDialog.mPriority.ordinal() + ")");
			}

			// Do not display in case priority is lower than priority of already
			// displayed dialog.
			if (priority.ordinal() < mDisplayedDialog.mPriority.ordinal()) {
				if (DEBUG_TIP_DIALOG) {
					Log.i(TAG,
							tip
									+ ": do not replace as priority is lower than already displayed tip");
				}
				return false;
			}

			// In case of equals priority it is randomly decided which dialog is
			// kept.
			if (priority.ordinal() == mDisplayedDialog.mPriority.ordinal()
					&& new Random().nextBoolean()) {
				Log.i(TAG, tip
						+ ": equal priorities. Randomly determined to replace");
				return false;
			}
		}

		if (DEBUG_TIP_DIALOG) {
			Log.i(TAG, tip + ": to be showed");
		}
		return true;
	}
}