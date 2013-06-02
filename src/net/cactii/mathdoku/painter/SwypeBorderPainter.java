package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class SwypeBorderPainter extends BorderPainter {

	// Width of the swype border
	private float mBorderWidth;

	// Background of swype border
	private Paint mUserValueBackgroundBorderPaint;
	private Paint mMaybeValueBackgroundBorderPaint;
	
	// Painter for digits inside the swype border
	private Paint mDigitPaint;

	// Painter for the swype line
	private Paint mSwypeLinePaint;
	
	// Offset from bottom for text inside swype border
	private float mBottomOffset;

	/**
	 * Creates a new instance of {@link CellPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public SwypeBorderPainter(Painter painter) {
		super(painter);
		
		// Background painters of swype border.
		mUserValueBackgroundBorderPaint = new Paint();
		mUserValueBackgroundBorderPaint.setStyle(Paint.Style.FILL);
		mMaybeValueBackgroundBorderPaint = new Paint();
		mMaybeValueBackgroundBorderPaint.setStyle(Paint.Style.FILL);
		
		// The digit painter
		mDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mDigitPaint.setFakeBoldText(true);
		
		mSwypeLinePaint = new Paint();
		mSwypeLinePaint.setAntiAlias(true);
		mSwypeLinePaint.setStrokeWidth(10);
		mSwypeLinePaint.setStyle(Style.STROKE);
		mSwypeLinePaint.setColor(0x80FFFF00);
	}

	@Override
	public void setTheme(GridTheme theme) {
		mUserValueBackgroundBorderPaint.setColor(mPainter.getHighlightedTextColorNormalInputMode());
		mUserValueBackgroundBorderPaint.setAlpha(100);
		
		mMaybeValueBackgroundBorderPaint.setColor(mPainter.getHighlightedTextColorMaybeInputMode());
		mMaybeValueBackgroundBorderPaint.setAlpha(150);

		mDigitPaint.setTypeface(mPainter.getTypeface());
		mDigitPaint.setColor(mPainter.getDefaultTextColor());
		
		mSwypeLinePaint.setPathEffect(mPainter.getPathEffect());
	}

	@Override
	protected void setBorderSizes(boolean thin) {
	}

	@Override
	protected void setCellSize(float cellSize) {
		mBorderWidth = cellSize / 2;
		
		int textSize = (int) (mBorderWidth * 0.8f);
		mDigitPaint.setTextSize(textSize);
		mBottomOffset = (int) ((mBorderWidth - textSize) / 2);
	}

	/**
	 * Get the size of the swype border.
	 * 
	 * @return The size of the swype border.
	 */
	public float getBorderWidth() {
		return mBorderWidth;
	}

	/**
	 * Get the offset of text form the bottom of the swype border.
	 * 
	 * @return The offset of text form the bottom of the swype border.
	 */
	public float getBottomOffset() {
		return mBottomOffset;
	}

	/**
	 * Get the paint for the background of the swype border which is used to enter a user value.
	 * 
	 * @return The paint for the background of the swype border which is used to enter a user value.
	 */
	public Paint getUserValueBackgroundBorderPaint() {
		return mUserValueBackgroundBorderPaint;
	}

	/**
	 * Get the paint for the background of the swype border which is used to enter a maybe value.
	 * 
	 * @return The paint for the background of the swype border which is used to enter a maybe value.
	 */
	public Paint getMaybeValueBackgroundBorderPaint() {
		return mMaybeValueBackgroundBorderPaint;
	}

	/**
	 * Get the paint for the digits inside the swype border.
	 * 
	 * @return The paint for the digits inside the swype border.
	 */
	public Paint getDigitPaint() {
		return mDigitPaint;
	}

	/**
	 * Get the paint for the swype line.
	 * 
	 * @return The paint for the swype line
	 */
	public Paint getSwypeLinePaint() {
		return mSwypeLinePaint;
	}
}