package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.Random;


import android.util.Log;

public class GridCage {

  public static final int ACTION_NONE = 0;
  public static final int ACTION_ADD = 1;
  public static final int ACTION_SUBTRACT = 2;
  public static final int ACTION_MULTIPLY = 3;
  public static final int ACTION_DIVIDE = 4;
  
  public static final int CAGE_UNDEF = -1;
  public static final int CAGE_1 = 0;
  public static final int CAGE_2V = 1;
  public static final int CAGE_2H = 2;
  public static final int CAGE_3V = 3;
  public static final int CAGE_3H = 4;
  public static final int CAGE_3LL = 5;
  public static final int CAGE_3LR = 6;
  public static final int CAGE_3UL = 7;
  public static final int CAGE_3UR = 8;
  public static final int CAGE_4SQ = 9;


  // Action for the cage
  public int mAction;
  // Number the action results in
  public int mResult;
  // List of cage's cells
  // public int[] mCells;
  public ArrayList<GridCell> mCells;
  // Type of the cage
  public int mType;
  // Id of the cage
  public int mId;
  // Enclosing context
  public GridView mContext;
  
  public GridCage (GridView context) {
    this.mContext = context;
  }
  
  public String toString() {
	  String retStr = "";
	  retStr += "Cage id: " + this.mId + ", Type " + this.mType + ", cells: ";
	  for (GridCell cell : this.mCells)
		  retStr += cell.mCellNumber + ", ";
	  return retStr;
  }
  
