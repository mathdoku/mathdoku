package net.mathdoku.plus.painter;

import net.mathdoku.plus.painter.Painter.GridTheme;

abstract class BasePainter {

    // Reference to the global painter object.
    final Painter mPainter;

    /**
     * Creates a new instance of {@link BasePainter}.
     *
     * @param painter
     *         The global container for all painters.
     */
    BasePainter(Painter painter) {
        mPainter = painter;
    }

    public abstract void setTheme(GridTheme theme);

    /**
     * Changes the painter objects relative to the new size of a cell in the grid.
     *
     * @param size
     *         The size of cells.
     */
    protected abstract void setCellSize(float size);
}
