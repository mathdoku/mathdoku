package net.mathdoku.plus.gridgenerating;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CandidateCageCreatorParametersTest {
	@Test
	public void setCorrectValueMatrix() throws Exception {
		Matrix<Integer> matrix = new Matrix<Integer>(2, -1);

		CandidateCageCreatorParameters candidateCageCreatorParameters = new CandidateCageCreatorParameters();

		assertThat(candidateCageCreatorParameters.getOverlappingSubsetChecker(),is(nullValue()));
		candidateCageCreatorParameters.setCorrectValueMatrix(matrix);
		assertThat(candidateCageCreatorParameters.getOverlappingSubsetChecker(),is(notNullValue()));
	}
}
