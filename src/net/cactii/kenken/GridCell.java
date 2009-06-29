package net.cactii.kenken;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;

public class GridCell {
  // Index of the cell (left to right, top to bottom, zero-indexed)
  public int mCellNumber;
  // X grid position, zero indexed
  public int mColumn;
  // Y grid position, zero indexed
  public int mRow;
  // X pixel position
  public int mPosX;
  // Y pixel position
  public int mPosY;
  // Value of the digit in the cell
  public int mValue;
  // User's entered value
  public int mUserValue;
  // Id of the enclosing cage
  public int mCageId;
  // String of the cage
  public String mCageText;
  // View context
  public GridView mContext;
  
  public static final int BORDER_NONE = 0;
  public static final int BORDER_SOLID = 1;
  public static final int BORDER_DASHED = 2;

  public int[] mBorderTypes;
  
  private Paint mValuePaint;
  private Paint mDashedBorderPaint;
  private Paint mBorderPaint;
  private Paint mCageTextPaint;
  
  public GridCell(GridView context, int cell) {
    int gridSize = context.mGridSize;
    this.mContext = context;
    this.mCellNumber = cell;
    this.mColumn = cell % gridSize;
    this.mRow = (int)(cell / gridSize);
    this.mCageText = "";
    this.mCageId = -1;
    this.mValue = 0;

    this.mPosX = 0;
    this.mPosY = 0;
    
    this.mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mValuePaint.setColor(0xFF000000);
    this.mValuePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

    this.mDashedBorderPaint = new Paint();
    this.mDashedBorderPaint.setColor(0xFF000000);
    this.mDashedBorderPaint.setStrokeWidth(0);
    this.mDashedBorderPaint.setPathEffect(new DashPathEffect(new float[] {2, 2}, 0));
    
    this.mBorderPaint = new Paint();
    this.mBorderPaint.setColor(0xFF000000);
    this.mBorderPaint.setStrokeWidth(3);
    
    this.mCageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mCageTextPaint.setColor(0xFF000000);
    this.mCageTextPaint.setTextSize(14);
    this.mCageTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

    this.setBorders(BORDER_NONE, BORDER_NONE, BORDER_NONE, BORDER_NONE);
  }
  
  public String toString() {
    String str = "<cell:" + this.mCellNumber + " col:" + this.mColumn +
                  " row:" + this.mRow + " posX:" + this.mPosX + " posY:" +
                  this.mPosY + " val:" + this.mValue + ">";
    return str;
  }
  
  public void setBorders(int north, int east, int south, int west) {
    int[] borders = new int[4];
    borders[0] = north;
    borders[1] = east;
    borders[2] = south;
    borders[3] = west;
    this.mBorderTypes = borders;
  }
  private Paint getBorderPaint(int border) {
    switch (this.mBorderTypes[border]) {
      case BORDER_NONE:
        return null;
      case BORDER_DASHED:
        return this.mDashedBorderPaint;
      case BORDER_SOLID :
        return this.mBorderPaint;
    }
    return null;
  }
  
  public void onDraw(Canvas canvas) {
    int cellSize = this.mContext.getMeasuredWidth() / this.mContext.mGridSize;
    this.mPosX = cellSize * this.mColumn;
    this.mPosY = cellSize * this.mRow;

    // North
    Paint borderPaint = this.getBorderPaint(0);
    if (borderPaint != null)
      canvas.drawLine(this.mPosX, this.mPosY, cellSize, this.mPosY, borderPaint);
    
    // East
    borderPaint = this.getBorderPaint(1);
    if (borderPaint != null)
      canvas.drawLine(this.mPosX+cellSize, this.mPosY, this.mPosX+cellSize, this.mPosY+cellSize, borderPaint);
    
    // South
    borderPaint = this.getBorderPaint(2);
    if (borderPaint != null)
      canvas.drawLine(this.mPosX, this.mPosY+cellSize, this.mPosX+cellSize, this.mPosY+cellSize, borderPaint);
    
    // West
    borderPaint = this.getBorderPaint(3);
    if (borderPaint != null)
      canvas.drawLine(this.mPosX, this.mPosY, this.mPosX, this.mPosY+cellSize, borderPaint);
    
    int textSize = (int)(cellSize/2);
    this.mValuePaint.setTextSize(textSize);
    canvas.drawText("" + this.mValue, this.mPosX + cellSize/2 - textSize/4, this.mPosY + cellSize/2 + textSize/2, this.mValuePaint);

    if (!this.mCageText.equals("")) {
      canvas.drawText(this.mCageText, this.mPosX + 2, this.mPosY + 13, this.mCageTextPaint);
    }
  }
  
}
