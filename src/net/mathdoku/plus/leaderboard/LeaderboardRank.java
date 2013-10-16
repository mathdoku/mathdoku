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
 * This class is used to retrieve a specific rank from Google Play Services.
 * 
 */
public class LeaderboardRank implements OnLeaderboardScoresLoadedListener {
	public final static String TAG = "MathDoku.LeaderboardRank";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// The games client which is connected to Google Play Services
	private final GamesClient mGamesClient;

	// In strict mode only received score for this player may be processed.
	private boolean mAllowCurrentPlayerOnly;

	// Listener methods which will be called by this class
	public interface Listener {
		public void onLeaderboardRankLoaded(Leaderboard leaderboard,
				LeaderboardScore leaderboardScore);
	};

	private final Listener mListener;

	public LeaderboardRank(GamesClient gamesClient,
			final LeaderboardRank.Listener listener) {
		mGamesClient = gamesClient;
		mListener = listener;
	}

	/**
	 * Loads the best rank score for the current player of the given
	 * leaderboard.
	 * 
	 * @param leaderboard
	 *            The leaderboard for which the first rank has to be loaded.
	 */
	public void loadCurrentPlayerRank(String leaderboardId) {
		if (mGamesClient != null) {
			if (DEBUG) {
				Log.i(TAG,
						"Send request for loading the current player rank score for leaderboard "
								+ leaderboardId);
			}

			// The scores centered around the current player will be
			// asynchronously loaded and submitted to the listener. As Google
			// Play Services will return the first rank in case the current
			// player has not register a score for the leaderboard, it must be
			// checked whether the received score actually applies
			// to the current player.
			mAllowCurrentPlayerOnly = true;
			mGamesClient.loadPlayerCenteredScores(this, leaderboardId,
					LeaderboardVariant.TIME_SPAN_ALL_TIME,
					LeaderboardVariant.COLLECTION_PUBLIC, 1, false);
		}
	}

	/**
	 * Loads the first rank score of the given leaderboard.
	 * 
	 * @param string
	 *            The leaderboard for which the first rank has to be loaded.
	 */
	public void loadFirstRank(String leaderboardId) {
		if (mGamesClient != null) {
			if (DEBUG) {
				Log.i(TAG,
						"Send request for loading the first rank score for leaderboard "
								+ leaderboardId);
			}

			// The top scores will be asynchronously loaded and submitted to the
			// listener. As it is not sure but very like that the first rank is
			// held by another player, it should not be checked if the rank
			// received was submitted by the current player.
			mAllowCurrentPlayerOnly = false;
			mGamesClient.loadTopScores(this, leaderboardId,
					LeaderboardVariant.TIME_SPAN_ALL_TIME,
					LeaderboardVariant.COLLECTION_PUBLIC, 1, false);
		}
	}

	@Override
	public void onLeaderboardScoresLoaded(int statusCode,
			LeaderboardBuffer leaderboardBuffer,
			LeaderboardScoreBuffer leaderboardScoresBuffer) {
		// First check if results can be processed.
		if (statusCode != GamesClient.STATUS_OK || leaderboardBuffer == null
				|| leaderboardBuffer.getCount() == 0
				|| leaderboardScoresBuffer == null
				|| leaderboardScoresBuffer.getCount() == 0) {
			if (DEBUG) {
				Log.i(TAG, "Invalid response received when loading the rank:\n"
						+ "   statusCode: " + statusCode + "\n");
				if (leaderboardBuffer == null) {
					Log.i(TAG, "   No leaderboard buffer");
				} else {
					Log.i(TAG, "   Leaderboard buffer count: "
							+ leaderboardBuffer.getCount());
				}
				if (leaderboardScoresBuffer == null) {
					Log.i(TAG, "   No leaderboard scores buffer");
				} else {
					Log.i(TAG, "   Leaderboard scores buffer count: "
							+ leaderboardScoresBuffer.getCount());
				}
			}
			return;
		}

		// If applicable check if score holder is the same player as
		// the current player. In case the current player has not
		// yet registered a top score for this leaderboard the first
		// rank player will have been returned by Google
		// Play Services in case the player centered scores are
		// requested.
		if (mAllowCurrentPlayerOnly
				&& mGamesClient.getCurrentPlayerId().equals(
						leaderboardScoresBuffer.get(0).getScoreHolder()
								.getPlayerId()) == false) {
			return;
		}

		if (mListener != null) {
			if (DEBUG) {
				Log.i(TAG, "Rank has been loaded for leaderboard "
						+ leaderboardBuffer.get(0).getDisplayName());
			}

			mListener.onLeaderboardRankLoaded(leaderboardBuffer.get(0),
					leaderboardScoresBuffer.get(0));
		} else if (DEBUG) {
			Log.i(TAG, "No listener defined to return rank score to.");
		}
	}
}