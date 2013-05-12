package net.cactii.mathdoku.developmentHelper;

import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.FragmentActivity;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DevelopmentHelperHoneycombAndAbove {
	/**
	 * To remain compatible with DONUT (Android 1.6) it was necessary to remove
	 * call to method Activity.recreate from the base development helper as this
	 * resulted in a verification error when running on android 1.6 as this
	 * method does not exist in that version.
	 * 
	 * @param fragmentActivity
	 */
	public static void restartActivity(final FragmentActivity fragmentActivity) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// The activity can be restarted automatically. No dialog
				// needed.
				fragmentActivity.recreate();
				return;
			}
		}
	}
}
