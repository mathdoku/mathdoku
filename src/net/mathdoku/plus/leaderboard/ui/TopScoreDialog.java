package net.mathdoku.plus.leaderboard.ui;

import net.mathdoku.plus.R;
import net.mathdoku.plus.ui.base.AppFragmentActivity;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class display a dialog containing a new topscore. This dialog has no
 * title or buttons and therefore looks like a toast. But unlike a toast it has
 * to dismissed by the user by tapping on or outside the dialog.
 */
public class TopScoreDialog extends AlertDialog {

	private final int mLeaderboardIconResID;
	private final String mDisplayScore;
	private final String mDisplayRank;
	private final int mOrientation;

	public TopScoreDialog(AppFragmentActivity appFragmentActivity,
			int leaderboardIconResID, String displayScore, String displayRank) {
		super(appFragmentActivity);
		mLeaderboardIconResID = leaderboardIconResID;
		mDisplayScore = displayScore;
		mDisplayRank = appFragmentActivity.getString(
				R.string.dialog_top_score_rank, displayRank);
		mOrientation = appFragmentActivity.getResources().getConfiguration().orientation;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the layout to the custom top score layout
		setContentView(R.layout.top_score_dialog);
		((ImageView) findViewById(R.id.dialog_top_score_leaderboard_icon))
				.setImageResource(mLeaderboardIconResID);
		((TextView) findViewById(R.id.dialog_top_score_score))
				.setText(mDisplayScore);
		((TextView) findViewById(R.id.dialog_top_score_rank))
				.setText(mDisplayRank);

		// Set touch listener so touches on the dialog will also dismiss the
		// dialog.
		findViewById(R.id.dialog_top_score_layout).setOnTouchListener(
				new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						dismiss();
						return true;
					}
				});

		// Align the dialog dependent on the orientation of the device
		Window window = getWindow();
		if (window != null) {
			WindowManager.LayoutParams layoutParams = window.getAttributes();
			if (layoutParams != null) {
				if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {

					layoutParams.width = LayoutParams.MATCH_PARENT;
				} else {
					layoutParams.gravity = Gravity.BOTTOM;
				}
				window.setAttributes(layoutParams);
			}
		}
	}

	@Override
	public void show() {
		super.show();

		// Build and start the animation on the leaderboard icon
		((ImageView) findViewById(R.id.dialog_top_score_leaderboard_icon))
				.startAnimation(AnimationUtils.loadAnimation(getContext(),
						R.anim.top_score));
	}
}
