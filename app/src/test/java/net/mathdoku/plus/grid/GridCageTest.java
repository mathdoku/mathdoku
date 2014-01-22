package net.mathdoku.plus.grid;

import net.mathdoku.plus.enums.CageOperator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridCageTest {
	private GridCage mGridCage = new GridCage();
	private GridCell mGridCell[];

	@Before
	public void setup() {
	}

	@Test
	public void setCageResults_CageWithGridCellListIsNull_CageInitialized()
			throws Exception {
		mGridCage.mCells = null;
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = false; // value is irrelevant for 1 cell cage

		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.getResult(), is(resultValue));
		assertThat(mGridCage.getOperator(), is(cageOperator));
		assertThat(mGridCage.isOperatorHidden(), is(hideOperator));
	}

	@Test
	public void setCageResults_CageWithSingleCellWhichHasNoOperator_ResultIsSet()
			throws Exception {
		addNumberOfGridCellMocks(1);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = false; // value is irrelevant for 1 cell cage

		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.getResult(), is(resultValue));
		assertThat(mGridCage.getOperator(), is(cageOperator));
		assertThat(mGridCage.isOperatorHidden(), is(hideOperator));
		verify(mGridCell[0]).setCageText("5");
	}

	@Test
	public void setCageResults_CageWithMultipleCellsAndVisibleOperator_ResultIsSet()
			throws Exception {
		addNumberOfGridCellMocks(2);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = false;

		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.getResult(), is(resultValue));
		assertThat(mGridCage.getOperator(), is(cageOperator));
		assertThat(mGridCage.isOperatorHidden(), is(hideOperator));
		verify(mGridCell[0]).setCageText("5+");
		verify(mGridCell[1], never()).setCageText(anyString());
	}

	@Test
	public void setCageResults_CageWithMultipleCellsAndHiddenOperator_ResultIsSet()
			throws Exception {
		addNumberOfGridCellMocks(2);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = true;

		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.getResult(), is(resultValue));
		assertThat(mGridCage.getOperator(), is(cageOperator));
		assertThat(mGridCage.isOperatorHidden(), is(hideOperator));
		verify(mGridCell[0]).setCageText("5");
		verify(mGridCell[1], never()).setCageText(anyString());
	}

	@Test
	public void revealOperator_RevealsAnOperatorFromACageWithHiddenOperator_Success()
			throws Exception {
		addNumberOfGridCellMocks(2);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);
		assertThat(mGridCage.isOperatorHidden(), is(true));
		verify(mGridCell[0]).setCageText("5");

		mGridCage.revealOperator();

		assertThat(mGridCage.isOperatorHidden(), is(false));
		verify(mGridCell[0]).setCageText("5/");
	}

	@Test
	public void setCageId_GridCellListIsNull_NoNullPointerExceptionThrown() throws Exception {
		int cageId = 4;
		mGridCage.mCells = null;
		mGridCage.setCageId(cageId);
	}

	@Test
	public void setCageId_SetCageIdForAllCells_AllCellsChanged() throws Exception {
		addNumberOfGridCellMocks(3);
		int cageId = 4;

		mGridCage.setCageId(cageId);

		verify(mGridCell[0]).setCageId(cageId);
		verify(mGridCell[1]).setCageId(cageId);
		verify(mGridCell[2]).setCageId(cageId);
	}

	@Test
	public void checkUserMath_CageGridCellListIsNull_UserMathIsCorrect() throws Exception {
		mGridCage.mCells = null;
		assertThat(mGridCage.checkUserMath(), is(false));
	}

	@Test
	public void checkUserMath_CageWithoutCells_UserMathIsCorrect() throws Exception {
		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageIncorrectNumberOfCellsVisibleNoneOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageHasTooManyCellsInvisibleNoneOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}	
	@Test
	public void checkUserMath_CageHasTooManyCellsVisibleNoneOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3);
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageOneCellVisibleNoneOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageOneCellInvisibleNoneOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageOneCellInvisibleNoneOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.NONE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooLittleCellsVisibleAddOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooLittleCellsInvisibleAddOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTwoCellsVisibleAddOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsVisibleAddOperator_UserMathIsNotCorrect() throws Exception {
		addGridCellMocksWithUserValue(1, 3);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTwoCellsInvisibleAddOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsInvisibleAddOperator_UserMathIsNotCorrect() throws Exception {
		addGridCellMocksWithUserValue(1, 3);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageThreeCellsVisibleAddOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4);
		int resultValue = 9;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageThreeCellsInvisibleAddOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4);
		int resultValue = 9;
		CageOperator cageOperator = CageOperator.ADD;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTooLittleCellsVisibleSubtractOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooLittleCellsInvisibleSubtractOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTwoCellsVisibleSubtractOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 2);
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsReversedVisibleSubtractOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3);
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsVisibleSubtractOperator_UserMathIsNotCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 1);
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTwoCellsInvisibleSubtractOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 2);
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsReversedInvisibleSubtractOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3);
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsInvisibleSubtractOperator_UserMathIsNotCorrect() throws Exception {
		addGridCellMocksWithUserValue(1, 3);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooManyCellsVisibleSubtractOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4, 5);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooManyCellsInvisibleSubtractOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4, 5);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.SUBTRACT;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooLittleCellsVisibleMultiplyOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooLittleCellsInvisibleMultiplyOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTwoCellsVisibleMultiplyOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 2);
		int resultValue = 6;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsVisibleMultiplyOperator_UserMathIsNotCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 1);
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTwoCellsInvisibleMultiplyOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 2);
		int resultValue = 6;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsInvisibleMultiplyOperator_UserMathIsNotCorrect() throws Exception {
		addGridCellMocksWithUserValue(1, 3);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageThreeCellsVisibleMultiplyOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4);
		int resultValue = 24;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageThreeCellsInvisibleMultiplyOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4);
		int resultValue = 24;
		CageOperator cageOperator = CageOperator.MULTIPLY;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTooLittleCellsVisibleDivideOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooLittleCellsInvisibleDivideOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTwoCellsVisibleDivideOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(6, 3);
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsReversedVisibleDivideOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 6);
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsVisibleDivideOperator_UserMathIsNotCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 1);
		int resultValue = 1;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTwoCellsInvisibleDivideOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(6, 3);
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsReversedInvisibleDivideOperator_UserMathIsCorrect() throws Exception {
		addGridCellMocksWithUserValue(3, 6);
		int resultValue = 2;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(true));
	}

	@Test
	public void checkUserMath_CageTwoCellsInvisibleDivideOperator_UserMathIsNotCorrect() throws Exception {
		addGridCellMocksWithUserValue(1, 3);
		int resultValue = 5;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooManyCellsVisibleDivideOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4, 5);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = false;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void checkUserMath_CageTooManyCellsInvisibleDivideOperator_UserMathIsIncorrect() throws Exception {
		addGridCellMocksWithUserValue(2, 3, 4, 5);
		int resultValue = 3;
		CageOperator cageOperator = CageOperator.DIVIDE;
		boolean hideOperator = true;
		mGridCage.setCageResults(resultValue, cageOperator, hideOperator);

		assertThat(mGridCage.checkUserMath(),is(false));
	}

	@Test
	public void setBorders_GridCellListIsNull_NoNullPointerExceptionThrown() throws Exception {
		mGridCage.mCells = null;
		mGridCage.setBorders();
	}

	@Test
	public void setBorders_SetCageIdForAllCells_AllCellsChanged() throws Exception {
		addNumberOfGridCellMocks(3);

		mGridCage.setBorders();

		verify(mGridCell[0]).setBorders();
		verify(mGridCell[1]).setBorders();
		verify(mGridCell[2]).setBorders();
	}

	private void addNumberOfGridCellMocks(int numberOfCells) {
		mGridCell = new GridCell[numberOfCells];
		for(int i = 0; i < numberOfCells; i++) {
			mGridCell[i] = mock(GridCell.class);
			mGridCage.mCells.add(mGridCell[i]);
		}
	}

	private void addGridCellMocksWithUserValue(int... userValue) {
		mGridCell = new GridCell[userValue.length];
		for(int i = 0; i < userValue.length; i++) {
			mGridCell[i] = mock(GridCell.class);
			when(mGridCell[i].getUserValue()).thenReturn(userValue[i]);
			when(mGridCell[i].isUserValueSet()).thenReturn(true);
			mGridCage.mCells.add(mGridCell[i]);
		}
	}
}
