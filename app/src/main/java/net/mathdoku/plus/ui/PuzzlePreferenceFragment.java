package net.mathdoku.plus.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.ui.base.AppPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

public class PuzzlePreferenceFragment extends AppPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.puzzle_preferences);

        setThemeSummary();

        setInputMethodSummary();

        // Build list of possible values for the outer swipe circle preference
        List<String> entries = new ArrayList<String>();
        List<String> entryValues = new ArrayList<String>();
        for (int gridSize = GridType.getSmallestGridSize(); gridSize <= GridType.getBiggestGridSize(); gridSize++) {
            entries.add(
                    getResources().getString(R.string.puzzle_setting_outer_swipe_circle_visible_from_grid_size_short,
                                             gridSize));
            entries.add(Integer.toString(gridSize));
        }
        entries.add(getResources().getString(R.string.puzzle_setting_outer_swipe_circle_visible_never));
        entryValues.add(Integer.toString(Integer.MAX_VALUE));

        // Set entries to the list preferences
        ListPreference listPreference = (ListPreference) findPreference(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE);
        if (listPreference != null) {
            listPreference.setEntries(entries.toArray(new String[entries.size()]));
            listPreference.setEntryValues(entryValues.toArray(new String[entryValues.size()]));
            listPreference.setValue(Preferences.getInstance()
                                            .getOuterSwipeCircleVisibility());
        }
        setOuterSwipeCircleSummary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
            preference.setSummary(getResources().getString(Preferences.getInstance()
                                                                   .getTheme() == Painter.GridTheme.LIGHT ? R.string
                    .theme_light : R.string.theme_dark));
        }
    }

    /**
     * Set summary for option "input method" to the current value of the option.
     */
    private void setInputMethodSummary() {
        Preference preference = findPreference(Preferences.PUZZLE_SETTING_INPUT_METHOD);
        if (preference != null) {
            switch (Preferences.getInstance()
                    .getDigitInputMethod()) {
                case SWIPE_ONLY:
                    preference.setSummary(getResources().getString(R.string.input_method_swipe_only));
                    break;
                case SWIPE_AND_BUTTONS:
                    preference.setSummary(getResources().getString(R.string.input_method_swipe_and_buttons));
                    break;
                case BUTTONS_ONLY:
                    preference.setSummary(getResources().getString(R.string.input_method_buttons_only));
                    break;
            }
        }
    }

    /**
     * Set summary for option "outer swipe circle" to the current value of the option.
     */
    private void setOuterSwipeCircleSummary() {
        Preference preference = findPreference(Preferences.PUZZLE_SETTING_OUTER_SWIPE_CIRCLE);
        if (preference != null) {
            if (Preferences.getInstance()
                    .isOuterSwipeCircleNeverVisible()) {
                preference.setSummary(
                        getResources().getString(R.string.puzzle_setting_outer_swipe_circle_visible_never));
            } else {
                String outerSwipeCircleVisibility = Preferences.getInstance()
                        .getOuterSwipeCircleVisibility();
                preference.setSummary(
                        getResources().getString(R.string.puzzle_setting_outer_swipe_circle_visible_from_grid_size_long,
                                                 Integer.valueOf(outerSwipeCircleVisibility)));
            }
        }
    }
}
