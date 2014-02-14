package net.mathdoku.plus.ui;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.ui.base.AppFragmentActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class GooglePlusSignInDialog extends Dialog implements
		android.view.View.OnClickListener {

	private final AppFragmentActivity mAppFragmentActivity;
	private CheckBox mHideTillNextTopScoreAchievedCheckbox;
	private boolean mCheckboxIsVisible;
	private boolean mCheckboxIsChecked;
	private int mMessageResID;

	public interface Listener {
		// To be called when sign in button is clicked
		void onGooglePlusSignInStart();

		// To be called when the dialog is cancelled.
		void onGooglePlusSignInCancelled();
	}

	private final Listener mListener;

	/**
	 * Creates a new instance of {@link GooglePlusSignInDialog}.
	 * 
	 * @param appFragmentActivity
	 *            The activity which has started this sign in dialog.
	 * @param listener
	 *            The listener which has to be called.
	 */
	public GooglePlusSignInDialog(AppFragmentActivity appFragmentActivity,
			Listener listener) {
		super(appFragmentActivity);
		mAppFragmentActivity = appFragmentActivity;
		mListener = listener;
		mCheckboxIsVisible = false;
		mCheckboxIsChecked = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.google_plus_login_dialog_title);
		setContentView(R.layout.google_plus_sign_in_dialog);
		((TextView) findViewById(R.id.google_plus_login_dialog_message))
				.setText(mMessageResID);
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_in_cancel_button).setOnClickListener(this);

		mHideTillNextTopScoreAchievedCheckbox = (CheckBox) findViewById(R.id.hide_till_next_top_score_achieved_checkbox);
		mHideTillNextTopScoreAchievedCheckbox
				.setVisibility(mCheckboxIsVisible ? View.VISIBLE : View.GONE);
		mHideTillNextTopScoreAchievedCheckbox.setChecked(mCheckboxIsChecked);
	}

	/**
	 * Set the message to be displayed in the dialog.
	 * 
	 * @param messageResID
	 *            The resource id of the message to be displayed.
	 * @return The GooglePlusSignInDialog so the method can be chained.
	 */
	public GooglePlusSignInDialog setMessage(int messageResID) {
		mMessageResID = messageResID;
		return this;
	}

	@Override
	public void onClick(View v) {
		if (mHideTillNextTopScoreAchievedCheckbox != null
				&& mHideTillNextTopScoreAchievedCheckbox.getVisibility() == View.VISIBLE) {
			Preferences
					.getInstance(mAppFragmentActivity)
					.setHideTillNextTopScoreAchievedChecked(
							mHideTillNextTopScoreAchievedCheckbox.isChecked());
		}

		switch (v.getId()) {
		case R.id.sign_in_button:
			if (mListener != null) {
				mListener.onGooglePlusSignInStart();
			}
			break;
		case R.id.sign_in_cancel_button:
			if (mListener != null) {
				mListener.onGooglePlusSignInCancelled();
			}
			dismiss();
			break;
		default:
			break;
		}
		dismiss();
	}

	/**
	 * Displays the checkbox 'Hide till next high score achieved'.
	 * 
	 * @param checked
	 *            True in case the checkbox is visible. False otherwise.
	 * @return The GooglePlusSignInDialog so the method can be chained.
	 */
	public GooglePlusSignInDialog displayCheckboxHideTillNextTopScoreAchieved(
			boolean checked) {
		mCheckboxIsVisible = true;
		mCheckboxIsChecked = checked;
		return this;
	}
}
