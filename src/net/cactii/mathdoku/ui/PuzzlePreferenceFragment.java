package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.util.UsageLog;
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
		UsageLog.getInstance(getActivity());

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
		if (key.equals(Preferences.THEME)) {
			setThemeSummary();
		}
	}

	/**
	 * Set summary for option "theme" to the current value of the
	 * option.
	 */
	private void setThemeSummary() {
		switch (Preferences.getInstance().getTheme()) {
		case CARVED:
			findPreference(Preferences.THEME).setSummary(
					getResources().getString(
							R.string.theme_carved));
			break;
		case NEWSPAPER:
			findPreference(Preferences.THEME).setSummary(
					getResources().getString(
							R.string.theme_newspaper));
			break;
		case DARK:
			findPreference(Preferences.THEME).setSummary(
					getResources().getString(
							R.string.theme_dark));
			break;
		}
	}
}
