package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.Random;

import com.srlee.DLX.DLX.SolveType;
import com.srlee.DLX.MathDokuDLX;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class GridView extends View implements OnTouchListener {

	public static final int THEME_CARVED = 0;
	public static final int THEME_NEWSPAPER = 1;
	public static final int THEME_INVERT = 2;

	// Solved listener
	private OnSolvedListener mSolvedListener;
	// Touched listener
	public OnGridTouchListener mTouchedListener;

	// Size of the grid
	public int mGridSize;

	// Random generator
	public Random mRandom;

	public Activity mContext;

	// Cages
	public ArrayList<GridCage> mCages;

	public ArrayList<GridCell> mCells;

	public boolean mActive;

	public boolean mSelectorShown = false;

	public float mTrackPosX;
	public float mTrackPosY;

	public GridCell mSelectedCell;

	public TextView animText;

	public int mCurrentWidth;
	public Paint mGridPaint;
	public Paint mBorderPaint;
	public int mBackgroundColor;

	public Typeface mFace;
	public boolean mDupedigits;
	public boolean mBadMaths;

	// Date of current game (used for saved games)
	public long mDate;
	// Epalsed time
	public long mElapsed;
	// Current theme
	public int mTheme;

	// Keep track of all moves as soon as grid is built or restored.
	private ArrayList<Move> moves;

	// Used to avoid redrawing or saving grid during creation of new grid
	public final Object mLock = new Object();

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

	public void initGridView() {

		this.mSolvedListener = null;
		this.mDupedigits = true;
		this.mBadMaths = true;

		this.mGridPaint = new Paint();
		this.mGridPaint.setColor(0x80000000);
		this.mGridPaint.setStrokeWidth(0);
		// this.mGridPaint.setPathEffect(new DashPathEffect(new float[] {2, 2},
		// 0));

		this.mBorderPaint = new Paint();
		this.mBorderPaint.setColor(0xFF000000);
		this.mBorderPaint.setStrokeWidth(3);
		this.mBorderPaint.setStyle(Style.STROKE);

		this.mCurrentWidth = 0;
		this.mGridSize = 0;
		this.mActive = false;

		this.setOnTouchListener((OnTouchListener) this);
	}

	public void setTheme(int theme) {
		if (theme == THEME_CARVED) {
			this.mGridPaint.setAntiAlias(true);
			this.mGridPaint.setPathEffect(new DiscretePathEffect(20, 1));
			this.mGridPaint.setColor(0xbf906050);
			this.mBorderPaint.setAntiAlias(true);
			this.mBorderPaint.setPathEffect(new DiscretePathEffect(30, 1));
			this.mBorderPaint.setColor(0xff000000);
			this.mBackgroundColor = 0x7ff0d090;
		} else if (theme == THEME_NEWSPAPER) {
			this.mGridPaint.setPathEffect(new DashPathEffect(
					new float[] { 2, 2 }, 0));
			this.mBorderPaint.setAntiAlias(false);
			this.mBorderPaint.setPathEffect(null);
			this.mBorderPaint.setColor(0xff000000);
			this.mBackgroundColor = 0xffffffff;
		} else if (theme == THEME_INVERT) {
			this.mGridPaint.setColor(0xff7f7f7f);
			this.mGridPaint.setPathEffect(new DashPathEffect(
					new float[] { 2, 2 }, 0));
			this.mBorderPaint.setPathEffect(new DiscretePathEffect(30, 1));

			this.mBorderPaint.setPathEffect(null);
			this.mBorderPaint.setColor(0xffe0e0e0);
			this.mBackgroundColor = 0xff000000;
		}
		if (this.getMeasuredHeight() < 150)
			this.mBorderPaint.setStrokeWidth(1);
		else
			this.mBorderPaint.setStrokeWidth(3);

		if (this.mCells != null)
			for (GridCell cell : this.mCells)
				cell.setTheme(theme);
	}

	public void reCreate(boolean hideOperators) {
		synchronized (mLock) { // Avoid redrawing at the same time as creating
								// puzzle
			int num_solns;
			int num_attempts = 0;
			this.mRandom = new Random();
			if (this.mGridSize < 4)
				return;
			do {
				this.mCells = new ArrayList<GridCell>();
				this.mSelectedCell = null;
				int cellnum = 0;
				for (int i = 0; i < this.mGridSize * this.mGridSize; i++)
					this.mCells.add(new GridCell(this, cellnum++));
				randomiseGrid();
				this.mTrackPosX = this.mTrackPosY = 0;
				this.mCages = new ArrayList<GridCage>();
				CreateCages(hideOperators);
				num_attempts++;
				MathDokuDLX mdd = new MathDokuDLX(this.mGridSize, this.mCages);
				// Stop solving as soon as we find multiple solutions
				num_solns = mdd.Solve(SolveType.MULTIPLE);
				Log.d("MathDoku", "Num Solns = " + num_solns);
			} while (num_solns > 1);
			Log.d("MathDoku", "Num Attempts = " + num_attempts);
			this.mActive = true;
			this.mSelectorShown = false;
			this.setTheme(this.mTheme);
		}
	}

	// Returns cage id of cell at row, column
	// Returns -1 if not a valid cell or cage
	public int CageIdAt(int row, int column) {
		if (row < 0 || row >= mGridSize || column < 0 || column >= mGridSize)
			return -1;
		return this.mCells.get(column + row * this.mGridSize).getCageId();
	}

	public int CreateSingleCages(boolean hideOperators) {
		int singles = this.mGridSize / 2;
		boolean RowUsed[] = new boolean[mGridSize];
		boolean ColUsed[] = new boolean[mGridSize];
		boolean ValUsed[] = new boolean[mGridSize];
		for (int i = 0; i < singles; i++) {
			GridCell cell;
			while (true) {
				cell = mCells.get(mRandom.nextInt(mGridSize * mGridSize));
				if (!RowUsed[cell.getRow()] && !ColUsed[cell.getColumn()]
						&& !ValUsed[cell.getCorrectValue() - 1])
					break;
			}
			ColUsed[cell.getColumn()] = true;
			RowUsed[cell.getRow()] = true;
			ValUsed[cell.getCorrectValue() - 1] = true;
			GridCage cage = new GridCage(this, GridCage.CAGE_1, hideOperators);
			cage.mCells.add(cell);
			cage.setArithmetic();
			cage.setCageId(i);
			this.mCages.add(cage);
		}
		return singles;
	}

	/* Take a filled grid and randomly create cages */
	public void CreateCages(boolean hideOperators) {

		boolean restart;

		do {
			restart = false;
			int cageId = CreateSingleCages(hideOperators);
			for (int cellNum = 0; cellNum < this.mCells.size(); cellNum++) {
				GridCell cell = this.mCells.get(cellNum);
				if (cell.CellInAnyCage())
					continue; // Cell already in a cage, skip

				ArrayList<Integer> possible_cages = getvalidCages(cell);
				if (possible_cages.size() == 1) { // Only possible cage is a
													// single
					ClearAllCages();
					restart = true;
					break;
				}

				// Choose a random cage type from one of the possible (not
				// single cage)
				int cage_type = possible_cages.get(mRandom
						.nextInt(possible_cages.size() - 1) + 1);
				GridCage cage = new GridCage(this, cage_type, hideOperators);
				int[][] cage_coords = GridCage.CAGE_COORDS[cage_type];
				for (int coord_num = 0; coord_num < cage_coords.length; coord_num++) {
					int col = cell.getColumn() + cage_coords[coord_num][0];
					int row = cell.getRow() + cage_coords[coord_num][1];
					cage.mCells.add(getCellAt(row, col));
				}

				cage.setArithmetic(); // Make the maths puzzle
				cage.setCageId(cageId++); // Set cage's id
				this.mCages.add(cage); // Add to the cage list
			}
		} while (restart);
		for (GridCage cage : this.mCages)
			cage.setBorders();
	}

	public ArrayList<Integer> getvalidCages(GridCell origin) {
		if (origin.CellInAnyCage())
			return null;

		boolean[] InvalidCages = new boolean[GridCage.CAGE_COORDS.length];

		// Don't need to check first cage type (single)
		for (int cage_num = 1; cage_num < GridCage.CAGE_COORDS.length; cage_num++) {
			int[][] cage_coords = GridCage.CAGE_COORDS[cage_num];
			// Don't need to check first coordinate (0,0)
			for (int coord_num = 1; coord_num < cage_coords.length; coord_num++) {
				int col = origin.getColumn() + cage_coords[coord_num][0];
				int row = origin.getRow() + cage_coords[coord_num][1];
				GridCell c = getCellAt(row, col);
				if (c == null || c.CellInAnyCage()) {
					InvalidCages[cage_num] = true;
					break;
				}
			}
		}

		ArrayList<Integer> valid = new ArrayList<Integer>();
		for (int i = 0; i < GridCage.CAGE_COORDS.length; i++)
			if (!InvalidCages[i])
				valid.add(i);

		return valid;
	}

	public void ClearAllCages() {
		for (GridCell cell : this.mCells) {
			cell.clearCage();
		}
		this.mCages = new ArrayList<GridCage>();
	}

	public void clearUserValues() {
		if (this.moves != null) {
			this.moves.clear();
		}
		for (GridCell cell : this.mCells) {
			cell.clearUserValue();
		}
		this.invalidate();
	}

	/* Fetch the cell at the given row, column */
	public GridCell getCellAt(int row, int column) {
		if (row < 0 || row >= mGridSize)
			return null;
		if (column < 0 || column >= mGridSize)
			return null;

		return this.mCells.get(column + row * this.mGridSize);
	}

	/*
	 * Fills the grid with random numbers, per the rules:
	 * 
	 * - 1 to <rowsize> on every row and column - No duplicates in any row or
	 * column.
	 */
	public void randomiseGrid() {
		int attempts;
		for (int value = 1; value < this.mGridSize + 1; value++) {
			for (int row = 0; row < this.mGridSize; row++) {
				attempts = 20;
				GridCell cell;
				int column;
				while (true) {
					column = this.mRandom.nextInt(this.mGridSize);
					cell = getCellAt(row, column);
					if (--attempts == 0)
						break;
					if (cell.getCorrectValue() != 0)
						continue;
					if (valueInColumn(column, value))
						continue;
					break;
				}
				if (attempts == 0) {
					this.clearValue(value--);
					break;
				}
				cell.setCorrectValue(value);
				// Log.d("KenKen", "New cell: " + cell);
			}
		}
	}

	/* Clear any cells containing the given number. */
	public void clearValue(int value) {
		for (GridCell cell : this.mCells)
			if (cell.getCorrectValue() == value)
				cell.setCorrectValue(0);
	}

	/* Determine if the given value is in the given row */
	public boolean valueInRow(int row, int value) {
		for (GridCell cell : this.mCells)
			if (cell.getRow() == row && cell.getCorrectValue() == value)
				return true;
		return false;
	}

	/* Determine if the given value is in the given column */
	public boolean valueInColumn(int column, int value) {
		for (int row = 0; row < mGridSize; row++)
			if (this.mCells.get(column + row * mGridSize).getCorrectValue() == value)
				return true;
		return false;
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

	@Override
	protected void onDraw(Canvas canvas) {
		synchronized (mLock) { // Avoid redrawing at the same time as creating
								// puzzle
			if (this.mGridSize < 4)
				return;
			if (this.mCages == null)
				return;

			int width = getMeasuredWidth();

			if (width != this.mCurrentWidth)
				this.mCurrentWidth = width;

			// Fill canvas background
			canvas.drawColor(this.mBackgroundColor);

			// Check cage correctness
			for (GridCage cage : this.mCages)
				cage.userValuesCorrect();

			// Draw (dashed) grid
			for (int i = 1; i < this.mGridSize; i++) {
				float pos = ((float) this.mCurrentWidth / (float) this.mGridSize)
						* i;
				canvas.drawLine(0, pos, this.mCurrentWidth, pos,
						this.mGridPaint);
				canvas.drawLine(pos, 0, pos, this.mCurrentWidth,
						this.mGridPaint);
			}

			// Draw cells
			for (GridCell cell : this.mCells) {
				if ((cell.isUserValueSet() && this.getNumValueInCol(cell) > 1)
						|| (cell.isUserValueSet() && this
								.getNumValueInRow(cell) > 1))
					cell.mShowWarning = true;
				else
					cell.mShowWarning = false;
				cell.onDraw(canvas, false);
			}

			// Draw borders
			canvas.drawLine(0, 1, this.mCurrentWidth, 1, this.mBorderPaint);
			canvas.drawLine(1, 0, 1, this.mCurrentWidth, this.mBorderPaint);
			canvas.drawLine(0, this.mCurrentWidth - 2, this.mCurrentWidth,
					this.mCurrentWidth - 2, this.mBorderPaint);
			canvas.drawLine(this.mCurrentWidth - 2, 0, this.mCurrentWidth - 2,
					this.mCurrentWidth, this.mBorderPaint);

			// Draw cells again
			for (GridCell cell : this.mCells) {
				cell.onDraw(canvas, true);
			}
			// Draw highlights for current cage.
			if (this.mSelectedCell != null
					&& this.mSelectedCell.getCageId() < this.mCages.size()) {
				for (GridCell cell : this.mCages.get(this.mSelectedCell
						.getCageId()).mCells) {
					cell.onDraw(canvas, true);
				}
				// Draws highlights at top.
				this.mSelectedCell.onDraw(canvas, false);
			}
			// Callback if puzzle is solved.
			if (this.mActive && this.isSolved()) {
				if (this.mSolvedListener != null)
					this.mSolvedListener.puzzleSolved();
				if (this.mSelectedCell != null)
					this.mSelectedCell.mSelected = false;
				this.mActive = false;
			}
		}
	}

	// Given a cell number, returns origin x,y coordinates.
	private float[] CellToCoord(int cell) {
		float xOrd;
		float yOrd;
		int cellWidth = this.mCurrentWidth / this.mGridSize;
		xOrd = ((float) cell % this.mGridSize) * cellWidth;
		yOrd = ((int) (cell / this.mGridSize) * cellWidth);
		return new float[] { xOrd, yOrd };
	}

	// Opposite of above - given a coordinate, returns the cell number within.
	private GridCell CoordToCell(float x, float y) {
		int row = (int) ((y / (float) this.mCurrentWidth) * this.mGridSize);
		int col = (int) ((x / (float) this.mCurrentWidth) * this.mGridSize);
		// Log.d("KenKen", "Track x/y = " + col + " / " + row);
		return getCellAt(row, col);
	}

	public boolean onTouch(View arg0, MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return false;
		if (!this.mActive)
			return false;

		// Find out where the grid was touched.
		float x = event.getX();
		float y = event.getY();
		int size = getMeasuredWidth();

		int row = (int) ((size - (size - y)) / (size / this.mGridSize));
		if (row > this.mGridSize - 1)
			row = this.mGridSize - 1;
		if (row < 0)
			row = 0;

		int col = (int) ((size - (size - x)) / (size / this.mGridSize));
		if (col > this.mGridSize - 1)
			col = this.mGridSize - 1;
		if (col < 0)
			col = 0;

		// We can now get the cell.
		GridCell cell = getCellAt(row, col);
		float[] cellPos = this.CellToCoord(cell.getCellNumber());
		this.mTrackPosX = cellPos[0];
		this.mTrackPosY = cellPos[1];

		if (this.mSelectedCell != cell) {
			// Another cell was touched
			this.playSoundEffect(SoundEffectConstants.CLICK);
			SetSelectedCell(cell);
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
		if (!this.mActive || this.mSelectorShown)
			return false;
		// On press event, take selected cell, call touched listener
		// which will popup the digit selector.
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.mTouchedListener != null) {
				this.mSelectedCell.mSelected = true;
				this.mTouchedListener.gridTouched(this.mSelectedCell);
			}
			return true;
		}
		// A multiplier amplifies the trackball event values
		int trackMult = 70;
		switch (this.mGridSize) {
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
		if (this.mSelectedCell != null) {
			this.mSelectedCell.mSelected = false;
			if (this.mSelectedCell != cell)
				this.mTouchedListener.gridTouched(cell);
		}
		for (GridCell c : this.mCells) {
			c.mSelected = false;
			this.mCages.get(c.getCageId()).mSelected = false;
		}
		this.mSelectedCell = cell;
		cell.mSelected = true;
		this.mCages.get(this.mSelectedCell.getCageId()).mSelected = true;
		invalidate();
		return true;
	}

	// Return the number of times a given user value is in a row
	public int getNumValueInRow(GridCell ocell) {
		int count = 0;
		for (GridCell cell : this.mCells) {
			if (cell.getRow() == ocell.getRow()
					&& cell.getUserValue() == ocell.getUserValue())
				count++;
		}
		return count;
	}

	// Return the number of times a given user value is in a column
	public int getNumValueInCol(GridCell ocell) {
		int count = 0;
		for (GridCell cell : this.mCells) {
			if (cell.getColumn() == ocell.getColumn()
					&& cell.getUserValue() == ocell.getUserValue())
				count++;
		}
		return count;
	}

	// Solve the puzzle by setting the Uservalue to the actual value
	public void Solve() {
		if (this.moves != null) {
			this.moves.clear();
		}
		for (GridCell cell : this.mCells)
			cell.setUserValue(cell.getCorrectValue());
		invalidate();
	}

	// Returns whether the puzzle is solved.
	public boolean isSolved() {
		for (GridCell cell : this.mCells)
			if (!cell.isUserValueCorrect())
				return false;
		return true;
	}

	// Checks whether the user has made any mistakes
	public boolean isSolutionValidSoFar() {
		for (GridCell cell : this.mCells)
			if (cell.isUserValueSet())
				if (cell.getUserValue() != cell.getCorrectValue())
					return false;

		return true;
	}

	// Highlight those cells where the user has made a mistake
	public void markInvalidChoices() {
		boolean isValid = true;
		for (GridCell cell : this.mCells)
			if (cell.isUserValueSet())
				if (cell.getUserValue() != cell.getCorrectValue()) {
					cell.setInvalidHighlight(true);
					isValid = false;
				}

		if (!isValid)
			invalidate();

		return;
	}

	// Return the list of cells that are highlighted as invalid
	public ArrayList<GridCell> invalidsHighlighted() {
		ArrayList<GridCell> invalids = new ArrayList<GridCell>();
		for (GridCell cell : this.mCells)
			if (cell.getInvalidHighlight())
				invalids.add(cell);

		return invalids;
	}

	public void setSolvedHandler(OnSolvedListener listener) {
		this.mSolvedListener = listener;
	}

	public abstract class OnSolvedListener {
		public abstract void puzzleSolved();
	}

	public void setOnGridTouchListener(OnGridTouchListener listener) {
		this.mTouchedListener = listener;
	}

	public abstract class OnGridTouchListener {
		public abstract void gridTouched(GridCell cell);
	}

	public void AddMove(Move move) {
		if (moves == null) {
			moves = new ArrayList<Move>();
		}

		boolean identicalToLastMove = false;
		int indexLastMove = moves.size() - 1;
		if (indexLastMove >= 0) {
			Move lastMove = moves.get(indexLastMove);
			identicalToLastMove = lastMove.equals(move);
		}
		if (!identicalToLastMove) {
			moves.add(move);
		}
	}

	public void UndoLastMove() {
		if (moves != null) {
			int undoPosition = moves.size() - 1;

			if (undoPosition >= 0) {
				moves.get(undoPosition).Undo();
				moves.remove(undoPosition);
				invalidate();
			}
		}
	}

	public void SetSelectedCell(GridCell cell) {
		mSelectedCell = cell;

		for (GridCell c : this.mCells) { // IMPROVE: use old value of
											// mSelectedCell to deselect cell
											// and cage ...
			c.mSelected = false;
			this.mCages.get(c.getCageId()).mSelected = false;
		}

		if (this.mTouchedListener != null) {
			this.mSelectedCell.mSelected = true;
			this.mCages.get(this.mSelectedCell.getCageId()).mSelected = true;
		}
	}
}
