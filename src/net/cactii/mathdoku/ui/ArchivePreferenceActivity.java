package net.cactii.mathdoku.ui;

import android.app.Activity;
import android.os.Bundle;

public class ArchivePreferenceActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new ArchivePreferenceFragment())
				.commit();
	}
}