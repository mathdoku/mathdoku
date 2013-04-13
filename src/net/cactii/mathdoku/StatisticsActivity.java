package net.cactii.mathdoku;

import net.cactii.mathdoku.statistics.CumulativeStatistics;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

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

	// Text size for body text
	private int mDefaultTextSize;

	// The inflater for this activity.
	private LayoutInflater mLayoutInflater;

	private boolean mDisplayStatisticDescription;

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
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = new StatisticsDatabaseAdapter(
				databaseHelper);
		mGridStatistics = statisticsDatabaseAdapter.get(mGridSignatureId);

		if (mGridStatistics != null) {
			mCumulativeStatisticsCurrentGridSize = statisticsDatabaseAdapter
					.getByGridSize(mGridStatistics.gridSize, mGridStatistics.gridSize);
			mCumulativeStatisticsAllGridSizes = statisticsDatabaseAdapter
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
		renderer.addSeriesRenderer(createSimpleSeriesRenderer(0xFF80FF00));

		// Cells empty
		categorySeries.add(
				getResources().getString(R.string.progress_chart_cells_empty)
						+ " (" + mGridStatistics.mCellsUserValueEmtpty + ")",
				(double) mGridStatistics.mCellsUserValueEmtpty / totalCells);
		renderer.addSeriesRenderer(createSimpleSeriesRenderer(0xFFD4D4D4));

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
		xyMultipleSeriesRenderer.setYTitle("Number of times used");
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
					.addSeriesRenderer(createSimpleSeriesRenderer(0xFF80FF00));
			maxYValue = Math.max(maxYValue, mGridStatistics.mUserValueReplaced);
		}

		// Bar for number of maybe values that have been used while playing the
		// game. Note this is *not* the actual number ofpossible value currently
		// visible.
		if (mGridStatistics.mMaybeValue > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_maybe_value_used));
			xySeries.add(++countCategories, mGridStatistics.mMaybeValue);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(0xFFFF00FF));
			maxYValue = Math.max(maxYValue, mGridStatistics.mMaybeValue);
		}

		// Bar for number of times the undo button was used
		if (mGridStatistics.mUndoButton > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_undo_button_used));
			xySeries.add(++countCategories, mGridStatistics.mUndoButton);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(0xFF8000FF));
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
					.addSeriesRenderer(createSimpleSeriesRenderer(0xFF0000FF));
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
					.addSeriesRenderer(createSimpleSeriesRenderer(0xFFFE9980));
			categoryIndex++;
		}

		// Cell revealed option used
		if (mGridStatistics.mCellsRevealed > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_cells_revealed));
			xySeries.add(categoryIndex, mGridStatistics.mCellsRevealed);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(0xFFFECCBF));
			categoryIndex++;
		}

		// Cage operator revealed option used
		if (mGridStatistics.mOperatorsRevevealed > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_operators_revealed));
			xySeries.add(categoryIndex, mGridStatistics.mOperatorsRevevealed);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(0xFFB22400));
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
					.addSeriesRenderer(createSimpleSeriesRenderer(0xFFFF3300));
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
	private boolean createSolvedUnSolvedChart(CumulativeStatistics cumulativeStatistics) {
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
			categorySeries
					.add(getResources().getString(
							R.string.solved_chart_games_solved)
							+ " ("
							+ cumulativeStatistics.mCountSolvedManually
							+ ")",
							(double) cumulativeStatistics.mCountSolvedManually
									/ cumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(0xFF80FF00));
		}

		// Games for which the solution is revealed
		if (cumulativeStatistics.mCountSolutionRevealed > 0) {
			categorySeries
					.add(getResources().getString(
							R.string.solved_chart_games_solution_revealed)
							+ " ("
							+ cumulativeStatistics.mCountSolutionRevealed
							+ ")",
							(double) cumulativeStatistics.mCountSolutionRevealed
									/ cumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(0xFFFF0000));
		}

		// Games which have not yet been finished
		int countUnfinished = cumulativeStatistics.mCountStarted
				- cumulativeStatistics.mCountFinished;
		if (countUnfinished > 0) {
			categorySeries
					.add(getResources().getString(
							R.string.solved_chart_games_unfinished)
							+ " (" + countUnfinished + ")",
							(double) countUnfinished
									/ cumulativeStatistics.mCountStarted);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(0xFFD4D4D4));
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

}
