package net.mathdoku.plus.gridgenerating.cageresult;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SingeCellCageResultTest {
	@Test(expected = IllegalStateException.class)
	public void tryToCreate_NullValue_ThrowsIllegalStateException()
			throws Exception {
		SingeCellCageResult.tryToCreate(null);
	}

	@Test(expected = IllegalStateException.class)
	public void tryToCreate_NoValues_ThrowsIllegalStateException()
			throws Exception {
		SingeCellCageResult.tryToCreate();
	}

	@Test
	public void tryToCreate_CorrectNumberOfValues_CageResultCreated()
			throws Exception {
		SingeCellCageResult singeCellCageResult = SingeCellCageResult
				.tryToCreate(3);
		assertThat(singeCellCageResult, is(notNullValue()));
	}

	@Test
	public void testGetResult_1Value_CorrectResult() throws Exception {
		SingeCellCageResult singeCellCageResult = SingeCellCageResult
				.tryToCreate(3);
		assertThat(singeCellCageResult.getResult(), is(3));
	}
}
