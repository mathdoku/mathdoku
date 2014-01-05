package testHelper;

import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;

/**
 * Helper class for unit testing. This class returns initialized grids.
 */
public class TestData {
	private static GridCell createGridCell(Grid grid, int cellNumber,
			int cellValue) {
		GridCell gridCell = new GridCell(grid, cellNumber);
		gridCell.setCorrectValue(cellValue);
		return gridCell;
	}

	public static Grid make_4x4GridWithVisibleOperators_old() {
		// Create grid with size 1
		Grid grid = new Grid();
		grid.setGridSize(4);

		// Create the cells
		GridCell gridCell[] = new GridCell[16];
		int cellNumber = 0;

		// Row 1
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 1));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 2));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 3));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 4));

		// Row 2
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 2));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 4));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 1));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 3));

		// Row 3
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 4));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 3));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 2));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 1));

		// Row 4
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 3));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 1));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 4));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 2));

		// Create the cages
		boolean VISIBLE_OPERATORS = false; // Actual parameter for calls is
											// hiddenOperator. For ease of
											// reading the parameter
											// VISIBLE_OPERATORS is used. So
											// false really results in visible
											// operators!
		int cageId = 0; // Cage id's are 0-based

		// Cage 0
		GridCage gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[0]);
		gridCage.mCells.add(gridCell[1]);
		gridCage.mCells.add(gridCell[5]);
		gridCage.mCells.add(gridCell[9]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(24, GridCage.ACTION_MULTIPLY, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 1
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[2]);
		gridCage.mCells.add(gridCell[6]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(3, GridCage.ACTION_MULTIPLY, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 2
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[3]);
		gridCage.mCells.add(gridCell[7]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(12, GridCage.ACTION_MULTIPLY, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 3
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[4]);
		gridCage.mCells.add(gridCell[8]);
		gridCage.mCells.add(gridCell[12]);
		gridCage.mCells.add(gridCell[13]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(10, GridCage.ACTION_ADD, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 4
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[10]);
		gridCage.mCells.add(gridCell[11]);
		gridCage.mCells.add(gridCell[15]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(5, GridCage.ACTION_ADD, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 5
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[14]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(4, GridCage.ACTION_NONE, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		return grid;
	}

	public static Grid make_4x4GridWithVisibleOperatorsAllCellsEmpty() {
		// Create grid with size 1
		Grid grid = new Grid();
		grid.setGridSize(4);

		// Create the cells
		GridCell gridCell[] = new GridCell[16];
		int cellNumber = 0;

		// Row 1
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 2));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 1));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 4));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 3));

		// Row 2
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 3));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 2));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 1));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 4));

		// Row 3
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 1));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 4));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 3));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 2));

		// Row 4
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 4));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 3));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 2));
		grid.mCells.add(gridCell[cellNumber++] = createGridCell(grid,
				cellNumber, 1));

		// Create the cages
		boolean VISIBLE_OPERATORS = false; // Actual parameter for calls is
											// hiddenOperator. For ease of
											// reading the parameter
											// VISIBLE_OPERATORS is used. So
											// false really results in visible
											// operators!
		int cageId = 0; // Cage id's are 0-based

		// Cage 0
		GridCage gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[0]);
		gridCage.mCells.add(gridCell[1]);
		gridCage.mCells.add(gridCell[5]);
		gridCage.mCells.add(gridCell[9]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(16, GridCage.ACTION_MULTIPLY, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 1
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[2]);
		gridCage.mCells.add(gridCell[3]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(7, GridCage.ACTION_ADD, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 2
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[4]);
		gridCage.mCells.add(gridCell[8]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(2, GridCage.ACTION_SUBTRACT, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 3
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[6]);
		gridCage.mCells.add(gridCell[7]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(4, GridCage.ACTION_DIVIDE, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 4
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[10]);
		gridCage.mCells.add(gridCell[14]);
		gridCage.mCells.add(gridCell[15]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(6, GridCage.ACTION_ADD, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 5
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[11]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(2, GridCage.ACTION_NONE, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		// Cage 6
		gridCage = new GridCage(grid, VISIBLE_OPERATORS);
		gridCage.mCells.add(gridCell[12]);
		gridCage.mCells.add(gridCell[13]);
		gridCage.setCageId(cageId++);
		gridCage.setCageResults(7, GridCage.ACTION_ADD, VISIBLE_OPERATORS);
		grid.mCages.add(gridCage);

		return grid;
	}
}
