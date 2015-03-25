package net.mathdoku.plus.ui.base;

import android.content.SharedPreferences;
import android.preference.PreferenceFragment;

import net.mathdoku.plus.Preferences;

public abstract class AppPreferenceFragment extends PreferenceFragment implements SharedPreferences
        .OnSharedPreferenceChangeListener {
    private Preferences preferences;

    @Override
    public void onStart() {
        preferences = Preferences.getInstance(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

}
