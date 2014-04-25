package net.mathdoku.plus.gridgenerating;

import android.os.AsyncTask;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.config.Config.AppMode;
import net.mathdoku.plus.gridgenerating.iface.GridGeneratorAsyncTaskIface;
import net.mathdoku.plus.gridgenerating.iface.GridGeneratorAsyncTaskListenerIface;
import net.mathdoku.plus.gridgenerating.iface.GridGeneratorIface;
import net.mathdoku.plus.gridgenerating.iface.GridGeneratorListenerIface;
import net.mathdoku.plus.puzzle.grid.Grid;

import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that generates a grid for each set of grid generating
 * parameters.
 */
public class GridGeneratorAsyncTask extends
		AsyncTask<GridGeneratingParameters, String, List<Grid>> implements
		GridGeneratorAsyncTaskIface, GridGeneratorListenerIface {
	@SuppressWarnings("unused")
	private static final String TAG = GridGeneratorAsyncTask.class.getName();

	private final GridGeneratorAsyncTaskListenerIface gridGeneratorAsyncTaskListenerIface;

	public static class ObjectsCreator {
		public GridGeneratorIface createGridGenerator(
				GridGeneratorAsyncTask gridGeneratorAsyncTask) {
			return new GridGenerator(gridGeneratorAsyncTask);
		}
	}

	private final ObjectsCreator objectsCreator;

	private boolean generateInDevelopmentMode = false;
	private String prefixProgressUpdateDevelopmentMode = "";
	private boolean forceSlowGeneratingExceptionInDevelopmentMode;

	public GridGeneratorAsyncTask(
			GridGeneratorAsyncTaskListenerIface generatorAsyncTaskListenerInterface) {
		this(generatorAsyncTaskListenerInterface, null);
	}

	// package private constructor is needed for unit testing.
	GridGeneratorAsyncTask(
			GridGeneratorAsyncTaskListenerIface generatorAsyncTaskListenerInterface,
			ObjectsCreator objectsCreator) {
		if (generatorAsyncTaskListenerInterface == null) {
			throw new IllegalArgumentException("Listener should be specified.");
		}
		this.gridGeneratorAsyncTaskListenerIface = generatorAsyncTaskListenerInterface;

		this.objectsCreator = objectsCreator == null ? new ObjectsCreator()
				: objectsCreator;
	}

	public void createGrids(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		super.execute(gridGeneratingParametersArray);
	}

	public void cancel() {
		super.cancel(true);
	}

	@Override
	protected List<Grid> doInBackground(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		setGenerateMode(gridGeneratingParametersArray);
		return generateInDevelopmentMode ? generateGridsInDevelopmentMode(gridGeneratingParametersArray)
				: generateGridsInNormalMode(gridGeneratingParametersArray);
	}

	private void setGenerateMode(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		if (gridGeneratingParametersArray == null
				|| gridGeneratingParametersArray.length == 0) {
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

	private List<Grid> generateGridsInDevelopmentMode(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		GridGeneratorIface gridGenerator = objectsCreator
				.createGridGenerator(this);
		List<Grid> generatedGridsInDevelopmentMode = new ArrayList<Grid>();
		for (GridGeneratingParameters gridGeneratingParameters : gridGeneratingParametersArray) {
			prefixProgressUpdateDevelopmentMode = String.format(
					"Generating grid %d: ",
					generatedGridsInDevelopmentMode.size() + 1);
			Grid grid = gridGenerator
					.createGridInDevelopmentMode(gridGeneratingParameters);
			if (grid != null) {
				generatedGridsInDevelopmentMode.add(grid);
			}
			gridGeneratorAsyncTaskListenerIface.onGridGenerated();
		}
		return generatedGridsInDevelopmentMode;
	}

	private List<Grid> generateGridsInNormalMode(
			GridGeneratingParameters... gridGeneratingParametersArray) {
		GridGeneratorIface gridGenerator = objectsCreator
				.createGridGenerator(this);
		List<Grid> generatedGrids = new ArrayList<Grid>();
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
		gridGeneratorAsyncTaskListenerIface
				.onHighLevelProgressUpdate(prefixProgressUpdateDevelopmentMode
						+ text);
	}

	@Override
	public void updateProgressDetailLevel(String text) {
		gridGeneratorAsyncTaskListenerIface
				.onDetailLevelProgressDetail(text);
	}

	@Override
	public void signalSlowGridGeneration() {
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

	@Override
	protected void onPostExecute(List<Grid> generatedGrids) {
		if (Config.mAppMode == AppMode.DEVELOPMENT
				&& forceSlowGeneratingExceptionInDevelopmentMode) {
			throw new GridGeneratingException(
					"Investigate slow game generation. See logcat for more info.");
		}

		gridGeneratorAsyncTaskListenerIface
				.onFinishGridGenerator(generatedGrids);
	}

	@Override
	protected void onCancelled(List<Grid> generatedGrids) {
		gridGeneratorAsyncTaskListenerIface
				.onCancelGridGeneratorAsyncTask();
		super.onCancelled(generatedGrids);
	}
}
