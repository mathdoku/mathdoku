package net.cactii.mathdoku;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.optionsview);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		UsageLog.getInstance(this);

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		super.onResume();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (sharedPreferences.contains(key)) {
			UsageLog.getInstance().logPreference("Preference.Changed", key,
					sharedPreferences.getAll().get(key));
		} else {
			UsageLog.getInstance().logPreference("Preference.Deleted", key,
					null);
		}
		;
	}
}
