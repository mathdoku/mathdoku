package net.mathdoku.plus.leaderboard;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;

public class LeaderboardConnector {
	public final static String TAG = "MathDoku.Leaderboard";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// Reference to the games client of google play services.
	private final GamesClient mGamesClient;

	// Top score info
	protected final LeaderboardTopScores[] mTopScoreInfos;

	// Reference to the resources of an the app.
	private final Context mContext;

	/**
	 * Create a new instance of the leaderboard.
	 * 
	 * @param resources
	 */
	public LeaderboardConnector(Context context, final GamesClient gamesClient) {
		mGamesClient = gamesClient;
		mContext = context;

		// Create leaderboard info array. This array will be filled via
		// different listeners which are called asynchronously.
		mTopScoreInfos = new LeaderboardTopScores[LeaderboardType.MAX_LEADERBOARDS];
		for (int i = 0; i < mTopScoreInfos.length; i++) {
			mTopScoreInfos[i] = new LeaderboardTopScores(
					LeaderboardType.getGridSize(i),
					LeaderboardType.hasHiddenOperator(i), getLeaderboardId(i));
		}

		// Asynchronously get the top scores from database and Google Play
		// Services.
		new Thread(new Runnable() {

			@Override
			public void run() {
				// Get the local top scores (as stored in the database on the
				// device) per leaderboard. Results are stored in
				// mTopScoreInfos.
				setLocalTopScores();

				if (isSignedIn()) {
					// Get the first rank and the player best score as known by
					// Google Play Services.
					for (int i = 0; i < mTopScoreInfos.length; i++) {
						mTopScoreInfos[i]
								.loadFirstRankGlobalTopScore(gamesClient);
						mTopScoreInfos[i].loadPlayerGlobalTopScore(gamesClient);
					}
				}
			}
		}).start();

	}

	/**
	 * Checks if the sign in on Google has succeeded.
	 * 
	 * @return
	 */
	public boolean isSignedIn() {
		return (mGamesClient != null && mGamesClient.isConnected());
	}

	/**
	 * Check is given score is a new top score for this player.
	 * 
	 * @param gridSize
	 *            The size of the grid to which the score applies.
	 * @param puzzleComplexity
	 *            The complexity of the grid to which the score applies.
	 * @param hideOperators
	 *            True in case of the grid to which the score applies had hidden
	 *            operators. False otherwise.
	 * @param timePlayed
	 *            The elapsed time for the grid.
	 */
	public boolean isTopScore(int gridSize, PuzzleComplexity puzzleComplexity,
			boolean hideOperators, long timePlayed) {
		// Negative and zero time are not allowed.
		if (timePlayed <= 0) {
			return false;
		}

		// Get index of leaderboard
		int leaderboardIndex = LeaderboardType.getIndex(gridSize,
				hideOperators, puzzleComplexity);
		if (leaderboardIndex < 0) {
			// Invalid index
			return false;
		}

		return mTopScoreInfos[leaderboardIndex]
				.isBetterThanLocalTopScore(timePlayed);
	}

