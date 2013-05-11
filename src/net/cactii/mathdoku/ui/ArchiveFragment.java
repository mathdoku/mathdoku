package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.DigitPositionGrid;
import net.cactii.mathdoku.DigitPositionGrid.DigitPositionGridType;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelpers.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelpers.DevelopmentHelper.Mode;
import net.cactii.mathdoku.ui.GridView.InputModeDeterminer;
import net.cactii.mathdoku.ui.PuzzleFragmentActivity.InputMode;
import net.cactii.mathdoku.util.Util;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * An archive fragment representing a puzzle which is archived.
 */
public class ArchiveFragment extends android.support.v4.app.Fragment {

	public static final String ARG_OBJECT = "object";

	private static DigitPositionGrid mDigitPositionGrid = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.archive_fragment, container,
				false);
		Bundle args = getArguments();
		int gameId = args.getInt(ARG_OBJECT);

		GridView mGridView;
		(mGridView = (GridView) rootView.findViewById(R.id.gridView)).mInputModeDeterminer = new InputModeDeterminer() {
			@Override
			public final InputMode getInputMode() {
				return PuzzleFragmentActivity.InputMode.NO_INPUT__DISPLAY_GRID;
			}
		};
		TextView mGameSeedLabel = (TextView) rootView
				.findViewById(R.id.gameSeedLabel);
		TextView mGameSeedText = (TextView) rootView
				.findViewById(R.id.gameSeedText);
		TextView mTimerText = (TextView) rootView.findViewById(R.id.timerText);

		// Load grid from database
		Grid grid = new Grid();
		if (grid.load(gameId)) {
			// Load grid into grid view
			mGridView.loadNewGrid(grid);

			// In case the grid isn't finished, the digit position grid type has
			// to be determined for positioning maybe values inside the cells.
			if (grid.isActive()) {
				// Determine the digit position grid type to be used based on
				// screen dimensions.
				DigitPositionGridType digitPositionGridType = DigitPositionGridType.GRID_3X3;
				if (getResources().getString(R.string.dimension).equals(
						"small-port")) {
					digitPositionGridType = DigitPositionGridType.GRID_2X5;
				}

				// Only create the digit position grid if needed
				if (mDigitPositionGrid == null
						|| !mDigitPositionGrid.isReusable(
								digitPositionGridType, grid.getGridSize())) {
					mDigitPositionGrid = new DigitPositionGrid(
							digitPositionGridType, grid.getGridSize());

					// Propagate setting to the grid view for displaying maybe
					// values (dependent on preferences).
					mGridView.setDigitPositionGrid(mDigitPositionGrid);
				}

				// Disable the grid as the user should not be able to click
				// cells in the archive view
				grid.setActive(false);
			} else {
				// Show elapsed time for puzzles which are solved manually.
				if (grid.isSolvedByCheating() == false) {
					mTimerText.setText(Util.durationTimeToString(grid
							.getElapsedTime()));
					mTimerText.setVisibility(View.VISIBLE);
				}
			}

			// Debug information
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				mGameSeedLabel.setVisibility(View.VISIBLE);
				mGameSeedText.setVisibility(View.VISIBLE);
				mGameSeedText.setText(String.format("%,d", grid.getGameSeed()));
			}

		}

		return rootView;
	}
}