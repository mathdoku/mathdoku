package net.mathdoku.plus.archive.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridLoader;
import net.mathdoku.plus.puzzle.ui.GridViewerView;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.ui.StatisticsBaseFragment;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;
import net.mathdoku.plus.ui.PuzzleParameterDifficultyRatingBar;
import net.mathdoku.plus.util.Util;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;

import java.text.DateFormat;

/**
 * An archive fragment representing a puzzle which is archived.
 */
public class ArchiveFragment extends StatisticsBaseFragment implements OnSharedPreferenceChangeListener {
    @SuppressWarnings("unused")
    private static final String TAG = ArchiveFragment.class.getName();

    public static final String BUNDLE_KEY_SOLVING_ATTEMPT_ID = "solvingAttemptId";
    private static final int Y_VALUE_CHEAT_SOLUTION_REVEALED = 1;

    private Grid mGrid;
    private GridStatistics mGridStatistics;

    // Tags to identify the statistics sections which are searched by tag.
    public static final String AVOIDABLE_MOVES_CHART_TAG_ID = "FinishedPuzzleAvoidableMovesChart";
    public static final String CHEATS_CHART_TAG_ID = "FinishedPuzzleCheatsCharts";

    // For all bar charts the same maximum number of bars is used. In this way
    // it can be ensured that bars in all bar charts have the same width.
    private static final int MAX_CATEGORIES_BAR_CHART = 5;

