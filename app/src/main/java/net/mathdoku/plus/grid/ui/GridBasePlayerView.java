package net.mathdoku.plus.grid.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;

import net.mathdoku.plus.grid.CellChange;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.statistics.GridStatistics.StatisticsCounterType;
import net.mathdoku.plus.tip.TipBadCageMath;
import net.mathdoku.plus.tip.TipCopyCellValues;
import net.mathdoku.plus.tip.TipDuplicateValue;
import net.mathdoku.plus.tip.TipIncorrectValue;
import net.mathdoku.plus.tip.TipOrderOfValuesInCage;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;

/**
 * The grid base player view allows to play a grid with a digit button interface
 * only.
 */
public class GridBasePlayerView extends GridViewerView implements
		OnTouchListener {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridBasePlayerView";

	// Input mode changed listener
	public interface OnInputModeChangedListener {
		public abstract void onInputModeChanged(GridInputMode inputMode,
				boolean enableCopyMode);
	}

	private OnInputModeChangedListener mOnInputModeChangedListener;

	// Current input mode of the grid.
	private GridInputMode mInputMode;

	// Reference to the last motion which was started.
	private Motion mMotion;

	// Handler and runnable for touch actions which need a delay
	Handler mTouchHandler;

	// Listeners
	public abstract class OnGridTouchListener {
		public abstract void gridTouched(GridCell cell);
	}

	OnGridTouchListener mTouchedListener;

	// In case the copy input mode is activated some additional information
	// needs to be stored.
	private class CopyInputModeState {
		// The input mode which was active before entering the copy input mode
		private GridInputMode mPreviousInputMode;

		// The cell which was long pressed
		private GridCell mCopyFromCell;
	}

	private CopyInputModeState mCopyInputModeState;

	GridBasePlayerView(Context context) {
		super(context);
		initGridView();
	}

	GridBasePlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGridView();
	}

	GridBasePlayerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGridView();
	}

	private void initGridView() {
		mInputMode = GridInputMode.NORMAL;

		// Initialize the handler use to process the on touch events
		mTouchHandler = new Handler();

		// Set listeners
		setOnTouchListener(this);
		mOnInputModeChangedListener = null;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if (mGrid == null || !mGrid.isActive()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mMotion == null) {
				mMotion = new Motion(this, mBorderWidth, mGridCellSize);
			}
			mMotion.setTouchDownEvent(event);
			if (mMotion.isDoubleTap()) {
				toggleInputMode();
				mMotion.clearDoubleTap();
			} else if (mMotion.isTouchDownInsideGrid()
					&& mTouchedListener != null) {
				// Inform listener about the cell which was tapped.
				mTouchedListener.gridTouched(mGrid.setSelectedCell(mMotion
						.getTouchDownCellCoordinates()));
				invalidate();
			}

			// Do not allow other views to respond to this action, for example
			// by
			// handling the long press, which would result in malfunction of the
			// swipe motion.
			return true;
		case MotionEvent.ACTION_UP:
			playSoundEffect(SoundEffectConstants.CLICK);
			if (mInputMode == GridInputMode.COPY) {
				// While in copy mode, the content of the previously selected
				// cell will be copied to the cell which is currently selected.
				if (mCopyInputModeState != null) {
					// Get the currently selected cell.
					GridCell selectedCell = mGrid.getSelectedCell();

					// Copy values of the origin cell to the currently selected
					// cell.
					copyCell(mCopyInputModeState.mCopyFromCell, selectedCell);

					// Use the currently selected cell as new origin for the
					// next copy.
					mCopyInputModeState.mCopyFromCell = selectedCell;

					// No further processing allowed.
					return true;
				}
			}
			return false;
		default:
			break;
		}

		return false;
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
		CellChange originalUserMove = selectedCell.saveUndoInformation(null);

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
						mGrid.clearRedundantPossiblesInSameRowOrColumn(originalUserMove);
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

						// noinspection ConstantConditions
						if (isMaybeDigitInFromCell == true
								&& isMaybeDigitInToCell == false) {
							toGridCell.addPossible(digit);
						} else // noinspection ConstantConditions
						if (isMaybeDigitInFromCell == false
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
					CellChange originalUserMove = toGridCell
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
						mGrid.clearRedundantPossiblesInSameRowOrColumn(originalUserMove);
					}

					checkGridValidity(toGridCell);
				}
			}
			invalidate();
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
		GridCage gridCage = selectedCell.getCage();
		if (gridCage != null && gridCage.isMathsCorrect() == false) {
			if (TipBadCageMath.toBeDisplayed(mPreferences)) {
				new TipBadCageMath(mContext).show();
			}
		}
	}

	/**
	 * Get the grid input mode but restrict its value to normal either maybe.
	 * 
	 * @return The current input mode but restricted to normal either maybe.
	 */
	@Override
	public GridInputMode getRestrictedGridInputMode() {
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

	/**
	 * Enables/disabled the copy mode.
	 * 
	 * @param enableCopyMode
	 *            True in case the copy mode should be enabled. False otherwise.
	 */
	public void setCopyModeEnabled(boolean enableCopyMode) {
		// Check if grid exists
		if (mGrid == null) {
			return;
		}

		if (enableCopyMode) {
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

			// Store the cell which is currently selected as the origin cell
			// from which values will be copied.
			mCopyInputModeState.mCopyFromCell = selectedCell;

			// Switch to copy mode if necessary. Do not alter the previous
			// input mode if already in copy mode.
			if (mInputMode == GridInputMode.NORMAL
					|| mInputMode == GridInputMode.MAYBE) {
				// Store current input mode so it can be restored when
				// ending the copy mode.
				mCopyInputModeState.mPreviousInputMode = mInputMode;
			}

			// Switch to copy mode.
			mInputMode = GridInputMode.COPY;
			mPreferences.increaseInputModeCopyCounter();

			// Inform listeners about change in input mode
			if (mOnInputModeChangedListener != null) {
				mOnInputModeChangedListener.onInputModeChanged(
						mCopyInputModeState.mPreviousInputMode, true);
			}
		} else {
			// Restore input mode to the last know value before the copy mode
			// was entered.
			if (mCopyInputModeState != null) {
				mInputMode = mCopyInputModeState.mPreviousInputMode;

				// Inform listeners about change in input mode
				if (mOnInputModeChangedListener != null) {
					mOnInputModeChangedListener.onInputModeChanged(
							mCopyInputModeState.mPreviousInputMode, false);
				}
			}
		}
	}

	/**
	 * Toggle the input mode between normal and maybe. Or if currently in
	 * another input mode than return to either normal or maybe mode dependent
	 * on which of these modes was used last.
	 */
	void toggleInputMode() {
		switch (mInputMode) {
		case NORMAL:
			mPreferences.increaseInputModeChangedCounter();
			setGridInputMode(GridInputMode.MAYBE, false);
			break;
		case MAYBE:
			mPreferences.increaseInputModeChangedCounter();
			setGridInputMode(GridInputMode.NORMAL, false);
			break;
		default:
			// Cannot toggle this mode.
			break;
		}
	}

	/**
	 * Set the input mode to the given mode.
	 * 
	 * @param gridInputMode
	 *            The new grid input mode.
	 */
	@SuppressWarnings("SameParameterValue")
	public void setGridInputMode(GridInputMode gridInputMode,
			boolean enableCopyMode) {
		mInputMode = gridInputMode;
		invalidate();

		// Inform listeners about change in input mode
		if (mOnInputModeChangedListener != null) {
			mOnInputModeChangedListener.onInputModeChanged(mInputMode,
					enableCopyMode);
		}
	}

	/**
	 * Register the listener to call in case the input mode has changed.
	 * 
	 * @param onInputChangedModeListener
	 *            The listener to call in case a hint has to be set.
	 */
	public void setOnInputModeChangedListener(
			OnInputModeChangedListener onInputChangedModeListener) {
		mOnInputModeChangedListener = onInputChangedModeListener;
	}

	/**
	 * Registers the on grid touch listener.
	 * 
	 * @param listener
	 *            The listener which is to be called in case the gird has been
	 *            touched.
	 */
	public void setOnGridTouchListener(OnGridTouchListener listener) {
		this.mTouchedListener = listener;
	}

	@Override
	public void loadNewGrid(Grid grid) {
		super.loadNewGrid(grid);

		// Reset motion for the newly loaded grid.
		mMotion = null;
	}
}
