package net.mathdoku.plus.puzzle.cell;

import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.statistics.GridStatistics;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CellTest {
    CellBuilder mCellBuilder;
    int mGridSize = 4;
    Grid mGridMock;
    GridStatistics mGridStatisticsMock;

    @Before
    public void setup() {
        // Setup Grid mock. Tests which uses this mock should also call
        // setGridReference(mGridMock)
        mGridMock = mock(Grid.class);
        when(mGridMock.getGridSize()).thenReturn(mGridSize);
        mGridStatisticsMock = mock(GridStatistics.class);
        when(mGridMock.getGridStatistics()).thenReturn(mGridStatisticsMock);

        // Setup a cell which can be build without errors.
        mCellBuilder = new CellBuilder();
        mCellBuilder.setGridSize(mGridSize)
                .setId(1)
                .setCorrectValue(3)
                .setCageId(2);
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_GridSizeTooLow_CellNotCreated() throws Exception {
        mGridSize = 0;
        mCellBuilder.setGridSize(mGridSize);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_IdIsInvalid_CellNotCreated() throws Exception {
        mCellBuilder.setId(-1);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_EnteredValueTooLow_CellNotCreated() throws Exception {
        mCellBuilder.setEnteredValue(-1);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_EnteredValueTooHigh_CellNotCreated() throws Exception {
        mCellBuilder.setEnteredValue(mGridSize + 1);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_PossiblesIsNull_CellNotCreated() throws Exception {
        mCellBuilder.setPossibles(null);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_PossiblesContainsATooLowValue_CellNotCreated() throws Exception {
        List<Integer> possibles = new ArrayList<Integer>();
        possibles.add(0);
        mCellBuilder.setPossibles(possibles);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_PossiblesContainsATooHighValue_CellNotCreated() throws Exception {
        List<Integer> possibles = new ArrayList<Integer>();
        possibles.add(mGridSize + 1);
        mCellBuilder.setPossibles(possibles);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_CorrectValueTooLow_CellNotCreated() throws Exception {
        mCellBuilder.setCorrectValue(0);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_CorrectValueTooHigh_CellNotCreated() throws Exception {
        mCellBuilder.setCorrectValue(mGridSize + 1);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_CorrectValueTooLowWhenLenientCorrectValueCheckIsEnabled_CellNotCreated() throws
            Exception {
        mCellBuilder.setCorrectValue(-1)
                .setLenientCheckCorrectValueOnBuild();

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_CorrectValueTooHighWhenLenientCorrectValueCheckIsEnabled_CellNotCreated() throws
            Exception {
        mCellBuilder.setCorrectValue(mGridSize + 1)
                .setLenientCheckCorrectValueOnBuild();

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test
    public void cellConstructor_CorrectValueIsZeroWhenLenientCorrectValueCheckIsEnabled_CellCreated() throws Exception {
        mCellBuilder.setCorrectValue(0)
                .setLenientCheckCorrectValueOnBuild();

        assertThat(mCellBuilder.build(), is(notNullValue()));
    }

    @Test(expected = InvalidGridException.class)
    public void cellConstructor_CageIdIsInvalid_CellNotCreated() throws Exception {
        mCellBuilder.setCageId(-1);

        assertThat(mCellBuilder.build(), is(nullValue()));
    }

    @Test
    public void cellConstructor_CageIdIsNotSetWhenSkipCageCheckOnBuildIsSet_CellCreated() throws Exception {
        mCellBuilder.setCageId(-1)
                .setSkipCheckCageReferenceOnBuild();

        assertThat(mCellBuilder.build(), is(notNullValue()));
    }

    @Test
    public void addPossible_DigitIsTooLow_DigitNotAdded() throws Exception {
        Cell cell = mCellBuilder.build();

        assertThat(cell.addPossible(0), is(false));
    }

    @Test
    public void addPossible_DigitIsTooHigh_DigitNotAdded() throws Exception {
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);

        assertThat(cell.addPossible(mGridSize + 1), is(false));
    }

    @Test
    public void addPossible_DigitWasAlreadyAddedBefore_DigitNotAdded() throws Exception {
        int possible = 1;
        Cell cell = mCellBuilder.setPossibles(createListOfPossibleValues(possible))
                .build();
        cell.setGridReference(mGridMock);

        assertThat(cell.addPossible(possible), is(false));
    }

    @Test
    public void addPossible_MultipleDigitsAdded_PossiblesAreSorted() throws Exception {
        int possibleLow = 1;
        int possibleHigh = possibleLow + 1;
        Cell cell = mCellBuilder.setPossibles(createListOfPossibleValues(possibleHigh))
                .build();
        cell.setGridReference(mGridMock);

        // Add a possible with a lower value than the existing possible
        cell.addPossible(possibleLow);

        List<Integer> sortedList = new ArrayList<Integer>();
        sortedList.add(possibleLow);
        sortedList.add(possibleHigh);

        assertThat(cell.getPossibles(), is(sortedList));
    }

    @Test
    public void addPossible_AddOnePossible_StatisticsUpdated() throws Exception {
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);

        assertThat(cell.addPossible(2), is(true));

        verify(mGridStatisticsMock).increaseCounter(GridStatistics.StatisticsCounterType.POSSIBLES);
    }

    @Test
    public void removePossible_removeValueWhichWasNotAddedAsPossible_NoValueRemoved() throws Exception {
        Cell cell = mCellBuilder.build();

        assertThat(cell.removePossible(3), is(false));
    }

    @Test
    public void removePossible_removeValueWhichWasAddedAsPossible_NoValueRemoved() throws Exception {
        int possibleToRemove = 3;
        Cell cell = mCellBuilder.setPossibles(createListOfPossibleValues(possibleToRemove))
                .build();
        cell.setGridReference(mGridMock);

        assertThat(cell.removePossible(possibleToRemove), is(true));
    }

    @Test
    public void hasEnteredValueSet_ValueIsSet_True() throws Exception {
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);
        int enteredValue = 1;
        cell.setEnteredValue(enteredValue);

        assertThat(cell.hasEnteredValue(), is(true));
    }

    @Test
    public void hasEnteredValueSet_ValueIsZero_False() throws Exception {
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);
        int enteredValue = Cell.NO_ENTERED_VALUE;
        cell.setEnteredValue(enteredValue);

        assertThat(cell.hasEnteredValue(), is(false));
    }

    @Test
    public void setEnteredValue_DigitIsTooLow_ValueNotSet() throws Exception {
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);

        assertThat(cell.setEnteredValue(-1), is(false)); // Note: 0 is a valid
        // user
        // value!
    }

    @Test
    public void setEnteredValue_DigitIsTooHigh_ValueNotSet() throws Exception {
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);

        assertThat(cell.setEnteredValue(mGridSize + 1), is(false));
    }

    @Test
    public void setEnteredValue_EnteredValueIsReplacedWithSameValue_ValueNotSet() throws Exception {
        int enteredValue = 2;
        Cell cell = mCellBuilder.setEnteredValue(enteredValue)
                .build();
        cell.setGridReference(mGridMock);

        assertThat(cell.setEnteredValue(enteredValue), is(false));
    }

    @Test
    public void setEnteredValue_CellContainsPossibleValue_PossiblesAreCleared() throws Exception {
        Cell cell = mCellBuilder.setPossibles(createListOfPossibleValues(1, 3))
                .build();
        cell.setGridReference(mGridMock);

        assertThat(cell.setEnteredValue(1), is(true));
        assertThat(cell.countPossibles(), is(0));
    }

    @Test
    public void setEnteredValue_EnteredValueEnteredInEmptyCell_StatisticsCellValueReplacedNotIncreased() throws
            Exception {
        setupSetEnteredValueTest_ReplaceEnteredValue(0, 2);
        verify(mGridStatisticsMock, never()).increaseCounter(GridStatistics.StatisticsCounterType.USER_VALUE_REPLACED);
    }

    @Test
    public void setEnteredValue_CellWithEnteredValueIsCleared_StatisticsCellValueReplacedNotIncreased() throws
            Exception {
        setupSetEnteredValueTest_ReplaceEnteredValue(2, 0);
        verify(mGridStatisticsMock, never()).increaseCounter(GridStatistics.StatisticsCounterType.USER_VALUE_REPLACED);
    }

    @Test
    public void setEnteredValue_EnteredValueIsReplacedWithOtherValue_StatisticsCellValueReplacedIncreased() throws
            Exception {
        setupSetEnteredValueTest_ReplaceEnteredValue(2, 3);
        verify(mGridStatisticsMock).increaseCounter(GridStatistics.StatisticsCounterType.USER_VALUE_REPLACED);
    }

    @Test
    public void setEnteredValue_EnteredValueEnteredInEmptyCell_StatisticsCellsEmptyIsDecreased() throws Exception {
        setupSetEnteredValueTest_ReplaceEnteredValue(0, 2);
        verify(mGridStatisticsMock).decreaseCounter(GridStatistics.StatisticsCounterType.CELLS_EMPTY);
    }

    @Test
    public void setEnteredValue_EnteredValueEnteredInEmptyCell_StatisticsCellsFilledIsIncreased() throws Exception {
        setupSetEnteredValueTest_ReplaceEnteredValue(0, 2);
        verify(mGridStatisticsMock).increaseCounter(GridStatistics.StatisticsCounterType.CELLS_FILLED);
    }

    @Test
    public void setEnteredValue_CellWithEnteredValueIsCleared_StatisticsCellsFilledIsDecreased() throws Exception {
        setupSetEnteredValueTest_ReplaceEnteredValue(2, 0);
        verify(mGridStatisticsMock).decreaseCounter(GridStatistics.StatisticsCounterType.CELLS_FILLED);
    }

    @Test
    public void setEnteredValue_CellWithEnteredValueIsCleared_StatisticsCellsEmptyIsIncreased() throws Exception {
        setupSetEnteredValueTest_ReplaceEnteredValue(2, 0);
        verify(mGridStatisticsMock).increaseCounter(GridStatistics.StatisticsCounterType.CELLS_EMPTY);
    }

    @Test
    public void setEnteredValue_RevealedCellValueIsChanged_StatisticsCellsEmptyNotUpdated() throws Exception {
        Cell cell = mCellBuilder.setRevealed(true)
                .build();
        cell.setGridReference(mGridMock);

        int newEnteredValue = 1;
        assertThat(cell.setEnteredValue(newEnteredValue), is(true));

        verify(mGridStatisticsMock, never()).increaseCounter(GridStatistics.StatisticsCounterType.CELLS_EMPTY);
    }

    @Test
    public void setEnteredValue_RevealedCellValueIsChanged_StatisticsCellsFilledNotUpdated() throws Exception {
        Cell cell = mCellBuilder.setRevealed(true)
                .build();
        cell.setGridReference(mGridMock);

        int newEnteredValue = 1;
        assertThat(cell.setEnteredValue(newEnteredValue), is(true));

        verify(mGridStatisticsMock, never()).increaseCounter(GridStatistics.StatisticsCounterType.CELLS_FILLED);
    }

    @Test
    public void setEnteredValue_CellValueIsChanged_InvalidEnteredValueHighlightIsReset() throws Exception {
        Cell cell = mCellBuilder.setInvalidValueHighlight(true)
                .build();
        cell.setGridReference(mGridMock);
        assertThat(cell.hasInvalidValueHighlight(), is(true));

        int newEnteredValue = 1;
        assertThat(cell.setEnteredValue(newEnteredValue), is(true));

        assertThat(cell.hasInvalidValueHighlight(), is(false));
    }

    @Test
    public void setEnteredValue_CellValueIsChanged_DuplicateValueHighlightIsReset() throws Exception {
        Cell cell = mCellBuilder.setDuplicateValueHighlight(true)
                .build();
        cell.setGridReference(mGridMock);
        assertThat(cell.isDuplicateValueHighlighted(), is(true));

        int newEnteredValue = 1;
        assertThat(cell.setEnteredValue(newEnteredValue), is(true));

        assertThat(cell.isDuplicateValueHighlighted(), is(false));
    }

    @Test
    public void setEnteredValue_CellValueIsChangedButGridNotYetSolved_GridNotSolved() throws Exception {
        Cell cell = mCellBuilder.build();
        when(mGridMock.isSolved()).thenReturn(false);
        cell.setGridReference(mGridMock);

        int newEnteredValue = 1;
        assertThat(cell.setEnteredValue(newEnteredValue), is(true));

        verify(mGridMock).isSolved();
        verify(mGridMock, never()).setSolved();
    }

    @Test
    public void setEnteredValue_CellValueIsChangedAndGridIsSolved_GridSolved() throws Exception {
        Cell cell = mCellBuilder.build();
        when(mGridMock.isSolved()).thenReturn(true);
        cell.setGridReference(mGridMock);

        int newEnteredValue = 1;
        assertThat(cell.setEnteredValue(newEnteredValue), is(true));

        verify(mGridMock).isSolved();
        verify(mGridMock).setSolved();
    }

    @Test
    public void isBordersInvalidated_BorderHaveBeenInvalidated_True() throws Exception {
        Cell cell = mCellBuilder.build();
        cell.invalidateBorders();

        // First call should confirm that the borders have been invalidated.
        assertThat(cell.isBordersInvalidated(), is(true));

        // The second call should confirm that the borders have NIT been
        // invalidated since last call.
        assertThat(cell.isBordersInvalidated(), is(false));
    }

    @Test
    public void clearValue_CellWithEnteredValue_ValueIsCleared() throws Exception {
        int oldEnteredValue = 1;
        Cell cell = mCellBuilder.setEnteredValue(oldEnteredValue)
                .build();
        cell.setGridReference(mGridMock);

        cell.clearValue();

        assertThat(cell.getEnteredValue(), is(0));
    }

    @Test
    public void isEnteredValueIncorrect_CorrectValueAndEnteredValueAreDifferent_True() throws Exception {
        int correctValue = 1;
        Cell cell = mCellBuilder.setCorrectValue(correctValue)
                .setEnteredValue(correctValue + 1)
                .build();
        cell.setGridReference(mGridMock);

        assertThat(cell.isEnteredValueIncorrect(), is(true));
    }

    @Test
    public void isEnteredValueIncorrect_CorrectValueAndEnteredValueAreIdentical_False() throws Exception {
        int correctValue = 1;
        Cell cell = mCellBuilder.setCorrectValue(correctValue)
                .setEnteredValue(correctValue)
                .build();
        cell.setGridReference(mGridMock);

        assertThat(cell.isEnteredValueIncorrect(), is(false));
    }

    @Test
    public void hasPossible_CellHasNoPossibles_PossibleNotFound() throws Exception {
        Cell cell = mCellBuilder.build();

        assertThat(cell.hasPossible(1), is(false));
    }

    @Test
    public void hasPossible_CellHasPossiblesButNotTheRequestedValue_PossibleNotFound() throws Exception {
        Cell cell = mCellBuilder.setPossibles(createListOfPossibleValues(2, 3, 4))
                .build();

        assertThat(cell.hasPossible(1), is(false));
    }

    @Test
    public void hasPossible_CellHasPossiblesIncludingTheRequestedValue_PossibleNotFound() throws Exception {
        Cell cell = mCellBuilder.setPossibles(createListOfPossibleValues(1, 2, 4))
                .build();

        assertThat(cell.hasPossible(2), is(true));
    }

    @Test
    public void revealCorrectValue_RevealedCellContainsEnteredValue_StatisticsFilledCellsUpdated() throws Exception {
        Cell cell = mCellBuilder.setEnteredValue(1)
                .build();
        cell.setGridReference(mGridMock);

        cell.revealCorrectValue();

        verify(mGridStatisticsMock).decreaseCounter(GridStatistics.StatisticsCounterType.CELLS_FILLED);
    }

    @Test
    public void revealCorrectValue_RevealedCellContainsNoEnteredValue_StatisticsEmptyCellsUpdated() throws Exception {
        Cell cell = mCellBuilder.setEnteredValue(0)
                .build();
        cell.setGridReference(mGridMock);

        cell.revealCorrectValue();

        verify(mGridStatisticsMock).decreaseCounter(GridStatistics.StatisticsCounterType.CELLS_EMPTY);
    }

    @Test
    public void revealCorrectValue_RevealedCell_StatisticsRevealedCellsUpdated() throws Exception {
        // For this test it is not relevant whether the cell contains a user
        // value or not.
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);

        cell.revealCorrectValue();

        verify(mGridStatisticsMock).increaseCounter(GridStatistics.StatisticsCounterType.CELLS_REVEALED);
    }

    @Test
    public void revealCorrectValue_RevealedCell_RevealIndicatorIsSet() throws Exception {
        // For this test it is not relevant whether the cell contains a user
        // value or not.
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);

        cell.revealCorrectValue();

        assertThat(cell.isEnteredValueIncorrect(), is(false));
    }

    @Test
    public void revealCorrectValue_RevealedCell_EnteredValueUpdatedToCorrectValue() throws Exception {
        int correctValue = 1;
        // For this test it is not relevant whether the cell contains a user
        // value or not. In case it contains a user value, it should not be
        // equal to the correct value as we need proof that the value is
        // changed.
        int enteredValue = correctValue + 1;
        Cell cell = mCellBuilder.setCorrectValue(correctValue)
                .setEnteredValue(enteredValue)
                .build();
        cell.setGridReference(mGridMock);

        cell.revealCorrectValue();

        assertThat(cell.getEnteredValue(), is(correctValue));
    }

    @Test
    public void isCellInSelectedCage_NoCellIsSelected_False() throws Exception {
        Cell cell = mCellBuilder.build();
        cell.setGridReference(mGridMock);
        when(mGridMock.getSelectedCell()).thenReturn(null);

        assertThat(cell.isCellInSelectedCage(), is(false));
    }

    @Test
    public void isCellInSelectedCage_SelectedCellIsInOtherCage_False() throws Exception {
        int cageId = 12;
        Cell cell = mCellBuilder.setCageId(cageId)
                .build();
        Cell selectedCellMock = mock(Cell.class);
        cell.setGridReference(mGridMock);
        when(mGridMock.getSelectedCell()).thenReturn(selectedCellMock);
        int cageIdSelectedCell = cageId + 1;
        when(selectedCellMock.getCageId()).thenReturn(cageIdSelectedCell);

        assertThat(cell.isCellInSelectedCage(), is(false));
    }

    @Test
    public void isCellInSelectedCage_SelectedCellIsInSameCage_True() throws Exception {
        int cageId = 12;
        Cell cell = mCellBuilder.setCageId(cageId)
                .build();
        Cell selectedCellMock = mock(Cell.class);
        cell.setGridReference(mGridMock);
        when(mGridMock.getSelectedCell()).thenReturn(selectedCellMock);
        when(selectedCellMock.getCageId()).thenReturn(cageId);

        assertThat(cell.isCellInSelectedCage(), is(true));
    }

    @Test
    public void isEmpty_CellHasNoEnteredValueAndNoPossibleValues_True() throws Exception {
        Cell cell = mCellBuilder.setEnteredValue(0)
                .setPossibles(createListOfPossibleValues())
                .build();

        assertThat(cell.isEmpty(), is(true));
    }

    @Test
    public void isEmpty_CellHasEnteredValueButNoPossibleValues_False() throws Exception {
        Cell cell = mCellBuilder.setEnteredValue(1)
                .setPossibles(createListOfPossibleValues())
                .build();

        assertThat(cell.isEmpty(), is(false));
    }

    @Test
    public void isEmpty_CellHasNoEnteredValueButHasPossibleValues_False() throws Exception {
        Cell cell = mCellBuilder.setEnteredValue(0)
                .setPossibles(createListOfPossibleValues(1, 2, 3))
                .build();

        assertThat(cell.isEmpty(), is(false));
    }

    @Test
    public void isEmpty_CellHasEnteredValueAndPossibleValues_False() throws Exception {
        Cell cell = mCellBuilder.setEnteredValue(1)
                .setPossibles(createListOfPossibleValues(1, 2))
                .build();

        assertThat(cell.isEmpty(), is(false));
    }

    private List<Integer> createListOfPossibleValues(Integer... possibles) {
        List<Integer> list = new ArrayList<Integer>();
        for (int possible : possibles) {
            list.add(possible);
        }
        return list;
    }

    private void setupSetEnteredValueTest_ReplaceEnteredValue(int oldEnteredValue, int newEnteredValue) {
        Cell cell = mCellBuilder.setEnteredValue(oldEnteredValue)
                .build();
        cell.setGridReference(mGridMock);

        assertThat(cell.setEnteredValue(newEnteredValue), is(true));
    }
}
