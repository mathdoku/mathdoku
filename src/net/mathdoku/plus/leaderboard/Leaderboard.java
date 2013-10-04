package net.mathdoku.plus.leaderboard;

import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import android.content.Intent;
import android.content.res.Resources;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.Player;

public class Leaderboard {
	public final static String TAG = "MathDoku.Leaderboard";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// Reference to the games client of google play services.
	GamesClient mGamesClient;

	// Reference to the resources of an the app.
	Resources mResources;

	// Available leaderboards
	public static class LeaderboardType {
		// All leaderboards as defined in the game service on Google Play
		private final static int[] mLeaderboard = {
				R.string.leaderboard_fastest_4x4_visibile_operators,
				R.string.leaderboard_fastest_5x5_visibile_operators,
				R.string.leaderboard_fastest_6x6_visibile_operators,
				R.string.leaderboard_fastest_7x7_visibile_operators,
				R.string.leaderboard_fastest_8x8_visibile_operators,
				R.string.leaderboard_fastest_9x9_visibile_operators,
				R.string.leaderboard_fastest_4x4_hidden_operators,
				R.string.leaderboard_fastest_5x5_hidden_operators,
				R.string.leaderboard_fastest_6x6_hidden_operators,
				R.string.leaderboard_fastest_7x7_hidden_operators,
				R.string.leaderboard_fastest_8x8_hidden_operators,
				R.string.leaderboard_fastest_9x9_hidden_operators };

		// Minimum and maximum allowed grid size
		private final static int MIN_GRID_SIZE = 4;
		private final static int MAX_GRID_SIZE = 9;

		/**
		 * Get the leaderboard resource id for the given combination of grid
		 * size, puzzle complexity and hide operators.
		 * 
		 * @param gridSize
		 *            The size of the grid.
		 * @param puzzleComplexity
		 *            The complexity of the grid (currently not yet in use).
		 * @param hideOperators
		 *            True in case operator are hidden. False otherwise.
		 * @return The resource id associated with the leaderboard.
		 */
		public static int getResId(int gridSize,
				PuzzleComplexity puzzleComplexity, boolean hideOperators) {

			// Test whether grid size is within known size.
			assert (gridSize >= MIN_GRID_SIZE && gridSize <= MAX_GRID_SIZE);
			int maxGrids = MAX_GRID_SIZE - MIN_GRID_SIZE + 1;

			// Determine the leaderboard to use
			int index = (gridSize - MIN_GRID_SIZE)
					* (int) Math.pow(maxGrids, 0);
			index *= (hideOperators ? 1 : 0) * Math.pow(maxGrids, 1);

			// Update leaderboard if an valid index was determined.
			if (index >= 0 && index < mLeaderboard.length) {
				return mLeaderboard[index];
			}

			return -1;
		}
	}

	/**
	 * Create a new instance of the leaderboard.
	 * 
	 * @param resources
	 */
	public Leaderboard(Resources resources) {
		mGamesClient = null;
		mResources = resources;
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
	 * Notifies the leaderboard that the sign in has failed.
	 */
	public void signInFailed() {
		mGamesClient = null;
	}

	/**
	 * Informs the leaderboard about the games client for which a succesfull
	 * connection to Google was made.
	 * 
	 * @param gamesClient
	 *            The games client which is connected with Google.
	 */
	public void signedIn(GamesClient gamesClient) {
		mGamesClient = gamesClient;
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
	public void onPuzzleSolvedWithoutCheats(int gridSize,
			PuzzleComplexity puzzleComplexity, boolean hideOperators,
			long timePlayed) {

		if (mGamesClient == null) {
			// Results cannot be pushed to Google Play Services now
			return;
		}

		int leaderboardResId = LeaderboardType.getResId(gridSize,
				puzzleComplexity, hideOperators);
		if (leaderboardResId >= 0) {
			mGamesClient.submitScore(mResources.getString(leaderboardResId),
					timePlayed);
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
}
