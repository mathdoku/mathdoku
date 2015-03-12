package net.mathdoku.plus.archive.ui;

import android.os.Bundle;
import android.view.MenuItem;

import net.mathdoku.plus.R;
import net.mathdoku.plus.ui.base.AppNavUtils;
import net.mathdoku.plus.ui.base.AppPreferenceActivity;

public class ArchivePreferenceActivity extends AppPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.archive_settings_action_bar_title);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ArchivePreferenceFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            return AppNavUtils.navigateFromActivityToClass(this, ArchiveFragmentActivity.class);
        }
        return super.onOptionsItemSelected(menuItem);
    }
}