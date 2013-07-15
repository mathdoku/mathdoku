package net.cactii.mathdoku.util;

import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.ui.SharedPuzzleActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

public class SharedPuzzle {
	private Context mContext;
	
	public SharedPuzzle(Context context) {
		mContext = context;
	}
	
	/**
	 * Start an email client with a prepared email which can be used to share a
	 * game with another user.
	 */
	public void share(int solvingAttemptId) {
		Grid grid = new Grid();
		if (grid.load(solvingAttemptId)) {

			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:"));
			intent.putExtra(Intent.EXTRA_SUBJECT,
					mContext.getResources().getString(R.string.archive_share_subject));

			String ShareURL = SharedPuzzleActivity.getShareUrl(grid
					.toGridDefinitionString());
			String downloadUrl = "https://code.google.com/p/mathdoku/downloads/list"; // TODO:
																						// replace
																						// before
																						// going
																						// live
																						// to
																						// Play
																						// Store!!
			intent.putExtra(
					Intent.EXTRA_TEXT,
					Html.fromHtml(mContext.getResources().getString(
							R.string.archive_share_body,
							"<a href =\"" + ShareURL + "\">puzzle</a>",
							"<a href =\"" + downloadUrl
									+ "\">MathDoku Project Download Page</a>")));

			try {
				mContext.startActivity(Intent.createChooser(intent, mContext.getResources()
						.getString(R.string.usage_log_choose_action_title)));
			} catch (android.content.ActivityNotFoundException ex) {
				// No clients installed which can handle
				// this intent.
			}
		}
	}
}
