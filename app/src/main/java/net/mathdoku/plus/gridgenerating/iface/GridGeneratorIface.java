package net.mathdoku.plus.gridgenerating.iface;

import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.puzzle.grid.Grid;

public interface GridGeneratorIface {
    Grid createGrid(GridGeneratingParameters gridGeneratingParameters);

    Grid createGridInDevelopmentMode(GridGeneratingParameters gridGeneratingParameters);
}
