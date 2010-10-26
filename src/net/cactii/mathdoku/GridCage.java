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
  public static final int CAGE_4UL = 10;
  public static final int CAGE_4UR = 11;
  public static final int CAGE_4LL = 12;
  public static final int CAGE_4LR = 13;



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
  // User math is correct
  public boolean mUserMathCorrect;
  // Cage (or a cell within) is selected
  public boolean mSelected;
  
  public GridCage (GridView context) {
    this.mContext = context;
  }
  
  public String toString() {
	  String retStr = "";
	  retStr += "Cage id: " + this.mId + ", Type: ";
	  switch (this.mType)
	  {
	  case CAGE_UNDEF: retStr += "UNDEF"; break;
	  case CAGE_1: retStr += "1"; break;
	  case CAGE_2V: retStr += "2V"; break;
	  case CAGE_2H: retStr += "2H"; break;
	  case CAGE_3V: retStr += "3V"; break;
	  case CAGE_3H: retStr += "3H"; break;
	  case CAGE_3LL: retStr += "3LL"; break;
	  case CAGE_3LR: retStr += "3LR"; break;
	  case CAGE_3UL: retStr += "3UL"; break;
	  case CAGE_3UR: retStr += "3UR"; break;
	  case CAGE_4SQ: retStr += "4SQ"; break;
	  case CAGE_4UL: retStr += "4UL"; break;
	  case CAGE_4UR: retStr += "4UR"; break;
	  case CAGE_4LL: retStr += "4LL"; break;
	  case CAGE_4LR: retStr += "4LR"; break;
	  }
	  retStr += ", Action: ";
	  switch (this.mAction)
	  {
	  case ACTION_NONE:
		  retStr += "None"; break;
	  case ACTION_ADD:
		  retStr += "Add"; break;
	  case ACTION_SUBTRACT:
		  retStr += "Subtract"; break;
	  case ACTION_MULTIPLY:
		  retStr += "Multiply"; break;
	  case ACTION_DIVIDE:
		  retStr += "Divide"; break;
	  }
	  retStr += ", Result: " + this.mResult;
	  retStr += ", cells: ";
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
    this.mUserMathCorrect = true;
    this.mSelected = false;
    
    while (type == CAGE_UNDEF && attempts.size() < 13) {
      
      this.mCells.clear();
      this.mCells.add(origin);
      
      do {
    	  this.mType = this.mContext.mRandom.nextInt(13) + 1;
      } while (attempts.contains(this.mType));

      attempts.add(this.mType);
      type = this.mType;
      switch (this.mType) {
        case CAGE_1 :

          type = -1;
          if (attempts.size() < 14)
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
              origin.mRow > this.mContext.mGridSize-2 ||
              this.mContext.mGridSize > 7) {
            type = -1;
            this.mType = CAGE_UNDEF;
            continue;
          }
          this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+1));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
          this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn+1));
          break;
        case CAGE_4UL :
            if (origin.mColumn > this.mContext.mGridSize-2 ||
                origin.mRow > this.mContext.mGridSize-3 ||
                this.mContext.mGridSize > 7) {
              type = -1;
              this.mType = CAGE_UNDEF;
              continue;
            }
            this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+1));
            this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
            this.mCells.add(this.mContext.getCellAt(origin.mRow+2, origin.mColumn));
            break;
        case CAGE_4UR :
            if (origin.mColumn > this.mContext.mGridSize-2 ||
                origin.mRow > this.mContext.mGridSize-3 ||
                this.mContext.mGridSize > 7) {
              type = -1;
              this.mType = CAGE_UNDEF;
              continue;
            }
            this.mCells.add(this.mContext.getCellAt(origin.mRow, origin.mColumn+1));
            this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn+1));
            this.mCells.add(this.mContext.getCellAt(origin.mRow+2, origin.mColumn+1));
            break;
        case CAGE_4LL :
            if (origin.mColumn > this.mContext.mGridSize-2 ||
                origin.mRow > this.mContext.mGridSize-3 ||
                this.mContext.mGridSize > 7) {
              type = -1;
              this.mType = CAGE_UNDEF;
              continue;
            }
            this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
            this.mCells.add(this.mContext.getCellAt(origin.mRow+2, origin.mColumn));
            this.mCells.add(this.mContext.getCellAt(origin.mRow+2, origin.mColumn+1));
            break;
        case CAGE_4LR :
            if (origin.mColumn < 1 ||
                origin.mRow > this.mContext.mGridSize-3 ||
                this.mContext.mGridSize > 7) {
              type = -1;
              this.mType = CAGE_UNDEF;
              continue;
            }
            this.mCells.add(this.mContext.getCellAt(origin.mRow+1, origin.mColumn));
            this.mCells.add(this.mContext.getCellAt(origin.mRow+2, origin.mColumn));
            this.mCells.add(this.mContext.getCellAt(origin.mRow+2, origin.mColumn-1));
            break;
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
  
  // Determine whether user entered values match the arithmetic.
  //
  // Only marks cells bad if all cells have a uservalue, and they dont
  // match the arithmetic hint.
  public void userValuesCorrect() {
    this.mUserMathCorrect = true;
    for (GridCell cell : this.mCells)
      if (cell.mUserValue < 1) {
        this.setBorders();
        return;
      }
    
    this.mUserMathCorrect = this.isSolved();
    this.setBorders();
  }
  
  /*
   * Sets the borders of the cage's cells.
   */
  public void setBorders() {
    for (GridCell cell : this.mCells) {
        for(int x = 0 ; x < 4 ; x++) {
      	  cell.mBorderTypes[x] = 0;
        }
      if (this.mContext.CageIdAt(cell.mRow-1, cell.mColumn) != this.mId)
        if (!this.mUserMathCorrect && this.mContext.mBadMaths)
          cell.mBorderTypes[0] = GridCell.BORDER_WARN;
        else if (this.mSelected)
        	cell.mBorderTypes[0] = GridCell.BORDER_CAGE_SELECTED;
        else
        	cell.mBorderTypes[0] = GridCell.BORDER_SOLID;
      
      if (this.mContext.CageIdAt(cell.mRow, cell.mColumn+1) != this.mId)
    	  if(!this.mUserMathCorrect && this.mContext.mBadMaths)
    		  cell.mBorderTypes[1] = GridCell.BORDER_WARN;
          else if (this.mSelected)
          	cell.mBorderTypes[1] = GridCell.BORDER_CAGE_SELECTED;
    	  else
    		  cell.mBorderTypes[1] = GridCell.BORDER_SOLID;
      
      if (this.mContext.CageIdAt(cell.mRow+1, cell.mColumn) != this.mId)
        if(!this.mUserMathCorrect && this.mContext.mBadMaths)
          cell.mBorderTypes[2] = GridCell.BORDER_WARN;
        else if (this.mSelected)
        	cell.mBorderTypes[2] = GridCell.BORDER_CAGE_SELECTED;
        else
        	cell.mBorderTypes[2] = GridCell.BORDER_SOLID;
      
      if (this.mContext.CageIdAt(cell.mRow, cell.mColumn-1) != this.mId)
        if(!this.mUserMathCorrect && this.mContext.mBadMaths)
          cell.mBorderTypes[3] = GridCell.BORDER_WARN;
        else if (this.mSelected)
        	cell.mBorderTypes[3] = GridCell.BORDER_CAGE_SELECTED;
        else
        	cell.mBorderTypes[3] = GridCell.BORDER_SOLID;
    }
  }

