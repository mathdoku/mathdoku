package net.mathdoku.plus.leaderboard.ui;

import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.LeaderboardType;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRow;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;
import net.mathdoku.plus.util.Util;

/**
 * Storage for leaderboard details of a single leaderboard which might be
 * displayed in a Leaderboard Overview list view.
 */
public class LeaderboardOverviewListItem {
	private final String mLeaderboardId;
	private final int mGridSize;
	private final boolean mHideOperators;
	private final PuzzleComplexity mPuzzleComplexity;
	private final View mLeaderboardListItemView;
	private final LeaderboardOverviewListItemType leaderboardOverviewListItemType;

	private enum LeaderboardOverviewListItemType {
		WITH_RANKING_INFORMATION, NO_RANKING_INFORMATION, PLACEHOLDER_EMPTY_OVERVIEW
	}

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
	public LeaderboardOverviewListItem(
			final LeaderboardOverview leaderboardOverview, int gridSize,
			boolean hideOperators, PuzzleComplexity puzzleComplexity) {
		mGridSize = gridSize;
		mHideOperators = hideOperators;
		mPuzzleComplexity = puzzleComplexity;

		// Inflate the view to display one leaderboard
		mLeaderboardListItemView = leaderboardOverview
				.getLayoutInflater()
				.inflate(R.layout.leaderboard_overview_list_item, null);

		// noinspection ConstantConditions
		ImageView mLeaderboardIcon = (ImageView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_icon);

		// Get the leaderboard index value
		int index = LeaderboardType.getIndex(mGridSize, mHideOperators,
				mPuzzleComplexity);

		// Determine the leaderboard id for the leaderboard index
		mLeaderboardId = leaderboardOverview.getResources().getString(
				LeaderboardType.getResId(index));
		mLeaderboardIcon.setImageResource(LeaderboardType.getIconResId(index));

		leaderboardOverviewListItemType = setupView();
		setupOnClickListener(leaderboardOverview);
		setupOnLongClickListener(leaderboardOverview);
	}

	/**
	 * Creates a new instance of a {@link LeaderboardOverviewListItem} for the
	 * dummy leaderboard which is shown in case the use has not played any
	 * leaderboard for this grid size and has enabled filter
	 * "My leaderboards only".
	 */
	public LeaderboardOverviewListItem(LeaderboardOverview leaderboardOverview,
			int gridSize) {
		mLeaderboardId = null;
		mGridSize = 0;
		mHideOperators = false;
		mPuzzleComplexity = null;
		leaderboardOverviewListItemType = LeaderboardOverviewListItemType.PLACEHOLDER_EMPTY_OVERVIEW;

		mLeaderboardListItemView = new TextView(
				leaderboardOverview.getContext());
		mLeaderboardListItemView.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		((TextView) mLeaderboardListItemView).setText(leaderboardOverview
				.getResources()
				.getString(R.string.leaderboard_none_played, gridSize));
		((TextView) mLeaderboardListItemView).setTextSize(
				TypedValue.COMPLEX_UNIT_DIP,
				(int) (leaderboardOverview.getResources().getDimension(
						R.dimen.text_size_default) / leaderboardOverview
						.getResources()
						.getDisplayMetrics().density));
	}

	private LeaderboardOverviewListItemType setupView() {
		// Retrieve leaderboard score from local database
		LeaderboardRankRow leaderboardRankRow = createLeaderboardRankDatabaseAdapter()
				.get(mLeaderboardId);
		if (leaderboardRankRow == null
				|| leaderboardRankRow.getScoreOrigin() == LeaderboardRankDatabaseAdapter.ScoreOrigin.NONE) {
			setupViewWithoutRankingInformation();
			return LeaderboardOverviewListItemType.NO_RANKING_INFORMATION;
		} else {
			setupViewWithRankingInformation(leaderboardRankRow);
			return LeaderboardOverviewListItemType.WITH_RANKING_INFORMATION;
		}
	}

	/**
	 * Package private method to allow unit test to overwrite the creation of a
	 * new Leaderboard Rank Database Adapter.
	 * 
	 * @return a new LeaderboardRankDatabaseAdapter
	 */
	LeaderboardRankDatabaseAdapter createLeaderboardRankDatabaseAdapter() {
		return new LeaderboardRankDatabaseAdapter();
	}

