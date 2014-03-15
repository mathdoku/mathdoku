package net.mathdoku.plus.puzzle.cage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.cell.Cell;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CageTest {
	private CageBuilderStub mCageBuilder;
	Grid mGridMock = mock(Grid.class);

	private class CageBuilderStub extends CageBuilder {
		private int mOffsetCellId = 0;

		private CageBuilderStub setOffsetCellId(int offsetCellId) {
			mOffsetCellId = offsetCellId;

			return this;
		}

		@Override
		public CageBuilderStub setCells(int... enteredValues) {
			if (enteredValues == null) {
				super.setCells((int[]) null);
				return this;
			}

			int[] ids = new int[enteredValues.length];
			for (int i = 0; i < enteredValues.length; i++) {
				ids[i] = mOffsetCellId + i;
			}
			super.setCells(ids);

			// Only add the real values. 0 and negative values indicate that the cell is not filled.
			List<Integer> enteredValuesArrayList = new ArrayList<Integer>();
			for (int enteredValue : enteredValues) {
				if (enteredValue > 0) {
					enteredValuesArrayList.add(enteredValue);
				}
			}
			when(mGridMock.getEnteredValuesForCells(any(int[].class))).thenReturn(
					enteredValuesArrayList);

			return this;
		}
	}

	@Before
	public void setup() {
		// The default test cage with id 1 is a single cell cage with id 3 and
		// correct value (which is also the cage result) of 5.
		mCageBuilder = new CageBuilderStub();
		mCageBuilder
				.setId(1)
				.setCells(new int[] { 3 })
				.setResult(5)
				.setCageOperator(CageOperator.NONE);
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_HasInvalidCageId_CageNotCreated()
			throws Exception {
		mCageBuilder.setId(-1);

		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_HasInvalidResult_CageNotCreated()
			throws Exception {
		mCageBuilder.setResult(-1);

		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_HasInvalidCageOperator_CageNotCreated()
			throws Exception {
		mCageBuilder.setCageOperator(null);

		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_HasNullCells_CageNotCreated() throws Exception {
		mCageBuilder.setCells((int[]) null);

		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_CageWithAddOperatorHasTooLittleCells_CageNotCreated()
			throws Exception {
		CageOperator cageOperator = CageOperator.ADD;
		mCageBuilder
				.setCells(2)
				.setCageOperator(cageOperator);
		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_CageWithSubtractOperatorHasTooLittleCells_CageNotCreated()
			throws Exception {
		CageOperator cageOperator = CageOperator.SUBTRACT;
		mCageBuilder
				.setCells(2)
				.setCageOperator(cageOperator);
		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_CageWithMultiplyOperatorHasTooLittleCells_CageNotCreated()
			throws Exception {
		CageOperator cageOperator = CageOperator.MULTIPLY;
		mCageBuilder
				.setCells(2)
				.setCageOperator(cageOperator);
		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_CageWithDivideOperatorHasTooLittleCells_CageNotCreated()
			throws Exception {
		CageOperator cageOperator = CageOperator.DIVIDE;
		mCageBuilder
				.setCells(2)
				.setCageOperator(cageOperator);
		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void cageConstructor_CageWithNoneOperatorHasTooManyCells_CageNotCreated()
			throws Exception {
		CageOperator cageOperator = CageOperator.NONE;
		mCageBuilder
				.setCells(2, 3)
				.setCageOperator(cageOperator);
		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void checkMathOnEnteredValues_CageTooManyCellsSubtractOperator_CageNotCreated()
			throws Exception {
		CageOperator cageOperator = CageOperator.SUBTRACT;
		mCageBuilder
				.setCells(2, 3, 4)
				.setCageOperator(cageOperator);
		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test(expected = InvalidGridException.class)
	public void checkMathOnEnteredValues_CageTooManyCellsDivideOperator_CageNotCreated()
			throws Exception {
		CageOperator cageOperator = CageOperator.DIVIDE;
		mCageBuilder
				.setCells(2, 3, 4)
				.setCageOperator(cageOperator);
		assertThat(mCageBuilder.build(), is(nullValue()));
	}

	@Test
	public void revealOperator_RevealsAnOperatorFromACageWithHiddenOperator_Success()
			throws Exception {
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(2, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		assertThat(cage.isOperatorHidden(), is(true));

		cage.revealOperator();

		assertThat(cage.isOperatorHidden(), is(false));
	}

	@Test(expected = InvalidGridException.class)
	public void checkMathOnEnteredValues_CageCellListIsNull_MathIsCorrect()
			throws Exception {
		Cage cage = mCageBuilder.setCells((int[]) null).build();

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageWithoutCells_MathIsCorrect()
			throws Exception {
		Cage cage = mCageBuilder.build();

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageOneCellVisibleNoneOperator_MathIsIncorrect()
			throws Exception {
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(2)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageOneCellInvisibleNoneOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(2)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageOneCellInvisibleNoneOperator_MathIsIncorrect()
			throws Exception {
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(2)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsVisibleAddOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(2, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsVisibleAddOperator_MathIsNotCorrect()
			throws Exception {
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(1, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsInvisibleAddOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(2, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsInvisibleAddOperator_MathIsNotCorrect()
			throws Exception {
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(1, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageThreeCellsVisibleAddOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 9;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(2, 3, 4)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageThreeCellsInvisibleAddOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 9;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(2, 3, 4)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsVisibleSubtractOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(3, 2)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsReversedVisibleSubtractOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(2, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsVisibleSubtractOperator_MathIsNotCorrect()
			throws Exception {
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(3, 1)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsInvisibleSubtractOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(3, 2)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsReversedInvisibleSubtractOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(2, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsInvisibleSubtractOperator_MathIsNotCorrect()
			throws Exception {
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(1, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsVisibleMultiplyOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 6;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(3, 2)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsVisibleMultiplyOperator_MathIsNotCorrect()
			throws Exception {
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(3, 1)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsInvisibleMultiplyOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 6;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(3, 2)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsInvisibleMultiplyOperator_MathIsNotCorrect()
			throws Exception {
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(1, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageThreeCellsVisibleMultiplyOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 24;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(2, 3, 4)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageThreeCellsInvisibleMultiplyOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 24;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(2, 3, 4)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsVisibleDivideOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(6, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsReversedVisibleDivideOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(3, 6)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsVisibleDivideOperator_MathIsNotCorrect()
			throws Exception {
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(3, 1)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsInvisibleDivideOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(6, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsReversedInvisibleDivideOperator_MathIsCorrect()
			throws Exception {
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(3, 6)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(true));
	}

	@Test
	public void checkMathOnEnteredValues_CageTwoCellsInvisibleDivideOperator_MathIsNotCorrect()
			throws Exception {
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mCageBuilder
				.setCells(1, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.checkMathOnEnteredValues(), is(false));
	}

	@Test
	public void invalidateBordersOfAllCells_SetCageIdForAllCells_AllCellsChanged()
			throws Exception {
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);
		Cell cellMock = mock(Cell.class);
		List<Cell> mCells = new ArrayList<Cell>();
		mCells.add(cellMock);
		mCells.add(cellMock);
		mCells.add(cellMock);
		when(mGridMock.getCells(any(int[].class))).thenReturn(mCells);

		cage.invalidateBordersOfAllCells();

		verify(cellMock,times(mCells.size())).invalidateBorders();
	}

	@Test
	public void getIdUpperLeftCell_CageWithMultipleCells_FirstCellReturned()
			throws Exception {
		int firstCellId = 13;
		mCageBuilder
				.setOffsetCellId(firstCellId)
				.setCells(3, 4)
				.setCageOperator(CageOperator.ADD);
		Cage cage = mCageBuilder.build();

		assertThat(cage.getIdUpperLeftCell(), is(firstCellId));
	}

	@Test
	public void getCageText_VisibleOperatorsForCageWithNoOperator_CorrectTextReturned()
			throws Exception {
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = false;
		mCageBuilder
				.setCells(2)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		Cage cage = mCageBuilder.build();

		assertThat(cage.getCageText(), is("3"));
	}

	@Test
	public void getCageText_VisibleOperatorsForCageWithAddOperator_CorrectTextReturned()
			throws Exception {
		Cage cage = setupForGetCageText_VisibleOperators(3, CageOperator.ADD);

		assertThat(cage.getCageText(), is("3+"));
	}

	@Test
	public void getCageText_VisibleOperatorsForCageWithSubtractOperator_CorrectTextReturned()
			throws Exception {
		Cage cage = setupForGetCageText_VisibleOperators(4, CageOperator.SUBTRACT);

		assertThat(cage.getCageText(), is("4-"));
	}

	@Test
	public void getCageText_VisibleOperatorsForCageWithMultiplyOperator_CorrectTextReturned()
			throws Exception {
		Cage cage = setupForGetCageText_VisibleOperators(5, CageOperator.MULTIPLY);

		assertThat(cage.getCageText(), is("5x"));
	}

	@Test
	public void getCageText_VisibleOperatorsForCageWithDivideOperator_CorrectTextReturned()
			throws Exception {
		Cage cage = setupForGetCageText_VisibleOperators(6, CageOperator.DIVIDE);

		assertThat(cage.getCageText(), is("6/"));
	}

	@Test
	public void hasEmptyCells_CageWithNoEmptyCells_True() throws Exception {
		mCageBuilder
				.setCells(2, 1, 4)
				.setCageOperator(CageOperator.ADD);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.hasEmptyCells(), is(false));
	}

	@Test
	public void hasEmptyCells_CageWitFilledAndEmptyCells_True() throws Exception {
		mCageBuilder
				.setCells(2, 0, 3)
				.setCageOperator(CageOperator.ADD);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.hasEmptyCells(), is(true));
	}

	@Test
	public void hasEmptyCells_CageWithOnlyEmptyCells_True() throws Exception {
		mCageBuilder
				.setCells(0, 0, 0)
				.setCageOperator(CageOperator.ADD);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.hasEmptyCells(), is(true));
	}

	@Test
	public void getCell_GridReferenceNotSet_Null() throws Exception {
		Cage cage = mCageBuilder.build();

		assertThat(cage.getCell(0), is(nullValue()));
	}

	@Test
	public void getCell_PositionTooLow_Null() throws Exception {
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.getCell(-1), is(nullValue()));
	}

	@Test
	public void getCell_PositionTooHigh_Null() throws Exception {
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);

		assertThat(cage.getCell(1), is(nullValue()));
	}

	@Test
	public void getCell_ValidPosition_CorrectCellReturned() throws Exception {
		mCageBuilder
				.setCells(2, 0, 3)
				.setCageOperator(CageOperator.ADD);
		Cage cage = mCageBuilder.build();
		cage.setGridReference(mGridMock);
		when(mGridMock.getCell(0)).thenReturn(null);
		when(mGridMock.getCell(1)).thenReturn(mock(Cell.class));
		when(mGridMock.getCell(2)).thenReturn(null);

		assertThat(cage.getCell(1), is(notNullValue()));
	}

	private Cage setupForGetCageText_VisibleOperators(int resultValue, CageOperator cageOperator) {
		boolean hideOperator = false;
		mCageBuilder
				.setCells(2, 3)
				.setResult(resultValue)
				.setCageOperator(cageOperator)
				.setHideOperator(hideOperator);
		return mCageBuilder.build();
	}
}
