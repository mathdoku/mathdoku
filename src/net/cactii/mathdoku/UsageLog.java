package net.cactii.mathdoku;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import net.cactii.mathdoku.GridGenerating.GridGeneratingParameters;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Simple logging of usage of functionality. Log data will be sent via email
 * only after consent of the user.
 * 
 * IMPORTANT: KEEP IN MIND THAT DO RESPECT THE PRIVACY OF OUR USERS. NO PERSONAL
 * INFORMATION MAT BE GATHERED AND STORED INTO THE LOG FILES.
 */
@TargetApi(Build.VERSION_CODES.DONUT)
// No harm can be done in case other apps can read the log file
@SuppressLint("WorldReadableFiles")
public class UsageLog {
	public final static String TAG = "MathDoku.UsageLogging";

	// Remove "&& false" in following line to show debug information about
	// creating the usage log when running in development mode.
	private static final boolean DEBUG_USAGE_LOG = (DevelopmentHelper.mode == Mode.DEVELOPMENT) && false;

	// Path and file for log information
	private final static String LOG_FILE_PREFIX = "usage_log_r";
	private final static String LOG_FILE_EXTENSION = ".txt";

	// Delimiters used in files to separate objects, fields and value
	private static final String EOL_DELIMITER = "\n"; // Separate objects
	private static final String FIELD_DELIMITER_LEVEL1 = "|"; // Separate fields
	private static final String FIELD_DELIMITER_LEVEL2 = "="; // Separate values

