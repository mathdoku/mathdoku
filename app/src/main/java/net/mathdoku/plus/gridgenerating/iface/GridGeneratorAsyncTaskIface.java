package net.mathdoku.plus.gridgenerating.iface;

import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;

public interface GridGeneratorAsyncTaskIface extends GridGeneratorListenerIface {
	// Start generating grid(s)
	void createGrids(GridGeneratingParameters... gridGeneratingParametersArray);

	// Cancel generating grid(s)
	void cancel();
}
