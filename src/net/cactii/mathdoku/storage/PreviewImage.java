package net.cactii.mathdoku.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.cactii.mathdoku.developmentHelpers.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelpers.DevelopmentHelper.Mode;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import net.cactii.mathdoku.ui.GridView;
import net.cactii.mathdoku.util.Util;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

public class PreviewImage extends Object {
	private static final String TAG = "MathDoku.PreviewImage";

	public static final String FILENAME_BASE = "Preview_";
	public static final String FILENAME_EXTENSION = ".png";
	
	private int mSolvingAttemptId;
	
	// Filename for this preview image
	private String mFilename;

	/**
	 * Creates a new instance of {@link PreviewImage}.
	 * 
	 * @param solvingAttemptId
	 *            The if of the solving attempt on which this preview applies.
	 */
	public PreviewImage(int solvingAttemptId) {
		mFilename = Util.getPath() + FILENAME_BASE + solvingAttemptId + FILENAME_EXTENSION;
		mSolvingAttemptId = solvingAttemptId;
	}

	/**
	 * Save a preview image of the grid view.
	 * 
	 * @param activity
	 *            The activity in which context the preview image is saved.
	 * @param view
	 *            The grid view for which the preview image has to be generated.
	 */
	public boolean save(GridView view) {
		// Check if the view dimensions allow to make a preview.
		if (view.getWidth() == 0 || view.getHeight() == 0) {
			if (DevelopmentHelper.mMode != Mode.PRODUCTION) {
				Log.i(TAG,
						"Can not save the preview image. If running on an Emulator for "
								+ "Android 2.2 this is normal behavior when rotating screen "
								+ "from landscap to portrait as the Activity.onCreate is called "
								+ "twice instead of one time.");
				return true;
			}
			// Could not create a preview.
			return false;
		}

		// Create a scaled bitmap and canvas and draw the view on this canvas.
		int size = getPreviewImageSize();
		if (size == 0) {
			// Can not calculate size of preview image.
			return false;
		}
		float scaleFactor = (float) size
				/ (float) Math.max(view.getWidth(), view.getHeight());
		Bitmap bitmap = Bitmap.createBitmap(size, size,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.scale(scaleFactor, scaleFactor);
		view.draw(canvas);

		// Write the created bitmap to a file.
		try {
			FileOutputStream out = new FileOutputStream(mFilename);
			bitmap.compress(Bitmap.CompressFormat.PNG, 1, out); // Compress
																// factor is
			// not used with PNG
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		// Update reference in database if needed.
		new SolvingAttemptDatabaseAdapter().updatePreviewFilename(mSolvingAttemptId, mFilename);
		
		return true;
	}

	/**
	 * Get the preview image.
	 * 
	 * @return The bitmap containing the preview image of this game file.
	 */
	public Bitmap load() {
		Bitmap bitmap = null;

		File file = new File(mFilename);
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			bitmap = BitmapFactory.decodeStream(in);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found error when loading image preview "
					+ mFilename + "\n" + e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}

		return bitmap;
	}

	/**
	 * Get the size (height equals width) for preview images used on this device.
	 * 
	 * @return The size of the preview images.
	 */
	public static int getPreviewImageSize() {
		return (int) ((float) 0.45 * Util.getMinimumDisplayHeigthWidth());
	}
}