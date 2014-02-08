package net.mathdoku.plus.grid;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(value = RobolectricGradleTestRunner.class)
public class GridCellSelectorInRowOrColumnTest {
	@Test
	public void select_CellWithIncorrectRowAndIncorrectColumn_False()
			throws Exception {
		int row = 1;
		int column = 2;

		// The actual content of the grid cell list is not relevant for this
		// test. An empty list is ok in order to initialize the
		// GridCellSelectorInRowOrColumn.
		List<GridCell> gridCellArrayList = new ArrayList<GridCell>();
		GridCellSelectorInRowOrColumn GridCellSelectorInRowOrColumn = new GridCellSelectorInRowOrColumn(
				gridCellArrayList, row, column);

		// Create grid cell stub with an incorrect row and incorrect column
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getRow()).thenReturn(row + 1);
		when(gridCellStub.getColumn()).thenReturn(column + 1);
		gridCellArrayList.add(gridCellStub);

		boolean expectedSelect = false;
		boolean resultSelect = GridCellSelectorInRowOrColumn
				.select(gridCellStub);
		assertEquals("Is GridCell selected?", expectedSelect, resultSelect);
	}

	@Test
	public void select_CellWithCorrectRowAndIncorrectColumn_True()
			throws Exception {
		int row = 1;
		int column = 2;

		// The actual content of the grid cell list is not relevant for this
		// test. An empty list is ok in order to initialize the
		// GridCellSelectorInRowOrColumn.
		List<GridCell> gridCellArrayList = new ArrayList<GridCell>();
		GridCellSelectorInRowOrColumn GridCellSelectorInRowOrColumn = new GridCellSelectorInRowOrColumn(
				gridCellArrayList, row, column);

		// Create grid cell stub with a correct row but incorrect column
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getRow()).thenReturn(row);
		when(gridCellStub.getColumn()).thenReturn(column + 1);
		gridCellArrayList.add(gridCellStub);

		boolean expectedSelect = true;
		boolean resultSelect = GridCellSelectorInRowOrColumn
				.select(gridCellStub);
		assertEquals("Is GridCell selected?", expectedSelect, resultSelect);
	}

	@Test
	public void select_CellWithIncorrectRowAndCorrectColumn_True()
			throws Exception {
		int row = 1;
		int column = 2;

		// The actual content of the grid cell list is not relevant for this
		// test. An empty list is ok in order to initialize the
		// GridCellSelectorInRowOrColumn.
		List<GridCell> gridCellArrayList = new ArrayList<GridCell>();
		GridCellSelectorInRowOrColumn GridCellSelectorInRowOrColumn = new GridCellSelectorInRowOrColumn(
				gridCellArrayList, row, column);

		// Create grid cell stub with am incorrect row and correct column
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getRow()).thenReturn(row + 1);
		when(gridCellStub.getColumn()).thenReturn(column);
		gridCellArrayList.add(gridCellStub);

		boolean expectedSelect = true;
		boolean resultSelect = GridCellSelectorInRowOrColumn
				.select(gridCellStub);
		assertEquals("Is GridCell selected?", expectedSelect, resultSelect);
	}

	@Test
	public void select_CellWithCorrectUserValueAndCorrectRowAndCorrectColumn_True()
			throws Exception {
		int row = 1;
		int column = 2;

		// The actual content of the grid cell list is not relevant for this
		// test. An empty list is ok in order to initialize the
		// GridCellSelectorInRowOrColumn.
		List<GridCell> gridCellArrayList = new ArrayList<GridCell>();
		GridCellSelectorInRowOrColumn GridCellSelectorInRowOrColumn = new GridCellSelectorInRowOrColumn(
				gridCellArrayList, row, column);

		// Create grid cell stub with a correct row and column
		GridCell gridCellStub = mock(GridCell.class);
		when(gridCellStub.getRow()).thenReturn(row);
		when(gridCellStub.getColumn()).thenReturn(column);
		gridCellArrayList.add(gridCellStub);

		boolean expectedSelect = true;
		boolean resultSelect = GridCellSelectorInRowOrColumn
				.select(gridCellStub);
		assertEquals("Is GridCell selected?", expectedSelect, resultSelect);
	}
}
