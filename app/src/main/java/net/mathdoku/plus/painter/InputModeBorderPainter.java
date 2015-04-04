package net.mathdoku.plus.painter;

import android.graphics.Paint;
import android.graphics.Paint.Style;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class InputModeBorderPainter extends BorderPainter {
    // Background of border
    private final Paint mNormalInputModeBorderPaint;
    private final Paint mMaybeInputModeBorderPaint;

    // Painter for text inside the border
    private final Paint mNormalTextPaint;
    private final Paint mHighlightedTextPaint;

    // Painter for the line dividing the segments in the border
    private final Paint mSegmentDividerPaint;

    // Painter for the swipe line
    private final Paint mSwipeLinePaint;

    public InputModeBorderPainter() {
        // Background painters for the border.
        mNormalInputModeBorderPaint = new Paint();
        mNormalInputModeBorderPaint.setStyle(Paint.Style.STROKE);

        mMaybeInputModeBorderPaint = new Paint();
        mMaybeInputModeBorderPaint.setStyle(Paint.Style.STROKE);

        // The digit painters
        mNormalTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNormalTextPaint.setFakeBoldText(true);
        mNormalTextPaint.setColor(0xFFFFFFFF);

        mHighlightedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightedTextPaint.setFakeBoldText(true);
        mHighlightedTextPaint.setColor(0xFF000000);

        mSegmentDividerPaint = new Paint();
        mSegmentDividerPaint.setStrokeWidth(2);
        mSegmentDividerPaint.setColor(0xFF000000);
        mSegmentDividerPaint.setStyle(Paint.Style.STROKE);

        mSwipeLinePaint = new Paint();
        mSwipeLinePaint.setAntiAlias(true);
        mSwipeLinePaint.setStrokeWidth(10);
        mSwipeLinePaint.setStyle(Style.STROKE);
        mSwipeLinePaint.setColor(0x80FFFF00);
    }

    @Override
    public void setTheme(Theme theme) {
        mNormalInputModeBorderPaint.setColor(theme.getHighlightedTextColorNormalInputMode());
        mNormalInputModeBorderPaint.setAlpha(200);

        mMaybeInputModeBorderPaint.setColor(theme.getHighlightedTextColorMaybeInputMode());
        mMaybeInputModeBorderPaint.setAlpha(200);

        mNormalTextPaint.setTypeface(theme.getTypeface());

        mHighlightedTextPaint.setTypeface(theme.getTypeface());

        mSwipeLinePaint.setPathEffect(theme.getPathEffect());
    }

    @Override
    protected void setBorderSizes(boolean thin) {
        throw new IllegalStateException("Method not implemented for class" + InputModeBorderPainter.class.getName());
    }

    protected void setCellSize(float cellSize) {
        float mBorderWidth = cellSize / 2;

        mNormalInputModeBorderPaint.setStrokeWidth(mBorderWidth);
        mMaybeInputModeBorderPaint.setStrokeWidth(mBorderWidth);

        int textSize = (int) (mBorderWidth * 0.8f);
        mNormalTextPaint.setTextSize(textSize);
        mHighlightedTextPaint.setTextSize(textSize);
    }

    /**
     * Get the paint for the border in the normal input mode.
     *
     * @return The paint for the border in the normal input mode.
     */
    public Paint getNormalInputModeBorderPaint() {
        return mNormalInputModeBorderPaint;
    }

    /**
     * Get the paint paint for the border in the maybe input mode.
     *
     * @return The paint paint for the border in the maybe input mode.
     */
    public Paint getMaybeInputModeBorderPaint() {
        return mMaybeInputModeBorderPaint;
    }

    /**
     * Get the paint for normal text inside the border.
     *
     * @return The paint for normal text inside the border.
     */
    public Paint getNormalTextPaint() {
        return mNormalTextPaint;
    }

    /**
     * Get the paint for the highlighted text inside the border.
     *
     * @return The paint for the highlighted digit inside the border.
     */
    public Paint getHighlightedTextPaint() {
        return mHighlightedTextPaint;
    }

    /**
     * Get the paint for the segment divider.
     *
     * @return The paint for the segment divider
     */
    public Paint getSegmentDivider() {
        return mSegmentDividerPaint;
    }

    /**
     * Get the paint for the swipe line.
     *
     * @return The paint for the swipe line
     */
    public Paint getSwipeLinePaint() {
        return mSwipeLinePaint;
    }
}