package net.cactii.mathdoku;

import net.cactii.mathdoku.ui.PuzzleFragment;
import android.os.AsyncTask;

/*
 * Controls the timer within the game.
 */
public class GameTimer extends AsyncTask<Void, Long, Long> {

	// Reference to activity that started the timer.
	public PuzzleFragment mPuzzleFragment;

	// Starting point of timer. Note this is not the real time at which the game
	// started but the actual time at which the timer started minus the time
	// elapsed until then.
	public Long mStartTime;

	// Time elapsed while (dis)playing the current grid.
	public Long mElapsedTime = (long) 0;

	// Time added to the real playing time because of using cheats. Effectively
	// the starting time of the game is decreased.
	public Long mCheatPenaltyTime = (long) 0;

	public GameTimer(PuzzleFragment puzzleFragmentActivity) {
		mPuzzleFragment = puzzleFragmentActivity;
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
	 * @param cheatPenaltyType
	 *            The cheat for which the elapsed time has to be increased.
	 */
	public void addCheatPenaltyTime(Cheat cheat) {
		addCheatPenaltyTime(cheat, 1);
	}

	/**
	 * Add a penalty to the elapsed time because of using the given cheat.
	 * 
	 * @param cheatPenaltyType
	 *            The cheat for which the elapsed time has to be increased.
	 * @param occurences
	 *            The number of occurrences this penalty has to be added.
	 */
	public void addCheatPenaltyTime(Cheat cheat, int occurrences) {
		// Determine penalty time for just one occurrence
		long cheatPenaltyTime = cheat.getPenaltyTimeMilis() * occurrences;

		// Change start time, elapsed and total penalty time. 
		mStartTime -= cheatPenaltyTime;
		mElapsedTime += cheatPenaltyTime;
		mCheatPenaltyTime += cheatPenaltyTime;
	}
}
