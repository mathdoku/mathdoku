package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.puzzle.grid.Grid;

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
public class GridCreator4x4 extends GridCreator {

	public static GridCreator4x4 create() {
		return new GridCreator4x4(false);
	}

	public static GridCreator4x4 createEmpty() {
		GridCreator4x4 gridCreator = new GridCreator4x4(false);
		gridCreator.setEmptyGrid();
		return gridCreator;
	}

	public static Grid createEmptyGrid() {
		return createEmpty().getGrid();
	}

	protected GridCreator4x4(boolean hideOperator) {
		super(hideOperator);
	}

	public static GridCreator4x4 createWithVisibleOperators() {
		return new GridCreator4x4(false);
	}

	@Override
	protected long getGameSeed() {
		// Actually this grid was generated with HIDDEN operators.
		return DO_NOT_USE_TO_REGENERATE_GRID;
	}

	protected GridType getGridType() {
		return GridType.GRID_4x4;
	}

	protected PuzzleComplexity getPuzzleComplexity() {
		return PuzzleComplexity.VERY_DIFFICULT;
	}

	protected int getGeneratorVersionNumber() {
		return 598;
	}

	protected int getMaxCageResult() {
		return 99999;
	}

	protected int getMaxCageSize() {
		return 6;
	}

	protected int[] getCorrectValuePerCell() {
		return new int[/* cell id */] {
				// Row 1
				3, 2, 4, 1,
				// Row 2
				1, 4, 3, 2,
				// Row 3
				2, 3, 1, 4,
				// Row 4
				4, 1, 2, 3 };
	}

	protected int[] getCageIdPerCell() {
		return new int[/* cell id */] {
				// Row 1
				0, 1, 1, 1,
				// Row 2
				0, 2, 2, 1,
				// Row 3
				2, 2, 2, 1,
				// Row 4
				3, 3, 2, 4 };
	}

	protected int[] getResultPerCage() {
		return new int[/* cage id */] {
				// Cage id 0
				3,
				// Cage id 1
				64,
				// Cage id 2
				15,
				// Cage id 3
				3,
				// Cage id 4
				3 };
	}

	protected CageOperator[] getCageOperatorPerCage() {
		return new CageOperator[/* cage id */] {
				// Cage id 0
				CageOperator.DIVIDE,
				// Cage id 1
				CageOperator.MULTIPLY,
				// Cage id 2
				CageOperator.ADD,
				// Cage id 3
				CageOperator.SUBTRACT,
				// Cage id 4
				CageOperator.NONE };
	}

	public String getGridDefinition() {
		return new StringBuilder() //
				// PuzzleComplexity id
				.append("5")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				// Cage ids for cells on row 1
				.append("00010101")
				// Cage ids for cells on row 2
				.append("00020201")
				// Cage ids for cells on row 3
				.append("02020201")
				// Cage ids for cells on row 4
				.append("03030204")
				// Definition for cage id 0
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("0,3,4")
				// Definition for cage id 1
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("1,64,3")
				// Definition for cage id 2
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("2,15,1")
				// Definition for cage id 3
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("3,3,2")
				// Definition for cage id 4
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("4,3,0")
				.toString();
	}
}
