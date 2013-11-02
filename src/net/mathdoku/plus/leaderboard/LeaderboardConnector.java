package net.mathdoku.plus.leaderboard;

import java.text.DateFormat;
import java.util.ArrayList;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter.ScoreOrigin;
import net.mathdoku.plus.storage.database.LeaderboardRankRow;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;

public class LeaderboardConnector {
	public final static String TAG = "MathDoku.Leaderboard";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG = (Config.mAppMode == AppMode.DEVELOPMENT) && true;

	// Reference to the games client of google play services.
	private final GamesClient mGamesClient;

	// Reference to the context
	private final PuzzleFragmentActivity mPuzzleFragmentActivity;

	// Reference to translate leaderboard id's back to leaderboard indexes
	private static ArrayList<String> mLeaderboardIds;

	/**
	 * Create a new instance of the leaderboard.
	 * 
	 * @param resources
	 */
	public LeaderboardConnector(PuzzleFragmentActivity puzzleFragmentActivity,
			final GamesClient gamesClient) {
		mGamesClient = gamesClient;
		mPuzzleFragmentActivity = puzzleFragmentActivity;

		// Store all leaderboards id's and its corresponding leaderboard type
		// index so the leaderboard index can be retrieved by searching on the
		// leaderboard id.
		mLeaderboardIds = new ArrayList<String>();
		Resources resources = mPuzzleFragmentActivity.getResources();
		for (int i = 0; i < LeaderboardType.MAX_LEADERBOARDS; i++) {
			mLeaderboardIds.add(i,
					resources.getString(LeaderboardType.getResId(i)));
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
		String leaderboardId = mPuzzleFragmentActivity.getResources()
				.getString(
						LeaderboardType.getResId(LeaderboardType.getIndex(
								gridSize, hideOperators, puzzleComplexity)));

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
	private void onRankCurrentPlayerReceived(Leaderboard leaderboard,
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
			Toast.makeText(
					mPuzzleFragmentActivity,
					"Leaderboard: " + leaderboard.getDisplayName() + "\n"
							+ "Your rank: " + leaderboardScore.getDisplayRank()
							+ "\n" + "Your time: "
							+ leaderboardScore.getDisplayScore(),
					Toast.LENGTH_LONG).show();
			if (DEBUG) {
				Log.i(TAG,
						"Leaderboard: " + leaderboard.getDisplayName() + "\n"
								+ "Your rank: "
								+ leaderboardScore.getDisplayRank() + "\n"
								+ "Your time: "
								+ leaderboardScore.getDisplayScore());
			}
		}
		return;
	}

	/**
	 * Update all leaderboards with ranking information from Google Play
	 * Services. This is useful after a clean install or in case a top score was
	 * achieved while no connection with Google Play Services was available.
	 * 
	 * Each time this method is called the leaderboard which has not been
	 * updated for the longest time will be updated first. Upon completion of
	 * update that leaderboard the methode is called again for processing the
	 * next leaderboard.
	 */
	public void updateLeaderboardsWithMissingRankInformation() {
		// Check if already signed in
		if (isSignedIn() == false) {
			return;
		}

		// Asynchronously process all leaderboards for which the score was
		// submitted before but which was not processed completely.
		new Thread(new Runnable() {

			@Override
			public void run() {
				// Initialize the leaderboards if needed.
				if (mPuzzleFragmentActivity.mMathDokuPreferences
						.isLeaderboardsInitialized() == false) {
					LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapter = new LeaderboardRankDatabaseAdapter();
					for (String leaderboardId : mLeaderboardIds) {
						// Create a leaderboard record if currently does not yet
						// exist.
						if (leaderboardRankDatabaseAdapter.get(leaderboardId) == null) {
							leaderboardRankDatabaseAdapter
									.insertInitializedLeaderboard(leaderboardId);
						}
					}
					mPuzzleFragmentActivity.mMathDokuPreferences
							.setLeaderboardsInitialized();
					if (DEBUG) {
						Log.i(TAG,
								"All leaderboards have been initialized in the database.");
					}
				}

				// Get a leaderboard which needs to be updated
				LeaderboardRankRow leaderboardRankRow = new LeaderboardRankDatabaseAdapter()
						.getMostOutdatedLeaderboardRank();
				if (leaderboardRankRow == null) {
					// No leadeboards to be updated.
					if (DEBUG) {
						Log.i(TAG, "All leaderboards are up to date.");
					}
					return;
				}

				if (leaderboardRankRow.mScoreOrigin == ScoreOrigin.LOCAL_DATABASE
						&& leaderboardRankRow.mRawScore > 0) {
					// A local top score was already registered for this
					// leaderboard. This score is submitted.

					if (DEBUG) {
						Log.i(TAG,
								"Submit score ("
										+ leaderboardRankRow.mRawScore
										+ ") for existing leaderboard"
										+ getLeaderboardNameForLogging(leaderboardRankRow.mLeaderboardId)
										+ " which was last submitted on "
										+ DateFormat
												.getDateTimeInstance()
												.format(leaderboardRankRow.mDateSubmitted)
										+ " with callback listener");
					}
					mGamesClient.submitScoreImmediate(
							new OnScoreSubmittedListener() {

								@Override
								public void onScoreSubmitted(int statusCode,
										SubmitScoreResult submitScoreResult) {
									if (statusCode == GamesClient.STATUS_OK
											&& submitScoreResult != null) {
										// The score was submitted and processed
										// by Google Play Services.
										if (DEBUG) {
											Log.i(TAG,
													"Score for leaderboard"
															+ getLeaderboardNameForLogging(submitScoreResult
																	.getLeaderboardId())
															+ " has been processed by Google Play Services.");
										}

										// Retrieve the current rank of the
										// player
										new LeaderboardRankPlayer(
												LeaderboardConnector.this,
												new LeaderboardRankPlayer.Listener() {

													@Override
													public void onLeaderboardRankLoaded(
															Leaderboard leaderboard,
															LeaderboardScore leaderboardScore) {
														onRankCurrentPlayerReceived(
																leaderboard,
																leaderboardScore,
																false);
														// Start process again
														// for the next
														// leaderboard.
														updateLeaderboardsWithMissingRankInformation();
													}

													@Override
													public void onNoRankFound(
															Leaderboard leaderboard) {
														// Nothing to do here.
														if (DEBUG) {
															Log.i(TAG,
																	"ERROR: it should not possible that a player rank "
																			+ "is not found after it has just been "
																			+ "successfully submitted and received.");
														}
													}
												})
												.loadCurrentPlayerRank(submitScoreResult
														.getLeaderboardId());
									}
								}
							}, leaderboardRankRow.mLeaderboardId,
							leaderboardRankRow.mRawScore);
				} else {
					// Only the ranking information needs to be updated.
					new LeaderboardRankPlayer(LeaderboardConnector.this,
							new LeaderboardRankPlayer.Listener() {

								@Override
								public void onLeaderboardRankLoaded(
										Leaderboard leaderboard,
										LeaderboardScore leaderboardScore) {
									onRankCurrentPlayerReceived(leaderboard,
											leaderboardScore, false);
									// Start process again
									// for the next
									// leaderboard.
									updateLeaderboardsWithMissingRankInformation();
								}

								@Override
								public void onNoRankFound(
										Leaderboard leaderboard) {
									// The current player has never played this
									// leaderboard as no rank for this player
									// was found on Google Play Services.
									if (DEBUG) {
										Log.i(TAG,
												"No local top score and no ranking information "
														+ "was found for the current user for leaderboard "
														+ getLeaderboardNameForLogging(leaderboard
																.getLeaderboardId())
														+ ".");
									}

									new LeaderboardRankDatabaseAdapter()
											.updateWithGooglePlayRankNotAvailable(leaderboard
													.getLeaderboardId());

									// Start process again for the next
									// leaderboard.
									updateLeaderboardsWithMissingRankInformation();
								}
							})
							.loadCurrentPlayerRank(leaderboardRankRow.mLeaderboardId);
				}
			}
		}).start();
	}

	/**
	 * Get the name for a leaderboard based on the leaderboard id as used by
	 * Google Play Services.
	 * 
	 * @param leaderboardId
	 *            The leaderboard id as used by Google Play Services.
	 * @return The description of the leaderboard name.
	 */
	protected String getLeaderboardNameForLogging(String leaderboardId) {
		// Check if reference list of leaderboard id is initialized.
		if (mLeaderboardIds == null) {
			return "<<mLeaderboardId is not initialized>>";
		}

		// Check if the given id does exists in the list.
		int leaderboardIndex = mLeaderboardIds.indexOf(leaderboardId);
		if (leaderboardIndex < 0) {
			return "<<leaderboardId " + leaderboardId + " is invalid>>";
		}

		// The leaderboard id is found in the list. As the index in this list
		// matches the leaderboard indexes used by the leaderboard type the
		// description can be retrieved.
		return LeaderboardType.getLeaderboardNameForLogging(leaderboardIndex);
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