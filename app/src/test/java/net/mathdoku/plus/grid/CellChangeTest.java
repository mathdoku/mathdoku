package net.mathdoku.plus.grid;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
public class CellChangeTest {
	@Test
	public void restore_CellWithUserValue_UndoForCellIsCalledWithCorrectParameters() {
		int expectedUserValue = 123;
		List<Integer> expectedMaybeValues = new ArrayList<Integer>();

		// Init the Grid Cell Mock
		Cell cellMock = mock(Cell.class);

		// Store current value of cell in a cell change
		CellChange cellChange = new CellChange(cellMock, expectedUserValue,
				expectedMaybeValues);

		// Restore the cell change which ...
		cellChange.restore();

		// ... results in undoing the change to the user value or the maybe
		// values for the cell
		verify(cellMock).undo(expectedUserValue, expectedMaybeValues);
	}

	@Test
	public void restore_CellWithMultipleMaybeValues_UndoForCellIsCalledWithCorrectParameters() {
		int expectedUserValue = 0;
		List<Integer> expectedMaybeValues = new ArrayList<Integer>();
		expectedMaybeValues.add(1);
		expectedMaybeValues.add(2);
		expectedMaybeValues.add(3);

		// Init the Grid Cell Mock
		Cell cellMock = mock(Cell.class);

		// Store current value of cell in a cell change
		CellChange cellChange = new CellChange(cellMock, expectedUserValue,
				expectedMaybeValues);

		// Restore the cell change which ...
		cellChange.restore();

		// ... results in undoing the change to the user value or the maybe
		// values for the cell
		verify(cellMock).undo(expectedUserValue, expectedMaybeValues);
	}
}
