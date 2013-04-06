package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;

public class GridPainter extends BorderPainter {

	// The background for the grid.
	protected Paint mBackgroundPaint;

	/**
	 * Creates a new instance of {@link GridPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public GridPainter(Painter painter) {
		super(painter);

		mBorderPaint = new Paint();
		mBorderPaint.setStyle(Paint.Style.STROKE);

		mBackgroundPaint = new Paint();
		mBackgroundPaint.setStyle(Paint.Style.STROKE);

		mBorderPaint.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THIN);
	}

	@Override
	public void setTheme(GridTheme theme) {
		// Set background color
		switch (theme) {
		case CARVED:
			mBackgroundPaint.setColor(0x7ff0d090);
			break;
		case NEWSPAPER:
			mBackgroundPaint.setColor(0xffffffff);
			break;
		case NEWSPAPER_OLD:
			mBackgroundPaint.setColor(0xffffffff);
			break;
		case DARK:
			mBackgroundPaint.setColor(0xff000000);
			break;
		}

		// Set border paint
		switch (theme) {
		case CARVED:
			mBorderPaint.setAntiAlias(true);
			mBorderPaint.setPathEffect(mPainter.getPathEffect());
			mBorderPaint.setColor(0xff000000);
			break;
		case NEWSPAPER:
			mBorderPaint.setAntiAlias(false);
			mBorderPaint.setPathEffect(null);
			mBorderPaint.setColor(0xFFAAAAAA);
			break;
		case NEWSPAPER_OLD:
			mBorderPaint.setAntiAlias(false);
			mBorderPaint.setPathEffect(null);
			mBorderPaint.setColor(0xFFAAAAAA);
			break;
		case DARK:
			mBorderPaint.setAntiAlias(true);
			mBorderPaint.setPathEffect(null);
			mBorderPaint.setColor(0xFFAAAAAA);
			break;
		}
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
	 * @return
	 */
	public Paint getBackgroundPaint() {
		return mBackgroundPaint;
	}
}