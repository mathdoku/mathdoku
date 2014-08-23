package net.mathdoku.plus.leaderboard.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.LeaderboardType;
import net.mathdoku.plus.leaderboard.ui.LeaderboardOverviewActivity.LeaderboardFilter;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends android.support.v4.app.Fragment {
	@SuppressWarnings("unused")
	private static final String TAG = LeaderboardFragment.class.getName();

	public static final String ARG_GRID_SIZE = "Leaderboard.arg_grid_size";
	public static final String ARG_FILTER = "Leaderboard.arg_filter";

	// The view holding all data of this fragment.
	private View mRootView;

	// The inflater used for the fragment
	private LayoutInflater mLayoutInflater;

	// Grid size of leaderboards displayed in the fragment
	private int mGridSize;

	// The filter which is applied to the leaderboard list
	private LeaderboardFilter mLeaderboardFilter;

	// The leaderboard fragment sections which are available in this leaderboard
	// fragment.
	private List<LeaderboardFragmentSection> mLeaderboardFragmentSectionsAvailable;
	private LeaderboardFragmentSection mLeaderboardFragmentSectionForEmptyList;

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
	 * Fill the root view with the list of leaderboards.
	 */
	public void refresh() {
		if (LeaderboardType.notDefinedForGridSize(mGridSize)) {
			return;
		}

		// Append all views to the fragment
		LinearLayout linearLayout = (LinearLayout) mRootView
				.findViewById(R.id.leaderboard_list);
		linearLayout.removeAllViews();

		// Add leaderboard fragment for each combination of operator visibility
		// and complexity
		mLeaderboardFragmentSectionsAvailable = createLeaderboardFragmentSections();
		for (LeaderboardFragmentSection leaderboardSection : mLeaderboardFragmentSectionsAvailable) {
			linearLayout.addView(leaderboardSection.getView());
		}

		// The leaderboard fragment below will be displayed only in case a
		// filter is applied which would not select any of the leaderboard
		// fragment sections created above. To prevent an empty list from being
		// shown, the fragment section below will be presented.
		mLeaderboardFragmentSectionForEmptyList = new LeaderboardFragmentSection(
				this, mGridSize);
		linearLayout.addView(mLeaderboardFragmentSectionForEmptyList.getView());

		// Apply the leaderboard filter so the view is initially displayed with
		// correct filter.
		setLeaderboardFilter(mLeaderboardFilter);
	}

	private List<LeaderboardFragmentSection> createLeaderboardFragmentSections() {
		List<LeaderboardFragmentSection> leaderboardFragmentSections = new ArrayList<LeaderboardFragmentSection>();
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, false, PuzzleComplexity.VERY_EASY));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, false, PuzzleComplexity.EASY));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, false, PuzzleComplexity.NORMAL));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, false, PuzzleComplexity.DIFFICULT));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, false, PuzzleComplexity.VERY_DIFFICULT));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, true, PuzzleComplexity.VERY_EASY));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, true, PuzzleComplexity.EASY));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, true, PuzzleComplexity.NORMAL));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, true, PuzzleComplexity.DIFFICULT));
		leaderboardFragmentSections.add(new LeaderboardFragmentSection(this,
				mGridSize, true, PuzzleComplexity.VERY_DIFFICULT));

		return leaderboardFragmentSections;
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

		boolean hideLeaderboardSectionForEmptyList = false;
		for (LeaderboardFragmentSection leaderboardSection : mLeaderboardFragmentSectionsAvailable) {
			if (isLeaderboardFragmentSectionVisibleForLeaderboardFilter(
					leaderboardSection, mLeaderboardFilter)) {
				leaderboardSection.setVisibility(View.VISIBLE);
				hideLeaderboardSectionForEmptyList = true;
			} else {
				leaderboardSection.setVisibility(View.GONE);
			}
		}

		mLeaderboardFragmentSectionForEmptyList
				.setVisibility(hideLeaderboardSectionForEmptyList ? View.GONE
						: View.VISIBLE);
	}

	private boolean isLeaderboardFragmentSectionVisibleForLeaderboardFilter(
			LeaderboardFragmentSection leaderboardFragmentSection,
			LeaderboardOverviewActivity.LeaderboardFilter leaderboardFilter) {
		if (leaderboardFilter == LeaderboardOverviewActivity.LeaderboardFilter.MY_LEADERBOARDS
				&& leaderboardFragmentSection.hasNoScore()) {
			return false;
		}

		if (leaderboardFilter == LeaderboardOverviewActivity.LeaderboardFilter.HIDDEN_OPERATORS
				&& !leaderboardFragmentSection.hasHiddenOperators()) {
			return false;
		}

		// noinspection RedundantIfStatement
		if (leaderboardFilter == LeaderboardOverviewActivity.LeaderboardFilter.VISIBLE_OPERATORS
				&& leaderboardFragmentSection.hasHiddenOperators()) {
			return false;
		}

		return true;
	}

	public LayoutInflater getLayoutInflater() {
		return mLayoutInflater;
	}
}
