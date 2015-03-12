package net.mathdoku.plus.puzzle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.mathdoku.plus.Preferences.PuzzleSettingInputMethod;
import net.mathdoku.plus.R;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.tickertape.TickerTape;

/**
 * This class implements the swiping functionality on top of the grid base player view. Note the XML
 * layout documents always include this class even in case the user has already chosen to use the
 * buttons input method only. For this reason the input method is checked and if necessary the swipe
 * specific code in this class is skipped and only the super class is called. Most likely this is
 * not the best Java approach but currently I do not know to solve it otherwise except for
 * programmatically build the entire layouts.
 */
public class GridPlayerView extends GridBasePlayerView {
    @SuppressWarnings("unused")
    private static final String TAG = GridPlayerView.class.getName();

    // Reference to the last swipe motion which was started.
    private SwipeMotion mSwipeMotion;

    // Handler and runnable for touch actions which need a delay
    private SwipeBorderDelayRunnable mSwipeBorderDelayRunnable;

    // Reference to the last ticker tape started by the grid view player.
    private TickerTape mTickerTape;

    // The input method(s) to be used. Note that in case the buttons only input
    // method is used, the class is still called but most code will be skipped.
    private boolean mSwipeInputMethodEnabled;

    // Minimum number of valid swipe motion to be made before the ticker tape is
    // disabled
    private final int MIN_VALID_SWIPE_MOTIONS = 15;

    public GridPlayerView(Context context) {
        super(context);
        initGridView();
    }

