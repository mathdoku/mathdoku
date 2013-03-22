package net.cactii.mathdoku;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
import net.cactii.mathdoku.GameFile.GameFileType;
import net.cactii.mathdoku.Painter.GridTheme;
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
import android.widget.CheckBox;
import android.widget.RelativeLayout;
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

	TextView solvedText;
	TextView pressMenu;
	GameTimer mTimerTask;

	RelativeLayout topLayout;
	RelativeLayout puzzleGrid;
	RelativeLayout controls;
	TextView mGameSeedLabel;
	TextView mGameSeedText;
	TextView mTimerText;

	Button digits[] = new Button[9];
	Button clearDigit;
	CheckBox maybeButton;
	TextView mMaybeText;
	Button undoButton;
	View[] sound_effect_views;
	private Animation outAnimation;
	private Animation solvedAnimation;

	public SharedPreferences preferences;

	// Background task for generating a new puzzle
	public GridGenerator mGridGeneratorTask;

	// Variables for process of creating preview images of game file for which
	// no preview image does exist.
	private GameFile mGameFileImagePreviewCreation;
	private ProgressDialog mProgressDialogImagePreviewCreation;

	final Handler mHandler = new Handler();

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
		this.controls = (RelativeLayout) findViewById(R.id.controls);
		this.mGameSeedLabel = (TextView) findViewById(R.id.gameSeedLabel);
		this.mGameSeedText = (TextView) findViewById(R.id.gameSeedText);
		this.mTimerText = (TextView) findViewById(R.id.timerText);
		this.mMaybeText = (TextView) findViewById(R.id.maybeText);
		digits[0] = (Button) findViewById(R.id.digitSelect1);
		digits[1] = (Button) findViewById(R.id.digitSelect2);
		digits[2] = (Button) findViewById(R.id.digitSelect3);
		digits[3] = (Button) findViewById(R.id.digitSelect4);
		digits[4] = (Button) findViewById(R.id.digitSelect5);
		digits[5] = (Button) findViewById(R.id.digitSelect6);
		digits[6] = (Button) findViewById(R.id.digitSelect7);
		digits[7] = (Button) findViewById(R.id.digitSelect8);
		digits[8] = (Button) findViewById(R.id.digitSelect9);
		this.clearDigit = (Button) findViewById(R.id.clearButton);
		this.maybeButton = (CheckBox) findViewById(R.id.maybeButton);
		this.undoButton = (Button) findViewById(R.id.undoButton);

		this.sound_effect_views = new View[] { this.mGridView, this.digits[0],
				this.digits[1], this.digits[2], this.digits[3], this.digits[4],
				this.digits[5], this.digits[6], this.digits[7], this.digits[8],
				this.clearDigit, this.maybeButton, this.undoButton };

		this.mPainter = Painter.getInstance(this);

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
					public void gridTouched(GridCell cell) {
						if (controls.getVisibility() == View.VISIBLE) {
							// digitSelector.setVisibility(View.GONE);
							if (MainActivity.this.preferences.getBoolean(
									"hideselector", false)) {
								controls.startAnimation(outAnimation);
								// cell.mSelected = false;
								mGridView.mSelectorShown = false;
							} else {
								controls.requestFocus();
							}
							mGridView.requestFocus();
						} else {
							if (MainActivity.this.preferences.getBoolean(
									PREF_HIDE_CONTROLS,
									PREF_HIDE_CONTROLS_DEFAULT)) {
								controls.setVisibility(View.VISIBLE);
								Animation animation = AnimationUtils
										.loadAnimation(MainActivity.this,
												R.anim.selectorzoomin);
								controls.startAnimation(animation);
								mGridView.mSelectorShown = true;
							}
							controls.requestFocus();
						}
					}
				});

		for (int i = 0; i < digits.length; i++)
			this.digits[i].setOnClickListener(new OnClickListener() {
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
						PREF_HIDE_CONTROLS, PREF_HIDE_CONTROLS_DEFAULT))
					MainActivity.this.controls.setVisibility(View.GONE);
			}
		});

		this.maybeButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					// Play sound
					v.playSoundEffect(SoundEffectConstants.CLICK);

					// Note: the maybeButton.isChecked holds *old* value
					// until this method is finished...
					boolean maybeIsChecked = !maybeButton.isChecked();

					// Update colors of buttons
					setButtonColor(maybeIsChecked);
				}
				return false;
			}

		});

		checkVersion();

		this.mGridView.setFocusable(true);
		this.mGridView.setFocusableInTouchMode(true);

		registerForContextMenu(this.mGridView);

		// Restore background process if running.
		Object object = this.getLastNonConfigurationInstance();
		if (object != null && object.getClass() == GridGenerator.class) {
			mGridGeneratorTask = (GridGenerator) object;
			mGridGeneratorTask.attachToActivity(this);
		}

		if (!this.preferences.getBoolean(PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
				PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT)) {
			// Process of creating preview images is not yet completed. Last
			// game can not yet be restarted.
			return;
		} else {
			restartLastGame();
			return;
		}
	}

	public void onPause() {
		if (mGrid != null && mGrid.getGridSize() > 3) {
			GameFile saver = new GameFile(GameFileType.LAST_GAME);
			this.mGrid.setElapsedTime((mTimerTask == null ? 0
					: mTimerTask.mElapsedTime));
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
			mMaybeText.setTextColor(0xFF000000);

		} else if (theme.equals(MainActivity.PREF_THEME_DARK)) {
			topLayout.setBackgroundResource(R.drawable.newspaper_dark);
			mPainter.setTheme(GridTheme.DARK);
			pressMenu.setTextColor(0xfff0f0f0);
			pressMenu.setBackgroundColor(0xff000000);
			mTimerText.setTextColor(0xFFF0F0F0);
			mMaybeText.setTextColor(0xFFFFFFFF);
		} else if (theme.equals(MainActivity.PREF_THEME_CARVED)) {
			topLayout.setBackgroundResource(R.drawable.background);
			mPainter.setTheme(GridTheme.CARVED);
			mTimerText.setBackgroundColor(0x10000000);
			mMaybeText.setTextColor(0xFF000000);
		}

		this.mGridView.invalidate();
	}

	public void onResume() {
		if (this.preferences.getBoolean(PREF_WAKE_LOCK, PREF_WAKE_LOCK_DEFAULT)) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		if (mGridGeneratorTask != null) {
			// In case the grid is created in the background ans the dialog is
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

		if (!this.preferences.getBoolean(PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
				PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT)) {
			// It is a bit dirty to abuse the onResume method to start the
			// preview image creation process. But for this process we need this
			// activity to be started as the process will display each saved
			// game in the interface in order to be able to make a preview
			// image.
			createPreviewImages();
		}

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
			if (selectedCell == null) {
				break;
			}
			for (GridCell cell : selectedGridCage.mCells) {
				if (cell.countPossibles() == 1) {
					cell.saveUndoInformation(null);
					cell.setUserValue(cell.getFirstPossible());
				}
			}
			this.mGridView.invalidate();
			break;
		case CONTEXT_MENU_REVEAL_CELL:
			if (selectedCell == null) {
				break;
			}
			selectedCell.saveUndoInformation(null);
			selectedCell.setUserValue(selectedCell.getCorrectValue());
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
			this.mGridView.invalidate();
			this.pressMenu.setVisibility(View.VISIBLE);
			break;
		case CONTEXT_MENU_REVEAL_OPERATOR:
			if (selectedGridCage == null) {
				break;
			}
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
			Intent i = new Intent(this, GameFileList.class);
			startActivityForResult(i, 7);
			return true;
		case R.id.checkprogress:
			int textId;
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
			startActivityForResult(new Intent(MainActivity.this,
					OptionsActivity.class), 0);
			return true;
		case R.id.help:
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
		this.mGridView.digitSelected(value, this.maybeButton.isChecked());

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
					.setPositiveButton(R.string.dialog_hide_operators_positive_button,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									startNewGame(gridSize, true);
								}
							})
					.setNegativeButton(R.string.dialog_hide_operators_negative_button,
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
		mGridGeneratorTask = new GridGenerator(this, gridSize, maxCageSize,
				hideOperators);
		mGridGeneratorTask.execute();
	}

	/**
	 * Reactivate the main ui after a new game is loaded into the grid view by
	 * the ASync GridGenerator task.
	 */
	public void onNewGridReady(final Grid newGrid) {
		// The background task for creating a new grid has been finished. The
		// new grid will always overwrite the current game without any warning.
		mGridGeneratorTask = null;
		setNewGrid(newGrid);
		this.maybeButton.setChecked(false);
	}

	/**
	 * Set the colors of number buttons based on the given status of the maybe
	 * button.
	 * 
	 * @param maybeIsChecked
	 *            True to change the color of the buttons to the same green
	 *            color as the checkmark button. False for black.
	 */
	public void setButtonColor(boolean maybeIsChecked) {
		int color = (maybeIsChecked ? 0xFF15DC23 : 0xFF000000);
		for (int i = 0; i < mGrid.getGridSize(); i++) {
			this.digits[i].setTextColor(color);
		}
	}

	public void setButtonVisibility() {
		int gridSize = mGrid.getGridSize();
		for (int i = 4; i < 9; i++)
			if (i < gridSize)
				this.digits[i].setVisibility(View.VISIBLE);
			else
				this.digits[i].setVisibility(View.GONE);

		this.solvedText.setVisibility(View.GONE);
		this.pressMenu.setVisibility(View.GONE);
		if (this.preferences.getBoolean(PREF_SHOW_TIMER,
				PREF_SHOW_TIMER_DEFAULT)) {
			this.mTimerText.setVisibility(View.VISIBLE);
		}
		if (!MainActivity.this.preferences.getBoolean(PREF_HIDE_CONTROLS,
				PREF_HIDE_CONTROLS_DEFAULT)) {
			this.controls.setVisibility(View.VISIBLE);
		}
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

		TextView tv = (TextView) view.findViewById(R.id.dialog_help_version_body);
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
		// Get revision number
		int revisionNumber = getVersionNumber();

		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.changelogview, null);

		TextView textView = (TextView) view
				.findViewById(R.id.changelog_version_body);
		textView.setText(getVersionName() + " (revision " + getVersionNumber()
				+ ")");

		textView = (TextView) view.findViewById(R.id.changelog_changes_body);
		textView.setText(getResources().getString(
				R.string.changelog_changes_body, revisionNumber));

		textView = (TextView) view.findViewById(R.id.changelog_changes_link);
		textView.setText(PROJECT_HOME + "source/list?num=25&start="
				+ revisionNumber);

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
				.setMessage(
						R.string.dialog_clear_grid_confirmation_message)
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
		int currentVersion = getVersionNumber();
		int previousInstalledVersion = preferences.getInt(PREF_CURRENT_VERSION,
				PREF_CURRENT_VERSION_DEFAULT);
		if (previousInstalledVersion < currentVersion) {
			Editor prefeditor = preferences.edit();

			// On Each update of the game, all game file will be converted to
			// the latest definitions.
			GameFile.ConvertGameFiles(previousInstalledVersion, currentVersion);

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
				if (!preferences.contains(PREF_CREATE_PREVIEW_IMAGES_COMPLETED)) {
					prefeditor.putBoolean(PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
							PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT);
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
					prefeditor.putBoolean(PREF_SHOW_TIMER,
							PREF_SHOW_TIMER_DEFAULT);
				}
				if (!preferences.contains(PREF_THEME)) {
					prefeditor.putString(PREF_THEME, PREF_THEME_DEFAULT);
				}
				if (!preferences.contains(PREF_WAKE_LOCK)) {
					prefeditor.putBoolean(PREF_WAKE_LOCK,
							PREF_WAKE_LOCK_DEFAULT);
				}
			}
			if (previousInstalledVersion < 135 && currentVersion >= 135) {
				if (!preferences.contains(PREF_ALLOW_BIG_CAGES)) {
					prefeditor.putBoolean(PREF_ALLOW_BIG_CAGES,
							PREF_ALLOW_BIG_CAGES_DEFAULT);
				}
			}

			prefeditor.putInt(PREF_CURRENT_VERSION, currentVersion);
			prefeditor.commit();
			if (previousInstalledVersion == -1) {
				// On first install of the game, display the help dialog.
				this.openHelpDialog();
			} else {
				// On upgrade of version show changes.
				this.openChangesDialog();
			}
		}
		return;
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
	 */
	public void createPreviewImages() {
		int countGameFilesWithoutPreview = countGameFilesWithoutPreview();
		if (countGameFilesWithoutPreview == 0) {
			// No games files without previews found.
			Editor prefEditor = preferences.edit();
			prefEditor.putBoolean(PREF_CREATE_PREVIEW_IMAGES_COMPLETED, true);
			prefEditor.commit();

			// Restart the last game.
			restartLastGame();
			return;
		}

		// At least one game file was found for which no preview exist. Show the
		// progress dialog.
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

		// Runnable for handling the next step of preview image
		// creation process which can not be done until the grid view has been
		// validated (refreshed).
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
					// and restarted
					// the dialog can not be dismissed without causing an error.
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

					// Restart the last game
					restartLastGame();

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
		if (mGridGeneratorTask != null) {
			// A new grid is generated in the background. Detach the background
			// task from this activity. It will keep on running until finished.
			mGridGeneratorTask.detachFromActivity();
		}
		return mGridGeneratorTask;
	}

	public void setOnSolvedHandler() {
		this.mGrid.setSolvedHandler(this.mGrid.new OnSolvedListener() {
			@Override
			public void puzzleSolved() {
				MainActivity.this.controls.setVisibility(View.GONE);
				if (mGrid.isActive() && !mGrid.isSolvedByCheating()
						&& mGrid.countMoves() > 0) {
					// Only display animation in case the user has just
					// solved this game. Do not show in case the user
					// cheated by requesting to show the solution or in
					// case an already solved game was reloaded.
					animText(R.string.main_ui_solved_messsage, 0xFF002F00);
				}

				MainActivity.this.pressMenu.setVisibility(View.VISIBLE);

				stopTimer();

				if (MainActivity.this.mTimerText.getVisibility() == View.VISIBLE
						&& mGrid.isSolvedByCheating()) {
					// Hide time in case the puzzle was solved by
					// requesting to show the solution.
					MainActivity.this.mTimerText.setVisibility(View.INVISIBLE);
				}

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
			this.mGrid = grid;
			this.mGridView.loadNewGrid(grid);

			// Show the grid of the loaded puzzle.
			this.puzzleGrid.setVisibility(View.VISIBLE);

			if (this.mGrid.isActive()) {
				GridCell cell = this.mGrid.getSelectedCell();
				if (cell != null && cell.countPossibles() > 1) {
					maybeButton.setChecked(true);
					setButtonColor(true);
				}

				// Set visibility of other controls
				this.setButtonVisibility();
				startTimer();

				// Handler for solved game
				setOnSolvedHandler();
			} else {
				// Set visibility of other controls
				this.pressMenu.setVisibility(View.VISIBLE);
				this.controls.setVisibility(View.GONE);

				stopTimer();
				if (this.mTimerText.getVisibility() == View.VISIBLE
						&& grid.isSolvedByCheating()) {
					// Hide time in case the puzzle was solved by
					// requesting to show the solution.
					this.mTimerText.setVisibility(View.INVISIBLE);
				}
			}

			// Debug information
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				MainActivity.this.mGameSeedLabel.setVisibility(View.VISIBLE);
				MainActivity.this.mGameSeedText.setVisibility(View.VISIBLE);
				MainActivity.this.mGameSeedText.setText(String.format("%,d",
						MainActivity.this.mGrid.getGameSeed()));
			}
		} else {
			// No grid available.
			this.puzzleGrid.setVisibility(View.GONE);
			this.controls.setVisibility(View.GONE);
			this.pressMenu.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Start a new timer (only in case the grid is active).
	 */
	private void startTimer() {
		// Stop old timer
		stopTimer();

		if (mGrid != null && mGrid.isActive()) {
			mTimerTask = new GameTimer();
			mTimerTask.mElapsedTime = mGrid.getElapsedTime();
			mTimerTask.mTimerLabel = mTimerText;
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
}