package net.cactii.mathdoku;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class GameFileListAdapter extends BaseAdapter {

	public ArrayList<String> mGameFiles;
	private LayoutInflater inflater;
	private GameFileList mContext;
	private Typeface mFace;

	public GameFileListAdapter(GameFileList context) {
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mGameFiles = new ArrayList<String>();
		this.mFace = Typeface.createFromAsset(context.getAssets(),
				"fonts/font.ttf");
		this.refreshFiles();

	}

	public class SortSavedGames implements Comparator<String> {
		long save1 = 0;
		long save2 = 0;

		public int compare(String object1, String object2) {
			try {
				save1 = new GameFile(object1).readDatetimeCreated();
				save2 = new GameFile(object2).readDatetimeCreated();
			} catch (Exception e) {
				//
			}
			return (int) ((save2 - save1) / 1000);
		}

	}

	public void refreshFiles() {
		this.mGameFiles.clear();
		File dir = new File(GameFile.PATH);
		String[] allFiles = dir.list();
		for (String entryName : allFiles) {
			if (entryName.startsWith(GameFile.PREFIX_FILENAME)) {
				this.mGameFiles.add(entryName);
			}
		}

		Collections.sort((List<String>) this.mGameFiles, new SortSavedGames());

	}

	public int getCount() {
		return this.mGameFiles.size() + 1;
	}

	public Object getItem(int arg0) {
		if (arg0 == 0)
			return "";
		return this.mGameFiles.get(arg0 - 1);
	}

	public long getItemId(int position) {
		return position;
	}

	public void setTheme(View convertView) {
		String theme = PreferenceManager.getDefaultSharedPreferences(
				convertView.getContext()).getString("theme", "newspaper");
		ListActivity activity = (ListActivity) this.mContext;
		if ("newspaper".equals(theme)) {
			convertView.findViewById(R.id.wordRow).setBackgroundResource(
					R.drawable.newspaper);
			activity.getListView().setBackgroundResource(R.drawable.newspaper);
		} else if ("inverted".equals(theme)) {
			convertView.findViewById(R.id.wordRow).setBackgroundResource(
					R.drawable.newspaper_dark);
			activity.getListView().setBackgroundResource(
					R.drawable.newspaper_dark);
		} else {
			convertView.findViewById(R.id.wordRow).setBackgroundResource(
					R.drawable.background1);
			activity.getListView().setBackgroundResource(R.drawable.background);
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
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

		convertView = inflater.inflate(R.layout.savedgameitem, null);
		setTheme(convertView);

		GridView grid = (GridView) convertView.findViewById(R.id.savedGridView);
		String theme = PreferenceManager.getDefaultSharedPreferences(
				convertView.getContext()).getString("theme", "newspaper");

		if ("newspaper".equals(theme)) {
			grid.setTheme(GridView.THEME_NEWSPAPER);
			convertView.findViewById(R.id.wordRow).setBackgroundResource(
					R.drawable.newspaper);
		} else if ("carved".equals(theme)) {
			grid.setTheme(GridView.THEME_CARVED);
			convertView.findViewById(R.id.wordRow).setBackgroundResource(
					R.drawable.background1);
		} else if ("inverted".equals(theme)) {
			grid.setTheme(GridView.THEME_INVERT);
			convertView.findViewById(R.id.wordRow).setBackgroundResource(
					R.drawable.newspaper_dark1);
		}
		TextView label = (TextView) convertView
				.findViewById(R.id.savedGridText);

		final String saveFile = this.mGameFiles.get(position - 1);

		grid.mContext = this.mContext;
		grid.mFace = this.mFace;
		grid.mActive = false;
		grid.mDupedigits = PreferenceManager.getDefaultSharedPreferences(
				convertView.getContext()).getBoolean("dupedigits", true);
		grid.mBadMaths = PreferenceManager.getDefaultSharedPreferences(
				convertView.getContext()).getBoolean("badmaths", true);

		GameFile saver = new GameFile(saveFile);
		try {
			saver.load(grid);
		} catch (Exception e) {
			// Error, delete the file.
			new File(saveFile).delete();
			return convertView;
		}
		Calendar currentTime = Calendar.getInstance();
		Calendar gameTime = Calendar.getInstance();
		gameTime.setTimeInMillis(grid.mDate);
		if (System.currentTimeMillis() - grid.mDate < 86400000
				&& gameTime.get(Calendar.DAY_OF_YEAR) != currentTime
						.get(Calendar.DAY_OF_YEAR))
			label.setText(gameTime.get(Calendar.HOUR)
					+ ":"
					+ gameTime.get(Calendar.MINUTE)
					+ ((gameTime.get(Calendar.AM_PM) == Calendar.AM) ? " AM"
							: " PM") + " yesterday");
		else if (System.currentTimeMillis() - grid.mDate < 86400000)
			label.setText(""
					+ DateFormat.getTimeInstance(DateFormat.SHORT).format(
							grid.mDate));
		else
			label.setText(""
					+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
							DateFormat.SHORT).format(grid.mDate));

		grid.setBackgroundColor(0xFFFFFFFF);

		if (grid.mCells != null) {
			for (GridCell cell : grid.mCells) {
				if (cell != null) {
					cell.mSelected = false;
				}
			}
		}

		Button loadButton = (Button) convertView.findViewById(R.id.gameLoad);
		loadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.loadGameFile(saveFile);
			}
		});

		Button deleteButton = (Button) convertView
				.findViewById(R.id.gameDelete);
		deleteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.deleteGameFile(saveFile);
			}
		});

		return convertView;
	}

}
