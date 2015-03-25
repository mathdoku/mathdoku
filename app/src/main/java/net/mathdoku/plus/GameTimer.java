package net.mathdoku.plus;

import android.os.AsyncTask;
import android.util.Log;

import net.mathdoku.plus.puzzle.cheat.Cheat;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.ui.PuzzleFragment;

/*
 * Controls the timer within the game.
 */
public class GameTimer extends AsyncTask<Void, Long, Long> {
    private static final String TAG = GameTimer.class.getName();

    // References to activity that started the timer and the solving attempt to which the timer applies
    private final PuzzleFragment puzzleFragment;
    private final int solvingAttemptId;

    // Starting point of timer. Note this is not the real time at which the game started but the actual time at which
    // the timer started minus the time elapsed until then.
    private Long startTime;

    private Long elapsedMillisSinceStartTime = (long) 0;

    // Time added to the real playing time because of using cheats. Effectively the starting time of the game is
    // decreased.
    private Long cheatPenaltyTimeInMilliseconds = (long) 0;

    public GameTimer(PuzzleFragment puzzleFragment, Grid grid) {
        this.puzzleFragment = puzzleFragment;
        solvingAttemptId = grid.getSolvingAttemptId();
        elapsedMillisSinceStartTime = grid.getElapsedTime();
        cheatPenaltyTimeInMilliseconds = grid.getCheatPenaltyTime();
    }

    @Override
    protected Long doInBackground(Void... arg0) {
        long previousTime = 0;

        if (this.isCancelled()) {
            return elapsedMillisSinceStartTime;
        }

        startTime = System.currentTimeMillis() - elapsedMillisSinceStartTime;
        publishProgress(elapsedMillisSinceStartTime);
        while (!this.isCancelled()) {
            elapsedMillisSinceStartTime = System.currentTimeMillis() - startTime;
            if (elapsedMillisSinceStartTime - previousTime > 1000) {
                publishProgress(elapsedMillisSinceStartTime);
                previousTime = elapsedMillisSinceStartTime;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Nothing to be done, task will be cancelled.
                Log.d(TAG, "Sleep of game timer is cancelled (not an error).", e);
            }
        }
        return elapsedMillisSinceStartTime;
    }

    @Override
    protected void onProgressUpdate(Long... time) {
        if (!this.isCancelled() && time.length > 0 && puzzleFragment != null) {
            puzzleFragment.setElapsedTime(time[0]);
        }
    }

    /**
     * Add a penalty to the elapsed time because of using the given cheat.
     *
     * @param cheat
     *         The cheat for which the elapsed time has to be increased.
     */
    public void addCheatPenaltyTime(Cheat cheat) {
        startTime -= cheat.getPenaltyTimeInMilliseconds();
        elapsedMillisSinceStartTime += cheat.getPenaltyTimeInMilliseconds();
        cheatPenaltyTimeInMilliseconds += cheat.getPenaltyTimeInMilliseconds();
    }

    public boolean isCreatedForSolvingAttemptId(int solvingAttemptId) {
        return this.solvingAttemptId == solvingAttemptId;
    }

    public Long getElapsedMillisSinceStartTime() {
        return elapsedMillisSinceStartTime;
    }

    public Long getCheatPenaltyTimeInMilliseconds() {
        return cheatPenaltyTimeInMilliseconds;
    }
}
