package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The test grid in this class is a variant on the grid in the super class. In
 * contrary to the superclass the cage ids are not consecutive. However, the
 * resulting grid is identical to the grid in the superclass.
 */
public class GridCreator4x4CageIdsNotConsecutive extends GridCreator4x4 {

	private GridCreator4x4CageIdsNotConsecutive(boolean hideOperator) {
		super(hideOperator);
	}

	public static GridCreator4x4CageIdsNotConsecutive create() {
		return new GridCreator4x4CageIdsNotConsecutive(false);
	}

	protected int[] getCageIdPerCell() {
		// Cage id 2 has been renumbered to cage id 6. The cage ids are no
		// longer consecutive as cage id 2 is missing.
		return new int[/* cell id */] {
				// Row 1
				0, 1, 1, 1,
				// Row 2
				0, 6, 6, 1,
				// Row 3
				6, 6, 6, 1,
				// Row 4
				3, 3, 6, 4 };
	}

	protected int[] getResultPerCage() {
		// Cage id 2 has been renumbered to cage id 6. The cage ids are no
		// longer consecutive as cage id 2 is missing.
		return new int[/* cage id */] {
				// Cage id 0
				3,
				// Cage id 1
				64,
				// Cage id 2: this cage is is not used in the list of cells!
				-1,
				// Cage id 3
				3,
				// Cage id 4
				3,
				// Cage id 5: this cage is is not used in the list of cells!
				-1,
				// Cage id 6
				15, };
	}

	protected CageOperator[] getCageOperatorPerCage() {
		return new CageOperator[/* cage id */] {
				// Cage id 0
				CageOperator.DIVIDE,
				// Cage id 1
				CageOperator.MULTIPLY,
				// Cage id 2: this cage is is not used in the list of cells!
				null,
				// Cage id 3
				CageOperator.SUBTRACT,
				// Cage id 4
				CageOperator.NONE,
				// Cage id 5: this cage is is not used in the list of cells!
				null,
				// Cage id 6
				CageOperator.ADD, };
	}

	public String getGridDefinition() {
		// Although the cages are numbered differently compared to the
		// superclass, it is essentially the same grid.
		return super.getGridDefinition();
	}

	@Override
	public List<Cage> getCages() {
		List<Cage> cages = new ArrayList<Cage>();

		for (int cageId = 0; cageId < getResultPerCage().length; cageId++) {
			if (getResultPerCage()[cageId] <= 0
					&& getCageOperatorPerCage()[cageId] == null) {
				// This cage id is missing in the list of cells.
				continue;
			}
			Cage cage = new CageBuilder()
					.setId(cageId)
					.setHideOperator(isHiddenOperator())
					.setCells(getCells(cageId))
					.setResult(getResultPerCage()[cageId])
					.setCageOperator(getCageOperatorPerCage()[cageId])
					.build();
			cages.add(cage);
		}

		return cages;
	}
}
