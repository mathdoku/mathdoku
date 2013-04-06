package net.cactii.mathdoku;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.optionsview);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		UsageLog.getInstance();
		super.onResume();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
	}
}
