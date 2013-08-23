package net.cactii.mathdoku.grid.ui;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.grid.DigitPositionGrid;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.grid.GridCell;
import net.cactii.mathdoku.painter.GridPainter;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.painter.Painter.DigitPainterMode;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GridViewerView extends View {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridViewerView";

	// Context and preferences in context
	protected Context mContext;
	protected Preferences mPreferences;

	// Actual content of the puzzle in this grid view
	protected Grid mGrid;

	// Size (in cells and pixels) of the grid view and size (in pixel) of cells
	// in grid
	protected int mGridSize;
	protected float mViewSize;
	protected float mBorderWidth;
	protected float mGridCellSize;

	// Reference to the global grid painter object
	private GridPainter mGridPainter;

	// Current orientation of the device
	private int mOrientation;

	// The layout to be used for positioning the maybe digits in a grid.
	private DigitPositionGrid mDigitPositionGrid;

	public GridViewerView(Context context) {
		super(context);
		initGridView(context);
	}

	public GridViewerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGridView(context);
	}

	public GridViewerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGridView(context);
	}

	private void initGridView(Context context) {
		mContext = context;
		mPreferences = Preferences.getInstance(mContext);

		mViewSize = 0;
		mGridPainter = Painter.getInstance().getGridPainter();

		mOrientation = getResources().getConfiguration().orientation;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mGrid == null) {
			// As long as no grid has been attached to the grid view, it can not
			// be drawn.
			return;
		}

		synchronized (mGrid.mLock) {
			onDrawLocked(canvas);
		}
	}

	/**
	 * Actually draw the locked. This method should always be encapsulaped
	 * within a synchronize block <code>
	 * synchronized (mGrid.mLock) {
	 *     onDrawLocked(canvas);
	 *     // other stuff
	 *  }
	 *  </code>
	 * 
	 * @param canvas
	 *            The canvas on which should be drawn.
	 */
	protected void onDrawLocked(Canvas canvas) {
		// Avoid redrawing at the same time as
		// creating
		if (mGridSize < 3)
			return;
		if (mGrid.mCages == null)
			return;

		// Draw outer grid border. For support of transparent borders it has
		// to be avoided that lines do overlap.
		Paint borderPaint = mGridPainter.getBorderPaint();
		canvas.drawRect(0, 0, mViewSize - mBorderWidth, mBorderWidth,
				borderPaint);
		canvas.drawRect(mViewSize - mBorderWidth, 0, mViewSize, mViewSize
				- mBorderWidth, borderPaint);
		canvas.drawRect(mBorderWidth, mViewSize - mBorderWidth, mViewSize,
				mViewSize, borderPaint);
		canvas.drawRect(0, mBorderWidth, 0 + mBorderWidth, mViewSize,
				borderPaint);

		// Draw background of the inner grid itself. This background is a
		// bit extended outside the inner grid which gives a nice outline
		// around the grid in the light theme.
		canvas.drawRect(mBorderWidth - 1, mBorderWidth - 1, mViewSize
				- mBorderWidth + 2, mViewSize - mBorderWidth + 2,
				mGridPainter.getBackgroundPaint());

		// Draw cells
		Painter painter = Painter.getInstance();
		painter.setCellSize(mGridCellSize);
		painter.getMaybeGridPainter().setDigitPositionGrid(mDigitPositionGrid);
		painter.setColorMode(mPreferences.isColoredDigitsVisible() ? DigitPainterMode.INPUT_MODE_BASED
				: DigitPainterMode.MONOCHROME);

		GridInputMode gridInputMode = getGridInputMode();
		for (GridCell cell : mGrid.mCells) {
			cell.draw(canvas, mBorderWidth, gridInputMode, 0);
		}
	}

	/**
	 * Get the current grid input mode.
	 * 
	 * @return The current grid input mode.
	 */
	protected GridInputMode getGridInputMode() {
		return GridInputMode.NORMAL;
	}

	public void loadNewGrid(Grid grid) {
		mGrid = grid;

		// Compute grid size. Set to 1 in case grid is null to avoid problems in
		// onMeasure as this will be called before the grid is loaded.
		mGridSize = (mGrid == null ? 1 : mGrid.getGridSize());

		// Determine the layout which has to be used for drawing the possible
		// values inside a cell.
		mDigitPositionGrid = (mGrid != null
				&& mGrid.hasPrefShowMaybesAs3x3Grid() ? new DigitPositionGrid(
				mGrid.getGridSize()) : null);

		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Get maximum width and height available to display the grid view.
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);

		// Get the maximum space available for the grid. As it is a square we
		// need the minimum of width and height.
		int maxSize = Math.min(measuredWidth, measuredHeight);

		// Compute the exact size needed to display a grid in which the
		// (integer) cell size is as big as possible but the grid still fits in
		// the space available.
		if (mGrid != null && mGrid.isActive()) {
			// The swipe border has to be entirely visible in case a cell at the
			// outer edge of the grid is selected. As the width of the swipe
			// border equals 50% of a normal cell, the entire width is dived by
			// the grid size + 1.
			mGridCellSize = (float) Math.floor(maxSize / (mGridSize + 1));

			// The grid border needs to be at least 50% of a normal cell in
			// order to display the swipe border entirely.
			mBorderWidth = mGridCellSize / 2;

		} else {
			// Force to compute the cell size
			mBorderWidth = -1;
		}

		// The grid view border has to be set to a minimal width which is big
		// enough to catch a swipe motion event. This is needed to be able to
		// end a swipe motion for cells at the outer edge of the grid. In case
		// the border width was already compute to display the swipe border, but
		// is less than the minimal width, both the border width as cell size
		// has to be recomputed as well.
		float minGridBorderWidth = mGridPainter.getBorderPaint()
				.getStrokeWidth();
		if (mBorderWidth < minGridBorderWidth) {
			mBorderWidth = minGridBorderWidth;
			mGridCellSize = (float) Math.floor((maxSize - 2 * mBorderWidth)
					/ mGridSize);
		}

		Painter.getInstance().setCellSize(mGridCellSize);

		// Finally compute the total size of the grid
		mViewSize = 2 * mBorderWidth + mGridSize * mGridCellSize;

		setMeasuredDimension((int) mViewSize, (int) mViewSize);
	}

	private int measure(int measureSpec) {
		int specSize = MeasureSpec.getSize(measureSpec);

		return (specSize);
	}

	/**
	 * Sets the {@link DigitPositionGridType} used to position the digit buttons
	 * for reuse when drawing the maybe values.
	 * 
	 * @param digitPositionGrid
	 *            The digit position grid type to be set.
	 */
	public void setDigitPositionGrid(DigitPositionGrid digitPositionGrid) {
		mDigitPositionGrid = (mGrid == null
				|| !mGrid.hasPrefShowMaybesAs3x3Grid() ? null
				: digitPositionGrid);
	}

	/**
	 * Get the orientation of the device.
	 */
	protected int getOrientation() {
		return mOrientation;
	}

	/**
	 * Get the grid which is displayed in the grid viewer view.
	 * 
	 * @return The grid which is displayed in the grid viewer view.
	 */
	protected Grid getGrid() {
		return mGrid;
	}
}