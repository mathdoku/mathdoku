package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;

import org.achartengine.GraphicalView;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A base fragment representing the statistics for a game or a grid size.
 */
public class StatisticsBaseFragment extends
		android.support.v4.app.Fragment {

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
		// Get default sizes for text
		mDefaultTextSize = getResources().getDimensionPixelSize(
				R.dimen.text_size_default);
		
		// Determine if a description of the statistic has to be shown below
		// each statistic
		mDisplayStatisticDescription = Preferences.getInstance(getActivity())
				.showStatisticsDescription();

		// Get inflater and return view
		mLayoutInflater = inflater;
		View rootView = inflater.inflate(R.layout.statistics_fragment,
				container, false);
		
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
	 * @param titleResId
	 *            Resource id for the title of this section.
	 * @param mMinGridSize
	 *            The grid size to be displayed as subtitle for the chart.
	 *            <b>Only specify for charts which relates to all grids of this
	 *            specific size. Use a value <= 0 if no subtitle should be
	 *            shown.</b>
	 * @param bodyResId
	 *            Resource id for the body text (explanation of this section).
	 * @param chart
	 *            The chart view.
	 */
	protected void addStatisticsSection(int titleResId, String subTitle,
			int bodyResId, GraphicalView chart, View extraDataView) {
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

		// Add extra data
		if (extraDataView != null) {
			LinearLayout linearLayout = ((LinearLayout) sectionView
					.findViewById(R.id.statistics_section_extra_data));
			linearLayout.setVisibility(View.VISIBLE);
			linearLayout.addView(extraDataView);
		}

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
}