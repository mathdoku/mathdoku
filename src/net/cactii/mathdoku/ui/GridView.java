package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.CellChange;
import net.cactii.mathdoku.DigitPositionGrid;
import net.cactii.mathdoku.DigitPositionGrid.DigitPositionGridType;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.painter.GridPainter;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.tip.TipIncorrectValue;
import net.cactii.mathdoku.tip.TipOrderOfValuesInCage;
import net.cactii.mathdoku.ui.PuzzleFragment.InputMode;
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

	// Touched listener
	public OnGridTouchListener mTouchedListener;

	// Previously touched cell
	private GridCell mPreviouslyTouchedCell;

	// Size (in cells and pixels) of the grid view and size (in pixel) of cells
	// in grid
	private int mGridSize;
	public float mGridViewSize;
	private float mGridCellSize;

	public boolean mSelectorShown = false;

	// Reference to the global grid painter object
	private GridPainter mGridPainter;

	// The layout to be used for positioning the maybe digits in a grid.
	private DigitPositionGrid mDigitPositionGrid;

	// Visible window rectangle
	private Rect mDisplayFrame;

	// Cell on which last touch down event took place
	private int mRowLastTouchDownCell;
	private int mColLastTouchDownCell;
	private boolean mHasLeftCellAfterTouchDown;

	// Used to grab the current input mode from.
	public InputModeDeterminer mInputModeDeterminer;

	public static interface InputModeDeterminer {
		/**
		 * Returns the current input mode the game this grid view is a part of,
		 * is in.
		 */
		public InputMode getInputMode();
	}

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
		mGridPainter = Painter.getInstance(mContext).getGridPainter();

		// Initialize the display frame for the grid view.
		mDisplayFrame = new Rect();
		updateWindowVisibleDisplayFrame();

		// Set listener
		this.setOnTouchListener((OnTouchListener) this);
	}

	public void setOnGridTouchListener(OnGridTouchListener listener) {
		this.mTouchedListener = listener;
	}

	public abstract class OnGridTouchListener {
		public abstract void gridTouched(GridCell cell,
				boolean sameCellSelectedAgain);
	}

	public boolean onTouch(View arg0, MotionEvent event) {
		if (mGrid == null || !mGrid.isActive())
			return false;

		// On down event select the cell but no further processing until we are
		// sure the long press event has been caught.
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Remember which cell was selected before.
			mPreviouslyTouchedCell = mGrid.getSelectedCell();

			// Find out where the grid was touched.
			mRowLastTouchDownCell = (int) (event.getY() / mGridCellSize);
			if (mRowLastTouchDownCell > mGridSize - 1) {
				mRowLastTouchDownCell = mGridSize - 1;
			} else if (mRowLastTouchDownCell < 0) {
				mRowLastTouchDownCell = 0;
			}
			mColLastTouchDownCell = (int) (event.getX() / mGridCellSize);
			if (mColLastTouchDownCell > mGridSize - 1) {
				mColLastTouchDownCell = mGridSize - 1;
			} else if (mColLastTouchDownCell < 0) {
				mColLastTouchDownCell = 0;
			}

			// Select new cell
			mGrid.setSelectedCell(mGrid.getCellAt(mRowLastTouchDownCell,
					mColLastTouchDownCell));
			mHasLeftCellAfterTouchDown = false;

			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			if (this.mTouchedListener != null) {
				this.playSoundEffect(SoundEffectConstants.CLICK);

				// Find out where the grid was released
				float rowRelease = event.getY() / mGridCellSize;
				if (rowRelease > mGridSize) {
					rowRelease = mGridSize;
				} else if (rowRelease < 0) {
					rowRelease = -1;
				} else {
					rowRelease = (int) rowRelease;
				}
				float colRelease = event.getX() / mGridCellSize;
				if (colRelease > mGridSize) {
					colRelease = mGridSize;
				} else if (colRelease < 0) {
					colRelease = -1;
				} else {
					colRelease = (int) colRelease;
				}

				// If a valid swype movement was made, set the value
				// corresponding to the swype in the cell.
				boolean swyped = false;
				if (((int) colRelease == mColLastTouchDownCell)
						&& ((int) rowRelease == mRowLastTouchDownCell)) {
					if (mHasLeftCellAfterTouchDown && mGridSize >= 5) {
						// A swipe has been made outside the cell which was
						// initially touched but it was release again a the same
						// cell.
						swyped = true;
						digitSelected(5, mInputModeDeterminer.getInputMode());
						invalidate();
					}
				} else {
					// A swipe has been made outside the cell which was
					// initially touched. Fill the initially touched cell with a
					// user value dependent on the direction the initially
					// touched cell was left.
					if (colRelease < mColLastTouchDownCell
							&& rowRelease < mRowLastTouchDownCell
							&& mGridSize >= 1) {
						// Release cell is to upper left of touched cell
						swyped = true;
						digitSelected(1, mInputModeDeterminer.getInputMode());
					} else if (colRelease == mColLastTouchDownCell
							&& rowRelease < mRowLastTouchDownCell
							&& mGridSize >= 2) {
						// Release cell is to upper of touched cell
						swyped = true;
						digitSelected(2, mInputModeDeterminer.getInputMode());
					} else if (colRelease > mColLastTouchDownCell
							&& rowRelease < mRowLastTouchDownCell
							&& mGridSize >= 3) {
						// Release cell is to upper right of touched cell
						swyped = true;
						digitSelected(3, mInputModeDeterminer.getInputMode());
					} else if (colRelease < mColLastTouchDownCell
							&& rowRelease == mRowLastTouchDownCell
							&& mGridSize >= 4) {
						// Release cell is to left of touched cell
						swyped = true;
						digitSelected(4, mInputModeDeterminer.getInputMode());
					} else if (colRelease > mColLastTouchDownCell
							&& rowRelease == mRowLastTouchDownCell
							&& mGridSize >= 6) {
						// Release cell is to right of touched cell
						swyped = true;
						digitSelected(6, mInputModeDeterminer.getInputMode());
					} else if (colRelease < mColLastTouchDownCell
							&& rowRelease > mRowLastTouchDownCell
							&& mGridSize >= 7) {
						// Release cell is to bottom left of touched cell
						swyped = true;
						digitSelected(7, mInputModeDeterminer.getInputMode());
					} else if (colRelease == mColLastTouchDownCell
							&& rowRelease > mRowLastTouchDownCell
							&& mGridSize >= 8) {
						// Release cell is to bottom of touched cell
						swyped = true;
						digitSelected(8, mInputModeDeterminer.getInputMode());
					} else if (colRelease > mColLastTouchDownCell
							&& rowRelease > mRowLastTouchDownCell
							&& mGridSize >= 9) {
						// Release cell is to bottom right of touched cell
						swyped = true;
						digitSelected(9, mInputModeDeterminer.getInputMode());
					}
					invalidate();
				}

				// In case no valid swype movement was made, determine if same cell was touched again.
				boolean sameCellSelectedAgain = (swyped == false && mGrid.getSelectedCell() != null && mGrid
						.getSelectedCell().equals(mPreviouslyTouchedCell));

				// Inform listener of puzzle fragment about the release action
				mTouchedListener.gridTouched(mGrid.getSelectedCell(),
						sameCellSelectedAgain);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (!mHasLeftCellAfterTouchDown) {
				// Check if touch cell is left
				float rowRelease = event.getY() / mGridCellSize;
				if (rowRelease < 0 || rowRelease > mGridSize
						|| (int) rowRelease != mRowLastTouchDownCell) {
					mHasLeftCellAfterTouchDown = true;
					break;
				}
				float colRelease = event.getX() / mGridCellSize;
				if (colRelease < 0 || colRelease > mGridSize
						|| (int) colRelease != mColLastTouchDownCell) {
					mHasLeftCellAfterTouchDown = true;
					break;
				}
			}
			break;
		default:
			break;
		}

		return false;
	}

	public GridCell getSelectedCell() {
		return mGrid.getSelectedCell();
	}

	public void digitSelected(int newValue, InputMode inputMode) {
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
			switch (inputMode) {
			case MAYBE:
				if (selectedCell.isUserValueSet()) {
					selectedCell.clear();
				}
				if (selectedCell.hasPossible(newValue)) {
					selectedCell.removePossible(newValue);
				} else {
					selectedCell.addPossible(newValue);
					mGrid.increaseCounter(StatisticsCounterType.POSSIBLES);
				}
				break;
			case NORMAL:
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
				break;
			case NO_INPUT__DISPLAY_GRID:
			case NO_INPUT__HIDE_GRID:
				// Should not be possible
				return;
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

			// Draw cells, except for cells in selected cage
			InputMode inputMode = mInputModeDeterminer.getInputMode();
			Painter.getInstance(mContext).setCellSize(mGridCellSize,
					mDigitPositionGrid);
			for (GridCell cell : mGrid.mCells) {
				cell.checkWithOtherValuesInRowAndColumn();
				cell.draw(canvas, gridBorderWidth, inputMode);
			}
		}
	}

	public void loadNewGrid(Grid grid) {
		mSelectorShown = false;

		mGrid = grid;

		// Compute grid size. Set to 1 in case grid is null to avoid problems in
		// onMeasure as this will be called before the grid is loaded.
		mGridSize = (mGrid == null ? 1 : mGrid.getGridSize());

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
					cell.setInvalidHighlight(true);
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
		// height/width. Small screens have specific layouts which do no need
		// further adjustments.
		if (!getResources().getString(R.string.dimension).startsWith("small")) {
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
	}
}