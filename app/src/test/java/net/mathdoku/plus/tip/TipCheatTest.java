package net.mathdoku.plus.tip;

import net.mathdoku.plus.Cheat;

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
public class TipCheatTest extends TipBaseTest {
	// All fields below are defaulted to a value which result in method
	// toBeDisplayed to return true.
	private Cheat cheat = mock(Cheat.class);
	private Cheat.CheatType cheatType;

	@Before
	public void setUp() {
		super.setUp();
	}

	@Override
	protected void initMocks() {
		super.initMocks();
		if (cheat != null) {
			when(cheat.getType()).thenReturn(cheatType, cheatType, cheatType);
		}
	}

	@Override
	protected void assertThatDialogToBeDisplayed(Matcher<Boolean> booleanMatcher) {
		assertThat(TipCheat.toBeDisplayed(preferencesMock, cheat),
				booleanMatcher);
	}

	@Test
	public void toBeDisplayed_CellRevealed_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(Cheat.CheatType.CELL_REVEALED,
				"Cheat.CellRevealed");
	}

	private void assertThatTipCheatIsNotDisplayed(Cheat.CheatType cheatType,
			String preferenceName) {
		long minTimeIntervalBetweenTwoConsecutiveDisplays = 12 * 60 * 60 * 1000;
		super.setUpTimeIntervalBetweenTwoConsecutiveDisplays(preferenceName,
				minTimeIntervalBetweenTwoConsecutiveDisplays);
		setIsDisplayedNotLongEnoughAgo();
		this.cheatType = cheatType;
		initMocks();
		assertThatDialogToBeDisplayed(is(false));
	}

	@Test
	public void toBeDisplayed_CheckProgress_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(Cheat.CheatType.CHECK_PROGRESS_USED,
				"Cheat.CheckProgress");
	}

	@Test
	public void toBeDisplayed_OperatorRevealed_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(Cheat.CheatType.OPERATOR_REVEALED,
				"Cheat.OperatorRevealed");
	}

	@Test
	public void toBeDisplayed_SolutionRevealed_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(Cheat.CheatType.SOLUTION_REVEALED,
				"Cheat.SolutionRevealed");
	}
}
