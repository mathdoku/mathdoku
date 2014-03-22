package net.mathdoku.plus.enums;

import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class GridTypeTest {
	private final int SMALLEST_GRID_SIZE = 2;
	private final int BIGGEST_GRID_SIZE = 9;
	private final String[] GRID_SIZE_TEXT = {"2x2", "3x3", "4x4", "5x5", "6x6", "7x7", "8x8", "9x9"};

	@Test
	public void getGridSize_CheckIfAllButNoMoreGridSizesExist() throws Exception {
		assertThat(GridType.GRID_2X2.getGridSize(),is(2));
		assertThat(GridType.GRID_3X3.getGridSize(),is(3));
		assertThat(GridType.GRID_4x4.getGridSize(),is(4));
		assertThat(GridType.GRID_5X5.getGridSize(),is(5));
		assertThat(GridType.GRID_6X6.getGridSize(),is(6));
		assertThat(GridType.GRID_7X7.getGridSize(),is(7));
		assertThat(GridType.GRID_8X8.getGridSize(),is(8));
		assertThat(GridType.GRID_9X9.getGridSize(),is(9));
		assertThat(GridType.values().length,is(GRID_SIZE_TEXT.length));
	}

	@Test
	public void testToZeroBasedIndex() throws Exception {
		int smallestGridSize = GridType.getSmallestGridSize();

		assertThat(GridType.toZeroBasedIndex(smallestGridSize), is(0));
	}

	@Test
	public void testFromZeroBasedIndex() throws Exception {
		assertThat(GridType.fromZeroBasedIndex(0).getGridSize(), is(SMALLEST_GRID_SIZE));
	}

	@Test
	public void testFromInteger() throws Exception {
		assertThat(GridType.fromInteger(2), is(GridType.GRID_2X2));
		assertThat(GridType.fromInteger(3), is(GridType.GRID_3X3));
		assertThat(GridType.fromInteger(4), is(GridType.GRID_4x4));
		assertThat(GridType.fromInteger(5), is(GridType.GRID_5X5));
		assertThat(GridType.fromInteger(6), is(GridType.GRID_6X6));
		assertThat(GridType.fromInteger(7), is(GridType.GRID_7X7));
		assertThat(GridType.fromInteger(8), is(GridType.GRID_8X8));
		assertThat(GridType.fromInteger(9), is(GridType.GRID_9X9));
	}

	@Test (expected = IllegalArgumentException.class)
	public void testFromInteger_GridSizeTooSmall_IllegalStateExceptionThrown() throws Exception {
		GridType.fromInteger(SMALLEST_GRID_SIZE - 1);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testFromInteger_GridSizeTooBig_IllegalStateExceptionThrown() throws Exception {
		GridType.fromInteger(BIGGEST_GRID_SIZE + 1);
	}

	@Test
	public void getSmallestGridSize() throws Exception {
		assertThat(GridType.getSmallestGridSize(), is(SMALLEST_GRID_SIZE));
	}

	@Test
	public void getBiggestGridSize() throws Exception {
		assertThat(GridType.getBiggestGridSize(),is(BIGGEST_GRID_SIZE));
	}

	@Test
	public void getFromNumberOfCells_ValidGridSize() throws Exception {
		assertThat(GridType.getFromNumberOfCells(4),is(GridType.GRID_2X2));
		assertThat(GridType.getFromNumberOfCells(9),is(GridType.GRID_3X3));
		assertThat(GridType.getFromNumberOfCells(16),is(GridType.GRID_4x4));
		assertThat(GridType.getFromNumberOfCells(25),is(GridType.GRID_5X5));
		assertThat(GridType.getFromNumberOfCells(36),is(GridType.GRID_6X6));
		assertThat(GridType.getFromNumberOfCells(49),is(GridType.GRID_7X7));
		assertThat(GridType.getFromNumberOfCells(64),is(GridType.GRID_8X8));
		assertThat(GridType.getFromNumberOfCells(81),is(GridType.GRID_9X9));
	}

	@Test (expected = IllegalArgumentException.class)
	public void getFromNumberOfCells_SquareOfSmallestGridSizeMinusOne() throws Exception {
		int gridSize = (SMALLEST_GRID_SIZE - 1) * (SMALLEST_GRID_SIZE - 1);
		GridType.getFromNumberOfCells(gridSize);
	}

	@Test (expected = IllegalArgumentException.class)
	public void getFromNumberOfCells_InvalidGridSize21() throws Exception {
		GridType.getFromNumberOfCells(21);
	}

	@Test (expected = IllegalArgumentException.class)
	public void getFromNumberOfCells_SquareOfBiggestGridSizePlusOne() throws Exception {
		int gridSize = (BIGGEST_GRID_SIZE + 1) * (BIGGEST_GRID_SIZE + 1);
		GridType.getFromNumberOfCells(gridSize);
	}

	@Test
	public void getNumberOfCells_ForAllGridTypes() throws Exception {
		assertThat(GridType.GRID_2X2.getNumberOfCells(),is(4));
		assertThat(GridType.GRID_3X3.getNumberOfCells(),is(9));
		assertThat(GridType.GRID_4x4.getNumberOfCells(),is(16));
		assertThat(GridType.GRID_5X5.getNumberOfCells(),is(25));
		assertThat(GridType.GRID_6X6.getNumberOfCells(),is(36));
		assertThat(GridType.GRID_7X7.getNumberOfCells(),is(49));
		assertThat(GridType.GRID_8X8.getNumberOfCells(),is(64));
		assertThat(GridType.GRID_9X9.getNumberOfCells(),is(81));
	}
}