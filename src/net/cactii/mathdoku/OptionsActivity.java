package net.cactii.mathdoku;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public final static String BUNDLE_KEY_OPTIONS_VIEW_XML_RES_ID = "OptionsViewXMLResId";
	
	SharedPreferences mSharedPreferences;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int optionsViewResId = -1;
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				optionsViewResId = extras.getInt(
						BUNDLE_KEY_OPTIONS_VIEW_XML_RES_ID, -1);
			}
		}
		if (optionsViewResId > 0) {
			addPreferencesFromResource(optionsViewResId);
		} else {
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				throw new RuntimeException(
						"Intent bundle does not contain key '"
								+ BUNDLE_KEY_OPTIONS_VIEW_XML_RES_ID
								+ "'. Option view can not be shown.");
			}
		}
		
		setHideOperatorsSummary();
		setThemeSummary();
		
	}

	@Override
	protected void onResume() {
		UsageLog.getInstance(this);

		mSharedPreferences = Preferences.getInstance(this).mSharedPreferences;
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this); 

		super.onResume();
	}

	@Override
	protected void onPause() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		
		super.onPause();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Preferences.HIDE_OPERATORS)) {
			setHideOperatorsSummary();
		}
		if (key.equals(Preferences.THEME)) {
			setThemeSummary();
		}
	}

	/**
	 * Set summary for option "hide operators" based on current value of the
	 * option.
	 */
	@SuppressWarnings("deprecation")
	private void setHideOperatorsSummary() {
		switch (Preferences.getInstance().getHideOperator()) {
		case ALWAYS:
			findPreference(Preferences.HIDE_OPERATORS).setSummary(
					getResources().getString(
							R.string.option_hide_operators_summary_always));
			break;
		case NEVER:
			findPreference(Preferences.HIDE_OPERATORS).setSummary(
					getResources().getString(
							R.string.option_hide_operators_summary_never));
			break;
		case ASK:
			findPreference(Preferences.HIDE_OPERATORS).setSummary(
					getResources().getString(
							R.string.option_hide_operators_summary_ask));
			break;
		}
	}

	/**
	 * Set summary for option "theme" to the current value of the
	 * option.
	 */
	@SuppressWarnings("deprecation")
	private void setThemeSummary() {
		switch (Preferences.getInstance().getTheme()) {
		case CARVED:
			findPreference(Preferences.THEME).setSummary(
					getResources().getString(
							R.string.theme_carved));
			break;
		case NEWSPAPER:
			findPreference(Preferences.THEME).setSummary(
					getResources().getString(
							R.string.theme_newspaper));
			break;
		case DARK:
			findPreference(Preferences.THEME).setSummary(
					getResources().getString(
							R.string.theme_dark));
			break;
		}
	}
}
