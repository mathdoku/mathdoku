package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.Random;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import net.cactii.mathdoku.GameFile.GameFileType;
import net.cactii.mathdoku.GridGenerating.GridGeneratingParameters;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.srlee.DLX.DLX.SolveType;
import com.srlee.DLX.MathDokuDLX;

/**
 * Generates a new grid while displaying a progress dialog.
 */
public class GridGenerator extends AsyncTask<Void, String, Void> {
	private static final String TAG = "MathDoku.GridGenerator";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG_GRID_GENERATOR = (DevelopmentHelper.mode == Mode.DEVELOPMENT) && false;
	public static final boolean DEBUG_GRID_GENERATOR_FULL = DEBUG_GRID_GENERATOR && false;

	// Cages with too many permutations will make cage generation and solving
	// too hard. The following arbitrary treshold is used to maintain a balance
	// between quick puzzle generation and solving complexity.
	private static final int MAX_CAGE_PERMUTATIONS = 80;
	private int mMaxCageSize;

	// The grid created by the generator
	private Grid mGrid;

	private boolean mHideOperators;
	private MainActivity mActivity;

	// Random generator
	public Random mRandom;
	private long mGameSeed;
	private int mGeneratorRevisionNumber;

	// Size of the grid
	public int mGridSize;

	// The dialog for this task
	private ProgressDialog mProgressDialog;

	// Cell and solution
	public ArrayList<GridCell> mCells;
	private int[][] mSolutionMatrix;

	// Cages
	private CageTypeGenerator mGridCageTypeGenerator;
	public ArrayList<GridCage> mCages;
	private int[][] mCageMatrix;
	private int mMaxCageResult;

	// Additional option for generating the grid
	private GridGeneratorOptions mGridGeneratorOptions;

	// Timestamp for logging purposes
	long mTimeStarted;
	long mTimeStartedSolution;

	// The grid generator options are used in development mode only to generate
	// fake games.
	public class GridGeneratorOptions {
		public int numberOfGamesToGenerate;
		public boolean createFakeUserGameFiles;
		public boolean randomGridSize;
		public boolean randomHideOperators;
	}

	/**
	 * Creates a new instance of {@link GridGenerator}.
	 * 
	 * @param activity
	 *            The activity from which this task is started.
	 * @param gridSize
	 *            The size of the gird to be created.
	 * @param hideOperators
	 *            True in case should be solvable without using operators.
	 */
	public GridGenerator(MainActivity activity, int gridSize, int maxCageSize,
			int maxCageResult, boolean hideOperators) {
		this.mGridSize = gridSize;
		this.mHideOperators = hideOperators;
		this.mMaxCageSize = maxCageSize;
		this.mMaxCageResult = maxCageResult;
		this.mGeneratorRevisionNumber = activity.getVersionNumber();

		setGridGeneratorOptions(null);

		// Attach the task to the activity activity and show progress dialog if
		// needed.
		attachToActivity(activity);
	}

