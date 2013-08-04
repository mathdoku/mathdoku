package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PuzzlePreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	SharedPreferences mSharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.puzzle_preferences);

		setThemeSummary();

	}

	@Override
	public void onStart() {
		mSharedPreferences = Preferences.getInstance(getActivity()).mSharedPreferences;
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		super.onStart();
	}

	@Override
	public void onStop() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Preferences.PUZZLE_SETTING_THEME)) {
			setThemeSummary();
		}
	}

	/**
	 * Set summary for option "theme" to the current value of the option.
	 */
	private void setThemeSummary() {
		switch (Preferences.getInstance().getTheme()) {
		case LIGHT:
			findPreference(Preferences.PUZZLE_SETTING_THEME).setSummary(
					getResources().getString(R.string.theme_light));
			break;
		case DARK:
			findPreference(Preferences.PUZZLE_SETTING_THEME).setSummary(
					getResources().getString(R.string.theme_dark));
			break;
		}
	}
}
