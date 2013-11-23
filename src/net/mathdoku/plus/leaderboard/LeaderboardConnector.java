package net.mathdoku.plus.leaderboard;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.ui.TopScoreDialog;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter.ScoreOrigin;
import net.mathdoku.plus.storage.database.LeaderboardRankRow;
import net.mathdoku.plus.ui.base.AppFragmentActivity;
import net.mathdoku.plus.util.Util;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;

public class LeaderboardConnector {
	public final static String TAG = "MathDoku.Leaderboard";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	@SuppressWarnings("PointlessBooleanExpression")
	public static final boolean DEBUG = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// Reference to the games client of google play services.
	private final GamesClient mGamesClient;

	// Reference to the context
	private final AppFragmentActivity mAppFragmentActivity;

	// The Google+ leaderboard id's. The items in this array are kept in sync
	// with array mLeaderboardResId. The array is only initialized in DEBUG
	// mode.
	private static String[] mLeaderboardId = null;

	/**
	 * Create a new instance of the leaderboard.
	 * 
	 * @param resources
	 */
	public LeaderboardConnector(AppFragmentActivity appFragmentActivity,
			final GamesClient gamesClient) {
		mGamesClient = gamesClient;
		mAppFragmentActivity = appFragmentActivity;

		if (DEBUG) {
			// Fill the array of Google+ leaderboard id's based on the resource
			// id's. This facilitates searching for a resource id based on the
			// Google+ leaderboard id.
			Resources resources = appFragmentActivity.getResources();
			mLeaderboardId = new String[LeaderboardType.MAX_LEADERBOARDS];
			for (int i = 0; i < mLeaderboardId.length; i++) {
				mLeaderboardId[i] = new String(
						resources.getString(LeaderboardType.getResId(i)));
			}
		}
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
	 * Submits a score to a leaderboard.
	 * 
	 * @param statisticsId
	 *            The statistics id in which the score is stored.
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
	public void submitScore(final int statisticsId, int gridSize,
			PuzzleComplexity puzzleComplexity, boolean hideOperators,
			long timePlayed) {
		// Check boundaries of time played
		if (timePlayed <= 0 || timePlayed == Long.MAX_VALUE) {
			return;
		}

		// Check if already signed in
		if (isSignedIn() == false) {
			return;
		}

		// Determine the leaderboardId to which the score has to be submitted.
		String leaderboardId = mAppFragmentActivity.getResources().getString(
				LeaderboardType.getResId(LeaderboardType.getIndex(gridSize,
						hideOperators, puzzleComplexity)));

		if (DEBUG) {
			Log.i(TAG, "Submit new score " + timePlayed + " for leaderboard"
					+ getLeaderboardNameForLogging(leaderboardId)
					+ " with callback listener");
		}

		// Submit the score as immediate. Upon receiving confirmation that the
		// score was processed by Google Play Services, the leaderboard rank
		// database can be updated.
		mGamesClient.submitScoreImmediate(new OnScoreSubmittedListener() {
			@Override
			public void onScoreSubmitted(int statusCode,
					SubmitScoreResult submitScoreResult) {
				if (statusCode == GamesClient.STATUS_OK
						&& submitScoreResult != null) {
					// The score was submitted and processed by Google Play
					// Services.
					if (DEBUG) {
						Log.i(TAG,
								"The onScoreSubmitted listener for method submitScore has "
										+ "been called succesfully for leaderboard "
										+ getLeaderboardNameForLogging(submitScoreResult
												.getLeaderboardId()));
					}

					// Retrieve the current rank of the player. This rank
					// information is needed to update the leaderboard rank
					// information.
					new LeaderboardRankPlayer(LeaderboardConnector.this,
							new LeaderboardRankPlayer.Listener() {

								@Override
								public void onLeaderboardRankLoaded(
										Leaderboard leaderboard,
										LeaderboardScore leaderboardScore) {
									// The leaderboard rank for the current
									// player has been received.
									onRankCurrentPlayerReceived(leaderboard,
											leaderboardScore, true);
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
		}, leaderboardId, timePlayed);
	}

	/**
	 * Updates the leaderboard rank information based on score received.
	 * 
	 * @param leaderboard
	 * @param leaderboardScore
	 */
	protected void onRankCurrentPlayerReceived(Leaderboard leaderboard,
			LeaderboardScore leaderboardScore, boolean displayToast) {
		if (leaderboard == null || leaderboardScore == null) {
			return;
		}

		// Get the id of the leaderboard as used by Google Play Services
		String leaderboardId = leaderboard.getLeaderboardId();

		if (DEBUG) {
			Log.i(TAG, "Received rank for current Player on leaderboard"
					+ getLeaderboardNameForLogging(leaderboardId));
		}

		LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapter = new LeaderboardRankDatabaseAdapter();

		// Get the current score registered for the leaderboard.
		LeaderboardRankRow leaderboardRankRow = leaderboardRankDatabaseAdapter
				.get(leaderboardId);
		if (leaderboardRankRow.mScoreOrigin == ScoreOrigin.NONE
				|| leaderboardRankRow.mRawScore > leaderboardScore
						.getRawScore()) {
			// The score which was registered on Google Play Services is better
			// than the local top score. This can only happen in case the user
			// has achieved that score using another device or in case the app
			// is re-installed or the database was removed manually.
			if (DEBUG) {
				if (leaderboardRankRow.mScoreOrigin == ScoreOrigin.NONE) {
					Log.i(TAG,
							"No local score does yet exist for leaderboard "
									+ getLeaderboardNameForLogging(leaderboardId)
									+ ". The top score as registered on Google Play Services ("
									+ leaderboardScore.getRawScore()
									+ ") will be set as the best score for this leaderboard.");
				} else {
					Log.i(TAG,
							"The local top score ("
									+ leaderboardRankRow.mRawScore
									+ ") is not as good as the top score as registered on Google Play Services ("
									+ leaderboardScore.getRawScore()
									+ ") for leaderboard"
									+ getLeaderboardNameForLogging(leaderboardId)
									+ ". The local top score will be updated to this score.");
				}
			}

			// Update both the score and ranking information for this
			// leaderboard.
			leaderboardRankDatabaseAdapter.updateWithGooglePlayScore(
					leaderboardId, leaderboardScore.getRawScore(),
					leaderboardScore.getRank(),
					leaderboardScore.getDisplayRank());

			// No toast may be displayed as the top score on Google Play
			// Services was not improved.
			displayToast = false;
		} else {
			// Update the leaderboard ranking information only
			new LeaderboardRankDatabaseAdapter().updateWithGooglePlayRank(
					leaderboardId, leaderboardScore.getRank(),
					leaderboardScore.getDisplayRank());
		}

		// Display a toast containing the ranking information for the score.
		if (displayToast) {
			new TopScoreDialog(mAppFragmentActivity,
					LeaderboardType.getIconResId(LeaderboardType.getIndex(
							leaderboardRankRow.mGridSize,
							leaderboardRankRow.mOperatorsHidden,
							leaderboardRankRow.mPuzzleComplexity)),
					Util.durationTimeToString(leaderboardScore.getRawScore()),
					leaderboardScore.getDisplayRank()).show();
			if (DEBUG) {
				Log.i(TAG, "Leaderboard: " + leaderboard.getDisplayName()
						+ "\n" + "Score: " + leaderboardScore.getDisplayScore()
						+ "\n" + "Rank: " + leaderboardScore.getDisplayRank());
			}
		}
	}

	/**
	 * Get the name for a leaderboard based on the leaderboard id as used by
	 * Google Play Services. Should only be used in DEBUG mode.
	 * 
	 * @param leaderboardId
	 *            The leaderboard id as used by Google Play Services.
	 * @return The description of the leaderboard name. Null if not in DEBUG
	 *         mode.
	 */
	protected String getLeaderboardNameForLogging(String leaderboardId) {
		if (DEBUG) {
			// Check if reference list of leaderboard id is initialized.
			if (mLeaderboardId == null) {
				return "<<mLeaderboardId is not initialized>>";
			}

			// Search for the leaderboard id
			for (int i = 0; i < mLeaderboardId.length; i++) {
				if (mLeaderboardId[i].equals(leaderboardId)) {
					return "[size "
							+ LeaderboardType.getGridSize(i)
							+ ", "
							+ (LeaderboardType.hasHiddenOperator(i) ? "hidden operators"
									: "visible operators") + ", "
							+ LeaderboardType.getPuzzleComplexity(i).toString()
							+ "]";
				}
			}

			// The leaderboard type was not found
			return "<<leaderboardId " + leaderboardId + " is invalid>>";
		} else {
			return null;
		}
	}

	/**
	 * Get the games client used by the leaderboard connector.
	 * 
	 * @return The games client used by the leaderboard connector.
	 */
	protected GamesClient getGamesClient() {
		return mGamesClient;
	}
}