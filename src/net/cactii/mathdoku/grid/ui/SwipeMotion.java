package net.cactii.mathdoku.grid.ui;

import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.grid.GridCell;
import android.util.Log;
import android.view.MotionEvent;

// Definition of swipe motion
public class SwipeMotion {
	private static final String TAG = "MathDoku.SwipeMotion";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG_SWIPE_MOTION = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && true;

	// Indexes for coordinates arrays
	private static final int X_POS = 0;
	private static final int Y_POS = 1;

	// Possible statuses of the swipe motion
	private enum Status {
		INIT, TOUCH_DOWN, MOVING, RELEASED, FINISHED
	}

	// Status of the swipe motion
	private Status mStatus;

	// Constant for an undetermined result of the swipe motion
	protected static final int DIGIT_UNDETERMINDED = -1;

	// The grid and its size for which a swipe motion is made.
	private final Grid mGrid;
	private final int mGridSize;

	// Size of the border and cells in pixels
	private final float mGridPlayerViewBorderWidth;
	private final float mGridCellSize;

	// Coordinates of the cell in the grid for which the touch down was
	// registered. Will be kept statically so it can be compared with the
	// previous touch down event.
	static private int[] mTouchDownCellCoordinates = { -1, -1 };

	// In case two consecutive touch downs on the same cell have been completed
	// within a small amount of time, this motion event might

	// Cell coordinates of the previous and the current swipe position. Note the
	// special values:
	// * -1 is to left of or above the grid
	// * gridSize is to right of or below the grid.
	private int[] mPreviousSwipePositionCellCoordinates = { -1, -1 };
	private int[] mCurrentSwipePositionCellCoordinates = { -1, -1 };

	// Coordinates of the current coordinates (pixels) of the swipe.
	private final float[] mCurrentSwipePositionPixelCoordinates = { -1f, -1f };

	// The resulting digit based on the previous and the current swipe position
	private int mPreviousSwipePositionDigit;
	private int mCurrentSwipePositionDigit;

	// Registration of event time to detect a double tap on the same touch down
	// cell. It is kept statically in order to compare with the previous swipe
	// motion.
	static private long mDoubleTapTouchDownTime = 0;
	private boolean mDoubleTapDetected;

	// Event time at which the previous swipe position was advised to be updated
	private long mPreviousSwipePositionEventTime = -1l;
	private long mCurrentSwipePositionEventTime = -1l;

	// The swipe circle has to display a maximum of 9 digits. So the circle has
	// to be divided in 9 segments of each 40 degrees width. The digits will be
	// arranged clockwise.
	// The swipe angle offset is used for the segment boundary between digits 9
	// and 1.
	public final static int SWIPE_ANGLE_OFFSET_91 = -170;
	public final static int SWIPE_SEGMENT_ANGLE = 360 / 9;

	/**
	 * Creates a new instance of the {@see SwipeMotion}.
	 * 
	 * @param grid
	 *            The grid for which a swipe motion is made.
	 */
	protected SwipeMotion(Grid grid, float gridViewBorderWidth,
			float gridCellSize) {
		mGrid = grid;
		mGridSize = (mGrid == null ? 1 : mGrid.getGridSize());

		mGridPlayerViewBorderWidth = gridViewBorderWidth;
		mGridCellSize = gridCellSize;

		mStatus = Status.INIT;
	}

	/**
	 * Register the touch down event.
	 * 
	 * @param event
	 *            The event which is registered as the touch down event of the
	 *            swipe motion.
	 * @return True in case a grid cell has been touched. False otherwise.
	 */
	protected boolean setTouchDownEvent(MotionEvent event) {
		// Store coordinates of previous touch down cell
		int[] previousTouchDownCellCoordinates = mTouchDownCellCoordinates
				.clone();

		// Set the resulting digit for previous to unknown
		mPreviousSwipePositionDigit = DIGIT_UNDETERMINDED;
		mCurrentSwipePositionDigit = DIGIT_UNDETERMINDED;

		// Update swipe position.
		setCurrentSwipeCoordinates(event);
		if (mCurrentSwipePositionCellCoordinates[X_POS] > mGridSize - 1
				|| mCurrentSwipePositionCellCoordinates[X_POS] < 0
				|| mCurrentSwipePositionCellCoordinates[Y_POS] > mGridSize - 1
				|| mCurrentSwipePositionCellCoordinates[Y_POS] < 0) {
			// A position inside the grid border (i.e. not inside a grid cell
			// has been selected. Such touch down position have to be ignored as
			// start of a swipe motion.
			return false;
		}

		// Store the cell coordinates for the swipe position of this touch down
		// event
		mTouchDownCellCoordinates = mCurrentSwipePositionCellCoordinates
				.clone();

		// Determine whether a new double tap motion is started
		mDoubleTapDetected = false;
		if (mTouchDownCellCoordinates[X_POS] != previousTouchDownCellCoordinates[X_POS]
				|| mTouchDownCellCoordinates[Y_POS] != previousTouchDownCellCoordinates[Y_POS]) {
			// Another cell is selected. Checking for double tap is not needed
			// as this is not the second (or more) consecutive swipe motion on
			// the same selected cell.
			mDoubleTapTouchDownTime = event.getDownTime();
		} else {
			// The same cell is selected again. The touch down time may not be
			// reseted as for the double tap it is required that tow
			// consecutive swipe motion haven been completed entire within the
			// double tap time duration.
			if (event.getEventTime() - mDoubleTapTouchDownTime < 300) {
				// A double tap is only allowed in case the total time
				// between
				// touch down of the first swipe motion until release of the
				// second swipe motion took less than 300 milliseconds.
				mDoubleTapDetected = true;
			} else {
				// Too slow for being recognized as double tap. Use touch
				// down time of this swipe motion as new start time of the
				// double tap event.
				mDoubleTapTouchDownTime = event.getDownTime();
			}
		}

		// Touch down has been fully completed.
		mStatus = Status.TOUCH_DOWN;

		return true;
	}

