package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.enums.CageOperator;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CageOperatorGeneratorTest {
	CageOperatorGenerator cageOperatorGenerator;
	final int ONE_CELL_VALUE = 3;
	final int[] TWO_CELL_VALUES = new int[] {3, 6};
	final int[] THREE_CELL_VALUES = new int[] {3, 6, 5};

	private class Setup {
		private int random;
		private int[] cellValues;
		private CageOperator[] cageOperators;

		// CageOperators and weight below should match with order defined in class CageOperatorGenerator.
		CageOperator[] orderedCageOperatorsInGenerator = new CageOperator[] {
			CageOperator.NONE, CageOperator.DIVIDE, CageOperator.SUBTRACT, CageOperator.MULTIPLY, CageOperator.ADD};
		int[] weightsForOrderedCageOperatorsInGenerator = new int[] {100, 50, 30, 15, 15};

		public Setup() {
			cellValues = TWO_CELL_VALUES;
			cageOperators = new CageOperator[] {CageOperator.ADD, CageOperator.SUBTRACT, CageOperator.MULTIPLY, CageOperator.DIVIDE};
		}
		
		public Setup setCellValues(int... cellValues) {
			this.cellValues = cellValues;
			return this;
		}

		public Setup setPossibleCageOperators(CageOperator... cageOperators) {
			this.cageOperators = cageOperators;
			return this;
		}

		public CageOperatorGenerator createCageOperatorGeneratorWhichReturns(CageOperator
																					 targetCageOperator) {
			GridGeneratingParameters gridGeneratingParametersMock;
			Random randomMock;

			gridGeneratingParametersMock = mock(GridGeneratingParameters.class);
			when(gridGeneratingParametersMock.getMaxCagePermutations()).thenReturn(99999);

			randomMock = mock(Random.class);
			when(randomMock.nextInt(anyInt())).thenReturn(getRandomWhichResultsInSelectingCageOperator(targetCageOperator));

			return new CageOperatorGenerator(gridGeneratingParametersMock, randomMock, cellValues);
		}

		private int getRandomWhichResultsInSelectingCageOperator(CageOperator... cageOperator) {
			int index = Arrays.asList(orderedCageOperatorsInGenerator).indexOf(cageOperator);
			int random = 0;
			for (int i = 0; i <= index; index++) {
				random += weightsForOrderedCageOperatorsInGenerator[i];
			}
			return random;
		}
	}
	

	@Test
	public void getCageOperator_1Value_Specified_NoneCageOperator() throws Exception {
		Setup setup = new Setup().setCellValues(ONE_CELL_VALUE).setPossibleCageOperators(CageOperator.NONE);
		cageOperatorGenerator = setup.createCageOperatorGeneratorWhichReturns(CageOperator.NONE);
		assertThat(cageOperatorGenerator.getCageOperator(), is(CageOperator.NONE));
	}

	@Test
	public void getCageOperator_2Values_Specified_NoneCageOperator() throws Exception {
		//cageOperatorGenerator = new Setup().createCageOperatorGeneratorWhichReturns(CageOperator.ADD);
		//assertThat(cageOperatorGenerator.getCageOperator(), is(CageOperator.ADD));
	}

	@Test
	public void getCageResult() throws Exception {
		Setup setup = new Setup().setCellValues(ONE_CELL_VALUE).setPossibleCageOperators(CageOperator.NONE);
		cageOperatorGenerator = setup.createCageOperatorGeneratorWhichReturns(CageOperator.NONE);
		assertThat(cageOperatorGenerator.getCageResult(), is(ONE_CELL_VALUE));
	}
}
