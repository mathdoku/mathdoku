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

		// The grid border paint is only used as a (transparent) buffer zone
		// around the actual grid. The buffer zone is needed to end swipe
		// motions which move out of the inner grid. Also it is used to display
		// (a part of) the swipe border around the cell at the outer edge of the
		// grid.
		mBorderPaint = new Paint();
		mBorderPaint.setStrokeWidth(15);

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