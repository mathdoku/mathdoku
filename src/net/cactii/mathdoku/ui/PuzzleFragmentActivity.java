package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.InvalidGridException;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.gridGenerating.DialogPresentingGridGenerator;
import net.cactii.mathdoku.gridGenerating.GridGenerator.PuzzleComplexity;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

public class PuzzleFragmentActivity extends AppFragmentActivity implements
		PuzzleFragment.OnGridFinishedListener {
	public final static String TAG = "MathDoku.PuzzleFragmentActivity";

	// Home directory url of promotion website. Most url's used in this app will
	// be forwarded from the promotion website to code.google.com/p/mathdoku.
	public final static String PROJECT_HOME = "http://mathdoku.net/";

	// Background tasks for generating a new puzzle and converting game files
	public DialogPresentingGridGenerator mDialogPresentingGridGenerator;
	public GameFileConverter mGameFileConverter;

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

	// Request code
	private final static int REQUEST_ARCHIVE = 1;

	/** Called when the activity is first created. */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize global objects (singleton instances)
		DatabaseHelper.getInstance(this);
		UsageLog.getInstance(this);
		Painter.getInstance(this);

		// Check if database is consistent.
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			DevelopmentHelper.checkDatabaseConsistency(this);
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
		// Disable or enable the archive and the settings
		menu.findItem(R.id.action_archive).setVisible(
				mMathDokuPreferences.isArchiveAvailable());
		menu.findItem(R.id.action_statistics).setVisible(
				mMathDokuPreferences.isStatisticsAvailable());

		boolean showCheats = false;

		// Set visibility for menu option check progress
		menu.findItem(R.id.checkprogress).setVisible(
				mPuzzleFragment != null && mPuzzleFragment.showCheckProgress());

		// Set visibility for menu option to reveal a cell
		if (mPuzzleFragment != null && mPuzzleFragment.showRevealCell()) {
			menu.findItem(R.id.action_reveal_cell).setVisible(true);
			showCheats = true;
		} else {
			menu.findItem(R.id.action_reveal_cell).setVisible(false);
		}

		// Set visibility for menu option to reveal a operator
		if (mPuzzleFragment != null && mPuzzleFragment.showRevealOperator()) {
			menu.findItem(R.id.action_reveal_operator).setVisible(true);
			showCheats = true;
		} else {
			menu.findItem(R.id.action_reveal_operator).setVisible(false);
		}

		// Set visibility for menu option to reveal a operator
		if (mPuzzleFragment != null && mPuzzleFragment.showRevealSolution()) {
			menu.findItem(R.id.action_show_solution).setVisible(true);
			showCheats = true;
		} else {
			menu.findItem(R.id.action_show_solution).setVisible(false);
		}

		// The cheats menu is only visible in case at lease one submenu item is
		// visible.
		menu.findItem(R.id.action_cheat).setVisible(showCheats);

		// Set visibility for menu option to clear the grid
		menu.findItem(R.id.action_clear_grid).setVisible(
				mPuzzleFragment != null && mPuzzleFragment.showClearGrid());

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
	public boolean onOptionsItemSelected(MenuItem menuItem) {

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
		case R.id.action_archive:
			Intent intentArchive = new Intent(this,
					ArchiveFragmentActivity.class);
			startActivityForResult(intentArchive, REQUEST_ARCHIVE);
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
		// Enable the archive as soon as the second game has been generated.
		if (mMathDokuPreferences.isArchiveAvailable() == false
				&& new GridDatabaseAdapter().countGrids() >= 2) {
			mMathDokuPreferences.setArchiveVisible();
			invalidateOptionsMenu();
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
			// Restart the last game
			restartLastGame();

			// On upgrade of version show changes.
			UsageLog.getInstance().logFunction("ViewChanges.AfterUpgrade");
			this.openChangesDialog();
		} else {
			// Restart the last game
			restartLastGame();
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

		// Commit with allowing state loss. Using normal commit results in a
		// forced close when reloading a finished puzzle from the archive. I am
		// not sure why commitAllowingStateLoss solves this problem.
		fragmentTransaction.commitAllowingStateLoss();

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
	private void showDialogNewGame(boolean cancelable) {
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

		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_puzzle_parameters_title)
				.setView(view)
				.setCancelable(cancelable)
				.setNeutralButton(
						R.string.dialog_puzzle_parameters_neutral_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
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
								mMathDokuPreferences
										.setPuzzleParameterSize(gridSize);
								mMathDokuPreferences
										.setPuzzleParameterOperatorsVisible(puzzleParameterDisplayOperatorsCheckBox
												.isChecked());
								mMathDokuPreferences
										.setPuzzleParameterComplexity(puzzleComplexity);

								// Start a new game with specified parameters
								startNewGame(
										gridSize,
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

						// Load the grid for the returned solving attempt id.
						Grid grid = new Grid();
						if (grid.load(solvingAttemptId)) {
							if (grid.isActive() == false) {
								grid.replay();
							}

							// Either load the grid in the existing puzzle
							// fragment or transform the fragment.
							if (mPuzzleFragment != null) {
								mPuzzleFragment.setNewGrid(grid);
							} else {
								// In case a finished grid is replayed a new
								// solving attempt has been be created for the
								// grid.
								initializePuzzleFragment(
										grid.getSolvingAttemptId(), true);
							}
							return;
						}
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
}