package net.mathdoku.plus.tip;

import android.app.Activity;

import net.mathdoku.plus.puzzle.cheat.Cheat;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class TipCheatTest extends TipBaseTest {
	public static final int OCCURRENCES_CONDITIONAL_PENALTY = 2;

	@Before
	public void setUp() {
		super.setUp();
	}

	private void assertThatDialogToBeDisplayed(Cheat cheat,
			Matcher<Boolean> booleanMatcher) {
		assertThat(TipCheat.toBeDisplayed(preferencesMock, cheat),
				booleanMatcher);
	}

	@Test
	public void toBeDisplayed_CellRevealed_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(new Cheat(new Activity(),
				Cheat.CheatType.CELL_REVEALED));
	}

	private void assertThatTipCheatIsNotDisplayed(Cheat cheat) {
		long minTimeIntervalBetweenTwoConsecutiveDisplays = 12 * 60 * 60 * 1000;
		super.setUpTimeIntervalBetweenTwoConsecutiveDisplays(
				cheat.getTipName(),
				minTimeIntervalBetweenTwoConsecutiveDisplays);
		setIsDisplayedNotLongEnoughAgo();
		initMocks();
		assertThatDialogToBeDisplayed(cheat, is(false));
	}

	@Test
	public void toBeDisplayed_CheckProgress_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(new Cheat(new Activity(),
				Cheat.CheatType.CHECK_PROGRESS_USED,
				OCCURRENCES_CONDITIONAL_PENALTY));
	}

	@Test
	public void toBeDisplayed_OperatorRevealed_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(new Cheat(new Activity(),
				Cheat.CheatType.OPERATOR_REVEALED));
	}

	@Test
	public void toBeDisplayed_SolutionRevealed_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(new Cheat(new Activity(),
				Cheat.CheatType.SOLUTION_REVEALED));
	}
}
