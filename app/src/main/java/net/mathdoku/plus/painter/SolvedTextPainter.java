package net.mathdoku.plus.painter;

import android.graphics.Typeface;

public class SolvedTextPainter extends BasePainter {
    private final int mTextColor;
    private final int mBackgroundColor;
    private final Typeface typeface;

    public SolvedTextPainter() {
        mTextColor = 0xFF002F00;
        mBackgroundColor = 0xD0DECA1E;
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
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

    public Typeface getTypeface() {
        return typeface;
    }
}