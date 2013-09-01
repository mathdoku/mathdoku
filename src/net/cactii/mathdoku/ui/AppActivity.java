package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Preferences;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.WindowManager;

public class AppActivity extends Activity implements
		OnSharedPreferenceChangeListener {

	// Preferences
	public Preferences mMathDokuPreferences;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize global objects (singleton instances)
		mMathDokuPreferences = Preferences.getInstance(this);

		mMathDokuPreferences.mSharedPreferences
				.registerOnSharedPreferenceChangeListener(this);

		setFullScreenWindowFlag();
		setKeepScreenOnWindowFlag();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				actionBar.setHomeButtonEnabled(true);
			}
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	};

	@Override
	protected void onDestroy() {
		if (mMathDokuPreferences != null
				&& mMathDokuPreferences.mSharedPreferences != null) {
			mMathDokuPreferences.mSharedPreferences
					.unregisterOnSharedPreferenceChangeListener(this);
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		setFullScreenWindowFlag();
		setKeepScreenOnWindowFlag();
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case android.R.id.home:
			// This is called when the Home (Up) button is pressed in the action
			// bar. Create a simple intent that starts the hierarchical parent
			// activity and use NavUtils in the Support Package to ensure proper
			// handling of Up.
			Intent upIntent = new Intent(this, PuzzleFragmentActivity.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is not part of the application's task, so
				// create a new task with a synthesized back stack.
				// If there are ancestor activities, they should be added here.
				TaskStackBuilder.create(this).addNextIntent(upIntent)
						.startActivities();
				finish();
			} else {
				// This activity is part of the application's task, so simply
				// navigate up to the hierarchical parent activity.
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Preferences.PUZZLE_SETTING_FULL_SCREEN)) {
			setFullScreenWindowFlag();
		}
		if (key.equals(Preferences.PUZZLE_SETTING_WAKE_LOCK)) {
			setKeepScreenOnWindowFlag();
		}
	}

	/**
	 * Sets the full screen flag for the window in which the activity is shown
	 * based on the app preference.
	 */
	private void setFullScreenWindowFlag() {
		// Check whether full screen mode is preferred.
		if (mMathDokuPreferences.isFullScreenEnabled()) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	/**
	 * Sets the keep screen on flag for the given window in which the activity
	 * is shown based on the app preference.
	 */
	private void setKeepScreenOnWindowFlag() {
		if (mMathDokuPreferences.isWakeLockEnabled()) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
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
