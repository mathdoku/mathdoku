package net.mathdoku.plus;

import java.util.Locale;
import java.util.Map;

import net.mathdoku.plus.grid.ui.GridInputMode;
import net.mathdoku.plus.gridGenerating.GridGenerator.PuzzleComplexity;
import net.mathdoku.plus.leaderboard.ui.LeaderboardFragmentActivity.LeaderboardFilter;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.painter.Painter.GridTheme;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter.SizeFilter;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter.StatusFilter;
import net.mathdoku.plus.tip.TipDialog;
import net.mathdoku.plus.util.SingletonInstanceNotInstantiated;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Preferences {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.Preferences";

	// Singleton reference to the preferences object
	private static Preferences mPreferencesSingletonInstance = null;

	// Actual preferences
	public SharedPreferences mSharedPreferences;

	// Global APP preferences
	public final static String APP_CURRENT_VERSION = "app_current_version";
	public final static int APP_CURRENT_VERSION_DEFAULT = -1;

	// Archive preferences
	public final static String ARCHIVE_AVAILABLE = "archive_available";
	public final static boolean ARCHIVE_AVAILABLE_DEFAULT = false;

	public final static String ARCHIVE_GRID_LAST_SHOWED = "archive_grid_last_showed";
	public final static int ARCHIVE_GRID_LAST_SHOWED_DEFAULT = -1;

	public final static String ARCHIVE_SIZE_FILTER_LAST_VALUE = "archive_size_filter_last_value";
	public final static String ARCHIVE_SIZE_FILTER_LAST_VALUE_DEFAULT = SizeFilter.ALL
			.toString();

	public final static String ARCHIVE_STATUS_FILTER_LAST_VALUE = "archive_status_filter_last_value";
	public final static String ARCHIVE_STATUS_FILTER_LAST_VALUE_DEFAULT = StatusFilter.ALL
			.toString();

	public final static String ARCHIVE_SETTING_SIZE_FILTER_VISIBLE = "archive_setting_size_filter_size_visible";
	public final static boolean ARCHIVE_SETTING_SIZE_FILTER_VISIBLE_DEFAULT = false;

	public final static String ARCHIVE_SETTING_STATUS_FILTER_VISIBLE = "archive_setting_status_filter_visible";
	public final static boolean ARCHIVE_SETTING_STATUS_FILTER_VISIBLE_DEFAULT = true;

	public final static String ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE = "archive_setting_chart_description_visible";
	public final static boolean ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT = true;

	// Leaderboard settings
	public final static String LEADERBOARD_ALL_INITIALIZED = "leaderboard_all_initialized";
	public final static boolean LEADERBOARD_ALL_INITIALIZED_DEFAULT = false;

	public final static String LEADERBOARD_FILTER_LAST_VALUE = "leaderboard_filter_last_value";
	public final static String LEADERBOARD_FILTER_LAST_VALUE_DEFAULT = LeaderboardFilter.ALL_LEADERBOARDS
			.toString();

	public final static String LEADERBOARD_TAB_LAST_SHOWED = "leaderboard_tab_last_showed";
	public final static int LEADERBOARD_TAB_LAST_SHOWED_DEFAULT = 0;

	public final static String PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE = "puzzle_hide_goole_plus_sign_in_till_next_top_score";
	public final static boolean PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE_DEFAULT = false;

	// Puzzle input mode settings
	public final static String PUZZLE_INPUT_MODE_CHANGED_COUNTER = "puzzle_input_mode_changed_counter";
	public final static int PUZZLE_INPUT_MODE_CHANGED_COUNTER_DEFAULT = 0;

	public final static String PUZZLE_INPUT_MODE_COPY_COUNTER = "puzzle_input_mode_copy_counter";
	public final static int PUZZLE_INPUT_MODE_COPY_COUNTER_DEFAULT = 0;

	public final static String PUZZLE_INPUT_MODE_LAST_USED = "puzzle_input_mode_last_used";
	public final static String PUZZLE_INPUT_MODE_LAST_USED_DEFAULT = GridInputMode.NORMAL
			.toString();

	public final static String PUZZLE_INPUT_MODE_COPY_ENABLED = "puzzle_input_mode_copy_enabled";
	public final static boolean PUZZLE_INPUT_MODE_COPY_ENABLED_DEFAULT = false;

	// Puzzle parameters settings to be set as default values for next game
	public final static String PUZZLE_PARAMETER_COMPLEXITY = "puzzle_parameter_complexity";
	public final static String PUZZLE_PARAMETER_COMPLEXITY_DEFAULT = PuzzleComplexity.VERY_EASY
			.toString();

	public final static String PUZZLE_PARAMETER_OPERATORS_VISIBLE = "puzzle_parameter_operators_visible";
	public final static boolean PUZZLE_PARAMETER_OPERATORS_VISIBLE_DEFAULT = true;

	public final static String PUZZLE_PARAMETER_SIZE = "puzzle_parameter_size";
	public final static int PUZZLE_PARAMETER_SIZE_DEFAULT = 4;

	// Puzzle setting preferences
	public final static String PUZZLE_SETTING_BAD_CAGE_MATHS_VISIBLE = "puzzle_setting_bad_cage_math_visible";
	public final static boolean PUZZLE_SETTING_BAD_CAGE_MATHS_VISIBLE_DEFAULT = true;

	public final static String PUZZLE_SETTING_CLEAR_MAYBES = "puzzle_setting_clear_maybes";
	public final static boolean PUZZLE_SETTING_CLEAR_MAYBES_DEFAULT = true;

	public final static String PUZZLE_SETTING_COLORED_DIGITS = "puzzle_setting_colored_digits";
	public final static boolean PUZZLE_SETTING_COLORED_DIGITS_DEFAULT = true;

	public final static String PUZZLE_SETTING_DUPLICATE_DIGITS_VISIBLE = "puzzle_setting_duplicate_digits_visible";
	public final static boolean PUZZLE_SETTING_DUPLICATE_DIGITS_VISIBLE_DEFAULT = true;

	public final static String PUZZLE_SETTING_FULL_SCREEN = "puzzle_setting_full_screen";
	public final static boolean PUZZLE_SETTING_FULL_SCREEN_DEFAULT = false;

	public enum PuzzleSettingInputMethod {
		SWIPE_ONLY, SWIPE_AND_BUTTONS, BUTTONS_ONLY
	};

	public final static String PUZZLE_SETTING_INPUT_METHOD = "puzzle_setting_input_method";
	public final static String PUZZLE_SETTING_INPUT_METHOD_DEFAULT = PuzzleSettingInputMethod.SWIPE_ONLY
			.toString();

	public final static String PUZZLE_SETTING_MAYBES_DISPLAYED_IN_GRID = "puzzle_setting_maybes_displayed_in_grid";
	public final static boolean PUZZLE_SETTING_MAYBES_DISPLAYED_IN_GRID_DEFAULT = true;

	public final static String PUZZLE_SETTING_OUTER_SWIPE_CIRCLE = "puzzle_setting_outer_swipe_circle";
	public final static String PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_DEFAULT = "4";
	public final static String PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_NEVER_VISIBLE = Integer
			.toString(Integer.MAX_VALUE);

	public final static String PUZZLE_SETTING_PLAY_SOUND_EFFECTS = "puzzle_setting_sound_effects";
	public final static boolean PUZZLE_SETTING_PLAY_SOUND_EFFECTS_DEFAULT = true;

	public final static String PUZZLE_SETTING_THEME = "puzzle_setting_theme";
	public final static String PUZZLE_SETTING_THEME_DARK = "theme_dark";
	public final static String PUZZLE_SETTING_THEME_LIGHT = "theme_light";
	public final static String PUZZLE_SETTING_THEME_DEFAULT = PUZZLE_SETTING_THEME_LIGHT;

	public final static String PUZZLE_SETTING_TIMER_VISIBLE = "puzzle_setting_timer_visible";
	public final static boolean PUZZLE_SETTING_TIMER_VISIBLE_DEFAULT = true;

	public final static String PUZZLE_SETTING_WAKE_LOCK = "puzzle_setting_wake_lock";
	public final static boolean PUZZLE_SETTING_WAKE_LOCK_DEFAULT = true;

	// Statistics setting preferences
	public final static String STATISTICS_AVAILABLE = "statistics_available";
	public final static boolean STATISTICS_AVAILABLE_DEFAULT = false;

	public final static String STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE = "statistics_setting_chart_description_visible";
	public final static boolean STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT = true;

	public final static String STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES = "statistics_setting_elapsed_time_chart_maximum_games";
	public final static String STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES_DEFAULT = Integer
			.toString(100);

	public final static String STATISTICS_TAB_LAST_SHOWED = "statistics_tab_last_showed";
	public final static int STATISTICS_TAB_LAST_SHOWED_DEFAULT = -1;

	// Swipe counters
	public final static String SWIPE_INVALID_MOTION_COUNTER = "swipe_invalid_motion_counter";
	public final static String SWIPE_VALID_MOTION_COUNTER = "swipe_valid_motion_counter";
	public final static String SWIPE_DIGIT_1_COUNTER = "swipe_digit_1_counter";
	public final static String SWIPE_DIGIT_2_COUNTER = "swipe_digit_2_counter";
	public final static String SWIPE_DIGIT_3_COUNTER = "swipe_digit_3_counter";
	public final static String SWIPE_DIGIT_4_COUNTER = "swipe_digit_4_counter";
	public final static String SWIPE_DIGIT_5_COUNTER = "swipe_digit_5_counter";
	public final static String SWIPE_DIGIT_6_COUNTER = "swipe_digit_6_counter";
	public final static String SWIPE_DIGIT_7_COUNTER = "swipe_digit_7_counter";
	public final static String SWIPE_DIGIT_8_COUNTER = "swipe_digit_8_counter";
	public final static String SWIPE_DIGIT_9_COUNTER = "swipe_digit_9_counter";
	public final static int SWIPE_DIGIT_COUNTER_DEFAULT = 0;

	// Dependent on the speed of playing, the counter preferences are often
	// updated. For this reason they are kept in memory and only committed to
	// memory on call to commitCounters.
	private final static int PUZZLE_INPUT_MODE_CHANGED_COUNTER_ID = 0;
	private final static int SWIPE_DIGIT_1_COUNTER_ID = 1;
	private final static int SWIPE_DIGIT_2_COUNTER_ID = 2;
	private final static int SWIPE_DIGIT_3_COUNTER_ID = 3;
	private final static int SWIPE_DIGIT_4_COUNTER_ID = 4;
	private final static int SWIPE_DIGIT_5_COUNTER_ID = 5;
	private final static int SWIPE_DIGIT_6_COUNTER_ID = 6;
	private final static int SWIPE_DIGIT_7_COUNTER_ID = 7;
	private final static int SWIPE_DIGIT_8_COUNTER_ID = 8;
	private final static int SWIPE_DIGIT_9_COUNTER_ID = 9;
	private final static int SWIPE_VALID_MOTION_COUNTER_ID = 10;
	private final static int SWIPE_INVALID_MOTION_COUNTER_ID = 11;
	private final static int PUZZLE_INPUT_MODE_COPY_COUNTER_ID = 12;
	private int[] counters = null;

	/**
	 * Creates a new instance of {@link Preferences}.
	 * 
	 * This object can not be instantiated directly. Use {@link #getInstance()}
	 * to get the singleton reference to the Preference object.
	 * 
	 * @param context
	 *            The context for which the preferences have to be determined.
	 * 
	 */
	private Preferences(Context context) {
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
	}

	/**
	 * Gets the singleton reference to the Preference object. If it does not yet
	 * exist then it will be created.
	 * 
	 * @param context
	 *            The context in which the GridPainter is created.
	 * 
	 * @return The context for which the preferences have to be determined.
	 */
	public static Preferences getInstance(Context context) {
		if (mPreferencesSingletonInstance == null) {
			// Only the first time this method is called, the object will be
			// created.
			mPreferencesSingletonInstance = new Preferences(context);
		}
		return mPreferencesSingletonInstance;
	}

	/**
	 * Gets the singleton reference to the Preferences object. If it does not
	 * yet exist an exception will be thrown.
	 * 
	 * @return The singleton reference to the Preferences object.
	 */
	public static Preferences getInstance() {
		if (mPreferencesSingletonInstance == null) {
			throw new SingletonInstanceNotInstantiated();
		}
		return mPreferencesSingletonInstance;
	}

	/**
	 * Upgrade preferences to another version.
	 * 
	 * @param previousInstalledVersion
	 *            The old version installed.
	 * @param currentVersion
	 *            The new version to which the preferences need to be upgraded.
	 */
	public void upgrade(int previousInstalledVersion, int currentVersion) {

		// Update preferences
		Editor prefeditor = mSharedPreferences.edit();

		// Each new setting which is displayed via the settings menu has to be
		// set to the default value when installing/upgrading the app. If this
		// is not done then most settings won't be displayed with the default
		// value.

		if (previousInstalledVersion < 583 && currentVersion >= 583) {
			prefeditor
					.putBoolean(PUZZLE_SETTING_CLEAR_MAYBES,
							PUZZLE_SETTING_CLEAR_MAYBES_DEFAULT)
					.putBoolean(PUZZLE_SETTING_PLAY_SOUND_EFFECTS,
							PUZZLE_SETTING_PLAY_SOUND_EFFECTS_DEFAULT)
					.putBoolean(PUZZLE_SETTING_BAD_CAGE_MATHS_VISIBLE,
							PUZZLE_SETTING_BAD_CAGE_MATHS_VISIBLE_DEFAULT)
					.putBoolean(PUZZLE_SETTING_DUPLICATE_DIGITS_VISIBLE,
							PUZZLE_SETTING_DUPLICATE_DIGITS_VISIBLE_DEFAULT)
					.putBoolean(PUZZLE_SETTING_MAYBES_DISPLAYED_IN_GRID,
							PUZZLE_SETTING_MAYBES_DISPLAYED_IN_GRID_DEFAULT)
					.putBoolean(PUZZLE_SETTING_TIMER_VISIBLE,
							PUZZLE_SETTING_TIMER_VISIBLE_DEFAULT)
					.putString(PUZZLE_SETTING_THEME,
							PUZZLE_SETTING_THEME_DEFAULT)
					.putBoolean(PUZZLE_SETTING_WAKE_LOCK,
							PUZZLE_SETTING_WAKE_LOCK_DEFAULT)
					.putString(ARCHIVE_SIZE_FILTER_LAST_VALUE,
							ARCHIVE_SIZE_FILTER_LAST_VALUE_DEFAULT)
					.putString(ARCHIVE_STATUS_FILTER_LAST_VALUE,
							ARCHIVE_STATUS_FILTER_LAST_VALUE_DEFAULT)
					.putInt(ARCHIVE_GRID_LAST_SHOWED,
							ARCHIVE_GRID_LAST_SHOWED_DEFAULT)
					.putBoolean(PUZZLE_SETTING_COLORED_DIGITS,
							PUZZLE_SETTING_COLORED_DIGITS_DEFAULT)
					.putString(
							STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES,
							STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES_DEFAULT)
					.putBoolean(PUZZLE_SETTING_FULL_SCREEN,
							PUZZLE_SETTING_FULL_SCREEN_DEFAULT)
					.putInt(PUZZLE_INPUT_MODE_CHANGED_COUNTER,
							PUZZLE_INPUT_MODE_CHANGED_COUNTER_DEFAULT)
					.putString(PUZZLE_PARAMETER_COMPLEXITY,
							PUZZLE_PARAMETER_COMPLEXITY_DEFAULT)
					.putBoolean(PUZZLE_PARAMETER_OPERATORS_VISIBLE,
							PUZZLE_PARAMETER_OPERATORS_VISIBLE_DEFAULT)
					.putInt(PUZZLE_PARAMETER_SIZE,
							PUZZLE_PARAMETER_SIZE_DEFAULT)
					.putBoolean(ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE,
							ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT)
					.putBoolean(ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE,
							ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT)
					.putBoolean(STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE,
							STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT)
					.putBoolean(STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE,
							STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT)
					.putBoolean(ARCHIVE_SETTING_STATUS_FILTER_VISIBLE,
							ARCHIVE_SETTING_STATUS_FILTER_VISIBLE_DEFAULT)
					.putBoolean(ARCHIVE_SETTING_SIZE_FILTER_VISIBLE,
							ARCHIVE_SETTING_SIZE_FILTER_VISIBLE_DEFAULT)
					.putInt(SWIPE_INVALID_MOTION_COUNTER,
							SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_VALID_MOTION_COUNTER,
							SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_1_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_2_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_3_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_4_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_5_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_6_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_7_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_8_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(SWIPE_DIGIT_9_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT)
					.putInt(STATISTICS_TAB_LAST_SHOWED,
							STATISTICS_TAB_LAST_SHOWED_DEFAULT)
					.putString(PUZZLE_SETTING_OUTER_SWIPE_CIRCLE,
							PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_DEFAULT)
					.putInt(PUZZLE_INPUT_MODE_COPY_COUNTER,
							PUZZLE_INPUT_MODE_COPY_COUNTER_DEFAULT)
					.putString(PUZZLE_SETTING_INPUT_METHOD,
							PUZZLE_SETTING_INPUT_METHOD_DEFAULT)
					.putString(PUZZLE_INPUT_MODE_LAST_USED,
							PUZZLE_INPUT_MODE_LAST_USED_DEFAULT)
					.putBoolean(PUZZLE_INPUT_MODE_COPY_ENABLED,
							PUZZLE_INPUT_MODE_COPY_ENABLED_DEFAULT);
		}
		if (previousInstalledVersion < 586 && currentVersion >= 586) {
			prefeditor
					.putBoolean(
							PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE,
							PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE_DEFAULT);
		}
		if (previousInstalledVersion < 587 && currentVersion >= 587) {
			prefeditor.putInt(LEADERBOARD_TAB_LAST_SHOWED,
					LEADERBOARD_TAB_LAST_SHOWED_DEFAULT);
			prefeditor.putBoolean(LEADERBOARD_ALL_INITIALIZED,
					LEADERBOARD_ALL_INITIALIZED_DEFAULT);
			prefeditor.putString(LEADERBOARD_FILTER_LAST_VALUE,
					LEADERBOARD_FILTER_LAST_VALUE_DEFAULT.toString());
		}

		// Save
		prefeditor.putInt(APP_CURRENT_VERSION, currentVersion);
		prefeditor.commit();
	}

	/**
	 * Checks whether the timer should be displayed.
	 * 
	 * @return True in case the timer should be displayed. False otherwise.
	 */
	public boolean isTimerVisible() {
		return mSharedPreferences.getBoolean(PUZZLE_SETTING_TIMER_VISIBLE,
				PUZZLE_SETTING_TIMER_VISIBLE_DEFAULT);
	}

	/**
	 * Gets the current theme.
	 * 
	 * @return The current theme.
	 */
	public Painter.GridTheme getTheme() {
		String theme = mSharedPreferences.getString(PUZZLE_SETTING_THEME,
				PUZZLE_SETTING_THEME_DEFAULT);

		if (theme.equals(PUZZLE_SETTING_THEME_DARK)) {
			return GridTheme.DARK;
		} else {
			return GridTheme.LIGHT;
		}
	}

	/**
	 * Checks whether redundant possible values in the same column or row should
	 * be removed automatically.
	 * 
	 * @return True in case redundant possible values in the same column or row
	 *         should be removed automatically. False otherwise.
	 */
	public boolean isPuzzleSettingClearMaybesEnabled() {
		return mSharedPreferences.getBoolean(PUZZLE_SETTING_CLEAR_MAYBES,
				PUZZLE_SETTING_CLEAR_MAYBES_DEFAULT);
	}

	/**
	 * Gets the version number of the currently installed version of the app.
	 * 
	 * @return The version number of the currently installed version of the app.
	 */
	public int getCurrentInstalledVersion() {
		return mSharedPreferences.getInt(APP_CURRENT_VERSION,
				APP_CURRENT_VERSION_DEFAULT);
	}

	/**
	 * Checks whether it should be prevented that the screen get locked while
	 * playing.
	 * 
	 * @return True in case it should be prevented that the screen get locked
	 *         while playing.
	 */
	public boolean isWakeLockEnabled() {
		return mSharedPreferences.getBoolean(PUZZLE_SETTING_WAKE_LOCK,
				PUZZLE_SETTING_WAKE_LOCK_DEFAULT);
	}

	/**
	 * Checks whether the sounds effects are enabled.
	 * 
	 * @return True in the sounds effects are enabled. False otherwise.
	 */
	public boolean isPlaySoundEffectEnabled() {
		return mSharedPreferences.getBoolean(PUZZLE_SETTING_PLAY_SOUND_EFFECTS,
				PUZZLE_SETTING_PLAY_SOUND_EFFECTS_DEFAULT);
	}

	/**
	 * Mark a tip so it will not be displayed again.
	 * 
	 * @param tip
	 *            The name of the tip.
	 */
	public void setTipDoNotDisplayAgain(String tip) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(
				TipDialog.getPreferenceStringDisplayTipAgain(tip), false);
		prefeditor.apply();
	}

	/**
	 * Check whether this tip will be shown. This method should only be called
	 * by the Tip-classes only. Use method toBeDisplayed of a specific Tip-class
	 * to determine whether a tip should be shown.
	 * 
	 * @param tip
	 *            The name of the tip.
	 * @return True in case the tip has to be shown. False otherwise.
	 */
	public boolean getTipDisplayAgain(String tip) {
		return mSharedPreferences.getBoolean(
				TipDialog.getPreferenceStringDisplayTipAgain(tip), true);
	}

	/**
	 * Get the time at which the given tip was last displayed.
	 * 
	 * @param tip
	 *            The name of the tip.
	 * @return The time at which the tip was last displayed.
	 */
	public long getTipLastDisplayTime(String tip) {
		return mSharedPreferences.getLong(
				TipDialog.getPreferenceStringLastDisplayTime(tip), 0L);
	}

	/**
	 * Set the time at which the given tip was last displayed.
	 * 
	 * @param tip
	 *            The name of the tip.
	 * @param time
	 *            The time at which the tip was last displayed.
	 */
	public void setTipLastDisplayTime(String tip, long time) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putLong(TipDialog.getPreferenceStringLastDisplayTime(tip),
				time);
		prefeditor.apply();
	}

	/**
	 * Gets all shared preferences. Should not be used if possible.
	 * 
	 * @return A mapset of all shared preferences.
	 */
	public Map<String, ?> getAllSharedPreferences() {
		return mSharedPreferences.getAll();
	}

	/**
	 * Checks whether duplicate digits should be shown in the grid.
	 * 
	 * @return True in case duplicate digits should be shown in the grid. False
	 *         otherwise.
	 */
	public boolean isDuplicateDigitHighlightVisible() {
		return mSharedPreferences.getBoolean(
				PUZZLE_SETTING_DUPLICATE_DIGITS_VISIBLE,
				PUZZLE_SETTING_DUPLICATE_DIGITS_VISIBLE_DEFAULT);
	}

	/**
	 * Checks whether maybes should be shown in the same pattern as the digit
	 * buttons.
	 * 
	 * @return True in case maybes should be shown in the same pattern as the
	 *         digit buttons. False otherwise.
	 */
	public boolean isMaybesDisplayedInGrid() {
		return mSharedPreferences.getBoolean(
				PUZZLE_SETTING_MAYBES_DISPLAYED_IN_GRID,
				PUZZLE_SETTING_MAYBES_DISPLAYED_IN_GRID_DEFAULT);
	}

	/**
	 * Checks whether cages having bad math should be highlighted.
	 * 
	 * @return True in case cages having bad math should be highlighted. False
	 *         otherwise.
	 */
	public boolean isBadCageMathHighlightVisible() {
		return mSharedPreferences.getBoolean(
				PUZZLE_SETTING_BAD_CAGE_MATHS_VISIBLE,
				PUZZLE_SETTING_BAD_CAGE_MATHS_VISIBLE_DEFAULT);
	}

	/**
	 * Checks whether a description has to be shown below a chart in the
	 * statistics activity.
	 * 
	 * @return True in case the charts descriptions have to be shown. False
	 *         otherwise.
	 */
	public boolean isStatisticsChartDescriptionVisible() {
		return mSharedPreferences.getBoolean(
				STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE,
				STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT);
	}

	/**
	 * Checks whether a description has to be shown below a chart in the archive
	 * activity.
	 * 
	 * @return True in case the charts descriptions have to be shown. False
	 *         otherwise.
	 */
	public boolean isArchiveChartDescriptionVisible() {
		return mSharedPreferences.getBoolean(
				ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE,
				ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT);
	}

	/**
	 * Checks whether the filter status should be shown in the archive action
	 * bar.
	 * 
	 * @return True in case the filter status should be shown in the archive
	 *         action bar. False otherwise.
	 */
	public boolean isArchiveStatusFilterVisible() {
		return mSharedPreferences.getBoolean(
				ARCHIVE_SETTING_STATUS_FILTER_VISIBLE,
				ARCHIVE_SETTING_STATUS_FILTER_VISIBLE_DEFAULT);
	}

	/**
	 * Checks whether the size status should be shown in the archive action bar.
	 * 
	 * @return True in case the size filter should be shown in the archive
	 *         action bar. False otherwise.
	 */
	public boolean isArchiveSizeFilterVisible() {
		return mSharedPreferences.getBoolean(
				ARCHIVE_SETTING_SIZE_FILTER_VISIBLE,
				ARCHIVE_SETTING_SIZE_FILTER_VISIBLE_DEFAULT);
	}

	/**
	 * Checks whether the archive is visible.
	 * 
	 * @return True in case the archive is visible. False otherwise.
	 */
	public boolean isArchiveAvailable() {
		return mSharedPreferences.getBoolean(ARCHIVE_AVAILABLE,
				ARCHIVE_AVAILABLE_DEFAULT);
	}

	/**
	 * Enables the archive.
	 */
	public void setArchiveVisible() {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(ARCHIVE_AVAILABLE, true);
		prefeditor.apply();
	}

	/**
	 * Checks whether the statistics are visible.
	 * 
	 * @return True in case the statistics are visible. False otherwise.
	 */
	public boolean isStatisticsAvailable() {
		return mSharedPreferences.getBoolean(STATISTICS_AVAILABLE,
				STATISTICS_AVAILABLE_DEFAULT);
	}

	/**
	 * Enables the statistics.
	 */
	public void setStatisticsAvailable() {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(STATISTICS_AVAILABLE, true);
		prefeditor.apply();
	}

	/**
	 * Get the last value used for the status filter in the archive.
	 * 
	 * @return The last value used for the status filter in the archive.
	 */
	public StatusFilter getArchiveStatusFilterLastValueUsed() {
		return StatusFilter.valueOf(mSharedPreferences.getString(
				ARCHIVE_STATUS_FILTER_LAST_VALUE,
				ARCHIVE_STATUS_FILTER_LAST_VALUE_DEFAULT));
	}

	/**
	 * Set the last value used for the status filter in the archive.
	 * 
	 * @param statusFilter
	 *            The status filter which has to be saved as last selected in
	 *            the archive.
	 */
	public void setArchiveStatusFilterLastValueUsed(StatusFilter statusFilter) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putString(ARCHIVE_STATUS_FILTER_LAST_VALUE,
				statusFilter.toString());
		prefeditor.apply();
	}

	/**
	 * Get the last value used for the size filter in the archive.
	 * 
	 * @return The last value used for the size filter in the archive.
	 */
	public SizeFilter getArchiveSizeFilterLastValueUsed() {
		return SizeFilter.valueOf(mSharedPreferences.getString(
				ARCHIVE_SIZE_FILTER_LAST_VALUE,
				ARCHIVE_SIZE_FILTER_LAST_VALUE_DEFAULT));
	}

	/**
	 * Set the last value used for the size filter in the archive.
	 * 
	 * @param sizeFilter
	 *            The size filter which has to be saved as last selected in the
	 *            archive.
	 */
	public void setArchiveSizeFilterLastValueUsed(SizeFilter sizeFilter) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putString(ARCHIVE_SIZE_FILTER_LAST_VALUE,
				sizeFilter.toString());
		prefeditor.apply();
	}

	/**
	 * Get the id of the grid which was last shown in the archive.
	 * 
	 * @return The id of the grid which was last shown in the archive.
	 */
	public int getArchiveGridIdLastShowed() {
		return mSharedPreferences.getInt(ARCHIVE_GRID_LAST_SHOWED,
				ARCHIVE_GRID_LAST_SHOWED_DEFAULT);
	}

	/**
	 * Set the id of the grid which is currently is shown when the archive is
	 * closed.
	 * 
	 * @return The last value used for the size filter in the archive.
	 */
	public void setArchiveGridIdLastShowed(int gridId) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putInt(ARCHIVE_GRID_LAST_SHOWED, gridId);
		prefeditor.apply();
	}

	/**
	 * Initializes the counters with current values as stored in the
	 * preferences.
	 */
	private void initializeCounters() {
		if (counters == null) {
			counters = new int[13];
			counters[PUZZLE_INPUT_MODE_CHANGED_COUNTER_ID] = mSharedPreferences
					.getInt(PUZZLE_INPUT_MODE_CHANGED_COUNTER,
							PUZZLE_INPUT_MODE_CHANGED_COUNTER_DEFAULT);
			counters[PUZZLE_INPUT_MODE_COPY_COUNTER_ID] = mSharedPreferences
					.getInt(PUZZLE_INPUT_MODE_COPY_COUNTER,
							PUZZLE_INPUT_MODE_CHANGED_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_1_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_1_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_2_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_2_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_3_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_3_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_4_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_4_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_5_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_5_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_6_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_6_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_7_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_7_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_8_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_8_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_DIGIT_9_COUNTER_ID] = mSharedPreferences.getInt(
					SWIPE_DIGIT_9_COUNTER, SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_VALID_MOTION_COUNTER_ID] = mSharedPreferences
					.getInt(SWIPE_VALID_MOTION_COUNTER,
							SWIPE_DIGIT_COUNTER_DEFAULT);
			counters[SWIPE_INVALID_MOTION_COUNTER_ID] = mSharedPreferences
					.getInt(SWIPE_INVALID_MOTION_COUNTER,
							SWIPE_DIGIT_COUNTER_DEFAULT);
		}
	}

	/**
	 * Increase the current value of a preference counter with 1 occurrence.
	 * 
	 * <b>Note:</b> For performance reasons the counter preferences are not
	 * updated on calling this method. Call commitCounters() to commit counters
	 * to storage.
	 * 
	 * @param counter_id
	 *            The id of the preferences counter.
	 * @return The number of occurrence for this counter (after being updated).
	 */
	private int increaseCounter(int counter_id) {
		if (counters == null) {
			initializeCounters();
		}
		return counters[counter_id]++;
	}

	/**
	 * Get the number of valid swipe motions which have successfully been
	 * completed.
	 * 
	 * @return The number of valid swipe motions which have successfully been
	 *         completed.
	 */
	public int getSwipeValidMotionCounter() {
		if (counters == null) {
			initializeCounters();
		}
		return counters[SWIPE_VALID_MOTION_COUNTER_ID];
	}

	/**
	 * Get the preference name associated with the discovery of the given digit.
	 * 
	 * @param digit
	 *            The digit for which the preference has to be determined.
	 * @return The preference name associated with the discovery of the given
	 *         digit.
	 */
	private int getSwipeDigitDiscoveredPreferenceName(int digit) {
		switch (digit) {
		case 1:
			return SWIPE_DIGIT_1_COUNTER_ID;
		case 2:
			return SWIPE_DIGIT_2_COUNTER_ID;
		case 3:
			return SWIPE_DIGIT_3_COUNTER_ID;
		case 4:
			return SWIPE_DIGIT_4_COUNTER_ID;
		case 5:
			return SWIPE_DIGIT_5_COUNTER_ID;
		case 6:
			return SWIPE_DIGIT_6_COUNTER_ID;
		case 7:
			return SWIPE_DIGIT_7_COUNTER_ID;
		case 8:
			return SWIPE_DIGIT_8_COUNTER_ID;
		case 9:
			return SWIPE_DIGIT_9_COUNTER_ID;
		}
		return -1;
	}

	/**
	 * Get the number of times a swipe motion for the given digit has been
	 * successfully completed.
	 * 
	 * @param digit
	 *            The digit for which the counter has to be determined.
	 * @return The number of times a swipe motion for the given digit has been
	 *         successfully completed.
	 */
	public int getSwipeMotionCounter(int digit) {
		if (counters == null) {
			initializeCounters();
		}
		return counters[getSwipeDigitDiscoveredPreferenceName(digit)];
	}

	/**
	 * Increase the number of times a swipe motion for the given digit has been
	 * succesfully completed. Also the total number of completed swipe motions
	 * is increased.
	 * 
	 * @param digit
	 *            The digit for which the counter has to be determined.
	 * @return The (updated) number of times a swipe motion for the given digit
	 *         has been successfully completed.
	 */
	public int increaseSwipeValidMotionCounter(int digit) {
		increaseCounter(SWIPE_VALID_MOTION_COUNTER_ID);
		return increaseCounter(getSwipeDigitDiscoveredPreferenceName(digit));
	}

	/**
	 * Increase the number of times an invalid swipe motion has been completed.
	 * 
	 * @return The (updated) number of times an invalid swipe motion has been
	 *         completed.
	 */
	public int increaseSwipeInvalidMotionCounter() {
		return increaseCounter(SWIPE_INVALID_MOTION_COUNTER_ID);
	}

	/**
	 * Get the maximum number of games which should be shown in a
	 * elapsed-time-chart.
	 * 
	 * @return The maximum number of games which should be shown in a
	 *         elapsed-time-chart.
	 */
	public int getStatisticsSettingElapsedTimeChartMaximumGames() {
		return Integer.parseInt(mSharedPreferences.getString(
				STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES,
				STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES_DEFAULT));
	}

	/**
	 * Increase the number of times the input mode has been changed from normal
	 * to maybe or vice versa.
	 * 
	 * @return The (updated) number of times the input mode has been changed
	 *         from normal to maybe or vice versa.
	 */
	public int increaseInputModeChangedCounter() {
		return increaseCounter(PUZZLE_INPUT_MODE_CHANGED_COUNTER_ID);
	}

	/**
	 * Increase the number of times the input mode has set to copy mode.
	 * 
	 * @return The (updated) number of times the input mode has been set to copy
	 *         mode.
	 */
	public int increaseInputModeCopyCounter() {
		return increaseCounter(PUZZLE_INPUT_MODE_COPY_COUNTER_ID);
	}

	/**
	 * Commit all counter values to the preferences.
	 */
	public void commitCounters() {
		if (counters != null) {
			Editor prefeditor = mSharedPreferences.edit();
			prefeditor.putInt(PUZZLE_INPUT_MODE_CHANGED_COUNTER,
					counters[PUZZLE_INPUT_MODE_CHANGED_COUNTER_ID]);
			prefeditor.putInt(PUZZLE_INPUT_MODE_COPY_COUNTER,
					counters[PUZZLE_INPUT_MODE_COPY_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_1_COUNTER,
					counters[SWIPE_DIGIT_1_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_2_COUNTER,
					counters[SWIPE_DIGIT_2_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_3_COUNTER,
					counters[SWIPE_DIGIT_3_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_4_COUNTER,
					counters[SWIPE_DIGIT_4_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_5_COUNTER,
					counters[SWIPE_DIGIT_5_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_6_COUNTER,
					counters[SWIPE_DIGIT_6_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_7_COUNTER,
					counters[SWIPE_DIGIT_7_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_8_COUNTER,
					counters[SWIPE_DIGIT_8_COUNTER_ID]);
			prefeditor.putInt(SWIPE_DIGIT_9_COUNTER,
					counters[SWIPE_DIGIT_9_COUNTER_ID]);
			prefeditor.putInt(SWIPE_VALID_MOTION_COUNTER,
					counters[SWIPE_VALID_MOTION_COUNTER_ID]);
			prefeditor.putInt(SWIPE_INVALID_MOTION_COUNTER,
					counters[SWIPE_INVALID_MOTION_COUNTER_ID]);
			prefeditor.commit();
		}
	}

	/**
	 * Gets the number of times the input mode has been changed.
	 * 
	 * @return The number of times the input mode has been changed.
	 */
	public int getInputModeChangedCounter() {
		return mSharedPreferences.getInt(PUZZLE_INPUT_MODE_CHANGED_COUNTER,
				PUZZLE_INPUT_MODE_CHANGED_COUNTER_DEFAULT);
	}

	/**
	 * Gets the number of times the input mode has set to copy mode.
	 * 
	 * @return The number of times the input mode has been set top copy mode.
	 */
	public int getInputModeCopyCounter() {
		return mSharedPreferences.getInt(PUZZLE_INPUT_MODE_COPY_COUNTER,
				PUZZLE_INPUT_MODE_COPY_COUNTER_DEFAULT);
	}

	/**
	 * Get the complexity of the puzzle which was last generated.
	 * 
	 * @return The complexity of the puzzle which was last generated.
	 */
	public PuzzleComplexity getPuzzleParameterComplexity() {
		return PuzzleComplexity.valueOf(mSharedPreferences.getString(
				PUZZLE_PARAMETER_COMPLEXITY,
				PUZZLE_PARAMETER_COMPLEXITY_DEFAULT));
	}

	/**
	 * Set the complexity of the puzzle which was last generated. closed.
	 * 
	 * @param puzzleComplexity
	 *            The complexity of the puzzle.
	 */
	public void setPuzzleParameterComplexity(PuzzleComplexity puzzleComplexity) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putString(PUZZLE_PARAMETER_COMPLEXITY,
				puzzleComplexity.toString());
		prefeditor.apply();
	}

	/**
	 * Get the setting for showing or hiding the operators for the puzzle which
	 * was last generated.
	 * 
	 * @return True in case the operators were hidden in the last puzzle
	 *         generated.
	 */
	public boolean getPuzzleParameterOperatorsVisible() {
		return mSharedPreferences.getBoolean(
				PUZZLE_PARAMETER_OPERATORS_VISIBLE,
				PUZZLE_PARAMETER_OPERATORS_VISIBLE_DEFAULT);
	}

	/**
	 * Set the setting for showing or hiding the operators for the puzzle which
	 * was last generated.
	 */
	public void setPuzzleParameterOperatorsVisible(boolean visible) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(PUZZLE_PARAMETER_OPERATORS_VISIBLE, visible);
		prefeditor.apply();
	}

	/**
	 * Get the size of the puzzle which was last generated.
	 * 
	 * @return The size of the puzzle which was last generated.
	 */
	public int getPuzzleParameterSize() {
		return mSharedPreferences.getInt(PUZZLE_PARAMETER_SIZE,
				PUZZLE_PARAMETER_SIZE_DEFAULT);
	}

	/**
	 * Set the size of the puzzle which was last generated. closed.
	 */
	public void setPuzzleParameterSize(int gridSize) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putInt(PUZZLE_PARAMETER_SIZE, gridSize);
		prefeditor.apply();
	}

	/**
	 * Checks whether the full screen preference is enabled.
	 * 
	 * @return True in case the activity should request full screen mode while
	 *         playing.
	 */
	public boolean isFullScreenEnabled() {
		return mSharedPreferences.getBoolean(PUZZLE_SETTING_FULL_SCREEN,
				PUZZLE_SETTING_FULL_SCREEN_DEFAULT);
	}

	/**
	 * Checks whether maybes and definitive digits should be displayed in
	 * distinct colors.
	 * 
	 * @return True in case distinct colors have to be used for displaying
	 *         maybes and definitive digits. False in case the same color is
	 *         used for both type of digits.
	 */
	public boolean isColoredDigitsVisible() {
		return mSharedPreferences.getBoolean(PUZZLE_SETTING_COLORED_DIGITS,
				PUZZLE_SETTING_COLORED_DIGITS_DEFAULT);
	}

	/**
	 * Get the tab which was displayed last time the statistics were displayed.
	 * 
	 * @return The tab which was displayed last time the statistics were
	 *         displayed.
	 */
	public int getStatisticsTabLastDisplayed() {
		return mSharedPreferences.getInt(STATISTICS_TAB_LAST_SHOWED,
				STATISTICS_TAB_LAST_SHOWED_DEFAULT);
	}

	/**
	 * Set the tab which was displayed last time the statistics were viewed.
	 */
	public void setStatisticsTabLastDisplayed(int tab) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putInt(STATISTICS_TAB_LAST_SHOWED, tab);
		prefeditor.apply();
	}

	/**
	 * Checks whether the outer swipe circle is visible for the given grid size.
	 * 
	 * @param gridSize
	 *            The grid size for which it has to be checked whether the outer
	 *            swipe circle should be shown.
	 * @return True in case the outer swipe circle should be displayed. False
	 *         otherwise.
	 */
	public boolean isOuterSwipeCircleVisible(int gridSize) {
		String puzzleSettingOuterSwipeCircle = mSharedPreferences.getString(
				PUZZLE_SETTING_OUTER_SWIPE_CIRCLE,
				PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_DEFAULT);
		int minGridSize = (puzzleSettingOuterSwipeCircle
				.equals(PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_NEVER_VISIBLE) ? Integer.MAX_VALUE
				: Integer.valueOf(puzzleSettingOuterSwipeCircle));

		return (gridSize >= minGridSize);
	}

	/**
	 * Checks whether the outer swipe circle is never visible.
	 * 
	 * @return True in case the outer swipe circle is never visible. False
	 *         otherwise.
	 */
	public boolean isOuterSwipeCircleNeverVisible() {
		return mSharedPreferences.getString(PUZZLE_SETTING_OUTER_SWIPE_CIRCLE,
				PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_DEFAULT).equals(
				PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_NEVER_VISIBLE);
	}

	/**
	 * Gets (string) value of option outer swipe circle.
	 * 
	 * @return The (string) value of option outer swipe circle.
	 */
	public String getOuterSwipeCircleVisibility() {
		return mSharedPreferences.getString(PUZZLE_SETTING_OUTER_SWIPE_CIRCLE,
				PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_DEFAULT);
	}

	/**
	 * Gets the input method.
	 */
	public PuzzleSettingInputMethod getDigitInputMethod() {
		return PuzzleSettingInputMethod.valueOf(mSharedPreferences.getString(
				PUZZLE_SETTING_INPUT_METHOD,
				PUZZLE_SETTING_INPUT_METHOD_DEFAULT).toUpperCase(
				Locale.getDefault()));
	}

	/**
	 * Sets the input method.
	 */
	public void setDigitInputMethod(boolean enableSwipe, boolean enableButtons) {
		Editor prefeditor = mSharedPreferences.edit();
		if (enableSwipe == true && enableButtons == false) {
			prefeditor.putString(PUZZLE_SETTING_INPUT_METHOD,
					PuzzleSettingInputMethod.SWIPE_ONLY.toString());
		} else if (enableSwipe == false && enableButtons == true) {
			prefeditor.putString(PUZZLE_SETTING_INPUT_METHOD,
					PuzzleSettingInputMethod.BUTTONS_ONLY.toString());
		} else {
			prefeditor.putString(PUZZLE_SETTING_INPUT_METHOD,
					PuzzleSettingInputMethod.SWIPE_AND_BUTTONS.toString());
		}
		prefeditor.apply();
	}

	/**
	 * Sets the current grid input mode.
	 * 
	 * @param copyModeEnabled
	 *            True in case the copy mode is enabled.
	 * @param gridInputMode
	 *            The current input mode if copy mode is disabled. The previous
	 *            input mode before copy mode in case copy mode is enabled.
	 */
	public void setGridInputMode(boolean copyModeEnabled,
			GridInputMode gridInputMode) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putString(PUZZLE_INPUT_MODE_LAST_USED,
				gridInputMode.toString());
		prefeditor.putBoolean(PUZZLE_INPUT_MODE_COPY_ENABLED, copyModeEnabled);
		prefeditor.apply();
	}

	/**
	 * Get the last used grid input mode.
	 * 
	 * @return The last used grid input mode.
	 */
	public GridInputMode getGridInputMode() {
		return GridInputMode.valueOf(mSharedPreferences.getString(
				PUZZLE_INPUT_MODE_LAST_USED,
				PUZZLE_INPUT_MODE_LAST_USED_DEFAULT));
	}

	/**
	 * Checks whether the copy mode was enabled.
	 * 
	 * @return True in case the copy mode was enabled.
	 */
	public boolean isGridInputModeCopyEnabled() {
		return mSharedPreferences.getBoolean(PUZZLE_INPUT_MODE_COPY_ENABLED,
				PUZZLE_INPUT_MODE_COPY_ENABLED_DEFAULT);
	}

	/**
	 * Checks whether the checkbox "Hide till next top score is achieved"
	 * checked by default.
	 * 
	 * @return True in case checkbox is enabled by default. False otherwise.
	 */
	public boolean isHideTillNextTopScoreAchievedChecked() {
		return mSharedPreferences.getBoolean(
				PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE,
				PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE_DEFAULT);
	}

	/**
	 * Set whether the checkbox "Hide till next top score is achieved" should be
	 * checked by default.
	 * 
	 * @param checked
	 *            True in case it is checked by default. False otherwise.
	 */
	public void setHideTillNextTopScoreAchievedChecked(boolean checked) {
		mSharedPreferences
				.edit()
				.putBoolean(
						PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE,
						checked).apply();
	}

	/**
	 * Get the tab which was displayed last time the leaderboards were
	 * displayed.
	 * 
	 * @return The tab which was displayed last time the leaderboards were
	 *         displayed.
	 */
	public int getLeaderboardsTabLastDisplayed() {
		return mSharedPreferences.getInt(LEADERBOARD_TAB_LAST_SHOWED,
				LEADERBOARD_TAB_LAST_SHOWED_DEFAULT);
	}

	/**
	 * Set the tab which was displayed last time the leaderboards were viewed.
	 */
	public void setLeaderboardsTabLastDisplayed(int tab) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putInt(LEADERBOARD_TAB_LAST_SHOWED, tab);
		prefeditor.apply();
	}

	/**
	 * Checks whether all leaderboards have been initialized.
	 * 
	 * @return True in case all leaderboards have been initialized. False
	 *         otherwise.
	 */
	public boolean isLeaderboardsInitialized() {
		return mSharedPreferences.getBoolean(LEADERBOARD_ALL_INITIALIZED,
				LEADERBOARD_ALL_INITIALIZED_DEFAULT);
	}

	/**
	 * Set leaderboard initialization to initialized.
	 */
	public void setLeaderboardsInitialized() {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(LEADERBOARD_ALL_INITIALIZED, true);
		prefeditor.apply();
	}

	/**
	 * Get the last value used for the leaderboard filter in the leaderboards
	 * overview.
	 * 
	 * @return The last value used for the leaderboard filter in the
	 *         leaderboards overview.
	 */
	public LeaderboardFilter getLeaderboardFilterLastValueUsed() {
		return LeaderboardFilter.valueOf(mSharedPreferences.getString(
				LEADERBOARD_FILTER_LAST_VALUE,
				LEADERBOARD_FILTER_LAST_VALUE_DEFAULT));
	}

	/**
	 * Set the last value used for the leaderboard filter in the leaderboards
	 * overview.
	 * 
	 * @param leaderboardFilter
	 *            The leaderboard filter which was last used.
	 */
	public void setLeaderboardFilterLastValueUsed(
			LeaderboardFilter leaderboardFilter) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putString(LEADERBOARD_FILTER_LAST_VALUE,
				leaderboardFilter.toString());
		prefeditor.apply();
	}
}