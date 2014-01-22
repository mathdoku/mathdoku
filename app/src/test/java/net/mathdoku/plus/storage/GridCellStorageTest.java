package net.mathdoku.plus.storage;

import net.mathdoku.plus.grid.GridCell;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridCellStorageTest {
	private GridCellStorage mGridCellStorage = new GridCellStorage();
	private String mLine;
	private int mRevisionNumber = 596;

	@Test(expected = NullPointerException.class)
	public void FromStorageString_NullLine_False() throws Exception {
		mLine = null;
		assertThat(mGridCellStorage.fromStorageString(mLine, mRevisionNumber),
				is(false));
	}

	@Test
	public void FromStorageString_RevisionIdToLow_False() throws Exception {
		mLine = "CELL:1:2:3:4+:5:6::true:false:false";
		mRevisionNumber = 368;
		assertThat(mGridCellStorage.fromStorageString(mLine, mRevisionNumber),
				is(false));
	}

	@Test
	public void FromStorageString_InvalidLineId_False() throws Exception {
		mLine = "WRONG:this is not a cell storage string";
		assertThat(mGridCellStorage.fromStorageString(mLine, mRevisionNumber),
				   is(false));
	}

	@Test(expected = InvalidParameterException.class)
	public void FromStorageString_StorageStringHasTooLittleElements_False()
			throws Exception {
		mLine = "CELL:2:3:4:5:6:7:8:9:10";
		assertThat(mGridCellStorage.fromStorageString(mLine, mRevisionNumber),
				is(false));
	}

	@Test(expected = InvalidParameterException.class)
	public void FromStorageString_StorageStringHasTooManyElements_False()
			throws Exception {
		mLine = "CELL:2:3:4:5:6:7:8:9:10:11:12";
		assertThat(mGridCellStorage.fromStorageString(mLine, mRevisionNumber),
				   is(false));
	}

	@Test
	public void FromStorageString_ValidLineWithoutPossibleValues_True()
			throws Exception {
		mLine = "CELL:1:2:3:4+:5:6::true:false:false";
		assertThat(mGridCellStorage.fromStorageString(mLine, mRevisionNumber),
				is(true));
		assertThat(mGridCellStorage.getId(), is(1));
		assertThat(mGridCellStorage.getRow(), is(2));
		assertThat(mGridCellStorage.getColumn(), is(3));
		assertThat(mGridCellStorage.getCageText(), is("4+"));
		assertThat(mGridCellStorage.getCorrectValue(), is(5));
		assertThat(mGridCellStorage.getUserValue(), is(6));
		assertThat(mGridCellStorage.getPossibles().size(), is(0));
		assertThat(mGridCellStorage.isInvalidUserValueHighlight(), is(true));
		assertThat(mGridCellStorage.isRevealed(), is(false));
		assertThat(mGridCellStorage.isSelected(), is(false));
	}

	@Test
	public void FromStorageString_ValidLineWithSinglePossibleValue_True()
			throws Exception {
		mLine = "CELL:1:2:3:4+:5:0:1,:false:true:false";
		assertThat(mGridCellStorage.fromStorageString(mLine, mRevisionNumber),
				is(true));
		assertThat(mGridCellStorage.getPossibles().size(), is(1));
		assertThat(mGridCellStorage.isRevealed(), is(true));
	}

	@Test
	public void FromStorageString_ValidLineWithMultiplePossibleValues_True()
			throws Exception {
		mLine = "CELL:1:2:3:4+:5:0:1,2,3,4,:false:false:true";
		assertThat(mGridCellStorage.fromStorageString(mLine, mRevisionNumber),
				is(true));
		assertThat(mGridCellStorage.getPossibles().size(), is(4));
		assertThat(mGridCellStorage.isSelected(), is(true));
	}

	@Test
	public void toStorageString_GridCellWithoutPossibleValues_StorageStringCreated()
			throws Exception {
		GridCell gridCellMock = mock(GridCell.class);
		when(gridCellMock.getCellId()).thenReturn(1);
		when(gridCellMock.getRow()).thenReturn(2);
		when(gridCellMock.getColumn()).thenReturn(3);
		when(gridCellMock.getCageText()).thenReturn("4+");
		when(gridCellMock.getCorrectValue()).thenReturn(5);
		when(gridCellMock.getUserValue()).thenReturn(6);
		when(gridCellMock.getPossibles()).thenReturn(new ArrayList<Integer>());
		when(gridCellMock.hasInvalidUserValueHighlight()).thenReturn(true);
		when(gridCellMock.isRevealed()).thenReturn(false);
		when(gridCellMock.isSelected()).thenReturn(false);
		assertThat(mGridCellStorage.toStorageString(gridCellMock),
				is("CELL:1:2:3:4+:5:6::true:false:false"));
	}

	@Test
	public void toStorageString_GridCellWithSinglePossibleValue_StorageStringCreated()
			throws Exception {
		GridCell gridCellMock = mock(GridCell.class);
		when(gridCellMock.getCellId()).thenReturn(1);
		when(gridCellMock.getRow()).thenReturn(2);
		when(gridCellMock.getColumn()).thenReturn(3);
		when(gridCellMock.getCageText()).thenReturn("4+");
		when(gridCellMock.getCorrectValue()).thenReturn(5);
		when(gridCellMock.getUserValue()).thenReturn(6);
		ArrayList<Integer> possibles = new ArrayList<Integer>();
		possibles.add(7);
		when(gridCellMock.getPossibles()).thenReturn(possibles);
		when(gridCellMock.hasInvalidUserValueHighlight()).thenReturn(false);
		when(gridCellMock.isRevealed()).thenReturn(true);
		when(gridCellMock.isSelected()).thenReturn(false);
		assertThat(mGridCellStorage.toStorageString(gridCellMock),
				is("CELL:1:2:3:4+:5:6:7,:false:true:false"));
	}

	@Test
	public void toStorageString_GridCellWithMultiplePossibleValue_StorageStringCreated()
			throws Exception {
		GridCell gridCellMock = mock(GridCell.class);
		when(gridCellMock.getCellId()).thenReturn(1);
		when(gridCellMock.getRow()).thenReturn(2);
		when(gridCellMock.getColumn()).thenReturn(3);
		when(gridCellMock.getCageText()).thenReturn("4+");
		when(gridCellMock.getCorrectValue()).thenReturn(5);
		when(gridCellMock.getUserValue()).thenReturn(6);
		ArrayList<Integer> possibles = new ArrayList<Integer>();
		possibles.add(7);
		possibles.add(8);
		possibles.add(9);
		when(gridCellMock.getPossibles()).thenReturn(possibles);
		when(gridCellMock.hasInvalidUserValueHighlight()).thenReturn(false);
		when(gridCellMock.isRevealed()).thenReturn(false);
		when(gridCellMock.isSelected()).thenReturn(true);
		assertThat(mGridCellStorage.toStorageString(gridCellMock),
				is("CELL:1:2:3:4+:5:6:7,8,9,:false:false:true"));
	}
}
