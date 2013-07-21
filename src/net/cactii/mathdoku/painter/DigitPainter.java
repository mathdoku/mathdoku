package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.DigitPainterMode;
import android.graphics.Paint;

abstract class DigitPainter extends BasePainter {

	// Painters for the different input modes
	protected Paint mTextPaintNormalInputMode;
	protected Paint mTextPaintMaybeInputMode;
	
	protected DigitPainterMode mDigitPainterMode; 
	
	// Offsets (bottom, left) of the region in which the value will be painted.
	protected float mLeftOffset;
	protected float mBottomOffset;

	/**
	 * Creates a new instance of {@link DigitPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public DigitPainter(Painter painter) {
		super(painter);
		setColorMode(DigitPainterMode.INPUT_MODE_BASED);
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
	public float getBottomOffset() {
		return mBottomOffset;
	}

	/**
	 * Set the color mode of the digit painter.
	 * 
	 * @param distinctColors
	 *            True in case distinct colors should be used dependent on the
	 *            input mode. False in case a monochrome color should be used.
	 */
	public void setColorMode(DigitPainterMode digitPainterMode) {
		mDigitPainterMode = digitPainterMode;
	}
}