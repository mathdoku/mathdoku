package net.mathdoku.plus.puzzle.cheat;

import android.content.Context;
import android.content.res.Resources;

import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.util.Util;

public abstract class Cheat {
	@SuppressWarnings("unused")
	private static final String TAG = Cheat.class.getName();

	enum CheatType {
		DUMMY
	}

	private final Resources mResources;

	// Constants to convert millisecond to calendar units
	private final static long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
	private final static long MILLISECONDS_PER_HOUR = 60 * 60 * 1000;
	private final static long MILLISECONDS_PER_MINUTE = 60 * 1000;
	protected final static long MILLISECONDS_PER_SECOND = 1000;

	// The type of cheat
	private final CheatType mCheatType;

	// Penalty time in milliseconds
	private final long mPenaltyTimeMillisecondsBase;

	// Title and text to be used in tip dialogs.
	private String mTipTitle;
	private String mTipText;
	private String mTipName;

	public Cheat(CheatParameters cheatParameters) {
		if (cheatParameters == null) {
			throw new IllegalStateException("CheatParameters can not be null");
		}
		validateCheatParameters(cheatParameters);

		mResources = cheatParameters.getResources();
		mCheatType = CheatType.DUMMY; // TODO: remove
		mPenaltyTimeMillisecondsBase = cheatParameters.getPenaltyTimeInSeconds() * MILLISECONDS_PER_SECOND;
		mTipName = cheatParameters.getTipName();
		mTipTitle = mResources
				.getString(cheatParameters.getTipTitleResId());
		mTipText = createTipText(cheatParameters);
	}

	protected String createTipText(CheatParameters cheatParameters) {
		return cheatParameters.getResources().getString(cheatParameters.getTipTextResId(),
									 getPenaltyTimeText(mPenaltyTimeMillisecondsBase));
	}

	private void validateCheatParameters(CheatParameters cheatParameters) {
		if (cheatParameters.getResources() == null) {
			throw new IllegalStateException("Resources can not be null");
		}
		if (Util.isNullOrEmpty(cheatParameters.getTipName())) {
			throw new IllegalStateException("TipName can not be null or empty.");
		}
	}

	/**
	 * Creates a new instance of {@link Cheat} which only consist of a base
	 * penalty.
	 * 
	 * @param context
	 *            The context in which the cheat is created.
	 * @param cheatType
	 *            The type of cheat to be created.
	 */
	public Cheat(Context context, CheatType cheatType) {
		mResources = context.getResources();
		mCheatType = cheatType;
		switch (mCheatType) {
		default:
			mPenaltyTimeMillisecondsBase = 0;
			mTipTitle = "";
			mTipText = "";
			if (Config.APP_MODE == AppMode.DEVELOPMENT) {
				throw new IllegalArgumentException(
						"Invalid value for parameter cheatType used in call to method Cheat(Context, CheatType).");
			}
			break;
		}
	}

	/**
	 * Get the penalty time for this cheat.
	 * 
	 * @return The penalty time in milliseconds for this cheat.
	 */
	public long getPenaltyTimeMilliseconds() {
		return mPenaltyTimeMillisecondsBase;
	}

	/**
	 * Get the title to be displayed in the tip cheat dialog.
	 * 
	 * @return The title to be displayed in the tip cheat dialog.
	 */
	public String getTipTitle() {
		return mTipTitle;
	}

	/**
	 * Get the text to be displayed in the tip cheat dialog.
	 * 
	 * @return The text to be displayed in the tip cheat dialog.
	 */
	public String getTipText() {
		return mTipText;
	}

	/**
	 * Get the type of the cheat.
	 * 
	 * @return The type of the cheat.
	 */
	public CheatType getType() {
		return mCheatType;
	}

	/**
	 * Converts a given penalty time from milliseconds to a formatted text
	 * string.
	 * 
	 * @param penaltyTime
	 *            The penalty time in milliseconds.
	 * @return A formatted text.
	 */
	protected String getPenaltyTimeText(long penaltyTime) {
		String penaltyTimeText;
		String and = " "
				+ mResources.getString(R.string.connector_last_two_elements)
				+ " ";
		long remainingPenaltyTime = penaltyTime;

		// Determine number of days
		long days = remainingPenaltyTime / MILLISECONDS_PER_DAY;
		if (days > 1) {
			penaltyTimeText = Long.toString(days) + " "
					+ mResources.getString(R.string.time_unit_days_plural);
		} else if (days == 1) {
			penaltyTimeText = "1 "
					+ mResources.getString(R.string.time_unit_days_singular);
		} else {
			penaltyTimeText = "";
		}
		remainingPenaltyTime -= days * MILLISECONDS_PER_DAY;

		if (remainingPenaltyTime > 0) {
			// Determine number of hours
			long hours = remainingPenaltyTime / MILLISECONDS_PER_HOUR;
			if (hours > 1) {
				penaltyTimeText += (days > 0 ? and : "") + hours + " "
						+ mResources.getString(R.string.time_unit_hours_plural);
			} else if (hours == 1) {
				penaltyTimeText += (days > 0 ? and : "")
						+ "1 "
						+ mResources
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
							+ mResources
									.getString(R.string.time_unit_minutes_plural);
				} else if (minutes == 1) {
					penaltyTimeText += (days + hours > 0 ? and : "")
							+ "1 "
							+ mResources
									.getString(R.string.time_unit_minutes_singular);
				} else {
					penaltyTimeText += "";
				}
				remainingPenaltyTime -= minutes * MILLISECONDS_PER_MINUTE;

				// Determine number of seconds
				if (remainingPenaltyTime > 0) {
					long seconds = remainingPenaltyTime / MILLISECONDS_PER_SECOND;
					if (seconds > 1) {
						penaltyTimeText += (days + hours + minutes > 0 ? and
								: "")
								+ seconds
								+ " "
								+ mResources
										.getString(R.string.time_unit_seconds_plural);
					} else if (seconds == 1) {
						penaltyTimeText += (days + hours + minutes > 0 ? and
								: "")
								+ seconds
								+ " "
								+ mResources
										.getString(R.string.time_unit_seconds_singular);
					}
				}
			}
		}
		return penaltyTimeText;
	}

	public String getTipName() {
		switch (mCheatType) {
			default:
				return mTipName;
		}
	}
}
