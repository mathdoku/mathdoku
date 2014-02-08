package net.mathdoku.plus.gridDefinition;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.grid.CageBuilder;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridBuilder;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.grid.GridObjectsCreator;
import net.mathdoku.plus.grid.InvalidGridException;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.util.Util;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class creates the unique definition for a grid. This definition is used
 * to uniquely identify grid stored in the database of share via url's. Also the
 * class is able to recreate a grid based on the grid definition.
 */
public class GridDefinition {
	private static final String TAG = "MathDoku.GridDefinition";

	// Delimiters below should not be altered as they are persisted.
	public static final String DELIMITER_LEVEL1 = ":";
	public static final String DELIMITER_LEVEL2 = ",";

	private GridObjectsCreator mGridObjectsCreator;

	private int mGridSize;
	private List<GridCage> mCages;
	private List<GridCell> mCells;
	private int[] mCageIdPerCell;
	private int[] mCountCellsPerCage;

	// By default this module throws exceptions on error when running in
	// development mode only.
	private boolean mThrowExceptionOnError;

	public GridDefinition() {
		mGridObjectsCreator = new GridObjectsCreator();

		setThrowExceptionOnError(Config.mAppMode == Config.AppMode.DEVELOPMENT);
	}

	public void setObjectsCreator(GridObjectsCreator gridObjectsCreator) {
		if (gridObjectsCreator == null) {
			throw new InvalidParameterException(
					"Parameter gridObjectsCreator can not be null.");
		}
		mGridObjectsCreator = gridObjectsCreator;
	}

	/**
	 * Converts the definition of this grid to a string. This definitions only
	 * consists of information needed to rebuild the puzzle. It does not include
	 * information about how it was created or about the current status of
	 * solving. This definition is unique regardless of grid size and or the
	 * version of the grid generator used.
	 * 
	 * @return A unique string representation of the grid.
	 */
	public static String getDefinition(List<GridCell> cells,
			List<GridCage> cages,
			GridGeneratingParameters gridGeneratingParameters) {
		StringBuilder definitionString = new StringBuilder();

		if (Util.isListNullOrEmpty(cells)) {
			throw new InvalidParameterException(
					"Parameter cells cannot be null or empty list.");
		}
		if (Util.isListNullOrEmpty(cages)) {
			throw new InvalidParameterException(
					"Parameter cages cannot be null or empty list.");
		}
		if (gridGeneratingParameters == null) {
			throw new InvalidParameterException(
					"Parameter gridGeneratingParameters cannot be null.");
		}

		definitionString.append(
				Integer.toString(gridGeneratingParameters.mPuzzleComplexity
						.getId())).append(DELIMITER_LEVEL1);

		// Get the cage number (represented as a value of two digits, if needed
		// prefixed with a 0) for each cell. Note: with a maximum of 81 cells in
		// a 9x9 grid we can never have a cage-id > 99.
		for (GridCell cell : cells) {
			definitionString.append(String.format("%02d", cell.getCageId()));
		}
		// Followed by cages
		for (GridCage cage : cages) {
			definitionString
					.append(DELIMITER_LEVEL1)
					.append(cage.getId())
					.append(DELIMITER_LEVEL2)
					.append(cage.getResult())
					.append(DELIMITER_LEVEL2)
					.append(gridGeneratingParameters.mHideOperators ? CageOperator.NONE
							.getId() : cage.getOperator().getId());
		}
		return definitionString.toString();
	}

	/**
	 * Convenience method for getting the definition of a grid.
	 * 
	 * @param grid
	 * @return
	 */
	public static String getDefinition(Grid grid) {
		return getDefinition(grid.mCells, grid.getCages(),
				grid.getGridGeneratingParameters());
	}

