package net.cactii.mathdoku.util;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.util.DisplayMetrics;
import android.util.Log;

public class Util {
	public final static String TAG = "MathDoku.Util";

	private int mPackageVersionNumber;
	private String mPackageVersionName;

	private DisplayMetrics mDisplayMetrics;

	public Util(Activity activity) {
		// Get package name and version
		mPackageVersionNumber = -1;
		mPackageVersionName = "";
		try {
			PackageInfo packageInfo = activity.getPackageManager()
					.getPackageInfo(activity.getPackageName(), 0);
			mPackageVersionNumber = packageInfo.versionCode;
			mPackageVersionName = packageInfo.versionName;
		} catch (Exception e) {
			Log.e(TAG, "Package not found", e);
		}

		// Set the display metrics
		mDisplayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(mDisplayMetrics);
	}

	/**
	 * Get the package version number.
	 * 
	 * @return The package version number.
	 * 
	 */
	public int getPackageVersionNumber() {
		return mPackageVersionNumber;
	}

	/**
	 * Get the package version name.
	 * 
	 * @return The package version name.
	 * 
	 */
	public String getPackageVersionName() {
		return mPackageVersionName;
	}

	/**
	 * Get the package version number.
	 * 
	 * @return The package version number.
	 * 
	 */
	public DisplayMetrics getDisplayMetrics() {
		return mDisplayMetrics;
	}

	/**
	 * Get the current display height.
	 * 
	 * @return The display height.
	 * 
	 */
	public int getDisplayHeight() {
		return mDisplayMetrics.heightPixels;
	}
}
