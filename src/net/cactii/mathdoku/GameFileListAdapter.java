package net.cactii.mathdoku;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.cactii.mathdoku.storage.GameFile;
import net.cactii.mathdoku.storage.GameFileHeader;
import net.cactii.mathdoku.storage.PreviewImage;
import android.app.ListActivity;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameFileListAdapter extends BaseAdapter {
	// The list of game files available.
	public ArrayList<GameFileHeader> mGameFiles;

	// Inflater and font for the previews
	private LayoutInflater mLayoutInflater;

	// Activity context in which the adapter is used.
	private GameFileList mContext;

	// Background for listview items
	private int mResIdBackGroundListViewItem;

	// Column width and margins for the list rows.
	private int mImageHeightWidth;
	private int mColumnWidth;
	private int mColumnMargin;

	/**
	 * Creates a new instance of {@link GameFileListAdapter}.
	 * 
	 * @param context
	 *            The {@link GameFileList} activity in which context this
	 *            adapted is used.
	 */
	public GameFileListAdapter(GameFileList context) {
		this.mLayoutInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mGameFiles = new ArrayList<GameFileHeader>();

		// Apply theme to the list view
		switch (Preferences.getInstance().getTheme()) {
		case NEWSPAPER:
			mResIdBackGroundListViewItem = R.drawable.newspaper1;
			((ListActivity) mContext).getListView().setBackgroundResource(
					R.drawable.newspaper);
			break;
		case DARK:
			mResIdBackGroundListViewItem = R.drawable.newspaper_dark1;
			((ListActivity) mContext).getListView().setBackgroundResource(
					R.drawable.newspaper_dark);
			break;
		case CARVED:
			mResIdBackGroundListViewItem = R.drawable.background1;
			((ListActivity) mContext).getListView().setBackgroundResource(
					R.drawable.background);
			break;
		}

		// Get the display metrics
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);

		// Calculate size of margins and columns. Both columns are 50% of total
		// width. Margins are calculated to center the columns horizontally
		int previewImageSize = PreviewImage.getPreviewImageSize();
		mImageHeightWidth = previewImageSize;
		mColumnWidth = (int) ((float) 0.5 * displayMetrics.widthPixels);
		mColumnMargin = (int) Math.max(0,
				(mColumnWidth - (float) previewImageSize));

		// Read and sort the files
		refreshFiles();
	}

	/**
	 * Comparator to sort game file headers based on the date time of creation.
	 */
	public class SortGameFileOnDatetimeSaved implements
			Comparator<GameFileHeader> {
		public int compare(GameFileHeader gameFileHeader1,
				GameFileHeader gameFileHeader2) {

			// Convert to integer by ignoring the miliseconds part of the
			// date/time.
			return (int) ((gameFileHeader2.mDatetimeSaved - gameFileHeader1.mDatetimeSaved) / 1000);
		}
	}

	/**
	 * Get all current game files.
	 */
	public void refreshFiles() {
		this.mGameFiles.clear();
		for (String gameFilename : GameFile
				.getAllGameFilesCreatedByUser(Integer.MAX_VALUE)) {
			// Load headers of this game file as they contain all
			// information needed (except the preview image itself) to
			// display and sort the list.
			GameFileHeader gameFileHeader = new GameFileHeader();
			if (gameFileHeader.load(gameFilename)) {
				this.mGameFiles.add(gameFileHeader);
			}
		}

		// Sort the file on date/time created.
		Collections.sort((List<GameFileHeader>) this.mGameFiles,
				new SortGameFileOnDatetimeSaved());
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

	// Following variables are declared outside method ViewHolder to reduce
	// memory usage and increase speed. These variables are intended to be used
	// inside this method only.
	Calendar getView_CurrentTime = null;
	Calendar getView_GameFileTime;
	Calendar getView_GameFileDate;
	ViewHolder getView_ViewHolder;
	RelativeLayout.LayoutParams getView_LayoutParams;

	// Class for holding references to all fields in the convertView of method
	// getView as described in the ViewHolder pattern on page
	// http://developer.android.com/training/improving-layouts/smooth-scrolling.html.
	private static class ViewHolder {
		boolean isSavedGameViewHolder;

		// Fields used for view holders which are used to display saved games.
		RelativeLayout previewRelativeLayout;
		RelativeLayout detailsRelativeLayout;
		ImageView imageView;
		RelativeLayout imagePreviewNotAvailable;
		TextView filenameTextView;
		TextView savedOnTextView;
		Button loadButton;
		Button deleteButton;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// The first row in the list contains the button for saving the current
		// game. It has a different layout compared to the other rows.
		if (position == 0) {
			// Create a new convertView only in case it can not be recycled.
			// Note: for this row no references are stored in the viewHolder
			// object as they are not needed to refresh data.
			if (convertView == null
					|| ((ViewHolder) convertView.getTag()).isSavedGameViewHolder) {
				// Define and initialise the viewHolder
				getView_ViewHolder = new ViewHolder();
				getView_ViewHolder.isSavedGameViewHolder = false;

				// Inflate the convertView and set the background.
				convertView = mLayoutInflater
						.inflate(R.layout.savedgamesaveitem, null);
				convertView.findViewById(R.id.gameFileListRow)
						.setBackgroundResource(mResIdBackGroundListViewItem);

				// Set button and listener.
				final Button saveCurrent = (Button) convertView
						.findViewById(R.id.saveCurrent);
				saveCurrent.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						saveCurrent.setEnabled(false);
						mContext.saveCurrent();
					}
				});
				if (mContext.mCurrentSaved) {
					saveCurrent.setEnabled(false);
				}

				// Store the view holder as a tag in the convert view so it can
				// be distinguished from the other rows.
				convertView.setTag(getView_ViewHolder);
			}
			return convertView;
		}

		// All other rows of this list view contain saved games. Create a new
		// convertView only in case it can not be recycled.
		if (convertView == null
				|| !((ViewHolder) convertView.getTag()).isSavedGameViewHolder) {
			// Define and initialise the viewHolder
			getView_ViewHolder = new ViewHolder();
			getView_ViewHolder.isSavedGameViewHolder = true;

			// Inflate the convertView and set the background.
			convertView = mLayoutInflater.inflate(R.layout.savedgameitem, null);
			convertView.findViewById(R.id.gameFileListRow)
					.setBackgroundResource(mResIdBackGroundListViewItem);

			// Set layout for column containing the previews
			getView_ViewHolder.previewRelativeLayout = (RelativeLayout) convertView
					.findViewById(R.id.detailsLayout);
			getView_LayoutParams = (RelativeLayout.LayoutParams) getView_ViewHolder.previewRelativeLayout
					.getLayoutParams();
			getView_LayoutParams.width = mColumnWidth;
			getView_ViewHolder.previewRelativeLayout
					.setLayoutParams(getView_LayoutParams);

			// Set height and width of the preview image
			getView_ViewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.previewGameFile);
			getView_LayoutParams = (RelativeLayout.LayoutParams) getView_ViewHolder.imageView
					.getLayoutParams();
			getView_LayoutParams.height = mImageHeightWidth;
			getView_LayoutParams.width = mImageHeightWidth;
			getView_LayoutParams.leftMargin = mColumnMargin;
			getView_ViewHolder.imageView.setLayoutParams(getView_LayoutParams);

			// Set height and width of place holder which is used in case the
			// preview image is missing
			getView_ViewHolder.imagePreviewNotAvailable = ((RelativeLayout) convertView
					.findViewById(R.id.previewGameFileNotAvailable));
			getView_LayoutParams = (RelativeLayout.LayoutParams) getView_ViewHolder.imagePreviewNotAvailable
					.getLayoutParams();
			getView_LayoutParams.height = mImageHeightWidth;
			getView_LayoutParams.width = mImageHeightWidth;
			getView_LayoutParams.leftMargin = mColumnMargin;
			getView_ViewHolder.imagePreviewNotAvailable
					.setLayoutParams(getView_LayoutParams);

			// Set width and margin for layout for column containing the details
			// and buttons
			getView_ViewHolder.detailsRelativeLayout = (RelativeLayout) convertView
					.findViewById(R.id.detailsLayout);
			getView_LayoutParams = (RelativeLayout.LayoutParams) getView_ViewHolder.detailsRelativeLayout
					.getLayoutParams();
			getView_LayoutParams.width = mColumnWidth;
			getView_LayoutParams.rightMargin = mColumnMargin;
			getView_ViewHolder.detailsRelativeLayout
					.setLayoutParams(getView_LayoutParams);

			// Set reference to field filename
			if (GameFile.DEBUG_SAVE_RESTORE) {
				getView_ViewHolder.filenameTextView = ((TextView) convertView
						.findViewById(R.id.filename));
				getView_ViewHolder.filenameTextView.setVisibility(View.VISIBLE);
			}

			// Set reference to field save date time
			getView_ViewHolder.savedOnTextView = (TextView) convertView
					.findViewById(R.id.savedOnText);

			// Set reference to buttons.
			getView_ViewHolder.loadButton = (Button) convertView
					.findViewById(R.id.gameLoad);
			getView_ViewHolder.deleteButton = (Button) convertView
					.findViewById(R.id.gameDelete);

			// Store the view holder as a tag in the convert view.
			convertView.setTag(getView_ViewHolder);
		} else {
			// In case a convertView is recycled, the references in the
			// viewHolder object can be reused.
			getView_ViewHolder = (ViewHolder) convertView.getTag();
		}

		// Get game file header information for current position.
		final GameFileHeader gameFile = this.mGameFiles
				.get(position - 1);

		// Display the preview image if available. Else display the placeholder.
		if (gameFile.mHasPreviewAvailable) {
			getView_ViewHolder.imageView.setVisibility(View.VISIBLE);
			getView_ViewHolder.imagePreviewNotAvailable
					.setVisibility(View.GONE);
			Bitmap preview = new PreviewImage(gameFile.mFilenamePreview).load();
			getView_ViewHolder.imageView.setImageBitmap(preview);
		} else {
			getView_ViewHolder.imageView.setVisibility(View.GONE);
			getView_ViewHolder.imagePreviewNotAvailable
					.setVisibility(View.VISIBLE);
		}

		// Set filename
		if (GameFile.DEBUG_SAVE_RESTORE) {
			getView_ViewHolder.filenameTextView.setText(gameFile.mFilename);
		}

		// Calculate the time elapsed since creating the game file.
		if (getView_CurrentTime == null) {
			getView_CurrentTime = Calendar.getInstance();
		}
		getView_GameFileTime = Calendar.getInstance();
		getView_GameFileDate = Calendar.getInstance();

		getView_GameFileTime.setTimeInMillis(gameFile.mDatetimeSaved);
		getView_GameFileDate.set(getView_GameFileTime.get(Calendar.YEAR),
				getView_GameFileTime.get(Calendar.MONTH),
				getView_GameFileTime.get(Calendar.DAY_OF_MONTH));
		if (getView_GameFileDate.get(Calendar.YEAR) == getView_CurrentTime
				.get(Calendar.YEAR)
				&& getView_GameFileDate.get(Calendar.DAY_OF_YEAR) == getView_CurrentTime
						.get(Calendar.DAY_OF_YEAR)) {
			// Game file was saved today. Only display time.
			getView_ViewHolder.savedOnTextView.setText(""
					+ DateFormat.getTimeInstance(DateFormat.SHORT).format(
							gameFile.mDatetimeSaved));
		} else if (getView_GameFileDate.get(Calendar.YEAR) == getView_CurrentTime
				.get(Calendar.YEAR)
				&& getView_GameFileDate.get(Calendar.DAY_OF_YEAR) == getView_CurrentTime
						.get(Calendar.DAY_OF_YEAR) - 1) {
			// Game file was saved yesterday.
			getView_ViewHolder.savedOnTextView
					.setText(getView_GameFileTime.get(Calendar.HOUR)
							+ ":"
							+ getView_GameFileTime.get(Calendar.MINUTE)
							+ ((getView_GameFileTime.get(Calendar.AM_PM) == Calendar.AM) ? " AM"
									: " PM") + " yesterday");
		} else {
			getView_ViewHolder.savedOnTextView.setText(""
					+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
							DateFormat.SHORT).format(gameFile.mDatetimeSaved));
		}

		// Set callback for loading this game.
		getView_ViewHolder.loadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.loadGameFile(gameFile.mFilename);
			}
		});

		// Set callback for deleting this game.
		getView_ViewHolder.deleteButton
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						mContext.deleteGameFile(gameFile.mFilename);
					}
				});

		return convertView;
	}
}