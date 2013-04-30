package net.cactii.mathdoku.util;

import net.cactii.mathdoku.SingletonInstanceNotInstantiated;
import android.annotation.SuppressLint;
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
	private static int mMinimumDisplayHeigthWidth;

	private static String mBasePath;

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

		// Determine minimum of height and widht. Note that the result is
		// independent of orientation.
		mMinimumDisplayHeigthWidth = Math.min(mDisplayMetrics.heightPixels,
				mDisplayMetrics.widthPixels);

		// Set path for files. Ensure that it ends with "/".
		mBasePath = activity.getApplicationInfo().dataDir;
		if (!mBasePath.endsWith("/")) {
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
	// Static call not implemented as the value changes at each configuration
	// change
	public int getDisplayHeight() {
		return mDisplayMetrics.heightPixels;
	}

	/**
	 * Get the current display width.
	 * 
	 * @return The display width.
	 * 
	 */
	// Static call not implemented as the value changes at each configuration
	// change
	public int getDisplayWidth() {
		return mDisplayMetrics.widthPixels;
	}

	/**
	 * Get the minimum of height and width of display.
	 * 
	 * @return The minimum of height and width of display.
	 * 
	 */
	// Although the display metrics (height and width) can change at a
	// configuration change, the result of the minimum value of height and width
	// will remain the same. Therefore this method can be called statically.
	public static int getMinimumDisplayHeigthWidth() {
		if (!mInitialized) {
			throw new SingletonInstanceNotInstantiated();
		}
		return mMinimumDisplayHeigthWidth;
	}

	/**
	 * Get the path where file are stored.
	 * 
	 * @return The path where file are stored.
	 */
	public static String getPath() {
		if (!mInitialized) {
			throw new SingletonInstanceNotInstantiated();
		}
		return mBasePath;
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
		// Convert to whole seconds
		int seconds = (int) Math.floor(elapsedTime / 1000) % 60;
		int minutes = (int) Math.floor(elapsedTime/ (1000 * 60)) % 60;
		int hours = (int) Math.floor(elapsedTime / (1000 * 60 * 60));

		// Build time string and ignore hours if not applicable.
		String duration = "";
		if (hours > 0) {
			duration = String.format("%dh%dm%02ds", hours, minutes, seconds);
		} else {
			duration = String.format("%dm%02ds", minutes, seconds);	
		}
		
		return duration;
	}
}