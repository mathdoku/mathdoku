package net.mathdoku.plus.painter;

import android.graphics.Paint;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class CagePainter extends BorderPainter {
    // Border for a cage which is selected
    private final Paint mBorderSelectedPaint;

    // Border for a cage which is not selected but which is having bad maths
    private final Paint mBorderBadMathPaint;

    // Border for a cage which is selected and is having bad maths
    private final Paint mBorderSelectedBadMathPaint;

    private final Paint mTextPaint;
    private float mTextLeftOffset;
    private float mTextBottomOffset;

    public CagePainter() {
        // Set border for a cage which is not selected and which does not have bad maths
        mBorderPaint = new Paint();
        mBorderSelectedPaint = new Paint();
        mBorderBadMathPaint = new Paint();
        mBorderSelectedBadMathPaint = new Paint();
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(14);
    }

    @Override
    public void setTheme(Theme theme) {
        mTextPaint.setColor(theme.getDefaultTextColor());
        mTextPaint.setTypeface(theme.getTypeface());

        mBorderPaint.setColor(theme.getDefaultCageBorderColor());
        mBorderPaint.setAntiAlias(theme.getBorderAntiAlias());

        mBorderBadMathPaint.setColor(theme.getBadMathCageBorderColor());
        mBorderBadMathPaint.setAntiAlias(theme.getBorderAntiAlias());

        mBorderSelectedPaint.setColor(theme.getSelectedCageBorderColor());
        mBorderSelectedPaint.setAntiAlias(theme.getBorderAntiAlias());

        mBorderSelectedBadMathPaint.setColor(theme.getSelectedBadMathCageBorderColor());
        mBorderSelectedBadMathPaint.setAntiAlias(theme.getBorderAntiAlias());
    }

    protected void setBorderSizes(boolean thin) {
        if (thin) {
            mBorderPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
            mBorderSelectedPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_MEDIUM);
            mBorderBadMathPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
            mBorderSelectedBadMathPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_MEDIUM);
        } else {
            mBorderPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
            mBorderSelectedPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THICK);
            mBorderBadMathPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
            mBorderSelectedBadMathPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THICK);
        }
    }

    protected void setCellSize(float size) {
        // Text size of cage text is 1/3 of cell size
        int cageTextSize = (int) (size / 3);

        mTextPaint.setTextSize(cageTextSize);
        mTextLeftOffset = 2;
        mTextBottomOffset = cageTextSize;
    }

    /**
     * Gets the border paint for a selected cage.
     *
     * @return The border paint for a selected cage.
     */
    public Paint getBorderSelectedPaint() {
        return mBorderSelectedPaint;
    }

    /**
     * Gets the border paint for cage which is not selected and has bas maths.
     *
     * @return The border paint for cage which is not selected and has bas maths.
     */
    public Paint getBorderBadMathPaint() {
        return mBorderBadMathPaint;
    }

    /**
     * Gets the border paint for the selected cage which has bas maths.
     *
     * @return The border paint for the selected cage which has bas maths.
     */
    public Paint getBorderSelectedBadMathPaint() {
        return mBorderSelectedBadMathPaint;
    }

    /**
     * Gets the text paint for the cage result.
     *
     * @return The text paint for the cage result.
     */
    public Paint getTextPaint() {
        return mTextPaint;
    }

    /**
     * Gets the horizontal (left) offset for the cage result inside a cell.
     *
     * @return The horizontal (left) offset for the cage result inside a cell.
     */
    public float getTextLeftOffset() {
        return mTextLeftOffset;
    }

    /**
     * Gets the vertical (top) offset for the cage result inside a cell.
     *
     * @return The vertical (top) offset for the cage result inside a cell.
     */
    public float getTextBottomOffset() {
        return mTextBottomOffset;
    }
}