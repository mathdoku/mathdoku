package testHelper;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.grid.Cage;
import net.mathdoku.plus.grid.CageBuilder;
import net.mathdoku.plus.grid.Cell;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;

import java.util.ArrayList;
import java.util.List;

public class TestGridHiddenOperators extends TestGrid {
	private int mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator;

	public TestGridHiddenOperators() {
		super(4, true);
	}

	@Override
	protected void createTestGrid() {
		// Create the cages
		List<Cage> mCages = new ArrayList<Cage>();
		int cageId = 0; // Cage id's are 0-based

		// Cage 0
		Cage cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 0, 1 })
				.setResult(2)
				.setCageOperator(CageOperator.SUBTRACT)
				.build();
		mCages.add(cage);

		// Cage 1
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 2, 3 })
				.setResult(6)
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
		mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator = 5;
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(true)
				.setCells(
						new int[] {
								mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator,
								6 })
				.setResult(12)
				.setCageOperator(CageOperator.MULTIPLY)
				.build();
		mCages.add(cage);

		// Cage 4
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 7, 11 })
				.setResult(3)
				.setCageOperator(CageOperator.DIVIDE)
				.build();
		mCages.add(cage);

		// Cage 5
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 9, 13 })
				.setResult(3)
				.setCageOperator(CageOperator.ADD)
				.build();
		mCages.add(cage);

		// Cage 6
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 10, 14 })
				.setResult(3)
				.setCageOperator(CageOperator.SUBTRACT)
				.build();
		mCages.add(cage);

		// Cage 7
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 12 })
				.setResult(3)
				.setCageOperator(CageOperator.NONE)
				.build();
		mCages.add(cage);

		// Cage 8
		cage = new CageBuilder()
				.setId(cageId++)
				.setHideOperator(mHideOperator)
				.setCells(new int[] { 15 })
				.setResult(2)
				.setCageOperator(CageOperator.NONE)
				.build();
		mCages.add(cage);

		// Create the cells
		List<Cell> mCells = new ArrayList<Cell>();
		Cell cell[] = new Cell[16];
		int cellNumber = 0;

		// Row 1
		mCells.add(createCell(cellNumber++, 1, 0));
		mCells.add(createCell(cellNumber++, 3, 0));
		mCells.add(createCell(cellNumber++, 2, 1));
		mCells.add(createCell(cellNumber++, 4, 1));

		// Row 2
		mCells.add(createCell(cellNumber++, 2, 2));
		mCells.add(createCell(cellNumber++, 4, 3));
		mCells.add(createCell(cellNumber++, 3, 3));
		mCells.add(createCell(cellNumber++, 1, 4));

		// Row 3
		mCells.add(createCell(cellNumber++, 4, 2));
		mCells.add(createCell(cellNumber++, 2, 5));
		mCells.add(createCell(cellNumber++, 1, 6));
		mCells.add(createCell(cellNumber++, 3, 4));

		// Row 4
		mCells.add(createCell(cellNumber++, 3, 7));
		mCells.add(createCell(cellNumber++, 1, 5));
		mCells.add(createCell(cellNumber++, 4, 6));
		mCells.add(createCell(cellNumber++, 2, 8));

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
