package net.cactii.mathdoku;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import net.cactii.mathdoku.DigitPositionGrid.DigitPositionGridType;
import net.cactii.mathdoku.GameFile.GameFileType;
import net.cactii.mathdoku.Painter.GridTheme;
import net.cactii.mathdoku.TipDialog.TipType;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Gravity;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public final static String TAG = "MathDoku.MainActivity";

	public final static String PROJECT_HOME = "https://code.google.com/p/mathdoku/";
	// Identifiers for preferences.

	public final static String PREF_CLEAR_REDUNDANT_POSSIBLES = "redundantPossibles";
	public final static boolean PREF_CLEAR_REDUNDANT_POSSIBLES_DEFAULT = true;

	public final static String PREF_CREATE_PREVIEW_IMAGES_COMPLETED = "CreatePreviewImagesCompleted";
	public final static boolean PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT = false;

	public final static String PREF_CURRENT_VERSION = "currentversion";
	public final static int PREF_CURRENT_VERSION_DEFAULT = -1;

	public final static String PREF_ALLOW_BIG_CAGES = "AllowBigCages";
	public final static boolean PREF_ALLOW_BIG_CAGES_DEFAULT = false;

	public final static String PREF_HIDE_CONTROLS = "hideselector";
	public final static boolean PREF_HIDE_CONTROLS_DEFAULT = false;

	public final static String PREF_HIDE_OPERATORS = "hideoperatorsigns";
	public final static String PREF_HIDE_OPERATORS_ALWAYS = "T";
	public final static String PREF_HIDE_OPERATORS_ASK = "A";
	public final static String PREF_HIDE_OPERATORS_NEVER = "F";
	public final static String PREF_HIDE_OPERATORS_DEFAULT = PREF_HIDE_OPERATORS_NEVER;

	public final static String PREF_SHOW_MAYBES_AS_3X3_GRID = "maybe3x3";
	public final static boolean PREF_SHOW_MAYBES_AS_3X3_GRID_DEFAULT = true;

	public final static String PREF_SHOW_BAD_CAGE_MATHS = "badmaths";
	public final static boolean PREF_SHOW_BAD_CAGE_MATHS_DEFAULT = true;

	public final static String PREF_SHOW_DUPE_DIGITS = "dupedigits";
	public final static boolean PREF_SHOW_DUPE_DIGITS_DEFAULT = true;

	public final static String PREF_SHOW_TIMER = "timer";
	public final static boolean PREF_SHOW_TIMER_DEFAULT = true;

	public final static String PREF_PLAY_SOUND_EFFECTS = "soundeffects";
	public final static boolean PREF_PLAY_SOUND_EFFECTS_DEFAULT = true;

	public final static String PREF_THEME = "theme";
	public final static String PREF_THEME_CARVED = "carved";
	public final static String PREF_THEME_DARK = "inverted";
	public final static String PREF_THEME_NEWSPAPER = "newspaper";
	public final static String PREF_THEME_DEFAULT = PREF_THEME_NEWSPAPER;

	public final static String PREF_USAGE_LOG_COUNT_GAMES_STARTED = "UsageLogCountGamesStarted";
	public final static int PREF_USAGE_LOG_COUNT_GAMES_STARTED_DEFAULT = 0;

	public final static String PREF_USAGE_LOG_DISABLED = "UsageLogDisabled";
	public final static boolean PREF_USAGE_LOG_DISABLED_DEFAULT = false;

	public final static String PREF_WAKE_LOCK = "wakelock";
	public final static boolean PREF_WAKE_LOCK_DEFAULT = true;

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

	// The input mode which is currently active
	private InputMode mInputMode;
	Button mInputModeTextView;

	TextView solvedText;
	TextView pressMenu;
	GameTimer mTimerTask;

	RelativeLayout topLayout;
	RelativeLayout puzzleGrid;
	TableLayout controls;
	TextView mGameSeedLabel;
	TextView mGameSeedText;
	TextView mTimerText;

	// Digit positions are the places on which the digit buttons can be placed.
	Button mDigitPosition[] = new Button[9];

	Button clearDigit;
	Button undoButton;
	View[] sound_effect_views;
	private Animation outAnimation;
	private Animation solvedAnimation;

	public SharedPreferences preferences;

	// Background tasks for generating a new puzzle and converting game files
	public GridGenerator mGridGeneratorTask;
	public GameFileConverter mGameFileConverter;

	// Variables for process of creating preview images of game file for which
	// no preview image does exist.
	private GameFile mGameFileImagePreviewCreation;
	private ProgressDialog mProgressDialogImagePreviewCreation;

	final Handler mHandler = new Handler();

	// Object to save data on a configuration change
	private class ConfigurationInstanceState {
		private GridGenerator mGridGeneratorTask;
		private GameFileConverter mGameFileConverter;
		private InputMode mInputMode;

		public ConfigurationInstanceState(GridGenerator gridGeneratorTask,
				GameFileConverter gameFileConverterTask, InputMode inputMode) {
			mGridGeneratorTask = gridGeneratorTask;
			mGameFileConverter = gameFileConverterTask;
			mInputMode = inputMode;
		}

		public GridGenerator getGridGeneratorTask() {
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		Display display = getWindowManager().getDefaultDisplay();
		if (display.getHeight() < 750) {
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(R.layout.main);

		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);

		this.topLayout = (RelativeLayout) findViewById(R.id.topLayout);
		this.puzzleGrid = (RelativeLayout) findViewById(R.id.puzzleGrid);
		this.mGridView = (GridView) findViewById(R.id.gridView);
		this.solvedText = (TextView) findViewById(R.id.solvedText);
		this.mGridView.animText = this.solvedText;
		this.pressMenu = (TextView) findViewById(R.id.pressMenu);
		this.controls = (TableLayout) findViewById(R.id.controls);
		this.mGameSeedLabel = (TextView) findViewById(R.id.gameSeedLabel);
		this.mGameSeedText = (TextView) findViewById(R.id.gameSeedText);
		this.mTimerText = (TextView) findViewById(R.id.timerText);

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
		this.clearDigit = (Button) findViewById(R.id.clearButton);
		this.undoButton = (Button) findViewById(R.id.undoButton);

		this.sound_effect_views = new View[] { this.mGridView,
				this.mDigitPosition[0], this.mDigitPosition[1],
				this.mDigitPosition[2], this.mDigitPosition[3],
				this.mDigitPosition[4], this.mDigitPosition[5],
				this.mDigitPosition[6], this.mDigitPosition[7],
				this.mDigitPosition[8], this.clearDigit,
				this.mInputModeTextView, this.undoButton };

		this.mPainter = Painter.getInstance(this);

		setInputMode(InputMode.NO_INPUT__HIDE_GRID);

		// Animation for a solved puzzle
		solvedAnimation = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.solvedanim);
		solvedAnimation.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				solvedText.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});

		// Animation for controls.
		outAnimation = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.selectorzoomout);
		outAnimation.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				controls.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});

		this.mGridView
				.setOnGridTouchListener(this.mGridView.new OnGridTouchListener() {
					@Override
					public void gridTouched(GridCell cell,
							boolean sameCellSelectedAgain) {
						if (MainActivity.this.preferences.getBoolean(
								PREF_HIDE_CONTROLS, PREF_HIDE_CONTROLS_DEFAULT)) {
							if (controls.getVisibility() == View.VISIBLE) {
								controls.startAnimation(outAnimation);
								mGridView.mSelectorShown = false;
								mGridView.requestFocus();
							} else {
								controls.setVisibility(View.VISIBLE);
								Animation animation = AnimationUtils
										.loadAnimation(MainActivity.this,
												R.anim.selectorzoomin);
								controls.startAnimation(animation);
								mGridView.mSelectorShown = true;
								controls.requestFocus();
							}
						} else {
							// Controls are always visible
							if (sameCellSelectedAgain) {
								toggleInputMode();
							}
						}
					}
				});

		for (int i = 0; i < mDigitPosition.length; i++)
			this.mDigitPosition[i].setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Convert text of button (number) to Integer
					int d = Integer.parseInt(((Button) v).getText().toString());
					MainActivity.this.digitSelected(d);
				}
			});
		this.clearDigit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(0);
			}
		});
		this.undoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (MainActivity.this.mGrid.UndoLastMove()) {
					// Succesfull undo
					mGridView.invalidate();
				}

				if (MainActivity.this.preferences.getBoolean(
						PREF_HIDE_CONTROLS, PREF_HIDE_CONTROLS_DEFAULT)) {
					MainActivity.this.controls.setVisibility(View.GONE);
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
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
			final MainActivity activity = this;
			this.mGameSeedText.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (mGrid != null) {
							mGrid.getGridGeneratingParameters().show(activity);
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
							mGrid.getGridGeneratingParameters().show(activity);
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
		Object object = this.getLastNonConfigurationInstance();
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

		restartLastGame();
	}

	public void onPause() {
		UsageLog.getInstance().close();

		if (mGrid != null && mGrid.getGridSize() > 3) {
			GameFile saver = new GameFile(GameFileType.LAST_GAME);
			if (mTimerTask != null) {
				// Update elapsed time in grid.
				this.mGrid.setElapsedTime(mTimerTask.mElapsedTime);
			}
			saver.save(mGrid, this.mGridView);
		}
		stopTimer();

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

	public void setTheme() {
		pressMenu.setTextColor(0xff000000);
		pressMenu.setBackgroundColor(0xa0f0f0f0);
		String theme = preferences.getString(PREF_THEME, PREF_THEME_DEFAULT);
		solvedText.setTypeface(mPainter.mGridPainter.mSolvedTypeface);

		if (theme.equals(MainActivity.PREF_THEME_NEWSPAPER)) {
			topLayout.setBackgroundResource(R.drawable.newspaper);
			mPainter.setTheme(GridTheme.NEWSPAPER);
			mTimerText.setBackgroundColor(0x90808080);
		} else if (theme.equals(MainActivity.PREF_THEME_DARK)) {
			topLayout.setBackgroundResource(R.drawable.newspaper_dark);
			mPainter.setTheme(GridTheme.DARK);
			pressMenu.setTextColor(0xfff0f0f0);
			pressMenu.setBackgroundColor(0xff000000);
			mTimerText.setTextColor(0xFFF0F0F0);
		} else if (theme.equals(MainActivity.PREF_THEME_CARVED)) {
			topLayout.setBackgroundResource(R.drawable.background);
			mPainter.setTheme(GridTheme.CARVED);
			mTimerText.setBackgroundColor(0x10000000);
		}

		this.mGridView.invalidate();
	}

	public void onResume() {
		UsageLog.getInstance(this);

		if (this.preferences.getBoolean(PREF_WAKE_LOCK, PREF_WAKE_LOCK_DEFAULT)) {
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

		// Propagate preferences to grid
		if (mGrid != null) {
			mGrid.setPreferences(preferences);
		}

		this.setSoundEffectsEnabled(this.preferences.getBoolean(
				PREF_PLAY_SOUND_EFFECTS, PREF_PLAY_SOUND_EFFECTS_DEFAULT));

		super.onResume();

		if (mTimerTask == null
				|| (mTimerTask != null && mTimerTask.isCancelled())) {
			startTimer();
		}
	}

	public void setSoundEffectsEnabled(boolean enabled) {
		for (View v : this.sound_effect_views)
			v.setSoundEffectsEnabled(enabled);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != 7 || resultCode != Activity.RESULT_OK)
			return;
		Bundle extras = data.getExtras();
		String filename = extras.getString("filename");
		Log.d("Mathdoku", "Loading game: " + filename);
		Grid newGrid = new GameFile(filename).load();
		if (newGrid != null) {
			setNewGrid(newGrid);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Disable or enable option to check progress depending on whether grid
		// is active
		menu.findItem(R.id.checkprogress).setVisible(
				(mGrid != null && mGrid.isActive()));

		// Load/save can only be used in case a grid is displayed (which can be
		// saved) or in case a game file exists which can be loaded.
		menu.findItem(R.id.saveload)
				.setVisible(
						(mGrid != null && mGrid.isActive())
								|| GameFileList.canBeUsed());

		// When running in development mode, an extra menu is available.
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
			menu.findItem(R.id.menu_development_mode).setVisible(true);
		} else {
			menu.findItem(R.id.menu_development_mode).setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
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
		for (GridCell cell : selectedGridCage.mCells) {
			if (cell.countPossibles() == 1) {
				// At least one cell within this cage has exactly one possible
				// value.
				menu.add(0, CONTEXT_MENU_USE_CAGE_MAYBES, 0,
						R.string.context_menu_use_cage_maybes);
				break;
			}
		}

		// Option: reveal the operator of the selected cage
		if (selectedGridCage != null && selectedGridCage.isOperatorHidden()) {
			menu.add(0, CONTEXT_MENU_REVEAL_OPERATOR, 0,
					R.string.context_menu_reveal_operator);
		}

		// Option: clear all cell in the selected cage
		for (GridCell cell : selectedGridCage.mCells) {
			if (cell.isUserValueSet() || cell.countPossibles() > 0) {
				// At least one cell within this cage has a value or a possible
				// value.
				menu.add(0, CONTEXT_MENU_CLEAR_CAGE_CELLS, 0,
						R.string.context_menu_clear_cage_cells);
				break;
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
	}

	public boolean onContextItemSelected(MenuItem item) {
		// Get selected cell
		GridCell selectedCell = mGrid.getSelectedCell();
		GridCage selectedGridCage = mGrid.getCageForSelectedCell();

		switch (item.getItemId()) {
		case CONTEXT_MENU_CLEAR_CAGE_CELLS:
			UsageLog.getInstance().logFunction("ContextMenu.ClearCageCells");
			if (selectedCell == null) {
				break;
			}
			for (GridCell cell : selectedGridCage.mCells) {
				cell.saveUndoInformation(null);
				cell.clearUserValue();
			}
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
					if (preferences.getBoolean(PREF_CLEAR_REDUNDANT_POSSIBLES,
							PREF_CLEAR_REDUNDANT_POSSIBLES_DEFAULT)) {
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
			UsageLog.getInstance().logFunction("ContextMenu.RevealCell");
			if (selectedCell == null) {
				break;
			}
			CellChange orginalUserMove = selectedCell.saveUndoInformation(null);
			selectedCell.setUserValue(selectedCell.getCorrectValue());
			if (preferences.getBoolean(PREF_CLEAR_REDUNDANT_POSSIBLES,
					PREF_CLEAR_REDUNDANT_POSSIBLES_DEFAULT)) {
				// Update possible values for other cells in this row and
				// column.
				mGrid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
			}
			selectedCell.setCheated();
			Toast.makeText(this, R.string.main_ui_cheat_messsage,
					Toast.LENGTH_SHORT).show();
			this.mGridView.invalidate();
			break;
		case CONTEXT_MENU_CLEAR_GRID:
			openClearDialog();
			break;
		case CONTEXT_MENU_SHOW_SOLUTION:
			this.mGrid.Solve();
			break;
		case CONTEXT_MENU_REVEAL_OPERATOR:
			if (selectedGridCage == null) {
				break;
			}
			UsageLog.getInstance().logFunction("ContextMenu.RevealOperator");
			selectedGridCage.revealOperator();
			mGridView.invalidate();
		}
		return super.onContextItemSelected(item);
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
		case R.id.saveload:
			UsageLog.getInstance().logFunction("Menu.SaveLoad");
			Intent i = new Intent(this, GameFileList.class);
			startActivityForResult(i, 7);
			return true;
		case R.id.checkprogress:
			int textId;
			UsageLog.getInstance().logFunction("Menu.CheckProgress");
			if (mGrid.isSolutionValidSoFar())
				textId = R.string.ProgressOK;
			else {
				textId = R.string.ProgressBad;
				mGridView.markInvalidChoices();
			}
			Toast toast = Toast.makeText(getApplicationContext(), textId,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return true;
		case R.id.options:
			UsageLog.getInstance().logFunction("Menu.ViewOptions");
			startActivityForResult(new Intent(MainActivity.this,
					OptionsActivity.class), 0);
			return true;
		case R.id.help:
			UsageLog.getInstance().logFunction("Menu.ViewHelp.Manual");
			this.openHelpDialog();
			return true;
		case R.id.development_mode_generate_games:
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				// Cancel old timer
				stopTimer();

				// Generate games
				DevelopmentHelper.generateGames(this);
			}
			return true;
		case R.id.development_mode_recreate_previews:
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				DevelopmentHelper.recreateAllPreviews(this);
			}
			return true;
		case R.id.development_mode_delete_games:
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				DevelopmentHelper.deleteAllGames(this);
			}
			return true;
		case R.id.development_mode_reset_preferences:
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				DevelopmentHelper.resetPreferences(this, 77);
			}
			return true;
		case R.id.development_mode_clear_data:
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				DevelopmentHelper.deleteGamesAndPreferences(this);
			}
			return true;
		case R.id.development_mode_reset_log:
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				// Delete old log
				UsageLog.getInstance().delete();

				// Reset preferences
				Editor prefeditor = preferences.edit();
				prefeditor.putBoolean(PREF_USAGE_LOG_DISABLED,
						PREF_USAGE_LOG_DISABLED_DEFAULT);
				prefeditor.putInt(PREF_USAGE_LOG_COUNT_GAMES_STARTED,
						PREF_USAGE_LOG_COUNT_GAMES_STARTED_DEFAULT);
				prefeditor.commit();

				// Re-enable usage log
				UsageLog.getInstance(this);
			}
			return true;
		case R.id.development_mode_send_log:
			UsageLog.getInstance().askConsentForSendingLog(this);
			return true;
		default:
			return super.onOptionsItemSelected(menuItem);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& this.mGridView.mSelectorShown) {
			this.controls.setVisibility(View.GONE);
			this.mGridView.requestFocus();
			this.mGridView.mSelectorShown = false;
			this.mGridView.invalidate();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void digitSelected(int value) {
		this.mGridView.digitSelected(value, mInputMode);

		if (this.preferences.getBoolean(PREF_HIDE_CONTROLS,
				PREF_HIDE_CONTROLS_DEFAULT)) {
			this.controls.setVisibility(View.GONE);
		}
		this.mGridView.requestFocus();
		this.mGridView.mSelectorShown = false;
		this.mGridView.invalidate();
	}

	private void restartLastGame() {
		Grid newGrid = new GameFile(GameFileType.LAST_GAME).load();
		setNewGrid(newGrid);
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
		String hideOperators = this.preferences.getString(PREF_HIDE_OPERATORS,
				PREF_HIDE_OPERATORS_DEFAULT);
		if (hideOperators.equals(PREF_HIDE_OPERATORS_ALWAYS)) {
			// All new games should be generated with hidden operators.
			this.startNewGame(gridSize, true);
			return true;
		} else if (hideOperators.equals(PREF_HIDE_OPERATORS_NEVER)) {
			// All new games should be generated with visible operators.
			this.startNewGame(gridSize, false);
			return true;
		} else if (hideOperators.equals(PREF_HIDE_OPERATORS_ASK)) {
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

		// Start a background task to generate the new grid. As soon as the new
		// grid is created, the method onNewGridReady will be called.
		int maxCageSize = (preferences.getBoolean(PREF_ALLOW_BIG_CAGES,
				PREF_ALLOW_BIG_CAGES_DEFAULT) ? 6 : 4);
		int maxCageResult = getResources().getInteger(
				R.integer.maximum_cage_value);
		mGridGeneratorTask = new GridGenerator(this, gridSize, maxCageSize,
				maxCageResult, hideOperators);
		mGridGeneratorTask.execute();
	}

	/**
	 * Reactivate the main ui after a new game is loaded into the grid view by
	 * the ASync GridGenerator task.
	 */
	public void onNewGridReady(final Grid newGrid) {
		if (mGrid != null) {
			UsageLog.getInstance().logGrid("Menu.StartNewGame.OldGame", mGrid);

			if (mGrid.moves.size() > 0) {
				// Increase counter for number of games on which playing has
				// been started.
				int countGamesStarted = preferences.getInt(
						PREF_USAGE_LOG_COUNT_GAMES_STARTED, 0) + 1;
				Editor prefeditor = preferences.edit();
				prefeditor.putInt(PREF_USAGE_LOG_COUNT_GAMES_STARTED,
						countGamesStarted);
				prefeditor.commit();

				// Check if we are going to ask the user to send feedback
				// TODO: change to definitive values
				if (countGamesStarted == 3 || countGamesStarted == 5
						|| countGamesStarted == 20 || countGamesStarted == 50) {
					UsageLog.getInstance().askConsentForSendingLog(this);

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
		this.solvedText.setText(textIdentifier);
		this.solvedText.setTextColor(color);
		this.solvedText.setVisibility(View.VISIBLE);
		final float SCALE_FROM = (float) 0;
		final float SCALE_TO = (float) 1.0;
		ScaleAnimation anim = new ScaleAnimation(SCALE_FROM, SCALE_TO,
				SCALE_FROM, SCALE_TO, this.mGridView.mGridViewSize / 2,
				this.mGridView.mGridViewSize / 2);
		anim.setDuration(1000);
		// animText.setAnimation(anim);
		this.solvedText.startAnimation(this.solvedAnimation);
	}

	private void openHelpDialog() {
		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.helpview, null);

		TextView tv = (TextView) view
				.findViewById(R.id.dialog_help_version_body);
		tv.setText(getVersionName() + " (revision " + getVersionNumber() + ")");

		tv = (TextView) view.findViewById(R.id.help_project_home_link);
		tv.setText(PROJECT_HOME);

		new AlertDialog.Builder(MainActivity.this)
				.setTitle(
						getResources().getString(R.string.application_name)
								+ (DevelopmentHelper.mode == Mode.DEVELOPMENT ? " r"
										+ getVersionNumber() + " "
										: " ")
								+ getResources().getString(R.string.menu_help))
				.setIcon(R.drawable.about)
				.setView(view)
				.setNeutralButton(R.string.menu_changes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								UsageLog.getInstance().logFunction(
										"ViewChanges.Manual");
								MainActivity.this.openChangesDialog();
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
		View view = li.inflate(R.layout.changelogview, null);

		TextView textView = (TextView) view
				.findViewById(R.id.changelog_version_body);
		textView.setText(getVersionName() + " (revision " + getVersionNumber()
				+ ")");

		textView = (TextView) view.findViewById(R.id.changelog_changes_link);
		textView.setText(PROJECT_HOME + "wiki/RevisionHistory");

		textView = (TextView) view.findViewById(R.id.changelog_issues_link);
		textView.setText(PROJECT_HOME + "issues/list?groupby=milestone");

		new AlertDialog.Builder(MainActivity.this)
				.setTitle(
						getResources().getString(R.string.application_name)
								+ (DevelopmentHelper.mode == Mode.DEVELOPMENT ? " r"
										+ getVersionNumber() + " "
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
		new AlertDialog.Builder(MainActivity.this)
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
								MainActivity.this.mGrid.clearUserValues();
								MainActivity.this.mGridView.invalidate();
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

		int currentVersion = getVersionNumber();
		int previousInstalledVersion = preferences.getInt(PREF_CURRENT_VERSION,
				PREF_CURRENT_VERSION_DEFAULT);

		// Start phase 1 of the upgrade process if needed.
		if (previousInstalledVersion < currentVersion) {
			// On Each update of the game, all game files will be converted to
			// the latest definitions. On completion of the game file
			// conversion, method upgradePhase2_CreatePreviewImages will be
			// called.
			mGameFileConverter = new GameFileConverter(this,
					previousInstalledVersion, currentVersion);
			mGameFileConverter.execute();
		} else if (preferences.contains(PREF_CREATE_PREVIEW_IMAGES_COMPLETED)
				&& !preferences.getBoolean(
						PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
						PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT)) {
			// Skip Phase 1 and go directly to Phase to generate new previews.
			upgradePhase2_createPreviewImages(previousInstalledVersion,
					currentVersion);
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
		Editor prefeditor = preferences.edit();
		if (previousInstalledVersion < 121 && currentVersion >= 121) {
			// Add missing preferences to the Shared Preferences.
			if (!preferences.contains(PREF_CLEAR_REDUNDANT_POSSIBLES)) {
				prefeditor.putBoolean(PREF_CLEAR_REDUNDANT_POSSIBLES,
						PREF_CLEAR_REDUNDANT_POSSIBLES_DEFAULT);
			}
		}
		if (previousInstalledVersion < 123 && currentVersion >= 123) {
			// Add missing preferences to the Shared Preferences. Note:
			// those preferences have been introduced in revisions prior to
			// revision 122. But as from revision 122 the default values
			// have been removed from optionsview.xml to prevent conflicts
			// in defaults values with default values defined in this
			// activity.
			if (!preferences.contains(PREF_HIDE_CONTROLS)) {
				prefeditor.putBoolean(PREF_HIDE_CONTROLS,
						PREF_HIDE_CONTROLS_DEFAULT);
			}
			if (!preferences.contains(PREF_HIDE_OPERATORS)) {
				prefeditor.putString(PREF_HIDE_OPERATORS,
						PREF_HIDE_OPERATORS_DEFAULT);
			}
			if (!preferences.contains(PREF_PLAY_SOUND_EFFECTS)) {
				prefeditor.putBoolean(PREF_PLAY_SOUND_EFFECTS,
						PREF_PLAY_SOUND_EFFECTS_DEFAULT);
			}
			if (!preferences.contains(PREF_SHOW_BAD_CAGE_MATHS)) {
				prefeditor.putBoolean(PREF_SHOW_BAD_CAGE_MATHS,
						PREF_SHOW_BAD_CAGE_MATHS_DEFAULT);
			}
			if (!preferences.contains(PREF_SHOW_DUPE_DIGITS)) {
				prefeditor.putBoolean(PREF_SHOW_DUPE_DIGITS,
						PREF_SHOW_DUPE_DIGITS_DEFAULT);
			}
			if (!preferences.contains(PREF_SHOW_MAYBES_AS_3X3_GRID)) {
				prefeditor.putBoolean(PREF_SHOW_MAYBES_AS_3X3_GRID,
						PREF_SHOW_MAYBES_AS_3X3_GRID_DEFAULT);
			}
			if (!preferences.contains(PREF_SHOW_TIMER)) {
				prefeditor.putBoolean(PREF_SHOW_TIMER, PREF_SHOW_TIMER_DEFAULT);
			}
			if (!preferences.contains(PREF_THEME)) {
				prefeditor.putString(PREF_THEME, PREF_THEME_DEFAULT);
			}
			if (!preferences.contains(PREF_WAKE_LOCK)) {
				prefeditor.putBoolean(PREF_WAKE_LOCK, PREF_WAKE_LOCK_DEFAULT);
			}
		}
		if (previousInstalledVersion < 135 && currentVersion >= 135) {
			if (!preferences.contains(PREF_ALLOW_BIG_CAGES)) {
				prefeditor.putBoolean(PREF_ALLOW_BIG_CAGES,
						PREF_ALLOW_BIG_CAGES_DEFAULT);
			}
		}
		if (previousInstalledVersion < 175 && currentVersion >= 175) {
			if (!preferences.contains(PREF_USAGE_LOG_DISABLED)) {
				prefeditor.putBoolean(PREF_USAGE_LOG_DISABLED,
						PREF_USAGE_LOG_DISABLED_DEFAULT);
			}
			if (!preferences.contains(PREF_USAGE_LOG_COUNT_GAMES_STARTED)) {
				prefeditor.putInt(PREF_USAGE_LOG_COUNT_GAMES_STARTED,
						PREF_USAGE_LOG_COUNT_GAMES_STARTED_DEFAULT);
			}
		}
		prefeditor.putInt(PREF_CURRENT_VERSION, currentVersion);
		prefeditor.commit();

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

	public int getVersionNumber() {
		int version = -1;
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = pi.versionCode;
		} catch (Exception e) {
			Log.e("Mathdoku", "Package name not found", e);
		}
		return version;
	}

	public String getVersionName() {
		String versionname = "";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionname = pi.versionName;
		} catch (Exception e) {
			Log.e("Mathdoku", "Package name not found", e);
		}
		return versionname;
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

		if (this.preferences.getBoolean(PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
				PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT)) {
			// Previews have already been created. Go to next phase of upgrading
			upgradePhase3_UpdatePreferences(previousInstalledVersion,
					currentVersion);
			return;
		}

		// Determine the number of previews to be created.
		int countGameFilesWithoutPreview = countGameFilesWithoutPreview();
		if (countGameFilesWithoutPreview == 0) {
			// No games files without previews found.
			Editor prefEditor = preferences.edit();
			prefEditor.putBoolean(PREF_CREATE_PREVIEW_IMAGES_COMPLETED, true);
			prefEditor.commit();

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
		puzzleGrid.setVisibility(View.VISIBLE);
		pressMenu.setVisibility(View.GONE);

		// Runnable for handling the next step of preview image creation process
		// which can not be done until the grid view has been validated
		// (refreshed).
		final Runnable createNextPreviewImage = new Runnable() {
			public void run() {
				// If a game file was already loaded, it is now loaded and
				// visible in the grid view.
				if (mGameFileImagePreviewCreation != null) {
					// Save preview for the current game file.
					mGameFileImagePreviewCreation.savePreviewImage(mGridView);
					mProgressDialogImagePreviewCreation.incrementProgressBy(1);
				}

				// Check if a preview for another game file needs to be
				// created.
				mGameFileImagePreviewCreation = getNextGameFileWithoutPreview();
				if (mGameFileImagePreviewCreation != null) {
					Grid newGrid = mGameFileImagePreviewCreation.load();
					if (newGrid != null) {
						mGrid = newGrid;
						mGridView.loadNewGrid(mGrid);
						puzzleGrid.setVisibility(View.INVISIBLE);
						controls.setVisibility(View.GONE);
						pressMenu.setVisibility(View.GONE);

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

					Editor prefEditor = preferences.edit();
					prefEditor.putBoolean(PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
							true);
					prefEditor.commit();

					// Go to next phase of upgrading
					upgradePhase3_UpdatePreferences(previousInstalledVersion,
							currentVersion);

					setTheme();
				}
			}
		};

		// Post a message to start the process of creating image previews.
		mGameFileImagePreviewCreation = null;
		(new Thread() {
			public void run() {
				MainActivity.this.mHandler.post(createNextPreviewImage);
				mProgressDialogImagePreviewCreation.setProgress(1);
			}
		}).start();
	}

	/**
	 * Get the next game file for which no preview image does exist.
	 * 
	 * @return A game file having no preview image. Null in case all game files
	 *         have a preview image.
	 */
	private GameFile getNextGameFileWithoutPreview() {
		// Check all files in the game file directory.
		for (String filename : GameFile
				.getAllGameFilesCreatedByUser(Integer.MAX_VALUE)) {
			GameFile gameFile = new GameFile(filename);
			if (!gameFile.hasPreviewImage()) {
				// No preview image does exist.
				return gameFile;
			}
		}

		// No more game files found without having a preview image.
		return null;
	}

	/**
	 * Count number of game files without a preview.
	 * 
	 * @return The number of games files not having a preview image.
	 */
	private int countGameFilesWithoutPreview() {
		int countGameFilesWithoutPreview = 0;

		// Check all files in the game file directory.
		for (String filename : GameFile
				.getAllGameFilesCreatedByUser(Integer.MAX_VALUE)) {
			GameFile gameFile = new GameFile(filename);
			if (!gameFile.hasPreviewImage()) {
				// No preview image does exist.
				countGameFilesWithoutPreview++;
			}
		}

		return countGameFilesWithoutPreview;
	}

	/*
	 * Responds to a configuration change just before the activity is destroyed.
	 * In case a background task is still running, a reference to this task will
	 * be retained so that the activity can reconnect to this task as soon as it
	 * is resumed.
	 * 
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	public Object onRetainNonConfigurationInstance() {
		stopTimer();
		
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
				// Update elapsed time in grid
				mGrid.setElapsedTime(mTimerTask.mElapsedTime);
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
			mGrid.setPreferences(preferences);
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

				startTimer();

				// Handler for solved game
				setOnSolvedHandler();
			} else {
				setInputMode(InputMode.NO_INPUT__DISPLAY_GRID);
				stopTimer();
			}

			// Debug information
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
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
			if (preferences
					.getBoolean(PREF_SHOW_TIMER, PREF_SHOW_TIMER_DEFAULT)) {
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
	private void stopTimer() {
		// Stop timer if running
		if (mTimerTask != null && !mTimerTask.isCancelled()) {
			mTimerTask.cancel(true);
		}
	}

	/**
	 * Get the current input mode.
	 * 
	 * @return The current input mode.
	 */
	public InputMode getInputMode() {
		return mInputMode;
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
			puzzleGrid.setVisibility(View.GONE);
			break;
		case NO_INPUT__DISPLAY_GRID:
		case NORMAL:
		case MAYBE:
			puzzleGrid.setVisibility(View.VISIBLE);
			break;
		}

		// visibility of pressMenu, controls and inputMode
		switch (inputMode) {
		case NO_INPUT__HIDE_GRID:
			mTimerText.setVisibility(View.GONE);
			controls.setVisibility(View.GONE);
			pressMenu.setVisibility(View.VISIBLE);
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
			controls.setVisibility(View.GONE);
			pressMenu.setVisibility(View.VISIBLE);
			break;
		case NORMAL:
		case MAYBE:
			solvedText.setVisibility(View.GONE);
			pressMenu.setVisibility(View.GONE);
			if (preferences
					.getBoolean(PREF_SHOW_TIMER, PREF_SHOW_TIMER_DEFAULT)) {
				mTimerText.setVisibility(View.VISIBLE);
			}
			if (!MainActivity.this.preferences.getBoolean(PREF_HIDE_CONTROLS,
					PREF_HIDE_CONTROLS_DEFAULT)) {
				this.controls.setVisibility(View.VISIBLE);
			}

			// Determine the color which is used for text which depends on the
			// actual input mode
			int color = (inputMode == InputMode.NORMAL ? mPainter.mHighlightedTextColorNormalInputMode
					: mPainter.mHighlightedTextColorMaybeInputMode);

			// Set text and color for input mode label
			mInputModeTextView.setTextColor(color);
			mInputModeTextView.setText(getResources().getString(
					(inputMode == InputMode.NORMAL ? R.string.input_mode_normal_long
							: R.string.input_mode_maybe_long)));

			// Determine which buttons to show on what positions
			if (mGrid != null) {
				// Create the mapping for mDigitPosition on the correct button
				// grid layout.
				DigitPositionGridType digitPositionGridType = DigitPositionGridType.GRID_3X3;
				if (getResources().getString(R.string.dimension).equals(
						"small-port")) {
					digitPositionGridType = DigitPositionGridType.GRID_2X5;
				}
				DigitPositionGrid digitPositionGrid = new DigitPositionGrid(
						digitPositionGridType, mGrid.getGridSize());

				// Use the created mapping to fill all digit positions.
				for (int i = 0; i < mDigitPosition.length; i++) {
					int value = digitPositionGrid.getValue(i);
					mDigitPosition[i].setText(value > 0 ? Integer
							.toString(value) : "");
					mDigitPosition[i].setVisibility(digitPositionGrid
							.getVisibility(i));
					mDigitPosition[i].setTextColor(color);
				}
				if (digitPositionGridType == DigitPositionGridType.GRID_2X5) {
					// This layout also has a buttonposition10 which is never
					// used to put a button there. However for a correct layout
					// of the buttons the visibility has to be set correctly.
					View view = findViewById(R.id.digitSelect10);
					if (view != null) {
						view.setVisibility(digitPositionGrid.getVisibility(9));
					}
				}

				// Store mapping in the grid view so it can be reused when
				// drawing the cells.
				mGridView.setDigitPositionGrid(digitPositionGrid);
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
			new TipDialog(this, TipType.INPUT_MODE_CHANGED).changeToInputMode(InputMode.MAYBE).show();
			inputMode = InputMode.MAYBE;
			break;
		case MAYBE:
			new TipDialog(this, TipType.INPUT_MODE_CHANGED).changeToInputMode(InputMode.NORMAL).show();
			inputMode = InputMode.NORMAL;
			break;
		}
		setInputMode(inputMode);
	}

	/**
	 * Sets the timer text with the actual elapsed time.
	 * 
	 * @param timerTextView
	 *            The textview which has to be filled.
	 * @param elapsedTime
	 *            The elapsed time (in mili seconds) while playing the game.
	 */
	@SuppressLint("DefaultLocale")
	public void setElapsedTime(long elapsedTime) {
		if (preferences.getBoolean(PREF_SHOW_TIMER, PREF_SHOW_TIMER_DEFAULT)) {
			String timeString;
			int seconds = (int) (elapsedTime / 1000); // Whole seconds.
			int hours = (int) Math.floor(seconds / (60 * 60));
			if (hours == 0) {
				timeString = String.format("%2dm%02ds",
						(seconds % (3600)) / 60, seconds % 60);
			} else {
				timeString = String.format("%dh%02dm%02ds", hours,
						(seconds % (3600)) / 60, seconds % 60);
			}
			if (mTimerText != null) {
				mTimerText.setText(timeString);
			}
		}
	}
}