	/**
	 * Submits a score to a leaderboard.
	 * 
	 * @param gridSize
	 *            The size of the grid to which the score applies.
	 * @param puzzleComplexity
	 *            The complexity of the grid to which the score applies.
	 * @param hideOperators
	 *            True in case of the grid to which the score applies had hidden
	 *            operators. False otherwise.
	 * @param timePlayed
	 *            The elapsed time for the grid.
	 */
	public void submitScore(int gridSize, PuzzleComplexity puzzleComplexity,
			boolean hideOperators, long timePlayed) {
		// Check boundaries of time played
		if (timePlayed <= 0 || timePlayed == Long.MAX_VALUE) {
			return;
		}

		// Check if already signed in
		if (isSignedIn() == false) {
			return;
		}

		// Submit score to leaderboard if it is a new top score for the player.
		int leaderboardIndex = LeaderboardType.getIndex(gridSize,
				hideOperators, puzzleComplexity);
		if (leaderboardIndex >= 0
				&& mTopScoreInfos != null
				&& mTopScoreInfos[leaderboardIndex]
						.isBetterThanPlayerGlobalTopScore(timePlayed)) {
			if (DEBUG) {
				Log.i(TAG,
						"Submit new score "
								+ timePlayed
								+ " for leaderboard "
								+ mTopScoreInfos[leaderboardIndex]
										.getLeaderboardDisplayName()
								+ " with callback listener");
			}
			mGamesClient.submitScoreImmediate(new OnScoreSubmittedListener() {

				@Override
				public void onScoreSubmitted(int statusCode,
						SubmitScoreResult submitScoreResult) {
					if (statusCode == GamesClient.STATUS_OK
							&& submitScoreResult != null) {
						// The score was submitted and processed by Google Play
						// Service.
						if (DEBUG) {
							Log.i(TAG,
									"New score submit listener has been called succesfully.");
						}

						// Retrieve the current rank of the player
						new LeaderboardRankPlayer(
								LeaderboardConnector.this.mGamesClient,
								new LeaderboardRankPlayer.Listener() {

									@Override
									public void onLeaderboardRankLoaded(
											Leaderboard leaderboard,
											LeaderboardScore leaderboardScore) {
										onSubmitScoreCurrentPlayerRankReceived(
												leaderboard, leaderboardScore);
									}

									@Override
									public void onNoRankFound(
											Leaderboard leaderboard) {
										// Nothing to do here. It should not be
										// possible that the player rank is not
										// found after it was just successfully
										// submitted and received.
									}
								}).loadCurrentPlayerRank(submitScoreResult
								.getLeaderboardId());

					}
				}
			}, mTopScoreInfos[leaderboardIndex].getLeaderboardId(), timePlayed);
		}
	}

	/**
	 * Gets the intent which is used to display all available leaderboard for
	 * the app.
	 * 
	 * @return The intent to use to display all available leaderboard for the
	 *         app.
	 */
	public Intent getLeaderboardsIntent() {
		return (mGamesClient != null ? mGamesClient.getAllLeaderboardsIntent()
				: null);
	}

	/**
	 * Gets the name of the player which is logged on.
	 * 
	 * @return The name of the player which is logged on. Null in case no user
	 *         is logged on or in case of an error.
	 */
	public String getPlayerName() {
		if (mGamesClient == null) {
			// Name cannot be retrieved when not logged in.
			return null;
		}

		Player player = mGamesClient.getCurrentPlayer();
		return (player != null ? player.getDisplayName() : null);
	}

	/**
	 * Get the leaderboard (string) id for the given index.
	 * 
	 * @param leaderboardIndex
	 *            The index of the leaderboard which has to be returned.
	 * @return The (string) id associated with the leaderboard. Null in case of
	 *         an error.
	 */
	public String getLeaderboardId(int leaderboardIndex) {
		int resId = LeaderboardType.getResId(leaderboardIndex);
		if (resId >= 0) {
			return mContext.getResources().getString(resId);
		}

		return null;
	}

	/**
	 * Get the leaderboard (string) id for the given combination of grid size,
	 * puzzle complexity and hide operators.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 * @param puzzleComplexity
	 *            The complexity of the grid (currently not yet in use).
	 * @param hideOperators
	 *            True in case operator are hidden. False otherwise.
	 * @return The (string) id associated with the leaderboard. Null in case of
	 *         an error.
	 */
	public String getLeaderboardIdxx(int gridSize, boolean hideOperators,
			PuzzleComplexity puzzleComplexity) {
		int resId = LeaderboardType.getResId(gridSize, hideOperators,
				puzzleComplexity);
		if (resId >= 0) {
			return mContext.getResources().getString(resId);
		}

		return null;
	}

