package net.mathdoku.plus.griddefinition;

import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.puzzle.InvalidGridException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GridDefinitionSplitter {
	// Example of a grid definition:
	// 1:00010202000103040506030405060707:0,4,1:1,2,4:2,2,4:3,1,2:4,4,4:5,2,4:6,4,1:7,6,3
	private final static String REGEXP = "" //
			// Part for puzzle complexity
			+ "\\d"
			// Part for cage id's per cell
			+ GridDefinitionDelimiter.LEVEL1.toString() //
			+ "(" //
			+ "\\d\\d" // Cage id for a cell
			+ ")+" // At least one cell needed
			// Part for cage definitions
			+ "(" //
			// Start of new cage part
			+ GridDefinitionDelimiter.LEVEL1.toString() //
			+ "\\d+" // Cage id
			+ GridDefinitionDelimiter.LEVEL2.toString() //
			+ "\\d+" // Result value of cage
			+ GridDefinitionDelimiter.LEVEL2.toString() //
			+ "\\d" // Cage operator
			+ ")+" // At least one cage needed
	;

	private final String gridDefinition;
	private final String[] gridDefinitionElements;
	private final int ID_PART_COMPLEXITY = 0;
	private final int ID_PART_CELLS = 1;
	private final int ID_PART_FIRST_CAGE = 2;

	public GridDefinitionSplitter(String gridDefinition) {
		if (gridDefinition == null) {
			throw new InvalidGridException("Grid definition cannot be null.");
		}

		this.gridDefinition = gridDefinition;
		if (!gridDefinition.matches(REGEXP)) {
			throw new InvalidGridException(
					"Grid definition has invalid format.");
		}

		gridDefinitionElements = gridDefinition
				.split(GridDefinitionDelimiter.LEVEL1.toString());
		if (gridDefinitionElements == null || gridDefinitionElements.length < 3) {
			throw new InvalidGridException(
					"Grid definition has too little elements.");
		}
	}

	public PuzzleComplexity getPuzzleComplexity() {
		PuzzleComplexity puzzleComplexity = null;
		try {
			puzzleComplexity = PuzzleComplexity
					.fromId(gridDefinitionElements[ID_PART_COMPLEXITY]);
		} catch (IllegalArgumentException e) {
			throw new InvalidGridException(
					String.format(
							"Grid definition '%s' has invalid complexity '%s'.",
							gridDefinition,
							gridDefinitionElements[ID_PART_COMPLEXITY]), e);
		}

		return puzzleComplexity;
	}

	public int getNumberOfCells() {
		// The first part of the definition contains the cage number for each
		// individual cell. The cage number always consists of two digits. So
		// the number of cells is equal to 50% of the length of this string.
		return gridDefinitionElements[ID_PART_CELLS].length() / 2;
	}

	public int getNumberOfCages() {
		return gridDefinitionElements.length - ID_PART_FIRST_CAGE;
	}

	public int[] getCageIdPerCell() {
		// The cagesString contains the cage number for each individual cell.
		// The cage number always consists of two digits.
		Pattern pattern = Pattern.compile("\\d\\d");
		Matcher matcher = pattern
				.matcher(gridDefinitionElements[ID_PART_CELLS]);
		int[] cageIdPerCell = new int[getNumberOfCells()];
		int cellNumber = 0;
		while (matcher.find()) {
			cageIdPerCell[cellNumber++] = Integer.valueOf(matcher.group());
		}
		return cageIdPerCell;
	}

	public String[] getCageDefinitions() {
		String[] cages = new String[getNumberOfCages()];
		for (int i = ID_PART_FIRST_CAGE; i < gridDefinitionElements.length; i++) {
			cages[i - ID_PART_FIRST_CAGE] = gridDefinitionElements[i];
		}
		return cages;
	}
}
