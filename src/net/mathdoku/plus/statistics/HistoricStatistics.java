package net.mathdoku.plus.statistics;

import java.util.ArrayList;

import org.achartengine.model.XYSeries;

import android.database.Cursor;

/**
 * This class holds time related statistics for one property.
 */
public class HistoricStatistics {

	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.HistoricStatistics";

	// Historic statistics will be split in distinct series.
	public enum Series {
		UNFINISHED, SOLUTION_REVEALED, SOLVED
	}

	// Scales which can be applied on the values
	public enum Scale {
		NO_SCALE, SECONDS, MINUTES, HOURS, DAYS
	}

	// Columns in the DATA cursor
	public final static String DATA_COL_ID = "id";
	public final static String DATA_COL_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY = "elapsed_time_excluding_cheat_penalty";
	public final static String DATA_COL_CHEAT_PENALTY = "cheat_penalty";
	public final static String DATA_COL_SERIES = "series";

	// Internal structure to store data points retrieved from database
	private class DataPoint {
		public long mElapsedTimeExcludingCheatPenalty;
		public long mCheatPenalty;
		public Series mSeries;
	}

	// Storage for data points retrieved from database
	private final ArrayList<DataPoint> dataPoints;

	// Internal data structure to store data per series
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
		 * Adds a data point to the series.
		 * 
		 * @param dataPoint
		 *            The data point which has to be included in the series.
		 */
		public void addValue(DataPoint dataPoint) {
			long totalValue = dataPoint.mElapsedTimeExcludingCheatPenalty
					+ dataPoint.mCheatPenalty;
			mMinValue = (totalValue < mMinValue ? totalValue : mMinValue);
			mMaxValue = (totalValue > mMaxValue ? totalValue : mMaxValue);
			mSumValue += totalValue;
			mCount++;
		}

		/**
		 * Gets the minimum value found for this series.
		 * 
		 * @return The minimum value found for this series.
		 */
		public long getMinimum() {
			return mMinValue;
		}

		/**
		 * Gets the maximum value found for this series.
		 * 
		 * @return The maximum value found for this series.
		 */
		public long getMaximum() {
			return mMaxValue;
		}

		/**
		 * Gets the average value for this series.
		 * 
		 * @return The average value for this series.
		 */
		public long getAverage() {
			if (mCount == 0) {
				return 0;
			} else {
				return mSumValue / mCount;
			}
		}