	/**
	 * Sets the additional options for the grid generator. Only to be used in
	 * development mode.
	 * 
	 * @param gridGeneratorOptions
	 *            The additional options to be set.
	 */
	public void setGridGeneratorOptions(
			GridGeneratorOptions gridGeneratorOptions) {
		if (gridGeneratorOptions == null) {
			// Use default values if options are not specified
			this.mGridGeneratorOptions = new GridGeneratorOptions();
			this.mGridGeneratorOptions.numberOfGamesToGenerate = 1;
			this.mGridGeneratorOptions.createFakeUserGameFiles = false;
			this.mGridGeneratorOptions.randomGridSize = false;
			this.mGridGeneratorOptions.randomHideOperators = false;
			return;
		}

		// Use specified options only if running in development mode.
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
			this.mGridGeneratorOptions = gridGeneratorOptions;

			// Rebuild the dialog using the grid generator options.
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				buildDialog();
			}
		}
	}

	/**
	 * Attaches the activity to the ASync task.
	 * 
	 * @param activity
	 *            The activity to which results will be sent on completion of
	 *            this task.
	 */
	public void attachToActivity(MainActivity activity) {
		if (activity.equals(this.mActivity) && mProgressDialog != null
				&& mProgressDialog.isShowing()) {
			// The activity is already attached to this task.
			return;
		}

		if (DEBUG_GRID_GENERATOR) {
			Log.i(TAG, "Attach to activity");
		}

		// Remember the activity that started this task.
		this.mActivity = activity;

		buildDialog();
	}

	/**
	 * Build and show the dialog.
	 */
	private void buildDialog() {
		// Build the dialog
		mProgressDialog = new ProgressDialog(mActivity);
		mProgressDialog.setTitle(R.string.dialog_building_puzzle_title);
		mProgressDialog.setMessage(mActivity.getResources().getString(
				R.string.dialog_building_puzzle_message));
		mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(false);

		// Set style of dialog.
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
			if (mGridGeneratorOptions.numberOfGamesToGenerate > 1) {
				mProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog
						.setMax(mGridGeneratorOptions.numberOfGamesToGenerate);
			}
		}

		// Show the dialog
		mProgressDialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		if (mGridSize < 4) {
			return null;
		}

		mTimeStarted = System.currentTimeMillis();

		// Create a new empty grid.
		mGrid = new Grid(mGridSize);

		int num_solns = 0;
		int num_attempts = 0;

		// Generate a random seed. This seed will be used to for another
		// Randomizer which will be used to generate the game. By saving and
		// displaying the seed as game number, it should be possible to
		// recreate a game (as long as the implementation of the Randomize
		// has not changed).
		mGameSeed = (new Random()).nextLong();
		mRandom = new Random(mGameSeed);

		if (DEBUG_GRID_GENERATOR) {
			Log.i(TAG, "Game seed: " + mGameSeed);
		}

		do {
			num_attempts++;

			mTimeStartedSolution = System.currentTimeMillis();

			if (DEBUG_GRID_GENERATOR) {
				Log.i(TAG, "Puzzle generation attempt: " + num_attempts);
				publishProgress(
						DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_TITLE,
						mActivity.getResources().getString(
								R.string.dialog_building_puzzle_title)
								+ " (attempt " + num_attempts + ")");
			}

			mCells = new ArrayList<GridCell>();
			int cellnum = 0;
			for (int i = 0; i < mGridSize * mGridSize; i++) {
				mCells.add(new GridCell(mGrid, cellnum++));
			}

			randomiseGrid();

			this.mCages = new ArrayList<GridCage>();
			createCages(mHideOperators);

			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				if (mGridGeneratorOptions.createFakeUserGameFiles) {
					// The faked user games files do not require a unique
					// solution which results in much faster generation time.

					// Create the grid object
					GridGeneratingParameters gridGeneratingParameters = new GridGeneratingParameters();
					gridGeneratingParameters.mGameSeed = this.mGameSeed;
					gridGeneratingParameters.mGeneratorRevisionNumber = this.mGeneratorRevisionNumber;
					gridGeneratingParameters.mHideOperators = this.mHideOperators;
					gridGeneratingParameters.mMaxCageResult = this.mMaxCageResult;
					gridGeneratingParameters.mMaxCageSize = this.mMaxCageSize;
					mGrid.create(mGridSize, mCells, mCages, true,
							gridGeneratingParameters);

					// Store grid as user file
					GameFile gameFile = new GameFile(GameFileType.NEW_GAME);
					gameFile.save(mGrid, false);
					gameFile.copyToNewGameFile();

					publishProgress(
							DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_MESSAGE,
							"");
					publishProgress(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_PROGRESS);

					// Check if more puzzles have to generated.
					if (num_attempts < mGridGeneratorOptions.numberOfGamesToGenerate) {
						// Determine random size and hide operator values of
						// next grid
						mGameSeed = (new Random()).nextLong();
						mRandom = new Random(mGameSeed);
						if (mGridGeneratorOptions.randomGridSize) {
							mGridSize = 4 + (new Random().nextInt(6));
						}
						if (mGridGeneratorOptions.randomHideOperators) {
							mHideOperators = new Random().nextBoolean();
						}
						mGrid = new Grid(mGridSize);

						// Fake a non unique solution so another grid is
						// generated.
						num_solns = 2;
						continue;
					} else {
						return null;
					}
				}
			}

			if (DEBUG_GRID_GENERATOR) {
				publishProgress(
						DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_MESSAGE,
						"Verify unique solution");
			}

			// Determine whether grid has a unique solution.
			MathDokuDLX mdd = new MathDokuDLX(this.mGridSize, this.mCages);
			num_solns = mdd.Solve(this, SolveType.MULTIPLE);

			if (DEBUG_GRID_GENERATOR) {
				Log.d(TAG, "Num Solns = " + num_solns);
			}
		} while (num_solns > 1);
		if (DEBUG_GRID_GENERATOR) {
			Log.d(TAG, "Found puzzle with unique solution in " + num_attempts
					+ " attempts.");
		}
		return null;
	}

	protected void onProgressUpdate(String... values) {
		if (DEBUG_GRID_GENERATOR) {
			long timeElapsed = System.currentTimeMillis() - mTimeStarted;
			if (values.length == 1 && values[0] != null) {
				if (values[0]
						.equals(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_SOLUTION)) {
					Log.i(TAG,
							Long.toString(timeElapsed)
									+ ": found a solution for this puzzle in "
									+ (System.currentTimeMillis() - mTimeStartedSolution)
									+ " miliseconds");
					mTimeStartedSolution = System.currentTimeMillis();
				}
			}
			if (values.length >= 2 && values[0] != null && values[1] != null) {

				if (values[0]
						.equals(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_TITLE)) {
					mProgressDialog.setTitle(values[1]);
				} else if (values[0]
						.equals(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_MESSAGE)) {
					mProgressDialog.setMessage(values[1]);
				}
				if (!values[1].equals("")) {
					Log.i(TAG, Long.toString(timeElapsed) + ": " + values[1]);
				}
			}
		}
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
			if (values.length > 0
					&& values[0] != null
					&& values[0]
							.equals(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_PROGRESS)) {
				mProgressDialog.incrementProgressBy(1);
			}
		}
	}

	/**
	 * Callback for the DLX algorithm in case a solution is found for a puzzle.
	 */
	public void publishProgressGridSolutionFound() {
		publishProgress(DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_MESSAGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Void result) {
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
			if (mGridGeneratorOptions.createFakeUserGameFiles) {
				mActivity.mGridGeneratorTask = null;
				// Grids are already saved.
				DevelopmentHelper.generateGamesReady(mActivity,
						mGridGeneratorOptions.numberOfGamesToGenerate);
				if (this.mProgressDialog != null) {
					dismissProgressDialog();
				}
				super.onPostExecute(result);
				return;
			}
		}

		// Create the grid object
		GridGeneratingParameters gridGeneratingParameters = new GridGeneratingParameters();
		gridGeneratingParameters.mGameSeed = this.mGameSeed;
		gridGeneratingParameters.mGeneratorRevisionNumber = this.mGeneratorRevisionNumber;
		gridGeneratingParameters.mHideOperators = this.mHideOperators;
		gridGeneratingParameters.mMaxCageResult = this.mMaxCageResult;
		gridGeneratingParameters.mMaxCageSize = this.mMaxCageSize;
		mGrid.create(mGridSize, mCells, mCages, true, gridGeneratingParameters);

		if (mActivity != null) {
			if (DEBUG_GRID_GENERATOR) {
				Log.d(TAG, "Send results to activity.");
			}

			// The task is still attached to a activity. Inform activity about
			// completing the new game generation. The activity will deal with
			// showing or showing the new grid directly.
			mActivity.onNewGridReady(mGrid);

			// Dismiss the dialog if still visible
			if (this.mProgressDialog != null) {
				dismissProgressDialog();
			}
		}

		super.onPostExecute(result);
	}

	/**
	 * Detaches the activity form the ASyn task. The progress dialog which was
	 * shown will be dismissed. The ASync task however still keeps running until
	 * finished.
	 */
	public void detachFromActivity() {
		if (DEBUG_GRID_GENERATOR) {
			Log.d(TAG, "Detach from activity");
		}

		dismissProgressDialog();
		mActivity = null;
	}

	/**
	 * Dismisses the progress dialog which was shown on start of this ASync
	 * task. The ASync task however still keeps running until finished.
	 */
	public void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	/*
	 * Fills the grid with random numbers, per the rules:
	 * 
	 * - 1 to <rowsize> on every row and column - No duplicates in any row or
	 * column.
	 */
	private void randomiseGrid() {
		int attempts;
		mSolutionMatrix = new int[this.mGridSize][this.mGridSize];
		for (int value = 1; value < this.mGridSize + 1; value++) {
			for (int row = 0; row < this.mGridSize; row++) {
				attempts = 20;
				GridCell cell;
				int column;
				while (true) {
					column = this.mRandom.nextInt(this.mGridSize);
					cell = getCellAt(row, column);
					if (--attempts == 0)
						break;
					if (cell.getCorrectValue() != 0)
						continue;
					if (valueInColumn(column, value))
						continue;
					break;
				}
				if (attempts == 0) {
					this.clearValue(value--);
					break;
				}
				cell.setCorrectValue(value);
				mSolutionMatrix[row][column] = value;
			}
		}
	}

	/**
	 * Creates cages for the current grid which is already filled with numbers.
	 * 
	 * @param hideOperators
	 *            True in case cages should hide the operator. False in
	 *            operators should be visible.
	 */
	private void createCages(boolean hideOperators) {
		this.mGridCageTypeGenerator = CageTypeGenerator.getInstance();

		boolean restart;
		int attempts = 1;
		do {
			restart = false;
			mCageMatrix = new int[this.mGridSize][this.mGridSize];
			for (int row = 0; row < this.mGridSize; row++) {
				for (int col = 0; col < this.mGridSize; col++) {
					mCageMatrix[row][col] = -1;
				}
			}

			if (mMaxCageSize >= CageTypeGenerator.MAX_CAGE_SIZE) {
				// Drop a first (bigger) cage type somewhere in the grid.
				int remaingAttemptsToPlaceBigCageType = 10;
				while (remaingAttemptsToPlaceBigCageType > 0) {
					GridCageType gridCageType = mGridCageTypeGenerator
							.getRandomCageType(mMaxCageSize, mGridSize,
									mGridSize, mRandom);
					if (gridCageType != null) {
						// Determine a random row and column at which the mask
						// will be placed. Use +1 in calls to randomizer to
						// prevent exceptions in case the entire height and/or
						// width is needed for the cagetype.
						int startRow = mRandom
								.nextInt((mGridSize - gridCageType.getHeight()) + 1);
						int startCol = mRandom
								.nextInt((mGridSize - gridCageType.getWidth()) + 1);

						// Determine the origin cell of the cage type in case
						// the cagetype mask is put at the randomly determined
						// position.
						int[] coordinatesTopLeft = gridCageType
								.getOriginCoordinates(startRow, startCol);

						// Get coordinates for the cage cells and add the cage.
						// Note: no checking is done on the maximum permutations
						// for the first cage.
						int[][] cageTypeCoords = gridCageType
								.getCellCoordinates(getCellAt(
										coordinatesTopLeft[0],
										coordinatesTopLeft[1]));
						GridCage firstCage = createCage(cageTypeCoords,
								4 * MAX_CAGE_PERMUTATIONS);
						if (firstCage != null) {
							this.mCages.add(firstCage);
							for (GridCell cellinCage : firstCage.mCells) {
								mCageMatrix[cellinCage.getRow()][cellinCage
										.getColumn()] = firstCage.mId;
							}
							break;
						}
					}

					// Try another time to drop a big cage type unless maximum
					// number of tries has been reached.
					remaingAttemptsToPlaceBigCageType--;
				}
			}

			// Fill remainder of grid
			int countSingles = 0;
			for (GridCell cell : this.mCells) {
				if (cell.cellInAnyCage()) {
					continue; // Cell already in a cage, skip
				}

				// Determine a random cage which will start at this cell.
				GridCage cage = selectRandomCageType(cell);
				if (cage.mCells.size() == 1) {
					countSingles++;
					if (countSingles > this.mGridSize / 2) {
						if (DEBUG_GRID_GENERATOR) {
							// Too many singles
							publishProgress(
									DevelopmentHelper.GRID_GENERATOR_PROGRESS_UPDATE_MESSAGE,
									"Too many single cells in attempt "
											+ attempts);
						}
						clearAllCages();
						restart = true;
						break;
					}
				}

				// Add the cage to the grid
				this.mCages.add(cage);
				for (GridCell cellinCage : cage.mCells) {
					mCageMatrix[cellinCage.getRow()][cellinCage.getColumn()] = cage.mId;
				}
			}
		} while (restart);

		if (DEBUG_GRID_GENERATOR) {
			printCageCreationDebugInformation(null);
		}
	}

	/**
	 * Create a new cage which originates at the given cell. The cage type for
	 * this cage will be randomly determined.
	 * 
	 * @param origin
	 *            The cell at which the cage originates.
	 * @return The selected grid cage type.
	 */
	private GridCage selectRandomCageType(GridCell origin) {
		if (DEBUG_GRID_GENERATOR_FULL) {
			Log.i(TAG, "Determine valid cages for cell[" + origin.getRow()
					+ "," + origin.getColumn() + "]");
		}

		// Store indexes of all defined cages types, except cage type 0 which is
		// a single cell, in a temporary list of available cages.
		ArrayList<Integer> availableCages = new ArrayList<Integer>();
		for (int i = 1; i < mGridCageTypeGenerator.size(mMaxCageSize); i++) {
			availableCages.add(i);
		}

		GridCageType selectedGridCageType;
		boolean cageIsValid;
		do {
			cageIsValid = true;

			// Randomly select any cage from the list of available cages. As
			// soon as a cage type is selected, it is removed from the list of
			// available cage types so it will not be selected again.
			int randomIndex = this.mRandom.nextInt(availableCages.size());
			int cageTypeToBeChecked = availableCages.get(randomIndex);
			availableCages.remove(randomIndex);
			selectedGridCageType = mGridCageTypeGenerator
					.getCageType(cageTypeToBeChecked);

			// Get coordinates of all cells involved when this cage type is
			// placed at this origin.
			int[][] cageTypeCoords = selectedGridCageType
					.getCellCoordinates(origin);

			// Build mask for this cage
			boolean[][] maskNewCage = new boolean[this.mGridSize][this.mGridSize];
			int[] maskNewCageRowCount = new int[this.mGridSize];
			int[] maskNewCageColCount = new int[this.mGridSize];
			for (int coord_num = 0; coord_num < cageTypeCoords.length; coord_num++) {
				int row = cageTypeCoords[coord_num][0];
				int col = cageTypeCoords[coord_num][1];

				if (row < 0 || row >= this.mGridSize || col < 0
						|| col >= this.mGridSize) {
					// Coordinates of this cell in cage falls outside the
					// grid.
					cageIsValid = false;
					break;
				} else if (mCageMatrix[row][col] >= 0) {
					// Cell is already used in another cage
					cageIsValid = false;
					break;
				} else {
					// Cell can be used for this new cage.
					maskNewCage[row][col] = true;
					maskNewCageRowCount[row]++;
					maskNewCageColCount[col]++;
				}
			}
			if (!cageIsValid) {
				continue;
			}

			if (DEBUG_GRID_GENERATOR_FULL) {
				// Print solution, cage matrix and makskNewCage
				printCageCreationDebugInformation(maskNewCage);
			}

			if (hasOverlappingSubsetOfValuesInColumns(maskNewCage,
					maskNewCageColCount)) {
				cageIsValid = false;
				continue;
			}

			if (hasOverlappingSubsetOfValuesInRows(maskNewCage,
					maskNewCageRowCount)) {
				cageIsValid = false;
				continue;
			}

			if (cageIsValid) {
				GridCage cage = createCage(cageTypeCoords,
						MAX_CAGE_PERMUTATIONS);
				if (cage == null) {
					// No cage created due to too many permutations.
					continue;
				}

				// As we randomly check available cages, we can stop as soon as
				// a valid cage is found which does fit on this position.
				return cage;
			}

			// Check next cage
		} while (availableCages.size() > 0);

		// No cage, other than a single cell, does fit on this position in the
		// grid.
		if (DEBUG_GRID_GENERATOR_FULL) {
			// Print solution, cage matrix and makskNewCage
			boolean[][] maskNewCage = new boolean[this.mGridSize][this.mGridSize];
			maskNewCage[origin.getRow()][origin.getColumn()] = true;
			printCageCreationDebugInformation(maskNewCage);
		}

		// Create the new cage for a single cell.
		return createCage(mGridCageTypeGenerator.getSingleCellCageType()
				.getCellCoordinates(origin), 0);
	}

	/**
	 * Create the cage at the given coordinates.
	 * 
	 * @param cageTypeCoords
	 *            The coordinates to be used for the cage.
	 * @param maxPermutations
	 *            The maximum permutations allowed to create the cage. Use 0 in
	 *            case no checking on the number of permutations needs to be
	 *            done.
	 * @return The grid cage which is created. Null in case the cage has too
	 *         many permutations.
	 */
	private GridCage createCage(int[][] cageTypeCoords, int maxPermutations) {
		GridCage cage = new GridCage(mGrid, mHideOperators);
		int newCageId = this.mCages.size();
		for (int coord_num = 0; coord_num < cageTypeCoords.length; coord_num++) {
			int row = cageTypeCoords[coord_num][0];
			int col = cageTypeCoords[coord_num][1];
			cage.mCells.add(getCellAt(row, col));
		}
		setArithmetic(cage);
		if (maxPermutations > 0
				&& cage.getPossibleNums().size() > maxPermutations) {
			// This cage has too many permutations which fulfill the
			// cage requirements. As this reduces the chance to find a
			// solution for the puzzle too much, the cage type will not
			// returned.
			if (DEBUG_GRID_GENERATOR_FULL) {
				Log.i(TAG, "This cage type has been rejected as it has more "
						+ "than " + maxPermutations + " initial "
						+ "permutations which fulfill the cage requirement.");
			}

			// Clear the cage result from the cage and the top left cell of the
			// cage.
			cage.clearCageResult();

			return null;
		}

		// Set cage id in cage (and indirectly in all cells in the cage as well)
		cage.setCageId(newCageId);

		return cage;
	}

	/**
	 * Print debug information for create cage process to logging.
	 * 
	 * @param maskNewCage
	 *            Mask of cage type which is currently processed.
	 */
	private void printCageCreationDebugInformation(boolean[][] maskNewCage) {
		Log.i(TAG, "   Checking cage type");
		String cageIdFormat = "%d";
		String emptyCell = ".";
		String usedCell = "X";
		if (this.mCages.size() > 100) {
			cageIdFormat = "%03d";
			emptyCell = "  .";
			usedCell = "  X";
		} else if (this.mCages.size() > 10) {
			cageIdFormat = "%02d";
			emptyCell = " .";
			usedCell = " X";
		}
		for (int row = 0; row < this.mGridSize; row++) {
			String line = "      ";
			for (int col = 0; col < this.mGridSize; col++) {
				line += " " + mSolutionMatrix[row][col];
			}
			line += "   ";
			for (int col = 0; col < this.mGridSize; col++) {
				line += " "
						+ (mCageMatrix[row][col] == -1 ? emptyCell : String
								.format(cageIdFormat, mCageMatrix[row][col]));
			}
			if (maskNewCage != null) {
				line += "   ";
				for (int col = 0; col < this.mGridSize; col++) {
					line += " "
							+ (maskNewCage[row][col] ? usedCell : emptyCell);
				}
			}
			Log.i(TAG, line);
		}
	}

	/**
	 * Determine whether the given new cage contains a subset of values in its
	 * columns which is also used in the columns of another cage.
	 * 
	 * @param maskNewCage
	 *            A mask of the new cage. Cells which are in use by this cage
	 *            have value 1. Cells not used have a value -1.
	 * @param maskNewCageColCount
	 *            The number of rows per column in use by this new cage.
	 * @return
	 */
	private boolean hasOverlappingSubsetOfValuesInColumns(
			boolean[][] maskNewCage, int[] maskNewCageColCount) {
		for (int newCageCol = 0; newCageCol < this.mGridSize; newCageCol++) {
			if (maskNewCageColCount[newCageCol] > 1) {
				// This column in the new cage has more than one row and
				// therefore needs to be checked with columns of other cages.

				// Compare the column in which the new cage is placed with cages
				// in other columns of the grid.
				for (int col = 0; col < this.mGridSize; col++) {
					if (col != newCageCol) {

						// Cages which are already checked during processing of
						// this column of the new cage, can be skipped.
						ArrayList<Integer> cagesChecked = new ArrayList<Integer>();

						// Iterate all cells in the column from top to bottom.
						for (int row = 0; row < this.mGridSize; row++) {
							int otherCageId = mCageMatrix[row][col];
							if (otherCageId >= 0
									&& maskNewCage[row][newCageCol]
									&& !cagesChecked.contains(otherCageId)) {
								// Cell[row][col] is used in a cage which is not
								// yet checked. This is the first row for which
								// the new cage and the other cage has a cell in
								// the columns which are compared.
								cagesChecked.add(otherCageId);

								// Check all remaining rows if the checked
								// columns contain a cell for the new cage and
								// the other cage.
								int[] valuesUsed = new int[this.mGridSize];
								for (int row2 = row; row2 < this.mGridSize; row2++) {
									if (mCageMatrix[row2][col] == otherCageId
											&& maskNewCage[row2][newCageCol]) {
										// Both cages contain a cell on the same
										// row. Remember values used in those
										// cells.
										valuesUsed[mSolutionMatrix[row2][col] - 1]++;
										valuesUsed[mSolutionMatrix[row2][newCageCol] - 1]++;
									}
								}

								// Determine which values are used in both cages
								ArrayList<Integer> duplicateValues = new ArrayList<Integer>();
								for (int i = 0; i < this.mGridSize; i++) {
									if (valuesUsed[i] > 1) {
										// Value (i+1) has been used in both
										// columns of the new cage and the other
										// cage.
										duplicateValues.add(i + 1);
									}
								}
								if (duplicateValues.size() > 1) {
									// At least two values have been found which
									// are used in both columns of the new cage
									// and the other cage. As this would result
									// in a non-unique solution, the cage is not
									// valid.
									if (DEBUG_GRID_GENERATOR_FULL) {
										Log.i(TAG,
												"         This cage type will result in a "
														+ "non-unique solution. The new cage "
														+ "contains values "
														+ duplicateValues
																.toString()
														+ " in column "
														+ newCageCol
														+ " which are also used in column "
														+ col + " within cage "
														+ otherCageId + ".");
									}
									return true;
								}
							}
						}
					}
				}
			}
		}

		// No overlapping subset found
		return false;
	}

	/**
	 * Determine whether the given new cage contains a subset of values in its
	 * rows which is also used in the rows of another cage.
	 * 
	 * @param maskNewCage
	 *            A mask of the new cage. Cells which are in use by this cage
	 *            have value 1. Cells not used have a value -1.
	 * @param maskNewCageRowCount
	 *            The number of columns per row in use by this new cage.
	 * @return
	 */
	private boolean hasOverlappingSubsetOfValuesInRows(boolean[][] maskNewCage,
			int[] maskNewCageRowCount) {
		for (int newCageRow = 0; newCageRow < this.mGridSize; newCageRow++) {
			if (maskNewCageRowCount[newCageRow] > 1) {
				// This row in the new cage has more than one column and
				// therefore needs to be checked with rows of other cages.

				// Compare the row in which the new cage is placed with cages
				// in other rows of the grid.
				for (int row = 0; row < this.mGridSize; row++) {
					if (row != newCageRow) {

						// Cages which are already checked during processing of
						// this row of the new cage, can be skipped.
						ArrayList<Integer> cagesChecked = new ArrayList<Integer>();

						// Iterate all cells in the row from left to right.
						for (int col = 0; col < this.mGridSize; col++) {
							int otherCageId = mCageMatrix[row][col];
							if (otherCageId >= 0
									&& maskNewCage[newCageRow][col]
									&& !cagesChecked.contains(otherCageId)) {
								// Cell[row][col] is used in a cage which is not
								// yet checked. This is the first column for
								// which
								// the new cage and the other cage has a cell in
								// the rows which are compared.
								cagesChecked.add(otherCageId);

								// Check all remaining columns if the checked
								// rows contain a cell for the new cage and
								// the other cage.
								int[] valuesUsed = new int[this.mGridSize];
								for (int cols2 = col; cols2 < this.mGridSize; cols2++) {
									if (mCageMatrix[row][cols2] == otherCageId
											&& maskNewCage[newCageRow][cols2]) {
										// Both cages contain a cell on the same
										// columns. Remember values used in
										// those
										// cells.
										valuesUsed[mSolutionMatrix[row][cols2] - 1]++;
										valuesUsed[mSolutionMatrix[newCageRow][cols2] - 1]++;
									}
								}

								// Determine which values are used in both cages
								ArrayList<Integer> duplicateValues = new ArrayList<Integer>();
								for (int i = 0; i < this.mGridSize; i++) {
									if (valuesUsed[i] > 1) {
										// Value (i+1) has been used in both
										// columns of the new cage and the other
										// cage.
										duplicateValues.add(i + 1);
									}
								}
								if (duplicateValues.size() > 1) {
									// At least two values have been found which
									// are used in both rows of the new cage
									// and the other cage. As this would result
									// in a non-unique solution, the cage is not
									// valid.
									if (DEBUG_GRID_GENERATOR_FULL) {
										Log.i(TAG,
												"         This cage type will result in a "
														+ "non-unique solution. The new cage "
														+ "contains values "
														+ duplicateValues
																.toString()
														+ " in row "
														+ newCageRow
														+ " which are also used in row "
														+ row + " within cage "
														+ otherCageId + ".");
									}
									return true;
								}
							}
						}
					}
				}
			}
		}

		// No overlapping subset found
		return false;
	}

	/**
	 * Generates the arithmetic for the cage, semi-randomly.
	 * 
	 * @param cage
	 *            The cage for which the arithmetic has to be generated.
	 */
	private void setArithmetic(GridCage cage) {
		// A cage consisting of one single cell has no operator.
		if (cage.mCells.size() == 1) {
			// Single cell cage have an empty operator which is never hidden. IN
			// this way it can be prevented that for a single cage cell it
			// operator can be revealed using the context menu.
			cage.setCageResults(cage.mCells.get(0).getCorrectValue(),
					GridCage.ACTION_NONE, false);
			return;
		}

		// For cages of size 2 and bigger a weight (i.e. the chance on choosing
		// that operator) will be determined.
		int divisionWeight;
		int subtractionWeight;
		int addWeight;
		int multiplyWeight;
		int divisionCageResult = -1;
		int subtractionCageResult = -1;

		// A cage consisting of two cells can have any operator but we give
		// divide and subtraction a little extra weight because those operators
		// will not be used in bigger cages. Of course division can not always
		// be used.
		if (cage.mCells.size() == 2) {
			int higher;
			int lower;
			if (cage.mCells.get(0).getCorrectValue() > cage.mCells.get(1)
					.getCorrectValue()) {
				higher = cage.mCells.get(0).getCorrectValue();
				lower = cage.mCells.get(1).getCorrectValue();
			} else {
				higher = cage.mCells.get(1).getCorrectValue();
				lower = cage.mCells.get(0).getCorrectValue();
			}
			// As division is less often possible compared to subtraction, it is
			// given a bit more weight.
			divisionWeight = ((higher % lower == 0) ? 50 : 0);
			subtractionWeight = 30;
			addWeight = 15;
			multiplyWeight = 15;
			// Also calculate the cage results for division and subtraction
			divisionCageResult = higher / lower;
			subtractionCageResult = higher - lower;
		} else {
			// Cage has three or more cells. Division and substration are not
			// allowed as operators.
			divisionWeight = 0;
			subtractionWeight = 0;
			addWeight = 50;
			multiplyWeight = 50;
		}

		// Determine a random number in the range of the total weight of the
		// operator available.
		int totalWeight = divisionWeight + subtractionWeight + addWeight
				+ multiplyWeight;
		double index = mRandom.nextInt(totalWeight);

		// Check whether the division operator has to be applied
		if (index < divisionWeight) {
			cage.setCageResults(divisionCageResult, GridCage.ACTION_DIVIDE,
					mHideOperators);
			return;
		}
		index -= divisionWeight;

		// Check whether the subtraction operator has to be applied
		if (index < subtractionWeight) {
			cage.setCageResults(subtractionCageResult,
					GridCage.ACTION_SUBTRACT, mHideOperators);
			return;
		}
		index -= subtractionWeight;

		// Check whether the multiply operator has to and can be applied. If
		// not, than
		// add is chosen.
		if (index < multiplyWeight) {
			int total = 1;
			for (GridCell cell : cage.mCells) {
				total *= cell.getCorrectValue();
			}
			if (total <= mMaxCageResult) {
				cage.setCageResults(total, GridCage.ACTION_MULTIPLY,
						mHideOperators);
				return;
			}
			Log.i(TAG, "GameSeed: " + mGameSeed + " cage result " + total
					+ " is rejected");
			// Multplication leads to a cage value that is too big to be
			// displayed on this device. For small screens this value is set to
			// 9,999. For bigger screens the value is 99,999. Instead of
			// multiplication the add operator will be used for this cage.
		}

		// Use ADD in all other cases.
		int total = 0;
		for (GridCell cell : cage.mCells) {
			total += cell.getCorrectValue();
		}
		cage.setCageResults(total, GridCage.ACTION_ADD, mHideOperators);
	}

	private void clearAllCages() {
		for (GridCell cell : this.mCells) {
			cell.clearCage();
		}
		this.mCages = new ArrayList<GridCage>();
	}

	/* Clear any cells containing the given number. */
	private void clearValue(int value) {
		for (GridCell cell : this.mCells)
			if (cell.getCorrectValue() == value)
				cell.setCorrectValue(0);
	}

	/* Determine if the given value is in the given column */
	private boolean valueInColumn(int column, int value) {
		for (int row = 0; row < mGridSize; row++)
			if (this.mCells.get(column + row * mGridSize).getCorrectValue() == value)
				return true;
		return false;
	}

	/* Fetch the cell at the given row, column */
	private GridCell getCellAt(int row, int column) {
		if (row < 0 || row >= mGridSize)
			return null;
		if (column < 0 || column >= mGridSize)
			return null;

		return mCells.get(column + row * mGridSize);
	}
}