package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;

import java.util.ArrayList;
import java.util.List;

public class TestGridVisibleOperators extends TestGrid {
	public TestGridVisibleOperators() {
		super(4, false);
	}

	protected void createTestGrid() {
		// Create the cages
		List<Cage> mCages = new ArrayList<Cage>();
		int cageId = 0; // Cage id's are 0-based

		// Cage 0
		Cage cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 0, 1, 5, 9 })
				.setResult(16)
				.setCageOperator(CageOperator.MULTIPLY)
				.build();
		mCages.add(cage);

		// Cage 1
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 2, 3 })
				.setResult(7)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(cage);

		// Cage 2
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 4, 8 })
				.setResult(2)
				.setCageOperator(CageOperator.SUBTRACT)
				.build();
		mCages.add(cage);

		// Cage 3
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 6, 7 })
				.setResult(4)
				.setCageOperator(CageOperator.DIVIDE)
				.build();
		mCages.add(cage);

		// Cage 4
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 10, 14, 15 })
				.setResult(6)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(cage);

		// Cage 5
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 11 })
				.setResult(2)
				.setCageOperator(CageOperator.NONE)
				.build();
		mCages.add(cage);

		// Cage 6
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 12, 13 })
				.setResult(7)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(cage);

		// Create the cells
		List<Cell> mCells = new ArrayList<Cell>();
		Cell cell[] = new Cell[16];
		int cellNumber = 0;

		// Row 1
		mCells.add(createCell(cellNumber++, 2, 0));
		mCells.add(createCell(cellNumber++, 1, 0));
		mCells.add(createCell(cellNumber++, 4, 1));
		mCells.add(createCell(cellNumber++, 3, 1));

		// Row 2
		mCells.add(createCell(cellNumber++, 3, 2));
		mCells.add(createCell(cellNumber++, 2, 0));
		mCells.add(createCell(cellNumber++, 1, 3));
		mCells.add(createCell(cellNumber++, 4, 3));

		// Row 3
		mCells.add(createCell(cellNumber++, 1, 2));
		mCells.add(createCell(cellNumber++, 4, 0));
		mCells.add(createCell(cellNumber++, 3, 4));
		mCells.add(createCell(cellNumber++, 2, 5));

		// Row 4
		mCells.add(createCell(cellNumber++, 4, 6));
		mCells.add(createCell(cellNumber++, 3, 6));
		mCells.add(createCell(cellNumber++, 2, 4));
		mCells.add(createCell(cellNumber++, 1, 4));

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
		Cell cell = mGrid.getCell(cellId);
		int correctValue = cell.getCorrectValue();
		cell.setUserValue(correctValue == 1 ? mGridSize : 1);

		return this;
	}
}