/*
 * Generates all combinations of numbers which satisfy the cage's arithmetic
 * and MathDoku constraints i.e. a digit can only appear once in a column/row 
 */
public ArrayList<int[]> GetPossibleNums()
{
	ArrayList<int[]> AllResults = new ArrayList<int[]>();

	switch (this.mAction) {
	case ACTION_NONE:
		assert (mCells.size() == 1);
		int number[] = {mResult};
		AllResults.add(number);
		break;
	  case ACTION_SUBTRACT:
		  assert(mCells.size() == 2);
		  for (int i1=1; i1<=this.mContext.mGridSize; i1++)
			  for (int i2=1; i2<=this.mContext.mGridSize; i2++)
				  if (i2 - i1 == mResult || i1 - i2 == mResult) {
					  int numbers[] = {i1, i2};
					  AllResults.add(numbers);
				  }
		  break;
	  case ACTION_DIVIDE:
		  assert(mCells.size() == 2);
		  for (int i1=1; i1<=this.mContext.mGridSize; i1++)
			  for (int i2=1; i2<=this.mContext.mGridSize; i2++)
				  if (mResult*i1 == i2 || mResult*i2 == i1) {
					  int numbers[] = {i1, i2};
					  AllResults.add(numbers);
				  }
		  break;
	  case ACTION_ADD:
		  AllResults = getalladdcombos(this.mContext.mGridSize,mResult,mCells.size());
		  break;
	  case ACTION_MULTIPLY:
		  AllResults = getallmultcombos(this.mContext.mGridSize,mResult,mCells.size());
		  break;
	}
	return AllResults;
}

