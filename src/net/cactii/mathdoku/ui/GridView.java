package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.CellChange;
import net.cactii.mathdoku.DigitPositionGrid;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.hint.Hint;
import net.cactii.mathdoku.hint.OnHintChangedListener;
import net.cactii.mathdoku.painter.GridPainter;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.tip.TipIncorrectValue;
import net.cactii.mathdoku.tip.TipInputModeChanged;
import net.cactii.mathdoku.tip.TipOrderOfValuesInCage;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class GridView extends View implements OnTouchListener {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridView";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG_GRID_VIEW_SWYPE = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	// Context and preferences in context
	Context mContext;
	Preferences mPreferences;

	// Actual content of the puzzle in this grid view
	private Grid mGrid;

	// Listeners
	public OnGridTouchListener mTouchedListener;
	private OnHintChangedListener mOnHintChangedListener;

	// Previously touched cell
	private GridCell mPreviouslyTouchedCell;

	// Size (in cells and pixels) of the grid view and size (in pixel) of cells
	// in grid
	private int mGridSize;
	public float mGridViewSize;
	private float mGridCellSize;

	// Reference to the global grid painter object
	private GridPainter mGridPainter;

	// The layout to be used for positioning the maybe digits in a grid.
	private DigitPositionGrid mDigitPositionGrid;

	// Visible window rectangle
	private Rect mDisplayFrame;

	// Cell on which last touch down event took place
	private int mRowLastTouchDownCell;
	private int mColLastTouchDownCell;
	private boolean mSwypeBorderIsVisible;
	private float mXPosSwype;
	private float mYPosSwype;
	private boolean mSwypeHasLeftSelectedCell;
	private int mSwypeDigit;

	// The input mode in which the puzzle can be.
	public enum InputMode {
		NORMAL, // Digits entered are handled as a new cell value
		MAYBE, // Digits entered are handled to toggle the possible value on/of
	};

	InputMode mInputMode;

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

		// Set listeners
		this.setOnTouchListener((OnTouchListener) this);
		mOnHintChangedListener = null;
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
			GridCell selectedCell = mGrid.getCellAt(mRowLastTouchDownCell,
					mColLastTouchDownCell);
			mGrid.setSelectedCell(selectedCell);

			// Inform listener of puzzle fragment of start of motion event.
			mTouchedListener.gridTouched(selectedCell);

			// When starting a swype motion, the swype digit is not yet known.
			mSwypeDigit = -1;

			// While moving the swype position, a line is drawn from the middle
			// of the initially touched cell to the middle of the cell which is
			// hovered by the swype position. This variable will be set as soon
			// as the swype motion leave this cell.
			mSwypeHasLeftSelectedCell = false;

			// Indicate to the onDraw method that the swype border has to be
			// shown.
			// TODO: for advanced used who succesfully have used this function
			// the border can be hidden.
			mSwypeBorderIsVisible = true;

			// Store current position of swype position
			mXPosSwype = event.getX();
			mYPosSwype = event.getY();

			// Show the basic swype hint. Replace this hint after a short pause.
			if (mOnHintChangedListener != null
					&& mPreferences.getSwipeValidMotionCounter() < 30) {
				class RepeatHint extends Hint {
					public RepeatHint(
							OnHintChangedListener onHintChangedListener) {
						super(onHintChangedListener);
					}

					@Override
					public boolean repeat() {
						return mSwypeBorderIsVisible;
					}

					@Override
					public String getNextHint() {
						return showUnfinishedSwypeHint(getRepetitionCounter());
					}
				}
				;

				new RepeatHint(mOnHintChangedListener)
						.setInitialDelay(1500)
						.setRepeatDelay(3000)
						.show(getResources().getString(
								R.string.hint_swipe_basic));
			}

			invalidate();

			// Do not allow other view to respond to this action, for example by
			// handling the long press, which would result in malfunction of the
			// swype motion.
			return true;
		case MotionEvent.ACTION_UP:
			if (this.mTouchedListener != null) {
				this.playSoundEffect(SoundEffectConstants.CLICK);

				mSwypeDigit = getSwypeDigit(event);
				if (mSwypeDigit > 0 && mSwypeDigit <= mGridSize) {
					// A swype motion for a valid digit was completed.

					// Set the swype digit as selected value for the cell which
					// was initially touched.
					digitSelected(mSwypeDigit);

					// Update preferences and inform listeners
					if (mOnHintChangedListener != null) {
						if (mPreferences
								.increaseSwipeValidMotionCounter(mSwypeDigit) <= 6) {
							Hint hint = new Hint(mOnHintChangedListener);
							hint.setEraseDelay(3000)
									.show(getResources()
											.getString(
													R.string.hint_swipe_valid_digit_completed,
													mSwypeDigit));
						}
					}
				} else if (mSwypeDigit > mGridSize
						&& mOnHintChangedListener != null
						&& mPreferences.increaseSwipeInvalidMotionCounter() <= 6) {
					// Inform listeners about completing a swype motion for a
					// invalid digit.
					Hint hint = new Hint(mOnHintChangedListener);
					hint.setEraseDelay(3000)
							.show(getResources()
									.getString(
											R.string.hint_swipe_invalid_digit_completed));
				}

				// Get selected cell
				selectedCell = mGrid.getSelectedCell();

				// In case no valid swype motion was made, determine if same
				// cell was touched again.
				if (mSwypeDigit == -1 && selectedCell != null
						&& selectedCell.equals(mPreviouslyTouchedCell)) {

					// Toggle the inputMode
					mInputMode = (mInputMode == InputMode.MAYBE ? InputMode.NORMAL
							: InputMode.MAYBE);

					if (TipInputModeChanged.toBeDisplayed(mPreferences)) {
						new TipInputModeChanged(mContext, mInputMode).show();
					}
				}

				// Inform listener of puzzle fragment about the release action
				mTouchedListener.gridTouched(selectedCell);

				// Hide swype border if displayed.
				if (mSwypeBorderIsVisible) {
					mSwypeBorderIsVisible = false;
					invalidate();
				}
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			// What was the swype position when the previous ACTION_MOVE event
			// was processed.
			int coordinatesPrevious[] = getSwypePosition(mXPosSwype, mYPosSwype);

			// Store current position of swype position
			mXPosSwype = event.getX();
			mYPosSwype = event.getY();

			// Get the current coordinates of the swype position
			int coordinatesCurrent[] = getSwypePosition(mXPosSwype, mYPosSwype);

			int newSwypeDigit = getSwypeDigit(event);
			if (newSwypeDigit > 0 && mSwypeDigit != newSwypeDigit) {
				mSwypeHasLeftSelectedCell = true;
				mSwypeDigit = newSwypeDigit;

				// As the swype digit has been changed, the grid view needs to
				// be updated.
				invalidate();
			} else {
				// Check if swype position has changed enough to be updated. For
				// performance reasons, the grid view is only update in case the
				// current swype position has moved to another cell.
				if (coordinatesPrevious[0] != coordinatesCurrent[0]
						|| coordinatesPrevious[1] != coordinatesCurrent[1]) {
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
			Painter.getInstance(mContext).setCellSize(mGridCellSize,
					mDigitPositionGrid);
			for (GridCell cell : mGrid.mCells) {
				cell.checkWithOtherValuesInRowAndColumn();
				cell.draw(canvas, gridBorderWidth, mInputMode);
			}

			// Draw the overlay for the selected cell
			if (mSwypeBorderIsVisible) {
				GridCell gridCell = mGrid.getSelectedCell();
				if (gridCell != null) {
					gridCell.drawOverlay(canvas, gridBorderWidth, mInputMode,
							mXPosSwype, mYPosSwype, mSwypeDigit);
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

		// Set default input mode to normal
		mInputMode = InputMode.NORMAL;

		if (mPreferences.getSwipeValidMotionCounter() < 30) {
			new Hint(mOnHintChangedListener).show(getResources().getString(
					R.string.hint_swipe_basic));
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
	 * Converts a given position (pixels) to coordinates relative to the grid.
	 * 
	 * @param xPos
	 *            The absolute x-position on the display
	 * @param yPos
	 *            The absolute y-position on the display
	 * @return The (x,y)-position relative to the grid. For x-position -1 means
	 *         left of grid, mGridSize means right of grid. For y-position -1
	 *         means above grid, mGridSize means below grid.
	 */
	int[] getSwypePosition(float xPos, float yPos) {
		int[] coordinates = { -1, -1 };

		// Convert x-position to a column number. -1 means left of grid,
		// mGridSize means right of grid.
		xPos = xPos / mGridCellSize;
		if (xPos > mGridSize) {
			coordinates[0] = mGridSize;
		} else if (xPos < 0) {
			coordinates[0] = -1;
		} else {
			coordinates[0] = (int) xPos;
		}

		// Convert y-position to a column number. -1 means above grid, mGridSize
		// means below grid.
		yPos = yPos / mGridCellSize;
		if (yPos > mGridSize) {
			coordinates[1] = mGridSize;
		} else if (yPos < 0) {
			coordinates[1] = -1;
		} else {
			coordinates[1] = (int) yPos;
		}

		return coordinates;
	}

	private int getSwypeDigit(MotionEvent event) {
		int[] coordinatesCurrent = getSwypePosition(event.getX(), event.getY());

		if (coordinatesCurrent[0] == mColLastTouchDownCell
				&& coordinatesCurrent[1] == mRowLastTouchDownCell) {
			// As long as the swype position has not left the cell, the swype
			// digit is undetermined. As the cell has been left at least once,
			// return to the cell will result in choosing digit 5.
			return (mSwypeHasLeftSelectedCell ? 5 : -1);
		}

		// In case the swype position is not inside the originally selected
		// cell, the digit is determined based on the angle of the swype line.

		// Get the start position of the swype line.
		float[] startCoordinates = mGrid.getSelectedCell()
				.getCellCentreCoordinates(
						mGridPainter.getBorderPaint().getStrokeWidth());

		// Compute current swype position relative to start position of swype.
		// Note the delta's are computed in such a way that we can compute the
		// angle between a horizontal line which crosses the start coordinates
		// and the swype line.
		float deltaX = startCoordinates[0] - event.getX();
		float deltaY = startCoordinates[1] - event.getY();
		double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));

		if (DEBUG_GRID_VIEW_SWYPE) {
			Log.i(TAG, "getSwypeDigitOnAngle");
			Log.i(TAG, " - start = (" + startCoordinates[0] + ", "
					+ startCoordinates[1] + ")");
			Log.i(TAG, " - current = (" + event.getX() + ", " + event.getY()
					+ ")");
			Log.i(TAG, " - deltaX = " + deltaX + " - deltaY = " + deltaY);
			Log.i(TAG, " - angle = " + angle);
		}

		// Starting from the horizontal line which crosses the start coordinate
		// we will divide the circle in 16 segments of each 22.5 degrees. Each
		// digit (1 - 4 and 6 to 9) will be associated with two segments. Note
		// that for segments below the x-asis the segments are order
		// counter-clockwise.
		if (angle >= 0) {
			if (angle <= 22.5) {
				return 4;
			} else if (angle <= 22.5 + 45) {
				return 1;
			} else if (angle <= 22.5 + 90) {
				return 2;
			} else if (angle <= 22.5 + 135) {
				return 3;
			} else {
				return 6;
			}
		} else {
			angle *= -1;
			if (angle <= 22.5) {
				return 4;
			} else if (angle <= 22.5 + 45) {
				return 7;
			} else if (angle <= 22.5 + 90) {
				return 8;
			} else if (angle <= 22.5 + 135) {
				return 9;
			} else {
				return 6;
			}
		}
	}

	/**
	 * Register the listener to call in case a hint has to be set.
	 * 
	 * @param onHintChangedListener
	 *            The listener to call in case a hint has to be set.
	 */
	public void setOnHintChangedListener(
			OnHintChangedListener onHintChangedListener) {
		mOnHintChangedListener = onHintChangedListener;
	}

	/**
	 * Get the appropriate basic swype hint.
	 */
	private String showUnfinishedSwypeHint(int hintCounterCurrentSwipeMotion) {
		// Hint can not be determined if grid is empty. Neither will hint be
		// showed for inactive grids.
		if (mGrid == null || mGrid.isActive() == false) {
			return null;
		}

		int gridSize = mGrid.getGridSize();
		int swipeMotionCounter = mPreferences.getSwipeValidMotionCounter();

		// For grid size 5 and above the hints will be alternated.
		if (gridSize >= 5 && hintCounterCurrentSwipeMotion % 2 == 0
				&& mPreferences.getSwipeMotionCounter(5) < 6) {
			return getResources().getString(R.string.hint_swipe_digit_5);
		} else if (swipeMotionCounter < 20
				&& mSwypeHasLeftSelectedCell == false) {
			// Swype motion has not yet left the cell. In case a cell is
			// selected at the border of the grid view, not all digits in the
			// swype border might be visible.
			GridCell selectedCell = mGrid.getSelectedCell();

			boolean isTopRow = (selectedCell.getRow() == 0);
			boolean isBottomRow = (selectedCell.getRow() == gridSize - 1);
			boolean isLeftColumn = (selectedCell.getColumn() == 0);
			boolean isRightColumn = (selectedCell.getColumn() == gridSize - 1);

			if (isTopRow || isBottomRow || isLeftColumn || isRightColumn) {
				// List for all digits which can not be shown.
				boolean digitNotVisible[] = { false, false, false, false,
						false, false, false, false, false, false, false, false };

				if (isTopRow) {
					digitNotVisible[1] = true;
					digitNotVisible[2] = true;
					digitNotVisible[3] = true;
				}
				if (isLeftColumn) {
					digitNotVisible[1] = true;
					digitNotVisible[4] = true;
					if (gridSize >= 7) {
						digitNotVisible[7] = true;
					}
				}
				if (isRightColumn) {
					digitNotVisible[3] = true;
					if (gridSize >= 6) {
						digitNotVisible[6] = true;
						if (gridSize >= 9) {
							digitNotVisible[9] = true;
						}
					}
				}
				if (isBottomRow && gridSize >= 7) {
					digitNotVisible[7] = true;
					if (gridSize >= 8) {
						digitNotVisible[8] = true;
						if (gridSize >= 9) {
							digitNotVisible[9] = true;
						}
					}
				}
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
					return getResources().getString(
							R.string.hint_swipe_basic_cell_at_border, digits);
				}
			}
			return getResources().getString(R.string.hint_swipe_outside_cell);
		} else if (swipeMotionCounter <= 10) {
			if (mSwypeDigit >= 1 && mSwypeDigit <= mGrid.getGridSize()) {
				// Swype motion has left the cell and a valid swype digit will
				// be selected when the motion stops.
				return getResources().getString(R.string.hint_swipe_release);
			} else {
				// Swype motion has left the cell but no valid swype digit will
				// be selected when the motion stops at this moment.
				return getResources().getString(R.string.hint_swipe_rotate);
			}
		} else {
			// Clear hint
			return "";
		}
	}
}