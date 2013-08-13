package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.CellChange;
import net.cactii.mathdoku.Cheat;
import net.cactii.mathdoku.Cheat.CheatType;
import net.cactii.mathdoku.GameTimer;
import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.GridCage;
import net.cactii.mathdoku.GridCell;
import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.hint.OnTickerTapeChangedListener;
import net.cactii.mathdoku.hint.TickerTape;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.tip.TipCheat;
import net.cactii.mathdoku.tip.TipDialog;
import net.cactii.mathdoku.tip.TipIncorrectValue;
import net.cactii.mathdoku.ui.GridView.InputMode;
import net.cactii.mathdoku.util.Util;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Paul
 * 
 */
public class PuzzleFragment extends android.support.v4.app.Fragment implements
		OnSharedPreferenceChangeListener, OnCreateContextMenuListener,
		OnTickerTapeChangedListener, GridView.OnInputModeChangedListener {
	public final static String TAG = "MathDoku.PuzzleFragment";

	public static final String BUNDLE_KEY_SOLVING_ATTEMPT_ID = "PuzzleFragment.solvingAttemptId";

	// The grid and the view which will display the grid.
	public Grid mGrid;
	public GridView mGridView;

	// A global painter object to paint the grid in different themes.
	public Painter mPainter;

	private GameTimer mTimerTask;

	private RelativeLayout mPuzzleGridLayout;
	private TextView mTimerText;
	private ImageView mInputModeImage;
	private TextView mInputModeText;

	private RelativeLayout mTickerTapeLayout;
	private TickerTape mTickerTape;

	private Button mClearButton;
	private Button mUndoButton;
	private View[] mSoundEffectViews;

	public Preferences mMathDokuPreferences;

	private Context mContext;

	private View mRootView;

	private OnGridFinishedListener mOnGridFinishedListener;

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
		mGridView = (GridView) mRootView.findViewById(R.id.gridView);
		mTimerText = (TextView) mRootView.findViewById(R.id.timerText);

		mClearButton = (Button) mRootView.findViewById(R.id.clearButton);
		mClearButton.setBackgroundColor(mPainter.getButtonBackgroundColor());

		mUndoButton = (Button) mRootView.findViewById(R.id.undoButton);
		mUndoButton.setBackgroundColor(mPainter.getButtonBackgroundColor());

		mTickerTapeLayout = (RelativeLayout) mRootView
				.findViewById(R.id.tickerTapeLayout);

		mSoundEffectViews = new View[] { mGridView, mClearButton, mUndoButton };

		// Hide all controls until sure a grid view can be displayed.
		setNoGridLoaded();

		mGridView.setOnGridTouchListener(mGridView.new OnGridTouchListener() {
			@Override
			public void gridTouched(GridCell cell) {
				setClearAndUndoButtonVisibility(cell);
			}
		});
		mGridView.setOnTickerTapeChangedListener(this);
		mClearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGridView != null) {
					mGridView.digitSelected(0);

					setClearAndUndoButtonVisibility(mGrid.getSelectedCell());
					mGridView.invalidate();
				}
			}
		});
		mUndoButton.setOnClickListener(new OnClickListener() {
			@Override
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

		mGridView.setFocusable(true);
		mGridView.setFocusableInTouchMode(true);

		// Input Mode Image.
		mInputModeImage = (ImageView) mRootView
				.findViewById(R.id.input_mode_image);
		mInputModeText = (TextView) mRootView
				.findViewById(R.id.input_mode_text);
		// On click directly toggle the input mode.
		mInputModeImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mGridView != null && mGrid != null) {
					// Toggle input mode
					mGridView
							.setInputMode(mGridView.getInputMode() == InputMode.NORMAL ? InputMode.MAYBE
									: InputMode.NORMAL);
				}
			}
		});
		// On a long press, toggle the input mode and show a message that mode
		// can also be change with double tap.
		mInputModeImage.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (mGridView != null && mGrid != null) {
					// Toggle input mode
					mGridView
							.setInputMode(mGridView.getInputMode() == InputMode.NORMAL ? InputMode.MAYBE
									: InputMode.NORMAL);

					// Display message
					mInputModeText.setVisibility(View.VISIBLE);
					mInputModeText
							.setText(mGridView.getInputMode() == InputMode.NORMAL ? R.string.input_mode_changed_to_normal
									: R.string.input_mode_changed_to_maybe);
					mInputModeText.invalidate();

					// Hide the message after 3000 milliseconds.
					final Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (mInputModeText != null) {
								mInputModeText.setVisibility(View.GONE);
							}
						}
					}, 3000);

					return true;
				}
				return false;
			}
		});

		mGridView.setOnInputModeChangedListener(this);

		registerForContextMenu(mGridView);

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
		stopTimer();
		if (mGrid != null) {
			mGrid.save();
		}
		if (mTickerTape != null) {
			mTickerTape.cancel();
		}

		super.onPause();
	}

	public void setTheme() {
		mPainter.setTheme(mMathDokuPreferences.getTheme());

		// Invalidate the grid view and the input mode button in order to used
		// the new theme setting
		if (mGridView != null) {
			mGridView.invalidate();
			if (mInputModeImage != null) {
				setInputModeImage(mGridView.getInputMode());
				mInputModeImage.invalidate();
			}
		}
	}

	@Override
	public void onDestroy() {
		mMathDokuPreferences.mSharedPreferences
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
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

		if (mTickerTape != null && mTickerTape.isCancelled()) {
			mTickerTape.show();
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
								PuzzleFragment.this.mGridView.invalidate();
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
				mGrid.setSelectedCell(null);
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
			mGridView.loadNewGrid(grid);

			// Show the grid of the loaded puzzle.
			if (mGrid.isActive()) {
				// Set visibility of grid layout
				if (mPuzzleGridLayout != null) {
					mPuzzleGridLayout.setVisibility(View.VISIBLE);
					mPuzzleGridLayout.invalidate();
				}

				// Set timer
				if (mMathDokuPreferences.isTimerVisible() && mTimerText != null) {
					mTimerText.setVisibility(View.VISIBLE);
					mTimerText.invalidate();
				}
				startTimer();

				setClearAndUndoButtonVisibility((mGrid == null ? null : mGrid
						.getSelectedCell()));

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

		selectedGridCage.revealOperator();

		mGrid.increaseCounter(StatisticsCounterType.ACTION_REVEAL_OPERATOR);
		Cheat cheat = getNewCheat(CheatType.OPERATOR_REVEALED);

		// Display tip
		if (TipCheat.toBeDisplayed(mMathDokuPreferences, cheat)) {
			new TipCheat(mContext, cheat).show();
		}

		mGridView.invalidate();
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
				StatisticsCounterType.ACTION_CHECK_PROGRESS);
		mGrid.getGridStatistics().increaseCounter(
				StatisticsCounterType.CHECK_PROGRESS_INVALIDS_CELLS_FOUND,
				countNewInvalidChoices);

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
								mGrid.setSelectedCell(null);
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
		if (mClearButton != null) {
			mClearButton.setVisibility(View.GONE);
			mClearButton.invalidate();
		}
		if (mUndoButton != null) {
			mUndoButton.setVisibility(View.GONE);
			mUndoButton.invalidate();
		}
	}

	@Override
	public void onTickerTapeChanged(TickerTape tickerTape) {
		// First cancel the old ticker tape
		if (mTickerTape != null) {
			mTickerTape.cancel();
		}
		mTickerTapeLayout.removeAllViews();

		mTickerTape = tickerTape;
		if (mTickerTape != null) {
			mTickerTapeLayout.addView(mTickerTape);
			mTickerTape.show();
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

	/**
	 * Checks whether the normal input mode menu item is visible.
	 * 
	 * @return
	 */
	protected boolean showInputModeNormal() {
		return (mGrid != null && mGrid.isActive() && mGridView != null && mGridView
				.getInputMode() == InputMode.MAYBE);
	}

	/**
	 * Checks whether the maybe input mode menu item is visible.
	 * 
	 * @return
	 */
	protected boolean showInputModeMaybe() {
		return (mGrid != null && mGrid.isActive() && mGridView != null && mGridView
				.getInputMode() == InputMode.NORMAL);
	}

	/**
	 * Set the input mode of the grid view for this puzzle fragment.
	 * 
	 * @param inputMode
	 *            The input mode to be set.
	 */
	protected void setInputMode(InputMode inputMode) {
		if (mGridView != null) {
			mGridView.setInputMode(inputMode);
		}
	}

	@Override
	public void onInputModeChanged(InputMode inputMode) {
		setInputModeImage(inputMode);
	}

	/**
	 * Set the input mode image for the given input mode.
	 * 
	 * @param inputMode
	 *            The input mode for which the input mode button has to be set.
	 */
	private void setInputModeImage(InputMode inputMode) {
		// Set the input mode image to the new value of the input mode
		if (mInputModeImage != null && mPainter != null) {
			mInputModeImage
					.setImageResource((inputMode == InputMode.NORMAL ? mPainter
							.getNormalInputModeButton() : mPainter
							.getMaybeInputModeButton()));
		}
	}
}