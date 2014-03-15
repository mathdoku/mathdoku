package net.mathdoku.plus.storage;

import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.cell.CellBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class CellStorageTest {
	private CellStorage mCellStorage = new CellStorage();
	private String mLine;

	@Test(expected = NullPointerException.class)
	public void FromStorageString_NullLine_False() throws Exception {
		mLine = null;
		int mRevisionNumber = 369;
		assertThat(mCellStorage.getCellBuilderFromStorageString(mLine,
				mRevisionNumber), is(nullValue()));
	}

	@Test
	public void FromStorageString_RevisionIdToLow_False() throws Exception {
		mLine = "** NOT RELEVANT FOR THIS TEST **";
		int mRevisionNumber = 369;
		assertThat(mCellStorage.getCellBuilderFromStorageString(mLine,
				mRevisionNumber), is(nullValue()));
	}

	@Test
	public void FromStorageString_InvalidLineId_False() throws Exception {
		mLine = "WRONG:this is not a cell storage string identifier";
		int mRevisionNumber = 369;
		assertThat(mCellStorage.getCellBuilderFromStorageString(mLine,
				mRevisionNumber), is(nullValue()));
	}

	@Test(expected = InvalidParameterException.class)
	public void FromStorageString_Revision369AndHigherStorageStringHasTooLittleElements_False()
			throws Exception {
		mLine = "CELL:2:3:4:5:6:7:8:9:10";
		int mRevisionNumber = 369;
		assertThat(mCellStorage.getCellBuilderFromStorageString(mLine,
				mRevisionNumber), is(nullValue()));
	}

	@Test(expected = InvalidParameterException.class)
	public void FromStorageString_Revision597AndHigherStorageStringHasTooLittleElements_False()
			throws Exception {
		mLine = "CELL:2:3:4:5:6:7:8";
		int mRevisionNumber = 597;
		assertThat(mCellStorage.getCellBuilderFromStorageString(mLine,
																	mRevisionNumber), is(nullValue()));
	}

	@Test(expected = InvalidParameterException.class)
	public void FromStorageString_Revision369AndHigherStorageStringHasTooManyElements_False()
			throws Exception {
		mLine = "CELL:2:3:4:5:6:7:8:9:10:11:12";
		int mRevisionNumber = 369;
		assertThat(mCellStorage.getCellBuilderFromStorageString(mLine,
				mRevisionNumber), is(nullValue()));
	}

	@Test(expected = InvalidParameterException.class)
	public void FromStorageString_Revision597AndHigherStorageStringHasTooManyElements_False()
			throws Exception {
		mLine = "CELL:2:3:4:5:6:7:8:9:10";
		int mRevisionNumber = 597;
		assertThat(mCellStorage.getCellBuilderFromStorageString(mLine,
																	mRevisionNumber), is(nullValue()));
	}

	@Test
	public void FromStorageString_Revision369AndHigherValidLineWithoutPossibleValues_True()
			throws Exception {
		mLine = "CELL:1:2:3:4+:5:6::true:false:false";
		int mRevisionNumber = 369;

		CellBuilder cellBuilder = mCellStorage
				.getCellBuilderFromStorageString(mLine, mRevisionNumber);

		CellBuilder expectedCellBuilder = new CellBuilder()
				.setId(1)
				.setCageText("4+")
				.setCorrectValue(5)
				.setEnteredValue(6)
				.setInvalidValueHighlight(true);
		assertThat(cellBuilder, is(expectedCellBuilder));
	}

	@Test
	public void FromStorageString_Revision597AndHigherValidLineWithoutPossibleValues_True()
			throws Exception {
		mLine = "CELL:1:2-:3:4::true:false:false";
		int mRevisionNumber = 597;

		CellBuilder cellBuilder = mCellStorage
				.getCellBuilderFromStorageString(mLine, mRevisionNumber);

		CellBuilder expectedCellBuilder = new CellBuilder()
				.setId(1)
				.setCageText("2-")
				.setCorrectValue(3)
				.setEnteredValue(4)
				.setInvalidValueHighlight(true);
		assertThat(cellBuilder, is(expectedCellBuilder));
	}

	@Test
	public void FromStorageString_Revision597AndHigherValidLineWithSinglePossibleValue_True()
			throws Exception {
		mLine = "CELL:1:2-:3:0:4,:false:true:false";
		int mRevisionNumber = 597;

		CellBuilder cellBuilder = mCellStorage
				.getCellBuilderFromStorageString(mLine, mRevisionNumber);

		List<Integer> possibles = new ArrayList<Integer>();
		possibles.add(4);
		CellBuilder expectedCellBuilder = new CellBuilder()
				.setId(1)
				.setCageText("2-")
				.setCorrectValue(3)
				.setPossibles(possibles)
				.setRevealed(true);
		assertThat(cellBuilder, is(expectedCellBuilder));
	}

	@Test
	public void FromStorageString_Revision369AndHigherValidLineWithMultiplePossibleValues_True()
			throws Exception {
		mLine = "CELL:1:2:3:4+:5:0:1,2,3,4,:false:false:true";
		int mRevisionNumber = 369;

		CellBuilder cellBuilder = mCellStorage
				.getCellBuilderFromStorageString(mLine, mRevisionNumber);

		List<Integer> possibles = new ArrayList<Integer>();
		possibles.add(1);
		possibles.add(2);
		possibles.add(3);
		possibles.add(4);
		CellBuilder expectedCellBuilder = new CellBuilder()
				.setId(1)
				.setCageText("4+")
				.setCorrectValue(5)
				.setPossibles(possibles)
				.setSelected(true);
		assertThat(cellBuilder, is(expectedCellBuilder));
	}

	@Test
	public void FromStorageString_Revision597AndHigherValidLineWithMultiplePossibleValues_True()
			throws Exception {
		mLine = "CELL:1:2-:3:0:1,2,3,4,:false:false:true";
		int mRevisionNumber = 597;

		CellBuilder cellBuilder = mCellStorage
				.getCellBuilderFromStorageString(mLine, mRevisionNumber);

		List<Integer> possibles = new ArrayList<Integer>();
		possibles.add(1);
		possibles.add(2);
		possibles.add(3);
		possibles.add(4);
		CellBuilder expectedCellBuilder = new CellBuilder()
				.setId(1)
				.setCageText("2-")
				.setCorrectValue(3)
				.setPossibles(possibles)
				.setSelected(true);
		assertThat(cellBuilder, is(expectedCellBuilder));
	}

	@Test
	public void toStorageString_CellWithoutPossibleValues_StorageStringCreated()
			throws Exception {
		Cell cellMock = mock(Cell.class);
		when(cellMock.getCellId()).thenReturn(1);
		when(cellMock.getCageText()).thenReturn("2+");
		when(cellMock.getCorrectValue()).thenReturn(3);
		when(cellMock.getEnteredValue()).thenReturn(4);
		when(cellMock.getPossibles()).thenReturn(new ArrayList<Integer>());
		when(cellMock.hasInvalidValueHighlight()).thenReturn(true);
		when(cellMock.isRevealed()).thenReturn(false);
		when(cellMock.isSelected()).thenReturn(false);
		assertThat(mCellStorage.toStorageString(cellMock),
				is("CELL:1:2+:3:4::true:false:false"));
	}

	@Test
	public void toStorageString_CellWithSinglePossibleValue_StorageStringCreated()
			throws Exception {
		Cell cellMock = mock(Cell.class);
		when(cellMock.getCellId()).thenReturn(1);
		when(cellMock.getCageText()).thenReturn("2x");
		when(cellMock.getCorrectValue()).thenReturn(3);
		when(cellMock.getEnteredValue()).thenReturn(4);
		List<Integer> possibles = new ArrayList<Integer>();
		possibles.add(5);
		when(cellMock.getPossibles()).thenReturn(possibles);
		when(cellMock.hasInvalidValueHighlight()).thenReturn(false);
		when(cellMock.isRevealed()).thenReturn(true);
		when(cellMock.isSelected()).thenReturn(false);
		assertThat(mCellStorage.toStorageString(cellMock),
				is("CELL:1:2x:3:4:5,:false:true:false"));
	}

	@Test
	public void toStorageString_CellWithMultiplePossibleValue_StorageStringCreated()
			throws Exception {
		Cell cellMock = mock(Cell.class);
		when(cellMock.getCellId()).thenReturn(1);
		when(cellMock.getCageText()).thenReturn("2/");
		when(cellMock.getCorrectValue()).thenReturn(3);
		when(cellMock.getEnteredValue()).thenReturn(4);
		List<Integer> possibles = new ArrayList<Integer>();
		possibles.add(5);
		possibles.add(6);
		possibles.add(7);
		when(cellMock.getPossibles()).thenReturn(possibles);
		when(cellMock.hasInvalidValueHighlight()).thenReturn(false);
		when(cellMock.isRevealed()).thenReturn(false);
		when(cellMock.isSelected()).thenReturn(true);
		assertThat(mCellStorage.toStorageString(cellMock),
				is("CELL:1:2/:3:4:5,6,7,:false:false:true"));
	}
}
