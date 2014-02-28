package net.mathdoku.plus.puzzle.ui;

import android.content.res.Configuration;
import android.util.Log;
import android.view.MotionEvent;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;

// Definition of swipe motion
public class SwipeMotion extends Motion {
	private static final String TAG = "MathDoku.SwipeMotion";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	@SuppressWarnings("PointlessBooleanExpression")
	private static final boolean DEBUG_SWIPE_MOTION = (Config.mAppMode == AppMode.DEVELOPMENT) && false;

	// Possible statuses of the swipe motion
	private enum Status {
		INIT, TOUCH_DOWN, MOVING, RELEASED, FINISHED
	}

	// Status of the swipe motion
	private Status mStatus;

	// Status of visibility of the swipe motion
	private boolean mVisible;

	// Constant for an undetermined result of the swipe motion
	private static final int DIGIT_UNDETERMINED = -1;

	// The grid player view for which a swipe motion is made.
	private final GridPlayerView mGridPlayerView;

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

	// Event time at which the previous swipe position was advised to be updated
	private long mPreviousSwipePositionEventTime = -1l;
	private long mCurrentSwipePositionEventTime = -1l;

	// The swipe circle has to display a maximum of 9 digits. So the circle has
	// to be divided in 9 segments of each 40 degrees width. The digits will be
	// arranged clockwise.
	// The swipe angle offset is used for the segment boundary between digits 9
	// and 1.
	private static final int SWIPE_ANGLE_OFFSET_91 = -170;
	private static final int SWIPE_SEGMENT_ANGLE = 360 / 9;

	// Listener
	public interface Listener {
		// Called as releasing the swipe motion at the current swipe position
		// will result in
		// selecting a digit.
		public void onSelectableDigit();

		// Called as releasing the swipe motion at the current swipe position
		// will not result in
		// selecting a digit.
		public void onNoSelectableDigit();
	}

	private Listener mListener;

	/**
	 * Creates a new instance of the {@see SwipeMotion}.
	 * 
	 * @param gridPlayerView
	 *            The grid player view in which the swipe motion is created.
	 * @param gridViewBorderWidth
	 *            The border width in the grid view in which the swipe motion is
	 *            created.
	 * @param cellSize
	 *            The size of the cells in the grid view in which the swipe
	 *            motion is created.
	 */
	SwipeMotion(GridPlayerView gridPlayerView, float gridViewBorderWidth,
			float cellSize) {
		super(gridPlayerView, gridViewBorderWidth, cellSize);

		mGridPlayerView = gridPlayerView;

		mStatus = Status.INIT;
		mVisible = false;
	}

	/**
	 * Register the touch down event.
	 * 
	 * @param event
	 *            The event which is registered as the touch down event of the
	 *            swipe motion.
	 */
	@Override
	void setTouchDownEvent(MotionEvent event) {
		// Register touch down event at superclass (including updating the
		// current mTouchDownCellCoordinates)
		super.setTouchDownEvent(event);

		// Get the cell coordinates of the new touch down position.
		int touchDownCellCoordinates[] = getTouchDownCellCoordinates();

		// Set the resulting digit for previous to unknown
		mPreviousSwipePositionDigit = DIGIT_UNDETERMINED;
		mCurrentSwipePositionDigit = DIGIT_UNDETERMINED;

		// Update swipe position.
		setCurrentSwipeCoordinates(event);

		if (!isTouchDownInsideGrid()) {
			mStatus = Status.INIT;
			mVisible = false;
			return;
		}

		// Determine the pixel coordinates of the center of the cell as the
		// swipe line will start at the center of the cell regardless of the
		// actual touch down position.
		CellDrawer cellDrawer = null;
		if (mGridPlayerView != null) {
			cellDrawer = mGridPlayerView.getCellDrawer(
					touchDownCellCoordinates[Y_POS],
					touchDownCellCoordinates[X_POS]);
		}
		mTouchDownCellCenterPixelCoordinates = (cellDrawer == null ? getTouchDownPixelCoordinates()
				.clone() : cellDrawer
				.getCellCentreCoordinates(mGridPlayerViewBorderWidth));

		// Touch down has been fully completed.
		mStatus = Status.TOUCH_DOWN;
		mVisible = false;
	}

