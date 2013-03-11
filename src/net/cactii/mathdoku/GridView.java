package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.Random;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.srlee.DLX.DLX.SolveType;
import com.srlee.DLX.MathDokuDLX;

public class GridView extends View implements OnTouchListener {
	private static final String TAG = "MathDoku.GridView";

	// Identifiers of different versions of grid view information which is
	// stored in a saved game. In the initial versions, view information was
	// stored on several distinct lines. As from now all view variables are
	// collected on single line as well.
	public static final String SAVE_GAME_GRID_VERSION_01 = "VIEW.v1";

	public static final int THEME_CARVED = 0;
	public static final int THEME_NEWSPAPER = 1;
	public static final int THEME_INVERT = 2;

	// Remove "&& false" in following line to show debug information about
	// creating cages when running in development mode.
	public static final boolean DEBUG_CREATE_CAGES = (DevelopmentHelper.mode == Mode.DEVELOPMENT) && false;

	// Solved listener
	private OnSolvedListener mSolvedListener;
	// Touched listener
	public OnGridTouchListener mTouchedListener;

	// Size of the grid
	public int mGridSize;

	// Random generator
	public Random mRandom;
	private long mGameSeed;

	public Activity mContext;

	// Cages
	private CageTypeGenerator mGridCageTypeGenerator;
	public ArrayList<GridCage> mCages;
	private int[][] cageMatrix;

	// Cell and solution
	public ArrayList<GridCell> mCells;
	private int[][] solutionMatrix;

	private boolean mCheated;

	public boolean mActive;

	public boolean mSelectorShown = false;

	public float mTrackPosX;
	public float mTrackPosY;

	public GridCell mSelectedCell;

	public TextView animText;

	public int mCurrentWidth;
	public Paint mGridPaint;
	public Paint mBorderPaint;
	public int mBackgroundColor;

	public Typeface mFace;
	public boolean mDupedigits;
	public boolean mBadMaths;

	// Date of current game (used for saved games)
	public long mDate;
	// Epalsed time
	public long mElapsed;
	// Current theme
	public int mTheme;

	// Keep track of all moves as soon as grid is built or restored.
	private ArrayList<CellChange> moves;

	// Used to avoid redrawing or saving grid during creation of new grid
	public final Object mLock = new Object();

	public GridView(Context context) {
		super(context);
		initGridView();
	}

