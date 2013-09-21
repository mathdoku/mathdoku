package net.mathdoku.battle.grid.ui;

import android.view.MotionEvent;

public class Motion {
	private long mDoubleTapTouchDownTime;
	protected boolean mDoubleTapDetected;

	public Motion() {
		mDoubleTapTouchDownTime = 0;
	}

	/**
	 * Register the touch down event.
	 * 
	 * @param event
	 *            The event which is registered as the touch down event of the
	 *            swipe motion.
	 * @return True in case a grid cell has been touched. False otherwise.
	 */
	protected void setTouchDownEvent(MotionEvent event) {
		long timeSincePreviousEvent = event.getEventTime()
				- mDoubleTapTouchDownTime;
		if (timeSincePreviousEvent > 0 && timeSincePreviousEvent < 300) {
			// A double tap is only allowed in case the total time
			// between touch down of the first swipe motion until release of
			// the second swipe motion took less than 300 milliseconds.
			mDoubleTapDetected = true;
			mDoubleTapTouchDownTime = 0;
		} else {
			// Too slow for being recognized as double tap. Use touch
			// down time of this swipe motion as new start time of the
			// double tap event.
			if (timeSincePreviousEvent > 0) {
				mDoubleTapTouchDownTime = event.getDownTime();
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
}
