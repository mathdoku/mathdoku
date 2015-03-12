package net.mathdoku.plus.statistics.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.ui.base.AppPreferenceFragment;

public class StatisticsPreferenceFragment extends AppPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statistics_preferences);

        setMaximumGamesElapsedTimeChart();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES)) {
            setMaximumGamesElapsedTimeChart();
        }
    }

    /**
     * Set summary for option "maximum games elapsed time chart" based on current value of the
     * option.
     */
    void setMaximumGamesElapsedTimeChart() {
        Preference preference = findPreference(
                Preferences.STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES);
        if (preference != null) {
            preference.setSummary(getResources().getString(
                    R.string.statistics_setting_elapsed_time_chart_maximum_puzzles_summary,
                    Preferences.getInstance()
                            .getStatisticsSettingElapsedTimeChartMaximumGames()));
        }
    }
}
