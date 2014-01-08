package net.mathdoku.plus.storage;

import net.mathdoku.plus.grid.CellChange;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridStorageTest {
	private GridStorage gridStorage;

	private Grid mGridMock = mock(Grid.class);
	private ArrayList<CellChange> mGridMockMoves;
	private GridCell mGridCellMock = mock(GridCell.class);
	private GridCage mGridCageMock = mock(GridCage.class);
	private CellChange mCellChangeMock = mock(CellChange.class);
	private GridCageStorage mGridCageStorageMock = mock(GridCageStorage.class);

	@Before
	public void setup() {
		mGridMock.mCells = new ArrayList<GridCell>();
		mGridMock.mCages = new ArrayList<GridCage>();

		// The Grid array list of moves cannot be accessed directly.
		mGridMockMoves = new ArrayList<CellChange>();
		when(mGridMock.getCellChanges()).thenReturn(mGridMockMoves);


		GridStorage.ObjectsCreator gridStorageObjectsCreator = new GridStorage.ObjectsCreator() {
			@Override
			public GridCageStorage createGridCageStorage() {
				return mGridCageStorageMock;
			}
		};
		gridStorage = new GridStorage(gridStorageObjectsCreator);
	}

	@Test(expected = NullPointerException.class)
	public void fromStorageString_StorageStringIsNull_NullPointerException()
			throws Exception {
		String storageString = null;
		int revisionNumber = 596;

		gridStorage.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringIsEmpty_InvalidParameterException()
			throws Exception {
		String storageString = "";
		int revisionNumber = 596;

		gridStorage.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasIncorrectNumberOfElementsForRevisionLessOrEqualTo595_InvalidParameterException()
			throws Exception {
		String storageString = "GRID:too little arguments";
		int revisionNumber = 595;

		gridStorage.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasIncorrectNumberOfElementsForRevisionGreaterOrEqualTo596_InvalidParameterException()
			throws Exception {
		String storageString = "GRID:too:many:arguments";
		int revisionNumber = 596;

		gridStorage.fromStorageString(storageString, revisionNumber);
	}

	@Test
	public void fromStorageString_InvalidLineId_False() throws Exception {
		String storageString = "WRONG:true:true";
		int revisionNumber = 596;

		assertThat(
				gridStorage.fromStorageString(storageString, revisionNumber),
				is(false));
	}

	@Test
	public void fromStorageString_RevisionIdTooLow_False() throws Exception {
		String storageString = "GRID:true:true:1";
		int revisionNumber = 368;

		assertThat(
				gridStorage.fromStorageString(storageString, revisionNumber),
				is(false));
	}

	@Test
	public void fromStorageString_ValidStorageStringRevision595_True()
			throws Exception {
		String storageString = "GRID:true:false:1";
		int revisionNumber = 595;

		assertThat(
				gridStorage.fromStorageString(storageString, revisionNumber),
				is(true));
		assertThat(gridStorage.isActive(), is(true));
		assertThat(gridStorage.isRevealed(), is(false));
		// The last element of the storage string ("1") is no longer processed
		// by the method and can therefore not be verified.
	}

	@Test
	public void fromStorageString_ValidStorageStringRevision596_True()
			throws Exception {
		String storageString = "GRID:false:true";
		int revisionNumber = 596;

		assertThat(
				gridStorage.fromStorageString(storageString, revisionNumber),
				is(true));
		assertThat(gridStorage.isActive(), is(false));
		assertThat(gridStorage.isRevealed(), is(true));
	}

	@Test
	public void toStorageString_SaveNewGrid_StorageStringCreated()
			throws Exception {
		when(mGridMock.isActive()).thenReturn(false);
		when(mGridMock.isSolutionRevealed()).thenReturn(false);
		assertThat("Storage string", gridStorage.toStorageString(mGridMock),
				is(equalTo("GRID:false:false" + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCell_StorageStringCreated()
			throws Exception {
		when(mGridMock.isActive()).thenReturn(false);
		when(mGridMock.isSolutionRevealed()).thenReturn(false);
		String gridCellStubStorageString = "** A CELL STORAGE STRING **";
		when(mGridCellMock.toStorageString()).thenReturn(
				gridCellStubStorageString);
		mGridMock.mCells.add(mGridCellMock);

		assertThat("Storage string", gridStorage.toStorageString(mGridMock),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCellStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCell_StorageStringCreated()
			throws Exception {
		when(mGridMock.isActive()).thenReturn(false);
		when(mGridMock.isSolutionRevealed()).thenReturn(true);
		String gridCellStubStorageString1[] = {
				"** FIRST CELL STORAGE STRING **",
				"** SECOND CELL STORAGE STRING **" };
		when(mGridCellMock.toStorageString()).thenReturn(
				gridCellStubStorageString1[0], gridCellStubStorageString1[1]);
		mGridMock.mCells.add(mGridCellMock);
		mGridMock.mCells.add(mGridCellMock);

		assertThat("Storage string", gridStorage.toStorageString(mGridMock),
				is(equalTo("GRID:false:true" + "\n"
						+ gridCellStubStorageString1[0] + "\n"
						+ gridCellStubStorageString1[1] + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCage_StorageStringCreated()
			throws Exception {
		when(mGridMock.isActive()).thenReturn(true);
		when(mGridMock.isSolutionRevealed()).thenReturn(false);
		String gridCageStubStorageString = "** A CAGE STORAGE STRING **";
		when(mGridCageStorageMock.toStorageString(any(GridCage.class))).thenReturn(
				gridCageStubStorageString);
		mGridMock.mCages.add(mGridCageMock);

		assertThat("Storage string", gridStorage.toStorageString(mGridMock),
				is(equalTo("GRID:true:false" + "\n" + gridCageStubStorageString
						+ "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCage_StorageStringCreated()
			throws Exception {
		when(mGridMock.isActive()).thenReturn(true);
		when(mGridMock.isSolutionRevealed()).thenReturn(true);
		String gridCageStubStorageString1[] = {
				"** FIRST CAGE STORAGE STRING **",
				"** SECOND CAGE STORAGE STRING **" };
		when(mGridCageStorageMock.toStorageString(any(GridCage.class))).thenReturn(
				gridCageStubStorageString1[0], gridCageStubStorageString1[1]);
		mGridMock.mCages.add(mGridCageMock);
		mGridMock.mCages.add(mGridCageMock);

		assertThat("Storage string", gridStorage.toStorageString(mGridMock),
				is(equalTo("GRID:true:true" + "\n"
						+ gridCageStubStorageString1[0] + "\n"
						+ gridCageStubStorageString1[1] + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCellChange_StorageStringCreated()
			throws Exception {
		when(mGridMock.isActive()).thenReturn(false);
		when(mGridMock.isSolutionRevealed()).thenReturn(false);
		String mCellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		when(mCellChangeMock.toStorageString()).thenReturn(
				mCellChangeStubStorageString);
		mGridMockMoves.add(mCellChangeMock);

		assertThat("Storage string", gridStorage.toStorageString(mGridMock),
				is(equalTo("GRID:false:false" + "\n"
						+ mCellChangeStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCellChange_StorageStringCreated()
			throws Exception {
		when(mGridMock.isActive()).thenReturn(false);
		when(mGridMock.isSolutionRevealed()).thenReturn(false);
		CellChange cellChangeStub1 = mock(CellChange.class);
		String cellChangeStubStorageString1 = "** FIRST CELL CHANGE STORAGE STRING **";
		when(cellChangeStub1.toStorageString()).thenReturn(
				cellChangeStubStorageString1);
		mGridMockMoves.add(cellChangeStub1);

		CellChange cellChangeStub2 = mock(CellChange.class);
		String cellChangeStubStorageString2 = "** SECOND CELL CHANGE STORAGE STRING **";
		when(cellChangeStub2.toStorageString()).thenReturn(
				cellChangeStubStorageString2);
		mGridMockMoves.add(cellChangeStub2);

		assertThat("Storage string", gridStorage.toStorageString(mGridMock),
				is(equalTo("GRID:false:false" + "\n"
						+ cellChangeStubStorageString1 + "\n"
						+ cellChangeStubStorageString2 + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithCellAndCageAndCellChange_StorageStringCreated()
			throws Exception {
		when(mGridMock.isActive()).thenReturn(false);
		when(mGridMock.isSolutionRevealed()).thenReturn(false);
		String gridCellStubStorageString = "** A CELL STORAGE STRING **";
		when(mGridCellMock.toStorageString()).thenReturn(
				gridCellStubStorageString);
		mGridMock.mCells.add(mGridCellMock);

		String gridCageStubStorageString = "** A CAGE STORAGE STRING **";
		when(mGridCageStorageMock.toStorageString(any(GridCage.class))).thenReturn(
				gridCageStubStorageString);
		mGridMock.mCages.add(mGridCageMock);

		String mCellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		when(mCellChangeMock.toStorageString()).thenReturn(
				mCellChangeStubStorageString);
		mGridMockMoves.add(mCellChangeMock);

		assertThat("Storage string", gridStorage.toStorageString(mGridMock),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCellStubStorageString + "\n"
						+ gridCageStubStorageString + "\n"
						+ mCellChangeStubStorageString + "\n")));
	}
}
