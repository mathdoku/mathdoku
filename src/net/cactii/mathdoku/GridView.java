package net.cactii.mathdoku;

import net.cactii.mathdoku.Painter.GridPainter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Path;
import android.preference.PreferenceManager;
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

	private boolean mDupedigits;
	private boolean mBadMaths;

	public int mCurrentWidth;
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
		this.mDupedigits = true;
		this.mBadMaths = true;

		this.mCurrentWidth = 0;

		this.setOnTouchListener((OnTouchListener) this);

		Painter painter = Painter.getInstance(this.getContext());
		painter.setGridBorder((this.getMeasuredHeight() < 150));
		this.mGridPainter = painter.mGridPainter;
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

		if (grid.getSelectedCell() != cell) {
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
		int cellWidth = this.mCurrentWidth / gridSize;
		xOrd = ((float) cell % gridSize) * cellWidth;
		yOrd = ((int) (cell / gridSize) * cellWidth);
		return new float[] { xOrd, yOrd };
	}

	// Opposite of above - given a coordinate, returns the cell number within.
	private GridCell CoordToCell(float x, float y) {
		int gridSize = grid.getGridSize();
		int row = (int) ((y / (float) this.mCurrentWidth) * gridSize);
		int col = (int) ((x / (float) this.mCurrentWidth) * gridSize);
		return grid.getCellAt(row, col);
	}

	public GridCell getSelectedCell() {
		return grid.getSelectedCell();
	}

	public void digitSelected(int value, boolean maybeSelected) {
		GridCell selectedCell = grid.getSelectedCell();
		if (selectedCell == null) {
			Toast.makeText(this.getContext(),
					R.string.select_cell_before_value, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		CellChange orginalUserMove = selectedCell.saveUndoInformation(null);
		if (value == 0) { // Clear Button
			selectedCell.clearPossibles();
			selectedCell.setUserValue(0);

		} else {
			if (maybeSelected) {
				if (selectedCell.isUserValueSet())
					selectedCell.clearUserValue();
				selectedCell.togglePossible(value);
			} else {
				selectedCell.setUserValue(value);
				selectedCell.clearPossibles();
			}

			if (((MainActivity) getContext()).preferences.getBoolean(
					"redundantPossibles", false)) {
				// Update possible values for other cells in this row and
				// column.
				grid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
			}
		}
	}

	public void setPreferences(SharedPreferences preferences) {
		mDupedigits = preferences.getBoolean("dupedigits", true);
		mBadMaths = preferences.getBoolean("badmaths", true);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		synchronized (grid.mLock) { // Avoid redrawing at the same time as
									// creating
			// puzzle
			int gridSize = grid.getGridSize();

			if (gridSize < 4)
				return;
			if (grid.mCages == null)
				return;

			int width = getMeasuredWidth();

			if (width != this.mCurrentWidth)
				this.mCurrentWidth = width;

			// Fill canvas background
			canvas.drawColor(mGridPainter.mBackgroundColor);

			// Check cage correctness
			for (GridCage cage : grid.mCages)
				cage.userValuesCorrect(mBadMaths);

			// Draw (dashed) grid
			for (int i = 1; i < gridSize; i++) {
				float pos = ((float) this.mCurrentWidth / (float) gridSize) * i;

				// Due to a bug
				// (https://code.google.com/p/android/issues/detail?id=29944), a
				// dashed line can not be drawn with drawLine at API-level 11 or above. 
				drawDashedLine(canvas, 0, pos, this.mCurrentWidth, pos);
				drawDashedLine(canvas, pos, 0, pos, this.mCurrentWidth);
			}

			// Get current setting for how to display possible values in a
			// cell.
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getContext());
			boolean possibleValuesIn3x3Grid = prefs
					.getBoolean("maybe3x3", true);

			// Draw cells
			Painter.getInstance().setCellSize((float) width / (float) gridSize);
			for (GridCell cell : grid.mCells) {
				cell.checkWithOtherValuesInRowAndColumn();
				cell.onDraw(canvas, false, mDupedigits, possibleValuesIn3x3Grid);
			}

			// Draw borders
			canvas.drawLine(0, 1, this.mCurrentWidth, 1,
					mGridPainter.mOuterPaint);
			canvas.drawLine(1, 0, 1, this.mCurrentWidth,
					mGridPainter.mOuterPaint);
			canvas.drawLine(0, this.mCurrentWidth - 2, this.mCurrentWidth,
					this.mCurrentWidth - 2, mGridPainter.mOuterPaint);
			canvas.drawLine(this.mCurrentWidth - 2, 0, this.mCurrentWidth - 2,
					this.mCurrentWidth, mGridPainter.mOuterPaint);

			// Draw cells again
			for (GridCell cell : grid.mCells) {
				cell.onDraw(canvas, true, mDupedigits, possibleValuesIn3x3Grid);
			}
			// Draw highlights for current cage.
			GridCage selectedCage = grid.getCageForSelectedCell();
			if (selectedCage != null) {
				for (GridCell cell : grid.getCageForSelectedCell().mCells) {
					cell.onDraw(canvas, true, mDupedigits,
							possibleValuesIn3x3Grid);
				}
				// Draws highlights for selected cell at top.
				grid.getSelectedCell().onDraw(canvas, false, mDupedigits,
						possibleValuesIn3x3Grid);
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

	private void drawDashedLine(Canvas canvas, float left, float top,
			float right, float bottom) {
		Path path = new Path();
		path.moveTo(left, top);
		path.lineTo(right, bottom);
		canvas.drawPath(path, mGridPainter.mInnerPaint);
	}
}