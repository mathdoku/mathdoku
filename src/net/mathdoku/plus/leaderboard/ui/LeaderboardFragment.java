package net.mathdoku.plus.leaderboard.ui;

import java.util.ArrayList;
import java.util.List;

import net.mathdoku.plus.R;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.LeaderboardType;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter.ScoreOrigin;
import net.mathdoku.plus.storage.database.LeaderboardRankRow;
import net.mathdoku.plus.ui.base.GooglePlayServiceFragmentActivity;
import net.mathdoku.plus.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.games.GamesClient;

public class LeaderboardFragment extends android.support.v4.app.ListFragment {
	public final static String TAG = "MathDoku.LeaderboardFragment";

	public final static String ARG_GRID_SIZE = "Leaderboard.arg_grid_size";

	public LeaderboardArrayAdapter mLeaderboardArrayAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle bundle = getArguments();
		int gridSize = bundle.getInt(ARG_GRID_SIZE);

		Resources resources = getActivity().getResources();

		// Create the list of leaderboards which have to be displayed.
		List<LeaderboardListItem> leaderboardList = new ArrayList<LeaderboardListItem>();
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, false,
				PuzzleComplexity.VERY_EASY));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, false,
				PuzzleComplexity.EASY));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, false,
				PuzzleComplexity.NORMAL));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, false,
				PuzzleComplexity.DIFFICULT));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, false,
				PuzzleComplexity.VERY_DIFFICULT));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, true,
				PuzzleComplexity.VERY_EASY));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, true,
				PuzzleComplexity.EASY));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, true,
				PuzzleComplexity.NORMAL));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, true,
				PuzzleComplexity.DIFFICULT));
		leaderboardList.add(new LeaderboardListItem(resources, gridSize, true,
				PuzzleComplexity.VERY_DIFFICULT));

		// Create the adapter and connect it to the listview.
		mLeaderboardArrayAdapter = new LeaderboardArrayAdapter(getActivity(),
				leaderboardList);
		setListAdapter(mLeaderboardArrayAdapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		String leaderboardId = mLeaderboardArrayAdapter
				.getLeaderboardId(position);
		if (leaderboardId != null) {
			// Connect to the gamesclient of the activity to start the
			// Google Play Services leaderboard intent.
			Activity activity = getActivity();
			if (activity instanceof LeaderboardFragmentActivity) {
				GamesClient gamesClient = ((LeaderboardFragmentActivity) activity)
						.getGamesClient();
				if (gamesClient != null) {
					Intent intent = gamesClient
							.getLeaderboardIntent(leaderboardId);
					if (intent != null) {
						// The OnActivityResult is handled by super class
						// GooglePlayServiceFragmentActivity. Therefore the
						// return code of that class is used here.
						startActivityForResult(intent,
								GooglePlayServiceFragmentActivity.RC_UNUSED);
					}
				}
			}
		}
	}

	/**
	 * Adapter for handling the listview content.
	 */
	public class LeaderboardArrayAdapter extends
			ArrayAdapter<LeaderboardListItem> {
		// The activity for which the adapter has been created.
		private final Activity mActivity;

		// The list of leaderboard which are available in this adapter.
		private final List<LeaderboardListItem> mLeaderboardList;

		// Fields to be stored and/or displayed for an item in this adapter.
		class ViewHolder {
			public ImageView mLeaderboardIcon;
			public TextView mLeaderboardScoreLabel;
			public TextView mLeaderboardScoreDisplay;
			public TextView mLeaderboardNotPlayed;
			public TextView mLeaderboardRankDisplay;
		}

		/**
		 * Creates a new instance of {@link LeaderboardArrayAdapter}.
		 * 
		 * @param activity
		 *            The activity which creates this adapater.
		 */
		public LeaderboardArrayAdapter(Activity activity,
				List<LeaderboardListItem> leaderboardList) {
			super(activity, R.layout.leaderboard_listview_item, leaderboardList);
			mActivity = activity;
			mLeaderboardList = leaderboardList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View listViewItem = convertView;

			// Create a new view only in case no existing view can be re-used
			// (ViewHolder pattern).
			if (listViewItem == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				listViewItem = inflater.inflate(
						R.layout.leaderboard_listview_item, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.mLeaderboardIcon = (ImageView) listViewItem
						.findViewById(R.id.leaderboard_icon);
				viewHolder.mLeaderboardScoreLabel = (TextView) listViewItem
						.findViewById(R.id.leaderboard_score_label);
				viewHolder.mLeaderboardScoreDisplay = (TextView) listViewItem
						.findViewById(R.id.leaderboard_score_display);
				viewHolder.mLeaderboardNotPlayed = (TextView) listViewItem
						.findViewById(R.id.leaderboard_not_played);
				viewHolder.mLeaderboardRankDisplay = (TextView) listViewItem
						.findViewById(R.id.leaderboard_rank_display);
				listViewItem.setTag(viewHolder);
			}

			// Get the viewHolder data for this row view.
			ViewHolder viewHolder = (ViewHolder) listViewItem.getTag();

			// Replace data in the view holder with data of new position.
			LeaderboardListItem leaderboardListItem = mLeaderboardList
					.get(position);
			viewHolder.mLeaderboardIcon.setImageResource(leaderboardListItem
					.getIconResId());
			if (leaderboardListItem.hasScore()) {
				viewHolder.mLeaderboardScoreLabel.setVisibility(View.VISIBLE);
				viewHolder.mLeaderboardScoreDisplay.setText(leaderboardListItem
						.getScoreDisplay());
				viewHolder.mLeaderboardScoreDisplay.setVisibility(View.VISIBLE);
				viewHolder.mLeaderboardNotPlayed.setVisibility(View.GONE);
				viewHolder.mLeaderboardRankDisplay.setText(leaderboardListItem
						.getRankDisplay());
				viewHolder.mLeaderboardRankDisplay.setVisibility(View.VISIBLE);
			} else {
				viewHolder.mLeaderboardScoreLabel.setVisibility(View.GONE);
				viewHolder.mLeaderboardScoreDisplay.setVisibility(View.GONE);
				viewHolder.mLeaderboardNotPlayed.setVisibility(View.VISIBLE);
				viewHolder.mLeaderboardRankDisplay
						.setVisibility(View.INVISIBLE);
			}
			return listViewItem;
		}

		/**
		 * Get the leaderboard id which is associated with the given position in
		 * the leaderboard adapter.
		 * 
		 * @param position
		 *            The position of the item to be retrieved.
		 * @return The leaderboard id.
		 */
		public String getLeaderboardId(int position) {
			return mLeaderboardList.get(position).getLeaderboardId();
		}
	}

	/**
	 * Storage for leaderboard details of leaderboards which have to be
	 * displayed in the listview.
	 */
	public class LeaderboardListItem {
		private final String mLeaderboardId;
		private final int mLeaderboardIconResId;
		private final String mLeaderboardDescription;
		private final boolean mDisplayScore;
		private final String mLeaderboardScoreDisplay;
		private final String mLeaderboardRankDisplay;

		/**
		 * Creates a new instance of {@link LeaderboardListItem}.
		 * 
		 * @param gridSize
		 *            The size of the grid.
		 * @param hideOperators
		 *            True in case operators are hidden. False otherwise
		 * @param puzzleComplexity
		 *            The complexity level of the puzzle.
		 */
		public LeaderboardListItem(Resources resources, int gridSize,
				boolean hideOperators, PuzzleComplexity puzzleComplexity) {
			super();

			// Get the leaderboard index value
			int index = LeaderboardType.getIndex(gridSize, hideOperators,
					puzzleComplexity);

			// Determine the leaderboard id for the leaderboard index
			mLeaderboardId = resources.getString(LeaderboardType
					.getResId(index));
			mLeaderboardIconResId = LeaderboardType.getIconResId(index);
			mLeaderboardDescription = "Grid " + gridSize
					+ "\nOperators hidden: " + hideOperators + "\nComplexity "
					+ puzzleComplexity.toString();

			// Retrieve leaderboard score from local database
			LeaderboardRankRow leaderboardRankRow = new LeaderboardRankDatabaseAdapter()
					.get(mLeaderboardId);
			if (leaderboardRankRow == null
					|| leaderboardRankRow.mScoreOrigin == ScoreOrigin.NONE) {
				mDisplayScore = false;
				mLeaderboardScoreDisplay = null;
				mLeaderboardRankDisplay = null;
			} else {
				mDisplayScore = true;
				mLeaderboardScoreDisplay = Util
						.durationTimeToString(leaderboardRankRow.mRawScore);
				mLeaderboardRankDisplay = leaderboardRankRow.mRankDisplay;
			}
		}

		public String getLeaderboardId() {
			return mLeaderboardId;
		}

		public int getIconResId() {
			return mLeaderboardIconResId;
		}

		public String getDescription() {
			return mLeaderboardDescription;
		}

		public boolean hasScore() {
			return mDisplayScore;
		}

		public String getScoreDisplay() {
			return mLeaderboardScoreDisplay;
		}

		public String getRankDisplay() {
			return mLeaderboardRankDisplay;
		}
	}
}