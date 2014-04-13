package net.mathdoku.plus.gridgenerating;

import org.junit.Test;

import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RandomIntegerMatrixGeneratorTest {
	Random random = mock(Random.class);
	private RandomIntegerMatrixGenerator randomIntegerMatrixGenerator;

	@Test
	public void getMatrix_NoRepetitionsNeeded_MatrixGenerated()
			throws Exception {
		when(random.nextInt(anyInt())).thenReturn(0, 1, 2, 2, 0, 1, 1, 2, 0);
		assertThatMatrixIsGenerated();
	}

	private void assertThatMatrixIsGenerated() {
		int size = 3;
		randomIntegerMatrixGenerator = new RandomIntegerMatrixGenerator(size,
				random);

		Matrix<Integer> expectedIntegerMatrix = createIntegerMatrixFromValues(new int[][] {
				// row 1
				{ 1, 3, 2 },
				// row 2
				{ 2, 1, 3 },
				// row 3
				{ 3, 2, 1 } });
		assertThat(randomIntegerMatrixGenerator.getMatrix(),
				is(expectedIntegerMatrix));
	}

	private Matrix<Integer> createIntegerMatrixFromValues(int[][] values) {
		Matrix<Integer> integerMatrix = new Matrix<Integer>(values.length,
				RandomIntegerMatrixGenerator.CORRECT_VALUE_NOT_SET);
		for (int row = 0; row < values.length; row++) {
			if (values[row].length != values.length) {
				throw new IllegalStateException("Matrix should be square.");
			}
			for (int col = 0; col < values.length; col++) {
				integerMatrix.setValueToRowColumn(values[row][col], row, col);
			}
		}
		return integerMatrix;
	}

	@Test
	public void getMatrix_WithMaximumRepetitionsAllowed_MatrixGenerated()
			throws Exception {
		when(random.nextInt(anyInt())).thenReturn(
		// Row 1
				0, 1, 2,
				// row 2, successful selecting col 0 and 1 ...
				2, 0,
				// ... but unsuccessful repetitions to select a value which was
				// not chosen before on this row ...
				2, 0, 0, 2, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 2, 2, 2, 2, 0,
				// before finally (in the last attempt) to select a value
				1,
				// row 3
				1, 2, 0);
		assertThatMatrixIsGenerated();
	}

	@Test(expected = GridGeneratingException.class)
	public void getMatrix_WithTooManyRepetitions_ThrowsInvalidGridException() throws Exception {
		when(random.nextInt(anyInt())).thenReturn(
		// Row 1
				0, 1, 2,
				// row 2, successful selecting col 0 and 1 ...
				2, 0,
				// ... but too many unsuccessful repetitions to select a value
				// which was not chosen before on this row ...
				2, 0, 0, 2, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 2
				);
		assertThatMatrixIsGenerated();
	}
}
