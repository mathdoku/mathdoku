package net.cactii.mathdoku;

import net.cactii.mathdoku.DevelopmentHelper.Mode;
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

	// Identifiers for preferences
	public final static String PREF_CREATE_PREVIEW_IMAGES_COMPLETED = "CreatePreviewImagesCompleted";
	public final static Boolean PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT = false;
	// TODO: add all other prefs here

	// Identifiers for the context menu
	private final static int CONTEXT_MENU_REVEAL_CELL = 1;
	private final static int CONTEXT_MENU_USE_CAGE_MAYBES = 2;
	private final static int CONTEXT_MENU_REVEAL_OPERATOR = 3;
	private final static int CONTEXT_MENU_CLEAR_CAGE_CELLS = 4;
	private final static int CONTEXT_MENU_CLEAR_GRID = 5;
	private final static int CONTEXT_MENU_SHOW_SOLUTION = 6;

	// The grid and the view which will display the grid.
	public Grid grid;
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

	private GridGenerator mGridGeneratorTask;

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
								// In case the cell holds multiple possible
								// values, the maybe checkbox has to checked as
								// it is more likely that one of possible values
								// has to be removed.
								if (cell.countPossibles() > 1) {
									maybeButton.setChecked(true);
								}

								controls.requestFocus();
							}
							mGridView.requestFocus();
						} else {
							if (MainActivity.this.preferences.getBoolean(
									"hideselector", false)) {
								controls.setVisibility(View.VISIBLE);
								Animation animation = AnimationUtils
										.loadAnimation(MainActivity.this,
												R.anim.selectorzoomin);
								controls.startAnimation(animation);
								mGridView.mSelectorShown = true;
							}
							// maybeButton.setChecked((cell.mPossibles.size() >
							// 0));
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
				if (MainActivity.this.grid.UndoLastMove()) {
					// Succesfull undo
					mGridView.invalidate();
				}

				if (MainActivity.this.preferences.getBoolean("hideselector",
						false))
					MainActivity.this.controls.setVisibility(View.GONE);
			}
		});

		this.maybeButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP)
					v.playSoundEffect(SoundEffectConstants.CLICK);

				GridCell selectedCell = grid.getSelectedCell();
				if (selectedCell != null) {
					// Apply new setting of maybe button on current selected
					// cell.

					// Note: the maybeButton.isChecked holds *old* value
					// until this method is finished...
					boolean maybeIsChecked = !maybeButton.isChecked();

					// Change user value to a possible value in case the maybe
					// button is just checked.
					if (maybeIsChecked && selectedCell.isUserValueSet()) {
						selectedCell.saveUndoInformation(null);
						int curValue = selectedCell.getUserValue();
						selectedCell.clearUserValue();
						selectedCell.togglePossible(curValue);
						mGridView.invalidate();
					}

					// In case the cell contains only one possible value, it
					// will be set as user value as the maybe button is just
					// unchecked.
					if (!maybeIsChecked && selectedCell.countPossibles() == 1) {
						// TODO: move to GridCell and/or Grid
						CellChange originalUserMove = selectedCell
								.saveUndoInformation(null);
						selectedCell.setUserValue(selectedCell
								.getFirstPossible());
						if (MainActivity.this.preferences.getBoolean(
								"redundantPossibles", false)) {
							// Update possible values for other cells in this
							// row and column.
							grid.clearRedundantPossiblesInSameRowOrColumn(originalUserMove);
						}
						mGridView.invalidate();
					}
				}
				return false;
			}

		});

		newVersionCheck();

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
		if (grid.getGridSize() > 3) {
			GameFile saver = new GameFile();
			this.grid.setElapsedTime((mTimerTask == null ? 0
					: mTimerTask.mElapsedTime));
			saver.save(grid, this.mGridView);
		}
		if (mTimerTask != null && !mTimerTask.isCancelled()) {
			mTimerTask.cancel(true);
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

	public void setTheme() {
		pressMenu.setTextColor(0xff000000);
		pressMenu.setBackgroundColor(0xa0f0f0f0);
		String theme = preferences.getString("theme", "newspaper");
		solvedText.setTypeface(mPainter.mGridPainter.mSolvedTypeface);

		if ("newspaper".equals(theme)) {
			topLayout.setBackgroundResource(R.drawable.newspaper);
			mPainter.setTheme(GridTheme.NEWSPAPER);
			mTimerText.setBackgroundColor(0x90808080);
			mMaybeText.setTextColor(0xFF000000);

		} else if ("inverted".equals(theme)) {
			topLayout.setBackgroundResource(R.drawable.newspaper_dark);
			mPainter.setTheme(GridTheme.DARK);
			pressMenu.setTextColor(0xfff0f0f0);
			pressMenu.setBackgroundColor(0xff000000);
			mTimerText.setTextColor(0xFFF0F0F0);
			mMaybeText.setTextColor(0xFFFFFFFF);
		} else if ("carved".equals(theme)) {
			topLayout.setBackgroundResource(R.drawable.background);
			mPainter.setTheme(GridTheme.CARVED);
			mTimerText.setBackgroundColor(0x10000000);
			mMaybeText.setTextColor(0xFF000000);
		}
	}

	public void onResume() {
		if (this.preferences.getBoolean("wakelock", true)) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		setTheme();
		
		this.mGridView.setPreferences(this.preferences);

		this.setSoundEffectsEnabled(this.preferences.getBoolean("soundeffects",
				true));

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
				(grid != null && grid.isActive()));

		// Load/save can only be used in case a grid is displayed (which can be
		// saved) or in case a game file exists which can be loaded.
		menu.findItem(R.id.saveload).setVisible(
				(grid != null && grid.isActive()) || GameFileList.canBeUsed());

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
		if (grid == null || !grid.isActive()) {
			// No context menu in case puzzle isn't active.
			return;
		}

		// Set title
		menu.setHeaderTitle(R.string.application_name);

		// Determine current selected cage.
		GridCage selectedGridCage = grid.getCageForSelectedCell();

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
		for (GridCell cell : selectedGridCage.mCells) {
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
		GridCell selectedCell = grid.getSelectedCell();
		GridCage selectedGridCage = grid.getCageForSelectedCell();

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
			this.grid.Solve();
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
		if (menuId == R.id.size4 || menuId == R.id.size5
				|| menuId == R.id.size6 || menuId == R.id.size7
				|| menuId == R.id.size8 || menuId == R.id.size9) {
			final int gridSize;
			switch (menuId) {
			case R.id.size4:
				gridSize = 4;
				break;
			case R.id.size5:
				gridSize = 5;
				break;
			case R.id.size6:
				gridSize = 6;
				break;
			case R.id.size7:
				gridSize = 7;
				break;
			case R.id.size8:
				gridSize = 8;
				break;
			case R.id.size9:
				gridSize = 9;
				break;
			default:
				gridSize = 4;
				break;
			}
			String hideOperators = this.preferences.getString(
					"hideoperatorsigns", "F");

			if (hideOperators.equals("T")) {
				this.startNewGame(gridSize, true);
				return true;
			}
			if (hideOperators.equals("F")) {
				this.startNewGame(gridSize, false);
				return true;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.hide_operators_dialog_message)
					.setCancelable(false)
					.setPositiveButton(R.string.Yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									startNewGame(gridSize, true);
								}
							})
					.setNegativeButton(R.string.No,
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

		switch (menuItem.getItemId()) {
		case R.id.saveload:
			Intent i = new Intent(this, GameFileList.class);
			startActivityForResult(i, 7);
			return true;
		case R.id.checkprogress:
			int textId;
			if (grid.isSolutionValidSoFar())
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

		if (this.preferences.getBoolean("hideselector", false)) {
			this.controls.setVisibility(View.GONE);
		}
		this.mGridView.requestFocus();
		this.mGridView.mSelectorShown = false;
		this.mGridView.invalidate();
	}

	private void restartLastGame() {
		Grid newGrid = new GameFile().load();
		setNewGrid(newGrid);
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
		if (mTimerTask != null && !mTimerTask.isCancelled()) {
			mTimerTask.cancel(true);
		}

		// Start a background task to generate the new grid. As soon as the new
		// grid is created, the method onNewGridReady will be called.
		mGridGeneratorTask = new GridGenerator(this, gridSize, hideOperators);
		mGridGeneratorTask.execute();
	}

	/**
	 * Reactivate the main ui after a new game is loaded into the grid view by
	 * the ASync GridGenerator task.
	 */
	public void onNewGridReady(Grid grid) {
		mGridGeneratorTask = null;
		setNewGrid(grid);
	}

	public void setButtonVisibility() {
		int gridSize = grid.getGridSize();
		for (int i = 4; i < 9; i++)
			if (i < gridSize)
				this.digits[i].setVisibility(View.VISIBLE);
			else
				this.digits[i].setVisibility(View.GONE);

		this.solvedText.setVisibility(View.GONE);
		this.pressMenu.setVisibility(View.GONE);
		if (this.preferences.getBoolean("timer", true)) {
			this.mTimerText.setVisibility(View.VISIBLE);
		}
		if (!MainActivity.this.preferences.getBoolean("hideselector", false)) {
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
				SCALE_FROM, SCALE_TO, this.mGridView.mCurrentWidth / 2,
				this.mGridView.mCurrentWidth / 2);
		anim.setDuration(1000);
		// animText.setAnimation(anim);
		this.solvedText.startAnimation(this.solvedAnimation);
	}

	private void openHelpDialog() {
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.aboutview, null);
		TextView tv = (TextView) view.findViewById(R.id.aboutVersionCode);
		tv.setText(getVersionName() + " (revision " + getVersionNumber() + ")");
		new AlertDialog.Builder(MainActivity.this)
				.setTitle(
						getResources().getString(R.string.application_name)
								+ " "
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
				.setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	private void openChangesDialog() {
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.changeview, null);
		new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.changelog_title)
				.setIcon(R.drawable.about)
				.setView(view)
				.setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								//
							}
						}).show();
	}

	private void openClearDialog() {
		new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.context_menu_clear_grid_confirmation_title)
				.setMessage(
						R.string.context_menu_clear_grid_confirmation_message)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNegativeButton(
						R.string.context_menu_clear_grid_negative_button_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								//
							}
						})
				.setPositiveButton(
						R.string.context_menu_clear_grid_positive_button_label,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MainActivity.this.grid.clearUserValues();
								MainActivity.this.mGridView.invalidate();
							}
						}).show();
	}

	public void newVersionCheck() {
		int pref_version = preferences.getInt("currentversion", -1);
		Editor prefeditor = preferences.edit();
		int current_version = getVersionNumber();

		if (!preferences.contains(PREF_CREATE_PREVIEW_IMAGES_COMPLETED)) {
			// When upgrading to this version we need to create image previews
			// for saved game files. Insert a new preference which will be used
			// to check if conversion of the previews has already been
			// completed.
			prefeditor.putBoolean(PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
					PREF_CREATE_PREVIEW_IMAGES_COMPLETED_DEFAULT);
			prefeditor.commit();
		}

		if (pref_version == -1 || pref_version != current_version) {
			prefeditor.putInt("currentversion", current_version);
			prefeditor.commit();
			if (pref_version == -1) {
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
						grid = newGrid;
						mGridView.loadNewGrid(grid);
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

					// if (QUICK_CREATE_PUZZLE_WITHOUT_PREVIEW) {
					// Reset theme to default after the last game was
					// restored.
					setTheme();
					// }

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
		this.grid.setSolvedHandler(this.grid.new OnSolvedListener() {
			@Override
			public void puzzleSolved() {
				MainActivity.this.controls.setVisibility(View.GONE);
				if (grid.isActive() && !grid.isSolvedByCheating()
						&& grid.countMoves() > 0) {
					// Only display animation in case the user has just
					// solved this game. Do not show in case the user
					// cheated by requesting to show the solution or in
					// case an already solved game was reloaded.
					animText(R.string.main_ui_solved_messsage, 0xFF002F00);
				}

				MainActivity.this.pressMenu.setVisibility(View.VISIBLE);

				if (MainActivity.this.mTimerTask != null
						&& !MainActivity.this.mTimerTask.isCancelled()) {
					MainActivity.this.mTimerTask.cancel(true);
				}

				if (MainActivity.this.mTimerText.getVisibility() == View.VISIBLE
						&& grid.isSolvedByCheating()) {
					// Hide time in case the puzzle was solved by
					// requesting to show the solution.
					MainActivity.this.mTimerText.setVisibility(View.INVISIBLE);
				}

			}
		});
	}

	public void setNewGrid(Grid grid) {
		if (grid != null) {
			this.grid = grid;
			this.mGridView.loadNewGrid(grid);

			// Show the grid of the loaded puzzle.
			this.puzzleGrid.setVisibility(View.VISIBLE);

			if (this.grid.isActive()) {
				// Set visibility of other controls
				this.setButtonVisibility();

				// Start the timer
				this.mTimerTask = new GameTimer();
				this.mTimerTask.mElapsedTime = grid.getElapsedTime();
				this.mTimerTask.mTimerLabel = mTimerText;
				if (this.preferences.getBoolean("timer", true)) {
					mTimerText.setVisibility(View.VISIBLE);
				}
				this.mTimerTask.execute();

				// Handler for solved game
				setOnSolvedHandler();
			} else {
				// Set visibility of other controls
				this.pressMenu.setVisibility(View.VISIBLE);
				this.controls.setVisibility(View.GONE);

				// Stop timer if running
				if (mTimerTask != null && !mTimerTask.isCancelled()) {
					mTimerTask.cancel(true);
				}
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
						MainActivity.this.grid.getGameSeed()));
			}
		} else {
			// No grid available.
			this.puzzleGrid.setVisibility(View.GONE);
			this.controls.setVisibility(View.GONE);
			this.pressMenu.setVisibility(View.VISIBLE);
		}
	}
}