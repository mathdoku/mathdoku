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

	public GridView kenKenGrid;
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
		this.kenKenGrid = (GridView) findViewById(R.id.gridView);
		this.solvedText = (TextView) findViewById(R.id.solvedText);
		this.kenKenGrid.animText = this.solvedText;
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

		this.sound_effect_views = new View[] { this.kenKenGrid, this.digits[0],
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

		this.kenKenGrid
				.setOnGridTouchListener(this.kenKenGrid.new OnGridTouchListener() {
					@Override
					public void gridTouched(GridCell cell) {
						if (controls.getVisibility() == View.VISIBLE) {
							// digitSelector.setVisibility(View.GONE);
							if (MainActivity.this.preferences.getBoolean(
									"hideselector", false)) {
								controls.startAnimation(outAnimation);
								// cell.mSelected = false;
								kenKenGrid.mSelectorShown = false;
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
							kenKenGrid.requestFocus();
						} else {
							if (MainActivity.this.preferences.getBoolean(
									"hideselector", false)) {
								controls.setVisibility(View.VISIBLE);
								Animation animation = AnimationUtils
										.loadAnimation(MainActivity.this,
												R.anim.selectorzoomin);
								controls.startAnimation(animation);
								kenKenGrid.mSelectorShown = true;
							}
							// maybeButton.setChecked((cell.mPossibles.size() >
							// 0));
							controls.requestFocus();
						}
					}
				});

		this.kenKenGrid
				.setSolvedHandler(this.kenKenGrid.new OnSolvedListener() {
					@Override
					public void puzzleSolved() {
						MainActivity.this.controls.setVisibility(View.GONE);
						if (kenKenGrid.mActive
								&& !kenKenGrid.isSolvedByCheating()
								&& kenKenGrid.countMoves() > 0) {
							// Only display animation in case the user has just
							// solved this game. Do not show in case the user
							// cheated by requesting to show the solution or in
							// case an already solved game was reloaded.
							animText(R.string.main_ui_solved_messsage,
									0xFF002F00);
						}

						MainActivity.this.pressMenu.setVisibility(View.VISIBLE);

						if (MainActivity.this.mTimerTask != null
								&& !MainActivity.this.mTimerTask.isCancelled()) {
							MainActivity.this.mTimerTask.cancel(true);
						}

						if (MainActivity.this.mTimerText.getVisibility() == View.VISIBLE
								&& kenKenGrid.isSolvedByCheating()) {
							// Hide time in case the puzzle was solved by
							// requesting to show the solution.
							MainActivity.this.mTimerText
									.setVisibility(View.INVISIBLE);
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
				MainActivity.this.kenKenGrid.UndoLastMove();
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

				if (kenKenGrid.mSelectedCell != null) {
					// Apply new setting of maybe button on current selected
					// cell.

					// Note: the maybeButton.isChecked holds *old* value
					// until this method is finished...
					boolean maybeIsChecked = !maybeButton.isChecked();

					// Change user value to a possible value in case the maybe
					// button is just checked.
					if (maybeIsChecked
							&& kenKenGrid.mSelectedCell.isUserValueSet()) {
						kenKenGrid.mSelectedCell.saveUndoInformation(null);
						int curValue = kenKenGrid.mSelectedCell.getUserValue();
						kenKenGrid.mSelectedCell.clearUserValue();
						kenKenGrid.mSelectedCell.togglePossible(curValue);
						kenKenGrid.invalidate();
					}

					// In case the cell contains only one possible value, it
					// will be set as user value as the maybe button is just
					// unchecked.
					if (!maybeIsChecked
							&& kenKenGrid.mSelectedCell.countPossibles() == 1) {
						CellChange originalUserMove = kenKenGrid.mSelectedCell
								.saveUndoInformation(null);
						kenKenGrid.mSelectedCell
								.setUserValue(kenKenGrid.mSelectedCell
										.getFirstPossible());
						if (MainActivity.this.preferences.getBoolean(
								"redundantPossibles", false)) {
							// Update possible values for other cells in this
							// row and column.
							kenKenGrid
									.clearRedundantPossiblesInSameRowOrColumn(originalUserMove);
						}
						kenKenGrid.invalidate();
					}
				}
				return false;
			}

		});

		newVersionCheck();

		this.kenKenGrid.setFocusable(true);
		this.kenKenGrid.setFocusableInTouchMode(true);

		registerForContextMenu(this.kenKenGrid);

		// Restore background process if running.
		Object object = this.getLastNonConfigurationInstance();
		if (object != null && object.getClass() == GridGenerator.class) {
			mGridGeneratorTask = (GridGenerator) object;
			mGridGeneratorTask.attachToActivity(this);
		} 
		
		
		if (!this.preferences.getBoolean(
				PREF_CREATE_PREVIEW_IMAGES_COMPLETED,
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
		if (this.kenKenGrid.mGridSize > 3) {
			GameFile saver = new GameFile();
			this.kenKenGrid.mElapsed = (mTimerTask == null ? 0
					: mTimerTask.mElapsedTime);
			saver.save(this.kenKenGrid);
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
		this.kenKenGrid.mDupedigits = this.preferences.getBoolean("dupedigits",
				true);
		this.kenKenGrid.mBadMaths = this.preferences.getBoolean("badmaths",
				true);
		if (this.kenKenGrid.mActive
				&& !MainActivity.this.preferences.getBoolean("hideselector",
						false)) {
			this.controls.setVisibility(View.VISIBLE);
		}
		if (this.kenKenGrid.mActive
				&& (this.mTimerTask == null || this.mTimerTask.isCancelled())) {
			this.mTimerTask = new GameTimer();
			this.mTimerTask.mElapsedTime = this.kenKenGrid.mElapsed;
			this.mTimerTask.mTimerLabel = mTimerText;
			if (this.preferences.getBoolean("timer", true)) {
				mTimerText.setVisibility(View.VISIBLE);
			} else {
				mTimerText.setVisibility(View.GONE);
			}
			this.mTimerTask.execute();
		}
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
		if (mTimerTask != null && !mTimerTask.isCancelled()) {
			mTimerTask.cancel(true);
		}
		Bundle extras = data.getExtras();
		String filename = extras.getString("filename");
		Log.d("Mathdoku", "Loading game: " + filename);
		GameFile saver = new GameFile(filename);
		if (saver.load(this.kenKenGrid)) {
			this.puzzleGrid.setVisibility(View.VISIBLE);
			this.setButtonVisibility(this.kenKenGrid.mGridSize);
			this.kenKenGrid.mActive = true;
			this.mTimerTask = new GameTimer();
			this.mTimerTask.mElapsedTime = this.kenKenGrid.mElapsed;
			this.mTimerTask.mTimerLabel = mTimerText;
			this.mTimerTask.execute();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Disable or enable solution option depending on whether grid is active
		menu.findItem(R.id.checkprogress).setVisible(kenKenGrid.mActive);

		// Load/save can only be used in case a grid is displayed (which can be
		// saved) or in case a game file exists which can be loaded.
		menu.findItem(R.id.saveload).setVisible(
				kenKenGrid.mActive || GameFileList.canBeUsed());

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
		if (!kenKenGrid.mActive)
			return;

		menu.add(3, 105, 0, R.string.context_menu_show_solution);
		menu.add(2, 101, 0, R.string.context_menu_use_cage_maybes);
		menu.setGroupEnabled(2, false);
		menu.add(0, 102, 0, R.string.context_menu_reveal_cell);
		menu.add(1, 103, 0, R.string.context_menu_clear_cage_cells);
		menu.setGroupEnabled(1, false);
		menu.add(0, 104, 0, R.string.context_menu_clear_grid);
		menu.setHeaderTitle(R.string.application_name);

		for (GridCell cell : this.kenKenGrid.mCages
				.get(this.kenKenGrid.mSelectedCell.getCageId()).mCells) {
			if (cell.isUserValueSet() || cell.countPossibles() > 0)
				menu.setGroupEnabled(1, true);
			if (cell.countPossibles() == 1)
				menu.setGroupEnabled(2, true);
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		GridCell selectedCell = this.kenKenGrid.mSelectedCell;
		switch (item.getItemId()) {
		case 103: // Clear cage
			if (selectedCell == null)
				break;
			for (GridCell cell : this.kenKenGrid.mCages.get(selectedCell
					.getCageId()).mCells) {
				cell.saveUndoInformation(null);
				cell.clearUserValue();
			}
			this.kenKenGrid.invalidate();
			break;
		case 101: // Use maybes
			if (selectedCell == null)
				break;
			for (GridCell cell : this.kenKenGrid.mCages.get(selectedCell
					.getCageId()).mCells) {
				if (cell.countPossibles() == 1) {
					cell.saveUndoInformation(null);
					cell.setUserValue(cell.getFirstPossible());
				}
			}
			this.kenKenGrid.invalidate();
			break;
		case 102: // Reveal cell
			if (selectedCell == null)
				break;
			selectedCell.saveUndoInformation(null);
			selectedCell.setUserValue(selectedCell.getCorrectValue());
			selectedCell.setCheated();
			Toast.makeText(this, R.string.main_ui_cheat_messsage,
					Toast.LENGTH_SHORT).show();
			this.kenKenGrid.invalidate();
			break;
		case 104: // Clear grid
			openClearDialog();
			break;
		case 105: // Show solution
			this.kenKenGrid.Solve();
			this.pressMenu.setVisibility(View.VISIBLE);
			break;
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
			if (kenKenGrid.isSolutionValidSoFar())
				textId = R.string.ProgressOK;
			else {
				textId = R.string.ProgressBad;
				kenKenGrid.markInvalidChoices();
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
				&& this.kenKenGrid.mSelectorShown) {
			this.controls.setVisibility(View.GONE);
			this.kenKenGrid.requestFocus();
			this.kenKenGrid.mSelectorShown = false;
			this.kenKenGrid.invalidate();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void digitSelected(int value) {
		if (this.kenKenGrid.mSelectedCell == null) {
			Toast.makeText(getBaseContext(), R.string.select_cell_before_value,
					Toast.LENGTH_SHORT).show();
			return;
		}
		CellChange orginalUserMove = this.kenKenGrid.mSelectedCell
				.saveUndoInformation(null);
		if (value == 0) { // Clear Button
			this.kenKenGrid.mSelectedCell.clearPossibles();
			this.kenKenGrid.mSelectedCell.setUserValue(0);

		} else {
			if (this.maybeButton.isChecked()) {
				if (kenKenGrid.mSelectedCell.isUserValueSet())
					this.kenKenGrid.mSelectedCell.clearUserValue();
				this.kenKenGrid.mSelectedCell.togglePossible(value);
			} else {
				this.kenKenGrid.mSelectedCell.setUserValue(value);
				this.kenKenGrid.mSelectedCell.clearPossibles();
			}

			if (MainActivity.this.preferences.getBoolean("redundantPossibles",
					false)) {
				// Update possible values for other cells in this row and
				// column.
				this.kenKenGrid
						.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
			}
		}
		if (this.preferences.getBoolean("hideselector", false))
			this.controls.setVisibility(View.GONE);
		// this.kenKenGrid.mSelectedCell.mSelected = false;
		this.kenKenGrid.requestFocus();
		this.kenKenGrid.mSelectorShown = false;
		this.kenKenGrid.invalidate();
	}

	private void restartLastGame() {
		GameFile defaultGameFile = new GameFile();
		if (defaultGameFile.load(this.kenKenGrid)) {
			// Game file is loaded into grid.
			this.kenKenGrid.invalidate();

			// Set visibility of controls.
			this.puzzleGrid.setVisibility(View.VISIBLE);
			this.setButtonVisibility(this.kenKenGrid.mGridSize);

			this.kenKenGrid.mActive = true;

			// Start timer
			this.mTimerTask = new GameTimer();
			this.mTimerTask.mElapsedTime = this.kenKenGrid.mElapsed;
			this.mTimerTask.mTimerLabel = mTimerText;
			if (this.preferences.getBoolean("timer", true)) {
				mTimerText.setVisibility(View.VISIBLE);
			}
			this.mTimerTask.execute();

			// Debug information
			if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
				MainActivity.this.mGameSeedLabel.setVisibility(View.VISIBLE);
				MainActivity.this.mGameSeedText.setVisibility(View.VISIBLE);
				MainActivity.this.mGameSeedText.setText(String.format("%,d",
						MainActivity.this.kenKenGrid.getGameSeed()));
			}
		} else {
			// Can not load the last game.
			this.puzzleGrid.setVisibility(View.GONE);
			this.controls.setVisibility(View.GONE);
			this.pressMenu.setVisibility(View.VISIBLE);
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
	public void startNewGame(final int gridSize, final boolean hideOperators) {
		// Cancel old timer if running.
		if (mTimerTask != null && !mTimerTask.isCancelled()) {
			mTimerTask.cancel(true);
		}
		kenKenGrid.mActive = false;

		// Start a background task to generate the new grid. As soon as the new
		// grid is created, the method onNewGridReady will be called.
		mGridGeneratorTask = new GridGenerator(this, gridSize, hideOperators);
		mGridGeneratorTask.execute();
	}

	/**
	 * Replace the current grid with the given grid.
	 * 
	 * @param gridView
	 *            The grid which has to be shown.
	 */
	public void onNewGridReady(GridView gridView) {
		mGridGeneratorTask = null;

		// Get relevant information from the new grid view.
		kenKenGrid.merge(gridView);

		// Set UI components
		puzzleGrid.setVisibility(View.VISIBLE);
		setTheme();
		setButtonVisibility(kenKenGrid.mGridSize);
		maybeButton.setChecked(false);

		// Start new timer
		mTimerTask = new GameTimer();
		mTimerTask.mTimerLabel = mTimerText;
		mTimerTask.execute();

		kenKenGrid.clearUserValues();

		// Show game seed in case running in development mode.
		if (DevelopmentHelper.mode == Mode.DEVELOPMENT) {
			mGameSeedLabel.setVisibility(View.VISIBLE);
			mGameSeedText.setVisibility(View.VISIBLE);
			mGameSeedText
					.setText(String.format("%,d", kenKenGrid.getGameSeed()));
		}
	}

	public void setButtonVisibility(int gridSize) {

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
				SCALE_FROM, SCALE_TO, this.kenKenGrid.mCurrentWidth / 2,
				this.kenKenGrid.mCurrentWidth / 2);
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
								MainActivity.this.kenKenGrid.clearUserValues();
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
		mProgressDialogImagePreviewCreation.setTitle(R.string.main_ui_creating_previews_title);
		mProgressDialogImagePreviewCreation.setMessage(getResources().getString(
				R.string.main_ui_creating_previews_message));
		mProgressDialogImagePreviewCreation.setIcon(android.R.drawable.ic_dialog_info);
		mProgressDialogImagePreviewCreation.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialogImagePreviewCreation.setMax(countGameFilesWithoutPreview);
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
					mGameFileImagePreviewCreation.savePreviewImage(kenKenGrid);
					mProgressDialogImagePreviewCreation.incrementProgressBy(1);
				}

				// Check if a preview for another game file needs to be
				// created.
				mGameFileImagePreviewCreation = getNextGameFileWithoutPreview();
				if (mGameFileImagePreviewCreation != null) {
					// Load the this next game file.
					mGameFileImagePreviewCreation.load(kenKenGrid);
					kenKenGrid.invalidate();

					// Post a message for further processing of the
					// conversion
					// game after the view has been refreshed with the
					// loaded
					// game.
					mHandler.post(this);
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
			// A new grid is generated in the background. Detach the background task from this activity. It will keep on running until finished.
			mGridGeneratorTask.detachFromActivity();
		}
		return mGridGeneratorTask;
	}
}