package net.cactii.mathdoku.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import net.cactii.mathdoku.DevelopmentHelper;
import net.cactii.mathdoku.DevelopmentHelper.Mode;
import android.util.Log;

public class GameFile extends File {
	private static final long serialVersionUID = -1996348039574358140L;

	private static final String TAG = "MathDoku.GameFile";

	// Game file types
	public static enum GameFileType {
		LAST_GAME, SAVED_GAME, NEW_GAME
	};

	// Delimiters used in files to separate objects, fields and field with
	// multiple values.
	public static final String EOL_DELIMITER = "\n"; // Separate objects
	public static final String FIELD_DELIMITER_LEVEL1 = ":"; // Separate fields
	public static final String FIELD_DELIMITER_LEVEL2 = ","; // Separate values
																// in fields

	
	// Identifiers for building file names
	private static final String PATH = "/data/data/net.cactii.mathdoku/";
	private static final String FILENAME_LAST_GAME = "last_game";
	private static final String FILENAME_SAVED_GAME = "saved_game_";
	private static final String FILENAME_NEW_GAME = "new_game";
	private static final String GAMEFILE_EXTENSION = ".mgf"; // MGF = Mathdoku
																// Game File
	private static final String PREVIEW_EXTENSION = ".png";

	// Remove "&& false" in following line to show debug information about
	// saving and restoring files when running in development mode.
	public static final boolean DEBUG_SAVE_RESTORE = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	// Base of filenames for this game file. The baseFilename does not include a
	// path.
	private String mBaseFilename;

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
			if (filename.endsWith(GameFile.PREVIEW_EXTENSION)) {
				// Skip previews images allways.
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
	public static String getFilenameForType(GameFileType gameFileType) {
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
	protected String getFullFilename() {
		return PATH + mBaseFilename;
	}
	
	/**
	 * Get full path of the preview image of the game file.
	 * 
	 * @return The full path of the preview image of the game file.
	 */
	protected String getFullFilenamePreview() {
		return PATH
				+ this.mBaseFilename.replace(GAMEFILE_EXTENSION,
						PREVIEW_EXTENSION);
	}
}