package net.mathdoku.plus.painter;

public class PagerTabStripPainter extends BasePainter {

    private final int mTextColor;
    private final int mBackgroundColor;

    public PagerTabStripPainter(Painter painter) {
        super(painter);

        mTextColor = 0xFFFFFFFF;
        mBackgroundColor = mPainter.getButtonBackgroundColor();
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