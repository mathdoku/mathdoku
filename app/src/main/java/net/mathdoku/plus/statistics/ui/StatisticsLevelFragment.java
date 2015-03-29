package net.mathdoku.plus.statistics.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.statistics.CumulativeStatistics;
import net.mathdoku.plus.statistics.HistoricStatistics;
import net.mathdoku.plus.storage.selector.CumulativeStatisticsSelector;
import net.mathdoku.plus.util.Util;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;

/**
 * A fragment representing the statistics for a specific grid size or the cumulative statistics for all levels.
 */
public class StatisticsLevelFragment extends StatisticsBaseFragment implements OnSharedPreferenceChangeListener {

    public static final String ARG_GRID_SIZE_MIN = "grid_size_min";
    public static final String ARG_GRID_SIZE_MAX = "grid_size_max";
    private int mDefaultTextSizeInDIP;

    // Grid size for currently selected grid
    private int mMinGridSize;
    private int mMaxGridSize;

    private CumulativeStatistics mCumulativeStatistics;

    private Preferences mPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        // Get minimum and maximum grid size from bundle
        Bundle bundle = getArguments();
        mMinGridSize = bundle.getInt(ARG_GRID_SIZE_MIN);
        mMaxGridSize = bundle.getInt(ARG_GRID_SIZE_MAX);

        // Get preferences
        mPreferences = Preferences.getInstance();
        setDisplayChartDescription(mPreferences.isStatisticsChartDescriptionVisible());
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        mDefaultTextSizeInDIP = (int) (getResources().getDimension(
                net.mathdoku.plus.R.dimen.text_size_default) / getResources().getDisplayMetrics().density);


        mCumulativeStatistics = new CumulativeStatisticsSelector(mMinGridSize, mMaxGridSize).getCumulativeStatistics();

        createAllCharts();

        return rootView;
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
        removeAllCharts();

        // Build all charts for all games at current level
        boolean statisticsDisplayed = createSolvedUnSolvedChart();

        statisticsDisplayed = createElapsedTimeHistoryChart() || statisticsDisplayed;

        // Check if at least one statistic is displayed.
        if (!statisticsDisplayed) {
            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(
                    new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(getResources().getString(R.string.statistics_not_available, mMinGridSize));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mDefaultTextSizeInDIP);

            addViewToStatisticsSection(textView);
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
        renderer.setMargins(new int[]{0, mDefaultTextSize, mDefaultTextSize, mDefaultTextSize});

        renderer.setLegendTextSize(mDefaultTextSize);
        renderer.setZoomButtonsVisible(false);
        renderer.setZoomEnabled(false);
        renderer.setPanEnabled(false);
        renderer.setInScroll(true);

        // Create object for category series and the series renderer
        CategorySeries categorySeries = new CategorySeries("");

        // Games solved manually
        if (mCumulativeStatistics.mCountSolvedManually > 0) {
            categorySeries.add(getResources().getString(R.string.chart_serie_solved) + " (" +
                                       mCumulativeStatistics.mCountSolvedManually + ")",
                               (double) mCumulativeStatistics.mCountSolvedManually / mCumulativeStatistics
                                       .mCountStarted);
            renderer.addSeriesRenderer(createSimpleSeriesRenderer(COLOR_GREEN));
        }

        // Games for which the solution is revealed
        if (mCumulativeStatistics.mCountSolutionRevealed > 0) {
            categorySeries.add(getResources().getString(R.string.chart_serie_solution_revealed) + " (" +
                                       mCumulativeStatistics.mCountSolutionRevealed + ")",
                               (double) mCumulativeStatistics.mCountSolutionRevealed / mCumulativeStatistics
                                       .mCountStarted);
            renderer.addSeriesRenderer(createSimpleSeriesRenderer(COLOR_RED_1));
        }

        // Games which have not yet been finished
        int countUnfinished = mCumulativeStatistics.mCountStarted - mCumulativeStatistics.mCountFinished;
        if (countUnfinished > 0) {
            categorySeries.add(getResources().getString(R.string.chart_serie_unfinished) + " (" + countUnfinished + ")",
                               (double) countUnfinished / mCumulativeStatistics.mCountStarted);
            renderer.addSeriesRenderer(createSimpleSeriesRenderer(COLOR_GREY));
        }

        // Add section to activity
        addChartToStatisticsSection(getResources().getString(R.string.solved_chart_title),
                                    ChartFactory.getPieChartView(getActivity(), categorySeries, renderer), null,
                                    getResources().getString(R.string.solved_chart_body));
        return true;
    }

