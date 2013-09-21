package net.mathdoku.battle.grid.ui;

import android.view.MotionEvent;

public class Motion {
	// Registration of event time to detect a double tap on the same touch down
	// cell. It is kept statically in order to compare with the previous swipe
	// motion.
	static private long mDoubleTapTouchDownTime = 0;
	protected boolean mDoubleTapDetected;

	/**
	 * Register the touch down event.
	 * 
	 * @param event
	 *            The event which is registered as the touch down event of the
	 *            swipe motion.
	 * @return True in case a grid cell has been touched. False otherwise.
	 */
	protected void setTouchDownEvent(MotionEvent event) {
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
