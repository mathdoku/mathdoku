package net.cactii.kenken;

import java.util.ArrayList;

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
  
  public GridCage (GridView context, int type, GridCell origin) {
    this.mContext = context;
    while (type == -1) {
      
      this.mCells = new ArrayList<GridCell>();
      this.mCells.add(origin);
      this.mType = (int)(Math.random() * 10);
      type = this.mType;
      switch (this.mType) {
        case CAGE_1 :
          /*
          if (originCell != (context.mGridSize * context.mGridSize) -1) {
            type = -1;
            continue;
          }
          */
            
          break;
        case CAGE_2V :
          if (origin.mRow > context.mGridSize-2) {
            type = -1;
            continue;
          }
          
          this.mCells.add(context.CellAtPos(origin.mColumn, origin.mRow+1));

          break;
        case CAGE_2H :
          if (origin.mColumn > context.mGridSize-2) {
            type = -1;
            continue;
          }
          this.mCells.add(context.CellAtPos(origin.mColumn+1, origin.mRow));
          break;
        case CAGE_3V :
          if (origin.mRow > context.mGridSize-3) {
            type = -1;
            continue;
          }
          this.mCells.add(context.CellAtPos(origin.mColumn, origin.mRow+1));
          this.mCells.add(context.CellAtPos(origin.mColumn, origin.mRow+2));
          break;
        case CAGE_3H :
          if (origin.mColumn > context.mGridSize - 3) {
            type = -1;
            continue;
          }
          this.mCells.add(context.CellAtPos(origin.mColumn+1, origin.mRow));
          this.mCells.add(context.CellAtPos(origin.mColumn+2, origin.mRow));
          break;
        case CAGE_3LL :
          if (origin.mColumn > context.mGridSize-2 ||
              origin.mRow > context.mGridSize-2) {
            type = -1;
            continue;
          }
          this.mCells.add(context.CellAtPos(origin.mColumn, origin.mRow+1));
          this.mCells.add(context.CellAtPos(origin.mColumn+1, origin.mRow+1));
          break;
        case CAGE_3LR :
          if (origin.mRow > context.mGridSize-2 ||
              origin.mColumn < 1) {
            type = -1;
            continue;
          }
          this.mCells.add(context.CellAtPos(origin.mColumn, origin.mRow+1));
          this.mCells.add(context.CellAtPos(origin.mColumn-1, origin.mRow+1));
          break;
        case CAGE_3UR :
          if (origin.mColumn > context.mGridSize-2 ||
              origin.mRow > context.mGridSize-2) {
            type = -1;
            continue;
          }
          this.mCells.add(context.CellAtPos(origin.mColumn+1, origin.mRow));
          this.mCells.add(context.CellAtPos(origin.mColumn+1, origin.mRow+1));

          break;
        case CAGE_3UL :
          if (origin.mColumn > context.mGridSize-2 ||
              origin.mRow > context.mGridSize-2) {
            type = -1;
            continue;
          }
          this.mCells.add(context.CellAtPos(origin.mColumn+1, origin.mRow));
          this.mCells.add(context.CellAtPos(origin.mColumn, origin.mRow+1));
          break;
        case CAGE_4SQ :
          if (origin.mColumn > context.mGridSize-2 ||
              origin.mRow > context.mGridSize-2) {
            type = -1;
            continue;
          }
          this.mCells.add(context.CellAtPos(origin.mColumn+1, origin.mRow));
          this.mCells.add(context.CellAtPos(origin.mColumn, origin.mRow+1));
          this.mCells.add(context.CellAtPos(origin.mColumn+1, origin.mRow+1));
        }
    }
  }
  public void setArithmetic() {
    this.mAction = -1;
    if (this.mType == CAGE_1) {
      this.mAction = ACTION_NONE;
      this.mResult = this.mCells.get(0).mValue;
      this.mCells.get(0).mCageText = "" + this.mResult;
      return;
    }
    double rand = Math.random();
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
    if (this.mAction > -1)
      return;

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
      this.mCells.get(0).mCageText = this.mResult + "\367";
    } else {
      this.mResult = higher - lower;
      this.mCells.get(0).mCageText = this.mResult + "-";
    }
  }
  public void setCageId(int id) {
    this.mId = id;
    for (GridCell cell : this.mCells)
      cell.mCageId = this.mId;
  }
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