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
 * This class is used to retrieve the first rank from Google Play Services.
 * 
 */
public class LeaderboardRankFirst implements OnLeaderboardScoresLoadedListener {
	public final static String TAG = "MathDoku.LeaderboardRankFirst";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG = (Config.mAppMode == AppMode.DEVELOPMENT) && true;

	// The games client which is connected to Google Play Services
	private final GamesClient mGamesClient;

	// Listener methods which will be called by this class
	public interface Listener {
		public void onLeaderboardRankLoaded(Leaderboard leaderboard,
				LeaderboardScore leaderboardScore);
	};

	private final Listener mListener;

	public LeaderboardRankFirst(GamesClient gamesClient,
			final LeaderboardRankFirst.Listener listener) {
		mGamesClient = gamesClient;
		mListener = listener;
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
			// listener.
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
				|| leaderboardScoresBuffer == null || mListener == null) {
			if (DEBUG) {
				String leaderboardName = "Unknown";
				if (leaderboardBuffer != null
						&& leaderboardBuffer.getCount() > 0) {
					leaderboardName = leaderboardBuffer.get(0).getDisplayName();
				}
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
		// An empty leaderboard score buffer may not be reported as error. This
		// will happen if no player has registered a score for this leaderboard
		// yet.
		if (leaderboardScoresBuffer.getCount() == 0) {
			Log.i(TAG, "No scores have been registered yet for leaderboard "
					+ leaderboardBuffer.get(0).getDisplayName());
			return;
		}

		if (mListener != null) {
			if (DEBUG) {
				Log.i(TAG, "First rank has been loaded for leaderboard "
						+ leaderboardBuffer.get(0).getDisplayName());
			}

			mListener.onLeaderboardRankLoaded(leaderboardBuffer.get(0),
					leaderboardScoresBuffer.get(0));
		} else if (DEBUG) {
			Log.i(TAG, "No listener defined to return first rank score to.");
		}
	}
}