package net.mathdoku.plus.archive.ui;

import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

public class PieChartSeries {
	private final DefaultRenderer renderer;
	private CategorySeries categorySeries;

	public PieChartSeries(int mDefaultTextSize) {
		categorySeries = new CategorySeries("");

		renderer = createDefaultRenderer(mDefaultTextSize);
	}

	public void addCategory(String name, int partValue, int totalValue, int color) {
		if (partValue > 0) {
			categorySeries.add(name + " (" + partValue +
									   ")",
							   (double) partValue / totalValue);
			renderer.addSeriesRenderer(createSimpleSeriesRenderer(color));
		}
	}

	private DefaultRenderer createDefaultRenderer(int defaultTextSize) {
		DefaultRenderer defaultRenderer = new DefaultRenderer();

		defaultRenderer.setShowLabels(false);
		defaultRenderer.setShowLegend(true);
		defaultRenderer.setLegendTextSize(defaultTextSize);
		defaultRenderer.setFitLegend(true);
		defaultRenderer.setMargins(new int[]{0, defaultTextSize, defaultTextSize, defaultTextSize});

		defaultRenderer.setZoomButtonsVisible(false);
		defaultRenderer.setZoomEnabled(false);
		defaultRenderer.setPanEnabled(false);
		defaultRenderer.setInScroll(true);

		return defaultRenderer;
	}

	private SimpleSeriesRenderer createSimpleSeriesRenderer(int color) {
		SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();
		simpleSeriesRenderer.setColor(color);

		return simpleSeriesRenderer;
	}

	public CategorySeries getCategorySeries() {
		return categorySeries;
	}

	public DefaultRenderer getRenderer() {
		return renderer;
	}
}