    public GridPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGridView();
    }

    public GridPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initGridView();
    }

    private void initGridView() {
        mTickerTape = null;

        // Initialize the runnable used to delay touch handling
        mSwipeBorderDelayRunnable = new SwipeBorderDelayRunnable();

        // Determine whether swiping is enabled.
        mSwipeInputMethodEnabled = mPreferences.getDigitInputMethod() != PuzzleSettingInputMethod
                .BUTTONS_ONLY;

        setSwipeBorder(mSwipeInputMethodEnabled);
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        // In case the input method does not allow swiping or in case the copy
        // mode has been enabled, only the super class is called.
        if (!mSwipeInputMethodEnabled || getGridInputMode() == GridInputMode.COPY) {
            return super.onTouch(arg0, event);
        }

        if (mGrid == null || !mGrid.isActive()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                super.onTouch(arg0, event);
                if (mSwipeMotion == null) {
                    mSwipeMotion = new SwipeMotion(this, mBorderWidth, mCellSize);

                    // Set an update listener on the swipe motion until the user has
                    // completed some
                    // swipe motions successfully.
                    if (mPreferences.getSwipeValidMotionCounter() <= MIN_VALID_SWIPE_MOTIONS) {
                        mSwipeMotion.setOnUpdateListener(new SwipeMotion.Listener() {
                            @Override
                            public void onSelectableDigit() {
                                // noinspection ConstantConditions
                                mTickerTape.reset()
                                        .addItem(getResources().getString(
                                                         R.string.hint_swipe_release))
                                        .setEraseConditions(2, 3000)
                                        .show();
                            }

                            @Override
                            public void onNoSelectableDigit() {
                                // noinspection ConstantConditions
                                mTickerTape.reset()
                                        .addItem(getResources().getString(
                                                         R.string.hint_swipe_rotate))
                                        .setEraseConditions(2, 3000)
                                        .show();
                            }
                        });
                    }
                }

                mSwipeMotion.setTouchDownEvent(event);
                if (mSwipeMotion.isTouchDownInsideGrid()) {
                    // Prevent displaying the swipe circle in case the user makes a
                    // very fast swipe motion by delaying the invalidate. Do not
                    // cancel this runnable as it is needed to finish the swipe
                    // motion.
                    mTouchHandler.postDelayed(mSwipeBorderDelayRunnable, 100);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!super.onTouch(arg0, event)) {
                    if (this.mTouchedListener != null && mSwipeMotion != null) {
                        mSwipeMotion.release(event);
                        int swipeDigit = mSwipeMotion.getFocusedDigit();
                        if (swipeDigit >= 1 && swipeDigit <= mGridSize) {
                            // A swipe motion for a valid digit was completed.

                            // Set the swipe digit as selected value for the cell
                            // which
                            // was initially touched.
                            digitSelected(swipeDigit);

                            if (mPreferences.increaseSwipeValidMotionCounter(swipeDigit) <= 6) {
                                // noinspection ConstantConditions
                                mTickerTape.reset()
                                        .addItem(getResources().getString(
                                                         R.string.hint_swipe_valid_digit_completed,
                                                         swipeDigit))
                                        .setEraseConditions(2, 3000)
                                        .show();

                                // Disable listener and ticker tape as soon as
                                // enough valid swipe
                                // motions have been completed.
                                if (mPreferences.getSwipeValidMotionCounter() >=
                                        MIN_VALID_SWIPE_MOTIONS) {
                                    mSwipeMotion.setOnUpdateListener(null);
                                    mTickerTape.disable();
                                }
                            }
                        } else {
                            if (swipeDigit > mGridSize && mPreferences
                                    .increaseSwipeInvalidMotionCounter() <= 6) {
                                // noinspection ConstantConditions
                                mTickerTape.reset()
                                        .addItem(getResources().getString(
                                                         R.string.hint_swipe_invalid_digit_completed))
                                        .setEraseConditions(1, 3000)
                                        .show();
                            }
                        }

                        // Inform listener of puzzle fragment about the release
                        // action
                        mTouchedListener.gridTouched(mGrid.getSelectedCell());

                        // Update to remove the swipe line
                        invalidate();
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                super.onTouch(arg0, event);
                if (mSwipeMotion != null) {
                    // Update current swipe position
                    mSwipeMotion.update(event);

                    // Check if the grid view has to be invalidated
                    int swipeDigit = mSwipeMotion.getFocusedDigit();
                    if (swipeDigit >= 1 && swipeDigit <= 9 && mSwipeMotion.hasChangedDigit()) {
                        // As the swipe digit has been changed, the grid view needs
                        // to be updated.
                        invalidate();
                    } else if (mSwipeMotion.isVisible() && mSwipeMotion
                            .needToUpdateCurrentSwipePosition()) {
                        // For performance reasons, the swipe position will not be
                        // update at each event but only if relevant as decided by
                        // the swipe motion.
                        invalidate();
                    }
                }
                return true;
            default:
                break;
        }

        return super.onTouch(arg0, event);
    }

    /**
     * Class definition of the runnable which implements swipe border delay.
     */
    private class SwipeBorderDelayRunnable implements Runnable {
        @Override
        public void run() {
            // Show the basic swipe hint. Replace this hint after a short pause.
            if (mPreferences.getSwipeValidMotionCounter() < MIN_VALID_SWIPE_MOTIONS) {
                setTickerTapeOnCellDown();
            }

            // Make the swipe border and motion visible at next draw of the grid
            // player view.
            if (mSwipeMotion != null && mSwipeMotion.isTouchDownInsideGrid()) {
                mSwipeMotion.setVisible(true);
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // In case the input method does not allow swiping, only the super class
        // is called.
        if (!mSwipeInputMethodEnabled) {
            super.onDraw(canvas);
            return;
        }

        if (mGrid == null) {
            // As long as no grid has been attached to the grid view, it can not
            // be drawn.
            return;
        }

        synchronized (mGrid.getLock()) {
            onDrawLocked(canvas);

            Cell selectedCell = mGrid.getSelectedCell();
            if (selectedCell == null) {
                // As long a no cell is selected, the input mode border can not
                // be drawn.
                return;
            }

            // Draw the overlay for the swipe border around the selected
            // cell plus the swipe line.
            if (mSwipeMotion != null && !mSwipeMotion.isFinished()) {
                if (mSwipeMotion.isReleased()) {
                    // The swipe motion was released. Now it can be set to
                    // completed as it is confirmed that the overlay border
                    // has been removed by not drawing it.
                    mSwipeMotion.setVisible(false);
                    mSwipeMotion.finish();
                } else if (mSwipeMotion.isVisible()) {
                    GridInputMode gridInputMode = getGridInputMode();
                    if (gridInputMode == GridInputMode.NORMAL || gridInputMode == GridInputMode
                            .MAYBE) {
                        // The overlay needs to be draw as the swipe motion is
                        // not yet released.
                        CellDrawer cellDrawer = getCellDrawer(selectedCell.getRow(),
                                                              selectedCell.getColumn());
                        cellDrawer.drawSwipeOverlay(canvas, mBorderWidth, gridInputMode,
                                                    mSwipeMotion.getCurrentSwipePositionX(),
                                                    mSwipeMotion.getCurrentSwipePositionY(),
                                                    mSwipeMotion.getFocusedDigit(),
                                                    mPreferences.isOuterSwipeCircleVisible(
                                                            mGridSize));
                        mSwipeMotion.setVisible(true);
                    }
                }
            }
        }
    }

    @Override
    public void loadNewGrid(Grid grid) {
        super.loadNewGrid(grid);

        // Reset the swipe motion
        mSwipeMotion = null;

        if (mSwipeInputMethodEnabled) {
            if (mPreferences.getSwipeValidMotionCounter() < MIN_VALID_SWIPE_MOTIONS) {
                // noinspection ConstantConditions,ConstantConditions
                mTickerTape.reset()
                        .addItem(getResources().getString(R.string.hint_swipe_basic))
                        .show();
            }
        }

        invalidate();
    }

    /**
     * Set the ticker tape for situation in which a cell is just touched and the swipe motion has
     * not yet left the cell.
     */
    private void setTickerTapeOnCellDown() {
        // Get information about position of the cell in the grid.
        int gridSize = mGrid.getGridSize();
        Cell selectedCell = mGrid.getSelectedCell();
        boolean isTopRow = selectedCell != null && selectedCell.getRow() == 0;
        boolean isBottomRow = selectedCell != null && selectedCell.getRow() == gridSize - 1;
        boolean isLeftColumn = selectedCell != null && selectedCell.getColumn() == 0;
        boolean isRightColumn = selectedCell != null && selectedCell.getColumn() == gridSize - 1;

        // In case the digit is on an outer row or column of the grid an
        // additional hint has to be shown.
        if (isTopRow || isBottomRow || isLeftColumn || isRightColumn) {
            // List for all digits which can not be shown.
            boolean[] digitNotVisible = {false, false, false, false, false, false, false, false,
                    false, false, false, false};

            // Determine invisible digits in case the selected cell is in the
            // top row.
            if (isTopRow) {
                digitNotVisible[1] = true;
                digitNotVisible[2] = true;
                digitNotVisible[3] = true;
            }

            // Determine invisible digits in case the selected cell is in the
            // left column.
            if (isLeftColumn) {
                digitNotVisible[1] = true;
                digitNotVisible[4] = true;
                if (gridSize >= 7) {
                    digitNotVisible[7] = true;
                }
            }

            // Determine invisible digits in case the selected cell is in the
            // right column.
            if (isRightColumn) {
                digitNotVisible[3] = true;
                if (gridSize >= 6) {
                    digitNotVisible[6] = true;
                    if (gridSize >= 9) {
                        digitNotVisible[9] = true;
                    }
                }
            }

            // Determine invisible digits in case the selected cell is in the
            // bottom row.
            if (isBottomRow && gridSize >= 7) {
                digitNotVisible[7] = true;
                if (gridSize >= 8) {
                    digitNotVisible[8] = true;
                    if (gridSize >= 9) {
                        digitNotVisible[9] = true;
                    }
                }
            }

            // Determine the minimum and maximum value of the invisible digits.
            int minDigitNotVisible = 0;
            int maxDigitNotVisible = 0;
            for (int i = 1; i <= gridSize; i++) {
                if (digitNotVisible[i]) {
                    if (minDigitNotVisible == 0) {
                        minDigitNotVisible = i;
                    }
                    maxDigitNotVisible = i;
                }
            }

            // Determine full text of hint in case an invisible digit has been
            // determined.
            if (minDigitNotVisible > 0) {
                String digits = Integer.toString(minDigitNotVisible);
                for (int i = minDigitNotVisible + 1; i <= maxDigitNotVisible; i++) {
                    if (digitNotVisible[i]) {
                        if (i == maxDigitNotVisible) {
                            digits += " " + getResources().getString(
                                    R.string.connector_last_two_elements,
                                    digits) + " " + Integer.toString(i);
                        } else {
                            digits += ", " + Integer.toString(i);
                        }
                    }
                }
                mTickerTape.reset()
                        .addItem(getResources().getString(R.string.hint_swipe_basic_cell_at_border,
                                                          digits))
                        .show();
            } else {
                mTickerTape.reset()
                        .addItem(getResources().getString(R.string.hint_swipe_outside_cell))
                        .show();
            }
        } else {
            mTickerTape.reset()
                    .addItem(getResources().getString(R.string.hint_swipe_outside_cell))
                    .show();
        }
    }

    /**
     * Toggle the input mode between normal and maybe. Or if currently in another input mode than
     * return to either normal or maybe mode dependent on which of these modes was used last.
     */
    @Override
    public void toggleInputMode() {
        // If currently in copy mode then cancel the swipe motion which is used
        // to display the copy mode status.
        if (getGridInputMode() == GridInputMode.COPY && mSwipeMotion != null && !mSwipeMotion.isReleased() && !mSwipeMotion.isFinished()) {
            mSwipeMotion.release(null);
            invalidate();
        }
        super.toggleInputMode();
    }

    /**
     * Sets the ticker tape in which the grid player view can put messages.
     *
     * @param tickerTape
     *         The ticker tape which can be used.
     */
    public void setTickerTape(TickerTape tickerTape) {
        mTickerTape = tickerTape;
    }

    /**
     * Enables or disables the swipe input method.
     *
     * @param swipeInputMethodEnabled
     *         True in case the swipe input method is enabled. False otherwise.
     */
    public void setSwipeInputMethodEnabled(boolean swipeInputMethodEnabled) {
        mSwipeInputMethodEnabled = swipeInputMethodEnabled;
        setSwipeBorder(mSwipeInputMethodEnabled);
        if (mTickerTape != null && !mSwipeInputMethodEnabled) {
            mTickerTape.disable();
        }
    }
}