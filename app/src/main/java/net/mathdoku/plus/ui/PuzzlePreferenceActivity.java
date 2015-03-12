package net.mathdoku.plus.ui;

import android.os.Bundle;
import android.view.MenuItem;

import net.mathdoku.plus.R;
import net.mathdoku.plus.ui.base.AppNavUtils;
import net.mathdoku.plus.ui.base.AppPreferenceActivity;

public class PuzzlePreferenceActivity extends AppPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.general_settings_actionbar_title);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PuzzlePreferenceFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            return AppNavUtils.navigateFromActivityToClass(this, PuzzleFragmentActivity.class);
        }
        return super.onOptionsItemSelected(menuItem);
    }
}