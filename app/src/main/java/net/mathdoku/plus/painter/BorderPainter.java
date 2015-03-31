package net.mathdoku.plus.painter;

import android.graphics.Paint;

abstract class BorderPainter extends BasePainter {
    // Border sizes
    static final int BORDER_STROKE_HAIR_LINE = 0;
    static final int BORDER_STROKE_WIDTH_THIN = 1;
    static final int BORDER_STROKE_WIDTH_NORMAL = 2;
    static final int BORDER_STROKE_WIDTH_MEDIUM = 3;
    static final int BORDER_STROKE_WIDTH_THICK = 4;

    // Common properties for all derived painter classes
    Paint mBorderPaint;

    /**
     * Change the width of the border of the grid.
     *
     * @param thin
     *         True in case a small border needs to be set. False in case a normal border should be used.
     */
    protected abstract void setBorderSizes(boolean thin);

    /**
     * Get the border paint.
     *
     * @return The paint for this border.
     */
    public Paint getBorderPaint() {
        return mBorderPaint;
    }
}
