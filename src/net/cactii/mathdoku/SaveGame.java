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
	public String filename;
	public static final String FIELD_DELIMITER_LEVEL1 = ":"; 
	public static final String FIELD_DELIMITER_LEVEL2 = ","; 

	public SaveGame() {
		this.filename = SaveGame.saveFilename;
	}

	public SaveGame(String filename) {
		this.filename = filename;
	}

	public boolean Save(GridView view) {
		synchronized (view.mLock) { // Avoid saving game at the same time as
									// creating puzzle
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(this.filename));
				long now = System.currentTimeMillis();
				writer.write(now + "\n");
				writer.write(view.mElapsed + "\n");
				writer.write(view.mGridSize + "\n");
				writer.write(view.mActive + "\n");
				for (GridCell cell : view.mCells) {
					cell.writeToFile(writer);
				}
				for (GridCage cage : view.mCages) {
					writer.write("CAGE:");
					writer.write(cage.mId + ":");
					writer.write(cage.mAction + ":");
					writer.write(cage.mResult + ":");
					writer.write(cage.mType + ":");
					for (GridCell cell : cage.mCells)
						writer.write(cell.getCellNumber() + ",");
					writer.write(":" + cage.isOperatorHidden());
					writer.write("\n");
				}
			} catch (IOException e) {
				Log.d("MathDoku", "Error saving game: " + e.getMessage());
				return false;
			} finally {
				try {
					if (writer != null)
						writer.close();
				} catch (IOException e) {
					// pass
					return false;
				}
			}
		} // End of synchronised block
		Log.d("MathDoku", "Saved game.");
		return true;
	}

	public long ReadDate() {
		BufferedReader br = null;
		InputStream ins = null;
		try {
			ins = new FileInputStream(new File(this.filename));
			br = new BufferedReader(new InputStreamReader(ins), 8192);
			return Long.parseLong(br.readLine());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ins.close();
				br.close();
			} catch (Exception e) {
				// Nothing.
				return 0;
			}
		}
		return 0;
	}

	public boolean Restore(GridView view) {
		String line = null;
		BufferedReader br = null;
		InputStream ins = null;
		String[] cageParts;
		try {
			ins = new FileInputStream(new File(this.filename));
			br = new BufferedReader(new InputStreamReader(ins), 8192);
			view.mDate = Long.parseLong(br.readLine());
			view.mElapsed = Long.parseLong(br.readLine());
			view.mGridSize = Integer.parseInt(br.readLine());
			if (br.readLine().equals("true"))
				view.mActive = true;
			else
				view.mActive = false;
			view.mSelectedCell = null;
			view.mCells = new ArrayList<GridCell>();
			while ((line = br.readLine()) != null) {
				GridCell cell = new GridCell(view, 0);
				if (!cell.restoreFromFile(line)) {
					break;
				}
				view.mCells.add(cell);
				if (cell.mSelected) {
					view.mSelectedCell = cell;
				}
			}
			if (line.startsWith("SELECTED:")) {
				// This information is now stored with the cell information. Do
				// not remove as long as we want to be able to read files in old
				// format.
				if (view.mSelectedCell == null) {
					int selected = Integer.parseInt(line.split(":")[1]);
					view.mSelectedCell = view.mCells.get(selected);
					view.mSelectedCell.mSelected = true;
				}
				line = br.readLine();
			}
			if (line.startsWith("INVALID:")) {
				// This information is now stored with the cell information. Do
				// not remove as long as we want to be able to read files in old
				// format.
				String invalidlist = line.split(":")[1];
				for (String cellId : invalidlist.split(",")) {
					int cellNum = Integer.parseInt(cellId);
					GridCell c = view.mCells.get(cellNum);
					c.setInvalidHighlight(true);
				}
				line = br.readLine();
			}
			view.mCages = new ArrayList<GridCage>();
			do {
				cageParts = line.split(":");
				GridCage cage;
				if (cageParts.length >= 7)
					cage = new GridCage(view, Integer.parseInt(cageParts[4]),
							Boolean.parseBoolean(cageParts[6]));
				else
					cage = new GridCage(view, Integer.parseInt(cageParts[4]),
							false);
				cage.mId = Integer.parseInt(cageParts[1]);
				cage.mAction = Integer.parseInt(cageParts[2]);
				cage.mResult = Integer.parseInt(cageParts[3]);
				for (String cellId : cageParts[5].split(",")) {
					int cellNum = Integer.parseInt(cellId);
					GridCell c = view.mCells.get(cellNum);
					c.setCageId(cage.mId);
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
		} catch (NumberFormatException e) {
			Log.d("Mathdoku", "Error restoring game: " + e.getMessage());
			return false;
		} catch (IndexOutOfBoundsException e) {
			Log.d("Mathdoku", "Error restoring game: " + e.getMessage());
			return false;
		} finally {
			try {
				ins.close();
				br.close();
				if (this.filename.equals(SaveGame.saveFilename))
					new File(filename).delete();
			} catch (Exception e) {
				// Nothing.
				return false;
			}
		}
		return true;
	}
}