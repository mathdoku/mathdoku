package net.mathdoku.plus.grid;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridCellSelectorTest {

	@Test(expected = InvalidParameterException.class)
	public void find_CreateGridSelectorWithNullList_InvalidParameterException() {
		GridCellSelector gridCellSelector = new GridCellSelector(null) {

			@Override
			public boolean select(GridCell gridCell) {
				return false;
			}
		};
	}

	@Test
	public void find_CreateGridSelectorWithEmptyList_NoCellsFound() {
		ArrayList<GridCell> gridCellArrayList = new ArrayList<GridCell>();

		GridCellSelector gridCellSelector = new GridCellSelector(gridCellArrayList) {

			@Override
			public boolean select(GridCell gridCell) {
				return true;
			}
		};

		ArrayList<GridCell> resultGridCellList = gridCellSelector.find();
		ArrayList<GridCell> expectedGridCellList = new ArrayList<GridCell>();

		assertEquals("Selected gridCell list", expectedGridCellList, resultGridCellList);
	}

	@Test
	public void find_CreateGridSelectorWithEmptyList_CellsFound() {
		final int duplicatedUserValue = 3;

		// Stubs for the grid Cells. Note that stub 2 and 4 contain the same user value
		GridCell gridCellStub1 = mock(GridCell.class);
		when(gridCellStub1.getUserValue()).thenReturn(duplicatedUserValue - 1);

		GridCell gridCellStub2 = mock(GridCell.class);
		when(gridCellStub2.getUserValue()).thenReturn(duplicatedUserValue);

		GridCell gridCellStub3 = mock(GridCell.class);
		when(gridCellStub3.getUserValue()).thenReturn(duplicatedUserValue + 1);

		GridCell gridCellStub4 = mock(GridCell.class);
		when(gridCellStub4.getUserValue()).thenReturn(duplicatedUserValue);

		// Add the the subbed grid cells to the list of cells.
		ArrayList<GridCell> gridCellArrayList = new ArrayList<GridCell>();
		gridCellArrayList.add(gridCellStub1);
		gridCellArrayList.add(gridCellStub2);
		gridCellArrayList.add(gridCellStub3);
		gridCellArrayList.add(gridCellStub4);

		// Create the grid cell selector
		GridCellSelector gridCellSelector = new GridCellSelector(gridCellArrayList) {
			@Override
			public boolean select(GridCell gridCell) {
				return (gridCell.getUserValue() == duplicatedUserValue);
			}
		};

		ArrayList<GridCell> expectedGridCellList = new ArrayList<GridCell>();
		expectedGridCellList.add(gridCellStub2);
		expectedGridCellList.add(gridCellStub4);

		ArrayList<GridCell> resultGridCellList = gridCellSelector.find();

		assertEquals("Selected gridCell list", expectedGridCellList, resultGridCellList);
	}
}
