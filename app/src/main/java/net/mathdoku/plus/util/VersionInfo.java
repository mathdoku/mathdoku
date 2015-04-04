package net.mathdoku.plus.util;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.util.Log;

public class VersionInfo {
    @SuppressWarnings("unused")
    private static final String TAG = VersionInfo.class.getName();

    private int versionNumber;
    private String name;

    public VersionInfo(Activity activity) {
            versionNumber = -1;
            name = "";
            try {
                // noinspection ConstantConditions
                PackageInfo packageInfo = activity.getPackageManager()
                        .getPackageInfo(activity.getPackageName(), 0);
                versionNumber = packageInfo.versionCode;
                name = packageInfo.versionName;
            } catch (Exception e) {
                Log.e(TAG, "Package not found", e);
            }
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getName() {
        return name;
    }
}