	private void setupViewWithoutRankingInformation() {
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_score_label))
				.setVisibility(View.GONE);
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_score_display))
				.setVisibility(View.GONE);
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_not_played))
				.setVisibility(View.VISIBLE);
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_rank_display))
				.setVisibility(View.GONE);
	}

	private void setupViewWithRankingInformation(
			LeaderboardRankRow leaderboardRankRow) {
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_score_label))
				.setVisibility(View.VISIBLE);
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_score_display)).setText(Util
				.durationTimeToString(leaderboardRankRow.getRawScore()));
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_score_display))
				.setVisibility(View.VISIBLE);
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_not_played))
				.setVisibility(View.GONE);
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_rank_display))
				.setText(leaderboardRankRow.getRankDisplay());
		((TextView) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_rank_display))
				.setVisibility(View.VISIBLE);
	}

	private void setupOnClickListener(LeaderboardOverview leaderboardOverview) {
		LinearLayout linearLayout = (LinearLayout) mLeaderboardListItemView
				.findViewById(R.id.leaderboard_section_layout);
		linearLayout.setClickable(true);
		linearLayout.setOnClickListener(new OnClickLeaderboardListener(
				leaderboardOverview));
	}

	private void setupOnLongClickListener(
			LeaderboardOverview leaderboardOverview) {
		// Attach a long click listener to start a new game for the selected
		// leaderboard
		mLeaderboardListItemView
				.setOnLongClickListener(new OnLongClickLeaderboardListener(
						leaderboardOverview));
	}

	public View getView() {
		return mLeaderboardListItemView;
	}

	public void setVisibility(int visibility) {
		mLeaderboardListItemView.setVisibility(visibility);
	}

	public boolean hasNoScore() {
		throwExceptionIfCalledForPlaceholder();
		return leaderboardOverviewListItemType == LeaderboardOverviewListItemType.NO_RANKING_INFORMATION;
	}

	private void throwExceptionIfCalledForPlaceholder() {
		if (leaderboardOverviewListItemType == LeaderboardOverviewListItemType.PLACEHOLDER_EMPTY_OVERVIEW) {
			throw new IllegalStateException(
					"Should not be called for the placeholder item.");
		}
	}

	public boolean hasHiddenOperators() {
		throwExceptionIfCalledForPlaceholder();
		return mHideOperators;
	}

	/**
	 * Navigate to the detail page on Google Play Services for this leaderboard.
	 */
	private class OnClickLeaderboardListener implements View.OnClickListener {
		private final LeaderboardOverview leaderboardOverview;

		public OnClickLeaderboardListener(
				LeaderboardOverview leaderboardOverview) {
			this.leaderboardOverview = leaderboardOverview;
		}

		@Override
		public void onClick(View view) {
			Activity activity = leaderboardOverview.getActivity();
			if (activity instanceof LeaderboardOverviewActivity) {
				((LeaderboardOverviewActivity) activity)
						.viewLeaderboardDetails(mLeaderboardId);
			}
		}
	}

	/**
	 * Finishes the current activity and starts a new puzzle with the same size,
	 * complexity and visibility of the operators as the leaderboard which was
	 * long clicked.
	 */
	private class OnLongClickLeaderboardListener implements
			View.OnLongClickListener {
		private final LeaderboardOverview leaderboardOverview;

		public OnLongClickLeaderboardListener(
				LeaderboardOverview leaderboardOverview) {
			this.leaderboardOverview = leaderboardOverview;
		}

		@Override
		public boolean onLongClick(View view) {
			Activity activity = leaderboardOverview.getActivity();

			// Finish the leaderboard overview activity.
			activity.finish();

			// Start a new puzzle with the same size, complexity and visibility
			// of the operators as the leaderboard which was long clicked.
			Intent intent = PuzzleFragmentActivity
					.createIntentToStartNewPuzzleFromSelectedLeaderboardFragment(
							activity, mGridSize, mHideOperators,
							mPuzzleComplexity);
			leaderboardOverview.startActivity(intent);

			Preferences.getInstance().increaseLeaderboardsGamesCreated();

			return true;
		}
	}

}
