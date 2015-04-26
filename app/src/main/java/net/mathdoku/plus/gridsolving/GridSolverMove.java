package net.mathdoku.plus.gridsolving;

// Additional data structure in case the solution has to be uncovered.
class GridSolverMove {
    final int mCageId;
    final int mSolutionRow;
    final int mCellRow;
    final int mCellCol;
    final int mCellValue;

    public GridSolverMove(int cageId, int solutionRow, int cellRow, int cellCol, int cellValue) {
        mCageId = cageId;
        mSolutionRow = solutionRow;
        mCellRow = cellRow;
        mCellCol = cellCol;
        mCellValue = cellValue;
    }
}
