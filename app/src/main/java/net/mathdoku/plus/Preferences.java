package net.mathdoku.plus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.puzzle.ui.GridInputMode;
import net.mathdoku.plus.leaderboard.ui.LeaderboardFragmentActivity.LeaderboardFilter;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.painter.Painter.GridTheme;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter.SizeFilter;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter.StatusFilter;
import net.mathdoku.plus.tip.TipDialog;
import net.mathdoku.plus.util.SingletonInstanceNotInstantiated;

import java.util.Locale;
import java.util.Map;

public class Preferences {
	@SuppressWarnings("unused")
	private static final String TAG = Preferences.class.getName();

	// Singleton reference to the preferences object
	private static Preferences mPreferencesSingletonInstance = null;

	// The Objects Creator is responsible for creating all new objects needed by
	// this class. For unit testing purposes the default create methods can be
	// overridden if needed.
	public static class ObjectsCreator {
		public Preferences createPreferences(Context context) {
			return new Preferences(context);
		}
	}

	// Actual preferences
	public final SharedPreferences mSharedPreferences;

	// Global APP preferences
	private static final String APP_CURRENT_VERSION = "app_current_version";
	private static final int APP_CURRENT_VERSION_DEFAULT = -1;

	// Archive preferences
	public static final String ARCHIVE_AVAILABLE = "archive_available";
	private static final boolean ARCHIVE_AVAILABLE_DEFAULT = false;

	private static final String ARCHIVE_GRID_LAST_SHOWED = "archive_grid_last_showed";
	private static final int ARCHIVE_GRID_LAST_SHOWED_DEFAULT = -1;

	private static final String ARCHIVE_SIZE_FILTER_LAST_VALUE = "archive_size_filter_last_value";
	private static final String ARCHIVE_SIZE_FILTER_LAST_VALUE_DEFAULT = SizeFilter.ALL
			.toString();

	private static final String ARCHIVE_STATUS_FILTER_LAST_VALUE = "archive_status_filter_last_value";
	private static final String ARCHIVE_STATUS_FILTER_LAST_VALUE_DEFAULT = StatusFilter.ALL
			.toString();

	public static final String ARCHIVE_SETTING_SIZE_FILTER_VISIBLE = "archive_setting_size_filter_size_visible";
	private static final boolean ARCHIVE_SETTING_SIZE_FILTER_VISIBLE_DEFAULT = false;

	public static final String ARCHIVE_SETTING_STATUS_FILTER_VISIBLE = "archive_setting_status_filter_visible";
	private static final boolean ARCHIVE_SETTING_STATUS_FILTER_VISIBLE_DEFAULT = true;

	public static final String ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE = "archive_setting_chart_description_visible";
	private static final boolean ARCHIVE_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT = true;

	// Leaderboard settings
	private static final String LEADERBOARD_ALL_INITIALIZED = "leaderboard_all_initialized";
	private static final boolean LEADERBOARD_ALL_INITIALIZED_DEFAULT = false;

	private static final String LEADERBOARD_FILTER_LAST_VALUE = "leaderboard_filter_last_value";
	private static final String LEADERBOARD_FILTER_LAST_VALUE_DEFAULT = LeaderboardFilter.ALL_LEADERBOARDS
			.toString();

	private static final String LEADERBOARD_TAB_LAST_SHOWED = "leaderboard_tab_last_showed";
	private static final int LEADERBOARD_TAB_LAST_SHOWED_DEFAULT = 0;

	private static final String LEADERBOARD_DETAILS_VIEWED_COUNTER = "leaderboard_details_viewed_counter";
	private static final int LEADERBOARD_DETAILS_VIEWED_COUNTER_DEFAULT = 0;

	private static final String LEADERBOARD_GAMES_CREATED_COUNTER = "leaderboard_games_created_counter";
	private static final int LEADERBOARD_GAMES_CREATED_COUNTER_DEFAULT = 0;

	private static final String LEADERBOARD_OVERVIEW_VIEWED_COUNTER = "leaderboard_overview_viewed";
	private static final int LEADERBOARD_OVERVIEW_VIEWED_COUNTER_DEFAULT = 0;

	private static final String PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE = "puzzle_hide_google_plus_sign_in_till_next_top_score";
	private static final boolean PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE_DEFAULT = false;

	// Puzzle input mode settings
	private static final String PUZZLE_INPUT_MODE_CHANGED_COUNTER = "puzzle_input_mode_changed_counter";
	private static final int PUZZLE_INPUT_MODE_CHANGED_COUNTER_DEFAULT = 0;

