package net.mathdoku.plus.leaderboard.ui;

import net.mathdoku.plus.R;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.LeaderboardType;
import net.mathdoku.plus.leaderboard.ui.LeaderboardFragmentActivity.LeaderboardFilter;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter.ScoreOrigin;
import net.mathdoku.plus.storage.database.LeaderboardRankRow;
import net.mathdoku.plus.ui.base.GooglePlayServiceFragmentActivity;
import net.mathdoku.plus.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.games.GamesClient;

public class LeaderboardFragment extends android.support.v4.app.Fragment {
	public final static String TAG = "MathDoku.LeaderboardFragment";

	public final static String ARG_GRID_SIZE = "Leaderboard.arg_grid_size";
	public final static String ARG_FILTER = "Leaderboard.arg_filter";

	// The view holding all data of this fragment.
	private View mRootView;

	// The inflater used for the fragment
	private LayoutInflater mLayoutInflater;

	// Grid size of leaderboards displayed in the fragment
	private int mGridSize;

	// The filter which is applied to the leaderboard list
	private LeaderboardFilter mLeaderboardFilter;

	// The data of all (unfiltered) leaderboard available for this fragment.
	private LeaderboardSection[] mLeaderboardSection;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		mRootView = inflater.inflate(R.layout.leaderboard_fragment, container,
				false);

		// Get arguments from bundle
		Bundle bundle = getArguments();
		mGridSize = bundle.getInt(ARG_GRID_SIZE);
		mLeaderboardFilter = LeaderboardFilter.values()[bundle
				.getInt(ARG_FILTER)];

		refresh();

