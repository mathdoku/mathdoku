package net.mathdoku.plus.ui;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class GooglePlusSignInDialog extends Dialog implements
		android.view.View.OnClickListener {

	private final PuzzleFragmentActivity mActivity;
	private final Preferences mPreferences;
	private CheckBox mHideTillNextTopScoreAchievedCheckbox;
	private boolean mCheckboxIsVisible;
	private boolean mCheckboxisChecked;

	public GooglePlusSignInDialog(PuzzleFragmentActivity activity,
			Preferences preferences) {
		super(activity);
		mActivity = activity;
		mPreferences = preferences;
		mCheckboxIsVisible = false;
		mCheckboxisChecked = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(mActivity.getResources().getString(
				R.string.google_plus_login_dialog_title));
		setContentView(R.layout.google_plus_sign_in_dialog);
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_in_cancel_button).setOnClickListener(this);

		mHideTillNextTopScoreAchievedCheckbox = (CheckBox) findViewById(R.id.hide_till_next_top_score_achieved_checkbox);
		mHideTillNextTopScoreAchievedCheckbox
				.setVisibility(mCheckboxIsVisible ? View.VISIBLE : View.GONE);
		mHideTillNextTopScoreAchievedCheckbox.setChecked(mCheckboxisChecked);
	}

	@Override
	public void onClick(View v) {
		if (mPreferences != null
				&& mHideTillNextTopScoreAchievedCheckbox != null
				&& mHideTillNextTopScoreAchievedCheckbox.getVisibility() == View.VISIBLE) {
			mPreferences
					.setHideTillNextTopScoreAchievedChecked(mHideTillNextTopScoreAchievedCheckbox
							.isChecked());
		}

		switch (v.getId()) {
		case R.id.sign_in_button:
			mActivity.signInGooglePlus();
			break;
		case R.id.sign_in_cancel_button:
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
	 * @param True
	 *            in case the checkbox is visible. False otherwise.
	 * @return The GooglePlusSignInDialog so the method can be chained.
	 */
	public GooglePlusSignInDialog displayCheckboxHideTillNextTopScoreAchieved(
			boolean checked) {
		mCheckboxIsVisible = true;
		mCheckboxisChecked = checked;
		return this;
	}
}
