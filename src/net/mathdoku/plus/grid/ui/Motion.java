package net.mathdoku.plus.grid.ui;

import net.mathdoku.plus.grid.Grid;
import android.view.MotionEvent;

public class Motion {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.Motion";

	// The cell coordinates of the cell in the grid for which the touch down was
	// registered. Will be kept statically so it can be compared with the
	// previous touch down event.
	static private int[] mTouchDownCellCoordinates = { -1, -1 };

	// The pixel coordinates of the touch down position.
	private final float[] mTouchDownPixelCoordinates = { -1f, -1f };

	// Flag and timestamp for double tap detection
	private long mDoubleTapTouchDownTime;
	protected boolean mDoubleTapDetected;

	// Size of the border and cells in pixels
	protected final float mGridPlayerViewBorderWidth;
	protected final float mGridCellSize;

	// The size of the grid
	protected final int mGridSize;

	// Indexes for coordinates arrays
	protected static final int X_POS = 0;
	protected static final int Y_POS = 1;

	public Motion(GridBasePlayerView gridBasePlayerView,
			float gridViewBorderWidth, float gridCellSize) {
		// Determine size of the grid
		Grid grid = gridBasePlayerView.getGrid();
		mGridSize = (grid == null ? 1 : grid.getGridSize());

		mGridPlayerViewBorderWidth = gridViewBorderWidth;
		mGridCellSize = gridCellSize;

		mDoubleTapTouchDownTime = 0;
	}

	/**
	 * Register the touch down event.
	 * 
	 * @param motionEvent
	 *            The event which is registered as the touch down event of the
	 *            swipe motion.
	 * @return True in case a grid cell has been touched. False otherwise.
	 */
	protected void setTouchDownEvent(MotionEvent motionEvent) {
		// Store coordinates of previous touch down cell
		int[] previousTouchDownCellCoordinates = mTouchDownCellCoordinates
				.clone();

		// Determine the swipe position pixel coordinates
		mTouchDownPixelCoordinates[X_POS] = motionEvent.getX();
		mTouchDownPixelCoordinates[Y_POS] = motionEvent.getY();

		// Determine coordinates of new position
		mTouchDownCellCoordinates = toGridCoordinates(
				mTouchDownPixelCoordinates[X_POS],
				mTouchDownPixelCoordinates[Y_POS]);

		// Determine whether a new double tap motion is started
		long timeSincePreviousEvent = motionEvent.getEventTime()
				- mDoubleTapTouchDownTime;
		if (mTouchDownCellCoordinates[X_POS] == previousTouchDownCellCoordinates[X_POS]
				&& mTouchDownCellCoordinates[Y_POS] == previousTouchDownCellCoordinates[Y_POS]
				&& timeSincePreviousEvent > 0 && timeSincePreviousEvent < 300) {
			// A double tap is only allowed in case the the second touch down
			// event was on the same cell and the total time between touch down
			// of the first motion until release of the second motion took less
			// than 300 milliseconds.
			mDoubleTapDetected = true;
			mDoubleTapTouchDownTime = 0;
		} else {
			// Too slow for being recognized as double tap. Use touch
			// down time of this swipe motion as new start time of the
			// double tap event.
			if (timeSincePreviousEvent > 0) {
				mDoubleTapTouchDownTime = motionEvent.getDownTime();
			}
			mDoubleTapDetected = false;
		}
	}

	/**
	 * Set the double tap detected flag.
	 * 
	 * @param doubleTapDetected
	 *            : True in case the double tap detected flag has to be set.
	 *            False otherwise.
	 */
	protected void setDoubleTap(boolean doubleTapDetected) {
		mDoubleTapDetected = doubleTapDetected;
	}

	/**
	 * Checks whether this swipe motion completes a double tap on the touch down
	 * cell.
	 * 
	 * @return True in case this swipe motion results in a double tap detection.
	 *         False otherwise.
	 */
	public boolean isDoubleTap() {
		return mDoubleTapDetected;
	}

	/**
	 * Clear the double tap detection. Wait till next touch down to start double
	 * tap detection again.
	 * 
	 */
	public void clearDoubleTap() {
		mDoubleTapTouchDownTime = 0;
		mDoubleTapDetected = false;
	}

