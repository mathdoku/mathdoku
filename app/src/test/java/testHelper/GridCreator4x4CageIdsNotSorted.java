package testHelper;

import net.mathdoku.plus.enums.CageOperator;

/**
 * The test grid in this class is a variant on the grid in the super class in
 * which the cages are generated in different order than the cages in the
 * superclass. However, the resulting grid is identical to the grid in the
 * superclass.
 */
public class GridCreator4x4CageIdsNotSorted extends GridCreator4x4 {

	private GridCreator4x4CageIdsNotSorted(boolean hideOperator) {
		super(hideOperator);
	}

	public static GridCreator4x4CageIdsNotSorted create() {
		return new GridCreator4x4CageIdsNotSorted(false);
	}

	protected int[] getCageIdPerCell() {
		return new int[/* cell id */] {
				// Row 1
				4, 0, 0, 0,
				// Row 2
				4, 3, 3, 0,
				// Row 3
				3, 3, 3, 0,
				// Row 4
				1, 1, 3, 2 };
	}

	protected int[] getResultPerCage() {
		//
		return new int[/* cage id */] {
				// Cage id 0
				64,
				// Cage id 1
				3,
				// Cage id 2
				3,
				// Cage id 3
				15,
				// Cage id 4
				3 };
	}

	protected CageOperator[] getCageOperatorPerCage() {
		return new CageOperator[/* cage id */] {
				// Cage id 0
				CageOperator.MULTIPLY,
				// Cage id 1
				CageOperator.SUBTRACT,
				// Cage id 2
				CageOperator.NONE,
				// Cage id 3
				CageOperator.ADD,
				// Cage id 4
				CageOperator.DIVIDE };
	}

	public String getGridDefinition() {
		// Although the cages are numbered differently compared to the
		// superclass, it is essentially the same grid.
		return super.getGridDefinition();
	}

}
