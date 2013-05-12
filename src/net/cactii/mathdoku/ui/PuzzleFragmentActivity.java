package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.CellChange;
import net.cactii.mathdoku.Cheat;
import net.cactii.mathdoku.Cheat.CheatType;
import net.cactii.mathdoku.DigitPositionGrid;
import net.cactii.mathdoku.DigitPositionGrid.DigitPositionGridType;
import net.cactii.mathdoku.GameTimer;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.GridCage;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.InvalidGridException;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.gridGenerating.DialogPresentingGridGenerator;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.painter.Painter.GridTheme;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.storage.GameFileConverter;
import net.cactii.mathdoku.storage.PreviewImage;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import net.cactii.mathdoku.tip.TipCheat;
import net.cactii.mathdoku.tip.TipDialog;
import net.cactii.mathdoku.tip.TipIncorrectValue;
import net.cactii.mathdoku.tip.TipInputModeChanged;
import net.cactii.mathdoku.ui.GridView.InputModeDeterminer;
import net.cactii.mathdoku.util.UsageLog;
import net.cactii.mathdoku.util.Util;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class PuzzleFragmentActivity extends FragmentActivity implements
		OnSharedPreferenceChangeListener {
	public final static String TAG = "MathDoku.MainActivity";

	public final static String PROJECT_HOME = "http://mathdoku.net/";

	// Identifiers for the context menu
	private final static int CONTEXT_MENU_REVEAL_CELL = 1;
	private final static int CONTEXT_MENU_USE_CAGE_MAYBES = 2;
	private final static int CONTEXT_MENU_REVEAL_OPERATOR = 3;
	private final static int CONTEXT_MENU_CLEAR_CAGE_CELLS = 4;
	private final static int CONTEXT_MENU_CLEAR_GRID = 5;
	private final static int CONTEXT_MENU_SHOW_SOLUTION = 6;

	// The grid and the view which will display the grid.
	public Grid mGrid;
	public GridView mGridView;

	// A global painter object to paint the grid in different themes.
	public Painter mPainter;

	// The input mode in which the puzzle can be.
	public enum InputMode {
		NORMAL, // Digits entered are handled as a new cell value
		MAYBE, // Digits entered are handled to toggle the possible value on/of
		NO_INPUT__HIDE_GRID, // No game active, no grid shown
		NO_INPUT__DISPLAY_GRID // No game active, solved puzzle shown
	};

	/**
	 * The input mode which is currently active. Access level is not private, to
	 * prevent the an extra access method (see
	 * http://developer.android.com/training
	 * /articles/perf-tips.html#PackageInner).
	 */
	/* package */InputMode mInputMode;
	Button mInputModeTextView;

	TextView mSolvedText;
	GameTimer mTimerTask;

	RelativeLayout mTopLayout;
	RelativeLayout mPuzzleGridLayout;
	TableLayout mControls;
	TextView mGameSeedLabel;
	TextView mGameSeedText;
	TextView mTimerText;

	Menu mMainMenu;

	// Digit positions are the places on which the digit buttons can be placed.
	Button mDigitPosition[] = new Button[9];
	DigitPositionGrid mDigitPositionGrid;

	Button mStartButton;

	Button mClearDigit;
	Button mUndoButton;
	View[] mSoundEffectViews;
	private Animation mOutAnimation;
	private Animation mSolvedAnimation;

	public Preferences mMathDokuPreferences;

	// Background tasks for generating a new puzzle and converting game files
	public DialogPresentingGridGenerator mGridGeneratorTask;
	public GameFileConverter mGameFileConverter;

	// Variables for process of creating preview images of game file for which
	// no preview image does exist.
	private int mSolvingAttemptImagePreviewCreation;
	private ProgressDialog mProgressDialogImagePreviewCreation;

	private Util mUtil;

	private boolean mBlockTouchSameCell = false;

	final Handler mHandler = new Handler();

	// Object to save data on a configuration change
	private class ConfigurationInstanceState {
		private DialogPresentingGridGenerator mGridGeneratorTask;
		private GameFileConverter mGameFileConverter;
		private InputMode mInputMode;

		public ConfigurationInstanceState(
				DialogPresentingGridGenerator gridGeneratorTask,
				GameFileConverter gameFileConverterTask, InputMode inputMode) {
			mGridGeneratorTask = gridGeneratorTask;
			mGameFileConverter = gameFileConverterTask;
			mInputMode = inputMode;
		}

		public DialogPresentingGridGenerator getGridGeneratorTask() {
			return mGridGeneratorTask;
		}

		public GameFileConverter getGameFileConverter() {
			return mGameFileConverter;
		}

		public InputMode getInputMode() {
			return mInputMode;
		}

	}

	/** Called when the activity is first created. */
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// REMOVE: FRAGMENT-CONVERSION
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Initialize the util helper.
		mUtil = new Util(this);

		// If too little height then request full screen usage
		if (mUtil.getDisplayHeight() < 750) {
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(R.layout.puzzle_activity_fragment);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			// Disable home as up on Ice Cream Sandwich and above. On Honeycomb
			// it will be enabled by default but this can do no harm.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				actionBar.setHomeButtonEnabled(false);
			}
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionBar.setSubtitle("Playing a game");
		}

		mMathDokuPreferences = Preferences.getInstance(this);

		this.mTopLayout = (RelativeLayout) findViewById(R.id.topLayout);
		this.mPuzzleGridLayout = (RelativeLayout) findViewById(R.id.puzzleGrid);
		(this.mGridView = (GridView) findViewById(R.id.gridView)).mInputModeDeterminer = new InputModeDeterminer() {
			@Override
			public final InputMode getInputMode() {
				return mInputMode;
			}
		};
		this.mSolvedText = (TextView) findViewById(R.id.solvedText);
		this.mGridView.mAnimationText = this.mSolvedText;
		this.mControls = (TableLayout) findViewById(R.id.controls);
		this.mGameSeedLabel = (TextView) findViewById(R.id.gameSeedLabel);
		this.mGameSeedText = (TextView) findViewById(R.id.gameSeedText);
		this.mTimerText = (TextView) findViewById(R.id.timerText);
		this.mStartButton = (Button) findViewById(R.id.startButton);

		this.mInputModeTextView = (Button) findViewById(R.id.inputModeText);
		mDigitPosition[0] = (Button) findViewById(R.id.digitPosition1);
		mDigitPosition[1] = (Button) findViewById(R.id.digitPosition2);
		mDigitPosition[2] = (Button) findViewById(R.id.digitPosition3);
		mDigitPosition[3] = (Button) findViewById(R.id.digitPosition4);
		mDigitPosition[4] = (Button) findViewById(R.id.digitPosition5);
		mDigitPosition[5] = (Button) findViewById(R.id.digitPosition6);
		mDigitPosition[6] = (Button) findViewById(R.id.digitPosition7);
		mDigitPosition[7] = (Button) findViewById(R.id.digitPosition8);
		mDigitPosition[8] = (Button) findViewById(R.id.digitPosition9);
		this.mClearDigit = (Button) findViewById(R.id.clearButton);
		this.mUndoButton = (Button) findViewById(R.id.undoButton);

		this.mSoundEffectViews = new View[] { this.mGridView,
				this.mDigitPosition[0], this.mDigitPosition[1],
				this.mDigitPosition[2], this.mDigitPosition[3],
				this.mDigitPosition[4], this.mDigitPosition[5],
				this.mDigitPosition[6], this.mDigitPosition[7],
				this.mDigitPosition[8], this.mClearDigit,
				this.mInputModeTextView, this.mUndoButton };

		// Initialize global objects (singleton instances)
		this.mPainter = Painter.getInstance(this);
		DatabaseHelper.getInstance(this);

		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			// Check if database is consistent.
			DevelopmentHelper.checkDatabaseConsistency(this);
		}

		setInputMode(InputMode.NO_INPUT__HIDE_GRID);

		// Animation for a solved puzzle
		mSolvedAnimation = AnimationUtils
				.loadAnimation(this, R.anim.solvedanim);
		mSolvedAnimation.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				mSolvedText.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});

		// Animation for controls.
		mOutAnimation = AnimationUtils.loadAnimation(this,
				R.anim.selectorzoomout);
		mOutAnimation.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				mControls.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});

		final PuzzleFragmentActivity puzzleFragmentActivity = this;
		this.mGridView
				.setOnGridTouchListener(this.mGridView.new OnGridTouchListener() {
					@Override
					public void gridTouched(GridCell cell,
							boolean sameCellSelectedAgain) {
						if (mMathDokuPreferences.isControlsBlockHidden()) {
							if (mControls.getVisibility() == View.VISIBLE) {
								mControls.startAnimation(mOutAnimation);
								mGridView.mSelectorShown = false;
								mGridView.requestFocus();
							} else {
								mControls.setVisibility(View.VISIBLE);
								Animation animation = AnimationUtils
										.loadAnimation(puzzleFragmentActivity,
												R.anim.selectorzoomin);
								mControls.startAnimation(animation);
								mGridView.mSelectorShown = true;
								mControls.requestFocus();
							}
						} else {
							// Controls are always visible

							if (sameCellSelectedAgain && !mBlockTouchSameCell) {
								if (TipInputModeChanged
										.toBeDisplayed(mMathDokuPreferences)) {
									new TipInputModeChanged(
											puzzleFragmentActivity,
											(mInputMode == InputMode.MAYBE ? InputMode.NORMAL
													: InputMode.MAYBE)).show();
								}
								toggleInputMode();
							}
						}

						setClearAndUndoButtonVisibility(cell);
					}
				});

		for (int i = 0; i < mDigitPosition.length; i++)
			this.mDigitPosition[i].setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Convert text of button (number) to Integer
					int d = Integer.parseInt(((Button) v).getText().toString());
					puzzleFragmentActivity.digitSelected(d);
				}
			});
		this.mClearDigit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				puzzleFragmentActivity.digitSelected(0);
			}
		});
		this.mUndoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (puzzleFragmentActivity.mGrid.undoLastMove()) {
					// Succesfull undo
					setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
					mGridView.invalidate();
				}

				if (mMathDokuPreferences.isControlsBlockHidden()) {
					puzzleFragmentActivity.mControls.setVisibility(View.GONE);
				}
			}
		});
		this.mInputModeTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.playSoundEffect(SoundEffectConstants.CLICK);
				toggleInputMode();
			}

		});
		this.mStartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openOptionsMenu();
				mMainMenu.performIdentifierAction(R.id.newgame, 0);
			}

		});
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			this.mGameSeedText.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (mGrid != null) {
							mGrid.getGridGeneratingParameters().show(
									puzzleFragmentActivity);
						}
					}
					return false;
				}
			});
			this.mGameSeedLabel.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (mGrid != null) {
							mGrid.getGridGeneratingParameters().show(
									puzzleFragmentActivity);
						}
					}
					return false;
				}
			});
		}

		this.mGridView.setFocusable(true);
		this.mGridView.setFocusableInTouchMode(true);

		registerForContextMenu(this.mGridView);

		// Restore the last configuration instance which was saved before the
		// configuration change.
		Object object = this.getLastCustomNonConfigurationInstance();
		if (object != null
				&& object.getClass() == ConfigurationInstanceState.class) {
			UsageLog.getInstance(this).logConfigurationChange(this);
			ConfigurationInstanceState configurationInstanceState = (ConfigurationInstanceState) object;

			// Restore input mode
			setInputMode(configurationInstanceState.getInputMode());

			// Restore background process if running.
			mGridGeneratorTask = configurationInstanceState
					.getGridGeneratorTask();
			if (mGridGeneratorTask != null) {
				mGridGeneratorTask.attachToActivity(this);
			}

			// Restore background process if running.
			mGameFileConverter = configurationInstanceState
					.getGameFileConverter();
			if (mGameFileConverter != null) {
				mGameFileConverter.attachToActivity(this);
			}
		}

		checkVersion();

		mMathDokuPreferences.mSharedPreferences
				.registerOnSharedPreferenceChangeListener(this);

		restartLastGame();
	}

	public void onPause() {
		UsageLog.getInstance().close();

		stopTimer();
		if (mGrid != null) {
			mGrid.save(mGridView);
		}

		if (mProgressDialogImagePreviewCreation != null
				&& mProgressDialogImagePreviewCreation.isShowing()) {
			try {
				mProgressDialogImagePreviewCreation.dismiss();
			} catch (IllegalArgumentException e) {
				// Abort processing. On resume of activity this process
				// is (or already has been) restarted.
				return;
			}
		}

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mMathDokuPreferences.mSharedPreferences
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void setTheme() {

		mSolvedText.setTypeface(mPainter.getTypeface());

		switch (mMathDokuPreferences.getTheme()) {
		case NEWSPAPER:
			mTopLayout.setBackgroundResource(R.drawable.newspaper);
			mPainter.setTheme(GridTheme.NEWSPAPER);
			mTimerText.setBackgroundColor(0x90808080);
			break;
		case DARK:
			mTopLayout.setBackgroundResource(R.drawable.newspaper_dark);
			mPainter.setTheme(GridTheme.DARK);
			mTimerText.setTextColor(0xFFF0F0F0);
			break;
		case CARVED:
			mTopLayout.setBackgroundResource(R.drawable.background);
			mPainter.setTheme(GridTheme.CARVED);
			mTimerText.setBackgroundColor(0x10000000);
			break;
		}

		this.mGridView.invalidate();
	}

	public void onResume() {
		UsageLog.getInstance(this);

		if (mMathDokuPreferences.isWakeLockEnabled()) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		if (mGridGeneratorTask != null) {
			// In case the grid is created in the background and the dialog is
			// closed, the activity will be moved to the background as well. In
			// case the user starts this app again onResume is called but
			// onCreate isn't. So we have to check here as well.
			mGridGeneratorTask.attachToActivity(this);
		}

		setTheme();

		// Propagate current preferences to the grid.
		if (mGrid != null) {
			mGrid.setPreferences();
		}

		this.setSoundEffectsEnabled(mMathDokuPreferences
				.isPlaySoundEffectEnabled());

		super.onResume();

		if (mTimerTask == null
				|| (mTimerTask != null && mTimerTask.isCancelled())) {
			startTimer();
		}
	}

	public void setSoundEffectsEnabled(boolean enabled) {
		for (View v : this.mSoundEffectViews)
			v.setSoundEffectsEnabled(enabled);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Disable or enable option to check progress depending on whether grid
		// is active
		menu.findItem(R.id.checkprogress).setVisible(
				(mGrid != null && mGrid.isActive()));

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
		mMainMenu = menu;

		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			inflater.inflate(R.menu.development_mode_menu, menu);
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		mBlockTouchSameCell = true;
		if (mGrid == null || !mGrid.isActive()) {
			// No context menu in case puzzle isn't active.
			return;
		}

		// Set title
		menu.setHeaderTitle(R.string.application_name);

		// Determine current selected cage.
		GridCage selectedGridCage = mGrid.getCageForSelectedCell();

		// Add options to menu only in case the option can be chosen.

		// Option: reveal correct value of selected cell only
		menu.add(0, CONTEXT_MENU_REVEAL_CELL, 0,
				R.string.context_menu_reveal_cell);

		// Option: for all cells in the selected cage which have exactly one
		// possible value, this possible value is set as the user value of the
		// cell.
		if (selectedGridCage != null) {
			for (GridCell cell : selectedGridCage.mCells) {
				if (cell.countPossibles() == 1) {
					// At least one cell within this cage has exactly one
					// possible
					// value.
					menu.add(0, CONTEXT_MENU_USE_CAGE_MAYBES, 0,
							R.string.context_menu_use_cage_maybes);
					break;
				}
			}
		}

		// Option: reveal the operator of the selected cage
		if (selectedGridCage != null && selectedGridCage.isOperatorHidden()) {
			menu.add(0, CONTEXT_MENU_REVEAL_OPERATOR, 0,
					R.string.context_menu_reveal_operator);
		}

		// Option: clear all cell in the selected cage
		if (selectedGridCage != null) {
			for (GridCell cell : selectedGridCage.mCells) {
				if (cell.isUserValueSet() || cell.countPossibles() > 0) {
					// At least one cell within this cage has a value or a
					// possible
					// value.
					menu.add(0, CONTEXT_MENU_CLEAR_CAGE_CELLS, 0,
							R.string.context_menu_clear_cage_cells);
					break;
				}
			}
		}

		// Option: clear all cells in the grid
		for (GridCell cell : mGrid.mCells) {
			if (cell.isUserValueSet() || cell.countPossibles() > 0) {
				// At least one cell within this grid view has a value or a
				// possible value.
				menu.add(0, CONTEXT_MENU_CLEAR_GRID, 0,
						R.string.context_menu_clear_grid);
				break;
			}
		}

		// Option: show the solution for this puzzle
		menu.add(3, CONTEXT_MENU_SHOW_SOLUTION, 0,
				R.string.context_menu_show_solution);

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	public boolean onContextItemSelected(MenuItem item) {
		// Get selected cell
		GridCell selectedCell = mGrid.getSelectedCell();
		GridCage selectedGridCage = mGrid.getCageForSelectedCell();

		switch (item.getItemId()) {
		case CONTEXT_MENU_CLEAR_CAGE_CELLS:
			UsageLog.getInstance().logFunction("ContextMenu.ClearCageCells");
			selectedGridCage.clearCells(mGrid.getGridStatistics());
			this.mGridView.invalidate();
			break;
		case CONTEXT_MENU_USE_CAGE_MAYBES:
			UsageLog.getInstance().logFunction("ContextMenu.UseCageMaybes");
			if (selectedCell == null) {
				break;
			}
			for (GridCell cell : selectedGridCage.mCells) {
				if (cell.countPossibles() == 1) {
					CellChange orginalUserMove = cell.saveUndoInformation(null);
					cell.setUserValue(cell.getFirstPossible());
					if (mMathDokuPreferences.isClearRedundantPossiblesEnabled()) {
						// Update possible values for other cells in this row
						// and
						// column.
						mGrid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
					}
				}
			}
			this.mGridView.invalidate();
			break;
		case CONTEXT_MENU_REVEAL_CELL:
			revealCell(selectedCell);
			break;
		case CONTEXT_MENU_CLEAR_GRID:
			openClearDialog();
			break;
		case CONTEXT_MENU_SHOW_SOLUTION:
			mGrid.getGridStatistics().solutionRevealed();
			registerAndProcessCheat(CheatType.SOLUTION_REVEALED);
			mGrid.solve();
			break;
		case CONTEXT_MENU_REVEAL_OPERATOR:
			revealOperator(selectedGridCage);
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		mBlockTouchSameCell = false;
		super.onContextMenuClosed(menu);
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
			checkProgress();
			return true;
		case R.id.action_archive:
			Intent intentArchive = new Intent(this,
					ArchiveFragmentActivity.class);
			startActivity(intentArchive);
			return true;
		case R.id.menu_statistics:
			UsageLog.getInstance().logFunction("Menu.Statistics");
			Intent intentStatistics = new Intent(this,
					StatisticsFragmentActivity.class);
			if (mGrid != null) {
				intentStatistics.putExtra(
						StatisticsGameFragment.BUNDLE_KEY_STATISTICS_ID, mGrid
								.getGridStatistics().getId());
			}
			startActivity(intentStatistics);
			return true;
		case R.id.options:
			UsageLog.getInstance().logFunction("Menu.ViewOptions");
			startActivity(new Intent(this, PuzzlePreferenceActivity.class));
			return true;
		case R.id.help:
			UsageLog.getInstance().logFunction("Menu.ViewHelp.Manual");
			this.openHelpDialog();
			return true;
		default:
			// When running in development mode it should be checked whether a
			// development menu item was selected.
			if (DevelopmentHelper.mMode != Mode.DEVELOPMENT) {
				return super.onOptionsItemSelected(menuItem);
			} else {
				if (DevelopmentHelper.onDevelopmentHelperOption(this, menuId)) {
					// A development helper menu option was processed
					// succesfully.
					return true;
				} else {
					return super.onOptionsItemSelected(menuItem);
				}
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& this.mGridView.mSelectorShown) {
			this.mControls.setVisibility(View.GONE);
			this.mGridView.requestFocus();
			this.mGridView.mSelectorShown = false;
			this.mGridView.invalidate();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void digitSelected(int value) {
		this.mGridView.digitSelected(value, mInputMode);

		if (mMathDokuPreferences.isControlsBlockHidden()) {
			this.mControls.setVisibility(View.GONE);
		}
		setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
		this.mGridView.requestFocus();
		this.mGridView.mSelectorShown = false;
		this.mGridView.invalidate();
	}

	/**
	 * Restart the last game which was played.
	 */
	private void restartLastGame() {
		// Determine if and which grid was last played
		int solvingAttemptId = new SolvingAttemptDatabaseAdapter()
				.getMostRecentPlayedId();
		if (solvingAttemptId >= 0) {
			// Load the grid
			try {
				Grid newGrid = new Grid();
				newGrid.load(solvingAttemptId);
				setNewGrid(newGrid);
			} catch (InvalidGridException e) {
				if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
					Log.e(TAG,
							"MainActivity.restartLastGame can not load grid with id '"
									+ solvingAttemptId + "'.");
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
		// Cancel old timer if running.
		stopTimer();

		// Save the game.
		if (mGrid != null) {
			mGrid.save(mGridView);
		}

		// Start a background task to generate the new grid. As soon as the new
		// grid is created, the method onNewGridReady will be called.
		int maxCageSize = (mMathDokuPreferences.isAllowBigCagesEnabled() ? 6
				: 4);
		int maxCageResult = getResources().getInteger(
				R.integer.maximum_cage_value);
		mGridGeneratorTask = new DialogPresentingGridGenerator(this, gridSize,
				maxCageSize, maxCageResult, hideOperators,
				Util.getPackageVersionNumber());
		mGridGeneratorTask.execute();
	}

	/**
	 * Reactivate the main ui after a new game is loaded into the grid view by
	 * the ASync GridGenerator task.
	 */
	public void onNewGridReady(final Grid newGrid) {
		if (mGrid != null) {
			UsageLog.getInstance().logGrid("Menu.StartNewGame.OldGame", mGrid);

			if (mGrid.mMoves.size() > 0) {
				// Increase counter for number of games on which playing has
				// been started.
				int countGamesStarted = mMathDokuPreferences
						.increaseGamesStarted();

				// As long as the user has not opted out for sending feedback,
				// check if we are going to ask the user to send feedback
				if (!mMathDokuPreferences.isUsageLogDisabled()) {
					// Check if we are going to ask the user to send feedback
					if (countGamesStarted == 3 || countGamesStarted == 10
							|| countGamesStarted == 30
							|| countGamesStarted == 60) {
						UsageLog.getInstance().askConsentForSendingLog(this);
					}
				}
			}
		}
		UsageLog.getInstance().logGrid("Menu.StartNewGame.NewGame", newGrid);

		// The background task for creating a new grid has been finished. The
		// new grid will always overwrite the current game without any warning.
		mGridGeneratorTask = null;
		setNewGrid(newGrid);
		setInputMode(InputMode.NORMAL);
	}

	private void animText(int textIdentifier, int color) {
		this.mSolvedText.setText(textIdentifier);
		this.mSolvedText.setTextColor(color);
		this.mSolvedText.setVisibility(View.VISIBLE);
		final float SCALE_FROM = (float) 0;
		final float SCALE_TO = (float) 1.0;
		ScaleAnimation anim = new ScaleAnimation(SCALE_FROM, SCALE_TO,
				SCALE_FROM, SCALE_TO, this.mGridView.mGridViewSize / 2,
				this.mGridView.mGridViewSize / 2);
		anim.setDuration(1000);
		// animText.setAnimation(anim);
		this.mSolvedText.startAnimation(this.mSolvedAnimation);
	}

	private void openHelpDialog() {
		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.help_dialog, null);

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
								+ getResources().getString(R.string.action_help))
				.setIcon(R.drawable.about)
				.setView(view)
				.setNeutralButton(R.string.dialog_help_neutral_button,
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

	private void openClearDialog() {
		final PuzzleFragmentActivity puzzleFragmentActivity = this;
		new AlertDialog.Builder(puzzleFragmentActivity)
				.setTitle(R.string.dialog_clear_grid_confirmation_title)
				.setMessage(R.string.dialog_clear_grid_confirmation_message)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNegativeButton(
						R.string.dialog_clear_grid_confirmation_negative_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								//
							}
						})
				.setPositiveButton(
						R.string.dialog_clear_grid_confirmation_positive_button,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								UsageLog.getInstance().logFunction(
										"ContextMenu.ClearGrid");
								puzzleFragmentActivity.mGrid.clearCells();
								puzzleFragmentActivity.mGridView.invalidate();
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
			// should not be restarted till the phase 1 backgroudn process is
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
			// On Each update of the game, all game files will be converted to
			// the latest definitions. On completion of the game file
			// conversion, method upgradePhase2_CreatePreviewImages will be
			// called.
			mGameFileConverter = new GameFileConverter(this,
					previousInstalledVersion, packageVersionNumber);
			mGameFileConverter.execute();
		} else if (!mMathDokuPreferences.isCreatePreviewImagesCompleted()) {
			// Skip Phase 1 and go directly to Phase 2 to generate new previews.
			upgradePhase2_createPreviewImages(previousInstalledVersion,
					packageVersionNumber);
		}
		return;
	}

	/**
	 * Finishes the upgrading process after the game files have been converted
	 * and the image previews have been created.
	 * 
	 * @param previousInstalledVersion
	 *            : Latest version of MathDoku which was actually used.
	 * @param currentVersion
	 *            Current (new) revision number of MathDoku.
	 */
	public void upgradePhase3_UpdatePreferences(int previousInstalledVersion,
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

	/**
	 * Create preview images for all stored games. Previews will be created one
	 * at a time because each stored game has to be loaded and displayed before
	 * a preview can be generated.
	 * 
	 * @param previousInstalledVersion
	 *            : Latest version of MathDoku which was actually used.
	 * @param currentVersion
	 *            Current (new) revision number of MathDoku.
	 */
	public void upgradePhase2_createPreviewImages(
			final int previousInstalledVersion, final int currentVersion) {
		// The background task for game file conversion is completed. Destroy
		// reference to the task.
		mGameFileConverter = null;

		if (mMathDokuPreferences.isCreatePreviewImagesCompleted()) {
			// Previews have already been created. Go to next phase of upgrading
			upgradePhase3_UpdatePreferences(previousInstalledVersion,
					currentVersion);
			return;
		}

		// Determine the number of previews to be created.
		int countGameFilesWithoutPreview = new SolvingAttemptDatabaseAdapter()
				.countSolvingAttemptsWithoutPreviewImage();
		if (countGameFilesWithoutPreview == 0) {
			// No games files without previews found.
			mMathDokuPreferences.setCreatePreviewImagesCompleted();

			// Go to next phase of upgrading
			upgradePhase3_UpdatePreferences(previousInstalledVersion,
					currentVersion);
			return;
		}

		// At least one game file was found for which no preview exist. Show
		// the progress dialog.
		mProgressDialogImagePreviewCreation = new ProgressDialog(this);
		mProgressDialogImagePreviewCreation
				.setTitle(R.string.main_ui_creating_previews_title);
		mProgressDialogImagePreviewCreation.setMessage(getResources()
				.getString(R.string.main_ui_creating_previews_message));
		mProgressDialogImagePreviewCreation
				.setIcon(android.R.drawable.ic_dialog_info);
		mProgressDialogImagePreviewCreation
				.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialogImagePreviewCreation
				.setMax(countGameFilesWithoutPreview);
		mProgressDialogImagePreviewCreation.setIndeterminate(false);
		mProgressDialogImagePreviewCreation.setCancelable(false);
		mProgressDialogImagePreviewCreation.show();

		// Display and hide elements so that the previews can be created.
		mPuzzleGridLayout.setVisibility(View.VISIBLE);
		mStartButton.setVisibility(View.GONE);

		// Runnable for handling the next step of preview image creation process
		// which can not be done until the grid view has been validated
		// (refreshed).
		final Runnable createNextPreviewImage = new Runnable() {
			public void run() {
				// If a game file was already loaded, it is now visible in the
				// grid view.
				if (mSolvingAttemptImagePreviewCreation >= 0) {
					// Save preview for the current game file.
					new PreviewImage(mSolvingAttemptImagePreviewCreation)
							.save(mGridView);
					mProgressDialogImagePreviewCreation.incrementProgressBy(1);
				}

				// Check if a preview for another game file needs to be
				// created.
				mSolvingAttemptImagePreviewCreation = new SolvingAttemptDatabaseAdapter()
						.getSolvingAttemptWithoutPreviewImage();
				if (mSolvingAttemptImagePreviewCreation >= 0) {
					Grid newGrid = new Grid();
					if (newGrid.load(mSolvingAttemptImagePreviewCreation)) {
						mGrid = newGrid;
						mGridView.loadNewGrid(mGrid);
						if (mGrid.isActive()) {
							// As this grid can contain maybe value we have to
							// set the corresponding digit position grid.
							setDigitPositionGrid(InputMode.NORMAL);
						}
						mPuzzleGridLayout.setVisibility(View.INVISIBLE);
						mControls.setVisibility(View.GONE);
						mStartButton.setVisibility(View.GONE);

						// Post a message for further processing of the
						// conversion game after the view has been refreshed
						// with the loaded game.
						mHandler.post(this);
					}
				} else {
					// All preview images have been created.

					// Dismiss the dialog. In case the process was interrupted
					// and restarted the dialog can not be dismissed without
					// causing an error.
					try {
						mProgressDialogImagePreviewCreation.dismiss();
					} catch (IllegalArgumentException e) {
						// Abort processing. On resume of activity this process
						// is (or already has been) restarted.
						return;
					} finally {
						mProgressDialogImagePreviewCreation = null;
					}

					mMathDokuPreferences.setCreatePreviewImagesCompleted();

					// Go to next phase of upgrading
					upgradePhase3_UpdatePreferences(previousInstalledVersion,
							currentVersion);

					setTheme();
				}
			}
		};

		// Post a message to start the process of creating image previews.
		mSolvingAttemptImagePreviewCreation = -1;
		(new Thread() {
			public void run() {
				PuzzleFragmentActivity.this.mHandler
						.post(createNextPreviewImage);
				mProgressDialogImagePreviewCreation.setProgress(1);
			}
		}).start();
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
		stopTimer();
		TipDialog.resetDisplayedDialogs();

		if (mGridGeneratorTask != null) {
			// A new grid is generated in the background. Detach the background
			// task from this activity. It will keep on running until finished.
			mGridGeneratorTask.detachFromActivity();
		}
		if (mGameFileConverter != null) {
			// The game files are converted in the background. Detach the
			// background
			// task from this activity. It will keep on running until finished.
			mGameFileConverter.detachFromActivity();
		}
		return new ConfigurationInstanceState(mGridGeneratorTask,
				mGameFileConverter, mInputMode);
	}

	public void setOnSolvedHandler() {
		this.mGrid.setSolvedHandler(this.mGrid.new OnSolvedListener() {
			@Override
			public void puzzleSolved() {
				stopTimer();

				setInputMode(InputMode.NO_INPUT__DISPLAY_GRID);
				if (mGrid.isActive() && !mGrid.isSolvedByCheating()
						&& mGrid.countMoves() > 0) {
					// Only display animation in case the user has just
					// solved this game. Do not show in case the user
					// cheated by requesting to show the solution or in
					// case an already solved game was reloaded.
					animText(R.string.main_ui_solved_messsage, 0xFF002F00);
				}

				// Unselect current cell / cage
				mGrid.setSelectedCell(null);
			}
		});
	}

	/**
	 * Load the new grid and set control visibility.
	 * 
	 * @param grid
	 *            The grid to display.
	 */
	public void setNewGrid(Grid grid) {
		if (grid != null) {
			mGrid = grid;
			mGridView.loadNewGrid(grid);

			// Show the grid of the loaded puzzle.
			if (mGrid.isActive()) {
				// Determine input mode. The input mode will only be set if it
				// was not yet set before.
				if (mInputMode == InputMode.NO_INPUT__HIDE_GRID) {
					setInputMode(InputMode.NORMAL);
				} else {
					// In case an unsolved game is displayed and the screen is
					// rotated, the input mode (maybe versus normal) will
					// already be restored in the onCreate. Reset the input mode
					// to the same value in order to update all controls
					// relavant to this new grid.
					setInputMode(mInputMode);
				}
				setClearAndUndoButtonVisibility(null);

				startTimer();

				// Handler for solved game
				setOnSolvedHandler();
			} else {
				setInputMode(InputMode.NO_INPUT__DISPLAY_GRID);
				stopTimer();
			}

			// Debug information
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				mGameSeedLabel.setVisibility(View.VISIBLE);
				mGameSeedText.setVisibility(View.VISIBLE);
				mGameSeedText
						.setText(String.format("%,d", mGrid.getGameSeed()));
			}
		} else {
			// No grid available.
			setInputMode(InputMode.NO_INPUT__HIDE_GRID);
		}
	}

	/**
	 * Start a new timer (only in case the grid is active).
	 */
	private void startTimer() {
		// Stop old timer
		stopTimer();

		if (mGrid != null && mGrid.isActive()) {
			mTimerTask = new GameTimer(this);
			mTimerTask.mElapsedTime = mGrid.getElapsedTime();
			mTimerTask.mCheatPenaltyTime = mGrid.getCheatPenaltyTime();
			if (mMathDokuPreferences.isTimerVisible()) {
				mTimerText.setVisibility(View.VISIBLE);
			} else {
				mTimerText.setVisibility(View.GONE);
			}
			mTimerTask.execute();
		}
	}

	/**
	 * Stop the current timer.
	 */
	public void stopTimer() {
		// Stop timer if running
		if (mTimerTask != null && !mTimerTask.isCancelled()) {
			if (mGrid != null) {
				this.mGrid.setElapsedTime(mTimerTask.mElapsedTime,
						mTimerTask.mCheatPenaltyTime);
			}
			mTimerTask.cancel(true);
		}
	}

	/**
	 * Set the new input mode.
	 * 
	 * @param inputMode
	 *            The new input mode to be set.
	 */
	public void setInputMode(InputMode inputMode) {
		this.mInputMode = inputMode;

		// Visibility of grid view
		switch (inputMode) {
		case NO_INPUT__HIDE_GRID:
			mPuzzleGridLayout.setVisibility(View.GONE);
			break;
		case NO_INPUT__DISPLAY_GRID:
		case NORMAL:
		case MAYBE:
			mPuzzleGridLayout.setVisibility(View.VISIBLE);
			break;
		}

		// visibility of pressMenu, controls and inputMode
		switch (inputMode) {
		case NO_INPUT__HIDE_GRID:
			mTimerText.setVisibility(View.GONE);
			mControls.setVisibility(View.GONE);
			mStartButton.setVisibility(View.VISIBLE);
			break;
		case NO_INPUT__DISPLAY_GRID:
			if (mGrid == null || (mGrid != null && mGrid.isSolvedByCheating())) {
				// Hide time in case the puzzle was solved by
				// requesting to show the solution.
				this.mTimerText.setVisibility(View.INVISIBLE);
			} else {
				// Show time
				this.mTimerText.setVisibility(View.VISIBLE);
				setElapsedTime(mGrid.getElapsedTime());
			}
			mControls.setVisibility(View.GONE);
			mStartButton.setVisibility(View.VISIBLE);

			// Determine the layout to be used for maybe values inside a grid
			if (mGrid != null) {
				setDigitPositionGrid(inputMode);
			}
			break;
		case NORMAL:
		case MAYBE:
			mSolvedText.setVisibility(View.GONE);
			mStartButton.setVisibility(View.GONE);
			if (mMathDokuPreferences.isTimerVisible()) {
				mTimerText.setVisibility(View.VISIBLE);
			}
			if (!mMathDokuPreferences.isControlsBlockHidden()) {
				this.mControls.setVisibility(View.VISIBLE);
			}

			// Determine the color which is used for text which depends on the
			// actual input mode
			int color = (inputMode == InputMode.NORMAL ? mPainter
					.getHighlightedTextColorNormalInputMode() : mPainter
					.getHighlightedTextColorMaybeInputMode());

			// Set text and color for input mode label
			mInputModeTextView.setTextColor(color);
			mInputModeTextView
					.setText(getResources()
							.getString(
									(inputMode == InputMode.NORMAL ? R.string.input_mode_normal_long
											: R.string.input_mode_maybe_long)));

			// Determine the layout to be used for the digit buttons and maybe
			// values inside a grid
			if (mGrid != null) {
				setDigitPositionGrid(inputMode);
			}
			break;
		}
		
		mGridView.invalidate();
	}

	/**
	 * Toggles the input mode to the next available state.
	 */
	public void toggleInputMode() {
		InputMode inputMode = InputMode.NO_INPUT__HIDE_GRID;
		switch (mInputMode) {
		case NO_INPUT__HIDE_GRID:
			// fall through
		case NO_INPUT__DISPLAY_GRID:
			inputMode = InputMode.NORMAL;
			break;
		case NORMAL:
			if (TipInputModeChanged.toBeDisplayed(mMathDokuPreferences)) {
				new TipInputModeChanged(this, InputMode.MAYBE).show();
			}
			inputMode = InputMode.MAYBE;
			break;
		case MAYBE:
			if (TipInputModeChanged.toBeDisplayed(mMathDokuPreferences)) {
				new TipInputModeChanged(this, InputMode.MAYBE).show();
			}
			inputMode = InputMode.NORMAL;
			break;
		}
		setInputMode(inputMode);
	}

	/**
	 * Sets the timer text with the actual elapsed time.
	 * 
	 * @param elapsedTime
	 *            The elapsed time (in mili seconds) while playing the game.
	 */
	@SuppressLint("DefaultLocale")
	public void setElapsedTime(long elapsedTime) {
		if (mMathDokuPreferences.isTimerVisible() && mTimerText != null) {
			mTimerText.setText(Util.durationTimeToString(elapsedTime));
		}
	}

	/**
	 * Set the digit position grid for layout the digit buttons and maybe
	 * values.
	 * 
	 * @param inputMode
	 *            The new input mode to be set.
	 */
	private void setDigitPositionGrid(InputMode inputMode) {
		// Determine the color which is used for text which depends on the
		// actual input mode
		int color = (inputMode == InputMode.NORMAL ? mPainter
				.getHighlightedTextColorNormalInputMode() : mPainter
				.getHighlightedTextColorMaybeInputMode());

		// Determine the digit position grid type to be used based on screen
		// dimensions.
		DigitPositionGridType digitPositionGridType = DigitPositionGridType.GRID_3X3;
		if (getResources().getString(R.string.dimension).equals("small-port")) {
			digitPositionGridType = DigitPositionGridType.GRID_2X5;
		}

		// Only create the digit position grid if needed
		if (mDigitPositionGrid == null
				|| !mDigitPositionGrid.isReusable(digitPositionGridType,
						mGrid.getGridSize())) {
			// Create the mapping for mDigitPosition on the correct button
			// grid layout.
			mDigitPositionGrid = new DigitPositionGrid(digitPositionGridType,
					mGrid.getGridSize());

			// The weight of the input mode has to be aligned with the
			// number of columns containing digit buttons.
			TableRow.LayoutParams layoutParams = (TableRow.LayoutParams) mInputModeTextView
					.getLayoutParams();
			layoutParams.weight = mDigitPositionGrid.getVisibleDigitColumns();
			mInputModeTextView.setLayoutParams(layoutParams);

			// Propagate setting to the grid view as well for displaying maybe
			// values (dependent on preferences).
			mGridView.setDigitPositionGrid(mDigitPositionGrid);
		}

		// Use the created mapping to fill all digit positions.
		for (int i = 0; i < mDigitPosition.length; i++) {
			int value = mDigitPositionGrid.getValue(i);
			mDigitPosition[i].setText(value > 0 ? Integer.toString(value) : "");
			mDigitPosition[i]
					.setVisibility(mDigitPositionGrid.getVisibility(i));
			mDigitPosition[i].setTextColor(color);
		}
		if (mDigitPositionGrid.isGrid2x5()) {
			// This layout also has a buttonposition10 which is never
			// used to put a button there. However for a correct layout
			// of the buttons the visibility has to be set correctly.
			View view = findViewById(R.id.digitSelect10);
			if (view != null) {
				view.setVisibility(mDigitPositionGrid.getVisibility(9));
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (sharedPreferences.contains(key)) {
			UsageLog.getInstance().logPreference("Preference.Changed", key,
					sharedPreferences.getAll().get(key));
		} else {
			UsageLog.getInstance().logPreference("Preference.Deleted", key,
					null);
		}
		if (key.equals(Preferences.THEME)) {
			setTheme();
			setInputMode(mInputMode);
		}
	}

	/**
	 * Handles revealing of user value in the given cell.
	 * 
	 * @param selectedCell
	 *            The cell for which the user value has to be revealed.
	 */
	private void revealCell(GridCell selectedCell) {
		UsageLog.getInstance().logFunction("ContextMenu.RevealCell");
		if (selectedCell == null) {
			return;
		}

		// Reveal the user value
		CellChange orginalUserMove = selectedCell.saveUndoInformation(null);
		selectedCell.setUserValue(selectedCell.getCorrectValue());
		if (mMathDokuPreferences.isClearRedundantPossiblesEnabled()) {
			// Update possible values for other cells in this row and
			// column.
			mGrid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
		}
		selectedCell.setCheated();

		mGrid.increaseCounter(StatisticsCounterType.CELLS_REVEALED);
		registerAndProcessCheat(CheatType.CELL_REVEALED);

		this.mGridView.invalidate();
	}

	/**
	 * Handles revealing of the operator of the given cage.
	 * 
	 * @param selectedCell
	 *            The cage for which the operator has to be revealed.
	 */
	private void revealOperator(GridCage selectedGridCage) {
		UsageLog.getInstance().logFunction("ContextMenu.RevealOperator");
		if (selectedGridCage == null) {
			return;
		}

		selectedGridCage.revealOperator();

		mGrid.increaseCounter(StatisticsCounterType.OPERATORS_REVEALED);
		registerAndProcessCheat(CheatType.OPERATOR_REVEALED);

		mGridView.invalidate();
	}

	/**
	 * Registers and processes a cheat of the given type.
	 * 
	 * @param cheatType
	 *            The type of cheat to be processed.
	 */
	private void registerAndProcessCheat(CheatType cheatType) {
		// Create new cheat
		Cheat cheat = new Cheat(this, cheatType);

		// Add penalty time
		if (mTimerTask != null) {
			mTimerTask.addCheatPenaltyTime(cheat);
		}

		// Display hint or toast
		if (TipCheat.toBeDisplayed(mMathDokuPreferences, cheat)) {
			new TipCheat(this, cheat).show();
		} else {
			Toast.makeText(this, R.string.main_ui_cheat_messsage,
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Checks the progress of solving the current grid
	 */
	private void checkProgress() {
		UsageLog.getInstance().logFunction("Menu.CheckProgress");
		if (mGrid == null || mGridView == null) {
			return;
		}

		boolean allUserValuesValid = mGrid.isSolutionValidSoFar();
		int countNewInvalidChoices = (allUserValuesValid ? 0 : mGridView
				.markInvalidChoices());

		// Create new cheat
		Cheat cheat = new Cheat(this, CheatType.CHECK_PROGRESS_USED,
				countNewInvalidChoices);

		// Register cheat in statistics
		mGrid.getGridStatistics().increaseCounter(
				StatisticsCounterType.CHECK_PROGRESS_USED);
		mGrid.getGridStatistics().increaseCounter(
				StatisticsCounterType.CHECK_PROGRESS_INVALIDS_FOUND,
				countNewInvalidChoices);

		// Add penalty time
		if (mTimerTask != null) {
			mTimerTask.addCheatPenaltyTime(cheat);
		}

		// Display hint or toast
		if (TipCheat.toBeDisplayed(mMathDokuPreferences, cheat)) {
			new TipCheat(this, cheat).show();
		} else if (allUserValuesValid) {
			Toast.makeText(this, R.string.ProgressOK, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, R.string.ProgressBad, Toast.LENGTH_SHORT)
					.show();
		}

		// Never show the tip about incorrect values and reference to function
		// Check Progress again.
		TipIncorrectValue.doNotDisplayAgain(mMathDokuPreferences);
	}

	/**
	 * Checks whether the clear and undo buttons should be visible in case the
	 * given cell is selected.
	 * 
	 * @param cell
	 *            The cell to be used to check whether the clear and undo button
	 *            should be visible. Use null in case no cell is selected.
	 */
	private void setClearAndUndoButtonVisibility(GridCell cell) {
		((Button) findViewById(R.id.clearButton))
				.setVisibility((cell == null || cell.isEmpty()) ? View.INVISIBLE
						: View.VISIBLE);
		((Button) findViewById(R.id.undoButton))
				.setVisibility((mGrid == null || mGrid.countMoves() == 0) ? View.INVISIBLE
						: View.VISIBLE);
	}
}