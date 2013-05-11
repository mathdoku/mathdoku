package net.cactii.mathdoku;

import java.util.Map;

import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.painter.Painter.GridTheme;
import net.cactii.mathdoku.tip.TipDialog.TipCategory;
import net.cactii.mathdoku.util.SingletonInstanceNotInstantiated;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Preferences {

	// Singleton reference to the preferences object
	private static Preferences mPreferencesSingletonInstance = null;

	// Actual preferences
	public SharedPreferences mSharedPreferences;

	// Identifiers for preferences.
	public final static String CHECK_PROGRESS_USED = "CheckProgressUsed";
	public final static boolean CHECK_PROGRESS_USED_DEFAULT = false;

	public final static String CLEAR_REDUNDANT_POSSIBLES = "redundantPossibles";
	public final static boolean CLEAR_REDUNDANT_POSSIBLES_DEFAULT = true;

	public final static String CREATE_PREVIEW_IMAGES_COMPLETED = "CreatePreviewImagesCompleted";
	public final static boolean CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT = false;

	public final static String CURRENT_VERSION = "currentversion";
	public final static int CURRENT_VERSION_DEFAULT = -1;

	public final static String ALLOW_BIG_CAGES = "AllowBigCages";
	public final static boolean ALLOW_BIG_CAGES_DEFAULT = false;

	public final static String HIDE_CONTROLS = "hideselector";
	public final static boolean HIDE_CONTROLS_DEFAULT = false;

	public final static String HIDE_OPERATORS = "hideoperatorsigns";

	public final static String HIDE_OPERATORS_ALWAYS = "T";
	public final static String HIDE_OPERATORS_ASK = "A";
	public final static String HIDE_OPERATORS_NEVER = "F";
	public final static String HIDE_OPERATORS_DEFAULT = HIDE_OPERATORS_NEVER;

	public final static String PLAY_SOUND_EFFECTS = "soundeffects";
	public final static boolean PLAY_SOUND_EFFECTS_DEFAULT = true;

	public final static String SHOW_MAYBES_AS_GRID = "maybe3x3";
	public final static boolean SHOW_MAYBES_AS_GRID_DEFAULT = true;

	public final static String SHOW_BAD_CAGE_MATHS = "badmaths";
	public final static boolean SHOW_BAD_CAGE_MATHS_DEFAULT = true;

	public final static String SHOW_STATISTICS_DESCRIPTION = "ShowStatisticsDescription";
	public final static boolean SHOW_STATISTICS_DESCRIPTION_DEFAULT = true;

	public final static String SHOW_DUPE_DIGITS = "dupedigits";
	public final static boolean SHOW_DUPE_DIGITS_DEFAULT = true;

	public final static String SHOW_TIMER = "timer";
	public final static boolean SHOW_TIMER_DEFAULT = true;

	public final static String THEME = "theme";
	public final static String THEME_CARVED = "carved";
	public final static String THEME_DARK = "inverted";
	public final static String THEME_NEWSPAPER = "newspaper";
	public final static String THEME_NEWSPAPER_OLD = "newspaperold";
	public final static String THEME_DEFAULT = THEME_NEWSPAPER;

	private static String TIP_CATEGORY_FAMILIAR_WITH_APP = "Tip.Category.FamiliarWithApp";
	private static boolean TIP_CATEGORY_FAMILIAR_WITH_APP_DEFAULT = false;

	private static String TIP_CATEGORY_FAMILIAR_WITH_RULES = "Tip.Category.FamiliarWithRules";
	private static boolean TIP_CATEGORY_FAMILIAR_WITH_RULES_DEFAULT = false;

	public final static String USAGE_LOG_COUNT_GAMES_STARTED = "UsageLogCountGamesStarted";
	public final static int USAGE_LOG_COUNT_GAMES_STARTED_DEFAULT = 0;

	public final static String USAGE_LOG_DISABLED = "UsageLogDisabled";
	public final static boolean USAGE_LOG_DISABLED_DEFAULT = false;

	public final static String WAKE_LOCK = "wakelock";
	public final static boolean WAKE_LOCK_DEFAULT = true;

	public enum HideOperator {
		ALWAYS, ASK, NEVER
	};

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

		if (previousInstalledVersion < 98 && currentVersion >= 98) {
			if (!mSharedPreferences.contains(CREATE_PREVIEW_IMAGES_COMPLETED)) {
				mSharedPreferences.getBoolean(
						Preferences.CREATE_PREVIEW_IMAGES_COMPLETED,
						Preferences.CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT);
			}
		}

		if (previousInstalledVersion < 121 && currentVersion >= 121) {
			// Add missing preferences to the Shared Preferences.
			if (!mSharedPreferences.contains(CLEAR_REDUNDANT_POSSIBLES)) {
				prefeditor.putBoolean(CLEAR_REDUNDANT_POSSIBLES,
						CLEAR_REDUNDANT_POSSIBLES_DEFAULT);
			}
		}
		if (previousInstalledVersion < 123 && currentVersion >= 123) {
			// Add missing preferences to the Shared Preferences. Note:
			// those preferences have been introduced in revisions prior to
			// revision 122. But as from revision 122 the default values
			// have been removed from optionsview.xml to prevent conflicts
			// in defaults values with default values defined in this
			// activity.
			if (!mSharedPreferences.contains(HIDE_CONTROLS)) {
				prefeditor.putBoolean(HIDE_CONTROLS, HIDE_CONTROLS_DEFAULT);
			}
			if (!mSharedPreferences.contains(HIDE_OPERATORS)) {
				prefeditor.putString(HIDE_OPERATORS, HIDE_OPERATORS_DEFAULT);
			}
			if (!mSharedPreferences.contains(PLAY_SOUND_EFFECTS)) {
				prefeditor.putBoolean(PLAY_SOUND_EFFECTS,
						PLAY_SOUND_EFFECTS_DEFAULT);
			}
			if (!mSharedPreferences.contains(SHOW_BAD_CAGE_MATHS)) {
				prefeditor.putBoolean(SHOW_BAD_CAGE_MATHS,
						SHOW_BAD_CAGE_MATHS_DEFAULT);
			}
			if (!mSharedPreferences.contains(SHOW_DUPE_DIGITS)) {
				prefeditor.putBoolean(SHOW_DUPE_DIGITS,
						SHOW_DUPE_DIGITS_DEFAULT);
			}
			if (!mSharedPreferences.contains(SHOW_MAYBES_AS_GRID)) {
				prefeditor.putBoolean(SHOW_MAYBES_AS_GRID,
						SHOW_MAYBES_AS_GRID_DEFAULT);
			}
			if (!mSharedPreferences.contains(SHOW_TIMER)) {
				prefeditor.putBoolean(SHOW_TIMER, SHOW_TIMER_DEFAULT);
			}
			if (!mSharedPreferences.contains(THEME)) {
				prefeditor.putString(THEME, THEME_DEFAULT);
			}
			if (!mSharedPreferences.contains(WAKE_LOCK)) {
				prefeditor.putBoolean(WAKE_LOCK, WAKE_LOCK_DEFAULT);
			}
		}
		if (previousInstalledVersion < 135 && currentVersion >= 135) {
			if (!mSharedPreferences.contains(ALLOW_BIG_CAGES)) {
				prefeditor.putBoolean(ALLOW_BIG_CAGES, ALLOW_BIG_CAGES_DEFAULT);
			}
		}
		if (previousInstalledVersion < 175 && currentVersion >= 175) {
			if (!mSharedPreferences.contains(USAGE_LOG_DISABLED)) {
				prefeditor.putBoolean(USAGE_LOG_DISABLED,
						USAGE_LOG_DISABLED_DEFAULT);
			}
			if (!mSharedPreferences.contains(USAGE_LOG_COUNT_GAMES_STARTED)) {
				prefeditor.putInt(USAGE_LOG_COUNT_GAMES_STARTED,
						USAGE_LOG_COUNT_GAMES_STARTED_DEFAULT);
			}
		}
		if (previousInstalledVersion < 198 && currentVersion >= 198) {
			// User who upgrade are assumed to be familiar with the app.
			if (!mSharedPreferences.contains(TIP_CATEGORY_FAMILIAR_WITH_APP)) {
				prefeditor.putBoolean(TIP_CATEGORY_FAMILIAR_WITH_APP,
						previousInstalledVersion > 0);
			}
			if (!mSharedPreferences.contains(TIP_CATEGORY_FAMILIAR_WITH_RULES)) {
				prefeditor.putBoolean(TIP_CATEGORY_FAMILIAR_WITH_RULES,
						TIP_CATEGORY_FAMILIAR_WITH_RULES_DEFAULT);
			}
		}
		if (previousInstalledVersion < 198 && currentVersion >= 198) {
			if (!mSharedPreferences.contains(SHOW_STATISTICS_DESCRIPTION)) {
				prefeditor.putBoolean(SHOW_STATISTICS_DESCRIPTION,
						SHOW_STATISTICS_DESCRIPTION_DEFAULT);
			}
		}
		if (previousInstalledVersion < 282 && currentVersion >= 282) {
			if (!mSharedPreferences.contains(CHECK_PROGRESS_USED)) {
				prefeditor.putBoolean(CHECK_PROGRESS_USED,
						CHECK_PROGRESS_USED_DEFAULT);
			}
		}

		prefeditor.putInt(CURRENT_VERSION, currentVersion);
		prefeditor.commit();
	}

	/**
	 * Checks whether the control digits should be hidden automatically.
	 * 
	 * @return
	 */
	public boolean isControlsBlockHidden() {
		return mSharedPreferences.getBoolean(HIDE_CONTROLS,
				HIDE_CONTROLS_DEFAULT);
	}

	/**
	 * Checks whether the preview images creation process has completed.
	 * 
	 * @return True in case the preview images creation process has completed.
	 *         False otherwise.
	 */
	public boolean isCreatePreviewImagesCompleted() {
		return mSharedPreferences.getBoolean(CREATE_PREVIEW_IMAGES_COMPLETED,
				CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT);
	}

	/**
	 * Marks the process of preview image creation as completed.
	 */
	public void setCreatePreviewImagesCompleted() {
		Editor prefEditor = mSharedPreferences.edit();
		prefEditor.putBoolean(CREATE_PREVIEW_IMAGES_COMPLETED, true);
		prefEditor.commit();

	}

	/**
	 * Resets preferences which determine whether the preview image creation
	 * process should be (re)started.
	 */
	public void initCreatePreviewImagesCompleted() {
		// Remove preference which controls the process of preview image
		// creation.
		if (mSharedPreferences
				.contains(Preferences.CREATE_PREVIEW_IMAGES_COMPLETED)) {
			Editor prefeditor = mSharedPreferences.edit();
			prefeditor.remove(Preferences.CREATE_PREVIEW_IMAGES_COMPLETED);
			prefeditor.commit();
		}
	}

	/**
	 * Checks whether the timer should be displayed.
	 * 
	 * @return True in case the timer should be displayed. False otherwise.
	 */
	public boolean isTimerVisible() {
		return mSharedPreferences.getBoolean(SHOW_TIMER, SHOW_TIMER_DEFAULT);
	}

	/**
	 * Gets the current theme.
	 * 
	 * @return The current theme.
	 */
	public Painter.GridTheme getTheme() {
		String theme = mSharedPreferences.getString(THEME, THEME_DEFAULT);

		if (theme.equals(THEME_NEWSPAPER)) {
			return GridTheme.NEWSPAPER;
		} else if (theme.equals(THEME_DARK)) {
			return GridTheme.DARK;
		} else if (theme.equals(THEME_CARVED)) {
			return GridTheme.CARVED;
		}
		return null;
	}

	/**
	 * Checks whether redundant possible values in the same column or row should
	 * be removed automatically.
	 * 
	 * @return True in case redundant possible values in the same column or row
	 *         should be removed automatically. False otherwise.
	 */
	public boolean isClearRedundantPossiblesEnabled() {
		return mSharedPreferences.getBoolean(CLEAR_REDUNDANT_POSSIBLES,
				CLEAR_REDUNDANT_POSSIBLES_DEFAULT);
	}

	/**
	 * Checks whether big cages are allowed.
	 * 
	 * @return
	 */
	public boolean isAllowBigCagesEnabled() {
		return mSharedPreferences.getBoolean(ALLOW_BIG_CAGES,
				ALLOW_BIG_CAGES_DEFAULT);
	}

	/**
	 * Gets the number of games which have been started.
	 * 
	 * @return The number of games which have been started.
	 */
	public int getNumberOfGamesStarted() {
		return mSharedPreferences.getInt(USAGE_LOG_COUNT_GAMES_STARTED,
				USAGE_LOG_COUNT_GAMES_STARTED_DEFAULT);
	}

	/**
	 * Increase the number of games which have been started by the user.
	 * 
	 * @return The total number of games which have been started by the user.
	 */
	public int increaseGamesStarted() {
		int countGamesStarted = mSharedPreferences.getInt(
				USAGE_LOG_COUNT_GAMES_STARTED, 0) + 1;
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putInt(USAGE_LOG_COUNT_GAMES_STARTED, countGamesStarted);
		prefeditor.commit();

		return countGamesStarted;
	}

	/**
	 * Gets the version number of the currently installed version of the app.
	 * 
	 * @return The version number of the currently installed version of the app.
	 */
	public int getCurrentInstalledVersion() {
		return mSharedPreferences.getInt(CURRENT_VERSION,
				CURRENT_VERSION_DEFAULT);
	}

	/**
	 * Checks whether it should be prevented that the screen get locked while
	 * playing.
	 * 
	 * @return True in case it should be prevented that the screen get locked
	 *         while playing.
	 */
	public boolean isWakeLockEnabled() {
		return mSharedPreferences.getBoolean(WAKE_LOCK, WAKE_LOCK_DEFAULT);
	}

	/**
	 * Checks whether the sounds effects are enabled.
	 * 
	 * @return True in the sounds effects are enabled. False otherwise.
	 */
	public boolean isPlaySoundEffectEnabled() {
		return mSharedPreferences.getBoolean(PLAY_SOUND_EFFECTS,
				PLAY_SOUND_EFFECTS_DEFAULT);
	}

	/**
	 * Checks whether operators should be hidden when creating a new game.
	 * 
	 * @return {@value HideOperator.ALWAYS} in case the operators should always
	 *         be hidden. {@value HideOperator.NEVER} in case the operators
	 *         should never be hidden. {@value HideOperator.ASK} in case it has
	 *         to be asked whether the operators should be hidden when creating
	 *         a new game.
	 */
	public HideOperator getHideOperator() {
		String hideOperator = mSharedPreferences.getString(HIDE_OPERATORS,
				HIDE_OPERATORS_DEFAULT);
		if (hideOperator.equals(HIDE_OPERATORS_ALWAYS)) {
			return HideOperator.ALWAYS;
		} else if (hideOperator.equals(Preferences.HIDE_OPERATORS_NEVER)) {
			return HideOperator.NEVER;
		} else if (hideOperator.equals(Preferences.HIDE_OPERATORS_ASK)) {
			return HideOperator.ASK;
		}
		return null;
	}

	/**
	 * Mark a tip so it will not be displayed again.
	 * 
	 * @param tipName
	 *            The preference name of the tip to be marked.
	 */
	public void setDoNotDisplayTipAgain(String tipName) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(tipName, false);
		prefeditor.commit();
	}

	/**
	 * Check whether this tip will be shown. Tips which are checked frequently
	 * should always call the static displayTip method of the corresponding
	 * subclass before actually call method show as this always creates a dialog
	 * while not knowing whether the tip has to be displayed.
	 * 
	 * @return True in case the tip has to be shown. False otherwise.
	 */
	public boolean getDisplayTipAgain(String preference, TipCategory tipCategory) {
		// Tip will not be displayed in case its checkbox was checked before.
		if (!mSharedPreferences.getBoolean(preference, true)) {
			return false;
		}

		switch (tipCategory) {
		case APP_USAGE:
			// Do not display this tip in case the user is already familiar
			// with the app
			return !mSharedPreferences.getBoolean(
					TIP_CATEGORY_FAMILIAR_WITH_APP,
					TIP_CATEGORY_FAMILIAR_WITH_APP_DEFAULT);
		case GAME_RULES:
			// Do not display this tip in case the user is already familiar
			// with the game rules
			return !mSharedPreferences.getBoolean(
					TIP_CATEGORY_FAMILIAR_WITH_RULES,
					TIP_CATEGORY_FAMILIAR_WITH_RULES_DEFAULT);
		}

		return true;
	}

	/**
	 * Sets a tip to do-not-display-again.
	 */
	public void doNotDisplayTipAgain(String preference) {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(preference, false);
		prefeditor.commit();
	}

	/**
	 * Initializes the preference "familiar with rules".
	 * 
	 * @param mainActivity
	 *            The activity for which the preference has to set.
	 * 
	 * @param familiarWithRules
	 *            The new value for this preference.
	 */
	public void setUserIsFamiliarWithRules(boolean familiarWithRules) {
		Editor prefeditor = mSharedPreferences.edit();
		if (!mSharedPreferences.contains(TIP_CATEGORY_FAMILIAR_WITH_RULES)) {
			prefeditor.putBoolean(TIP_CATEGORY_FAMILIAR_WITH_RULES,
					TIP_CATEGORY_FAMILIAR_WITH_RULES_DEFAULT);
		}
		prefeditor.commit();
	}

	/**
	 * Checks if the usage log is disabled.
	 * 
	 * @return True in case the usage log is disabled.
	 */
	public boolean isUsageLogDisabled() {
		return mSharedPreferences.getBoolean(USAGE_LOG_DISABLED,
				USAGE_LOG_DISABLED_DEFAULT);
	}

	/**
	 * Disables the usage log.
	 */
	public void setUsageLogDisabled() {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(USAGE_LOG_DISABLED, true);
		prefeditor.commit();
	}

	/**
	 * Resets the usage log preferences to the default values.
	 */
	public void resetUsageLogDisabled() {
		Editor prefeditor = mSharedPreferences.edit();
		prefeditor.putBoolean(USAGE_LOG_DISABLED, USAGE_LOG_DISABLED_DEFAULT);
		prefeditor.putInt(USAGE_LOG_COUNT_GAMES_STARTED,
				USAGE_LOG_COUNT_GAMES_STARTED_DEFAULT);
		prefeditor.commit();
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
	public boolean showDuplicateDigits() {
		return mSharedPreferences.getBoolean(SHOW_DUPE_DIGITS,
				SHOW_DUPE_DIGITS_DEFAULT);
	}

	/**
	 * Checks whether maybes should be shown in the same pattern as the digit
	 * buttons.
	 * 
	 * @return True in case maybes should be shown in the same pattern as the
	 *         digit buttons. False otherwise.
	 */
	public boolean showMaybesAsGrid() {
		return mSharedPreferences.getBoolean(SHOW_MAYBES_AS_GRID,
				SHOW_MAYBES_AS_GRID_DEFAULT);
	}

	/**
	 * Checks whether cages having bad math should be highlighted.
	 * 
	 * @return True in case cages having bad math should be highlighted. False
	 *         otherwise.
	 */
	public boolean showBadCageMaths() {
		return mSharedPreferences.getBoolean(SHOW_BAD_CAGE_MATHS,
				SHOW_BAD_CAGE_MATHS_DEFAULT);
	}

	/**
	 * Checks whether a description has to be shown below a statistics chart.
	 * 
	 * @return True in case statistic descriptions have to be shown. False
	 *         otherwise.
	 */
	public boolean showStatisticsDescription() {
		return mSharedPreferences.getBoolean(SHOW_STATISTICS_DESCRIPTION,
				SHOW_STATISTICS_DESCRIPTION_DEFAULT);
	}
}