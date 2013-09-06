package net.cactii.mathdoku.ui;

import java.util.ArrayList;
import java.util.Arrays;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.grid.InvalidGridException;
import net.cactii.mathdoku.gridGenerating.DialogPresentingGridGenerator;
import net.cactii.mathdoku.gridGenerating.GridGenerator.PuzzleComplexity;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.storage.GameFileConverter;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter.SizeFilter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter.StatusFilter;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import net.cactii.mathdoku.tip.TipArchiveAvailable;
import net.cactii.mathdoku.tip.TipDialog;
import net.cactii.mathdoku.tip.TipStatistics;
import net.cactii.mathdoku.util.FeedbackEmail;
import net.cactii.mathdoku.util.SharedPuzzle;
import net.cactii.mathdoku.util.Util;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class PuzzleFragmentActivity extends AppFragmentActivity implements
		PuzzleFragment.OnGridFinishedListener {
	public final static String TAG = "MathDoku.PuzzleFragmentActivity";

	// Background tasks for generating a new puzzle and converting game files
	public DialogPresentingGridGenerator mDialogPresentingGridGenerator;
	public GameFileConverter mGameFileConverter;

	// Reference to fragments which can be displayed in this activity.
	private PuzzleFragment mPuzzleFragment;
	private ArchiveFragment mArchiveFragment;

	// References to the navigation drawer
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mActionBarDrawerToggle;
	private ListView mDrawerListView;
	private String[] mNavigationDrawerItems;

	// Object to save data on a configuration change. Note: for the puzzle
	// fragment the RetainInstance property is set to true.
	private class ConfigurationInstanceState {
		private final DialogPresentingGridGenerator mDialogPresentingGridGenerator;
		private final GameFileConverter mGameFileConverter;

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

	// Request code
	private final static int REQUEST_ARCHIVE = 1;

	// When using the support package, onActivityResult is called before
	// onResume. As a result fragment can not be manipulated in the
	// onActivityResult. If following variable contains a value >=0 the solving
	// attempt with this specific number should be replayed in the onResume
	// call.
	private int mOnResumeReplaySolvingAttempt;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// In BugSense mode the bug sense handler is initiated and started. In
		// case an exception occurs in this mode, it will be reported via the
		// BugSense web site. In this way exceptions which occurs while testing
		// the app can be monitored more closely. Note: the internet permission
		// needs to activated for this.
		if (DevelopmentHelper.mMode == Mode.BUG_SENSE) {
			BugSenseHandler.initAndStartSession(this,
					DevelopmentHelper.BUG_SENSE_API_KEY);
		}

		setContentView(R.layout.puzzle_activity_fragment);

		// Initialize global objects (singleton instances)
		DatabaseHelper.getInstance(this);
		new Util(this);

		// Check if database is consistent.
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			if (DevelopmentHelper.checkDatabaseConsistency(this) == false) {
				// Skip remainder of onCreate because further database access
				// can result in a forced close.
				return;
			}
		}

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionBar.setSubtitle(getResources().getString(
					R.string.action_bar_subtitle_puzzle_fragment));
		}

		// Set up the navigation drawer.
		setNavigationDrawer();

		// Restore the last configuration instance which was saved before the
		// configuration change.
		Object object = this.getLastCustomNonConfigurationInstance();
		if (object != null
				&& object.getClass() == ConfigurationInstanceState.class) {
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

		// Check if app needs to be upgraded. If not, restart the last game.
		if (isUpgradeRunning() == false) {
			restartLastGame();
		}

		// At this point there will never be a need to replay a solving attempt.
		mOnResumeReplaySolvingAttempt = -1;
	}

	@Override
	public void onResume() {
		if (mDialogPresentingGridGenerator != null) {
			// In case the grid is created in the background and the dialog is
			// closed, the activity will be moved to the background as well. In
			// case the user starts this app again onResume is called but
			// onCreate isn't. So we have to check here as well.
			mDialogPresentingGridGenerator.attachToActivity(this);
		}

		// Select the the play puzzle item as active item. This is especially
		// needed when resuming after returning form another activity like the
		// Archive or the Statistics.
		if (mDrawerListView != null) {
			mDrawerListView.setItemChecked(0, true);
		}

		// In case onActivityResult is called and a solving attempt has to be
		// replayed then the actual replaying of the solving attempt is
		// postponed till this moment as fragments can not be manipulated in
		// onActivityResult.
		if (mOnResumeReplaySolvingAttempt >= 0) {
			replayPuzzle(mOnResumeReplaySolvingAttempt);
			mOnResumeReplaySolvingAttempt = -1;
		}

		super.onResume();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Sync the navigation drawer toggle state after onRestoreInstanceState
		// has occurred.
		if (mActionBarDrawerToggle != null) {
			mActionBarDrawerToggle.syncState();
		}
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the navigation drawer is open, hide action items related to the
		// content view
		boolean drawerOpen = (mDrawerLayout == null || mDrawerListView == null ? false
				: mDrawerLayout.isDrawerOpen(mDrawerListView));

		boolean showCheats = false;

		// Set visibility for menu option check progress
		menu.findItem(R.id.checkprogress).setVisible(
				!drawerOpen && mPuzzleFragment != null
						&& mPuzzleFragment.showCheckProgress());

		// Set visibility for menu option to reveal a cell
		if (!drawerOpen && mPuzzleFragment != null
				&& mPuzzleFragment.showRevealCell()) {
			menu.findItem(R.id.action_reveal_cell).setVisible(true);
			showCheats = true;
		} else {
			menu.findItem(R.id.action_reveal_cell).setVisible(false);
		}

		// Set visibility for menu option to reveal a operator
		if (!drawerOpen && mPuzzleFragment != null
				&& mPuzzleFragment.showRevealOperator()) {
			menu.findItem(R.id.action_reveal_operator).setVisible(true);
			showCheats = true;
		} else {
			menu.findItem(R.id.action_reveal_operator).setVisible(false);
		}

		// Set visibility for menu option to reveal the solution
		if (!drawerOpen && mPuzzleFragment != null
				&& mPuzzleFragment.showRevealSolution()) {
			menu.findItem(R.id.action_show_solution).setVisible(true);
			showCheats = true;
		} else {
			menu.findItem(R.id.action_show_solution).setVisible(false);
		}

		// The cheats menu is only visible in case at least one sub menu item is
		// visible. Note: the item does not exist when running on Android 3 as
		// that version does not allow sub menu's.
		MenuItem menuItem = menu.findItem(R.id.action_cheat);
		if (menuItem != null) {
			menuItem.setVisible(!drawerOpen && showCheats);
		}

		// Set visibility for menu option to clear the grid
		menu.findItem(R.id.action_clear_grid).setVisible(
				!drawerOpen && mPuzzleFragment != null
						&& mPuzzleFragment.showClearGrid());

		// Determine position of new game button
		menu.findItem(R.id.action_new_game)
				.setVisible(!drawerOpen)
				.setShowAsAction(
						(mPuzzleFragment != null && mPuzzleFragment.isActive() ? MenuItem.SHOW_AS_ACTION_NEVER
								: MenuItem.SHOW_AS_ACTION_ALWAYS));

		// Display the share button on the action bar dependent on the fragment
		// being showed.
		menu.findItem(R.id.action_share)
				.setVisible(!drawerOpen)
				.setShowAsAction(
						(mArchiveFragment != null ? MenuItem.SHOW_AS_ACTION_IF_ROOM
								: MenuItem.SHOW_AS_ACTION_NEVER));

		// When running in development mode, an extra menu is available.
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			menu.findItem(R.id.menu_development_mode).setVisible(true);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		// First pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mActionBarDrawerToggle != null
				&& mActionBarDrawerToggle.onOptionsItemSelected(menuItem)) {
			return true;
		}

		int menuId = menuItem.getItemId();
		switch (menuId) {
		case R.id.action_new_game:
			showDialogNewGame(true);
			return true;
		case R.id.checkprogress:
			if (mPuzzleFragment != null) {
				mPuzzleFragment.checkProgress();
			}
			return true;
		case R.id.action_reveal_cell:
			if (mPuzzleFragment != null) {
				mPuzzleFragment.revealCell();
			}
			return true;
		case R.id.action_reveal_operator:
			if (mPuzzleFragment != null) {
				mPuzzleFragment.revealOperator();
			}
			return true;
		case R.id.action_show_solution:
			if (mPuzzleFragment != null) {
				mPuzzleFragment.revealSolution();
			}
			return true;
		case R.id.action_clear_grid:
			if (mPuzzleFragment != null) {
				mPuzzleFragment.clearGrid();
			}
			return true;
		case R.id.action_share:
			if (mPuzzleFragment != null) {
				new SharedPuzzle(this).share(mPuzzleFragment
						.getSolvingAttemptId());
			} else if (mArchiveFragment != null) {
				// Only the archive fragment possibly contains the statistics
				// views which can be enclosed as attachments to the share
				// email.
				new SharedPuzzle(this).addStatisticsChartsAsAttachments(
						this.getWindow().getDecorView()).share(
						mArchiveFragment.getSolvingAttemptId());
			}
			return true;
		case R.id.action_puzzle_settings:
			startActivity(new Intent(this, PuzzlePreferenceActivity.class));
			return true;
		case R.id.action_send_feedback:
			new FeedbackEmail(this).show();
			return true;
		case R.id.action_puzzle_help:
			this.openHelpDialog(false);
			return true;
		default:
			// When running in development mode it should be checked whether a
			// development menu item was selected.
			if (DevelopmentHelper.mMode != Mode.DEVELOPMENT) {
				return super.onOptionsItemSelected(menuItem);
			} else {
				if (mPuzzleFragment != null) {
					// Cancel old timer
					mPuzzleFragment.stopTimer();
				}

				if (DevelopmentHelper.onDevelopmentHelperOption(this, menuId)) {
					// A development helper menu option was processed
					// succesfully.
					if (mPuzzleFragment != null) {
						mPuzzleFragment.startTimer();
					}
					return true;
				} else {
					if (mPuzzleFragment != null) {
						mPuzzleFragment.startTimer();
					}
					return super.onOptionsItemSelected(menuItem);
				}
			}
		}
	}

	/**
	 * Starts a new game by building a new grid at the specified size.
	 * 
	 * @param gridSize
	 *            The grid size of the new puzzle.
	 * @param hideOperators
	 *            True in case operators should be hidden in the new puzzle.
	 */
	public void startNewGame(int gridSize, boolean hideOperators,
			PuzzleComplexity puzzleComplexity) {
		if (mPuzzleFragment != null) {
			mPuzzleFragment.prepareLoadNewGame();
		}

		// Start a background task to generate the new grid. As soon as the new
		// grid is created, the method onNewGridReady will be called.
		mDialogPresentingGridGenerator = new DialogPresentingGridGenerator(
				this, gridSize, hideOperators, puzzleComplexity,
				Util.getPackageVersionNumber());
		mDialogPresentingGridGenerator.execute();
	}

	/**
	 * Reactivate the main ui after a new game is loaded into the grid view by
	 * the ASync GridGenerator task.
	 */
	public void onNewGridReady(final Grid newGrid) {
		// The background task for creating a new grid has been finished.
		mDialogPresentingGridGenerator = null;
		initializePuzzleFragment(newGrid.getSolvingAttemptId());
	}

	/**
	 * Displays the Help Dialog.
	 * 
	 * @param displayLeadIn
	 *            True to display the welcome message in the help.
	 */
	private void openHelpDialog(boolean displayLeadIn) {
		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.puzzle_help_dialog, null);

		TextView tv = (TextView) view
				.findViewById(R.id.puzzle_help_dialog_leadin);
		tv.setVisibility(displayLeadIn ? View.VISIBLE : View.GONE);

		tv = (TextView) view.findViewById(R.id.dialog_help_version_body);
		tv.setText(Util.getPackageVersionName() + " (revision "
				+ Util.getPackageVersionNumber() + ")");

		tv = (TextView) view.findViewById(R.id.help_project_home_link);
		tv.setText(Util.PROJECT_HOME);

		final PuzzleFragmentActivity puzzleFragmentActivity = this;
		new AlertDialog.Builder(puzzleFragmentActivity)
				.setTitle(
						getResources().getString(R.string.application_name)
								+ (DevelopmentHelper.mMode == Mode.DEVELOPMENT ? " r"
										+ Util.getPackageVersionNumber() + " "
										: " ")
								+ getResources()
										.getString(R.string.action_help))
				.setIcon(R.drawable.icon)
				.setView(view)
				.setNeutralButton(R.string.puzzle_help_dialog_neutral_button,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								puzzleFragmentActivity.openChangesDialog(false);
							}
						})
				.setNegativeButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	/**
	 * Displays the changes dialog. As from version 1.96 the changes itself are
	 * no longer described in this dialog. Instead a reference to the web site
	 * is shown.
	 */
	private void openChangesDialog(boolean showLeadInVersion2) {
		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.changelog_dialog, null);

		TextView textView = (TextView) view
				.findViewById(R.id.changelog_version_body);
		textView.setText(Util.getPackageVersionName() + " (revision "
				+ Util.getPackageVersionNumber() + ")");

		if (showLeadInVersion2) {
			textView = (TextView) view
					.findViewById(R.id.changelog_lead_in_version_2);
			textView.setVisibility(View.VISIBLE);
		}

		textView = (TextView) view.findViewById(R.id.changelog_changes_link);
		textView.setText(Util.PROJECT_HOME + "changes.php");

		textView = (TextView) view.findViewById(R.id.changelog_issues_link);
		textView.setText(Util.PROJECT_HOME + "issues.php");

		new AlertDialog.Builder(this)
				.setTitle(
						getResources().getString(R.string.application_name)
								+ (DevelopmentHelper.mMode == Mode.DEVELOPMENT ? " r"
										+ Util.getPackageVersionNumber() + " "
										: " ")
								+ getResources().getString(
										R.string.changelog_title))
				.setIcon(R.drawable.icon)
				.setView(view)
				.setNegativeButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							@Override
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
	private boolean isUpgradeRunning() {
		if (mGameFileConverter != null) {
			// Phase 1 of the upgrade is not yet completed. The upgrade process
			// should not be restarted till the phase 1 background process is
			// completed.
			return true;
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

			return true;
		}

		return false;
	}

	/**
	 * Finishes the upgrading process after the game files have been converted.
	 * 
	 * @param previousInstalledVersion
	 *            Latest version of MathDoku which was actually used.
	 * @param currentVersion
	 *            Current (new) revision number of MathDoku.
	 */
	public void upgradePhase2_UpdatePreferences(int previousInstalledVersion,
			int currentVersion) {
		// The game file converter process has been completed. Reset it in order
		// to prevent restarting the game file conversion after a configuration
		// change.
		mGameFileConverter = null;

		// Update preferences
		mMathDokuPreferences.upgrade(previousInstalledVersion, currentVersion);

		// Show help dialog after new/fresh install or changes dialog
		// otherwise.
		if (previousInstalledVersion == -1) {
			// At clean install display the create-new-game-dialog and on top of
			// it show the help-dialog.
			showDialogNewGame(false);
			openHelpDialog(true);
		} else if (previousInstalledVersion <= 286) {
			// On upgrade from a MathDoku version 1.x revision to a MathDoku
			// version 2.x revision the history and the last played game are
			// lost. Show the create-new-game-dialog and on top of it show a
			// dialog to welcome.
			showDialogNewGame(false);
			openChangesDialog(true);
		} else if (previousInstalledVersion < currentVersion) {
			// Restart the last game and show the changes dialog on top of it.
			restartLastGame();
			openChangesDialog(false);
		} else {
			// No upgrade was needed. Restart the last game
			restartLastGame();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mActionBarDrawerToggle != null) {
			mActionBarDrawerToggle.onConfigurationChanged(newConfig);
		}
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
	public void onGridFinishedListener(int solvingAttemptId) {
		// Once the grid has been solved, the archive fragment has to be
		// displayed.
		initializeArchiveFragment(solvingAttemptId);

		// Update the actions available in the action bar.
		invalidateOptionsMenu();

		// Enable the statistics as soon as the first game has been
		// finished.
		if (mMathDokuPreferences.isStatisticsAvailable() == false) {
			mMathDokuPreferences.setStatisticsAvailable();
			setNavigationDrawer();
		}
		if (TipStatistics.toBeDisplayed(mMathDokuPreferences)) {
			new TipStatistics(this).show();
		}

		// Enable the archive as soon as 5 games have been solved. Note: as the
		// gird is actually not yet saved in the database at this moment the
		// check on the number of completed games is lowered with 1.
		if (mMathDokuPreferences.isArchiveAvailable() == false
				&& new GridDatabaseAdapter().countGrids(StatusFilter.SOLVED,
						SizeFilter.ALL) >= 4) {
			mMathDokuPreferences.setArchiveVisible();
			setNavigationDrawer();
		}
		if (TipArchiveAvailable.toBeDisplayed(mMathDokuPreferences)) {
			new TipArchiveAvailable(this).show();
		}
	}

	/**
	 * Initializes the puzzle fragment. The archive fragment will be disabled.
	 */
	private void initializePuzzleFragment(int solvingAttemptId) {
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
		fragmentTransaction.replace(R.id.content_frame, mPuzzleFragment);
		fragmentTransaction.commit();
		fragmentManager.executePendingTransactions();

		// Disable the archive fragment
		mArchiveFragment = null;
	}

	/**
	 * Initializes the archive fragment. The puzzle fragment will be disabled.
	 */
	private void initializeArchiveFragment(int solvingAttemptId) {
		// Set the archive fragment
		mArchiveFragment = new ArchiveFragment();
		Bundle args = new Bundle();
		args.putInt(ArchiveFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID,
				solvingAttemptId);
		mArchiveFragment.setArguments(args);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.content_frame, mArchiveFragment)
				.commit();
		fragmentManager.executePendingTransactions();

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
					initializePuzzleFragment(solvingAttemptId);
				} else {
					initializeArchiveFragment(solvingAttemptId);
				}
			} catch (InvalidGridException e) {
				if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
					Log.e(TAG,
							"PuzzleFragmentActivity.restartLastGame can not load solvingAttempt with id '"
									+ solvingAttemptId + "'.");
				}
			}
		} else {
			showDialogNewGame(false);
		}
	}

	/**
	 * Shows the dialog in which the parameters have to specified which will be
	 * used to create the new game.
	 * 
	 * @param cancelable
	 *            True in case the dialog can be cancelled.
	 */
	private void showDialogNewGame(final boolean cancelable) {
		// Get view and put relevant information into the view.
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View view = layoutInflater.inflate(R.layout.puzzle_parameter_dialog,
				null);

		// Get views for the puzzle generating parameters
		final Spinner puzzleParameterSizeSpinner = (Spinner) view
				.findViewById(R.id.puzzleParameterSizeSpinner);
		final CheckBox puzzleParameterDisplayOperatorsCheckBox = (CheckBox) view
				.findViewById(R.id.puzzleParameterDisplayOperatorsCheckBox);
		final RatingBar puzzleParameterDifficultyRatingBar = (RatingBar) view
				.findViewById(R.id.puzzleParameterDifficultyRatingBar);

		// Create the list of available puzzle sizes.
		String[] puzzleSizes = { "4x4", "5x5", "6x6", "7x7", "8x8", "9x9" };
		final int OFFSET_INDEX_TO_GRID_SIZE = 4;

		// Populate the spinner. Initial value is set to value used for
		// generating the previous puzzle.
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, puzzleSizes);
		adapterStatus
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		puzzleParameterSizeSpinner.setAdapter(adapterStatus);

		// Restore value which were used when generating the previous game
		puzzleParameterSizeSpinner.setSelection(mMathDokuPreferences
				.getPuzzleParameterSize() - OFFSET_INDEX_TO_GRID_SIZE);
		puzzleParameterDisplayOperatorsCheckBox.setChecked(mMathDokuPreferences
				.getPuzzleParameterOperatorsVisible());
		switch (mMathDokuPreferences.getPuzzleParameterComplexity()) {
		case VERY_EASY:
			puzzleParameterDifficultyRatingBar.setRating(1);
			break;
		case EASY:
			puzzleParameterDifficultyRatingBar.setRating(2);
			break;
		case NORMAL:
			puzzleParameterDifficultyRatingBar.setRating(3);
			break;
		case DIFFICULT:
			puzzleParameterDifficultyRatingBar.setRating(4);
			break;
		case VERY_DIFFICULT:
			puzzleParameterDifficultyRatingBar.setRating(5);
			break;
		}

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_puzzle_parameters_title)
				.setView(view).setCancelable(cancelable);
		if (cancelable) {
			alertDialogBuilder.setNegativeButton(
					R.string.dialog_general_button_cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// do nothing
						}
					});
		}
		alertDialogBuilder.setNeutralButton(
				R.string.dialog_puzzle_parameters_neutral_button,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Transform size spinner to grid size
						int gridSize = (int) puzzleParameterSizeSpinner
								.getSelectedItemId()
								+ OFFSET_INDEX_TO_GRID_SIZE;

						// Transform rating to puzzle complexity.
						int rating = Math
								.round(puzzleParameterDifficultyRatingBar
										.getRating());
						PuzzleComplexity puzzleComplexity;
						if (rating >= 5) {
							puzzleComplexity = PuzzleComplexity.VERY_DIFFICULT;
						} else if (rating >= 4) {
							puzzleComplexity = PuzzleComplexity.DIFFICULT;
						} else if (rating >= 3) {
							puzzleComplexity = PuzzleComplexity.NORMAL;
						} else if (rating >= 2) {
							puzzleComplexity = PuzzleComplexity.EASY;
						} else {
							puzzleComplexity = PuzzleComplexity.VERY_EASY;
						}

						// Store current settings in the preferences
						mMathDokuPreferences.setPuzzleParameterSize(gridSize);
						mMathDokuPreferences
								.setPuzzleParameterOperatorsVisible(puzzleParameterDisplayOperatorsCheckBox
										.isChecked());
						mMathDokuPreferences
								.setPuzzleParameterComplexity(puzzleComplexity);

						// Start a new game with specified parameters
						startNewGame(gridSize,
								(puzzleParameterDisplayOperatorsCheckBox
										.isChecked() == false),
								puzzleComplexity);
					}
				}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == REQUEST_ARCHIVE && resultCode == RESULT_OK) {
			// When returning from the archive with result code OK, the grid
			// which was displayed in the archive will be reloaded if the
			// returned solving attempt can be loaded.
			if (intent != null) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					int solvingAttemptId = bundle
							.getInt(ArchiveFragmentActivity.BUNDLE_KEY_SOLVING_ATTEMPT_ID);
					if (solvingAttemptId >= 0) {
						// In onActivityResult fragments can not be manipulated
						// as this results in IllegalStateException [Can not
						// perform this action after onSaveInstanceState].
						// Therefore the actual replaying is postponed till
						// onResume is called.
						mOnResumeReplaySolvingAttempt = solvingAttemptId;
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent != null) {
			Bundle bundle = intent.getExtras();

			if (bundle != null) {
				if (bundle
						.getBoolean(SharedPuzzleActivity.RESTART_LAST_GAME_SHARED_PUZZLE)) {
					restartLastGame();
				}
			}
		}

		super.onNewIntent(intent);
	}

	/**
	 * The grid for the given solving attempt has to be replayed.
	 * 
	 * @param solvingAttemptId
	 *            The solving attempt for which the grid has to be replayed.
	 * @return
	 */
	private boolean replayPuzzle(int solvingAttemptId) {
		// Load the grid for the returned solving attempt id and reset the grid
		// so it can be replayed.
		Grid grid = new Grid();
		if (grid.load(solvingAttemptId)) {
			if (grid.isActive() == false) {
				grid.replay();
			}

			initializePuzzleFragment(grid.getSolvingAttemptId());
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Reload the finished game currently displayed.
	 * 
	 * @param view
	 */
	public void onClickReloadGame(View view) {
		if (mArchiveFragment != null) {
			replayPuzzle(mArchiveFragment.getSolvingAttemptId());
		}
	}

	/* The click listener for the list view in the navigation drawer */
	private class NavigationDrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (mNavigationDrawerItems[position].equals(getResources()
					.getString(R.string.action_archive))) {
				Intent intentArchive = new Intent(PuzzleFragmentActivity.this,
						ArchiveFragmentActivity.class);
				startActivityForResult(intentArchive, REQUEST_ARCHIVE);
			} else if (mNavigationDrawerItems[position].equals(getResources()
					.getString(R.string.action_statistics))) {
				Intent intentStatistics = new Intent(
						PuzzleFragmentActivity.this,
						StatisticsFragmentActivity.class);
				startActivity(intentStatistics);
			}
			mDrawerLayout.closeDrawer(mDrawerListView);
		}
	}

	/**
	 * Set the navigation drawer. The drawer can be open in following ways: -
	 * tapping the drawer or the app icon - tapping the left side of the screen.
	 * 
	 * The drawer icon will only be visible as soon as the archive or the
	 * statistics are unlocked. From that moment it will be possible to open the
	 * drawer by tapping the drawer or the app icon.
	 * 
	 * It is not possible to disable the navigation drawer entirely in case the
	 * archive and statistics are not yet unlocked. To prevent showing an empty
	 * drawer, the puzzle activity itself will always be displayed as a
	 * navigation item. In case the user opens the drawer accidently by tapping
	 * the left side of the screen before the archive or statistics are unlocked
	 * it will be less confusing.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setNavigationDrawer() {
		// Determine the item which have to be shown in the drawer.
		boolean openDrawer = false;
		boolean mDrawerIconVisible = false;
		ArrayList<String> navigationDrawerItems = new ArrayList<String>();
		navigationDrawerItems.add(getResources().getString(
				R.string.action_bar_subtitle_puzzle_fragment));
		if (mMathDokuPreferences.isArchiveAvailable()) {
			String string = getResources().getString(R.string.action_archive);
			navigationDrawerItems.add(string);
			if (openDrawer == false && mNavigationDrawerItems != null) {
				openDrawer = (Arrays.asList(mNavigationDrawerItems).contains(
						string) == false);
			}
			mDrawerIconVisible = true;
		}
		if (mMathDokuPreferences.isStatisticsAvailable()) {
			String string = getResources()
					.getString(R.string.action_statistics);
			navigationDrawerItems.add(string);
			if (openDrawer == false && mNavigationDrawerItems != null) {
				openDrawer = (Arrays.asList(mNavigationDrawerItems).contains(
						string) == false);
			}
			mDrawerIconVisible = true;
		}
		mNavigationDrawerItems = navigationDrawerItems
				.toArray(new String[navigationDrawerItems.size()]);

		// Set up the action bar for displaying the drawer icon and making the
		// app icon clickable in order to display the drawer.
		final ActionBar actionBar = getActionBar();
		if (actionBar != null && mDrawerIconVisible) {
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				actionBar.setHomeButtonEnabled(true);
			}
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Set up the navigation drawer.
		mDrawerLayout = (DrawerLayout) findViewById(R.id.puzzle_activity_drawer_layout);
		mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.navigation_drawer_open,
				R.string.navigation_drawer_close) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.support.v4.app.ActionBarDrawerToggle#onDrawerClosed(android
			 * .view.View)
			 */
			@Override
			public void onDrawerClosed(View view) {
				// Update the options menu with relevant options.
				invalidateOptionsMenu();

				// Reset the subtitle
				actionBar.setSubtitle(getResources().getString(
						R.string.action_bar_subtitle_puzzle_fragment));
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.support.v4.app.ActionBarDrawerToggle#onDrawerOpened(android
			 * .view.View)
			 */
			@Override
			public void onDrawerOpened(View drawerView) {
				// Update the options menu with relevant options.
				invalidateOptionsMenu();

				// Remove the subtitle
				actionBar.setSubtitle(null);
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

		// Get list view for drawer
		mDrawerListView = (ListView) findViewById(R.id.left_drawer);

		mDrawerListView.setBackgroundColor(Painter.getInstance()
				.getNavigationDrawerPainter().getBackgroundColor());

		// Set the adapter for the list view containing the navigation items
		mDrawerListView.setAdapter(new ArrayAdapter<String>(this,
				R.layout.navigation_drawer_list_item, mNavigationDrawerItems));

		// Set the list's click listener
		mDrawerListView
				.setOnItemClickListener(new NavigationDrawerItemClickListener());

		// Select the the play puzzle item as active item.
		mDrawerListView.setItemChecked(0, true);

		if (openDrawer) {
			mDrawerLayout.openDrawer(mDrawerListView);
		}
		mDrawerLayout.invalidate();
	}

	public void onCancelGridGeneration() {
		// The background task for creating a new grid has been finished.
		mDialogPresentingGridGenerator = null;

		if (mPuzzleFragment != null) {
			mPuzzleFragment.startTimer();
		}
	}
}