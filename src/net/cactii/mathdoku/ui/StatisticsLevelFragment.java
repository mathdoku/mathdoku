package net.cactii.mathdoku.ui;

import java.util.ArrayList;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.statistics.CumulativeStatistics;
import net.cactii.mathdoku.statistics.HistoricStatistics;
import net.cactii.mathdoku.statistics.HistoricStatistics.Scale;
import net.cactii.mathdoku.statistics.HistoricStatistics.Serie;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;
import net.cactii.mathdoku.util.Util;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.LineChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

/**
 * A fragment representing the statistics for a specific grid size or the
 * cumulative statistics for all levels.
 */
public class StatisticsLevelFragment extends StatisticsBaseFragment {

	public static final String ARG_GRID_SIZE_MIN = "grid_size_min";
	public static final String ARG_GRID_SIZE_MAX = "grid_size_max";

	// Grid size for currently selected grid
	private int mMinGridSize;
	private int mMaxGridSize;

	private CumulativeStatistics mCumulativeStatistics;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container,
				savedInstanceState);

		// Get minimum and maximum grid size from bundle
		Bundle bundle = getArguments();
		mMinGridSize = bundle.getInt(ARG_GRID_SIZE_MIN);
		mMaxGridSize = bundle.getInt(ARG_GRID_SIZE_MAX);

		// Retrieve statistics from database
		mStatisticsDatabaseAdapter = new StatisticsDatabaseAdapter();
		mCumulativeStatistics = mStatisticsDatabaseAdapter
				.getCumulativeStatistics(mMinGridSize, mMaxGridSize);

		// Get layout where charts will be drawn and the inflater for
		// creating new statistics sections.
		mChartsLayout = (LinearLayout) rootView.findViewById(R.id.chartLayouts);
		mChartsLayout.removeAllViewsInLayout();

		// Build all charts for all games at current level
		boolean statisticsDisplayed = createSolvedUnSolvedChart(mCumulativeStatistics);

		statisticsDisplayed = createElapsedTimeHistoryChart()
				|| statisticsDisplayed;

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
	 * Create a pie chart for the number of solved versus unsolved games.
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createSolvedUnSolvedChart(
			CumulativeStatistics cumulativeStatistics) {
		if (cumulativeStatistics == null) {
			// No progress to report.
			return false;
		}

		// Display chart only if at least 1 game have been started for this grid
		// size
		if (cumulativeStatistics.mCountStarted <= 0) {
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

		// Games solved manually
		if (cumulativeStatistics.mCountSolvedManually > 0) {
			categorySeries.add(
					getResources().getString(R.string.chart_serie_solved)
							+ " (" + cumulativeStatistics.mCountSolvedManually
							+ ")",
					(double) cumulativeStatistics.mCountSolvedManually
							/ cumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartGreen1));
		}

		// Games for which the solution is revealed
		if (cumulativeStatistics.mCountSolutionRevealed > 0) {
			categorySeries
					.add(getResources().getString(
							R.string.chart_serie_solution_revealed)
							+ " ("
							+ cumulativeStatistics.mCountSolutionRevealed + ")",
							(double) cumulativeStatistics.mCountSolutionRevealed
									/ cumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartRed1));
		}

		// Games which have not yet been finished
		int countUnfinished = cumulativeStatistics.mCountStarted
				- cumulativeStatistics.mCountFinished;
		if (countUnfinished > 0) {
			categorySeries.add(
					getResources().getString(R.string.chart_serie_unfinished)
							+ " (" + countUnfinished + ")",
					(double) countUnfinished
							/ cumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartGrey1));
		}

		// Determine title
		String subTitle;
		if (cumulativeStatistics.mMinGridSize == cumulativeStatistics.mMaxGridSize) {
			subTitle = this.getResources().getString(
					R.string.statistics_chart_one_grid_size_subtitle,
					cumulativeStatistics.mMinGridSize);
		} else {
			subTitle = this.getResources().getString(
					R.string.statistics_chart_all_grid_sizes_subtitle,
					cumulativeStatistics.mMinGridSize);
		}

		// Add section to activity
		addStatisticsSection(
				getResources().getString(R.string.solved_chart_title),
				subTitle, ChartFactory.getPieChartView(getActivity(),
						categorySeries, renderer), null, getResources()
						.getString(R.string.solved_chart_body));
		return true;
	}

	/**
	 * Create a combined bar and line chart which displays the elapsed time per
	 * game and the historic average.
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createElapsedTimeHistoryChart() {
		// Retrieve the data
		HistoricStatistics historicStatistics = mStatisticsDatabaseAdapter
				.getHistoricData(StatisticsDatabaseAdapter.KEY_ELAPSED_TIME,
						mMinGridSize, mMaxGridSize);

		// Check if at least one serie will contain data.
		if (!historicStatistics.isXYSeriesUsed(null)) {
			return false;
		}

		// Define the renderer
		XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
		
		// Fix background color problem of margin in AChartEngine
		xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 50, 50, 50));
		
		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setXAxisMin(0);
		xyMultipleSeriesRenderer.setXAxisMax(historicStatistics.getMaxX() + 1);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setMargins(new int[] { 0, 80, 50, 40 });
		xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
		xyMultipleSeriesRenderer.setZoomEnabled(false);
		xyMultipleSeriesRenderer.setPanEnabled(false);
		xyMultipleSeriesRenderer.setInScroll(true);

		// Y-axis
		Scale yScale = Scale.DAYS;
		double maxY = historicStatistics.getMaxY(yScale) * 1.1;
		if (maxY < 1) {
			yScale = Scale.HOURS;
			maxY = historicStatistics.getMaxY(yScale) * 1.1;
			if (maxY < 1) {
				yScale = Scale.MINUTES;
				maxY = historicStatistics.getMaxY(yScale) * 1.1;
				if (maxY < 1) {
					yScale = Scale.SECONDS;
					maxY = historicStatistics.getMaxY(yScale) * 1.1;
				}
			}
		}
		xyMultipleSeriesRenderer.setYAxisMax(maxY);
		switch (yScale) {
		case DAYS:
			xyMultipleSeriesRenderer.setYTitle(getResources().getString(
					R.string.statistics_elapsed_time_historic_title)
					+ " ("
					+ getResources().getString(R.string.time_unit_days_plural)
					+ ")");
			break;
		case HOURS:
			xyMultipleSeriesRenderer.setYTitle(getResources().getString(
					R.string.statistics_elapsed_time_historic_title)
					+ " ("
					+ getResources().getString(R.string.time_unit_hours_plural)
					+ ")");
			break;
		case MINUTES:
			xyMultipleSeriesRenderer.setYTitle(getResources().getString(
					R.string.statistics_elapsed_time_historic_title)
					+ " ("
					+ getResources().getString(
							R.string.time_unit_minutes_plural) + ")");
			break;
		case SECONDS:
			xyMultipleSeriesRenderer.setYTitle(getResources().getString(
					R.string.statistics_elapsed_time_historic_title)
					+ " ("
					+ getResources().getString(
							R.string.time_unit_seconds_plural) + ")");
			break;
		case NO_SCALE:
			break;
		}
		xyMultipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
		xyMultipleSeriesRenderer.setYLabelsPadding(5f);


		// Create object for category series and the series renderer
		XYMultipleSeriesDataset xyMultipleSeriesDataset = new XYMultipleSeriesDataset();

		ArrayList<String> typesList = new ArrayList<String>();

		// Add series for solved games
		if (historicStatistics.isXYSeriesUsed(Serie.SOLVED)) {
			typesList.add(BarChart.TYPE);
			xyMultipleSeriesDataset.addSeries(historicStatistics.getXYSeries(
					Serie.SOLVED,
					getResources().getString(R.string.chart_serie_solved),
					yScale));
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartGreen1));
		}

		// Add series for games in which the solution was revealed
		if (historicStatistics.isXYSeriesUsed(Serie.SOLUTION_REVEALED)) {
			typesList.add(BarChart.TYPE);
			xyMultipleSeriesDataset.addSeries(historicStatistics.getXYSeries(
					Serie.SOLUTION_REVEALED,
					getResources().getString(
							R.string.chart_serie_solution_revealed), yScale));
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed1));
		}

		// Add series for unfinished games
		if (historicStatistics.isXYSeriesUsed(Serie.UNFINISHED)) {
			typesList.add(BarChart.TYPE);
			xyMultipleSeriesDataset.addSeries(historicStatistics.getXYSeries(
					Serie.UNFINISHED,
					getResources().getString(R.string.chart_serie_unfinished),
					yScale));
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartGrey1));
		}

		// Add series for historic average of solved games
		if (historicStatistics.isXYSeriesUsed(Serie.SOLVED)) {
			typesList.add(LineChart.TYPE);
			xyMultipleSeriesDataset
					.addSeries(historicStatistics
							.getXYSeriesHistoricAverage(
									Serie.SOLVED,
									getResources()
											.getString(
													R.string.statistics_elapsed_time_historic_solved_average_serie),
									yScale));
			XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
			xySeriesRenderer.setColor(chartSignal2);
			xySeriesRenderer.setLineWidth(4);
			xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesRenderer);
		}

		// Create a table with extra data for fastest, average and slowest time.
		TableLayout tableLayout = null;
		if (historicStatistics.isXYSeriesUsed(Serie.SOLVED)) {
			tableLayout = new TableLayout(getActivity());
			TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
					TableLayout.LayoutParams.WRAP_CONTENT,
					TableLayout.LayoutParams.WRAP_CONTENT);
			tableLayout.setLayoutParams(tableLayoutParams);

			tableLayout
					.addView(createDataTableRow(
							tableLayoutParams,
							getResources().getString(
									R.string.chart_serie_solved), null));
			tableLayout
					.addView(createDataTableRow(
							tableLayoutParams,
							getResources()
									.getString(
											R.string.statistics_elapsed_time_historic_solved_slowest),
							Util.durationTimeToString(historicStatistics
									.getSolvedSlowest())));
			tableLayout
					.addView(createDataTableRow(
							tableLayoutParams,
							getResources()
									.getString(
											R.string.statistics_elapsed_time_historic_solved_average),
							Util.durationTimeToString(historicStatistics
									.getSolvedAverage())));
			tableLayout
					.addView(createDataTableRow(
							tableLayoutParams,
							getResources()
									.getString(
											R.string.statistics_elapsed_time_historic_solved_fastest),
							Util.durationTimeToString(historicStatistics
									.getSolvedFastest())));
		}

		// Display as stacked bar chart here. As the series are mutually
		// exclusive this will result in one single bar per game which is
		// entirely colored based on status of game.
		String[] types = typesList.toArray(new String[typesList.size()]);
		addStatisticsSection(
				getResources().getString(
						R.string.statistics_elapsed_time_historic_title),
				null,
				ChartFactory.getCombinedXYChartView(getActivity(),
						xyMultipleSeriesDataset, xyMultipleSeriesRenderer,
						types),
				tableLayout,
				getResources().getString(
						R.string.statistics_elapsed_time_historic_body));

		return true;
	}
}