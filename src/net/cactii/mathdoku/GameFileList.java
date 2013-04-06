package net.cactii.mathdoku;

import net.cactii.mathdoku.storage.GameFile;
import net.cactii.mathdoku.storage.GameFile.GameFileType;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class GameFileList extends ListActivity {
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
		UsageLog.getInstance();
		super.onResume();
	}

	@Override
	protected void onPause() {
		UsageLog.getInstance().close();
		super.onPause();
	}

	/**
	 * Display a dialog to confirm deletion of the game file with the given
	 * name.
	 * 
	 * @param filename
	 *            Name of file to be deleted.
	 */
	public void deleteGameFile(final String filename) {
		new AlertDialog.Builder(GameFileList.this)
				.setTitle(
						R.string.game_file_list__delete_game_file_confirmation_title)
				.setMessage(
						R.string.game_file_list__delete_game_file_confirmation_message)
				.setNegativeButton(
						R.string.game_file_list__delete_game_file_negative_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Do nothing.
								UsageLog.getInstance().logFunction("DeleteGame.Cancelled");
							}
						})
				.setPositiveButton(
						R.string.game_file_list__delete_game_file_positive_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								UsageLog.getInstance().logFunction("DeleteGame.Confirmed");
								// Deletion has been confirmed.
								new GameFile(filename).delete();
								GameFileList.this.mAdapter.refreshFiles();
								GameFileList.this.mAdapter
										.notifyDataSetChanged();
							}
						}).setIcon(android.R.drawable.ic_dialog_alert).show();
	}

	/**
	 * Load the game file with the give file name.
	 * 
	 * @param filename
	 *            Name of the file to be loaded.
	 */
	public void loadGameFile(String filename) {
		UsageLog.getInstance().logFunction("LoadGame");
		Intent i = new Intent().putExtra("filename", filename);
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	/**
	 * Saves the current game to a new game file.
	 */
	public void saveCurrent() {
		UsageLog.getInstance().logFunction("SaveGame");
		
		// The current game was already saved as the default game file when the
		// game file list was shown. To save the current game a copy of the
		// default file has to be made.
		new GameFile(GameFileType.LAST_GAME).copyToNewGameFile();
		this.mCurrentSaved = true;

		this.mAdapter.refreshFiles();
		this.mAdapter.notifyDataSetChanged();
	}

	/**
	 * Checks whether the load/save option, which will start the
	 * {@link GameFileList} activity, can be used.
	 * 
	 * @return True in case the {@link GameFileList} activity can be used. False
	 *         otherwise.
	 */
	static boolean canBeUsed() {
		// Check if a file exists that can be saved or loaded using the game
		// file list.
		return (GameFile.getAllGameFiles(1).size() > 0);
	}
}