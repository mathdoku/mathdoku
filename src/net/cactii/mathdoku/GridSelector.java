package net.cactii.mathdoku;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;

/*
 * Handles the selector where the player chooses the number to enter into the
 * selected cell.
 */
public class GridSelector {
	
	private GridView mContext;
	
	private int mGridWidth;
	private int mOrigin;
	private int mGridRows;
	private int mGridCols;
	private int mCellWidth;
	private int mCellHeight;
	private Paint mDigitPaint;
	private Paint mTextPaint;
	private Paint mGridPaint;
	private Paint mBackgroundPaint;
	private Paint mMaybePaint;
	public boolean mVisible;
	public boolean mMaybe;
	
	public GridSelector(GridView context) {
		this.mContext = context;
		this.mVisible = false;
		this.mMaybe = false;
	}
	
	public void createGeometry() {
		mGridWidth = this.mContext.getWidth()*3/4;
		mOrigin = this.mContext.getWidth()/2 - this.mGridWidth/2;
		if (this.mContext.mGridSize == 4) {
			this.mGridCols = 2;
			this.mGridRows = 3;
		}
		if (this.mContext.mGridSize == 5) {
			this.mGridCols = 3;
			this.mGridRows = 3;
		}
		if (this.mContext.mGridSize == 6) {
			this.mGridCols = 3;
			this.mGridRows = 3;
		}
		if (this.mContext.mGridSize == 7) {
			this.mGridCols = 3;
			this.mGridRows = 4;
		}
		this.mCellWidth = this.mGridWidth / this.mGridCols;
		this.mCellHeight = this.mGridWidth / this.mGridRows;
		
	    this.mDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    this.mDigitPaint.setColor(0xFF000000);
	    this.mDigitPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
	    this.mDigitPaint.setTextSize(32);
	    
	    this.mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    this.mTextPaint.setColor(0xFF000000);
	    this.mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
	    this.mTextPaint.setTextSize(16);
	    
	    this.mMaybePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    this.mMaybePaint.setColor(0x7F000000);
	    this.mMaybePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
	    this.mMaybePaint.setTextSize(16);
	    
	    this.mGridPaint = new Paint();
	    this.mGridPaint.setColor(0xFF000000);
	    this.mGridPaint.setStrokeWidth(1);
	    	    
	    this.mBackgroundPaint = new Paint();
	    this.mBackgroundPaint.setColor(0xC0FFFFFF);
	    this.mBackgroundPaint.setStrokeWidth(0);
	    
	}

	public void drawDigitSelectGrid(Canvas canvas) {
		
		canvas.drawARGB(0x7f, 0, 0, 0);
		canvas.drawRect(this.mOrigin, this.mOrigin,
						this.mOrigin + this.mGridWidth, this.mOrigin + this.mGridWidth,
						this.mBackgroundPaint);
		for (int row = 0 ; row <= this.mGridRows ; row++) {
			canvas.drawLine(this.mOrigin, this.mOrigin + this.mCellHeight * row,
							this.mOrigin + this.mGridWidth, this.mOrigin + this.mCellHeight * row,
							this.mGridPaint);
			for (int col = 0 ; col <= this.mGridCols ; col++) {
				canvas.drawLine(this.mOrigin + this.mCellWidth * col, this.mOrigin,
						this.mOrigin + this.mCellWidth * col,
						this.mOrigin + this.mGridWidth,
						this.mGridPaint);
				int digit = this.mGridCols * row + col +1;
				if (digit <= this.mContext.mGridSize && row < this.mGridRows && col < this.mGridCols)
					canvas.drawText("" + digit, this.mOrigin + this.mCellWidth * col + this.mCellWidth/3,
							this.mOrigin + this.mCellHeight * row + this.mCellHeight*2/3, this.mDigitPaint);
				if (row == this.mGridRows-1 && col == this.mGridCols-1)
					canvas.drawText("Clear", this.mOrigin + this.mCellWidth * col + this.mCellWidth/5,
							this.mOrigin + this.mCellHeight * row + this.mCellHeight*2/3, this.mTextPaint);
				if (row == this.mGridRows-1 && col == 0) {
					if (this.mMaybe)
						this.mMaybePaint.setColor(0xFF000000);
					else
						this.mMaybePaint.setColor(0x7F000000);
					canvas.drawText("Maybe", this.mOrigin + this.mCellWidth * col + this.mCellWidth/5,
							this.mOrigin + this.mCellHeight * row + this.mCellHeight*2/3, this.mMaybePaint);
				}

			}
		}
	}
	
	private int digitAtPos(int row, int col) {
		int digit = row * (this.mGridCols) + col+1;
		if (digit > this.mContext.mGridSize)
			digit = 0;
		return digit;
	}
	
	public boolean onTouch(MotionEvent event) {
		float x = event.getX()-this.mOrigin;
		float y = event.getY() - this.mOrigin;
		
	    int row = (int)((this.mGridWidth - (this.mGridWidth-y))/(this.mGridWidth/this.mGridRows));
	    row++;
	    // Log.d("KenKen", "Touched row " + row + " y " + y);


	    int col = (int)((this.mGridWidth - (this.mGridWidth-x))/(this.mGridWidth/this.mGridCols));
	    col++;
	    // Log.d("KenKen", "Touched col " + col + " x " + x);

	    if (row > this.mGridRows || row < 1 || col > this.mGridCols || col < 1) {
	    	this.mVisible = false;
	    	this.mContext.invalidate();
	    	return true;
	    }
	    
	    if (row == this.mGridRows && col == this.mGridCols) {
	    	this.mContext.mSelectedCell.setUserValue(0);
	    } else if (row == this.mGridRows && col == 1) {
	    	this.mMaybe = !this.mMaybe;
	    	this.mContext.invalidate();
	    	return true;
	    } else {
		    int digit = this.digitAtPos(row-1, col-1);
		    if (digit > 0) {
		    	if (this.mMaybe) {
		    		this.mContext.mSelectedCell.togglePossible(digit);
		    	} else
		    		this.mContext.mSelectedCell.setUserValue(digit);
		    }
	    }
	    this.mVisible = false;
	    this.mContext.invalidate();
	    return true;
	}
}
