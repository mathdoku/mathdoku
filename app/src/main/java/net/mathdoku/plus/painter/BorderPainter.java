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
     * Get the border paint.
     *
     * @return The paint for this border.
     */
    public Paint getBorderPaint() {
        return mBorderPaint;
    }
}
