package net.mathdoku.plus.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.util.DisplayMetrics;
import android.util.Log;

public class Util {
	private final static String TAG = "MathDoku.Util";

	// Home directory url of promotion website. Most url's used in this app will
	// be forwarded from the promotion website to code.google.com/p/mathdoku.
	public final static String PROJECT_HOME = "http://mathdoku.net/";

	private static boolean mInitialized = false;

	private static int mPackageVersionNumber;
	private static String mPackageVersionName;

	private static DisplayMetrics mDisplayMetrics;

	private static String mBasePath;

	public Util(Activity activity) {
		// Get package name and version
		mPackageVersionNumber = -1;
		mPackageVersionName = "";
		try {
			// noinspection ConstantConditions
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

		// Set path for files. Ensure that it ends with "/".
		// noinspection ConstantConditions
		mBasePath = activity.getApplicationInfo().dataDir;
		if (mBasePath != null && !mBasePath.endsWith("/")) {
			mBasePath += "/";
		}

		// Set flag to indicate that it is now save to call the static
		// functions.
		mInitialized = true;
	}

	/**
	 * Get the package version number.
	 * 
	 * @return The package version number.
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
	 */
	// Static call not implemented as the value changes at each configuration
	// change
	public DisplayMetrics getDisplayMetrics() {
		return mDisplayMetrics;
	}

	/**
	 * Converts a duration value from long to a string.
	 * 
	 * @param elapsedTime
	 *            The duration value in milliseconds.
	 * @return The string representing the duration.
	 */
	@SuppressLint("DefaultLocale")
	public static String durationTimeToString(long elapsedTime) {
		// Convert to whole seconds (rounded)
		long roundedElapsedTime = Math.round((float) elapsedTime / 1000);
		int seconds = (int) roundedElapsedTime % 60;
		int minutes = (int) Math.floor(roundedElapsedTime / 60) % 60;
		int hours = (int) Math.floor(roundedElapsedTime / (60 * 60));

		// Build time string and ignore hours if not applicable.
		String duration = "";
		if (hours > 0) {
			duration = String.format("%dh%02dm%02ds", hours, minutes, seconds);
		} else if (minutes > 0) {
			duration = String.format("%dm%02ds", minutes, seconds);
		} else {
			duration = String.format("%ds", seconds);
		}

		return duration;
	}
}