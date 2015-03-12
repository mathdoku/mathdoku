package net.mathdoku.plus.statistics;

public class CumulativeStatistics {
    // Minimum and maximum size of grids grouped in these statistics
    public int mMinGridSize;
    public int mMaxGridSize;

    // ***********************
    // Total grids per status
    // ***********************
    public int mCountStarted;
    public int mCountSolutionRevealed;
    public int mCountSolvedManually;
    public int mCountFinished;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CumulativeStatistics{");
        sb.append("mMinGridSize=")
                .append(mMinGridSize);
        sb.append(", mMaxGridSize=")
                .append(mMaxGridSize);
        sb.append(", mCountStarted=")
                .append(mCountStarted);
        sb.append(", mCountSolutionRevealed=")
                .append(mCountSolutionRevealed);
        sb.append(", mCountSolvedManually=")
                .append(mCountSolvedManually);
        sb.append(", mCountFinished=")
                .append(mCountFinished);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CumulativeStatistics)) {
            return false;
        }

        CumulativeStatistics that = (CumulativeStatistics) o;

        if (mCountFinished != that.mCountFinished) {
            return false;
        }
        if (mCountSolutionRevealed != that.mCountSolutionRevealed) {
            return false;
        }
        if (mCountSolvedManually != that.mCountSolvedManually) {
            return false;
        }
        if (mCountStarted != that.mCountStarted) {
            return false;
        }
        if (mMaxGridSize != that.mMaxGridSize) {
            return false;
        }
        if (mMinGridSize != that.mMinGridSize) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = mMinGridSize;
        result = 31 * result + mMaxGridSize;
        result = 31 * result + mCountStarted;
        result = 31 * result + mCountSolutionRevealed;
        result = 31 * result + mCountSolvedManually;
        result = 31 * result + mCountFinished;
        return result;
    }
}
