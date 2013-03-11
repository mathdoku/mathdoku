package net.cactii.mathdoku;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import net.cactii.mathdoku.DevelopmentHelper.Mode;

import android.util.Log;

public class GameFile extends File {
	private static final String TAG = "MathDoku.GameFile";

	public static final String PATH = "/data/data/net.cactii.mathdoku/";
	public static final String DEFAULT_FILENAME = "savedgame";
	public static final String PREFIX_FILENAME = DEFAULT_FILENAME + "_";

	// Remove "&& false" in following line to show debug information about
	// saving and restoring files when running in development mode.
	public static final boolean DEBUG_SAVE_RESTORE = (DevelopmentHelper.mode == Mode.DEVELOPMENT) && true;

	// Name of file to be saved or restored
	public String filename;

	// Delimiters used in files to separate objects, fields and field with
	// multiple values.
	public static final String EOL_DELIMITER = "\n"; // Separate objects
	public static final String FIELD_DELIMITER_LEVEL1 = ":"; // Separate fields
	public static final String FIELD_DELIMITER_LEVEL2 = ","; // Separate values
																// in fields

	/**
	 * Creates a new instance of {@link GameFile} for the default game file.
	 */
	public GameFile() {
		super(PATH + DEFAULT_FILENAME);
		this.filename = PATH + DEFAULT_FILENAME;

	}

	/**
	 * Creates a new instance of {@link GameFile} for the given game file.
	 * 
	 * @param filename
	 *            Name of file to be processed.
	 */
	public GameFile(String filename) {
		super(PATH + filename);
		this.filename = PATH + filename;
	}

	/**
	 * Creates a new instance of {@link GameFile} for the given sequence number.
	 * 
	 * @param index
	 *            Sequence number of the game file.
	 */
	public GameFile(int index) {
		super(PATH + PREFIX_FILENAME + index);
		this.filename = PATH + PREFIX_FILENAME + index;
	}

