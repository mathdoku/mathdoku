package net.cactii.mathdoku;

import java.io.BufferedInputStream;
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
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

public class GameFile extends File {
	private static final long serialVersionUID = -1996348039574358140L;

	private static final String TAG = "MathDoku.GameFile";

	// Game file types
	public static enum GameFileType {
		LAST_GAME, SAVED_GAME, NEW_GAME
	};

	// Identifiers for building file names
	private static final String PATH = "/data/data/net.cactii.mathdoku/";
	private static final String FILENAME_LAST_GAME = "last_game";
	private static final String FILENAME_SAVED_GAME = "saved_game_";
	private static final String FILENAME_NEW_GAME = "new_game";
	private static final String GAMEFILE_EXTENSION = ".mgf"; // MGF = Mathdoku
																// Game File
	private static final String GAMEFILE_EXTENSION_ERROR = ".err"; // Game File
																	// with an
																	// error
	private static final String PREVIEW_EXTENSION = ".png";

	// Remove "&& false" in following line to show debug information about
	// saving and restoring files when running in development mode.
	public static final boolean DEBUG_SAVE_RESTORE = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	// Base of filenames for this game file. The baseFilename does not include a
	// path.
	private String mBaseFilename;

	// Delimiters used in files to separate objects, fields and field with
	// multiple values.
	public static final String EOL_DELIMITER = "\n"; // Separate objects
	public static final String FIELD_DELIMITER_LEVEL1 = ":"; // Separate fields
	public static final String FIELD_DELIMITER_LEVEL2 = ","; // Separate values
																// in fields

	// The game file header object contains some basic information about a game
	// file which is needed by the GameFileAdapter.
	public class GameFileHeader {
		public String filename;
		public long datetimeSaved;
		public int gridSize;
		public boolean hasPreviewAvailable;
	};

	/**
	 * Creates a new instance of {@link GameFile} for the default game file.
	 * 
	 * @param gameFileType
	 *            The type of game file which needs to be instantiated.
	 */
	public GameFile(GameFileType gameFileType) {
		super(PATH + getFilenameForType(gameFileType));
		this.mBaseFilename = getFilenameForType(gameFileType);
	}

	/**
	 * Creates a new instance of {@link GameFile} for the given game file.
	 * 
	 * @param filename
	 *            Name of file to be processed.
	 */
	public GameFile(String filename) {
		// Only append filename with PATH if not yet included.
		super(PATH + filename);
		this.mBaseFilename = filename;
	}

