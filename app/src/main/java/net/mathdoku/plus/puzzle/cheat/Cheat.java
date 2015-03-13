package net.mathdoku.plus.puzzle.cheat;

import android.content.res.Resources;

import net.mathdoku.plus.R;
import net.mathdoku.plus.util.Util;

public abstract class Cheat {
    protected static final long MILLISECONDS_PER_SECOND = 1000;
    @SuppressWarnings("unused")
    private static final String TAG = Cheat.class.getName();
    // Constants to convert millisecond to calendar units
    private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long MILLISECONDS_PER_HOUR = 60 * 60 * 1000;
    private static final long MILLISECONDS_PER_MINUTE = 60 * 1000;
    private final Resources resources;
    private final long penaltyTimeInMilliseconds;

    private String mTipTitle;
    private String mTipText;
    private String mTipName;

    public Cheat(CheatParameters cheatParameters) {
        if (cheatParameters == null) {
            throw new IllegalStateException("CheatParameters can not be null");
        }
        validateCheatParameters(cheatParameters);

        resources = cheatParameters.getResources();
        penaltyTimeInMilliseconds = cheatParameters.getPenaltyTimeInSeconds() * MILLISECONDS_PER_SECOND;
        mTipName = cheatParameters.getTipName();
        mTipTitle = resources.getString(cheatParameters.getTipTitleResId());
        mTipText = createTipText(cheatParameters);
    }

    private void validateCheatParameters(CheatParameters cheatParameters) {
        if (cheatParameters.getResources() == null) {
            throw new IllegalStateException("Resources can not be null");
        }
        if (Util.isNullOrEmpty(cheatParameters.getTipName())) {
            throw new IllegalStateException("TipName can not be null or empty.");
        }
    }

    protected String createTipText(CheatParameters cheatParameters) {
        return cheatParameters.getResources()
                .getString(cheatParameters.getTipTextResId(), getPenaltyTimeText(penaltyTimeInMilliseconds));
    }

    protected String getPenaltyTimeText(long penaltyTime) {
        String and = " " + resources.getString(R.string.connector_last_two_elements) + " ";
        long remainingPenaltyTime = penaltyTime;

        StringBuilder penaltyTimeStringBuilder = new StringBuilder();

        long days = remainingPenaltyTime / MILLISECONDS_PER_DAY;
        if (days != 0) {
            penaltyTimeStringBuilder.append(getDaysToText(days));
            remainingPenaltyTime -= days * MILLISECONDS_PER_DAY;
        }

        long hours = remainingPenaltyTime / MILLISECONDS_PER_HOUR;
        if (hours != 0) {
            if (remainingPenaltyTime != penaltyTime) {
                penaltyTimeStringBuilder.append(and);
            }
            penaltyTimeStringBuilder.append(getHoursToText(hours));
            remainingPenaltyTime -= hours * MILLISECONDS_PER_HOUR;
        }

        long minutes = remainingPenaltyTime / MILLISECONDS_PER_MINUTE;
        if (minutes != 0) {
            if (remainingPenaltyTime != penaltyTime) {
                penaltyTimeStringBuilder.append(and);
            }
            penaltyTimeStringBuilder.append(getMinutesToText(minutes));
            remainingPenaltyTime -= minutes * MILLISECONDS_PER_MINUTE;
        }

        long seconds = remainingPenaltyTime / MILLISECONDS_PER_SECOND;
        if (seconds != 0) {
            if (remainingPenaltyTime != penaltyTime) {
                penaltyTimeStringBuilder.append(and);
            }
            penaltyTimeStringBuilder.append(getSecondsToText(seconds));
        }

        return penaltyTimeStringBuilder.toString();
    }

    private String getDaysToText(long days) {
        if (days == 1) {
            return "1 " + resources.getString(R.string.time_unit_days_singular);
        } else {
            return Long.toString(days) + " " + resources.getString(R.string.time_unit_days_plural);
        }
    }

    private String getHoursToText(long hours) {
        if (hours == 1) {
            return "1 " + resources.getString(R.string.time_unit_hours_singular);
        } else {
            return hours + " " + resources.getString(R.string.time_unit_hours_plural);
        }
    }

    private String getMinutesToText(long minutes) {
        if (minutes == 1) {
            return "1 " + resources.getString(R.string.time_unit_minutes_singular);
        } else {
            return minutes + " " + resources.getString(R.string.time_unit_minutes_plural);
        }
    }

    private String getSecondsToText(long seconds) {
        if (seconds == 1) {
            return "1 " + resources.getString(R.string.time_unit_seconds_singular);
        } else {
            return seconds + " " + resources.getString(R.string.time_unit_seconds_plural);
        }
    }

    public long getPenaltyTimeInMilliseconds() {
        return penaltyTimeInMilliseconds;
    }

    public String getTipTitle() {
        return mTipTitle;
    }

    public String getTipText() {
        return mTipText;
    }

    public String getTipName() {
        return mTipName;
    }
}
