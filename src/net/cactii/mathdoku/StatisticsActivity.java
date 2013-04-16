package net.cactii.mathdoku;

import java.util.ArrayList;

import net.cactii.mathdoku.statistics.CumulativeStatistics;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.statistics.HistoricStatistics;
import net.cactii.mathdoku.statistics.HistoricStatistics.Scale;
import net.cactii.mathdoku.statistics.HistoricStatistics.Serie;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.LineChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StatisticsActivity extends Activity {

	public final static String BUNDLE_KEY_SIGNATURE_ID = "GridSignatureId";

	private LinearLayout mChartsLayout;
	private int mGridSignatureId;

	private GridStatistics mGridStatistics;
	private CumulativeStatistics mCumulativeStatisticsCurrentGridSize;
	private CumulativeStatistics mCumulativeStatisticsAllGridSizes;

	// Database adapter for the statistics data
	StatisticsDatabaseAdapter mStatisticsDatabaseAdapter;

	// Grid size for currently selected grid
	private int mGridsize;

	// Text size for body text
	private int mDefaultTextSize;

	// The inflater for this activity.
	private LayoutInflater mLayoutInflater;

	private boolean mDisplayStatisticDescription;

	// Green colors will be used at things which are positive
	private int chartGreen1 = 0xFF80FF00;
	private int chartGreen2 = 0xFF59B200;

	// Grey colors will be used at things which are neutral
	private int chartGrey1 = 0xFFD4D4D4;
	private int chartSignal1 = 0xFFFF00FF;
	private int chartSignal2 = 0xFF8000FF;
	private int chartSignal3 = 0xFF0000FF;

	// Green colors will be used at things which are negative
	private int chartRed1 = 0xFFFF0000;
	private int chartRed2 = 0xFFFF3300;
	private int chartRed3 = 0xFFB22400;
	private int chartRed4 = 0xFFFECCBF;
	private int chartRed5 = 0xFFFE9980;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.statistics);

		// Determine if statistics for a specific signature have to be shown.
		Intent intent = getIntent();
		mGridSignatureId = -1;
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mGridSignatureId = extras.getInt(BUNDLE_KEY_SIGNATURE_ID, -1);
			}
		}

		// Currently no cumulative statistics are implemented.
		if (mGridSignatureId < 0) {
			finish();
		}

		// Retrieve the statistics.
		DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
		mStatisticsDatabaseAdapter = new StatisticsDatabaseAdapter(
				databaseHelper);
		mGridStatistics = mStatisticsDatabaseAdapter.get(mGridSignatureId);

		if (mGridStatistics != null) {
			mGridsize = mGridStatistics.gridSize;
			mCumulativeStatisticsCurrentGridSize = mStatisticsDatabaseAdapter
					.getByGridSize(mGridsize, mGridsize);
			mCumulativeStatisticsAllGridSizes = mStatisticsDatabaseAdapter
					.getByGridSize(1, 9);
		}

		if (mGridStatistics == null
				&& mCumulativeStatisticsCurrentGridSize == null) {
			Toast.makeText(
					getBaseContext(),
					getResources().getString(R.string.statistics_not_available),
					Toast.LENGTH_SHORT).show();
			finish();
		}

	}

	@Override
	protected void onResume() {
		// Get layout where charts will be drawn and an the inflater for
		// creating new statistics sections.
		mChartsLayout = (LinearLayout) findViewById(R.id.chartLayouts);
		mChartsLayout.removeAllViewsInLayout();
		mLayoutInflater = LayoutInflater.from(this);

		// Get default sizes for text
		mDefaultTextSize = getResources().getDimensionPixelSize(
				R.dimen.text_size_default);

		// Determine if a description of the statistic has to be shown below
		// each statistic
		mDisplayStatisticDescription = Preferences.getInstance(this)
				.showStatisticsDescription();

		// Build all charts for current game only
		boolean statisticsDisplayed = createProgressChart();
		statisticsDisplayed = createAvoidableMovesChart()
				|| statisticsDisplayed;
		statisticsDisplayed = createUsedCheatsChart() || statisticsDisplayed;

		// Build all charts for all games at current level
		statisticsDisplayed = createSolvedUnSolvedChart(mCumulativeStatisticsCurrentGridSize)
				|| statisticsDisplayed;

		statisticsDisplayed = createElapsedTimeHistoryChart()
				|| statisticsDisplayed;

		// Build all charts regardless of level
		statisticsDisplayed = createSolvedUnSolvedChart(mCumulativeStatisticsAllGridSizes)
				|| statisticsDisplayed;

		// Check if at least one statistic is displayed.
		if (!statisticsDisplayed) {
			Toast.makeText(
					getBaseContext(),
					getResources().getString(R.string.statistics_not_available),
					Toast.LENGTH_SHORT).show();
			finish();
		}
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.statisticsmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		int menuId = menuItem.getItemId();
		switch (menuId) {
		case R.id.menu_statistics_options:
			UsageLog.getInstance().logFunction("Menu.ViewStatisticsOptions");
			Intent intent = new Intent(this, OptionsActivity.class);
			intent.putExtra(OptionsActivity.BUNDLE_KEY_OPTIONS_VIEW_XML_RES_ID,
					R.xml.statistics_optionsview);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(menuItem);
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
		float totalCells = mGridStatistics.gridSize * mGridStatistics.gridSize;

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
				R.string.progress_chart_body,
				ChartFactory.getPieChartView(this, categorySeries, renderer));
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
				ChartFactory.getBarChartView(this, xyMultipleSeriesDataset,
						xyMultipleSeriesRenderer, Type.DEFAULT));

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
				ChartFactory.getBarChartView(this, xyMultipleSeriesDataset,
						xyMultipleSeriesRenderer, Type.DEFAULT));

		return true;
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
		addStatisticsSection(R.string.solved_chart_title, subTitle,
				R.string.solved_chart_body,
				ChartFactory.getPieChartView(this, categorySeries, renderer));
		return true;
	}

	/**
	 * Creates a new simple series renderer for the given color.
	 * 
	 * @param color
	 *            The color for the new simple series renderer.
	 * @return
	 */
	private SimpleSeriesRenderer createSimpleSeriesRenderer(int color) {
		SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();
		simpleSeriesRenderer.setColor(color);

		return simpleSeriesRenderer;
	}

	/**
	 * Add a statistics section to the activity.
	 * 
	 * @param titleResId
	 *            Resource id for the title of this section.
	 * @param gridSize
	 *            The grid size to be displayed as subtitle for the chart.
	 *            <b>Only specify for charts which relates to all grids of this
	 *            specific size. Use a value <= 0 if no subtitle should be
	 *            shown.</b>
	 * @param bodyResId
	 *            Resource id for the body text (explanation of this section).
	 * @param chart
	 *            The chart view.
	 */
	private void addStatisticsSection(int titleResId, String subTitle,
			int bodyResId, GraphicalView chart) {
		// Inflate a new view for this statistics section
		View sectionView = mLayoutInflater.inflate(R.layout.statistics_section,
				null);

		// Set title and subtitle. The chart title of achartengine is not used.
		((TextView) sectionView.findViewById(R.id.statistics_section_title))
				.setText(titleResId);
		TextView subtitle = (TextView) sectionView
				.findViewById(R.id.statistics_section_subtitle);
		if (subTitle != null && !subtitle.equals("")) {
			subtitle.setText(subTitle);
			subtitle.setVisibility(View.VISIBLE);
		} else {
			subtitle.setVisibility(View.GONE);
		}

		// Add chart
		((LinearLayout) sectionView.findViewById(R.id.statistics_section_chart))
				.addView(chart);

		// Add body text for explaining the chart
		TextView textView = ((TextView) sectionView
				.findViewById(R.id.statistics_section_body));
		if (mDisplayStatisticDescription) {
			textView.setText(bodyResId);
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}

		// Add the section to the general charts layout
		mChartsLayout.addView(sectionView);
	}

	private int getElementWidth(int elements) {
		// Get screen width
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		// Assume 90% of screen width is actually available to display all
		// elements
		return (int) ((float) 0.90 * displayMetrics.widthPixels / elements);
	}

	private boolean createElapsedTimeHistoryChart() {
		// Retrieve the data
		HistoricStatistics historicStatistics = mStatisticsDatabaseAdapter
				.getHistoricData(StatisticsDatabaseAdapter.KEY_ELAPSED_TIME,
						mGridsize, mGridsize);

		// Check if at least one serie will contain data.
		if (!historicStatistics.isXYSeriesUsed(null)) {
			return false;
		}

		// Define the renderer
		XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setXAxisMin(0);
		xyMultipleSeriesRenderer.setXAxisMax(historicStatistics.getMaxX() + 1);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setMargins(new int[] { 0, 50, 40, 10 });
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
													R.string.statistics_elapsed_time_historic_solved_average),
									yScale));
			XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
			xySeriesRenderer.setColor(chartSignal2);
			xySeriesRenderer.setLineWidth(4);
			xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesRenderer);
		}

		// Display as stacked bar chart here. As the series are mutually
		// exclusive this will result in one single bar per game which is
		// entirely colored based on status of game.
		String[] types = typesList.toArray(new String[typesList.size()]);
		addStatisticsSection(R.string.statistics_elapsed_time_historic_title,
				null, R.string.statistics_elapsed_time_historic_body,
				ChartFactory.getCombinedXYChartView(this,
						xyMultipleSeriesDataset, xyMultipleSeriesRenderer,
						types));

		return true;
	}
}