	public GridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGridView();
	}

	public GridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGridView();
	}

	public void initGridView() {

		this.mSolvedListener = null;
		this.mDupedigits = true;
		this.mBadMaths = true;

		this.mGridPaint = new Paint();
		this.mGridPaint.setColor(0x80000000);
		this.mGridPaint.setStrokeWidth(0);
		// this.mGridPaint.setPathEffect(new DashPathEffect(new float[] {2, 2},
		// 0));

		this.mBorderPaint = new Paint();
		this.mBorderPaint.setColor(0xFF000000);
		this.mBorderPaint.setStrokeWidth(3);
		this.mBorderPaint.setStyle(Style.STROKE);

		this.mCurrentWidth = 0;
		this.mGridSize = 0;
		this.mActive = false;

		this.setOnTouchListener((OnTouchListener) this);
	}

	public void setTheme(int theme) {
		if (theme == THEME_CARVED) {
			this.mGridPaint.setAntiAlias(true);
			this.mGridPaint.setPathEffect(new DiscretePathEffect(20, 1));
			this.mGridPaint.setColor(0xbf906050);
			this.mBorderPaint.setAntiAlias(true);
			this.mBorderPaint.setPathEffect(new DiscretePathEffect(30, 1));
			this.mBorderPaint.setColor(0xff000000);
			this.mBackgroundColor = 0x7ff0d090;
		} else if (theme == THEME_NEWSPAPER) {
			this.mGridPaint.setPathEffect(new DashPathEffect(
					new float[] { 2, 2 }, 0));
			this.mBorderPaint.setAntiAlias(false);
			this.mBorderPaint.setPathEffect(null);
			this.mBorderPaint.setColor(0xff000000);
			this.mBackgroundColor = 0xffffffff;
		} else if (theme == THEME_INVERT) {
			this.mGridPaint.setColor(0xff7f7f7f);
			this.mGridPaint.setPathEffect(new DashPathEffect(
					new float[] { 2, 2 }, 0));
			this.mBorderPaint.setPathEffect(new DiscretePathEffect(30, 1));

			this.mBorderPaint.setPathEffect(null);
			this.mBorderPaint.setColor(0xffe0e0e0);
			this.mBackgroundColor = 0xff000000;
		}
		if (this.getMeasuredHeight() < 150)
			this.mBorderPaint.setStrokeWidth(1);
		else
			this.mBorderPaint.setStrokeWidth(3);

		if (this.mCells != null)
			for (GridCell cell : this.mCells)
				cell.setTheme(theme);
	}

	public void reCreate(boolean hideOperators) {
		synchronized (mLock) { // Avoid redrawing at the same time as creating
								// puzzle

			int num_solns;
			int num_attempts = 0;

			this.mCheated = false;

			// Generate a random seed. This seed will be used to for another
			// Randomizer which will be used to generate the game. By saving and
			// displaying the seed as game number, it should be possible to
			// recreate a game (as long as the implementation of the Randomize
			// has not changed).
			this.mGameSeed = (new Random()).nextLong();
			this.mRandom = new Random(this.mGameSeed);

			if (DEBUG_CREATE_CAGES) {
				Log.i(TAG, "Game seed: " + this.mGameSeed);
			}

			if (this.mGridSize < 4)
				return;
			do {
				this.mCells = new ArrayList<GridCell>();
				this.mSelectedCell = null;
				int cellnum = 0;
				for (int i = 0; i < this.mGridSize * this.mGridSize; i++)
					this.mCells.add(new GridCell(this, cellnum++));
				randomiseGrid();
				this.mTrackPosX = this.mTrackPosY = 0;
				this.mCages = new ArrayList<GridCage>();
				CreateCages(hideOperators);
				num_attempts++;
				MathDokuDLX mdd = new MathDokuDLX(this.mGridSize, this.mCages);
				// Stop solving as soon as we find multiple solutions
				num_solns = mdd.Solve(SolveType.MULTIPLE);
				Log.d("MathDoku", "Num Solns = " + num_solns);
			} while (num_solns > 1);
			Log.d("MathDoku", "Num Attempts = " + num_attempts);
			this.mActive = true;
			this.mSelectorShown = false;
			this.setTheme(this.mTheme);
		}
	}

	// Returns cage id of cell at row, column
	// Returns -1 if not a valid cell or cage
	public int CageIdAt(int row, int column) {
		if (row < 0 || row >= mGridSize || column < 0 || column >= mGridSize)
			return -1;
		return this.mCells.get(column + row * this.mGridSize).getCageId();
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
				GridCage cage = new GridCage(this, hideOperators);
				for (int coord_num = 0; coord_num < cage_coords.length; coord_num++) {
					int row = cage_coords[coord_num][0];
					int col = cage_coords[coord_num][1];
					cage.mCells.add(getCellAt(row, col));
					cageMatrix[row][col] = cageId;
				}
				cage.setArithmetic(); // Make the maths puzzle
				cage.setCageId(cageId++); // Set cage's id
				this.mCages.add(cage); // Add to the cage list

			}
		} while (restart);
		for (GridCage cage : this.mCages) {
			cage.setBorders();
		}
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

	public void ClearAllCages() {
		for (GridCell cell : this.mCells) {
			cell.clearCage();
		}
		this.mCages = new ArrayList<GridCage>();
	}

	public void clearUserValues() {
		if (this.moves != null) {
			this.moves.clear();
		}
		if (mCells != null) {
			for (GridCell cell : this.mCells) {
				cell.clearUserValue();
			}
		}
		this.invalidate();
	}

	/**
	 * Clear this view so a new game can be restored.
	 */
	public void clear() {
		if (this.moves != null) {
			this.moves.clear();
		}
		if (this.mCells != null) {
			this.mCells.clear();
		}
		if (this.mCages != null) {
			this.mCages.clear();
		}
		mSelectedCell = null;
	}

	/* Fetch the cell at the given row, column */
	public GridCell getCellAt(int row, int column) {
		if (row < 0 || row >= mGridSize)
			return null;
		if (column < 0 || column >= mGridSize)
			return null;

		return this.mCells.get(column + row * this.mGridSize);
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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Our target grid is a square, measuring 80% of the minimum dimension
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);

		int dim = Math.min(measuredWidth, measuredHeight);

		setMeasuredDimension(dim, dim);
	}

	private int measure(int measureSpec) {

		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.UNSPECIFIED)
			return 180;
		else
			return (int) (specSize);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		synchronized (mLock) { // Avoid redrawing at the same time as creating
								// puzzle
			if (this.mGridSize < 4)
				return;
			if (this.mCages == null)
				return;

			int width = getMeasuredWidth();

			if (width != this.mCurrentWidth)
				this.mCurrentWidth = width;

			// Fill canvas background
			canvas.drawColor(this.mBackgroundColor);

			// Check cage correctness
			for (GridCage cage : this.mCages)
				cage.userValuesCorrect();

			// Draw (dashed) grid
			for (int i = 1; i < this.mGridSize; i++) {
				float pos = ((float) this.mCurrentWidth / (float) this.mGridSize)
						* i;
				canvas.drawLine(0, pos, this.mCurrentWidth, pos,
						this.mGridPaint);
				canvas.drawLine(pos, 0, pos, this.mCurrentWidth,
						this.mGridPaint);
			}

			// Draw cells
			for (GridCell cell : this.mCells) {
				if ((cell.isUserValueSet() && this.getNumValueInCol(cell) > 1)
						|| (cell.isUserValueSet() && this
								.getNumValueInRow(cell) > 1))
					cell.mShowWarning = true;
				else
					cell.mShowWarning = false;
				cell.onDraw(canvas, false);
			}

			// Draw borders
			canvas.drawLine(0, 1, this.mCurrentWidth, 1, this.mBorderPaint);
			canvas.drawLine(1, 0, 1, this.mCurrentWidth, this.mBorderPaint);
			canvas.drawLine(0, this.mCurrentWidth - 2, this.mCurrentWidth,
					this.mCurrentWidth - 2, this.mBorderPaint);
			canvas.drawLine(this.mCurrentWidth - 2, 0, this.mCurrentWidth - 2,
					this.mCurrentWidth, this.mBorderPaint);

			// Draw cells again
			for (GridCell cell : this.mCells) {
				cell.onDraw(canvas, true);
			}
			// Draw highlights for current cage.
			if (this.mSelectedCell != null
					&& this.mSelectedCell.getCageId() < this.mCages.size()) {
				for (GridCell cell : this.mCages.get(this.mSelectedCell
						.getCageId()).mCells) {
					cell.onDraw(canvas, true);
				}
				// Draws highlights at top.
				this.mSelectedCell.onDraw(canvas, false);
			}
			// Callback if puzzle is solved.
			if (this.mActive && this.isSolved()) {
				if (this.mSolvedListener != null)
					this.mSolvedListener.puzzleSolved();
				if (this.mSelectedCell != null)
					this.mSelectedCell.mSelected = false;
				this.mActive = false;
			}
		}
	}

	// Given a cell number, returns origin x,y coordinates.
	private float[] CellToCoord(int cell) {
		float xOrd;
		float yOrd;
		int cellWidth = this.mCurrentWidth / this.mGridSize;
		xOrd = ((float) cell % this.mGridSize) * cellWidth;
		yOrd = ((int) (cell / this.mGridSize) * cellWidth);
		return new float[] { xOrd, yOrd };
	}

	// Opposite of above - given a coordinate, returns the cell number within.
	private GridCell CoordToCell(float x, float y) {
		int row = (int) ((y / (float) this.mCurrentWidth) * this.mGridSize);
		int col = (int) ((x / (float) this.mCurrentWidth) * this.mGridSize);
		// Log.d("KenKen", "Track x/y = " + col + " / " + row);
		return getCellAt(row, col);
	}

	public boolean onTouch(View arg0, MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return false;
		if (!this.mActive)
			return false;

		// Find out where the grid was touched.
		float x = event.getX();
		float y = event.getY();
		int size = getMeasuredWidth();

		int row = (int) ((size - (size - y)) / (size / this.mGridSize));
		if (row > this.mGridSize - 1)
			row = this.mGridSize - 1;
		if (row < 0)
			row = 0;

		int col = (int) ((size - (size - x)) / (size / this.mGridSize));
		if (col > this.mGridSize - 1)
			col = this.mGridSize - 1;
		if (col < 0)
			col = 0;

		// We can now get the cell.
		GridCell cell = getCellAt(row, col);
		float[] cellPos = this.CellToCoord(cell.getCellNumber());
		this.mTrackPosX = cellPos[0];
		this.mTrackPosY = cellPos[1];

		if (this.mSelectedCell != cell) {
			// Another cell was touched
			this.playSoundEffect(SoundEffectConstants.CLICK);
			SetSelectedCell(cell);
		}
		if (this.mTouchedListener != null) {
			this.mTouchedListener.gridTouched(cell);
		}
		invalidate();

		return false;
	}

	// Handle trackball, both press down, and scrolling around to
	// select a cell.
	public boolean onTrackballEvent(MotionEvent event) {
		if (!this.mActive || this.mSelectorShown)
			return false;
		// On press event, take selected cell, call touched listener
		// which will popup the digit selector.
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.mTouchedListener != null) {
				this.mSelectedCell.mSelected = true;
				this.mTouchedListener.gridTouched(this.mSelectedCell);
			}
			return true;
		}
		// A multiplier amplifies the trackball event values
		int trackMult = 70;
		switch (this.mGridSize) {
		case 5:
			trackMult = 60;
			break;
		case 6:
			trackMult = 50;
			break;
		case 7:
			trackMult = 40;
			break;
		case 8:
			trackMult = 40;
		}
		// Fetch the trackball position, work out the cell it's at
		float x = event.getX();
		float y = event.getY();
		this.mTrackPosX += x * trackMult;
		this.mTrackPosY += y * trackMult;
		GridCell cell = this.CoordToCell(this.mTrackPosX, this.mTrackPosY);
		if (cell == null) {
			this.mTrackPosX -= x * trackMult;
			this.mTrackPosY -= y * trackMult;
			return true;
		}
		// Set the cell as selected
		if (this.mSelectedCell != null) {
			this.mSelectedCell.mSelected = false;
			if (this.mSelectedCell != cell)
				this.mTouchedListener.gridTouched(cell);
		}
		for (GridCell c : this.mCells) {
			c.mSelected = false;
			this.mCages.get(c.getCageId()).mSelected = false;
		}
		this.mSelectedCell = cell;
		cell.mSelected = true;
		this.mCages.get(this.mSelectedCell.getCageId()).mSelected = true;
		invalidate();
		return true;
	}

	// Return the number of times a given user value is in a row
	public int getNumValueInRow(GridCell ocell) {
		int count = 0;
		for (GridCell cell : this.mCells) {
			if (cell.getRow() == ocell.getRow()
					&& cell.getUserValue() == ocell.getUserValue())
				count++;
		}
		return count;
	}

	// Return the number of times a given user value is in a column
	public int getNumValueInCol(GridCell ocell) {
		int count = 0;
		for (GridCell cell : this.mCells) {
			if (cell.getColumn() == ocell.getColumn()
					&& cell.getUserValue() == ocell.getUserValue())
				count++;
		}
		return count;
	}

	// Solve the puzzle by setting the Uservalue to the actual value
	public void Solve() {
		this.mCheated = true;
		if (this.moves != null) {
			this.moves.clear();
		}
		for (GridCell cell : this.mCells) {
			if (!cell.isUserValueCorrect()) {
				cell.setCheated();
			}
			cell.setUserValue(cell.getCorrectValue());
		}
		invalidate();
	}

	// Returns whether the puzzle is solved.
	public boolean isSolved() {
		for (GridCell cell : this.mCells)
			if (!cell.isUserValueCorrect())
				return false;
		return true;
	}

	// Checks whether the user has made any mistakes
	public boolean isSolutionValidSoFar() {
		for (GridCell cell : this.mCells)
			if (cell.isUserValueSet())
				if (cell.getUserValue() != cell.getCorrectValue())
					return false;

		return true;
	}

	// Highlight those cells where the user has made a mistake
	public void markInvalidChoices() {
		boolean isValid = true;
		for (GridCell cell : this.mCells)
			if (cell.isUserValueSet())
				if (cell.getUserValue() != cell.getCorrectValue()) {
					cell.setInvalidHighlight(true);
					isValid = false;
				}

		if (!isValid)
			invalidate();

		return;
	}

	// Return the list of cells that are highlighted as invalid
	public ArrayList<GridCell> invalidsHighlighted() {
		ArrayList<GridCell> invalids = new ArrayList<GridCell>();
		for (GridCell cell : this.mCells)
			if (cell.getInvalidHighlight())
				invalids.add(cell);

		return invalids;
	}

	public void setSolvedHandler(OnSolvedListener listener) {
		this.mSolvedListener = listener;
	}

	public abstract class OnSolvedListener {
		public abstract void puzzleSolved();
	}

	public void setOnGridTouchListener(OnGridTouchListener listener) {
		this.mTouchedListener = listener;
	}

	public abstract class OnGridTouchListener {
		public abstract void gridTouched(GridCell cell);
	}

	public void AddMove(CellChange move) {
		if (moves == null) {
			moves = new ArrayList<CellChange>();
		}

		boolean identicalToLastMove = false;
		int indexLastMove = moves.size() - 1;
		if (indexLastMove >= 0) {
			CellChange lastMove = moves.get(indexLastMove);
			identicalToLastMove = lastMove.equals(move);
		}
		if (!identicalToLastMove) {
			moves.add(move);
		}
	}

	/**
	 * Get the number of moves made by the user.
	 * 
	 * @return The number of moves made by the user.
	 */
	public int countMoves() {
		return (moves == null ? 0 : moves.size());
	}

	public void UndoLastMove() {
		if (moves != null) {
			int undoPosition = moves.size() - 1;

			if (undoPosition >= 0) {
				moves.get(undoPosition).restore();
				moves.remove(undoPosition);
				invalidate();
			}
		}
	}

	public void SetSelectedCell(GridCell cell) {
		mSelectedCell = cell;

		for (GridCell c : this.mCells) { // IMPROVE: use old value of
											// mSelectedCell to deselect cell
											// and cage ...
			c.mSelected = false;
			this.mCages.get(c.getCageId()).mSelected = false;
		}

		if (this.mTouchedListener != null) {
			this.mSelectedCell.mSelected = true;
			this.mCages.get(this.mSelectedCell.getCageId()).mSelected = true;
		}
	}

	/**
	 * Clear the user value of the selected cell from the list of possible
	 * values in all other cells in the same row or in the same column as the
	 * selected cell.
	 * 
	 * @param originalCellChange
	 *            The cell which was originally changed.
	 */
	public void clearRedundantPossiblesInSameRowOrColumn(
			CellChange originalCellChange) {
		int rowSelectedCell = this.mSelectedCell.getRow();
		int columnSelectedCell = this.mSelectedCell.getColumn();
		int valueSelectedCell = this.mSelectedCell.getUserValue();
		if (this.mSelectedCell != null) {
			for (GridCell cell : this.mCells) {
				if (cell.getRow() == rowSelectedCell
						|| cell.getColumn() == columnSelectedCell) {
					if (cell.hasPossible(valueSelectedCell)) {
						cell.saveUndoInformation(originalCellChange);
						cell.togglePossible(valueSelectedCell);
					}
				}
			}
		}
	}

	/**
	 * Get the seed which is used to generate this puzzle.
	 * 
	 * @return The seed which can be used to generate this puzzle.
	 */
	public long getGameSeed() {
		return this.mGameSeed;
	}

	/**
	 * Check is user has cheated with solving this puzzle by requesting the
	 * solution.
	 * 
	 * @return True in case the user has solved the puzzle by requesting the
	 *         solution. False otherwise.
	 */
	public boolean isSolvedByCheating() {
		return this.mCheated;
	}

	/**
	 * Create a string representation of the Grid View which can be used to
	 * store a grid view in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString() {
		String storageString = SAVE_GAME_GRID_VERSION_01
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mElapsed
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mGridSize
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mActive;
		return storageString;
	}

	/**
	 * Read view information from or a storage string which was created with @
	 * GridView#toStorageString()} before.
	 * 
	 * @param line
	 *            The line containing the view information.
	 * @return True in case the given line contains view information and is
	 *         processed correctly. False otherwise.
	 */
	public boolean fromStorageString(String line) {
		String[] viewParts = line.split(GameFile.FIELD_DELIMITER_LEVEL1);

		int cellInformationVersion = 0;
		if (viewParts[0].equals(SAVE_GAME_GRID_VERSION_01)) {
			cellInformationVersion = 1;
		} else {
			return false;
		}

		// Process all parts
		int index = 1;
		mElapsed = Long.parseLong(viewParts[index++]);
		mGridSize = Integer.parseInt(viewParts[index++]);
		mActive = Boolean.parseBoolean(viewParts[index++]);

		return true;
	}
}
