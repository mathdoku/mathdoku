package net.cactii.mathdoku;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

public class SaveGame {
	public static final String saveFilename = "/data/data/net.cactii.mathdoku/savedgame";
	
	public boolean Save(GridView view) {
		BufferedWriter writer = null;
	    try {
	        writer = new BufferedWriter(new FileWriter(SaveGame.saveFilename));
	        writer.write(view.mGridSize + "\n");
	        writer.write(view.mActive + "\n");
	        for (GridCell cell : view.mCells) {
	        	writer.write("CELL:");
	        	writer.write(cell.mCellNumber + ":");
	        	writer.write(cell.mRow + ":");
	        	writer.write(cell.mColumn + ":");
	        	writer.write(cell.mCageText + ":");
	        	writer.write(cell.mValue + ":");
	        	writer.write(cell.mUserValue + ":");
	        	for (int possible : cell.mPossibles)
	        		writer.write(possible + ",");
	        	writer.write("\n");
	        }
	        if (view.mSelectedCell != null)
	        	writer.write("SELECTED:" + view.mSelectedCell.mCellNumber + "\n");
	        for (GridCage cage : view.mCages) {
	        	writer.write("CAGE:");
	        	writer.write(cage.mId + ":");
	        	writer.write(cage.mAction + ":");
	        	writer.write(cage.mResult + ":");
	        	writer.write(cage.mType + ":");
	        	for (GridCell cell : cage.mCells)
	        		writer.write(cell.mCellNumber + ",");
	        	writer.write("\n");
	        }
	    }
	    catch (IOException e) {
	        Log.d("MathDoku", "Error saving game: "+e.getMessage());
	        return false;
	    }
	    finally {
	        try {
	          if (writer != null)
	            writer.close();
	        } catch (IOException e) {
	          //pass
	        	return false;
	        }
	    }
	    Log.d("MathDoku", "Saved game.");
	    return true;
	}
	
	public boolean Restore(GridView view) {
	    String line = null;
	    BufferedReader br = null;
	    InputStream ins = null;
	    String[] cellParts;
	    String[] cageParts;
	    try {
	        ins = new FileInputStream(new File(SaveGame.saveFilename));
	        br = new BufferedReader(new InputStreamReader(ins), 8192);
	        view.mGridSize = Integer.parseInt(br.readLine());
	        if (br.readLine().equals("true"))
	        	view.mActive = true;
	        else
	        	view.mActive = false;
	        view.mCells = new ArrayList<GridCell>();
	        while ((line = br.readLine()) != null) {
	        	if (!line.startsWith("CELL:")) break;
	        	cellParts = line.split(":");
	        	int cellNum = Integer.parseInt(cellParts[1]);
	        	GridCell cell = new GridCell(view, cellNum);
	        	cell.mRow = Integer.parseInt(cellParts[2]);
	        	cell.mColumn = Integer.parseInt(cellParts[3]);
	        	cell.mCageText = cellParts[4];
	        	cell.mValue = Integer.parseInt(cellParts[5]);
	        	cell.mUserValue = Integer.parseInt(cellParts[6]);
	        	if (cellParts.length == 8)
		        	for (String possible : cellParts[7].split(","))
		        		cell.mPossibles.add(Integer.parseInt(possible));
	        	view.mCells.add(cell);
	        }
	        if (line.startsWith("SELECTED:")) {
	        	int selected = Integer.parseInt(line.split(":")[1]);
	        	view.mSelectedCell = view.mCells.get(selected);
	        	view.mSelectedCell.mSelected = true;
	        	line = br.readLine();
	        }
	        view.mCages = new ArrayList<GridCage>();
	        do {
	        	cageParts = line.split(":");
	        	GridCage cage = new GridCage(view);
	        	cage.mId = Integer.parseInt(cageParts[1]);
	        	cage.mAction = Integer.parseInt(cageParts[2]);
	        	cage.mResult = Integer.parseInt(cageParts[3]);
	        	cage.mType = Integer.parseInt(cageParts[4]);
	        	cage.mCells = new ArrayList<GridCell>();
	        	for (String cellId : cageParts[5].split(",")) {
	        		int cellNum = Integer.parseInt(cellId);
	        		GridCell c = view.mCells.get(cellNum);
	        		c.mCageId = cage.mId;
	        		cage.mCells.add(c);
	        	}
	        	view.mCages.add(cage);
	        } while ((line = br.readLine()) != null);
	        
	    } catch (FileNotFoundException e) {
	        Log.d("Mathdoku", "FNF Error restoring game: " + e.getMessage());
	        return false;
		} catch (IOException e) {
		  Log.d("Mathdoku", "IO Error restoring game: " + e.getMessage());
		  return false;
		}
		finally {
		  try {
		    ins.close();
		    br.close();
		    new File(saveFilename).delete();
		  } catch (Exception e) {
		    // Nothing.
			  return false;
		  }
		}
		return true;
	}
}