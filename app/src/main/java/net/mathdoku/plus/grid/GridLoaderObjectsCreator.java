package net.mathdoku.plus.grid;

import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import java.util.ArrayList;

public class GridLoaderObjectsCreator {
	public GridCell createGridCell(int id, int gridSize) {
		return new GridCell(id, gridSize);
	}

	public CellChange createCellChange() {
		return new CellChange();
	}

	public GridCage createGridCage(int id, boolean hideOperator, int result, int action,
								   ArrayList<GridCell> cells) {
		return new GridCage(id, hideOperator, result, action, cells);
	}

	public StatisticsDatabaseAdapter createStatisticsDatabaseAdapter() {
		return new StatisticsDatabaseAdapter();
	}

	public GridStorage createGridStorage() {
		return new GridStorage();
	}

	public GridCageStorage createGridCageStorage() {
		return new GridCageStorage();
	}
}