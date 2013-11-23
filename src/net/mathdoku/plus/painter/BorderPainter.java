package net.mathdoku.plus.painter;

import android.graphics.Paint;

abstract class BorderPainter extends BasePainter {
	// Border sizes
	final static int BORDER_STROKE_HAIR_LINE = 0;
	final static int BORDER_STROKE_WIDTH_THIN = 1;
	final static int BORDER_STROKE_WIDTH_NORMAL = 2;
	final static int BORDER_STROKE_WIDTH_MEDIUM = 3;
	final static int BORDER_STROKE_WIDTH_THICK = 4;

	// Common properties for all derived painter classes
	Paint mBorderPaint;

	/**
	 * Creates a new instance of {@link BorderPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	BorderPainter(Painter painter) {
		super(painter);
	}

	/**
	 * Change the width of the border of the grid.
	 * 
	 * @param thin
	 *            True in case a small border needs to be set. False in case a
	 *            normal border should be used.
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
