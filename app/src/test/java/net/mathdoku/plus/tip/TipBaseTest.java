package net.mathdoku.plus.tip;

import net.mathdoku.plus.Preferences;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TipBaseTest {
	protected Preferences preferencesMock = mock(Preferences.class);
	private boolean instantiatedBySubclass = false;
	private boolean initMockOnBaseInvoked = false;
	private long tipLastDisplayTime;
	private String preferenceName;

	protected void setUp(String preferenceName,
			long minTimeIntervalBetweenTwoConsecutiveDisplays) {
		instantiatedBySubclass = true;
		this.preferenceName = preferenceName;

		// By default the tip is checked to be displayed exactly after the
		// minimal time interval needed between two consecutive displays of the
		// same dialog is passed.
		tipLastDisplayTime = System.currentTimeMillis()
				- minTimeIntervalBetweenTwoConsecutiveDisplays;

		// Reset static singleton for consecutive unit tests
		TipCopyCellValues.resetDisplayedDialogs();
	}

	@Test
	public void toBeDisplayed_NoConditionsViolated_DialogIsDisplayed()
			throws Exception {
		if (instantiatedBySubclass) {
			initMocks();
			assertThatDialogIsDisplayed(is(true));
		}
	}

	/**
	 * Subclass should override and invoke this method in case additional mocks
	 * have to be instantiated.
	 */
	protected void initMocks() {
		// Value below is not flexible in this unit test as it deals with
		// behavior of the super class.
		when(preferencesMock.getTipDisplayAgain(preferenceName)).thenReturn(
				true, true);
		when(preferencesMock.getTipLastDisplayTime(preferenceName)).thenReturn(
				tipLastDisplayTime);
	}

	protected void assertThatDialogIsDisplayed(Matcher<Boolean> booleanMatcher) {
		// Unit test does not allow abstract class. Throw exception if method
		// not overridden.
		throw new IllegalStateException(
				"Subclass should override method assertThatDialogIsDisplayed.");
	}

	@Test
	public void toBeDisplayed_DisplayTooShortAgo_DialogIsNotDisplayed()
			throws Exception {
		if (instantiatedBySubclass) {
			// Increase last display time with 1 millisecond. As a results the
			// time interval between this check and the last time the dialog was
			// displayed is exactly one millisecond to short.
			tipLastDisplayTime++;
			initMocks();
			assertThatDialogIsDisplayed(is(false));
		}
	}
}
