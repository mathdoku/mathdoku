package net.mathdoku.plus.gridgenerating.cageresult;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AdditionCageResultTest {
	@Test(expected = IllegalStateException.class)
	public void tryToCreate_NullValue_ThrowsIllegalStateException() throws Exception {
		AdditionCageResult.tryToCreate(null);
	}

	@Test(expected = IllegalStateException.class)
	public void tryToCreate_TooLittleValues_ThrowsIllegalStateException() throws Exception {
		AdditionCageResult.tryToCreate(1);
	}

	@Test
	public void tryToCreate_CorrectNumberOfValues_CageResultCreated() throws Exception {
		AdditionCageResult additionCageResult = AdditionCageResult.tryToCreate(1,3);
		assertThat(additionCageResult, is(notNullValue()));
	}

	@Test
	public void testGetResult_2Values_CorrectResult() throws Exception {
		AdditionCageResult additionCageResult = AdditionCageResult.tryToCreate(1,3);
		assertThat(additionCageResult.getResult(), is(1+3));
	}

	@Test
	public void testGetResult_10Values_CorrectResult() throws Exception {
		AdditionCageResult additionCageResult = AdditionCageResult.tryToCreate(8,4,5,3,2,9,7,1,6);
		assertThat(additionCageResult.getResult(), is(1+2+3+4+5+6+7+8+9));
	}
}
