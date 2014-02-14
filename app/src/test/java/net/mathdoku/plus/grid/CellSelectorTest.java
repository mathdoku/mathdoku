package net.mathdoku.plus.grid;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CellSelectorTest {

	@Test(expected = InvalidParameterException.class)
	public void find_CreateGridSelectorWithNullList_InvalidParameterException() {
		CellSelector cellSelector = new CellSelector(null) {

			@Override
			public boolean select(Cell cell) {
				return false;
			}
		};
	}

	@Test
	public void find_CreateGridSelectorWithEmptyList_NoCellsFound() {
		List<Cell> cellArrayList = new ArrayList<Cell>();

		CellSelector cellSelector = new CellSelector(cellArrayList) {

			@Override
			public boolean select(Cell cell) {
				return true;
			}
		};

		List<Cell> resultCellList = cellSelector.find();
		List<Cell> expectedCellList = new ArrayList<Cell>();

		assertEquals("Selected cell list", expectedCellList, resultCellList);
	}

	@Test
	public void find_CreateGridSelectorWithEmptyList_CellsFound() {
		final int duplicatedUserValue = 3;

		// Stubs for the grid Cells. Note that stub 2 and 4 contain the same
		// user value
		Cell cellStub1 = mock(Cell.class);
		when(cellStub1.getUserValue()).thenReturn(duplicatedUserValue - 1);

		Cell cellStub2 = mock(Cell.class);
		when(cellStub2.getUserValue()).thenReturn(duplicatedUserValue);

		Cell cellStub3 = mock(Cell.class);
		when(cellStub3.getUserValue()).thenReturn(duplicatedUserValue + 1);

		Cell cellStub4 = mock(Cell.class);
		when(cellStub4.getUserValue()).thenReturn(duplicatedUserValue);

		// Add the the subbed grid cells to the list of cells.
		List<Cell> cellArrayList = new ArrayList<Cell>();
		cellArrayList.add(cellStub1);
		cellArrayList.add(cellStub2);
		cellArrayList.add(cellStub3);
		cellArrayList.add(cellStub4);

		// Create the grid cell selector
		CellSelector cellSelector = new CellSelector(cellArrayList) {
			@Override
			public boolean select(Cell cell) {
				return (cell.getUserValue() == duplicatedUserValue);
			}
		};

		List<Cell> expectedCellList = new ArrayList<Cell>();
		expectedCellList.add(cellStub2);
		expectedCellList.add(cellStub4);

		List<Cell> resultCellList = cellSelector.find();

		assertEquals("Selected cell list", expectedCellList, resultCellList);
	}
}
