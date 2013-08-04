package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;

abstract class BasePainter {

	// Reference to the global painter object.
	protected Painter mPainter;

	/**
	 * Creates a new instance of {@link BasePainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public BasePainter(Painter painter) {
		mPainter = painter;
	}

	public abstract void setTheme(GridTheme theme);

	/**
	 * Changes the painter objects relative to the new size of a cell in the
	 * grid.
	 * 
	 * @param size
	 *            The size of cells.
	 */
	protected abstract void setCellSize(float size);
}
