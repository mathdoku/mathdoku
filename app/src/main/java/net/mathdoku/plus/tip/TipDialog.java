package net.mathdoku.plus.tip;

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

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;

import java.util.Random;

/**
 * For each tip this class has to be extended. The constructor of the subclass determine the
 * preference, title, text and image to be used for this tip.
 */
public class TipDialog extends AlertDialog {
    @SuppressWarnings("unused")
    private static final String TAG = TipDialog.class.getName();

    // Remove "&& false" in following line to show debug information about
    // creating cages when running in development mode.
    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_TIP_DIALOG = Config.APP_MODE == AppMode.DEVELOPMENT && false;

    // Context in which the tip is created.
    private final Context mContext;

    // Preferences defined for the current context.
    private final Preferences mPreferences;

    // Name of the preference (filled with name of the subclass)
    private final String mTip;

    // Priority of the tip relative to the other tip dialogs.
    private final TipPriority mPriority;

    public enum TipPriority {
        LOW,
        MEDIUM,
        HIGH
    }

    // Indicates whether this dialog should be displayed again.
    private final boolean mDisplayAgain;

    // No more than one tip dialog should be showed at the same time.
    private static TipDialog mDisplayedDialog = null;

    // Handler to be called when closing the dialog
    private OnClickCloseListener mOnClickCloseListener;

    public interface OnClickCloseListener {
        void onTipDialogClose();
    }

    /**
     * Creates a new instance of {@link TipDialog}.
     *
     * @param context
     *         The activity in which context the tip is used.
     * @param tip
     *         The name of the tip class.
     * @param priority
     *         The priority of this tip relative to other tip classes.
     */
    TipDialog(Context context, String tip, TipPriority priority) {
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
        debug(TAG, "Added dialog " + mTip);

        mDisplayAgain = mPreferences.getTipDisplayAgain(mTip);
    }

