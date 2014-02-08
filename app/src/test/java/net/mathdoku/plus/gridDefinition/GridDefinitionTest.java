package net.mathdoku.plus.gridDefinition;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.grid.GridObjectsCreator;
import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import com.srlee.DLX.MathDokuDLX;

@RunWith(RobolectricGradleTestRunner.class)
public class GridDefinitionTest {
	private GridDefinition mGridDefinition;
	private GridObjectsCreatorStub mGridObjectsCreatorStub;
	private String mGridDefinitionString;
	private Grid mGrid;
	private MathDokuDLX mMathDokuDLXMock = mock(MathDokuDLX.class);

	private class GridObjectsCreatorStub extends GridObjectsCreator {
		List<GridCell> gridCells;
		List<GridCage> gridCages;

		@Override
		public List<GridCell> createArrayListOfGridCells() {
			gridCells = super.createArrayListOfGridCells();
			return gridCells;
		}

		@Override
		public List<GridCage> createArrayListOfGridCages() {
			gridCages = super.createArrayListOfGridCages();
			return gridCages;
		}

		@Override
		public MathDokuDLX createMathDokuDLX(int gridSize,
				List<GridCage> cages) {
			return mMathDokuDLXMock;
		}
	}

	@Before
	public void setup() {
		mGridObjectsCreatorStub = new GridObjectsCreatorStub();
		mGridDefinition = new GridDefinition();
		mGridDefinition.setObjectsCreator(mGridObjectsCreatorStub);

		// Even when running the unit test in the debug variant, the grid loader
		// should not throw development exceptions as the tests below only test
		// the release variant in which no such exceptions are thrown.
		if (Config.mAppMode == Config.AppMode.DEVELOPMENT) {
			// Disable this until all unit tests succeed in development mode!
			mGridDefinition.setThrowExceptionOnError(false);
		}

	}

