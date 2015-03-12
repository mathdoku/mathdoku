package net.mathdoku.plus.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.util.Log;

import java.util.List;

public class Util {
	@SuppressWarnings("unused")
	private static final String TAG = Util.class.getName();

	// Home directory url of promotion website. Most url's used in this app will
	// be forwarded from the promotion website to code.google.com/p/mathdoku.
	public static final String PROJECT_HOME = "http://mathdoku.net/";

	private static boolean mInitialized = false;

	private static int mPackageVersionNumber;
	private static String mPackageVersionName;

	public Util(Activity activity) {
		// Get package name and version
		mPackageVersionNumber = -1;
		mPackageVersionName = "";
		try {
			// noinspection ConstantConditions
			PackageInfo packageInfo = activity
					.getPackageManager()
					.getPackageInfo(activity.getPackageName(), 0);
			mPackageVersionNumber = packageInfo.versionCode;
			mPackageVersionName = packageInfo.versionName;
		} catch (Exception e) {
			Log.e(TAG, "Package not found", e);
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
		String duration;
		if (hours > 0) {
			duration = String.format("%dh%02dm%02ds", hours, minutes, seconds);
		} else if (minutes > 0) {
			duration = String.format("%dm%02ds", minutes, seconds);
		} else {
			duration = String.format("%ds", seconds);
		}

		return duration;
	}

	public static <T> boolean isListNullOrEmpty(List<T> list) {
		return list == null || list.size() == 0;
	}

	public static <T> boolean isListNotEmpty(List<T> list) {
		return list != null && list.size() > 0;
	}

	public static boolean isArrayNullOrEmpty(int[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullOrEmpty(String string) {
		return string == null || string.isEmpty();
	}
}
