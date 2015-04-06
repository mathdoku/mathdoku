package net.mathdoku.plus.util;

import android.annotation.SuppressLint;

import java.util.List;

public class Util {
    @SuppressWarnings("unused")
    private static final String TAG = Util.class.getName();

    private Util() {
        // Prevent accidental instantiation of utility class
    }

    /**
     * Converts a duration value from long to a string.
     *
     * @param elapsedTime
     *         The duration value in milliseconds.
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
        return list == null || list.isEmpty();
    }

    public static <T> boolean isListNotNullOrEmpty(List<T> list) {
        return !isListNullOrEmpty(list);
    }

    public static boolean isArrayNullOrEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