// The following two variables are required by the recursive methods below.
// They could be passed as parameters of the recursive methods, but this
// reduces performance.
private int[] numbers;
private ArrayList<int[]> result_set;

private ArrayList<int[]> getalladdcombos (int max_val, int target_sum, int n_cells)
{
	numbers = new int[n_cells];
	result_set = new ArrayList<int[]> ();
	getaddcombos(max_val, target_sum, n_cells);
	return result_set;
}

/*
 * Recursive method to calculate all combinations of digits which add up to target
 * 
 * @param max_val		maximum permitted value of digit (= dimension of grid)
 * @param target_sum	the value which all the digits should add up to
 * @param n_cells		number of digits still to select
 */
private void getaddcombos(int max_val, int target_sum, int n_cells)
{
	for (int n=1; n<= max_val; n++)
	{
		if (n_cells == 1)
		{
			if (n == target_sum) {
				numbers[0] = n;
				if (satisfiesConstraints(numbers))
					result_set.add(numbers.clone());
			}
		}
		else {
			numbers[n_cells-1] = n;
			getaddcombos(max_val, target_sum-n, n_cells-1);
		}
	}
	return;
}

private ArrayList<int[]> getallmultcombos (int max_val, int target_sum, int n_cells)
{
	numbers = new int[n_cells];
	result_set = new ArrayList<int[]> ();
	getmultcombos(max_val, target_sum, n_cells);
	
	return result_set;
}

/*
 * Recursive method to calculate all combinations of digits which multiply up to target
 * 
 * @param max_val		maximum permitted value of digit (= dimension of grid)
 * @param target_sum	the value which all the digits should multiply up to
 * @param n_cells		number of digits still to select
 */
private void getmultcombos(int max_val, int target_sum, int n_cells)
{
	for (int n=1; n<= max_val; n++)
	{
		if (target_sum % n != 0)
			continue;
		
		if (n_cells == 1)
		{
			if (n == target_sum) {
				numbers[0] = n;
				if (satisfiesConstraints(numbers))
					result_set.add(numbers.clone());
			}
		}
		else {
			numbers[n_cells-1] = n;
			getmultcombos(max_val, target_sum/n, n_cells-1);
		}
	}
	return;
}

/*
 * Check whether the set of numbers satisfies all constraints
 * Looking for cases where a digit appears more than once in a column/row
 * Constraints:
 * 0 -> (mGridSize * mGridSize)-1 = column constraints
 * (each column must contain each digit) 
 * mGridSize * mGridSize -> 2*(mGridSize * mGridSize)-1 = row constraints
 * (each row must contain each digit) 
 */
private boolean satisfiesConstraints(int[] test_nums) {
	
	boolean constraints[] = new boolean[mContext.mGridSize*mContext.mGridSize*2];
	int constraint_num;
	for (int i = 0; i<this.mCells.size(); i++) {
		constraint_num = mContext.mGridSize*(test_nums[i]-1) + mCells.get(i).mColumn;
		if (constraints[constraint_num])
			return false;
		else
			constraints[constraint_num]= true;
		constraint_num = mContext.mGridSize*mContext.mGridSize + mContext.mGridSize*(test_nums[i]-1) + mCells.get(i).mRow;
		if (constraints[constraint_num])
			return false;
		else
			constraints[constraint_num]= true;
	}
	return true;
}

}