package net.cactii.mathdoku;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionsActivity extends PreferenceActivity {

	public final static String BUNDLE_KEY_OPTIONS_VIEW_XML_RES_ID = "OptionsViewXMLResId";

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int optionsViewResId = -1;
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				optionsViewResId = extras.getInt(BUNDLE_KEY_OPTIONS_VIEW_XML_RES_ID, -1);
			}
		}
		if (optionsViewResId > 0) {
			addPreferencesFromResource(optionsViewResId);
		} else {
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				throw new RuntimeException(
						"Intent bundle does not contain key '"
								+ BUNDLE_KEY_OPTIONS_VIEW_XML_RES_ID
								+ "'. Option view can not be shown.");
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