	/**
	 * Registers the swipe motion as released.
	 */
	void release(MotionEvent event) {
		if (mStatus == Status.INIT) {
			// Swipe motion is not yet started from inside of grid cell. Nothing
			// to do here.
			return;
		} else if (mStatus == Status.TOUCH_DOWN || mStatus == Status.MOVING) {
			mStatus = Status.RELEASED;
		} else if (mStatus == Status.RELEASED || mStatus == Status.FINISHED) {
			// Already released. Nothing to do here.
			return;
		} else if (Config.mAppMode == AppMode.DEVELOPMENT) {
			throw new RuntimeException(
					"Swipe Motion status can not be changed from "
							+ mStatus.toString() + " to " + Status.RELEASED);
		}
		if (event != null && update(event)) {
			// A digit was determined upon release the swipe motion. This motion
			// may therefore not be used to detect a double tap. This prevents
			// false detection of double taps due to rapid entering of maybe
			// digits.
			clearDoubleTap();
		}
	}

	/**
	 * Checks if this swipe motion should be made visible (e.g. drawn).
	 * 
	 * @return True in case the motion is visible. False otherwise.
	 */
	boolean isVisible() {
		return mVisible;
	}

	/**
	 * Sets the visibility status of the swipe motion.
	 * 
	 * @param visible
	 *            True in case the swipe motion has been drawn. False otherwise.
	 */
	void setVisible(boolean visible) {
		mVisible = visible;
	}

	/**
	 * Checks if this swipe motion has been released but is not yet completed.
	 * 
	 * @return True in case the motion has been released but not yet completed.
	 *         False otherwise.
	 */
	boolean isReleased() {
		return (mStatus == Status.RELEASED);
	}

	/**
	 * Checks if this swipe motion has been finished completely.
	 * 
	 * @return True in case the motion has finished completely. False otherwise.
	 */
	boolean isFinished() {
		return (mStatus == Status.FINISHED);
	}

