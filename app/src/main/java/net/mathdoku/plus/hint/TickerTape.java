package net.mathdoku.plus.hint;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.mathdoku.plus.R;
import net.mathdoku.plus.painter.Painter;

import java.util.ArrayList;
import java.util.List;

public class TickerTape extends HorizontalScrollView {
	@SuppressWarnings("unused")
	private static final String TAG = TickerTape.class.getName();

	private static final int SCROLL_STEP_SIZE = 100;

	// Private package access for unit testing.
	static final int ID_VIEW_TICKER_TAPE_LINEAR_LAYOUT = 1012;

	private final Context context;
	private final LinearLayout tickerTapeLinearLayout;
	private final int textSizeInDIP;
	private boolean isEraseConditionSet;
	private int minDisplayCycles;
	private long minDisplayTime;
	private boolean isDisabled;
	private TickerTapeUpdater tickerTapeUpdater;
	private final List<TextView> textViewList = new ArrayList<TextView>();

	/**
	 * Creates a new instance of {@see TickerTape}.
	 * 
	 * @param context
	 *            The context in which the ticker tape is created.
	 */
	public TickerTape(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Save the context for later use
		this.context = context;

		// Determine text size
		// noinspection ConstantConditions,ConstantConditions
		int textSizeInPixels = getResources().getDimensionPixelSize(
				R.dimen.text_size_default);
		textSizeInDIP = (int) (getResources().getDimension(
				R.dimen.text_size_default) / getResources().getDisplayMetrics().density);

		// The total vertical margin is 50% of the text height. As the text will
		// be vertically centered, approximately a margin of 25% of the text
		// height will be visible both above and below the text.
		int mTextVerticalMargin = textSizeInPixels / 2;

		// Set layout parameters of the horizontal scroll view.
		setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				textSizeInPixels + mTextVerticalMargin));

		// Add a linear layout to the scroll view which will hold all the text
		// view for all items.
		tickerTapeLinearLayout = new LinearLayout(this.context);
		tickerTapeLinearLayout.setId(ID_VIEW_TICKER_TAPE_LINEAR_LAYOUT);
		tickerTapeLinearLayout.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				textSizeInPixels + mTextVerticalMargin));
		addView(tickerTapeLinearLayout);

		setHorizontalScrollBarEnabled(false);
		setBackgroundColor(Painter
				.getInstance()
				.getTickerTapePainter()
				.getBackgroundColor());

		setVisibility(View.INVISIBLE);
		isDisabled = false;
		isEraseConditionSet = false;
	}

	/**
	 * Adds a new string to the end of the ticker tape.
	 * 
	 * @param string
	 *            The string to be added.
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	public TickerTape addItem(String string) {
		TextView textView = createItemTextView(string);
		tickerTapeLinearLayout.addView(textView);
		textViewList.add(textView);

		invalidate();

		return this;
	}

	private TextView createItemTextView(String string) {
		TextView textView = new TextView(context);
		textView.setText(string);

		// Ticker tape layout has been set to a fixed height, so use DIP text
		// size
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeInDIP);

		textView.setId(textViewList.size());
		textView.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		textView.setPadding(0, 0, 4 * SCROLL_STEP_SIZE, 0);
		textView.setTextColor(Painter
				.getInstance()
				.getTickerTapePainter()
				.getTextColor());

		return textView;
	}

	/**
	 * Disables the ticker tape completely.
	 * 
	 */
	public void disable() {
		isDisabled = true;
		hide();
	}

	/**
	 * Show the ticker tape and start moving the items in the ticker tape.
	 */
	public void show() {
		if (isDisabled) {
			return;
		}

		// In case only one item has been added, this item is duplicated. In
		// this way the message nicely scrolls away on the left while the
		// duplicate message flows in from the right.
		if (textViewList.size() == 1) {
			addItem(getCurrentItem());
		}

		if (tickerTapeUpdater == null || tickerTapeUpdater.isCancelled()) {
			tickerTapeUpdater = createTickerTapeUpdaterRunnable();
		}
		tickerTapeUpdater.startMoving();
	}

	// Package private access for unit testing
	TickerTapeUpdater createTickerTapeUpdaterRunnable() {
		return new TickerTapeUpdater(this);
	}

	/**
	 * Hides the ticker tape and stops moving the items in the ticker tape.
	 */
	void hide() {
		if (isDisabled) {
			setVisibility(View.GONE);
		} else {
			setVisibility(View.INVISIBLE);
		}
		cancel();
		invalidate();
	}

	/**
	 * Update the scroll position to the next position.
	 */
	void moveToNextPosition() {
		if (hasNeedForScrolling() && !scrollToNextPositionInCurrentItem()) {
			moveCurrentItemToEndOfList();

			// Reset scroll position to beginning of the new current item.
			scrollTo(0, 0);
		}
	}

	private boolean hasNeedForScrolling() {
		return computeHorizontalScrollRange() > getWidth();
	}

	String getCurrentItem() {
		return textViewList.isEmpty() ? null : textViewList
				.get(0)
				.getText()
				.toString();
	}

	/**
	 * Scrolls to the next position.
	 * 
	 * @return True is the next scroll position is valid for the current item.
	 *         False otherwise.
	 */
	private boolean scrollToNextPositionInCurrentItem() {
		int newScrollPos = getNextScrollPosition();
		scrollTo(newScrollPos, 0);

		// Check whether the scroll position has been set on the requested
		// position. In case getScrollX() < newScrollPos the scroll position is
		// only moved partially and the next item has to be displayed.
		return newScrollPos <= textViewList.get(0).getWidth()
				&& getScrollX() >= newScrollPos;
	}

	private int getNextScrollPosition() {
		return getScrollX() + SCROLL_STEP_SIZE;
	}

	private void moveCurrentItemToEndOfList() {
		if (textViewList.size() > 1) {
			TextView textView = textViewList.get(0);

			// Remove the item which currently is at front of the list.
			tickerTapeLinearLayout.removeView(textView);
			textViewList.remove(textView);

			// Than add the item again to the end.
			tickerTapeLinearLayout.addView(textView);
			textViewList.add(textView);
		}
	}

	/**
	 * Cancel updates of the ticker tape.
	 */
	public void cancel() {
		if (tickerTapeUpdater != null) {
			tickerTapeUpdater.cancel();
			tickerTapeUpdater = null;
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
	@SuppressWarnings("SameParameterValue")
	public TickerTape setEraseConditions(int minDisplayCycles,
			long minDisplayTime) {
		isEraseConditionSet = true;
		this.minDisplayCycles = minDisplayCycles;
		this.minDisplayTime = minDisplayTime;

		return this;
	}

	/**
	 * Resets the ticker tape. All messages will be cleared and the ticker tape
	 * is hidden.
	 * 
	 * @return The ticker tape object itself so it can be used as a builder.
	 */
	public TickerTape reset() {
		hide();
		tickerTapeLinearLayout.removeAllViewsInLayout();
		textViewList.clear();

		return this;
	}

	/**
	 * Checks if the eraseCondition is set and whether it is fulfilled already.
	 * 
	 * @param countItemsDisplayedCompletely
	 *            The number of items which have been displayed completely.
	 * @param actualDisplayTime
	 *            The time the ticker tape has been displayed actually.
	 * @return True in case the erase condition is set and fulfilled. False
	 *         otherwise.
	 */
	boolean isEraseConditionFulfilled(int countItemsDisplayedCompletely,
			long actualDisplayTime) {
		return isEraseConditionSet
				&& countItemsDisplayedCompletely >= minDisplayCycles
						* textViewList.size()
				&& actualDisplayTime >= minDisplayTime;

	}
}
