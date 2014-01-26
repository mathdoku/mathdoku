package net.mathdoku.plus.gridDefinition;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridBuilder;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.grid.GridObjectsCreator;
import net.mathdoku.plus.grid.InvalidGridException;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridGenerating.GridGenerator;
import net.mathdoku.plus.util.Util;

import java.security.InvalidParameterException;
import java.util.ArrayList;
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
	private ArrayList<GridCage> mCages;
	private ArrayList<GridCell> mCells;

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
	public static String getDefinition(ArrayList<GridCell> cells,
			ArrayList<GridCage> cages,
			GridGeneratingParameters gridGeneratingParameters) {
		StringBuilder definitionString = new StringBuilder();

		if (Util.isArrayListNullOrEmpty(cells)) {
			throw new InvalidParameterException(
					"Parameter cells cannot be null or empty list.");
		}
		if (Util.isArrayListNullOrEmpty(cages)) {
			throw new InvalidParameterException(
					"Parameter cages cannot be null or empty list.");
		}
		if (gridGeneratingParameters == null) {
			throw new InvalidParameterException(
					"Parameter gridGeneratingParameters cannot be null.");
		}

		// Convert puzzle complexity to an integer value. Do not use the ordinal
		// of the enumeration as this value is not persistent.
		int complexity = 0;
		switch (gridGeneratingParameters.mPuzzleComplexity) {
		case RANDOM:
			// Note: puzzles will never be stored with this complexity.
			complexity = 0;
			break;
		case VERY_EASY:
			complexity = 1;
			break;
		case EASY:
			complexity = 2;
			break;
		case NORMAL:
			complexity = 3;
			break;
		case DIFFICULT:
			complexity = 4;
			break;
		case VERY_DIFFICULT:
			complexity = 5;
			break;
		// NO DEFAULT here as we want to be notified at compile time in case a
		// new enum value is added.
		}
		definitionString.append(Integer.toString(complexity)).append(
				DELIMITER_LEVEL1);

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
		return getDefinition(grid.mCells, grid.mCages,
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

		// Create an empty cage for each cage part. The cages needs to exists
		// before the cells can be created.
		int cageCount = definitionParts.length - ID_PART_FIRST_CAGE;
		if (createArrayListOfCages(cageCount) == false) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Cannot create array list of cages.");
			}
			return null;
		}

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

		// Create the cells based on the list of cage numbers for each cell.
		if (createArrayListOfCells(definitionParts[ID_PART_CELLS]) == false) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Cannot create array list of cells.");
			}
			return null;
		}

		// Finalize the grid cages which only can be done after the cell have
		// been attached to the cages.
		for (GridCage cage : mCages) {
			int definitionPartId = ID_PART_FIRST_CAGE + cage.getId();

			if (definitionPartId < ID_PART_FIRST_CAGE
					|| definitionPartId >= definitionParts.length) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException("Cage definition for cage "
							+ cage.getId() + " does not exist.");
				}
				return null;
			}
			if (setResults(cage, definitionParts[definitionPartId]) == false) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException("Cage results for cage "
							+ cage.getId() + " cannot be set.");
				}
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

	private boolean createArrayListOfCages(int numberOfCages) {
		mCages = mGridObjectsCreator.createArrayListOfGridCages();
		for (int i = 0; i < numberOfCages; i++) {
			GridCage gridCage = mGridObjectsCreator.createGridCage();
			gridCage.setCageId(i);

			if (mCages.add(gridCage) == false) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException("Adding new cage failed.");
				}
				return false;
			}
		}

		return true;
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

	private GridGenerator.PuzzleComplexity getPuzzleComplexity(
			String puzzleComplexityString) {
		// TODO: refactor enumeration for persisting id of enum values. Note
		// that values are not consistent with the ordinal values of the
		// enumeration.
		GridGenerator.PuzzleComplexity puzzleComplexity = null;

		switch (Integer.parseInt(puzzleComplexityString)) {
		case 1:
			puzzleComplexity = GridGenerator.PuzzleComplexity.VERY_EASY;
			break;
		case 2:
			puzzleComplexity = GridGenerator.PuzzleComplexity.EASY;
			break;
		case 3:
			puzzleComplexity = GridGenerator.PuzzleComplexity.NORMAL;
			break;
		case 4:
			puzzleComplexity = GridGenerator.PuzzleComplexity.DIFFICULT;
			break;
		case 5:
			puzzleComplexity = GridGenerator.PuzzleComplexity.VERY_DIFFICULT;
			break;
		default:
			// This value can not be specified in a share url created by the
			// app. But in case it is manipulated by a user before sending
			// to another user, the receiver should not get an exception.
			puzzleComplexity = null;
		}

		return puzzleComplexity;
	}

	private boolean createArrayListOfCells(String cagesString) {
		mCells = mGridObjectsCreator.createArrayListOfGridCells();

		// The cagesString contains the cage number for each individual cell.
		// The cage number always consists of two digits.
		Pattern pattern = Pattern.compile("\\d\\d");
		Matcher matcher = pattern.matcher(cagesString);
		int cellNumber = 0;
		while (matcher.find()) {
			int cageId = Integer.valueOf(matcher.group());

			// Create new cell and add it to the cells list.
			GridCell gridCell = mGridObjectsCreator.createGridCell(
					cellNumber++, mGridSize);
			gridCell.setCageId(cageId);
			mCells.add(gridCell);

			// Determine the cage to which the cell has to be added.
			if (cageId < 0 || cageId >= mCages.size()) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException(
							"Cell refers to invalid cage id '" + cageId + "'.");
				}
				return false;
			}
			GridCage gridCage = mCages.get(cageId);
			if (gridCage == null || gridCage.getId() != cageId) {
				if (mThrowExceptionOnError) {
					throw new InvalidGridException("Id of cage is "
							+ gridCage.getId() + " while " + cageId
							+ " was expected.");
				}
				return false;
			}
			gridCage.mCells.add(gridCell);
		}

		return true;
	}

	private boolean setResults(GridCage cage, String cageDefinition) {
		if (cage == null || cageDefinition == null) {
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
		int cageId = Integer.valueOf(cageElements[0]);
		int cageResultValue = Integer.valueOf(cageElements[1]);
		CageOperator cageOperator;
		try {
			cageOperator = CageOperator.fromId(cageElements[2]);
		} catch (InvalidParameterException e) {
			// If the cage operator in the url was manipulated this should not
			// result in an Invalid Parameter Exception as the receiving user
			// might be ignorant of the url being manipulated.
			if (mThrowExceptionOnError) {
				throw e;
			}
			return false;
		}

		if (cage.getId() != cageId) {
			if (mThrowExceptionOnError) {
				throw new InvalidGridException(
						"Cage part does not contain the expected cage id. Got id "
								+ cageId + " while " + cage.getId()
								+ " was expected.");
			}
			return false;
		}

		cage.setCageResults(cageResultValue, cageOperator, false);

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
