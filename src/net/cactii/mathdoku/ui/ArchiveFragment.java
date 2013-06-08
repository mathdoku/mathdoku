package net.cactii.mathdoku.ui;

import java.text.DateFormat;

import net.cactii.mathdoku.DigitPositionGrid;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.util.Util;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * An archive fragment representing a puzzle which is archived.
 */
public class ArchiveFragment extends StatisticsBaseFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "ArchiveFragment";

	public static final String BUNDLE_KEY_SOLVING_ATTEMPT_ID = "solvingAttemptId";

	private static DigitPositionGrid mDigitPositionGrid = null;

	private GridStatistics mGridStatistics;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, R.layout.archive_fragment,
				container, savedInstanceState);

		Bundle args = getArguments();
		int solvingAttemptId = args.getInt(BUNDLE_KEY_SOLVING_ATTEMPT_ID);

		// Get fragment manager and start a transaction.
		GridView mGridView;
		mGridView = (GridView) rootView.findViewById(R.id.gridView);

		// Load grid from database
		Grid grid = new Grid();
		if (grid.load(solvingAttemptId)) {
			// Load grid into grid view
			mGridView.loadNewGrid(grid);

			// In case the grid isn't finished, the digit position grid type
			// has to be determined for positioning maybe values inside the
			// cells.
			if (grid.isActive()) {
				// Only create the digit position grid if needed
				if (mDigitPositionGrid == null
						|| !mDigitPositionGrid.isReusable(grid.getGridSize())) {
					mDigitPositionGrid = new DigitPositionGrid(
							grid.getGridSize());
				}

				// Propagate setting to the grid view for displaying maybe
				// values (dependent on preferences).
				mGridView.setDigitPositionGrid(mDigitPositionGrid);

				// Disable the grid as the user should not be able to click
				// cells in the archive view
				grid.setActive(false);
			}

			// Load grid statistics
			mGridStatistics = grid.getGridStatistics();

			// Set date created
			if (grid.getDateCreated() > 0) {
				((TableRow) rootView
						.findViewById(R.id.statistics_general_date_created_row))
						.setVisibility(View.VISIBLE);
				((TextView) rootView
						.findViewById(R.id.statistics_general_date_created))
						.setText(DateFormat.getDateTimeInstance().format(
								grid.getDateCreated()));
			}

			// Set date finished
			if (mGridStatistics != null && mGridStatistics.isFinished()) {
				((TableRow) rootView
						.findViewById(R.id.statistics_general_date_finished_row))
						.setVisibility(View.VISIBLE);
				((TextView) rootView
						.findViewById(R.id.statistics_general_date_finished))
						.setText(DateFormat.getDateTimeInstance().format(
								mGridStatistics.mLastMove));
			}

			// Show elapsed time for puzzles which are solved manually.
			if (grid.isActive() == false && grid.isSolvedByCheating() == false) {
				((TableRow) rootView
						.findViewById(R.id.statistics_general_elapsed_time_row))
						.setVisibility(View.VISIBLE);
				((TextView) rootView.findViewById(R.id.timerText)).setText(Util
						.durationTimeToString(grid.getElapsedTime()));
			}

			// Set cheat penalty time
			if (mGridStatistics != null
					&& mGridStatistics.getCheatPenaltyTime() > 0) {
				((TableRow) rootView
						.findViewById(R.id.statistics_general_cheat_penalty_time_row))
						.setVisibility(View.VISIBLE);
				((TextView) rootView
						.findViewById(R.id.statistics_general_cheat_penalty_time))
						.setText(Util.durationTimeToString(mGridStatistics
								.getCheatPenaltyTime()));
			}

			// Get layout where charts will be drawn and the inflater for
			// creating new statistics sections.
			mChartsLayout = (LinearLayout) rootView
					.findViewById(R.id.chartLayouts);
			mChartsLayout.removeAllViewsInLayout();

			// Build all charts for current game only
			createProgressChart(grid.getGridSize());
			createAvoidableMovesChart();
			createUsedCheatsChart();
		}

		return rootView;
	}

	/**
	 * Create a pie chart for the progress of solving.
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createProgressChart(int gridSize) {
		if (gridSize == 0 || mGridStatistics == null
				|| mGridStatistics.mSolvedManually) {
			// No progress to report.
			return false;
		}

		// Determine total number of cells in grid
		float totalCells = gridSize * gridSize;

		// Display chart only if grid not completely filled and not completely
		// empty.
		if (mGridStatistics.mCellsUserValueFilled == 0
				|| mGridStatistics.mCellsUserValueFilled == totalCells) {
			return false;
		}

		// Define the renderer
		DefaultRenderer renderer = new DefaultRenderer();
		renderer.setLabelsTextSize(mDefaultTextSize);
		renderer.setLegendTextSize(mDefaultTextSize);
		renderer.setZoomButtonsVisible(false);
		renderer.setZoomEnabled(false);
		renderer.setPanEnabled(false);
		renderer.setInScroll(true);

		// Create object for category series and the series renderer
		CategorySeries categorySeries = new CategorySeries("");

		// Cells filled
		categorySeries.add(
				getResources().getString(R.string.progress_chart_cells_filled)
						+ " (" + mGridStatistics.mCellsUserValueFilled + ")",
				(double) mGridStatistics.mCellsUserValueFilled / totalCells);
		renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartGreen1));

		// Cells empty
		categorySeries.add(
				getResources().getString(R.string.progress_chart_cells_empty)
						+ " (" + mGridStatistics.mCellsUserValueEmtpty + ")",
				(double) mGridStatistics.mCellsUserValueEmtpty / totalCells);
		renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartGrey1));

		addStatisticsSection(
				getResources().getString(R.string.progress_chart_title),
				ChartFactory.getPieChartView(getActivity(), categorySeries,
						renderer), null,
				getResources().getString(R.string.progress_chart_body));
		return true;
	}

	/**
	 * Create the chart for the avoidable moves.
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createAvoidableMovesChart() {
		// Build chart for analysis of moves only in case at least one move
		// has been made.
		int totalAvoidableMoves = mGridStatistics.mUserValueReplaced
				+ mGridStatistics.mMaybeValue + mGridStatistics.mUndoButton
				+ mGridStatistics.mCellCleared + mGridStatistics.mCageCleared
				+ mGridStatistics.mGridCleared;
		if (totalAvoidableMoves == 0) {
			return false;
		}

		// Define the renderer
		XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();

		// Fix background color problem of margin in AChartEngine
		xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 50, 50, 50));

		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setYTitle(getResources().getString(
				R.string.avoidable_moves_yaxis_description));
		xyMultipleSeriesRenderer.setXAxisMin(-1);
		xyMultipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
		xyMultipleSeriesRenderer.setMargins(new int[] { 0, 50, 40, 10 });
		xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
		xyMultipleSeriesRenderer.setZoomEnabled(false);
		xyMultipleSeriesRenderer.setPanEnabled(false);
		xyMultipleSeriesRenderer.setInScroll(true);
		xyMultipleSeriesRenderer.setYLabelsPadding(5f);

		// Create object for category series and the series renderer
		XYMultipleSeriesDataset xyMultipleSeriesDataset = new XYMultipleSeriesDataset();

		// While filling the categories the number of categories used and the
		// maximum Y-value is determined.
		int countCategories = 0;
		int maxYValue = 0;

		// Bar for number of times a user value in a cell was replace by another
		// value
		if (mGridStatistics.mUserValueReplaced > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_user_value_replaced));
			xySeries.add(++countCategories, mGridStatistics.mUserValueReplaced);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartGreen1));
			maxYValue = Math.max(maxYValue, mGridStatistics.mUserValueReplaced);
		}

		// Bar for number of maybe values that have been used while playing the
		// game. Note this is *not* the actual number of possible values
		// currently visible.
		if (mGridStatistics.mMaybeValue > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_maybe_value_used));
			xySeries.add(++countCategories, mGridStatistics.mMaybeValue);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartSignal1));
			maxYValue = Math.max(maxYValue, mGridStatistics.mMaybeValue);
		}

		// Bar for number of times the undo button was used
		if (mGridStatistics.mUndoButton > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_undo_button_used));
			xySeries.add(++countCategories, mGridStatistics.mUndoButton);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartSignal2));
			maxYValue = Math.max(maxYValue, mGridStatistics.mUndoButton);
		}

		// Bar for number of times a user cleared a value in a cell, the cage or
		// the entire grid.
		int totalClears = mGridStatistics.mCellCleared
				+ mGridStatistics.mCageCleared + mGridStatistics.mGridCleared;
		if (totalClears > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_clear_used));
			xySeries.add(++countCategories, totalClears);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartSignal3));
			maxYValue = Math.max(maxYValue, totalClears);
		}

		// Fill dimensions of axis based on number of categories and maximum
		// Y-value.
		xyMultipleSeriesRenderer.setXAxisMax(countCategories + 2);
		xyMultipleSeriesRenderer.setXLabels(0);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setYAxisMax(maxYValue + 1);
		xyMultipleSeriesRenderer.setYLabels(Math.min(4, maxYValue + 1));
		xyMultipleSeriesRenderer
				.setBarWidth(getElementWidth(countCategories) / 2);

		// Add new statistics section to the activity
		addStatisticsSection(
				getResources().getString(R.string.avoidable_moves_chart_title),
				ChartFactory.getBarChartView(getActivity(),
						xyMultipleSeriesDataset, xyMultipleSeriesRenderer,
						Type.DEFAULT), null,
				getResources().getString(R.string.avoidable_moves_chart_body));

		return true;
	}

	/**
	 * Create the pie chart for the cheats which are used
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createUsedCheatsChart() {
		// Build chart for analysis of moves only in case at least one cheat
		// has been used.
		int totalCheats = mGridStatistics.mCheckProgressUsed
				+ mGridStatistics.mCellsRevealed
				+ mGridStatistics.mOperatorsRevevealed
				+ (mGridStatistics.isSolutionRevealed() ? 1 : 0);
		if (totalCheats == 0) {
			return false;
		}

		// Determine number of cheat categories to show
		int cheatCategories = (mGridStatistics.mCheckProgressUsed > 0 ? 1 : 0)
				+ (mGridStatistics.mCellsRevealed > 0 ? 1 : 0)
				+ (mGridStatistics.mOperatorsRevevealed > 0 ? 1 : 0)
				+ (mGridStatistics.isSolutionRevealed() ? 1 : 0);

		// Determine the highest number of cheats for a single category
		int maxCheats = Math.max(mGridStatistics.mCheckProgressUsed,
				mGridStatistics.mCellsRevealed);
		maxCheats = Math.max(maxCheats, mGridStatistics.mCheckProgressUsed);
		maxCheats = Math.max(maxCheats,
				(mGridStatistics.isSolutionRevealed() ? 1 : 0));

		// Define the renderer
		XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();

		// Fix background color problem of margin in AChartEngine
		xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 50, 50, 50));

		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setYTitle(getResources().getString(
				R.string.statistics_cheats_yaxis_description));
		xyMultipleSeriesRenderer.setXAxisMin(-1);
		xyMultipleSeriesRenderer.setXAxisMax(cheatCategories + 2);
		xyMultipleSeriesRenderer.setXLabels(cheatCategories);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setYAxisMax(maxCheats + 1);
		xyMultipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
		xyMultipleSeriesRenderer.setMargins(new int[] { 0, 50, 40, 10 });
		xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
		xyMultipleSeriesRenderer.setZoomEnabled(false);
		xyMultipleSeriesRenderer.setPanEnabled(false);
		xyMultipleSeriesRenderer.setInScroll(true);
		xyMultipleSeriesRenderer
				.setBarWidth(getElementWidth(cheatCategories) / 2);
		xyMultipleSeriesRenderer.setYLabelsPadding(5f);

		// Create object for category series and the series renderer
		XYMultipleSeriesDataset xyMultipleSeriesDataset = new XYMultipleSeriesDataset();

		// While filling the categories the number of categories used and the
		// maximum Y-value is determined.
		int categoryIndex = 1;
		int maxYValue = 0;

		// Check progress option used
		if (mGridStatistics.mCheckProgressUsed > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_check_progress));
			xySeries.add(categoryIndex, mGridStatistics.mCheckProgressUsed);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed1));
			categoryIndex++;
			maxYValue = Math.max(maxYValue, mGridStatistics.mCheckProgressUsed);
		}

		// Cell revealed option used
		if (mGridStatistics.mCellsRevealed > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_cells_revealed));
			xySeries.add(categoryIndex, mGridStatistics.mCellsRevealed);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed2));
			categoryIndex++;
			maxYValue = Math.max(maxYValue, mGridStatistics.mCellsRevealed);
		}

		// Cage operator revealed option used
		if (mGridStatistics.mOperatorsRevevealed > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_operators_revealed));
			xySeries.add(categoryIndex, mGridStatistics.mOperatorsRevevealed);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed3));
			categoryIndex++;
			maxYValue = Math.max(maxYValue,
					mGridStatistics.mOperatorsRevevealed);
		}

		// Solution revealed option used
		if (mGridStatistics.isSolutionRevealed()) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_solution_revealed));
			xySeries.add(categoryIndex,
					(mGridStatistics.isSolutionRevealed() ? 1 : 0));
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed4));
			categoryIndex++;
			maxYValue = Math.max(maxYValue,
					(mGridStatistics.isSolutionRevealed() ? 1 : 0));
		}

		// Fill dimensions of axis based on number of categories and maximum
		// Y-value.
		xyMultipleSeriesRenderer.setXAxisMax(categoryIndex + 2);
		xyMultipleSeriesRenderer.setXLabels(0);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setYAxisMax(maxYValue + 1);
		xyMultipleSeriesRenderer.setYLabels(Math.min(4, maxYValue + 1));
		xyMultipleSeriesRenderer
				.setBarWidth(getElementWidth(categoryIndex) / 2);

		addStatisticsSection(
				getResources().getString(R.string.statistics_cheats_used_title),
				ChartFactory.getBarChartView(getActivity(),
						xyMultipleSeriesDataset, xyMultipleSeriesRenderer,
						Type.DEFAULT), null,
				getResources().getString(R.string.statistics_cheats_used_body));

		return true;
	}

	private int getElementWidth(int elements) {
		// Get screen width
		DisplayMetrics displayMetrics = new Util(getActivity())
				.getDisplayMetrics();

		// Assume 90% of screen width is actually available to display all
		// elements
		return (int) ((float) 0.90 * displayMetrics.widthPixels / elements);
	}
}