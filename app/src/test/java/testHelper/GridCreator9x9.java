package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;

/**
 * The test grid in this class has been generated with the specified generator
 * version of the app. In case the grid is recreated with the same grid
 * generating parameters as specified by this class, the result should be equal
 * to the grid itself.
 * 
 * All data in the methods below should be kept in sync with the specified
 * version of the generator.
 * 
 * As the data below was generated with hidden operators, this grid can be used
 * with visible and with hidden operator as well.
 */
public class GridCreator9x9 extends GridCreator {
	public static GridCreator9x9 create() {
		return new GridCreator9x9(false);
	}

	public static GridCreator9x9 createEmpty() {
		GridCreator9x9 gridCreator = new GridCreator9x9(false);
		gridCreator.setEmptyGrid();
		return gridCreator;
	}

	protected GridCreator9x9(boolean hideOperator) {
		super(hideOperator);
	}

	@Override
	protected long getGameSeed() {
		return -3248887143081219555L;
	}

	protected GridType getGridType() {
		return GridType.GRID_9X9;
	}

	protected PuzzleComplexity getPuzzleComplexity() {
		return PuzzleComplexity.NORMAL;
	}

	protected int getGeneratorVersionNumber() {
		return 598;
	}

	protected int getMaxCageResult() {
		return 2500;
	}

	protected int getMaxCageSize() {
		return 4;
	}

	protected int[] getCorrectValuePerCell() {
		return new int[] {
				// Row 1
				7, 1, 8, 9, 3, 2, 6, 4, 5, 2, 5, 3, 8, 1, 9, 4, 7, 6, 5, 8, 9,
				3, 4, 7, 2, 6, 1, 6, 4, 2, 7, 8, 3, 1, 5, 9, 1, 3, 7, 5, 6, 8,
				9, 2, 4, 8, 7, 4, 6, 9, 5, 3, 1, 2, 4, 9, 1, 2, 7, 6, 5, 8, 3,
				3, 6, 5, 4, 2, 1, 7, 9, 8, 9, 2, 6, 1, 5, 4, 8, 3, 7 };
	}

	protected int[] getCageIdPerCell() {
		return new int[] {
				// Row 1
				0, 1, 1, 1, 2, 2, 3, 4, 5, 0, 6, 6, 7, 2, 2, 3, 4, 5, 0, 0, 8,
				7, 9, 10, 11, 4, 12, 13, 14, 8, 8, 9, 10, 10, 15, 12, 13, 13,
				16, 17, 9, 18, 18, 15, 19, 20, 16, 16, 17, 9, 21, 18, 15, 19,
				20, 20, 16, 22, 22, 21, 21, 21, 19, 23, 23, 24, 22, 25, 25, 26,
				26, 26, 27, 27, 24, 28, 28, 25, 29, 29, 30 };
	}

	protected int[] getResultPerCage() {
		return new int[] { 560, 72, 54, 2, 168, 1, 8, 24, 126, 1728, 21, 2, 9,
				18, 4, 8, 196, 30, 20, 9, 288, 1200, 56, 9, 1, 7, 504, 7, 4,
				11, 7 };
	}

	protected CageOperator[] getCageOperatorPerCage() {
		return new CageOperator[] { CageOperator.MULTIPLY,
				CageOperator.MULTIPLY, CageOperator.MULTIPLY,
				CageOperator.SUBTRACT, CageOperator.MULTIPLY,
				CageOperator.SUBTRACT, CageOperator.ADD, CageOperator.MULTIPLY,
				CageOperator.MULTIPLY, CageOperator.MULTIPLY,
				CageOperator.MULTIPLY, CageOperator.NONE, CageOperator.DIVIDE,
				CageOperator.MULTIPLY, CageOperator.NONE, CageOperator.ADD,
				CageOperator.MULTIPLY, CageOperator.MULTIPLY, CageOperator.ADD,
				CageOperator.ADD, CageOperator.MULTIPLY, CageOperator.MULTIPLY,
				CageOperator.MULTIPLY, CageOperator.ADD, CageOperator.SUBTRACT,
				CageOperator.ADD, CageOperator.MULTIPLY, CageOperator.SUBTRACT,
				CageOperator.SUBTRACT, CageOperator.ADD, CageOperator.NONE };
	}

	@Override
	public String getGridDefinition() {
		return new StringBuilder() //
				// PuzzleComplexity id
				.append("3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				// Cage ids for cells on row 1
				.append("000101010202030405")
				.append("000606070202030405")
				.append("000008070910110412")
				.append("131408080910101512")
				.append("131316170918181519")
				.append("201616170921181519")
				.append("202016222221212119")
				.append("232324222525262626")
				.append("272724282825292930")
				// Definition for cage id 0
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("0,560,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("1,72,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("2,54,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("3,2,2")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("4,168,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("5,1,2")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("6,8,1")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("7,24,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("8,126,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("9,1728,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("10,21,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("11,2,0")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("12,9,4")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("13,18,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("14,4,0")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("15,8,1")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("16,196,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("17,30,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("18,20,1")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("19,9,1")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("20,288,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("21,1200,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("22,56,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("23,9,1")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("24,1,2")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("25,7,1")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("26,504,3")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("27,7,2")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("28,4,2")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("29,11,1")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("30,7,0")
				.toString();
	}
}
