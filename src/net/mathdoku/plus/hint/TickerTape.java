package net.mathdoku.plus.hint;

import java.util.ArrayList;

import net.mathdoku.plus.R;
import net.mathdoku.plus.painter.Painter;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TickerTape extends HorizontalScrollView {
	public final static String TAG = "MathDoku.TickerTape";

	private final LinearLayout mTickerTapeLinearLayout;

	// Context in which the ticker tape is created.
	private final Context mContext;

	// Size of text within the ticker tape.
	private final int mTextSizeInDIP;

	// Margins of text within the ticker tape.
	private final int mTextTopMargin;
	private final int mTextBottomMargin;

	// Speed of horizontal scrolling
	private final int mScrollStepSize = 100;

	// Conditions which need to be satisfied for automatic removal of ticker
	// tape
	private boolean mEraseConditionSet;
	private int mMinDisplayCycles;
	private int mMinDisplayItems;
	private long mMinDisplayTime;

	// Ticker tape is disabled;
	private boolean mDisabled;

	// The runnable which will update the ticker tape at regular intervals
	private class TickerTapeUpdaterRunnable implements Runnable {
		// The time at which this runnable was started
		private boolean mStartedMoving = false;
		private long mStartTime;

		// Flag whether this runnable should be cancelled.
		private boolean mIsCancelled = false;

		// The number of items which have been displayed completely.
		private int mCountDisplayedItems;

		@Override
		public void run() {
			if (!mIsCancelled) {
				if (!mStartedMoving) {
					// First display the message and do not move yet.
					mStartedMoving = true;
					mStartTime = System.currentTimeMillis();
					mCountDisplayedItems = 0;
					setVisibility(View.VISIBLE);
				} else {
					// Update the scroll view
					mCountDisplayedItems += moveToNextPosition();

					// Repeat after a short while unless canceled and the
					// erase conditions have not yet been met.
					if (mEraseConditionSet
							&& mCountDisplayedItems >= mMinDisplayItems
							&& System.currentTimeMillis() >= mStartTime
									+ mMinDisplayTime) {
						hide();
						return;
					}
				}

				// Repeat unless cancelled in between.
				if (!mIsCancelled && mTickerTapeUpdaterHandler != null) {
					mTickerTapeUpdaterHandler.postDelayed(this, 400);
				}
			}
		}

		/**
		 * Inform the runnable that it should be cancelled as soon as possible.
		 */
		public void cancel() {
			mIsCancelled = true;
		}

		/**
		 * Checks whether the runnable will be cancelled on the next occasion.
		 * 
		 * @return True in case the runnable will be cancelled on the next
		 *         occasion. False otherwise.
		 */
		public boolean isCancelled() {
			return mIsCancelled;
		}
	}

	// The handler and runnable which takes care of updating the ticker tape
	private Handler mTickerTapeUpdaterHandler;
	private TickerTapeUpdaterRunnable mTickerTapeUpdaterRunnable;

	private final ArrayList<TextView> mTextViewList = new ArrayList<TextView>();

	/**
	 * Creates a new instance of {@see TickerTape}.
	 * 
	 * @param context
	 *            The context in which the ticker tape is created.
	 */
	public TickerTape(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Save the context for later use
		mContext = context;

		// Determine text size
		int textSizeInPixels = getResources().getDimensionPixelSize(
				R.dimen.text_size_default);
		mTextSizeInDIP = (int) (getResources().getDimension(
				R.dimen.text_size_default) / getResources().getDisplayMetrics().density);

		// Determine margins for text (25% of text height)
		mTextBottomMargin = mTextTopMargin = textSizeInPixels / 4;

		// Set layout parameters of the horizontal scroll view.
		setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				mTextTopMargin + textSizeInPixels + mTextBottomMargin));

		// Add a linear layout to the scroll view which will hold all the text
		// view for all items.
		mTickerTapeLinearLayout = new LinearLayout(mContext);
		mTickerTapeLinearLayout.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				mTextTopMargin + textSizeInPixels + mTextBottomMargin));
		addView(mTickerTapeLinearLayout);

		// disable scroll bar
		setHorizontalScrollBarEnabled(false);

		// set color
		setBackgroundColor(Painter.getInstance().getTickerTapePainter()
				.getBackgroundColor());

		mDisabled = false;
		// Hide by default
		setVisibility(View.INVISIBLE);

		// By default no conditions for automatic erasing will be set
		mEraseConditionSet = false;
	}

	/**
	 * Adds a new string to the end of the ticker tape.
	 * 
	 * @param string
	 *            The string to be added.
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	public TickerTape addItem(String string) {
		// Create a new text view for this item
		TextView textView = new TextView(mContext);
		textView.setText(string);

		// Use a DIP text size as the height of ticker tape layout has been set
		// to a fixed height.
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mTextSizeInDIP);

		textView.setId(mTextViewList.size());
		textView.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		textView.setPadding(0, 0, 4 * mScrollStepSize, 0);
		textView.setTextColor(Painter.getInstance().getTickerTapePainter()
				.getTextColor());

		// Add the text view to the layout and the list of text views.
		mTickerTapeLinearLayout.addView(textView);
		mTextViewList.add(textView);

		invalidate();

		return this;
	}

	/**
	 * Adds multiple strings to the end of the ticker tape.
	 * 
	 * @param string
	 *            The strings to be added.
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	public TickerTape addItems(String[] string) {
		// Add a text view to the linear layout for each item
		for (int i = 0; i < string.length; i++) {
			addItem(string[i]);
		}

		return this;
	}

	/**
	 * Sets whether the ticker tape is completely disabled.
	 * 
	 * @param disabled
	 */
	public void setDisabled(boolean disabled) {
		mDisabled = disabled;
		if (mDisabled) {
			hide();
		}
	}

	/**
	 * Show the ticker tape and start moving the items in the ticker tape.
	 */
	public void show() {
		if (!mDisabled) {
			startMoving();
		}
	}

	/**
	 * Hides the ticker tape and stop moving the items in the ticker tape.
	 */
	public void hide() {
		if (mDisabled) {
			setVisibility(View.GONE);
		} else {
			setVisibility(View.INVISIBLE);
		}
		cancel();
		invalidate();
	}

	/**
	 * Starts moving the ticker tape.
	 * 
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	public TickerTape startMoving() {

		if (mDisabled) {
			return this;
		}

		// Determine how many items have to be displayed at least.
		mMinDisplayItems = mMinDisplayCycles * mTextViewList.size();

		// In case the only one item has been added, this item is duplicated. In
		// this way the the message nicely scrolls away on the left while the
		// duplicate message flows in from the right.
		if (mTextViewList.size() == 1) {
			addItem(mTextViewList.get(0).getText().toString());
		}

		// Define handler and runnable for repeatable updating of the ticker
		// tape.
		if (mTickerTapeUpdaterHandler == null) {
			mTickerTapeUpdaterHandler = new Handler();
		}
		if (mTickerTapeUpdaterRunnable == null
				|| mTickerTapeUpdaterRunnable.isCancelled()) {
			mTickerTapeUpdaterRunnable = new TickerTapeUpdaterRunnable();
		}

		// Start moving the ticker tape after an initial delay
		mTickerTapeUpdaterHandler.postDelayed(mTickerTapeUpdaterRunnable, 300);

		return this;
	}

	/**
	 * Update the scroll position to the next position.
	 * 
	 * @return 1 in case the next item is displayed. 0 in case the current item
	 *         is moved.
	 */
	private int moveToNextPosition() {
		boolean displayNextItem = false;

		// Only move the scroll position in case the total width of the
		// containing text views is greater than the available width.
		if (computeHorizontalScrollRange() > getWidth()) {

			// Determine next scroll position
			int newScrollPos = getScrollX() + mScrollStepSize;

			// Scroll to new position
			scrollTo(newScrollPos, 0);

			// Change order of items in scroll view and items list.
			if (newScrollPos >= mTextViewList.get(0).getWidth()
					|| getScrollX() < newScrollPos) {
				// Check whether the scroll position has been set on the
				// requested position (getScrollX() < newScrollPos) is a hack.
				// Without this check the ticker will stop updating the long
				// item as soon as the visible part of this long item plus the
				// total width of all other items is less than the display
				// width.

				// Show next item in list if applicable
				if (mTextViewList.size() > 1) {
					TextView textView = mTextViewList.get(0);

					// Remove the item which currently is at front of the list.
					mTickerTapeLinearLayout.removeView(textView);
					mTextViewList.remove(textView);

					// Than add the item again to the end.
					mTickerTapeLinearLayout.addView(textView);
					mTextViewList.add(textView);
				}
				displayNextItem = true;

				// Reset scroll position to beginning of the first item.
				scrollTo(0, 0);
			}
		}

		return (displayNextItem ? 1 : 0);
	}

	/**
	 * Cancel updates of the ticker tape.
	 */
	public void cancel() {
		if (mTickerTapeUpdaterRunnable != null) {
			mTickerTapeUpdaterRunnable.cancel();
		}
	}

	/**
	 * Set the conditions after which the ticker tape should be erased
	 * automatically. Note: all conditions have to be met before the ticker tape
	 * is erased.
	 * 
	 * @param minDisplayCycles
	 *            The minimum number of times each message has to be displayed.
	 * @param minDisplayTime
	 *            The minimum amount of milliseconds the ticker tape has to be
	 *            displayed.
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	public TickerTape setEraseConditions(int minDisplayCycles,
			long minDisplayTime) {
		mEraseConditionSet = true;
		mMinDisplayCycles = minDisplayCycles;
		mMinDisplayTime = minDisplayTime;

		return this;
	}

	/**
	 * Resets the ticker tape. All messages will be cleared and the ticker tape
	 * is hidden.
	 * 
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	public TickerTape reset() {
		// Hide the ticker tape and Cancel updating
		hide();

		// Clear the list
		while (mTextViewList.size() > 0) {

			// Remove the item which currently is at front of the list.
			TextView textView = mTextViewList.get(0);
			mTickerTapeLinearLayout.removeView(textView);
			mTextViewList.remove(0);
		}

		return this;
	}
}
