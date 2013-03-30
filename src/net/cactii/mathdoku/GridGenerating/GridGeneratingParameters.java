package net.cactii.mathdoku.GridGenerating;

import net.cactii.mathdoku.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * All parameters which have influenced the grid generating process.
 * 
 */
public class GridGeneratingParameters {
	public int mGeneratorRevisionNumber;
	public long mGameSeed;
	public int mMaxCageSize;
	public int mMaxCageResult;
	public boolean mHideOperators;

	public void show(Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Grid generating parameters")
				.setMessage(
						"This grid was generated with revision "
								+ mGeneratorRevisionNumber
								+ " and following parameters:\n"
								+ " * MaxCageSize = " + mMaxCageSize + "\n"
								+ " * MaxCageResult = " + mMaxCageResult + "\n"
								+ " * HideOperators = " + mHideOperators + "\n"
								+ " * GameSeed = " + String.format("%,d", mGameSeed))
				.setCancelable(false)
				.setPositiveButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do nothing
							}
						});
		AlertDialog dialog = builder.create();
		dialog.show();

	}
}
