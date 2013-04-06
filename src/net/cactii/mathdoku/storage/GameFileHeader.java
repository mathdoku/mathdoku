package net.cactii.mathdoku.storage;

import net.cactii.mathdoku.Grid;

// The game file header object contains some basic information about a game
// file which is needed by the GameFileAdapter.

public class GameFileHeader {

	public String mFilename;
	public String mFilenamePreview;
	public long mDatetimeSaved;
	public int mGridSize;
	public boolean mHasPreviewAvailable;

	/**
	 * Load the game file header from a grid file. As cells and cages are not
	 * loaded, this will result in faster loading.
	 * 
	 * @param gameFile
	 *            The game file for which the headers have to be extracted.
	 * 
	 * @return True if the game file header is loaded from the given file. False
	 *         otherwise.
	 */
	public boolean load(String filename) {
		GameFile gameFile = new GameFile(filename);

		GridFile gridFile = new GridFile(gameFile);
		Grid grid = new Grid(0);
		if (!gridFile.loadIntoGrid(grid, true)) {
			return false;
		}

		mFilename = gameFile.getName();
		mDatetimeSaved = grid.getDateSaved();
		mGridSize = grid.getGridSize();
		mHasPreviewAvailable = gameFile.hasPreviewImage();
		mFilenamePreview = (mHasPreviewAvailable ? gameFile
				.getFullFilenamePreview() : "");

		return true;
	}
}