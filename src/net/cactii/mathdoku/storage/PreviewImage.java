package net.cactii.mathdoku.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.cactii.mathdoku.DevelopmentHelper;
import net.cactii.mathdoku.DevelopmentHelper.Mode;
import net.cactii.mathdoku.GridView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;

public class PreviewImage extends Object {
	private static final String TAG = "MathDoku.PreviewImage";

	String mFilename;

	// This object will only be statically initialized once.
	static boolean mInitialized = false;
	static int mSize;

	/**
	 * Creates a new instance of {@link PreviewImage}.
	 * 
	 * @param filename
	 *            The name (full path) of the preview file.
	 */
	public PreviewImage(String filename) {
		init(filename);
	}

	/**
	 * Creates a new instance of {@link PreviewImage}.
	 * 
	 * @param gameFile
	 *            The game file for which a preview has to be saved.
	 */
	public PreviewImage(GameFile gameFile) {
		init(gameFile == null ? "" : gameFile.getFullFilenamePreview());
	}

	/**
	 * Initializes the object.
	 * 
	 * @param filename
	 *            The name (full path) of the preview file.
	 */
	private void init(String filename) {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (filename == null || filename.equals("")) {
				throw new RuntimeException(
						"Filename should not be null or emtpy.");
			}
			if (!mInitialized) {
				throw new RuntimeException(
						"PreviewImage has not been initialized statically.");
			}
		}
		mFilename = filename;
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
		if (mSize == 0) {
			// Can not calculate size of preview image.
			return false;
		}
		float scaleFactor = (float) mSize
				/ (float) Math.max(view.getWidth(), view.getHeight());
		Bitmap bitmap = Bitmap.createBitmap(mSize, mSize,
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

	public static boolean setSize(Activity activity) {
		if (mInitialized) {
			// PreviewImage needs only to initialized once
			return true;
		}

		if (activity == null) {
			return false;
		}

		// Get the display metrics
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);

		mSize = (int) ((float) 0.45 * (float) Math.min(
				displayMetrics.heightPixels, displayMetrics.widthPixels));
		mInitialized = true;

		return mInitialized;
	}

	/**
	 * Get the size (height equals width) for preview images used on this device.
	 * 
	 * @return The size of the preview images.
	 */
	public static int getPreviewImageSize() {
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (!mInitialized) {
				throw new RuntimeException(
						"PreviewImage has not been initialized statically.");
			}
		}
		return mSize;
	}
}