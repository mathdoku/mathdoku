package net.mathdoku.plus.ui.base;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;

@SuppressLint("Registered")
public class AppPreferenceActivity extends Activity {
    private WindowPreference windowPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize global objects (singleton instances)
        DatabaseHelper.getInstance(this);

        windowPreference = new WindowPreference(this);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                actionBar.setHomeButtonEnabled(true);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        windowPreference.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        windowPreference.onResume();
        super.onResume();
    }

    @Override
    public void setTitle(int resId) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setTitle(resId);
        }
    }
}