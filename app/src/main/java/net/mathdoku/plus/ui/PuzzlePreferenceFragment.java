package net.mathdoku.plus.ui;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.painter.Painter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

class PuzzlePreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	private Preferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.puzzle_preferences);

		mPreferences = Preferences.getInstance();

		setThemeSummary();

		setInputMethodSummary();

		// Build list of possible values for the outer swipe circle preference
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

		// Set entries to the list preferences
		ListPreference listPreference = (ListPreference) findPreference(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE);
		if (listPreference != null) {
			listPreference.setEntries(entries);
			listPreference.setEntryValues(entryValues);
			listPreference.setValue(mPreferences
					.getOuterSwipeCircleVisibility());
		}
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

		if (key.equals(Preferences.PUZZLE_SETTING_INPUT_METHOD)) {
			setInputMethodSummary();
		}

		if (key.equals(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE)) {
			setOuterSwipeCircleSummary();
		}
	}

	/**
	 * Set summary for option "theme" to the current value of the option.
	 */
	private void setThemeSummary() {
		Preference preference = findPreference(Preferences.PUZZLE_SETTING_THEME);
		if (preference != null) {
			preference
					.setSummary(getResources()
							.getString(
									mPreferences.getTheme() == Painter.GridTheme.LIGHT ? R.string.theme_light
											: R.string.theme_dark));
		}
	}

	/**
	 * Set summary for option "input method" to the current value of the option.
	 */
	private void setInputMethodSummary() {
		Preference preference = findPreference(Preferences.PUZZLE_SETTING_INPUT_METHOD);
		if (preference != null) {
			switch (mPreferences.getDigitInputMethod()) {
			case SWIPE_ONLY:
				preference.setSummary(getResources().getString(
						R.string.input_method_swipe_only));
				break;
			case SWIPE_AND_BUTTONS:
				preference.setSummary(getResources().getString(
						R.string.input_method_swipe_and_buttons));
				break;
			case BUTTONS_ONLY:
				preference.setSummary(getResources().getString(
						R.string.input_method_buttons_only));
				break;
			}
		}
	}

	/**
	 * Set summary for option "outer swipe circle" to the current value of the
	 * option.
	 */
	private void setOuterSwipeCircleSummary() {
		Preference preference = findPreference(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE);
		if (preference != null) {
			if (mPreferences.isOuterSwipeCircleNeverVisible()) {
				preference
						.setSummary(getResources()
								.getString(
										R.string.puzzle_setting_outer_swipe_circle_visible_never));
			} else {
				String outerSwipeCircleVisibility = mPreferences
						.getOuterSwipeCircleVisibility();
				preference
						.setSummary(getResources()
								.getString(
										R.string.puzzle_setting_outer_swipe_circle_visible_from_grid_size_long,
										Integer.valueOf(outerSwipeCircleVisibility)));
			}
		}
	}
}