package net.cactii.mathdoku;

import net.cactii.mathdoku.MainActivity.InputMode;
import net.cactii.mathdoku.Painter.GridPainter;
import android.content.Context;
import android.graphics.Canvas;
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

	// Actual content of the puzzle in this grid view
	private Grid grid;

	// Touched listener
	public OnGridTouchListener mTouchedListener;

	public float mGridViewSize;
	public float mTrackPosX;
	public float mTrackPosY;

	public boolean mSelectorShown = false;

	// Reference to the global grid painter object
	private GridPainter mGridPainter;

	public TextView animText;

	public GridView(Context context) {
		super(context);
		initGridView();
	}

	public GridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGridView();
	}

	public GridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGridView();
	}

	private void initGridView() {
		this.mGridViewSize = 0;

		this.setOnTouchListener((OnTouchListener) this);
	}

	public void setOnGridTouchListener(OnGridTouchListener listener) {
		this.mTouchedListener = listener;
	}

	public abstract class OnGridTouchListener {
		public abstract void gridTouched(GridCell cell);
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

		if (grid.getSelectedCell() == cell) {
			// The selected cell has been touch again.
			((MainActivity) getContext()).toggleInputMode();
		} else {
			// Another cell was touched
			this.playSoundEffect(SoundEffectConstants.CLICK);
			grid.setSelectedCell(cell);
			if (this.mTouchedListener != null) {
				grid.getSelectedCell().mSelected = true;
				grid.getCageForSelectedCell().mSelected = true;
			}
		}
		if (this.mTouchedListener != null) {
			this.mTouchedListener.gridTouched(cell);
		}
		invalidate();

		return false;
	}

	// Handle trackball, both press down, and scrolling around to
	// select a cell.
	public boolean onTrackballEvent(MotionEvent event) {
		if (!this.grid.isActive() || this.mSelectorShown)
			return false;
		// On press event, take selected cell, call touched listener
		// which will popup the digit selector.
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.mTouchedListener != null) {
				grid.getSelectedCell().mSelected = true;
				this.mTouchedListener.gridTouched(grid.getSelectedCell());
			}
			return true;
		}
		// A multiplier amplifies the trackball event values
		int trackMult = 70;
		switch (grid.getGridSize()) {
		case 5:
			trackMult = 60;
			break;
		case 6:
			trackMult = 50;
			break;
		case 7:
			trackMult = 40;
			break;
		case 8:
			trackMult = 40;
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
			if (grid.getSelectedCell() != cell)
				this.mTouchedListener.gridTouched(cell);
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
		int cellWidth = (int) (this.mGridViewSize / gridSize);
		xOrd = ((float) cell % gridSize) * cellWidth;
		yOrd = ((int) (cell / gridSize) * cellWidth);
		return new float[] { xOrd, yOrd };
	}

	// Opposite of above - given a coordinate, returns the cell number within.
	private GridCell CoordToCell(float x, float y) {
		int gridSize = grid.getGridSize();
		int row = (int) ((y / (float) this.mGridViewSize) * gridSize);
		int col = (int) ((x / (float) this.mGridViewSize) * gridSize);
		return grid.getCellAt(row, col);
	}

	public GridCell getSelectedCell() {
		return grid.getSelectedCell();
	}

	public void digitSelected(int value, MainActivity.InputMode inputMode) {
		// Display a message in case no cell is selected.
		GridCell selectedCell = grid.getSelectedCell();
		if (selectedCell == null) {
			Toast.makeText(this.getContext(),
					R.string.select_cell_before_value, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		
		// Save undo information
		CellChange orginalUserMove = selectedCell.saveUndoInformation(null);
		
		
		if (value == 0) { // Clear Button
			selectedCell.clearPossibles();
			selectedCell.setUserValue(0);
		} else {
			switch(inputMode) {
			case MAYBE:
				if (selectedCell.isUserValueSet()) {
					selectedCell.clearUserValue();
				}
				selectedCell.togglePossible(value);
				break;
			case NORMAL:
				selectedCell.setUserValue(value);
				selectedCell.clearPossibles();

				if (((MainActivity) getContext()).preferences.getBoolean(
						MainActivity.PREF_CLEAR_REDUNDANT_POSSIBLES,
						MainActivity.PREF_CLEAR_REDUNDANT_POSSIBLES_DEFAULT)) {
					// Update possible values for other cells in this row and
					// column.
					grid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
				}

				break;
			case NONE:
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

		// Get painter if not yet retrieved.
		if (mGridPainter == null) {
			mGridPainter = Painter.getInstance(this.getContext()).mGridPainter;
		}

		synchronized (grid.mLock) { // Avoid redrawing at the same time as
									// creating
			// puzzle
			int gridSize = grid.getGridSize();

			if (gridSize < 4)
				return;
			if (grid.mCages == null)
				return;

			// Get the size of the gridview. As it is a square, either width or
			// height can be used.
			float realGridViewSize = getMeasuredWidth();
			float gridBorderWidth = mGridPainter.mBorderPaint.getStrokeWidth();
			float cellSize = (float) Math
					.floor((float) (realGridViewSize - 2 * gridBorderWidth)
							/ (float) gridSize);
			mGridViewSize = (float) (2 * gridBorderWidth + gridSize * cellSize);

			// Draw background (can not use canvas.drawColor here as we maybe do
			// not use entire canvas)
			canvas.drawRect((float) 0, (float) 0, mGridViewSize, mGridViewSize,
					mGridPainter.mBackgroundPaint);

			// Draw borders around grid. Use an of offset of 50% of strokewidth
			// to ensure that full width of border is visible inside the
			// gridview.
			// Note: the offset will 0 for borders of width 1.
			float gridBorderOffset = (float) Math
					.floor((float) (0.5 * gridBorderWidth));
			canvas.drawLine(0, 0 + gridBorderOffset, this.mGridViewSize,
					0 + gridBorderOffset, mGridPainter.mBorderPaint); // Top
			canvas.drawLine(this.mGridViewSize - gridBorderOffset, 0,
					this.mGridViewSize - gridBorderOffset, this.mGridViewSize,
					mGridPainter.mBorderPaint); // right
			canvas.drawLine(0, this.mGridViewSize - gridBorderOffset,
					this.mGridViewSize, this.mGridViewSize - gridBorderOffset,
					mGridPainter.mBorderPaint); // bottom
			canvas.drawLine(0 + gridBorderOffset, 0, 0 + gridBorderOffset,
					this.mGridViewSize, mGridPainter.mBorderPaint); // left

			// Draw cells, except for cells in selected cage
			InputMode inputMode = ((MainActivity) getContext()).getInputMode();
			Painter.getInstance(this.getContext()).setCellSize(cellSize);
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
		// Our target grid is a square, measuring 80% of the minimum dimension
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);

		int dim = Math.min(measuredWidth, measuredHeight);

		setMeasuredDimension(dim, dim);
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
}