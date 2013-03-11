package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.Random;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.srlee.DLX.DLX.SolveType;
import com.srlee.DLX.MathDokuDLX;

/**
 * Generates a new grid while displaying a progress dialog.
 */
public class GridGenerator extends AsyncTask<Void, Integer, Void> {
	private static final String TAG = "MathDoku.GridGenerator";

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG_CREATE_CAGES = (DevelopmentHelper.mode == Mode.DEVELOPMENT) && true;

	// The grid created by the generator
	private Grid mGrid;

	private boolean mHideOperators;
	private MainActivity mActivity;
	private ProgressDialog mProgressDialog;

	// Random generator
	public Random mRandom;
	private long mGameSeed;

	// Size of the grid
	public int mGridSize;

	// Cell and solution
	public ArrayList<GridCell> mCells;
	private int[][] solutionMatrix;

	// Cages
	private CageTypeGenerator mGridCageTypeGenerator;
	public ArrayList<GridCage> mCages;
	private int[][] cageMatrix;

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
	public GridGenerator(MainActivity activity, int gridSize,
			boolean hideOperators) {
		this.mGridSize = gridSize;
		this.mHideOperators = hideOperators;

		// Attach the task to the activity activity and show progress dialog if
		// needed.
		attachToActivity(activity);
	}

	/**
	 * Attaches the activity to the ASync task.
	 * 
	 * @param activity
	 *            The activity to which results will be sent on completion of
	 *            this task.
	 */
	public void attachToActivity(MainActivity activity) {
		// Remember the activity that started this task.
		this.mActivity = activity;

		// Build the dialog
		mProgressDialog = new ProgressDialog(activity);
		mProgressDialog.setTitle(R.string.main_ui_building_puzzle_title);
		mProgressDialog.setMessage(activity.getResources().getString(
				R.string.main_ui_building_puzzle_message));
		mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		reCreate();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Void result) {
		if (this.mProgressDialog != null) {
			dismissProgressDialog();
		}

		// Load results into the grid.
		mGrid.create(mGameSeed, mGridSize, mCells, mCages, true);

		if (mActivity != null) {
			// The task is still attached to a activity. Send new grid to
			// activity.
			mActivity.onNewGridReady(mGrid);
		} else {
			// The activity is no longer available.

			// Store the game so it can be played next time the activity is
			// started.
			new GameFile("newGame").save(mGrid);
		}

		super.onPostExecute(result);
	}

	/**
	 * Detaches the activity form the ASyn task. The progress dialog which was
	 * shown will be dismissed. The ASync task however still keeps running until
	 * finished.
	 */
	public void detachFromActivity() {
		dismissProgressDialog();
		mActivity = null;
	}

	/**
	 * Dismisses the progress dialog which was shown on start of this ASync
	 * task. The ASync task however still keeps running until finished.
	 */
	public void dismissProgressDialog() {
		mProgressDialog.dismiss();
		mProgressDialog = null;
	}

	public void reCreate() {
		// Create a new empty grid.
		mGrid = new Grid(mGridSize);

		int num_solns;
		int num_attempts = 0;

		// Generate a random seed. This seed will be used to for another
		// Randomizer which will be used to generate the game. By saving and
		// displaying the seed as game number, it should be possible to
		// recreate a game (as long as the implementation of the Randomize
		// has not changed).
		mGameSeed = (new Random()).nextLong();
		mRandom = new Random(mGameSeed);

		if (DEBUG_CREATE_CAGES) {
			Log.i(TAG, "Game seed: " + mGameSeed);
		}

		if (mGridSize < 4)
			return;
		do {
			mCells = new ArrayList<GridCell>();
			int cellnum = 0;
			for (int i = 0; i < mGridSize * mGridSize; i++) {
				mCells.add(new GridCell(mGrid, cellnum++));
			}
			randomiseGrid();
			this.mCages = new ArrayList<GridCage>();
			CreateCages(mHideOperators);
			num_attempts++;

			MathDokuDLX mdd = new MathDokuDLX(this.mGridSize, this.mCages);
			// Stop solving as soon as we find multiple solutions
			num_solns = mdd.Solve(SolveType.MULTIPLE);
			Log.d("MathDoku", "Num Solns = " + num_solns);
		} while (num_solns > 1);
		Log.d("MathDoku", "Num Attempts = " + num_attempts);
	}

