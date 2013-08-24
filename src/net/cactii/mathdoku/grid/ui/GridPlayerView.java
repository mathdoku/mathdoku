package net.cactii.mathdoku.grid.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.grid.CellChange;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.grid.GridCell;
import net.cactii.mathdoku.hint.OnTickerTapeChangedListener;
import net.cactii.mathdoku.hint.TickerTape;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.tip.TipBadCageMath;
import net.cactii.mathdoku.tip.TipDuplicateValue;
import net.cactii.mathdoku.tip.TipIncorrectValue;
import net.cactii.mathdoku.tip.TipOrderOfValuesInCage;
import net.cactii.mathdoku.ui.PuzzleFragmentActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class GridPlayerView extends GridViewerView implements OnTouchListener {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridPlayerView";

	// Listeners
	public OnGridTouchListener mTouchedListener;
	private OnTickerTapeChangedListener mOnTickerTapeChangedListener;
	private OnInputModeChangedListener mOnInputModeChangedListener;

	public interface OnInputModeChangedListener {
		public abstract void onInputModeChanged(GridInputMode inputMode);
	}

	// Reference to the last swipe motion which was started.
	private SwipeMotion mSwipeMotion;

	private GridInputMode mInputMode;

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
		// Set listeners
		this.setOnTouchListener(this);
		mOnTickerTapeChangedListener = null;
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
				if (mOnTickerTapeChangedListener != null
						&& mPreferences.getSwipeValidMotionCounter() < 30) {
					setTickerTapeOnCellDown();
				}

				invalidate();
			}

			// Do not allow other view to respond to this action, for example by
			// handling the long press, which would result in malfunction of the
			// swipe motion.
			return true;
		case MotionEvent.ACTION_UP:
			if (this.mTouchedListener != null && mSwipeMotion != null) {
				this.playSoundEffect(SoundEffectConstants.CLICK);

				mSwipeMotion.release(event);
				int swipeDigit = mSwipeMotion.getFocussedDigit();
				if (swipeDigit >= 1 && swipeDigit <= mGridSize) {
					// A swipe motion for a valid digit was completed.

					// Set the swipe digit as selected value for the cell which
					// was initially touched.
					digitSelected(swipeDigit);

					// Update preferences and inform listeners
					if (mOnTickerTapeChangedListener != null) {
						if (mPreferences
								.increaseSwipeValidMotionCounter(swipeDigit) <= 6) {
							setTickerTapeWithEraseConditions(
									getResources()
											.getString(
													R.string.hint_swipe_valid_digit_completed,
													swipeDigit), 2, 3000);
						} else if (mTickerTape != null) {
							mTickerTape.cancel();
							mTickerTape = null;
							mOnTickerTapeChangedListener
									.onTickerTapeChanged(null);
						}
					}
				} else if (mSwipeMotion.isDoubleTap()) {
					toggleInputMode();
					mSwipeMotion.clearDoubleTap();
				} else if (swipeDigit > mGridSize
						&& mOnTickerTapeChangedListener != null
						&& mPreferences.increaseSwipeInvalidMotionCounter() <= 6) {
					setTickerTapeWithEraseConditions(
							getResources()
									.getString(
											R.string.hint_swipe_invalid_digit_completed),
							1, 3000);
				} else if (mTickerTape != null) {
					mTickerTape.cancel();
					mTickerTape = null;
					mOnTickerTapeChangedListener.onTickerTapeChanged(null);
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

					// As the swipe digit has been changed, the grid view needs
					// to be updated.
					invalidate();
				} else if (mSwipeMotion.needToUpdateCurrentSwipePosition()) {
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

	public GridCell getSelectedCell() {
		return mGrid.getSelectedCell();
	}

	public void digitSelected(int newValue) {
		// Display a message in case no cell is selected.
		GridCell selectedCell = mGrid.getSelectedCell();
		if (selectedCell == null) {
			Toast.makeText(mContext, R.string.select_cell_before_value,
					Toast.LENGTH_SHORT).show();
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
					mGrid.increaseCounter(StatisticsCounterType.POSSIBLES);
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
						// and
						// column.
						mGrid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
					}
					if (newValue != selectedCell.getCorrectValue()
							&& TipIncorrectValue.toBeDisplayed(mPreferences)) {
						new TipIncorrectValue(mContext).show();
					}
				}
			}
		}

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

			// Draw the overlay for the swipe border around the selected cell
			// plus the swipe line.
			if (mSwipeMotion != null && mSwipeMotion.isVisible()) {
				if (mSwipeMotion.isReleased()) {
					// The swipe motion was released. Now it can be set to
					// completed as it is confirmed that the overlay border has
					// been removed by not drawing it.
					mSwipeMotion.finish();
				} else {
					// The overlay needs to be draw as the swipe motion is not
					// yet released.
					GridCell gridCell = mGrid.getSelectedCell();
					if (gridCell != null) {
						gridCell.drawSwipeOverlay(canvas, mBorderWidth,
								mInputMode, mSwipeMotion
										.getCurrentSwipePositionX(),
								mSwipeMotion.getCurrentSwipePositionY(),
								mSwipeMotion.getFocussedDigit(), mPreferences
										.isOuterSwipeCircleVisible(mGridSize));
					}

				}
			}
		}
	}

	/**
	 * Get the current grid input mode.
	 * 
	 * @return The current grid input mode.
	 */
	@Override
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

		if (mOnTickerTapeChangedListener != null
				&& mPreferences.getSwipeValidMotionCounter() < 30) {
			if (mTickerTape != null) {
				mTickerTape.cancel();
			}
			mTickerTape = new TickerTape(mContext);
			mTickerTape.addItem(getResources().getString(
					R.string.hint_swipe_basic));
			mOnTickerTapeChangedListener.onTickerTapeChanged(mTickerTape);
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
	 * Register the listener to call in case the ticker tape for the hints has
	 * to be set.
	 * 
	 * @param onHintChangedListener
	 *            The listener to call in case a hint has to be set.
	 */
	public void setOnTickerTapeChangedListener(
			OnTickerTapeChangedListener onTickerTapeChangedListener) {
		mOnTickerTapeChangedListener = onTickerTapeChangedListener;
	}

	/**
	 * Set the ticker tape for situation in which a cell is just touched and the
	 * swipe motion has not yet left the cell.
	 */
	private void setTickerTapeOnCellDown() {
		// Create the ticker tape.
		if (mTickerTape != null) {
			mTickerTape.cancel();
		}
		mTickerTape = new TickerTape(mContext);

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

		// Inform listeners about change
		if (mOnTickerTapeChangedListener != null) {
			mOnTickerTapeChangedListener.onTickerTapeChanged(mTickerTape);
		}
	}

	/**
	 * Set a message in the ticket tape.
	 * 
	 * @param message
	 *            The message to be set.
	 * @param minDisplayCycles
	 *            The minimum number of times each message has to be displayed.
	 * @param minDisplayTime
	 *            The minimum amount of milliseconds the ticker tape has to be
	 *            displayed.
	 */
	private void setTickerTapeWithEraseConditions(String message,
			int minDisplayCycles, int minDisplayTime) {
		// Cancel the previous ticker tape
		if (mTickerTape != null) {
			mTickerTape.cancel();
		}

		// Create a new ticker tape
		mTickerTape = new TickerTape(mContext);
		mTickerTape.addItem(message)
				.setEraseConditions(minDisplayCycles, minDisplayTime).show();

		// Inform listeners about the new ticker tape.
		mOnTickerTapeChangedListener.onTickerTapeChanged(mTickerTape);
	}

	/**
	 * Toggle the input mode to the other mode
	 */
	public void toggleInputMode() {
		mInputMode = (mInputMode == GridInputMode.NORMAL ? GridInputMode.MAYBE
				: GridInputMode.NORMAL);
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
}