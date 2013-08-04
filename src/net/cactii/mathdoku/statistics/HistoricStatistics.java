package net.cactii.mathdoku.statistics;

import java.util.ArrayList;

import org.achartengine.model.XYSeries;

import android.database.Cursor;

/**
 * This class holds time related statistics for one property.
 * 
 */
public class HistoricStatistics {

	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.HistoricStatistics";

	// Historic statistics will be splitted per serie.
	public enum Serie {
		UNFINISHED, SOLUTION_REVEALED, SOLVED
	};

	// Scales which can be applied on the values
	public enum Scale {
		NO_SCALE, SECONDS, MINUTES, HOURS, DAYS
	};

	// Columns in the DATA cursor
	public final static String DATA_COL_ID = "id";
	public final static String DATA_COL_VALUE = "value";
	public final static String DATA_COL_SERIES = "serie";

	// Internal structure to store data points retrieved from database
	private class DataPoint {
		public long mValue;
		public Serie mSerie;
	}

	// Storage for data points retrieved from database
	private ArrayList<DataPoint> dataPoints;

	// Internal data structure to store data per serie
	private class SeriesSummary {
		private long mMinValue;
		private long mMaxValue;
		private long mSumValue;
		private long mCount;

		/**
		 * Creates a new instance of {@link SeriesSummary}.
		 */
		public SeriesSummary() {
			mMinValue = Long.MAX_VALUE;
			mMaxValue = Long.MIN_VALUE;
			mCount = 0;
		}

		/**
		 * Update the summary with a new value which is added.
		 * 
		 * @param value
		 *            The value which has to be included in the summary.
		 */
		public void addValue(long value) {
			mMinValue = (value < mMinValue ? value : mMinValue);
			mMaxValue = (value > mMaxValue ? value : mMaxValue);
			mSumValue += value;
			mCount++;
		}

		/**
		 * Gets the minimum value found for this serie.
		 * 
		 * @return The minimum value found for this serie.
		 */
		public long getMinimum() {
			return mMinValue;
		}

		/**
		 * Gets the maximum value found for this serie.
		 * 
		 * @return The maximum value found for this serie.
		 */
		public long getMaximum() {
			return mMaxValue;
		}

		/**
		 * Gets the average value for this serie.
		 * 
		 * @return The average value for this serie.
		 */
		public long getAverage() {
			if (mCount == 0) {
				return 0;
			} else {
				return mSumValue / mCount;
			}
		}

		/**
		 * Gets the number of values used in this serie.
		 * 
		 * @return The number of values used in this serie.
		 */
		public long getCount() {
			return mCount;
		}
	}

	// Storage of the series.
	private SeriesSummary mAllSeriesSummary;
	private SeriesSummary mSolvedSeriesSummary;
	private SeriesSummary mSolutionRevealedSeriesSummary;
	private SeriesSummary mUnfinishedSeriesSummary;

	// Limit on XYSeries
	public final static int XYSERIES_NOT_LIMITED = -1;
	private int mLimit;

	/**
	 * Creates a new instance of {@link HistoricStatistics}. Note that order of
	 * columns in cursors is defined.
	 * 
	 * @param data
	 * @param summary
	 */
	public HistoricStatistics(Cursor data) {
		dataPoints = new ArrayList<DataPoint>();
		mAllSeriesSummary = new SeriesSummary();
		mSolvedSeriesSummary = new SeriesSummary();
		mSolutionRevealedSeriesSummary = new SeriesSummary();
		mUnfinishedSeriesSummary = new SeriesSummary();

		// Get historic data from cursor
		if (data != null && data.moveToFirst()) {
			do {
				// Fill new datapoint
				DataPoint dataPoint = new DataPoint();
				dataPoint.mValue = data.getLong(data
						.getColumnIndexOrThrow(DATA_COL_VALUE));
				dataPoint.mSerie = Serie.valueOf(data.getString(data
						.getColumnIndexOrThrow(DATA_COL_SERIES)));

				// Update summary for the series
				mAllSeriesSummary.addValue(dataPoint.mValue);
				switch (dataPoint.mSerie) {
				case UNFINISHED:
					mUnfinishedSeriesSummary.addValue(dataPoint.mValue);
					break;
				case SOLUTION_REVEALED:
					mSolutionRevealedSeriesSummary.addValue(dataPoint.mValue);
					break;
				case SOLVED:
					mSolvedSeriesSummary.addValue(dataPoint.mValue);
					break;
				}

				// Add data point to the list
				dataPoints.add(dataPoint);
			} while (data.moveToNext());

			mLimit = XYSERIES_NOT_LIMITED;
		}
	}

