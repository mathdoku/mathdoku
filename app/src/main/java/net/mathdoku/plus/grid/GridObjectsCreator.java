package net.mathdoku.plus.grid;

import com.srlee.DLX.MathDokuDLX;

import net.mathdoku.plus.gridGenerating.GridGeneratingParameters;
import net.mathdoku.plus.statistics.GridStatistics;
import net.mathdoku.plus.storage.database.DatabaseHelper;
import net.mathdoku.plus.storage.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;
import net.mathdoku.plus.storage.database.StatisticsDatabaseAdapter;

import java.util.ArrayList;

/**
 * The GridObjectsCreator is responsible for creating all objects needed
 * by the Grid.
 */
public class GridObjectsCreator {
	public Grid createGrid(GridBuilder gridBuilder) {
		return new Grid(gridBuilder);
	}

	public GridCell createGridCell(int id, int gridSize) {
		return new GridCell(id, gridSize);
	}

	public GridCage createGridCage() {
		return new GridCage();
	}

	public GridStatistics createGridStatistics() {
		return new GridStatistics();
	}

	public GridGeneratingParameters createGridGeneratingParameters() {
		return new GridGeneratingParameters();
	}

	public MathDokuDLX createMathDokuDLX(int gridSize,
										 ArrayList<GridCage> cages) {
		return new MathDokuDLX(gridSize, cages);
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

	public GridCellSelectorInRowOrColumn createGridCellSelectorInRowOrColumn(
			ArrayList<GridCell> cells, int row, int column) {
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

	public GridBuilder createGridBuilder() {
		return new GridBuilder();
	}
}
