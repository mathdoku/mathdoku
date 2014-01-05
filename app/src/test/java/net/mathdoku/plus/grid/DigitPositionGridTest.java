package net.mathdoku.plus.grid;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class DigitPositionGridTest {
	@Test
	public void IsNotReusable_CreateNewGridWithSameGridSizeAsLastGrid_CannotReuse()
			throws Exception {
		int gridSize = 5;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		boolean resultIsNotReusable = digitPositionGrid
				.isNotReusable(gridSize + 1);
		assertTrue(
				"DigitPositionGrid cannot be reused as grid size of new grid is different",
				resultIsNotReusable);
	}

	@Test
	public void IsNotReusable_CreateNewGridWithSameGridSizeAsLastGrid_CanReuse()
			throws Exception {
		int gridSize = 5;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		boolean resultIsNotReusable = digitPositionGrid.isNotReusable(gridSize);
		assertFalse(
				"DigitPositionGrid can be reused as grid size of new grid is identical",
				resultIsNotReusable);
	}

	@Test
	public void GetVisibility_ForAllIndicesInGridWithSize4_VisibilityIsCorrect()
			throws Exception {
		int gridSize = 4;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 4",
				View.VISIBLE, digitPositionGrid.getVisibility(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 4",
				View.VISIBLE, digitPositionGrid.getVisibility(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 4",
				View.GONE, digitPositionGrid.getVisibility(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 4",
				View.VISIBLE, digitPositionGrid.getVisibility(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 4",
				View.VISIBLE, digitPositionGrid.getVisibility(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 4",
				View.GONE, digitPositionGrid.getVisibility(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 4",
				View.GONE, digitPositionGrid.getVisibility(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 4",
				View.GONE, digitPositionGrid.getVisibility(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 4",
				View.GONE, digitPositionGrid.getVisibility(8));
	}

	@Test
	public void GetVisibility_ForAllIndicesInGridWithSize5_VisibilityIsCorrect()
			throws Exception {
		int gridSize = 5;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 5",
				View.VISIBLE, digitPositionGrid.getVisibility(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 5",
				View.VISIBLE, digitPositionGrid.getVisibility(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 5",
				View.VISIBLE, digitPositionGrid.getVisibility(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 5",
				View.VISIBLE, digitPositionGrid.getVisibility(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 5",
				View.VISIBLE, digitPositionGrid.getVisibility(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 5",
				View.INVISIBLE, digitPositionGrid.getVisibility(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 5",
				View.GONE, digitPositionGrid.getVisibility(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 5",
				View.GONE, digitPositionGrid.getVisibility(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 5",
				View.GONE, digitPositionGrid.getVisibility(8));
	}

	@Test
	public void GetVisibility_ForAllIndicesInGridWithSize6_VisibilityIsCorrect()
			throws Exception {
		int gridSize = 6;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 6",
				View.VISIBLE, digitPositionGrid.getVisibility(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 6",
				View.VISIBLE, digitPositionGrid.getVisibility(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 6",
				View.VISIBLE, digitPositionGrid.getVisibility(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 6",
				View.VISIBLE, digitPositionGrid.getVisibility(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 6",
				View.VISIBLE, digitPositionGrid.getVisibility(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 6",
				View.VISIBLE, digitPositionGrid.getVisibility(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 6",
				View.GONE, digitPositionGrid.getVisibility(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 6",
				View.GONE, digitPositionGrid.getVisibility(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 6",
				View.GONE, digitPositionGrid.getVisibility(8));
	}

	@Test
	public void GetVisibility_ForAllIndicesInGridWithSize7_VisibilityIsCorrect()
			throws Exception {
		int gridSize = 7;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 7",
				View.VISIBLE, digitPositionGrid.getVisibility(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 7",
				View.VISIBLE, digitPositionGrid.getVisibility(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 7",
				View.VISIBLE, digitPositionGrid.getVisibility(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 7",
				View.VISIBLE, digitPositionGrid.getVisibility(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 7",
				View.VISIBLE, digitPositionGrid.getVisibility(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 7",
				View.VISIBLE, digitPositionGrid.getVisibility(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 7",
				View.VISIBLE, digitPositionGrid.getVisibility(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 7",
				View.INVISIBLE, digitPositionGrid.getVisibility(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 7",
				View.INVISIBLE, digitPositionGrid.getVisibility(8));
	}

	@Test
	public void GetVisibility_ForAllIndicesInGridWithSize8_VisibilityIsCorrect()
			throws Exception {
		int gridSize = 8;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 8",
				View.VISIBLE, digitPositionGrid.getVisibility(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 8",
				View.VISIBLE, digitPositionGrid.getVisibility(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 8",
				View.VISIBLE, digitPositionGrid.getVisibility(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 8",
				View.VISIBLE, digitPositionGrid.getVisibility(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 8",
				View.VISIBLE, digitPositionGrid.getVisibility(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 8",
				View.VISIBLE, digitPositionGrid.getVisibility(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 8",
				View.VISIBLE, digitPositionGrid.getVisibility(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 8",
				View.VISIBLE, digitPositionGrid.getVisibility(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 8",
				View.INVISIBLE, digitPositionGrid.getVisibility(8));
	}

	@Test
	public void GetVisibility_ForAllIndicesInGridWithSize9_VisibilityIsCorrect()
			throws Exception {
		int gridSize = 9;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 9",
				View.VISIBLE, digitPositionGrid.getVisibility(8));
	}

	@Test
	public void GetValue_ForAllIndicesInGridWithSize4_ValueIsCorrect()
			throws Exception {
		int gridSize = 4;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);
		int POSITION_NOT_USED = -1;

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 4", 1,
				digitPositionGrid.getValue(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 4", 2,
				digitPositionGrid.getValue(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 4",
				POSITION_NOT_USED, digitPositionGrid.getValue(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 4", 3,
				digitPositionGrid.getValue(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 4", 4,
				digitPositionGrid.getValue(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 4",
				POSITION_NOT_USED, digitPositionGrid.getValue(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 4",
				POSITION_NOT_USED, digitPositionGrid.getValue(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 4",
				POSITION_NOT_USED, digitPositionGrid.getValue(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 4",
				POSITION_NOT_USED, digitPositionGrid.getValue(8));
	}

	@Test
	public void GetValue_ForAllIndicesInGridWithSize5_ValueIsCorrect()
			throws Exception {
		int gridSize = 5;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);
		int POSITION_NOT_USED = -1;

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 5", 1,
				digitPositionGrid.getValue(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 5", 2,
				digitPositionGrid.getValue(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 5", 3,
				digitPositionGrid.getValue(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 5", 4,
				digitPositionGrid.getValue(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 5", 5,
				digitPositionGrid.getValue(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 5",
				POSITION_NOT_USED, digitPositionGrid.getValue(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 5",
				POSITION_NOT_USED, digitPositionGrid.getValue(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 5",
				POSITION_NOT_USED, digitPositionGrid.getValue(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 5",
				POSITION_NOT_USED, digitPositionGrid.getValue(8));
	}

	@Test
	public void GetValue_ForAllIndicesInGridWithSize6_ValueIsCorrect()
			throws Exception {
		int gridSize = 6;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);
		int POSITION_NOT_USED = -1;

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 6", 1,
				digitPositionGrid.getValue(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 6", 2,
				digitPositionGrid.getValue(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 6", 3,
				digitPositionGrid.getValue(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 6", 4,
				digitPositionGrid.getValue(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 6", 5,
				digitPositionGrid.getValue(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 6", 6,
				digitPositionGrid.getValue(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 6",
				POSITION_NOT_USED, digitPositionGrid.getValue(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 6",
				POSITION_NOT_USED, digitPositionGrid.getValue(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 6",
				POSITION_NOT_USED, digitPositionGrid.getValue(8));
	}

	@Test
	public void GetValue_ForAllIndicesInGridWithSize7_ValueIsCorrect()
			throws Exception {
		int gridSize = 7;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);
		int POSITION_NOT_USED = -1;

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 7", 1,
				digitPositionGrid.getValue(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 7", 2,
				digitPositionGrid.getValue(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 7", 3,
				digitPositionGrid.getValue(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 7", 4,
				digitPositionGrid.getValue(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 7", 5,
				digitPositionGrid.getValue(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 7", 6,
				digitPositionGrid.getValue(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 7", 7,
				digitPositionGrid.getValue(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 7",
				POSITION_NOT_USED, digitPositionGrid.getValue(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 7",
				POSITION_NOT_USED, digitPositionGrid.getValue(8));
	}

	@Test
	public void GetValue_ForAllIndicesInGridWithSize8_ValueIsCorrect()
			throws Exception {
		int gridSize = 8;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);
		int POSITION_NOT_USED = -1;

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 8", 1,
				digitPositionGrid.getValue(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 8", 2,
				digitPositionGrid.getValue(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 8", 3,
				digitPositionGrid.getValue(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 8", 4,
				digitPositionGrid.getValue(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 8", 5,
				digitPositionGrid.getValue(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 8", 6,
				digitPositionGrid.getValue(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 8", 7,
				digitPositionGrid.getValue(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 8", 8,
				digitPositionGrid.getValue(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 8",
				POSITION_NOT_USED, digitPositionGrid.getValue(8));
	}

	@Test
	public void GetValue_ForAllIndicesInGridWithSize9_ValueIsCorrect()
			throws Exception {
		int gridSize = 9;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);
		int POSITION_NOT_USED = -1;

		assertEquals("Index 0 in DigitPositionGrid for Grid with size 9", 1,
				digitPositionGrid.getValue(0));
		assertEquals("Index 1 in DigitPositionGrid for Grid with size 9", 2,
				digitPositionGrid.getValue(1));
		assertEquals("Index 2 in DigitPositionGrid for Grid with size 9", 3,
				digitPositionGrid.getValue(2));

		assertEquals("Index 3 in DigitPositionGrid for Grid with size 9", 4,
				digitPositionGrid.getValue(3));
		assertEquals("Index 4 in DigitPositionGrid for Grid with size 9", 5,
				digitPositionGrid.getValue(4));
		assertEquals("Index 5 in DigitPositionGrid for Grid with size 9", 6,
				digitPositionGrid.getValue(5));

		assertEquals("Index 6 in DigitPositionGrid for Grid with size 9", 7,
				digitPositionGrid.getValue(6));
		assertEquals("Index 7 in DigitPositionGrid for Grid with size 9", 8,
				digitPositionGrid.getValue(7));
		assertEquals("Index 8 in DigitPositionGrid for Grid with size 9", 9,
				digitPositionGrid.getValue(8));
	}

	@Test
	public void GetRow_ForAllValuesInGridWithSize4_RowIsCorrect()
			throws Exception {
		int gridSize = 4;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals(
				"Row for value 1 in DigitPositionGrid for Grid with size 4", 0,
				digitPositionGrid.getRow(1));
		assertEquals(
				"Row for value 2 in DigitPositionGrid for Grid with size 4", 0,
				digitPositionGrid.getRow(2));
		assertEquals(
				"Row for value 3 in DigitPositionGrid for Grid with size 4", 1,
				digitPositionGrid.getRow(3));
		assertEquals(
				"Row for value 4 in DigitPositionGrid for Grid with size 4", 1,
				digitPositionGrid.getRow(4));
	}

	@Test
	public void GetRow_ForAllValuesInGridWithSize5_RowIsCorrect()
			throws Exception {
		int gridSize = 5;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals(
				"Row for value 1 in DigitPositionGrid for Grid with size 5", 0,
				digitPositionGrid.getRow(1));
		assertEquals(
				"Row for value 2 in DigitPositionGrid for Grid with size 5", 0,
				digitPositionGrid.getRow(2));
		assertEquals(
				"Row for value 3 in DigitPositionGrid for Grid with size 5", 0,
				digitPositionGrid.getRow(3));
		assertEquals(
				"Row for value 4 in DigitPositionGrid for Grid with size 5", 1,
				digitPositionGrid.getRow(4));
		assertEquals(
				"Row for value 5 in DigitPositionGrid for Grid with size 5", 1,
				digitPositionGrid.getRow(5));
	}

	@Test
	public void GetRow_ForAllValuesInGridWithSize6_RowIsCorrect()
			throws Exception {
		int gridSize = 6;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals(
				"Row for value 1 in DigitPositionGrid for Grid with size 6", 0,
				digitPositionGrid.getRow(1));
		assertEquals(
				"Row for value 2 in DigitPositionGrid for Grid with size 6", 0,
				digitPositionGrid.getRow(2));
		assertEquals(
				"Row for value 3 in DigitPositionGrid for Grid with size 6", 0,
				digitPositionGrid.getRow(3));
		assertEquals(
				"Row for value 4 in DigitPositionGrid for Grid with size 6", 1,
				digitPositionGrid.getRow(4));
		assertEquals(
				"Row for value 5 in DigitPositionGrid for Grid with size 6", 1,
				digitPositionGrid.getRow(5));
		assertEquals(
				"Row for value 6 in DigitPositionGrid for Grid with size 6", 1,
				digitPositionGrid.getRow(6));
	}

	@Test
	public void GetRow_ForAllValuesInGridWithSize9_RowIsCorrect()
			throws Exception {
		int gridSize = 9;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals(
				"Row for value 1 in DigitPositionGrid for Grid with size 9", 0,
				digitPositionGrid.getRow(1));
		assertEquals(
				"Row for value 2 in DigitPositionGrid for Grid with size 9", 0,
				digitPositionGrid.getRow(2));
		assertEquals(
				"Row for value 3 in DigitPositionGrid for Grid with size 9", 0,
				digitPositionGrid.getRow(3));
		assertEquals(
				"Row for value 4 in DigitPositionGrid for Grid with size 9", 1,
				digitPositionGrid.getRow(4));
		assertEquals(
				"Row for value 5 in DigitPositionGrid for Grid with size 9", 1,
				digitPositionGrid.getRow(5));
		assertEquals(
				"Row for value 6 in DigitPositionGrid for Grid with size 9", 1,
				digitPositionGrid.getRow(6));
		assertEquals(
				"Row for value 7 in DigitPositionGrid for Grid with size 9", 2,
				digitPositionGrid.getRow(7));
		assertEquals(
				"Row for value 8 in DigitPositionGrid for Grid with size 9", 2,
				digitPositionGrid.getRow(8));
		assertEquals(
				"Row for value 9 in DigitPositionGrid for Grid with size 9", 2,
				digitPositionGrid.getRow(9));
	}

	@Test
	public void GetCol_ForAllValuesInGridWithSize4_ColIsCorrect()
			throws Exception {
		int gridSize = 4;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals(
				"Col for value 1 in DigitPositionGrid for Grid with size 4", 0,
				digitPositionGrid.getCol(1));
		assertEquals(
				"Col for value 2 in DigitPositionGrid for Grid with size 4", 1,
				digitPositionGrid.getCol(2));
		assertEquals(
				"Col for value 3 in DigitPositionGrid for Grid with size 4", 0,
				digitPositionGrid.getCol(3));
		assertEquals(
				"Col for value 4 in DigitPositionGrid for Grid with size 4", 1,
				digitPositionGrid.getCol(4));
	}

	@Test
	public void GetCol_ForAllValuesInGridWithSize5_ColIsCorrect()
			throws Exception {
		int gridSize = 5;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals(
				"Col for value 1 in DigitPositionGrid for Grid with size 5", 0,
				digitPositionGrid.getCol(1));
		assertEquals(
				"Col for value 2 in DigitPositionGrid for Grid with size 5", 1,
				digitPositionGrid.getCol(2));
		assertEquals(
				"Col for value 3 in DigitPositionGrid for Grid with size 5", 2,
				digitPositionGrid.getCol(3));
		assertEquals(
				"Col for value 4 in DigitPositionGrid for Grid with size 5", 0,
				digitPositionGrid.getCol(4));
		assertEquals(
				"Col for value 5 in DigitPositionGrid for Grid with size 5", 1,
				digitPositionGrid.getCol(5));
	}

	@Test
	public void GetCol_ForAllValuesInGridWithSize6_ColIsCorrect()
			throws Exception {
		int gridSize = 6;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals(
				"Col for value 1 in DigitPositionGrid for Grid with size 6", 0,
				digitPositionGrid.getCol(1));
		assertEquals(
				"Col for value 2 in DigitPositionGrid for Grid with size 6", 1,
				digitPositionGrid.getCol(2));
		assertEquals(
				"Col for value 3 in DigitPositionGrid for Grid with size 6", 2,
				digitPositionGrid.getCol(3));
		assertEquals(
				"Col for value 4 in DigitPositionGrid for Grid with size 6", 0,
				digitPositionGrid.getCol(4));
		assertEquals(
				"Col for value 5 in DigitPositionGrid for Grid with size 6", 1,
				digitPositionGrid.getCol(5));
		assertEquals(
				"Col for value 6 in DigitPositionGrid for Grid with size 6", 2,
				digitPositionGrid.getCol(6));
	}

	@Test
	public void GetCol_ForAllValuesInGridWithSize9_ColIsCorrect()
			throws Exception {
		int gridSize = 9;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		assertEquals(
				"Col for value 1 in DigitPositionGrid for Grid with size 9", 0,
				digitPositionGrid.getCol(1));
		assertEquals(
				"Col for value 2 in DigitPositionGrid for Grid with size 9", 1,
				digitPositionGrid.getCol(2));
		assertEquals(
				"Col for value 3 in DigitPositionGrid for Grid with size 9", 2,
				digitPositionGrid.getCol(3));
		assertEquals(
				"Col for value 4 in DigitPositionGrid for Grid with size 9", 0,
				digitPositionGrid.getCol(4));
		assertEquals(
				"Col for value 5 in DigitPositionGrid for Grid with size 9", 1,
				digitPositionGrid.getCol(5));
		assertEquals(
				"Col for value 6 in DigitPositionGrid for Grid with size 9", 2,
				digitPositionGrid.getCol(6));
		assertEquals(
				"Col for value 7 in DigitPositionGrid for Grid with size 9", 0,
				digitPositionGrid.getCol(7));
		assertEquals(
				"Col for value 8 in DigitPositionGrid for Grid with size 9", 1,
				digitPositionGrid.getCol(8));
		assertEquals(
				"Col for value 9 in DigitPositionGrid for Grid with size 9", 2,
				digitPositionGrid.getCol(9));
	}

	@Test
	public void getVisibleDigitRows_CreateNewDigitPositionGrid4_TwoRowsVisible()
			throws Exception {
		int gridSize = 4;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfRowsVisible = 2;
		int resultNumberOfRowsVisible = digitPositionGrid.getVisibleDigitRows();
		assertEquals("Rows", expectedNumberOfRowsVisible,
				resultNumberOfRowsVisible);
	}

	@Test
	public void getVisibleDigitRows_CreateNewDigitPositionGrid5_TwoRowsVisible()
			throws Exception {
		int gridSize = 5;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfRowsVisible = 2;
		int resultNumberOfRowsVisible = digitPositionGrid.getVisibleDigitRows();
		assertEquals("Rows", expectedNumberOfRowsVisible,
				resultNumberOfRowsVisible);
	}

	@Test
	public void getVisibleDigitRows_CreateNewDigitPositionGrid6_TwoRowsVisible()
			throws Exception {
		int gridSize = 6;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfRowsVisible = 2;
		int resultNumberOfRowsVisible = digitPositionGrid.getVisibleDigitRows();
		assertEquals("Rows", expectedNumberOfRowsVisible,
				resultNumberOfRowsVisible);
	}

	@Test
	public void getVisibleDigitRows_CreateNewDigitPositionGrid7_TwoRowsVisible()
			throws Exception {
		int gridSize = 7;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfRowsVisible = 3;
		int resultNumberOfRowsVisible = digitPositionGrid.getVisibleDigitRows();
		assertEquals("Rows", expectedNumberOfRowsVisible,
				resultNumberOfRowsVisible);
	}

	@Test
	public void getVisibleDigitRows_CreateNewDigitPositionGrid8_TwoRowsVisible()
			throws Exception {
		int gridSize = 8;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfRowsVisible = 3;
		int resultNumberOfRowsVisible = digitPositionGrid.getVisibleDigitRows();
		assertEquals("Rows", expectedNumberOfRowsVisible,
				resultNumberOfRowsVisible);
	}

	@Test
	public void getVisibleDigitRows_CreateNewDigitPositionGrid9_TwoRowsVisible()
			throws Exception {
		int gridSize = 9;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfRowsVisible = 3;
		int resultNumberOfRowsVisible = digitPositionGrid.getVisibleDigitRows();
		assertEquals("Rows", expectedNumberOfRowsVisible,
				resultNumberOfRowsVisible);
	}

	@Test
	public void getVisibleDigitCols_CreateNewDigitPositionGrid4_TwoColsVisible()
			throws Exception {
		int gridSize = 4;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfColsVisible = 2;
		int resultNumberOfColsVisible = digitPositionGrid
				.getVisibleDigitColumns();
		assertEquals("Cols", expectedNumberOfColsVisible,
				resultNumberOfColsVisible);
	}

	@Test
	public void getVisibleDigitCols_CreateNewDigitPositionGrid5_ThreeColsVisible()
			throws Exception {
		int gridSize = 5;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfColsVisible = 3;
		int resultNumberOfColsVisible = digitPositionGrid
				.getVisibleDigitColumns();
		assertEquals("Cols", expectedNumberOfColsVisible,
				resultNumberOfColsVisible);
	}

	@Test
	public void getVisibleDigitCols_CreateNewDigitPositionGrid6_TwoColsVisible()
			throws Exception {
		int gridSize = 6;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfColsVisible = 3;
		int resultNumberOfColsVisible = digitPositionGrid
				.getVisibleDigitColumns();
		assertEquals("Cols", expectedNumberOfColsVisible,
				resultNumberOfColsVisible);
	}

	@Test
	public void getVisibleDigitCols_CreateNewDigitPositionGrid7_ThreeColsVisible()
			throws Exception {
		int gridSize = 7;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfColsVisible = 3;
		int resultNumberOfColsVisible = digitPositionGrid
				.getVisibleDigitColumns();
		assertEquals("Cols", expectedNumberOfColsVisible,
				resultNumberOfColsVisible);
	}

	@Test
	public void getVisibleDigitCols_CreateNewDigitPositionGrid8_ThreeColsVisible()
			throws Exception {
		int gridSize = 8;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfColsVisible = 3;
		int resultNumberOfColsVisible = digitPositionGrid
				.getVisibleDigitColumns();
		assertEquals("Cols", expectedNumberOfColsVisible,
				resultNumberOfColsVisible);
	}

	@Test
	public void getVisibleDigitCols_CreateNewDigitPositionGrid9_ThreeColsVisible()
			throws Exception {
		int gridSize = 9;
		DigitPositionGrid digitPositionGrid = new DigitPositionGrid(gridSize);

		int expectedNumberOfColsVisible = 3;
		int resultNumberOfColsVisible = digitPositionGrid
				.getVisibleDigitColumns();
		assertEquals("Cols", expectedNumberOfColsVisible,
				resultNumberOfColsVisible);
	}
}