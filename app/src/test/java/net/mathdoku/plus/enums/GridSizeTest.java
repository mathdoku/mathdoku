package net.mathdoku.plus.enums;

import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class GridSizeTest {
	private final int SMALLEST_GRID_SIZE = 4;
	private final int BIGGEST_GRID_SIZE = 9;
	private final String[] GRID_SIZE_TEXT = {"4x4", "5x5", "6x6", "7x7", "8x8", "9x9"};

	@Test
	public void getGridSizeText() throws Exception {
		assertThat(GridSize.GRID_4X4.getGridSizeText(),is("4x4"));
		assertThat(GridSize.GRID_5X5.getGridSizeText(),is("5x5"));
		assertThat(GridSize.GRID_6X6.getGridSizeText(),is("6x6"));
		assertThat(GridSize.GRID_7X7.getGridSizeText(),is("7x7"));
		assertThat(GridSize.GRID_8X8.getGridSizeText(),is("8x8"));
		assertThat(GridSize.GRID_9X9.getGridSizeText(),is("9x9"));
	}

	@Test
	public void testGetAllGridSizes() throws Exception {
		assertThat(GridSize.getAllGridSizes(),is(GRID_SIZE_TEXT));
	}

	@Test
	public void testToZeroBasedIndex() throws Exception {
		int smallestGridSize = GridSize.getSmallestGridSize();

		assertThat(GridSize.toZeroBasedIndex(smallestGridSize), is(0));
	}

	@Test
	public void testFromZeroBasedIndex() throws Exception {
		assertThat(GridSize.fromZeroBasedIndex(0).getGridSize(), is(SMALLEST_GRID_SIZE));
	}

	@Test
	public void testFromInteger() throws Exception {
		assertThat(GridSize.fromInteger(4), is(GridSize.GRID_4X4));
		assertThat(GridSize.fromInteger(5), is(GridSize.GRID_5X5));
		assertThat(GridSize.fromInteger(6), is(GridSize.GRID_6X6));
		assertThat(GridSize.fromInteger(7), is(GridSize.GRID_7X7));
		assertThat(GridSize.fromInteger(8), is(GridSize.GRID_8X8));
		assertThat(GridSize.fromInteger(9), is(GridSize.GRID_9X9));
	}

	@Test (expected = IllegalArgumentException.class)
	public void testFromInteger_GridSizeTooSmall_IllegalStateExceptionThrown() throws Exception {
		GridSize.fromInteger(SMALLEST_GRID_SIZE - 1);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testFromInteger_GridSizeTooBig_IllegalStateExceptionThrown() throws Exception {
		GridSize.fromInteger(BIGGEST_GRID_SIZE + 1);
	}

	@Test
	public void getSmallestGridSize() throws Exception {
		assertThat(GridSize.getSmallestGridSize(), is(SMALLEST_GRID_SIZE));
	}

	@Test
	public void getBiggestGridSize() throws Exception {
		assertThat(GridSize.getBiggestGridSize(),is(BIGGEST_GRID_SIZE));
	}

	@Test
	public void getFromNumberOfCells_ValidGridSize() throws Exception {
		assertThat(GridSize.getFromNumberOfCells(16),is(GridSize.GRID_4X4));
		assertThat(GridSize.getFromNumberOfCells(25),is(GridSize.GRID_5X5));
		assertThat(GridSize.getFromNumberOfCells(36),is(GridSize.GRID_6X6));
		assertThat(GridSize.getFromNumberOfCells(49),is(GridSize.GRID_7X7));
		assertThat(GridSize.getFromNumberOfCells(64),is(GridSize.GRID_8X8));
		assertThat(GridSize.getFromNumberOfCells(81),is(GridSize.GRID_9X9));
	}

	@Test (expected = IllegalArgumentException.class)
	public void getFromNumberOfCells_SquareOfSmallestGridSizeMinusOne() throws Exception {
		int gridSize = (SMALLEST_GRID_SIZE - 1) * (SMALLEST_GRID_SIZE - 1);
		GridSize.getFromNumberOfCells(gridSize);
	}

	@Test (expected = IllegalArgumentException.class)
	public void getFromNumberOfCells_InvalidGridSize21() throws Exception {
		GridSize.getFromNumberOfCells(21);
	}

	@Test (expected = IllegalArgumentException.class)
	public void getFromNumberOfCells_SquareOfBiggestGridSizePlusOne() throws Exception {
		int gridSize = (BIGGEST_GRID_SIZE + 1) * (BIGGEST_GRID_SIZE + 1);
		GridSize.getFromNumberOfCells(gridSize);
	}
}