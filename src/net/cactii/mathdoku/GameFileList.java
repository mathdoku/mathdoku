package net.cactii.mathdoku;

import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class GameFileList extends ListActivity {

	public final static String INTENT_EXTRA_KEY_SOLVING_ATTEMPT_ID = "SolvingAttemptId";

	private GameFileListAdapter mAdapter;
	public boolean mCurrentSaved;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mAdapter = new GameFileListAdapter(this);
		setListAdapter(this.mAdapter);
	}

	@Override
	protected void onResume() {
		UsageLog.getInstance(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		UsageLog.getInstance().close();
		super.onPause();
	}

	/**
	 * Load the grid with the given id.
	 * 
	 * @param gridId
	 *            The id of the grid to be loaded.
	 */
	public void loadGameFile(int gridId) {
		UsageLog.getInstance().logFunction("LoadGame");
		Intent i = new Intent().putExtra(INTENT_EXTRA_KEY_SOLVING_ATTEMPT_ID,
				gridId);
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	/**
	 * Checks whether the load/save option, which will start the
	 * {@link GameFileList} activity, can be used.
	 * 
	 * @return True in case the {@link GameFileList} activity can be used. False
	 *         otherwise.
	 */
	static boolean canBeUsed() {
		// Check if a solving attempt exists.
		return (new SolvingAttemptDatabaseAdapter().getMostRecentPlayedId() > 0);
	}
}