	/**
	 * Checks whether the given serie has been filled with at least one data
	 * point.
	 * 
	 * @param serie
	 *            The serie to be checked.
	 * @return True in case the serie contains at least one data point.
	 */
	public boolean isXYSeriesUsed(Serie serie) {
		if (serie == null) {
			return (mAllSeriesSummary.getCount() > 0);
		}

		switch (serie) {
		case UNFINISHED:
			return (mUnfinishedSeriesSummary.getCount() > 0);
		case SOLUTION_REVEALED:
			return (mSolutionRevealedSeriesSummary.getCount() > 0);
		case SOLVED:
			return (mSolvedSeriesSummary.getCount() > 0);
		}
		return false;
	}

	/**
	 * Converts the given serie to a XYSerie object which can be processed by
	 * AChartEngine.
	 * 
	 * @param serie
	 *            The serie to be converted.
	 * @param title
	 *            The title to be used in the XYSeries.
	 * @param scale
	 *            The scaling factor which has to be applied when converting
	 *            values.
	 * 
	 * @return A XYSerie object which can be processed by AChartEngine
	 */
	public XYSeries getXYSeries(Serie serie, String title, Scale scale) {
		if (serie == Serie.SOLUTION_REVEALED) {
			throw new RuntimeException(
					"Method getXYSeries should not be used for the solution "
							+ "revealed series. Use getXYSeriesSolutionsRevelead "
							+ "instead.");
		}
		XYSeries xySeries = new XYSeries(title);

		double scaleFactor = getScaleFactor(scale);

		// In case a limit is specified, only the last <limit> number of
		// datapoints are converted to the series.
		int start = getIndexFirstEntry();
		int index = 1;

		for (DataPoint dataPoint : dataPoints) {
			if (index >= start) {
				xySeries.add(index,
						(dataPoint.mSerie == serie ? (dataPoint.mValue)
								/ scaleFactor : 0));
			}
			index++;
		}

		return xySeries;
	}

	/**
	 * Converts the solution revealed series to a XYSerie object which can be
	 * processed by AChartEngine.
	 * 
	 * @param title
	 *            The title to be used in the XYSeries.
	 * @param maxY
	 *            The maximum Y value to be used for each game in which the
	 *            solution was revealed.
	 * 
	 * @return A XYSerie object which can be processed by AChartEngine
	 */
	public XYSeries getXYSeriesSolutionRevealed(String title, double maxY) {
		XYSeries xySeries = new XYSeries(title);

		// In case a limit is specified, only the last <limit> number of
		// datapoints are converted to the series.
		int start = getIndexFirstEntry();
		int index = 1;

		// For games in which the solution is revealed the Y-value of the
		// games will always be equals to the maximum Y-value.
		for (DataPoint dataPoint : dataPoints) {
			if (index >= start) {
				xySeries.add(
						index,
						(dataPoint.mSerie == Serie.SOLUTION_REVEALED ? maxY : 0));
			}
			index++;
		}

		return xySeries;
	}