	/**
	 * Set the swipe motion as completed.
	 */
	void finish() {
		if (mStatus == Status.RELEASED) {
			mStatus = Status.FINISHED;
		} else if (Config.mAppMode == AppMode.DEVELOPMENT
				&& mStatus != Status.FINISHED) {
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
	float getCurrentSwipePositionX() {
		return mCurrentSwipePositionPixelCoordinates[X_POS];
	}

	/**
	 * Get the y-coordinate of the current swipe position.
	 * 
	 * @return The y-coordinate of the current swipe position.
	 */
	float getCurrentSwipePositionY() {
		return mCurrentSwipePositionPixelCoordinates[Y_POS];
	}

	/**
	 * Set the coordinates of the current swipe position.
	 * 
	 * @param motionEvent
	 *            The motion event which holding the current swipe position.
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
		mCurrentSwipePositionCellCoordinates = toGridCoordinates(
				mCurrentSwipePositionPixelCoordinates[X_POS],
				mCurrentSwipePositionPixelCoordinates[Y_POS]);
	}

	/**
	 * Update the the swipe motion with data from a new motion event.
	 * 
	 * @param motionEvent
	 *            The event which holding the current swipe position.
	 * @return True in case a digit has been determined. False otherwise.
	 */
	boolean update(MotionEvent motionEvent) {
		if (mStatus == Status.INIT) {
			// Movement has not yet started inside a grid cell.
			mCurrentSwipePositionDigit = DIGIT_UNDETERMINED;
			return false;
		}
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
				// Call listener only in case previously a digit was selectable.
				if (mListener != null
						&& mCurrentSwipePositionDigit != DIGIT_UNDETERMINED) {
					mListener.onNoSelectableDigit();
				}

				// Normally the swipe digit will only be updated in case the
				// swipe position is outside the touch down cell.
				mCurrentSwipePositionDigit = DIGIT_UNDETERMINED;
				return false;
			}

			// The swipe motion for a higher size grid, is released in an outer
			// cell of the grid or just outside the grid. In this case it is not
			// checked whether the touch down cell has been left as it is
			// sometimes more difficult to select the digit due to a smaller
			// border around the grid (especially when using a bumper case to
			// protect the device).
		}

		// Compute the length of the swipe line. In case the current swipe
		// position is inside the touch down cell the distance is measured
		// between the *real* touch down position and the current position.
		// In case the current swipe position is outside the touch down cell
		// than the distance is measured between the center of the touch down
		// cell and the current position.
		float deltaX = mCurrentSwipePositionPixelCoordinates[X_POS]
				- (inTouchDownCell ? getTouchDownPixelCoordinate(X_POS)
						: mTouchDownCellCenterPixelCoordinates[X_POS]);
		float deltaY = mCurrentSwipePositionPixelCoordinates[Y_POS]
				- (inTouchDownCell ? getTouchDownPixelCoordinate(Y_POS)
						: mTouchDownCellCenterPixelCoordinates[Y_POS]);
		if (Math.sqrt(deltaX * deltaX + deltaY * deltaY) < 10) {
			// The distance is too small to be accepted.
			if (DEBUG_SWIPE_MOTION) {
				Log.i(TAG, " - deltaX = " + deltaX + " - deltaY = " + deltaY);
			}
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
				Log
						.i(TAG,
								"Current swipe position inside touch down cell. Angle computed to real touch down position");
				Log.i(TAG, " - start = (" + getTouchDownPixelCoordinate(X_POS)
						+ ", " + getTouchDownPixelCoordinate(Y_POS) + ")");
			} else {
				Log
						.i(TAG,
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

		if (digit <= mGridSize) {
			// Determine whether the digit should be accepted based on the
			// current swipe position.
			boolean acceptDigit = (!inTouchDownCell);
			if (!acceptDigit) {
				// Normally the digit is not accepted in case the swipe motion
				// is inside the touch down cell. In case a swipe motion is
				// started and ended in a cell on the outer edge of the grid,
				// the digit will be accepted in case the swipe motion was
				// heading outside the grid.
				//
				// In portrait mode the swipe motions to the left and to the
				// right of the grid view needs to be examined. Swipe motions to
				// the top can be neglected as the action bar is displayed above
				// the grid. Swipe motions to the bottom can be neglected as the
				// clear and undo buttons are shown below the grid view.
				//
				// In portrait mode the swipe motions to the left and to the
				// bottom of the grid view needs to be examined. Swipe motions
				// to the top can be neglected as the action bar is displayed
				// above the grid. Swipe motions to the right can be neglected
				// as the clear and undo buttons are shown to the right of the
				// grid view.
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
				// Call listener only in case previously no digit was
				// selectable.
				if (mListener != null
						&& mCurrentSwipePositionDigit == DIGIT_UNDETERMINED) {
					mListener.onSelectableDigit();
				}

				// Set the digit for the current swipe position.
				mCurrentSwipePositionDigit = digit;
				return true;
			}
		}

		// The current swipe position does not correspond with a selectable
		// digit for this grid
		// size.
		if (DEBUG_SWIPE_MOTION) {
			Log
					.i(TAG,
							"Current swipe position does not correspond with a selectable digit.");
		}

		// Call listener only in case previously a digit was selectable.
		if (mListener != null
				&& mCurrentSwipePositionDigit != DIGIT_UNDETERMINED) {
			mListener.onNoSelectableDigit();
		}

		// Set the digit for the current swipe position.
		mCurrentSwipePositionDigit = DIGIT_UNDETERMINED;
		return false;
	}

	/**
	 * Checks whether the current swipe position needs to be updated.
	 * 
	 * @return True in case the current swipe position needs to be updated.
	 *         False otherwise.
	 */
	boolean needToUpdateCurrentSwipePosition() {
		if (mStatus == Status.INIT) {
			// Swipe motion has not started inside grid cell. Nothing to do
			// here.
			return false;
		}

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
	 * Checks whether the current swipe position results in another digit
	 * compared to the previous swipe position.
	 * 
	 * @return True in case the swipe motion will result in another digit being
	 *         selected compared to the previous swipe position. False if still
	 *         the same digit will be selected.
	 */
	boolean hasChangedDigit() {
		return (mPreviousSwipePositionDigit != mCurrentSwipePositionDigit);
	}

	/**
	 * Return the digit which is associated with the current swipe position.
	 * 
	 * @return The digit which is associated with the current swipe position.
	 */
	public int getFocusedDigit() {
		return mCurrentSwipePositionDigit;
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

	/**
	 * Sets the listener for the swipe motion.
	 * 
	 * @param listener
	 *            The listener to be used.
	 */
	public void setOnUpdateListener(Listener listener) {
		mListener = listener;
	}
}