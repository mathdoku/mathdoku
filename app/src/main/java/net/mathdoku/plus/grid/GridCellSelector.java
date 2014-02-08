package net.mathdoku.plus.grid;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public abstract class GridCellSelector {
	private List<GridCell> mCells;

	public GridCellSelector(List<GridCell> cells) {
		if (cells == null) {
			throw new InvalidParameterException(
					"GridSelector cannot be instantiated without list of GridCell.");
		}

		mCells = cells;
	}

	/**
	 * Determines whether a grid cell should be returned by the selector upon
	 * calling find().
	 * 
	 * @param gridCell
	 *            The grid cell for which it has to be determined whether it has
	 *            to be selected.
	 * @return True if the gridCell has to be selected. False otherwise.
	 */
	abstract public boolean select(GridCell gridCell);

	/**
	 * Selects all cells from the list which fulfill the select-criteria.
	 */
	public List<GridCell> find() {
		List<GridCell> cells = new ArrayList<GridCell>();

		for (GridCell gridCell : mCells) {
			if (select(gridCell)) {
				cells.add(gridCell);
			}
		}

		return cells;
	}
}
