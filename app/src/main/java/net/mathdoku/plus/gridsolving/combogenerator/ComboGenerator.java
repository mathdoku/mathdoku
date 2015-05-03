package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.enums.CageOperator;
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
    private List<Cell> mCageCells;
    private int mGridSize;

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
        mResult = cage.getResult();
        mCageOperator = cage.getOperator();
        mHideOperator = cage.isOperatorHidden();
        mCageCells = cells;

        if (mHideOperator) {
            return getPossibleCombosHiddenOperator();
        } else {
            return getPossibleCombosVisibleOperator();
        }
    }

    /**
     * Get all permutations of cell values for this cage.
     *
     * @return The list of all permutations of cell values which can be used for this cage.
     */
    private List<int[]> getPossibleCombosHiddenOperator() {
        List<int[]> resultCombos = new ArrayList<int[]>();

        // Single cell cages can only contain the value of the single cell.
        if (mCageCells.size() == 1) {
            int[] number = {mResult};
            resultCombos.add(number);
            return resultCombos;
        }

        // Cages of size two can contain any operation
        if (mCageCells.size() == 2) {
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
        resultCombos = convertToOldStyle(new AddCageComboGenerator(this).getCombosForCage(cage));
        List<int[]> multiplyCombos = convertToOldStyle(new MultiplyCageComboGenerator(this).getCombosForCage(cage));

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
                assert mCageCells.size() == 1;
                int[] number = {mResult};
                AllResults.add(number);
                break;
            case SUBTRACT:
                assert mCageCells.size() == 2;
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
                assert mCageCells.size() == 2;
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
                AllResults = convertToOldStyle(new AddCageComboGenerator(this).getCombosForCage(cage));
                break;
            case MULTIPLY:
                AllResults = convertToOldStyle(new MultiplyCageComboGenerator(this).getCombosForCage(cage));
                break;
        }
        return AllResults;
    }

    /**
     * Method is to be removed when refactor of CageCombo is complete throughout whole app.
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

    /**
     * Checks if the given permutation can be filled in in the cells of the cages without violating the rule that a
     * digit can be used only once on each row and each column.
     */
    boolean satisfiesConstraints(CageCombo cageCombo) {
        // The first dimension for rowConstraints holds the different rows of
        // the grid. The second dimension indicates whether digit (columnIndex +
        // 1) is used in this row.
        boolean[][] rowConstraints = new boolean[mGridSize][mGridSize];

        // The first dimension for columnConstraints holds the different columns
        // of the grid. The second dimension indicates whether digit
        // (columnIndex + 1) is used in this column.
        boolean[][] columnConstraints = new boolean[mGridSize][mGridSize];

        // The values of the given permutation are copied in the specified order
        // to the cells of the cages.
        int rowConstraintsDimension1;
        int columnConstraintsDimension1;
        int constraintsDimension2;
        for (int i = 0; i < this.mCageCells.size(); i++) {
            // The actual position of i-th cell in the grid determines the first
            // dimension of the constraint arrays.
            rowConstraintsDimension1 = mCageCells.get(i)
                    .getRow();
            columnConstraintsDimension1 = mCageCells.get(i)
                    .getColumn();

            // The value of the i-th position of the permutation determines the
            // second dimension for both constraint arrays.
            constraintsDimension2 = cageCombo.getCellValue(i) - 1;

            if (rowConstraints[rowConstraintsDimension1][constraintsDimension2]) {
                // The value is already used on this row of the grid
                return false;
            }
            rowConstraints[rowConstraintsDimension1][constraintsDimension2] = true;

            if (columnConstraints[columnConstraintsDimension1][constraintsDimension2]) {
                // The value is already used on this column of the grid.
                return false;
            }
            columnConstraints[columnConstraintsDimension1][constraintsDimension2] = true;
        }

        // This permutation can be used to fill the cells of the cage without
        // violation the rules.
        return true;
    }

    public int getGridSize() {
        return mGridSize;
    }
}
