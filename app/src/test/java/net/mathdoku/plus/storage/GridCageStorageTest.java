package net.mathdoku.plus.storage;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.grid.CageBuilder;
import net.mathdoku.plus.grid.Cell;
import net.mathdoku.plus.grid.GridCage;

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
public class GridCageStorageTest {
	private GridCageStorage mGridCageStorage = new GridCageStorage();
	private String mLine;
	private int mRevisionNumber = 596;
	private List<Cell> mCells = mock(ArrayList.class);

	@Test(expected = NullPointerException.class)
	public void fromStorageString_NullLine_False() throws Exception {
		mLine = null;

		assertThat(mGridCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test
	public void fromStorageString_RevisionIdToLow_False() throws Exception {
		mLine = "CAGE:1:2:3:4,5,6:false";
		mRevisionNumber = 368;
		assertThat(mGridCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test
	public void fromStorageString_InvalidLineId_False() throws Exception {
		mLine = "WRONG:This is not a valid cage storage string";
		assertThat(mGridCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasTooLittleElements_False()
			throws Exception {
		mLine = "CAGE:2:3:4:5";
		assertThat(mGridCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasTooManyElements_False()
			throws Exception {
		mLine = "CAGE:2:3:4:5:6:7";
		assertThat(mGridCageStorage.getCageBuilderFromStorageString(mLine,
				mRevisionNumber, mCells), is(nullValue()));
	}

	@Test
	public void fromStorageString_ValidLineWithoutCells_True() throws Exception {
		mLine = "CAGE:1:2:3::false";
		when(mCells.get(anyInt())).thenReturn(mock(Cell.class));

		CageBuilder cageBuilder = mGridCageStorage
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

		CageBuilder cageBuilder = mGridCageStorage
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

		CageBuilder cageBuilder = mGridCageStorage
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
	public void toStorageString_GridCageWithoutCells_StorageStringCreated()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);
		when(gridCageMock.getId()).thenReturn(1);
		when(gridCageMock.getOperator()).thenReturn(CageOperator.MULTIPLY);
		when(gridCageMock.getResult()).thenReturn(4);
		List<Cell> cells = new ArrayList<Cell>();
		when(gridCageMock.getListOfCells()).thenReturn(cells);
		when(gridCageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mGridCageStorage.toStorageString(gridCageMock),
				is("CAGE:1:3:4::false"));
	}

	@Test
	public void toStorageString_GridCageWithSingleCell_StorageStringCreated()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);
		when(gridCageMock.getId()).thenReturn(1);
		when(gridCageMock.getOperator()).thenReturn(CageOperator.SUBTRACT);
		when(gridCageMock.getResult()).thenReturn(3);
		List<Cell> cells = new ArrayList<Cell>();
		Cell cell = mock(Cell.class);
		when(cell.getCellId()).thenReturn(4);
		cells.add(cell);
		when(gridCageMock.getListOfCells()).thenReturn(cells);
		when(gridCageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mGridCageStorage.toStorageString(gridCageMock),
				is("CAGE:1:2:3:4,:false"));
	}

	@Test
	public void toStorageString_GridCageWithMultipleCells_StorageStringCreated()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);
		when(gridCageMock.getId()).thenReturn(1);
		when(gridCageMock.getOperator()).thenReturn(CageOperator.DIVIDE);
		when(gridCageMock.getResult()).thenReturn(3);
		List<Cell> cells = new ArrayList<Cell>();
		Cell cell = mock(Cell.class);
		when(cell.getCellId()).thenReturn(5, 6, 7);
		cells.add(cell);
		cells.add(cell);
		cells.add(cell);
		when(gridCageMock.getListOfCells()).thenReturn(cells);
		when(gridCageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mGridCageStorage.toStorageString(gridCageMock),
				is("CAGE:1:4:3:5,6,7,:false"));
	}
}
