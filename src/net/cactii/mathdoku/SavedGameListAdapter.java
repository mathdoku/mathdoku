package net.cactii.mathdoku;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class SavedGameListAdapter extends BaseAdapter {
	
	public static final String SAVEDGAME_DIR = "/data/data/net.cactii.mathdoku/";
	public ArrayList<String> mGameFiles;
	private LayoutInflater inflater;
	private SavedGameList mContext;
	private Typeface mFace;

	public SavedGameListAdapter(SavedGameList context) {
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mGameFiles = new ArrayList<String>();
		this.mFace=Typeface.createFromAsset(context.getAssets(), "fonts/font.ttf");
		this.refreshFiles();

	}
	
	public class SortSavedGames implements Comparator<String> {
		long save1 = 0;
		long save2 = 0;
		public int compare(String object1, String object2) {
			try {
				save1 = new SaveGame(SAVEDGAME_DIR + "/" + object1).ReadDate();
				save2 = new SaveGame(SAVEDGAME_DIR + "/" + object2).ReadDate();
			}
			catch (Exception e) {
				//
			}
			return (int) (save2 - save1);
		}
		
	}
	
	public void refreshFiles() {
		this.mGameFiles.clear();
		File dir = new File(SAVEDGAME_DIR);
		String[] allFiles = dir.list();
		for (String entryName : allFiles)
			if (entryName.startsWith("savedgame_"))
				this.mGameFiles.add(entryName);
		
		Collections.sort((List<String>)this.mGameFiles, new SortSavedGames());
		
	}

	public int getCount() {
		return this.mGameFiles.size() + 1;
	}

	public Object getItem(int arg0) {
		if (arg0 == 0)
			return "";
		return this.mGameFiles.get(arg0-1);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {
			convertView = inflater.inflate(R.layout.savedgamesaveitem, null);
			if (PreferenceManager.getDefaultSharedPreferences(convertView.getContext()).getBoolean("alternatetheme", true)) {
				convertView.findViewById(R.id.wordRow).setBackgroundDrawable(null);
				convertView.findViewById(R.id.wordRow).setBackgroundColor(0xFFA0A0CC);
			} else {
				convertView.findViewById(R.id.wordRow).setBackgroundResource(R.drawable.background1);
			}
			
			final Button saveCurrent = (Button)convertView.findViewById(R.id.saveCurrent);
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
		
		
		GridView grid = (GridView)convertView.findViewById(R.id.savedGridView);
		if (PreferenceManager.getDefaultSharedPreferences(convertView.getContext()).getBoolean("alternatetheme", false)) {
			grid.setTheme(GridView.THEME_NEWSPAPER);
			convertView.findViewById(R.id.wordRow).setBackgroundDrawable(null);
			convertView.findViewById(R.id.wordRow).setBackgroundColor(0xFFA0A0CC);
		} else {
			grid.setTheme(GridView.THEME_CARVED);
			convertView.findViewById(R.id.wordRow).setBackgroundResource(R.drawable.background1);
		}
		TextView label = (TextView)convertView.findViewById(R.id.savedGridText);

		final String saveFile = SAVEDGAME_DIR + "/" + this.mGameFiles.get(position-1);
		
		grid.mContext = this.mContext;
		grid.mFace = this.mFace;
		grid.mActive = false;

		SaveGame saver = new SaveGame(saveFile);
		try {
			saver.Restore(grid);
		}
		catch (Exception e) {
			// Error, delete the file.
			new File(saveFile).delete();
			return convertView;
		}
		Calendar currentTime = Calendar.getInstance();
		Calendar gameTime = Calendar.getInstance();
		gameTime.setTimeInMillis(grid.mDate);
		if (System.currentTimeMillis() - grid.mDate < 86400000 &&
			gameTime.get(Calendar.DAY_OF_YEAR) != currentTime.get(Calendar.DAY_OF_YEAR))
			label.setText(gameTime.get(Calendar.HOUR) + ":" + gameTime.get(Calendar.MINUTE) + 
					((gameTime.get(Calendar.AM_PM) == Calendar.AM) ? " AM" : " PM") + " yesterday");
		else if (System.currentTimeMillis() - grid.mDate < 86400000)
			label.setText("" + DateFormat.getTimeInstance(DateFormat.SHORT).format(grid.mDate));
		else
			label.setText("" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(grid.mDate));

		grid.setBackgroundColor(0xFFFFFFFF);
		
		for (GridCell cell : grid.mCells)
			cell.mSelected = false;
		
		Button loadButton = (Button)convertView.findViewById(R.id.gameLoad);
		loadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.LoadGame(saveFile);
			}
		});
		
		Button deleteButton = (Button)convertView.findViewById(R.id.gameDelete);
		deleteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.DeleteGame(saveFile);
			}
		});
		
		return convertView;
	}

}
