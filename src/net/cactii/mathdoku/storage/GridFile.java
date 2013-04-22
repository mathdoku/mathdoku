package net.cactii.mathdoku.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.cactii.mathdoku.CellChange;
import net.cactii.mathdoku.DevelopmentHelper;
import net.cactii.mathdoku.DevelopmentHelper.Mode;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.GridCage;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.util.Util;
import android.util.Log;

public class GridFile {
	private static final String TAG = "MathDoku.GridFile";

	// Identifier for line in grid file which holds information about the
	// revision which was used to save the file.
	public static final String SAVE_GAME_REVISION = "SAVED_WITH_REVISION";

	// The filename in which the puzzle and solving progress is stored. The
	// filename should never been changed as a reference to this filename is
	// stored in the statistics database.
	String mFilename;

	/**
	 * Creates a new instance of {@link PreviewImage}.
	 * 
	 * @param filename
	 *            The name (without path) of the preview file.
	 */
	public GridFile(String filename) {
		init(new GameFile(filename));
	}

	/**
	 * Creates a new instance of {@link PreviewImage}.
	 * 
	 * @param gameFile
	 *            The game file for which a preview has to be saved.
	 */
	public GridFile(GameFile gameFile) {
		init(gameFile);
	}

	/**
	 * Initializes the object.
	 * 
	 * @param mFilename
	 *            The game file to which the grid file belongs.
	 */
	private void init(GameFile gameFile) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (gameFile == null) {
				throw new RuntimeException(
						"GameFile should not be null or emtpy.");
			}
		}
		mFilename = gameFile.getFullFilename();
	}

	/**
	 * Save the given grid view to a game file.
	 * 
	 * @param grid
	 *            The grid to be saved.
	 * @param keepOriginalDatetimeLastSaved
	 *            True in case the datetime on which the game was last saved may
	 *            not be altered. When not converting an existing gamefile one
	 *            should use false here.
	 * @return True in case the grid was saved to a file. False otherwise.
	 */
	public boolean save(Grid grid, boolean keepOriginalDatetimeLastSaved) {
		synchronized (grid.mLock) { // Avoid saving game at the same time as
									// creating puzzle
			BufferedWriter writer = null;
			try {
				// Open file
				writer = new BufferedWriter(new FileWriter(mFilename));

				// Store information about the revision number
				writer.write(SAVE_GAME_REVISION
						+ GameFile.FIELD_DELIMITER_LEVEL1
						+ Util.getPackageVersionNumber()
						+ GameFile.EOL_DELIMITER);

				// Store information about the Grid View on a single line
				writer.write(grid
						.toStorageString(keepOriginalDatetimeLastSaved)
						+ GameFile.EOL_DELIMITER);

				// Store information about the cells. Use one line per single
				// cell.
				for (GridCell cell : grid.mCells) {
					writer.write(cell.toStorageString()
							+ GameFile.EOL_DELIMITER);
				}

				// Store information about the cages. Use one line per single
				// cage.
				for (GridCage cage : grid.mCages) {
					writer.write(cage.toStorageString()
							+ GameFile.EOL_DELIMITER);
				}

				// Store information about the cell changes. Use one line per
				// single
				// cell change. Note: watch for lengthy line due to recursive
				// cell changes.
				for (CellChange cellChange : grid.mMoves) {
					writer.write(cellChange.toStorageString()
							+ GameFile.EOL_DELIMITER);
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

		return true;
	}

	/**
	 * Load a grid file into a given grid object.
	 * 
	 * @param grid
	 *            The grid into which the data has to be loaded.
	 * 
	 * @return True in case the grid has been loaded succesfully. False
	 *         otherwise.
	 */
	public boolean loadIntoGrid(Grid grid) {
		return loadIntoGrid(grid, false);
	}

	/**
	 * Load a grid file into a given grid object.
	 * 
	 * @param grid
	 *            The grid into which the data has to be loaded.
	 * 
	 * @param headersOnly
	 *            True in case only the headers of the game file have to be
	 *            read. False in case the complete grid has to be returned.
	 * @return True in case the grid has been loaded succesfully. False
	 *         otherwise.
	 */
	protected boolean loadIntoGrid(Grid grid, boolean headersOnly) {
		String line = null;
		BufferedReader br = null;
		InputStream ins = null;
		try {
			// Open buffer to read from
			ins = new FileInputStream(new File(mFilename));
			br = new BufferedReader(new InputStreamReader(ins), 8192);

			// Read first line
			if ((line = br.readLine()) == null) {
				throw new InvalidFileFormatException("Unexpected end of file");
			}

			int savedWithRevisionNumber = -1;
			if (line.startsWith(SAVE_GAME_REVISION)) {
				savedWithRevisionNumber = Integer.parseInt(line
						.split(GameFile.FIELD_DELIMITER_LEVEL1)[1]);

				// Read next line
				if ((line = br.readLine()) == null) {
					throw new InvalidFileFormatException(
							"Unexpected end of file");
				}

			}

			// Read view information
			if (!grid.fromStorageString(line, savedWithRevisionNumber)) {
				// The initial version of the saved games stored view
				// information on 4 different lines. Rewrite to valid view
				// storage information (version 1).
				// Do not remove as long as backward compatibility with old save
				// file should be remained.
				line = Grid.SAVE_GAME_GRID_LINE
						+ GameFile.FIELD_DELIMITER_LEVEL1 + 0 // game seed. Use
																// 0 as it was
																// not stored in
																// initial
																// files
						+ GameFile.FIELD_DELIMITER_LEVEL1 + line // date created
						+ GameFile.FIELD_DELIMITER_LEVEL1 + br.readLine() // elapsed
																			// time
						+ GameFile.FIELD_DELIMITER_LEVEL1 + br.readLine() // grid
																			// size
						+ GameFile.FIELD_DELIMITER_LEVEL1 + br.readLine(); // active

				// Retry to process this line as if it was saved with revision
				// 1.
				if (!grid.fromStorageString(line, 1)) {
					throw new InvalidFileFormatException(
							"View information can not be processed: " + line);
				}
			}
			if ((line = br.readLine()) == null) {
				throw new InvalidFileFormatException("Unexpected end of file");
			}

			// Exit loading file in case only headers should be loaded.
			if (headersOnly) {
				return true;
			}

			// Read cell information
			int gridSize = grid.getGridSize();
			int countCellsToRead = gridSize * gridSize;
			GridCell selectedCell = null;
			while (countCellsToRead > 0) {
				GridCell cell = new GridCell(grid, 0);
				if (!cell.fromStorageString(line, savedWithRevisionNumber)) {
					throw new InvalidFileFormatException(
							"Line does not contain cell information while this was expected:"
									+ line);
				}
				grid.mCells.add(cell);
				if (cell.mSelected && selectedCell == null) {
					// Remember first cell which is marked as selected. Note the
					// cell can not be selected until the cages are loaded as
					// well.
					selectedCell = cell;
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
				if (selectedCell == null) {
					// No cell is selected yet.
					int selected = Integer.parseInt(line
							.split(GameFile.FIELD_DELIMITER_LEVEL1)[1]);
					selectedCell = grid.mCells.get(selected);
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
				String invalidlist = line
						.split(GameFile.FIELD_DELIMITER_LEVEL1)[1];
				for (String cellId : invalidlist
						.split(GameFile.FIELD_DELIMITER_LEVEL2)) {
					int cellNum = Integer.parseInt(cellId);
					GridCell c = grid.mCells.get(cellNum);
					c.setInvalidHighlight(true);
				}

				// Read next line
				if ((line = br.readLine()) == null) {
					throw new InvalidFileFormatException(
							"Unexpected end of file");
				}
			}

			// Cages (at least one expected)
			GridCage cage = new GridCage(grid);
			if (!cage.fromStorageString(line, savedWithRevisionNumber)) {
				throw new InvalidFileFormatException(
						"Line does not contain cage  information while this was expected:"
								+ line);
			}
			do {
				grid.mCages.add(cage);

				// Read next line. No checking of unexpected end of file might
				// be done here because the last line in a file can contain a
				// cage.
				line = br.readLine();

				// Create a new empty cage
				cage = new GridCage(grid);
			} while (line != null
					&& cage.fromStorageString(line, savedWithRevisionNumber));

			// Check cage maths after all cages have been read.
			for (GridCage cage2 : grid.mCages) {
				cage2.checkCageMathsCorrect(true);
			}

			// Set the selected cell (and indirectly the selected cage).
			if (selectedCell != null) {
				grid.setSelectedCell(selectedCell);
			}

			// Remaining lines contain cell changes (zero or more expected)
			CellChange cellChange = new CellChange();
			while (line != null
					&& cellChange.fromStorageString(line, grid.mCells,
							savedWithRevisionNumber)) {
				grid.addMove(cellChange);

				// Read next line. No checking of unexpected end of file might
				// be done here because the last line in a file can contain a
				// cage.
				line = br.readLine();

				// Create a new empty cell change
				cellChange = new CellChange();
			}

			// Check if en of file is reached an no information was unread yet.
			if (line != null) {
				throw new InvalidFileFormatException(
						"Unexpected line found while end of file was expected: "
								+ line);
			}

			// Load the statistics of the grid
			if (!grid.loadStatistics()) {
				throw new InvalidStatisticsException(
						"Can not load statistics neither create a new statistics records.");
			}
		} catch (InvalidStatisticsException e) {
			Log.d(TAG, "Statistics exception when restoring game " + mFilename
					+ "\n" + e.getMessage());
			return false;
		} catch (InvalidFileFormatException e) {
			Log.d(TAG, "Invalid file format error when restoring game "
					+ mFilename + "\n" + e.getMessage());
			return false;
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found error when restoring game " + mFilename
					+ "\n" + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.d(TAG,
					"IO error when restoring game " + mFilename + "\n"
							+ e.getMessage());
			return false;
		} catch (NumberFormatException e) {
			Log.d(TAG, "Number format error when restoring game " + mFilename
					+ "\n" + e.getMessage());
			return false;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAG, "Index out of bound error when restoring game "
					+ mFilename + "\n" + e.getMessage());
			return false;
		} finally {
			try {
				ins.close();
				br.close();
			} catch (Exception e) {
				// Nothing.
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the file name.
	 * 
	 * @return The file name.
	 */
	public String getFilename() {
		return mFilename;
	}
}
