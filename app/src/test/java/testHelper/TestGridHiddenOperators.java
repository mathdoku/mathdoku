package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.grid.CageBuilder;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;

import java.util.ArrayList;
import java.util.List;

public class TestGridHiddenOperators extends TestGrid {
	private int mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator;

	public TestGridHiddenOperators() {
		super(4, true);

		// Create the cages
		List<GridCage> mCages = new ArrayList<GridCage>();
		int cageId = 0; // Cage id's are 0-based

		// Cage 0
		GridCage gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 0, 1 })
				.setResult(2)
				.setCageOperator(CageOperator.SUBTRACT)
				.build();
		mCages.add(gridCage);

		// Cage 1
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 2, 3 })
				.setResult(6)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		// Cage 2
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 4, 8 })
				.setResult(2)
				.setCageOperator(CageOperator.SUBTRACT)
				.build();
		mCages.add(gridCage);

		// Cage 3
		mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator = 5;
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(true)
				.setCells(
						new int[] {
								mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator,
								6 })
				.setResult(12)
				.setCageOperator(CageOperator.MULTIPLY)
				.build();
		mCages.add(gridCage);

		// Cage 4
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 7, 11 })
				.setResult(3)
				.setCageOperator(CageOperator.DIVIDE)
				.build();
		mCages.add(gridCage);

		// Cage 5
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 9, 13 })
				.setResult(3)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		// Cage 6
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 10, 14 })
				.setResult(3)
				.setCageOperator(CageOperator.SUBTRACT)
				.build();
		mCages.add(gridCage);

		// Cage 7
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 12 })
				.setResult(3)
				.setCageOperator(CageOperator.NONE)
				.build();
		mCages.add(gridCage);

		// Cage 8
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 15 })
				.setResult(2)
				.setCageOperator(CageOperator.NONE)
				.build();
		mCages.add(gridCage);

		// Create the cells
		List<GridCell> mCells = new ArrayList<GridCell>();
		GridCell gridCell[] = new GridCell[16];
		int cellNumber = 0;

		// Row 1
		mCells.add(createGridCell(cellNumber++, 1, 0));
		mCells.add(createGridCell(cellNumber++, 3, 0));
		mCells.add(createGridCell(cellNumber++, 2, 1));
		mCells.add(createGridCell(cellNumber++, 4, 1));

		// Row 2
		mCells.add(createGridCell(cellNumber++, 2, 2));
		mCells.add(createGridCell(cellNumber++, 4, 3));
		mCells.add(createGridCell(cellNumber++, 3, 3));
		mCells.add(createGridCell(cellNumber++, 1, 4));

		// Row 3
		mCells.add(createGridCell(cellNumber++, 4, 2));
		mCells.add(createGridCell(cellNumber++, 2, 5));
		mCells.add(createGridCell(cellNumber++, 1, 6));
		mCells.add(createGridCell(cellNumber++, 3, 4));

		// Row 4
		mCells.add(createGridCell(cellNumber++, 3, 7));
		mCells.add(createGridCell(cellNumber++, 1, 5));
		mCells.add(createGridCell(cellNumber++, 4, 6));
		mCells.add(createGridCell(cellNumber++, 2, 8));

		GridGeneratingParameters gridGeneratingParameters = new GridGeneratingParameters();
		gridGeneratingParameters.mHideOperators = mHideOperator;
		gridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.NORMAL;
		gridGeneratingParameters.mGameSeed = 0;
		gridGeneratingParameters.mGeneratorRevisionNumber = 596;
		gridGeneratingParameters.mMaxCageResult = 999999;
		gridGeneratingParameters.mMaxCageSize = 2;
		mGridBuilder
				.setGridSize(mGridSize)
				.setGridGeneratingParameters(gridGeneratingParameters)
				.setCells(mCells)
				.setCages(mCages);
	}

	public TestGridHiddenOperators setEmptyGrid() {
		return (TestGridHiddenOperators) super.setEmptyGrid();
	}

	public int getIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator() {
		return mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator;
	}
}
