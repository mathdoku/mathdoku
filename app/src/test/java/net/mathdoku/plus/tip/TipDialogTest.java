package net.mathdoku.plus.tip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class TipDialogTest {
	private TipDialog tipDialogStub;
	private String nameTipDialogStub = "*** INITIAL TIP NAME ***";
	private String titleTipDialogStub = "*** INITIAL TIP TITLE ***";
	private String messageTipDialogStub = "*** INITIAL TIP MESSAGE ***";
	private Drawable drawableTipDialogStub = null;
	private boolean isDismissedTipDialogStub = false;

	private Activity activity;
	private Preferences preferences;


	private class TipDialogStub extends TipDialog {
		TipDialogStub(Context context, String tip, TipPriority priority) {
			super(context, tip, priority);
		}

		@Override
		public void setOnDismissListener(final OnDismissListener listener) {
			super.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					isDismissedTipDialogStub = true;
					listener.onDismiss(dialog);
				}
			});
		}
	}

	@Before
	public void setUp() throws Exception {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		preferences = Preferences.getInstance(activity);
	}

	@Test
	public void constructor_PreviousDialogWasNotDismissed_PreviousTipDialogIsDismissed()
			throws Exception {
		showInitialTipDialog();

		TipDialog nextTipDialog = new TipDialog(activity, "Other Tip",
				TipDialog.TipPriority.HIGH);
		assertThat(nextTipDialog.build(R.drawable.alert,
				"*** OTHER TIP TITLE ***", "*** OTHER TIP MESSAGE ***", null),
				is(true));
		nextTipDialog.show();
		assertThat(ShadowAlertDialog.getLatestAlertDialog(),
				is((AlertDialog) nextTipDialog));
		assertThat(shadowOf(tipDialogStub).hasBeenDismissed(), is(true));
	}

	private ShadowAlertDialog showInitialTipDialog() {
		when(preferences.getTipDisplayAgain(anyString())).thenReturn(true);
		tipDialogStub = new TipDialogStub(activity, nameTipDialogStub,
				TipDialog.TipPriority.LOW);
		tipDialogStub.build(R.drawable.alert, titleTipDialogStub, messageTipDialogStub,
							drawableTipDialogStub);
		tipDialogStub.show();

		assertThat(ShadowAlertDialog.getLatestAlertDialog(),
				is((AlertDialog) tipDialogStub));

		return shadowOf(tipDialogStub);
	}

	@Test
	public void build_TipShouldNotBeDisplayedAgain_TipIsNotIDisplayed()
			throws Exception {
		when(preferences.getTipDisplayAgain(anyString())).thenReturn(false);
		assertThat(createTipDialogStub(), is(false));
	}

	private Boolean createTipDialogStub() {
		tipDialogStub = new TipDialogStub(activity, nameTipDialogStub,
				TipDialog.TipPriority.LOW);
		return tipDialogStub.build(R.drawable.alert, titleTipDialogStub, messageTipDialogStub,
								   drawableTipDialogStub);
	}

	@Test
	public void build_TitleIsSet() throws Exception {
		assertThat(showInitialTipDialog().getTitle().toString(),
				is(titleTipDialogStub));
	}

	@Test
	public void build_MessageIsSet() throws Exception {
		assertThat(((TextView) showInitialTipDialog().getCustomView()
				.findViewById(R.id.dialog_tip_text)).getText().toString(),
				is(messageTipDialogStub));
	}

	@Test
	public void build_NotHavingImage_ImageViewNotVisible() throws Exception {
		assertNotNull(showInitialTipDialog());
		assertNotNull(showInitialTipDialog().getCustomView());
		assertNotNull(showInitialTipDialog().getCustomView().findViewById(R.id.dialog_tip_image));
		assertNotNull(showInitialTipDialog().getCustomView().findViewById(R.id.dialog_tip_image).getVisibility());
		assertThat(showInitialTipDialog().getCustomView()
				.findViewById(R.id.dialog_tip_image)
				.getVisibility(), is(View.GONE));
	}

	@Test
	public void build_HavingImage_ImageIsVisible() throws Exception {
		drawableTipDialogStub = mock(Drawable.class);
		showInitialTipDialog();
		assertThat(showInitialTipDialog().getCustomView()
						   .findViewById(R.id.dialog_tip_image)
						   .getVisibility(), is(View.VISIBLE));
	}

	@Test
	public void resetDisplayedDialogs() throws Exception {
		showInitialTipDialog();

	}

	@Test
	public void show() throws Exception {

	}

	@Test
	public void displayTip() throws Exception {

	}

	@Test
	public void getDisplayTipAgain() throws Exception {

	}

	@Test
	public void getPreferenceStringDisplayTipAgain() throws Exception {

	}

	@Test
	public void getPreferenceStringLastDisplayTime() throws Exception {

	}

	@Test
	public void setOnClickCloseListener() throws Exception {

	}
}
