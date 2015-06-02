package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.matrix.Matrix;
import net.mathdoku.plus.matrix.UniqueValuePerRowAndColumnChecker;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComboGenerator {
    private Cage cage;
    private int mResult;
    private CageOperator mCageOperator;
    private boolean mHideOperator;
    private List<Cell> cells;
    private int mGridSize;
    private int minRow;
    private int maxRow;
    private int minColumn;
    private int maxColumn;

    public ComboGenerator(int gridSize) {
        mGridSize = gridSize;
    }

    /**
     * Get all possible combinations for the given cage.
     *
     * @param cage
     *         The cage for which all possible combo's have to be determined.
     * @param cells
     *         The list of cells for this cage.
     * @return The list of all possible combinations. Null in case no combinations or too many permutations have been
     * found.
     */
    public List<int[]> getPossibleCombos(Cage cage, List<Cell> cells) {
        this.cage = cage;
        this.cells = cells;
        mResult = cage.getResult();
        mCageOperator = cage.getOperator();
        mHideOperator = cage.isOperatorHidden();

        minRow = getMinRow(this.cells);
        maxRow = getMaxRow(this.cells);
        minColumn = getMinColumn(this.cells);
        maxColumn = getMaxColumn(this.cells);

        if (mHideOperator) {
            return getPossibleCombosHiddenOperator();
        } else {
            return getPossibleCombosVisibleOperator();
        }
    }

    private int getMinRow(List<Cell> cells) {
        int minRow = Integer.MAX_VALUE;
        for (Cell cell : cells) {
            minRow = Math.min(minRow, cell.getRow());
        }
        return minRow;
    }

    private int getMaxRow(List<Cell> cells) {
        int maxRow = Integer.MIN_VALUE;
        for (Cell cell : cells) {
            maxRow = Math.max(maxRow, cell.getRow());
        }
        return maxRow;
    }

    private int getMinColumn(List<Cell> cells) {
        int minColumn = Integer.MAX_VALUE;
        for (Cell cell : cells) {
            minColumn = Math.min(minColumn, cell.getColumn());
        }
        return minColumn;
    }

    private int getMaxColumn(List<Cell> cells) {
        int maxColumn = Integer.MIN_VALUE;
        for (Cell cell : cells) {
            maxColumn = Math.max(maxColumn, cell.getColumn());
        }
        return maxColumn;
    }

    /**
     * Get all permutations of cell values for this cage.
     *
     * @return The list of all permutations of cell values which can be used for this cage.
     */
    private List<int[]> getPossibleCombosHiddenOperator() {
        List<int[]> resultCombos = new ArrayList<int[]>();

        // Single cell cages can only contain the value of the single cell.
        if (cells.size() == 1) {
            int[] number = {mResult};
            resultCombos.add(number);
            return resultCombos;
        }

        // Cages of size two can contain any operation
        if (cells.size() == 2) {
            for (int i1 = 1; i1 <= mGridSize; i1++) {
                for (int i2 = i1 + 1; i2 <= mGridSize; i2++) {
                    if (i2 - i1 == mResult || i1 - i2 == mResult || mResult * i1 == i2 || mResult * i2 == i1 || i1 +
                            i2 == mResult || i1 * i2 == mResult) {
                        int[] numbers = {i1, i2};
                        resultCombos.add(numbers);
                        numbers = new int[]{i2, i1};
                        resultCombos.add(numbers);
                    }
                }
            }
            return resultCombos;
        }

        // Cages of size two and above can only contain an add or a multiply
        // operation
        resultCombos = convertToOldStyle(new AddCageComboGenerator(this, cage).getCombos());
        List<int[]> multiplyCombos = convertToOldStyle(new MultiplyCageComboGenerator(this, cage).getCombos());

        // Combine Add & Multiply result sets
        for (int[] multiplyCombo : multiplyCombos) {
            boolean newCombo = true;
            for (int[] resultCombo : resultCombos) {
                if (Arrays.equals(multiplyCombo, resultCombo)) {
                    newCombo = false;
                    break;
                }
            }
            if (newCombo) {
                resultCombos.add(multiplyCombo);
            }
        }

        return resultCombos;
    }

    /*
     * Generates all combinations of numbers which satisfy the cage's arithmetic
     * and MathDoku constraints i.e. a digit can only appear once in a
     * column/row
     */
    private List<int[]> getPossibleCombosVisibleOperator() {
        List<int[]> AllResults = new ArrayList<int[]>();

        switch (mCageOperator) {
            case NONE:
                assert cells.size() == 1;
                int[] number = {mResult};
                AllResults.add(number);
                break;
            case SUBTRACT:
                assert cells.size() == 2;
                for (int i1 = 1; i1 <= mGridSize; i1++) {
                    for (int i2 = i1 + 1; i2 <= mGridSize; i2++) {
                        if (i2 - i1 == mResult || i1 - i2 == mResult) {
                            int[] numbers = {i1, i2};
                            AllResults.add(numbers);
                            numbers = new int[]{i2, i1};
                            AllResults.add(numbers);
                        }
                    }
                }
                break;
            case DIVIDE:
                assert cells.size() == 2;
                for (int i1 = 1; i1 <= mGridSize; i1++) {
                    for (int i2 = i1 + 1; i2 <= mGridSize; i2++) {
                        if (mResult * i1 == i2 || mResult * i2 == i1) {
                            int[] numbers = {i1, i2};
                            AllResults.add(numbers);
                            numbers = new int[]{i2, i1};
                            AllResults.add(numbers);
                        }
                    }
                }
                break;
            case ADD:
                AllResults = convertToOldStyle(CageComboGenerator.create(this, cage).getCombos());
                break;
            case MULTIPLY:
                AllResults = convertToOldStyle(CageComboGenerator.create(this, cage).getCombos());
                break;
        }
        return AllResults;
    }

    /**
     * TODO: Method is to be removed when refactor of CageCombo is complete throughout whole app.
     */
    @Deprecated
    private static List<int[]> convertToOldStyle(List<CageCombo> cageCombos) {
        List<int []> oldCageCombos = new ArrayList<int[]>();
        for (CageCombo cageCombo : cageCombos) {
            List<Integer> cellValues = cageCombo.getCellValues();
            int[] oldCageCombo = new int[cellValues.size()];
            int index = 0;
            for (Integer cellValue : cellValues) {
                oldCageCombo[index] = cellValue;
                index++;
            }
           oldCageCombos.add(oldCageCombo);
        }
        return oldCageCombos;
    }

    boolean satisfiesConstraints(CageCombo cageCombo) {
        Matrix<Integer> cageComboMatrix = mapCageComboToValueMatrix(cageCombo);
        return UniqueValuePerRowAndColumnChecker.create(cageComboMatrix).hasNoDuplicateValues();
    }

    private Matrix<Integer> mapCageComboToValueMatrix(CageCombo cageCombo) {
        Matrix<Integer> matrix = new Matrix<Integer>(maxRow - minRow + 1, maxColumn - minColumn + 1,
                                                     Cell.NO_ENTERED_VALUE);

        if (cageCombo.getCellValues().size() != cells.size()) {
            throw new IllegalArgumentException("Size of combo is not equal to size of cage.");
        }
        for (int index = 0; index < cells.size(); index++) {
            matrix.setValueToRowColumn(cageCombo.getCellValue(index), cells.get(index).getRow() - minRow,
                                       cells.get(index).getColumn() - minColumn);
        }

        return matrix;
    }

    public int getGridSize() {
        return mGridSize;
    }
}
