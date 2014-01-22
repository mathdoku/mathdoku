package net.mathdoku.plus.storage;

import net.mathdoku.plus.grid.CellChange;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;

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
	private GridStorageObjectsCreator mGridStorageObjectsCreator;
	private GridCellStorage mGridCellStorageMock = mock(GridCellStorage.class);
	private CellChangeStorage mCellChangeStorageMock = mock(CellChangeStorage.class);
	private GridCageStorage mGridCageStorageMock = mock(GridCageStorage.class);

	private class GridStub extends Grid {
		private boolean mIsActive;
		private boolean mIsSolutionRevealed;

		public void setIsActive(boolean isActive) {
			mIsActive = isActive;
		}

		public void setIsSolutionRevealed(boolean isSolutionRevealed) {
			mIsSolutionRevealed = isSolutionRevealed;
		}

		private void addGridCellMockWithStorageString(String... storageString) {
			switch (storageString.length) {
			case 1:
				mCells.add(mock(GridCell.class));
				when(mGridCellStorageMock.toStorageString(any(GridCell.class)))
						.thenReturn(storageString[0]);
				break;
			case 2:
				mCells.add(mock(GridCell.class));
				mCells.add(mock(GridCell.class));
				when(mGridCellStorageMock.toStorageString(any(GridCell.class)))
						.thenReturn(storageString[0], storageString[1]);
				break;
			default:
				throw new InvalidParameterException(
						"Invalid number of parameters in helper method addGridCellMockWithStorageString");
			}
		}

		private void addGridCageMockWithStorageString(String... storageString) {
			switch (storageString.length) {
			case 1:
				mCages.add(mock(GridCage.class));
				when(mGridCageStorageMock.toStorageString(any(GridCage.class)))
						.thenReturn(storageString[0]);
				break;
			case 2:
				mCages.add(mock(GridCage.class));
				mCages.add(mock(GridCage.class));
				when(mGridCageStorageMock.toStorageString(any(GridCage.class)))
						.thenReturn(storageString[0], storageString[1]);
				break;
			default:
				throw new InvalidParameterException(
						"Invalid number of parameters in helper method addGridCageMockWithStorageString");
			}
		}

		private void addCellChangeMockWithStorageString(String... storageString) {
			switch (storageString.length) {
				case 1:
					addMove(mock(CellChange.class));
					when(mCellChangeStorageMock.toStorageString(any(CellChange.class)))
							.thenReturn(storageString[0]);
					break;
				case 2:
					addMove(mock(CellChange.class));
					addMove(mock(CellChange.class));
					when(mCellChangeStorageMock.toStorageString(any(CellChange.class)))
							.thenReturn(storageString[0], storageString[1]);
					break;
				default:
					throw new InvalidParameterException(
							"Invalid number of parameters in helper method addCellChangeMockWithStorageString");
			}
		}

		@Override
		public boolean isActive() {
			return mIsActive;
		}

		@Override
		public boolean isSolutionRevealed() {
			return mIsSolutionRevealed;
		}
	}

	private GridStub mGridStub;

	@Before
	public void setup() {
		gridStorage = new GridStorage();
		mGridStorageObjectsCreator = new GridStorageObjectsCreator() {
			@Override
			public GridCageStorage createGridCageStorage() {
				return mGridCageStorageMock;
			}

			@Override
			public GridCellStorage createGridCellStorage() {
				return mGridCellStorageMock;
			}

			@Override
			public CellChangeStorage createCellChangeStorage() {
				return mCellChangeStorageMock;
			}
		};
		gridStorage.setObjectsCreator(mGridStorageObjectsCreator);

		mGridStub = new GridStub();
	}

	@Test(expected = NullPointerException.class)
	public void fromStorageString_StorageStringIsNull_NullPointerException()
			throws Exception {
		String storageString = null;
		int revisionNumber = 596;

		gridStorage.fromStorageString(storageString, revisionNumber);
	}

	@Test
	public void fromStorageString_InvalidLineId_False() throws Exception {
		String storageString = "WRONG:this is not a valid grid storage string";
		int revisionNumber = 596;

		assertThat(
				gridStorage.fromStorageString(storageString, revisionNumber),
				is(false));
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasTooLittleElementsForRevisionLessOrEqualTo595_InvalidParameterException()
			throws Exception {
		String storageString = "GRID:2:3";
		int revisionNumber = 595;

		gridStorage.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasTooManyElementsForRevisionLessOrEqualTo595_InvalidParameterException()
			throws Exception {
		String storageString = "GRID:2:3:4:5";
		int revisionNumber = 595;

		gridStorage.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasTooLittleElementsForRevisionLessOrEqualTo596_InvalidParameterException()
			throws Exception {
		String storageString = "GRID:2";
		int revisionNumber = 596;

		gridStorage.fromStorageString(storageString, revisionNumber);
	}

	@Test(expected = InvalidParameterException.class)
	public void fromStorageString_StorageStringHasTooManyElementsForRevisionLessOrEqualTo596_InvalidParameterException()
			throws Exception {
		String storageString = "GRID:2:3:4";
		int revisionNumber = 596;

		gridStorage.fromStorageString(storageString, revisionNumber);
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
		mGridStub.setIsActive(true);
		mGridStub.setIsSolutionRevealed(true);
		assertThat("Storage string", gridStorage.toStorageString(mGridStub),
				is(equalTo("GRID:true:true" + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCell_StorageStringCreated()
			throws Exception {
		mGridStub.setIsActive(true);
		mGridStub.setIsSolutionRevealed(false);
		String gridCellStubStorageString = "** A CELL STORAGE STRING **";
		mGridStub.addGridCellMockWithStorageString(gridCellStubStorageString);

		assertThat("Storage string", gridStorage.toStorageString(mGridStub),
				is(equalTo("GRID:true:false" + "\n" + gridCellStubStorageString
						+ "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCell_StorageStringCreated()
			throws Exception {
		mGridStub.setIsActive(false);
		mGridStub.setIsSolutionRevealed(true);
		String gridCellStubStorageString[] = {
				"** FIRST CELL STORAGE STRING **",
				"** SECOND CELL STORAGE STRING **" };
		mGridStub.addGridCellMockWithStorageString(gridCellStubStorageString);

		assertThat("Storage string", gridStorage.toStorageString(mGridStub),
				is(equalTo("GRID:false:true" + "\n"
						+ gridCellStubStorageString[0] + "\n"
						+ gridCellStubStorageString[1] + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCage_StorageStringCreated()
			throws Exception {
		mGridStub.setIsActive(false);
		mGridStub.setIsSolutionRevealed(false);
		String gridCageStubStorageString = "** A CAGE STORAGE STRING **";
		mGridStub.addGridCageMockWithStorageString(gridCageStubStorageString);

		assertThat("Storage string", gridStorage.toStorageString(mGridStub),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCageStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCage_StorageStringCreated()
			throws Exception {
		mGridStub.setIsActive(false);
		mGridStub.setIsSolutionRevealed(false);
		String gridCageStubStorageString[] = {
				"** FIRST CAGE STORAGE STRING **",
				"** SECOND CAGE STORAGE STRING **" };
		mGridStub.addGridCageMockWithStorageString(gridCageStubStorageString);

		assertThat("Storage string", gridStorage.toStorageString(mGridStub),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCageStubStorageString[0] + "\n"
						+ gridCageStubStorageString[1] + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCellChange_StorageStringCreated()
			throws Exception {
		mGridStub.setIsActive(false);
		mGridStub.setIsSolutionRevealed(false);
		String mCellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		mGridStub
				.addCellChangeMockWithStorageString(mCellChangeStubStorageString);

		assertThat("Storage string", gridStorage.toStorageString(mGridStub),
				is(equalTo("GRID:false:false" + "\n"
						+ mCellChangeStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCellChange_StorageStringCreated()
			throws Exception {
		mGridStub.setIsActive(false);
		mGridStub.setIsSolutionRevealed(false);
		String mCellChangeStubStorageString[] = {
				"** FIRST CELL CHANGE STORAGE STRING **",
				"** SECOND CELL CHANGE STORAGE STRING **" };
		mGridStub
				.addCellChangeMockWithStorageString(mCellChangeStubStorageString);

		assertThat("Storage string", gridStorage.toStorageString(mGridStub),
				is(equalTo("GRID:false:false" + "\n"
						+ mCellChangeStubStorageString[0] + "\n"
						+ mCellChangeStubStorageString[1] + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithCellAndCageAndCellChange_StorageStringCreated()
			throws Exception {
		mGridStub.setIsActive(false);
		mGridStub.setIsSolutionRevealed(false);
		String gridCellStubStorageString = "** A CELL STORAGE STRING **";
		mGridStub.addGridCellMockWithStorageString(gridCellStubStorageString);
		String gridCageStubStorageString = "** A CAGE STORAGE STRING **";
		mGridStub.addGridCageMockWithStorageString(gridCageStubStorageString);
		String mCellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		mGridStub
				.addCellChangeMockWithStorageString(mCellChangeStubStorageString);

		assertThat("Storage string", gridStorage.toStorageString(mGridStub),
				is(equalTo("GRID:false:false" + "\n"
						+ gridCellStubStorageString + "\n"
						+ gridCageStubStorageString + "\n"
						+ mCellChangeStubStorageString + "\n")));
	}
}
