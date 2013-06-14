package net.cactii.mathdoku.hint;

import java.util.ArrayList;

import net.cactii.mathdoku.R;
import android.content.Context;
import android.content.res.ColorStateList;
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

	ArrayList<TextView> mTextViewList = new ArrayList<TextView>();

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
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		// Add a linear layout to the scroll view which will hold all the text
		// view for all items
		mTickerTapeLinearLayout = new LinearLayout(mContext);
		mTickerTapeLinearLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		addView(mTickerTapeLinearLayout);

		// disable scroll bar
		setHorizontalScrollBarEnabled(false);

		// set color
		setBackgroundColor(0xFF33B5E5); // TODO: retrieve from painter

		// Hide by default
		setVisibility(View.INVISIBLE);
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
		textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		textView.setPadding(0, 0, 4 * mScrollStepSize, 0);
		textView.setTextColor(0xFFFFFFFF); 

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
		setVisibility(View.VISIBLE);

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
	 * Starts moving the ticket tape.
	 * 
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	private TickerTape startMoving() {
		mCancelled = false;

		if (mTextViewList.size() == 1) {
			addItem(mTextViewList.get(0).getText().toString());
		}
		
		// Define handler and runnable for repeatable updating of the ticker
		// tape.
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {
				// Update the scroll view
				moveToNextPosition();

				// Repeat after a short while
				if (!mCancelled) {
					handler.postDelayed(this, 400);
				}
			}
		};

		// Start moving the ticker tape after an initial delay
		handler.postDelayed(runnable, 700);

		return this;
	}

	/**
	 * Update the scroll position to the next position.
	 */
	private void moveToNextPosition() {
		// Only move the scroll position in case the total width of the containing text views is greater than the available width.
		if (computeHorizontalScrollRange() > getWidth()) {
			
			// Determine next scroll position
			int newScrollPos = getScrollX() + mScrollStepSize; 

			// Scroll to new position
			scrollTo(newScrollPos, 0);

			// Change order of items in scroll view and items list.
			if (newScrollPos >= mTextViewList.get(0).getWidth()
					|| getScrollX() < newScrollPos) {
				// Check whether the scroll position has been set on the
				// requested
				// position (getScrollX() < newScrollPos) is a hack. Without
				// this
				// check the ticker will stop updating the long item as soon as
				// the
				// visible part of this long item plus the total width of all
				// other
				// items is less than the display width.

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

				// Reset scroll position to beginning of the first item.
				scrollTo(0, 0);
			}
		}
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
	 * Earse the ticker tape after the given delay.
	 * 
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	public TickerTape setEraseDelay(long delay) {
		postDelayed(new Runnable() {
			@Override
			public void run() {
				hide();
			}
		}, delay);

		return this;
	}

}
