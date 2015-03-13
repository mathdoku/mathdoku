package net.mathdoku.plus.painter;

public class SolvedTextPainter extends BasePainter {

    private final int mTextColor;
    private final int mBackgroundColor;

    public SolvedTextPainter(Painter painter) {
        super(painter);

        mTextColor = 0xFF002F00;
        mBackgroundColor = 0xD0DECA1E;
    }

    /**
     * Get the text color for the pager tab strip.
     *
     * @return The text color the pager tab strip.
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Get the background color for the pager tab strip.
     *
     * @return The background color the pager tab strip.
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }
}