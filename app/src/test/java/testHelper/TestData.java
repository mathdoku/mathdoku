package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.grid.CageBuilder;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridBuilder;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;

import java.util.ArrayList;

/**
 * Helper class for unit testing. This class returns initialized grids.
 */
public class TestData {
	private static GridCell createGridCell(int cellNumber, int cellValue,
			int gridSize) {
		GridCell gridCell = new GridCell(cellNumber, gridSize);
		gridCell.setCorrectValue(cellValue);
		return gridCell;
	}

	public static Grid make_4x4GridWithVisibleOperators_old() {
		int gridSize = 4;

		// Create the cells
		ArrayList<GridCell> mCells = new ArrayList<GridCell>();
		GridCell gridCell[] = new GridCell[16];
		int cellNumber = 0;

		// Row 1
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 1,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 2,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 3,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 4,
				gridSize));

		// Row 2
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 2,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 4,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 1,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 3,
				gridSize));

		// Row 3
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 4,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 3,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 2,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 1,
				gridSize));

		// Row 4
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 3,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 1,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 4,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 2,
				gridSize));

		// Create the cages
		ArrayList<GridCage> mCages = new ArrayList<GridCage>();
		boolean VISIBLE_OPERATORS = false; // Actual parameter for calls is
											// hiddenOperator. For ease of
											// reading the parameter
											// VISIBLE_OPERATORS is used. So
											// false really results in visible
											// operators!
		int cageId = 0; // Cage id's are 0-based

		// Cage 0
		GridCage gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 0, 1, 5, 9 })
				.setResult(24)
				.setCageOperator(CageOperator.MULTIPLY)
				.build();
		mCages.add(gridCage);

		// Cage 1
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 2, 6 })
				.setResult(3)
				.setCageOperator(CageOperator.MULTIPLY)
				.build();
		mCages.add(gridCage);

		// Cage 2
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 3, 7 })
				.setResult(12)
				.setCageOperator(CageOperator.MULTIPLY)
				.build();
		mCages.add(gridCage);

		// Cage 3
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 4, 8, 12, 13 })
				.setResult(10)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		// Cage 4
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 10, 11, 15 })
				.setResult(5)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		// Cage 5
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 14 })
				.setResult(4)
				.setCageOperator(CageOperator.NONE)
				.build();
		mCages.add(gridCage);

		GridBuilder gridBuilder = new GridBuilder()
				.setGridSize(4)
				.setCells(mCells)
				.setCages(mCages);
		return gridBuilder.build();
	}

	public static Grid make_4x4GridWithVisibleOperatorsAllCellsEmpty() {
		// Create grid with size 1
		int gridSize = 4;

		// Create the cells
		ArrayList<GridCell> mCells = new ArrayList<GridCell>();
		GridCell gridCell[] = new GridCell[16];
		int cellNumber = 0;

		// Row 1
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 2,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 1,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 4,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 3,
				gridSize));

		// Row 2
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 3,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 2,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 1,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 4,
				gridSize));

		// Row 3
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 1,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 4,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 3,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 2,
				gridSize));

		// Row 4
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 4,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 3,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 2,
				gridSize));
		mCells.add(gridCell[cellNumber++] = createGridCell(cellNumber, 1,
				gridSize));

		// Create the cages
		ArrayList<GridCage> mCages = new ArrayList<GridCage>();
		boolean VISIBLE_OPERATORS = false; // Actual parameter for calls is
											// hiddenOperator. For ease of
											// reading the parameter
											// VISIBLE_OPERATORS is used. So
											// false really results in visible
											// operators!
		int cageId = 0; // Cage id's are 0-based

		// Cage 0
		GridCage gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 0, 1, 5, 9 })
				.setResult(16)
				.setCageOperator(CageOperator.MULTIPLY)
				.build();
		mCages.add(gridCage);

		// Cage 1
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 2, 3 })
				.setResult(7)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		// Cage 2
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 4, 8 })
				.setResult(2)
				.setCageOperator(CageOperator.SUBTRACT)
				.build();
		mCages.add(gridCage);

		// Cage 3
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 6, 7 })
				.setResult(4)
				.setCageOperator(CageOperator.DIVIDE)
				.build();
		mCages.add(gridCage);

		// Cage 4
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 10, 14, 15 })
				.setResult(6)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		// Cage 5
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 11 })
				.setResult(2)
				.setCageOperator(CageOperator.NONE)
				.build();
		mCages.add(gridCage);

		// Cage 6
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(VISIBLE_OPERATORS)
				.setCells(new int[] { 12, 13 })
				.setResult(7)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		GridBuilder gridBuilder = new GridBuilder()
				.setGridSize(4)
				.setCells(mCells)
				.setCages(mCages);
		return gridBuilder.build();
	}
}