	/**
	 * Save the given grid to a game file. The preview image of the grid is
	 * based upon the given grid view.
	 * 
	 * @param mainActivity
	 *            The main activity in which context the game is saved.
	 * 
	 * @return True in case both the grid and the preview image have been saved.
	 *         False otherwise.
	 */
	public boolean save(MainActivity mainActivity) {
		if (mainActivity.mGrid == null) {
			return false;
		}

		// First save the grid.
		if (save(mainActivity.mGrid, false)) {
			if (mainActivity.mGridView == null) {
				return false;
			}

			// Save a preview image of the grid view for faster scrolling in
			// the GameFileListAdacpter.
			return savePreviewImage(mainActivity, mainActivity.mGridView);
		} else {
			return false;
		}
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
				writer = new BufferedWriter(new FileWriter(getFullFilename()));

				// Store information about the Grid View on a single line
				writer.write(grid
						.toStorageString(keepOriginalDatetimeLastSaved)
						+ EOL_DELIMITER);

				// Store information about the cells. Use one line per single
				// cell.
				for (GridCell cell : grid.mCells) {
					writer.write(cell.toStorageString() + EOL_DELIMITER);
				}

				// Store information about the cages. Use one line per single
				// cage.
				for (GridCage cage : grid.mCages) {
					writer.write(cage.toStorageString() + EOL_DELIMITER);
				}

				// Store information about the cell changes. Use one line per
				// single
				// cell change. Note: watch for lengthy line due to recursive
				// cell changes.
				for (CellChange cellChange : grid.mMoves) {
					writer.write(cellChange.toStorageString() + EOL_DELIMITER);
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
	 * Load the game file header only. As cells and cages are not loaded, this
	 * will result in faster loading.
	 * 
	 * @return The game file header {@link GameFileHeader} if headers are
	 *         loaded. Null otherwise.
	 */
	public GameFileHeader loadHeadersOnly() {
		Grid grid = load(true);
		if (grid != null) {
			// Return game file header only if grid was successfully loaded
			GameFileHeader gameFileHeader = new GameFileHeader();
			gameFileHeader.filename = this.mBaseFilename;
			gameFileHeader.datetimeSaved = grid.getDateSaved();
			gameFileHeader.gridSize = grid.getGridSize();
			gameFileHeader.hasPreviewAvailable = this.hasPreviewImage();
			return gameFileHeader;
		}
		return null;
	}

	/**
	 * Load the grid from the game file.
	 * 
	 * @return The grid which was loaded from the game file. Null in case of an
	 *         error.
	 */
	public Grid load() {
		return load(false);
	}

	/**
	 * Load the grid from the game file.
	 * 
	 * @param headersOnly
	 *            True in case only the headers of the game file have to be
	 *            read. False in case the complete grid has to be returned.
	 * @return The grid which was loaded from the game file. Null in case of an
	 *         error.
	 */
	private Grid load(boolean headersOnly) {
		Grid grid = new Grid(0);

		String filenameLong = getFullFilename();
		String line = null;
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

			// Read view information
			if (!grid.fromStorageString(line)) {
				// The initial version of the saved games stored view
				// information on 4 different lines. Rewrite to valid view
				// storage information (version 1).
				// Do not remove as long as backward compatibility with old save
				// file should be remained.
				line = Grid.SAVE_GAME_GRID_VERSION_01 + FIELD_DELIMITER_LEVEL1
						+ 0 // game seed. Use 0 as it was not stored in initial
							// files
						+ FIELD_DELIMITER_LEVEL1 + line // date created
						+ FIELD_DELIMITER_LEVEL1 + br.readLine() // elapsed time
						+ FIELD_DELIMITER_LEVEL1 + br.readLine() // grid size
						+ FIELD_DELIMITER_LEVEL1 + br.readLine(); // active

				// Retry to process this line.
				if (!grid.fromStorageString(line)) {
					throw new InvalidFileFormatException(
							"View information can not be processed: " + line);
				}
			}
			if ((line = br.readLine()) == null) {
				throw new InvalidFileFormatException("Unexpected end of file");
			}

			// Exit loading file in case only headers should be loaded.
			if (headersOnly) {
				return grid;
			}

			// Read cell information
			int gridSize = grid.getGridSize();
			int countCellsToRead = gridSize * gridSize;
			GridCell selectedCell = null;
			while (countCellsToRead > 0) {
				GridCell cell = new GridCell(grid, 0);
				if (!cell.fromStorageString(line)) {
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
							.split(FIELD_DELIMITER_LEVEL1)[1]);
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
				String invalidlist = line.split(FIELD_DELIMITER_LEVEL1)[1];
				for (String cellId : invalidlist.split(FIELD_DELIMITER_LEVEL2)) {
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
			if (!cage.fromStorageString(line)) {
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
			} while (line != null && cage.fromStorageString(line));

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
					&& cellChange.fromStorageString(line, grid.mCells)) {
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
		} catch (InvalidFileFormatException e) {
			Log.d(TAG, "Invalid file format error when restoring game "
					+ filenameLong + "\n" + e.getMessage());
			return null;
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found error when restoring game "
					+ filenameLong + "\n" + e.getMessage());
			return null;
		} catch (IOException e) {
			Log.d(TAG, "IO error when restoring game " + filenameLong + "\n"
					+ e.getMessage());
			return null;
		} catch (NumberFormatException e) {
			Log.d(TAG, "Number format error when restoring game "
					+ filenameLong + "\n" + e.getMessage());
			return null;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAG, "Index out of bound error when restoring game "
					+ filenameLong + "\n" + e.getMessage());
			return null;
		} finally {
			try {
				ins.close();
				br.close();
			} catch (Exception e) {
				// Nothing.
				return null;
			}
		}
		return grid;
	}

	/**
	 * Copy this game file to a new file. The preview image will be copied as
	 * well.
	 * 
	 * @throws IOException
	 */
	public void copyToNewGameFile() {
		// Determine first file index number which is currently not in use.
		int fileIndex;
		for (fileIndex = 0;; fileIndex++) {
			if (!new File(PATH + FILENAME_SAVED_GAME + fileIndex
					+ GAMEFILE_EXTENSION).exists()) {
				break;
			}
		}

		// Save the file at the first unused file index number.
		copyFile(getFullFilename(), PATH + FILENAME_SAVED_GAME + fileIndex
				+ GAMEFILE_EXTENSION);

		// Copy preview if it exists
		if (hasPreviewImage()) {
			copyFile(getFullFilenamePreview(), PATH + FILENAME_SAVED_GAME
					+ fileIndex + PREVIEW_EXTENSION);
		}
	}

	/**
	 * Copy a file to a new file with the given file name.
	 * 
	 * @param filenameInput
	 *            Name of file to be copied.
	 * @param filenameOutput
	 *            Name of file to which the input file is copied.
	 */
	public static void copyFile(String filenameInput, String filenameOutput) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(new File(filenameInput));
			out = new FileOutputStream(new File(filenameOutput));

			// Transfer bytes from in to out
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, bytesRead);
			}
		} catch (FileNotFoundException e) {
			Log.d(TAG,
					"File not found error when copying game file "
							+ filenameInput + " to " + filenameOutput + "\n"
							+ e.getMessage());
		} catch (IOException e) {
			Log.d(TAG,
					"IO Exception error when copying game file "
							+ filenameInput + " to " + filenameOutput + "\n"
							+ e.getMessage());
		} finally {
			try {
				in.close();
				out.close();
			} catch (Exception e) {
				// Do nothing
			}
		}
	}

	/**
	 * Save a preview image of the grid view.
	 * 
	 * @param activity
	 *            The activity in which context the preview image is saved.
	 * @param view
	 *            The grid view for which the preview image has to be generated.
	 */
	public boolean savePreviewImage(Activity activity, GridView view) {
		// Check if the view dimensions allow to make a preview.
		if (view.getWidth() == 0 || view.getHeight() == 0) {
			if (DevelopmentHelper.mMode != Mode.PRODUCTION) {
				Log.i(TAG,
						"Can not save the preview image. If running on an Emulator for "
								+ "Android 2.2 this is normal behavior when rotating screen "
								+ "from landscap to portrait as the Activity.onCreate is called "
								+ "twice instead of one time.");
				return true;
			}
			// Could not create a preview.
			return false;
		}

		// Determine size of preview images as needed by the file adapter.
		int previewSize = GameFileListAdapter.getPreviewImageSize(activity);
		if (previewSize == 0) {
			// Can not calculate size of preview image.
			return false;
		}

		// Create a scaled bitmap and canvas and draw the view on this canvas.
		float scaleFactor = (float) previewSize
				/ (float) Math.max(view.getWidth(), view.getHeight());
		Bitmap bitmap = Bitmap.createBitmap(previewSize, previewSize,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.scale(scaleFactor, scaleFactor);
		view.draw(canvas);

		// Write the created bitmap to a file.
		try {
			FileOutputStream out = new FileOutputStream(
					getFullFilenamePreview());
			bitmap.compress(Bitmap.CompressFormat.PNG, 1, out); // Compress
																// factor is
			// not used with PNG
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Get the preview image for this game file.
	 * 
	 * @return The bitmap containing the preview image of this game file.
	 */
	public Bitmap getPreviewImage() {
		Bitmap bitmap = null;

		String filenamePreview = getFullFilenamePreview();
		File file = new File(filenamePreview);
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			bitmap = BitmapFactory.decodeStream(in);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found error when loading image preview "
					+ filenamePreview + "\n" + e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}

		return bitmap;
	}

	/**
	 * Checks if a preview image exist for is this game file.
	 * 
	 * @return True if a preview image exists for this game file.
	 */
	public boolean hasPreviewImage() {
		return new File(getFullFilenamePreview()).exists();
	}

	/**
	 * Delete the preview of this game file.
	 * 
	 * @return True if succesful. False otherwise.
	 */
	public boolean deletePreviewImage() {
		return new File(getFullFilenamePreview()).delete();
	}

	/**
	 * Deletes the game file and its preview image if it exists.
	 */
	@Override
	public boolean delete() {
		// Delete preview image first
		File previewFile = new File(getFullFilenamePreview());
		if (previewFile.exists()) {
			previewFile.delete();
		}

		// Delete game file itself.
		return super.delete();
	}

	/**
	 * Get a list of files which meet the given conditions.
	 * 
	 * @param includeDefaultGameFile
	 *            True in case the default game file should be included in the
	 *            list of game files. False otherwise.
	 * @param maximumFiles
	 *            The maximum number of files which should be returned.
	 * @return A list of files. An empty list in case of an error.
	 */
	private static ArrayList<String> getAllGameFiles(
			GameFileType[] gameFileType, int maximumFiles) {
		// Check which game file types have to be returned.
		boolean includeLastGame = false;
		boolean includeUserGame = false;
		boolean includeNewGame = false;
		for (int i = 0; i < gameFileType.length; i++) {
			switch (gameFileType[i]) {
			case LAST_GAME:
				includeLastGame = true;
				break;
			case SAVED_GAME:
				includeUserGame = true;
				break;
			case NEW_GAME:
				includeNewGame = true;
				break;
			default:
				// Ignore this game type.
			}
		}

		// Resulting array list of file names.
		ArrayList<String> gameFiles = new ArrayList<String>();

		// Retrieve all files from game file directory
		File dir = new File(PATH);
		String[] filenames = dir.list();

		// Check all files but stop if maximum is reached
		int countFiles = 0;
		for (String filename : filenames) {
			if (filename.endsWith(GameFile.PREVIEW_EXTENSION)
					|| filename.endsWith(GameFile.GAMEFILE_EXTENSION_ERROR)) {
				// Always skip these files.
				continue;
			}

			if ((includeUserGame && filename
					.startsWith(GameFile.FILENAME_SAVED_GAME))
					|| (includeLastGame && filename
							.startsWith(GameFile.FILENAME_LAST_GAME))
					|| (includeNewGame && filename
							.startsWith(GameFile.FILENAME_NEW_GAME))) {
				// The file has to be included.
				gameFiles.add(filename);
				countFiles++;
				if (countFiles == maximumFiles) {
					break;
				}
			}
		}

		return gameFiles;
	}

	/**
	 * Gets a list of all game files (including the default game file)
	 * available.
	 * 
	 * @param maximumFiles
	 *            The maximum number of files that may be returned. Use
	 *            {@link Integer.MAX_VALUE} for an unlimited list.
	 * @return A list of game files, possibly including the default game files.
	 *         Preview image files will not be returned. Neither will more files
	 *         be returned than requested.
	 */
	public static ArrayList<String> getAllGameFiles(int maximumFiles) {
		GameFileType[] gameFileTypes = new GameFileType[] {
				GameFileType.SAVED_GAME, GameFileType.LAST_GAME };
		return getAllGameFiles(gameFileTypes, maximumFiles);
	}

	/**
	 * Gets a list of all game files created by the user (i.e. not including the
	 * default game file).
	 * 
	 * @param maximumFiles
	 *            The maximum number of files that may be returned. Use
	 *            {@link Integer.MAX_VALUE} for an unlimited list.
	 * @return A list of game files, not including the default game files.
	 *         Preview image files will not be returned.
	 */
	public static ArrayList<String> getAllGameFilesCreatedByUser(
			int maximumFiles) {
		GameFileType[] gameFileTypes = new GameFileType[] { GameFileType.SAVED_GAME };
		return getAllGameFiles(gameFileTypes, maximumFiles);
	}

	/**
	 * Converts a game file type to a file name.
	 * 
	 * @param gameFileType
	 *            The game file type. Can not be used for game file type
	 *            SAVED_GAME.
	 * @return The filename (without path) for the game file type.
	 */
	private static String getFilenameForType(GameFileType gameFileType) {
		switch (gameFileType) {
		case LAST_GAME:
			return FILENAME_LAST_GAME + GAMEFILE_EXTENSION;
		case SAVED_GAME:
			throw new RuntimeException(
					"Method getFilenameForType(GameFileType) can not be used for GameFileType = SAVED_GAME");
		case NEW_GAME:
			return FILENAME_NEW_GAME + GAMEFILE_EXTENSION;
		}
		return null;
	}

	/**
	 * Get full path of the game file.
	 * 
	 * @return The full path of the game file.
	 */
	private String getFullFilename() {
		return PATH + mBaseFilename;
	}

	/**
	 * Get full path of the preview image of the game file.
	 * 
	 * @return The full path of the preview image of the game file.
	 */
	private String getFullFilenamePreview() {
		return PATH
				+ this.mBaseFilename.replace(GAMEFILE_EXTENSION,
						PREVIEW_EXTENSION);
	}
}