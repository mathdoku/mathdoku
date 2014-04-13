package net.mathdoku.plus.gridgenerating;

import android.os.AsyncTask;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.puzzle.grid.Grid;

import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that generates a grid for each set of grid generating
 * parameters.
 */
public class GridGeneratorAsyncTask extends
		AsyncTask<GridGeneratingParameters, String, List<Grid>> implements
		GridGenerator.Listener {
	private static final String TAG = GridGeneratorAsyncTask.class.getName();

	private final Listener mListener;

	public interface Listener {
		/**
		 * Inform the listeners if a grid is generated. This event is only sent
		 * in case multiple grids have to be generated.
		 */
		void onGridGenerated();

		/**
		 * Inform the listeners when the grid generator has finished generating
		 * the grid(s).
		 * 
		 * @param grid
		 *            The list of generated grid(s).
		 */
		void onFinishGridGenerator(List<Grid> grid);

		void onCancelGridGeneratorAsyncTask();

		void onHighLevelProgressUpdate(String text);

		void onDetailLevelProgressDetail(String text);
	}

	private boolean generateInDevelopmentMode = false;
	private String prefixProgressUpdateDevelopmentMode;
	private boolean forceSlowGeneratingExceptionInDevelopmentMode;

	public GridGeneratorAsyncTask(Listener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Listener should be specified.");
		}
		mListener = listener;
	}

	@Override
	protected List<Grid> doInBackground(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		setGenerateMode(gridGeneratingParametersArray);
		return (generateInDevelopmentMode ? generateGridsInDevelopmentMode(gridGeneratingParametersArray)
				: generateGridsInNormalMode(gridGeneratingParametersArray));
	}

	private void setGenerateMode(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		if (gridGeneratingParametersArray == null) {
			throw new GridGeneratingException(
					"Grid generating parameters must be specified.");
		}

		if (gridGeneratingParametersArray.length == 1) {
			generateInDevelopmentMode = false;
		} else if (Config.mAppMode == AppMode.DEVELOPMENT
				&& gridGeneratingParametersArray.length > 1) {
			generateInDevelopmentMode = true;
		} else {
			throw new GridGeneratingException(
					String
							.format("Unexpected number (%d) of grid generating parameters specified.",
									gridGeneratingParametersArray.length));
		}
	}

	private ArrayList<Grid> generateGridsInDevelopmentMode(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		GridGenerator gridGenerator = new GridGenerator(this);
		ArrayList<Grid> generatedGridsInDevelopmentMode = new ArrayList<Grid>();
		for (GridGeneratingParameters gridGeneratingParameters : gridGeneratingParametersArray) {
			prefixProgressUpdateDevelopmentMode = String.format(
					"Generating grid %d: ",
					generatedGridsInDevelopmentMode.size() + 1);
			Grid grid = gridGenerator
					.createGridInDevelopmentMode(gridGeneratingParameters);
			if (grid != null) {
				generatedGridsInDevelopmentMode.add(grid);
			}
			mListener.onGridGenerated();
		}
		return generatedGridsInDevelopmentMode;
	}

	private ArrayList<Grid> generateGridsInNormalMode(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		GridGenerator gridGenerator = new GridGenerator(this);
		ArrayList<Grid> generatedGrids = new ArrayList<Grid>();
		for (GridGeneratingParameters gridGeneratingParameters : gridGeneratingParametersArray) {
			Grid grid = gridGenerator.createGrid(gridGeneratingParameters);
			if (grid != null) {
				generatedGrids.add(grid);
			}
		}
		return generatedGrids;
	}

	@Override
	public void updateProgressHighLevel(String text) {
		if (generateInDevelopmentMode) {
			mListener
					.onHighLevelProgressUpdate(prefixProgressUpdateDevelopmentMode
							+ text);
		}
	}

	@Override
	public void updateProgressDetailLevel(String text) {
		if (generateInDevelopmentMode) {
			mListener.onDetailLevelProgressDetail(text);
		}
	}

	@Override
	public void signalSlowGridGeneration() {
		if (generateInDevelopmentMode) {
			forceSlowGeneratingExceptionInDevelopmentMode = true;
			// Pause a moment to publish message in the progress dialog
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.d(TAG, "Sleep was interrupted.", e);
			} finally {
				cancel(true);
			}
		}
	}

	@Override
	protected void onPostExecute(List<Grid> generatedGrids) {
		if (Config.mAppMode == AppMode.DEVELOPMENT
				&& forceSlowGeneratingExceptionInDevelopmentMode) {
			throw new GridGeneratingException(
					"Investigate slow game generation. See logcat for more info.");
		}

		mListener.onFinishGridGenerator(generatedGrids);
	}

	@Override
	protected void onCancelled(List<Grid> generatedGrids) {
		mListener.onCancelGridGeneratorAsyncTask();
		super.onCancelled(generatedGrids);
	}
}
