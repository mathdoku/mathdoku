package net.mathdoku.plus.grid;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public abstract class CellSelector {
	private List<Cell> mCells;

	public CellSelector(List<Cell> cells) {
		if (cells == null) {
			throw new InvalidParameterException(
					"GridSelector cannot be instantiated without list of Cell.");
		}

		mCells = cells;
	}

	/**
	 * Determines whether a grid cell should be returned by the selector upon
	 * calling find().
	 * 
	 * @param cell
	 *            The grid cell for which it has to be determined whether it has
	 *            to be selected.
	 * @return True if the cell has to be selected. False otherwise.
	 */
	abstract public boolean select(Cell cell);

	/**
	 * Selects all cells from the list which fulfill the select-criteria.
	 */
	public List<Cell> find() {
		List<Cell> cells = new ArrayList<Cell>();

		for (Cell cell : mCells) {
			if (select(cell)) {
				cells.add(cell);
			}
		}

		return cells;
	}
}
