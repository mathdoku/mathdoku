package net.mathdoku.plus.storage;

import net.mathdoku.plus.storage.selector.StorageDelimiter;

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
     *         The data string to be set.
     */
    public SolvingAttemptStorage(String data) {
        mData = data == null ? null : data.split(StorageDelimiter.EOL_DELIMITER);
        mDataIndex = -1;
    }

    /**
     * Get the next line of data.
     *
     * @return The next line of data. Null in case no line was found.
     */
    public String getNextLine() {
        if (mData == null || mDataIndex + 1 >= mData.length) {
            // Set data index beyond end of mData so method getLine does not
            // return a line anymore.
            mDataIndex = mData.length;
            return null;
        } else {
            return mData[++mDataIndex];
        }
    }

    /**
     * Get the current line of data.
     *
     * @return The current line of data. Null in case no line was found.
     */
    public String getLine() {
        if (mData == null || mDataIndex < 0 || mDataIndex >= mData.length) {
            return null;
        } else {
            // Do not increase the current line pointer.
            return mData[mDataIndex];
        }
    }

    public boolean hasNextLine() {
        return mDataIndex + 1 < mData.length;
    }
}