	/**
	 * Get the cell for which the touch down event was registered.
	 * 
	 * @return The cell for which the touch down event was registered.
	 */
	protected GridCell getTouchDownCell() {
		return (mGrid == null ? null : mGrid.getCellAt(
				mTouchDownCellCoordinates[Y_POS],
				mTouchDownCellCoordinates[X_POS]));
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
	 * Registers the swipe motion as released.
	 */
	protected void release(MotionEvent event) {
		update(event);
		if (mStatus == Status.TOUCH_DOWN || mStatus == Status.MOVING) {
			mStatus = Status.RELEASED;
		} else if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			throw new RuntimeException(
					"Swipe Motion status can not be changed from "
							+ mStatus.toString() + " to " + Status.RELEASED);
		}
	}

	/**
	 * Checks if this swipe motion is visible.
	 * 
	 * @return True in case the motion is visible. False otherwise.
	 */
	protected boolean isVisible() {
		switch (mStatus) {
		case TOUCH_DOWN: // fall through
		case MOVING: // fall through
			return true;
		default:
			return false;
		}
	}

	/**
	 * Checks if this swipe motion has been released but is not yet completed.
	 * 
	 * @return True in case the motion has been released but not yet completed.
	 *         False otherwise.
	 */
	protected boolean isReleased() {
		return (mStatus == Status.RELEASED);
	}

