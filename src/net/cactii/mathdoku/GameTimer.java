package net.cactii.mathdoku;

import android.os.AsyncTask;
import android.widget.TextView;

/*
 * Controls the timer within the game.
 */
public class GameTimer extends AsyncTask<Void, Long, Long> {

	public TextView mTimerLabel;
	public Long mStartTime;
	public Long mElapsedTime = (long) 0;

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

	protected void onProgressUpdate(Long... time) {
		String timeString;
		int seconds = (int) (time[0] / 1000); // Whole seconds.
		int hours = (int) Math.floor(seconds / (60 * 60));
		if (hours == 0) {
			timeString = String.format("%2dm%02ds", (seconds % (3600)) / 60,
					seconds % 60);
		} else {
			timeString = String.format("%dh%02dm%02ds", hours,
					(seconds % (3600)) / 60, seconds % 60);
		}
		if (this.isCancelled()) {
			return;
		}
		mTimerLabel.setText(timeString);
	}

	protected void onPostExecute(Void none) {
		//
	}

}
