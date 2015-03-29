package net.mathdoku.plus.statistics.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;

import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.SolvingAttemptStatus;
import net.mathdoku.plus.statistics.HistoricStatistics;

import org.achartengine.chart.BarChart;
import org.achartengine.chart.LineChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;

class ElapsedTimeSeries {
    public static final double SCALE_TO_MAX_YVALUE_FACTOR = 1.1;
    private final HistoricStatistics historicStatistics;
    private final Resources resources;
    private final XYMultipleSeriesRenderer xyMultipleSeriesRenderer;
    private final XYMultipleSeriesDataset xyMultipleSeriesDataset;
    private final List<String> typesList;
    private int textSize;
    private HistoricStatistics.Scale yScale;
    private double maxYValue;
    private int timeUnitsPluralYAxisResId;
    boolean cheatLegendAlreadyDisplayedForAnotherSeries;


    public ElapsedTimeSeries(HistoricStatistics historicStatistics, Resources resources) {
        this.historicStatistics = historicStatistics;
        this.resources = resources;

        xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
        typesList = new ArrayList<String>();
        cheatLegendAlreadyDisplayedForAnotherSeries = false;

        setScaleAndMaxValueOfYAxis();
        addSolvedGamesXYSeriesAndRenderer();
        addUnfinishedGamesXYSeriesAndRenderer();
        addSolutionRevealedGamesXYSeriesAndRenderer();
        addHistoricAverageSolvedGamesXYSeriesAndRenderer();
    }

    private void setScaleAndMaxValueOfYAxis() {
        maxYValue = historicStatistics.getMaxY(HistoricStatistics.Scale.DAYS);
        if (maxYValue >= 1) {
            yScale = HistoricStatistics.Scale.DAYS;
            timeUnitsPluralYAxisResId = R.string.time_unit_days_plural;
            return;
        }


        maxYValue = historicStatistics.getMaxY(HistoricStatistics.Scale.HOURS);
        if (maxYValue >= 1) {
            yScale = HistoricStatistics.Scale.HOURS;
            timeUnitsPluralYAxisResId = R.string.time_unit_hours_plural;
            return;
        }

        maxYValue = historicStatistics.getMaxY(HistoricStatistics.Scale.MINUTES);
        if (maxYValue >= 1) {
            yScale = HistoricStatistics.Scale.MINUTES;
            timeUnitsPluralYAxisResId = R.string.time_unit_minutes_plural;
        }

        maxYValue = historicStatistics.getMaxY(HistoricStatistics.Scale.SECONDS);
        yScale = HistoricStatistics.Scale.SECONDS;
        timeUnitsPluralYAxisResId = R.string.time_unit_seconds_plural;
    }

    private void addSolvedGamesXYSeriesAndRenderer() {
        // Add series for elapsed time (including cheat time) of solved games
        if (historicStatistics.containsTotalPlayingTimeDataPointForXYSeries(SolvingAttemptStatus.FINISHED_SOLVED)) {
            typesList.add(BarChart.TYPE);
            xyMultipleSeriesDataset.addSeries(historicStatistics.getXYSeries(SolvingAttemptStatus.FINISHED_SOLVED,
                                                                             resources.getString(
                                                                                     R.string.statistics_elapsed_time_historic_elapsed_time_solved),
                                                                             yScale, true, true));
            xyMultipleSeriesRenderer.addSeriesRenderer(createDefaultRenderer(StatisticsBaseFragment.chartGreen1));
        }

        // Add series for cheat time of solved games
        if (historicStatistics.containsCheatPenaltyTimeDataPointForXYSeries(SolvingAttemptStatus.FINISHED_SOLVED)) {
            typesList.add(BarChart.TYPE);
            xyMultipleSeriesDataset.addSeries(historicStatistics.getXYSeries(SolvingAttemptStatus.FINISHED_SOLVED,
                                                                             resources.getString(
                                                                                     R.string.statistics_elapsed_time_historic_cheat_time),
                                                                             yScale, false, true));
            xyMultipleSeriesRenderer.addSeriesRenderer(createCheatRenderer());
        }
    }

    private SimpleSeriesRenderer createDefaultRenderer(int color) {
        SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();

        simpleSeriesRenderer.setColor(color);

        return simpleSeriesRenderer;
    }

    private SimpleSeriesRenderer createCheatRenderer() {
        SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();

        simpleSeriesRenderer.setColor(StatisticsBaseFragment.chartRed1);

        // The cheat legend should only be displayed once
        // noinspection ConstantConditions
        if (cheatLegendAlreadyDisplayedForAnotherSeries) {
            simpleSeriesRenderer.setShowLegendItem(false);
        } else {
            simpleSeriesRenderer.setShowLegendItem(true);
            cheatLegendAlreadyDisplayedForAnotherSeries = true;
        }

        return simpleSeriesRenderer;
    }


