package net.mathdoku.plus.tip;

import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.grid.Grid;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class TipCopyCellValuesTest extends TipBaseTest {
	// All fields below are defaulted to a value which result in method
	// toBeDisplayed to return true.
	private Grid gridMock = mock(Grid.class);
	private Cell selectedCellMock = mock(Cell.class);
	private Cage selectedCageMock = mock(Cage.class);
	private int inputModeCopyCounter = 0;
	private int countPossiblesInSelectedGridCell = 3;
	private int numberOfCellsInSelectedCage = 2;
	private boolean hasEmptyCellInSelectedCages = true;

	@Before
	public void setUp() {
		super.setUp();
		long minTimeIntervalBetweenTwoConsecutiveDisplays = 2 * 60 * 60 * 1000;
		super.setUpTimeIntervalBetweenTwoConsecutiveDisplays("CopyCellValues",
				minTimeIntervalBetweenTwoConsecutiveDisplays);
	}

	@Override
	protected void initMocks() {
		super.initMocks();
		when(preferencesMock.getInputModeCopyCounter()).thenReturn(
				inputModeCopyCounter);
		if (gridMock != null) {
			when(gridMock.getSelectedCell()).thenReturn(selectedCellMock);
		}
		if (selectedCellMock != null) {
			when(selectedCellMock.countPossibles()).thenReturn(
					countPossiblesInSelectedGridCell);
		}
		if (gridMock != null && selectedCellMock != null) {
			when(gridMock.getCage(selectedCellMock)).thenReturn(
					selectedCageMock);
		}
		if (selectedCageMock != null) {
			when(selectedCageMock.getNumberOfCells()).thenReturn(
					numberOfCellsInSelectedCage);
			when(selectedCageMock.hasEmptyCells()).thenReturn(
					hasEmptyCellInSelectedCages);
		}
	}

	@Override
	protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
		assertThat(TipCopyCellValues.toBeDisplayed(preferencesMock, gridMock),
				booleanMatcher);
	}

	@Test
	public void toBeDisplayed_CopyModeHasAlreadyBeenUsed_DialogIsNotDisplayed()
			throws Exception {
		inputModeCopyCounter = 10;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_GridIsNull_DialogIsNotDisplayed()
			throws Exception {
		gridMock = null;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_SelectedCellIsNull_DialogIsNotDisplayed()
			throws Exception {
		selectedCellMock = null;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_TooFewPossibleValuesInSelectedCell_DialogIsNotDisplayed()
			throws Exception {
		countPossiblesInSelectedGridCell = 2;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_SelectedCageIsNull_DialogIsNotDisplayed()
			throws Exception {
		selectedCageMock = null;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_TooFewCellsInSelectedCage_DialogIsNotDisplayed()
			throws Exception {
		numberOfCellsInSelectedCage = 1;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_CageHasNoEmptyCellsLeft_DialogIsNotDisplayed()
			throws Exception {
		hasEmptyCellInSelectedCages = false;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}
}
