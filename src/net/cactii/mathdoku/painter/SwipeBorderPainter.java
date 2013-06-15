package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class SwipeBorderPainter extends BorderPainter {

	// Width of the swipe border
	private float mBorderWidth;

	// Background of swipe border
	private Paint mUserValueBackgroundBorderPaint;
	private Paint mMaybeValueBackgroundBorderPaint;
	
	// Painter for digits inside the swipe border
	private Paint mNormalDigitPaint;
	private Paint mHighlightedDigitPaint;

	// Painter for the swipe line
	private Paint mSwipeLinePaint;
	
	// Offset from bottom for text inside swipe border
	private float mBottomOffset;

	/**
	 * Creates a new instance of {@link CellPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public SwipeBorderPainter(Painter painter) {
		super(painter);
		
		// Background painters of swipe border.
		mUserValueBackgroundBorderPaint = new Paint();
		mUserValueBackgroundBorderPaint.setStyle(Paint.Style.FILL);
		mMaybeValueBackgroundBorderPaint = new Paint();
		mMaybeValueBackgroundBorderPaint.setStyle(Paint.Style.FILL);
		
		// The digit painters
		mNormalDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNormalDigitPaint.setFakeBoldText(true);
		mHighlightedDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mHighlightedDigitPaint.setFakeBoldText(true);
		
		mSwipeLinePaint = new Paint();
		mSwipeLinePaint.setAntiAlias(true);
		mSwipeLinePaint.setStrokeWidth(10);
		mSwipeLinePaint.setStyle(Style.STROKE);
		mSwipeLinePaint.setColor(0x80FFFF00);
	}

	@Override
	public void setTheme(GridTheme theme) {
		mUserValueBackgroundBorderPaint.setColor(mPainter.getHighlightedTextColorNormalInputMode());
		mUserValueBackgroundBorderPaint.setAlpha(100);
		
		mMaybeValueBackgroundBorderPaint.setColor(mPainter.getHighlightedTextColorMaybeInputMode());
		mMaybeValueBackgroundBorderPaint.setAlpha(150);

		mNormalDigitPaint.setTypeface(mPainter.getTypeface());
		mNormalDigitPaint.setColor(0xFF7D7D7D);
		mHighlightedDigitPaint.setTypeface(mPainter.getTypeface());
		mHighlightedDigitPaint.setColor(0xFF000000);
		
		mSwipeLinePaint.setPathEffect(mPainter.getPathEffect());
	}

	@Override
	protected void setBorderSizes(boolean thin) {
	}

	@Override
	protected void setCellSize(float cellSize) {
		mBorderWidth = cellSize / 2;
		
		int textSize = (int) (mBorderWidth * 0.8f);
		mNormalDigitPaint.setTextSize(textSize);
		mHighlightedDigitPaint.setTextSize(textSize);
		mBottomOffset = (int) ((mBorderWidth - textSize) / 2);
	}

	/**
	 * Get the size of the swipe border.
	 * 
	 * @return The size of the swipe border.
	 */
	public float getBorderWidth() {
		return mBorderWidth;
	}

	/**
	 * Get the offset of text form the bottom of the swipe border.
	 * 
	 * @return The offset of text form the bottom of the swipe border.
	 */
	public float getBottomOffset() {
		return mBottomOffset;
	}

	/**
	 * Get the paint for the background of the swipe border which is used to enter a user value.
	 * 
	 * @return The paint for the background of the swipe border which is used to enter a user value.
	 */
	public Paint getUserValueBackgroundBorderPaint() {
		return mUserValueBackgroundBorderPaint;
	}

	/**
	 * Get the paint for the background of the swipe border which is used to enter a maybe value.
	 * 
	 * @return The paint for the background of the swipe border which is used to enter a maybe value.
	 */
	public Paint getMaybeValueBackgroundBorderPaint() {
		return mMaybeValueBackgroundBorderPaint;
	}

	/**
	 * Get the paint for the normal digits inside the swipe border.
	 * 
	 * @return The paint for the normal digits inside the swipe border.
	 */
	public Paint getNormalDigitPaint() {
		return mNormalDigitPaint;
	}

	/**
	 * Get the paint for the highlighted digit inside the swipe border.
	 * 
	 * @return The paint for the highlighted digit inside the swipe border.
	 */
	public Paint getHighlightedDigitPaint() {
		return mHighlightedDigitPaint;
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