package net.cactii.mathdoku.painter;

import android.graphics.Paint;

abstract class BorderPainter extends BasePainter {
	// Border sizes
	protected final static int BORDER_STROKE_HAIR_LINE = 0;
	protected final static int BORDER_STROKE_WIDTH_THIN = 1;
	protected final static int BORDER_STROKE_WIDTH_NORMAL = 2;
	protected final static int BORDER_STROKE_WIDTH_MEDIUM = 3;
	protected final static int BORDER_STROKE_WIDTH_THICK = 4;

	// Common properties for all derived painter classes
	protected Paint mBorderPaint;

	/**
	 * Creates a new instance of {@link BorderPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public BorderPainter(Painter painter) {
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
