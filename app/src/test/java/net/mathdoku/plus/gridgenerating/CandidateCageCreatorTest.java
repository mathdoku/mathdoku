package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.gridgenerating.CellCoordinates.CellCoordinates;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CandidateCageCreatorTest {
	private CandidateCageCreator candidateCageCreator;
	private GridGeneratingParameters gridGeneratingParametersMock = mock(GridGeneratingParameters.class);
	private Random randomMock = mock(Random.class);
	private Matrix<Integer> correctValueMatrix;
	private Matrix<Integer> cageIdMatrix;
	private OverlappingSubsetChecker overlappingSubsetCheckerMock = mock(OverlappingSubsetChecker.class);
	private CageOperatorGenerator cageOperatorGeneratorMock = mock(CageOperatorGenerator.class);
	private CellCoordinates[] arrayOfCellCoordinates;
	private final GridType gridType = GridType.GRID_2X2;
	private final int sizeOfMatrix = gridType.getGridSize();
	private final CellCoordinates validCellCoordinates1 = new CellCoordinates(
			0, 0);
	private final CellCoordinates validCellCoordinates2 = new CellCoordinates(
			1, 1);
	private final CellCoordinates invalidCellCoordinates = new CellCoordinates(
			sizeOfMatrix, sizeOfMatrix);

	private class CandidateCageCreatorParametersStub extends
			CandidateCageCreatorParameters {
		@Override
		public OverlappingSubsetChecker getOverlappingSubsetChecker() {
			return overlappingSubsetCheckerMock;
		}

		@Override
		public CageOperatorGenerator createCageOperatorGenerator(int... cellValues) {
			return cageOperatorGeneratorMock;
		}
	}

	@Before
	public void setup() {
		correctValueMatrix = new Matrix<Integer>(sizeOfMatrix,
				Cell.NO_ENTERED_VALUE);
		cageIdMatrix = new Matrix<Integer>(sizeOfMatrix, Cage.CAGE_ID_NOT_SET);

		CandidateCageCreatorParameters candidateCageCreatorParametersStub;
		candidateCageCreatorParametersStub = new CandidateCageCreatorParametersStub()
				.setGridGeneratingParameters(gridGeneratingParametersMock)
				.setRandom(randomMock)
				.setCorrectValueMatrix(correctValueMatrix)
				.setCageIdMatrix(cageIdMatrix);
		candidateCageCreator = new CandidateCageCreator(
				candidateCageCreatorParametersStub);
	}

	@Test
	public void cageTypeDoesNotFitAtCellCoordinates_ListOfCellCoordinatesContainsInvalidCellCoordinates_DoesNotFit()
			throws Exception {
		setInvalidCellCoordinates();
		assertThatCageTypeDoesNotFitAtCellCoordinates(is(true));
	}

	private void setInvalidCellCoordinates() {
		fillArrayOfCellCoordinatesWithCoordinates(validCellCoordinates1,
				validCellCoordinates2, invalidCellCoordinates);
	}

	private void fillArrayOfCellCoordinatesWithCoordinates(
			CellCoordinates... cellCoordinates) {
		arrayOfCellCoordinates = cellCoordinates;
	}

	private void assertThatCageTypeDoesNotFitAtCellCoordinates(
			Matcher<Boolean> doesFitMatcher) {
		CellCoordinates originCellCoordinates = new CellCoordinates(0, 0);

		CageType cageType = mock(CageType.class);
		when(cageType.getCellCoordinatesOfAllCellsInCage(originCellCoordinates))
				.thenReturn(arrayOfCellCoordinates);

		assertThat(candidateCageCreator.cageTypeDoesNotFitAtCellCoordinates(
				cageType, originCellCoordinates), doesFitMatcher);
	}

	@Test
	public void cageTypeDoesNotFitAtCellCoordinates_ListOfCellCoordinatesAreNotAllEmpty_DoesNotFit()
			throws Exception {
		setOneCellCoordinatesToNotEmpty();
		assertThatCageTypeDoesNotFitAtCellCoordinates(is(true));
	}

	private void setOneCellCoordinatesToNotEmpty() {
		setValidCellCoordinates();
		CellCoordinates lastCellCoordinates = arrayOfCellCoordinates[arrayOfCellCoordinates.length - 1];
		cageIdMatrix.setValueToCellCoordinates(2, lastCellCoordinates);
	}

	private void setValidCellCoordinates() {
		fillArrayOfCellCoordinatesWithCoordinates(validCellCoordinates1,
				validCellCoordinates2);
	}

	@Test
	public void cageTypeDoesNotFitAtCellCoordinates_ListOfCellCoordinatesHasOverlappingSubsetsOfValues_DoesNotFit()
			throws Exception {
		setValidCellCoordinates();
		newCageHasOverlappingSubsetsOfValuesWithExistingCages();

		assertThatCageTypeDoesNotFitAtCellCoordinates(is(true));
	}

	private void newCageHasOverlappingSubsetsOfValuesWithExistingCages() {
		when(
				overlappingSubsetCheckerMock.hasOverlap(any(Matrix.class),
						any(Matrix.class))).thenReturn(true);
	}

	@Test
	public void cageTypeDoesNotFitAtCellCoordinates_CageTypeFitsAtOrigin_DoesFit()
			throws Exception {
		setValidCellCoordinates();
		newCageHasNoOverlappingSubsetsOfValuesWithExistingCages();

		assertThatCageTypeDoesNotFitAtCellCoordinates(is(false));
	}

	private void newCageHasNoOverlappingSubsetsOfValuesWithExistingCages() {
		when(
				overlappingSubsetCheckerMock.hasOverlap(any(Matrix.class),
														any(Matrix.class))).thenReturn(false);
	}

	@Test
	public void getCellsCoordinates() throws Exception {
		setValidCellCoordinates();
		newCageHasNoOverlappingSubsetsOfValuesWithExistingCages();

		assertThatCageTypeDoesNotFitAtCellCoordinates(is(false));

		assertThat(candidateCageCreator.getCellsCoordinates(),
				   is(sameInstance(arrayOfCellCoordinates)));
	}

	@Test
	public void create() throws Exception {
		List<Cell> cells = new ArrayList<Cell>();

		int idCellMock1 = 4;
		int correctValueCellMock1 = 2;
		Cell cellMock1 = mock(Cell.class);
		when(cellMock1.getCellId()).thenReturn(idCellMock1);
		when(cellMock1.getCorrectValue()).thenReturn(correctValueCellMock1);
		cells.add(cellMock1);

		int idCellMock2 = 8;
		int correctValueCellMock2 = 1;
		Cell cellMock2 = mock(Cell.class);
		when(cellMock2.getCellId()).thenReturn(idCellMock2);
		when(cellMock2.getCorrectValue()).thenReturn(correctValueCellMock2);
		cells.add(cellMock2);

		CageOperator cageOperatorMock = CageOperator.MULTIPLY;
		when(cageOperatorGeneratorMock.getCageOperator()).thenReturn(
				cageOperatorMock);

		int cageResult = correctValueCellMock1 * correctValueCellMock2;
		when(cageOperatorGeneratorMock.getCageResult()).thenReturn(cageResult);

		boolean hideOperators = true;
		when(gridGeneratingParametersMock.isHideOperators()).thenReturn(hideOperators);

		int cageId = 3;
		Cage cage = candidateCageCreator.create(cageId, cells);

		assertThat(cage, is(notNullValue()));
		assertThat(cage.getId(), is(cageId));
		assertThat(cage.getCells(), is(new int[] { idCellMock1, idCellMock2 }));
		assertThat(cage.getOperator(), is(cageOperatorMock));
		assertThat(cage.getResult(), is(cageResult));
		assertThat(cage.isOperatorHidden(), is(hideOperators));
		assertThat(cage.getPossibleCombos(), is(nullValue()));
	}
}