	/*
	 * Fills the grid with random numbers, per the rules:
	 * 
	 * - 1 to <rowsize> on every row and column - No duplicates in any row or
	 * column.
	 */
	public void randomiseGrid() {
		int attempts;
		solutionMatrix = new int[this.mGridSize][this.mGridSize];
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
				solutionMatrix[row][column] = value;
				// Log.d("KenKen", "New cell: " + cell);
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
	public void CreateCages(boolean hideOperators) {

		// Create all possible grid cage types which can be used in this new
		// grid.
		if (this.mGridCageTypeGenerator == null) {
			// Maxsize can be set to any number but it takes a (very) long time
			// to create a new puzzle due to exponential growth of number of
			// possible cage types. Also it seems to get very evil to play the
			// game. Further testing needs to be done how much maxCageSize does
			// influence difficulty of puzzle. If so, it can be added as an
			// option to the player to wait longer for even more difficult
			// puzzles ;-)
			int maxCageSize = (this.mGridSize > 5 ? 5 : this.mGridSize);
			this.mGridCageTypeGenerator = new CageTypeGenerator(maxCageSize);
		}

		boolean restart;

		do {
			restart = false;
			cageMatrix = new int[this.mGridSize][this.mGridSize];
			for (int row = 0; row < this.mGridSize; row++) {
				for (int col = 0; col < this.mGridSize; col++) {
					cageMatrix[row][col] = -1;
				}
			}

			int cageId = 0;
			int countSingles = 0;
			for (GridCell cell : this.mCells) {
				if (cell.CellInAnyCage()) {
					continue; // Cell already in a cage, skip
				}

				// Determine a random cage type which will start at this cell.
				GridCageType cageType = selectRandomCageType(cell);
				if (cageType.size() == 1) {
					countSingles++;
					if (countSingles > this.mGridSize / 2) {
						// Too many singles
						Log.i(" xx", " Too many single cells");
						ClearAllCages();
						restart = true;
						break;
					}
				}

				// Add the cage to the grid.
				int[][] cage_coords = cageType.getCellCoordinates(cell);
				GridCage cage = new GridCage(mGrid, hideOperators);
				for (int coord_num = 0; coord_num < cage_coords.length; coord_num++) {
					int row = cage_coords[coord_num][0];
					int col = cage_coords[coord_num][1];
					cage.mCells.add(getCellAt(row, col));
					cageMatrix[row][col] = cageId;
				}
				setArithmetic(cage); // Make the maths puzzle
				cage.setCageId(cageId++); // Set cage's id
				this.mCages.add(cage); // Add to the cage list

			}
		} while (restart);
	}

	/**
	 * Selects a cage type which does fit on the given origin.
	 * 
	 * @param origin
	 *            The origin cell at which the cage type will start.
	 * @return The selected grid cage type.
	 */
	public GridCageType selectRandomCageType(GridCell origin) {
		if (DEBUG_CREATE_CAGES) {
			Log.i(TAG, "Determine valid cages for cell[" + origin.getRow()
					+ "," + origin.getColumn() + "]");
		}

		// Store indexes of all defined cages types, except cage type 0 which is
		// a single cell, in a temporary list of available cages.
		ArrayList<Integer> availableCages = new ArrayList<Integer>();
		for (int i = 1; i < mGridCageTypeGenerator.size(); i++) {
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
				} else if (cageMatrix[row][col] >= 0) {
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

			if (cageIsValid) {
				// As we randomly check available cages, we can stop as soon
				// as
				// a valid cage is found which does fit on this position.
				return selectedGridCageType;
			}

			if (DEBUG_CREATE_CAGES) {
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
				// As we randomly check available cages, we can stop as soon as
				// a valid cage is found which does fit on this position.
				return selectedGridCageType;
			}

			// Check next cage
		} while (availableCages.size() > 0);

		// No cage, other than a single cell, does fit on this position in the
		// grid.
		if (DEBUG_CREATE_CAGES) {
			// Print solution, cage matrix and makskNewCage
			boolean[][] maskNewCage = new boolean[this.mGridSize][this.mGridSize];
			maskNewCage[origin.getRow()][origin.getColumn()] = true;
			printCageCreationDebugInformation(maskNewCage);
		}
		return mGridCageTypeGenerator.getSingleCellCageType();
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
				line += " " + solutionMatrix[row][col];
			}
			line += "   ";
			for (int col = 0; col < this.mGridSize; col++) {
				line += " "
						+ (cageMatrix[row][col] == -1 ? emptyCell : String
								.format(cageIdFormat, cageMatrix[row][col]));
			}
			line += "   ";
			for (int col = 0; col < this.mGridSize; col++) {
				line += " " + (maskNewCage[row][col] ? usedCell : emptyCell);
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
							int otherCageId = cageMatrix[row][col];
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
									if (cageMatrix[row2][col] == otherCageId
											&& maskNewCage[row2][newCageCol]) {
										// Both cages contain a cell on the same
										// row. Remember values used in those
										// cells.
										valuesUsed[solutionMatrix[row2][col] - 1]++;
										valuesUsed[solutionMatrix[row2][newCageCol] - 1]++;
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
									if (DEBUG_CREATE_CAGES) {
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
							int otherCageId = cageMatrix[row][col];
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
									if (cageMatrix[row][cols2] == otherCageId
											&& maskNewCage[newCageRow][cols2]) {
										// Both cages contain a cell on the same
										// columns. Remember values used in
										// those
										// cells.
										valuesUsed[solutionMatrix[row][cols2] - 1]++;
										valuesUsed[solutionMatrix[newCageRow][cols2] - 1]++;
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
									if (DEBUG_CREATE_CAGES) {
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

	/*
	 * Generates the arithmetic for the cage, semi-randomly.
	 * 
	 * - If a cage has 3 or more cells, it can only be an add or multiply. -
	 * else if the cells are evenly divisible, division is used, else
	 * subtraction.
	 */
	public void setArithmetic(GridCage cage) {
		if (cage.mCells.size() == 1) {
			// Single cell cage have an empty operator which is never hidden. IN
			// this way it can be prevented that for a single cage cell it
			// operator can be revealed using the context menu.
			cage.setCageResults(cage.mCells.get(0).getCorrectValue(),
					GridCage.ACTION_NONE, false);
			return;
		}

		double rand = mRandom.nextDouble();
		double addChance = 0.25;
		double multChance = 0.5;
		if (cage.mCells.size() > 2) {
			addChance = 0.5;
			multChance = 1.0;
		}
		if (rand <= addChance) {
			int total = 0;
			for (GridCell cell : cage.mCells) {
				total += cell.getCorrectValue();
			}
			cage.setCageResults(total, GridCage.ACTION_ADD, mHideOperators);
			return;
		} else if (rand <= multChance) {
			int total = 1;
			for (GridCell cell : cage.mCells) {
				total *= cell.getCorrectValue();
			}
			cage.setCageResults(total, GridCage.ACTION_MULTIPLY, mHideOperators);
			return;
		}

		if (cage.mCells.size() < 2) {
			Log.d("KenKen", "Why only length 1? Type: " + this);
		}
		int cell1Value = cage.mCells.get(0).getCorrectValue();
		int cell2Value = cage.mCells.get(1).getCorrectValue();
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
			cage.setCageResults(higher / lower, GridCage.ACTION_DIVIDE,
					mHideOperators);
		} else {
			cage.setCageResults(higher - lower, GridCage.ACTION_SUBTRACT,
					mHideOperators);
		}
	}

	public void ClearAllCages() {
		for (GridCell cell : this.mCells) {
			cell.clearCage();
		}
		this.mCages = new ArrayList<GridCage>();
	}

	/* Clear any cells containing the given number. */
	public void clearValue(int value) {
		for (GridCell cell : this.mCells)
			if (cell.getCorrectValue() == value)
				cell.setCorrectValue(0);
	}

	/* Determine if the given value is in the given row */
	public boolean valueInRow(int row, int value) {
		for (GridCell cell : this.mCells)
			if (cell.getRow() == row && cell.getCorrectValue() == value)
				return true;
		return false;
	}

	/* Determine if the given value is in the given column */
	public boolean valueInColumn(int column, int value) {
		for (int row = 0; row < mGridSize; row++)
			if (this.mCells.get(column + row * mGridSize).getCorrectValue() == value)
				return true;
		return false;
	}

	/* Fetch the cell at the given row, column */
	public GridCell getCellAt(int row, int column) {
		if (row < 0 || row >= mGridSize)
			return null;
		if (column < 0 || column >= mGridSize)
			return null;

		return mCells.get(column + row * mGridSize);
	}
}
