package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class ArchivePreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	SharedPreferences mSharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.archive_preferences);

		setStatusFilterSummary();
		setSizeFilterSummary();
	}

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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Preferences.ARCHIVE_SETTING_STATUS_FILTER_VISIBLE)) {
			setStatusFilterSummary();
		}
		if (key.equals(Preferences.ARCHIVE_SETTING_SIZE_FILTER_VISIBLE)) {
			setSizeFilterSummary();
		}
	}

	/**
	 * Set summary for option "show status filter" based on current value of the
	 * option.
	 */
	private void setStatusFilterSummary() {
		int summaryResId = Preferences.getInstance().isArchiveStatusFilterVisible() ? R.string.archive_settings_show_status_filter_enabled
				: R.string.archive_settings_show_status_filter_disabled;
		findPreference(Preferences.ARCHIVE_SETTING_STATUS_FILTER_VISIBLE).setSummary(
				getResources().getString(summaryResId));
	}

	/**
	 * Set summary for option "show size filter" based on current value of the
	 * option.
	 */
	private void setSizeFilterSummary() {
		int summaryResId = Preferences.getInstance().isArchiveSizeFilterVisible() ? R.string.archive_settings_show_size_filter_enabled
				: R.string.archive_settings_show_size_filter_disabled;
		findPreference(Preferences.ARCHIVE_SETTING_SIZE_FILTER_VISIBLE).setSummary(
				getResources().getString(summaryResId));
	}
}
