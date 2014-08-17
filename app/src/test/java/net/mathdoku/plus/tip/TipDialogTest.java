package net.mathdoku.plus.tip;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowAlertDialog;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class TipDialogTest {
	private TipDialog primaryTipDialog;
	private String primaryTipName = "*** INITIAL TIP NAME ***";
	private String primaryTipTitle = "*** INITIAL TIP TITLE ***";
	private String primaryTipMessage = "*** INITIAL TIP MESSAGE ***";
	private TipDialog.TipPriority primaryTipPriority = TipDialog.TipPriority.MEDIUM;
	private Drawable primaryDrawable = null;
	private Activity activity;
	private Preferences preferences;

	private class OnCloseDialogException extends RuntimeException {
	}

	@Before
	public void setUp() throws Exception {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		preferences = Preferences.getInstance(activity);
	}

	@Test
	public void constructor_PreviousDialogWasNotDismissed_PreviousTipDialogIsDismissed()
			throws Exception {
		setTipDisplayAgain(primaryTipName, true);
		showPrimaryTipDialog();
		showSecondaryTipDialogWithPriority(TipDialog.TipPriority.HIGH);
		assertThat(shadowOf(primaryTipDialog).hasBeenDismissed(), is(true));
	}

	private void showSecondaryTipDialogWithPriority(
			TipDialog.TipPriority tipPriority) {
		String otherTipName = "Other Tip";
		setTipDisplayAgain(otherTipName, true);
		TipDialog nextTipDialog = new TipDialog(activity, otherTipName,
				tipPriority);
		assertThat(nextTipDialog.build(R.drawable.alert,
				"*** OTHER TIP TITLE ***", "*** OTHER TIP MESSAGE ***", null),
				is(true));
		nextTipDialog.show();
		assertThat(ShadowAlertDialog.getLatestAlertDialog(),
				is((AlertDialog) nextTipDialog));
	}

	private void setTipDisplayAgain(String otherTipName, boolean b) {
		when(preferences.getTipDisplayAgain(otherTipName)).thenReturn(b);
	}

	private ShadowAlertDialog showPrimaryTipDialog() {
		createPrimaryTipDialog();
		primaryTipDialog.show();

		assertThat(ShadowAlertDialog.getLatestAlertDialog(),
				is((AlertDialog) primaryTipDialog));

		return shadowOf(primaryTipDialog);
	}

	private Boolean createPrimaryTipDialog() {
		primaryTipDialog = new TipDialog(activity, primaryTipName,
				primaryTipPriority);
		return primaryTipDialog.build(R.drawable.alert, primaryTipTitle,
				primaryTipMessage, primaryDrawable);
	}

	@Test
	public void build_TipShouldNotBeDisplayedAgain_TipIsNotIDisplayed()
			throws Exception {
		setTipDisplayAgain(primaryTipName, false);
		assertThat(createPrimaryTipDialog(), is(false));
	}

	@Test
	public void build_TitleIsSet() throws Exception {
		setTipDisplayAgain(primaryTipName, true);
		assertThat(showPrimaryTipDialog().getTitle().toString(),
				is(primaryTipTitle));
	}

	@Test
	public void build_MessageIsSet() throws Exception {
		setTipDisplayAgain(primaryTipName, true);
		showPrimaryTipDialog();
		assertThat(((TextView) primaryTipDialog
				.findViewById(R.id.dialog_tip_text)).getText().toString(),
				is(primaryTipMessage));
	}

	@Test
	public void build_NotHavingImage_ImageViewNotVisible() throws Exception {
		setTipDisplayAgain(primaryTipName, true);
		showPrimaryTipDialog();
		assertThat(primaryTipDialog
				.findViewById(R.id.dialog_tip_image)
				.getVisibility(), is(View.GONE));
	}

	@Test
	public void build_HavingImage_ImageIsVisible() throws Exception {
		primaryDrawable = mock(Drawable.class);
		setTipDisplayAgain(primaryTipName, true);
		showPrimaryTipDialog();
		assertThat(primaryTipDialog
				.findViewById(R.id.dialog_tip_image)
				.getVisibility(), is(View.VISIBLE));
	}

	@Test
	public void resetDisplayedDialogs_DialogWasDisplayed_DialogIsRemoved()
			throws Exception {
		setTipDisplayAgain(primaryTipName, true);
		showPrimaryTipDialog();
		assertThat(shadowOf(primaryTipDialog).hasBeenDismissed(), is(false));
		TipDialog.resetDisplayedDialogs();
		assertThat(shadowOf(primaryTipDialog).hasBeenDismissed(), is(true));
	}

	@Test(expected = IllegalStateException.class)
	public void show_DotNotDisplayAgainIsEnabled_IllegalStateExceptionIsThrown()
			throws Exception {
		setTipDisplayAgain(primaryTipName, false);
		showPrimaryTipDialog();
	}

	@Test
	public void getDisplayTipAgain_DotNotDisplayAgainIsEnabled_TipWillNotBeDisplayedAgain()
			throws Exception {
		setTipDisplayAgain(primaryTipName, false);
		assertThat(primaryTipDialog.getDisplayTipAgain(preferences,
				primaryTipName, TipDialog.TipPriority.HIGH), is(false));
	}

	@Test
	public void getDisplayTipAgain_SecondaryTipDialogHasLowerPriority_PrimaryTipDialogIsNotDismissed()
			throws Exception {
		setTipDisplayAgain(primaryTipName, true);
		showPrimaryTipDialog();
		String secondaryTipName = "** secondary tip **";
		assertThat(primaryTipDialog.getDisplayTipAgain(preferences,
				secondaryTipName, TipDialog.TipPriority.LOW), is(false));
		assertThat(shadowOf(primaryTipDialog).hasBeenDismissed(), is(false));
	}

	@Test
	public void getDisplayTipAgain_SecondaryTipDialogHasSamePriorityAsFirstDialog_FirstTipDialogIsRandomlySelected()
			throws Exception {
		assertThatSecondaryTipDialogIsDisplayed(false);
	}

	private void assertThatSecondaryTipDialogIsDisplayed(boolean displaySecondaryTip) {
		setTipDisplayAgain(primaryTipName, true);
		showPrimaryTipDialog();
		String secondaryTipName = "** secondary tip **";
		setTipDisplayAgain(secondaryTipName, true);

		int maxTries = 10;
		boolean displayTip;
		do {
			// In case of equal priorities the getDisplayTipAgain will randomly
			// select the current version or the new tip.
			displayTip = primaryTipDialog.getDisplayTipAgain(preferences,
					secondaryTipName, primaryTipPriority);
			maxTries--;
		} while (displayTip != displaySecondaryTip && maxTries > 0);
		assertThat(
				"Failed too many times. As a random factor is involved please run test again before investigating it.",
				displayTip, is(displaySecondaryTip));
	}

	@Test
	public void getDisplayTipAgain_SecondaryTipDialogHasSamePriorityAsFirstDialog_SecondaryTipDialogIsRandomlySelected()
			throws Exception {
		assertThatSecondaryTipDialogIsDisplayed(true);
	}

	@Test
	public void getPreferenceStringDisplayTipAgain() throws Exception {
		String tipName = "***NAME***";
		assertThat(TipDialog.getPreferenceStringDisplayTipAgain(tipName),
				is("Tip.***NAME***.DisplayAgain"));
	}

	@Test
	public void getPreferenceStringLastDisplayTime() throws Exception {
		String tipName = "***NAME***";
		assertThat(TipDialog.getPreferenceStringLastDisplayTime(tipName),
				is("Tip.***NAME***.LastDisplayTime"));
	}

	@Test(expected = OnCloseDialogException.class)
	public void setOnClickCloseListener() throws Exception {
		setTipDisplayAgain(primaryTipName, true);
		createPrimaryTipDialog();
		primaryTipDialog
				.setOnClickCloseListener(new TipDialog.OnClickCloseListener() {
					@Override
					public void onTipDialogClose() {
						// Throw a special exception which is used to verify
						// whether the listener was called. Indirectly this
						// proves whether the listener was set programmatically.
						throw new OnCloseDialogException();
					}
				});
		primaryTipDialog.show();

		// Either close or cancel the dialog to invoke the onClickCLoseListener which will throw the exception set in the listener above.
		primaryTipDialog.cancel();
	}
}
