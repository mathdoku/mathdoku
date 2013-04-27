package net.cactii.mathdoku;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import net.cactii.mathdoku.storage.PreviewImage;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import net.cactii.mathdoku.storage.database.SolvingAttemptPreview;
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

	// Remove "&& false" in following line to show debug information about
	// saving and restoring files when running in development mode.
	private static final boolean DEBUG_SAVE_RESTORE = (DevelopmentHelper.mMode == Mode.DEVELOPMENT) && false;

	// The list of game files available.
	public ArrayList<SolvingAttemptPreview> mSolvingAttemptPreviews;

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
		this.mSolvingAttemptPreviews = new ArrayList<SolvingAttemptPreview>();

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
	 * Get all current game files.
	 */
	public void refreshFiles() {
		// Read from database which grids have to be shown in the list. In
		// future additional filters can be easily added to this query.
		mSolvingAttemptPreviews = new SolvingAttemptDatabaseAdapter()
				.getPreviewList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return this.mSolvingAttemptPreviews.size();
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
		return this.mSolvingAttemptPreviews.get(arg0 - 1);
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
		RelativeLayout previewRelativeLayout;
		RelativeLayout detailsRelativeLayout;
		ImageView imageView;
		RelativeLayout imagePreviewNotAvailable;
		TextView filenameTextView;
		TextView savedOnTextView;
		Button loadButton;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// Create a new convertView only in case it can not be recycled.
		if (convertView == null) {
			// Define and initialise the viewHolder
			getView_ViewHolder = new ViewHolder();

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
			if (DEBUG_SAVE_RESTORE) {
				getView_ViewHolder.filenameTextView = ((TextView) convertView
						.findViewById(R.id.filename));
				getView_ViewHolder.filenameTextView.setVisibility(View.VISIBLE);
			}

			// Set reference to field save date time
			getView_ViewHolder.savedOnTextView = (TextView) convertView
					.findViewById(R.id.savedOnText);

			// Set reference to button.
			getView_ViewHolder.loadButton = (Button) convertView
					.findViewById(R.id.gameLoad);

			// Store the view holder as a tag in the convert view.
			convertView.setTag(getView_ViewHolder);
		} else {
			// In case a convertView is recycled, the references in the
			// viewHolder object can be reused.
			getView_ViewHolder = (ViewHolder) convertView.getTag();
		}

		// Get game file header information for current position.
		final SolvingAttemptPreview gameFileHeader = this.mSolvingAttemptPreviews
				.get(position);

		// Display the preview image if available. Else display the placeholder.
		if (gameFileHeader.mPreviewImageFilename == null
				|| gameFileHeader.mPreviewImageFilename.equals("")
				|| !(new File(gameFileHeader.mPreviewImageFilename).exists())) {
			getView_ViewHolder.imageView.setVisibility(View.GONE);
			getView_ViewHolder.imagePreviewNotAvailable
					.setVisibility(View.VISIBLE);
		} else {
			getView_ViewHolder.imageView.setVisibility(View.VISIBLE);
			getView_ViewHolder.imagePreviewNotAvailable
					.setVisibility(View.GONE);
			Bitmap preview = new PreviewImage(gameFileHeader.mId).load();
			getView_ViewHolder.imageView.setImageBitmap(preview);
		}

		// Set filename
		if (DEBUG_SAVE_RESTORE) {
			getView_ViewHolder.filenameTextView.setText("Id: "
					+ gameFileHeader.mId); // TODO: rename filenameTextView
		}

		// Calculate the time elapsed since creating the game file.
		if (getView_CurrentTime == null) {
			getView_CurrentTime = Calendar.getInstance();
		}
		getView_GameFileTime = Calendar.getInstance();
		getView_GameFileDate = Calendar.getInstance();

		getView_GameFileTime.setTimeInMillis(gameFileHeader.mDateUpdated);
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
							gameFileHeader.mDateUpdated));
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
							DateFormat.SHORT).format(
							gameFileHeader.mDateUpdated));
		}

		// Set callback for loading this game.
		getView_ViewHolder.loadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mContext.loadGameFile(gameFileHeader.mId);
			}
		});

		return convertView;
	}
}