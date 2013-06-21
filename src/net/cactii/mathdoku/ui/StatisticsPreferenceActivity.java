package net.cactii.mathdoku.ui;

import android.os.Bundle;

public class StatisticsPreferenceActivity extends AppActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new StatisticsPreferenceFragment())
				.commit();
	}
}