		/**
		 * Gets the number of values used in this series.
		 * 
		 * @return The number of values used in this series.
		 */
		public long getCount() {
			return mCount;
		}
	}

	// Storage of the series.
	private final SeriesSummary mSolvedSeriesSummary;
	private final SeriesSummary mSolutionRevealedSeriesSummary;
	private final SeriesSummary mUnfinishedSeriesSummary;

	// Limit on XYSeries
	private final static int XY_SERIES_NOT_LIMITED = -1;
	private int mLimit;

	/**
	 * Creates a new instance of {@link HistoricStatistics}. Note that order of
	 * columns in cursors is defined.
	 * 
	 * @param dataPointsCursor
	 *            The database cursor holding the data to be transformed into
	 *            the historic statistics series.
	 */
	public HistoricStatistics(Cursor dataPointsCursor) {
		dataPoints = new ArrayList<DataPoint>();
		mSolvedSeriesSummary = new SeriesSummary();
		mSolutionRevealedSeriesSummary = new SeriesSummary();
		mUnfinishedSeriesSummary = new SeriesSummary();

		// Get historic data from cursor
		if (dataPointsCursor != null && dataPointsCursor.moveToFirst()) {
			do {
				// Fill new data point
				DataPoint dataPoint = new DataPoint();
				dataPoint.mElapsedTimeExcludingCheatPenalty = dataPointsCursor
						.getLong(dataPointsCursor
								.getColumnIndexOrThrow(DATA_COL_ELAPSED_TIME_EXCLUDING_CHEAT_PENALTY));
				dataPoint.mCheatPenalty = dataPointsCursor
						.getLong(dataPointsCursor
								.getColumnIndexOrThrow(DATA_COL_CHEAT_PENALTY));
				dataPoint.mSeries = Series.valueOf(dataPointsCursor
						.getString(dataPointsCursor
								.getColumnIndexOrThrow(DATA_COL_SERIES)));

				// Update summary for the series
				switch (dataPoint.mSeries) {
				case UNFINISHED:
					mUnfinishedSeriesSummary.addValue(dataPoint);
					break;
				case SOLUTION_REVEALED:
					mSolutionRevealedSeriesSummary.addValue(dataPoint);
					break;
				case SOLVED:
					mSolvedSeriesSummary.addValue(dataPoint);
					break;
				}

				// Add data point to the list
				dataPoints.add(dataPoint);
			} while (dataPointsCursor.moveToNext());

			mLimit = XY_SERIES_NOT_LIMITED;
		}
	}

	/**
	 * Checks whether the given series has been filled with at least one data
	 * point.
	 * 
	 * @param series
	 *            The series to be converted. Use null in case it needs to be
	 *            checked if at least one data point exists for any of the
	 *            series.
	 * @param includeElapsedTime
	 *            True in case the elapsed time should be included in the values
	 *            of the return series.
	 * @param includeCheatTime
	 *            True in case the cheat time should be included in the values
	 *            of the return series.
	 * @return True in case the series contain at least one data point.
	 */
	@SuppressWarnings("SameParameterValue")
	public boolean isXYSeriesUsed(Series series, boolean includeElapsedTime,
			boolean includeCheatTime) {
		// In case a limit is specified, only the last <limit> number of
		// data points are converted to the series.
		int start = getIndexFirstEntry();
		int index = 1;

		for (DataPoint dataPoint : dataPoints) {
			if (index >= start) {
				if (dataPoint.mSeries == series || series == null) {
					if ((includeElapsedTime && dataPoint.mElapsedTimeExcludingCheatPenalty > 0)
							|| (includeCheatTime && dataPoint.mCheatPenalty > 0)) {
						return true;
					}
				}
			}
			index++;
		}

		return false;
	}

	/**
	 * Converts the given series to a XYSeries object which can be processed by
	 * AChartEngine.
	 * 
	 * @param series
	 *            The series to be converted.
	 * @param title
	 *            The title to be used in the XYSeries.
	 * @param scale
	 *            The scaling factor which has to be applied when converting
	 *            values.
	 * @param includeElapsedTime
	 *            True in case the elapsed time should be included in the values
	 *            of the return series.
	 * @param includeCheatTime
	 *            True in case the cheat time should be included in the values
	 *            of the return series.
	 * @return A XYSeries object which can be processed by AChartEngine
	 */
	@SuppressWarnings("SameParameterValue")
	public XYSeries getXYSeries(Series series, String title, Scale scale,
			boolean includeElapsedTime, boolean includeCheatTime) {
		if (series == Series.SOLUTION_REVEALED) {
			throw new RuntimeException(
					"Method getXYSeries should not be used for the solution "
							+ "revealed series. Use getXYSeriesSolutionsRevealed "
							+ "instead.");
		}
		XYSeries xySeries = new XYSeries(title);

		double scaleFactor = getScaleFactor(scale);

		// In case a limit is specified, only the last <limit> number of
		// data points are converted to the series.
		int start = getIndexFirstEntry();
		int index = 1;

		for (DataPoint dataPoint : dataPoints) {
			if (index >= start) {
				double value = 0;
				if (dataPoint.mSeries == series) {
					// Get unscaled value
					value = (includeElapsedTime ? dataPoint.mElapsedTimeExcludingCheatPenalty
							: 0)
							+ (includeCheatTime ? dataPoint.mCheatPenalty : 0);

					// Scale value
					value /= scaleFactor;
				}
				xySeries.add(index, value);
			}
			index++;
		}

		return xySeries;
	}

	/**
	 * Converts the solution revealed series to a XYSeries object which can be
	 * processed by AChartEngine.
	 * 
	 * @param title
	 *            The title to be used in the XYSeries.
	 * @param maxY
	 *            The maximum Y value to be used for each game in which the
	 *            solution was revealed.
	 * @return A XYSeries object which can be processed by AChartEngine
	 */
	@SuppressWarnings("SameParameterValue")
	public XYSeries getXYSeriesSolutionRevealed(String title, double maxY,
			boolean includeElapsedTime, boolean includeCheatTime) {
		XYSeries xySeries = new XYSeries(title);
		if (includeElapsedTime == false && includeCheatTime == false) {
			// No data points will be included in series.
			return xySeries;
		}

		// In case a limit is specified, only the last <limit> number of
		// data points are converted to the series.
		int start = getIndexFirstEntry();
		int index = 1;

		// For games in which the solution is revealed the Y-value of the
		// games will always be equals to the maximum Y-value.
		for (DataPoint dataPoint : dataPoints) {
			if (index >= start) {
				double value = 0;
				if (dataPoint.mSeries == Series.SOLUTION_REVEALED) {
					if (includeElapsedTime) {
						value = (includeCheatTime ? maxY : Math.min(
								dataPoint.mElapsedTimeExcludingCheatPenalty,
								maxY));
					} else {
						value = Math.max(Math.min(maxY
								- dataPoint.mElapsedTimeExcludingCheatPenalty,
								dataPoint.mCheatPenalty), 0);
					}
				}
				xySeries.add(index, value);
			}
			index++;
		}

		return xySeries;
	}

	/**
	 * Gets a XYSeries object for the historic average of the given series.
	 * 
	 * @param series
	 *            The series to be converted.
	 * @param title
	 *            The title to be used in the XYSeries.
	 * @param scale
	 *            The scaling factor which has to be applied when converting
	 *            values.
	 * @return A XYSeries object which can be processed by AChartEngine
	 */
	@SuppressWarnings("SameParameterValue")
	public XYSeries getXYSeriesHistoricAverage(Series series, String title,
			Scale scale) {
		XYSeries xySeries = new XYSeries(title);

		double totalValue = 0;
		long countValue = 0;
		double scaleFactor = getScaleFactor(scale);

		// In case a limit is specified, only the last <limit> number of
		// data points are converted to the series.
		int start = getIndexFirstEntry();
		int index = 1;

		for (DataPoint dataPoint : dataPoints) {
			if (series == null || dataPoint.mSeries == series) {
				totalValue += dataPoint.mElapsedTimeExcludingCheatPenalty;
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
	 * Gets the number of index entries with respect to the limit set.
	 * 
	 * @return The number of index entries with respect to the limit set.
	 */
	public double getCountIndexEntries() {
		return Math.min(dataPoints.size(), mLimit);
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