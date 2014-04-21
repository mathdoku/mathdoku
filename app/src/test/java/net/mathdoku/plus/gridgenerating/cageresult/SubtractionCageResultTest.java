package net.mathdoku.plus.gridgenerating.cageresult;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SubtractionCageResultTest {
	@Test(expected = IllegalStateException.class)
	public void tryToCreate_NullValue_ThrowsIllegalStateException() throws Exception {
		SubtractionCageResult.tryToCreate(null);
	}

	@Test(expected = IllegalStateException.class)
	public void tryToCreate_TooLittleValues_ThrowsIllegalStateException() throws Exception {
		SubtractionCageResult.tryToCreate(1);
	}

	@Test(expected = IllegalStateException.class)
	public void tryToCreate_TooManyValues_ThrowsIllegalStateException() throws Exception {
		SubtractionCageResult.tryToCreate(1, 2, 3);
	}

	@Test
	public void tryToCreate_CorrectNumberOfValues_CageResultCreated() throws Exception {
		assertThat(SubtractionCageResult.tryToCreate(3, 9), is(not(nullValue())));
	}

	@Test
	public void testGetResult_2ValuesOrderedAscending_CorrectResult() throws Exception {
		SubtractionCageResult subtractionCageResult = SubtractionCageResult.tryToCreate(2,8);
		assertThat(subtractionCageResult.getResult(), is(8-2));
	}

	@Test
	public void testGetResult_2ValuesOrderedDescending_CorrectResult() throws Exception {
		SubtractionCageResult subtractionCageResult = SubtractionCageResult.tryToCreate(6,3);
		assertThat(subtractionCageResult.getResult(), is(6-3));
	}
}
