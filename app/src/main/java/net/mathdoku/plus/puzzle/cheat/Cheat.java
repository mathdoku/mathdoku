package net.mathdoku.plus.puzzle.cheat;

import android.content.res.Resources;

import net.mathdoku.plus.R;
import net.mathdoku.plus.util.Util;

public abstract class Cheat {
	@SuppressWarnings("unused")
	private static final String TAG = Cheat.class.getName();

	// Constants to convert millisecond to calendar units
	private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
	private static final long MILLISECONDS_PER_HOUR = 60 * 60 * 1000;
	private static final long MILLISECONDS_PER_MINUTE = 60 * 1000;
	protected static final long MILLISECONDS_PER_SECOND = 1000;

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
		penaltyTimeInMilliseconds = cheatParameters
				.getPenaltyTimeInSeconds() * MILLISECONDS_PER_SECOND;
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
		return cheatParameters.getResources().getString(
				cheatParameters.getTipTextResId(),
				getPenaltyTimeText(penaltyTimeInMilliseconds));
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

	protected String getPenaltyTimeText(long penaltyTime) {
		String penaltyTimeText;
		String and = " "
				+ resources.getString(R.string.connector_last_two_elements)
				+ " ";
		long remainingPenaltyTime = penaltyTime;

		// Determine number of days
		long days = remainingPenaltyTime / MILLISECONDS_PER_DAY;
		if (days > 1) {
			penaltyTimeText = Long.toString(days) + " "
					+ resources.getString(R.string.time_unit_days_plural);
		} else if (days == 1) {
			penaltyTimeText = "1 "
					+ resources.getString(R.string.time_unit_days_singular);
		} else {
			penaltyTimeText = "";
		}
		remainingPenaltyTime -= days * MILLISECONDS_PER_DAY;

		if (remainingPenaltyTime > 0) {
			// Determine number of hours
			long hours = remainingPenaltyTime / MILLISECONDS_PER_HOUR;
			if (hours > 1) {
				penaltyTimeText += (days > 0 ? and : "") + hours + " "
						+ resources.getString(R.string.time_unit_hours_plural);
			} else if (hours == 1) {
				penaltyTimeText += (days > 0 ? and : "")
						+ "1 "
						+ resources
								.getString(R.string.time_unit_hours_singular);
			} else {
				penaltyTimeText += "";
			}
			remainingPenaltyTime -= hours * MILLISECONDS_PER_HOUR;

			// Determine number of minutes
			if (remainingPenaltyTime > 0) {
				long minutes = remainingPenaltyTime / MILLISECONDS_PER_MINUTE;
				if (minutes > 1) {
					penaltyTimeText += (days + hours > 0 ? and : "")
							+ minutes
							+ " "
							+ resources
									.getString(R.string.time_unit_minutes_plural);
				} else if (minutes == 1) {
					penaltyTimeText += (days + hours > 0 ? and : "")
							+ "1 "
							+ resources
									.getString(R.string.time_unit_minutes_singular);
				} else {
					penaltyTimeText += "";
				}
				remainingPenaltyTime -= minutes * MILLISECONDS_PER_MINUTE;

				// Determine number of seconds
				if (remainingPenaltyTime > 0) {
					long seconds = remainingPenaltyTime
							/ MILLISECONDS_PER_SECOND;
					if (seconds > 1) {
						penaltyTimeText += (days + hours + minutes > 0 ? and
								: "")
								+ seconds
								+ " "
								+ resources
										.getString(R.string.time_unit_seconds_plural);
					} else if (seconds == 1) {
						penaltyTimeText += (days + hours + minutes > 0 ? and
								: "")
								+ seconds
								+ " "
								+ resources
										.getString(R.string.time_unit_seconds_singular);
					}
				}
			}
		}
		return penaltyTimeText;
	}

	public String getTipName() {
		return mTipName;
	}
}
