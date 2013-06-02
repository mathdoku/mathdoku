package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.InvalidGridException;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.gridGenerating.DialogPresentingGridGenerator;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.storage.GameFileConverter;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import net.cactii.mathdoku.tip.TipArchive;
import net.cactii.mathdoku.tip.TipDialog;
import net.cactii.mathdoku.util.UsageLog;
import net.cactii.mathdoku.util.Util;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class PuzzleFragmentActivity extends FragmentActivity implements
		PuzzleFragment.OnGridFinishedListener {
	public final static String TAG = "MathDoku.PuzzleFragmentActivity";

	// Home directory url of promotion website. Most url's used in this app will
	// be forwarded from the promotion website to code.google.com/p/mathdoku.
	public final static String PROJECT_HOME = "http://mathdoku.net/";

	// Preferences
	public Preferences mMathDokuPreferences;

	// Background tasks for generating a new puzzle and converting game files
	public DialogPresentingGridGenerator mDialogPresentingGridGenerator;
	public GameFileConverter mGameFileConverter;

	// Reference to utilities
	private Util mUtil;

	// Reference to fragments which can be displayed in this activity.
	private PuzzleFragment mPuzzleFragment;
	private ArchiveFragment mArchiveFragment;

	// Object to save data on a configuration change. Note: for the puzzle
	// fragment the RetainInstance property is set to true.
	private class ConfigurationInstanceState {
		private DialogPresentingGridGenerator mDialogPresentingGridGenerator;
		private GameFileConverter mGameFileConverter;

		public ConfigurationInstanceState(
				DialogPresentingGridGenerator gridGeneratorTask,
				GameFileConverter gameFileConverterTask) {
			mDialogPresentingGridGenerator = gridGeneratorTask;
			mGameFileConverter = gameFileConverterTask;
		}

		public DialogPresentingGridGenerator getGridGeneratorTask() {
			return mDialogPresentingGridGenerator;
		}

		public GameFileConverter getGameFileConverter() {
			return mGameFileConverter;
		}
	}

	/** Called when the activity is first created. */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize the util helper.
		mUtil = new Util(this);

		// Initialize global objects (singleton instances)
		mMathDokuPreferences = Preferences.getInstance(this);
		DatabaseHelper.getInstance(this);
		UsageLog.getInstance(this);
		Painter.getInstance(this);

		// Check if database is consistent.
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			DevelopmentHelper.checkDatabaseConsistency(this);
		}

		// If too little height then request full screen usage
		if (mUtil.getDisplayHeight() < 750) {
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			// Disable home as up on Ice Cream Sandwich and above. On Honeycomb
			// it will be enabled by default but this can do no harm.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				actionBar.setHomeButtonEnabled(false);
			}
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionBar.setSubtitle(getResources().getString(
					R.string.action_bar_subtitle_puzzle_fragment));
		}

		// Restore the last configuration instance which was saved before the
		// configuration change.
		Object object = this.getLastCustomNonConfigurationInstance();
		if (object != null
				&& object.getClass() == ConfigurationInstanceState.class) {
			UsageLog.getInstance(this).logConfigurationChange(this);
			ConfigurationInstanceState configurationInstanceState = (ConfigurationInstanceState) object;

			// Restore background process if running.
			mDialogPresentingGridGenerator = configurationInstanceState
					.getGridGeneratorTask();
			if (mDialogPresentingGridGenerator != null) {
				mDialogPresentingGridGenerator.attachToActivity(this);
			}

			// Restore background process if running.
			mGameFileConverter = configurationInstanceState
					.getGameFileConverter();
			if (mGameFileConverter != null) {
				mGameFileConverter.attachToActivity(this);
			}
		}

		checkVersion();

		restartLastGame();
	}

	public void onPause() {
		UsageLog.getInstance().close();

		super.onPause();
	}

	public void onResume() {
		UsageLog.getInstance(this);

		if (mMathDokuPreferences.isWakeLockEnabled()) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		if (mDialogPresentingGridGenerator != null) {
			// In case the grid is created in the background and the dialog is
			// closed, the activity will be moved to the background as well. In
			// case the user starts this app again onResume is called but
			// onCreate isn't. So we have to check here as well.
			mDialogPresentingGridGenerator.attachToActivity(this);
		}

		super.onResume();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Disable or enable the archive and the settings
		menu.findItem(R.id.action_archive).setVisible(
				mMathDokuPreferences.isArchiveAvailable());
		menu.findItem(R.id.action_statistics).setVisible(
				mMathDokuPreferences.isStatisticsAvailable());

		// Disable or enable option to check progress depending on whether grid
		// is active
		menu.findItem(R.id.checkprogress).setVisible(
				mPuzzleFragment != null && mPuzzleFragment.showCheckProgress());

		// Determine position of new game button
		menu.findItem(R.id.action_new_game)
				.setShowAsAction(
						(mPuzzleFragment != null && mPuzzleFragment.isActive() ? MenuItem.SHOW_AS_ACTION_NEVER
								: MenuItem.SHOW_AS_ACTION_ALWAYS));

		// When running in development mode, an extra menu is available.
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			menu.findItem(R.id.menu_development_mode).setVisible(true);
		} else {
			menu.findItem(R.id.menu_development_mode).setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.puzzle_menu, menu);

		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			inflater.inflate(R.menu.development_mode_menu, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {

		int menuId = menuItem.getItemId();
		switch (menuId) {
		case R.id.size4:
			return prepareStartNewGame(4);
		case R.id.size5:
			return prepareStartNewGame(5);
		case R.id.size6:
			return prepareStartNewGame(6);
		case R.id.size7:
			return prepareStartNewGame(7);
		case R.id.size8:
			return prepareStartNewGame(8);
		case R.id.size9:
			return prepareStartNewGame(9);
		case R.id.checkprogress:
			if (mPuzzleFragment != null) {
				mPuzzleFragment.checkProgress();
			}
			return true;
		case R.id.action_archive:
			Intent intentArchive = new Intent(this,
					ArchiveFragmentActivity.class);
			startActivity(intentArchive);
			return true;
		case R.id.action_statistics:
			UsageLog.getInstance().logFunction("Menu.Statistics");
			Intent intentStatistics = new Intent(this,
					StatisticsFragmentActivity.class);
			startActivity(intentStatistics);
			return true;
		case R.id.action_puzzle_settings:
			UsageLog.getInstance().logFunction("Menu.ViewOptions");
			startActivity(new Intent(this, PuzzlePreferenceActivity.class));
			return true;
		case R.id.action_puzzle_help:
			UsageLog.getInstance().logFunction("Menu.ViewHelp.Manual");
			this.openHelpDialog();
			return true;
		default:
			// When running in development mode it should be checked whether a
			// development menu item was selected.
			if (DevelopmentHelper.mMode != Mode.DEVELOPMENT) {
				return super.onOptionsItemSelected(menuItem);
			} else {
				if (mPuzzleFragment == null) {
					return super.onOptionsItemSelected(menuItem);
				} else {
					// Cancel old timer
					mPuzzleFragment.stopTimer();

					if (DevelopmentHelper.onDevelopmentHelperOption(this,
							menuId)) {
						// A development helper menu option was processed
						// succesfully.
						mPuzzleFragment.startTimer();
						return true;
					} else {
						mPuzzleFragment.startTimer();
						return super.onOptionsItemSelected(menuItem);
					}
				}
			}
		}
	}

	/**
	 * Prepare to start a new game. This means that it has to be determined
	 * whether the new game should be generated with operators hidden or
	 * visible.
	 * 
	 * @param gridSize
	 *            The size of the grid to be created.
	 * @return True if start of game is prepared.
	 */
	private boolean prepareStartNewGame(final int gridSize) {
		switch (mMathDokuPreferences.getHideOperator()) {
		case ALWAYS:
			// All new games should be generated with hidden operators.
			this.startNewGame(gridSize, true);
			return true;
		case NEVER:
			// All new games should be generated with visible operators.
			this.startNewGame(gridSize, false);
			return true;
		case ASK:
			// Ask for every new game which is to be generated whether operators
			// should be hidden or visible.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_hide_operators_message)
					.setCancelable(false)
					.setPositiveButton(
							R.string.dialog_hide_operators_positive_button,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									startNewGame(gridSize, true);
								}
							})
					.setNegativeButton(
							R.string.dialog_hide_operators_negative_button,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									startNewGame(gridSize, false);
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		}
		return false;
	}

	/**
	 * Starts a new game by building a new grid at the specified size.
	 * 
	 * @param gridSize
	 *            The grid size of the new puzzle.
	 * @param hideOperators
	 *            True in case operators should be hidden in the new puzzle.
	 */
	public void startNewGame(final int gridSize, final boolean hideOperators) {
		if (mPuzzleFragment != null) {
			mPuzzleFragment.prepareLoadNewGame();
		}

		// Start a background task to generate the new grid. As soon as the new
		// grid is created, the method onNewGridReady will be called.
		int maxCageSize = (mMathDokuPreferences.isAllowBigCagesEnabled() ? 6
				: 4);
		int maxCageResult = getResources().getInteger(
				R.integer.maximum_cage_value);
		mDialogPresentingGridGenerator = new DialogPresentingGridGenerator(
				this, gridSize, maxCageSize, maxCageResult, hideOperators,
				Util.getPackageVersionNumber());
		mDialogPresentingGridGenerator.execute();
	}

	/**
	 * Reactivate the main ui after a new game is loaded into the grid view by
	 * the ASync GridGenerator task.
	 */
	public void onNewGridReady(final Grid newGrid) {
		// Enable the archive as soon as the second game has been generated.
		if (mMathDokuPreferences.isArchiveAvailable() == false) {
			if (new GridDatabaseAdapter().countGrids() >= 2) {
				mMathDokuPreferences.setArchiveVisible();
				invalidateOptionsMenu();
			}
		}
		if (TipArchive.toBeDisplayed(mMathDokuPreferences)) {
			new TipArchive(PuzzleFragmentActivity.this).show();
		}

		// The background task for creating a new grid has been finished.
		mDialogPresentingGridGenerator = null;

		// Create a new puzzle fragment. Make sure that the transformation
		// transaction is completed before loading the fragment with the new
		// grid. The new grid will always overwrite the current game without any
		// warning.
		initializePuzzleFragment(newGrid.getSolvingAttemptId(), true);
	}

	/**
	 * Displays the Help Dialog.
	 */
	private void openHelpDialog() {
		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.puzzle_help_dialog, null);

		TextView tv = (TextView) view
				.findViewById(R.id.dialog_help_version_body);
		tv.setText(Util.getPackageVersionName() + " (revision "
				+ Util.getPackageVersionNumber() + ")");

		tv = (TextView) view.findViewById(R.id.help_project_home_link);
		tv.setText(PROJECT_HOME);

		final PuzzleFragmentActivity puzzleFragmentActivity = this;
		new AlertDialog.Builder(puzzleFragmentActivity)
				.setTitle(
						getResources().getString(R.string.application_name)
								+ (DevelopmentHelper.mMode == Mode.DEVELOPMENT ? " r"
										+ Util.getPackageVersionNumber() + " "
										: " ")
								+ getResources()
										.getString(R.string.action_help))
				.setIcon(R.drawable.about)
				.setView(view)
				.setNeutralButton(R.string.puzzle_help_dialog_neutral_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								UsageLog.getInstance().logFunction(
										"ViewChanges.Manual");
								puzzleFragmentActivity.openChangesDialog();
							}
						})
				.setNegativeButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	/**
	 * Displayes the changes dialog. As from version 1.96 the changes itself are
	 * no longer described in this dialog. Instead a reference to the wehsite is
	 * shown.
	 */
	private void openChangesDialog() {
		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.changelog_dialog, null);

		TextView textView = (TextView) view
				.findViewById(R.id.changelog_version_body);
		textView.setText(Util.getPackageVersionName() + " (revision "
				+ Util.getPackageVersionNumber() + ")");

		textView = (TextView) view.findViewById(R.id.changelog_changes_link);
		textView.setText(PROJECT_HOME + "changes.php");

		textView = (TextView) view.findViewById(R.id.changelog_issues_link);
		textView.setText(PROJECT_HOME + "issues.php");

		new AlertDialog.Builder(this)
				.setTitle(
						getResources().getString(R.string.application_name)
								+ (DevelopmentHelper.mMode == Mode.DEVELOPMENT ? " r"
										+ Util.getPackageVersionNumber() + " "
										: " ")
								+ getResources().getString(
										R.string.changelog_title))
				.setIcon(R.drawable.about)
				.setView(view)
				.setNegativeButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								//
							}
						}).show();
	}

	/**
	 * Checks whether a new version of the game has been installed. If so,
	 * modify preferences and convert if necessary.
	 */
	private void checkVersion() {
		if (mGameFileConverter != null) {
			// Phase 1 of the upgrade is not yet completed. The upgrade process
			// should not be restarted till the phase 1 background process is
			// completed.
			return;
		}

		// Get current version number from the package.
		int packageVersionNumber = Util.getPackageVersionNumber();

		// Get the previous installed version from the preferences.
		int previousInstalledVersion = mMathDokuPreferences
				.getCurrentInstalledVersion();

		// Start phase 1 of the upgrade process if needed.
		if (previousInstalledVersion < packageVersionNumber) {
			// On Each update of the game, all game data will be converted to
			// the latest definitions. On completion of the game file
			// conversion, method upgradePhase2_UpdatePreferences will be
			// called.
			mGameFileConverter = new GameFileConverter(this,
					previousInstalledVersion, packageVersionNumber);
			mGameFileConverter.execute();
		}
		return;
	}

	/**
	 * Finishes the upgrading process after the game files have been converted.
	 * 
	 * @param previousInstalledVersion
	 *            : Latest version of MathDoku which was actually used.
	 * @param currentVersion
	 *            Current (new) revision number of MathDoku.
	 */
	public void upgradePhase2_UpdatePreferences(int previousInstalledVersion,
			int currentVersion) {

		// Update preferences
		mMathDokuPreferences.upgrade(previousInstalledVersion, currentVersion);

		// Show help dialog after new/fresh install or changes dialog
		// otherwise.
		if (previousInstalledVersion == -1) {
			// On first install of the game, display the help dialog.
			UsageLog.getInstance().logFunction("ViewHelp.AfterUpgrade");
			this.openHelpDialog();
		} else if (previousInstalledVersion < currentVersion) {
			// On upgrade of version show changes.
			UsageLog.getInstance().logFunction("ViewChanges.AfterUpgrade");
			this.openChangesDialog();
		}

		// Restart the last game
		restartLastGame();
	}

	/*
	 * Responds to a configuration change just before the activity is destroyed.
	 * In case a background task is still running, a reference to this task will
	 * be retained so that the activity can reconnect to this task as soon as it
	 * is resumed.
	 * 
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		// http://stackoverflow.com/questions/11591302/unable-to-use-fragment-setretaininstance-as-a-replacement-for-activity-onretai

		// Cleanup
		if (mPuzzleFragment != null) {
			mPuzzleFragment.stopTimer();
		}
		TipDialog.resetDisplayedDialogs();

		if (mDialogPresentingGridGenerator != null) {
			// A new grid is generated in the background. Detach the background
			// task from this activity. It will keep on running until finished.
			mDialogPresentingGridGenerator.detachFromActivity();
		}
		if (mGameFileConverter != null) {
			// The game files are converted in the background. Detach the
			// background
			// task from this activity. It will keep on running until finished.
			mGameFileConverter.detachFromActivity();
		}
		return new ConfigurationInstanceState(mDialogPresentingGridGenerator,
				mGameFileConverter);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		if (mPuzzleFragment != null) {
			mPuzzleFragment.onContextMenuClosed(menu);
		}
	}

	@Override
	public void onGridFinishedListener(int solvingAttemptId) {
		// Once the grid has been solved, the statistics fragment has to be
		// displayed.
		initializeArchiveFragment(solvingAttemptId, false);

		// Refresh option menu. For example check progress should be
		// hidden.
		invalidateOptionsMenu();
	}

	/**
	 * Initializes the puzzle fragment. The archive fragment will be disabled.
	 * 
	 * @param forceFragmentTransaction
	 *            Force the execution of the fragment transition which is needed
	 *            in case the content of the fragment needs to be accessed right
	 *            away.
	 */
	private void initializePuzzleFragment(int solvingAttemptId,
			boolean forceFragmentTransaction) {
		// Set the puzzle fragment
		mPuzzleFragment = new PuzzleFragment();
		if (solvingAttemptId >= 0) {
			Bundle args = new Bundle();
			args.putInt(PuzzleFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID,
					solvingAttemptId);
			mPuzzleFragment.setArguments(args);
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(android.R.id.content, mPuzzleFragment);
		fragmentTransaction.commit();
		if (forceFragmentTransaction) {
			fragmentManager.executePendingTransactions();
		}

		// Disable the archive fragment
		mArchiveFragment = null;
	}

	/**
	 * Initializes the archive fragment. The puzzle fragment will be disabled.
	 * 
	 * @param forceFragmentTransaction
	 *            Force the execution of the fragment transition which is needed
	 *            in case the content of the fragment needs to be accessed right
	 *            away.
	 */
	private void initializeArchiveFragment(int solvingAttemptId,
			boolean forceFragmentTransaction) {
		// Set the archive fragment
		mArchiveFragment = new ArchiveFragment();
		Bundle args = new Bundle();
		args.putInt(ArchiveFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID,
				solvingAttemptId);
		mArchiveFragment.setArguments(args);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(android.R.id.content, mArchiveFragment)
				.commit();
		if (forceFragmentTransaction) {
			fragmentManager.executePendingTransactions();
		}

		// Disable the archive fragment
		mPuzzleFragment = null;

	}

	/**
	 * Restart the last game which was played.
	 */
	protected void restartLastGame() {
		// Determine if and which grid was last played
		int solvingAttemptId = new SolvingAttemptDatabaseAdapter()
				.getMostRecentPlayedId();
		if (solvingAttemptId >= 0) {
			// Load the grid
			try {
				Grid newGrid = new Grid();
				newGrid.load(solvingAttemptId);

				if (newGrid.isActive()) {
					// Create a new puzzle fragment. Make sure that the
					// transformation transaction is completed before loading
					// the fragment with the new grid. The new grid will always
					// overwrite the current game without any warning.
					initializePuzzleFragment(solvingAttemptId, true);
				} else {
					initializeArchiveFragment(solvingAttemptId, true);
				}
			} catch (InvalidGridException e) {
				if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
					Log.e(TAG,
							"PuzzleFragmentActivity.restartLastGame can not load solvingAttempt with id '"
									+ solvingAttemptId + "'.");
				}
			}
		}
	}
}