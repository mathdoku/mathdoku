package net.mathdoku.plus.storage;

import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cellChange.CellChange;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridBuilder;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class GridStorageTest {
	private GridStorage gridStorage;
	private CellStorage mCellStorageMock = mock(CellStorage.class);
	private CellChangeStorage mCellChangeStorageMock = mock(CellChangeStorage.class);
	private CageStorage mCageStorageMock = mock(CageStorage.class);

	private class GridBuilderStub extends GridBuilder {
		int mGridSize;

		public GridBuilderStub() {
			super();

			setGridSize(4);

			super.setGridStatistics(mock(GridStatistics.class));

			super
					.setGridGeneratingParameters(mock(GridGeneratingParameters.class));
		}

		@Override
		public GridBuilder setGridSize(int gridSize) {
			mGridSize = gridSize;
			return super.setGridSize(gridSize);
		}

		/**
		 * Initializes the list of cells of the GridBuilder with the given grid
		 * cells.
		 */
		private GridBuilderStub addCellMockWithStorageString(String... storageString) {
			List<Cell> cells = new ArrayList<Cell>();

			// Create the number of cell as determined by the grid size. The
			// cells return the given storage strings if enough storage strings
			// are available.
			int numberOfCells = mGridSize * mGridSize;
			for (int i = 0; i < numberOfCells; i++) {
				Cell cell = mock(Cell.class);
				when(mCellStorageMock.toStorageString(cell))
						.thenReturn(
								i < storageString.length ? storageString[i]
										: "");
				cells.add(cell);
			}
			super.setCells(cells);

			return this;
		}

		/**
		 * Initializes the list of cages of the GridBuilder with the given grid
		 * cages.
		 */
		private GridBuilderStub addCageMockWithStorageString(String... storageString) {
			List<Cage> cages = new ArrayList<Cage>();

			for (int i = 0; i < storageString.length; i++) {
				Cage cage = mock(Cage.class);
				when(mCageStorageMock.toStorageString(cage))
						.thenReturn(storageString[i]);
				cages.add(cage);
			}
			super.setCages(cages);

			return this;
		}

		/**
		 * Initializes the list of cell changess of the GridBuilder with the
		 * given cell changes.
		 */
		private GridBuilderStub addCellChangeMockWithStorageString(
				String... storageString) {
			List<CellChange> cellChanges = new ArrayList<CellChange>();

			for (int i = 0; i < storageString.length; i++) {
				CellChange cellChange = mock(CellChange.class);
				when(mCellChangeStorageMock.toStorageString(cellChange))
						.thenReturn(storageString[i]);
				cellChanges.add(cellChange);
			}
			super.setCellChanges(cellChanges);

			return this;
		}
	}

	private class GridStorageTestObjectsCreator extends
			GridStorage.ObjectsCreator {
		@Override
		public CageStorage createCageStorage() {
			return mCageStorageMock;
		}

		@Override
		public CellStorage createCellStorage() {
			return mCellStorageMock;
		}

		@Override
		public CellChangeStorage createCellChangeStorage() {
			return mCellChangeStorageMock;
		}
	};

	private GridStorageTestObjectsCreator mGridStorageTestObjectsCreator;

	@Before
	public void setup() {
		gridStorage = new GridStorage();
		mGridStorageTestObjectsCreator = new GridStorageTestObjectsCreator();
		gridStorage.setObjectsCreator(mGridStorageTestObjectsCreator);
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
	public void toStorageString_SaveNewGridWithOneCellAndOneCage_StorageStringCreated()
			throws Exception {
		GridBuilderStub gridBuilderStub = new GridBuilderStub();
		gridBuilderStub.setGridSize(1);
		gridBuilderStub.setActive(true);
		gridBuilderStub.setRevealed(false);
		String cellStubStorageString = "** A CELL STORAGE STRING **";
		gridBuilderStub
				.addCellMockWithStorageString(cellStubStorageString);
		String cageStubStorageString = "** A CAGE STORAGE STRING **";
		gridBuilderStub
				.addCageMockWithStorageString(cageStubStorageString);
		Grid grid = gridBuilderStub.build();

		assertThat("Storage string", gridStorage.toStorageString(grid),
				is(equalTo("GRID:true:false" + "\n" + //
						cellStubStorageString + "\n" + //
						cageStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCellsAndOneCage_StorageStringCreated()
			throws Exception {
		GridBuilderStub gridBuilderStub = new GridBuilderStub();
		gridBuilderStub.setGridSize(2);
		gridBuilderStub.setActive(false);
		gridBuilderStub.setRevealed(true);
		String cellStubStorageString[] = { "** CELL 1 STORAGE STRING **",
				"** CELL 2 STORAGE STRING **", "** CELL 3 STORAGE STRING **",
				"** CELL 4 STORAGE STRING **" };
		gridBuilderStub
				.addCellMockWithStorageString(cellStubStorageString);
		String cageStubStorageString = "** A CAGE STORAGE STRING **";
		gridBuilderStub
				.addCageMockWithStorageString(cageStubStorageString);
		Grid grid = gridBuilderStub.build();

		assertThat("Storage string", gridStorage.toStorageString(grid),
				is(equalTo("GRID:false:true" + "\n"
						+ cellStubStorageString[0] + "\n"
						+ cellStubStorageString[1] + "\n"
						+ cellStubStorageString[2] + "\n"
						+ cellStubStorageString[3] + "\n"
						+ cageStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithMultipleCellsAndMultipleCages_StorageStringCreated()
			throws Exception {
		GridBuilderStub gridBuilderStub = new GridBuilderStub();
		gridBuilderStub.setGridSize(2);
		gridBuilderStub.setActive(false);
		gridBuilderStub.setRevealed(true);
		String cellStubStorageString[] = { "** CELL 1 STORAGE STRING **",
				"** CELL 2 STORAGE STRING **", "** CELL 3 STORAGE STRING **",
				"** CELL 4 STORAGE STRING **" };
		gridBuilderStub
				.addCellMockWithStorageString(cellStubStorageString);
		String cageStubStorageString[] = { "** CAGE 1 STORAGE STRING **",
				"** CAGE 2 STORAGE STRING **" };
		gridBuilderStub
				.addCageMockWithStorageString(cageStubStorageString);
		Grid grid = gridBuilderStub.build();

		assertThat("Storage string", gridStorage.toStorageString(grid),
				is(equalTo("GRID:false:true" + "\n"
						+ cellStubStorageString[0] + "\n"
						+ cellStubStorageString[1] + "\n"
						+ cellStubStorageString[2] + "\n"
						+ cellStubStorageString[3] + "\n"
						+ cageStubStorageString[0] + "\n"
						+ cageStubStorageString[1] + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCellAndOneCageAndOneCellChange_StorageStringCreated()
			throws Exception {
		GridBuilderStub gridBuilderStub = new GridBuilderStub();
		gridBuilderStub.setGridSize(1);
		gridBuilderStub.setActive(true);
		gridBuilderStub.setRevealed(false);
		String cellStubStorageString = "** A CELL STORAGE STRING **";
		gridBuilderStub
				.addCellMockWithStorageString(cellStubStorageString);
		String cageStubStorageString = "** A CAGE STORAGE STRING **";
		gridBuilderStub
				.addCageMockWithStorageString(cageStubStorageString);
		String mCellChangeStubStorageString = "** A CELL CHANGE STORAGE STRING **";
		gridBuilderStub
				.addCellChangeMockWithStorageString(mCellChangeStubStorageString);
		Grid grid = gridBuilderStub.build();

		assertThat("Storage string", gridStorage.toStorageString(grid),
				is(equalTo("GRID:true:false" + "\n" //
						+ cellStubStorageString + "\n" //
						+ cageStubStorageString + "\n" //
						+ mCellChangeStubStorageString + "\n")));
	}

	@Test
	public void toStorageString_SaveNewGridWithOneCellAndOneCageAndMultipleCellChange_StorageStringCreated()
			throws Exception {
		GridBuilderStub gridBuilderStub = new GridBuilderStub();
		gridBuilderStub.setGridSize(1);
		gridBuilderStub.setActive(true);
		gridBuilderStub.setRevealed(false);
		String cellStubStorageString = "** A CELL STORAGE STRING **";
		gridBuilderStub
				.addCellMockWithStorageString(cellStubStorageString);
		String cageStubStorageString = "** A CAGE STORAGE STRING **";
		gridBuilderStub
				.addCageMockWithStorageString(cageStubStorageString);
		String mCellChangeStubStorageString[] = {
				"** CELL CHANGE 1 STORAGE STRING **",
				"** CELL CHANGE 2 STORAGE STRING **" };
		gridBuilderStub
				.addCellChangeMockWithStorageString(mCellChangeStubStorageString);
		Grid grid = gridBuilderStub.build();

		assertThat("Storage string", gridStorage.toStorageString(grid),
				is(equalTo("GRID:true:false" + "\n" //
						+ cellStubStorageString + "\n" //
						+ cageStubStorageString + "\n" //
						+ mCellChangeStubStorageString[0] + "\n" //
						+ mCellChangeStubStorageString[1] + "\n")));
	}
}