	/**
	 * Save the given grid view to a game file.
	 * 
	 * @param view
	 *            The grid view to be saved.
	 * @return True in case the grid view was saved to a file. False otherwise.
	 */
	public boolean save(GridView view) {
		synchronized (view.mLock) { // Avoid saving game at the same time as
									// creating puzzle
			BufferedWriter writer = null;
			try {
				// Open file
				writer = new BufferedWriter(new FileWriter(this.filename));

				// Store current time
				writer.write(System.currentTimeMillis() + EOL_DELIMITER);

				// Store information about the Grid View on a single line
				writer.write(view.toStorageString() + EOL_DELIMITER);

				// Store information about the cells. Use one line per single
				// cell.
				for (GridCell cell : view.mCells) {
					writer.write(cell.toStorageString() + EOL_DELIMITER);
				}

				// Store information about the cages. Use one line per single
				// cage.
				for (GridCage cage : view.mCages) {
					writer.write(cage.toStorageString() + EOL_DELIMITER);
				}
			} catch (IOException e) {
				Log.e(TAG, "Error saving game: " + e.getMessage());
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

		if (DEBUG_SAVE_RESTORE) {
			Log.d(TAG, "Saved game.");
		}
		return true;
	}

	/**
	 * Retrieves the date/time at which the game file was created.
	 * 
	 * @return The date/time on which the game file was created.
	 */
	public long readDatetimeCreated() {
		BufferedReader br = null;
		InputStream ins = null;
		String line = null;
		try {
			// Open buffer to read from
			ins = new FileInputStream(this);
			br = new BufferedReader(new InputStreamReader(ins), 8192);

			// Read first line
			if ((line = br.readLine()) == null) {
				throw new InvalidFileFormatException("Unexpected end of file");
			}

			// Read date/time created. Because there is no need to load the
			// remainder of the game file we can exit directly.
			return Long.parseLong(line);
		} catch (InvalidFileFormatException e) {
			Log.d(TAG, "Invalid file format error when restoring game "
					+ this.filename + "\n" + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found error when restoring game "
					+ this.filename + "\n" + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "IO error when restoring game " + this.filename + "\n"
					+ e.getMessage());
		} catch (NumberFormatException e) {
			Log.d(TAG, "Number format error when restoring game "
					+ this.filename + "\n" + e.getMessage());
		} finally {
			try {
				ins.close();
				br.close();
			} catch (Exception e) {
				// Nothing.
			}
		}
		return 0;
	}

	/**
	 * Load the game file into the given grid view.
	 * 
	 * @param view
	 *            The grid view which has to be filled with information loaded
	 *            from the game file.
	 * @return True in case the game file has been loaded successful in the grid
	 *         view. False otherwise.
	 */
	public boolean load(GridView view) {
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
			ins = new FileInputStream(this);
			br = new BufferedReader(new InputStreamReader(ins), 8192);

			// Read first line
			if ((line = br.readLine()) == null) {
				throw new InvalidFileFormatException("Unexpected end of file");
			}

			// Read date/time created
			view.mDate = Long.parseLong(line);
			if ((line = br.readLine()) == null) {
				throw new InvalidFileFormatException("Unexpected end of file");
			}

			// Read view information
			if (!view.fromStorageString(line)) {
				// The initial version of the saved games stored view
				// information on 3 different lines. Rewrite to valid view
				// storage information (version 1).
				// Do not remove as long as backward compatibility with old save
				// file should be remained.
				line = GridView.SAVE_GAME_GRID_VERSION_01
						+ FIELD_DELIMITER_LEVEL1 + line
						+ FIELD_DELIMITER_LEVEL1 + br.readLine()
						+ FIELD_DELIMITER_LEVEL1 + br.readLine();

				// Retry to process this line.
				if (!view.fromStorageString(line)) {
					throw new InvalidFileFormatException(
							"View information can not be processed: " + line);
				}
			}
			if ((line = br.readLine()) == null) {
				throw new InvalidFileFormatException("Unexpected end of file");
			}

			// Read cell information
			int countCellsToRead = view.mGridSize * view.mGridSize;
			while (countCellsToRead > 0) {
				GridCell cell = new GridCell(view, 0);
				if (!cell.fromStorageString(line)) {
					throw new InvalidFileFormatException(
							"Line does not contain cell information while this was expected:"
									+ line);
				}
				view.mCells.add(cell);
				if (cell.mSelected) {
					view.mSelectedCell = cell;
				}
				countCellsToRead--;

				// Read next line
				if ((line = br.readLine()) == null) {
					throw new InvalidFileFormatException(
							"Unexpected end of file");
				}
			}

			if (line.startsWith("SELECTED:")) {
				// Do not remove as long as backward compatibility with old save
				// file should be remained. In new save files this information
				// is stored as part of the cell information.
				if (view.mSelectedCell == null) {
					int selected = Integer.parseInt(line
							.split(FIELD_DELIMITER_LEVEL1)[1]);
					view.mSelectedCell = view.mCells.get(selected);
					view.mSelectedCell.mSelected = true;
				}

				// Read next line
				if ((line = br.readLine()) == null) {
					throw new InvalidFileFormatException(
							"Unexpected end of file");
				}
			}
			if (line.startsWith("INVALID:")) {
				// Do not remove as long as backward compatibility with old save
				// file should be remained. In new save files this information
				// is stored as part of the cell information.
				String invalidlist = line.split(FIELD_DELIMITER_LEVEL1)[1];
				for (String cellId : invalidlist.split(FIELD_DELIMITER_LEVEL2)) {
					int cellNum = Integer.parseInt(cellId);
					GridCell c = view.mCells.get(cellNum);
					c.setInvalidHighlight(true);
				}

				// Read next line
				if ((line = br.readLine()) == null) {
					throw new InvalidFileFormatException(
							"Unexpected end of file");
				}
			}

			// Remaining lines contain cage information
			while (line != null) {
				GridCage cage = new GridCage(view);
				if (!cage.fromStorageString(line)) {
					throw new InvalidFileFormatException(
							"Line does not contain cage  information while this was expected:"
									+ line);
				}
				view.mCages.add(cage);

				// Read next line. No checking of unexpected end of file needed
				// here because the last line of the file does contain a cage.
				line = br.readLine();
			}
		} catch (InvalidFileFormatException e) {
			Log.d(TAG, "Invalid file format error when restoring game "
					+ this.filename + "\n" + e.getMessage());
			return false;
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found error when restoring game "
					+ this.filename + "\n" + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IO error when restoring game " + this.filename + "\n"
					+ e.getMessage());
			return false;
		} catch (NumberFormatException e) {
			Log.d(TAG, "Number format error when restoring game "
					+ this.filename + "\n" + e.getMessage());
			return false;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAG, "Index out of bound error when restoring game "
					+ this.filename + "\n" + e.getMessage());
			return false;
		} finally {
			try {
				ins.close();
				br.close();
				if (this.filename.equals(PATH + DEFAULT_FILENAME))
					new File(filename).delete();
			} catch (Exception e) {
				// Nothing.
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the full qualified path for a game file based on a given sequence
	 * number.
	 * 
	 * @param index
	 *            The sequence number of the game file.
	 * @return The full qualified path based on the given file sequence number.
	 */
	static public String creatdeFullPath(int index) {
		return PREFIX_FILENAME + index;
	}

	/**
	 * Get the full qualified path for a game file based on a given file name.
	 * 
	 * @param filename
	 *            Name of file without a path.
	 * @return The full qualified path based on the given file name.
	 */
	static public String createFullPath(String filename) {
		return PATH + filename;
	}

	/**
	 * Copy this game file to a new file with the given file name.
	 * 
	 * @param index
	 *            The sequence number of the new game file.
	 * @throws IOException
	 */
	void copyTo(int index) throws IOException {
		InputStream in = new FileInputStream(this);
		OutputStream out = new FileOutputStream(new File(PATH + PREFIX_FILENAME + index));

		// Transfer bytes from in to out
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) > 0) {
			out.write(buffer, 0, bytesRead);
		}
		in.close();
		out.close();
	}
}