package net.mathdoku.battle.ui;

import net.mathdoku.battle.R;
import android.os.Bundle;

public class PuzzlePreferenceActivity extends AppActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.general_settings_actionbar_title);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PuzzlePreferenceFragment())
				.commit();
	}
}