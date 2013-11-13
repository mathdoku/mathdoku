package net.mathdoku.plus.leaderboard;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import android.util.Log;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.OnLeaderboardScoresLoadedListener;

/**
 * This class is used to retrieve the rank for the current player from Google
 * Play Services for a specific leaderboard.
 * 
 */
public class LeaderboardRankPlayer implements OnLeaderboardScoresLoadedListener {
	public final static String TAG = "MathDoku.LeaderboardRankPlayer";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// The games client which is connected to Google Play Services
	private final LeaderboardConnector mLeaderboardConnector;

	// Listener methods which will be called by this class
	public interface Listener {
		/**
		 * Callback in case the leaderboard score for the current player has
		 * been loaded.
		 * 
		 * @param leaderboard
		 *            The leaderboard for which the rank is loaded.
		 * @param leaderboardScore
		 *            The leaderboard score for the current player which is
		 *            loaded.
		 */
		public void onLeaderboardRankLoaded(Leaderboard leaderboard,
				LeaderboardScore leaderboardScore);

		/**
		 * Callback in case no leaderboard score exists for the current player.
		 * 
		 * @param leaderboard
		 *            The leaderboard for which no rank for the current player
		 *            was found.
		 */
		public void onNoRankFound(Leaderboard leaderboard);
	};

	// The listener to be called when events have been completed.
	private final Listener mListener;

	/**
	 * Create a new instance of the {@link LeaderboardRankPlayer}.
	 * 
	 * @param leaderboardConnector
	 *            The leaderboard connector which creates the new instance.
	 * @param listener
	 *            The listener to be called when applicable.
	 */
	public LeaderboardRankPlayer(LeaderboardConnector leaderboardConnector,
			final LeaderboardRankPlayer.Listener listener) {
		mLeaderboardConnector = leaderboardConnector;
		mListener = listener;
	}

	/**
	 * Loads the best rank score for the current player of the given
	 * leaderboard.
	 * 
	 * @param leaderboard
	 *            The leaderboard for which the best rank of the current player
	 *            has to be loaded.
	 */
	public void loadCurrentPlayerRank(String leaderboardId) {
		if (mLeaderboardConnector != null) {
			GamesClient gamesClient = mLeaderboardConnector.getGamesClient();

			if (gamesClient != null) {
				if (DEBUG) {
					Log.i(TAG,
							"Request the current player rank score for leaderboard "
									+ mLeaderboardConnector
											.getLeaderboardNameForLogging(leaderboardId));
				}

				// The scores centered around the current player will be
				// asynchronously loaded and submitted to the listener.
				gamesClient.loadPlayerCenteredScores(this, leaderboardId,
						LeaderboardVariant.TIME_SPAN_ALL_TIME,
						LeaderboardVariant.COLLECTION_PUBLIC, 1, false);
			}
		}
	}

	@Override
	public void onLeaderboardScoresLoaded(int statusCode,
			LeaderboardBuffer leaderboardBuffer,
			LeaderboardScoreBuffer leaderboardScoresBuffer) {
		// Determine description for the leaderboard which is used for logging
		// only
		String leaderboardName = "Unknown";
		if (DEBUG) {
			if (mLeaderboardConnector != null && leaderboardBuffer != null
					&& leaderboardBuffer.getCount() > 0) {
				leaderboardName = mLeaderboardConnector
						.getLeaderboardNameForLogging(leaderboardBuffer.get(0)
								.getLeaderboardId());
			}
		}

		// First check if results can be processed.
		if (statusCode != GamesClient.STATUS_OK || leaderboardBuffer == null
				|| leaderboardBuffer.getCount() == 0
				|| leaderboardScoresBuffer == null || mListener == null) {
			if (DEBUG) {
				Log.i(TAG,
						"Invalid response when loading the first rank for leaderboard "
								+ leaderboardName + ":\n" + "   statusCode: "
								+ statusCode + "\n");
				if (leaderboardBuffer == null) {
					Log.i(TAG, "   No leaderboard buffer (null)");
				} else if (leaderboardBuffer.getCount() == 0) {
					Log.i(TAG, "   Leaderboard buffer is empty.");
				}
				if (leaderboardScoresBuffer == null) {
					Log.i(TAG, "   No leaderboard scores buffer (null)");
				}
				if (leaderboardScoresBuffer == null) {
					Log.i(TAG, "No listener defined to return rank score to.");
				}
			}
			return;
		}

		assert (mLeaderboardConnector != null);

		GamesClient gamesClient = mLeaderboardConnector.getGamesClient();
		assert (gamesClient != null);

		if (leaderboardScoresBuffer.getCount() > 0
				&& gamesClient.getCurrentPlayerId().equals(
						leaderboardScoresBuffer.get(0).getScoreHolder()
								.getPlayerId())) {
			// A leaderboard score for the current player is found.
			if (DEBUG) {
				Log.i(TAG,
						"Rank has been loaded for current player on leaderboard "
								+ leaderboardName);
			}

			mListener.onLeaderboardRankLoaded(leaderboardBuffer.get(0),
					leaderboardScoresBuffer.get(0));
		} else {
			// Google Play Service will sent the score of the first rank player
			// in case the current player has not yet registered a score for the
			// leaderboard. In case no player has registered a score for this
			// leaderboard an empty score buffer is sent. }
			if (DEBUG) {
				Log.i(TAG,
						"No rank was found for current player on leaderboard "
								+ leaderboardName);
			}

			mListener.onNoRankFound(leaderboardBuffer.get(0));
		}
	}
}