    /**
     * Create a combined bar and line chart which displays the elapsed time per game and the historic average.
     *
     * @return True in case the chart has been created. False otherwise.
     */
    private boolean createElapsedTimeHistoryChart() {
        HistoricStatistics historicStatistics = new HistoricStatistics(mMinGridSize, mMaxGridSize);

        // The number of entries to be displayed is restricted to the maximum set in the preferences.
        historicStatistics.setLimit(Preferences.getInstance()
                                            .getStatisticsSettingElapsedTimeChartMaximumGames());

        if (historicStatistics.isEmpty()) {
            return false;
        }

        ElapsedTimeSeries elapsedTimeSeries = new ElapsedTimeSeries(historicStatistics, getResources());
        elapsedTimeSeries.setTextSize(mDefaultTextSize);
        addChartToStatisticsSection(getResources().getString(R.string.statistics_elapsed_time_historic_title),
                                    ChartFactory.getCombinedXYChartView(getActivity(),
                                                                        elapsedTimeSeries.getXyMultipleSeriesDataset(),
                                                                        elapsedTimeSeries.getXyMultipleSeriesRenderer(),
                                                                        elapsedTimeSeries.getTypes()),
                                    getSummaryTableLayout(historicStatistics),
                                    getResources().getString(R.string.statistics_elapsed_time_historic_body));

        return true;
    }

    private TableLayout getSummaryTableLayout(HistoricStatistics historicStatistics) {
        TableLayout tableLayout = null;
        if (historicStatistics.containsTotalPlayingTimeDataPointForXYSeries(SolvingAttemptStatus.FINISHED_SOLVED)) {
            // Create a table with extra data for fastest, average and slowest time.
            tableLayout = new TableLayout(getActivity());
            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                                                      LayoutParams.WRAP_CONTENT);
            tableLayout.setLayoutParams(tableLayoutParams);

            tableLayout.addView(createSummaryTableRow(tableLayoutParams, getResources().getString(
                                                              R.string.chart_serie_solved) + String.format(" (%d)",
                                                                                                           mCumulativeStatistics.mCountSolvedManually),
                                                      null));
            tableLayout.addView(createSummaryTableRow(tableLayoutParams, getResources().getString(
                                                              R.string.statistics_elapsed_time_historic_solved_fastest),
                                                      Util.durationTimeToString(historicStatistics.getSolvedFastest())));
            tableLayout.addView(createSummaryTableRow(tableLayoutParams, getResources().getString(
                                                              R.string.statistics_elapsed_time_historic_solved_average),
                                                      Util.durationTimeToString(historicStatistics.getSolvedAverage())));
            tableLayout.addView(createSummaryTableRow(tableLayoutParams, getResources().getString(
                                                              R.string.statistics_elapsed_time_historic_solved_slowest),
                                                      Util.durationTimeToString(historicStatistics.getSolvedSlowest())));
        }
        return tableLayout;
    }

    private TableRow createSummaryTableRow(TableLayout.LayoutParams tableLayoutParams, String label, String value) {
        TableRow tableRow = new TableRow(getActivity());

        tableRow.setLayoutParams(tableLayoutParams);
        TableRow.LayoutParams tableRowLayoutParams = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                                               LayoutParams.WRAP_CONTENT);
        tableRow.addView(createSummaryTextView(tableRowLayoutParams, label));
        if (value != null) {
            tableRow.addView(createSummaryTextView(tableRowLayoutParams, value));
        }

        return tableRow;
    }

    private TextView createSummaryTextView(TableRow.LayoutParams tableRowLayoutParams, String label) {
        TextView textViewLabel = new TextView(getActivity());

        textViewLabel.setLayoutParams(tableRowLayoutParams);
        textViewLabel.setText(label);
        textViewLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mDefaultTextSizeInDIP);

        return textViewLabel;
    }
}
