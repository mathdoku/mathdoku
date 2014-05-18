package net.mathdoku.plus.storage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CageStorageTest {
	private String mLine;
	private int mRevisionNumber = 596;
	private List<Cell> mCells = new ArrayList<Cell>();

	@Test(expected = IllegalArgumentException.class)
	public void fromStorageString_NullLine_False() throws Exception {
		mLine = null;

		assertThat(CageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = StorageException.class)
	public void fromStorageString_RevisionIdToLow_False() throws Exception {
		mLine = "CAGE:1:2:3:4,5,6:false";
		mRevisionNumber = 368;
		assertThat(CageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = StorageException.class)
	public void fromStorageString_InvalidLineId_False() throws Exception {
		mLine = "WRONG:This is not a valid cage storage string";
		assertThat(CageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = StorageException.class)
	public void fromStorageString_StorageStringHasTooLittleElements_False()
			throws Exception {
		mLine = "CAGE:2:3:4:5";
		assertThat(CageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = StorageException.class)
	public void fromStorageString_StorageStringHasTooManyElements_False()
			throws Exception {
		mLine = "CAGE:2:3:4:5:6:7";
		assertThat(CageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = StorageException.class)
	public void fromStorageString_ValidLineWithoutCells_True() throws Exception {
		mLine = "CAGE:1:2:3::false";

		CageBuilder cageBuilder = CageStorage.getCageBuilderFromStorageString(
				mLine, mRevisionNumber, mCells);

		CageBuilder expectedCageBuilder = new CageBuilder()
				.setId(1)
				.setCageOperator(CageOperator.fromId("2"))
				.setResult(3)
				.setHideOperator(false);
		assertThat(cageBuilder, is(expectedCageBuilder));
	}

	@Test
	public void fromStorageString_ValidLineWithSingeCell_True()
			throws Exception {
		mLine = "CAGE:1:2:3:4:true";

		CageBuilder cageBuilder = CageStorage.getCageBuilderFromStorageString(
				mLine, mRevisionNumber, createCellMocksBasedOnCageIds(new int[] {0, 0, 0, 0, 1, 0, 0, 0, 0, 0}));

		CageBuilder expectedCageBuilder = new CageBuilder()
				.setId(1)
				.setCageOperator(CageOperator.fromId("2"))
				.setResult(3)
				.setCells(new int[] { 4 })
				.setHideOperator(true);
		assertThat(cageBuilder, is(expectedCageBuilder));
	}

	@Test
	public void fromStorageString_ValidLineWithMultipleCellCage_True()
			throws Exception {
		mLine = "CAGE:1:2:3:4,5,6,7:false";

		CageBuilder cageBuilder = CageStorage.getCageBuilderFromStorageString(
				mLine, mRevisionNumber, createCellMocksBasedOnCageIds(new int[] {0, 0, 0, 0, 1, 1, 1, 1, 0, 0}));

		CageBuilder expectedCageBuilder = new CageBuilder()
				.setId(1)
				.setCageOperator(CageOperator.fromId("2"))
				.setResult(3)
				.setCells(new int[] { 4, 5, 6, 7 })
				.setHideOperator(false);
		assertThat(cageBuilder, is(expectedCageBuilder));
	}

	@Test
	public void toStorageString_CageWithoutCells_StorageStringCreated()
			throws Exception {
		Cage cageMock = mock(Cage.class);
		when(cageMock.getId()).thenReturn(1);
		when(cageMock.getOperator()).thenReturn(CageOperator.MULTIPLY);
		when(cageMock.getResult()).thenReturn(4);
		when(cageMock.getCells()).thenReturn(new int[] {});
		when(cageMock.isOperatorHidden()).thenReturn(false);

		assertThat(CageStorage.toStorageString(cageMock),
				is("CAGE:1:3:4::false"));
	}

	@Test
	public void toStorageString_CageWithSingleCell_StorageStringCreated()
			throws Exception {
		Cage cageMock = mock(Cage.class);
		when(cageMock.getId()).thenReturn(1);
		when(cageMock.getOperator()).thenReturn(CageOperator.SUBTRACT);
		when(cageMock.getResult()).thenReturn(3);
		when(cageMock.getCells()).thenReturn(new int[] {4});
		when(cageMock.isOperatorHidden()).thenReturn(false);

		assertThat(CageStorage.toStorageString(cageMock),
				is("CAGE:1:2:3:4,:false"));
	}

	@Test
	public void toStorageString_CageWithMultipleCells_StorageStringCreated()
			throws Exception {
		Cage cageMock = mock(Cage.class);
		when(cageMock.getId()).thenReturn(1);
		when(cageMock.getOperator()).thenReturn(CageOperator.DIVIDE);
		when(cageMock.getResult()).thenReturn(3);
		when(cageMock.getCells()).thenReturn(new int[] {5, 6, 7});
		when(cageMock.isOperatorHidden()).thenReturn(false);

		assertThat(CageStorage.toStorageString(cageMock),
				is("CAGE:1:4:3:5,6,7,:false"));
	}

	private List<Cell> createCellMocksBasedOnCageIds(int...cageIds) {
		List<Cell> cells = new ArrayList<Cell>();
		int cellNumber = 0;
		for (int cageId : cageIds) {
			Cell cellMock = mock(Cell.class);
			when(cellMock.getCellId()).thenReturn(cellNumber++);
			when(cellMock.getCageId()).thenReturn(cageId);
			cells.add(cellMock);
		}
		return cells;
	}
}
