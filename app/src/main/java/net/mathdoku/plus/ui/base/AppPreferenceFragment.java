package net.mathdoku.plus.ui.base;

import android.content.SharedPreferences;
import android.preference.PreferenceFragment;

import net.mathdoku.plus.Preferences;

public abstract class AppPreferenceFragment extends PreferenceFragment
		implements SharedPreferences.OnSharedPreferenceChangeListener {
	private SharedPreferences mSharedPreferences;

	@Override
	public void onStart() {
		mSharedPreferences = Preferences.getInstance(getActivity()).mSharedPreferences;
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		super.onStart();
	}

	@Override
	public void onStop() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

		super.onPause();
	}

}
