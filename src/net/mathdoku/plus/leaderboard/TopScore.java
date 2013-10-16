package net.mathdoku.plus.leaderboard;

/**
 * The top score hold information of a specific type of top score.
 */
public class TopScore {
	public final static String TAG = "MathDoku.TopScore";

	// Types of top scores.
	private enum TopScoreType {
		FIRST_RANK, PLAYER_GLOBAL, PLAYER_LOCAL
	};

	protected TopScoreType mGlobalScoreType;

	// Timestamp of achieving the top score. 0 in case it is unknown.
	protected long mTimestamp;

	// The display name of the score holder.
	protected String mHolderDisplayName;

	// The raw score in milliseconds.
	protected long mScore;

	/**
	 * Creates a new instance of a {{@link TopScore}.
	 * 
	 * @param topScoreType
	 *            The type of the top score.
	 * @param timestamp
	 *            The timestamp of achieving the top score. 0 in case it is
	 *            unknown.
	 * @param holderDisplayName
	 *            The display name of the score holder.
	 * @param score
	 *            The raw score in milliseconds.
	 */
	private TopScore(TopScoreType topScoreType, long timestamp,
			String holderDisplayName, long score) {
		mGlobalScoreType = topScoreType;
		mTimestamp = timestamp;
		mHolderDisplayName = holderDisplayName;
		mScore = score;
	}

	/**
	 * Creates a new instance of a {{@link TopScore} for a first rank top score.
	 * 
	 * @param topScoreType
	 *            The type of the top score.
	 * @param timestamp
	 *            The timestamp of achieving the top score. 0 in case it is
	 *            unknown.
	 * @param holderDisplayName
	 *            The display name of the score holder.
	 * @param score
	 *            The raw score in milliseconds.
	 */
	public static TopScore getNewTopScoreFirstRank(long timestamp,
			String holderDisplayName, long score) {
		return new TopScore(TopScoreType.FIRST_RANK, timestamp,
				holderDisplayName, score);
	}

	/**
	 * Creates a new instance of a {{@link TopScore} for a player global top
	 * score.
	 * 
	 * @param topScoreType
	 *            The type of the top score.
	 * @param timestamp
	 *            The timestamp of achieving the top score. 0 in case it is
	 *            unknown.
	 * @param score
	 *            The raw score in milliseconds.
	 */
	public static TopScore getNewTopScorePlayerGlobal(long timestamp, long score) {
		return new TopScore(TopScoreType.PLAYER_GLOBAL, timestamp, null, score);
	}

	/**
	 * Creates a new instance of a {{@link TopScore} for a local top score.
	 * 
	 * @param topScoreType
	 *            The type of the top score.
	 * @param score
	 *            The raw score in milliseconds.
	 */
	public static TopScore getNewTopScorePlayerLocal(long score) {
		return new TopScore(TopScoreType.PLAYER_LOCAL, 0, null, score);
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer("Score type: "
				+ mGlobalScoreType.toString());
		stringBuffer.append("\n   score: " + mScore);
		if (mGlobalScoreType == TopScoreType.FIRST_RANK) {
			stringBuffer.append("\n   score holder: " + mHolderDisplayName);
		}
		return stringBuffer.toString();
	}
}