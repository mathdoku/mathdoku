package net.mathdoku.plus;

import android.os.AsyncTask;
import android.util.Log;

import net.mathdoku.plus.puzzle.cheat.Cheat;
import net.mathdoku.plus.ui.PuzzleFragment;

/*
 * Controls the timer within the game.
 */
public class GameTimer extends AsyncTask<Void, Long, Long> {
	private final static String TAG = GameTimer.class.getName();

	// References to activity that started the timer and the solving attempt to
	// which the timer applies
	private final PuzzleFragment mPuzzleFragment;
	private final int solvingAttemptId;

	// Starting point of timer. Note this is not the real time at which the game
	// started but the actual time at which the timer started minus the time
	// elapsed until then.
	private Long mStartTime;

	// Time elapsed while (dis)playing the current grid.
	public Long mElapsedTime = (long) 0;

	// Time added to the real playing time because of using cheats. Effectively
	// the starting time of the game is decreased.
	public Long mCheatPenaltyTime = (long) 0;

	public GameTimer(PuzzleFragment puzzleFragmentActivity, int solvingAttemptId) {
		mPuzzleFragment = puzzleFragmentActivity;
		this.solvingAttemptId = solvingAttemptId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Long doInBackground(Void... arg0) {
		long previousTime = 0;

		if (this.isCancelled()) {
			return mElapsedTime;
		}

		mStartTime = System.currentTimeMillis() - mElapsedTime;
		publishProgress(mElapsedTime);
		while (!this.isCancelled()) {
			mElapsedTime = System.currentTimeMillis() - mStartTime;
			if (mElapsedTime - previousTime > 1000) {
				publishProgress(mElapsedTime);
				previousTime = mElapsedTime;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Nothing to be done, task will be cancelled.
				Log.d(TAG, "Sleep of game timer is cancelled (not an error).",
						e);
			}
		}
		return mElapsedTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(Long... time) {
		if (!this.isCancelled() && time.length > 0 && mPuzzleFragment != null) {
			mPuzzleFragment.setElapsedTime(time[0]);
		}
	}

	/**
	 * Add a penalty to the elapsed time because of using the given cheat.
	 * 
	 * @param cheat
	 *            The cheat for which the elapsed time has to be increased.
	 */
	public void addCheatPenaltyTime(Cheat cheat) {
		addCheatPenaltyTime(cheat, 1);
	}

	/**
	 * Add a penalty to the elapsed time because of using the given cheat.
	 * 
	 * @param cheat
	 *            The cheat for which the elapsed time has to be increased.
	 * @param occurrences
	 *            The number of occurrences this penalty has to be added.
	 */
	@SuppressWarnings("SameParameterValue")
	void addCheatPenaltyTime(Cheat cheat, int occurrences) {
		// Determine penalty time for just one occurrence
		long cheatPenaltyTime = cheat.getPenaltyTimeMilliseconds()
				* occurrences;

		// Change start time, elapsed and total penalty time.
		mStartTime -= cheatPenaltyTime;
		mElapsedTime += cheatPenaltyTime;
		mCheatPenaltyTime += cheatPenaltyTime;
	}

	public boolean isCreatedForSolvingAttemptId(int solvingAttemptId) {
		return this.solvingAttemptId == solvingAttemptId;
	}
}
