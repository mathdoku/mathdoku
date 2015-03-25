package net.mathdoku.plus.ui.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.util.Util;

@SuppressLint("Registered")
public class AppFragmentActivity extends FragmentActivity {
    protected Preferences mMathDokuPreferences;
    private WindowPreference windowPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize global objects (singleton instances)
        mMathDokuPreferences = Preferences.getInstance(this);
        DatabaseHelper.getInstance(this);
        new Util(this);

        windowPreference = new WindowPreference(this);
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
}
