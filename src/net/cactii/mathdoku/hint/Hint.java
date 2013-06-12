package net.cactii.mathdoku.hint;

import android.os.Handler;

/**
 * This class can be used to display a hint text using a l
 *
 */
public class Hint {
	// Count total number of hints displayed since start of app.
	private static long mHintCounter = 0;

	// The hint which is initially displayed.
	private String mHint;

	// Counter for the number of times the hint has been repeated.
	private int mLoopCounter;

	// Intervals for repeating and erasing hints.
	private long mInitialDelay;
	private long mRepeatDelay;
	private long mEraseDelay;

	// The listener which has to be informed if the hint has been set.
	private OnHintChangedListener mOnHintChangedListener;
	
	public Hint(OnHintChangedListener onHintChangedListener) {
		mOnHintChangedListener = onHintChangedListener;

		mLoopCounter = 0;
		mInitialDelay = 0;
		mRepeatDelay = 0;
		mEraseDelay = 0;
	}

	/**
	 * Get the number of times the loop has been repeated.
	 * 
	 * @return The number of times the loop has been repeated.
	 */
	public int getRepetitionCounter() {
		return mLoopCounter;
	}

	/**
	 * Checks whether the hint loop should be repeated one more time. Needs to
	 * be overridden.
	 * 
	 * @return True in case the hint has to be repeated. False if the hint loop
	 *         should stop.
	 */
	public boolean repeat() {
		return false;
	}

	/**
	 * Get the hint text to be displayed when the hint loop is repeated.
	 * Override this method to determine this text dynamically on each
	 * repetition of the hint loop.
	 * 
	 * @return The hint text to be displayed.
	 */
	public String getNextHint() {
		return mHint;
	}

	/**
	 * Sets the delay before the initial hint is shown.
	 * 
	 * @param delay
	 *            Duration in milliseconds.
	 * @return Return the {@see Hint} object itself.
	 */
	public Hint setInitialDelay(long delay) {
		mInitialDelay = delay;

		return this;
	}

	/**
	 * Sets the delay before the hint is repeated.
	 * 
	 * @param delay
	 *            Duration in milliseconds.
	 * @return Return the {@see Hint} object itself.
	 */
	public Hint setRepeatDelay(long delay) {
		mRepeatDelay = delay;

		return this;
	}

	/**
	 * Sets the delay before the hint is erased.
	 * 
	 * @param delay
	 *            Duration in milliseconds.
	 * @return Return the {@see Hint} object itself.
	 */
	public Hint setEraseDelay(long delay) {
		mEraseDelay = delay;

		return this;
	}

	/**
	 * Shows the hint.
	 * 
	 * @param hint
	 *            The hint text to be shown.
	 */
	public void show(final String hint) {
		if (mOnHintChangedListener == null) {
			return;
		}

		// Increase the hint counter and store this a the id for the hint which
		// will be displayed.
		final long hintCounter = ++mHintCounter;

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// In case no new hint has been displayed, it is assume that
				// this hint is still displayed.
				if (hintCounter == mHintCounter) {
					// Display the initial hint.
					mOnHintChangedListener.setHint(hint);
					
					if (mRepeatDelay > 0) {
						executeRepeat(hintCounter);
					} else if (mEraseDelay > 0) {
						executeErase(hintCounter);
					}
				}
			}
		}, mInitialDelay);


	}
	
	/**
	 * Repeat the hint if applicable.
	 */
	private void executeRepeat(final long hintCounter) {
		// Start repeat loop or erase delay
		if (mRepeatDelay > 0) {
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// In case no new hint has been displayed, it is assume that
					// this hint is still displayed.
					if (hintCounter == mHintCounter) {
						if (repeat()) {
							mOnHintChangedListener.setHint(getNextHint());
							
							// Repeat this check until the swype motion has
							// finished.
							mLoopCounter++;
							handler.postDelayed(this, mRepeatDelay);

						} else if (mEraseDelay > 0) {
							executeErase(hintCounter);
						}
					}
				}
			}, mRepeatDelay);
		} else if (mEraseDelay > 0) {
			executeErase(hintCounter);
		}
	}

	/**
	 * Erase the hint is applicable.
	 * 
	 * @param hintCounter
	 */
	private void executeErase(final long hintCounter) {
		if (mEraseDelay > 0) {
			// Create a handler to erase the hint and post this handler with the
			// delay.
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// In case no new hint has been displayed, it is assume that
					// this hint is still displayed.
					if (hintCounter == mHintCounter) {
						mOnHintChangedListener.setHint(null);
					}
				}
			}, mEraseDelay);
		}
	}
}
