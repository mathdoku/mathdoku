package net.cactii.mathdoku.ui;

import java.text.DateFormat;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.grid.DigitPositionGrid;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.grid.ui.GridViewerView;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.util.Util;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * An archive fragment representing a puzzle which is archived.
 */
public class ArchiveFragment extends StatisticsBaseFragment implements
		OnSharedPreferenceChangeListener {

	@SuppressWarnings("unused")
	private static final String TAG = "ArchiveFragment";

	public static final String BUNDLE_KEY_SOLVING_ATTEMPT_ID = "solvingAttemptId";

	private static DigitPositionGrid mDigitPositionGrid = null;

	private GridStatistics mGridStatistics;

	private int mSolvingAttemptId;
	private int mGridSize;

	// Tags to identify the statistics sections which are searched by tag.
	public static final String AVOIDABLE_MOVES_CHART_TAG_ID = "FinishedPuzzleAvoidableMovesChart";
	public static final String CHEATS_CHART_TAG_ID = "FinishedPuzzleCheatsCharts";

	// For all bar charts the same maximum number of bars is used. In this way
	// it can be ensured that bars in all bar charts have the same width.
	private static final int MAX_CATEGORIES_BAR_CHART = 5;

	private Preferences mPreferences;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, R.layout.archive_fragment,
				container, savedInstanceState);

		Bundle args = getArguments();
		mSolvingAttemptId = args.getInt(BUNDLE_KEY_SOLVING_ATTEMPT_ID);

		// Get preferences
		mPreferences = Preferences.getInstance();
		setDisplayChartDescription(mPreferences
				.isArchiveChartDescriptionVisible());
		mPreferences.mSharedPreferences
				.registerOnSharedPreferenceChangeListener(this);

		// Get fragment manager and start a transaction.
		GridViewerView mGridViewerView;
		mGridViewerView = (GridViewerView) rootView
				.findViewById(R.id.grid_viewer_view);

		// Load grid from database
		Grid grid = new Grid();
		if (grid.load(mSolvingAttemptId)) {
			mGridSize = grid.getGridSize();

			// Load grid into grid view
			mGridViewerView.loadNewGrid(grid);

			// Set background color of button
			Button archiveReloadButton = (Button) rootView
					.findViewById(R.id.archiveReloadButton);
			archiveReloadButton.setBackgroundColor(Painter.getInstance()
					.getButtonBackgroundColor());

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
				mGridViewerView.setDigitPositionGrid(mDigitPositionGrid);

				// Change text of the reload button below grid
				archiveReloadButton
						.setText(R.string.archive_reload_unfinished_game);

				// Disable the grid as the user should not be able to click
				// cells in the archive view
				grid.setActive(false);
			}

			// Display the difficulty rating.
			final VerticalRatingBar puzzleParameterDifficultyRatingBar = (VerticalRatingBar) rootView
					.findViewById(R.id.puzzleParameterDifficultyRatingBar);
			puzzleParameterDifficultyRatingBar.setEnabled(false);
			switch (grid.getPuzzleComplexity()) {
			case VERY_EASY:
				puzzleParameterDifficultyRatingBar.setNumStars(1);
				break;
			case EASY:
				puzzleParameterDifficultyRatingBar.setNumStars(2);
				break;
			case NORMAL:
				puzzleParameterDifficultyRatingBar.setNumStars(3);
				break;
			case DIFFICULT:
				puzzleParameterDifficultyRatingBar.setNumStars(4);
				break;
			case VERY_DIFFICULT:
				puzzleParameterDifficultyRatingBar.setNumStars(5);
				break;
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

			// Show the number of times the puzzle is replayed.
			if (mGridStatistics != null && mGridStatistics.getReplayCount() > 0) {
				((TableRow) rootView
						.findViewById(R.id.statistics_general_replays_row))
						.setVisibility(View.VISIBLE);
				((TextView) rootView
						.findViewById(R.id.statistics_general_replays))
						.setText(Integer.toString(mGridStatistics
								.getReplayCount()));
			}

			// Show elapsed time for puzzles which are solved manually.
			if (grid.isActive() == false) {
				((TableRow) rootView
						.findViewById(R.id.statistics_general_elapsed_time_row))
						.setVisibility(View.VISIBLE);
				((TextView) rootView
						.findViewById(R.id.statistics_general_elapsed_time))
						.setText(Util.durationTimeToString(grid
								.getElapsedTime()));
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
			createAllCharts();
		}

		return rootView;
	}

	@Override
	public void onDestroy() {
		if (mPreferences != null && mPreferences.mSharedPreferences != null) {
			mPreferences.mSharedPreferences
					.unregisterOnSharedPreferenceChangeListener(this);
		}
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Preferences.ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE)) {
			setDisplayChartDescription(Preferences.getInstance(getActivity())
					.isArchiveChartDescriptionVisible());
		}

		createAllCharts();
	}

	/**
	 * Creates all charts.
	 */
	private void createAllCharts() {
		mChartsLayout.removeAllViewsInLayout();

		// Build all charts for current game only
		createProgressChart();
		createAvoidableMovesChart();
		createUsedCheatsChart();
	}

	/**
	 * Create a pie chart for the progress of solving.
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createProgressChart() {
		if (mGridSize == 0 || mGridStatistics == null) {
			// No progress to report.
			return false;
		}

		// Determine total number of cells in grid
		float totalCells = mGridSize * mGridSize;

		// Count number of categories. Chart will only be displayed it minimal 2
		// categories are shown.
		int countCategories = 0;

		// Define the renderer
		DefaultRenderer renderer = new DefaultRenderer();
		renderer.setShowLabels(false);
		renderer.setShowLegend(true);
		renderer.setLegendTextSize(mDefaultTextSize);
		renderer.setFitLegend(true);
		renderer.setMargins(new int[] { 0, mDefaultTextSize, mDefaultTextSize,
				mDefaultTextSize });

		renderer.setZoomButtonsVisible(false);
		renderer.setZoomEnabled(false);
		renderer.setPanEnabled(false);
		renderer.setInScroll(true);

		// Create object for category series and the series renderer
		CategorySeries categorySeries = new CategorySeries("");

		// Cells filled
		if (mGridStatistics.mCellsFilled > 0) {
			categorySeries.add(
					getResources().getString(
							R.string.progress_chart_cells_filled)
							+ " (" + mGridStatistics.mCellsFilled + ")",
					(double) mGridStatistics.mCellsFilled / totalCells);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartGreen1));
			countCategories++;
		}

		// Cells revealed
		if (mGridStatistics.mCellsRevealed > 0) {
			categorySeries.add(
					getResources().getString(
							R.string.progress_chart_cells_revealed)
							+ " (" + mGridStatistics.mCellsRevealed + ")",
					(double) mGridStatistics.mCellsRevealed / totalCells);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartRed1));
			countCategories++;
		}

		// Cells empty
		if (mGridStatistics.mCellsEmtpty > 0) {
			categorySeries.add(
					getResources().getString(
							R.string.progress_chart_cells_empty)
							+ " (" + mGridStatistics.mCellsEmtpty + ")",
					(double) mGridStatistics.mCellsEmtpty / totalCells);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(chartGrey1));
			countCategories++;
		}

		if (countCategories > 1 || mGridStatistics.mCellsRevealed > 0) {
			addStatisticsSection(null,
					getResources().getString(R.string.progress_chart_title),
					ChartFactory.getPieChartView(getActivity(), categorySeries,
							renderer), null,
					getResources().getString(R.string.progress_chart_body));
			return true;
		} else {
			return false;
		}
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
				+ mGridStatistics.mMaybeValue + mGridStatistics.mActionUndoMove
				+ mGridStatistics.mActionClearCell
				+ mGridStatistics.mActionClearGrid;
		if (totalAvoidableMoves == 0) {
			return false;
		}

		// Define the renderer
		XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();

		// Fix background color problem of margin in AChartEngine
		xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 50, 50, 50));

		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setFitLegend(true);
		xyMultipleSeriesRenderer.setMargins(new int[] { 0,
				2 * mDefaultTextSize, mDefaultTextSize, mDefaultTextSize });

		xyMultipleSeriesRenderer.setYTitle(getResources().getString(
				R.string.avoidable_moves_yaxis_description));
		xyMultipleSeriesRenderer.setXAxisMin(-1);
		xyMultipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
		xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
		xyMultipleSeriesRenderer.setZoomEnabled(false);
		xyMultipleSeriesRenderer.setPanEnabled(false);
		xyMultipleSeriesRenderer.setInScroll(true);
		xyMultipleSeriesRenderer.setYLabelsPadding(5f);

		// Create object for category series and the series renderer
		XYMultipleSeriesDataset xyMultipleSeriesDataset = new XYMultipleSeriesDataset();

		// While filling the categories the number of categories used and the
		// maximum Y-value is determined.
		int categoryIndex = 1;
		int maxYValue = 0;

		// Bar for number of times a user value in a cell was replace by another
		// value
		if (mGridStatistics.mUserValueReplaced > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_user_value_replaced));
			xySeries.add(categoryIndex, mGridStatistics.mUserValueReplaced);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartGreen1));
			categoryIndex++;
			maxYValue = Math.max(maxYValue, mGridStatistics.mUserValueReplaced);
		}

		// Bar for number of maybe values that have been used while playing the
		// game. Note this is *not* the actual number of possible values
		// currently visible.
		if (mGridStatistics.mMaybeValue > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_maybe_value_used));
			xySeries.add(categoryIndex, mGridStatistics.mMaybeValue);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartSignal1));
			categoryIndex++;
			maxYValue = Math.max(maxYValue, mGridStatistics.mMaybeValue);
		}

		// Bar for number of times the undo button was used
		if (mGridStatistics.mActionUndoMove > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_undo_button_used));
			xySeries.add(categoryIndex, mGridStatistics.mActionUndoMove);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartSignal2));
			categoryIndex++;
			maxYValue = Math.max(maxYValue, mGridStatistics.mActionUndoMove);
		}

		// Bar for number of times a user cleared a value in a cell or
		// the entire grid.
		int totalClears = mGridStatistics.mActionClearCell
				+ mGridStatistics.mActionClearGrid;
		if (totalClears > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.avoidable_moves_chart_clear_used));
			xySeries.add(categoryIndex, totalClears);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartSignal3));
			categoryIndex++;
			maxYValue = Math.max(maxYValue, totalClears);
		}

		// Fill dimensions of axis based on number of categories and maximum
		// Y-value.
		xyMultipleSeriesRenderer.setXAxisMax(MAX_CATEGORIES_BAR_CHART + 2);
		xyMultipleSeriesRenderer.setXLabels(0);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setYAxisMax(maxYValue + 1);
		xyMultipleSeriesRenderer.setYLabels(Math.min(4, maxYValue + 1));
		xyMultipleSeriesRenderer
				.setBarWidth(getElementWidth(MAX_CATEGORIES_BAR_CHART) / 2);

		// Add new statistics section to the activity
		addStatisticsSection(AVOIDABLE_MOVES_CHART_TAG_ID, getResources()
				.getString(R.string.avoidable_moves_chart_title),
				ChartFactory.getBarChartView(getActivity(),
						xyMultipleSeriesDataset, xyMultipleSeriesRenderer,
						Type.DEFAULT), null,
				getResources().getString(R.string.avoidable_moves_chart_body));

		return true;
	}

	/**
	 * Create bar chart for the cheats which are used
	 * 
	 * @return True in case the chart has been created. False otherwise.
	 */
	private boolean createUsedCheatsChart() {
		// Build chart for analysis of moves only in case at least one cheat
		// has been used.
		int totalCheats = mGridStatistics.mActionCheckProgress
				+ mGridStatistics.mActionRevealCell
				+ mGridStatistics.mActionRevealOperator
				+ (mGridStatistics.isSolutionRevealed() ? 1 : 0);
		if (totalCheats == 0) {
			return false;
		}

		// Determine number of cheat categories to show
		int cheatCategories = (mGridStatistics.mActionCheckProgress > 0 ? 1 : 0)
				+ (mGridStatistics.mActionRevealCell > 0 ? 1 : 0)
				+ (mGridStatistics.mActionRevealOperator > 0 ? 1 : 0)
				+ (mGridStatistics.isSolutionRevealed() ? 1 : 0);

		// Determine the highest number of cheats for a single category
		int maxCheats = Math.max(mGridStatistics.mActionCheckProgress,
				mGridStatistics.mActionRevealCell);
		maxCheats = Math.max(maxCheats, mGridStatistics.mActionCheckProgress);
		maxCheats = Math.max(maxCheats,
				(mGridStatistics.isSolutionRevealed() ? 1 : 0));

		// Define the renderer
		XYMultipleSeriesRenderer xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();

		// Fix background color problem of margin in AChartEngine
		xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 50, 50, 50));

		xyMultipleSeriesRenderer.setLabelsTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setLegendTextSize(mDefaultTextSize);
		xyMultipleSeriesRenderer.setFitLegend(true);
		xyMultipleSeriesRenderer.setMargins(new int[] { 0,
				2 * mDefaultTextSize, mDefaultTextSize, mDefaultTextSize });

		xyMultipleSeriesRenderer.setYTitle(getResources().getString(
				R.string.statistics_cheats_yaxis_description));
		xyMultipleSeriesRenderer.setXAxisMin(-1);
		xyMultipleSeriesRenderer.setXAxisMax(cheatCategories + 2);
		xyMultipleSeriesRenderer.setXLabels(cheatCategories);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setYAxisMax(maxCheats + 1);
		xyMultipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
		xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
		xyMultipleSeriesRenderer.setZoomEnabled(false);
		xyMultipleSeriesRenderer.setPanEnabled(false);
		xyMultipleSeriesRenderer.setInScroll(true);
		xyMultipleSeriesRenderer.setYLabelsPadding(5f);

		// Create object for category series and the series renderer
		XYMultipleSeriesDataset xyMultipleSeriesDataset = new XYMultipleSeriesDataset();

		// While filling the categories the number of categories used and the
		// maximum Y-value is determined.
		int categoryIndex = 1;
		int maxYValue = 0;

		// Check progress option used
		if (mGridStatistics.mActionCheckProgress > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_check_progress));
			xySeries.add(categoryIndex, mGridStatistics.mActionCheckProgress);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed1));
			categoryIndex++;
			maxYValue = Math.max(maxYValue,
					mGridStatistics.mActionCheckProgress);
		}

		// Cell revealed option used
		if (mGridStatistics.mActionRevealCell > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_cells_revealed));
			xySeries.add(categoryIndex, mGridStatistics.mActionRevealCell);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed2));
			categoryIndex++;
			maxYValue = Math.max(maxYValue, mGridStatistics.mActionRevealCell);
		}

		// Cage operator revealed option used
		if (mGridStatistics.mActionRevealOperator > 0) {
			XYSeries xySeries = new XYSeries(getResources().getString(
					R.string.statistics_cheats_operators_revealed));
			xySeries.add(categoryIndex, mGridStatistics.mActionRevealOperator);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(chartRed3));
			categoryIndex++;
			maxYValue = Math.max(maxYValue,
					mGridStatistics.mActionRevealOperator);
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
		xyMultipleSeriesRenderer.setXAxisMax(MAX_CATEGORIES_BAR_CHART + 2);
		xyMultipleSeriesRenderer.setXLabels(0);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setYAxisMax(maxYValue + 1);
		xyMultipleSeriesRenderer.setYLabels(Math.min(4, maxYValue + 1));
		xyMultipleSeriesRenderer
				.setBarWidth(getElementWidth(MAX_CATEGORIES_BAR_CHART) / 2);

		addStatisticsSection(
				CHEATS_CHART_TAG_ID,
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

	/**
	 * Get the solving attempt id which is being showed in this archive
	 * fragment.
	 * 
	 * @return The solving attempt id which is being showed in this archive
	 *         fragment.
	 */
	public int getSolvingAttemptId() {
		return mSolvingAttemptId;
	}
}