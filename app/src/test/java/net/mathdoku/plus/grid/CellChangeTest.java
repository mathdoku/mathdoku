package net.mathdoku.plus.grid;

import android.app.Activity;
import android.content.Context;

import net.mathdoku.plus.Preferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CellChangeTest {
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		Context context = new Activity();
		Preferences.getInstance(context);
	}

	@Test
	public void restore_CellWithUserValue_UndoForGridCellIsCalledWithCorrectParameters() {
		int expectedUserValue = 123;
		ArrayList<Integer> expectedMaybeValues = new ArrayList<Integer>();

		// Init the Grid Cell Mock
		GridCell gridCellMock = mock(GridCell.class);

		// Store current value of cell in a cell change
		CellChange cellChange = new CellChange(gridCellMock, expectedUserValue, expectedMaybeValues);

		// Restore the cell change which ...
		cellChange.restore();

		// ... results in undoing the change to the user value or the maybe values for the cell
		verify(gridCellMock).undo(expectedUserValue, expectedMaybeValues);
	}

	@Test
	public void restore_GridCellWithMultipleMaybeValues_UndoForGridCellIsCalledWithCorrectParameters() {
		int expectedUserValue = 0;
		ArrayList<Integer> expectedMaybeValues = new ArrayList<Integer>();
		expectedMaybeValues.add(1);
		expectedMaybeValues.add(2);
		expectedMaybeValues.add(3);

		// Init the Grid Cell Mock
		GridCell gridCellMock = mock(GridCell.class);

		// Store current value of cell in a cell change
		CellChange cellChange = new CellChange(gridCellMock, expectedUserValue, expectedMaybeValues);

		// Restore the cell change which ...
		cellChange.restore();

		// ... results in undoing the change to the user value or the maybe values for the cell
		verify(gridCellMock).undo(expectedUserValue, expectedMaybeValues);
	}

	@Test
	public void toStorageString_CellChangeWithUserValue_StorageStringCreated() {
		// Assume that the cell at row 2, col 1 (cell number 5) is filled with
		// user value 2 and no maybe values
		int expectedCellNumber = 5;
		int expectedUserValue = 2;
		ArrayList<Integer> expectedMaybeValues = new ArrayList<Integer>();

		// Init the GridCell mock
		GridCell gridCell = mock(GridCell.class);
		when(gridCell.getCellNumber()).thenReturn(expectedCellNumber);

		// In case this cell is going to be changed, the current situation of
		// the cell if stored before actually changing it.
		String expectedStorageString = "CELL_CHANGE:[5:2::]";

		// Create actual cell change and resulting storage string
		CellChange cellChange = new CellChange(gridCell, expectedUserValue, expectedMaybeValues);
		String resultStorageString = cellChange.toStorageString();

		assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_CellChangeWithOneMaybeValue_StorageStringCreated() {
		// Assume that the cell at row 2, col 1 (cell number 5) is filled with
		// one maybe value 3
		int expectedCellNumber = 5;
		int expectedUserValue = 0; // NO user value
		ArrayList<Integer> expectedMaybeValues = new ArrayList<Integer>();
		expectedMaybeValues.add(3);

		// Init the GridCell mock
		GridCell gridCell = mock(GridCell.class);
		when(gridCell.getCellNumber()).thenReturn(expectedCellNumber);

		// In case this cell is going to be changed, the current situation of
		// the cell if stored before actually changing it.
		String expectedStorageString = "CELL_CHANGE:[5:0:3,:]";

		// Create actual cell change and resulting storage string
		CellChange cellChange = new CellChange(gridCell, expectedUserValue, expectedMaybeValues);
		String resultStorageString = cellChange.toStorageString();

		assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_CellChangeWithMultipleMaybeValue_StorageStringCreated() {
		// Assume that the cell at row 2, col 1 (cell number 5) is filled with
		// three maybe values (3, 4, 5)
		int expectedCellNumber = 5;
		int expectedUserValue = 0; // NO user value
		ArrayList<Integer> expectedMaybeValues = new ArrayList<Integer>();
		expectedMaybeValues.add(3);
		expectedMaybeValues.add(4);
		expectedMaybeValues.add(5);

		// Init the GridCell mock
		GridCell gridCell = mock(GridCell.class);
		when(gridCell.getCellNumber()).thenReturn(expectedCellNumber);

		// In case this cell is going to be changed, the current situation of
		// the cell if stored before actually changing it.
		String expectedStorageString = "CELL_CHANGE:[5:0:3,4,5,:]";

		// Create actual cell change and resulting storage string
		CellChange cellChange = new CellChange(gridCell, expectedUserValue, expectedMaybeValues);
		String resultStorageString = cellChange.toStorageString();

		assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_CellChangeWithOneRelatedCellChange_StorageStringCreated() {
		// Assume that the cell at row 1, col 2 (cell number 2) is filled with
		// maybe value 3.
		int expectedCellNumber2 = 2;
		int expectedUserValue2 = 0; // NO user value
		ArrayList<Integer> expectedMaybeValues2 = new ArrayList<Integer>();
		expectedMaybeValues2.add(3);

		// Assume that the cell at row 1, col 4 (cell number 4) is filled with
		// user value 1.
		int expectedCellNumber4 = 4;
		int expectedUserValue4 = 1;
		ArrayList<Integer> expectedMaybeValues4 = new ArrayList<Integer>();


		// Init the GridCell mocks
		GridCell gridCell2 = mock(GridCell.class);
		when(gridCell2.getCellNumber()).thenReturn(expectedCellNumber2);
		GridCell gridCell4 = mock(GridCell.class);
		when(gridCell4.getCellNumber()).thenReturn(expectedCellNumber4);

		// In case the cell at row 1, col 4 is changed (for example by setting
		// the user value to 3), the cell change would store the old user value
		// and old maybe values of this cell. As user value 3 should no longer
		// be used in the other cells in the same row or same column this value
		// is removed automatically from the maybe values of cells in this row
		// and column. So multiple cell change due to changing cell 4.
		String expectedStorageString = "CELL_CHANGE:[4:1::[2:0:3,:],]";

		// Create actual cell change and resulting storage string
		CellChange cellChange2 = new CellChange(gridCell2, expectedUserValue2, expectedMaybeValues2);
		CellChange cellChange4 = new CellChange(gridCell4, expectedUserValue4, expectedMaybeValues4);
		cellChange4.addRelatedMove(cellChange2);
		String resultStorageString = cellChange4.toStorageString();

		assertEquals("Storage string", expectedStorageString,
				resultStorageString);
	}

	@Test
	public void toStorageString_CellChangeWithMultipleRelatedCellChanges_StorageStringCreated() {
		// Assume that the cell at row 1, col 2 (cell number 2) is filled with
		// maybe value 3.
		int expectedCellNumber2 = 2;
		int expectedUserValue2 = 0; // NO user value
		ArrayList<Integer> expectedMaybeValues2 = new ArrayList<Integer>();
		expectedMaybeValues2.add(3);

		// Assume that the cell at row 1, col 4 (cell number 4) is filled with
		// user value 1.
		int expectedCellNumber4 = 4;
		int expectedUserValue4 = 1;
		ArrayList<Integer> expectedMaybeValues4 = new ArrayList<Integer>();

		// Assume that cell at row 4, col 4 (cell number 16) is filled with
		// maybe values 2, 3, 4
		int expectedCellNumber16 = 16;
		int expectedUserValue16 = 0; // NO user value
		ArrayList<Integer> expectedMaybeValues16 = new ArrayList<Integer>();
		expectedMaybeValues16.add(2);
		expectedMaybeValues16.add(3);
		expectedMaybeValues16.add(4);

		// Init the GridCell mocks
		GridCell gridCell2 = mock(GridCell.class);
		when(gridCell2.getCellNumber()).thenReturn(expectedCellNumber2);
		GridCell gridCell4 = mock(GridCell.class);
		when(gridCell4.getCellNumber()).thenReturn(expectedCellNumber4);
		GridCell gridCell16 = mock(GridCell.class);
		when(gridCell16.getCellNumber()).thenReturn(expectedCellNumber16);

		// In case the cell at row 1, col 4 is changed (for example by setting
		// the user value to 3), the cell change would store the old user value
		// and old maybe values of this cell. As user value 3 can no longer be
		// used in the other cells in row 1 and the other cells in column 4,
		// this maybe value has to removed from the cells in which it is used.
		// This will be stored in a related cell change.
		String expectedStorageString = "CELL_CHANGE:[4:1::[2:0:3,:],[16:0:2,3,4,:],]";

		// Create actual cell change and resulting storage string
		CellChange cellChange2 = new CellChange(gridCell2, expectedUserValue2, expectedMaybeValues2);
		CellChange cellChange4 = new CellChange(gridCell4, expectedUserValue4, expectedMaybeValues4);
		CellChange cellChange16 = new CellChange(gridCell16, expectedUserValue16, expectedMaybeValues16);
		cellChange4.addRelatedMove(cellChange2);
		cellChange4.addRelatedMove(cellChange16);
		String resultStorageString = cellChange4.toStorageString();
	}

	@Test
	public void fromStorageString_SingleCellChangeForCellContainingAUserValue_GridCellCreated() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Assume that a cell change exists for this cell
		CellChange resultCellChange = new CellChange();
		assertTrue("Single cell change for  a cell containing a user value",
				resultCellChange.fromStorageString("CELL_CHANGE:[0:1::]",
						gridCellsStub, 369));
	}

	@Test
	public void fromStorageString_SingleCellChangeForCellContainingOneMaybeValue_GridCellCreated() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Assume that a cell change exists for this cell
		CellChange resultCellChange = new CellChange();
		assertTrue("Single cell change for  a cell containing a user value",
				resultCellChange.fromStorageString("CELL_CHANGE:[0:0:1:]",
						gridCellsStub, 369));
	}

	@Test
	public void fromStorageString_SingleCellChangeForCellContainingTwoMaybeValues_GridCellCreated() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Assume that a cell change exists for this cell
		CellChange resultCellChange = new CellChange();
		assertTrue("Single cell change for  a cell containing a user value",
				resultCellChange.fromStorageString("CELL_CHANGE:[0:0:1,2,:]",
						gridCellsStub, 369));
	}

	@Test
	public void fromStorageString_SingleCellChangeForCellContainingManyMaybeValues_SuccessfulRead() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Assume that a cell change exists for this cell
		CellChange resultCellChange = new CellChange();
		assertTrue("Single cell change for  a cell containing a user value",
				resultCellChange.fromStorageString(
						"CELL_CHANGE:[0:0:1,2,3,4,5,6,7,8,9,:]", gridCellsStub,
						369));
	}

	@Test
	public void fromStorageString_NestedCellChange2_SuccessfulRead() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Assume that a cell change exists for this cell
		CellChange resultCellChange = new CellChange();
		assertTrue("Single cell change for  a cell containing a user value",
				resultCellChange.fromStorageString(
						"CELL_CHANGE:[1:1::[0:0:3,:],]", gridCellsStub, 369));
	}

	@Test
	public void fromStorageString_NestedCellChange3_SuccessfulRead() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Assume that a cell change exists for this cell
		CellChange resultCellChange = new CellChange();
		assertTrue("Single cell change for  a cell containing a user value",
				resultCellChange.fromStorageString(
						"CELL_CHANGE:[4:1::[2:0:3,:],[16:0:2,3,4,:],]",
						gridCellsStub, 369));
	}

	@Test
	public void fromStorageString_CellChangeInvalidStorageStringLabel_FailedToRead() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Create an invalid storage string: missing underscore in label
		CellChange resultCellChange = new CellChange();
		assertFalse("Cell change with invalid storage string (wrong label)",
				resultCellChange.fromStorageString("CELLCHANGE:[0:1::]",
						gridCellsStub, 369));
	}

	@Test
	public void fromStorageString_CellChangeInvalidStorageStringUnbalancedBrackets_FailedToRead() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Create an invalid storage string: missing underscore in label
		CellChange resultCellChange = new CellChange();
		assertFalse(
				"Cell change with invalid storage string (missing closing bracket)",
				resultCellChange.fromStorageString("CELL_CHANGE:[0:1::",
						gridCellsStub, 369));
	}

	@Test
	public void fromStorageString_CellChangeInvalidStorageStringTooManyArguments_FailedToRead() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Create an invalid storage string: missing underscore in label
		CellChange resultCellChange = new CellChange();
		assertFalse(
				"Cell change with invalid storage string (too many arguments)",
				resultCellChange.fromStorageString("CELL_CHANGE:[0:1:::]",
						gridCellsStub, 369));
	}

	@Test
	public void fromStorageString_CellChangeRevisionTooLow_FailedToRead() {
		// Create a stub fir the list of cells
		ArrayList<GridCell> gridCellsStub = mock(ArrayList.class);

		// Create an invalid storage string: missing underscore in label
		CellChange resultCellChange = new CellChange();
		assertFalse("Cannot restore for this revision number",
				resultCellChange.fromStorageString("CELL_CHANGE:[0:1::]",
						gridCellsStub, 368));
	}

}
