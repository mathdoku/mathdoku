package net.mathdoku.plus.tickertape;

import android.os.Handler;
import android.view.View;

// The runnable which will update the ticker tape at regular intervals
class TickerTapeUpdater implements Runnable {
    private static final int DELAY_MILLIS = 400;
    private TickerTape tickerTape;
    private Handler tickerTapeUpdaterHandler;
    private long startTime;
    private int countItemsDisplayedCompletely;
    private boolean isCancelled;

    public TickerTapeUpdater(TickerTape tickerTape) {
        this.tickerTape = tickerTape;
        tickerTapeUpdaterHandler = new Handler();
    }

    /**
     * Starts moving the ticker tape.
     */
    public void startMoving() {
        isCancelled = false;
        startTime = System.currentTimeMillis();
        countItemsDisplayedCompletely = 0;
        tickerTape.setVisibility(View.VISIBLE);
        runAfterDelay();
    }

    @Override
    public void run() {
        if (!isCancelled) {
            String currentItemBeforeUpdate = tickerTape.getCurrentItem();
            tickerTape.moveToNextPosition();
            countItemsDisplayedCompletely += currentItemBeforeUpdate.equals(
                    tickerTape.getCurrentItem()) ? 0 : 1;

            if (tickerTape.isEraseConditionFulfilled(countItemsDisplayedCompletely,
                                                     System.currentTimeMillis() - startTime)) {
                tickerTape.hide();
                return;
            }

            runAfterDelay();
        }
    }

    private void runAfterDelay() {
        if (!isCancelled) {
            tickerTapeUpdaterHandler.postDelayed(this, DELAY_MILLIS);
        }
    }

    /**
     * Informs the TickerTapeUpdater that it should cancel as soon as possible.
     */
    public void cancel() {
        isCancelled = true;
    }

    /**
     * Checks whether the runnable will be cancelled on the next occasion.
     *
     * @return True in case the runnable will be cancelled on the next occasion. False otherwise.
     */
    public boolean isCancelled() {
        return isCancelled;
    }
}
