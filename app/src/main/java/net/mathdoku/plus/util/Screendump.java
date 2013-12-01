package net.mathdoku.plus.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class Screendump {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.Screendump";

	private final Context mContext;

	/**
	 * Creates a new instance of {@link Screendump}.
	 * 
	 * @param context
	 *            The context in which a screendump has to be created.
	 */
	public Screendump(Context context) {
		mContext = context;
	}

	/**
	 * Create a screendump of the given view.
	 * 
	 * @param view
	 *            The view for which a screendump has to be generated.
	 */
	public boolean save(View view, String filename) {
		// Check if the view dimensions allow to make a preview.
		if (view == null || view.getWidth() == 0 || view.getHeight() == 0) {
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
				if (!file.createNewFile()) {
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		// Write the created bitmap to a file.
		try {

			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 1, out); /*
																 * Compress
																 * factor is not
																 * used not used
																 * with PNG
																 */
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}