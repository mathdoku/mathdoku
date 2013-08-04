package net.cactii.mathdoku.storage.database;

public class SolvingAttemptData extends SolvingAttempt {
	// Revision used to save the data
	public int mSavedWithRevision;

	// Data
	private String[] mData;
	private int mDataIndex = -1;

	/**
	 * Set the data of the solving attempt.
	 * 
	 * @param data
	 *            The data string to be set.
	 */
	public void setData(String data) {
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