	@Test(expected = InvalidParameterException.class)
	public void getDefinition_ArrayListGridCellsIsNull_ThrowsInvalidParameterException()
			throws Exception {
		List<GridCell> gridCells = null;
		List<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		GridDefinition.getDefinition(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void getDefinition_ArrayListGridCellsIsEmpty_ThrowsInvalidParameterException()
			throws Exception {
		List<GridCell> gridCells = new ArrayList<GridCell>();
		List<GridCage> gridCages = mock(ArrayList.class);
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		GridDefinition.getDefinition(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void getDefinition_ArrayListGridCagesIsNull_ThrowsInvalidParameterException()
			throws Exception {
		List<GridCell> gridCells = mock(ArrayList.class);
		when(gridCells.size()).thenReturn(1);
		List<GridCage> gridCages = null;
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		GridDefinition.getDefinition(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void getDefinition_ArrayListGridCagesIsEmpty_ThrowsInvalidParameterException()
			throws Exception {
		List<GridCell> gridCells = mock(ArrayList.class);
		when(gridCells.size()).thenReturn(1);
		List<GridCage> gridCages = new ArrayList<GridCage>();
		GridGeneratingParameters gridGeneratingParameters = mock(GridGeneratingParameters.class);

		GridDefinition.getDefinition(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test(expected = InvalidParameterException.class)
	public void getDefinition_GridGeneratingParametersIsNull_ThrowsInvalidParameterException()
			throws Exception {
		List<GridCell> gridCells = mock(ArrayList.class);
		when(gridCells.size()).thenReturn(1);
		List<GridCage> gridCages = mock(ArrayList.class);
		when(gridCages.size()).thenReturn(1);
		GridGeneratingParameters gridGeneratingParameters = null;

		GridDefinition.getDefinition(gridCells, gridCages,
				gridGeneratingParameters);
	}

	@Test
	public void getDefinition_WithValidParameters_GridDefinitionCreated()
			throws Exception {
		GridCell gridCellMock = mock(GridCell.class);
		when(gridCellMock.getCageId()).thenReturn( //
				0, 1, 2, 2, // Row 1
				0, 1, 2, 2, // Row 2
				1, 1, 1, 2, // Row 3
				3, 3, 3, 2 // Row 4
				);
		List<GridCell> gridCells = new ArrayList<GridCell>();
		for (int i = 0; i < 16; i++) {
			gridCells.add(gridCellMock);
		}

		GridCage gridCageStub1 = mock(GridCage.class);
		when(gridCageStub1.getId()).thenReturn(0);
		when(gridCageStub1.getResult()).thenReturn(1);
		when(gridCageStub1.getOperator()).thenReturn(CageOperator.NONE);

		GridCage gridCageStub2 = mock(GridCage.class);
		when(gridCageStub2.getId()).thenReturn(1);
		when(gridCageStub2.getResult()).thenReturn(3);
		when(gridCageStub2.getOperator()).thenReturn(CageOperator.ADD);

		GridCage gridCageStub3 = mock(GridCage.class);
		when(gridCageStub3.getId()).thenReturn(2);
		when(gridCageStub3.getResult()).thenReturn(2);
		when(gridCageStub3.getOperator()).thenReturn(CageOperator.NONE);

		List<GridCage> gridCages = new ArrayList<GridCage>();
		gridCages.add(gridCageStub1);
		gridCages.add(gridCageStub2);
		gridCages.add(gridCageStub3);

		GridGeneratingParameters gridGeneratingParameters = new GridGeneratingParameters();
		gridGeneratingParameters.mPuzzleComplexity = PuzzleComplexity.NORMAL;
		gridGeneratingParameters.mHideOperators = false;

		assertThat(
				"Grid definition",
				GridDefinition.getDefinition(gridCells, gridCages,
						gridGeneratingParameters),
				is(equalTo("3:00010202000102020101010203030302:0,1,0:1,3,1:2,2,0")));
	}

	@Test
	public void createGrid_NullDefinition_NoGridCreated() throws Exception {
		mGridDefinitionString = null;

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_EmptyDefinition_NoGridCreated() throws Exception {
		mGridDefinitionString = "";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasTooLittleElements_NoGridCreated()
			throws Exception {
		mGridDefinitionString = "1:02";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionWithNonNumericPuzzleComplexity_NoGridCreated()
			throws Exception {
		mGridDefinitionString = "INVALID PUZZLE COMPLEXITY ID:00:0,9,1";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionWithTooBigNumericPuzzleComplexity_NoGridCreated()
			throws Exception {
		mGridDefinitionString = "10:00:0,9,1";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasEmptyCellCagesPart_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1::0,9,0";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasCellCagesPartWithUnevenNumberOfDigitis_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:001:0,9,0";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasCellCagesPartWithNonNumericCharacters_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:0x:0,9,0";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasCagePartWithTooLittleElements_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasCagePartWithTooManyElements_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9,1,2";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasCagePartWithEmptyCageId_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:,9,0";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasCagePartWithEmptyCageResult_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,,0";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasCagePartWithEmptyCageOperator_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9,";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionHasCagePartWithTooBigCageOperator_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9,10";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionWithVeryEasyComplexity_GridCreated()
			throws Exception {
		createGrid_DefinitionWithGivenComplexity_GridCreated("1",
				PuzzleComplexity.VERY_EASY);
	}

	@Test
	public void createGrid_DefinitionWithEasyComplexity_GridCreated()
			throws Exception {
		createGrid_DefinitionWithGivenComplexity_GridCreated("2",
				PuzzleComplexity.EASY);
	}

	@Test
	public void createGrid_DefinitionWithNormalComplexity_GridCreated()
			throws Exception {
		createGrid_DefinitionWithGivenComplexity_GridCreated("3",
				PuzzleComplexity.NORMAL);
	}

	@Test
	public void createGrid_DefinitionWithDifficultComplexity_GridCreated()
			throws Exception {
		createGrid_DefinitionWithGivenComplexity_GridCreated("4",
				PuzzleComplexity.DIFFICULT);
	}

	@Test
	public void createGrid_DefinitionWithVeryDifficultComplexity_GridCreated()
			throws Exception {
		createGrid_DefinitionWithGivenComplexity_GridCreated("5",
				PuzzleComplexity.VERY_DIFFICULT);
	}

	@Test
	public void createGrid_DefinitionHasCellCagesPartForInvalidNumberOfCells_GridNotCreated()
			throws Exception {
		for (int i = 1; i <= 82; i++) {
			switch (i) {
			case 1: // fall through
			case 4: // fall through
			case 9: // fall through
			case 16: // fall through
			case 25: // fall through
			case 36: // fall through
			case 49: // fall through
			case 64: // fall through
			case 81: // fall through
				// The test should not be executed for these number of cells as
				// these are valid lengths
				continue;
			default:
				createGrid_DefinitionHasCellCagesPartForInvalidNumberOfCells_GridNotCreated(i);
			}
		}
	}

	@Test
	public void createGrid_DefinitionHasCellCagesPartForValidNumberOfCells_GridCreated()
			throws Exception {
		// For grid size 1, the None operator has to be used as this is the only
		// valid operator for a single cage cell.
		createGrid_DefinitionHasCellCagesPartForValidNumberOfCells_GridCreated(
				1, "1:00:0,9,0");
		// For the other grid sizes the add or multiply operator has to be used
		// as all cells are part of one cage having at least 4 (2 * 2) cells.
		int j = 1;
		StringBuilder cellParts = new StringBuilder();
		for (int gridSize = 2; gridSize <= 9; gridSize++) {
			for (; j <= gridSize * gridSize; j++) {
				cellParts.append("00");
			}
			createGrid_DefinitionHasCellCagesPartForValidNumberOfCells_GridCreated(
					gridSize, "1:" + cellParts.toString() + ":0,9,1");
		}
	}

	@Test
	public void createGrid_DefinitionCellRefersToNonExistentCage_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:01:0,9,1";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionCageHasUnexpectedId_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:1,9,1";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionDoesNotHaveAUniqueSolution_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9,0";
		when(mMathDokuDLXMock.getSolutionGrid()).thenReturn(null);

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionInvalidCageOperator_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9,9";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_DefinitionCageOperatorNone_GridCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9,0";
		int solution[][] = { { 9 } };
		when(mMathDokuDLXMock.getSolutionGrid()).thenReturn(solution);

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(notNullValue()));
		assertThat(mGridObjectsCreatorStub.gridCages.get(0).getOperator(),
				is(CageOperator.NONE));
	}

	@Test
	public void createGrid_DefinitionCageOperatorAdd_GridCreated()
			throws Exception {
		createGrid_DefinitionWithValidCageOperator_GridCreated("1",
				CageOperator.ADD);
	}

	@Test
	public void createGrid_DefinitionCageOperatorSubtract_GridCreated()
			throws Exception {
		createGrid_DefinitionWithValidCageOperator_GridCreated("2",
				CageOperator.SUBTRACT);
	}

	@Test
	public void createGrid_DefinitionCageOperatorMultiply_GridCreated()
			throws Exception {
		createGrid_DefinitionWithValidCageOperator_GridCreated("3",
				CageOperator.MULTIPLY);
	}

	@Test
	public void createGrid_DefinitionCageOperatorDivide_GridCreated()
			throws Exception {
		createGrid_DefinitionWithValidCageOperator_GridCreated("4",
				CageOperator.DIVIDE);
	}

	@Test
	public void createGrid_SolutionHasUnexpectedNumberOfRows_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9,0";
		int solution[][] = { { 1 }, { 2 } };
		when(mMathDokuDLXMock.getSolutionGrid()).thenReturn(solution);

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	@Test
	public void createGrid_SolutionHasUnexpectedNumberOfColumns_GridNotCreated()
			throws Exception {
		mGridDefinitionString = "1:00:0,9,0";
		int solution[][] = { { 1, 2 } };
		when(mMathDokuDLXMock.getSolutionGrid()).thenReturn(solution);

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	private void createGrid_DefinitionHasCellCagesPartForInvalidNumberOfCells_GridNotCreated(
			int numberOfCellsInCagePart) throws Exception {
		StringBuilder cagesPart = new StringBuilder();
		for (int i = 0; i < numberOfCellsInCagePart; i++) {
			cagesPart.append("00");
		}
		mGridDefinitionString = "1:" + cagesPart + ":0,9,0";

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(nullValue()));
	}

	private void createGrid_DefinitionHasCellCagesPartForValidNumberOfCells_GridCreated(
			int gridSize, String definitionString) throws Exception {
		mGridDefinitionString = definitionString;
		// The mMathdokuDLXMock has to return a solution matrix of correct size.
		// The content does not need to be correct for this test.
		int solution[][] = new int[gridSize][gridSize];
		when(mMathDokuDLXMock.getSolutionGrid()).thenReturn(solution);
		mGridDefinition.setThrowExceptionOnError(true);

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(notNullValue()));
	}

	private void createGrid_DefinitionWithGivenComplexity_GridCreated(
			String puzzleComplexityString, PuzzleComplexity puzzleComplexity) {
		mGridDefinitionString = puzzleComplexityString + ":00:0,9,0";
		int solution[][] = { { 9 } };
		when(mMathDokuDLXMock.getSolutionGrid()).thenReturn(solution);

		mGrid = mGridDefinition.createGrid(mGridDefinitionString);

		// Additional assertions in case an unexpected error occurs
		// assertThat(mGridObjectsCreatorStub.gridCages.size(), is(1));
		// assertThatCageHasIdAndResultAndOperator(0, 9, CageOperator.NONE);
		// assertThat(mGridObjectsCreatorStub.gridCells.size(), is(1));
		// assertThatCellHasIdAndCageIdAndCorrectValue(0, 0, solution[0][0]);
		assertThat(mGrid, is(notNullValue()));
		assertThat(mGrid.getPuzzleComplexity(), is(puzzleComplexity));
	}

	private void assertThatCageHasIdAndResultAndOperator(int cageId,
			int cageResult, CageOperator cageOperator) {
		assertThat(mGridObjectsCreatorStub.gridCages, is(notNullValue()));
		GridCage cage = mGridObjectsCreatorStub.gridCages.get(cageId);
		assertThat(cage, is(notNullValue()));
		assertThat(cage.getId(), is(cageId));
		assertThat(cage.getResult(), is(cageResult));
		assertThat(cage.getOperator(), is(cageOperator));
	}

	private void createGrid_DefinitionWithValidCageOperator_GridCreated(
			String cageOperatorString, CageOperator cageOperator) {
		mGridDefinitionString = "1:00010201:0,1,0:1,3," + cageOperatorString
				+ ":2,2,0";
		int solution[][] = { { 1, 2 }, { 2, 1 } };
		when(mMathDokuDLXMock.getSolutionGrid()).thenReturn(solution);

		assertThat(mGridDefinition.createGrid(mGridDefinitionString),
				is(notNullValue()));
		assertThat(mGridObjectsCreatorStub.gridCages.get(1).getOperator(),
				is(cageOperator));
	}
}
