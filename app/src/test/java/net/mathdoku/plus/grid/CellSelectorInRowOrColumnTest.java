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
public class CellSelectorInRowOrColumnTest {
	@Test
	public void select_CellWithIncorrectRowAndIncorrectColumn_False()
			throws Exception {
		int row = 1;
		int column = 2;

		// The actual content of the grid cell list is not relevant for this
		// test. An empty list is ok in order to initialize the
		// CellSelectorInRowOrColumn.
		List<Cell> cellArrayList = new ArrayList<Cell>();
		CellSelectorInRowOrColumn cellSelectorInRowOrColumn = new CellSelectorInRowOrColumn(

				cellArrayList, row, column);

		// Create grid cell stub with an incorrect row and incorrect column
		Cell cellStub = mock(Cell.class);
		when(cellStub.getRow()).thenReturn(row + 1);
		when(cellStub.getColumn()).thenReturn(column + 1);
		cellArrayList.add(cellStub);

		boolean expectedSelect = false;
		boolean resultSelect = cellSelectorInRowOrColumn
				.select(cellStub);
		assertEquals("Is Cell selected?", expectedSelect, resultSelect);
	}

	@Test
	public void select_CellWithCorrectRowAndIncorrectColumn_True()
			throws Exception {
		int row = 1;
		int column = 2;

		// The actual content of the grid cell list is not relevant for this
		// test. An empty list is ok in order to initialize the
		// CellSelectorInRowOrColumn.
		List<Cell> cellArrayList = new ArrayList<Cell>();
		CellSelectorInRowOrColumn cellSelectorInRowOrColumn = new CellSelectorInRowOrColumn(
				cellArrayList, row, column);

		// Create grid cell stub with a correct row but incorrect column
		Cell cellStub = mock(Cell.class);
		when(cellStub.getRow()).thenReturn(row);
		when(cellStub.getColumn()).thenReturn(column + 1);
		cellArrayList.add(cellStub);

		boolean expectedSelect = true;
		boolean resultSelect = cellSelectorInRowOrColumn
				.select(cellStub);
		assertEquals("Is Cell selected?", expectedSelect, resultSelect);
	}

	@Test
	public void select_CellWithIncorrectRowAndCorrectColumn_True()
			throws Exception {
		int row = 1;
		int column = 2;

		// The actual content of the grid cell list is not relevant for this
		// test. An empty list is ok in order to initialize the
		// CellSelectorInRowOrColumn.
		List<Cell> cellArrayList = new ArrayList<Cell>();
		CellSelectorInRowOrColumn cellSelectorInRowOrColumn = new CellSelectorInRowOrColumn(
				cellArrayList, row, column);

		// Create grid cell stub with am incorrect row and correct column
		Cell cellStub = mock(Cell.class);
		when(cellStub.getRow()).thenReturn(row + 1);
		when(cellStub.getColumn()).thenReturn(column);
		cellArrayList.add(cellStub);

		boolean expectedSelect = true;
		boolean resultSelect = cellSelectorInRowOrColumn
				.select(cellStub);
		assertEquals("Is Cell selected?", expectedSelect, resultSelect);
	}

	@Test
	public void select_CellWithCorrectUserValueAndCorrectRowAndCorrectColumn_True()
			throws Exception {
		int row = 1;
		int column = 2;

		// The actual content of the grid cell list is not relevant for this
		// test. An empty list is ok in order to initialize the
		// CellSelectorInRowOrColumn.
		List<Cell> cellArrayList = new ArrayList<Cell>();
		CellSelectorInRowOrColumn cellSelectorInRowOrColumn = new CellSelectorInRowOrColumn(
				cellArrayList, row, column);

		// Create grid cell stub with a correct row and column
		Cell cellStub = mock(Cell.class);
		when(cellStub.getRow()).thenReturn(row);
		when(cellStub.getColumn()).thenReturn(column);
		cellArrayList.add(cellStub);

		boolean expectedSelect = true;
		boolean resultSelect = cellSelectorInRowOrColumn
				.select(cellStub);
		assertEquals("Is Cell selected?", expectedSelect, resultSelect);
	}
}
