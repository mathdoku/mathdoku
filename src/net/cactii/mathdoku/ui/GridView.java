package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.CellChange;
import net.cactii.mathdoku.DigitPositionGrid;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.hint.OnTickerTapeChangedListener;
import net.cactii.mathdoku.hint.TickerTape;
import net.cactii.mathdoku.painter.GridPainter;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.tip.TipBadCageMath;
import net.cactii.mathdoku.tip.TipDuplicateValue;
import net.cactii.mathdoku.tip.TipIncorrectValue;
import net.cactii.mathdoku.tip.TipOrderOfValuesInCage;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class GridView extends View implements OnTouchListener {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridView";

	// Context and preferences in context
	Context mContext;
	Preferences mPreferences;

	// Actual content of the puzzle in this grid view
	private Grid mGrid;

	// Listeners
	public OnGridTouchListener mTouchedListener;
	private OnTickerTapeChangedListener mOnTickerTapeChangedListener;

	// Size (in cells and pixels) of the grid view and size (in pixel) of cells
	// in grid
	private int mGridSize;
	public float mGridViewSize;
	private float mGridBorderWidth;
	private float mGridCellSize;

	// Reference to the global grid painter object
	private GridPainter mGridPainter;

	// The layout to be used for positioning the maybe digits in a grid.
	private DigitPositionGrid mDigitPositionGrid;

	// Visible window rectangle
	private Rect mDisplayFrame;

	// Reference to the last swipe motion which was started.
	private SwipeMotion mSwipeMotion;

	// The input mode in which the puzzle can be.
	public enum InputMode {
		NORMAL, // Digits entered are handled as a new cell value
		MAYBE, // Digits entered are handled to toggle the possible value on/of
	};

	InputMode mInputMode;

	// Reference to the last ticker tape started by the grid view.
	TickerTape mTickerTape;

	public GridView(Context context) {
		super(context);
		initGridView(context);
	}

	public GridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGridView(context);
	}

	public GridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGridView(context);
	}

	private void initGridView(Context context) {
		mContext = context;
		mPreferences = Preferences.getInstance(mContext);

		mGridViewSize = 0;
		mGridPainter = Painter.getInstance().getGridPainter();
		mGridBorderWidth = (mGridPainter == null ? 0 : mGridPainter
				.getBorderPaint().getStrokeWidth());

		// Initialize the display frame for the grid view.
		mDisplayFrame = new Rect();
		updateWindowVisibleDisplayFrame();

		// Set listeners
		this.setOnTouchListener((OnTouchListener) this);
		mOnTickerTapeChangedListener = null;
	}

	public void setOnGridTouchListener(OnGridTouchListener listener) {
		this.mTouchedListener = listener;
	}

	public abstract class OnGridTouchListener {
		public abstract void gridTouched(GridCell cell);
	}

	public boolean onTouch(View arg0, MotionEvent event) {
		if (mGrid == null || !mGrid.isActive())
			return false;

		// On down event select the cell but no further processing until we are
		// sure the long press event has been caught.
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mSwipeMotion == null) {
				mSwipeMotion = new SwipeMotion(mGrid, mGridBorderWidth,
						mGridCellSize);
			}
			mSwipeMotion.setTouchDownEvent(event);
			if (mSwipeMotion.isDoubleTap()) {
				mInputMode = (mInputMode == InputMode.MAYBE ? InputMode.NORMAL
						: InputMode.MAYBE);
				mSwipeMotion.clearDoubleTap();
			} else {
				GridCell selectedCell = mSwipeMotion.getTouchDownCell();

				// Select new cell and inform listener of puzzle fragment of
				// start of motion event.
				mGrid.setSelectedCell(selectedCell);
				mTouchedListener.gridTouched(selectedCell);
			}

			// Show the basic swipe hint. Replace this hint after a short pause.
			if (mOnTickerTapeChangedListener != null
					&& mPreferences.getSwipeValidMotionCounter() < 30) {
				setTickerTapeOnCellDown();
			}

			invalidate();

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
					mInputMode = (mInputMode == InputMode.MAYBE ? InputMode.NORMAL
							: InputMode.MAYBE);
					mSwipeMotion.clearDoubleTap();

					if (mPreferences.increaseHintInputModeShowedCounter() < 4) {

						// Determine old and new input mode (short) description.
						String mOldInputModeText = (mInputMode == InputMode.MAYBE ? mContext
								.getResources().getString(
										R.string.input_mode_normal_short)
								: mContext.getResources().getString(
										R.string.input_mode_maybe_short));
						String mNewInputModeText = (mInputMode == InputMode.NORMAL ? mContext
								.getResources().getString(
										R.string.input_mode_normal_short)
								: mContext.getResources().getString(
										R.string.input_mode_maybe_short));

						setTickerTapeWithEraseConditions(
								getResources().getString(
										R.string.hint_input_mode_changed,
										mOldInputModeText, mNewInputModeText),
								2, 3000);
					}
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
				// Check whether the touch down cell was already left once
				// before this event.
				boolean hasLeftTouchDownCellBefore = mSwipeMotion
						.hasLeftTouchDownCell();

				// Update current swipe position
				mSwipeMotion.update(event);

				// Check if the grid view has to be invalidated
				int swipeDigit = mSwipeMotion.getFocussedDigit();
				if (swipeDigit >= 1 && swipeDigit <= 9
						&& mSwipeMotion.hasChangedDigit()) {

					if (hasLeftTouchDownCellBefore == false
							&& mSwipeMotion.hasLeftTouchDownCell()) {
						// When the cell is left for the first time, reset the
						// hints in the ticker tape.
						setTickerTapeOnLeavingSelectedCell((swipeDigit >= 1 && swipeDigit <= mGrid
								.getGridSize()));
					}

					// As the swipe digit has been changed, the grid view needs
					// to be updated.
					invalidate();
				} else if (mSwipeMotion
						.hasChangedCoordinatesCurrentSwipePosition()) {
					// For performance reasons, the swipe position will only be
					// updated in case the current swipe position has moved to
					// another cell.
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
						StatisticsCounterType.CELL_CLEARED);
				mGrid.getGridStatistics().increaseCounter(
						StatisticsCounterType.CELLS_EMPTY);
				mGrid.getGridStatistics().decreaseCounter(
						StatisticsCounterType.CELLS_FILLED);

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
			if (mInputMode == InputMode.MAYBE) {
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
						mGrid.getGridStatistics().increaseCounter(
								StatisticsCounterType.CELLS_FILLED);
						mGrid.getGridStatistics().decreaseCounter(
								StatisticsCounterType.CELLS_EMPTY);

						// In case a user value has been entered, the
						// check progress should be made available.
						((PuzzleFragmentActivity) mContext)
								.invalidateOptionsMenu();
					} else {
						mGrid.getGridStatistics().increaseCounter(
								StatisticsCounterType.USER_VALUE_REPLACED);
					}
					if (mPreferences.isClearRedundantPossiblesEnabled()) {
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
			// As long as no grid has been attached to the gridview, it can not
			// be drawn.
			return;
		}

		synchronized (mGrid.mLock) { // Avoid redrawing at the same time as
										// creating
			if (mGridSize < 3)
				return;
			if (mGrid.mCages == null)
				return;

			float gridBorderWidth = mGridPainter.getBorderPaint()
					.getStrokeWidth();

			// Draw grid background and border grid
			canvas.drawColor(mGridPainter.getBackgroundPaint().getColor());
			canvas.drawRect((float) 1, (float) 1, mGridViewSize, mGridViewSize,
					mGridPainter.getBorderPaint());

			// Draw cells
			Painter.getInstance()
					.setCellSize(mGridCellSize, mDigitPositionGrid);
			for (GridCell cell : mGrid.mCells) {
				cell.draw(canvas, gridBorderWidth, mInputMode);
			}

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
						gridCell.drawSwipeOverlay(canvas, gridBorderWidth,
								mInputMode,
								mSwipeMotion.getCurrentSwipePositionX(),
								mSwipeMotion.getCurrentSwipePositionY(),
								mSwipeMotion.getFocussedDigit());
					}

				}
			}
		}
	}

	public void loadNewGrid(Grid grid) {
		mGrid = grid;

		// Compute grid size. Set to 1 in case grid is null to avoid problems in
		// onMeasure as this will be called before the grid is loaded.
		mGridSize = (mGrid == null ? 1 : mGrid.getGridSize());

		// Determine the layout which has to be used for drawing the possible
		// values inside a cell.
		mDigitPositionGrid = (mGrid != null
				&& mGrid.hasPrefShowMaybesAs3x3Grid() ? new DigitPositionGrid(
				mGrid.getGridSize()) : null);

		// Reset the swipe motion
		mSwipeMotion = null;

		// Set default input mode to normal
		mInputMode = InputMode.NORMAL;

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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Get maximum width and height available to display the grid view.
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);

		// Correct width/height for low aspect ratio screens.
		measuredWidth = Math.min(measuredWidth, mDisplayFrame.width());
		measuredHeight = Math.min(measuredHeight, mDisplayFrame.height());

		// Get the maximum space available for the grid. As it is a square we
		// need the minimum of width and height.
		int maxSize = (int) Math.min(measuredWidth, measuredHeight);

		// Finally compute the exact size needed to display a grid in which the
		// (integer) cell size is as big as possible but the grid still fits in
		// the space available.
		float gridBorderWidth = (mGridPainter == null ? 0 : mGridPainter
				.getBorderPaint().getStrokeWidth());
		mGridCellSize = (float) Math
				.floor((float) (maxSize - 2 * gridBorderWidth)
						/ (float) mGridSize);
		mGridViewSize = (float) (2 * gridBorderWidth + mGridSize
				* mGridCellSize);

		setMeasuredDimension((int) mGridViewSize, (int) mGridViewSize);
	}

	private int measure(int measureSpec) {
		int specSize = MeasureSpec.getSize(measureSpec);

		return (int) (specSize);
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
					mGrid.increaseCounter(StatisticsCounterType.CHECK_PROGRESS_INVALIDS_FOUND);
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
	 * Sets the {@link DigitPositionGridType} used to position the digit buttons
	 * for reuse when drawing the maybe values.
	 * 
	 * @param digitPositionGrid
	 *            The digit position grid type to be set.
	 */
	public void setDigitPositionGrid(DigitPositionGrid digitPositionGrid) {
		mDigitPositionGrid = (mGrid == null
				|| !mGrid.hasPrefShowMaybesAs3x3Grid() ? null
				: digitPositionGrid);
	}

	/**
	 * Determine the maximum display frame for the grid view.
	 */
	public void updateWindowVisibleDisplayFrame() {
		// Update display frame to size on entire screen in current orientation.
		getWindowVisibleDisplayFrame(mDisplayFrame);

		// Determine whether adjustments are needed due to low aspect ratio
		// height/width.
		if (!mDisplayFrame.isEmpty()) {
			// Get orientation
			float newWidth = mDisplayFrame.width();
			float newHeight = mDisplayFrame.height();
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// In landscape mode the grid view should be resized in case
				// its size is bigger than 67% of the total width. Optimal
				// scaling factor has been determined based on a Nexus7
				// display.
				newWidth *= (float) 0.67;
			} else {
				// In portrait mode the grid view should be resized in case
				// its size is bigger than 60% of the total height. Optimal
				// scaling factor has been determined based on a Nexus7
				// display.
				newHeight *= (float) 0.6;
			}
			mDisplayFrame.set(0, 0, (int) newWidth, (int) newHeight);
		}
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
	 * Set the ticker tape for situation in which a swipe motions has just left
	 * the selected cell for the first time.
	 * 
	 * @param The
	 *            digit
	 */
	private void setTickerTapeOnLeavingSelectedCell(boolean selectableDigit) {
		// Hint can not be determined if grid is empty. Neither will hint be
		// showed for inactive grids.
		if (mGrid == null || mGrid.isActive() == false) {
			return;
		}

		// Create the ticker tape.
		if (mTickerTape != null) {
			mTickerTape.cancel();
		}
		mTickerTape = null;
		if (mPreferences.getSwipeValidMotionCounter() <= 10) {
			if (mTickerTape == null) {
				mTickerTape = new TickerTape(mContext);
			}

			mTickerTape.addItem(getResources().getString(
					(selectableDigit ? R.string.hint_swipe_release
							: R.string.hint_swipe_rotate)));
		}

		// Add hint for digit 5
		if (mGrid.getGridSize() >= 5
				&& mPreferences.getSwipeMotionCounter(5) < 6) {
			if (mTickerTape == null) {
				mTickerTape = new TickerTape(mContext);
			}
			mTickerTape.addItem(getResources().getString(
					R.string.hint_swipe_digit_5));
		}

		// Inform listeners about change
		if (mTickerTape != null && mOnTickerTapeChangedListener != null) {
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
}