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
import net.cactii.mathdoku.util.UsageLog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
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

	// Size of the grid view and cells in grid
	public float mGridViewSize;
	private float mGridCellSize;

	public float mTrackPosX;
	public float mTrackPosY;

	public boolean mSelectorShown = false;

	// Reference to the global grid painter object
	private GridPainter mGridPainter;

	// The layout to be used for positioning the maybe digits in a grid.
	private DigitPositionGrid mDigitPositionGrid;

	public TextView mAnimationText;

	// Visible window rectangle
	private Rect mDisplayFrame;

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
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// Remember which cell was selected before.
			mPreviouslyTouchedCell = mGrid.getSelectedCell();

			// Find out where the grid was touched.
			float x = event.getX();
			float y = event.getY();
			int size = getMeasuredWidth();

			int gridSize = mGrid.getGridSize();
			int row = (int) ((size - (size - y)) / (size / gridSize));
			if (row > gridSize - 1)
				row = gridSize - 1;
			if (row < 0)
				row = 0;

			int col = (int) ((size - (size - x)) / (size / gridSize));
			if (col > gridSize - 1)
				col = gridSize - 1;
			if (col < 0)
				col = 0;

			// We can now get the cell.
			GridCell cell = mGrid.getCellAt(row, col);
			float[] cellPos = this.cellToCoordinates(cell.getCellNumber());
			this.mTrackPosX = cellPos[0];
			this.mTrackPosY = cellPos[1];

			// Select new cell
			mGrid.setSelectedCell(cell);
		}

		// On up event complete processing of cell selection.
		if (event.getAction() == MotionEvent.ACTION_UP) {
			this.playSoundEffect(SoundEffectConstants.CLICK);

			if (this.mTouchedListener != null) {
				// Determine if same cell was touched again
				boolean sameCellSelectedAgain = (mGrid.getSelectedCell() != null && mGrid
						.getSelectedCell().equals(mPreviouslyTouchedCell));

				mTouchedListener.gridTouched(mGrid.getSelectedCell(),
						sameCellSelectedAgain);
			}
		}

		invalidate();

		return false;
	}

	// Handle trackball, both press down, and scrolling around to
	// select a cell.
	public boolean onTrackballEvent(MotionEvent event) {
		if (!this.mGrid.isActive() || this.mSelectorShown)
			return false;

		UsageLog.getInstance().logTrackball(mGrid.toGridDefinitionString());

		// On press event, take selected cell, call touched listener
		// which will popup the digit selector.
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.mTouchedListener != null) {
				mGrid.getSelectedCell().mSelected = true;
				this.mTouchedListener.gridTouched(mGrid.getSelectedCell(),
						false); // TODO: test on change of cell?
			}
			return true;
		}
		// A multiplier amplifies the trackball event values
		int trackMult = 70;
		switch (mGrid.getGridSize()) {
		case 4:
			// fall through
		case 5:
			trackMult = 60;
			break;
		case 6:
			trackMult = 50;
			break;
		case 7:
			// fall through
		case 8:
			// fall through
		case 9:
			trackMult = 40;
			break;
		}
		// Fetch the trackball position, work out the cell it's at
		float x = event.getX();
		float y = event.getY();
		this.mTrackPosX += x * trackMult;
		this.mTrackPosY += y * trackMult;
		GridCell cell = this
				.coordinatesToCell(this.mTrackPosX, this.mTrackPosY);
		if (cell == null) {
			this.mTrackPosX -= x * trackMult;
			this.mTrackPosY -= y * trackMult;
			return true;
		}
		// Set the cell as selected
		if (mGrid.getSelectedCell() != null) {
			mGrid.getSelectedCell().mSelected = false;
			if (mGrid.getSelectedCell() != cell) // TODO: test with toggling
													// input mode
				this.mTouchedListener.gridTouched(cell, false);
		}
		for (GridCell c : mGrid.mCells) {
			c.mSelected = false;
			mGrid.mCages.get(c.getCageId()).mSelected = false;
		}
		mGrid.setSelectedCell(cell);
		cell.mSelected = true;
		mGrid.mCages.get(mGrid.getSelectedCell().getCageId()).mSelected = true;
		invalidate();
		return true;
	}

	// Given a cell number, returns origin x,y coordinates.
	private float[] cellToCoordinates(int cell) {
		float xOrd;
		float yOrd;
		int gridSize = mGrid.getGridSize();
		xOrd = ((float) cell % gridSize) * mGridCellSize;
		yOrd = ((int) (cell / gridSize) * mGridCellSize);
		return new float[] { xOrd, yOrd };
	}

	// Opposite of above - given a coordinate, returns the cell number within.
	private GridCell coordinatesToCell(float x, float y) {
		int gridSize = mGrid.getGridSize();
		int row = (int) ((y / mGridViewSize) * gridSize);
		int col = (int) ((x / mGridViewSize) * gridSize);
		return mGrid.getCellAt(row, col);
	}

	public GridCell getSelectedCell() {
		return mGrid.getSelectedCell();
	}

	public void digitSelected(int newValue,
			InputMode inputMode) {
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
			if (oldValue != 0) {
				selectedCell.clearPossibles();
				selectedCell.setUserValue(0);
				mGrid.getGridStatistics().increaseCounter(
						StatisticsCounterType.CELL_CLEARED);
				mGrid.getGridStatistics().increaseCounter(
						StatisticsCounterType.CELLS_EMPTY);
				mGrid.getGridStatistics().decreaseCounter(
						StatisticsCounterType.CELLS_FILLED);

				// In case the last user value has been cleared in the grid, the
				// check progress should nog longer be available.
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
						((PuzzleFragmentActivity) mContext).invalidateOptionsMenu();
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
			// puzzle
			int gridSize = mGrid.getGridSize();

			if (gridSize < 3)
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
		this.mGrid = grid;
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
		int gridSize = (mGrid == null ? 1 : mGrid.getGridSize());
		float gridBorderWidth = (mGridPainter == null ? 0 : mGridPainter
				.getBorderPaint().getStrokeWidth());
		mGridCellSize = (float) Math
				.floor((float) (maxSize - 2 * gridBorderWidth)
						/ (float) gridSize);
		mGridViewSize = (float) (2 * gridBorderWidth + gridSize * mGridCellSize);

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