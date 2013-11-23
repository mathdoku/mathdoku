package net.mathdoku.plus.painter;

import net.mathdoku.plus.painter.Painter.GridTheme;
import android.graphics.Paint;

public class GridPainter extends BorderPainter {

	// The background for the grid.
	private Paint mBackgroundPaint;

	/**
	 * Creates a new instance of {@link GridPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public GridPainter(Painter painter) {
		super(painter);

		mBorderPaint = new Paint();
		mBorderPaint.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);

		mBackgroundPaint = new Paint();
		mBackgroundPaint.setStyle(Paint.Style.FILL);
	}

	@Override
	public void setTheme(GridTheme theme) {
		// Set background color
		switch (theme) {
		case LIGHT:
			mBackgroundPaint.setColor(0xFFFFFFFF);
			break;
		case DARK:
			mBackgroundPaint.setColor(0x00000000);
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