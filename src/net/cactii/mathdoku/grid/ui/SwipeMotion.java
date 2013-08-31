package net.cactii.mathdoku.grid.ui;

import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.grid.GridCell;
import android.content.res.Configuration;
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
	private final GridPlayerView mGridPlayerView;
	private final int mGridSize;

	// Size of the border and cells in pixels
	private final float mGridPlayerViewBorderWidth;
	private final float mGridCellSize;

	// The cell coordinates of the cell in the grid for which the touch down was
	// registered. Will be kept statically so it can be compared with the
	// previous touch down event.
	static private int[] mTouchDownCellCoordinates = { -1, -1 };

	// The pixel coordinates of the touch down position.
	private float[] mTouchDownPixelCoordinates;

	// The pixel coordinates of the center of the touch down cell which will be
	// used as start of the swipe line.
	private float[] mTouchDownCellCenterPixelCoordinates;

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
	protected SwipeMotion(GridPlayerView gridPlayerView,
			float gridViewBorderWidth, float gridCellSize) {
		mGridPlayerView = gridPlayerView;
		Grid grid = mGridPlayerView.getGrid();
		mGridSize = (grid == null ? 1 : grid.getGridSize());

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

		// Store the pixel and cell coordinates for the swipe position of this
		// touch down event.
		mTouchDownPixelCoordinates = mCurrentSwipePositionPixelCoordinates
				.clone();
		mTouchDownCellCoordinates = mCurrentSwipePositionCellCoordinates
				.clone();

		// Determine the pixel coordinates of the center of the cell as the
		// swipe line will start at the center of the cell regardless of the
		// actual touch down position.
		GridCell gridCell = null;
		if (mGridPlayerView != null) {
			Grid grid = mGridPlayerView.getGrid();
			if (grid != null) {
				gridCell = grid.getCellAt(mTouchDownCellCoordinates[Y_POS],
						mTouchDownCellCoordinates[X_POS]);
			}
		}
		mTouchDownCellCenterPixelCoordinates = (gridCell == null ? mTouchDownPixelCoordinates
				.clone() : gridCell
				.getCellCentreCoordinates(mGridPlayerViewBorderWidth));

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
			// reseted as for the double tap it is required that two
			// consecutive swipe motion haven been completed entire within the
			// double tap time duration.
			if (event.getEventTime() - mDoubleTapTouchDownTime < 300) {
				// A double tap is only allowed in case the total time
				// between touch down of the first swipe motion until release of
				// the second swipe motion took less than 300 milliseconds.
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
	 * Get the cell coordinates for which the touch down event was registered.
	 * 
	 * @return The cell coordinates for which the touch down event was
	 *         registered.
	 */
	protected int[] getTouchDownCellCoordinates() {
		return mTouchDownCellCoordinates;
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
		if (mStatus == Status.TOUCH_DOWN || mStatus == Status.MOVING) {
			mStatus = Status.RELEASED;
		} else if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			throw new RuntimeException(
					"Swipe Motion status can not be changed from "
							+ mStatus.toString() + " to " + Status.RELEASED);
		}
		if (update(event)) {
			// A digit was determined upon release the swipe motion. This motion
			// may therefore not be used to detect a double tap. This prevents
			// false detection of double taps due to rapid entering of maybe
			// digits.
			clearDoubleTap();
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

		// Determine the swipe position pixel coordinates
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
	 * @return True in case a digit has been determined. False otherwise.
	 */
	protected boolean update(MotionEvent motionEvent) {
		if (mStatus == Status.TOUCH_DOWN) {
			mStatus = Status.MOVING;
		}

		// Update the coordinates of the current swipe position
		setCurrentSwipeCoordinates(motionEvent);

		// Check whether current swipe position is inside or outside the touch
		// down cell.
		boolean inTouchDownCell = equalsCoordinatesTouchDownCell(mCurrentSwipePositionCellCoordinates);

		// Determine the digit for the current swipe position.
		if (inTouchDownCell) {
			if (mStatus != Status.RELEASED
					|| mGridSize < 7
					|| (mCurrentSwipePositionCellCoordinates[X_POS] > 0
							&& mCurrentSwipePositionCellCoordinates[X_POS] < mGridSize - 1
							&& mCurrentSwipePositionCellCoordinates[Y_POS] > 0 && mCurrentSwipePositionCellCoordinates[Y_POS] < mGridSize - 1)) {
				// Normally the swipe digit will only be updated in case the
				// swipe position is outside the touch down cell.
				mCurrentSwipePositionDigit = DIGIT_UNDETERMINDED;
				return false;
			} else {
				// The swipe motion for a higher size grid, is released in an
				// outer cell of the grid or just outside the grid. In this case
				// it is not checked whether the touch down cell has been left
				// as it is sometimes more difficult to select the digit due to
				// a smaller border around the grid (especially when using a
				// bumper case to protect the device).
			}
		}

		// Compute the length of the swipe line. In case the current swipe
		// position is inside the touch down cell the distance is measured
		// between the *real* touch down position and the current position.
		// In case the current swipe position is outside the touch down cell
		// than the distance is measured between the center of the touch down
		// cell and the current position.
		float deltaX = mCurrentSwipePositionPixelCoordinates[X_POS]
				- (inTouchDownCell ? mTouchDownPixelCoordinates[X_POS]
						: mTouchDownCellCenterPixelCoordinates[X_POS]);
		float deltaY = mCurrentSwipePositionPixelCoordinates[Y_POS]
				- (inTouchDownCell ? mTouchDownPixelCoordinates[Y_POS]
						: mTouchDownCellCenterPixelCoordinates[Y_POS]);
		if (Math.sqrt(deltaX * deltaX + deltaY * deltaY) < 10) {
			// The distance is too small to be accepted.
			Log.i(TAG, " - deltaX = " + deltaX + " - deltaY = " + deltaY);
			return false;
		}

		// Compute the current swipe digit by measuring the angle of the swipe
		// line. In case the current swipe position is inside the touch down
		// cell the angle of the swipe line will be computed related to the
		// *real* touch down position. In case the current swipe position is
		// outside the touch down cell than the angle is computed relative to
		// the center of the touch down cell.
		double angle = Math.toDegrees(Math.atan2(deltaY, deltaX))
				+ (-1 * SWIPE_ANGLE_OFFSET_91);
		int digit = (angle < 0 ? 9 : (int) (angle / SWIPE_SEGMENT_ANGLE) + 1);

		if (DEBUG_SWIPE_MOTION) {
			Log.i(TAG, "getDigit");
			if (inTouchDownCell) {
				Log.i(TAG,
						"Current swipe position inside touch down cell. Angle computed to real touch down position");
				Log.i(TAG, " - start = (" + mTouchDownPixelCoordinates[X_POS]
						+ ", " + mTouchDownPixelCoordinates[Y_POS] + ")");
			} else {
				Log.i(TAG,
						"Current swipe position outside touch down cell. Angle computed to center of touch down cell");
				Log.i(TAG, " - start = ("
						+ mTouchDownCellCenterPixelCoordinates[X_POS] + ", "
						+ mTouchDownCellCenterPixelCoordinates[Y_POS] + ")");
			}
			Log.i(TAG, " - current = ("
					+ mCurrentSwipePositionPixelCoordinates[X_POS] + ", "
					+ mCurrentSwipePositionPixelCoordinates[Y_POS] + ")");
			Log.i(TAG, " - deltaX = " + deltaX + " - deltaY = " + deltaY);
			Log.i(TAG, " - angle = " + angle);
			Log.i(TAG, " - digit = " + digit);
		}

		// Determine whether the digit should be accepted.
		boolean acceptDigit = (inTouchDownCell ? false : true);
		if (acceptDigit == false) {
			// Normally the digit is not accepted in case the swipe motion is
			// inside the touch down cell. In case a swipe motion is started and
			// ended in a cell on the outer edge of the grid, the digit will be
			// accepted in case the swipe motion was heading outside the grid.
			//
			// In portrait mode the swipe motions to the left and to the right
			// of the grid view needs to be examined. Swipe motions to the top
			// can be neglected as the action bar is displayed above the grid.
			// Swipe motions to the bottom can be neglected as the clear and
			// undo buttons are shown below the grid view.
			//
			// In portrait mode the swipe motions to the left and to the bottom
			// of the grid view needs to be examined. Swipe motions to the top
			// can be neglected as the action bar is displayed above the grid.
			// Swipe motions to the right can be neglected as the clear and undo
			// buttons are shown to the right of the grid view.
			switch (digit) {
			case 1:
				acceptDigit = (mCurrentSwipePositionCellCoordinates[X_POS] == 0);
				break;
			case 2:
				break;
			case 3:
				break;
			case 4: // fall through
			case 5:
				acceptDigit = (mGridPlayerView.getOrientation() == Configuration.ORIENTATION_PORTRAIT && mCurrentSwipePositionCellCoordinates[X_POS] == mGridSize - 1);
				break;
			case 6:
				acceptDigit = (mGridPlayerView.getOrientation() == Configuration.ORIENTATION_PORTRAIT && mCurrentSwipePositionCellCoordinates[X_POS] == mGridSize - 1)
						|| (mGridPlayerView.getOrientation() == Configuration.ORIENTATION_LANDSCAPE && mCurrentSwipePositionCellCoordinates[Y_POS] == mGridSize - 1);
				break;
			case 7:
				acceptDigit = (mGridPlayerView.getOrientation() == Configuration.ORIENTATION_LANDSCAPE && mCurrentSwipePositionCellCoordinates[Y_POS] == mGridSize - 1);
				break;
			case 8:
				acceptDigit = (mCurrentSwipePositionCellCoordinates[X_POS] == 0)
						|| (mGridPlayerView.getOrientation() == Configuration.ORIENTATION_LANDSCAPE && mCurrentSwipePositionCellCoordinates[Y_POS] == mGridSize - 1);
				break;
			case 9:
				acceptDigit = (mCurrentSwipePositionCellCoordinates[X_POS] == 0);
				break;
			}
		}
		if (acceptDigit) {
			mCurrentSwipePositionDigit = digit;
			return true;
		}

		return false;
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
		mDoubleTapTouchDownTime = 0;
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
		return SWIPE_ANGLE_OFFSET_91 + ((digit - 1) * SWIPE_SEGMENT_ANGLE)
				+ (SWIPE_SEGMENT_ANGLE / 2);
	}

	/**
	 * Get the angle which separates the segment of the given digit with the
	 * next (clock wise) digit.
	 * 
	 * @param digit
	 *            The digit of the segment for which the angle has to be
	 *            determined which separates this segment from the next segment.
	 * @return The angle to which separates the segment of the given digit with
	 *         the next (clock wise) digit.
	 */
	public static int getAngleToNextSwipeSegment(int digit) {
		return SWIPE_ANGLE_OFFSET_91 + (digit * SWIPE_SEGMENT_ANGLE);
	}
}