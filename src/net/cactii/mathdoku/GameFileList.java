package net.cactii.mathdoku;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
							}
						})
				.setPositiveButton(
						R.string.game_file_list__delete_game_file_positive_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Deletion has been confirmed.
								new File(filename).delete();
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
		Intent i = new Intent().putExtra("filename", filename);
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	/**
	 * Saves the current game to a new game file.
	 */
	public void saveCurrent() {
		this.mCurrentSaved = true;

		// Determine first file index number which is currently not in use.
		int fileIndex;
		for (fileIndex = 0;; fileIndex++) {
			if (!new GameFile(fileIndex).exists())
				break;
		}

		// Save the file at the first unused file index number. The current game
		// was already saved to the default game file when the game file list
		// was shown. To save the current game a copy of the default file has to
		// be made.
		try {
			new GameFile().copyTo(fileIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.mAdapter.refreshFiles();
		this.mAdapter.notifyDataSetChanged();
	}
}