	private static final String PUZZLE_INPUT_MODE_COPY_COUNTER = "puzzle_input_mode_copy_counter";
	private static final int PUZZLE_INPUT_MODE_COPY_COUNTER_DEFAULT = 0;

	private static final String PUZZLE_INPUT_MODE_LAST_USED = "puzzle_input_mode_last_used";
	private static final String PUZZLE_INPUT_MODE_LAST_USED_DEFAULT = GridInputMode.NORMAL
			.toString();

	private static final String PUZZLE_INPUT_MODE_COPY_ENABLED = "puzzle_input_mode_copy_enabled";
	private static final boolean PUZZLE_INPUT_MODE_COPY_ENABLED_DEFAULT = false;

	// Puzzle parameters settings to be set as default values for next game
	private static final String PUZZLE_PARAMETER_COMPLEXITY = "puzzle_parameter_complexity";
	private static final String PUZZLE_PARAMETER_COMPLEXITY_DEFAULT = PuzzleComplexity.VERY_EASY
			.toString();

	private static final String PUZZLE_PARAMETER_OPERATORS_VISIBLE = "puzzle_parameter_operators_visible";
	private static final boolean PUZZLE_PARAMETER_OPERATORS_VISIBLE_DEFAULT = true;

	private static final String PUZZLE_PARAMETER_SIZE = "puzzle_parameter_size";
	private static final int PUZZLE_PARAMETER_SIZE_DEFAULT = 4;

	// Puzzle setting preferences
	private static final String PUZZLE_SETTING_BAD_CAGE_MATHS_VISIBLE = "puzzle_setting_bad_cage_math_visible";
	private static final boolean PUZZLE_SETTING_BAD_CAGE_MATHS_VISIBLE_DEFAULT = true;

	private static final String PUZZLE_SETTING_CLEAR_MAYBES = "puzzle_setting_clear_maybes";
	private static final boolean PUZZLE_SETTING_CLEAR_MAYBES_DEFAULT = true;

	public static final String PUZZLE_SETTING_COLORED_DIGITS = "puzzle_setting_colored_digits";
	private static final boolean PUZZLE_SETTING_COLORED_DIGITS_DEFAULT = true;

	private static final String PUZZLE_SETTING_DUPLICATE_DIGITS_VISIBLE = "puzzle_setting_duplicate_digits_visible";
	private static final boolean PUZZLE_SETTING_DUPLICATE_DIGITS_VISIBLE_DEFAULT = true;

	public static final String PUZZLE_SETTING_FULL_SCREEN = "puzzle_setting_full_screen";
	private static final boolean PUZZLE_SETTING_FULL_SCREEN_DEFAULT = false;

	public enum PuzzleSettingInputMethod {
		SWIPE_ONLY, SWIPE_AND_BUTTONS, BUTTONS_ONLY
	}

	public static final String PUZZLE_SETTING_INPUT_METHOD = "puzzle_setting_input_method";
	private static final String PUZZLE_SETTING_INPUT_METHOD_DEFAULT = PuzzleSettingInputMethod.SWIPE_ONLY
			.toString();

	private static final String PUZZLE_SETTING_MAYBES_DISPLAYED_IN_GRID = "puzzle_setting_maybes_displayed_in_grid";
	private static final boolean PUZZLE_SETTING_MAYBES_DISPLAYED_IN_GRID_DEFAULT = true;

	public static final String PUZZLE_SETTING_OUTER_SWIPE_CIRCLE = "puzzle_setting_outer_swipe_circle";
	private static final String PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_DEFAULT = "4";
	private static final String PUZZLE_SETTING_OUTER_SWIPE_CIRCLE_NEVER_VISIBLE = Integer
			.toString(Integer.MAX_VALUE);

	private static final String PUZZLE_SETTING_PLAY_SOUND_EFFECTS = "puzzle_setting_sound_effects";
	private static final boolean PUZZLE_SETTING_PLAY_SOUND_EFFECTS_DEFAULT = true;

	public static final String PUZZLE_SETTING_THEME = "puzzle_setting_theme";
	private static final String PUZZLE_SETTING_THEME_DARK = "theme_dark";
	private static final String PUZZLE_SETTING_THEME_LIGHT = "theme_light";
	private static final String PUZZLE_SETTING_THEME_DEFAULT = PUZZLE_SETTING_THEME_LIGHT;

	private static final String PUZZLE_SETTING_TIMER_VISIBLE = "puzzle_setting_timer_visible";
	private static final boolean PUZZLE_SETTING_TIMER_VISIBLE_DEFAULT = true;

	public static final String PUZZLE_SETTING_WAKE_LOCK = "puzzle_setting_wake_lock";
	private static final boolean PUZZLE_SETTING_WAKE_LOCK_DEFAULT = true;

