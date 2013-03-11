package net.cactii.mathdoku;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GameFileListAdapter extends BaseAdapter {
	// The list of game files available.
	public ArrayList<GameFile.GameFileHeader> mGameFiles;

	// Inflater and font for the previews
	private LayoutInflater inflater;
	private Typeface mFace;

	// Activity context in which the adapter is used.
	private GameFileList mContext;

	/**
	 * Creates a new instance of {@link GameFileListAdapter}.
	 * 
	 * @param context
	 *            The {@link GameFileList} activity in which context this
	 *            adapted is used.
	 */
	public GameFileListAdapter(GameFileList context) {
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mGameFiles = new ArrayList<GameFile.GameFileHeader>();
		this.refreshFiles();

	}

	/**
	 * Comparator to sort game file headers based on the date time of creation.
	 */
	public class SortGameFileOnDatetimeCreated implements
			Comparator<GameFile.GameFileHeader> {
		public int compare(GameFile.GameFileHeader gameFileHeader1,
				GameFile.GameFileHeader gameFileHeader2) {

			// Convert to integer by ignoring the miliseconds part of the
			// date/time.
			return (int) ((gameFileHeader2.datetimeCreated - gameFileHeader1.datetimeCreated) / 1000);
		}
	}

	/**
	 * Get all current game files.
	 */
	public void refreshFiles() {
		this.mGameFiles.clear();
		for (String gameFilename : GameFile.getAllGameFilesCreatedByUser(Integer.MAX_VALUE)) {
			// Load headers of this game file as they contain all
			// information needed (except the preview image itself) to
			// display and sort the list.
			GameFile.GameFileHeader gameFileHeader = new GameFile(gameFilename)
					.loadHeadersOnly(this.mContext);
			if (gameFileHeader != null) {
				this.mGameFiles.add(gameFileHeader);
			}
		}

		// Sort the file on date/time created.
		Collections.sort((List<GameFile.GameFileHeader>) this.mGameFiles,
				new SortGameFileOnDatetimeCreated());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return this.mGameFiles.size() + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int arg0) {
		if (arg0 == 0) {
			return "";
		}
		return this.mGameFiles.get(arg0 - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Apply theme settings to the given view.
	 * 
	 * @param convertView
	 *            The view to which the theme settings have to be applied.
	 */
	public void setTheme(View convertView) {
		String theme = PreferenceManager.getDefaultSharedPreferences(
				convertView.getContext()).getString("theme", "newspaper");
		ListActivity activity = (ListActivity) this.mContext;
		if ("newspaper".equals(theme)) {
			convertView.findViewById(R.id.gameFileListRow)
					.setBackgroundResource(R.drawable.newspaper1);
			activity.getListView().setBackgroundResource(R.drawable.newspaper);
		} else if ("inverted".equals(theme)) {
			convertView.findViewById(R.id.gameFileListRow)
					.setBackgroundResource(R.drawable.newspaper_dark1);
			activity.getListView().setBackgroundResource(
					R.drawable.newspaper_dark);
		} else {
			convertView.findViewById(R.id.gameFileListRow)
					.setBackgroundResource(R.drawable.background1);
			activity.getListView().setBackgroundResource(R.drawable.background);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// Check if current position should hold the button to save the current
		// game.
		if (position == 0) {
			convertView = inflater.inflate(R.layout.savedgamesaveitem, null);
			setTheme(convertView);
			final Button saveCurrent = (Button) convertView
					.findViewById(R.id.saveCurrent);
			saveCurrent.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					saveCurrent.setEnabled(false);
					mContext.saveCurrent();
				}
			});
			if (mContext.mCurrentSaved)
				saveCurrent.setEnabled(false);
			return convertView;
		}

		// All other positions in this list will hold a saved game. For smooth
		// scrolling in this list the preview images of saved games are used.
		convertView = inflater.inflate(R.layout.savedgameitem, null);
		setTheme(convertView);

		String theme = PreferenceManager.getDefaultSharedPreferences(
				convertView.getContext()).getString("theme", "newspaper");

		if ("newspaper".equals(theme)) {
			convertView.findViewById(R.id.gameFileListRow)
					.setBackgroundResource(R.drawable.newspaper1);
		} else if ("carved".equals(theme)) {
			convertView.findViewById(R.id.gameFileListRow)
					.setBackgroundResource(R.drawable.background1);
		} else if ("inverted".equals(theme)) {
			convertView.findViewById(R.id.gameFileListRow)
					.setBackgroundResource(R.drawable.newspaper_dark1);
		}

		// Get game file header information for current position.
		final GameFile.GameFileHeader gameFile = this.mGameFiles
				.get(position - 1);

		// Show preview image or textview when the preview is missing.
		ImageView imageView = (ImageView) convertView
				.findViewById(R.id.previewGameFile);
		if (gameFile.hasPreviewAvailable) {
			// Load the preview for displaying.
			GameFile saver = new GameFile(gameFile.filename);
			Bitmap preview = saver.getPreviewImage();
			imageView.setImageBitmap(preview);
		} else {
			// Preview image is not yet available. Create a back ground process
			// to create it.
			// new GameFilePreviewCreator(this.mContext,
			// gameFile.filenameShort).execute();

			// Replace image preview with text view containing a message that
			// the preview is missing.
			imageView.setVisibility(View.GONE);
			((TextView) convertView
					.findViewById(R.id.previewGameFileNotAvailable))
					.setVisibility(View.VISIBLE);

		}

		if (GameFile.DEBUG_SAVE_RESTORE) {
			TextView filenameTextView = ((TextView) convertView
					.findViewById(R.id.filename));
			filenameTextView.setText(gameFile.filename);
			filenameTextView.setVisibility(View.VISIBLE);
		}

		// Calculate the time elapsed since creating the game file.
		TextView savedOnTextView = (TextView) convertView
				.findViewById(R.id.savedOnText);
		Calendar currentTime = Calendar.getInstance();
		Calendar gameFileTime = Calendar.getInstance();
		Calendar gameFileDate = Calendar.getInstance();
		gameFileTime.setTimeInMillis(gameFile.datetimeCreated);
		gameFileDate.set(gameFileTime.get(Calendar.YEAR),
				gameFileTime.get(Calendar.MONTH),
				gameFileTime.get(Calendar.DAY_OF_MONTH));
		if (gameFileDate.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR)
				&& gameFileDate.get(Calendar.DAY_OF_YEAR) == currentTime
						.get(Calendar.DAY_OF_YEAR)) {
			// Game file was saved today. Only display time.
			savedOnTextView.setText(""
					+ DateFormat.getTimeInstance(DateFormat.SHORT).format(
							gameFile.datetimeCreated));
		} else if (gameFileDate.get(Calendar.YEAR) == currentTime
				.get(Calendar.YEAR)
				&& gameFileDate.get(Calendar.DAY_OF_YEAR) == currentTime
						.get(Calendar.DAY_OF_YEAR) - 1) {
			// Game file was saved yesterday.
			savedOnTextView
					.setText(gameFileTime.get(Calendar.HOUR)
							+ ":"
							+ gameFileTime.get(Calendar.MINUTE)
							+ ((gameFileTime.get(Calendar.AM_PM) == Calendar.AM) ? " AM"
									: " PM") + " yesterday");
		} else {
			savedOnTextView.setText(""
					+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
							DateFormat.SHORT).format(gameFile.datetimeCreated));
		}

		// Set callback for loading this game.
		Button loadButton = (Button) convertView.findViewById(R.id.gameLoad);
		loadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.loadGameFile(gameFile.filename);
			}
		});

		// Set callback for deleting this game.
		Button deleteButton = (Button) convertView
				.findViewById(R.id.gameDelete);
		deleteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.deleteGameFile(gameFile.filename);
			}
		});

		return convertView;
	}
}