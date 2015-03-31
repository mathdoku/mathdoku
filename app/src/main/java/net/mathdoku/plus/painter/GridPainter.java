package net.mathdoku.plus.painter;

import android.graphics.Paint;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class GridPainter extends BorderPainter {
    // The background for the grid.
    private final Paint mBackgroundPaint;

    public GridPainter() {
        mBorderPaint = new Paint();
        mBorderPaint.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void setTheme(Theme theme) {
        mBackgroundPaint.setColor(theme.getGridBackgroundColor());
    }

    @Override
    protected void setBorderSizes(boolean thin) {
        // Nothing to do for a grid.
    }

    @Override
    protected void setCellSize(float size) {
        // Nothing to do for a grid.
    }

    /**
     * Gets the paint for the background of the grid.
     *
     * @return The paint for the background of the grid.
     */
    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }
}