	// Statistics setting preferences
	public static final String STATISTICS_AVAILABLE = "statistics_available";
	private static final boolean STATISTICS_AVAILABLE_DEFAULT = false;

	public static final String STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE = "statistics_setting_chart_description_visible";
	private static final boolean STATISTICS_SETTING_CHART_DESCRIPTION_VISIBLE_DEFAULT = true;

	public static final String STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES = "statistics_setting_elapsed_time_chart_maximum_games";
	private static final String STATISTICS_SETTING_ELAPSED_TIME_CHART_MAXIMUM_GAMES_DEFAULT = Integer
			.toString(100);

	private static final String STATISTICS_TAB_LAST_SHOWED = "statistics_tab_last_showed";
	private static final int STATISTICS_TAB_LAST_SHOWED_DEFAULT = -1;

	// Swipe counters
	private static final String SWIPE_INVALID_MOTION_COUNTER = "swipe_invalid_motion_counter";
	private static final String SWIPE_VALID_MOTION_COUNTER = "swipe_valid_motion_counter";
	private static final String SWIPE_DIGIT_1_COUNTER = "swipe_digit_1_counter";
	private static final String SWIPE_DIGIT_2_COUNTER = "swipe_digit_2_counter";
	private static final String SWIPE_DIGIT_3_COUNTER = "swipe_digit_3_counter";
	private static final String SWIPE_DIGIT_4_COUNTER = "swipe_digit_4_counter";
	private static final String SWIPE_DIGIT_5_COUNTER = "swipe_digit_5_counter";
	private static final String SWIPE_DIGIT_6_COUNTER = "swipe_digit_6_counter";
	private static final String SWIPE_DIGIT_7_COUNTER = "swipe_digit_7_counter";
	private static final String SWIPE_DIGIT_8_COUNTER = "swipe_digit_8_counter";
	private static final String SWIPE_DIGIT_9_COUNTER = "swipe_digit_9_counter";
	private static final int SWIPE_DIGIT_COUNTER_DEFAULT = 0;

	// Dependent on the speed of playing, the counter preferences are often
	// updated. For this reason they are kept in memory and only committed to
	// memory on call to commitCounters.
	private static final int PUZZLE_INPUT_MODE_CHANGED_COUNTER_ID = 0;
	private static final int SWIPE_DIGIT_1_COUNTER_ID = 1;
	private static final int SWIPE_DIGIT_2_COUNTER_ID = 2;
	private static final int SWIPE_DIGIT_3_COUNTER_ID = 3;
	private static final int SWIPE_DIGIT_4_COUNTER_ID = 4;
	private static final int SWIPE_DIGIT_5_COUNTER_ID = 5;
	private static final int SWIPE_DIGIT_6_COUNTER_ID = 6;
	private static final int SWIPE_DIGIT_7_COUNTER_ID = 7;
	private static final int SWIPE_DIGIT_8_COUNTER_ID = 8;
	private static final int SWIPE_DIGIT_9_COUNTER_ID = 9;
	private static final int SWIPE_VALID_MOTION_COUNTER_ID = 10;
	private static final int SWIPE_INVALID_MOTION_COUNTER_ID = 11;
	private static final int PUZZLE_INPUT_MODE_COPY_COUNTER_ID = 12;
	private int[] counters = null;

	/**
	 * Creates a new instance of {@link Preferences}.
	 * 
	 * This object can not be instantiated directly. Use {@link #getInstance()}
	 * or {@link #getInstance(android.content.Context)} to get the singleton
	 * reference to the Preference object.
	 * 
	 * @param context
	 *            The context for which the preferences have to be determined.
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
	 * @return The context for which the preferences have to be determined.
	 */
	public static Preferences getInstance(Context context) {
		if (mPreferencesSingletonInstance == null) {
			// Only the first time this method is called, the object will be
			// created.
			mPreferencesSingletonInstance = new ObjectsCreator()
					.createPreferences(context);
		}
		return mPreferencesSingletonInstance;
	}

