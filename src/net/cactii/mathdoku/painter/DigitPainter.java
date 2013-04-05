package net.cactii.mathdoku.painter;

import android.graphics.Paint;

abstract class DigitPainter extends BasePainter {

	// Painters for the different input modes
	protected Paint mTextPaintNormalInputMode;
	protected Paint mTextPaintMaybeInputMode;

	// Offsets (top, left) of the region in which the value will be painted.
	protected float mLeftOffset;
	protected float mTopOffset;

	/**
	 * Creates a new instance of {@link DigitPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public DigitPainter(Painter painter) {
		super(painter);
	}

	/**
	 * Gets the paint for the text in normal input mode.
	 * 
	 * @return
	 */
	public Paint getTextPaintNormalInputMode() {
		return mTextPaintNormalInputMode;
	}

	/**
	 * Gets the paint for the text in maybe input mode.
	 * 
	 * @return
	 */
	public Paint getTextPaintMaybeInputMode() {
		return mTextPaintMaybeInputMode;
	}

	/**
	 * Gets the horizontal (left) offset for the text inside the cell.
	 * 
	 * @return
	 */
	public float getLeftOffset() {
		return mLeftOffset;
	}

	/**
	 * Gets the vertical (top) offset for the text inside the cell.
	 * 
	 * @return
	 */
	public float getTopOffset() {
		return mTopOffset;
	}
}