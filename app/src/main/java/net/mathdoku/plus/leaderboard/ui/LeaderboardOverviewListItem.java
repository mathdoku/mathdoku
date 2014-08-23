package net.mathdoku.plus.leaderboard.ui;

import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.games.GamesClient;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.LeaderboardType;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRow;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;
import net.mathdoku.plus.ui.base.GooglePlayServiceFragmentActivity;
import net.mathdoku.plus.util.Util;

/**
 * Storage for leaderboard details of a single leaderboard which might be
 * displayed in a Leaderboard Overview list view.
 */
public class LeaderboardOverviewListItem {
	private final int mGridSize;
	private boolean mHideOperators;
	private PuzzleComplexity mPuzzleComplexity;
	private String mLeaderboardId;
	private boolean mHasScore;
	private final View mView;

	/**
	 * Creates a new instance of a {@link LeaderboardOverviewListItem} for a
	 * leaderboard.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 * @param hideOperators
	 *            True in case operators are hidden. False otherwise
	 * @param puzzleComplexity
	 *            The complexity level of the puzzle.
	 */
	public LeaderboardOverviewListItem(final LeaderboardOverview leaderboardOverview,
									   int gridSize, boolean hideOperators,
									   PuzzleComplexity puzzleComplexity) {
		mGridSize = gridSize;
		mHideOperators = hideOperators;
		mPuzzleComplexity = puzzleComplexity;

		// Get the view and the layout to store the leaderboard section
		mView = leaderboardOverview.getLayoutInflater().inflate(
				R.layout.leaderboard_section, null);

		// noinspection ConstantConditions
		LinearLayout linearLayout = (LinearLayout) mView
				.findViewById(R.id.leaderboard_section_layout);
		ImageView mLeaderboardIcon = (ImageView) mView
				.findViewById(R.id.leaderboard_icon);
		TextView mLeaderboardScoreLabel = (TextView) mView
				.findViewById(R.id.leaderboard_score_label);
		TextView mLeaderboardScoreDisplay = (TextView) mView
				.findViewById(R.id.leaderboard_score_display);
		TextView mLeaderboardNotPlayed = (TextView) mView
				.findViewById(R.id.leaderboard_not_played);
		TextView mLeaderboardRankDisplay = (TextView) mView
				.findViewById(R.id.leaderboard_rank_display);

		// Get the leaderboard index value
		int index = LeaderboardType.getIndex(mGridSize, mHideOperators,
				mPuzzleComplexity);

		// Determine the leaderboard id for the leaderboard index
		mLeaderboardId = leaderboardOverview.getResources().getString(
				LeaderboardType.getResId(index));
		mLeaderboardIcon.setImageResource(LeaderboardType.getIconResId(index));

		// Retrieve leaderboard score from local database
		LeaderboardRankRow leaderboardRankRow = new LeaderboardRankDatabaseAdapter()
				.get(mLeaderboardId);
		if (leaderboardRankRow == null
				|| leaderboardRankRow.getScoreOrigin() == LeaderboardRankDatabaseAdapter.ScoreOrigin.NONE) {
			mHasScore = false;
			mLeaderboardScoreLabel.setVisibility(View.GONE);
			mLeaderboardScoreDisplay.setVisibility(View.GONE);
			mLeaderboardNotPlayed.setVisibility(View.VISIBLE);
			mLeaderboardRankDisplay.setVisibility(View.GONE);
		} else {
			mHasScore = true;
			mLeaderboardScoreLabel.setVisibility(View.VISIBLE);
			mLeaderboardScoreDisplay.setText(Util
					.durationTimeToString(leaderboardRankRow.getRawScore()));
			mLeaderboardScoreDisplay.setVisibility(View.VISIBLE);
			mLeaderboardNotPlayed.setVisibility(View.GONE);
			mLeaderboardRankDisplay
					.setText(leaderboardRankRow.getRankDisplay());
			mLeaderboardRankDisplay.setVisibility(View.VISIBLE);
		}

		// Set a listener on the leaderboard linear layout to navigate to
		// the detail page of the leaderboard on Google Play Services.
		linearLayout.setClickable(true);
		linearLayout.setOnClickListener(new OnClickLeaderboardListener(
				leaderboardOverview));

		// Attach a long click listener to start a new game for the selected
		// leaderboard
		mView.setOnLongClickListener(new OnLongClickLeaderboardListener(
				leaderboardOverview));
	}

	/**
	 * Creates a new instance of a {@link LeaderboardOverviewListItem} for the
	 * dummy leaderboard which is shown in case the use has not played any
	 * leaderboard for this grid size and has enabled filter
	 * "My leaderboards only".
	 */
	public LeaderboardOverviewListItem(LeaderboardOverview leaderboardOverview, int gridSize) {
		mGridSize = gridSize;

		TextView textView = new TextView(leaderboardOverview.getActivity());
		textView.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.setText(leaderboardOverview.getResources().getString(
				R.string.leaderboard_none_played, mGridSize));
		textView.setTextSize(
				TypedValue.COMPLEX_UNIT_DIP,
				(int) (leaderboardOverview.getResources().getDimension(
						R.dimen.text_size_default) / leaderboardOverview
						.getResources()
						.getDisplayMetrics().density));

		mView = textView;
	}

	public View getView() {
		return mView;
	}

	public void setVisibility(int visibility) {
		mView.setVisibility(visibility);
	}

	public boolean hasNoScore() {
		return !mHasScore;
	}

	public boolean hasHiddenOperators() {
		return mHideOperators;
	}

	private class OnLongClickLeaderboardListener implements
			View.OnLongClickListener {
		private final LeaderboardOverview leaderboardOverview;

		public OnLongClickLeaderboardListener(
				LeaderboardOverview leaderboardOverview) {
			this.leaderboardOverview = leaderboardOverview;
		}

		@Override
		public boolean onLongClick(View v) {
			Activity activity = leaderboardOverview.getActivity();

			// Finish the leaderboard activity.
			activity.finish();

			// Restart the main activity of MathDoku
			Intent intent = PuzzleFragmentActivity
					.createIntentToStartNewPuzzleFromSelectedLeaderboardFragment(
							activity, mGridSize, mHideOperators,
							mPuzzleComplexity);
			leaderboardOverview.startActivity(intent);

			Preferences.getInstance().increaseLeaderboardsGamesCreated();

			return true;
		}
	}

	private class OnClickLeaderboardListener implements View.OnClickListener {
		private final LeaderboardOverview leaderboardOverview;

		public OnClickLeaderboardListener(
				LeaderboardOverview leaderboardOverview) {
			this.leaderboardOverview = leaderboardOverview;
		}

		@Override
		public void onClick(View v) {
			// Connect to the games client of the activity to start
			// the Google Play Services leaderboard intent.
			Activity activity = leaderboardOverview.getActivity();
			if (activity instanceof LeaderboardOverviewActivity) {
				GamesClient gamesClient = ((LeaderboardOverviewActivity) activity)
						.getGamesClient();
				if (gamesClient != null) {
					Intent intent = gamesClient
							.getLeaderboardIntent(mLeaderboardId);
					if (intent != null) {
						// The OnActivityResult is handled by super
						// class GooglePlayServiceFragmentActivity.
						// Therefore the return code of that class is
						// used here.
						leaderboardOverview.startActivityForResult(intent,
								GooglePlayServiceFragmentActivity.RC_UNUSED);

						Preferences
								.getInstance()
								.increaseLeaderboardsDetailsViewed();
					}
				}
			}
		}
	}
}
