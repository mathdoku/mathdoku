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
	private final Context mContext;

	// Preferences defined for the current context.
	private final Preferences mPreferences;

	// Name of the preference (filled with name of the subclass)
	private final String mTip;

	// Priority of the tip relative to the other tip dialogs.
	private final TipPriority mPriority;

	public enum TipPriority {
		LOW, MEDIUM, HIGH
	};

	// Indicates whether this dialog should be displayed again.
	private final boolean mDisplayAgain;

	// No more than one tip dialog should be showed at the same time.
	private static TipDialog mDisplayedDialog = null;

	// Handler to be called when closing the dialog
	private OnClickCloseListener mOnClickCloseListener;

	public interface OnClickCloseListener {
		public void onTipDialogClose();
	}

	/**
	 * Creates a new instance of {@link TipDialog}.
	 * 
	 * @param context
	 *            The activity in which context the tip is used.
	 * @param tip
	 *            The name of the tip class.
	 * @param priority
	 *            The priority of this tip relative to other tip classes.
	 */
	public TipDialog(Context context, String tip, TipPriority priority) {
		super(context);

		// Store reference to activity and preferences
		mContext = context;
		mPreferences = Preferences.getInstance();
		mTip = tip;
		mPriority = priority;

		// In case the dialog is created, the previous dialog should be
		// cancelled. Priority checking should already be done before
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

		setIcon(R.drawable.help);
		setTitle(tipTitle);
		setView(tipView);

		// Allow all possibilities for cancelling
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// After closing this dialog, a new TipDialog may be
				// raised immediately.
				if (DEBUG_TIP_DIALOG) {
					Log.i(TAG, "OnClose: removed dialog after click on cancel "
							+ mTip);
				}
				mDisplayedDialog = null;

				// Store time at which the tip was last displayed
				mPreferences.setTipLastDisplayTime(mTip,
						System.currentTimeMillis());

				// If an additional close listener was set, it needs to
				// be called.
				if (mOnClickCloseListener != null) {
					mOnClickCloseListener.onTipDialogClose();
				}
			}
		});

		setButton(DialogInterface.BUTTON_POSITIVE, mContext.getResources()
				.getString(R.string.dialog_general_button_close),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Check if do not show again checkbox is
						// checked
						if (checkBoxView.isChecked()) {
							mPreferences.setTipDoNotDisplayAgain(mTip);
						}

						// After closing this dialog, a new TipDialog may be
						// raised immediately.
						if (DEBUG_TIP_DIALOG) {
							Log.i(TAG,
									"OnClose: removed dialog after click on close "
											+ mTip);
						}
						mDisplayedDialog = null;

						// Store time at which the tip was last displayed
						mPreferences.setTipLastDisplayTime(mTip,
								System.currentTimeMillis());

						// If an additional close listener was set, it needs to
						// be called.
						if (mOnClickCloseListener != null) {
							mOnClickCloseListener.onTipDialogClose();
						}
					}
				});

		// In case the dialog is shown, it is registered as the displayed
		// tip dialog. On dismissal of the dialog it has to be unregistered.
		setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (DEBUG_TIP_DIALOG) {
					Log.i(TAG, "OnDismiss: removed dialog in dismiss " + mTip);
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
		return mPreferences.getTipDisplayAgain(mTip);
	}

	/**
	 * Check whether this tip will be shown.
	 * 
	 * @return True in case the tip has to be shown. False otherwise.
	 */
	public static boolean getDisplayTipAgain(Preferences preferences,
			String tip, TipPriority priority) {
		// Check do-not-show-again-preference for this tip first.
		if (preferences.getTipDisplayAgain(tip) == false) {
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

	/**
	 * Checks whether the tip dialog is still available (i.e. not yet reserved
	 * for a tip dialog).
	 * 
	 * @return True in case no other dialog is showed currently. False
	 *         otherwise.
	 */
	public static boolean isAvailable() {
		return (mDisplayedDialog == null);
	}

	/**
	 * Gets the preference name which is used to store whether this tip has to
	 * displayed again.
	 * 
	 * @param tip
	 *            The name of the tip.
	 * @return The preference name which is used to store whether this tip has
	 *         to displayed again.
	 */
	public static String getPreferenceStringDisplayTipAgain(String tip) {
		return "Tip." + tip + ".DisplayAgain";
	}

	/**
	 * Gets the preference name which is used to store the timestamp at which
	 * the was last displayed.
	 * 
	 * @param tip
	 *            The name of the tip.
	 * @return The preference name which is used to store the timestamp at which
	 *         the was last displayed.
	 */
	public static String getPreferenceStringLastDisplayTime(String tip) {
		return "Tip." + tip + ".LastDisplayTime";
	}

	/**
	 * Set the listener to be called upon closing the tip dialog.
	 * 
	 * @param onClickCloseListener
	 *            The listener to be called upon closing the tip dialog.
	 * @return The tip dialog itself so it can be used as a builder.
	 */
	public TipDialog setOnClickCloseListener(
			OnClickCloseListener onClickCloseListener) {
		mOnClickCloseListener = onClickCloseListener;

		return this;
	}
}