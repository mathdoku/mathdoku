package net.mathdoku.plus.storage;

import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

/**
 * The data for the grid in a compound string of storage strings.
 */
public class SolvingAttemptStorage {
	private String[] mData;
	private int mDataIndex = -1;

	/**
	 * Creates a new {@link SolvingAttemptStorage} instance.
	 * 
	 * @param data
	 *            The data string to be set.
	 */
	public SolvingAttemptStorage(String data) {
		mData = (data == null ? null : data
				.split(SolvingAttemptDatabaseAdapter.EOL_DELIMITER));
		mDataIndex = -1;
	}

	/**
	 * Get the first line of data.
	 * 
	 * @return The first line of data. Null in case no line was found.
	 */
	public String getFirstLine() {
		mDataIndex = 0;
		return getNextLine();
	}

	/**
	 * Get the next line of data.
	 * 
	 * @return The next line of data. Null in case no line was found.
	 */
	public String getNextLine() {
		if (mData == null || mDataIndex >= mData.length) {
			return null;
		} else {
			return mData[mDataIndex++];
		}
	}
}