	/**
	 * Creates new instance of {@link net.mathdoku.plus.Preferences}. All
	 * objects in this class will be created with the given
	 * Preferences.ObjectsCreator. This method is intended for unit testing.
	 * 
	 * @param context
	 *            The context in which the Preference object is created.
	 * @param objectsCreator
	 *            The Preferences.ObjectsCreator to be used by this class. Only
	 *            create methods for which the default implementation does not
	 *            suffice, should be overridden.
	 * @return The singleton instance for the Preferences.
	 */
	public static Preferences getInstance(Context context,
			Preferences.ObjectsCreator objectsCreator) {
		if (objectsCreator != null) {
			mPreferencesSingletonInstance = objectsCreator
					.createPreferences(context);
		}
		return getInstance(context);
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
		Editor editor = mSharedPreferences.edit();

		// Each new setting which is displayed via the settings menu has to be
		// set to the default value when installing/upgrading the app. If this
		// is not done then most settings won't be displayed with the default
		// value.

		if (previousInstalledVersion < 583 && currentVersion >= 583) {
			editor.putBoolean(PUZZLE_SETTING_CLEAR_MAYBES,
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
			editor
					.putBoolean(
							PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE,
							PUZZLE_HIDE_GOOGLE_PLUS_SIGN_IN_TILL_NEXT_TOP_SCORE_DEFAULT);
		}
		if (previousInstalledVersion < 587 && currentVersion >= 587) {
			editor.putInt(LEADERBOARD_TAB_LAST_SHOWED,
					LEADERBOARD_TAB_LAST_SHOWED_DEFAULT);
			editor.putBoolean(LEADERBOARD_ALL_INITIALIZED,
					LEADERBOARD_ALL_INITIALIZED_DEFAULT);
			editor.putString(LEADERBOARD_FILTER_LAST_VALUE,
					LEADERBOARD_FILTER_LAST_VALUE_DEFAULT);
		}
		if (previousInstalledVersion < 595 && currentVersion >= 595) {
			editor.putInt(LEADERBOARD_DETAILS_VIEWED_COUNTER,
					LEADERBOARD_DETAILS_VIEWED_COUNTER_DEFAULT);
			editor.putInt(LEADERBOARD_GAMES_CREATED_COUNTER,
					LEADERBOARD_GAMES_CREATED_COUNTER_DEFAULT);
			editor.putInt(LEADERBOARD_OVERVIEW_VIEWED_COUNTER,
					LEADERBOARD_OVERVIEW_VIEWED_COUNTER_DEFAULT);
		}

		// Save
		editor.putInt(APP_CURRENT_VERSION, currentVersion);
		editor.commit();
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
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(TipDialog.getPreferenceStringDisplayTipAgain(tip),
				false);
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putLong(TipDialog.getPreferenceStringLastDisplayTime(tip), time);
		editor.apply();
	}

	/**
	 * Gets all shared preferences. Should not be used if possible.
	 * 
	 * @return A map set of all shared preferences.
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
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(ARCHIVE_AVAILABLE, true);
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(STATISTICS_AVAILABLE, true);
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putString(ARCHIVE_STATUS_FILTER_LAST_VALUE,
				statusFilter.toString());
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putString(ARCHIVE_SIZE_FILTER_LAST_VALUE, sizeFilter.toString());
		editor.apply();
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
	 * @param gridId
	 *            The id of the grid which is currently is shown.
	 */
	public void setArchiveGridIdLastShowed(int gridId) {
		Editor editor = mSharedPreferences.edit();
		editor.putInt(ARCHIVE_GRID_LAST_SHOWED, gridId);
		editor.apply();
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
	 * <p/>
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
	 * Increase the number of times a swipe motion for the given digit has been
	 * successfully completed. Also the total number of completed swipe motions
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
	@SuppressWarnings("UnusedReturnValue")
	public int increaseInputModeChangedCounter() {
		return increaseCounter(PUZZLE_INPUT_MODE_CHANGED_COUNTER_ID);
	}

	/**
	 * Increase the number of times the input mode has set to copy mode.
	 * 
	 * @return The (updated) number of times the input mode has been set to copy
	 *         mode.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public int increaseInputModeCopyCounter() {
		return increaseCounter(PUZZLE_INPUT_MODE_COPY_COUNTER_ID);
	}

	/**
	 * Commit all counter values to the preferences.
	 */
	public void commitCounters() {
		if (counters != null) {
			Editor editor = mSharedPreferences.edit();
			editor.putInt(PUZZLE_INPUT_MODE_CHANGED_COUNTER,
					counters[PUZZLE_INPUT_MODE_CHANGED_COUNTER_ID]);
			editor.putInt(PUZZLE_INPUT_MODE_COPY_COUNTER,
					counters[PUZZLE_INPUT_MODE_COPY_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_1_COUNTER,
					counters[SWIPE_DIGIT_1_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_2_COUNTER,
					counters[SWIPE_DIGIT_2_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_3_COUNTER,
					counters[SWIPE_DIGIT_3_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_4_COUNTER,
					counters[SWIPE_DIGIT_4_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_5_COUNTER,
					counters[SWIPE_DIGIT_5_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_6_COUNTER,
					counters[SWIPE_DIGIT_6_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_7_COUNTER,
					counters[SWIPE_DIGIT_7_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_8_COUNTER,
					counters[SWIPE_DIGIT_8_COUNTER_ID]);
			editor.putInt(SWIPE_DIGIT_9_COUNTER,
					counters[SWIPE_DIGIT_9_COUNTER_ID]);
			editor.putInt(SWIPE_VALID_MOTION_COUNTER,
					counters[SWIPE_VALID_MOTION_COUNTER_ID]);
			editor.putInt(SWIPE_INVALID_MOTION_COUNTER,
					counters[SWIPE_INVALID_MOTION_COUNTER_ID]);
			editor.commit();
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
		Editor editor = mSharedPreferences.edit();
		editor.putString(PUZZLE_PARAMETER_COMPLEXITY,
				puzzleComplexity.toString());
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(PUZZLE_PARAMETER_OPERATORS_VISIBLE, visible);
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putInt(PUZZLE_PARAMETER_SIZE, gridSize);
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putInt(STATISTICS_TAB_LAST_SHOWED, tab);
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		if (enableSwipe && !enableButtons) {
			editor.putString(PUZZLE_SETTING_INPUT_METHOD,
					PuzzleSettingInputMethod.SWIPE_ONLY.toString());
		} else if (!enableSwipe && enableButtons) {
			editor.putString(PUZZLE_SETTING_INPUT_METHOD,
					PuzzleSettingInputMethod.BUTTONS_ONLY.toString());
		} else {
			editor.putString(PUZZLE_SETTING_INPUT_METHOD,
					PuzzleSettingInputMethod.SWIPE_AND_BUTTONS.toString());
		}
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putString(PUZZLE_INPUT_MODE_LAST_USED, gridInputMode.toString());
		editor.putBoolean(PUZZLE_INPUT_MODE_COPY_ENABLED, copyModeEnabled);
		editor.apply();
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
						checked)
				.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putInt(LEADERBOARD_TAB_LAST_SHOWED, tab);
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(LEADERBOARD_ALL_INITIALIZED, true);
		editor.apply();
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
		Editor editor = mSharedPreferences.edit();
		editor.putString(LEADERBOARD_FILTER_LAST_VALUE,
				leaderboardFilter.toString());
		editor.apply();
	}

	/**
	 * Get the number of times the details of a leaderboard are viewed.
	 * 
	 * @return The number of times the details of a leaderboard are viewed.
	 */
	public int getLeaderboardsDetailsViewed() {
		return mSharedPreferences.getInt(LEADERBOARD_DETAILS_VIEWED_COUNTER,
				LEADERBOARD_DETAILS_VIEWED_COUNTER_DEFAULT);
	}

	/**
	 * Increase the number of times the details of a leaderboard are viewed.
	 */
	public void increaseLeaderboardsDetailsViewed() {
		Editor editor = mSharedPreferences.edit();
		editor.putInt(LEADERBOARD_DETAILS_VIEWED_COUNTER,
				getLeaderboardsDetailsViewed() + 1);
		editor.apply();

	}

	/**
	 * Get the number of times a game for a leaderboard is created.
	 * 
	 * @return The number of times a game for a leaderboard is created.
	 */
	public int getLeaderboardsGamesCreated() {
		return mSharedPreferences.getInt(LEADERBOARD_GAMES_CREATED_COUNTER,
				LEADERBOARD_GAMES_CREATED_COUNTER_DEFAULT);
	}

	/**
	 * Increase the number of times a game for a leaderboard is created.
	 */
	public void increaseLeaderboardsGamesCreated() {
		Editor editor = mSharedPreferences.edit();
		editor.putInt(LEADERBOARD_GAMES_CREATED_COUNTER,
				getLeaderboardsGamesCreated() + 1);
		editor.apply();

	}

	/**
	 * Get the number of times the leaderboards overview is started to be
	 * viewed.
	 * 
	 * @return The number of times the leaderboards overview is started to be
	 *         viewed.
	 */
	public int getLeaderboardsOverviewViewed() {
		return mSharedPreferences.getInt(LEADERBOARD_OVERVIEW_VIEWED_COUNTER,
				LEADERBOARD_OVERVIEW_VIEWED_COUNTER_DEFAULT);
	}

	/**
	 * Increase the number of times the leaderboards have been viewed.
	 */
	public void increaseLeaderboardsOverviewViewed() {
		Editor editor = mSharedPreferences.edit();
		editor.putInt(LEADERBOARD_OVERVIEW_VIEWED_COUNTER,
				getLeaderboardsOverviewViewed() + 1);
		editor.apply();

	}
}
