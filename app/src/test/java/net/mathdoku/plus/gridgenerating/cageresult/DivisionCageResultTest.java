package net.mathdoku.plus.gridgenerating.cageresult;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DivisionCageResultTest {
	@Test(expected = IllegalStateException.class)
	public void tryToCreate_NullValue_ThrowsIllegalStateException()
			throws Exception {
		DivisionCageResult.tryToCreate(null);
	}

	@Test(expected = IllegalStateException.class)
	public void tryToCreate_TooLittleValues_ThrowsIllegalStateException()
			throws Exception {
		DivisionCageResult.tryToCreate(1);
	}

	@Test(expected = IllegalStateException.class)
	public void tryToCreate_TooManyValues_ThrowsIllegalStateException()
			throws Exception {
		DivisionCageResult.tryToCreate(1, 2, 3);
	}

	@Test(expected = IllegalStateException.class)
	public void tryToCreate_ValuesCanNotBeDivided_ThrowsIllegalStateException()
			throws Exception {
		DivisionCageResult.tryToCreate(2, 9);
	}

	@Test
	public void tryToCreate_ValuesOrderedAscendingCanBeDivided_CageResultCreated()
			throws Exception {
		assertThat(DivisionCageResult.tryToCreate(3, 9), is(not(nullValue())));
	}

	@Test
	public void tryToCreate_ValuesOrderedDescendingCanBeDivided_CageResultCreated()
			throws Exception {
		assertThat(DivisionCageResult.tryToCreate(8, 2), is(not(nullValue())));
	}

	@Test
	public void testGetResult_ValuesOrderedAscendingCanBeDivided_CorrectResult()
			throws Exception {
		DivisionCageResult divisionCageResult = DivisionCageResult.tryToCreate(
				2, 8);
		assertThat(divisionCageResult.getResult(), is(8 / 2));
	}

	@Test
	public void testGetResult_ValuesOrderedDescendingCanBeDivided_CorrectResult()
			throws Exception {
		DivisionCageResult divisionCageResult = DivisionCageResult.tryToCreate(
				6, 3);
		assertThat(divisionCageResult.getResult(), is(6 / 3));
	}
}
