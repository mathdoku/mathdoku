package net.mathdoku.plus.ui.base;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.WindowManager;

import net.mathdoku.plus.Preferences;

public class WindowPreference implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Activity activity;
    private Preferences mMathDokuPreferences;

    public WindowPreference(Activity activity) {
        this.activity = activity;
        mMathDokuPreferences = Preferences.getInstance(activity);

        mMathDokuPreferences.registerOnSharedPreferenceChangeListener(this);

        setFullScreenWindowFlag();
        setKeepScreenOnWindowFlag();
    }

    public void onDestroy() {
        if (mMathDokuPreferences != null) {
            mMathDokuPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    public void onResume() {
        setFullScreenWindowFlag();
        setKeepScreenOnWindowFlag();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.PUZZLE_SETTING_FULL_SCREEN)) {
            setFullScreenWindowFlag();
        }
        if (key.equals(Preferences.PUZZLE_SETTING_WAKE_LOCK)) {
            setKeepScreenOnWindowFlag();
        }
    }

    /**
     * Sets the full screen flag for the window in which the activity is shown based on the app preference.
     */
    private void setFullScreenWindowFlag() {
        // Check whether full screen mode is preferred.
        if (mMathDokuPreferences.isFullScreenEnabled()) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * Sets the keep screen on flag for the given window in which the activity is shown based on the app preference.
     */
    private void setKeepScreenOnWindowFlag() {
        if (mMathDokuPreferences.isWakeLockEnabled()) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}