package net.mathdoku.plus.grid;

import android.app.Activity;
import android.content.Context;

import net.mathdoku.plus.Preferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;
import testHelper.TestData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class CellChangeTest {
	@Before
	public void setup() {
		Context context = new Activity();
		Preferences.getInstance(context);
	}

	/**
	 * Create a 1 x 1 Grid, containing 1 cage and 1 cell.
	 * 
	 * @return The 1 x 1 Grid.
	 */
	private Grid make_Grid1x1() {
		// Create grid with size 1
		Grid grid = new Grid();
		grid.setGridSize(1);

		// Create the cell and set the expected cell value
		int cellNumber = 1; // Cells are numbered started from 1
		int cellValue = 1; // In a 1 x 1 grid the cell must equal 1
		GridCell gridCell = new GridCell(grid, cellNumber);
		gridCell.setCorrectValue(cellValue);

		// Add the cell to the list of cells in the grid
		grid.mCells.add(gridCell);

		// Create the cage
		//
		// Although a 1 x 1 grid with visible operators is identical to a 1 x 1
		// grid with hidden operator, a grid with visible operators is created.
		boolean HIDDEN_OPERATORS = false;
		int cageId = 0; // Cage id's are 0-based
		int cageResultValue = 1;
		GridCage gridCage = new GridCage(grid, HIDDEN_OPERATORS);
		gridCage.mCells.add(gridCell);
		gridCage.setCageId(cageId);
		gridCage.setCageResults(cageResultValue, GridCage.ACTION_NONE,
				HIDDEN_OPERATORS);

		// Add the cage to the list of cages in the grid
		grid.mCages.add(gridCage);

		return grid;
	}

	@Test
	public void restore_CellWithUserValue_UserValueRestored() {
		// Setup the grid
		Grid grid = make_Grid1x1();
		GridCell gridCell = grid.getCellAt(0, 0); // Row 1, Column 1

		// Initially the cells in a new grid have a user value 0 (empty) and no
		// maybe values. Note that the maybe values cannot be retrieved from the
		// cell.
		int expectedUserValue = gridCell.getUserValue();
		ArrayList<Integer> expectedMaybeValues = new ArrayList<Integer>();

		// Store current value of cell in a cell change
		CellChange cellChange = new CellChange(gridCell, expectedUserValue,
				expectedMaybeValues);

		// Manipulate the cell
		gridCell.setUserValue(1);
		gridCell.addPossible(1);

		// Restore the cell
		cellChange.restore();

		// Compare resulting user and maybe values (either user value or maybe
		// values are registerd for the cell).
		int resultUserValue = gridCell.getUserValue();
		assertEquals("User value", expectedUserValue, resultUserValue);
		assertTrue("Count maybe values", gridCell.countPossibles() == 0);
	}

	@Test
	public void restore_GridCellWithMultipleMaybeValues_AllMaybeValuesRestored() {
		// Setup the grid
		Grid grid = TestData.make_4x4GridWithVisibleOperatorsAllCellsEmpty();

		GridCell gridCell = grid.getCellAt(1, 1); // Row 2, col 2
		int expectedUserValue = gridCell.getUserValue();

		// Initialize the cell with a maybe value. Note that the maybe values
		// cannot be retrieved from the cell so we have maintain it locally.
		ArrayList<Integer> expectedMaybeValues = new ArrayList<Integer>();

		// Store first maybe value in the grid cell
		int expectedMaybeValue1 = 2;
		gridCell.addPossible(expectedMaybeValue1);
		expectedMaybeValues.add(expectedMaybeValue1);

		// Store second maybe value
		int expectedMaybeValue2 = 4;
		gridCell.addPossible(expectedMaybeValue2);
		expectedMaybeValues.add(expectedMaybeValue2);

		// Store the maybe values in a cell change
		CellChange cellChange = new CellChange(gridCell, expectedUserValue,
				expectedMaybeValues);

		// Manipulate the cell
		gridCell.setUserValue(1);
		gridCell.addPossible(1);

		// Restore the cell
		cellChange.restore();

		// Compare resulting user and maybe values (either user value or maybe
		// values are registered for the cell).
		int resultUserValue = gridCell.getUserValue();
		assertTrue("Expected maybe value #1 is not found",
				gridCell.hasPossible(expectedMaybeValue1));
		assertTrue("Expected maybe value #2 is not found",
				gridCell.hasPossible(expectedMaybeValue2));
		assertEquals("Expected number of maybe values", 2,
				gridCell.countPossibles());
		assertEquals("User value", expectedUserValue, resultUserValue);
	}

	@Test
	public void toStorageString_CellChangeWhenChangingUserValueOfCellAlreadyContainingAUserValue_CreatesStorageString() {
		// Setup the grid
		Grid grid = TestData.make_4x4GridWithVisibleOperatorsAllCellsEmpty();

		// Assume that the cell at row 2, col 1 (cell number 5) is filled with
		// user value 2 and no maybe values
		int userValueCell16 = 2;
		GridCell gridCell = grid.getCellAt(2 - 1, 1 - 1);
		ArrayList<Integer> maybeValuesCell16 = new ArrayList<Integer>();

		// In case this cell would have been changed (for example by setting the
		// user value to 3), the cell changed would store the old user values
		// and old maybe values before altering the cell.
		//
		// The current situation of the cells involved has to be stored before
		// actually performing the change.
		String expectedStorageString = "CELL_CHANGE:[5:2::]";

		// Create actual cell change and resulting storage string
		CellChange cellChange = new CellChange(gridCell, userValueCell16,
				maybeValuesCell16);
		String resultStorageString = cellChange.toStorageString();

		assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_CellChangeWhenChangingUserValueOfCellAndClearAMaybeValueInOneOtherCell_CreatesStorageString() {
		// Setup the grid
		Grid grid = TestData.make_4x4GridWithVisibleOperatorsAllCellsEmpty();

		// Assume that the cell at row 4, col 4 (cell number 16) is filled with
		// maybe values 2, 3, 4
		int userValueCell16 = 0;
		GridCell gridCell = grid.getCellAt(4 - 1, 4 - 1);
		ArrayList<Integer> maybeValuesCell16 = new ArrayList<Integer>();
		maybeValuesCell16.add(2);
		maybeValuesCell16.add(3);
		maybeValuesCell16.add(4);

		// In case this cell would have been changed (for example by setting the
		// user value), the cell changed would store the old user values and old
		// maybe values before altering the cell.
		//
		// The current situation of the cells involved has to be stored before
		// actually performing the change.
		String expectedStorageString = "CELL_CHANGE:[16:0:2,3,4,:]";

		// Create actual cell change and resulting storage string
		CellChange cellChange = new CellChange(gridCell, userValueCell16,
				maybeValuesCell16);

		String resultStorageString = cellChange.toStorageString();
		assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_CellChangeWhenChangingUserValueOfCellAndClearAMaybeValueInTwoOtherCells_CreatesStorageString() {
		// Setup the grid
		Grid grid = TestData.make_4x4GridWithVisibleOperatorsAllCellsEmpty();

		// Assume that the cell at row 1, col 2 (cell number 2) is filled with
		// maybe value 3.
		int userValueCell2 = 0;
		GridCell gridCell2 = grid.getCellAt(1 - 1, 2 - 1);
		ArrayList<Integer> maybeValuesCell2 = new ArrayList<Integer>();
		maybeValuesCell2.add(3);

		// Assume that the cell at row 1, col 4 (cell number 4) is filled with
		// user value 1.
		int userValueCell4 = 1;
		GridCell gridCell4 = grid.getCellAt(1 - 1, 4 - 1);
		ArrayList<Integer> maybeValuesCell4 = new ArrayList<Integer>();

		// Assume that cell at row 4, col 4 (cell number 16) is filled with
		// maybe values 2, 3, 4
		int userValueCell16 = 0;
		GridCell gridCell16 = grid.getCellAt(4 - 1, 4 - 1);
		ArrayList<Integer> maybeValuesCell16 = new ArrayList<Integer>();
		maybeValuesCell16.add(2);
		maybeValuesCell16.add(3);
		maybeValuesCell16.add(4);

		// In case the cell at row 1, col 4 is changed (for example by setting
		// the user value to 3), the cell change would store the old user value
		// and old maybe values of this cell. As user value 3 can no longer be
		// used in the other cells in row 1 and the other cells in column 4,
		// this maybe values has to removed from the cells in which it is used.
		//
		// The current situation of the cells involved has to be stored before
		// actually performing the change.
		String expectedStorageString = "CELL_CHANGE:[4:1::[2:0:3,:],[16:0:2,3,4,:],]";

		// Create actual cell change and resulting storage string
		CellChange cellChangeRelated2 = new CellChange(gridCell2,
				userValueCell2, maybeValuesCell2);
		CellChange cellChangeRelated16 = new CellChange(gridCell16,
				userValueCell16, maybeValuesCell16);
		CellChange cellChangeRoot = new CellChange(gridCell4, userValueCell4,
				maybeValuesCell4);
		cellChangeRoot.addRelatedMove(cellChangeRelated2);
		cellChangeRoot.addRelatedMove(cellChangeRelated16);

		String resultStorageString = cellChangeRoot.toStorageString();
		assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}
}