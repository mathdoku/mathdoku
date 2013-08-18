package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class SwipeBorderPainter extends BorderPainter {

	// Width of the swipe border
	private float mBorderWidth;

	// Background of swipe border
	private final Paint mUserValueBackgroundBorderPaint;
	private final Paint mMaybeValueBackgroundBorderPaint;

	// Painter for digits inside the swipe border
	private final Paint mNormalDigitPaint;
	private final Paint mHighlightedDigitPaint;

	// Painter for the line dividing the segments in the swipe circle
	private final Paint mSwipeSegmentDividerPaint;

	// Painter for the swipe line
	private final Paint mSwipeLinePaint;

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
		mUserValueBackgroundBorderPaint.setStyle(Paint.Style.STROKE);

		mMaybeValueBackgroundBorderPaint = new Paint();
		mMaybeValueBackgroundBorderPaint.setStyle(Paint.Style.STROKE);

		// The digit painters
		mNormalDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNormalDigitPaint.setFakeBoldText(true);
		mNormalDigitPaint.setColor(0xFFFFFFFF);

		mHighlightedDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mHighlightedDigitPaint.setFakeBoldText(true);
		mHighlightedDigitPaint.setColor(0xFF000000);

		mSwipeSegmentDividerPaint = new Paint();
		mSwipeSegmentDividerPaint.setStrokeWidth(2);
		mSwipeSegmentDividerPaint.setColor(0xFF000000);
		mSwipeSegmentDividerPaint.setStyle(Paint.Style.STROKE);

		mSwipeLinePaint = new Paint();
		mSwipeLinePaint.setAntiAlias(true);
		mSwipeLinePaint.setStrokeWidth(10);
		mSwipeLinePaint.setStyle(Style.STROKE);
		mSwipeLinePaint.setColor(0x80FFFF00);
	}

	@Override
	public void setTheme(GridTheme theme) {
		mUserValueBackgroundBorderPaint.setColor(mPainter
				.getHighlightedTextColorNormalInputMode());
		mUserValueBackgroundBorderPaint.setAlpha(200);

		mMaybeValueBackgroundBorderPaint.setColor(mPainter
				.getHighlightedTextColorMaybeInputMode());
		mMaybeValueBackgroundBorderPaint.setAlpha(200);

		mNormalDigitPaint.setTypeface(mPainter.getTypeface());

		mHighlightedDigitPaint.setTypeface(mPainter.getTypeface());

		mSwipeLinePaint.setPathEffect(mPainter.getPathEffect());
	}

	@Override
	protected void setBorderSizes(boolean thin) {
	}

	@Override
	protected void setCellSize(float cellSize) {
		mBorderWidth = cellSize / 2;

		mUserValueBackgroundBorderPaint.setStrokeWidth(mBorderWidth);
		mMaybeValueBackgroundBorderPaint.setStrokeWidth(mBorderWidth);

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
	 * Get the paint for the background of the swipe border which is used to
	 * enter a user value.
	 * 
	 * @return The paint for the background of the swipe border which is used to
	 *         enter a user value.
	 */
	public Paint getUserValueBackgroundBorderPaint() {
		return mUserValueBackgroundBorderPaint;
	}

	/**
	 * Get the paint for the background of the swipe border which is used to
	 * enter a maybe value.
	 * 
	 * @return The paint for the background of the swipe border which is used to
	 *         enter a maybe value.
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
	 * Get the paint for the swipe segment divider.
	 * 
	 * @return The paint for the swipe segment divider
	 */
	public Paint getSwipeSegmentDivider() {
		return mSwipeSegmentDividerPaint;
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