		return mRootView;
	}

	/**
	 * Fill the rootview with the list of leaderboards.
	 * 
	 * @param gridSize
	 * @param leaderboardFilter
	 */
	public void refresh() {
		Resources resources = getActivity().getResources();

		// Create the fixes list of available leaderboards.
		mLeaderboardSection = new LeaderboardSection[11];
		mLeaderboardSection[0] = new LeaderboardSection(resources, mGridSize,
				false, PuzzleComplexity.VERY_EASY);
		mLeaderboardSection[1] = new LeaderboardSection(resources, mGridSize,
				false, PuzzleComplexity.EASY);
		mLeaderboardSection[2] = new LeaderboardSection(resources, mGridSize,
				false, PuzzleComplexity.NORMAL);
		mLeaderboardSection[3] = new LeaderboardSection(resources, mGridSize,
				false, PuzzleComplexity.DIFFICULT);
		mLeaderboardSection[4] = new LeaderboardSection(resources, mGridSize,
				false, PuzzleComplexity.VERY_DIFFICULT);
		mLeaderboardSection[5] = new LeaderboardSection(resources, mGridSize,
				true, PuzzleComplexity.VERY_EASY);
		mLeaderboardSection[6] = new LeaderboardSection(resources, mGridSize,
				true, PuzzleComplexity.EASY);
		mLeaderboardSection[7] = new LeaderboardSection(resources, mGridSize,
				true, PuzzleComplexity.NORMAL);
		mLeaderboardSection[8] = new LeaderboardSection(resources, mGridSize,
				true, PuzzleComplexity.DIFFICULT);
		mLeaderboardSection[9] = new LeaderboardSection(resources, mGridSize,
				true, PuzzleComplexity.VERY_DIFFICULT);
		mLeaderboardSection[10] = new LeaderboardSection(resources, mGridSize);

		// Append all views to the fragment
		LinearLayout linearLayout = (LinearLayout) mRootView
				.findViewById(R.id.leaderboard_list);
		linearLayout.removeAllViews();
		for (int i = 0; i < mLeaderboardSection.length; i++) {
			linearLayout.addView(mLeaderboardSection[i].mView);
		}

		// Apply the leaderboard filter so the view is initially displayed with
		// correct filter.
		setLeaderboardFilter(mLeaderboardFilter);
	}

	/**
	 * Apply the given filter to the list of available leaderboards and only
	 * display the leaderboards that are valid for this filter.
	 * 
	 * @param leaderboardFilter
	 *            The filter to be applied.
	 */
	public void setLeaderboardFilter(LeaderboardFilter leaderboardFilter) {
		mLeaderboardFilter = leaderboardFilter;
		if (mLeaderboardSection != null) {
			int countVisibleLeaderboards = 0;
			for (LeaderboardSection leaderboardSection : mLeaderboardSection) {
				if (leaderboardSection.mDummyLeaderboard) {
					leaderboardSection.mView
							.setVisibility(countVisibleLeaderboards == 0 ? View.VISIBLE
									: View.GONE);
				} else {
					boolean visible = leaderboardSection
							.isValidForFilter(mLeaderboardFilter);
					if (visible) {
						countVisibleLeaderboards++;
						leaderboardSection.mView.setVisibility(View.VISIBLE);
					} else {
						leaderboardSection.mView.setVisibility(View.GONE);
					}
				}
			}
		}
	}

	/**
	 * Storage for leaderboard details of leaderboards which have to be
	 * displayed in the listview.
	 */
	public class LeaderboardSection {
		public final boolean mDummyLeaderboard;
		private final int mGridSize;
		private boolean mHideOperators;
		private PuzzleComplexity mPuzzleComplexity;
		private String mLeaderboardId;
		private boolean mHasScore;
		public final View mView;

		/**
		 * Creates a new instance of a {@link LeaderboardSection} for a
		 * leaderboard.
		 * 
		 * @param gridSize
		 *            The size of the grid.
		 * @param hideOperators
		 *            True in case operators are hidden. False otherwise
		 * @param puzzleComplexity
		 *            The complexity level of the puzzle.
		 */
		public LeaderboardSection(Resources resources, int gridSize,
				boolean hideOperators, PuzzleComplexity puzzleComplexity) {
			mDummyLeaderboard = false;
			mGridSize = gridSize;
			mHideOperators = hideOperators;
			mPuzzleComplexity = puzzleComplexity;

			// Get the view and the layout to store the leaderboard section
			mView = mLayoutInflater.inflate(R.layout.leaderboard_section, null);
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
			mLeaderboardId = resources.getString(LeaderboardType
					.getResId(index));
			mLeaderboardIcon.setImageResource(LeaderboardType
					.getIconResId(index));

			// Retrieve leaderboard score from local database
			LeaderboardRankRow leaderboardRankRow = new LeaderboardRankDatabaseAdapter()
					.get(mLeaderboardId);
			if (leaderboardRankRow == null
					|| leaderboardRankRow.mScoreOrigin == ScoreOrigin.NONE) {
				mHasScore = false;
				mLeaderboardScoreLabel.setVisibility(View.GONE);
				mLeaderboardScoreDisplay.setVisibility(View.GONE);
				mLeaderboardNotPlayed.setVisibility(View.VISIBLE);
				mLeaderboardRankDisplay.setVisibility(View.GONE);
			} else {
				mHasScore = true;
				mLeaderboardScoreLabel.setVisibility(View.VISIBLE);
				mLeaderboardScoreDisplay.setText(Util
						.durationTimeToString(leaderboardRankRow.mRawScore));
				mLeaderboardScoreDisplay.setVisibility(View.VISIBLE);
				mLeaderboardNotPlayed.setVisibility(View.GONE);
				mLeaderboardRankDisplay
						.setText(leaderboardRankRow.mRankDisplay);
				mLeaderboardRankDisplay.setVisibility(View.VISIBLE);
			}

			// Set a listeneren on the leaderboard linear layout to navigate to
			// the detail page of the leaderboard on Google Play Services.
			linearLayout.setClickable(true);
			linearLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Connect to the gamesclient of the activity to start
					// the Google Play Services leaderboard intent.
					Activity activity = getActivity();
					if (activity instanceof LeaderboardFragmentActivity) {
						GamesClient gamesClient = ((LeaderboardFragmentActivity) activity)
								.getGamesClient();
						if (gamesClient != null) {
							Intent intent = gamesClient
									.getLeaderboardIntent(mLeaderboardId);
							if (intent != null) {
								// The OnActivityResult is handled by super
								// class
								// GooglePlayServiceFragmentActivity.
								// Therefore the
								// return code of that class is used here.
								startActivityForResult(
										intent,
										GooglePlayServiceFragmentActivity.RC_UNUSED);
							}
						}
					}
				}
			});
		}

		/**
		 * Creates a new instance of a {@link LeaderboardSection} for the dummy
		 * leaderboard which is shown in case the use has not played any
		 * leaderboard for this gridsize and has enabled filter
		 * "My leaderboards only".
		 */
		public LeaderboardSection(Resources resources, int gridSize) {
			mDummyLeaderboard = true;
			mGridSize = gridSize;

			TextView textView = new TextView(getActivity());
			textView.setLayoutParams(new LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
			textView.setText(getResources().getString(
					R.string.leaderboard_none_played, mGridSize));
			textView.setTextSize(
					TypedValue.COMPLEX_UNIT_DIP,
					(int) (getResources().getDimension(
							net.mathdoku.plus.R.dimen.text_size_default) / getResources()
							.getDisplayMetrics().density));

			mView = textView;
		}

		/**
		 * Checks if the leaderboard section is valid for the given leaderboard
		 * filter.
		 * 
		 * @param leaderboardFilter
		 *            The leaderboard filter to check for.
		 * @return True in case the leaderboard is valid for the filter. False
		 *         otherwise.
		 */
		public boolean isValidForFilter(LeaderboardFilter leaderboardFilter) {
			if (mDummyLeaderboard
					&& leaderboardFilter != LeaderboardFilter.MY_LEADERBOARDS) {
				return false;
			}
			if (leaderboardFilter == LeaderboardFilter.MY_LEADERBOARDS
					&& mHasScore == false) {
				return false;
			}
			if (leaderboardFilter == LeaderboardFilter.HIDDEN_OPERATORS
					&& mHideOperators == false) {
				return false;
			}
			if (leaderboardFilter == LeaderboardFilter.VISBLE_OPERATORS
					&& mHideOperators == true) {
				return false;
			}
			return true;
		}
	}
}