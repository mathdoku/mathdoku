package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

public class PuzzlePreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	Preferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.puzzle_preferences);

		mPreferences = Preferences.getInstance();

		setThemeSummary();

		// Build list preference
		ListPreference listPreference = (ListPreference) findPreference(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE);
		String[] entries = new String[7];
		String[] entryValues = new String[7];
		int index = 0;
		for (int gridSize = 4; gridSize <= 9; gridSize++) {
			entries[index] = getResources()
					.getString(
							R.string.puzzle_setting_outer_swipe_circle_visible_from_grid_size_short,
							gridSize);
			entryValues[index] = Integer.toString(gridSize);
			index++;
		}
		entries[index] = getResources().getString(
				R.string.puzzle_setting_outer_swipe_circle_visible_never);
		entryValues[index] = Integer.toString(Integer.MAX_VALUE);
		listPreference.setEntries(entries);
		listPreference.setEntryValues(entryValues);
		listPreference.setValue(mPreferences.getOuterSwipeCircleVisibility());
		setOuterSwipeCircleSummary();
	}

	@Override
	public void onStart() {
		mPreferences.mSharedPreferences
				.registerOnSharedPreferenceChangeListener(this);
		super.onStart();
	}

	@Override
	public void onStop() {
		mPreferences.mSharedPreferences
				.unregisterOnSharedPreferenceChangeListener(this);

		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Preferences.PUZZLE_SETTING_THEME)) {
			setThemeSummary();
		}
		if (key.equals(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE)) {
			setOuterSwipeCircleSummary();
		}
	}

	/**
	 * Set summary for option "theme" to the current value of the option.
	 */
	private void setThemeSummary() {
		switch (mPreferences.getTheme()) {
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

	/**
	 * Set summary for option "outer swipe circle" to the current value of the
	 * option.
	 */
	private void setOuterSwipeCircleSummary() {

		String outerSwipeCircleVisibility = mPreferences
				.getOuterSwipeCircleVisibility();
		if (mPreferences.isOuterSwipeCircleNeverVisible()) {
			findPreference(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE)
					.setSummary(
							getResources()
									.getString(
											R.string.puzzle_setting_outer_swipe_circle_visible_never));
		} else {
			findPreference(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE)
					.setSummary(
							getResources()
									.getString(
											R.string.puzzle_setting_outer_swipe_circle_visible_from_grid_size_long,
											Integer.valueOf(outerSwipeCircleVisibility)));
		}
	}
}