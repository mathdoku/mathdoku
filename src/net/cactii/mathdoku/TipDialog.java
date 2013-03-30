package net.cactii.mathdoku;

import net.cactii.mathdoku.MainActivity.InputMode;
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

public class TipDialog extends AlertDialog {

	// Supported tips
	public enum TipType {
		INPUT_MODE_CHANGED
	};

	private MainActivity mMainActivity;
	private TipType mTipType;
	private String mTitle;
	private String mText;
	private Drawable mImage;

	private String mPreferenceDisplayAgain;

	public TipDialog(MainActivity mainActivity, TipType tipType) {
		super(mainActivity);
		mMainActivity = mainActivity;
		mTipType = tipType;
		
		switch (mTipType) {
		case INPUT_MODE_CHANGED:
			mTitle = mainActivity.getResources().getString(
					R.string.dialog_tip_input_mode_changed_title);
			mText = mainActivity.getResources().getString(
					R.string.dialog_tip_input_mode_changed_text);
			mImage = mainActivity.getResources().getDrawable(
					R.drawable.tip_input_mode_maybe_discovered);
			mPreferenceDisplayAgain = "Tip.InputModeMaybeDiscovered.DisplayAgain";
			break;
		}
	}

	public void show() {
		// Check if this tip is shown before
		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(mMainActivity);
		if (!preferences.getBoolean(mPreferenceDisplayAgain, true)) {
			// Do no show this tip again
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		LayoutInflater inflater = LayoutInflater.from(mMainActivity);
		View tipView = inflater.inflate(R.layout.tip_dialog, null);

		TextView textView = (TextView) tipView
				.findViewById(R.id.dialog_tip_text);
		textView.setText(mText);

		ImageView imageView = (ImageView) tipView
				.findViewById(R.id.dialog_tip_image);
		imageView.setImageDrawable(mImage);

		final CheckBox checkBoxView = (CheckBox) tipView
				.findViewById(R.id.dialog_tip_do_not_show_again);

		builder.setTitle(mTitle)
				.setView(tipView)
				.setCancelable(true)
				.setPositiveButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Check if do not show again checkbox is
								// checked
								if (checkBoxView.isChecked()) {
									Editor prefeditor = preferences.edit();
									prefeditor.putBoolean(
											mPreferenceDisplayAgain, false);
									prefeditor.commit();
								}
							}
						}).create().show();
	}

	public TipDialog changeToInputMode(InputMode newInputMode) {
		if (mTipType == TipType.INPUT_MODE_CHANGED) {
			String normalInputMode = mMainActivity.getResources().getString(
					R.string.input_mode_normal_short);
			String maybeInputMode = mMainActivity.getResources().getString(
					R.string.input_mode_maybe_short);
			if (newInputMode == InputMode.MAYBE) {
				mText = mMainActivity.getResources().getString(
						R.string.dialog_tip_input_mode_changed_text,
						normalInputMode, maybeInputMode);
			} else {
				mText = mMainActivity.getResources().getString(
						R.string.dialog_tip_input_mode_changed_text,
						maybeInputMode, normalInputMode);
			}
		}
		return this;
	}
}
