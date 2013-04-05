package net.cactii.mathdoku.DevelopmentHelpers;

import net.cactii.mathdoku.DevelopmentHelper;
import net.cactii.mathdoku.DevelopmentHelper.Mode;
import net.cactii.mathdoku.MainActivity;
import android.annotation.TargetApi;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DevelopmentHelperHoneycombAndAbove {
	/**
	 * To remain compatible with DONUT (Android 1.6) it was necessary to remove
	 * call to method Activity.recreate from the base development helper as this
	 * resulted in a verification error when running on android 1.6 as this
	 * method does not exist in that version.
	 * 
	 * @param mainActivity
	 */
	public static void restartActivity(final MainActivity mainActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// The activity can be restarted automatically. No dialog
				// needed.
				mainActivity.recreate();
				return;
			}
		}
	}
}
