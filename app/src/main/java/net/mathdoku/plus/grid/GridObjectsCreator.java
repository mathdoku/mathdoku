package net.mathdoku.plus.grid;

import com.srlee.DLX.MathDokuDLX;

import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.CellChangeStorage;
import net.mathdoku.plus.storage.GridCageStorage;
import net.mathdoku.plus.storage.GridCellStorage;
import net.mathdoku.plus.storage.GridStorage;
import net.mathdoku.plus.storage.SolvingAttemptStorage;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * The GridObjectsCreator is responsible for creating all objects needed by the
 * Grid.
 */
public class GridObjectsCreator {
	public Grid createGrid(GridBuilder gridBuilder) {
		return new Grid(gridBuilder);
	}

	public GridCell createGridCell(int id, int gridSize) {
		return new GridCell(id, gridSize);
	}

	public GridCage createGridCage(CageBuilder cageBuilder) {
		return new GridCage(cageBuilder);
	}

	public GridStatistics createGridStatistics() {
		return new GridStatistics();
	}

	public GridGeneratingParameters createGridGeneratingParameters() {
		return new GridGeneratingParameters();
	}

	public MathDokuDLX createMathDokuDLX(int gridSize, List<GridCage> cages) {
		return new MathDokuDLX(gridSize, cages);
	}

	public List<GridCell> createArrayListOfGridCells() {
		return new ArrayList<GridCell>();
	}

	public List<GridCage> createArrayListOfGridCages() {
		return new ArrayList<GridCage>();
	}

	public List<CellChange> createArrayListOfCellChanges() {
		return new ArrayList<CellChange>();
	}

	public GridCellSelectorInRowOrColumn createGridCellSelectorInRowOrColumn(
			List<GridCell> cells, int row, int column) {
		return new GridCellSelectorInRowOrColumn(cells, row, column);
	}

	public DatabaseHelper createDatabaseHelper() {
		return DatabaseHelper.getInstance();
	}

	public GridDatabaseAdapter createGridDatabaseAdapter() {
		return new GridDatabaseAdapter();
	}

	public SolvingAttemptDatabaseAdapter createSolvingAttemptDatabaseAdapter() {
		return new SolvingAttemptDatabaseAdapter();
	}

	public StatisticsDatabaseAdapter createStatisticsDatabaseAdapter() {
		return new StatisticsDatabaseAdapter();
	}

	public GridCell createGridCell(GridCellStorage gridCellStorage) {
		return new GridCell(gridCellStorage);
	}

	public CellChange createCellChange(CellChangeStorage cellChangeStorage) {
		return new CellChange(cellChangeStorage);
	}

	public GridStorage createGridStorage() {
		return new GridStorage();
	}

	public GridCageStorage createGridCageStorage() {
		return new GridCageStorage();
	}

	public GridBuilder createGridBuilder() {
		return new GridBuilder();
	}

	public GridCellStorage createGridCellStorage() {
		return new GridCellStorage();
	}

	public CellChangeStorage createCellChangeStorage() {
		return new CellChangeStorage();
	}

	public CageBuilder createCageBuilder() {
		return new CageBuilder();
	}

	public GridSaver createGridSaver() {
		return new GridSaver();
	}

	public SolvingAttemptStorage createSolvingAttemptStorage(
			String storageString) {
		return new SolvingAttemptStorage(storageString);
	}
}
