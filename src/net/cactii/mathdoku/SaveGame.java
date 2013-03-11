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

	public static final String EOL_DELIMITER_LEVEL1 = "\n";
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
					cage.writeToFile(writer);
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
		// Empty cells and cages
		view.clear();
		if (view.mCells == null) {
			view.mCells = new ArrayList<GridCell>();
		}
		if (view.mCages == null) {
			view.mCages = new ArrayList<GridCage>();
		}

		String line = null;
		String[] lineParts;
		BufferedReader br = null;
		InputStream ins = null;
		try {
			// Open buffer to read from
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
			
			// Process cage information
			while ((line = br.readLine()) != null) {
				GridCage cage = new GridCage(view);
				if (!cage.restoreFromFile(line)) {
					// Line does not contain cage information
					break;
				}
				view.mCages.add(cage);
			}
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