	/**
	 * Converts a given position (pixels) to coordinates relative to the grid.
	 * 
	 * @param xPos
	 *            The absolute x-position on the display
	 * @param yPos
	 *            The absolute y-position on the display
	 * @return The (x,y)-position relative to the grid. For x-position -1 means
	 *         left of grid, mGridSize means right of grid. For y-position -1
	 *         means above grid, mGridSize means below grid.
	 */
	protected int[] toGridCoordinates(float xPos, float yPos) {
		int[] coordinates = { -1, -1 };

		// Convert x-position to a column number. -1 means left of grid,
		// mGridSize means right of grid.
		xPos = (xPos - mGridPlayerViewBorderWidth) / mGridCellSize;
		if (xPos > mGridSize) {
			coordinates[X_POS] = mGridSize;
		} else if (xPos < 0) {
			coordinates[X_POS] = -1;
		} else {
			coordinates[X_POS] = (int) xPos;
		}

		// Convert y-position to a column number. -1 means above grid, mGridSize
		// means below grid.
		yPos = (yPos - mGridPlayerViewBorderWidth) / mGridCellSize;
		if (yPos > mGridSize) {
			coordinates[Y_POS] = mGridSize;
		} else if (yPos < 0) {
			coordinates[Y_POS] = -1;
		} else {
			coordinates[Y_POS] = (int) yPos;
		}

		return coordinates;
	}

	/**
	 * Get the cell coordinates for which the touch down event was registered.
	 * 
	 * @return The cell coordinates for which the touch down event was
	 *         registered.
	 */
	public int[] getTouchDownCellCoordinates() {
		return mTouchDownCellCoordinates;
	}

	/**
	 * Get the cell coordinate of the touch down cell, for the given dimension
	 * only.
	 * 
	 * @param dimension
	 *            The dimension of the coordinates which has to be returned.
	 * @return The cell coordinate of the touch down cell, for the given
	 *         dimension only. -1 in case of an error.
	 */
	public int getTouchDownCellCoordinate(int dimension) {
		return (dimension == X_POS || dimension == Y_POS ? mTouchDownCellCoordinates[dimension]
				: -1);
	}

	/**
	 * Get the pixel coordinates for which the touch down event was registered.
	 * 
	 * @return The pixel coordinates for which the touch down event was
	 *         registered.
	 */
	public float[] getTouchDownPixelCoordinates() {
		return mTouchDownPixelCoordinates;
	}

	/**
	 * Get the pixel coordinate for which the touch down event was registered
	 * for the given dimension.
	 * 
	 * @param dimension
	 *            The dimension of the coordinated which has to be returned.
	 * @return The pixel coordinate for which the touch down event was
	 *         registered.
	 */
	public float getTouchDownPixelCoordinate(int dimension) {
		return (dimension == X_POS || dimension == Y_POS ? mTouchDownPixelCoordinates[dimension]
				: -1f);
	}

	/**
	 * Checks if given coordinates match with coordinates of the cell for the
	 * touch down event was registered.
	 * 
	 * @param coordinates
	 *            The (x,y) coordinates which have to be compared with the
	 *            coordinates of the cell for the touch down event was
	 *            registered.
	 * @return True in case the coordinates match. False otherwise.
	 */
	protected boolean equalsCoordinatesTouchDownCell(int[] coordinates) {
		return (coordinates != null && mTouchDownCellCoordinates != null
				&& coordinates.length == 2
				&& mTouchDownCellCoordinates.length == 2
				&& coordinates[X_POS] == mTouchDownCellCoordinates[X_POS] && coordinates[Y_POS] == mTouchDownCellCoordinates[Y_POS]);
	}

	/**
	 * Checks whether the last known touch down position was inside or outside
	 * the grid.
	 * 
	 * @return True in case a position inside the grid was touched. False
	 *         otherwise.
	 */
	public boolean isTouchDownInsideGrid() {
		return !(mTouchDownCellCoordinates[X_POS] > mGridSize - 1
				|| mTouchDownCellCoordinates[X_POS] < 0
				|| mTouchDownCellCoordinates[Y_POS] > mGridSize - 1 || mTouchDownCellCoordinates[Y_POS] < 0);

	}
}