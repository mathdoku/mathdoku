package net.mathdoku.plus.archive.ui;

import android.graphics.Color;
import android.graphics.Paint;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

public class BarChartSeries {
	// For all bar charts the same maximum number of bars is used. In this way
	// it can be ensured that bars in all bar charts have the same width.
	private static final int MAX_CATEGORIES_BAR_CHART = 5;

	private final XYMultipleSeriesRenderer xyMultipleSeriesRenderer;
	private final XYMultipleSeriesDataset xyMultipleSeriesDataset;

	private int maxYValue;
	private int textSize;
	private String yTitle;
	private int barWidth;

	public BarChartSeries() {
		xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
		xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
	}

	public void addSeries(String name, int yValue, int color) {
		if (yValue > 0) {
			XYSeries xySeries = new XYSeries(name);
			xySeries.add(xyMultipleSeriesDataset.getSeriesCount() + 1, yValue);
			xyMultipleSeriesDataset.addSeries(xySeries);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(createSimpleSeriesRenderer(color));
			maxYValue = Math.max(maxYValue,
								 yValue);
		}
	}

	public boolean isEmpty() {
		return xyMultipleSeriesDataset.getSeriesCount() == 0;
	}

	public void setYTitle(String yTitle) {
		this.yTitle = yTitle;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
	}

	public XYMultipleSeriesDataset getDataset() {
		return xyMultipleSeriesDataset;
	}

	private void formatRenderer() {
		// Fix background color problem of margin in AChartEngine
		xyMultipleSeriesRenderer.setMarginsColor(Color.argb(0, 50, 50, 50));
		xyMultipleSeriesRenderer.setMargins(new int[] { 0,
				2 * textSize, textSize, textSize });

		xyMultipleSeriesRenderer.setLabelsTextSize(textSize);
		xyMultipleSeriesRenderer.setLegendTextSize(textSize);

		xyMultipleSeriesRenderer.setFitLegend(true);

		xyMultipleSeriesRenderer.setZoomButtonsVisible(false);
		xyMultipleSeriesRenderer.setZoomEnabled(false);
		xyMultipleSeriesRenderer.setPanEnabled(false);
		xyMultipleSeriesRenderer.setInScroll(true);

		// Setup X-axis and labels.
		xyMultipleSeriesRenderer.setXAxisMin(-1);
		xyMultipleSeriesRenderer.setXAxisMax(MAX_CATEGORIES_BAR_CHART + 2);
		xyMultipleSeriesRenderer.setXLabels(0);

		// Setup Y-axis and labels.
		xyMultipleSeriesRenderer.setYTitle(yTitle);
		xyMultipleSeriesRenderer.setYLabels(Math.min(4, maxYValue + 1));
		xyMultipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT);
		xyMultipleSeriesRenderer.setYLabelsPadding(5f);
		xyMultipleSeriesRenderer.setYLabelsVerticalPadding(-1
																   * textSize);
		xyMultipleSeriesRenderer.setYAxisMin(0);
		xyMultipleSeriesRenderer.setYAxisMax(maxYValue + 1);

		xyMultipleSeriesRenderer.setBarWidth(barWidth);
	}

	public XYMultipleSeriesRenderer getRenderer() {
		formatRenderer();
		return xyMultipleSeriesRenderer;
	}

	private SimpleSeriesRenderer createSimpleSeriesRenderer(int color) {
		SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();
		simpleSeriesRenderer.setColor(color);

		return simpleSeriesRenderer;
	}
}
