package net.mathdoku.plus.ui;

import net.mathdoku.plus.R;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

public class GooglePlusSignInDialog extends Dialog implements
		android.view.View.OnClickListener {

	private final PuzzleFragmentActivity mActivity;

	public GooglePlusSignInDialog(PuzzleFragmentActivity activity) {
		super(activity);
		mActivity = activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(mActivity.getResources().getString(
				R.string.google_plus_login_dialog_title));
		setContentView(R.layout.google_plus_sign_in_dialog);
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_in_cancel_button).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
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
}
