package net.cactii.mathdoku;

import android.os.AsyncTask;

/*
 * Controls the timer within the game.
 */
public class GameTimer extends AsyncTask<Void, Long, Long> {

	public MainActivity mActivity;
	public Long mStartTime;
	public Long mElapsedTime = (long) 0;

	public GameTimer(MainActivity mainActivity) {
		mActivity = mainActivity;
	}
	
	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	protected void onProgressUpdate(Long... time) {
		if (!this.isCancelled() && time.length > 0 && mActivity != null) {
			mActivity.setElapsedTime(time[0]);
		}
	}
}
