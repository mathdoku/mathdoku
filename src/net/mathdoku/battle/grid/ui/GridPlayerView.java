package net.mathdoku.battle.grid.ui;

import net.mathdoku.battle.R;
import net.mathdoku.battle.grid.Grid;
import net.mathdoku.battle.grid.GridCell;
import net.mathdoku.battle.hint.TickerTape;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

public class GridPlayerView extends GridBasePlayerView {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridPlayerView";

	// Reference to the last swipe motion which was started.
	private SwipeMotion mSwipeMotion;

	// Handler and runnable for touch actions which need a delay
	private SwipeBorderDelayRunnable mSwipeBorderDelayRunnable;

	// Reference to the last ticker tape started by the grid view player.
	TickerTape mTickerTape;

	public GridPlayerView(Context context) {
		super(context);
		initGridView(context);
	}

	public GridPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGridView(context);
	}

	public GridPlayerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGridView(context);
	}

	private void initGridView(Context context) {
		mTickerTape = null;

		// Initialize the runnables used to delay touch handling
		mSwipeBorderDelayRunnable = new SwipeBorderDelayRunnable();
	}

	public void setOnGridTouchListener(OnGridTouchListener listener) {
		this.mTouchedListener = listener;
	}

	public abstract class OnGridTouchListener {
		public abstract void gridTouched(GridCell cell);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if (mGrid == null || !mGrid.isActive())
			return false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mSwipeMotion == null) {
				mSwipeMotion = new SwipeMotion(this, mBorderWidth,
						mGridCellSize);
			}

			mSwipeMotion.setTouchDownEvent(event);
			if (mSwipeMotion.isDoubleTap()) {
				toggleInputMode();
				mSwipeMotion.clearDoubleTap();
			} else if (mSwipeMotion.isTouchDownInsideGrid()) {
				// Select the touch down cell.
				GridCell selectedCell = mGrid.setSelectedCell(mSwipeMotion
						.getTouchDownCellCoordinates());

				// Inform listener of puzzle fragment of start of motion
				// event.
				mTouchedListener.gridTouched(selectedCell);

				// Show the basic swipe hint. Replace this hint after a short
				// pause.
				clearTickerTape();
				if (mPreferences.getSwipeValidMotionCounter() < 30) {
					setTickerTapeOnCellDown();
				}

				// Prevent displaying the swipe circle in case the user makes a
				// very fast swipe motion by delaying the invalidate. Do not
				// cancel this runnable as it is needed to finish the swipe
				// motion.
				mTouchHandler.postDelayed(mSwipeBorderDelayRunnable, 100);
			}
			return super.onTouch(arg0, event);
		case MotionEvent.ACTION_UP:
			if (super.onTouch(arg0, event) == false) {
				if (this.mTouchedListener != null && mSwipeMotion != null) {
					playSoundEffect(SoundEffectConstants.CLICK);
					mSwipeMotion.release(event);
					int swipeDigit = mSwipeMotion.getFocussedDigit();
					if (swipeDigit >= 1 && swipeDigit <= mGridSize) {
						// A swipe motion for a valid digit was completed.

						// Set the swipe digit as selected value for the cell
						// which
						// was initially touched.
						digitSelected(swipeDigit);

						clearTickerTape();
						if (mPreferences
								.increaseSwipeValidMotionCounter(swipeDigit) <= 6) {
							mTickerTape
									.addItem(
											getResources()
													.getString(
															R.string.hint_swipe_valid_digit_completed,
															swipeDigit))
									.setEraseConditions(2, 3000).show();
						}
					} else {
						clearTickerTape();
						if (swipeDigit > mGridSize
								&& mPreferences
										.increaseSwipeInvalidMotionCounter() <= 6) {
							mTickerTape
									.addItem(
											getResources()
													.getString(
															R.string.hint_swipe_invalid_digit_completed))
									.setEraseConditions(1, 3000).show();
						}
					}

					// Inform listener of puzzle fragment about the release
					// action
					mTouchedListener.gridTouched(mGrid.getSelectedCell());

					// Update to remove the swipe line
					invalidate();
				}
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			super.onTouch(arg0, event);
			if (mSwipeMotion != null) {
				// Update current swipe position
				mSwipeMotion.update(event);

				// Check if the grid view has to be invalidated
				int swipeDigit = mSwipeMotion.getFocussedDigit();
				if (swipeDigit >= 1 && swipeDigit <= 9
						&& mSwipeMotion.hasChangedDigit()) {
					// As the swipe digit has been changed, the grid view needs
					// to be updated.
					invalidate();
				} else if (mSwipeMotion.isVisible()
						&& mSwipeMotion.needToUpdateCurrentSwipePosition()) {
					// For performance reasons, the swipe position will not be
					// update at each event but only if relevant as decided by
					// the swipe motion.
					invalidate();
				}
			}
			return true;
		default:
			break;
		}

		return super.onTouch(arg0, event);
	}

	/**
	 * Class definition of the runnable which implements swipe border delay.
	 */
	private class SwipeBorderDelayRunnable implements Runnable {
		@Override
		public void run() {
			// Make the swipe border and motion visible at next draw of the grid
			// player view.
			if (mSwipeMotion != null && mSwipeMotion.isTouchDownInsideGrid()) {
				mSwipeMotion.setVisible(true);
			}
			invalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mGrid == null) {
			// As long as no grid has been attached to the grid view, it can not
			// be drawn.
			return;
		}

		synchronized (mGrid.mLock) {
			onDrawLocked(canvas);

			GridCell selectedCell = mGrid.getSelectedCell();
			if (selectedCell == null) {
				// As long a no cell is selected, the input mode border can not
				// be drawn.
			}

			// Draw the overlay for the swipe border around the selected
			// cell plus the swipe line.
			if (mSwipeMotion != null) {
				if (mSwipeMotion.isFinished()) {
					// do nothing.
				} else if (mSwipeMotion.isReleased()) {
					// The swipe motion was released. Now it can be set to
					// completed as it is confirmed that the overlay border
					// has been removed by not drawing it.
					mSwipeMotion.setVisible(false);
					mSwipeMotion.finish();
				} else if (mSwipeMotion.isVisible()) {
					GridInputMode gridInputMode = getGridInputMode();
					if (gridInputMode == GridInputMode.COPY) {
						selectedCell.drawCopyOverlay(canvas, mBorderWidth,
								getRestrictedGridInputMode());
					} else if (gridInputMode == GridInputMode.NORMAL
							|| gridInputMode == GridInputMode.MAYBE) {
						// The overlay needs to be draw as the swipe motion is
						// not yet released.
						selectedCell.drawSwipeOverlay(canvas, mBorderWidth,
								gridInputMode, mSwipeMotion
										.getCurrentSwipePositionX(),
								mSwipeMotion.getCurrentSwipePositionY(),
								mSwipeMotion.getFocussedDigit(), mPreferences
										.isOuterSwipeCircleVisible(mGridSize));
						mSwipeMotion.setVisible(true);
					}
				}
			}
		}
	}

	@Override
	public void loadNewGrid(Grid grid) {
		super.loadNewGrid(grid);

		// Reset the swipe motion
		mSwipeMotion = null;

		clearTickerTape();
		if (mPreferences.getSwipeValidMotionCounter() < 30) {
			mTickerTape.addItem(
					getResources().getString(R.string.hint_swipe_basic)).show();
		}

		invalidate();
	}

	/**
	 * Set the ticker tape for situation in which a cell is just touched and the
	 * swipe motion has not yet left the cell.
	 */
	private void setTickerTapeOnCellDown() {
		// Get information about position of the cell in the grid.
		int gridSize = mGrid.getGridSize();
		GridCell selectedCell = mGrid.getSelectedCell();
		boolean isTopRow = (selectedCell.getRow() == 0);
		boolean isBottomRow = (selectedCell.getRow() == gridSize - 1);
		boolean isLeftColumn = (selectedCell.getColumn() == 0);
		boolean isRightColumn = (selectedCell.getColumn() == gridSize - 1);

		// In case the digit is on an outer row or column of the grid an
		// additional hint has to be shown.
		if (isTopRow || isBottomRow || isLeftColumn || isRightColumn) {
			// List for all digits which can not be shown.
			boolean digitNotVisible[] = { false, false, false, false, false,
					false, false, false, false, false, false, false };

			// Determine invisible digits in case the selected cell is in the
			// top row.
			if (isTopRow) {
				digitNotVisible[1] = true;
				digitNotVisible[2] = true;
				digitNotVisible[3] = true;
			}

			// Determine invisible digits in case the selected cell is in the
			// left column.
			if (isLeftColumn) {
				digitNotVisible[1] = true;
				digitNotVisible[4] = true;
				if (gridSize >= 7) {
					digitNotVisible[7] = true;
				}
			}

			// Determine invisible digits in case the selected cell is in the
			// right column.
			if (isRightColumn) {
				digitNotVisible[3] = true;
				if (gridSize >= 6) {
					digitNotVisible[6] = true;
					if (gridSize >= 9) {
						digitNotVisible[9] = true;
					}
				}
			}

			// Determine invisible digits in case the selected cell is in the
			// bottom row.
			if (isBottomRow && gridSize >= 7) {
				digitNotVisible[7] = true;
				if (gridSize >= 8) {
					digitNotVisible[8] = true;
					if (gridSize >= 9) {
						digitNotVisible[9] = true;
					}
				}
			}

			// Determine the minimum and maximum value of the invisible digits.
			int minDigitNotVisible = 0;
			int maxDigitNotVisible = 0;
			for (int i = 1; i <= gridSize; i++) {
				if (digitNotVisible[i]) {
					if (minDigitNotVisible == 0) {
						minDigitNotVisible = i;
					}
					maxDigitNotVisible = i;
				}
			}

			// Determine full text of hint in case an invisible digit has been
			// determined.
			if (minDigitNotVisible > 0) {
				String digits = Integer.toString(minDigitNotVisible);
				for (int i = minDigitNotVisible + 1; i <= maxDigitNotVisible; i++) {
					if (digitNotVisible[i]) {
						if (i == maxDigitNotVisible) {
							digits += " "
									+ getResources()
											.getString(
													R.string.connector_last_two_elements,
													digits) + " "
									+ Integer.toString(i);
						} else {
							digits += ", " + Integer.toString(i);
						}
					}
				}
				mTickerTape.addItem(getResources().getString(
						R.string.hint_swipe_basic_cell_at_border, digits));
			} else {
				mTickerTape.addItem(getResources().getString(
						R.string.hint_swipe_outside_cell));
			}
		} else {
			mTickerTape.addItem(getResources().getString(
					R.string.hint_swipe_outside_cell));
		}

		mTickerTape.show();
	}

	/**
	 * Toggle the input mode between normal and maybe. Or if currently in
	 * another input mode than return to either normal or maybe mode dependent
	 * on which of these modes was used last.
	 */
	@Override
	public void toggleInputMode() {
		// If currently in copy mode then cancel the swipe motion which is used
		// to display the copy mode status.
		if (getGridInputMode() == GridInputMode.COPY && mSwipeMotion != null
				&& mSwipeMotion.isReleased() == false
				&& mSwipeMotion.isFinished() == false) {
			mSwipeMotion.release(null);
			invalidate();
		}
		super.toggleInputMode();
	}

	/**
	 * Sets the ticker tape in which the grid player view can put messages.
	 * 
	 * @param tickerTape
	 *            The ticker tape which can be used.
	 */
	public void setTickerTape(TickerTape tickerTape) {
		mTickerTape = tickerTape;
	}

	/**
	 * Clear the ticker tape.
	 */
	private void clearTickerTape() {
		// Reset the ticker tape
		if (mTickerTape != null) {
			mTickerTape.reset();
		}
	}
}