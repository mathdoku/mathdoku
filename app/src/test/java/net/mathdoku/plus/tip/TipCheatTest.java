package net.mathdoku.plus.tip;

import android.app.Activity;

import net.mathdoku.plus.puzzle.cheat.CellRevealedCheat;
import net.mathdoku.plus.puzzle.cheat.Cheat;
import net.mathdoku.plus.puzzle.cheat.CheckProgressUsedCheat;
import net.mathdoku.plus.puzzle.cheat.OperatorRevealedCheat;
import net.mathdoku.plus.puzzle.cheat.SolutionRevealedCheat;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class TipCheatTest extends TipBaseTest {
	public static final int OCCURRENCES_CONDITIONAL_PENALTY = 2;
	private Activity activity;

	@Before
	public void setUp() {
			activity = Robolectric.buildActivity(Activity.class).create().get();

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
		assertThatTipCheatIsNotDisplayed(new CellRevealedCheat(activity));
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
		assertThatTipCheatIsNotDisplayed(new CheckProgressUsedCheat(new Activity(), OCCURRENCES_CONDITIONAL_PENALTY));
	}

	@Test
	public void toBeDisplayed_OperatorRevealed_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(new OperatorRevealedCheat(
				new Activity()));
	}

	@Test
	public void toBeDisplayed_SolutionRevealed_DialogIsNotDisplayed()
			throws Exception {
		assertThatTipCheatIsNotDisplayed(new SolutionRevealedCheat(
				new Activity()));
	}
}
