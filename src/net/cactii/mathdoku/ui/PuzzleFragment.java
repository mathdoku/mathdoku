package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.CellChange;
import net.cactii.mathdoku.Cheat;
import net.cactii.mathdoku.Cheat.CheatType;
import net.cactii.mathdoku.DigitPositionGrid;
import net.cactii.mathdoku.GameTimer;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.GridCage;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PuzzleFragment extends android.support.v4.app.Fragment implements
		OnSharedPreferenceChangeListener, OnCreateContextMenuListener {
	public final static String TAG = "MathDoku.PuzzleFragment";

	public static final String BUNDLE_KEY_SOLVING_ATTEMPT_ID = "PuzzleFragment.solvingAttemptId";

	// The grid and the view which will display the grid.
	public Grid mGrid;
	public GridView mGridView;

	// A global painter object to paint the grid in different themes.
	public Painter mPainter;

	// The input mode in which the puzzle can be.
	public enum InputMode {
		NORMAL, // Digits entered are handled as a new cell value
		MAYBE, // Digits entered are handled to toggle the possible value on/of
	};

	/**
	 * The input mode which is currently active. Access level is not private, to
	 * prevent the an extra access method (see
	 * http://developer.android.com/training
	 * /articles/perf-tips.html#PackageInner).
	 */
	/* package */InputMode mInputMode;

	GameTimer mTimerTask;

	RelativeLayout mTopLayout;
	RelativeLayout mPuzzleGridLayout;
	TableLayout mControls;
	TextView mGameSeedLabel;
	TextView mGameSeedText;
	TextView mTimerText;

	// Digit positions are the places on which the digit buttons can be placed.
	DigitPositionGrid mDigitPositionGrid;

	Button mClearButton;
	Button mUndoButton;
	View[] mSoundEffectViews;

	public Preferences mMathDokuPreferences;

	private Context mContext;

	private View mRootView;

	OnGridFinishedListener mOnGridFinishedListener;

	// Container Activity must implement this interface
	public interface OnGridFinishedListener {
		public void onGridFinishedListener(int solvingAttemptId);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mOnGridFinishedListener = (OnGridFinishedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnGridFinishedListener");
		}
	}

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

		mPainter = Painter.getInstance();
		mMathDokuPreferences = Preferences.getInstance();
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
		this.mControls = (TableLayout) mRootView.findViewById(R.id.controls);
		this.mGameSeedLabel = (TextView) mRootView
				.findViewById(R.id.gameSeedLabel);
		this.mGameSeedText = (TextView) mRootView
				.findViewById(R.id.gameSeedText);
		this.mTimerText = (TextView) mRootView.findViewById(R.id.timerText);

		this.mClearButton = (Button) mRootView.findViewById(R.id.clearButton);
		this.mUndoButton = (Button) mRootView.findViewById(R.id.undoButton);

		this.mSoundEffectViews = new View[] { mGridView, mClearButton,
				mUndoButton };

		// Hide all controls untill sure a grid view can be displayed.
		setNoGridLoaded();

		this.mGridView
				.setOnGridTouchListener(this.mGridView.new OnGridTouchListener() {
					@Override
					public void gridTouched(GridCell cell,
							boolean sameCellSelectedAgain) {
						if (sameCellSelectedAgain) {
							if (TipInputModeChanged
									.toBeDisplayed(mMathDokuPreferences)) {
								new TipInputModeChanged(
										mContext,
										(mInputMode == InputMode.MAYBE ? InputMode.NORMAL
												: InputMode.MAYBE)).show();
							}
							toggleInputMode();
						}

						setClearAndUndoButtonVisibility(cell);
					}
				});

		this.mClearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mGridView != null) {
					mGridView.digitSelected(0);

					setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
					mGridView.invalidate();
				}
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

		// In case a solving attempt id has been passed, this attempt has to be
		// loaded.
		Bundle args = getArguments();
		if (args != null) {
			int solvingAttemptId = args.getInt(BUNDLE_KEY_SOLVING_ATTEMPT_ID);

			mGrid = new Grid();
			if (mGrid.load(solvingAttemptId)) {
				setNewGrid(mGrid);
			}
		}

		return mRootView;
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
		mPainter.setTheme(mMathDokuPreferences.getTheme());

		// Invalidate the grid view in order to used the new theme setting
		if (mGridView != null) {
			mGridView.invalidate();
		}
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

	/**
	 * Checks whether the clear grid menu item is available.
	 * 
	 * @return True in case the clear grid menu item is available. False
	 *         otherwise.
	 */
	protected boolean showClearGrid() {
		return (mGrid != null && mGrid.isActive() && mGrid.isEmpty(true) == false);
	}

	/**
	 * Handles clearing of the entire grid. The grid will only be cleared after
	 * the user has confirmed clearing.
	 */
	protected void clearGrid() {
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

				// Enable the statistics as soon as the first game has been
				// finished.
				if (mMathDokuPreferences.isStatisticsAvailable() == false) {
					mMathDokuPreferences.setStatisticsVisible();
				}
				if (TipStatistics.toBeDisplayed(mMathDokuPreferences)) {
					new TipStatistics(mContext).show();
				}

				// Notify the containing fragment activity about the finishing
				// of the grid. In case the puzzle has been solved manually, a
				// animation is played first.
				if (!mGrid.isActive() || mGrid.isSolvedByCheating()
						|| mGrid.countMoves() == 0) {
					mOnGridFinishedListener.onGridFinishedListener(mGrid
							.getSolvingAttemptId());
				} else {
					// Set input mode to hide controls while playing the
					// animation.
					setInactiveGridLoaded();

					// Set the text view which will be animated
					final TextView textView = (TextView) mRootView
							.findViewById(R.id.solvedText);
					textView.setText(R.string.main_ui_solved_messsage);
					textView.setTextColor(0xFF002F00);
					textView.setTypeface(Painter.getInstance().getTypeface());
					textView.setVisibility(View.VISIBLE);

					// Build the animation
					Animation animation = AnimationUtils.loadAnimation(
							mContext, R.anim.solved);
					animation.setAnimationListener(new AnimationListener() {
						public void onAnimationEnd(Animation animation) {
							textView.setVisibility(View.GONE);
							mOnGridFinishedListener
									.onGridFinishedListener(mGrid
											.getSolvingAttemptId());
						}

						public void onAnimationRepeat(Animation animation) {
						}

						public void onAnimationStart(Animation animation) {
						}
					});

					// Start animation of the text view.
					textView.startAnimation(animation);
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
			mGrid = grid;
			mGridView.loadNewGrid(grid);

			// Show the grid of the loaded puzzle.
			if (mGrid.isActive()) {
				// Determine input mode. The input mode will only be set if it
				// was not yet set before.
				setActiveGridLoaded();
				setClearAndUndoButtonVisibility(mGrid.getSelectedCell());

				startTimer();

				// Handler for solved game
				setOnSolvedHandler();
			} else {
				setInactiveGridLoaded();
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
			setNoGridLoaded();
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
	 * Toggles the input mode to the next available state.
	 */
	public void toggleInputMode() {
		switch (mInputMode) {
		case NORMAL:
			if (TipInputModeChanged.toBeDisplayed(mMathDokuPreferences)) {
				new TipInputModeChanged(mContext, InputMode.MAYBE).show();
			}
			mInputMode = InputMode.MAYBE;
			break;
		case MAYBE:
			if (TipInputModeChanged.toBeDisplayed(mMathDokuPreferences)) {
				new TipInputModeChanged(mContext, InputMode.MAYBE).show();
			}
			mInputMode = InputMode.NORMAL;
			break;
		}
	}

	/**
	 * Sets the timer text with the actual elapsed time.
	 * 
	 * @param elapsedTime
	 *            The elapsed time (in mili seconds) while playing the game.
	 */
	@SuppressLint("DefaultLocale")
	public void setElapsedTime(long elapsedTime) {
		if (mTimerText != null) {
			mTimerText.setText(Util.durationTimeToString(elapsedTime));
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
		}
	}

	/**
	 * Checks whether the reveal cell menu item is available.
	 * 
	 * @return
	 */
	protected boolean showRevealCell() {
		return (mGrid != null && mGrid.isActive() && mGrid.getSelectedCell() != null);
	}

	/**
	 * Handles revealing of user value in the given cell.
	 * 
	 * @param selectedCell
	 *            The cell for which the user value has to be revealed.
	 */
	protected void revealCell() {
		if (mGrid == null) {
			return;
		}

		GridCell selectedCell = mGrid.getSelectedCell();
		if (selectedCell == null) {
			return;
		}

		UsageLog.getInstance().logFunction("ContextMenu.RevealCell");

		// Reveal the user value
		CellChange orginalUserMove = selectedCell.saveUndoInformation(null);
		selectedCell.setUserValue(selectedCell.getCorrectValue());
		if (mMathDokuPreferences.isClearRedundantPossiblesEnabled()) {
			// Update possible values for other cells in this row and
			// column.
			mGrid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
		}
		selectedCell.setCheated();
		setClearAndUndoButtonVisibility(selectedCell);

		mGrid.increaseCounter(StatisticsCounterType.CELLS_REVEALED);
		registerAndProcessCheat(CheatType.CELL_REVEALED);

		this.mGridView.invalidate();
	}

	/**
	 * Checks whether the reveal operator menu item is available.
	 * 
	 * @return True in case the reveal operator menu item is available. False
	 *         otherwise.
	 */
	protected boolean showRevealOperator() {
		if (mGrid == null || mGrid.isActive() == false) {
			return false;
		}

		// Determine current selected cage.
		GridCage selectedGridCage = mGrid.getCageForSelectedCell();
		return (selectedGridCage != null && selectedGridCage.isOperatorHidden());
	}

	/**
	 * Handles revealing of the operator of the given cage.
	 */
	protected void revealOperator() {
		if (mGrid == null || mGrid.isActive() == false) {
			return;
		}

		// Determine current selected cage.
		GridCage selectedGridCage = mGrid.getCageForSelectedCell();

		if (selectedGridCage == null) {
			return;
		}

		UsageLog.getInstance().logFunction("ContextMenu.RevealOperator");
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

	/**
	 * Checks whether the check progress menu item is available.
	 * 
	 * @return
	 */
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
		if (mClearButton != null) {
			mClearButton
					.setVisibility((cell == null || cell.isEmpty()) ? View.INVISIBLE
							: View.VISIBLE);
			mClearButton.invalidate();
		}
		if (mUndoButton != null) {
			mUndoButton
					.setVisibility((mGrid == null || mGrid.countMoves() == 0 || mGrid
							.isActive() == false) ? View.INVISIBLE
							: View.VISIBLE);
			mUndoButton.invalidate();
		}
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

	/**
	 * Checks whether the reveal solution menu item is available.
	 * 
	 * @return True in case the reveal solution menu item is available. False
	 *         otherwise.
	 */
	protected boolean showRevealSolution() {
		return (mGrid != null && mGrid.isActive());
	}

	/**
	 * Handles revealing of the solution of the grid.
	 */
	protected void revealSolution() {
		if (mGrid == null) {
			return;
		}

		new AlertDialog.Builder(this.getActivity())
				.setTitle(R.string.dialog_reveal_solution_confirmation_title)
				.setMessage(
						R.string.dialog_reveal_solution_confirmation_message)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNegativeButton(
						R.string.dialog_reveal_solution_confirmation_negative_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// do nothing
							}
						})
				.setPositiveButton(
						R.string.dialog_reveal_solution_confirmation_positive_button,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mGrid.getGridStatistics().solutionRevealed();
								registerAndProcessCheat(CheatType.SOLUTION_REVEALED);
								mGrid.solve();
							}
						}).show();
	}

	private void setNoGridLoaded() {
		if (mPuzzleGridLayout != null) {
			mPuzzleGridLayout.setVisibility(View.GONE);
		}
		if (mTimerText != null) {
			mTimerText.setVisibility(View.GONE);
		}
		if (mClearButton != null) {
			mClearButton.setVisibility(View.GONE);
		}
		if (mUndoButton != null) {
			mUndoButton.setVisibility(View.GONE);
		}
	}

	private void setInactiveGridLoaded() {
		if (mPuzzleGridLayout != null) {
			mPuzzleGridLayout.setVisibility(View.VISIBLE);
		}
		if (mGrid == null || (mGrid != null && mGrid.isSolvedByCheating())) {
			// Hide time in case the puzzle was solved by
			// requesting to show the solution.
			if (mTimerText != null) {
				mTimerText.setVisibility(View.INVISIBLE);
				mTimerText.invalidate();
			}
		} else {
			// Show time
			if (mTimerText != null) {
				mTimerText.setVisibility(View.VISIBLE);
				mTimerText.invalidate();
				setElapsedTime(mGrid.getElapsedTime());
			}
		}
		if (mClearButton != null) {
			mClearButton.setVisibility(View.GONE);
			mClearButton.invalidate();
		}
		if (mUndoButton != null) {
			mUndoButton.setVisibility(View.GONE);
			mUndoButton.invalidate();
		}
		if (mControls != null) {
			mControls.invalidate();
		}
	}

	private void setActiveGridLoaded() {
		mInputMode = InputMode.NORMAL;
		if (mPuzzleGridLayout != null) {
			mPuzzleGridLayout.setVisibility(View.VISIBLE);
			mPuzzleGridLayout.invalidate();
		}
		if (mMathDokuPreferences.isTimerVisible() && mTimerText != null) {
			mTimerText.setVisibility(View.VISIBLE);
			mTimerText.invalidate();
		}
		setClearAndUndoButtonVisibility((mGrid == null ? null : mGrid
				.getSelectedCell()));
	}
}