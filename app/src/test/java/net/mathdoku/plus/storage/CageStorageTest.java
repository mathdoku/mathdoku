package net.mathdoku.plus.storage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.grid.Cage;
import net.mathdoku.plus.grid.CageBuilder;
import net.mathdoku.plus.grid.Cell;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CageStorageTest {
	private CageStorage mCageStorage = new CageStorage();
	private String mLine;
	private int mRevisionNumber = 596;
	private List<Cell> mCells = mock(ArrayList.class);

	@Test(expected = NullPointerException.class)
	public void fromStorageString_NullLine_False() throws Exception {
		mLine = null;

		assertThat(mCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test
	public void fromStorageString_RevisionIdToLow_False() throws Exception {
		mLine = "CAGE:1:2:3:4,5,6:false";
		mRevisionNumber = 368;
		assertThat(mCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test
	public void fromStorageString_InvalidLineId_False() throws Exception {
		mLine = "WRONG:This is not a valid cage storage string";
		assertThat(mCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasTooLittleElements_False()
			throws Exception {
		mLine = "CAGE:2:3:4:5";
		assertThat(mCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasTooManyElements_False()
			throws Exception {
		mLine = "CAGE:2:3:4:5:6:7";
		assertThat(mCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test
	public void fromStorageString_ValidLineWithoutCells_True() throws Exception {
		mLine = "CAGE:1:2:3::false";
		when(mCells.get(anyInt())).thenReturn(mock(Cell.class));

		CageBuilder cageBuilder = mCageStorage
				.getCageBuilderFromStorageString(mLine, mRevisionNumber, mCells);

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
		Cell cellMock = mock(Cell.class);
		when(cellMock.getCellId()).thenReturn(4);
		when(mCells.get(anyInt())).thenReturn(cellMock);

		CageBuilder cageBuilder = mCageStorage
				.getCageBuilderFromStorageString(mLine, mRevisionNumber, mCells);

		CageBuilder expectedCageBuilder = new CageBuilder()
				.setId(1)
				.setCageOperator(CageOperator.fromId("2"))
				.setResult(3)
				.setCells(new int[] {4})
				.setHideOperator(true);
		assertThat(cageBuilder, is(expectedCageBuilder));
	}

	@Test
	public void fromStorageString_ValidLineWithMultipleCellCage_True()
			throws Exception {
		mLine = "CAGE:1:2:3:4,5,6,7:false";
		Cell cellMock = mock(Cell.class);
		when(cellMock.getCellId()).thenReturn(4,5,6,7);
		when(mCells.get(anyInt())).thenReturn(cellMock);

		CageBuilder cageBuilder = mCageStorage
				.getCageBuilderFromStorageString(mLine, mRevisionNumber, mCells);

		CageBuilder expectedCageBuilder = new CageBuilder()
				.setId(1)
				.setCageOperator(CageOperator.fromId("2"))
				.setResult(3)
				.setCells(new int[] {4, 5, 6, 7})
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
		List<Cell> cells = new ArrayList<Cell>();
		when(cageMock.getListOfCells()).thenReturn(cells);
		when(cageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mCageStorage.toStorageString(cageMock),
				is("CAGE:1:3:4::false"));
	}

	@Test
	public void toStorageString_CageWithSingleCell_StorageStringCreated()
			throws Exception {
		Cage cageMock = mock(Cage.class);
		when(cageMock.getId()).thenReturn(1);
		when(cageMock.getOperator()).thenReturn(CageOperator.SUBTRACT);
		when(cageMock.getResult()).thenReturn(3);
		List<Cell> cells = new ArrayList<Cell>();
		Cell cell = mock(Cell.class);
		when(cell.getCellId()).thenReturn(4);
		cells.add(cell);
		when(cageMock.getListOfCells()).thenReturn(cells);
		when(cageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mCageStorage.toStorageString(cageMock),
				is("CAGE:1:2:3:4,:false"));
	}

	@Test
	public void toStorageString_CageWithMultipleCells_StorageStringCreated()
			throws Exception {
		Cage cageMock = mock(Cage.class);
		when(cageMock.getId()).thenReturn(1);
		when(cageMock.getOperator()).thenReturn(CageOperator.DIVIDE);
		when(cageMock.getResult()).thenReturn(3);
		List<Cell> cells = new ArrayList<Cell>();
		Cell cell = mock(Cell.class);
		when(cell.getCellId()).thenReturn(5, 6, 7);
		cells.add(cell);
		cells.add(cell);
		cells.add(cell);
		when(cageMock.getListOfCells()).thenReturn(cells);
		when(cageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mCageStorage.toStorageString(cageMock),
				is("CAGE:1:4:3:5,6,7,:false"));
	}
}
