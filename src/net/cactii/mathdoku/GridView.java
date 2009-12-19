package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

public class GridView extends View implements OnTouchListener {

	public static final int THEME_CARVED = 0;
	public static final int THEME_NEWSPAPER = 1;
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
  public Paint mDigitPaint;
  public Paint mCagePaint;
  public Paint mCageTextPaint;
  public Paint mGridBackgroundPaint;
  public int mBackgroundColor;

private Paint mShadePaint;

private Paint mSolvedPaint;
public Typeface mFace;
public boolean mDupedigits;
public boolean mBadMaths;

	// Date of current game (used for saved games)
	public long mDate;
	// Current theme
	public int mTheme;
  
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
    //this.mGridPaint.setPathEffect(new DashPathEffect(new float[] {2, 2}, 0));
    
    this.mBorderPaint = new Paint();
    this.mBorderPaint.setColor(0xFF000000);
    this.mBorderPaint.setStrokeWidth(3);
    
    this.mGridBackgroundPaint = new Paint();
    this.mGridBackgroundPaint.setColor(0xF0FFFFFF);
    this.mGridBackgroundPaint.setStrokeWidth(2);
    
    this.mCagePaint = new Paint();
    this.mCagePaint.setColor(0xFFF0D090);
    this.mCagePaint.setStrokeWidth(0);

    this.mCageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mCageTextPaint.setColor(0xFF000000);
    this.mCageTextPaint.setTextSize(10);
    this.mCageTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    
    this.mShadePaint = new Paint();
    this.mShadePaint.setColor(0xA0000000);
    this.mShadePaint.setStrokeWidth(0);
    
