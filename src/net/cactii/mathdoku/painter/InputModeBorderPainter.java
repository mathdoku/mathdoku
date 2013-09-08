package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class InputModeBorderPainter extends BorderPainter {

	// Width of the border
	private float mBorderWidth;

	// Background of border
	private final Paint mNormalInputModeBorderPaint;
	private final Paint mMaybeInputModeBorderPaint;
	private final Paint mCopyInputModeBorderPaint;

	// Painter for text inside the border
	private final Paint mNormalTextPaint;
	private final Paint mHighlightedTextPaint;

	// Painter for the line dividing the segments in the border
	private final Paint mSegmentDividerPaint;

	// Painter for the swipe line
	private final Paint mSwipeLinePaint;

	/**
	 * Creates a new instance of {@link CellPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public InputModeBorderPainter(Painter painter) {
		super(painter);

		// Background painters for the border.
		mNormalInputModeBorderPaint = new Paint();
		mNormalInputModeBorderPaint.setStyle(Paint.Style.STROKE);

		mMaybeInputModeBorderPaint = new Paint();
		mMaybeInputModeBorderPaint.setStyle(Paint.Style.STROKE);

		mCopyInputModeBorderPaint = new Paint();
		mCopyInputModeBorderPaint.setStyle(Paint.Style.STROKE);
		mCopyInputModeBorderPaint.setColor(0xFFFF37EE);
		mCopyInputModeBorderPaint.setAlpha(200);

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
	public void setTheme(GridTheme theme) {
		mNormalInputModeBorderPaint.setColor(mPainter
				.getHighlightedTextColorNormalInputMode());
		mNormalInputModeBorderPaint.setAlpha(200);

		mMaybeInputModeBorderPaint.setColor(mPainter
				.getHighlightedTextColorMaybeInputMode());
		mMaybeInputModeBorderPaint.setAlpha(200);

		mNormalTextPaint.setTypeface(mPainter.getTypeface());

		mHighlightedTextPaint.setTypeface(mPainter.getTypeface());

		mSwipeLinePaint.setPathEffect(mPainter.getPathEffect());
	}

	@Override
	protected void setBorderSizes(boolean thin) {
	}

	@Override
	protected void setCellSize(float cellSize) {
		mBorderWidth = cellSize / 2;

		mNormalInputModeBorderPaint.setStrokeWidth(mBorderWidth);
		mMaybeInputModeBorderPaint.setStrokeWidth(mBorderWidth);
		mCopyInputModeBorderPaint.setStrokeWidth(mBorderWidth);

		int textSize = (int) (mBorderWidth * 0.8f);
		mNormalTextPaint.setTextSize(textSize);
		mHighlightedTextPaint.setTextSize(textSize);
	}

	/**
	 * Get the size of the border.
	 * 
	 * @return The size of the border.
	 */
	public float getBorderWidth() {
		return mBorderWidth;
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
	 * Get the paint paint for the border in the copy input mode.
	 * 
	 * @return The paint paint for the border in the copy input mode.
	 */
	public Paint getCopyBackgroundBorderPaint() {
		return mCopyInputModeBorderPaint;
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