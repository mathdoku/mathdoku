package net.cactii.mathdoku.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class FeedbackEmail {
	public final static String TAG = "MathDoku.FeedbackEmail";

	// Path and file for log information
	private final static String LOG_FILE_NAME = "device_info_and_settings.txt";

	// Delimiters used in files to separate objects, fields and value
	private static final String EOL_DELIMITER = "\n"; // Separate objects
	private static final String FIELD_DELIMITER_LEVEL1 = "|"; // Separate fields
	private static final String FIELD_DELIMITER_LEVEL2 = "="; // Separate values

	// Date format for log file
	DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
			Locale.US);

	// Reference to log file
	private String mLogFilePath;
	private OutputStream mLogFile;

	private Activity mActivity;

	/**
	 * Creates a new instance of {@link #FeedbackEmail()}.
	 * 
	 * @param activity
	 *            The activity context in which the UsageLogging is created.
	 */
	public FeedbackEmail(Activity activity) {
		mActivity = activity;
	}

	/**
	 * Create a log file containing basic information about the device, the
	 * configuration and the preferences.
	 * 
	 * @param filename
	 *            The name of the log file to be created.
	 * @return True in case the file has been created. False otherwise.
	 */
	private boolean createLogFile(String filename) {
		File file = new File(mActivity.getFilesDir(), LOG_FILE_NAME);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		mLogFilePath = file.getAbsolutePath();

		try {
			mLogFile = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		logDevice();
		logConfiguration();
		logPreferences();

		close();

		return true;
	}

	/**
	 * Logs info about device.
	 * 
	 * @param activity
	 *            The activity used to get device info.
	 */
	public void logDevice() {
		SortedMap<String, String> sortedMap = new TreeMap<String, String>();

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			sortedMap.put("Android.Version", android.os.Build.VERSION.CODENAME);
			sortedMap.put("Android.SDK_INT",
					Integer.toString(android.os.Build.VERSION.SDK_INT));
		} else {
			sortedMap.put("Android.Version", null);
			sortedMap.put("Android.SDK_INT", null);
		}

		sortedMap.put("Dimension",
				mActivity.getResources().getString(R.string.dimension));

		DisplayMetrics metrics = new Util(mActivity).getDisplayMetrics();
		sortedMap.put("Display.Density", Float.toString(metrics.density));
		sortedMap.put("Display.Width", Float.toString(metrics.widthPixels));
		sortedMap.put("Display.Height", Float.toString(metrics.heightPixels));
		sortedMap.put("Display.xdpi", Float.toString(metrics.xdpi));
		sortedMap.put("Display.ydpi", Float.toString(metrics.ydpi));

		logSortedMap("Device", sortedMap);
	}

	/**
	 * Logs information about a configuration.
	 */
	public void logConfiguration() {
		SortedMap<String, String> sortedMap = new TreeMap<String, String>();

		Configuration configuration = mActivity.getResources()
				.getConfiguration();
		sortedMap.put("locale", configuration.locale.toString());
		sortedMap.put("orientation",
				Integer.toString(configuration.orientation));

		logSortedMap("Configuration", sortedMap);
	}

	/**
	 * Logs all preferences.
	 * 
	 * @param identifier
	 *            Identifier for this log item.
	 * @param preferences
	 *            The preferences to be logged.
	 */
	public void logPreferences() {
		// Get preferences and check whether it is allowed to gather new data.
		Preferences preferences = Preferences.getInstance();

		if (preferences != null) {
			SortedMap<String, String> sortedMap = new TreeMap<String, String>();

			for (Map.Entry<String, ?> entry : preferences
					.getAllSharedPreferences().entrySet()) {
				if (entry != null) {
					String key = (String) entry.getKey();
					if (key == null) {
						continue;
					}
					Object value = entry.getValue();
					sortedMap.put(key, (value == null ? "" : value.toString()));
				}
			}
			logSortedMap("Settings", sortedMap);
		}
	}

	/**
	 * Write a set of key/value pairs to the log.
	 * 
	 * @param identifier
	 *            The identifier for this map
	 * @param map
	 *            The set of key/values to be written to the log.
	 */
	private void logSortedMap(String identifier, SortedMap<String, String> map) {
		String logLine = identifier;

		// Get Map in Set interface to get key and value
		for (Map.Entry<String, String> entry : map.entrySet()) {
			// Get key and value
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();

			logLine += FIELD_DELIMITER_LEVEL1 + key + FIELD_DELIMITER_LEVEL2
					+ (value == null ? "" : value.toString());
		}
		writeLine(logLine + EOL_DELIMITER);
	}

	/**
	 * Write a single line to the log file.
	 * 
	 * @param line
	 *            The line to be written.
	 */
	private void writeLine(String line) {
		try {
			// Prefix line with current date time
			line = mDateFormat.format(new Date()) + FIELD_DELIMITER_LEVEL1
					+ line;
			mLogFile.write(line.getBytes());
			mLogFile.flush();
		} catch (IOException e) {
			// Could not write
			return;
		}
	}

	/**
	 * Close the log file.
	 */
	public void close() {
		if (mLogFile != null) {
			try {
				mLogFile.flush();
				mLogFile.close();
			} catch (IOException e) {
				// Do nothing.
			}
		}
	}

	/**
	 * Delete the log file.
	 */
	public void delete() {
		// Close the log file
		close();

		// Delete the log file
		File file = new File(mLogFilePath);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * Ask consent of user to send log via email.
	 * 
	 * @param activity
	 *            The main activity context in which the UsageLogging is
	 *            created.
	 */
	public void show() {
		if (!isEmailIntentAvailable(mActivity)) {
			// No email client available anymore.
			return;
		}

		// Create a screen dump before showing the alert.
		final Screendump screendump = new Screendump(mActivity);
		screendump.save(mActivity
				.getWindow().getDecorView(),
				FileProvider.SCREENDUMP_FILE_NAME);

		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(mActivity);
		View view = li.inflate(R.layout.send_feedback_dialog, null);

		TextView textView = (TextView) view
				.findViewById(R.id.dialog_send_feedback_issues_link);
		textView.setText(Util.PROJECT_HOME + "issues.php");

		new AlertDialog.Builder(mActivity)
				.setTitle(
						mActivity.getResources().getString(
								R.string.dialog_send_feedback_title))
				.setView(view)
				.setNegativeButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Do nothing
							}
						})
				.setPositiveButton(
						R.string.dialog_send_feedback_positive_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Send log file
								Intent i = new Intent(
										Intent.ACTION_SEND_MULTIPLE);
								i.setType("message/rfc822");
								i.putExtra(Intent.EXTRA_EMAIL,
										new String[] { "info@mathdoku.net" });
								i.putExtra(
										Intent.EXTRA_SUBJECT,
										mActivity
												.getResources()
												.getString(
														R.string.feedback_email_subject));
								i.putExtra(
										Intent.EXTRA_TEXT,
										mActivity.getResources().getString(
												R.string.feedback_email_body));

								if (createLogFile(FileProvider.FEEDBACK_LOG_FILE_NAME)) {
									ArrayList<Uri> uris = new ArrayList<Uri>();
									uris.add(FileProvider
											.getUri(FileProvider.FEEDBACK_LOG_FILE_NAME));
									uris.add(FileProvider
											.getUri(FileProvider.SCREENDUMP_FILE_NAME));
									i.putParcelableArrayListExtra(
											Intent.EXTRA_STREAM, uris);
								}
								try {
									mActivity.startActivity(Intent
											.createChooser(
													i,
													mActivity
															.getResources()
															.getString(
																	R.string.dialog_send_feedback_title)));
								} catch (android.content.ActivityNotFoundException ex) {
									// No clients installed which can handle
									// this intent.
								}
							}
						}).show();
	}

	/**
	 * Checks if an email client is available on the device.
	 * 
	 * @param activity
	 *            The activity which started this usage logger.
	 * @return True in case an email client is installed. False otherwise.
	 */
	public static boolean isEmailIntentAvailable(Activity activity) {
		final PackageManager packageManager = activity.getPackageManager();
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		return list.size() > 0;
	}
}