    /**
     * Build the dialog.
     *
     * @param tipIconResId
     *         The resource id of the icon top be used in the tip title.
     * @param tipTitle
     *         The title of the dialog.
     * @param tipText
     *         The body text of the tip.
     * @param tipImage
     *         The image to be shown with this tip. It is preferred to have an image in each tip. In
     *         case the tip can no be clarified with an image use value null.
     * @return True if the dialog is build. False otherwise.
     */
    boolean build(int tipIconResId, String tipTitle, String tipText, Drawable tipImage) {
        // Check if dialog should be built.
        if (!mDisplayAgain) {
            return false;
        }

        // Fill the fields for this dialog
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View tipView = inflater.inflate(R.layout.tip_dialog, null);

        ((TextView) tipView.findViewById(R.id.dialog_tip_text)).setText(tipText);

        ImageView imageView = (ImageView) tipView.findViewById(R.id.dialog_tip_image);
        if (tipImage != null) {
            imageView.setImageDrawable(tipImage);
            imageView.requestLayout();
        } else {
            imageView.setVisibility(View.GONE);
        }

        final CheckBox checkBoxView = (CheckBox) tipView.findViewById(
                R.id.dialog_tip_do_not_show_again);

        setView(tipView);

        setIcon(tipIconResId);
        setTitle(tipTitle);

        // Allow all possibilities for cancelling
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setOnCancelListener(new OnTipDialogCancelListener());

        setButton(DialogInterface.BUTTON_POSITIVE, mContext.getResources()
                          .getString(R.string.dialog_general_button_close),
                  new OnTipDialogCloseListener(checkBoxView));

        // In case the dialog is shown, it is registered as the displayed
        // tip dialog. On dismissal of the dialog it has to be unregistered.
        setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                debug(TAG, "OnDismiss: removed dialog in dismiss " + mTip);
                mDisplayedDialog = null;
            }
        });

        return true;
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

    @Override
    public void show() {
        if (!mDisplayAgain) {
            throw new IllegalStateException(
                    "Show tip dialog should not be called when do not display again is set.");
        }

        super.show();
    }

    /**
     * Check whether this tip will be shown.
     *
     * @return True in case the tip has to be shown. False otherwise.
     */
    static boolean getDisplayTipAgain(Preferences preferences, String tip, TipPriority priority) {
        // Check do-not-show-again-preference for this tip first.
        if (!preferences.getTipDisplayAgain(tip)) {
            debug(TAG, tip + ": do-not-show-again enabled");
            return false;
        }

        // If already a dialog is showed, check whether it has to be replaced
        // with a higher priority dialog.
        if (mDisplayedDialog != null) {
            debug(TAG,
                  tip + ": priority (" + priority.ordinal() + ") compared with priority of " +
                          mDisplayedDialog.mTip + "(" + mDisplayedDialog.mPriority.ordinal() + ")");

            // Do not display in case priority is lower than priority of already
            // displayed dialog.
            if (priority.ordinal() < mDisplayedDialog.mPriority.ordinal()) {
                Log.i(TAG,
                      tip + ": do not replace as priority is lower than already displayed tip");
                return false;
            }

            // In case of equals priority it is randomly decided which dialog is
            // kept.
            if (priority.ordinal() == mDisplayedDialog.mPriority.ordinal() && new Random()
                    .nextBoolean()) {
                Log.i(TAG, tip + ": equal priorities. Randomly determined to replace");
                return false;
            }
        }

        debug(TAG, tip + ": to be showed");
        return true;
    }

    /**
     * Gets the preference name which is used to store whether this tip has to displayed again.
     *
     * @param tip
     *         The name of the tip.
     * @return The preference name which is used to store whether this tip has to displayed again.
     */
    public static String getPreferenceStringDisplayTipAgain(String tip) {
        return "Tip." + tip + ".DisplayAgain";
    }

    /**
     * Gets the preference name which is used to store the timestamp at which the was last
     * displayed.
     *
     * @param tip
     *         The name of the tip.
     * @return The preference name which is used to store the timestamp at which the was last
     * displayed.
     */
    public static String getPreferenceStringLastDisplayTime(String tip) {
        return "Tip." + tip + ".LastDisplayTime";
    }

    /**
     * Set the listener to be called upon closing the tip dialog.
     *
     * @param onClickCloseListener
     *         The listener to be called upon closing the tip dialog.
     * @return The tip dialog itself so it can be used as a builder.
     */
    public TipDialog setOnClickCloseListener(OnClickCloseListener onClickCloseListener) {
        mOnClickCloseListener = onClickCloseListener;

        return this;
    }

    private class OnTipDialogCancelListener implements OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialog) {
            // After closing this dialog, a new TipDialog may be
            // raised immediately.
            debug(TAG, "OnClose: removed dialog after click on cancel " + mTip);
            mDisplayedDialog = null;

            // Store time at which the tip was last displayed
            mPreferences.setTipLastDisplayTime(mTip, System.currentTimeMillis());

            // If an additional close listener was set, it needs to
            // be called.
            if (mOnClickCloseListener != null) {
                mOnClickCloseListener.onTipDialogClose();
            }
        }
    }

    private class OnTipDialogCloseListener implements OnClickListener {
        private final CheckBox checkBoxView;

        public OnTipDialogCloseListener(CheckBox checkBoxView) {
            this.checkBoxView = checkBoxView;
        }

        @Override
        public void onClick(DialogInterface dialog, int id) {
            // Check if do not show again checkbox is
            // checked
            if (checkBoxView.isChecked()) {
                mPreferences.setTipDoNotDisplayAgain(mTip);
            }

            // After closing this dialog, a new TipDialog may be
            // raised immediately.
            debug(TAG, "OnClose: removed dialog after click on close " + mTip);
            mDisplayedDialog = null;

            // Store time at which the tip was last displayed
            mPreferences.setTipLastDisplayTime(mTip, System.currentTimeMillis());

            // If an additional close listener was set, it needs to
            // be called.
            if (mOnClickCloseListener != null) {
                mOnClickCloseListener.onTipDialogClose();
            }
        }
    }

    private static void debug(String tag, String message) {
        if (DEBUG_TIP_DIALOG) {
            Log.d(tag, message);
        }
    }
}