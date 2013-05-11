package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.GridRow;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;
import net.cactii.mathdoku.util.Util;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * A fragment representing the statistics for a specific game.
 */
public class StatisticsGameFragment extends StatisticsBaseFragment {

	public final static String BUNDLE_KEY_STATISTICS_ID = "GridStatisticsId";

	private int mGridStatisticsId;
	private GridStatistics mGridStatistics;

	// Grid size for currently selected grid
	private int mGridSize;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container,
				savedInstanceState);

		// Get the game id from bundle
		Bundle bundle = getArguments();
		mGridStatisticsId = (bundle == null ? -1 : bundle.getInt(
				BUNDLE_KEY_STATISTICS_ID, -1));

		// Retrieve statistics from database
		boolean statisticsDisplayed = false;
		if (mGridStatisticsId >= 0) {
			mStatisticsDatabaseAdapter = new StatisticsDatabaseAdapter();
			mGridStatistics = mStatisticsDatabaseAdapter.get(mGridStatisticsId);
			if (mGridStatistics != null) {
				GridRow gridRow = new GridDatabaseAdapter()
						.get(mGridStatistics.mGridId);
				if (gridRow != null) {
					mGridSize = gridRow.mGridSize;
				}
			}

			// Get layout where charts will be drawn and the inflater for
			// creating new statistics sections.
			mChartsLayout = (LinearLayout) rootView
					.findViewById(R.id.chartLayouts);
			mChartsLayout.removeAllViewsInLayout();

			// Build all charts for current game only
			statisticsDisplayed = createProgressChart();
			statisticsDisplayed = createAvoidableMovesChart()
					|| statisticsDisplayed;
			statisticsDisplayed = createUsedCheatsChart()
					|| statisticsDisplayed;
		}

		// Check if at least one statistic is displayed.
		if (!statisticsDisplayed) {
			Toast.makeText(
					getActivity().getBaseContext(),
					getResources().getString(R.string.statistics_not_available),
					Toast.LENGTH_SHORT).show();
		}

		return rootView;
	}

	/**
	 * Create a pie chart for the progress of solving.
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createProgressChart() {
		if (mGridStatistics.mSolvedManually) {
			// No progress to report.
			return false;
		}

		// Determine total number of cells in grid
		float totalCells = mGridSize * mGridSize;

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

		addStatisticsSection(R.string.progress_chart_title, null,
				R.string.progress_chart_body, ChartFactory.getPieChartView(
						getActivity(), categorySeries, renderer), null);
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
		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setYTitle("Number of times used"); // TODO:
																	// hard
																	// coded
																	// string
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
		// Y-value
		xyMultipleSeriesRenderer.setXAxisMax(countCategories + 2);
		xyMultipleSeriesRenderer.setXLabels(countCategories);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setYAxisMax(maxYValue + 1);
		xyMultipleSeriesRenderer
				.setBarWidth(getElementWidth(countCategories) / 2);

		// Add new statistics section to the activity
		addStatisticsSection(R.string.avoidable_moves_chart_title, null,
				R.string.avoidable_moves_chart_body,
				ChartFactory.getBarChartView(getActivity(),
						xyMultipleSeriesDataset, xyMultipleSeriesRenderer,
						Type.DEFAULT), null);

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
		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setYTitle("Number of times used");
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
		int categoryIndex = 1;

		// Check progress option used
		if (mGridStatistics.mCheckProgressUsed > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_check_progress));
			xySeries.add(categoryIndex, mGridStatistics.mCheckProgressUsed);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed1));
			categoryIndex++;
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
		}

		addStatisticsSection(R.string.statistics_cheats_used_title, null,
				R.string.statistics_cheats_used_body,
				ChartFactory.getBarChartView(getActivity(),
						xyMultipleSeriesDataset, xyMultipleSeriesRenderer,
						Type.DEFAULT), null);

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