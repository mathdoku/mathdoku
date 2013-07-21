package net.cactii.mathdoku.hint;

import java.util.ArrayList;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.painter.Painter;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TickerTape extends HorizontalScrollView {
	public final static String TAG = "MathDoku.TickerTape";

	private LinearLayout mTickerTapeLinearLayout;

	// Context in which the ticker tape is created.
	private Context mContext;

	// Size of text within the ticker tape.
	private int mTextSize;

	// Speed of horizontal scrolling
	private int mScrollStepSize = 100;

	// Flag whether ticker tape has been cancelled and should stop moving.
	private boolean mCancelled;

	// The number of items which have been displayed completely.
	private int mCountDisplayedItems;

	// The time at which the ticker tape can be erased.
	private boolean mStartedMoving;
	private long mStartTime;

	// Conditions which need to be satisfied for automatic removal of ticker
	// tape
	private boolean mEraseConditionSet;
	private int mMinDisplayCycles;
	private int mMinDisplayItems;
	private long mMinDisplayTime;

	private ArrayList<TextView> mTextViewList = new ArrayList<TextView>();

	/**
	 * Creates a new instance of {@see TickerTape}.
	 * 
	 * @param mContext
	 *            The context in which the ticker tape is created.
	 */
	public TickerTape(Context context) {
		super(context);

		// Save the context for later use
		mContext = context;

		// Determine text size to be used
		mTextSize = (int) ((mContext.getResources().getDimension(
				R.dimen.controls_text_size_default) / getResources()
				.getDisplayMetrics().density) + 0.5f);

		// Set layout parameters of the horizontal scroll view.
		setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

		// Add a linear layout to the scroll view which will hold all the text
		// view for all items
		mTickerTapeLinearLayout = new LinearLayout(mContext);
		mTickerTapeLinearLayout.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		addView(mTickerTapeLinearLayout);

		// disable scroll bar
		setHorizontalScrollBarEnabled(false);

		// set color
		setBackgroundColor(Painter.getInstance().getTickerTapePainter()
				.getBackgroundColor());

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
		textView.setTextSize(mTextSize);
		textView.setId(mTextViewList.size());
		textView.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.setPadding(0, 0, 4 * mScrollStepSize, 0);
		textView.setTextColor(Painter.getInstance().getTickerTapePainter()
				.getTextColor());

		// Add the text view to the layout and the list of text views.
		mTickerTapeLinearLayout.addView(textView);
		mTextViewList.add(textView);

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
	 * Show the ticker tape and start moving the items in the ticker tape.
	 */
	public void show() {
		startMoving();
	}

	/**
	 * Hides the ticker tape and stop moving the items in the ticker tape.
	 */
	public void hide() {
		setVisibility(View.INVISIBLE);
		cancel();
		invalidate();
	}

	/**
	 * Starts moving the ticker tape.
	 * 
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	private TickerTape startMoving() {
		mCancelled = false;

		// Determine how many items have to be displayed at least.
		mMinDisplayItems = mMinDisplayCycles * mTextViewList.size();

		// In case the only one item has been added, this item is duplicated. In
		// this way the the message nicely scrolls away on the left while the
		// duplicate message flows in from the right.
		if (mTextViewList.size() == 1) {
			addItem(mTextViewList.get(0).getText().toString());
		}

		// The ticker tape will start moving with a small delay.
		mStartedMoving = false;

		// Define handler and runnable for repeatable updating of the ticker
		// tape.
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if (!isCancelled()) {
					if (!mStartedMoving) {
						// First display the message and do not move yet.
						mStartedMoving = true;
						mStartTime = System.currentTimeMillis();
						mCountDisplayedItems = 0;
						setVisibility(View.VISIBLE);
					} else {
						// Update the scroll view
						moveToNextPosition();

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
					if (!mCancelled) {
						handler.postDelayed(this, 400);
					}
				}

			}
		};

		// Start moving the ticker tape after an initial delay
		handler.postDelayed(runnable, 300);

		return this;
	}

	/**
	 * Update the scroll position to the next position.
	 */
	private boolean moveToNextPosition() {
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
				mCountDisplayedItems++;

				// Reset scroll position to beginning of the first item.
				scrollTo(0, 0);
			}
		}

		return true;
	}

	/**
	 * Cancel updates of the ticker tape.
	 */
	public void cancel() {
		mCancelled = true;
	}

	/**
	 * Checks whether the ticker tape has been cancelled.
	 * 
	 * @return True is the ticker tape was cancelled. False otherwise.
	 */
	public boolean isCancelled() {
		return mCancelled;
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
	/**
	 * @return
	 */
	public TickerTape setEraseConditions(int minDisplayCycles,
			long minDisplayTime) {
		mEraseConditionSet = true;
		mMinDisplayCycles = minDisplayCycles;
		mMinDisplayTime = minDisplayTime;

		return this;
	}

}