    this.mSolvedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mSolvedPaint.setColor(0xFF000000);
    this.mSolvedPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    this.mSolvedPaint.setTextSize(32);

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
	    this.mCagePaint.setPathEffect(new DiscretePathEffect(30, 1));
	    this.mBackgroundColor = 0x7ff0d090;
	  } else if (theme == THEME_NEWSPAPER) {
			this.mGridPaint.setPathEffect(new DashPathEffect(new float[] {2, 2}, 0));
		    this.mBorderPaint.setPathEffect(null);
		    this.mCagePaint.setPathEffect(null);
		    this.mBackgroundColor = 0xffffffff;
	  }
	  if (this.getMeasuredHeight() < 150)
		this.mBorderPaint.setStrokeWidth(1);
	  else
	  	this.mBorderPaint.setStrokeWidth(3);
	  
	  if (this.mCells != null)
		  for (GridCell cell : this.mCells)
			  cell.setTheme(theme);
  }
  
  public void reCreate() {
    //this.mGridSize = 6; // TODO: Change based on initalisation (xml?)
	this.mRandom = new Random();
    if (this.mGridSize < 4) return;
    this.mCells = new ArrayList<GridCell>();
    int cellnum = 0;
    for (int i = 0 ; i < this.mGridSize * this.mGridSize ; i++)
      this.mCells.add(new GridCell(this, cellnum++));
    randomiseGrid();
    this.mTrackPosX = this.mTrackPosY = 0;
    this.mCages = new ArrayList<GridCage>();
    CreateCages();
    this.mActive = true;
    this.mSelectorShown = false;
    this.setTheme(this.mTheme);
  }

  
  /* Returns whether the given cell # is a member of any cage */
  public boolean CellInAnyCage(int cell) {
    for (GridCage cage : this.mCages) {
      for (GridCell c : cage.mCells) {
        if (c.mCellNumber == cell)
          return true;
      }
    }
    return false;
  }
  
  // Returns cage id of cell at row, column
  // Returns -1 if not a valid cell or cage
  public int CageIdAt(int row, int column) {
    for (GridCell cell : this.mCells)
      if (cell.mRow == row && cell.mColumn == column)
        return cell.mCageId;
    return -1;
  }
  
  public int CreateSingleCages() {
    int singles = this.mGridSize / 2;
    int cageId = 0;
    for (int i = 0 ; i < singles ; i++) {
    	int cellNum;
    	boolean foundCell = false;
    	GridCell cell = null;
    	while (!foundCell) {
    		cellNum = this.mRandom.nextInt(this.mGridSize * this.mGridSize);
    		cell = this.mCells.get(cellNum);
    		foundCell = true;
    		for (GridCell c : this.mCells) {
    			if ((c.mRow == cell.mRow || c.mColumn == cell.mColumn) &&
    					c.mCageId > -1) {
    				foundCell = false;
    				break;
    			}
    			if (c.mCellNumber != cell.mCellNumber && c.mValue == cell.mValue &&
    					c.mCageId > -1) {
    				foundCell = false;
    				break;
    			}
    		}
    	}
    	GridCage cage = new GridCage(this);
    	cage.createCage(GridCage.CAGE_1, cell);
    	cage.setArithmetic();
    	cage.setCageId(cageId++);
    	this.mCages.add(cage);
    }
    return cageId;
  }
   
  /* Take a filled grid and randomly create cages */
  public void CreateCages() {
    int cageId = CreateSingleCages();

    for (int cellNum = 0 ; cellNum < this.mCells.size() ; cellNum++) {
      GridCell cell = this.mCells.get(cellNum);
      if (CellInAnyCage(cell.mCellNumber))
        continue; // Cell already in a cage, skip
      boolean validCage;
      int attempts = 0;
      while (true) {
        validCage = true;
        attempts++;
        GridCage cage = new GridCage(this);
        cage.createCage(GridCage.CAGE_UNDEF, cell);
        for (GridCell c : cage.mCells)
            if (CellInAnyCage(c.mCellNumber))
              validCage = false;

        if (cage.mType == GridCage.CAGE_UNDEF || attempts > 35) {
        	ClearAllCages();
        	cellNum = -1;
        	cageId = CreateSingleCages();
        	validCage = false;
        	break;
        }


        if (validCage) {
          cage.setArithmetic();  // Make the maths puzzle
          cage.setCageId(cageId++);  // Set cage's id
          this.mCages.add(cage);  // Add to the cage list
          break;
        }
      }
    }
    for (GridCage cage : this.mCages)
      cage.setBorders();
  }
  
  public void ClearAllCages() {
	  for (GridCell cell : this.mCells) {
		  cell.mCageId = -1;
		  cell.mCageText = "";
	  }
	  this.mCages = new ArrayList<GridCage>();
  }
  
  /* Fetch the cell at the given row, column */
  public GridCell getCellAt(int row, int column) {
    for (GridCell cell : this.mCells)
      if (cell.mRow == row && cell.mColumn == column)
        return cell;
    return null;
  }
  
  /*
   * Fills the grid with random numbers, per the rules:
   * 
   * - 1 to <rowsize> on every row and column
   * - No duplicates in any row or column.
   */
  public void randomiseGrid() {
    int attempts;
    for (int value = 1 ; value < this.mGridSize+1 ; value++) {
      attempts = 20;
      for (int row = 0 ; row < this.mGridSize ; row++) {
        GridCell cell;
        int column;
        while (true) {
          column = this.mRandom.nextInt(this.mGridSize);
          cell = getCellAt(row, column);
          if (--attempts == 0)
            break;
          if (cell.mValue != 0)
            continue;
          if (valueInColumn(column, value))
            continue;
          break;
        }
        if (attempts == 0) {
          this.clearValue(value--);
          break;
        }
        cell.mValue = value;
        //Log.d("KenKen", "New cell: " + cell);
      }
    }
  }
  /* Clear any cells containing the given number. */
  public void clearValue(int value) {
    for (GridCell cell : this.mCells)
      if (cell.mValue == value)
        cell.mValue = 0;
  }
  
  /* Determine if the given value is in the given row */
  public boolean valueInRow(int row, int value) {
    for (GridCell cell : this.mCells)
      if (cell.mRow == row && cell.mValue == value)
        return true;
    return false;
  }
  
  /* Determine if the given value is in the given column */
  public boolean valueInColumn(int column, int value) {
    for (GridCell cell : this.mCells)
      if (cell.mColumn == column && cell.mValue == value)
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
      return (int)(specSize);
  }
  
  @Override
  protected void onDraw(Canvas canvas) {

    if (this.mGridSize < 4) return;
    if (this.mCages == null) return;
    int width = getMeasuredWidth();

    if (width != this.mCurrentWidth)
      this.mCurrentWidth = width;

    // Fill canvas background
    canvas.drawColor(this.mBackgroundColor);
    
    // Check cage correctness
    for (GridCage cage : this.mCages)
      cage.userValuesCorrect();
    
    // Draw (dashed) grid
    for (int i = 1 ; i < this.mGridSize ; i++) {
      float pos = ((float)this.mCurrentWidth / (float)this.mGridSize) * i;
      canvas.drawLine(0, pos, this.mCurrentWidth, pos, this.mGridPaint);
      canvas.drawLine(pos, 0, pos, this.mCurrentWidth, this.mGridPaint);
    }
    
    // Draw cells
    for (GridCell cell : this.mCells) {
  	  if ((cell.mUserValue > 0 && this.getNumValueInCol(cell) > 1) ||
  			  (cell.mUserValue > 0 && this.getNumValueInRow(cell) > 1))
  		  cell.mShowWarning = true;
  	  else
  		  cell.mShowWarning = false;
        cell.onDraw(canvas);
    }

    
    // Draw borders
    canvas.drawLine(1, 1, this.mCurrentWidth-1, 1, this.mBorderPaint);
    canvas.drawLine(1, 1, 1, this.mCurrentWidth-1, this.mBorderPaint);
    canvas.drawLine(1, this.mCurrentWidth-1, this.mCurrentWidth-2, this.mCurrentWidth-2, this.mBorderPaint);
    canvas.drawLine(this.mCurrentWidth-1, 1, this.mCurrentWidth-2, this.mCurrentWidth-2, this.mBorderPaint);

    if (this.mActive && this.isSolved()) {
  	  if (this.mSolvedListener != null)
  		  this.mSolvedListener.puzzleSolved();
  	  if (this.mSelectedCell != null)
  		  this.mSelectedCell.mSelected = false;
  	  this.mActive = false;
    }

    	// this.drawSolvedResult(canvas);
  }
  
  // Given a cell number, returns origin x,y coordinates.
  private float[] CellToCoord(int cell) {
    float xOrd;
    float yOrd;
    int cellWidth = this.mCurrentWidth / this.mGridSize;
    xOrd = ((float)cell % this.mGridSize) * cellWidth;
    yOrd = ((int)(cell / this.mGridSize) * cellWidth);
    return new float[] {xOrd, yOrd};
  }
  
  // Opposite of above - given a coordinate, returns the cell number within.
  private GridCell CoordToCell(float x, float y) {
	  int row = (int) ((y / (float)this.mCurrentWidth) * this.mGridSize);
	  int col = (int) ((x / (float)this.mCurrentWidth) * this.mGridSize);
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
	
    int row = (int)((size - (size-y))/(size/this.mGridSize));
    if (row > this.mGridSize-1) row = this.mGridSize-1;
    if (row < 0) row = 0;

    int col = (int)((size - (size-x))/(size/this.mGridSize));
    if (col > this.mGridSize-1) col = this.mGridSize-1;
    if (col < 0) col = 0;
	
    // We can now get the cell.
    GridCell cell = getCellAt(row, col);
    this.mSelectedCell = cell;
    
    float[] cellPos = this.CellToCoord(cell.mCellNumber);
    this.mTrackPosX = cellPos[0];
    this.mTrackPosY = cellPos[1];

    for (GridCell c : this.mCells)
    	c.mSelected = false;
    if (this.mTouchedListener != null) {
    	this.playSoundEffect(SoundEffectConstants.CLICK);
    	this.mSelectedCell.mSelected = true;
    	this.mTouchedListener.gridTouched(this.mSelectedCell);
    }
    invalidate();
    return true;
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
    this.mTrackPosX += x*trackMult;
    this.mTrackPosY += y*trackMult;
    GridCell cell = this.CoordToCell(this.mTrackPosX, this.mTrackPosY);
    if (cell == null) {
    	this.mTrackPosX -= x*trackMult;
    	this.mTrackPosY -= y*trackMult;
    	return true;
    }
    // Set the cell as selected
    if (this.mSelectedCell != null) {
    	this.mSelectedCell.mSelected = false;
	    if (this.mSelectedCell != cell)
	    	this.mTouchedListener.gridTouched(cell);
    }
    this.mSelectedCell = cell;
    cell.mSelected = true;
    invalidate();
    return true;
  }
  
  
  // Return the number of times a given user value is in a row
  public int getNumValueInRow(GridCell ocell) {
	  int count = 0;
	  for (GridCell cell : this.mCells) {
		  if (cell.mRow == ocell.mRow && cell.mUserValue == ocell.mUserValue)
			  count++;
	  }
	  return count;
  }
  // Return the number of times a given user value is in a column
  public int getNumValueInCol(GridCell ocell) {
	  int count = 0;
	  for (GridCell cell : this.mCells) {
		  if (cell.mColumn == ocell.mColumn && cell.mUserValue == ocell.mUserValue)
			  count++;
	  }
	  return count;
  }
  
  // Solve the puzzle by setting the Uservalue to the actual value
  public void Solve() {
	  for (GridCell cell : this.mCells)
		  cell.mUserValue = cell.mValue;
	  invalidate();
  }
  
  // Returns whether the puzle is solved.
  public boolean isSolved() {
	  for (GridCell cell : this.mCells) {
		  if (cell.mUserValue < 1)
			  return false;
		  if (getNumValueInCol(cell) != 1)
			  return false;
		  if (getNumValueInRow(cell) != 1)
			  return false;
	  }
	  for (GridCage cage : this.mCages)
		  if (!cage.isSolved())
			  return false;
	  return true;
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
}
