package net.cactii.kenken;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;

public class GridView extends View implements OnTouchListener {

  // Size of the grid
  public int mGridSize;

  // Cages
  public ArrayList<GridCage> mCages;
  
  public ArrayList<GridCell> mCells;
  
  public int mCurrentWidth;
  public Paint mGridPaint;
  public Paint mBorderPaint;
  public Paint mDigitPaint;
  public Paint mCagePaint;
  public Paint mCageTextPaint;
  
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


    this.mGridPaint = new Paint();
    this.mGridPaint.setColor(0xFF000000);
    this.mGridPaint.setStrokeWidth(0);
    this.mGridPaint.setPathEffect(new DashPathEffect(new float[] {2, 2}, 0));
    
    this.mBorderPaint = new Paint();
    this.mBorderPaint.setColor(0xFF000000);
    this.mBorderPaint.setStrokeWidth(3);
    
    this.mCagePaint = new Paint();
    this.mCagePaint.setColor(0xFF000000);
    this.mCagePaint.setStrokeWidth(0);

    this.mCageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mCageTextPaint.setColor(0xFF000000);
    this.mCageTextPaint.setTextSize(10);
    this.mCageTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

    this.mCurrentWidth = 0;
    this.mGridSize = 0;
  }
  
  public void reCreate() {
    //this.mGridSize = 6; // TODO: Change based on initalisation (xml?)
    if (this.mGridSize < 4) return;
    this.mCells = new ArrayList<GridCell>();
    int cellnum = 0;
    for (int i = 0 ; i < this.mGridSize * this.mGridSize ; i++)
      this.mCells.add(new GridCell(this, cellnum++));
    randomiseGrid();
    this.mCages = new ArrayList<GridCage>();
    CreateCages();
    invalidate();
  }
  
  public boolean CellInAnyCage(int cell) {
    for (GridCage cage : this.mCages) {
      for (GridCell c : cage.mCells) {
        if (c.mCellNumber == cell)
          return true;
      }
    }
    return false;
  }
  
  public GridCell CellAtPos(int column, int row) {
    for (GridCell cell : this.mCells)
      if (cell.mRow == row && cell.mColumn == column)
        return cell;
    return null;
  }
  
  // Returns cage id of cell at row, column
  // Returns -1 if not a valid cell or cage
  public int CageIdAt(int row, int column) {
    for (GridCell cell : this.mCells)
      if (cell.mRow == row && cell.mColumn == column)
        return cell.mCageId;
    return -1;
  }
  
  public void CreateCages() {
    int cageId = 0;
    for (GridCell cell : this.mCells) {
      if (CellInAnyCage(cell.mCellNumber))
        continue;
      boolean validCage;
      while (true) {
        validCage = true;
        GridCage cage = new GridCage(this, -1, cell);
        for (GridCell c : cage.mCells) {
          if (CellInAnyCage(c.mCellNumber)) {
            validCage = false;
          }
        }
        if (validCage) {
          cage.setArithmetic();
          cage.setCageId(cageId++);
          this.mCages.add(cage);
          //Log.d("KenKen", "Added cage! Type " + cage.mType);
          break;
        }
      }      
    }
    for (GridCage cage : this.mCages)
      cage.setBorders();
  }
  
  public GridCell getCellAt(int row, int column) {
    for (GridCell cell : this.mCells)
      if (cell.mRow == row && cell.mColumn == column)
        return cell;
    return null;
  }
  
  public void randomiseGrid() {
    Random rand = new Random();
    int attempts;
    for (int value = 1 ; value < this.mGridSize+1 ; value++) {
      attempts = 20;
      for (int row = 0 ; row < this.mGridSize ; row++) {
        GridCell cell;
        int column;
        while (true) {
          column = rand.nextInt(this.mGridSize);
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
  public void clearValue(int value) {
    for (GridCell cell : this.mCells)
      if (cell.mValue == value)
        cell.mValue = 0;
  }
  
  public boolean valueInRow(int row, int value) {
    for (GridCell cell : this.mCells)
      if (cell.mRow == row && cell.mValue == value)
        return true;
    return false;
  }
  
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
      return (int)(specSize * 0.8);
  }
  
  @Override
  protected void onDraw(Canvas canvas) {

    if (this.mGridSize < 4) return;
    int width = getMeasuredWidth();

    if (width != this.mCurrentWidth)
      this.mCurrentWidth = width;

    canvas.drawARGB(255, 255, 255, 255);


    for (int i = 0 ; i < this.mGridSize ; i++) {
      float pos = (this.mCurrentWidth / this.mGridSize) * i;
      canvas.drawLine(0, pos, this.mCurrentWidth, pos, this.mGridPaint);
      canvas.drawLine(pos, 0, pos, this.mCurrentWidth, this.mGridPaint);
    }
    
    canvas.drawLine(0, 0, this.mCurrentWidth-1, 0, this.mBorderPaint);
    canvas.drawLine(0, 0, 0, this.mCurrentWidth-1, this.mBorderPaint);

    canvas.drawLine(0, this.mCurrentWidth-1, this.mCurrentWidth-1, this.mCurrentWidth-1, this.mBorderPaint);
    canvas.drawLine(this.mCurrentWidth-1, 0, this.mCurrentWidth-1, this.mCurrentWidth-1, this.mBorderPaint);
    
    for (GridCell cell : this.mCells)
      cell.onDraw(canvas);

  }
  
  private float[] CellToCoord(int cell) {
    float xOrd;
    float yOrd;
    int cellWidth = this.mCurrentWidth / this.mGridSize;
    xOrd = ((float)cell % this.mGridSize) * cellWidth;
    yOrd = ((int)(cell / this.mGridSize) * cellWidth);
    return new float[] {xOrd, yOrd};
  }

  @Override
  public boolean onTouch(View arg0, MotionEvent arg1) {
    // TODO Auto-generated method stub
    return false;
  }
  
  public enum Action {
    ACTION_NONE, ACTION_ADD, ACTION_SUBTRACT,
    ACTION_MULTIPLY, ACTION_DIVIDE
  }
  


}
