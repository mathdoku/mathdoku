package net.cactii.mathdoku;

import net.cactii.mathdoku.DigitPositionGrid.DigitPositionGridType;
import net.cactii.mathdoku.MainActivity.InputMode;
import net.cactii.mathdoku.Tip.TipOrderOfValuesInCage;
import net.cactii.mathdoku.painter.GridPainter;
import net.cactii.mathdoku.painter.Painter;
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
	MainActivity mMainActivity;
	Preferences mPreferences;

	// Actual content of the puzzle in this grid view
	private Grid grid;

	// Touched listener
	public OnGridTouchListener mTouchedListener;

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

	public TextView animText;

	// Visible window rectangle
	private Rect mDisplayFrame;

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
		mMainActivity = (MainActivity) context;
		mPreferences = Preferences.getInstance(mMainActivity);

		mGridViewSize = 0;
		mGridPainter = Painter.getInstance(mMainActivity).getGridPainter();

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
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return false;
		if (!this.grid.isActive())
			return false;

		// Find out where the grid was touched.
		float x = event.getX();
		float y = event.getY();
		int size = getMeasuredWidth();

		int gridSize = grid.getGridSize();
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
		GridCell cell = grid.getCellAt(row, col);
		float[] cellPos = this.CellToCoord(cell.getCellNumber());
		this.mTrackPosX = cellPos[0];
		this.mTrackPosY = cellPos[1];

		// Determine if same cell was touched again
		boolean sameCellSelectedAgain = (grid.getSelectedCell() == null ? false
				: grid.getSelectedCell().equals(cell));

		// Select new cell
		this.playSoundEffect(SoundEffectConstants.CLICK);
		grid.setSelectedCell(cell);
		if (this.mTouchedListener != null) {
			mTouchedListener.gridTouched(cell, sameCellSelectedAgain);
		}

		invalidate();

		return false;
	}

	// Handle trackball, both press down, and scrolling around to
	// select a cell.
	public boolean onTrackballEvent(MotionEvent event) {
		if (!this.grid.isActive() || this.mSelectorShown)
			return false;

		UsageLog.getInstance().logTrackball(grid.getSignatureString());

		// On press event, take selected cell, call touched listener
		// which will popup the digit selector.
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.mTouchedListener != null) {
				grid.getSelectedCell().mSelected = true;
				this.mTouchedListener
						.gridTouched(grid.getSelectedCell(), false); // TODO;
																		// test
																		// on
																		// change
																		// of
																		// cell?
			}
			return true;
		}
		// A multiplier amplifies the trackball event values
		int trackMult = 70;
		switch (grid.getGridSize()) {
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
		GridCell cell = this.CoordToCell(this.mTrackPosX, this.mTrackPosY);
		if (cell == null) {
			this.mTrackPosX -= x * trackMult;
			this.mTrackPosY -= y * trackMult;
			return true;
		}
		// Set the cell as selected
		if (grid.getSelectedCell() != null) {
			grid.getSelectedCell().mSelected = false;
			if (grid.getSelectedCell() != cell) // TODO: test with toggling
												// input mode
				this.mTouchedListener.gridTouched(cell, false);
		}
		for (GridCell c : grid.mCells) {
			c.mSelected = false;
			grid.mCages.get(c.getCageId()).mSelected = false;
		}
		grid.setSelectedCell(cell);
		cell.mSelected = true;
		grid.mCages.get(grid.getSelectedCell().getCageId()).mSelected = true;
		invalidate();
		return true;
	}

	// Given a cell number, returns origin x,y coordinates.
	private float[] CellToCoord(int cell) {
		float xOrd;
		float yOrd;
		int gridSize = grid.getGridSize();
		xOrd = ((float) cell % gridSize) * mGridCellSize;
		yOrd = ((int) (cell / gridSize) * mGridCellSize);
		return new float[] { xOrd, yOrd };
	}

	// Opposite of above - given a coordinate, returns the cell number within.
	private GridCell CoordToCell(float x, float y) {
		int gridSize = grid.getGridSize();
		int row = (int) ((y / mGridViewSize) * gridSize);
		int col = (int) ((x / mGridViewSize) * gridSize);
		return grid.getCellAt(row, col);
	}

	public GridCell getSelectedCell() {
		return grid.getSelectedCell();
	}

	public void digitSelected(int value, MainActivity.InputMode inputMode) {
		// Display a message in case no cell is selected.
		GridCell selectedCell = grid.getSelectedCell();
		if (selectedCell == null) {
			Toast.makeText(mMainActivity, R.string.select_cell_before_value,
					Toast.LENGTH_SHORT).show();
			return;
		}

		// Save undo information
		CellChange orginalUserMove = selectedCell.saveUndoInformation(null);

		if (value == 0) { // Clear Button
			selectedCell.clearPossibles();
			selectedCell.setUserValue(0);
		} else {
			if (TipOrderOfValuesInCage.toBeDisplayed(mPreferences,
					selectedCell.getCage())) {
				new TipOrderOfValuesInCage(mMainActivity).show();
			}
			switch (inputMode) {
			case MAYBE:
				if (selectedCell.isUserValueSet()) {
					selectedCell.clearUserValue();
				}
				selectedCell.togglePossible(value);
				break;
			case NORMAL:
				selectedCell.setUserValue(value);
				selectedCell.clearPossibles();

				if (mPreferences.isClearRedundantPossiblesEnabled()) {
					// Update possible values for other cells in this row and
					// column.
					grid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
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
		if (grid == null) {
			// As long as no grid has been attached to the gridview, it can not
			// be drawn.
			return;
		}

		synchronized (grid.mLock) { // Avoid redrawing at the same time as
									// creating
			// puzzle
			int gridSize = grid.getGridSize();

			if (gridSize < 4)
				return;
			if (grid.mCages == null)
				return;

			float gridBorderWidth = mGridPainter.getBorderPaint()
					.getStrokeWidth();

			// Draw grid background and border grid
			canvas.drawColor(mGridPainter.getBackgroundPaint().getColor());
			canvas.drawRect((float) 1, (float) 1, mGridViewSize, mGridViewSize,
					mGridPainter.getBorderPaint());

			// Draw cells, except for cells in selected cage
			InputMode inputMode = mMainActivity.getInputMode();
			Painter.getInstance(mMainActivity).setCellSize(mGridCellSize,
					mDigitPositionGrid);
			for (GridCell cell : grid.mCells) {
				cell.checkWithOtherValuesInRowAndColumn();
				cell.draw(canvas, gridBorderWidth, inputMode);
			}
		}
	}

	public void loadNewGrid(Grid grid) {
		mSelectorShown = false;
		this.grid = grid;
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
		int gridSize = (grid == null ? 1 : grid.getGridSize());
		float gridBorderWidth = (mGridPainter == null ? 0 : mGridPainter
				.getBorderPaint().getStrokeWidth());
		mGridCellSize = (float) Math
				.floor((float) (maxSize - 2 * gridBorderWidth)
						/ (float) gridSize);
		mGridViewSize = (float) (2 * gridBorderWidth + gridSize * mGridCellSize);

		setMeasuredDimension((int) mGridViewSize, (int) mGridViewSize);
	}

	private int measure(int measureSpec) {

		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.UNSPECIFIED)
			return 180;
		else
			return (int) (specSize);
	}

	// Highlight those cells where the user has made a mistake
	public void markInvalidChoices() {
		boolean isValid = true;
		for (GridCell cell : grid.mCells)
			if (cell.isUserValueSet())
				if (cell.getUserValue() != cell.getCorrectValue()) {
					cell.setInvalidHighlight(true);
					isValid = false;
				}

		if (!isValid)
			invalidate();

		return;
	}

	/**
	 * Sets the {@link DigitPositionGridType} used to position the digit buttons
	 * for reuse when drawing the maybe values.
	 * 
	 * @param digitPositionGrid
	 *            The digit position grid type to be set.
	 */
	public void setDigitPositionGrid(DigitPositionGrid digitPositionGrid) {
		mDigitPositionGrid = (grid == null
				|| !grid.hasPrefShowMaybesAs3x3Grid() ? null
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