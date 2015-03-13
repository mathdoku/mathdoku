package net.mathdoku.plus.gridgenerating.iface;

import net.mathdoku.plus.puzzle.grid.Grid;

import java.util.List;

/**
 * Interface for listeners on the GridGeneratorAsyncTask.
 */
public interface GridGeneratorAsyncTaskListenerIface {
    /**
     * Inform the listener if a grid is generated. This event is only sent in case multiple grids have to be generated.
     */
    void onGridGenerated();

    /**
     * Inform the listener when the grid generator has finished generating the grid(s).
     *
     * @param grids
     *         The list of generated grid(s).
     */
    void onFinishGridGenerator(List<Grid> grids);

    /**
     * Inform the listener if the grid generating task has been cancelled by the async task or its super class.
     */
    void onCancelGridGeneratorAsyncTask();

    /**
     * Inform the listener when a new phase is entered.
     *
     * @param text
     *         Text describing the high level update.
     */
    void onHighLevelProgressUpdate(String text);

    /**
     * Inform the listener with a detailed progress update.
     *
     * @param text
     *         Text describing the detail level update.
     */
    void onDetailLevelProgressDetail(String text);
}
