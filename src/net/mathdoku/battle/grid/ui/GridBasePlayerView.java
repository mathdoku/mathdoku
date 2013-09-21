package net.mathdoku.battle.grid.ui;

import net.mathdoku.battle.grid.CellChange;
import net.mathdoku.battle.grid.Grid;
import net.mathdoku.battle.grid.GridCell;
import net.mathdoku.battle.grid.ui.GridPlayerView.OnGridTouchListener;
import net.mathdoku.battle.statistics.GridStatistics.StatisticsCounterType;
import net.mathdoku.battle.tip.TipBadCageMath;
import net.mathdoku.battle.tip.TipCopyCellValues;
import net.mathdoku.battle.tip.TipDuplicateValue;
import net.mathdoku.battle.tip.TipIncorrectValue;
import net.mathdoku.battle.tip.TipOrderOfValuesInCage;
import net.mathdoku.battle.ui.PuzzleFragmentActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * The grid base player view allows to play a grid with a digit button interface
 * only.
 * 
 */
public class GridBasePlayerView extends GridViewerView implements
		OnTouchListener {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridBasePlayerView";

	// Input mode changed listener
	public interface OnInputModeChangedListener {
		public abstract void onInputModeChanged(GridInputMode inputMode);
	}

	private OnInputModeChangedListener mOnInputModeChangedListener;

	// Current input mode of the grid.
	private GridInputMode mInputMode;

	// Reference to the last motion which was started.
	private Motion mMotion;

	// Handler and runnable for touch actions which need a delay
	protected Handler mTouchHandler;
	private LongPressRunnable mLongPressRunnable;
	private static final int LONG_PRESS_MILlIS = 1000;

	// Listeners
	public OnGridTouchListener mTouchedListener;

	// In case the copy input mode is activated some additional information
	// needs to be stored.
	private class CopyInputModeState {
		// The input mode which was active before entering the copy input mode
		private GridInputMode mPreviousInputMode;

		// The cell which was long pressed
		private GridCell mCopyFromCell;
	}

	private CopyInputModeState mCopyInputModeState;

	public GridBasePlayerView(Context context) {
		super(context);
		initGridView(context);
	}

	public GridBasePlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGridView(context);
	}

	public GridBasePlayerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGridView(context);
	}

	private void initGridView(Context context) {
		mInputMode = GridInputMode.NORMAL;

		// Initialize the handler use to process the on touch events
		mTouchHandler = new Handler();

		// Initialize the runnables used to delay touch handling
		mLongPressRunnable = new LongPressRunnable();

		// Set listeners
		setOnTouchListener(this);
		mOnInputModeChangedListener = null;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if (mGrid == null || !mGrid.isActive())
			return false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mMotion == null) {
				mMotion = new Motion();
			}
			mMotion.setTouchDownEvent(event);
			if (mMotion.isDoubleTap()) {
				toggleInputMode();
				mMotion.clearDoubleTap();
			}

			// Post a runnable to detect a long press. This runnable is
			// canceled on any motion or the up-event.
			mTouchHandler.postDelayed(mLongPressRunnable, LONG_PRESS_MILlIS);

			// Do not allow other views to respond to this action, for example
			// by
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

					// No further processing allowed.
					return true;
				}
			}
			return false;
		case MotionEvent.ACTION_MOVE:
			if (mTouchHandler != null) {
				mTouchHandler.removeCallbacks(mLongPressRunnable);
			}
			return false;
		default:
			break;
		}

		return false;
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

			// Update the swipe border
			invalidate();

			// Inform listeners about change in input mode
			if (mOnInputModeChangedListener != null) {
				mOnInputModeChangedListener.onInputModeChanged(mInputMode);
			}
		}
	}

	/**
	 * Enter/remove a value from the selected cell.
	 * 
	 * @param newValue
	 *            The value which has to be entered/removed from the selected
	 *            cell in the grid.
	 */
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

			if (mInputMode == GridInputMode.COPY) {
				selectedCell.drawCopyOverlay(canvas, mBorderWidth,
						mCopyInputModeState.mPreviousInputMode);
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

		// Set default input mode to normal
		mInputMode = GridInputMode.NORMAL;

		invalidate();
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
	 * Clear the double tap on the motion. Only to be called in case the double
	 * tap has been handled otherwise.
	 */
	protected void clearDoubleTap() {
		if (mMotion != null) {
			mMotion.clearDoubleTap();
		}
	}
}