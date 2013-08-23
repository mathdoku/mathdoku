package net.cactii.mathdoku.ui;

import java.util.ArrayList;

import net.cactii.mathdoku.Preferences;
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
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * A fragment representing the statistics for a specific grid size or the
 * cumulative statistics for all levels.
 */
public class StatisticsLevelFragment extends StatisticsBaseFragment implements
		OnSharedPreferenceChangeListener {

	public static final String ARG_GRID_SIZE_MIN = "grid_size_min";
	public static final String ARG_GRID_SIZE_MAX = "grid_size_max";

	// Grid size for currently selected grid
	private int mMinGridSize;
	private int mMaxGridSize;

	private CumulativeStatistics mCumulativeStatistics;

	private Preferences mPreferences;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container,
				savedInstanceState);

		// Get minimum and maximum grid size from bundle
		Bundle bundle = getArguments();
		mMinGridSize = bundle.getInt(ARG_GRID_SIZE_MIN);
		mMaxGridSize = bundle.getInt(ARG_GRID_SIZE_MAX);

		// Get preferences
		mPreferences = Preferences.getInstance();
		setDisplayChartDescription(mPreferences
				.isStatisticsChartDescriptionVisible());
		mPreferences.mSharedPreferences
				.registerOnSharedPreferenceChangeListener(this);

		// Retrieve statistics from database
		mStatisticsDatabaseAdapter = new StatisticsDatabaseAdapter();
		mCumulativeStatistics = mStatisticsDatabaseAdapter
				.getCumulativeStatistics(mMinGridSize, mMaxGridSize);

		// Get layout where charts will be drawn and the inflater for
		// creating new statistics sections.
		mChartsLayout = (LinearLayout) rootView.findViewById(R.id.chartLayouts);
		createAllCharts();

		return rootView;
	}

	@Override
	public void onDestroy() {
		mPreferences.mSharedPreferences
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Preferences.STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE)) {
			setDisplayChartDescription(Preferences.getInstance(getActivity())
					.isStatisticsChartDescriptionVisible());
		}

		createAllCharts();
	}

	/**
	 * Creates all charts.
	 */
	private void createAllCharts() {
		mChartsLayout.removeAllViewsInLayout();

		// Build all charts for all games at current level
		boolean statisticsDisplayed = createSolvedUnSolvedChart();

		statisticsDisplayed = createElapsedTimeHistoryChart()
				|| statisticsDisplayed;

		// Check if at least one statistic is displayed.
		if (!statisticsDisplayed) {
			TextView textView = new TextView(getActivity());
			textView.setLayoutParams(new LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
			textView.setText(getResources().getString(
					R.string.statistics_not_available, mMinGridSize));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
					mDefaultTextSizeInDIP);

			mChartsLayout.addView(textView);
		}

	}

	/**
	 * Create a pie chart for the number of solved versus unsolved games.
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createSolvedUnSolvedChart() {
		if (mCumulativeStatistics == null) {
			// No progress to report.
			return false;
		}

		// Display chart only if at least 1 game have been started for this grid
		// size
		if (mCumulativeStatistics.mCountStarted <= 0) {
			return false;
		}

		// Define the renderer
		DefaultRenderer renderer = new DefaultRenderer();
		renderer.setShowLabels(false);
		renderer.setShowLegend(true);
		renderer.setFitLegend(true);
		renderer.setMargins(new int[] { 0, mDefaultTextSize, mDefaultTextSize,
				mDefaultTextSize });

		renderer.setLegendTextSize(mDefaultTextSize);
		renderer.setZoomButtonsVisible(false);
		renderer.setZoomEnabled(false);
		renderer.setPanEnabled(false);
		renderer.setInScroll(true);

		// Create object for category series and the series renderer
		CategorySeries categorySeries = new CategorySeries("");

		// Games solved manually
		if (mCumulativeStatistics.mCountSolvedManually > 0) {
			categorySeries.add(
					getResources().getString(R.string.chart_serie_solved)
							+ " (" + mCumulativeStatistics.mCountSolvedManually
							+ ")",
					(double) mCumulativeStatistics.mCountSolvedManually
							/ mCumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartGreen1));
		}

		// Games for which the solution is revealed
		if (mCumulativeStatistics.mCountSolutionRevealed > 0) {
			categorySeries.add(
					getResources().getString(
							R.string.chart_serie_solution_revealed)
							+ " ("
							+ mCumulativeStatistics.mCountSolutionRevealed
							+ ")",
					(double) mCumulativeStatistics.mCountSolutionRevealed
							/ mCumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartRed1));
		}

		// Games which have not yet been finished
		int countUnfinished = mCumulativeStatistics.mCountStarted
				- mCumulativeStatistics.mCountFinished;
		if (countUnfinished > 0) {
			categorySeries.add(
					getResources().getString(R.string.chart_serie_unfinished)
							+ " (" + countUnfinished + ")",
					(double) countUnfinished
							/ mCumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartGrey1));
		}

		// Add section to activity
		addStatisticsSection(null,
				getResources().getString(R.string.solved_chart_title),
				ChartFactory.getPieChartView(getActivity(), categorySeries,
						renderer), null,
				getResources().getString(R.string.solved_chart_body));
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
				.getHistoricData(mMinGridSize, mMaxGridSize);

		// The number of entries to be displayed is restricted to the maximum
		// set in the preferences.
		historicStatistics.setLimit(Preferences.getInstance()
				.getStatisticsSettingElapsedTimeChartMaximumGames());

		// Check if at least one serie will contain data in the limited range.
		if (!historicStatistics.isXYSeriesUsed(null, true, true)) {
			return false;
		}

		// Define the renderer
		XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();

		// Fix background color problem of margin in AChartEngine
		xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 50, 50, 50));

		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setXAxisMin(historicStatistics
				.getIndexFirstEntry() - 1);
		xyMultipleSeriesRenderer.setXAxisMax(historicStatistics
				.getCountIndexEntries() + 1);
		xyMultipleSeriesRenderer.setXLabels((int) Math.min(
				historicStatistics.getCountIndexEntries() + 1, 4));
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setMargins(new int[] { 0,
				2 * mDefaultTextSize, 2 * mDefaultTextSize, mDefaultTextSize });
		xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
		xyMultipleSeriesRenderer.setZoomEnabled(false);
		xyMultipleSeriesRenderer.setPanEnabled(false);
		xyMultipleSeriesRenderer.setInScroll(true);
		xyMultipleSeriesRenderer.setFitLegend(true);

		// Use 20% of bar width as space between bars
		xyMultipleSeriesRenderer.setBarSpacing(0.2);

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

		// Add series for elapsed time (including cheat time) of solved games
		if (historicStatistics.isXYSeriesUsed(Serie.SOLVED, true, true)) {
			typesList.add(BarChart.TYPE);
			xyMultipleSeriesDataset
					.addSeries(historicStatistics
							.getXYSeries(
									Serie.SOLVED,
									getResources()
											.getString(
													R.string.statistics_elapsed_time_historic_elapsed_time_solved),
									yScale, true, true));
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartGreen1));
		}

		// Add series for cheat time of solved games
		boolean cheatLegendDisplayed = false;
		if (historicStatistics.isXYSeriesUsed(Serie.SOLVED, false, true)) {
			typesList.add(BarChart.TYPE);
			xyMultipleSeriesDataset
					.addSeries(historicStatistics
							.getXYSeries(
									Serie.SOLVED,
									getResources()
											.getString(
													R.string.statistics_elapsed_time_historic_cheat_time),
									yScale, false, true));

			SimpleSeriesRenderer simpleSeriesRenderer = createSimpleSeriesRenderer(chartRed1);

			// Cheat legend should only be displayed once
			if (cheatLegendDisplayed) {
				simpleSeriesRenderer.setShowLegendItem(false);
			}
			cheatLegendDisplayed = true;

			xyMultipleSeriesRenderer.addSeriesRenderer(simpleSeriesRenderer);
		}

		// Add series for elapsed time (including cheat time) of unfinished
		// games
		if (historicStatistics.isXYSeriesUsed(Serie.UNFINISHED, true, true)) {
			// Elapsed time so far including cheats
			typesList.add(BarChart.TYPE);
			xyMultipleSeriesDataset
					.addSeries(historicStatistics
							.getXYSeries(
									Serie.UNFINISHED,
									getResources()
											.getString(
													R.string.statistics_elapsed_time_historic_elapsed_time_unfinished),
									yScale, true, true));
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartGrey1));
		}

		// Add series for cheat time of solved games
		if (historicStatistics.isXYSeriesUsed(Serie.UNFINISHED, false, true)) {
			typesList.add(BarChart.TYPE);
			xyMultipleSeriesDataset
					.addSeries(historicStatistics
							.getXYSeries(
									Serie.UNFINISHED,
									getResources()
											.getString(
													R.string.statistics_elapsed_time_historic_cheat_time),
									yScale, false, true));
			SimpleSeriesRenderer simpleSeriesRenderer = createSimpleSeriesRenderer(chartRed1);

			// Cheat legend should only be displayed once
			if (cheatLegendDisplayed) {
				simpleSeriesRenderer.setShowLegendItem(false);
			}
			cheatLegendDisplayed = true;

			xyMultipleSeriesRenderer.addSeriesRenderer(simpleSeriesRenderer);
		}

		// Add series for games in which the solution was revealed
		if (historicStatistics.isXYSeriesUsed(Serie.SOLUTION_REVEALED, true,
				true)) {
			typesList.add(BarChart.TYPE);

			xyMultipleSeriesDataset
					.addSeries(historicStatistics
							.getXYSeriesSolutionRevealed(
									getResources()
											.getString(
													R.string.statistics_elapsed_time_historic_cheat_time),
									maxY, true, true));

			SimpleSeriesRenderer simpleSeriesRenderer = createSimpleSeriesRenderer(chartRed1);

			// Cheat legend should only be displayed once
			if (cheatLegendDisplayed) {
				simpleSeriesRenderer.setShowLegendItem(false);
			}
			cheatLegendDisplayed = true;

			xyMultipleSeriesRenderer.addSeriesRenderer(simpleSeriesRenderer);
		}

		// Add series for the historic average of solved games. As this series
		// is displayed as a line chart, it can only be shown if at least two
		// data points in the series are available.
		if (historicStatistics.isXYSeriesUsed(Serie.SOLVED, true, true)) {
			XYSeries xySeries = historicStatistics
					.getXYSeriesHistoricAverage(
							Serie.SOLVED,
							getResources()
									.getString(
											R.string.statistics_elapsed_time_historic_solved_average_serie),
							yScale);
			if (xySeries.getItemCount() > 1) {
				typesList.add(LineChart.TYPE);
				xyMultipleSeriesDataset.addSeries(xySeries);
				XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
				xySeriesRenderer.setColor(chartSignal2);
				xySeriesRenderer.setLineWidth(4);
				xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesRenderer);
			}
		}

		// Create a table with extra data for fastest, average and slowest time.
		TableLayout tableLayout = null;
		if (historicStatistics.isXYSeriesUsed(Serie.SOLVED, true, true)) {
			tableLayout = new TableLayout(getActivity());
			TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tableLayout.setLayoutParams(tableLayoutParams);

			tableLayout
					.addView(createDataTableRow(
							tableLayoutParams,
							getResources().getString(
									R.string.chart_serie_solved)
									+ String.format(
											" (%d)",
											mCumulativeStatistics.mCountSolvedManually),
							null));
			tableLayout
					.addView(createDataTableRow(
							tableLayoutParams,
							getResources()
									.getString(
											R.string.statistics_elapsed_time_historic_solved_fastest),
							Util.durationTimeToString(historicStatistics
									.getSolvedFastest())));
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
											R.string.statistics_elapsed_time_historic_solved_slowest),
							Util.durationTimeToString(historicStatistics
									.getSolvedSlowest())));
		}

		// Display as stacked bar chart here. As the series are mutually
		// exclusive this will result in one single bar per game which is
		// entirely colored based on status of game.
		String[] types = typesList.toArray(new String[typesList.size()]);
		addStatisticsSection(
				null,
				getResources().getString(
						R.string.statistics_elapsed_time_historic_title),
				ChartFactory.getCombinedXYChartView(getActivity(),
						xyMultipleSeriesDataset, xyMultipleSeriesRenderer,
						types),
				tableLayout,
				getResources().getString(
						R.string.statistics_elapsed_time_historic_body));

		return true;
	}
}