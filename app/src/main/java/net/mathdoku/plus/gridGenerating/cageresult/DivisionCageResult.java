package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;

public class DivisionCageResult extends CageResult {
    private DivisionCageResult(int cellValue1, int cellValue2) {
        super(CageOperator.DIVIDE, cellValue1, cellValue2);
    }

    static DivisionCageResult tryToCreate(int... cellValues) {
        if (canBeCreated(cellValues)) {
            return new DivisionCageResult(cellValues[0], cellValues[1]);
        }
        throw new IllegalStateException(
                String.format("Cannot instantiate with specified values: %s",
                              Arrays.toString(cellValues)));
    }

    public static boolean canBeCreated(int... cellValues) {
        return hasCorrectNumberOfCells(cellValues) && canDivide(cellValues);
    }

    private static boolean hasCorrectNumberOfCells(int[] cellValues) {
        return cellValues != null && cellValues.length == 2;
    }

    private static boolean canDivide(int[] cellValues) {
        return Math.max(cellValues[0], cellValues[1]) % Math.min(cellValues[0], cellValues[1]) == 0;
    }

    @Override
    public int getResult() {
        return getResult(getCellValue(0), getCellValue(1));
    }

    private static int getResult(int cellValue1, int cellValue2) {
        return Math.max(cellValue1, cellValue2) / Math.min(cellValue1, cellValue2);
    }
}