	/**
	 * Gets a XYSerie object for the historic average of the given serie.
	 * 
	 * @param serie
	 *            The serie to be converted.
	 * @param title
	 *            The title to be used in the XYSeries.
	 * @param scale
	 *            The scaling factor which has to be applied when converting
	 *            values.
	 * 
	 * @return A XYSerie object which can be processed by AChartEngine
	 */
	public XYSeries getXYSeriesHistoricAverage(Serie serie, String title,
			Scale scale) {
		XYSeries xySeries = new XYSeries(title);

		double totalValue = 0;
		long countValue = 0;
		double scaleFactor = getScaleFactor(scale);

		// In case a limit is specified, only the last <limit> number of
		// datapoints are converted to the series.
		int start = getIndexFirstEntry();
		int index = 1;

		for (DataPoint dataPoint : dataPoints) {
			if (serie == null || dataPoint.mSerie == serie) {
				totalValue += dataPoint.mValue;
				countValue++;
			}
			if (countValue > 0 && index >= start) {
				xySeries.add(index, totalValue / countValue / scaleFactor);
			}
			index++;
		}
		return xySeries;
	}

	/**
	 * Gets the maximum X value (e.g. the number of games to be displayed).
	 * 
	 * @return The maximum X value to be used for displaying the historic
	 *         statistics.
	 */
	public double getIndexLastEntry() {
		return dataPoints.size();
	}

	/**
	 * Gets the maximum Y values to be used for series. Y-values of grid in
	 * which the solution was revealed will not be taken into account unless no
	 * grids have been solved or unfinished.
	 * 
	 * @return The maximum Y-value to be used for displaying the historic
	 *         statistics.
	 */
	public double getMaxY(Scale scale) {
		double scaleFactor = getScaleFactor(scale);

		if (mSolvedSeriesSummary.getCount() > 0
				|| mUnfinishedSeriesSummary.getCount() > 0) {
			return (Math.max(mSolvedSeriesSummary.getMaximum(),
					mUnfinishedSeriesSummary.getMaximum()) / scaleFactor);
		} else {
			return mSolutionRevealedSeriesSummary.getMaximum() / scaleFactor;
		}
	}

	/**
	 * Converts a given scale to a scaleFactor which can be applied on the value
	 * in the data points.
	 * 
	 * @param scale
	 *            The scale to be converted.
	 * @return The scale factor associated with the given scale.
	 */
	private double getScaleFactor(Scale scale) {
		double scaleFactor;
		switch (scale) {
		case SECONDS:
			scaleFactor = 1000;
			break;
		case MINUTES:
			scaleFactor = 60 * 1000;
			break;
		case HOURS:
			scaleFactor = 60 * 60 * 1000;
			break;
		case DAYS:
			scaleFactor = 24 * 60 * 60 * 1000;
			break;
		case NO_SCALE:
			// fall through
		default:
			scaleFactor = 1;
			break;
		}
		return scaleFactor;
	}

	/**
	 * Get the slowest time in which a game was solved.
	 * 
	 * @return The slowest time in which a game was solved.
	 */
	public long getSolvedSlowest() {
		return (mSolvedSeriesSummary.getCount() > 0 ? mSolvedSeriesSummary
				.getMaximum() : 0);
	}

	/**
	 * Get the fastest time in which a game was solved.
	 * 
	 * @return The fastest time in which a game was solved.
	 */
	public long getSolvedFastest() {
		return (mSolvedSeriesSummary.getCount() > 0 ? mSolvedSeriesSummary
				.getMinimum() : 0);
	}

	/**
	 * Get the average time in which game have been solved.
	 * 
	 * @return The average time in which game have been solved.
	 */
	public long getSolvedAverage() {
		return (mSolvedSeriesSummary.getCount() > 0 ? mSolvedSeriesSummary
				.getAverage() : 0);
	}

	/**
	 * Set a limit on the number of entries returned in the XYSeries.
	 * 
	 * @param limit
	 *            The maximum number of entries to return.
	 */
	public void setLimit(int limit) {
		mLimit = limit;
	}

	/**
	 * Get the index number of the first entry in the series to be returned.
	 * 
	 * @return The index number of the first entry in the series to be returned.
	 */
	public int getIndexFirstEntry() {
		return Math.max(dataPoints.size() - mLimit, 1);
	}
}