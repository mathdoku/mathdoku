package net.mathdoku.plus.archive.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.ui.base.AppPreferenceFragment;

public class ArchivePreferenceFragment extends AppPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.archive_preferences);

        setStatusFilterSummary();
        setSizeFilterSummary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.ARCHIVE_SETTING_STATUS_FILTER_VISIBLE)) {
            setStatusFilterSummary();
        }
        if (key.equals(Preferences.ARCHIVE_SETTING_GRID_TYPE_FILTER_VISIBLE)) {
            setSizeFilterSummary();
        }
    }

    /**
     * Set summary for option "show status filter" based on current value of the option.
     */
    private void setStatusFilterSummary() {
        Preference preference = findPreference(Preferences.ARCHIVE_SETTING_STATUS_FILTER_VISIBLE);
        if (preference != null) {
            int summaryResId = Preferences.getInstance()
                    .isArchiveStatusFilterVisible() ? R.string.archive_settings_show_status_filter_enabled : R.string
                    .archive_settings_show_status_filter_disabled;
            preference.setSummary(getResources().getString(summaryResId));
        }
    }

    /**
     * Set summary for option "show size filter" based on current value of the option.
     */
    private void setSizeFilterSummary() {
        Preference preference = findPreference(Preferences.ARCHIVE_SETTING_GRID_TYPE_FILTER_VISIBLE);
        if (preference != null) {
            int summaryResId = Preferences.getInstance()
                    .isArchiveSizeFilterVisible() ? R.string.archive_settings_show_grid_type_filter_enabled : R
                    .string.archive_settings_show_grid_type_filter_disabled;
            preference.setSummary(getResources().getString(summaryResId));
        }
    }
}
