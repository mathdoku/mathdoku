package net.mathdoku.plus.tip;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class TipOrderOfValuesInCageTest extends TipBaseTest {
	// All fields below are defaulted to a value which result in method
	// toBeDisplayed to return true.
	private Cage cageMock = mock(Cage.class);
	private CageOperator cageOperator = CageOperator.DIVIDE;
	private boolean cageOperatorHidden = true;

	@Before
	public void setUp() {
		super.setUp();
		long minTimeIntervalBetweenTwoConsecutiveDisplays = 5 * 60 * 1000;
		super.setUpTimeIntervalBetweenTwoConsecutiveDisplays(
				"OrderOfValuesInCage",
				minTimeIntervalBetweenTwoConsecutiveDisplays);
	}

	@Override
	protected void initMocks() {
		super.initMocks();
		if (cageMock != null) {
			when(cageMock.getOperator()).thenReturn(cageOperator);
			when(cageMock.isOperatorHidden()).thenReturn(cageOperatorHidden);
		}
	}

	@Override
	protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
		assertThat(
				TipOrderOfValuesInCage.toBeDisplayed(preferencesMock, cageMock),
				booleanMatcher);
	}

	@Test
	public void toBeDisplayed_CageMockIsNull_DialogIsNotDisplayed()
			throws Exception {
		cageMock = null;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_CageOperatorNone_DialogIsNotDisplayed()
			throws Exception {
		cageOperator = CageOperator.NONE;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_CageOperatorAddIsVisible_DialogIsNotDisplayed()
			throws Exception {
		cageOperator = CageOperator.ADD;
		cageOperatorHidden = false;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_CageOperatorMultipleIsVisible_DialogIsNotDisplayed()
			throws Exception {
		cageOperator = CageOperator.MULTIPLY;
		cageOperatorHidden = false;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}
}
