package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.R;
import android.app.ActionBar;
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