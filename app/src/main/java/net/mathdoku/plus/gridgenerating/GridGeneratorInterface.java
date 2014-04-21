package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.puzzle.grid.Grid;

public interface GridGeneratorInterface {
	Grid createGrid(GridGeneratingParameters gridGeneratingParameters);

	Grid createGridInDevelopmentMode(GridGeneratingParameters gridGeneratingParameters);
}
