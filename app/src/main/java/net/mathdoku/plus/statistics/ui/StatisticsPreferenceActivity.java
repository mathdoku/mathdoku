package net.mathdoku.plus.statistics.ui;

import android.os.Bundle;
import android.view.MenuItem;

import net.mathdoku.plus.R;
import net.mathdoku.plus.ui.base.AppNavUtils;
import net.mathdoku.plus.ui.base.AppPreferenceActivity;

public class StatisticsPreferenceActivity extends AppPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.statistics_settings_actionbar_title);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new StatisticsPreferenceFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            return AppNavUtils.navigateFromActivityToClass(this, StatisticsFragmentActivity.class);
        }
        return super.onOptionsItemSelected(menuItem);
    }
}