	/**
	 * Gets the top score info which is related to the given leaderboard.
	 * 
	 * @param leaderboard
	 *            The leaderboard for which the top score info has to be
	 *            determined.
	 * @return The top score info related to the given leaderboard. Null in case
	 *         of an error.
	 */
	protected LeaderboardTopScores getLeaderboardTopScoreInfo(
			Leaderboard leaderboard) {
		if (leaderboard == null) {
			return null;
		}
		String leaderboardId = leaderboard.getLeaderboardId();
		for (LeaderboardTopScores topScoreInfo : mTopScoreInfos) {
			if (topScoreInfo.hasId(leaderboardId)) {
				return topScoreInfo;
			}
		}
		return null;
	}

	/**
	 * Retrieve and set the local top scores from the database on the device.
	 */
	private void setLocalTopScores() {
		// Get the top scores from the database.
		long[][] topScores = new StatisticsDatabaseAdapter().getTopScores();
		if (topScores == null) {
			return;
		}
		assert (topScores.length == LeaderboardType.MAX_LEADERBOARDS);

		for (int i = 0; i < LeaderboardType.MAX_LEADERBOARDS; i++) {
			if (topScores[i][0] > 0) {
				mTopScoreInfos[i].setLocalTopScore(topScores[i][1]);
			}
		}
	}

	/**
	 * Registers the global top score for the current player for this
	 * leaderboard. In case the local top score is better than the top score,
	 * this score will be submitted.
	 * 
	 * @param leaderboard
	 *            The leaderboard for which the global top score has to be
	 *            registered.
	 * @param leaderboardScores
	 *            The best score for the current player on the leaderboard.
	 */
	public void onReceiveCurrentPlayerTopScore(Leaderboard leaderboard,
			LeaderboardScore leaderboardScore) {
		// Score can only be processed if a valid leaderboard is supplied.
		if (leaderboard == null && leaderboardScore != null) {
			return;
		}

		// Determine the leaderboard for which the best score is received
		LeaderboardTopScores topScoreInfo = getLeaderboardTopScoreInfo(leaderboard);
		if (topScoreInfo != null) {
			// Store the display name of the leaderboard
			topScoreInfo
					.setLeaderboardDisplayName(leaderboard.getDisplayName());

			// Update the global top score for the player.
			topScoreInfo.setPlayerGlobalTopScore(
					leaderboardScore.getTimestampMillis(),
					leaderboardScore.getRawScore());

			// Submit the local score to Google Play in case it is better than
			// the known global score for the player.
			if (topScoreInfo.hasLocalTopScore()) {
				long localTopScore = topScoreInfo.getLocalTopScore();
				if (topScoreInfo
						.isBetterThanPlayerGlobalTopScore(localTopScore)) {
					mGamesClient.submitScore(leaderboard.getLeaderboardId(),
							localTopScore);
					if (DEBUG) {
						Log.i(TAG, "Submit the local top score "
								+ localTopScore + " for leaderboard "
								+ topScoreInfo.getLeaderboardDisplayName()
								+ " without callback listener");
						Log.i(TAG, topScoreInfo.toString());
					}
				}
			}
		}
	}

	/**
	 * In case a new top score for the player was submitted to and processed by
	 * Google Play Service the current rank of this player is retrieved. On
	 * callback this method is called.
	 * 
	 * @param leaderboard
	 * @param leaderboardScore
	 */
	private void onSubmitScoreCurrentPlayerRankReceived(
			Leaderboard leaderboard, LeaderboardScore leaderboardScore) {
		if (leaderboard == null || leaderboardScore == null) {
			return;
		}

		// Determine the leaderboard for which the best score is received
		LeaderboardTopScores topScoreInfo = getLeaderboardTopScoreInfo(leaderboard);
		if (topScoreInfo != null) {
			// Update the top score(s) for the player
			topScoreInfo.setPlayerTopScore(leaderboardScore);

			// Display a toast containing the ranking information for the score.
			Toast.makeText(
					mContext,
					"Leaderboard: " + leaderboard.getDisplayName() + "\n"
							+ "Your rank: " + leaderboardScore.getDisplayRank()
							+ "\n" + "Your time: "
							+ leaderboardScore.getDisplayScore(),
					Toast.LENGTH_LONG).show();
			return;
		}
		return;
	}
}