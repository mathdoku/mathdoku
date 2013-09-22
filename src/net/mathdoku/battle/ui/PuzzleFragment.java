package net.mathdoku.battle.ui;

import net.mathdoku.battle.Cheat;
import net.mathdoku.battle.Cheat.CheatType;
import net.mathdoku.battle.GameTimer;
import net.mathdoku.battle.Preferences;
import net.mathdoku.battle.R;
import net.mathdoku.battle.grid.CellChange;
import net.mathdoku.battle.grid.DigitPositionGrid;
import net.mathdoku.battle.grid.Grid;
import net.mathdoku.battle.grid.GridCage;
import net.mathdoku.battle.grid.GridCell;
import net.mathdoku.battle.grid.ui.GridInputMode;
import net.mathdoku.battle.grid.ui.GridPlayerView;
import net.mathdoku.battle.hint.TickerTape;
import net.mathdoku.battle.painter.Painter;
import net.mathdoku.battle.statistics.GridStatistics.StatisticsCounterType;
import net.mathdoku.battle.tip.TipCheat;
import net.mathdoku.battle.tip.TipDialog;
import net.mathdoku.battle.tip.TipIncorrectValue;
import net.mathdoku.battle.util.Util;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class PuzzleFragment extends android.support.v4.app.Fragment implements
		OnSharedPreferenceChangeListener, OnCreateContextMenuListener,
		GridPlayerView.OnInputModeChangedListener {
	public final static String TAG = "MathDoku.PuzzleFragment";

	public static final String BUNDLE_KEY_SOLVING_ATTEMPT_ID = "PuzzleFragment.solvingAttemptId";

	// The grid and the view which will display the grid.
	public Grid mGrid;
	public GridPlayerView mGridPlayerView;

	// A global painter object to paint the grid in different themes.
	public Painter mPainter;

	private GameTimer mTimerTask;

	private RelativeLayout mPuzzleGridLayout;
	private TextView mTimerText;
	private ImageView mInputModeImageView;
	private TextView mInputModeText;

	private TickerTape mTickerTape;

	// The digit positions are the places in the table layout on which the digit
	// buttons will be placed.
	private LinearLayout mControlsSwipeOnlyLinearLayout;
	private TableLayout mControlsPadBigTableLayout;
	private TableRow mCcontrolsPadBigTableRow3;
	private final Button mDigitPosition[] = new Button[9];
	private DigitPositionGrid mDigitPositionGrid;

	// Clear buttons
	private Button mClearButton;
	private Button mClearButtonSwipeOnly;

	// Undo buttons
	private Button mUndoButton;
	private Button mUndoButtonSwipeOnly;

	private View[] mSoundEffectViews;

	public Preferences mMathDokuPreferences;

	private Context mContext;

	private View mRootView;

	private OnGridFinishedListener mOnGridFinishedListener;

	private BroadcastReceiver mDreamingBroadcastReceiver;

	// Container Activity must implement these interfaces
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
		mRootView = inflater
				.inflate(R.layout.puzzle_fragment, container, false);

		mContext = getActivity();

		mPainter = Painter.getInstance();
		mMathDokuPreferences = Preferences.getInstance();
		mMathDokuPreferences.mSharedPreferences
				.registerOnSharedPreferenceChangeListener(this);

		mPuzzleGridLayout = (RelativeLayout) mRootView
				.findViewById(R.id.puzzleGrid);
		mGridPlayerView = (GridPlayerView) mRootView
				.findViewById(R.id.grid_player_view);
		mTimerText = (TextView) mRootView.findViewById(R.id.timerText);

		// Get button references from layout
		mControlsSwipeOnlyLinearLayout = (LinearLayout) mRootView
				.findViewById(R.id.controlsSwipeOnly);
		mControlsPadBigTableLayout = (TableLayout) mRootView
				.findViewById(R.id.controlsPadBigTableLayout);
		mCcontrolsPadBigTableRow3 = (TableRow) mRootView
				.findViewById(R.id.controlsButtonRow3);
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

		mClearButton = (Button) mRootView.findViewById(R.id.clearButton);
		mClearButtonSwipeOnly = (Button) mRootView
				.findViewById(R.id.clearButtonSwipeOnly);

		mUndoButton = (Button) mRootView.findViewById(R.id.undoButton);
		mUndoButtonSwipeOnly = (Button) mRootView
				.findViewById(R.id.undoButtonSwipeOnly);

		mTickerTape = (TickerTape) mRootView.findViewById(R.id.tickerTape);
		mGridPlayerView.setTickerTape(mTickerTape);

		mSoundEffectViews = new View[] { mGridPlayerView, mClearButton,
				mClearButtonSwipeOnly, mUndoButton, mUndoButtonSwipeOnly,
				mDigitPosition[0], mDigitPosition[1], mDigitPosition[2],
				mDigitPosition[3], mDigitPosition[4], mDigitPosition[5],
				mDigitPosition[6], mDigitPosition[7], mDigitPosition[8] };

		// Hide all controls until sure a grid view can be displayed.
		setNoGridLoaded();

		mGridPlayerView
				.setOnGridTouchListener(mGridPlayerView.new OnGridTouchListener() {
					@Override
					public void gridTouched(GridCell cell) {
						setClearAndUndoButtonVisibility(cell);
					}
				});

		for (int i = 0; i < mDigitPosition.length; i++) {
			if (mDigitPosition[i] != null) {
				mDigitPosition[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// Convert text of button (number) to Integer
						int d = Integer.parseInt(((Button) v).getText()
								.toString());

						mGridPlayerView.digitSelected(d);

						setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
						mGridPlayerView.requestFocus();
						mGridPlayerView.invalidate();
					}
				});
			}
		}

		// Set same onClickListener for both clear buttons
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGridPlayerView != null) {
					mGridPlayerView.digitSelected(0);

					setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
					mGridPlayerView.invalidate();
				}
			}
		};
		mClearButton.setOnClickListener(onClickListener);
		mClearButtonSwipeOnly.setOnClickListener(onClickListener);

		// Set same onClickListener for both undo buttons
		onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGrid.undoLastMove()) {
					// Successful undo
					setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
					mGridPlayerView.invalidate();

					// Undo can toggle the visibility of the check progress
					// button in the action bar
					((FragmentActivity) mContext).invalidateOptionsMenu();
				}
			}
		};
		mUndoButton.setOnClickListener(onClickListener);
		mUndoButtonSwipeOnly.setOnClickListener(onClickListener);

		mGridPlayerView.setFocusable(true);
		mGridPlayerView.setFocusableInTouchMode(true);

		// Initialize the input mode
		mInputModeImageView = (ImageView) mRootView
				.findViewById(R.id.input_mode_image);
		mInputModeText = (TextView) mRootView
				.findViewById(R.id.input_mode_text);
		setInputModeTextVisibility(mGridPlayerView.getGridInputMode());
		mInputModeText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGridPlayerView != null && mGrid != null) {
					// Toggle input mode
					mGridPlayerView.toggleInputMode();
				}
			}
		});

		mInputModeImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGridPlayerView != null && mGrid != null) {
					// Toggle input mode
					mGridPlayerView.toggleInputMode();
				}
			}
		});
		mGridPlayerView.setOnInputModeChangedListener(this);
		// Force display of normal mode text.
		this.onInputModeChanged(GridInputMode.NORMAL);

		registerForContextMenu(mGridPlayerView);

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

	@Override
	public void onPause() {
		// Store preference counters as they are kept in internal memory for
		// performance reasons.
		if (mMathDokuPreferences != null) {
			mMathDokuPreferences.commitCounters();
		}

		// Unregister the receiver of the day dreaming intent.
		if (mContext != null && mDreamingBroadcastReceiver != null) {
			mContext.unregisterReceiver(mDreamingBroadcastReceiver);
		}

		pause();

		super.onPause();
	}

	public void setTheme() {
		mPainter.setTheme(mMathDokuPreferences.getTheme());

		// Invalidate the grid view and the input mode button in order to used
		// the new theme setting
		if (mGridPlayerView != null) {
			mGridPlayerView.invalidate();
			if (mInputModeImageView != null) {
				setInputModeImage(mGridPlayerView.getGridInputMode());
				mInputModeImageView.invalidate();
			}
		}
	}

	@Override
	public void onDestroy() {
		if (mMathDokuPreferences != null
				&& mMathDokuPreferences.mSharedPreferences != null) {
			mMathDokuPreferences.mSharedPreferences
					.unregisterOnSharedPreferenceChangeListener(this);
		}
		super.onDestroy();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onResume() {
		// Register a broadcast receiver on the intents related to day dreaming.
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// Create broad cast receiver
			if (mDreamingBroadcastReceiver == null) {
				mDreamingBroadcastReceiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if (intent.getAction().equals(
								Intent.ACTION_DREAMING_STARTED)) {
							// Pause the fragment on start of dreaming
							pause();
						} else if (intent.getAction().equals(
								Intent.ACTION_DREAMING_STOPPED)) {
							// Resume the fragment as soon as the dreaming has
							// stopped
							resume();
						}
					}
				};
			}

			// Create intent filter for day dreaming
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_DREAMING_STARTED);
			intentFilter.addAction(Intent.ACTION_DREAMING_STOPPED);

			// Register receiver
			mContext.registerReceiver(mDreamingBroadcastReceiver, intentFilter,
					null, null);
		}

		// Resume the fragment
		resume();

		super.onResume();
	}

	/**
	 * Enables/disables playing a sound effect on touch motions.
	 * 
	 * @param soundEffectEnabled
	 *            True in case sounds are enabled. False otherwise.
	 */
	public void setSoundEffectsEnabled(boolean soundEffectEnabled) {
		for (View view : mSoundEffectViews) {
			view.setSoundEffectsEnabled(soundEffectEnabled);
		}
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
							@Override
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
								PuzzleFragment.this.mGrid.clearCells(false);
								PuzzleFragment.this.mGridPlayerView
										.invalidate();
								// Invalidate the option menu to hide the check
								// progress action if necessary
								((FragmentActivity) mContext)
										.invalidateOptionsMenu();
								setClearAndUndoButtonVisibility(null);
							}
						}).show();
	}

	/**
	 * Sets the handler on the grid which will be called as soon as the grid has
	 * been solved.
	 */
	public void setOnSolvedHandler() {
		if (mGrid == null) {
			return;
		}

		mGrid.setSolvedHandler(mGrid.new OnSolvedListener() {
			@Override
			public void puzzleSolved() {
				// Stop the time and unselect the current cell and cage. Finally
				// save the grid.
				stopTimer();
				mGrid.deselectSelectedCell();
				mGrid.save();

				// Notify the containing fragment activity about the finishing
				// of the grid. In case the puzzle has been solved manually, a
				// animation is played first.
				if (!mGrid.isActive() || mGrid.isSolutionRevealed()
						|| mGrid.countMoves() == 0) {
					mOnGridFinishedListener.onGridFinishedListener(mGrid
							.getSolvingAttemptId());
				} else {
					// Hide controls while showing the animation.
					setInactiveGridLoaded();

					// Set the text view which will be animated
					final TextView textView = (TextView) mRootView
							.findViewById(R.id.solvedText);
					textView.setText(R.string.main_ui_solved_messsage);
					textView.setTextColor(Painter.getInstance()
							.getSolvedTextPainter().getTextColor());
					textView.setBackgroundColor(Painter.getInstance()
							.getSolvedTextPainter().getBackgroundColor());
					textView.setTypeface(Painter.getInstance().getTypeface());
					textView.setVisibility(View.VISIBLE);

					// Build the animation
					Animation animation = AnimationUtils.loadAnimation(
							mContext, R.anim.solved);
					animation.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationEnd(Animation animation) {
							textView.setVisibility(View.GONE);
							mOnGridFinishedListener
									.onGridFinishedListener(mGrid
											.getSolvingAttemptId());
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
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
			mGridPlayerView.loadNewGrid(grid);

			// Show the grid of the loaded puzzle.
			if (mGrid.isActive()) {
				// Set visibility of grid layout
				if (mPuzzleGridLayout != null) {
					mPuzzleGridLayout.setVisibility(View.VISIBLE);
					mPuzzleGridLayout.invalidate();
				}

				setButtonLayout();

				// Set timer
				if (mMathDokuPreferences.isTimerVisible() && mTimerText != null) {
					mTimerText.setVisibility(View.VISIBLE);
					mTimerText.invalidate();
				}
				startTimer();

				setClearAndUndoButtonVisibility((mGrid == null ? null : mGrid
						.getSelectedCell()));
				setDigitPositionGrid();

				// Handler for solved game
				setOnSolvedHandler();
			} else {
				setInactiveGridLoaded();
				stopTimer();
			}

			// Check actionbar and menu options
			((Activity) mContext).invalidateOptionsMenu();

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
				mTimerText.setVisibility(View.INVISIBLE);
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
		if (key.equals(Preferences.PUZZLE_SETTING_THEME)) {
			setTheme();
		}

		if (key.equals(Preferences.PUZZLE_SETTING_INPUT_METHOD)) {
			setButtonLayout();

			// Inform the GridPlayerView about the change of input method.
			if (mGridPlayerView != null) {
				mGridPlayerView
						.setSwipeInputMethodEnabled((mMathDokuPreferences
								.getDigitInputMethod()
								.equals(Preferences.PUZZLE_SETTING_INPUT_METHOD_BUTTONS_ONLY) == false));
			}
		}

		if (key.equals(Preferences.PUZZLE_SETTING_COLORED_DIGITS)) {
			setColorDigitButtons();
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

		// Save old cell info
		CellChange orginalUserMove = selectedCell.saveUndoInformation(null);

		// Reveal the user value
		selectedCell.setRevealed();
		selectedCell.setUserValue(selectedCell.getCorrectValue());
		if (mMathDokuPreferences.isPuzzleSettingClearMaybesEnabled()) {
			// Update possible values for other cells in this row and
			// column.
			mGrid.clearRedundantPossiblesInSameRowOrColumn(orginalUserMove);
		}
		setClearAndUndoButtonVisibility(selectedCell);

		mGrid.increaseCounter(StatisticsCounterType.ACTION_REVEAL_CELL);
		Cheat cheat = getNewCheat(CheatType.CELL_REVEALED);

		// Display tip
		if (TipCheat.toBeDisplayed(mMathDokuPreferences, cheat)) {
			new TipCheat(mContext, cheat).show();
		}

		this.mGridPlayerView.invalidate();
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

		selectedGridCage.revealOperator();

		mGrid.increaseCounter(StatisticsCounterType.ACTION_REVEAL_OPERATOR);
		Cheat cheat = getNewCheat(CheatType.OPERATOR_REVEALED);

		// Display tip
		if (TipCheat.toBeDisplayed(mMathDokuPreferences, cheat)) {
			new TipCheat(mContext, cheat).show();
		}

		mGridPlayerView.invalidate();
	}

	/**
	 * Create a cheat of the given type.
	 * 
	 * @param cheatType
	 *            The type of cheat to be processed.
	 */
	private Cheat getNewCheat(CheatType cheatType) {
		// Create new cheat
		Cheat cheat = new Cheat(mContext, cheatType);

		// Add penalty time
		if (mTimerTask != null) {
			mTimerTask.addCheatPenaltyTime(cheat);
		}

		return cheat;
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
		if (mGrid == null || mGridPlayerView == null) {
			return;
		}

		boolean allUserValuesValid = mGrid.isSolutionValidSoFar();
		int countNewInvalidChoices = (allUserValuesValid ? 0 : mGrid
				.markInvalidChoices());

		if (countNewInvalidChoices > 0) {
			mGridPlayerView.invalidate();
		}

		// Always create a new cheat as the usage of the function (even in case
		// all cells are valid) will result in a cheat penalty being counted.
		Cheat cheat = new Cheat(this.getActivity(),
				CheatType.CHECK_PROGRESS_USED, countNewInvalidChoices);

		// Add penalty time
		if (mTimerTask != null) {
			mTimerTask.addCheatPenaltyTime(cheat);
		}

		// Display tip or toast
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
		// Set the clear button of the visible controls layout
		Button clearButton = (mControlsPadBigTableLayout.getVisibility() == View.VISIBLE ? mClearButton
				: mClearButtonSwipeOnly);
		if (clearButton != null) {
			clearButton
					.setVisibility((cell == null || cell.isEmpty()) ? View.INVISIBLE
							: View.VISIBLE);
			clearButton.invalidate();
		}

		// Set the undo button of the visible controls layout
		Button undoButton = (mControlsPadBigTableLayout.getVisibility() == View.VISIBLE ? mUndoButton
				: mUndoButtonSwipeOnly);
		if (undoButton != null) {
			undoButton
					.setVisibility((mGrid == null || mGrid.countMoves() == 0 || mGrid
							.isActive() == false) ? View.INVISIBLE
							: View.VISIBLE);
			undoButton.invalidate();
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
							@Override
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
								// Disable the solved listener before revealing
								// the solution.
								mGrid.setSolvedHandler(null);

								// Reveal the solution
								mGrid.revealSolution();

								// Create the cheat. This also updates the cheat
								// penalty in the timer.
								Cheat cheat = getNewCheat(CheatType.SOLUTION_REVEALED);

								// Stop the timer and unselect the current cell
								// and cage. Finally save the grid.
								stopTimer();
								mGrid.deselectSelectedCell();
								mGrid.save();

								// Check if tip has to be displayed before
								// informing the listener about finishing the
								// grid.
								if (cheat != null
										&& TipCheat.toBeDisplayed(
												mMathDokuPreferences, cheat)) {
									new TipCheat(mContext, cheat)
											.setOnClickCloseListener(
													new TipDialog.OnClickCloseListener() {
														@Override
														public void onTipDialogClose() {
															// Notify the
															// containing
															// fragment
															// activity
															// about the
															// finishing
															// of the grid.
															// In case the
															// puzzle has
															// been solved
															// manually, a
															// animation is
															// played first.
															if (mGrid != null
																	&& mOnGridFinishedListener != null) {
																mOnGridFinishedListener
																		.onGridFinishedListener(mGrid
																				.getSolvingAttemptId());
															}
														};
													}).show();
								} else {
									if (mGrid != null
											&& mOnGridFinishedListener != null) {
										mOnGridFinishedListener
												.onGridFinishedListener(mGrid
														.getSolvingAttemptId());
									}
								}
							}
						}).show();
	}

	/**
	 * Set visibility of controls for the case no grid could be loaded.
	 */
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
		if (mControlsPadBigTableLayout != null) {
			mControlsPadBigTableLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * Set visibility of controls for an inactive grid.
	 */
	private void setInactiveGridLoaded() {
		if (mPuzzleGridLayout != null) {
			mPuzzleGridLayout.setVisibility(View.VISIBLE);
		}
		if (mGrid == null || (mGrid != null && mGrid.isSolutionRevealed())) {
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
		if (mInputModeImageView != null) {
			mInputModeImageView.setVisibility(View.INVISIBLE);
			mInputModeImageView.invalidate();
		}
		if (mClearButton != null) {
			mClearButton.setVisibility(View.GONE);
			mClearButton.invalidate();
		}
		if (mUndoButton != null) {
			mUndoButton.setVisibility(View.GONE);
			mUndoButton.invalidate();
		}
		if (mControlsPadBigTableLayout != null) {
			mControlsPadBigTableLayout.setVisibility(View.GONE);
			mControlsPadBigTableLayout.invalidate();
		}
	}

	/**
	 * Get the solving attempt id which is being showed in this archive
	 * fragment.
	 * 
	 * @return The solving attempt id which is being showed in this archive
	 *         fragment.
	 */
	public int getSolvingAttemptId() {
		return mGrid.getSolvingAttemptId();
	}

	@Override
	public void onInputModeChanged(final GridInputMode inputMode) {
		setInputModeImage(inputMode);

		// Display message
		mInputModeText.setVisibility(View.VISIBLE);

		boolean showInputModeText = false;
		int inputModeTextResId = 0;
		switch (inputMode) {
		case NORMAL: {
			int counter = mMathDokuPreferences
					.increaseInputModeChangedCounter();
			if (counter < 4) {
				inputModeTextResId = R.string.input_mode_normal_long_description;
				showInputModeText = true;
			} else if (counter < 20) {
				inputModeTextResId = R.string.input_mode_normal_short_description;
				showInputModeText = true;
			}
			break;
		}
		case MAYBE: {
			int counter = mMathDokuPreferences
					.increaseInputModeChangedCounter();
			if (counter < 4) {
				inputModeTextResId = R.string.input_mode_maybe_long_description;
				showInputModeText = true;
			} else if (counter < 20) {
				inputModeTextResId = R.string.input_mode_maybe_short_description;
				showInputModeText = true;
			}
			break;
		}
		case COPY: {
			int counter = mMathDokuPreferences.increaseInputModeCopyCounter();
			if (counter < 4) {
				inputModeTextResId = R.string.input_mode_copy_long_description;
				showInputModeText = true;
			} else if (counter < 20) {
				inputModeTextResId = R.string.input_mode_copy_short_description;
				showInputModeText = true;
			}
			break;
		}

		}
		if (showInputModeText) {
			mInputModeText.setText(inputModeTextResId);
			mInputModeText.invalidate();

			// Hide the message after 5000 milliseconds.
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					setInputModeTextVisibility(inputMode);
				}
			}, 5000);
		}

		// Change color of digit buttons
		setColorDigitButtons();
	}

	/**
	 * Set the visibility of the input mode text to invisible or gone.
	 */
	private void setInputModeTextVisibility(GridInputMode inputMode) {
		if (mInputModeText != null) {
			// After the input mode changed message has been displayed three
			// times, it will never be displayed again. The visibility of the
			// text view should then be set to gone as this decreases the
			// height of the relative layout in which the field is encapsulated.
			if (inputMode == GridInputMode.NORMAL
					|| inputMode == GridInputMode.MAYBE) {
				mInputModeText.setVisibility(mMathDokuPreferences
						.getInputModeChangedCounter() < 4 ? View.INVISIBLE
						: View.GONE);
			} else {
				mInputModeText.setVisibility(mMathDokuPreferences
						.getInputModeCopyCounter() < 4 ? View.INVISIBLE
						: View.GONE);
			}
		}
	}

	/**
	 * Set the input mode image for the given input mode.
	 * 
	 * @param inputMode
	 *            The input mode for which the input mode button has to be set.
	 */
	private void setInputModeImage(GridInputMode inputMode) {
		// Set the input mode image to the new value of the input mode
		if (mInputModeImageView != null && mPainter != null) {
			switch (inputMode) {
			case NORMAL:
				mInputModeImageView.setImageResource(mPainter
						.getNormalInputModeButton());
				break;
			case MAYBE:
				mInputModeImageView.setImageResource(mPainter
						.getMaybeInputModeButton());
				break;
			case COPY:
				mInputModeImageView.setImageResource(mPainter
						.getCopyInputModeButton());
				break;
			}
			mInputModeImageView.invalidate();
		}
	}

	/**
	 * Resumes the fragment.
	 */
	private void resume() {
		setTheme();

		// Propagate current preferences to the grid.
		if (mGrid != null) {
			mGrid.setPreferences();
		}

		// Inform the GridPlayerView about the input method.
		if (mGridPlayerView != null) {
			mGridPlayerView
					.setSwipeInputMethodEnabled((mMathDokuPreferences
							.getDigitInputMethod()
							.equals(Preferences.PUZZLE_SETTING_INPUT_METHOD_BUTTONS_ONLY) == false));
		}

		// Set sound effects if applicable
		setSoundEffectsEnabled(mMathDokuPreferences.isPlaySoundEffectEnabled());

		// Resume the timer
		if (mTimerTask == null
				|| (mTimerTask != null && mTimerTask.isCancelled())) {
			startTimer();
		}

		// Resume the ticker tape if visible
		if (mTickerTape.getVisibility() == View.VISIBLE) {
			if (mTickerTape != null && mTickerTape.isCancelled()) {
				mTickerTape.show();
			}
		}
	}

	/**
	 * Paused the fragment.
	 */
	private void pause() {
		stopTimer();
		if (mGrid != null) {
			mGrid.save();
		}
		if (mTickerTape != null) {
			mTickerTape.cancel();
		}
	}

	/**
	 * Set the digit position grid for layout the digit buttons and maybe
	 * values.
	 * 
	 */
	private void setDigitPositionGrid() {
		// Get grid size
		assert (mGrid != null);
		int gridSize = mGrid.getGridSize();

		// Only create the digit position grid if needed
		if (mDigitPositionGrid == null
				|| !mDigitPositionGrid.isReusable(gridSize)) {
			// Create the mapping for mDigitPosition on the correct button
			// grid layout.
			mDigitPositionGrid = new DigitPositionGrid(gridSize);

			// Propagate setting to the grid view as well for displaying maybe
			// values (dependent on preferences).
			mGridPlayerView.setDigitPositionGrid(mDigitPositionGrid);
		}

		// Use the created mapping to fill all digit positions.
		for (int i = 0; i < mDigitPosition.length; i++) {
			int value = mDigitPositionGrid.getValue(i);
			mDigitPosition[i].setText(value > 0 ? Integer.toString(value) : "");
			mDigitPosition[i]
					.setVisibility(mDigitPositionGrid.getVisibility(i));
		}

		// Visibility of third row of digits buttons
		if (mCcontrolsPadBigTableRow3 != null) {
			mCcontrolsPadBigTableRow3
					.setVisibility(gridSize >= 7 ? View.VISIBLE : View.GONE);
		}
	}

	/**
	 * Set the button layout which has to be shown. Note that the
	 * mControlsPadBigTableLayout and the mControlsSwipeOnlyLinearLayout both
	 * contain a clear and an undo buttons due to different layout restrictions.
	 * So only one of those layouts should be displayed.
	 */
	private void setButtonLayout() {
		// In case the digit buttons are hidden, entering digit can only be done
		// using swiping.
		boolean swipeOnly = mMathDokuPreferences.getDigitInputMethod().equals(
				Preferences.PUZZLE_SETTING_INPUT_METHOD_SWIPE_ONLY);

		if (mControlsPadBigTableLayout != null) {
			mControlsPadBigTableLayout.setVisibility(swipeOnly ? View.GONE
					: View.VISIBLE);
			mControlsPadBigTableLayout.invalidate();
		}
		if (mControlsSwipeOnlyLinearLayout != null) {
			mControlsSwipeOnlyLinearLayout
					.setVisibility(swipeOnly ? View.VISIBLE : View.GONE);
			mControlsSwipeOnlyLinearLayout.invalidate();
		}

		// Reset the visibility of the clear and undo button for the selected
		// cell.
		if (mGrid != null) {
			setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
		}

		setColorDigitButtons();
	}

	/**
	 * Set the text color for all digit buttons.
	 */
	private void setColorDigitButtons() {
		if (mControlsPadBigTableLayout.getVisibility() == View.VISIBLE) {
			// Determine color to use for the digit buttons.
			int color = 0xFFFFFFFF;
			if (mPainter != null && mGridPlayerView != null) {
				if (mMathDokuPreferences.isColoredDigitsVisible()) {
					color = (mGridPlayerView.getGridInputMode() == GridInputMode.NORMAL ? mPainter
							.getHighlightedTextColorNormalInputMode()
							: mPainter.getHighlightedTextColorMaybeInputMode());
				}
			}

			// Change the buttons.
			for (int i = 0; i < mDigitPosition.length; i++) {
				if (mDigitPosition[i] != null) {
					mDigitPosition[i].setTextColor(color);
				}
			}
		}
	}
}