package net.cactii.mathdoku.grid.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.grid.CellChange;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.grid.GridCell;
import net.cactii.mathdoku.hint.TickerTape;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.tip.TipBadCageMath;
import net.cactii.mathdoku.tip.TipCopyCellValues;
import net.cactii.mathdoku.tip.TipDuplicateValue;
import net.cactii.mathdoku.tip.TipIncorrectValue;
import net.cactii.mathdoku.tip.TipOrderOfValuesInCage;
import net.cactii.mathdoku.ui.PuzzleFragmentActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;

public class GridPlayerView extends GridViewerView implements OnTouchListener {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridPlayerView";

	// Listeners
	public OnGridTouchListener mTouchedListener;
	private OnInputModeChangedListener mOnInputModeChangedListener;

	public interface OnInputModeChangedListener {
		public abstract void onInputModeChanged(GridInputMode inputMode);
	}

	// Reference to the last swipe motion which was started.
	private SwipeMotion mSwipeMotion;

	// Current input mode of the grid.
	private GridInputMode mInputMode;

	// In case the copy input mode is activated some additional information
	// needs to be stored.
	private class CopyInputModeState {
		// The input mode which was active before entering the copy input mode
		private GridInputMode mPreviousInputMode;

		// The cell which was long pressed
		private GridCell mCopyFromCell;
	}

	private CopyInputModeState mCopyInputModeState;

	// Handler and runnable for touch actions which need a delay
	private Handler mTouchHandler;
	private SwipeBorderDelayRunnable mSwipeBorderDelayRunnable;
	private LongPressRunnable mLongPressRunnable;

	// Reference to the last ticker tape started by the grid view player.
	TickerTape mTickerTape;

	private static final int LONG_PRESS_MILlIS = 1000;

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
		mInputMode = GridInputMode.NORMAL;

		// Initialize the handler use to process the on touch events
		mTouchHandler = new Handler();

		// Initialize the runnables used to delay touch handling
		mSwipeBorderDelayRunnable = new SwipeBorderDelayRunnable();
		mLongPressRunnable = new LongPressRunnable();

		// Set listeners
		this.setOnTouchListener(this);
		mOnInputModeChangedListener = null;
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