    private void addUnfinishedGamesXYSeriesAndRenderer() {
        // Add series for elapsed time (including cheat time) of unfinished
        // games
        if (historicStatistics.containsTotalPlayingTimeDataPointForXYSeries(SolvingAttemptStatus.UNFINISHED)) {
            // Elapsed time so far including cheats
            typesList.add(BarChart.TYPE);
            xyMultipleSeriesDataset.addSeries(historicStatistics.getXYSeries(SolvingAttemptStatus.UNFINISHED,
                                                                             resources.getString(
                                                                                     R.string.statistics_elapsed_time_historic_elapsed_time_unfinished),
                                                                             yScale, true, true));
            xyMultipleSeriesRenderer.addSeriesRenderer(createDefaultRenderer(StatisticsBaseFragment.chartGrey1));
        }

        // Add series for cheat time of solved games
        if (historicStatistics.containsCheatPenaltyTimeDataPointForXYSeries(SolvingAttemptStatus.UNFINISHED)) {
            typesList.add(BarChart.TYPE);
            xyMultipleSeriesDataset.addSeries(historicStatistics.getXYSeries(SolvingAttemptStatus.UNFINISHED,
                                                                             resources.getString(
                                                                                     R.string.statistics_elapsed_time_historic_cheat_time),
                                                                             yScale, false, true));
            xyMultipleSeriesRenderer.addSeriesRenderer(createCheatRenderer());
        }
    }

    private void addSolutionRevealedGamesXYSeriesAndRenderer() {
        // Add series for games in which the solution was revealed
        if (historicStatistics.containsTotalPlayingTimeDataPointForXYSeries(SolvingAttemptStatus.REVEALED_SOLUTION)) {
            typesList.add(BarChart.TYPE);

            xyMultipleSeriesDataset.addSeries(historicStatistics.getXYSeriesSolutionRevealed(
                    resources.getString(R.string.statistics_elapsed_time_historic_cheat_time), maxYValue));
            xyMultipleSeriesRenderer.addSeriesRenderer(createCheatRenderer());
        }
    }

    private void addHistoricAverageSolvedGamesXYSeriesAndRenderer() {
        // Add series for the historic average of solved games. As this series
        // is displayed as a line chart, it can only be shown if at least two
        // data points in the series are available.
        if (historicStatistics.containsTotalPlayingTimeDataPointForXYSeries(SolvingAttemptStatus.FINISHED_SOLVED)) {
            XYSeries xySeries = historicStatistics.getXYSeriesHistoricAverage(SolvingAttemptStatus.FINISHED_SOLVED,
                                                                              resources.getString(
                                                                                      R.string.statistics_elapsed_time_historic_solved_average_serie),
                                                                              yScale);
            if (xySeries.getItemCount() > 1) {
                typesList.add(LineChart.TYPE);
                xyMultipleSeriesDataset.addSeries(xySeries);
                XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
                xySeriesRenderer.setColor(StatisticsBaseFragment.chartSignal2);
                xySeriesRenderer.setLineWidth(4);
                xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesRenderer);
            }
        }
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public XYMultipleSeriesRenderer getXyMultipleSeriesRenderer() {
        formatRenderer();
        return xyMultipleSeriesRenderer;
    }

    public XYMultipleSeriesDataset getXyMultipleSeriesDataset() {
        return xyMultipleSeriesDataset;
    }

    public String[] getTypes() {
        return typesList.toArray(new String[typesList.size()]);
    }

    public void formatRenderer() {
        // Fix background color problem of margin in AChartEngine
        xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 50, 50, 50));

        xyMultipleSeriesRenderer.setLabelsTextSize(textSize);
        xyMultipleSeriesRenderer.setLegendTextSize(textSize);
        xyMultipleSeriesRenderer.setXAxisMin(historicStatistics.getIndexFirstEntry() - 1);
        xyMultipleSeriesRenderer.setXAxisMax(historicStatistics.getCountIndexEntries() + 1);
        xyMultipleSeriesRenderer.setXLabels((int) Math.min(historicStatistics.getCountIndexEntries() + 1, 4));
        xyMultipleSeriesRenderer.setMargins(new int[]{0, 2 * textSize, 2 * textSize, textSize});
        xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
        xyMultipleSeriesRenderer.setZoomEnabled(false);
        xyMultipleSeriesRenderer.setPanEnabled(false);
        xyMultipleSeriesRenderer.setInScroll(true);
        xyMultipleSeriesRenderer.setFitLegend(true);

        // Use 20% of bar width as space between bars
        xyMultipleSeriesRenderer.setBarSpacing(0.2);

        // Setup Y-axis
        xyMultipleSeriesRenderer.setYAxisMin(0);
        xyMultipleSeriesRenderer.setYAxisMax(maxYValue);
        xyMultipleSeriesRenderer.setYTitle(
                resources.getString(R.string.statistics_elapsed_time_historic_title) + " (" + resources.getString(
                        timeUnitsPluralYAxisResId) + ")");
        xyMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        xyMultipleSeriesRenderer.setYLabelsPadding(5f);
        xyMultipleSeriesRenderer.setYLabelsVerticalPadding(-1 * textSize);
    }
}
