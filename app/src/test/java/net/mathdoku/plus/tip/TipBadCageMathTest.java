package net.mathdoku.plus.tip;

import org.hamcrest.Matcher;
import org.junit.Before;

import static org.junit.Assert.assertThat;

public class TipBadCageMathTest extends TipBaseTest {
	// All fields below are defaulted to a value which result in method
	// toBeDisplayed to return true.

	@Before
	public void setUp() {
		long minTimeIntervalBetweenTwoConsecutiveDisplays = 5 * 60 * 1000;
		super.setUp("BadCageMath", minTimeIntervalBetweenTwoConsecutiveDisplays);
	}

	@Override
	protected void initMocks() {
		super.initMocks();
	}

	@Override
	protected void assertThatDialogIsDisplayed(Matcher<Boolean> booleanMatcher) {
		assertThat(TipBadCageMath.toBeDisplayed(preferencesMock),
				   booleanMatcher);
	}
}
