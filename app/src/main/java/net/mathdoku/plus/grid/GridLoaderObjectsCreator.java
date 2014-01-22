package net.mathdoku.plus.grid;

import net.mathdoku.plus.storage.CellChangeStorage;
import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.GridCellStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import java.util.ArrayList;

/**
 * The GridLoaderObjectsCreator is responsible for creating all objects needed
 * by the GridLoader.
 */
public class GridLoaderObjectsCreator {
	public GridCell createGridCell(GridCellStorage gridCellStorage) {
		return new GridCell(gridCellStorage);
	}

	public CellChange createCellChange(CellChangeStorage cellChangeStorage) {
		return new CellChange(cellChangeStorage);
	}

	public GridCage createGridCage(GridCageStorage gridCageStorage) {
		return new GridCage(gridCageStorage);
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

	public GridLoaderData createGridLoaderData() {
		return new GridLoaderData();
	}

	public GridDatabaseAdapter createGridDatabaseAdapter() {
		return new GridDatabaseAdapter();
	}

	public Grid createGrid(GridLoaderData gridLoaderData) {
		return new Grid(gridLoaderData);
	}

	public SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
		return new SolvingAttemptDatabaseAdapter();
	}

	public ArrayList<GridCell> createArrayListOfGridCells() {
		return new ArrayList<GridCell>();
	}

	public ArrayList<GridCage> createArrayListOfGridCages() {
		return new ArrayList<GridCage>();
	}

	public ArrayList<CellChange> createArrayListOfCellChanges() {
		return new ArrayList<CellChange>();
	}

	public GridCellStorage createGridCellStorage() {
		return new GridCellStorage();
	}

	public CellChangeStorage createCellChangeStorage() {
		return new CellChangeStorage();
	}
}