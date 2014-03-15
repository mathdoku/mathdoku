package net.mathdoku.plus.storage;

import net.mathdoku.plus.puzzle.cellchange.CellChange;
import net.mathdoku.plus.puzzle.cell.Cell;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CellChangeStorageTest {
	private CellChangeStorage mCellChangeStorage = new CellChangeStorage();
	private String mLine;
	private List<Cell> mArrayListOfCellsStub = mock(ArrayList.class);
	private int mRevisionNumber = 596;


	private Cell createCellMock(int id) {
		Cell cell = mock(Cell.class);
		when(cell.getCellId()).thenReturn(id);

		return cell;
	}

	@Test(expected = NullPointerException.class)
	public void fromStorageString_NullLine_False() throws Exception {
		mLine = null;
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(false));
	}

	@Test
	public void fromStorageString_RevisionIdToLow_False() throws Exception {
		mLine = "CELL_CHANGE:[0:1::]";
		mRevisionNumber = 368;
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(false));
	}

	@Test
	public void fromStorageString_SingleCellChangeForCellContainingAnEnteredValue_CellCreated() {
		mLine = "CELL_CHANGE:[0:1::]";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(true));
	}

	@Test
	public void fromStorageString_SingleCellChangeForCellContainingOneMaybeValue_CellCreated() {
		mLine = "CELL_CHANGE:[0:0:1:]";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub,
														mRevisionNumber), is(true));
	}

	@Test
	public void fromStorageString_SingleCellChangeForCellContainingTwoMaybeValues_CellCreated() {
		mLine = "CELL_CHANGE:[0:0:1,2,:]";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(true));
	}

	@Test
	public void fromStorageString_SingleCellChangeForCellContainingManyMaybeValues_SuccessfulRead() {
		mLine = "CELL_CHANGE:[0:0:1,2,3,4,5,6,7,8,9,:]";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(true));
	}

	@Test
	public void fromStorageString_NestedCellChange2_SuccessfulRead() {
		mLine = "CELL_CHANGE:[1:1::[0:0:3,:],]";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(true));
	}

	@Test
	public void fromStorageString_NestedCellChange3_SuccessfulRead() {
		mLine = "CELL_CHANGE:[4:1::[2:0:3,:],[16:0:2,3,4,:],]";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub,
														mRevisionNumber), is(true));
	}

	@Test
	public void fromStorageString_CellChangeInvalidStorageStringLabel_FailedToRead() {
		mLine = "WRONG:[0:1::]";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(false));
	}

	@Test
	public void fromStorageString_CellChangeInvalidStorageStringUnbalancedBrackets_FailedToRead() {
		mLine = "CELL_CHANGE:[0:1::";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(false));
	}

	@Test(expected = IllegalStateException.class)
	public void fromStorageString_CellChangeInvalidStorageStringTooManyArguments_FailedToRead() {
		mLine = "CELL_CHANGE:[0:1:::]";
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub, mRevisionNumber), is(false));
	}

	@Test
	public void fromStorageString_CellChangeRevisionTooLow_FailedToRead() {
		mLine = "CELL_CHANGE:[0:1::]";
		mRevisionNumber = 368;
		assertThat(mCellChangeStorage.fromStorageString(mLine, mArrayListOfCellsStub,
														mRevisionNumber), is(false));
	}

	@Test
	public void toStorageString_CellChangeWithEnteredValue_StorageStringCreated() {
		int cellId = 5;
		int enteredValue = 2;
		List<Integer> cellPossibles = new ArrayList<Integer>();

		CellChange cellChange = new CellChange(createCellMock(cellId), enteredValue,
						cellPossibles);
		assertThat(mCellChangeStorage.toStorageString(cellChange),
				is("CELL_CHANGE:[5:2::]"));
	}

	@Test
	public void toStorageString_CellChangeWithOneMaybeValue_StorageStringCreated() {
		int cellId = 5;
		int enteredValue = Cell.NO_ENTERED_VALUE;
		List<Integer> cellPossibles = new ArrayList<Integer>();
		cellPossibles.add(3);

		CellChange cellChange = new CellChange(createCellMock(cellId), enteredValue,
											   cellPossibles);
		assertThat(mCellChangeStorage.toStorageString(cellChange),
				is("CELL_CHANGE:[5:0:3,:]"));
	}

	@Test
	public void toStorageString_CellChangeWithMultipleMaybeValue_StorageStringCreated() {
		int cellId = 5;
		// Cell has no entered value
		int enteredValue = Cell.NO_ENTERED_VALUE;
		List<Integer> cellPossibles = new ArrayList<Integer>();
		cellPossibles.add(3);
		cellPossibles.add(4);
		cellPossibles.add(5);

		CellChange cellChange = new CellChange(createCellMock(cellId), enteredValue,
											   cellPossibles);
		assertThat(mCellChangeStorage.toStorageString(cellChange),
				is("CELL_CHANGE:[5:0:3,4,5,:]"));
	}

	@Test
	public void toStorageString_CellChangeWithOneRelatedCellChange_StorageStringCreated() {
		int rootCellId = 4;
		int rootCellEnteredValue = 1;
		List<Integer> rootCellPossibles = new ArrayList<Integer>();
		CellChange cellChange = new CellChange(createCellMock(rootCellId), rootCellEnteredValue,
												rootCellPossibles);

		int relatedCellId = 2;
		int relatedCellEnteredValue = Cell.NO_ENTERED_VALUE;
		List<Integer> relatedCellPossibles = new ArrayList<Integer>();
		relatedCellPossibles.add(3);
		CellChange relatedCellChange = new CellChange(createCellMock(relatedCellId),
						relatedCellEnteredValue, relatedCellPossibles);
		cellChange.addRelatedMove(relatedCellChange);

		assertThat(mCellChangeStorage.toStorageString(cellChange),
				is("CELL_CHANGE:[4:1::[2:0:3,:],]"));
	}

	@Test
	public void toStorageString_CellChangeWithMultipleRelatedCellChanges_StorageStringCreated() {
		int rootCellId = 4;
		int rootCellEnteredValue = 1;
		List<Integer> rootCellPossibles = new ArrayList<Integer>();
		CellChange cellChange = new CellChange(createCellMock(rootCellId), rootCellEnteredValue,
											   rootCellPossibles);

		int relatedCellId1 = 2;
		int relatedCellEnteredValue1 = Cell.NO_ENTERED_VALUE;
		List<Integer> relatedCellPossibles1 = new ArrayList<Integer>();
		relatedCellPossibles1.add(3);
		CellChange relatedCellChange1 = new CellChange(createCellMock(relatedCellId1),
													  relatedCellEnteredValue1, relatedCellPossibles1);
		cellChange.addRelatedMove(relatedCellChange1);

		int relatedCellId2 = 16;
		int relatedCellEnteredValue2 = Cell.NO_ENTERED_VALUE;
		List<Integer> relatedCellPossibles2 = new ArrayList<Integer>();
		relatedCellPossibles2.add(2);
		relatedCellPossibles2.add(3);
		relatedCellPossibles2.add(4);
		CellChange relatedCellChange2 = new CellChange(createCellMock(relatedCellId2),
													  relatedCellEnteredValue2, relatedCellPossibles2);
		cellChange.addRelatedMove(relatedCellChange2);

		assertThat(mCellChangeStorage.toStorageString(cellChange),
				   is("CELL_CHANGE:[4:1::[2:0:3,:],[16:0:2,3,4,:],]"));
	}
}
