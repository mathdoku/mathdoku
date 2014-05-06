package net.mathdoku.plus.gridgenerating.cageresult;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NullCageResultTest {
	NullCageResult nullCageResult;

	@Before
	public void setup() {
		nullCageResult = NullCageResult.create();
	}

	@Test
	public void create() throws Exception {
		assertThat(nullCageResult, is(notNullValue()));
	}

	@Test
	public void isNull() throws Exception {
		assertThat(nullCageResult.isNull(), is(true));
	}

	@Test
	public void isValid() throws Exception {
		assertThat(nullCageResult.isValid(), is(false));
	}

	@Test
	public void getResult() throws Exception {
		assertThat(nullCageResult.getResult(), is(0));
	}
}
