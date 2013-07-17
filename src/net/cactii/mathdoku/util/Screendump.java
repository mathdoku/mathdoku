package net.cactii.mathdoku.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class Screendump extends Object {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.Screendump";

	private boolean mSaved;
	private String mFilePath;
	private Context mContext;

	/**
	 * Creates a new instance of {@link Screendump}.
	 * 
	 * @param solvingAttemptId
	 *            The if of the solving attempt on which this preview applies.
	 */
	public Screendump(Context context) {
		mContext = context;
		mSaved = false;
	}

	/**
	 * Create a screendump of the given view.
	 * 
	 * @param view
	 *            The view for which a screendump has to be generated.
	 */
	public boolean save(View view, String filename) {
		// Check if the view dimensions allow to make a preview.
		if (view.getWidth() == 0 || view.getHeight() == 0) {
			// Could not create a preview.
			return false;
		}
		
         // Create bitmap and canvas and draw the view on this canvas.
         Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
 				Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(bitmap);
 		view.draw(canvas);

		// Create file
		File file = new File(mContext.getFilesDir(), filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		// Write the created bitmap to a file.
		try {

			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 1, out); // Compress
																// factor is not
																// used
			// not used with PNG
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		mFilePath = file.getAbsolutePath();
		mSaved = true;

		return mSaved;
	}

	/**
	 * Get the qualified path of the screendump.
	 * 
	 * @return The qualified path if the screendump was saved succesfully. Null
	 *         otherwise.
	 */
	public String getFilePath() {
		return (mSaved ? mFilePath : null);
	}
}