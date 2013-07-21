package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;

import org.achartengine.GraphicalView;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * A base fragment representing the statistics for a game or a grid size.
 */
public class StatisticsBaseFragment extends android.support.v4.app.Fragment {

	protected LinearLayout mChartsLayout;

	// Database adapter for the statistics data
	StatisticsDatabaseAdapter mStatisticsDatabaseAdapter;

	// Text size for body text
	protected int mDefaultTextSize;

	// The inflater for this activity.
	protected LayoutInflater mLayoutInflater;

	private boolean mDisplayStatisticDescription;

	// Green colors will be used at things which are positive
	protected static final int chartGreen1 = 0xFF80FF00;
	protected static final int chartGreen2 = 0xFF59B200;

	// Grey colors will be used at things which are neutral
	protected static final int chartGrey1 = 0xFFD4D4D4;
	protected static final int chartSignal1 = 0xFFFF00FF;
	protected static final int chartSignal2 = 0xFF8000FF;
	protected static final int chartSignal3 = 0xFF0000FF;

	// Green colors will be used at things which are negative
	protected static final int chartRed1 = 0xFFFF0000;
	protected static final int chartRed2 = 0xFFFF3300;
	protected static final int chartRed3 = 0xFFB22400;
	protected static final int chartRed4 = 0xFFFECCBF;
	protected static final int chartRed5 = 0xFFFE9980;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return onCreateView(inflater, R.layout.statistics_fragment, container,
				savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, int layout,
			ViewGroup container, Bundle savedInstanceState) {
		// Get default sizes for text
		mDefaultTextSize = getResources().getDimensionPixelSize(
				R.dimen.text_size_default);

		// Chart description will be displayed by default.
		mDisplayStatisticDescription = true;

		// Get inflater and return view
		mLayoutInflater = inflater;
		View rootView = inflater.inflate(layout, container, false);

		return rootView;
	}

	/**
	 * Creates a new simple series renderer for the given color.
	 * 
	 * @param color
	 *            The color for the new simple series renderer.
	 * @return
	 */
	protected SimpleSeriesRenderer createSimpleSeriesRenderer(int color) {
		SimpleSeriesRenderer simpleSeriesRenderer = new SimpleSeriesRenderer();
		simpleSeriesRenderer.setColor(color);

		return simpleSeriesRenderer;
	}

	/**
	 * Add a statistics section to the activity.
	 * 
	 * @param tag
	 *            The tag which can be used to identify the view.
	 * @param title
	 *            The title of this section. Null in case no title has to be
	 *            displayed.
	 * @param subTitle
	 *            The subtitle of this section. Null in case no subtitle has to
	 *            be displayed.
	 * @param explanation
	 *            The explanatory text of this section which will be displayed
	 *            with respect to settings. Null in case explanation is never
	 *            available.
	 * @param chart
	 *            The chart view. Null in case no chart has to be displayed.
	 * @param extraDataView
	 *            An additional view which has to be displayed between chart and
	 *            explanation.
	 */
	protected void addStatisticsSection(String tag, String title,
			GraphicalView chart, View extraDataView, String explanation) {
		// Inflate a new view for this statistics section
		View sectionView = mLayoutInflater.inflate(R.layout.statistics_section,
				null);

		// Add the tag to the view.
		if (tag != null) {
			sectionView.setTag(tag);
		}

		// Set title. The chart title of achartengine is not used.
		if (title != null && title.isEmpty() == false) {
			TextView textView = (TextView) sectionView
					.findViewById(R.id.statistics_section_title);
			if (textView != null) {
				textView.setText(title);
				textView.setVisibility(View.VISIBLE);
			}
		}

		// Add chart
		if (chart != null) {
			LinearLayout linearLayout = (LinearLayout) sectionView
					.findViewById(R.id.statistics_section_chart);
			if (linearLayout != null) {
				linearLayout.addView(chart);
				linearLayout.setVisibility(View.VISIBLE);
			}
		}

		// Add extra data
		if (extraDataView != null) {
			LinearLayout linearLayout = ((LinearLayout) sectionView
					.findViewById(R.id.statistics_section_extra_data));
			if (linearLayout != null) {
				linearLayout.setVisibility(View.VISIBLE);
				linearLayout.addView(extraDataView);
			}
		}

		// Add body text for explaining the chart
		if (explanation != null && explanation.isEmpty() == false
				&& mDisplayStatisticDescription) {
			TextView textView = (TextView) sectionView
					.findViewById(R.id.statistics_section_explanation);
			if (textView != null) {
				textView.setText(explanation);
				textView.setVisibility(View.VISIBLE);
			}
		}

		// Add the section to the general charts layout
		mChartsLayout.addView(sectionView);
	}

	/**
	 * Creates a new row in a data table consisting of two columns (label and
	 * value).
	 * 
	 * @param tableLayoutParams
	 *            The layout parameters for the table.
	 * @param label
	 *            The label (required) for the row
	 * @param value
	 *            The value (optional) for the row
	 * @return The table row with fields for label and optionally the value.
	 */
	protected TableRow createDataTableRow(
			TableLayout.LayoutParams tableLayoutParams, String label,
			String value) {
		// Create new TableRow
		TableRow tableRow = new TableRow(getActivity());
		tableRow.setLayoutParams(tableLayoutParams);

		// Set layout parameters for fields in the row
		TableRow.LayoutParams tableRowLayoutParams = new TableRow.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		// Add label to row
		TextView textViewLabel = new TextView(getActivity());
		textViewLabel.setLayoutParams(tableRowLayoutParams);
		textViewLabel.setText(label);
		tableRow.addView(textViewLabel);

		// Add value to row
		if (value != null) {
			TextView textViewValue = new TextView(getActivity());
			textViewValue.setLayoutParams(tableRowLayoutParams);
			textViewValue.setText(value);
			tableRow.addView(textViewValue);
		}

		return tableRow;
	}

	/**
	 * Sets whether the chart descriptions have to be displayed.
	 * 
	 * @param display
	 *            True in case the chart descriptions have to be displayed.
	 */
	protected void setDisplayChartDescription(boolean display) {
		mDisplayStatisticDescription = display;
	}
}