	// Date format for log file
	DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
			Locale.US);

	// Singleton reference to the logger
	private static UsageLog mUsageLogginSingletonInstance = null;

	// Date at or after which the logging should be stopped.
	Calendar mDateEndLogging;

	// Reference to log file
	private String mLogFileName;
	private String mLogFilePath;
	private OutputStream mLogFile;

	// Flag whether log information should be written or not.
	private boolean mBuildLog;

	// The activity which has started this usage logger
	private static Activity mMainActivity;

	/**
	 * Creates a new instance of {@link #UsageLogging()}.
	 * 
	 * This object can not be instantiated directly. Use {@link #getInstance()}
	 * to get the singleton reference to the UsageLogging object.
	 * 
	 * @param activity
	 *            The activity context in which the UsageLogging is created.
	 */
	@SuppressWarnings("deprecation")
	private UsageLog(Activity activity) {
		mMainActivity = activity;

		// Get preferences and check whether it is allowed to gather new data.
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(activity);
		if (preferences.getBoolean(MainActivity.PREF_USAGE_LOG_DISABLED,
				MainActivity.PREF_USAGE_LOG_DISABLED_DEFAULT)) {
			mBuildLog = false;
			return;
		}

		// Determine path and file names
		mLogFileName = LOG_FILE_PREFIX
				+ preferences.getInt(MainActivity.PREF_CURRENT_VERSION,
						MainActivity.PREF_CURRENT_VERSION_DEFAULT)
				+ LOG_FILE_EXTENSION;
		mLogFilePath = mMainActivity.getFileStreamPath(mLogFileName)
				.getAbsolutePath();

		// Initialize the fixed date at which the logging should be closed
		// anyway.
		mDateEndLogging = Calendar.getInstance();
		mDateEndLogging.set(2013, 5, 1); // TODO: Should be three weeks
											// after the release

		// Check if logging should continue or closed
		if (Calendar.getInstance().after(mDateEndLogging)) {
			askConsentForSendingLog(mMainActivity);
			mBuildLog = false;
			return;
		}

		// Check if log already exists.
		boolean newLogFile = false;
		File file = new File(mLogFilePath);
		if (!file.exists()) {
			newLogFile = true;
		}

		// Open the log file. Create if needed.
		mBuildLog = true;
		try {
			// Open file for append, file needs to be world readable in oder to
			// be able to send it via email.
			mLogFile = mMainActivity.openFileOutput(mLogFileName,
					Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
		} catch (IOException e) {
			// Could not create the file.
			mBuildLog = false;
			return;
		}

		// Log all preferences in case this is a new logfile.
		if (newLogFile) {
			logDevice(activity);
			logConfigurationChange(activity);
			logPreferences("Preference.Initial", preferences);
		}

		preferences.registerOnSharedPreferenceChangeListener(null);
	}

	/**
	 * Gets the singleton reference to the UsageLogging object. If it does not
	 * yet exist than it will be created.
	 * 
	 * @param mainActivity
	 *            The main activity context in which the UsageLogging is
	 *            created.
	 * 
	 * @return The singleton reference to the UsageLogging object.
	 */
	public static UsageLog getInstance(Activity activity) {
		if (mUsageLogginSingletonInstance == null
				|| !activity.equals(mMainActivity)) {
			// Only the first time this method is called for this activity, the
			// object will be created.
			mUsageLogginSingletonInstance = new UsageLog(activity);
		}
		return mUsageLogginSingletonInstance;
	}

	/**
	 * Gets the singleton reference to the UsageLogging object. If it does not
	 * yet exist an exception will be thrown.
	 * 
	 * @return The singleton reference to the UsageLogging object.
	 */
	public static UsageLog getInstance() {
		if (mUsageLogginSingletonInstance == null) {
			throw new NullPointerException(
					"UsageLogging can not be retrieved if not instantiated before.");
		}
		return mUsageLogginSingletonInstance;
	}

	/**
	 * Logs all preferences.
	 * 
	 * @param identifier
	 *            Identifier for this log item.
	 * @param preferences
	 *            The preferences to be logged.
	 */
	public void logPreferences(String identifier, SharedPreferences preferences) {
		if (mBuildLog && preferences != null) {
			SortedMap<String, String> sortedMap = new TreeMap<String, String>();

			for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
				if (entry != null) {
					String key = (String) entry.getKey();
					if (key == null) {
						continue;
					}
					Object value = entry.getValue();
					sortedMap.put(key, (value == null ? "" : value.toString()));
				}
			}
			logSortedMap(identifier, sortedMap);
		}
	}

	/**
	 * Logs info about device.
	 * 
	 * @param activity
	 *            The activity used to get device info.
	 */
	public void logDevice(Activity activity) {
		if (mBuildLog) {
			SortedMap<String, String> sortedMap = new TreeMap<String, String>();

			sortedMap.put("Android.SDK", android.os.Build.VERSION.SDK);
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				sortedMap.put("Android.Version",
						android.os.Build.VERSION.CODENAME);
				sortedMap.put("Android.SDK_INT",
						Integer.toString(android.os.Build.VERSION.SDK_INT));
			} else {
				sortedMap.put("Android.Version", null);
				sortedMap.put("Android.SDK_INT", null);
			}

			sortedMap.put("Dimension",
					activity.getResources().getString(R.string.dimension));

			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			sortedMap.put("Display.Density", Float.toString(metrics.density));
			sortedMap.put("Display.Width", Float.toString(metrics.widthPixels));
			sortedMap.put("Display.Height",
					Float.toString(metrics.heightPixels));
			sortedMap.put("Display.xdpi", Float.toString(metrics.xdpi));
			sortedMap.put("Display.ydpi", Float.toString(metrics.ydpi));

			logSortedMap("Device", sortedMap);
		}
	}

	/**
	 * Logs a single preference.
	 * 
	 * @param identifier
	 *            Identifier for this log item.
	 * @param preferences
	 *            The preferences to be logged.
	 */
	public void logPreference(String identifier, String key, Object value) {
		if (mBuildLog) {

			SortedMap<String, String> sortedMap = new TreeMap<String, String>();

			sortedMap.put(key, value.toString());

			logSortedMap(identifier, sortedMap);
		}
	}

	/**
	 * Logs results of the game file conversion.
	 * 
	 */
	public void logGameFileConversion(int mCurrentVersion, int mNewVersion,
			int mTotalGrids, int mUniqueGrids) {
		if (mBuildLog) {
			SortedMap<String, String> sortedMap = new TreeMap<String, String>();

			sortedMap.put("FromRevision", Integer.toString(mCurrentVersion));
			sortedMap.put("ToRevision", Integer.toString(mNewVersion));
			sortedMap.put("TotalGrids", Integer.toString(mTotalGrids));
			sortedMap.put("UniqueGrids", Integer.toString(mUniqueGrids));

			logSortedMap("GameFileConversion", sortedMap);
		}
	}

	/**
	 * Logs usage of function for which no additional data needs to be stored.
	 * 
	 * @param identifier
	 *            Identifier for functionality which is used.
	 */
	public void logFunction(String identifier) {
		if (mBuildLog) {
			writeLine("Function" + FIELD_DELIMITER_LEVEL1 + identifier
					+ EOL_DELIMITER);
		}
	}

	/**
	 * Logs information about a newly created grid.
	 * 
	 * @param identifier
	 *            Identifier for this log item.
	 * @param grid
	 *            The grid for which information has to be logged.
	 */
	public void logGrid(String identifier, Grid grid) {
		if (mBuildLog) {
			SortedMap<String, String> sortedMap = new TreeMap<String, String>();

			sortedMap.put("Grid.Size", Integer.toString(grid.getGridSize()));
			sortedMap.put("Grid.UndoCount",
					Integer.toString(grid.getUndoCount()));
			sortedMap.put("Grid.Cheated", Boolean.toString(grid.getCheated()));
			sortedMap
					.put("Grid.ClearRedundantPossiblesInSameRowOrColumnCount",
							Integer.toString(grid
									.getClearRedundantPossiblesInSameRowOrColumnCount()));

			GridGeneratingParameters gridGeneratingParameters = grid
					.getGridGeneratingParameters();
			sortedMap.put("Grid.Generation.GameSeed",
					Long.toString(gridGeneratingParameters.mGameSeed));
			sortedMap
					.put("Grid.Generation.Revision",
							Integer.toString(gridGeneratingParameters.mGeneratorRevisionNumber));
			sortedMap.put("Grid.Generation.HideOperators",
					Boolean.toString(gridGeneratingParameters.mHideOperators));
			sortedMap.put("Grid.Generation.MaxCageResult",
					Integer.toString(gridGeneratingParameters.mMaxCageResult));
			sortedMap.put("Grid.Generation.MaxCageSize",
					Integer.toString(gridGeneratingParameters.mMaxCageSize));

			logSortedMap(identifier, sortedMap);
		}
	}

	/**
	 * Logs information about a configuration change.
	 * 
	 * @param identifier
	 *            Identifier for this log item.
	 */
	public void logConfigurationChange(Activity activity) {
		if (mBuildLog) {
			SortedMap<String, String> sortedMap = new TreeMap<String, String>();

			Configuration configuration = activity.getResources()
					.getConfiguration();
			sortedMap.put("locale", configuration.locale.toString());
			sortedMap.put("orientation",
					Integer.toString(configuration.orientation));

			logSortedMap("Configuration", sortedMap);
		}
	}

	/**
	 * Log values related to the survey.
	 * 
	 * @param email
	 *            The email address to be logged.
	 * 
	 * @param name
	 *            The (user) name to be logged.
	 */
	public void logSurvey(String email, String name) {
		if (mBuildLog) {
			SortedMap<String, String> sortedMap = new TreeMap<String, String>();

			sortedMap.put("Email", email);
			sortedMap.put("Name", name);

			logSortedMap("Survey", sortedMap);
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
		if (mBuildLog) {
			String logLine = identifier;

			// Get Map in Set interface to get key and value
			for (Map.Entry<String, String> entry : map.entrySet()) {
				// Get key and value
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();

				logLine += FIELD_DELIMITER_LEVEL1 + key
						+ FIELD_DELIMITER_LEVEL2
						+ (value == null ? "" : value.toString());
			}
			writeLine(logLine + EOL_DELIMITER);
		}
	}

	/**
	 * Write a single line to the log file.
	 * 
	 * @param line
	 *            The line to be written.
	 */
	private void writeLine(String line) {
		if (mBuildLog && mLogFile != null) {
			try {
				// Prefix line with current date time
				line = mDateFormat.format(new Date()) + FIELD_DELIMITER_LEVEL1
						+ line;
				mLogFile.write(line.getBytes());
				mLogFile.flush();

				if (DEBUG_USAGE_LOG) {
					Log.i(TAG, line);
				}
			} catch (IOException e) {
				// Could not write
				mBuildLog = false;
				return;
			}
		}

		// Check if logging period has ended.
		if (Calendar.getInstance().after(mDateEndLogging)) {
			askConsentForSendingLog(mMainActivity);
			mBuildLog = false;
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
		mBuildLog = false;
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

			if (DEBUG_USAGE_LOG) {
				Log.i(TAG,
						"**************** Log has been deleted ******************");
			}

			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				// Clear references to he activity after delete log, so it can
				// be recreated.
				mMainActivity = null;
			}
		}
	}

	/**
	 * Ask consent of user to send log via email.
	 * 
	 * @param mainActivity
	 *            The main activity context in which the UsageLogging is
	 *            created.
	 */
	public void askConsentForSendingLog(final Activity mainActivity) {
		if (!isEmailIntentAvailable(mainActivity)) {
			// No email client available anymore.
			return;
		}
		
		// Insert link into the dialog
		LayoutInflater inflater = LayoutInflater.from(mainActivity);
		View usagelogView = inflater.inflate(R.layout.usagelog_dialog, null);
		TextView textView = (TextView) usagelogView
				.findViewById(R.id.dialog_share_log_link);
		textView.setText(MainActivity.PROJECT_HOME
										+ "wiki/UsageLogging");

		// Build dialog
		new AlertDialog.Builder(mainActivity)
			.setTitle(R.string.dialog_usagelog_title)
			.setView(usagelogView)
				.setNegativeButton(R.string.dialog_usagelog_negative_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								getInstance().delete();

								// Update preferences
								SharedPreferences preferences = PreferenceManager
										.getDefaultSharedPreferences(mainActivity);
								Editor prefeditor = preferences.edit();
								prefeditor.putBoolean(
										MainActivity.PREF_USAGE_LOG_DISABLED,
										true);
								prefeditor.commit();
							}
						})
				.setPositiveButton(R.string.dialog_usagelog_positive_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								askConsentForSurvey(mainActivity);
							}
						}).show();
	/*
		 FrameLayout fl = (FrameLayout) builder.findViewById(android.R.id.custom);
		 fl.addView(textView, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));*/
	}

	/**
	 * Ask consent for sending an additional survey.
	 * 
	 * @param mainActivity
	 *            The main activity context in which the UsageLogging is
	 *            created.
	 */
	private void askConsentForSurvey(final Activity mainActivity) {
		if (!isEmailIntentAvailable(mainActivity)) {
			// No email client available anymore.
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

		LayoutInflater inflater = LayoutInflater.from(mainActivity);
		final View surveyView = inflater.inflate(R.layout.survey_dialog, null);
		builder.setTitle(R.string.dialog_survey_title)
				.setView(surveyView)
				.setPositiveButton(R.string.dialog_survey_positive_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Write actual preferences to log.
								SharedPreferences preferences = PreferenceManager
										.getDefaultSharedPreferences(mainActivity);
								logPreferences("Preference.Final", preferences);

								// If user has entered his email address, and
								// optionally a name these should be added to
								// the logfile.
								EditText userEmail = (EditText) surveyView
										.findViewById(R.id.dialog_survey_user_email);
								String email = (userEmail == null ? ""
										: userEmail.getText().toString());
								if (!email.equals("")) {
									// User has left an email address
									EditText userName = (EditText) surveyView
											.findViewById(R.id.dialog_survey_user_name);
									String name = (userName == null ? ""
											: userName.getText().toString());
									getInstance().logSurvey(email, name);
								}

								// Close log file
								getInstance().close();

								// Send log file
								Intent i = new Intent(Intent.ACTION_SEND);
								i.setType("message/rfc822");
								i.putExtra(Intent.EXTRA_EMAIL,
										new String[] { "log@mathdoku.net" });
								i.putExtra(
										Intent.EXTRA_SUBJECT,
										mainActivity.getResources().getString(
												R.string.usage_log_subject));
								i.putExtra(
										Intent.EXTRA_TEXT,
										mainActivity.getResources().getString(
												R.string.usage_log_body));
								i.putExtra(Intent.EXTRA_STREAM,
										Uri.parse("file://" + mLogFilePath));
								try {
									mainActivity.startActivity(Intent
											.createChooser(
													i,
													mainActivity
															.getResources()
															.getString(
																	R.string.usage_log_choose_action_title)));
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