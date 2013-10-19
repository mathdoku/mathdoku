package net.mathdoku.plus.leaderboard;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import android.util.Log;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;

/**
 * This class hold all information about top scores of a specific leaderboard.
 * 
 */
public class LeaderboardTopScores {
	public final static String TAG = "MathDoku.LeaderboardFirstRank";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// Characteristics of the leaderboard
	private final int mGridSize;
	private final boolean mHideOperators;

	// The id of the leaderboard as used by Google Play Services
	private final String mLeaderboardId;
	private String mLeaderboardDisplayName;

	// The scores for this leaderboard
	private TopScore mLocalTopScore;
	private TopScore mPlayerGlobalTopScore;
	private TopScore mFirstRankGlobalTopScore;

	/**
	 * Creates a new instance of {@link LeaderboardTopScores}.
	 * 
	 * @param gridSize
	 *            The grid size for this leaderboard type.
	 * @param hideOperators
	 *            The visibility of the operators for this leaderboard type.
	 *            True in case the operators as hidden. False otherwise.
	 * @param leaderboardId
	 *            The leaderboard id as used by Google Play Services.
	 */
	public LeaderboardTopScores(int gridSize, boolean hideOperators,
			String leaderboardId) {
		mGridSize = gridSize;
		mHideOperators = hideOperators;
		mLeaderboardId = leaderboardId;
		mLocalTopScore = null;
		mPlayerGlobalTopScore = null;
		mFirstRankGlobalTopScore = null;
	}

	/**
	 * Checks if a local top score is available.
	 * 
	 * @return True in case a local top score is available. False otherwise.
	 */
	public boolean hasLocalTopScore() {
		return (mLocalTopScore != null);
	}

	/**
	 * Gets the local top score.
	 * 
	 * @return The local top score. Long.MAX_VALUE in case of an error.
	 */
	public long getLocalTopScore() {
		if (mLocalTopScore != null) {
			return mLocalTopScore.mScore;
		} else {
			return Long.MAX_VALUE;
		}
	}

	/**
	 * Update the top scores based on the given leader board score in case the
	 * latter is better.
	 * 
	 * @param score
	 *            The score to be set as local top score.
	 */
	public void setPlayerTopScore(LeaderboardScore leaderboardScore) {
		if (leaderboardScore == null) {
			return;
		}
		long score = leaderboardScore.getRawScore();
		if (isBetterThanTopScore(score, mLocalTopScore)) {
			setLocalTopScore(score);
		}
		if (isBetterThanTopScore(score, mPlayerGlobalTopScore)) {
			setPlayerGlobalTopScore(leaderboardScore.getTimestampMillis(),
					score);
		}
		if (isBetterThanTopScore(score, mFirstRankGlobalTopScore)) {
			setFirstRankGlobalTopScore(leaderboardScore);
		}
	}

	/**
	 * Sets the local top score.
	 * 
	 * @param score
	 *            The score to be set as local top score.
	 */
	public void setLocalTopScore(long score) {
		if (mLocalTopScore == null) {
			mLocalTopScore = TopScore.getNewTopScorePlayerLocal(score);
		} else {
			mLocalTopScore.mScore = score;
		}
	}

	/**
	 * Checks if the given score is better than the given top score.
	 * 
	 * @param score
	 *            The new score.
	 * @param topScore
	 *            The top score. This value may be null.
	 * @return True in case the new score is better than the top score or if no
	 *         top score was given. False otherwise.
	 */
	public boolean isBetterThanTopScore(long score, TopScore topScore) {
		return (topScore == null || score < topScore.mScore);
	}

	/**
	 * Checks if the given score is better than the local top score.
	 * 
	 * @param score
	 *            The new score.
	 * @return True in case the new score is better than current local top
	 *         score. False otherwise.
	 */
	public boolean isBetterThanLocalTopScore(long score) {
		return isBetterThanTopScore(score, mLocalTopScore);
	}

	/**
	 * Checks if the given score is better than the player global top score.
	 * 
	 * @param score
	 *            The new score.
	 * @return True in case the new score is better than the current player
	 *         global top score. False otherwise.
	 */
	public boolean isBetterThanPlayerGlobalTopScore(long score) {
		return isBetterThanTopScore(score, mPlayerGlobalTopScore);
	}

	/**
	 * Checks if the given score is better than the first rank global top score.
	 * 
	 * @param score
	 *            The new score.
	 * @return True in case the new score is better than the current first rank
	 *         global top score. False otherwise.
	 */
	public boolean isBetterThanFirstRankGlobalTopScore(long score) {
		return isBetterThanTopScore(score, mFirstRankGlobalTopScore);
	}

	/**
	 * Get the leaderboard id as used by Google Play Services.
	 * 
	 * @return The leaderboard id as used by Google Play Services.
	 */
	public String getLeaderboardId() {
		return mLeaderboardId;
	}

	/**
	 * Checks if the given id is registered as leaderboard id.
	 * 
	 * @param id
	 *            The id which as to checked against the leaderboard id.
	 * @return
	 */
	public boolean hasId(String id) {
		return (mLeaderboardId.equals(id));
	}

	/**
	 * Gets the display name for this leaderboard as used on Google Play
	 * Services.
	 * 
	 * @return The display name for this leaderboard as used on Google Play
	 *         Services.
	 */
	public String getLeaderboardDisplayName() {
		return mLeaderboardDisplayName;
	}

