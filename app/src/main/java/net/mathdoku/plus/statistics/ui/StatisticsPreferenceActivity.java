package net.mathdoku.plus.statistics.ui;

import net.mathdoku.plus.R;
import net.mathdoku.plus.ui.base.AppActivity;

import android.os.Bundle;

public class StatisticsPreferenceActivity extends AppActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.statistics_settings_actionbar_title);

		getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content,
						new StatisticsPreferenceFragment())
				.commit();
	}
}