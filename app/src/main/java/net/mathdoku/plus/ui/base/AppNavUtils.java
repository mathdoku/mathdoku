package net.mathdoku.plus.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

public class AppNavUtils {
	private AppNavUtils() {
		// Prevent accidental instantiation of this utility class.
	}

	public static boolean navigateFromActivityToClass(Activity activity,
			Class<?> cls) {
		Intent intent = new Intent(activity, cls);
		if (intent != null) {
			if (NavUtils.shouldUpRecreateTask(activity, intent)) {
				// The intent is not part of the application's task, so
				// create a new task with a synthesized back stack.
				TaskStackBuilder
						.create(activity)
						.addNextIntent(intent)
						.startActivities();
				activity.finish();
			} else {
				// This intent is already part of the application's task, so
				// simply
				// navigate up to the hierarchical parent activity.
				NavUtils.navigateUpTo(activity, intent);
			}
			return true;
		} else {
			return false;
		}
	}
}
