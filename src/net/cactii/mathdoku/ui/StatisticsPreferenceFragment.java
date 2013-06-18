package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.util.UsageLog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class StatisticsPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	SharedPreferences mSharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.statistics_preferences);

		setMaximumGamesElapsedTimeChart();
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
		if (key.equals(Preferences.ELAPSED_TIME_CHART_MAXIMUM_GAMES_DISPLAYED)) {
			setMaximumGamesElapsedTimeChart();
		}
	}

	/**
	 * Set summary for option "maximum games elapsed time chart" based on
	 * current value of the option.
	 */
	public void setMaximumGamesElapsedTimeChart() {
		findPreference(
				Preferences.ELAPSED_TIME_CHART_MAXIMUM_GAMES_DISPLAYED)
				.setSummary(
						getResources()
								.getString(
										R.string.option_max_games_in_elapsed_time_chart_summary,
										Preferences
												.getInstance()
												.getMaximumGamesElapsedTimeChart()));
	}
}