  /*
   * Create a random cage that starts at 'origin' and
   * fits the grid in our context.
   */
  public void createCage(int type, GridCell origin) {

    ArrayList<Integer> attempts = new ArrayList<Integer>();
    this.mCells = new ArrayList<GridCell>();
    this.mCells.add(origin);
    this.mType = type;
    
    while (type == CAGE_UNDEF && attempts.size() < 9) {
      
      this.mCells.clear();
      this.mCells.add(origin);
      
      do {
    	  this.mType = this.mContext.mRandom.nextInt(9) + 1;
      } while (attempts.contains(this.mType));

      attempts.add(this.mType);
      type = this.mType;
      switch (this.mType) {
        case CAGE_1 :

          type = -1;
          if (attempts.size() < 10)
        	  continue;
          /*
          if (origin.mCellNumber < (this.mContext.mGridSize * this.mContext.mGridSize) -1) {
            type = -1;
            continue;
          }
          */
            
          break;
        case CAGE_2V :
          if (origin.mRow > this.mContext.mGridSize-2) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));

          break;
        case CAGE_2H :
          if (origin.mColumn > this.mContext.mGridSize-2) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+1));
          break;
        case CAGE_3V :
          if (origin.mRow > this.mContext.mGridSize-3) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+2, origin.mColumn));
          break;
        case CAGE_3H :
          if (origin.mColumn > this.mContext.mGridSize - 3) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+1));
          this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+2));
          break;
        case CAGE_3LL :
          if (origin.mColumn > this.mContext.mGridSize-2 ||
              origin.mRow > this.mContext.mGridSize-2) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn+1));
          break;
        case CAGE_3LR :
          if (origin.mRow > this.mContext.mGridSize-2 ||
              origin.mColumn < 1) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn-1));
          break;
        case CAGE_3UR :
          if (origin.mColumn > this.mContext.mGridSize-2 ||
              origin.mRow > this.mContext.mGridSize-2) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+1));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn+1));

          break;
        case CAGE_3UL :
          if (origin.mColumn > this.mContext.mGridSize-2 ||
              origin.mRow > this.mContext.mGridSize-2) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+1));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
          break;
        case CAGE_4SQ :
          if (origin.mColumn > this.mContext.mGridSize-2 ||
              origin.mRow > this.mContext.mGridSize-2) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+1));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn+1));
        }
    }
  }
  
  /*
   * Generates the arithmetic for the cage, semi-randomly.
   * 
   * - If a cage has 3 or more cells, it can only be an add or multiply.
   * - else if the cells are evenly divisible, division is used, else
   *   subtraction.
   */
  public void setArithmetic() {
    this.mAction = -1;
    if (this.mType == CAGE_1) {
      this.mAction = ACTION_NONE;
      this.mResult = this.mCells.get(0).mValue;
      this.mCells.get(0).mCageText = "" + this.mResult;
      return;
    }
    double rand = this.mContext.mRandom.nextDouble();
    double addChance = 0.25;
    double multChance = 0.5;
    if (this.mCells.size() > 2) {
      addChance = 0.5;
      multChance = 1.0;
    }
    if (rand <= addChance)
      this.mAction = ACTION_ADD;
    else if (rand <= multChance)
      this.mAction = ACTION_MULTIPLY;
    
    if (this.mAction == ACTION_ADD) {
      int total = 0;
      for (GridCell cell : this.mCells) {
        total += cell.mValue;
      }
      this.mResult = total;
      this.mCells.get(0).mCageText = this.mResult + "+";
    }
    if (this.mAction == ACTION_MULTIPLY) {
      int total = 1;
      for (GridCell cell : this.mCells) {
        total *= cell.mValue;
      }
      this.mResult = total;
      this.mCells.get(0).mCageText = this.mResult + "x";
    }
    if (this.mAction > -1) {
      return;
    }

    if (this.mCells.size() < 2) {
    	Log.d("KenKen", "Why only length 1? Type: " + this);
    }
    int cell1Value = this.mCells.get(0).mValue;
    int cell2Value = this.mCells.get(1).mValue;
    int higher = cell1Value;
    int lower = cell2Value;
    boolean canDivide = false;
    if (cell1Value < cell2Value) {
      higher = cell2Value;
      lower = cell1Value;
    }
    if (higher % lower == 0)
      canDivide = true;
    if (canDivide) {
      this.mResult = higher / lower;
      this.mAction = ACTION_DIVIDE;
      // this.mCells.get(0).mCageText = this.mResult + "\367";
      this.mCells.get(0).mCageText = this.mResult + "/";
    } else {
      this.mResult = higher - lower;
      this.mAction = ACTION_SUBTRACT;
      this.mCells.get(0).mCageText = this.mResult + "-";
    }
  }
  
  /*
   * Sets the cageId of the cage's cells.
   */
  public void setCageId(int id) {
    this.mId = id;
    for (GridCell cell : this.mCells)
      cell.mCageId = this.mId;
  }
  
  // Returns whether the cage is solved (with mUserValue)
  public boolean isSolved() {
	  if (this.mCells.size() == 1)
		  return (this.mCells.get(0).mValue == this.mCells.get(0).mUserValue);
	  
	  switch (this.mAction) {
		  case ACTION_ADD :
			  int total = 0;
			  for (GridCell cell : this.mCells) {
				  total += cell.mUserValue;
			  }
			  return (total == this.mResult);
		  case ACTION_MULTIPLY :
			  int mtotal = 1;
			  for (GridCell cell : this.mCells) {
				  mtotal *= cell.mUserValue;
			  }
			  return (mtotal == this.mResult);
		  case ACTION_DIVIDE :
			  if (this.mCells.get(0).mUserValue > this.mCells.get(1).mUserValue)
				  return ((this.mCells.get(0).mUserValue / this.mCells.get(1).mUserValue) == this.mResult);
			  else
				  return ((this.mCells.get(1).mUserValue / this.mCells.get(0).mUserValue) == this.mResult);
		  case ACTION_SUBTRACT :
			  if (this.mCells.get(0).mUserValue > this.mCells.get(1).mUserValue)
				  return ((this.mCells.get(0).mUserValue - this.mCells.get(1).mUserValue) == this.mResult);
			  else
				  return ((this.mCells.get(1).mUserValue - this.mCells.get(0).mUserValue) == this.mResult);
	  }
	  throw new RuntimeException("isSolved() got to an unreachable point " + this.mAction + ": " + this.toString());
  }
  
  /*
   * Sets the borders of the cage's cells.
   */
  public void setBorders() {
    for (GridCell cell : this.mCells) {
      //if (this.mContext.CageIdAt(cell.mRow-1, cell.mColumn) != cell.mCageId)
      //  cell.mBorderTypes[0] = GridCell.BORDER_SOLID;
      if (this.mContext.CageIdAt(cell.mRow, cell.mColumn+1) != cell.mCageId)
        cell.mBorderTypes[1] = GridCell.BORDER_SOLID;
      if (this.mContext.CageIdAt(cell.mRow+1, cell.mColumn) != cell.mCageId)
        cell.mBorderTypes[2] = GridCell.BORDER_SOLID;
      //if (this.mContext.CageIdAt(cell.mRow, cell.mColumn-1) != cell.mCageId)
      //  cell.mBorderTypes[3] = GridCell.BORDER_SOLID;
    }
  }

}