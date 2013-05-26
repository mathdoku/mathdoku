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
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.painter.Painter.GridTheme;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import net.cactii.mathdoku.tip.TipCheat;
import net.cactii.mathdoku.tip.TipIncorrectValue;
import net.cactii.mathdoku.tip.TipInputModeChanged;
import net.cactii.mathdoku.tip.TipStatistics;
import net.cactii.mathdoku.ui.GridView.InputModeDeterminer;
import net.cactii.mathdoku.util.UsageLog;
import net.cactii.mathdoku.util.Util;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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

public class PuzzleFragment extends android.support.v4.app.Fragment implements
		OnSharedPreferenceChangeListener, OnCreateContextMenuListener {
	public final static String TAG = "MathDoku.PuzzleFragment";

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

	// Digit positions are the places on which the digit buttons can be placed.
	Button mDigitPosition[] = new Button[9];
	DigitPositionGrid mDigitPositionGrid;

	Button mClearDigit;
	Button mUndoButton;
	View[] mSoundEffectViews;
	private Animation mOutAnimation;
	private Animation mSolvedAnimation;

	public Preferences mMathDokuPreferences;

	private boolean mBlockTouchSameCell = false;

	private Context mContext;

	private View mRootView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.puzzle_activity_fragment,
				container, false);

		mContext = (Context) this.getActivity();

		mPainter = Painter.getInstance(mContext);

		mMathDokuPreferences = Preferences.getInstance(mContext);
		mMathDokuPreferences.mSharedPreferences
				.registerOnSharedPreferenceChangeListener(this);

		this.mTopLayout = (RelativeLayout) mRootView
				.findViewById(R.id.topLayout);
		this.mPuzzleGridLayout = (RelativeLayout) mRootView
				.findViewById(R.id.puzzleGrid);
		(this.mGridView = (GridView) mRootView.findViewById(R.id.gridView)).mInputModeDeterminer = new InputModeDeterminer() {
			@Override
			public final InputMode getInputMode() {
				return mInputMode;
			}
		};
		this.mSolvedText = (TextView) mRootView.findViewById(R.id.solvedText);
		this.mGridView.mAnimationText = this.mSolvedText;
		this.mControls = (TableLayout) mRootView.findViewById(R.id.controls);
		this.mGameSeedLabel = (TextView) mRootView
				.findViewById(R.id.gameSeedLabel);
		this.mGameSeedText = (TextView) mRootView
				.findViewById(R.id.gameSeedText);
		this.mTimerText = (TextView) mRootView.findViewById(R.id.timerText);

		this.mInputModeTextView = (Button) mRootView
				.findViewById(R.id.inputModeText);
		mDigitPosition[0] = (Button) mRootView
				.findViewById(R.id.digitPosition1);
		mDigitPosition[1] = (Button) mRootView
				.findViewById(R.id.digitPosition2);
		mDigitPosition[2] = (Button) mRootView
				.findViewById(R.id.digitPosition3);
		mDigitPosition[3] = (Button) mRootView
				.findViewById(R.id.digitPosition4);
		mDigitPosition[4] = (Button) mRootView
				.findViewById(R.id.digitPosition5);
		mDigitPosition[5] = (Button) mRootView
				.findViewById(R.id.digitPosition6);
		mDigitPosition[6] = (Button) mRootView
				.findViewById(R.id.digitPosition7);
		mDigitPosition[7] = (Button) mRootView
				.findViewById(R.id.digitPosition8);
		mDigitPosition[8] = (Button) mRootView
				.findViewById(R.id.digitPosition9);
		this.mClearDigit = (Button) mRootView.findViewById(R.id.clearButton);
		this.mUndoButton = (Button) mRootView.findViewById(R.id.undoButton);

		this.mSoundEffectViews = new View[] { this.mGridView,
				this.mDigitPosition[0], this.mDigitPosition[1],
				this.mDigitPosition[2], this.mDigitPosition[3],
				this.mDigitPosition[4], this.mDigitPosition[5],
				this.mDigitPosition[6], this.mDigitPosition[7],
				this.mDigitPosition[8], this.mClearDigit,
				this.mInputModeTextView, this.mUndoButton };

		setInputMode(InputMode.NO_INPUT__HIDE_GRID);

		// Animation for a solved puzzle
		mSolvedAnimation = AnimationUtils.loadAnimation(mContext,
				R.anim.solvedanim);
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
		mOutAnimation = AnimationUtils.loadAnimation(mContext,
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
										.loadAnimation(mContext,
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
											mContext,
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
					digitSelected(d);
				}
			});
		this.mClearDigit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				digitSelected(0);
			}
		});
		this.mUndoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mGrid.undoLastMove()) {
					// Successful undo
					setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
					mGridView.invalidate();

					// Undo can toggle the visibility of the check progress
					// button in the action bar
					((FragmentActivity) mContext).invalidateOptionsMenu();
				}

				if (mMathDokuPreferences.isControlsBlockHidden()) {
					mControls.setVisibility(View.GONE);
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
		if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
			this.mGameSeedText.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (mGrid != null) {
							mGrid.getGridGeneratingParameters().show(
									(Activity) mContext);
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
									(Activity) mContext);
						}
					}
					return false;
				}
			});
		}

		this.mGridView.setFocusable(true);
		this.mGridView.setFocusableInTouchMode(true);

		registerForContextMenu(this.mGridView);

		return mRootView;
	}

	@Override
	public void onStart() {
		restartLastGame();
		super.onStart();
	}

	public void onPause() {
		UsageLog.getInstance().close();

		stopTimer();
		if (mGrid != null) {
			mGrid.save();
		}

		super.onPause();
	}

	public void setTheme() {

		mSolvedText.setTypeface(mPainter.getTypeface());

		switch (mMathDokuPreferences.getTheme()) {
		case NEWSPAPER:
			mPainter.setTheme(GridTheme.NEWSPAPER);
			mTimerText.setBackgroundColor(0x90808080);
			break;
		case DARK:
			mPainter.setTheme(GridTheme.DARK);
			mTimerText.setTextColor(0xFFF0F0F0);
			break;
		case CARVED:
			mPainter.setTheme(GridTheme.CARVED);
			mTimerText.setBackgroundColor(0x10000000);
			break;
		}

		this.mGridView.invalidate();
	}

	@Override
	public void onDestroy() {
		mMathDokuPreferences.mSharedPreferences
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onResume() {
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
		if (mGrid.isEmpty(true) == false) {
			// At least one cell within this grid view has a value or a
			// possible value.
			menu.add(0, CONTEXT_MENU_CLEAR_GRID, 0,
					R.string.context_menu_clear_grid);
		}

		// Option: show the solution for this puzzle
		menu.add(3, CONTEXT_MENU_SHOW_SOLUTION, 0,
				R.string.context_menu_show_solution);

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/**
	 * Callback for responding on closing the context menu.
	 * 
	 * @param menu The context menu which was closed.
	 */
	public void onContextMenuClosed(Menu menu) {
		mBlockTouchSameCell = false;
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

	public boolean hideControls() {
		if (mGridView.mSelectorShown) {
			mControls.setVisibility(View.GONE);
			mGridView.requestFocus();
			mGridView.mSelectorShown = false;
			mGridView.invalidate();
			return true;
		}
		return false;
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
	protected void restartLastGame() {
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
							"PuzzleFragmentActivity.restartLastGame can not load grid with id '"
									+ solvingAttemptId + "'.");
				}
			}
		}
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

	private void openClearDialog() {
		new AlertDialog.Builder(this.getActivity())
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
								PuzzleFragment.this.mGrid.clearCells();
								PuzzleFragment.this.mGridView.invalidate();
							}
						}).show();
	}

	public void setOnSolvedHandler() {
		this.mGrid.setSolvedHandler(this.mGrid.new OnSolvedListener() {
			@Override
			public void puzzleSolved() {
				// Stop the time and unselect the current cell and cage. Finally
				// save the grid.
				stopTimer();
				mGrid.setSelectedCell(null);
				mGrid.save();

				setInputMode(InputMode.NO_INPUT__DISPLAY_GRID);
				if (mGrid.isActive() && !mGrid.isSolvedByCheating()
						&& mGrid.countMoves() > 0) {
					// Only display animation in case the user has just
					// solved this game. Do not show in case the user
					// cheated by requesting to show the solution or in
					// case an already solved game was reloaded.
					animText(R.string.main_ui_solved_messsage, 0xFF002F00);
					
					// TODO: The animation is currently NOT SHOWN due to replacing the fragment !!!!!!!!!!!!!!!!!!
				}

				// Enable the statistics as soon as the first game has been
				// finished.
				if (mMathDokuPreferences.isStatisticsAvailable() == false) {
					mMathDokuPreferences.setStatisticsVisible();
				}
				if (TipStatistics.toBeDisplayed(mMathDokuPreferences)) {
					new TipStatistics(mContext).show();
				}

				// Refresh option menu. For example check progress should be
				// hidden.
				((Activity) mContext).invalidateOptionsMenu();

				// Display the statistics fragment
				android.support.v4.app.Fragment fragment = new ArchiveFragment();
				Bundle args = new Bundle();
				args.putInt(ArchiveFragment.BUNDLE_KEY_SOLVING_ATTEMPT_ID,
						mGrid.getSolvingAttemptId());
				fragment.setArguments(args);
				((FragmentActivity) mContext).getSupportFragmentManager()
						.beginTransaction()
						.replace(android.R.id.content, fragment).commit();
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

			// Check actionbar and menu options
			((Activity) mContext).invalidateOptionsMenu();

			// Debug information
			if (DevelopmentHelper.mMode == Mode.DEVELOPMENT) {
				mGameSeedLabel.setVisibility(View.VISIBLE);
				mGameSeedText.setVisibility(View.VISIBLE);
				mGameSeedText
						.setText(String.format("%,d", mGrid.getGameSeed()));
			}

			mRootView.invalidate();
		} else {
			// No grid available.
			setInputMode(InputMode.NO_INPUT__HIDE_GRID);
		}
	}

	/**
	 * Start a new timer (only in case the grid is active).
	 */
	protected void startTimer() {
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

			// Determine the layout to be used for maybe values inside a grid
			if (mGrid != null) {
				setDigitPositionGrid(inputMode);
			}
			break;
		case NORMAL:
		case MAYBE:
			mSolvedText.setVisibility(View.GONE);
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
					.setText(mContext
							.getResources()
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
				new TipInputModeChanged(mContext, InputMode.MAYBE).show();
			}
			inputMode = InputMode.MAYBE;
			break;
		case MAYBE:
			if (TipInputModeChanged.toBeDisplayed(mMathDokuPreferences)) {
				new TipInputModeChanged(mContext, InputMode.MAYBE).show();
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
		if (mContext.getResources().getString(R.string.dimension)
				.equals("small-port")) {
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
			View view = mRootView.findViewById(R.id.digitSelect10);
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
		Cheat cheat = new Cheat(mContext, cheatType);

		// Add penalty time
		if (mTimerTask != null) {
			mTimerTask.addCheatPenaltyTime(cheat);
		}

		// Display hint or toast
		if (TipCheat.toBeDisplayed(mMathDokuPreferences, cheat)) {
			new TipCheat(mContext, cheat).show();
		} else {
			Toast.makeText(mContext, R.string.main_ui_cheat_messsage,
					Toast.LENGTH_SHORT).show();
		}
	}

	protected boolean showCheckProgress() {
		return (mGrid != null && mGrid.isActive() && !mGrid.isEmpty(false));
	}

	/**
	 * Checks the progress of solving the current grid
	 */
	protected void checkProgress() {
		UsageLog.getInstance().logFunction("Menu.CheckProgress");
		if (mGrid == null || mGridView == null) {
			return;
		}

		boolean allUserValuesValid = mGrid.isSolutionValidSoFar();
		int countNewInvalidChoices = (allUserValuesValid ? 0 : mGridView
				.markInvalidChoices());

		// Create new cheat
		Cheat cheat = new Cheat(this.getActivity(),
				CheatType.CHECK_PROGRESS_USED, countNewInvalidChoices);

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
			new TipCheat(mContext, cheat).show();
		} else if (allUserValuesValid) {
			Toast.makeText(mContext, R.string.ProgressOK, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(mContext, R.string.ProgressBad, Toast.LENGTH_SHORT)
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
		((Button) mRootView.findViewById(R.id.clearButton))
				.setVisibility((cell == null || cell.isEmpty()) ? View.INVISIBLE
						: View.VISIBLE);
		((Button) mRootView.findViewById(R.id.undoButton))
				.setVisibility((mGrid == null || mGrid.countMoves() == 0) ? View.INVISIBLE
						: View.VISIBLE);
	}

	public boolean isActive() {
		return (mGrid != null && mGrid.isActive());
	}

	public void prepareLoadNewGame() {
		// Cancel old timer if running.
		stopTimer();

		// Save the game.
		if (mGrid != null) {
			mGrid.save();
		}
	}
}