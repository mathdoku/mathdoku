package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;

public class CagePainter extends BorderPainter {

	private Paint mBorderSelectedPaint;
	private Paint mBorderBadMathPaint;
	private Paint mBorderSelectedBadMathPaint;

	private Paint mTextPaint;
	private float mTextLeftOffset;
	private float mTextTopOffset;

	/**
	 * Creates a new instance of {@link CagePainter}.
	 * 
	 * @param painter The global container for all painters.
	 */
	public CagePainter(Painter painter) {
		super(painter);

		mBorderPaint = new Paint();
		mBorderSelectedPaint = new Paint();
		mBorderBadMathPaint = new Paint();
		mBorderSelectedBadMathPaint = new Paint();
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize(14);
	}

	@Override
	public void setTheme(GridTheme theme) {
		// Set text paint
		switch (theme) {
		case CARVED:
			mTextPaint.setColor(0xFF0000A0);
			mTextPaint.setTypeface(mPainter.getTypeface());
			break;
		case NEWSPAPER:
			mTextPaint.setColor(0xFF0000A0);
			mTextPaint.setTypeface(mPainter.getTypeface());
			break;
		case DARK:
			mTextPaint.setColor(0xFFFFFFC0);
			mTextPaint.setTypeface(mPainter.getTypeface());
			break;
		}

		// Set border for a cage which is not selected and which does not have
		// bad maths
		switch (theme) {
		case CARVED:
			mBorderPaint.setColor(0xFF000000);
			mBorderPaint.setAntiAlias(true);
			mBorderPaint.setPathEffect(mPainter.getPathEffect());
			break;
		case NEWSPAPER:
			mBorderPaint.setColor(0xFF000000);
			mBorderPaint.setAntiAlias(false);
			mBorderPaint.setPathEffect(null);
			break;
		case DARK:
			mBorderPaint.setColor(0xFFFFFFFF);
			mBorderPaint.setAntiAlias(true);
			mBorderPaint.setPathEffect(null);
			break;
		}

		// Set border for a cage which is not selected but which is having bad
		// maths
		switch (theme) {
		case CARVED:
			mBorderBadMathPaint.setColor(0xFFBB0000);
			mBorderBadMathPaint.setAntiAlias(true);
			mBorderBadMathPaint.setPathEffect(mPainter.getPathEffect());
			break;
		case NEWSPAPER:
			mBorderBadMathPaint.setColor(0xFFBB0000);
			mBorderBadMathPaint.setAntiAlias(true);
			mBorderBadMathPaint.setPathEffect(null);
			break;
		case DARK:
			mBorderBadMathPaint.setColor(0xFFBB0000);
			mBorderBadMathPaint.setAntiAlias(true);
			mBorderBadMathPaint.setPathEffect(null);
			break;
		}

		// Set border for a cage which is selected
		switch (theme) {
		case CARVED:
			mBorderSelectedPaint.setColor(0xFF000000);
			mBorderSelectedPaint.setAntiAlias(true);
			mBorderSelectedPaint.setPathEffect(mPainter.getPathEffect());
			break;
		case NEWSPAPER:
			mBorderSelectedPaint.setColor(0xFF000000);
			mBorderSelectedPaint.setAntiAlias(false);
			mBorderSelectedPaint.setPathEffect(null);
			break;
		case DARK:
			mBorderSelectedPaint.setColor(0xFFA0A030);
			mBorderSelectedPaint.setAntiAlias(true);
			mBorderSelectedPaint.setPathEffect(null);
			break;
		}

		// Set border for a cages which is selected and is having bad maths
		switch (theme) {
		case CARVED:
			mBorderSelectedBadMathPaint.setColor(0xFFBB0000);
			mBorderSelectedBadMathPaint.setAntiAlias(true);
			mBorderSelectedBadMathPaint.setPathEffect(mPainter.getPathEffect());
			break;
		case NEWSPAPER:
			mBorderSelectedBadMathPaint.setColor(0xFFBB0000);
			mBorderSelectedBadMathPaint.setAntiAlias(true);
			mBorderSelectedBadMathPaint.setPathEffect(null);
			break;
		case DARK:
			mBorderSelectedBadMathPaint.setColor(0xFFBB0000);
			mBorderSelectedBadMathPaint.setAntiAlias(true);
			mBorderSelectedBadMathPaint.setPathEffect(null);
			break;
		}
	}

	@Override
	protected void setBorderSizes(boolean thin) {
		if (thin) {
			mBorderPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
			mBorderSelectedPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_MEDIUM);
			mBorderBadMathPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
			mBorderSelectedBadMathPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_MEDIUM);
		} else {
			mBorderPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
			mBorderSelectedPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THICK);
			mBorderBadMathPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
			mBorderSelectedBadMathPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THICK);
		}
	}

	@Override
	protected void setCellSize(float size) {
		// Text size of cage text is 1/3 of cell size
		int cageTextSize = (int) (size / 3);
		
		mTextPaint.setTextSize(cageTextSize);
		mTextLeftOffset = 2;
		mTextTopOffset = cageTextSize;  
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
	public float getTextTopOffset() {
		return mTextTopOffset;
	}
}