package net.cactii.mathdoku.util;

import net.cactii.mathdoku.SingletonInstanceNotInstantiated;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.util.DisplayMetrics;
import android.util.Log;

public class Util {
	public final static String TAG = "MathDoku.Util";

	private static boolean mInitialized = false;

	private static int mPackageVersionNumber;
	private static String mPackageVersionName;

	private static DisplayMetrics mDisplayMetrics;

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

		// Set flag to indicate that it is now save to call the static
		// functions.
		mInitialized = true;
	}

	/**
	 * Get the package version number.
	 * 
	 * @return The package version number.
	 * 
	 */
	public static int getPackageVersionNumber() {
		if (!mInitialized) {
			throw new SingletonInstanceNotInstantiated();
		}
		return mPackageVersionNumber;
	}

	/**
	 * Get the package version name.
	 * 
	 * @return The package version name.
	 * 
	 */
	public static String getPackageVersionName() {
		if (!mInitialized) {
			throw new SingletonInstanceNotInstantiated();
		}
		return mPackageVersionName;
	}

	/**
	 * Get the package version number.
	 * 
	 * @return The package version number.
	 * 
	 */
	// Static call not implemented as the value changes at each configuration
	// change
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