	/**
	 * Create a grid from the given definition string.
	 * 
	 * @return The grid created from the definition. Null in case of an error.
	 */
	public Grid createGrid(String definition) {
		// Example of a grid definition:
		// 1:00010202000103040506030405060707:0,4,1:1,2,4:2,2,4:3,1,2:4,4,4:5,2,4:6,4,1:7,6,3

		if (definition == null) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException("Definition cannot be null.");
			}
			return null;
		}

		if (definition.matches("" //
				// Part for puzzle complexity
				+ "\\d"
				// Part for cage id's per cell
				+ DELIMITER_LEVEL1 //
				+ "(" //
				+ "\\d\\d" // Cage id for a cell
				+ ")+" // At least one cell needed
				// Part for cage definitions
				+ "(" //
				// Start of new cage part
				+ DELIMITER_LEVEL1 //
				+ "\\d+" // Cage id
				+ DELIMITER_LEVEL2 //
				+ "\\d+" // Result value of cage
				+ DELIMITER_LEVEL2 //
				+ "\\d" // Cage operator
				+ ")+" // At least one cage needed
		) == false) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException("Definition has invalid format.");
			}
			return null;
		}

		// Split the definition into parts.
		String[] definitionParts = definition.split(DELIMITER_LEVEL1);
		if (definitionParts == null || definitionParts.length < 3) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Definition has too little elements.");
			}
			return null;
		}

		// The definition contains followings parts:
		int ID_PART_COMPLEXITY = 0;
		int ID_PART_CELLS = 1;
		int ID_PART_FIRST_CAGE = 2;

		// The complexity is not needed to rebuild the puzzle, but it is stored
		// as it is a great communicator to the (receiving) user how difficult
		// the puzzle is.
		GridGeneratingParameters mGridGeneratingParameters = mGridObjectsCreator
				.createGridGeneratingParameters();
		mGridGeneratingParameters.mPuzzleComplexity = getPuzzleComplexity(definitionParts[ID_PART_COMPLEXITY]);
		if (mGridGeneratingParameters.mPuzzleComplexity == null) {
			// The complexity as stored in the definition is not valid. Most
			// likely this definition is received via a shared puzzle url which
			// is manipulated. To prevent NULL pointer exceptions, the
			// definition is no converted to a grid.
			if (mThrowExceptionOnError) {
				throw new InvalidGridException("Complexity '"
						+ definitionParts[ID_PART_COMPLEXITY] + "' is invalid.");
			}
			return null;
		}

		// The first part of the definition contains the cage number for each
		// individual cell. The cage number always consists of two digits. So
		// the number of cells is equal to 50% of the length of this string.
		int cellCount = definitionParts[ID_PART_CELLS].length() / 2;
		if (setGridSize(cellCount) == false) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException("Number of cells " + cellCount
						+ " does not match with accepted grid sizes (1-9).");
			}
			return null;
		}

		// Determine number of cages expected
		int cageCount = definitionParts.length - ID_PART_FIRST_CAGE;

		// Initialize helper variables. Those variables are filled while adding
		// the cells. Later, when adding the cages, the data of those vars is
		// used.
		mCountCellsPerCage = new int[cageCount];
		mCageIdPerCell = new int[cellCount];

		// Create the cells based on the list of cage numbers for each cell.
		if (createArrayListOfCells(definitionParts[ID_PART_CELLS], cageCount) == false) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Cannot create array list of cells.");
			}
			return null;
		}

		mCages = mGridObjectsCreator.createArrayListOfGridCages();
		for (int i = ID_PART_FIRST_CAGE; i < definitionParts.length; i++) {
			if (createCage(definitionParts[i]) == false) {
				return null;
			}
		}

		// Calculate and set the correct values for each cell if a single
		// solution can be determined for the definition.
		if (setCorrectCellValues() == false) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Cannot set the correct values for all cells.");
			}
			return null;
		}

		// All data is gathered now
		GridBuilder gridBuilder = mGridObjectsCreator.createGridBuilder();
		return gridBuilder
				.setGridSize(mGridSize)
				.setCells(mCells)
				.setCages(mCages)
				.setGridGeneratingParameters(mGridGeneratingParameters)
				.build();
	}

	private boolean setGridSize(int cellCount) {
		switch (cellCount) {
		case 1:
			mGridSize = 1;
			break;
		case 4:
			mGridSize = 2;
			break;
		case 9:
			mGridSize = 3;
			break;
		case 16:
			mGridSize = 4;
			break;
		case 25:
			mGridSize = 5;
			break;
		case 36:
			mGridSize = 6;
			break;
		case 49:
			mGridSize = 7;
			break;
		case 64:
			mGridSize = 8;
			break;
		case 81:
			mGridSize = 9;
			break;
		default:
			// Invalid number of cells.
			mGridSize = 0;
		}
		return (mGridSize != 0);
	}

	private PuzzleComplexity getPuzzleComplexity(String puzzleComplexityString) {
		PuzzleComplexity puzzleComplexity = null;
		try {
			puzzleComplexity = PuzzleComplexity.fromId(puzzleComplexityString);
		} catch (InvalidParameterException e) {
			// This value can not be specified in a share url created by the
			// app. But in case it is manipulated by a user before sending
			// to another user, the receiver should not get an exception.
			puzzleComplexity = null;
		}

		return puzzleComplexity;
	}

	private boolean createArrayListOfCells(String cagesString,
			int expectedNumberOfCages) {
		mCells = mGridObjectsCreator.createArrayListOfGridCells();

		// The cagesString contains the cage number for each individual cell.
		// The cage number always consists of two digits.
		Pattern pattern = Pattern.compile("\\d\\d");
		Matcher matcher = pattern.matcher(cagesString);
		int cellNumber = 0;
		while (matcher.find()) {
			int cageId = Integer.valueOf(matcher.group());

			// Create new cell and add it to the cells list.
			GridCell gridCell = mGridObjectsCreator.createGridCell(cellNumber,
					mGridSize);
			gridCell.setCageId(cageId);
			mCells.add(gridCell);

			// Determine the cage to which the cell has to be added.
			if (cageId < 0 || cageId >= expectedNumberOfCages) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Cell refers to invalid cage id '" + cageId + "'.");
				}
				return false;
			}
			mCageIdPerCell[cellNumber] = cageId;
			mCountCellsPerCage[cageId]++;

			cellNumber++;
		}

		return true;
	}

	private boolean createCage(String cageDefinition) {
		if (cageDefinition == null) {
			return false;
		}

		String[] cageElements = cageDefinition.split(DELIMITER_LEVEL2);
		if (cageElements == null || cageElements.length != 3) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Invalid number of elements for cage part with definition '"
								+ cageDefinition + "'.");
			}
			return false;
		}

		CageBuilder cageBuilder = new CageBuilder();
		int cageId = Integer.valueOf(cageElements[0]);
		if (cageId < 0 || cageId >= mCountCellsPerCage.length) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Invalid cage id in cage definition '" + cageDefinition
								+ "'.");
			}
			return false;
		}
		cageBuilder.setId(cageId);
		cageBuilder.setResult(Integer.valueOf(cageElements[1]));
		try {
			cageBuilder.setCageOperator(CageOperator.fromId(cageElements[2]));
		} catch (InvalidParameterException e) {
			// If the cage operator in the url was manipulated this should not
			// result in an Invalid Parameter Exception as the receiving user
			// might be ignorant of the url being manipulated.
			if (mThrowExceptionOnError) {
				throw e;
			}
			return false;
		}
		int[] cells = new int[mCountCellsPerCage[cageId]];
		int cellIndex = 0;
		for (int i = 0; i < mCageIdPerCell.length; i++) {
			if (mCageIdPerCell[i] == cageId) {
				cells[cellIndex++] = i;
			}
		}

		cageBuilder.setCells(cells);
		cageBuilder.setHideOperator(false);

		GridCage cage = cageBuilder.build();
		mCages.add(cage);

		return true;
	}

	private boolean setCorrectCellValues() {
		// Check whether a single solution can be found.
		int[][] solution = mGridObjectsCreator.createMathDokuDLX(mGridSize,
				mCages).getSolutionGrid();
		if (solution == null) {
			// Either no or multiple solutions can be found. In both case this
			// would mean that the grid definition string was manipulated by the
			// user.
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Grid does not have a unique solution.");
			}
			return false;
		}
		if (solution.length != mGridSize) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException("Solution array has "
						+ solution.length + " rows while " + mGridSize
						+ " row were expected.");
			}
			return false;
		}
		for (int row = 0; row < mGridSize; row++) {
			if (solution[row].length != mGridSize) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException("Solution array has "
							+ (solution == null ? 0 : solution[row].length)
							+ " columns in row " + row + " while " + mGridSize
							+ " columns were expected.");
				}
				return false;
			}
		}

		// Store the solution in the grid cells.
		for (GridCell cell : mCells) {
			int row = cell.getRow();
			int column = cell.getColumn();
			if (row >= 0 && row < solution.length && column >= 0
					&& column < solution[row].length) {
				cell.setCorrectValue(solution[row][column]);
			} else {
				return false;
			}
		}

		return true;
	}

	public void setThrowExceptionOnError(boolean throwExceptionOnError) {
		mThrowExceptionOnError = throwExceptionOnError;
	}
}
