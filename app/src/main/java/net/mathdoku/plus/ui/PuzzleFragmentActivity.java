package net.mathdoku.plus.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.games.GamesClient;

import net.mathdoku.plus.R;
import net.mathdoku.plus.archive.ui.ArchiveFragment;
import net.mathdoku.plus.archive.ui.ArchiveFragmentActivity;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.developmenthelper.DevelopmentHelper;
import net.mathdoku.plus.enums.GridType;
import net.mathdoku.plus.enums.GridTypeFilter;
import net.mathdoku.plus.enums.PuzzleComplexity;
import net.mathdoku.plus.enums.StatusFilter;
import net.mathdoku.plus.gridconverting.GridConverter;
import net.mathdoku.plus.gridgenerating.GeneratePuzzleProgressDialog;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.gridgenerating.GridGeneratingParametersBuilder;
import net.mathdoku.plus.leaderboard.LeaderboardConnector;
import net.mathdoku.plus.leaderboard.LeaderboardRankUpdater;
import net.mathdoku.plus.leaderboard.LeaderboardType;
import net.mathdoku.plus.leaderboard.ui.LeaderboardOverviewActivity;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridLoader;
import net.mathdoku.plus.puzzle.ui.GridInputMode;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.statistics.ui.StatisticsFragmentActivity;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter.ScoreOrigin;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRow;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankRowBuilder;
import net.mathdoku.plus.storage.databaseadapter.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.selector.ArchiveSolvingAttemptSelector;
import net.mathdoku.plus.tip.TipArchiveAvailable;
import net.mathdoku.plus.tip.TipDialog;
import net.mathdoku.plus.tip.TipStatistics;
import net.mathdoku.plus.ui.base.GooglePlayServiceFragmentActivity;
import net.mathdoku.plus.util.FeedbackEmail;
import net.mathdoku.plus.util.SharedPuzzle;
import net.mathdoku.plus.util.Util;
import net.mathdoku.plus.util.VersionInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PuzzleFragmentActivity extends GooglePlayServiceFragmentActivity implements PuzzleFragment
        .PuzzleFragmentListener {
    @SuppressWarnings("unused")
    private static final String TAG = PuzzleFragmentActivity.class.getName();

    // Intent parameters for creating a new game of specified type
    private static final String NEW_PUZZLE_FOR_LEADERBOARD = "CreateNewGameForLeaderboard";
    private static final String NEW_PUZZLE_FOR_LEADERBOARD_GRID_SIZE = "CreateNewGameForLeaderboard_Size";
    private static final String NEW_PUZZLE_FOR_LEADERBOARD_HIDE_OPERATORS = "CreateNewGameForLeaderboard_HideOperators";
    private static final String NEW_PUZZLE_FOR_LEADERBOARD_PUZZLE_COMPLEXITY =
            "CreateNewGameForLeaderboard_PuzzleComplexity";

    // Background tasks for generating a new puzzle and converting game files
    public GeneratePuzzleProgressDialog mGeneratePuzzleProgressDialog;
    private GridConverter mGridConverter;

    // Different types of fragments supported by this activity.
    public enum FragmentType {
        NO_FRAGMENT,
        PUZZLE_FRAGMENT,
        ARCHIVE_FRAGMENT
    }

    // Current type of fragment being active
    private FragmentType mActiveFragmentType;

    // Reference to fragments which can be displayed in this activity.
    private PuzzleFragment mPuzzleFragment;
    private ArchiveFragment mArchiveFragment;

    // References to the navigation drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private ListView mDrawerListView;
    private String[] mNavigationDrawerItems;

    // Reference to the leaderboard
    private LeaderboardConnector mLeaderboardConnector;

    // Object to save data on a configuration change. Note: for the puzzle
    // fragment the RetainInstance property is set to true.
    private class ConfigurationInstanceState {
        private final GeneratePuzzleProgressDialog mGeneratePuzzleProgressDialog;
        private final GridConverter mGridConverter;

        public ConfigurationInstanceState(GeneratePuzzleProgressDialog gridGeneratorTask,
                                          GridConverter gridConverterTask) {
            mGeneratePuzzleProgressDialog = gridGeneratorTask;
            mGridConverter = gridConverterTask;
        }

        public GeneratePuzzleProgressDialog getGridGeneratorTask() {
            return mGeneratePuzzleProgressDialog;
        }

        public GridConverter getGameFileConverter() {
            return mGridConverter;
        }
    }

    // Request code
    private static final int REQUEST_ARCHIVE = 1;

    // When using the support package, onActivityResult is called before
    // onResume. As a result fragment can not be manipulated in the
    // onActivityResult. If following variable contains a value >=0 the solving
    // attempt with this specific number should be replayed in the onResume
    // call.
    private int mOnResumeReplaySolvingAttempt;
    private boolean mOnResumeRestartLastGame;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableDebugLog(LeaderboardConnector.DEBUG, TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puzzle_activity_fragment);

        // Check if database is consistent.
        if (Config.APP_MODE == AppMode.DEVELOPMENT) {
            if (!DevelopmentHelper.checkDatabaseConsistency(this)) {
                // Skip remainder of onCreate because further database access
                // can result in a forced close.
                return;
            }
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setSubtitle(getResources().getString(R.string.action_bar_subtitle_puzzle_fragment));
        }

        // Set up the navigation drawer.
        setNavigationDrawer();

        // Restore the last configuration instance which was saved before the
        // configuration change.
        Object object = this.getLastCustomNonConfigurationInstance();
        if (object != null && object.getClass() == ConfigurationInstanceState.class) {
            ConfigurationInstanceState configurationInstanceState = (ConfigurationInstanceState) object;

            // Restore background process if running.
            mGeneratePuzzleProgressDialog = configurationInstanceState.getGridGeneratorTask();
            if (mGeneratePuzzleProgressDialog != null) {
                mGeneratePuzzleProgressDialog.attachToActivity(this)
                        .show();
            }

            // Restore background process if running.
            mGridConverter = configurationInstanceState.getGameFileConverter();
            if (mGridConverter != null) {
                mGridConverter.attachToActivity(this);
            }
        }

        // Check if app needs to be upgraded. If not, restart the last game.
        if (!isUpgradeRunning()) {
            restartLastGame();
        }

        // At this point there will never be a need to replay a solving attempt.
        mOnResumeReplaySolvingAttempt = -1;
        mOnResumeRestartLastGame = false;
    }

    @Override
    public void onResume() {
        if (mGeneratePuzzleProgressDialog != null) {
            // In case the grid is created in the background and the dialog is
            // closed, the activity will be moved to the background as well. In
            // case the user starts this app again onResume is called but
            // onCreate isn't. So we have to check here as well.
            mGeneratePuzzleProgressDialog.attachToActivity(this)
                    .show();
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
        if (mOnResumeRestartLastGame) {
            restartLastGame();
            mOnResumeRestartLastGame = false;
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

        if (Config.APP_MODE == AppMode.DEVELOPMENT) {
            inflater.inflate(R.menu.development_mode_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the navigation drawer is open, hide action items related to the
        // content view
        boolean drawerOpen = !(mDrawerLayout == null || mDrawerListView == null) && mDrawerLayout.isDrawerOpen(
                mDrawerListView);

        // Determine which fragment is active
        boolean showCheats = false;

        // Set visibility for menu option input mode
        MenuItem menuItem = menu.findItem(R.id.action_input_mode);
        if (menuItem != null) {
            menuItem.setVisible(!drawerOpen && mActiveFragmentType == FragmentType.PUZZLE_FRAGMENT &&
                                        mPuzzleFragment != null && mPuzzleFragment.isActive());
            if (menuItem.isVisible()) {
                menuItem.setIcon(mPuzzleFragment.getActionCurrentInputModeIconResId())
                        .setTitle(mPuzzleFragment.getActionCurrentInputModeTitleResId());
            }
        }

        // Set visibility for menu option copy cell values
        if ((menuItem = menu.findItem(R.id.action_copy_cell_values)) != null) {
            menuItem.setVisible(!drawerOpen && mActiveFragmentType == FragmentType.PUZZLE_FRAGMENT &&
                                        mPuzzleFragment != null && mPuzzleFragment.showCopyCellValues());
        }

        // Set visibility for menu option check progress
        if ((menuItem = menu.findItem(R.id.checkprogress)) != null) {
            menuItem.setVisible(!drawerOpen && mActiveFragmentType == FragmentType.PUZZLE_FRAGMENT &&
                                        mPuzzleFragment != null && mPuzzleFragment.showCheckProgress());
        }

        // Set visibility for menu option to reveal a cell
        if ((menuItem = menu.findItem(R.id.action_reveal_cell)) != null) {
            menuItem.setVisible(!drawerOpen && mActiveFragmentType == FragmentType.PUZZLE_FRAGMENT &&
                                        mPuzzleFragment != null && mPuzzleFragment.showRevealCell());
            if (menuItem.isVisible()) {
                showCheats = true;
            }
        }

        // Set visibility for menu option to reveal a operator
        if ((menuItem = menu.findItem(R.id.action_reveal_operator)) != null) {
            menuItem.setVisible(!drawerOpen && mActiveFragmentType == FragmentType.PUZZLE_FRAGMENT &&
                                        mPuzzleFragment != null && mPuzzleFragment.showRevealOperator());
            if (menuItem.isVisible()) {
                showCheats = true;
            }
        }

        // Set visibility for menu option to reveal the solution
        if ((menuItem = menu.findItem(R.id.action_show_solution)) != null) {
            menuItem.setVisible(!drawerOpen && mActiveFragmentType == FragmentType.PUZZLE_FRAGMENT &&
                                        mPuzzleFragment != null && mPuzzleFragment.showRevealSolution());
            if (menuItem.isVisible()) {
                showCheats = true;
            }
        }

        // The cheats menu is only visible in case at least one sub menu item is
        // visible. Note: the item does not exist when running on Android 3 as
        // that version does not allow sub menu's.
        if ((menuItem = menu.findItem(R.id.action_cheat)) != null) {
            menuItem.setVisible(!drawerOpen && showCheats);
        }

        // Set visibility for menu option to clear the grid
        if ((menuItem = menu.findItem(R.id.action_clear_grid)) != null) {
            menuItem.setVisible(!drawerOpen && mActiveFragmentType == FragmentType.PUZZLE_FRAGMENT &&
                                        mPuzzleFragment != null && mPuzzleFragment.showClearGrid());
        }

        // Determine position of new game button
        if ((menuItem = menu.findItem(R.id.action_new_game)) != null) {
            menuItem.setVisible(!drawerOpen)
                    .setShowAsAction(
                            mPuzzleFragment != null && mPuzzleFragment.isActive() ? MenuItem.SHOW_AS_ACTION_NEVER :
                                    MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        // Display the share button on the action bar dependent on the fragment
        // being showed.
        if ((menuItem = menu.findItem(R.id.action_share)) != null) {
            menuItem.setVisible(!drawerOpen)
                    .setShowAsAction(
                            mArchiveFragment != null ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER);
        }

        // The replay button on the action bar is only visible in case the
        // archive fragment is shown. This action is never visible in case the
        // puzzle fragment is display as it would duplicate the clear grid
        // option.
        if ((menuItem = menu.findItem(R.id.action_replay)) != null) {
            menuItem.setVisible(!drawerOpen && mArchiveFragment != null);
        }

        // Determine visibility of sign out button
        if ((menuItem = menu.findItem(R.id.action_sign_out_google_play_services)) != null) {
            menuItem.setVisible(mLeaderboardConnector != null && mLeaderboardConnector.isSignedIn());
        }

        // When running in development mode, an extra menu is available.
        if (Config.APP_MODE == AppMode.DEVELOPMENT) {
            if ((menuItem = menu.findItem(R.id.menu_development_mode)) != null) {
                menuItem.setVisible(true);
            }
            if ((menuItem = menu.findItem(R.id.development_mode_leaderboard_menu)) != null) {
                menuItem.setVisible(mArchiveFragment != null);
            }
            if ((menuItem = menu.findItem(R.id.development_mode_submit_manual_score)) != null) {
                menuItem.setVisible(mArchiveFragment != null);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // First pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mActionBarDrawerToggle != null && mActionBarDrawerToggle.onOptionsItemSelected(menuItem)) {
            return true;
        }

        int menuId = menuItem.getItemId();
        switch (menuId) {
            case R.id.action_new_game:
                showDialogNewGame(true);
                return true;
            case R.id.action_input_mode:
                if (mPuzzleFragment != null) {
                    mPuzzleFragment.toggleInputMode();
                }
                return true;
            case R.id.action_copy_cell_values:
                if (mPuzzleFragment != null) {
                    mPuzzleFragment.copyCellValues();
                }
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
            case R.id.action_replay:
                if (mArchiveFragment != null) {
                    openReplayDialog(mArchiveFragment.getSolvingAttemptId());
                }
                return true;
            case R.id.action_clear_grid:
                if (mPuzzleFragment != null) {
                    mPuzzleFragment.openClearGridDialog();
                }
                return true;
            case R.id.action_share:
                if (mPuzzleFragment != null) {
                    new SharedPuzzle(this).share(mPuzzleFragment.getSolvingAttemptId());
                } else if (mArchiveFragment != null) {
                    // Only the archive fragment possibly contains the statistics
                    // views which can be enclosed as attachments to the share
                    // email.
                    new SharedPuzzle(this).addStatisticsChartsAsAttachments(this.getWindow()
                                                                                    .getDecorView())
                            .share(mArchiveFragment.getSolvingAttemptId());
                }
                return true;
            case R.id.action_sign_out_google_play_services:
                // Sign out of Google Play Services
                signOut();

                // Clear the leaderboard.
                mLeaderboardConnector = null;
                return true;
            case R.id.action_google_plus_community:
                startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                                         Uri.parse(getResources().getString(R.string.google_plus_community_url))));
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
                if (mArchiveFragment != null && mArchiveFragment.onOptionsItemSelected(menuItem)) {
                    return true;
                }

                // When running in development mode it should be checked whether a
                // development menu item was selected.
                if (Config.APP_MODE == AppMode.DEVELOPMENT) {
                    if (mPuzzleFragment != null) {
                        // Cancel old timer
                        mPuzzleFragment.stopTimer();
                    }

                    boolean processedByDevelopmentHelper = DevelopmentHelper.onDevelopmentHelperOption(this, menuId,
                                                                                                       getActiveFragmentGrid());
                    if (mPuzzleFragment != null) {
                        mPuzzleFragment.startTimer();
                    }
                    if (processedByDevelopmentHelper) {
                        return true;
                    }
                }
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private Grid getActiveFragmentGrid() {
        if (mPuzzleFragment != null) {
            return mPuzzleFragment.getGrid();
        } else if (mArchiveFragment != null) {
            return mArchiveFragment.getGrid();
        }
        return null;
    }

    /**
     * Starts a new game by building a new grid at the specified size.
     *
     * @param gridType
     *         The grid size of the new puzzle.
     * @param hideOperators
     *         True in case operators should be hidden in the new puzzle.
     */
    public void startNewGame(GridType gridType, boolean hideOperators, PuzzleComplexity puzzleComplexity) {
        if (mPuzzleFragment != null) {
            mPuzzleFragment.prepareLoadNewGame();
        }

        // Start a background task to generate the new grid. As soon as the new
        // grid is created, the method onNewGridReady will be called.
        mGeneratePuzzleProgressDialog = new GeneratePuzzleProgressDialog(this, createGridGeneratingParameters(gridType,
                                                                                                              hideOperators,
                                                                                                              puzzleComplexity));
        mGeneratePuzzleProgressDialog.show();
    }

    private GridGeneratingParameters createGridGeneratingParameters(GridType gridType, boolean hideOperators,
                                                                    PuzzleComplexity puzzleComplexity) {
        GridGeneratingParametersBuilder gridGeneratingParametersBuilder = new GridGeneratingParametersBuilder()
                .setGridType(
                gridType)
                .setHideOperators(hideOperators);
        if (puzzleComplexity == PuzzleComplexity.RANDOM) {
            gridGeneratingParametersBuilder.setRandomPuzzleComplexity();
        } else {
            gridGeneratingParametersBuilder.setPuzzleComplexity(puzzleComplexity);
        }
        return gridGeneratingParametersBuilder.createGridGeneratingParameters();
    }

    /**
     * Reactivate the main ui after a new puzzle is created.
     */
    public void onNewGridReady(final Grid newGrid) {
        // The background task for creating a new grid has been finished.
        mGeneratePuzzleProgressDialog = null;

        // Initializes a new puzzle fragment
        initializePuzzleFragment(newGrid.getSolvingAttemptId());

        // Reset preferences regarding the input mode of the puzzle
        mMathDokuPreferences.setGridInputMode(false, GridInputMode.NORMAL);
    }

    /**
     * Displays the Help Dialog.
     *
     * @param cleanInstall
     *         True in case the help dialog is displayed after a clean install. False in case of an upgrade.
     */
    private void openHelpDialog(boolean cleanInstall) {
        // Get view and put relevant information into the view.
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.puzzle_help_dialog, null);
        if (view == null) {
            return;
        }

        TextView tv;
        if ((tv = (TextView) view.findViewById(R.id.puzzle_help_dialog_leadin)) != null) {
            tv.setVisibility(cleanInstall ? View.VISIBLE : View.GONE);
        }

        VersionInfo versionInfo = new VersionInfo(this);
        if ((tv = (TextView) view.findViewById(R.id.dialog_help_version_body)) != null) {
            tv.setText(versionInfo.getName() + " (revision " + versionInfo.getVersionNumber() + ")");
        }

        if ((tv = (TextView) view.findViewById(R.id.help_project_home_link)) != null) {
            tv.setText(Util.PROJECT_HOME);
        }

        final PuzzleFragmentActivity puzzleFragmentActivity = this;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(puzzleFragmentActivity);
        alertDialogBuilder.setTitle(getResources().getString(
                R.string.application_name) + (Config.APP_MODE == AppMode.DEVELOPMENT ? " r" + versionInfo
                .getVersionNumber() + " " : " ") + getResources().getString(R.string.action_help))
                .setIcon(R.drawable.icon)
                .setView(view)
                .setNegativeButton(R.string.dialog_general_button_close, new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                       }
                                   });
        alertDialogBuilder.show();
    }

    /**
     * Checks whether a new version of the game has been installed. If so, modify preferences and convert if necessary.
     */
    private boolean isUpgradeRunning() {
        if (mGridConverter != null) {
            // Phase 1 of the upgrade is not yet completed. The upgrade process
            // should not be restarted till the phase 1 background process is
            // completed.
            return true;
        }

        // Get current version number from the package.
        int packageVersionNumber = new VersionInfo(this).getVersionNumber();

        // Get the previous installed version from the preferences.
        int previousInstalledVersion = mMathDokuPreferences.getCurrentInstalledVersion();

        // Start phase 1 of the upgrade process if needed.
        if (previousInstalledVersion < packageVersionNumber) {
            // On Each update of the game, all game data will be converted to
            // the latest definitions. On completion of the game file
            // conversion, method upgradePhase2_UpdatePreferences will be
            // called.
            mGridConverter = new GridConverter(this, previousInstalledVersion, packageVersionNumber);
            mGridConverter.execute();

            return true;
        }

        return false;
    }

    /**
     * Finishes the upgrading process after the game files have been converted.
     *
     * @param previousInstalledVersion
     *         Latest version of MathDoku which was actually used.
     * @param currentVersion
     *         Current (new) revision number of MathDoku.
     */
    public void upgradePhase2(int previousInstalledVersion, int currentVersion) {
        // The game file converter process has been completed. Reset it in order
        // to prevent restarting the game file conversion after a configuration
        // change.
        mGridConverter = null;

        // Update preferences
        mMathDokuPreferences.upgrade(previousInstalledVersion, currentVersion);

        // Initialize new leaderboards
        if (!mMathDokuPreferences.isLeaderboardsInitialized()) {
            Resources resources = getResources();
            String leaderboardId;
            LeaderboardRankDatabaseAdapter leaderboardRankDatabaseAdapter = new LeaderboardRankDatabaseAdapter();
            for (int i = 0; i < LeaderboardType.MAX_LEADERBOARDS; i++) {
                // Get the Google+ leaderboard id
                leaderboardId = resources.getString(LeaderboardType.getResId(i));

                // Create a leaderboard record if currently does not yet exist.
                if (leaderboardRankDatabaseAdapter.get(leaderboardId) == null) {
                    LeaderboardRankRow leaderboardRankRow = new LeaderboardRankRowBuilder(leaderboardId,
                                                                                          LeaderboardType.getGridSize(
                                                                                                  i),
                                                                                          LeaderboardType
                                                                                                  .hasHiddenOperator(
                                                                                                  i),
                                                                                          LeaderboardType
                                                                                                  .getPuzzleComplexity(
                                                                                                  i)).build();
                    leaderboardRankDatabaseAdapter.insert(leaderboardRankRow);
                }
            }
            mMathDokuPreferences.setLeaderboardsInitialized();
        }

        // Show help dialog after new/fresh install or changes dialog
        // otherwise.
        if (previousInstalledVersion == -1) {
            // At clean install display the create-new-game-dialog and on top of
            // it show the help-dialog.
            showDialogNewGame(false);
            openInputMethodDialog();
            openHelpDialog(true);
        } else if (previousInstalledVersion < currentVersion) {
            // Restart the last game and show the changes dialog on top of it.
            restartLastGame();
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
        // http://stackoverflow.com/questions/11591302/unable-to-use-fragment-setretaininstance
        // -as-a-replacement-for-activity-onretai

        // Cleanup
        if (mPuzzleFragment != null) {
            mPuzzleFragment.stopTimer();
        }
        TipDialog.resetDisplayedDialogs();

        if (mGeneratePuzzleProgressDialog != null) {
            // A new grid is generated in the background. Detach the background
            // task from this activity. It will keep on running until finished.
            mGeneratePuzzleProgressDialog.detachFromActivity();
        }
        if (mGridConverter != null) {
            // The game files are converted in the background. Detach the
            // background
            // task from this activity. It will keep on running until finished.
            mGridConverter.detachFromActivity();
        }
        return new ConfigurationInstanceState(mGeneratePuzzleProgressDialog, mGridConverter);
    }

    @Override
    public void onPuzzleFinishedListener(Grid grid) {
        // Once the grid has been solved, the archive fragment has to be
        // displayed.
        initializeArchiveFragment(grid.getSolvingAttemptId());

        // Update the actions available in the action bar.
        invalidateOptionsMenu();

        // Enable the statistics as soon as the first game has been
        // finished.
        if (!mMathDokuPreferences.isStatisticsAvailable()) {
            mMathDokuPreferences.setStatisticsAvailable();
            setNavigationDrawer();
        }
        if (TipStatistics.toBeDisplayed(mMathDokuPreferences)) {
            new TipStatistics(this).show();
        }

        // Enable the archive as soon as 5 games have been solved. Note: as the
        // gird is actually not yet saved in the database at this moment the
        // check on the number of completed games is lowered with 1.
        if (!mMathDokuPreferences.isArchiveAvailable() && new ArchiveSolvingAttemptSelector(StatusFilter.SOLVED,
                                                                                            GridTypeFilter.ALL)
                .countGrids() >= 4) {
            mMathDokuPreferences.setArchiveVisible();
            setNavigationDrawer();
        }
        if (TipArchiveAvailable.toBeDisplayed(mMathDokuPreferences)) {
            new TipArchiveAvailable(this).show();
        }

        // Check whether the grid meets the criteria for submitting to the
        // leaderboards.
        if (isEligibleForScoreSubmission(grid)) {
            GridGeneratingParameters gridGeneratingParameters = grid.getGridGeneratingParameters();

            // Determine the leaderboard for this puzzle
            int leaderboardResId = LeaderboardType.getResId(grid.getGridSize(),
                                                            gridGeneratingParameters.isHideOperators(),
                                                            gridGeneratingParameters.getPuzzleComplexity());
            String leaderboardId = getResources().getString(leaderboardResId);

            // Retrieve the best score for this leaderboard
            LeaderboardRankRow leaderboardRankRow = new LeaderboardRankDatabaseAdapter().get(leaderboardId);

            // Check if a new top score is achieved.
            boolean newTopScore = leaderboardRankRow.getScoreOrigin() == ScoreOrigin.NONE || grid.getElapsedTime() <
                    leaderboardRankRow.getRawScore();

            // Store the top score in the leaderboard table.
            if (newTopScore) {
                leaderboardRankRow = LeaderboardRankRowBuilder.from(leaderboardRankRow)
                        .setScoreLocal(grid.getGridStatistics().mId, grid.getElapsedTime())
                        .build();
                new LeaderboardRankDatabaseAdapter().update(leaderboardRankRow);
            }

            // Submit score to Google+ in case already signed in.
            if (mLeaderboardConnector == null || !mLeaderboardConnector.isSignedIn()) {
                // The user is not logged in to Google Plus. Check whether the
                // sign in dialog should be shown.
                boolean hideTillNextTopScore = mMathDokuPreferences.isHideTillNextTopScoreAchievedChecked();
                if (!hideTillNextTopScore || newTopScore) {
                    // In case the google sign dialog is shown, the score will
                    // be processed after the sign in has succeeded.
                    new GooglePlusSignInDialog(this, new GooglePlusSignInDialog.Listener() {
                        @Override
                        public void onGooglePlusSignInStart() {
                            beginUserInitiatedSignIn();
                        }

                        @Override
                        public void onGooglePlusSignInCancelled() {
                            // Nothing to do here.
                        }
                    }).setMessage(R.string.google_plus_login_dialog_on_game_completion)
                            .displayCheckboxHideTillNextTopScoreAchieved(hideTillNextTopScore)
                            .show();
                }
            } else if (newTopScore) {
                GridStatistics gridStatistics = grid.getGridStatistics();
                if (gridStatistics != null) {
                    mLeaderboardConnector.submitScore(grid.getGridSize(),
                                                      gridGeneratingParameters.getPuzzleComplexity(),
                                                      gridGeneratingParameters.isHideOperators(),
                                                      grid.getElapsedTime());
                }
            }
        }
    }

    private boolean isEligibleForScoreSubmission(Grid grid) {
        if (grid.isActive()) {
            return false;
        }
        if (grid.getCheatPenaltyTime() > 0 || grid.isSolutionRevealed()) {
            return false;
        }
        if (grid.isReplay()) {
            return false;
        }
        if (LeaderboardType.notDefinedForGridSize(grid.getGridSize())) {
            return false;
        }
        return true;
    }

    private void replaceFragment(android.support.v4.app.Fragment fragment) {
        // Replace current fragment with the new puzzle fragment.
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    /**
     * Initializes the puzzle fragment. The archive fragment will be disabled.
     */
    private void initializePuzzleFragment(int solvingAttemptId) {
        // Disable the archive fragment.
        mArchiveFragment = null;

        // Create a new puzzle fragment if needed, else re-use the existing
        // fragment.
        mActiveFragmentType = FragmentType.PUZZLE_FRAGMENT;
        if (mPuzzleFragment == null) {
            mPuzzleFragment = new PuzzleFragment();

            if (solvingAttemptId >= 0) {
                Bundle args = new Bundle();
                args.putInt(PuzzleFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID, solvingAttemptId);
                mPuzzleFragment.setArguments(args);
            }

            replaceFragment(mPuzzleFragment);
        } else {
            // Re-use the current puzzle fragment. The solving attempt should
            // directly be loaded as the view of this fragment already exists.
            //
            // This path can be tested as follows:
            // 1. Display an active puzzle fragment
            // 2. Open a shared puzzle from an email
            // 3. Choose to play this shared puzzle
            mPuzzleFragment.loadSolvingAttempt(solvingAttemptId);
        }
    }

    /**
     * Initializes the archive fragment. The puzzle fragment will be disabled.
     */
    private void initializeArchiveFragment(int solvingAttemptId) {
        // Disable the puzzle fragment
        mPuzzleFragment = null;

        // Create a new archive fragment
        mActiveFragmentType = FragmentType.ARCHIVE_FRAGMENT;
        mArchiveFragment = new ArchiveFragment();

        Bundle args = new Bundle();
        args.putInt(ArchiveFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID, solvingAttemptId);
        mArchiveFragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, mArchiveFragment)
                .commit();
        fragmentManager.executePendingTransactions();
    }

    /**
     * Restart the last game which was played.
     */
    private void restartLastGame() {
        // Determine if and which grid was last played
        int solvingAttemptId = new SolvingAttemptDatabaseAdapter().getMostRecentPlayedId();
        if (solvingAttemptId >= 0) {
            // Load the grid
            try {
                Grid newGrid = new GridLoader().load(solvingAttemptId);
                if (newGrid != null) {
                    if (newGrid.isActive()) {
                        initializePuzzleFragment(solvingAttemptId);
                    } else {
                        initializeArchiveFragment(solvingAttemptId);
                    }
                }
            } catch (InvalidGridException e) {
                Log.d(TAG, "PuzzleFragmentActivity.restartLastGame can not load solvingAttempt with id" +
                              " '" + solvingAttemptId + "'.", e);
            }
        } else {
            showDialogNewGame(false);
        }
    }

    /**
     * Shows the dialog in which the parameters have to specified which will be used to create the new game. The
     * parameters will be defaulted to the values used to create the last puzzle (as stored in the preferences).
     *
     * @param cancelable
     *         True in case the dialog can be cancelled.
     */
    private void showDialogNewGame(final boolean cancelable) {
        showDialogNewGame(cancelable, mMathDokuPreferences.getPuzzleParameterGridSize(),
                          mMathDokuPreferences.getPuzzleParameterOperatorsVisible(),
                          mMathDokuPreferences.getPuzzleParameterComplexity());
    }

    /**
     * Shows the dialog in which the parameters have to specified which will be used to create the new game. The
     * parameters will be defaulted to the given values.
     *
     * @param cancelable
     *         True in case the dialog can be cancelled.
     * @param gridType
     *         The grid size of the new puzzle.
     * @param visibleOperators
     *         True in case operators should be hidden in the new puzzle.
     * @param puzzleComplexity
     *         Complexity of the puzzle new puzzle.
     */
    private void showDialogNewGame(final boolean cancelable, GridType gridType, boolean visibleOperators,
                                   PuzzleComplexity puzzleComplexity) {
        // Get view and put relevant information into the view.
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.puzzle_parameter_dialog, null);
        if (view == null) {
            return;
        }

        // Get views for the puzzle generating parameters
        final Spinner puzzleParameterSizeSpinner = (Spinner) view.findViewById(R.id.puzzleParameterSizeSpinner);
        final CheckBox puzzleParameterDisplayOperatorsCheckBox = (CheckBox) view.findViewById(
                R.id.puzzleParameterDisplayOperatorsCheckBox);
        final RatingBar puzzleParameterDifficultyRatingBar = (RatingBar) view.findViewById(
                R.id.puzzleParameterDifficultyRatingBar);
        final CheckableImageView puzzleParameterDifficultyRandom = (CheckableImageView) view.findViewById(
                R.id.puzzleParameterDifficultyRandom);

        // Populate the spinner. Initial value is set to value used for
        // generating the previous puzzle.
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                                                                      getAllGridSizeDescriptions());
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        puzzleParameterSizeSpinner.setAdapter(adapterStatus);

        // Initialize the parameters to the given values
        puzzleParameterSizeSpinner.setSelection(gridType.toZeroBasedIndex());
        puzzleParameterDisplayOperatorsCheckBox.setChecked(visibleOperators);
        switch (puzzleComplexity) {
            case RANDOM:
                puzzleParameterDifficultyRandom.setChecked(true);
                puzzleParameterDifficultyRatingBar.setRating(0);
                break;
            case VERY_EASY:
                puzzleParameterDifficultyRandom.setChecked(false);
                puzzleParameterDifficultyRatingBar.setRating(1);
                break;
            case EASY:
                puzzleParameterDifficultyRandom.setChecked(false);
                puzzleParameterDifficultyRatingBar.setRating(2);
                break;
            case NORMAL:
                puzzleParameterDifficultyRandom.setChecked(false);
                puzzleParameterDifficultyRatingBar.setRating(3);
                break;
            case DIFFICULT:
                puzzleParameterDifficultyRandom.setChecked(false);
                puzzleParameterDifficultyRatingBar.setRating(4);
                break;
            case VERY_DIFFICULT:
                puzzleParameterDifficultyRandom.setChecked(false);
                puzzleParameterDifficultyRatingBar.setRating(5);
                break;
        }
        puzzleParameterDifficultyRatingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

                                                                            @Override
                                                                            public void onRatingChanged(RatingBar
                                                                                                                ratingBar, float rating, boolean fromUser) {
                                                                                if (fromUser) {
                                                                                    puzzleParameterDifficultyRandom
                                                                                            .setChecked(
                                                                                            rating < 0.5f);
                                                                                    puzzleParameterDifficultyRandom
                                                                                            .invalidate();
                                                                                }
                                                                            }
                                                                        });
        puzzleParameterDifficultyRandom.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    puzzleParameterDifficultyRandom.toggle();
                    if (puzzleParameterDifficultyRandom.isChecked()) {
                        puzzleParameterDifficultyRatingBar.setRating(0);
                        puzzleParameterDifficultyRatingBar.invalidate();
                    } else {
                        puzzleParameterDifficultyRatingBar.setRating(3);
                    }
                }
                return true;
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this).setTitle(
                R.string.dialog_puzzle_parameters_title)
                .setView(view)
                .setCancelable(cancelable);
        if (cancelable) {
            alertDialogBuilder.setNegativeButton(R.string.dialog_general_button_cancel,
                                                 new DialogInterface.OnClickListener() {

                                                     @Override
                                                     public void onClick(DialogInterface dialog, int which) {
                                                         // do nothing
                                                     }
                                                 });
        }
        alertDialogBuilder.setNeutralButton(R.string.dialog_puzzle_parameters_neutral_button,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    // Transform size spinner to grid size
                                                    GridType gridType = GridType.fromZeroBasedIndex(
                                                            (int) puzzleParameterSizeSpinner.getSelectedItemId());

                                                    // Transform rating to puzzle complexity.
                                                    int rating = Math.round(
                                                            puzzleParameterDifficultyRatingBar.getRating());
                                                    PuzzleComplexity puzzleComplexity;
                                                    if (rating >= 5) {
                                                        puzzleComplexity = PuzzleComplexity.VERY_DIFFICULT;
                                                    } else if (rating >= 4) {
                                                        puzzleComplexity = PuzzleComplexity.DIFFICULT;
                                                    } else if (rating >= 3) {
                                                        puzzleComplexity = PuzzleComplexity.NORMAL;
                                                    } else if (rating >= 2) {
                                                        puzzleComplexity = PuzzleComplexity.EASY;
                                                    } else if (rating >= 1) {
                                                        puzzleComplexity = PuzzleComplexity.VERY_EASY;
                                                    } else {
                                                        puzzleComplexity = PuzzleComplexity.RANDOM;
                                                    }

                                                    // Store current settings in the preferences
                                                    mMathDokuPreferences.setPuzzleParameterGridSize(gridType);
                                                    mMathDokuPreferences.setPuzzleParameterOperatorsVisible(
                                                            puzzleParameterDisplayOperatorsCheckBox.isChecked());
                                                    mMathDokuPreferences.setPuzzleParameterComplexity(puzzleComplexity);

                                                    // Start a new game with specified parameters
                                                    startNewGame(gridType,
                                                                 !puzzleParameterDisplayOperatorsCheckBox.isChecked(),
                                                                 puzzleComplexity);
                                                }
                                            })
                .show();
    }

    private String[] getAllGridSizeDescriptions() {
        GridType[] gridTypes = GridType.values();
        String[] gridSizeDescriptions = new String[gridTypes.length];
        for (int i = 0; i < gridTypes.length; i++) {
            gridSizeDescriptions[i] = getString(R.string.grid_description_short, gridTypes[i].getGridSize(),
                                                gridTypes[i].getGridSize());
        }
        return gridSizeDescriptions;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_ARCHIVE && resultCode == RESULT_OK) {
            // When returning from the archive with result code OK, the grid
            // which was displayed in the archive will be reloaded if the
            // returned solving attempt can be loaded.
            if (intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    int solvingAttemptId = bundle.getInt(ArchiveFragmentActivity.BUNDLE_KEY_SOLVING_ATTEMPT_ID);
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
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                if (bundle.containsKey(SharedPuzzleActivity.RESTART_LAST_GAME_SHARED_PUZZLE) && bundle.getBoolean(
                        SharedPuzzleActivity.RESTART_LAST_GAME_SHARED_PUZZLE)) {
                    if (mPuzzleFragment != null) {
                        restartLastGame();
                    } else {
                        // Currently the Archive fragment is beïng displayed.
                        // This fragment cannot yet be replaced with a
                        // PuzzleFragment as this results in
                        // IllegalStateException [Can not perform this action
                        // after onSaveInstanceState]. Therefore restart is
                        // postponed till onResume is called.
                        mOnResumeRestartLastGame = true;
                    }
                }
                if (bundle.containsKey(NEW_PUZZLE_FOR_LEADERBOARD) && bundle.getBoolean(
                        NEW_PUZZLE_FOR_LEADERBOARD) && bundle.containsKey(
                        NEW_PUZZLE_FOR_LEADERBOARD_GRID_SIZE) && bundle.containsKey(
                        NEW_PUZZLE_FOR_LEADERBOARD_HIDE_OPERATORS) && bundle.containsKey(
                        NEW_PUZZLE_FOR_LEADERBOARD_PUZZLE_COMPLEXITY)) {
                    GridType gridType = GridType.valueOf(bundle.getString(NEW_PUZZLE_FOR_LEADERBOARD_GRID_SIZE));
                    boolean visibleOperators = !bundle.getBoolean(NEW_PUZZLE_FOR_LEADERBOARD_HIDE_OPERATORS);
                    PuzzleComplexity puzzleComplexity = PuzzleComplexity.valueOf(
                            bundle.getString(NEW_PUZZLE_FOR_LEADERBOARD_PUZZLE_COMPLEXITY));
                    showDialogNewGame(true, gridType, visibleOperators, puzzleComplexity);
                }
            }
        }

        super.onNewIntent(intent);
    }

    public static Intent createIntentToStartNewPuzzleFromSelectedLeaderboardFragment(Activity activity, int gridSize,
                                                                                     boolean hideOperators,
                                                                                     PuzzleComplexity
                                                                                             puzzleComplexity) {
        Intent intent = new Intent(activity, PuzzleFragmentActivity.class).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intent != null) {
            intent.putExtra(NEW_PUZZLE_FOR_LEADERBOARD, true);
            intent.putExtra(NEW_PUZZLE_FOR_LEADERBOARD_GRID_SIZE, GridType.fromInteger(gridSize)
                    .toString());
            intent.putExtra(NEW_PUZZLE_FOR_LEADERBOARD_HIDE_OPERATORS, hideOperators);
            intent.putExtra(NEW_PUZZLE_FOR_LEADERBOARD_PUZZLE_COMPLEXITY, puzzleComplexity.toString());
        }

        return intent;
    }

    /**
     * The grid for the given solving attempt has to be replayed.
     *
     * @param solvingAttemptId
     *         The solving attempt for which the grid has to be replayed.
     */
    private void replayPuzzle(int solvingAttemptId) {
        Grid grid = new GridLoader().load(solvingAttemptId);
        if (grid != null) {
            Grid newGrid = grid.createNewGridForReplay();
            if (newGrid != null) {
                if (newGrid.save()) {
                    initializePuzzleFragment(newGrid.getSolvingAttemptId());
                }
            }
        }
    }

    /* The click listener for the list view in the navigation drawer */
    private class NavigationDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mNavigationDrawerItems[position].equals(
                    getResources().getString(R.string.action_bar_subtitle_puzzle_fragment))) {
                if (mArchiveFragment != null) {
                    showDialogNewGame(true);
                }
            } else if (mNavigationDrawerItems[position].equals(getResources().getString(R.string.action_archive))) {
                Intent intentArchive = new Intent(PuzzleFragmentActivity.this, ArchiveFragmentActivity.class);
                startActivityForResult(intentArchive, REQUEST_ARCHIVE);
            } else if (mNavigationDrawerItems[position].equals(getResources().getString(R.string.action_statistics))) {
                Intent intentStatistics = new Intent(PuzzleFragmentActivity.this, StatisticsFragmentActivity.class);
                startActivity(intentStatistics);
            } else if (mNavigationDrawerItems[position].equals(
                    getResources().getString(R.string.action_leaderboards))) {
                startLeaderboardsOverview();
            }
            mDrawerLayout.closeDrawer(mDrawerListView);
        }
    }

    /**
     * Set the navigation drawer. The drawer can be open in following ways:<br> - tapping the drawer or the app icon<br>
     * - tapping the left side of the screen.
     * <p/>
     * The drawer icon will only be visible as soon as at least one item is available for display in the drawer. As of
     * that moment it will be possible to open the drawer by tapping the drawer or the app icon.
     */
    private void setNavigationDrawer() {
        // The drawer will be opened automatically in case a new item has been
        // added to the drawer.
        boolean openDrawer = false;

        // Determine the items which have to be shown in the drawer.
        List<String> navigationDrawerItems = new ArrayList<String>();

        // It is not possible to disable the navigation drawer entirely in case
        // the archive and statistics are not yet unlocked. To prevent showing
        // an empty drawer, the puzzle activity itself will always be displayed
        // as a navigation item. In case the user opens the drawer accidentally
        // by tapping the left side of the screen before the archive or
        // statistics are unlocked it will be less confusing.
        navigationDrawerItems.add(getResources().getString(R.string.action_bar_subtitle_puzzle_fragment));

        // Add archive if unlocked
        if (mMathDokuPreferences.isArchiveAvailable()) {
            String string = getResources().getString(R.string.action_archive);
            navigationDrawerItems.add(string);
            // noinspection ConstantConditions
            if (!openDrawer && mNavigationDrawerItems != null) {
                openDrawer = !Arrays.asList(mNavigationDrawerItems)
                        .contains(string);
            }
        }

        // Add statistics if unlocked
        if (mMathDokuPreferences.isStatisticsAvailable()) {
            String string = getResources().getString(R.string.action_statistics);
            navigationDrawerItems.add(string);
            if (!openDrawer && mNavigationDrawerItems != null) {
                openDrawer = !Arrays.asList(mNavigationDrawerItems)
                        .contains(string);
            }
        }

        // Leaderboards are always available
        String string = getResources().getString(R.string.action_leaderboards);
        navigationDrawerItems.add(string);
        if (!openDrawer && mNavigationDrawerItems != null) {
            openDrawer = !Arrays.asList(mNavigationDrawerItems)
                    .contains(string);
        }

        mNavigationDrawerItems = navigationDrawerItems.toArray(new String[navigationDrawerItems.size()]);

        // Set up the action bar for displaying the drawer icon and making the
        // app icon clickable in order to display the drawer.
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                actionBar.setHomeButtonEnabled(true);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.puzzle_activity_drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                                                           R.string.navigation_drawer_open,
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
                if (actionBar != null) {
                    actionBar.setSubtitle(getResources().getString(R.string.action_bar_subtitle_puzzle_fragment));
                }
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
                if (actionBar != null) {
                    actionBar.setSubtitle(null);
                }
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        // Get list view for drawer
        mDrawerListView = (ListView) findViewById(R.id.left_drawer);

        mDrawerListView.setBackgroundColor(mMathDokuPreferences.getTheme().getNavigationDrawerBackgroundColor());

        // Set the adapter for the list view containing the navigation items
        mDrawerListView.setAdapter(
                new ArrayAdapter<String>(this, R.layout.navigation_drawer_list_item, mNavigationDrawerItems));

        // Set the list's click listener
        mDrawerListView.setOnItemClickListener(new NavigationDrawerItemClickListener());

        // Select the the play puzzle item as active item.
        mDrawerListView.setItemChecked(0, true);

        if (openDrawer) {
            mDrawerLayout.openDrawer(mDrawerListView);
        }
        mDrawerLayout.invalidate();
    }

    public void onCancelGridGeneration() {
        // The background task for creating a new grid has been finished.
        mGeneratePuzzleProgressDialog = null;

        if (mPuzzleFragment != null) {
            mPuzzleFragment.startTimer();
        }
    }

    @Override
    public void onSignInFailed() {
        // Can not sign in to Google Play Services. For now, nothing is done.
    }

    /**
     * Sign in on Google Play Services has succeeded.
     */
    private void onSignSucceeded() {
        // Get the game client an attach the content view of the activity.
        GamesClient gamesClient = getGamesClient();
        gamesClient.setViewForPopups(findViewById(android.R.id.content));

        // Set up leaderboard
        mLeaderboardConnector = new LeaderboardConnector(this, gamesClient);
    }

    @Override
    public void onAutoSignInSucceeded() {
        onSignSucceeded();

        // Submit or re-submit leaderboard scores for which the rank information
        // is missing. Do not show a progress dialog as this is an auto sign in.
        new LeaderboardRankUpdater(mLeaderboardConnector).update();
    }

    @Override
    public void onUserInitiatedSignInSucceeded() {
        onSignSucceeded();

        // After the user has successfully signed to Google+, the leaderboards
        // view is automatically started.
        startLeaderboardsOverview();
    }

    /**
     * Displays the choose input method dialog.
     */
    private void openInputMethodDialog() {
        // Get view and put relevant information into the view.
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.input_method_dialog, null);

        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.choose_input_method_title))
                .setIcon(R.drawable.icon)
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(R.string.input_method_swipe_only, new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                           mMathDokuPreferences.setDigitInputMethod(true, false);
                                       }
                                   })
                .setNeutralButton(R.string.input_method_swipe_and_buttons, new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int whichButton) {
                                          mMathDokuPreferences.setDigitInputMethod(true, true);
                                      }
                                  })
                .setPositiveButton(R.string.input_method_buttons_only, new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                           mMathDokuPreferences.setDigitInputMethod(false, true);
                                       }
                                   })
                .show();
    }

    /**
     * Displays the dialog in which the user is asked whether the puzzle should be replayed.
     */
    private void openReplayDialog(final int solvingAttemptId) {
        new AlertDialog.Builder(this).setTitle(R.string.dialog_replay_puzzle_confirmation_title)
                .setMessage(R.string.dialog_replay_puzzle_confirmation_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton(R.string.dialog_replay_puzzle_confirmation_negative_button,
                                   new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                           //
                                       }
                                   })
                .setPositiveButton(R.string.dialog_replay_puzzle_confirmation_positive_button,
                                   new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           replayPuzzle(solvingAttemptId);
                                       }
                                   })
                .show();
    }

    /**
     * Start the leaderboards overview activity.
     */
    private void startLeaderboardsOverview() {
        Intent intentLeaderboards = new Intent(this, LeaderboardOverviewActivity.class);
        startActivity(intentLeaderboards);
        mMathDokuPreferences.increaseLeaderboardsOverviewViewed();
    }
}
