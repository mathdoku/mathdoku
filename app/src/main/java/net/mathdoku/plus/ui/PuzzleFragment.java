package net.mathdoku.plus.ui;

import net.mathdoku.plus.Cheat;
import net.mathdoku.plus.Cheat.CheatType;
import net.mathdoku.plus.GameTimer;
import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.Preferences.PuzzleSettingInputMethod;
import net.mathdoku.plus.R;
import net.mathdoku.plus.grid.DigitPositionGrid;
import net.mathdoku.plus.grid.Grid;
import net.mathdoku.plus.grid.GridCage;
import net.mathdoku.plus.grid.GridCell;
import net.mathdoku.plus.grid.GridLoader;
import net.mathdoku.plus.grid.ui.GridInputMode;
import net.mathdoku.plus.grid.ui.GridPlayerView;
import net.mathdoku.plus.hint.TickerTape;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.painter.Painter.GridTheme;
import net.mathdoku.plus.tip.TipCheat;
import net.mathdoku.plus.tip.TipDialog;
import net.mathdoku.plus.tip.TipIncorrectValue;
import net.mathdoku.plus.util.FeedbackEmail;
import net.mathdoku.plus.util.Util;
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
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
	private Grid mGrid;
	private GridPlayerView mGridPlayerView;

	// A global painter object to paint the grid in different themes.
	private Painter mPainter;

	private GameTimer mTimerTask;

	private RelativeLayout mPuzzleGridLayout;
	private TextView mTimerText;

	private TickerTape mTickerTape;

	// The digit positions are the places in the table layout on which the digit
	// buttons will be placed.
	private LinearLayout mControlsSwipeOnlyLinearLayout;
	private TableLayout mControlsPadBigTableLayout;
	private TableRow mControlsPadBigTableRow3;
	private final Button mDigitPosition[] = new Button[9];
	private DigitPositionGrid mDigitPositionGrid;

	// Clear buttons
	private Button mClearButton;
	private Button mClearButtonSwipeOnly;

	// Undo buttons
	private Button mUndoButton;
	private Button mUndoButtonSwipeOnly;

	private View[] mSoundEffectViews;

	private Preferences mMathDokuPreferences;

	private Context mContext;

	private View mRootView;

	private BroadcastReceiver mDreamingBroadcastReceiver;

	// Contextual action bar for the copy cell values input mode
	private ActionMode mActionMode;

	// Container Activity must implement these interfaces
	private PuzzleFragmentListener mPuzzleFragmentListener;

	public interface PuzzleFragmentListener {
		/**
		 * Callback to inform listener when the puzzle is finished.
		 * 
		 * @param grid
		 *            The grid which has been finished.
		 */
		public void onPuzzleFinishedListener(Grid grid);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mPuzzleFragmentListener = (PuzzleFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement PuzzleFragmentListener");
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
		mControlsPadBigTableRow3 = (TableRow) mRootView
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

		// Listener for swipe motions on the grid player view.
		mGridPlayerView
				.setOnGridTouchListener(mGridPlayerView.new OnGridTouchListener() {
					@Override
					public void gridTouched(GridCell cell) {
						setClearAndUndoButtonVisibility(cell);

						// Invalidate the option menu as the copy cell value
						// action item may need a change.
						((FragmentActivity) mContext).invalidateOptionsMenu();
					}
				});

		for (Button digitPosition : mDigitPosition) {
			if (digitPosition != null) {
				digitPosition.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// Convert text of button (number) to Integer
						@SuppressWarnings("ConstantConditions")
						int d = Integer.parseInt(((Button) v).getText()
								.toString());

						mGridPlayerView.digitSelected(d);

						setClearAndUndoButtonVisibility(mGrid.getSelectedCell());

						// Invalidate the option menu as the copy cell value
						// action item may need a change.
						((FragmentActivity) mContext).invalidateOptionsMenu();

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

					// Invalidate the option menu as the copy cell value action
					// item may need a change.
					((FragmentActivity) mContext).invalidateOptionsMenu();

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

		// Register listener for changes of input mode
		mGridPlayerView.setOnInputModeChangedListener(this);

		registerForContextMenu(mGridPlayerView);

		// The contextual action bar will not be defined before the copy mode is
		// entered.
		mActionMode = null;

		// In case a solving attempt id has been passed, this attempt has to be
		// loaded.
		Bundle args = getArguments();
		if (args != null) {
			int solvingAttemptId = args.getInt(BUNDLE_KEY_SOLVING_ATTEMPT_ID);

			mGrid = new GridLoader().load(solvingAttemptId);
			if (mGrid != null) {
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

	private void setTheme() {
		mPainter.setTheme(mMathDokuPreferences.getTheme());

		// Invalidate the grid view and the input mode button in order to used
		// the new theme setting
		if (mGridPlayerView != null) {
			mGridPlayerView.invalidate();
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
						String action = intent.getAction();
						if (action != null) {
							if (action.equals(Intent.ACTION_DREAMING_STARTED)) {
								// Pause the fragment on start of dreaming
								pause();
							} else if (action
									.equals(Intent.ACTION_DREAMING_STOPPED)) {
								// Resume the fragment as soon as the dreaming
								// has stopped
								resume();
							}
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
	private void setSoundEffectsEnabled(boolean soundEffectEnabled) {
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
	boolean showClearGrid() {
		return (mGrid != null && mGrid.isActive() && mGrid.isEmpty() == false);
	}

	/**
	 * Displays the dialog which ask confirmation whether the current grid
	 * should be cleared.
	 */
	void openClearGridDialog() {
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
	private void setOnSolvedHandler() {
		if (mGrid == null) {
			return;
		}

		mGrid.setSolvedHandler(mGrid.new OnSolvedListener() {
			@Override
			public void puzzleSolved() {
				// Stop the time and deselect the current cell and cage. Finally
				// save the grid.
				stopTimer();
				mGrid.deselectSelectedCell();
				mGrid.setActive(false);
				mGrid.save();

				// Notify the containing fragment activity about the finishing
				// of the grid. In case the puzzle has been solved manually, a
				// animation is played first.
				if (!mGrid.isActive() || mGrid.isSolutionRevealed()
						|| mGrid.countMoves() == 0) {
					mPuzzleFragmentListener.onPuzzleFinishedListener(mGrid);
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
					if (animation != null) {
						animation.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								textView.setVisibility(View.GONE);
								mPuzzleFragmentListener
										.onPuzzleFinishedListener(mGrid);
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

			}
		});
	}

	/**
	 * Loads a solving attempt into the fragment. Can only be called if the
	 * fragment view were already created.
	 * 
	 * @param solvingAttemptId
	 *            The solvingAttemptId which has to be loaded.
	 */
	public void loadSolvingAttempt(int solvingAttemptId) {
		Grid grid = new GridLoader().load(solvingAttemptId);
		if (grid != null) {
			setNewGrid(grid);
		}
	}

	/**
	 * Load the new grid and set control visibility.
	 * 
	 * @param grid
	 *            The grid to display.
	 */
	private void setNewGrid(Grid grid) {
		if (grid != null) {
			mGrid = grid;
			mGridPlayerView.loadNewGrid(grid);

			// Show the grid of the loaded puzzle.
			if (mGrid.isActive()) {
				// Get the input mode which was last used (possibly in another
				// game).

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

				// Restore the input mode which was used in the last puzzle.
				boolean copyModeEnabled = mMathDokuPreferences
						.isGridInputModeCopyEnabled();
				mGridPlayerView.setGridInputMode(
						mMathDokuPreferences.getGridInputMode(), false);
				if (copyModeEnabled) {
					copyCellValues();
				}
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
	void startTimer() {
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
	 *            The elapsed time (in milliseconds) while playing the game.
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

			// Invalidate the option menu as the input mode action icon needs to
			// be updated
			((FragmentActivity) mContext).invalidateOptionsMenu();
		}

		if (key.equals(Preferences.PUZZLE_SETTING_INPUT_METHOD)) {
			setButtonLayout();

			// Inform the GridPlayerView about the change of input method.
			if (mGridPlayerView != null) {
				mGridPlayerView
						.setSwipeInputMethodEnabled((mMathDokuPreferences
								.getDigitInputMethod() != PuzzleSettingInputMethod.BUTTONS_ONLY));
			}
		}

		if (key.equals(Preferences.PUZZLE_SETTING_COLORED_DIGITS)) {
			setColorDigitButtons();

			// Invalidate the option menu as the input mode action icon needs to
			// be updated
			((FragmentActivity) mContext).invalidateOptionsMenu();
		}
	}

	/**
	 * Checks whether the reveal cell menu item is available.
	 * 
	 * @return True in case the selected cell can be revealed. False otherwise.
	 */
	boolean showRevealCell() {
		return (mGrid != null && mGrid.isActive() && mGrid.getSelectedCell() != null);
	}

	/**
	 * Reveals the values in the selected cell.
	 */
	void revealCell() {
		if (mGrid != null && mGrid.revealSelectedCell()) {
			GridCell selectedCell = mGrid.getSelectedCell();
			setClearAndUndoButtonVisibility(selectedCell);

			Cheat cheat = registerNewCheat(CheatType.CELL_REVEALED);

			if (TipCheat.toBeDisplayed(mMathDokuPreferences, cheat)) {
				new TipCheat(mContext, cheat).show();
			}

			mGridPlayerView.invalidate();
		}
	}

	/**
	 * Checks whether the reveal operator menu item is available.
	 * 
	 * @return True in case the reveal operator menu item is available. False
	 *         otherwise.
	 */
	boolean showRevealOperator() {
		if (mGrid == null || mGrid.isActive() == false) {
			return false;
		}

		// Determine current selected cage.
		GridCage selectedGridCage = mGrid.getSelectedCage();
		return (selectedGridCage != null && selectedGridCage.isOperatorHidden());
	}

	/**
	 * Handles revealing of the operator of the given cage.
	 */
	void revealOperator() {
		if (mGrid != null && mGrid.revealOperatorSelectedCage()) {
			Cheat cheat = registerNewCheat(CheatType.OPERATOR_REVEALED);

			// Display tip
			if (TipCheat.toBeDisplayed(mMathDokuPreferences, cheat)) {
				new TipCheat(mContext, cheat).show();
			}

			mGridPlayerView.invalidate();
		}
	}

	/**
	 * Register a new cheat of the given type. The penalty time of the cheat is
	 * added to the timer.
	 * 
	 * @param cheatType
	 *            The type of cheat to be processed.
	 */
	private Cheat registerNewCheat(CheatType cheatType) {
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
	 * @return True if check progress can be used on the grid. False otherwise.
	 */
	boolean showCheckProgress() {
		return (mGrid != null && mGrid.isActive() && mGrid
				.containsNoUserValues());
	}

	/**
	 * Checks the progress of solving the current grid
	 */
	void checkProgress() {
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
		// Set the clear button of the visible controls layout. The clear
		// buttons is always visible but is disabled in case the cell is empty.
		Button clearButton = (mControlsPadBigTableLayout.getVisibility() == View.VISIBLE ? mClearButton
				: mClearButtonSwipeOnly);
		if (clearButton != null) {
			clearButton.setVisibility(View.VISIBLE);
			clearButton.setEnabled((cell != null && cell.isEmpty() == false));
			clearButton.invalidate();
		}

		// Set the undo button of the visible controls layout. The button is
		// only visible when appropriate (e.g. as long as the user has not made
		// any moves or did not undo all moves).
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
	boolean showRevealSolution() {
		return (mGrid != null && mGrid.isActive());
	}

	/**
	 * Handles revealing of the solution of the grid.
	 */
	void revealSolution() {
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
								Cheat cheat = registerNewCheat(CheatType.SOLUTION_REVEALED);

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
																	&& mPuzzleFragmentListener != null) {
																mPuzzleFragmentListener
																		.onPuzzleFinishedListener(mGrid);
															}
														}
													}).show();
								} else {
									if (mGrid != null
											&& mPuzzleFragmentListener != null) {
										mPuzzleFragmentListener
												.onPuzzleFinishedListener(mGrid);
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
		if (mGrid == null || mGrid.isSolutionRevealed()) {
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
	public void onInputModeChanged(final GridInputMode inputMode,
			boolean enableCopyMode) {
		// Store the current input mode in the preferences.
		mMathDokuPreferences.setGridInputMode(enableCopyMode, inputMode);

		// Change color of digit buttons
		setColorDigitButtons();

		// Invalidate the option menu to toggle the input mode menu action
		((FragmentActivity) mContext).invalidateOptionsMenu();
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
							.getDigitInputMethod() != PuzzleSettingInputMethod.BUTTONS_ONLY));
		}

		// Set sound effects if applicable
		setSoundEffectsEnabled(mMathDokuPreferences.isPlaySoundEffectEnabled());

		// Resume the timer
		if (mTimerTask == null || mTimerTask.isCancelled()) {
			startTimer();
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
	 */
	private void setDigitPositionGrid() {
		// Get grid size
		assert (mGrid != null);
		int gridSize = mGrid.getGridSize();

		// Only create the digit position grid if needed
		if (mDigitPositionGrid == null
				|| mDigitPositionGrid.isNotReusable(gridSize)) {
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
			// noinspection MagicConstant
			mDigitPosition[i]
					.setVisibility(mDigitPositionGrid.getVisibility(i));
		}

		// Visibility of third row of digits buttons
		if (mControlsPadBigTableRow3 != null) {
			mControlsPadBigTableRow3.setVisibility(gridSize >= 7 ? View.VISIBLE
					: View.GONE);
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
		boolean swipeOnly = (mMathDokuPreferences.getDigitInputMethod() == PuzzleSettingInputMethod.SWIPE_ONLY);

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
			for (Button digitPosition : mDigitPosition) {
				if (digitPosition != null) {
					digitPosition.setTextColor(color);
				}
			}
		}
	}

	/**
	 * Gets the resource id for the icon which matches the current input mode.
	 * 
	 * @return The resource id for the icon which matches the current input
	 *         mode.
	 */
	public int getActionCurrentInputModeIconResId() {
		if (mGridPlayerView == null) {
			return R.drawable.normal_input_mode_colored;
		}

		// Determine which button should be displayed.
		int index = (mGridPlayerView.getGridInputMode() == GridInputMode.NORMAL ? 0
				: 4)
				+ (mMathDokuPreferences.isColoredDigitsVisible() ? 0 : 2)
				+ (mMathDokuPreferences.getTheme() == GridTheme.LIGHT ? 0 : 1);
		switch (index) {
		case 0:
		case 1:
			return R.drawable.normal_input_mode_colored;
		case 2:
			return R.drawable.normal_input_mode_monochrome_light;
		case 3:
			return R.drawable.normal_input_mode_monochrome_dark;
		case 4:
		case 5:
			return R.drawable.maybe_input_mode_colored;
		case 6:
			return R.drawable.maybe_input_mode_monochrome_light;
		case 7:
			return R.drawable.maybe_input_mode_monochrome_dark;
		}

		// Impossible
		return -1;
	}

	/**
	 * Gets the resource id of the title of the current input mode.
	 * 
	 * @return The resource id of the title of the current input mode.
	 */
	public int getActionCurrentInputModeTitleResId() {
		if (mGridPlayerView == null) {
			return R.string.input_mode_normal_long_description;
		}

		return (mGridPlayerView.getGridInputMode() == GridInputMode.NORMAL ? R.string.input_mode_normal_long_description
				: R.string.input_mode_maybe_long_description);
	}

	/**
	 * Toggles the input mode from normal to maybe and vice versa.
	 */
	public void toggleInputMode() {
		if (mGridPlayerView != null) {
			// Toggle input mode
			mGridPlayerView.toggleInputMode();
		}
	}

	/**
	 * Checks whether the copy cell values menu item is available.
	 * 
	 * @return True in case the copy cell values menu item is available. False
	 *         otherwise.
	 */
	boolean showCopyCellValues() {
		if (mGrid == null || mGrid.isActive() == false
				|| mGridPlayerView.getGridInputMode() == GridInputMode.COPY) {
			return false;
		}

		GridCell selectedCell = mGrid.getSelectedCell();
		return !(selectedCell == null || selectedCell.isEmpty());

	}

	/**
	 * Checks the progress of solving the current grid
	 */
	void copyCellValues() {
		if (mGrid == null || mGridPlayerView == null) {
			return;
		}

		// TODO: enter a visual different activity.
		// -- other action bar
		// -- no digit buttons
		// -- no clear button
		// -- undo should still work

		// Start the context action bar. Note: this contextual action bar does
		// not contain any action items. It merely serves as a visual indicator
		// that copy mode is enabled until the done button is clicked which
		// closes the copy mode.
		if (mActionMode == null) {
			mActionMode = getActivity().startActionMode(
					new ActionMode.Callback() {
						/**
						 * Start the contextual action bar when the copy mode is
						 * enabled.
						 */
						@Override
						public boolean onCreateActionMode(ActionMode mode,
								Menu menu) {
							// Set grid player view into copy mode.
							mGridPlayerView.setCopyModeEnabled(true);

							// Hide all buttons except undo
							for (Button digitPosition : mDigitPosition) {
								if (digitPosition != null) {
									digitPosition.setVisibility(View.INVISIBLE);
								}
							}
							if (mClearButton != null) {
								mClearButton.setVisibility(View.INVISIBLE);
							}

							// Inflate a menu resource providing context menu
							// items
							mode.setTitle(R.string.tap_to_copy_cell_values);
							MenuInflater inflater = mode.getMenuInflater();
							if (inflater != null) {
								inflater.inflate(
										R.menu.copy_cell_context_action_menu,
										menu);
							}
							return true;
						}

						@Override
						public boolean onPrepareActionMode(ActionMode mode,
								Menu menu) {
							return false; // Return false if nothing is done
						}

						// Called when the user selects a contextual menu item
						@Override
						public boolean onActionItemClicked(ActionMode mode,
								MenuItem item) {
							switch (item.getItemId()) {
							case R.id.action_send_feedback:
								new FeedbackEmail(getActivity()).show();
								return true;
							default:
								return false;
							}
						}

						// Called when the user exits the action mode
						@Override
						public void onDestroyActionMode(ActionMode mode) {
							mActionMode = null;

							// Restore grid player to previous input mode.
							mGridPlayerView.setCopyModeEnabled(false);

							// Restore buttons
							setDigitPositionGrid();
							setClearAndUndoButtonVisibility(mGrid
									.getSelectedCell());
						}
					});
		}
	}
}
