package net.mathdoku.plus.leaderboard.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.mathdoku.plus.R;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.LeaderboardType;
import net.mathdoku.plus.ui.base.GooglePlayServiceFragmentActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.games.GamesClient;

public class LeaderboardFragment extends android.support.v4.app.ListFragment {
	public final static String TAG = "MathDoku.LeaderboardFragment";

	public final static String ARG_GRID_SIZE = "Leaderboard.arg_grid_size";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle bundle = getArguments();
		int gridSize = bundle.getInt(ARG_GRID_SIZE);

		// Create a list of leaderboards which have to be displayed in this
		// fragment.
		List<HashMap<String, String>> leaderboardList = new ArrayList<HashMap<String, String>>();
		leaderboardList.add(createLeaderboardListEntry(gridSize, false,
				PuzzleComplexity.VERY_EASY));
		leaderboardList.add(createLeaderboardListEntry(gridSize, false,
				PuzzleComplexity.EASY));
		leaderboardList.add(createLeaderboardListEntry(gridSize, false,
				PuzzleComplexity.NORMAL));
		leaderboardList.add(createLeaderboardListEntry(gridSize, false,
				PuzzleComplexity.DIFFICULT));
		leaderboardList.add(createLeaderboardListEntry(gridSize, false,
				PuzzleComplexity.VERY_DIFFICULT));
		leaderboardList.add(createLeaderboardListEntry(gridSize, true,
				PuzzleComplexity.VERY_EASY));
		leaderboardList.add(createLeaderboardListEntry(gridSize, true,
				PuzzleComplexity.EASY));
		leaderboardList.add(createLeaderboardListEntry(gridSize, true,
				PuzzleComplexity.NORMAL));
		leaderboardList.add(createLeaderboardListEntry(gridSize, true,
				PuzzleComplexity.DIFFICULT));
		leaderboardList.add(createLeaderboardListEntry(gridSize, true,
				PuzzleComplexity.VERY_DIFFICULT));

		// Mapping from attributes names in list to resource id's in the
		// layouts.
		String[] from = { "leaderboard_icon", "leaderboard_title",
				"leaderboard_res_id" };
		int[] to = { R.id.leaderboard_icon, R.id.leaderboard_title,
				R.id.leaderboard_res_id };

		// Initialize and set the list adapter.
		SimpleAdapter adapter = new SimpleAdapter(getActivity()
				.getBaseContext(), leaderboardList,
				R.layout.leaderboard_listview_item, from, to);
		setListAdapter(adapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * Creates a new entry for the list of leaderboards.
	 * 
	 * @param gridSize
	 *            The size of the grid.
	 * @param hideOperators
	 *            True in case operators are hidden. False otherwise
	 * @param puzzleComplexity
	 *            The complexity level of the puzzle.
	 * @param resId
	 *            The resource id of the icon of the leaderboard.
	 * @return A new entry for the list of leaderboards.
	 */
	private HashMap<String, String> createLeaderboardListEntry(int gridSize,
			boolean hideOperators, PuzzleComplexity puzzleComplexity) {
		// Get the leaderboard index value
		int index = LeaderboardType.getIndex(gridSize, hideOperators,
				puzzleComplexity);

		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("leaderboard_icon",
				Integer.toString(LeaderboardType.getIconResId(index)));
		hashMap.put("leaderboard_title", "Grid " + gridSize
				+ "\nOperators hidden: " + hideOperators + "\nComplexity "
				+ puzzleComplexity.toString());
		hashMap.put("leaderboard_res_id",
				Integer.toString(LeaderboardType.getResId(index)));

		return hashMap;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		if (view instanceof RelativeLayout) {
			RelativeLayout relativeLayout = (RelativeLayout) view;

			// Get the resource id of the leaderboard from the clicked view
			TextView textView = (TextView) relativeLayout
					.findViewById(R.id.leaderboard_res_id);
			if (textView != null) {
				// Convert the resource id to a Google Play Services leaderboard
				// id.
				int resId = Integer.valueOf(textView.getText().toString());
				String leaderboardId = getResources().getString(resId);

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
	}
}