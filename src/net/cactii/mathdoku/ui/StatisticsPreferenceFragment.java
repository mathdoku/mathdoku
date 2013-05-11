package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class StatisticsPreferenceFragment extends PreferenceFragment {

	SharedPreferences mSharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.statistics_preferences);
	}
}