		// On down event select the cell but no further processing until we are
		// sure the long press event has been caught.
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mSwipeMotion == null) {
				mSwipeMotion = new SwipeMotion(this, mBorderWidth,
						mGridCellSize);
			}
			if (mSwipeMotion.setTouchDownEvent(event) == false) {
				// A position inside the grid border has been touched. Normally
				// this border is very small and will therefore not be hit.
				// However in training mode this border is much thicker in order
				// to display the entire swipe border and can easy be touched.
				mSwipeMotion = null;
			} else {
				if (mSwipeMotion.isDoubleTap()) {
					toggleInputMode();
					mSwipeMotion.clearDoubleTap();
				} else {
					// Select the touch down cell.
					GridCell selectedCell = mGrid.setSelectedCell(mSwipeMotion
							.getTouchDownCellCoordinates());

					// Inform listener of puzzle fragment of start of motion
					// event.
					mTouchedListener.gridTouched(selectedCell);
				}

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

				// Post a runnable to detect a long press. This runnable is
				// canceled on any motion or the up-event.
				mTouchHandler
						.postDelayed(mLongPressRunnable, LONG_PRESS_MILlIS);
			}

			// Do not allow other view to respond to this action, for example by
			// handling the long press, which would result in malfunction of the
			// swipe motion.
			return true;
		case MotionEvent.ACTION_UP:
			if (mTouchHandler != null) {
				mTouchHandler.removeCallbacks(mLongPressRunnable);
			}
			if (mInputMode == GridInputMode.COPY) {
				// Copy the content of the origin cell to the selected cell in
				// case the cell was not long pressed.
				if (event.getEventTime() - event.getDownTime() < LONG_PRESS_MILlIS
						&& mCopyInputModeState != null) {
					playSoundEffect(SoundEffectConstants.CLICK);
					GridCell selectedCell = mGrid.getSelectedCell();
					copyCell(mCopyInputModeState.mCopyFromCell, selectedCell);
					mCopyInputModeState.mCopyFromCell = selectedCell;
				}
			} else if (this.mTouchedListener != null && mSwipeMotion != null) {
				this.playSoundEffect(SoundEffectConstants.CLICK);

				mSwipeMotion.release(event);
				int swipeDigit = mSwipeMotion.getFocussedDigit();
				if (swipeDigit >= 1 && swipeDigit <= mGridSize) {
					// A swipe motion for a valid digit was completed.

					// Set the swipe digit as selected value for the cell which
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
				} else if (mSwipeMotion.isDoubleTap()) {
					toggleInputMode();
					mSwipeMotion.clearDoubleTap();
				} else {
					clearTickerTape();
					if (swipeDigit > mGridSize
							&& mPreferences.increaseSwipeInvalidMotionCounter() <= 6) {
						mTickerTape
								.addItem(
										getResources()
												.getString(
														R.string.hint_swipe_invalid_digit_completed))
								.setEraseConditions(1, 3000).show();
					}
				}

				// Inform listener of puzzle fragment about the release action
				mTouchedListener.gridTouched(mGrid.getSelectedCell());

				// Update to remove the swipe line
				invalidate();
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (mSwipeMotion != null) {
				// Update current swipe position
				mSwipeMotion.update(event);

				// Check if the grid view has to be invalidated
				int swipeDigit = mSwipeMotion.getFocussedDigit();
				if (swipeDigit >= 1 && swipeDigit <= 9
						&& mSwipeMotion.hasChangedDigit()) {

					if (mTouchHandler != null) {
						mTouchHandler.removeCallbacks(mLongPressRunnable);
					}
					// As the swipe digit has been changed, the grid view needs
					// to be updated.
					invalidate();
				} else if (mSwipeMotion.isVisible()
						&& mSwipeMotion.needToUpdateCurrentSwipePosition()) {
					if (mTouchHandler != null) {
						mTouchHandler.removeCallbacks(mLongPressRunnable);
					}
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

		return false;
	}

	/**
	 * Class definition of the runnable which implements swipe border delay.
	 */
	private class SwipeBorderDelayRunnable implements Runnable {
		@Override
		public void run() {
			// Make the swipe border and motion visible at next draw of the grid
			// player view.
			if (mSwipeMotion != null) {
				mSwipeMotion.setVisible(true);
			}
			invalidate();
		}
	}

	/**
	 * Class definition of the runnable which implements a long press on a cell.
	 */
	private class LongPressRunnable implements Runnable {
		@Override
		public void run() {
			// Check if grid exists
			if (mGrid == null) {
				return;
			}

			// Check if a cell is selected
			GridCell selectedCell = mGrid.getSelectedCell();
			if (selectedCell == null) {
				return;
			}

			// Store state
			if (mCopyInputModeState == null) {
				mCopyInputModeState = new CopyInputModeState();
				mCopyInputModeState.mPreviousInputMode = GridInputMode.NORMAL;
			}

			// Store the cell which was long pressed as the origin cell from
			// which will be copied.
			mCopyInputModeState.mCopyFromCell = selectedCell;

			// Switch to copy mode if necessary. Do not alter the previous input
			// mode if already in copy mode while it is possible that the user
			// long presses a cell while already in copy mode.
			if (mInputMode == GridInputMode.NORMAL
					|| mInputMode == GridInputMode.MAYBE) {
				// Store current input mode so it can be restored when ending
				// the copy mode.
				mCopyInputModeState.mPreviousInputMode = mInputMode;
			}

			// Switch to copy mode.
			mInputMode = GridInputMode.COPY;

			// Remove all messages from ticker tape as they do not apply to copy
			// mode.
			clearTickerTape();

			// Update the swipe border
			invalidate();

			// Inform listeners about change in input mode
			if (mOnInputModeChangedListener != null) {
				mOnInputModeChangedListener.onInputModeChanged(mInputMode);
			}
		}
	}

	public GridCell getSelectedCell() {
		return mGrid.getSelectedCell();
	}

	public void digitSelected(int newValue) {
		assert (mInputMode == GridInputMode.NORMAL
				|| mInputMode == GridInputMode.MAYBE || (mInputMode == GridInputMode.COPY && newValue == 0));

		GridCell selectedCell = mGrid.getSelectedCell();
		if (selectedCell == null) {
			// It should not be possible to select a digit without having
			// selected a cell first. But better safe then sorry.
			return;
		}

		// Save undo information
		CellChange orginalUserMove = selectedCell.saveUndoInformation(null);

		// Get old value of selected cell
		int oldValue = selectedCell.getUserValue();

		if (newValue == 0) { // Clear Button
			if (selectedCell.isEmpty() == false) {
				selectedCell.clearPossibles();
				selectedCell.setUserValue(0);
				mGrid.getGridStatistics().increaseCounter(
						StatisticsCounterType.ACTION_CLEAR_CELL);

				// In case the last user value has been cleared in the grid, the
				// check progress should no longer be available.
				if (mGrid.isEmpty(false)) {
					((PuzzleFragmentActivity) mContext).invalidateOptionsMenu();
				}
			}
		} else {
			if (TipOrderOfValuesInCage.toBeDisplayed(mPreferences,
					selectedCell.getCage())) {
				new TipOrderOfValuesInCage(mContext).show();
			}
			if (mInputMode == GridInputMode.MAYBE) {
				if (selectedCell.isUserValueSet()) {
					selectedCell.clear();
				}
				if (selectedCell.hasPossible(newValue)) {
					selectedCell.removePossible(newValue);
				} else {
					selectedCell.addPossible(newValue);
					if (TipCopyCellValues.toBeDisplayed(mPreferences,
							selectedCell)) {
						new TipCopyCellValues(mContext).show();
					}
				}
			} else {
				if (newValue != oldValue) {
					// User value of cell has actually changed.
					selectedCell.setUserValue(newValue);
					selectedCell.clearPossibles();
					if (oldValue == 0) {
						// In case a user value has been entered, the
						// check progress should be made available.
						((PuzzleFragmentActivity) mContext)
								.invalidateOptionsMenu();
					}
					if (mPreferences.isPuzzleSettingClearMaybesEnabled()) {
						// Update possible values for other cells in this row
						// and column.
						mGrid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
					}
					if (newValue != selectedCell.getCorrectValue()
							&& TipIncorrectValue.toBeDisplayed(mPreferences)) {
						new TipIncorrectValue(mContext).show();
					}
				}
			}
		}

		checkGridValidity(selectedCell);
	}

	/**
	 * Check the validity of the grid after a user value has been set for the
	 * given grid cell.
	 * 
	 * @param selectedCell
	 *            The grid cell for which a user value was set.
	 */
	private void checkGridValidity(GridCell selectedCell) {
		// Each cell in the same column or row as the given cell has to be
		// checked for duplicate values.
		int targetRow = selectedCell.getRow();
		int targetColumn = selectedCell.getColumn();
		for (GridCell checkedCell : mGrid.mCells) {
			if (checkedCell.getRow() == targetRow
					|| checkedCell.getColumn() == targetColumn) {
				if (mGrid.markDuplicateValuesInRowAndColumn(checkedCell)) {
					if (checkedCell == selectedCell
							&& TipDuplicateValue.toBeDisplayed(mPreferences)) {
						new TipDuplicateValue(mContext).show();
					}
				}
			}
		}

		// Check the cage math
		if (selectedCell.getCage().checkCageMathsCorrect(false) == false) {
			if (TipBadCageMath.toBeDisplayed(mPreferences)) {
				new TipBadCageMath(mContext).show();
			}
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
					if (mInputMode == GridInputMode.COPY) {
						selectedCell.drawCopyOverlay(canvas, mBorderWidth,
								mInputMode,
								mCopyInputModeState.mPreviousInputMode,
								mSwipeMotion.getCurrentSwipePositionX(),
								mSwipeMotion.getCurrentSwipePositionY());
					} else if (mInputMode == GridInputMode.NORMAL
							|| mInputMode == GridInputMode.MAYBE) {
						// The overlay needs to be draw as the swipe motion is
						// not yet released.
						selectedCell.drawSwipeOverlay(canvas, mBorderWidth,
								mInputMode, mSwipeMotion
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

	/**
	 * Get the grid input mode but restrict its value to normal either maybe.
	 * 
	 * @return The current input mode but restricted to normal either maybe.
	 */
	@Override
	protected GridInputMode getRestrictedGridInputMode() {
		return (mInputMode == GridInputMode.COPY && mCopyInputModeState != null ? mCopyInputModeState.mPreviousInputMode
				: mInputMode);
	}

	/**
	 * Get the current grid input mode.
	 * 
	 * @return The current grid input mode.
	 */
	public GridInputMode getGridInputMode() {
		return mInputMode;
	}

	@Override
	public void loadNewGrid(Grid grid) {
		super.loadNewGrid(grid);

		// Reset the swipe motion
		mSwipeMotion = null;

		// Set default input mode to normal
		mInputMode = GridInputMode.NORMAL;

		clearTickerTape();
		if (mPreferences.getSwipeValidMotionCounter() < 30) {
			mTickerTape.addItem(
					getResources().getString(R.string.hint_swipe_basic)).show();
		}

		invalidate();
	}

	/**
	 * Highlight those cells where the user has made a mistake.
	 * 
	 * @return The number of cells which have been marked as invalid. Cells
	 *         which were already marked as invalid will not be counted again.
	 */
	public int markInvalidChoices() {
		int countNewInvalids = 0;
		for (GridCell cell : mGrid.mCells) {
			// Check all cells having a value and not (yet) marked as invalid.
			if (cell.isUserValueSet() && !cell.hasInvalidUserValueHighlight()) {
				if (cell.getUserValue() != cell.getCorrectValue()) {
					cell.setInvalidHighlight();
					mGrid.increaseCounter(StatisticsCounterType.CHECK_PROGRESS_INVALIDS_CELLS_FOUND);
					countNewInvalids++;
				}
			}
		}

		if (countNewInvalids > 0) {
			invalidate();
		}

		return countNewInvalids;
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
	public void toggleInputMode() {
		switch (mInputMode) {
		case NORMAL:
			mInputMode = GridInputMode.MAYBE;
			break;
		case MAYBE:
			mInputMode = GridInputMode.NORMAL;
			break;
		case COPY:
			if (mSwipeMotion != null && mSwipeMotion.isReleased() == false
					&& mSwipeMotion.isFinished() == false) {
				mSwipeMotion.release(null);
			}

			// Restore input mode to the last know value before the copy mode
			// was entered.
			if (mCopyInputModeState != null) {
				mInputMode = mCopyInputModeState.mPreviousInputMode;
			}
			break;
		}
		invalidate();

		// Inform listeners about change in input mode
		if (mOnInputModeChangedListener != null) {
			mOnInputModeChangedListener.onInputModeChanged(mInputMode);
		}
	}

	/**
	 * Register the listener to call in case the ticker tape for the hints has
	 * to be set.
	 * 
	 * @param onHintChangedListener
	 *            The listener to call in case a hint has to be set.
	 */
	public void setOnInputModeChangedListener(
			OnInputModeChangedListener onInputChangedModeListener) {
		mOnInputModeChangedListener = onInputChangedModeListener;
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

	/**
	 * Copies the user value or the possible values from one cell to another
	 * cell.
	 * 
	 * @param fromGridCell
	 *            The cell from which the values have to be copied.
	 * @param toGridCell
	 *            The cell to which the values are copied.
	 */
	private void copyCell(GridCell fromGridCell, GridCell toGridCell) {
		if (fromGridCell != null && toGridCell != null
				&& fromGridCell.equals(toGridCell) == false) {

			if (fromGridCell.countPossibles() > 0) {
				// For the origin cell at least one maybe value has been set.
				// Maybe values will only be copied to the to-cell in case at
				// least one maybe value differs between the two cells.
				boolean updatePossibleValues = false;
				boolean isMaybeDigitInFromCell;
				boolean isMaybeDigitInToCell;
				for (int digit = 1; digit <= mGridSize; digit++) {
					isMaybeDigitInFromCell = fromGridCell.hasPossible(digit);
					isMaybeDigitInToCell = toGridCell.hasPossible(digit);
					if (isMaybeDigitInFromCell != isMaybeDigitInToCell) {
						if (updatePossibleValues == false) {
							updatePossibleValues = true;

							// Save undo information
							toGridCell.saveUndoInformation(null);

							// Clear the to-cell in case it contains a user
							// value which will now be overwritten with maybe
							// values.
							if (toGridCell.isUserValueSet()) {
								toGridCell.clear();
							}
						}

						if (isMaybeDigitInFromCell == true
								&& isMaybeDigitInToCell == false) {
							toGridCell.addPossible(digit);
						} else if (isMaybeDigitInFromCell == false
								&& isMaybeDigitInToCell == true) {
							toGridCell.removePossible(digit);
						}
					}
				}
			} else {
				// The from cell does not contain a maybe value. So the cell is
				// either empty or is filled with a user value.
				if (fromGridCell.getUserValue() != toGridCell.getUserValue()) {
					// Save undo information
					CellChange orginalUserMove = toGridCell
							.saveUndoInformation(null);

					if (fromGridCell.isUserValueSet()) {
						toGridCell.setUserValue(fromGridCell.getUserValue());
						toGridCell.clearPossibles();
					} else {
						toGridCell.clear();
					}

					if (mPreferences.isPuzzleSettingClearMaybesEnabled()) {
						// Update possible values for other cells in this row
						// and column.
						mGrid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
					}

					checkGridValidity(toGridCell);
				}
			}
		}
	}
}