	/**
	 * Set the swipe motion as completed.
	 */
	protected void finish() {
		if (mStatus == Status.RELEASED) {
			mStatus = Status.FINISHED;
		} else if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			throw new RuntimeException(
					"Swipe Motion status can not be changed from "
							+ mStatus.toString() + " to " + Status.FINISHED);
		}
	}

	/**
	 * Get the x-coordinate of the current swipe position.
	 * 
	 * @return The x-coordinate of the current swipe position.
	 */
	protected float getCurrentSwipePositionX() {
		return mCurrentSwipePositionPixelCoordinates[X_POS];
	}

	/**
	 * Get the y-coordinate of the current swipe position.
	 * 
	 * @return The y-coordinate of the current swipe position.
	 */
	protected float getCurrentSwipePositionY() {
		return mCurrentSwipePositionPixelCoordinates[Y_POS];
	}

	/**
	 * Set the coordinates of the current swipe position.
	 * 
	 * @param event
	 *            The event which holding the current swipe position.
	 */
	private void setCurrentSwipeCoordinates(MotionEvent motionEvent) {
		// Save the old values of the current swipe position and digit
		mPreviousSwipePositionCellCoordinates = mCurrentSwipePositionCellCoordinates;
		mPreviousSwipePositionDigit = mCurrentSwipePositionDigit;

		mCurrentSwipePositionPixelCoordinates[X_POS] = motionEvent.getX();
		mCurrentSwipePositionPixelCoordinates[Y_POS] = motionEvent.getY();
		mCurrentSwipePositionEventTime = motionEvent.getEventTime();

		// Determine coordinates of new current swipe position on the actual
		// swipe position
		mCurrentSwipePositionCellCoordinates = getCoordinatesSwipePosition(
				mCurrentSwipePositionPixelCoordinates[X_POS],
				mCurrentSwipePositionPixelCoordinates[Y_POS]);
	}

	/**
	 * Update the the swipe motion with data from a new motion event.
	 * 
	 * @param event
	 *            The event which holding the current swipe position.
	 */
	protected void update(MotionEvent motionEvent) {
		if (mStatus == Status.TOUCH_DOWN) {
			mStatus = Status.MOVING;
		} else if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (mStatus != Status.MOVING) {
				throw new RuntimeException(
						"Swipe Motion status can not be changed from "
								+ mStatus.toString() + " to " + Status.MOVING);
			}
		}

		// Update the coordinates of the current swipe position
		setCurrentSwipeCoordinates(motionEvent);

		// Determine the digit for the current swipe position
		mCurrentSwipePositionDigit = getDigit();
	}

	/**
	 * Checks whether the current swipe position needs to be updated.
	 * 
	 * @return True in case the current swipe position needs to be updated.
	 *         False otherwise.
	 */
	protected boolean needToUpdateCurrentSwipePosition() {
		// Update at start of motion
		if (mPreviousSwipePositionCellCoordinates == null
				&& mCurrentSwipePositionCellCoordinates != null) {
			mPreviousSwipePositionEventTime = mCurrentSwipePositionEventTime;
			return true;
		}

		// Update when the motion has reached another cell
		if (mPreviousSwipePositionCellCoordinates != null
				&& mCurrentSwipePositionCellCoordinates != null) {
			if (mPreviousSwipePositionCellCoordinates[X_POS] != mCurrentSwipePositionCellCoordinates[X_POS]
					|| mPreviousSwipePositionCellCoordinates[Y_POS] != mCurrentSwipePositionCellCoordinates[Y_POS]) {
				mPreviousSwipePositionEventTime = mCurrentSwipePositionEventTime;
				return true;
			}
		}

		// Update if the last update was 100 milliseconds or longer ago
		if (mPreviousSwipePositionEventTime > 0
				&& mCurrentSwipePositionEventTime > 0
				&& mCurrentSwipePositionEventTime
						- mPreviousSwipePositionEventTime > 100) {
			mPreviousSwipePositionEventTime = mCurrentSwipePositionEventTime;
			return true;
		}

		// No update to save on performance
		return false;
	}

	/**
	 * Checks whether the current swipe position results in the same digit as
	 * the previous swipe position.
	 * 
	 * @return
	 */
	protected boolean hasChangedDigit() {
		return (mPreviousSwipePositionDigit != mCurrentSwipePositionDigit);
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
	private int[] getCoordinatesSwipePosition(float xPos, float yPos) {
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
	 * Determine the digit which corresponds with the angle of the current swipe
	 * line.
	 * 
	 * @param event
	 *            The event holding the current swipe position.
	 * @return The digit which corresponds with the angle of the current swipe
	 *         line.
	 */
	private int getDigit() {
		if (equalsCoordinatesTouchDownCell(mCurrentSwipePositionCellCoordinates)) {
			return DIGIT_UNDETERMINDED;
		}

		// In case the swipe position is not inside the originally selected
		// cell, the digit is determined based on the angle of the swipe line.

		// Get the start position of the swipe line.
		float[] startCoordinates = getTouchDownCell().getCellCentreCoordinates(
				mGridPlayerViewBorderWidth);

		// Compute current swipe position by measuring the angle of the current
		// swipe position with respect to the start position.
		//
		float deltaX = mCurrentSwipePositionPixelCoordinates[X_POS]
				- startCoordinates[X_POS];
		float deltaY = mCurrentSwipePositionPixelCoordinates[Y_POS]
				- startCoordinates[Y_POS];
		double angle = Math.toDegrees(Math.atan2(deltaY, deltaX))
				+ (-1 * SWIPE_ANGLE_OFFSET_91);
		int digit = (angle < 0 ? 9 : (int) (angle / SWIPE_SEGMENT_ANGLE) + 1);

		if (DEBUG_SWIPE_MOTION) {
			Log.i(TAG, "getDigit");
			Log.i(TAG, " - start = (" + startCoordinates[X_POS] + ", "
					+ startCoordinates[Y_POS] + ")");
			Log.i(TAG, " - current = ("
					+ mCurrentSwipePositionPixelCoordinates[X_POS] + ", "
					+ mCurrentSwipePositionPixelCoordinates[Y_POS] + ")");
			Log.i(TAG, " - deltaX = " + deltaX + " - deltaY = " + deltaY);
			Log.i(TAG, " - angle = " + angle);
			Log.i(TAG, " - digit = " + digit);
		}

		return digit;
	}

	/**
	 * Checks whether the current swipe position results in a digit in the given
	 * range.
	 * 
	 * @param minimum
	 *            The minimum value (should be 1)
	 * @param maximum
	 *            The maximum value (value from 1 to 9)
	 * @return True in case the current swipe position results in a digit in the
	 *         given range. False otherwise.
	 */
	public boolean isResultDigitInRange(int minimum, int maximum) {
		return (mCurrentSwipePositionDigit >= minimum && mCurrentSwipePositionDigit <= maximum);
	}

	/**
	 * Return the digit which is associated with the current swipe position.
	 * 
	 * @return The digit which is associated with the current swipe position.
	 */
	public int getFocussedDigit() {
		return mCurrentSwipePositionDigit;
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
	 * Clear the double tap detection.
	 * 
	 */
	public void clearDoubleTap() {
		mDoubleTapDetected = false;
	}

	/**
	 * Get the angle to the middle of the segment which is used to select the
	 * given digit.
	 * 
	 * @param digit
	 *            The digit for which the middle of the swipe segment has to be
	 *            determined.
	 * @return The angle to the middle of the segment which is used to select
	 *         the given digit.
	 */
	public static int getAngleCenterSwipeSegment(int digit) {
		return ((digit - 1) * SWIPE_SEGMENT_ANGLE) + SWIPE_ANGLE_OFFSET_91
				+ (SWIPE_SEGMENT_ANGLE / 2);
	}
}