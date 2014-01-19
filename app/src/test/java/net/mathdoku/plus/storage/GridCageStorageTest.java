package net.mathdoku.plus.storage;

import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridCageStorageTest {
	private GridCageStorage mGridCageStorage = new GridCageStorage();
	private String mLine;
	private int mRevisionNumber = 596;
	private ArrayList<GridCell> mCells = mock(ArrayList.class);

	@Test(expected = NullPointerException.class)
	public void fromStorageString_NullLine_False() throws Exception {
		mLine = null;
		assertThat(mGridCageStorage.fromStorageString(mLine, mRevisionNumber,
				mCells), is(false));
	}

	@Test
	public void fromStorageString_RevisionIdToLow_False() throws Exception {
		mLine = "CAGE:1:2:3:4,5,6:false";
		mRevisionNumber = 368;
		assertThat(mGridCageStorage.fromStorageString(mLine, mRevisionNumber,
				mCells), is(false));
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasIncorrectNumberOfElements_False()
			throws Exception {
		mLine = "CAGE:too little arguments";
		assertThat(mGridCageStorage.fromStorageString(mLine, mRevisionNumber,
				mCells), is(false));
	}

	@Test
	public void fromStorageString_InvalidLineId_False() throws Exception {
		mLine = "WRONG:Line:with:an:invalid:identifier but with a correct number of elements (6)";
		assertThat(mGridCageStorage.fromStorageString(mLine, mRevisionNumber,
				mCells), is(false));
	}

	@Test
	public void fromStorageString_ValidLineWithoutCells_True() throws Exception {
		mLine = "CAGE:1:2:3::false";
		when(mCells.get(anyInt())).thenReturn(mock(GridCell.class));
		assertThat(mGridCageStorage.fromStorageString(mLine, mRevisionNumber,
				mCells), is(true));
		assertThat(mGridCageStorage.getId(), is(1));
		assertThat(mGridCageStorage.getAction(), is(2));
		assertThat(mGridCageStorage.getResult(), is(3));
		assertThat(mGridCageStorage.getCells().size(), is(0));
		assertThat(mGridCageStorage.isHideOperator(), is(false));
	}

	@Test
	public void fromStorageString_ValidLineWithSingeCell_True()
			throws Exception {
		mLine = "CAGE:1:2:3:4:true";
		when(mCells.get(anyInt())).thenReturn(mock(GridCell.class));
		assertThat(mGridCageStorage.fromStorageString(mLine, mRevisionNumber,
				mCells), is(true));
		assertThat(mGridCageStorage.getId(), is(1));
		assertThat(mGridCageStorage.getAction(), is(2));
		assertThat(mGridCageStorage.getResult(), is(3));
		assertThat(mGridCageStorage.getCells().size(), is(1));
		assertThat(mGridCageStorage.isHideOperator(), is(true));
	}

	@Test
	public void fromStorageString_ValidLineWithMultipleCellCage_True()
			throws Exception {
		mLine = "CAGE:1:2:3:4,5,6,7:false";
		when(mCells.get(anyInt())).thenReturn(mock(GridCell.class));
		assertThat(mGridCageStorage.fromStorageString(mLine, mRevisionNumber,
				mCells), is(true));
		assertThat(mGridCageStorage.getId(), is(1));
		assertThat(mGridCageStorage.getAction(), is(2));
		assertThat(mGridCageStorage.getResult(), is(3));
		assertThat(mGridCageStorage.getCells().size(), is(4));
		assertThat(mGridCageStorage.isHideOperator(), is(false));
	}

	@Test
	public void toStorageString_GridCageWithoutCells_StorageStringCreated()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);
		when(gridCageMock.getId()).thenReturn(1);
		when(gridCageMock.getAction()).thenReturn(2);
		when(gridCageMock.getResult()).thenReturn(3);
		ArrayList<GridCell> cells = new ArrayList<GridCell>();
		when(gridCageMock.getCells()).thenReturn(cells);
		when(gridCageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mGridCageStorage.toStorageString(gridCageMock),
				is("CAGE:1:2:3::false"));
	}

	@Test
	public void toStorageString_GridCageWithSingleCell_StorageStringCreated()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);
		when(gridCageMock.getId()).thenReturn(1);
		when(gridCageMock.getAction()).thenReturn(2);
		when(gridCageMock.getResult()).thenReturn(3);
		ArrayList<GridCell> cells = new ArrayList<GridCell>();
		GridCell gridCell = mock(GridCell.class);
		when(gridCell.getCellId()).thenReturn(4);
		cells.add(gridCell);
		when(gridCageMock.getCells()).thenReturn(cells);
		when(gridCageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mGridCageStorage.toStorageString(gridCageMock),
				is("CAGE:1:2:3:4,:false"));
	}

	@Test
	public void toStorageString_GridCageWithMultipleCells_StorageStringCreated()
			throws Exception {
		GridCage gridCageMock = mock(GridCage.class);
		when(gridCageMock.getId()).thenReturn(1);
		when(gridCageMock.getAction()).thenReturn(2);
		when(gridCageMock.getResult()).thenReturn(3);
		ArrayList<GridCell> cells = new ArrayList<GridCell>();
		GridCell gridCell = mock(GridCell.class);
		when(gridCell.getCellId()).thenReturn(4, 5, 6);
		cells.add(gridCell);
		cells.add(gridCell);
		cells.add(gridCell);
		when(gridCageMock.getCells()).thenReturn(cells);
		when(gridCageMock.isOperatorHidden()).thenReturn(false);

		assertThat(mGridCageStorage.toStorageString(gridCageMock),
				is("CAGE:1:2:3:4,5,6,:false"));
	}
}
