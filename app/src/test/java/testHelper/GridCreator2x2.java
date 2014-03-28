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
public class GridCreator2x2 extends GridCreator {
	public static GridCreator2x2 create() {
		return new GridCreator2x2(false);
	}

	public static GridCreator2x2 createEmpty() {
		GridCreator2x2 gridCreator = new GridCreator2x2(false);
		gridCreator.setEmptyGrid();
		return gridCreator;
	}

	protected GridCreator2x2(boolean hideOperator) {
		super(hideOperator);
	}

	@Override
	protected long getGameSeed() {
		return 6396331247626949785L;
	}

	protected GridType getGridType() {
		return GridType.GRID_2X2;
	}

	protected PuzzleComplexity getPuzzleComplexity() {
		return PuzzleComplexity.VERY_EASY;
	}

	protected int getGeneratorVersionNumber() {
		return 598;
	}

	protected int getMaxCageResult() {
		return 72;
	}

	protected int getMaxCageSize() {
		return 2;
	}

	protected int[] getCorrectValuePerCell() {
		return new int[] {
				// Row 1
				1, 2,
				// Row 2
				2, 1 };
	}

	protected int[] getCageIdPerCell() {
		return new int[] {
				// Row 1
				0, 1,
				// Row 2
				0, 2 };
	}

	protected int[] getResultPerCage() {
		return new int[] {
				// Cage 1
				3,
				// Cage 2
				2,
				// Cage 3
				1 };
	}

	protected CageOperator[] getCageOperatorPerCage() {
		return new CageOperator[] {
				// Cage 1
				CageOperator.ADD,
				// Cage 2
				CageOperator.NONE,
				// Cage 3
				CageOperator.NONE };
	}

	@Override
	public String getGridDefinition() {
		return new StringBuilder() //
				// PuzzleComplexity id
				.append("1")
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				// Cage ids for cells on row 1
				.append("0001")
				// Cage ids for cells on row 2
				.append("0002")
				// Definition for cage id 0
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("0,3,1")
				// Definition for cage id 1
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("1,2,0")
				// Definition for cage id 2
				.append(FIELD_SEPARATOR_GRID_DEFINITION_PART)
				.append("2,1,0")
				.toString();
	}
}