    private Preferences mPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, R.layout.archive_fragment, container, savedInstanceState);

        // Get preferences
        mPreferences = Preferences.getInstance();
        setDisplayChartDescription(mPreferences.isArchiveChartDescriptionVisible());
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        // Load grid from database
        mGrid = new GridLoader().load(getSolvingAttemptIdFromBundle());
        if (mGrid != null) {
            mGridStatistics = mGrid.getGridStatistics();

            setGridViewerView(rootView);
            setArchiveActionButton(rootView);
            deactivateGrid();
            setPuzzleParameterDifficultyRatingBar(rootView);
            setDateCreated(rootView);
            setDateFinished(rootView);
            setNumberOfReplays(rootView);
            setElapsedTime(rootView);
            setCheatPenalty(rootView);

            createAllCharts();
        }

        return rootView;
    }

    private void setCheatPenalty(View rootView) {
        if (mGridStatistics != null && mGridStatistics.getCheatPenaltyTime() > 0) {
            rootView.findViewById(R.id.statistics_general_cheat_penalty_time_row)
                    .setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.statistics_general_cheat_penalty_time)).setText(
                    Util.durationTimeToString(mGridStatistics.getCheatPenaltyTime()));
        }
    }

    private void setElapsedTime(View rootView) {
        if (!mGrid.isActive()) {
            rootView.findViewById(R.id.statistics_general_elapsed_time_row)
                    .setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.statistics_general_elapsed_time)).setText(
                    Util.durationTimeToString(mGrid.getElapsedTime()));
        }
    }

    private void setNumberOfReplays(View rootView) {
        if (mGridStatistics != null && mGridStatistics.getReplayCount() > 0) {
            rootView.findViewById(R.id.statistics_general_replays_row)
                    .setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.statistics_general_replays)).setText(
                    Integer.toString(mGridStatistics.getReplayCount()));
        }
    }

    private void setDateFinished(View rootView) {
        if (mGridStatistics != null && mGridStatistics.isFinished()) {
            rootView.findViewById(R.id.statistics_general_date_finished_row)
                    .setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.statistics_general_date_finished)).setText(
                    DateFormat.getDateTimeInstance()
                            .format(mGridStatistics.mLastMove));
        }
    }

    private void setDateCreated(View rootView) {
        // Set date created
        if (mGrid.getDateCreated() > 0) {
            rootView.findViewById(R.id.statistics_general_date_created_row)
                    .setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.statistics_general_date_created)).setText(
                    DateFormat.getDateTimeInstance()
                            .format(mGrid.getDateCreated()));
        }
    }

    private void setGridViewerView(View rootView) {
        // Get fragment manager and start a transaction.
        GridViewerView mGridViewerView;
        mGridViewerView = (GridViewerView) rootView.findViewById(R.id.grid_viewer_view);

        // Load grid into grid view
        mGridViewerView.loadNewGrid(mGrid);

        // Restrict the width of the grid viewer view when displayed in
        // landscape mode to the maximum height of the available area.
        mGridViewerView.setInScrollView(true);
        mGridViewerView.setMaximumWidth(getMaxContentHeight(0, 20));
    }

    private void setPuzzleParameterDifficultyRatingBar(View rootView) {
        ((PuzzleParameterDifficultyRatingBar) rootView.findViewById(
                R.id.puzzleParameterDifficultyRatingBar)).setNumStars(mGrid.getPuzzleComplexity());
    }

    private void deactivateGrid() {
        if (mGrid.isActive()) {
            // Disable the grid as the user should not be able to click
            // cells in the archive view
            mGrid.setActive(false);
        }
    }

    private void setArchiveActionButton(View rootView) {
        Button archiveActionButton = (Button) rootView.findViewById(R.id.archiveActionButton);
        archiveActionButton.setBackgroundColor(Painter.getInstance()
                                                       .getButtonBackgroundColor());
        if (getActivity() instanceof PuzzleFragmentActivity) {
            final PuzzleFragmentActivity puzzleFragmentActivity = (PuzzleFragmentActivity) getActivity();

            // In case the fragment was called by the puzzle fragment
            // activity the play button will create a similar game.
            archiveActionButton.setText(R.string.archive_play_similar_puzzle);

            archiveActionButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Start a game with the same puzzle parameter settings
                    // as were used for creating the last game.
                    puzzleFragmentActivity.startNewGame(mPreferences.getPuzzleParameterGridSize(),
                                                        !mPreferences.getPuzzleParameterOperatorsVisible(),
                                                        mPreferences.getPuzzleParameterComplexity());
                }
            });

        } else if (getActivity() instanceof ArchiveFragmentActivity && mGrid.isActive()) {
            // The fragment is started by the archive fragment activity. In
            // case the puzzle isn't finished the action button reloads the
            // puzzle so it can be continued.
            archiveActionButton.setText(R.string.archive_continue_unfinished_puzzle);

            archiveActionButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(BUNDLE_KEY_SOLVING_ATTEMPT_ID, getSolvingAttemptId());
                    getActivity().setResult(Activity.RESULT_OK, intent);

                    // Finish the archive activity
                    getActivity().finish();
                }
            });
        } else {
            archiveActionButton.setVisibility(View.GONE);
        }
    }

    private int getSolvingAttemptIdFromBundle() {
        return getArguments().getInt(BUNDLE_KEY_SOLVING_ATTEMPT_ID);
    }

    @Override
    public void onDestroy() {
        if (mPreferences != null) {
            mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
        removeAllCharts();

        // Build all charts for current game only
        createProgressChart();
        createAvoidableMovesChart();
        createUsedCheatsChart();
    }

    /**
     * Create a pie chart for the progress of solving.
     */
    private void createProgressChart() {
        if (mGrid == null || mGrid.getGridSize() == 0 || mGridStatistics == null) {
            // No progress to report.
            return;
        }

        // Determine total number of cells in grid
        int totalCells = mGrid.getGridSize() * mGrid.getGridSize();

        PieChartSeries pieChartSeries = new PieChartSeries(mDefaultTextSize);
        pieChartSeries.addCategory(getResources().getString(R.string.progress_chart_cells_filled),
                                   mGridStatistics.mCellsFilled, totalCells, COLOR_GREEN);
        pieChartSeries.addCategory(getResources().getString(R.string.progress_chart_cells_revealed),
                                   mGridStatistics.mCellsRevealed, totalCells, COLOR_RED_1);
        pieChartSeries.addCategory(getResources().getString(R.string.progress_chart_cells_empty),
                                   mGridStatistics.mCellsEmpty, totalCells, COLOR_GREY);
        pieChartSeries.addCategory(getResources().getString(R.string.progress_chart_cells_revealed),
                                   mGridStatistics.mCellsRevealed, totalCells, COLOR_RED_3);

        if (pieChartSeries.getCategorySeries()
                .getItemCount() > 1 || mGridStatistics.mCellsRevealed > 0) {
            addChartToStatisticsSection(null, getResources().getString(R.string.progress_chart_title),
                                        ChartFactory.getPieChartView(getActivity(), pieChartSeries.getCategorySeries(),
                                                                     pieChartSeries.getRenderer()), null,
                                        getResources().getString(R.string.progress_chart_body));
        }
    }

    /**
     * Create the chart for the avoidable moves.
     */
    private void createAvoidableMovesChart() {
        BarChartSeries barChartSeries = new BarChartSeries();
        barChartSeries.addSeries(getResources().getString(R.string.avoidable_moves_chart_entered_value_replaced),
                                 mGridStatistics.mEnteredValueReplaced, COLOR_GREEN);
        barChartSeries.addSeries(getResources().getString(R.string.avoidable_moves_chart_maybe_value_used),
                                 mGridStatistics.mMaybeValue, COLOR_PINK);
        barChartSeries.addSeries(getResources().getString(R.string.avoidable_moves_chart_undo_button_used),
                                 mGridStatistics.mActionUndoMove, COLOR_PURPLE);
        barChartSeries.addSeries(getResources().getString(R.string.avoidable_moves_chart_clear_used),
                                 mGridStatistics.mActionClearCell + mGridStatistics.mActionClearGrid, COLOR_BLUE);

        if (!barChartSeries.isEmpty()) {
            barChartSeries.setYTitle(getResources().getString(R.string.avoidable_moves_yaxis_description));
            barChartSeries.setTextSize(mDefaultTextSize);
            barChartSeries.setBarWidth(getBarWidth());
            // Add new statistics section to the activity
            addChartToStatisticsSection(AVOIDABLE_MOVES_CHART_TAG_ID,
                                        getResources().getString(R.string.avoidable_moves_chart_title),
                                        ChartFactory.getBarChartView(getActivity(), barChartSeries.getDataset(),
                                                                     barChartSeries.getRenderer(), Type.DEFAULT), null,
                                        getResources().getString(R.string.avoidable_moves_chart_body));
        }
    }

    /**
     * Create bar chart for the cheats which are used
     */
    private void createUsedCheatsChart() {
        BarChartSeries barChartSeries = new BarChartSeries();
        barChartSeries.addSeries(getResources().getString(R.string.statistics_cheats_check_progress),
                                 mGridStatistics.mActionCheckProgress, COLOR_RED_1);
        barChartSeries.addSeries(getResources().getString(R.string.statistics_cheats_cells_revealed),
                                 mGridStatistics.mActionRevealCell, COLOR_RED_2);
        barChartSeries.addSeries(getResources().getString(R.string.statistics_cheats_operators_revealed),
                                 mGridStatistics.mActionRevealOperator, COLOR_RED_3);
        if (mGridStatistics.isSolutionRevealed()) {
            barChartSeries.addSeries(getResources().getString(R.string.statistics_cheats_solution_revealed),
                                     Y_VALUE_CHEAT_SOLUTION_REVEALED, COLOR_RED_4);
        }

        if (!barChartSeries.isEmpty()) {
            barChartSeries.setYTitle(getResources().getString(R.string.statistics_cheats_yaxis_description));
            barChartSeries.setTextSize(mDefaultTextSize);
            barChartSeries.setBarWidth(getBarWidth());
            addChartToStatisticsSection(CHEATS_CHART_TAG_ID,
                                        getResources().getString(R.string.statistics_cheats_used_title),
                                        ChartFactory.getBarChartView(getActivity(), barChartSeries.getDataset(),
                                                                     barChartSeries.getRenderer(), Type.DEFAULT), null,
                                        getResources().getString(R.string.statistics_cheats_used_body));
        }
    }

    /**
     * Get the width to be used for a bar in bar chart given a maximum number of bars.
     *
     * @return The width for a bar.
     */
    private int getBarWidth() {
        // Get screen width
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager()
                .getDefaultDisplay()
                .getMetrics(mDisplayMetrics);

        // Assume 90% of screen width is actually available to display all
        // elements
        return (int) ((float) 0.90 * mDisplayMetrics.widthPixels / MAX_CATEGORIES_BAR_CHART / 2);
    }

    /**
     * Get the solving attempt id which is being showed in this archive fragment.
     *
     * @return The solving attempt id which is being showed in this archive fragment.
     */
    public int getSolvingAttemptId() {
        return mGrid.getSolvingAttemptId();
    }

    @Override
    protected int getMaxContentHeight(int titleHeightPixels, int paddingChartPixels) {
        int maxContentHeight = super.getMaxContentHeight(titleHeightPixels, paddingChartPixels);

        // The archive has an additional pager which is shown below the action
        // bar. The height of this pager is approximately two time the size of
        // the text displayed inside as the top and bottom padding are 50% of
        // the text size.
        maxContentHeight -= getResources().getDimensionPixelSize(R.dimen.text_size_default) * 2;

        return maxContentHeight;
    }

    /**
     * Get the grid which is displayed in the archive fragment.
     *
     * @return The grid which is displayed in the archive fragment.
     */
    public Grid getGrid() {
        return mGrid;
    }
}
