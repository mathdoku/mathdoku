package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.grid.CageBuilder;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;

import java.util.ArrayList;

public class TestGridVisibleOperators extends TestGrid {
	public TestGridVisibleOperators() {
		super(4, false);

		// Create the cages
		ArrayList<GridCage> mCages = new ArrayList<GridCage>();
		int cageId = 0; // Cage id's are 0-based

		// Cage 0
		GridCage gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 0, 1, 5, 9 })
				.setResult(16)
				.setCageOperator(CageOperator.MULTIPLY)
				.build();
		mCages.add(gridCage);

		// Cage 1
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 2, 3 })
				.setResult(7)
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
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 6, 7 })
				.setResult(4)
				.setCageOperator(CageOperator.DIVIDE)
				.build();
		mCages.add(gridCage);

		// Cage 4
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 10, 14, 15 })
				.setResult(6)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		// Cage 5
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 11 })
				.setResult(2)
				.setCageOperator(CageOperator.NONE)
				.build();
		mCages.add(gridCage);

		// Cage 6
		gridCage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 12, 13 })
				.setResult(7)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(gridCage);

		// Create the cells
		ArrayList<GridCell> mCells = new ArrayList<GridCell>();
		GridCell gridCell[] = new GridCell[16];
		int cellNumber = 0;

		// Row 1
		mCells.add(createGridCell(cellNumber++, 2, 0));
		mCells.add(createGridCell(cellNumber++, 1, 0));
		mCells.add(createGridCell(cellNumber++, 4, 1));
		mCells.add(createGridCell(cellNumber++, 3, 1));

		// Row 2
		mCells.add(createGridCell(cellNumber++, 3, 2));
		mCells.add(createGridCell(cellNumber++, 2, 0));
		mCells.add(createGridCell(cellNumber++, 1, 3));
		mCells.add(createGridCell(cellNumber++, 4, 3));

		// Row 3
		mCells.add(createGridCell(cellNumber++, 1, 2));
		mCells.add(createGridCell(cellNumber++, 4, 0));
		mCells.add(createGridCell(cellNumber++, 3, 4));
		mCells.add(createGridCell(cellNumber++, 2, 5));

		// Row 4
		mCells.add(createGridCell(cellNumber++, 4, 6));
		mCells.add(createGridCell(cellNumber++, 3, 6));
		mCells.add(createGridCell(cellNumber++, 2, 4));
		mCells.add(createGridCell(cellNumber++, 1, 4));

		GridGeneratingParameters gridGeneratingParameters = new GridGeneratingParameters();
		gridGeneratingParameters.mHideOperators = mHideOperator;
		gridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.NORMAL;
		gridGeneratingParameters.mGameSeed = 0;
		gridGeneratingParameters.mGeneratorRevisionNumber = 596;
		gridGeneratingParameters.mMaxCageResult = 999999;
		gridGeneratingParameters.mMaxCageSize = 4;

		mGridBuilder
				.setGridSize(mGridSize)
				.setGridGeneratingParameters(gridGeneratingParameters)
				.setCells(mCells)
				.setCages(mCages);
	}

	public TestGridVisibleOperators setEmptyGrid() {
		return (TestGridVisibleOperators) super.setEmptyGrid();
	}

	public TestGridVisibleOperators setIncorrectUserValueInCell(int cellId) {
		GridCell gridCell = mGrid.getCell(cellId);
		int correctValue = gridCell.getCorrectValue();
		gridCell.setUserValue(correctValue == 1 ? mGridSize : 1);

		return this;
	}
}
