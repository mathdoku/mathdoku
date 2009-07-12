package net.cactii.mathdoku;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

public class GridCell {
  // Index of the cell (left to right, top to bottom, zero-indexed)
  public int mCellNumber;
  // X grid position, zero indexed
  public int mColumn;
  // Y grid position, zero indexed
  public int mRow;
  // X pixel position
  public float mPosX;
  // Y pixel position
  public float mPosY;
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
  // User's candidate digits
  public ArrayList<Integer> mPossibles;
  // Whether to show warning background (duplicate value in row/col)
  public boolean mShowWarning;
  
  public static final int BORDER_NONE = 0;
  public static final int BORDER_SOLID = 1;
  public static final int BORDER_DASHED = 2;
  public static final int BORDER_WARN = 3;

  public int[] mBorderTypes;
  
  private Paint mValuePaint;
  private Paint mDashedBorderPaint;
  private Paint mBorderPaint;
  
  private Paint mWrongBorderPaint;
  private Paint mCageTextPaint;
  private Paint mPossiblesPaint;
  private Paint mWarningPaint;
  
  public GridCell(GridView context, int cell) {
    int gridSize = context.mGridSize;
    this.mContext = context;
    this.mCellNumber = cell;
    this.mColumn = cell % gridSize;
    this.mRow = (int)(cell / gridSize);
    this.mCageText = "";
    this.mCageId = -1;
    this.mValue = 0;
    this.mUserValue = 0;
    this.mShowWarning = false;

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
    this.mBorderPaint.setStrokeWidth(2);
    
    this.mWrongBorderPaint = new Paint();
    this.mWrongBorderPaint.setColor(0xFFBB0000);
    this.mWrongBorderPaint.setStrokeWidth(2);
    
    this.mWarningPaint = new Paint();
    this.mWarningPaint.setColor(0x30FF0000);
    this.mWarningPaint.setStyle(Paint.Style.FILL);
    
    this.mCageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mCageTextPaint.setColor(0xFF000000);
    this.mCageTextPaint.setTextSize(14);
    this.mCageTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
   
    this.mPossiblesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mPossiblesPaint.setColor(0xFF000000);
    this.mPossiblesPaint.setTextSize(10);
    this.mPossiblesPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
    
    this.mPossibles = new ArrayList<Integer>();
    //this.mPossibles.add(1);
    //this.mPossibles.add(2);
    //this.mPossibles.add(3);
    //this.mPossibles.add(4);

    //this.mPossibles.add(5);
    
    this.setBorders(BORDER_NONE, BORDER_NONE, BORDER_NONE, BORDER_NONE);
  }
  
  public String toString() {
    String str = "<cell:" + this.mCellNumber + " col:" + this.mColumn +
                  " row:" + this.mRow + " posX:" + this.mPosX + " posY:" +
                  this.mPosY + " val:" + this.mValue + ", userval: " + this.mUserValue + ">";
    return str;
  }
  
  /* Sets the cells border type to the given values.
   * 
   * Border is BORDER_NONE, BORDER_SOLID or BORDER_DASHED.
   */
  public void setBorders(int north, int east, int south, int west) {
    int[] borders = new int[4];
    borders[0] = north;
    borders[1] = east;
    borders[2] = south;
    borders[3] = west;
    this.mBorderTypes = borders;
  }
  
  /* Returns the Paint object for the given border of this cell. */
  private Paint getBorderPaint(int border) {
    switch (this.mBorderTypes[border]) {
      case BORDER_NONE:
        return null;
      case BORDER_DASHED:
        return this.mDashedBorderPaint;
      case BORDER_SOLID :
        return this.mBorderPaint;
      case BORDER_WARN :
        return this.mWrongBorderPaint;
    }
    return null;
  }
  
  public void togglePossible(int digit) {
	  if (this.mPossibles.indexOf(new Integer(digit)) == -1)
		  this.mPossibles.add(digit);
	  else
		  this.mPossibles.remove(new Integer(digit));
  }
  
  public void setUserValue(int digit) {
    this.mPossibles.clear();
    this.mUserValue = digit;
  }
  
  /* Draw the cell. Border and text is drawn. */
  public void onDraw(Canvas canvas) {
    
    // Calculate x and y for the cell origin (topleft)
    float cellSize = (float)this.mContext.getMeasuredWidth() / (float)this.mContext.mGridSize;
    this.mPosX = cellSize * this.mColumn;
    this.mPosY = cellSize * this.mRow;
    
    if (this.mShowWarning) {
    	canvas.drawRect(this.mPosX, this.mPosY, this.mPosX + cellSize, this.mPosY + cellSize, this.mWarningPaint);
    }
    // North
    Paint borderPaint = this.getBorderPaint(0);
    if (borderPaint != null)
      canvas.drawLine(this.mPosX, this.mPosY, this.mPosX + cellSize, this.mPosY, borderPaint);
    
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
    
    // Cell value
    if (this.mUserValue > 0) {
	    int textSize = (int)(cellSize/2);
	    this.mValuePaint.setTextSize(textSize);
	    canvas.drawText("" + this.mUserValue, this.mPosX + cellSize/2 - textSize/4, this.mPosY + cellSize/2 + textSize/2, this.mValuePaint);
    }

    // Cage text
    if (!this.mCageText.equals("")) {
      canvas.drawText(this.mCageText, this.mPosX + 2, this.mPosY + 13, this.mCageTextPaint);
    }
    
    // Small 'possible' values.
    for (int i = 0 ; i < this.mPossibles.size() ; i++) {
    	canvas.drawText("" + this.mPossibles.get(i),
    			this.mPosX + 3 + (8 * i), this.mPosY + cellSize-5,
    			this.mPossiblesPaint);
    }
  }
  
}
