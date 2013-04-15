package net.cactii.mathdoku;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import android.content.Context;
import android.content.res.Resources;

public class Cheat {

	public enum CheatType {
		CELL_REVEALED, OPERATOR_REVEALED, SOLUTION_REVEALED, CHECK_PROGRESS_USED
	};

	private Resources mResources;

	// The type of cheat
	private CheatType mCheatType;

	// The name of the cheat. This is used to build the name of the
	// tip-preference associated with this cheat. Do not alter without
	// converting the preferences as well.
	private String mName;

	// The penalty consists of a base penalty and an optionally a penalty per
	// occurrence of a certain condition relevant for the cheat.
	private int mConditionalOccurrences;

	// Penalty time in mili seconds
	private long mPenaltyTimeMilisBase;
	private long mPenaltyTimeMilisPerOccurrence;

	// Title and text to be used in tip dialogs.
	private String mTipTitle;
	private String mTipText;

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
		mPenaltyTimeMilisPerOccurrence = 0;
		mConditionalOccurrences = 0;
		switch (mCheatType) {
		case CELL_REVEALED:
			mName = "CellRevealed";
			mPenaltyTimeMilisBase = 60 * 1000;
			mTipTitle = mResources
					.getString(R.string.dialog_tip_cheat_reveal_value_title);
			mTipText = mResources.getString(
					R.string.dialog_tip_cheat_reveal_value_text,
					getPenaltyTimeText(mPenaltyTimeMilisBase));
			break;
		case OPERATOR_REVEALED:
			mName = "OperatorRevealed";
			mPenaltyTimeMilisBase = 300000;
			mTipTitle = mResources
					.getString(R.string.dialog_tip_cheat_reveal_operator_title);
			mTipText = mResources.getString(
					R.string.dialog_tip_cheat_reveal_operator_text,
					getPenaltyTimeText(mPenaltyTimeMilisBase));
			break;
		case SOLUTION_REVEALED:
			mName = "SolutionRevealed";
			mPenaltyTimeMilisBase = 86400000;
			mTipTitle = mResources
					.getString(R.string.dialog_tip_cheat_reveal_solution_title);
			mTipText = mResources.getString(
					R.string.dialog_tip_cheat_reveal_solution_text,
					getPenaltyTimeText(mPenaltyTimeMilisBase));
			break;
		default:
			mName = "";
			mPenaltyTimeMilisBase = 0;
			mTipTitle = "";
			mTipText = "";
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				throw new RuntimeException(
						"Invalid value for parameter cheatType used in call to method Cheat(Context, CheatType).");
			}
			break;
		}
	}

	/**
	 * Creates a new instance of {@link Cheat} which consist of a base and a
	 * conditional penalty.
	 * 
	 * @param context
	 *            The context in which the cheat is created.
	 * @param occurrencesConditionalPenalty
	 *            The number of occurrences of the conditional penalty.
	 * @param cheatType
	 *            The type of cheat to be created.
	 */
	public Cheat(Context context, CheatType cheatType,
			int occurrencesConditionalPenalty) {
		mResources = context.getResources();
		mCheatType = cheatType;

		switch (mCheatType) {
		case CHECK_PROGRESS_USED:
			mName = "CheckProgress";
			mPenaltyTimeMilisBase = 20000;
			mPenaltyTimeMilisPerOccurrence = 15000;
			mConditionalOccurrences = occurrencesConditionalPenalty;
			mTipTitle = mResources
					.getString(R.string.dialog_tip_cheat_check_progress_title);
			mTipText = mResources.getString(
					R.string.dialog_tip_cheat_check_progress_text,
					getPenaltyTimeText(mPenaltyTimeMilisBase),
					getPenaltyTimeText(mPenaltyTimeMilisPerOccurrence));
			break;
		default:
			mPenaltyTimeMilisBase = 0;
			mPenaltyTimeMilisPerOccurrence = 0;
			mConditionalOccurrences = 0;
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				throw new RuntimeException(
						"Invalid value for parameter cheatType used in call to method Cheat(Context, CheatType).");
			}
			break;
		}
	}

	/**
	 * Get the penalty time for this cheat.
	 * 
	 * @return The penalty time in mili seconds for this cheat.
	 */
	public long getPenaltyTimeMilis() {
		return mPenaltyTimeMilisBase + (mConditionalOccurrences * mPenaltyTimeMilisPerOccurrence);
	}

	/**
	 * Get the name of this cheat.
	 * 
	 * @return The name for this cheat.
	 */
	public String getName() {
		return mName;
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
	 * Converts a given penalty time from mili seconds to a formatted text
	 * string.
	 * 
	 * @param penaltyTime
	 *            The penalty time in mili seconds.
	 * @return A formatted text.
	 */
	private String getPenaltyTimeText(long penaltyTime) {
		String penaltyTimeText = "";
		String and = " "
				+ mResources
						.getString(R.string.dialog_tip_cheat_unit_connector)
				+ " ";

		// Determine number of days
		long DAY_IN_MILIS = 24 * 60 * 60 * 1000;
		long days = penaltyTime / DAY_IN_MILIS;
		if (days > 1) {
			penaltyTimeText = Long.toString(days)
					+ " "
					+ mResources
							.getString(R.string.dialog_tip_cheat_days_plural);
		} else if (days == 1) {
			penaltyTimeText = "1 "
					+ mResources
							.getString(R.string.dialog_tip_cheat_days_singular);
		} else {
			penaltyTimeText = "";
		}
		penaltyTime -= (days * DAY_IN_MILIS);

		if (penaltyTime > 0) {
			// Determine number of hours
			long HOUR_IN_MILIS = (60 * 60 * 1000);
			long hours = penaltyTime / HOUR_IN_MILIS;
			if (hours > 1) {
				penaltyTimeText += (days > 0 ? and : "")
						+ hours
						+ " "
						+ mResources
								.getString(R.string.dialog_tip_cheat_hours_pluaral);
			} else if (hours == 1) {
				penaltyTimeText += (days > 0 ? and : "")
						+ "1 "
						+ mResources
								.getString(R.string.dialog_tip_cheat_hours_singular);
			} else {
				penaltyTimeText += "";
			}
			penaltyTime -= (hours * HOUR_IN_MILIS);

			// Determine number of minutes
			if (penaltyTime > 0) {
				long MINUTE_IN_MILIS = (60 * 1000);
				long minutes = penaltyTime / MINUTE_IN_MILIS;
				if (minutes > 1) {
					penaltyTimeText += ((days + hours) > 0 ? and : "")
							+ minutes
							+ " "
							+ mResources
									.getString(R.string.dialog_tip_cheat_minutes_plural);
				} else if (minutes == 1) {
					penaltyTimeText += ((days + hours) > 0 ? and : "")
							+ "1 "
							+ mResources
									.getString(R.string.dialog_tip_cheat_minutes_singular);
				} else {
					penaltyTimeText += "";
				}
				penaltyTime -= (minutes * MINUTE_IN_MILIS);

				// Determine number of seconds
				if (penaltyTime > 0) {
					long seconds = penaltyTime / 1000;
					if (seconds > 1) {
						penaltyTimeText += ((days + hours + minutes) > 0 ? and
								: "")
								+ seconds
								+ " "
								+ mResources
										.getString(R.string.dialog_tip_cheat_seconds_plural);
					} else if (seconds == 1) {
						penaltyTimeText += ((days + hours + minutes) > 0 ? and
								: "")
								+ seconds
								+ " "
								+ mResources
										.getString(R.string.dialog_tip_cheat_seconds_singular);
					}
				}
			}
		}
		return penaltyTimeText;
	}
}