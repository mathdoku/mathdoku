package net.mathdoku.battle.ui;

import net.mathdoku.battle.R;
import android.os.Bundle;

public class ArchivePreferenceActivity extends AppActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.archive_settings_action_bar_title);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new ArchivePreferenceFragment())
				.commit();
	}
}