	/**
	 * Sets the display name for this leaderboard as used on Google Play
	 * Services.
	 * 
	 * @param displayName
	 */
	public void setLeaderboardDisplayName(String displayName) {
		mLeaderboardDisplayName = displayName;
	}

	/**
	 * Set the best score and timestamp of the current player for the
	 * leaderboard.
	 * 
	 * @param timestamp
	 *            The timestamp on which the score was achieved.
	 * @param score
	 *            The score which was achieved.
	 */
	public void setPlayerGlobalTopScore(long timestamp, long score) {
		mPlayerGlobalTopScore = TopScore.getNewTopScorePlayerGlobal(timestamp,
				score);
	}

	/**
	 * Set the best score and timestamp of the first rank player for the
	 * leaderboard.
	 * 
	 * @param leaderboardScore
	 *            The leaderboard score which has to be set as first rank.
	 */
	public void setFirstRankGlobalTopScore(LeaderboardScore leaderboardScore) {
		if (leaderboardScore != null && leaderboardScore.getRank() == 1) {
			mFirstRankGlobalTopScore = TopScore.getNewTopScoreFirstRank(
					leaderboardScore.getTimestampMillis(),
					leaderboardScore.getScoreHolderDisplayName(),
					leaderboardScore.getRawScore());
			if (DEBUG) {
				Log.i(TAG, "Set the first rank score for leaderboard "
						+ getLeaderboardDisplayName());
				Log.i(TAG, toString());
			}
		}
	}

	/**
	 * Load the players best score for the leaderboard from Google Play Services
	 * asynchronously.
	 * 
	 * @param puzzleLeaderboard
	 * @param leaderboard
	 */
	public void loadPlayerGlobalTopScore(final GamesClient gamesClient) {
		new LeaderboardRankPlayer(gamesClient,
				new LeaderboardRankPlayer.Listener() {

					@Override
					public void onLeaderboardRankLoaded(
							Leaderboard leaderboard,
							LeaderboardScore leaderboardScore) {
						// Check if this score can be processed.
						if (leaderboard == null || leaderboardScore == null) {
							return;
						}
						if (DEBUG) {
							Log.i(TAG,
									"Received the players best score for leaderboard "
											+ leaderboard.getDisplayName());
						}

						// Set the best score for the player.
						setPlayerGlobalTopScore(
								leaderboardScore.getTimestampMillis(),
								leaderboardScore.getRawScore());

						// Submit the local score to Google Play in case it is
						// better than the known global score for the player.
						if (mLocalTopScore != null
								&& isBetterThanTopScore(mLocalTopScore.mScore,
										mPlayerGlobalTopScore)) {
							submitLocalTopScore(leaderboard);
						}
					}

					@Override
					public void onNoRankFound(Leaderboard leaderboard) {
						// Submit the local score if available
						if (mLocalTopScore != null) {
							submitLocalTopScore(leaderboard);
							setPlayerGlobalTopScore(0, mLocalTopScore.mScore);

						}
					}

					/**
					 * Submits the local top score to Google Play Services.
					 * 
					 * @param leaderboard
					 *            The leaderboard to which the score has to be
					 *            submitted.
					 */
					private void submitLocalTopScore(Leaderboard leaderboard) {
						gamesClient.submitScore(leaderboard.getLeaderboardId(),
								mLocalTopScore.mScore);
						if (DEBUG) {
							Log.i(TAG,
									"Submit the local top score "
											+ mLocalTopScore.mScore
											+ " for leaderboard "
											+ leaderboard.getDisplayName());
						}
					}
				}).loadCurrentPlayerRank(mLeaderboardId);
	}

	/**
	 * Load the first rank score for the leaderboard from Google Play Services
	 * asynchronously.
	 * 
	 * @param puzzleLeaderboard
	 * @param leaderboard
	 */
	public void loadFirstRankGlobalTopScore(final GamesClient gamesClient) {
		new LeaderboardRankFirst(gamesClient,
				new LeaderboardRankFirst.Listener() {

					@Override
					public void onLeaderboardRankLoaded(
							Leaderboard leaderboard,
							LeaderboardScore leaderboardScore) {
						// Check if this score can be processed.
						if (leaderboard == null || leaderboardScore == null) {
							return;
						}
						if (DEBUG) {
							Log.i(TAG,
									"Received the first rank score for leaderboard "
											+ leaderboard.getDisplayName());
						}

						// Store the display name of the leaderboard
						mLeaderboardDisplayName = leaderboard.getDisplayName();

						// Set the score for the first rank.
						setFirstRankGlobalTopScore(leaderboardScore);
					}
				}).loadFirstRank(mLeaderboardId);

	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Leaderboard: "
				+ (mLeaderboardDisplayName != null ? mLeaderboardDisplayName
						: mGridSize + "x" + mGridSize + " - "
								+ (mHideOperators ? "hidden" : "visible")
								+ " operators"));
		if (mLocalTopScore != null) {
			stringBuffer.append("\n  Local top score: "
					+ mLocalTopScore.toString());
		}
		if (mPlayerGlobalTopScore != null) {
			stringBuffer.append("\n  " + mPlayerGlobalTopScore.toString());
		}
		if (mFirstRankGlobalTopScore != null) {
			stringBuffer.append("\n  " + mFirstRankGlobalTopScore.toString());
		}
		return stringBuffer.toString();
	}
}
