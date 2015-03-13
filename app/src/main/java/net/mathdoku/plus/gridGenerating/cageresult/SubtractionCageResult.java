package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;

public class SubtractionCageResult extends CageResult {
    private SubtractionCageResult(int cellValue1, int cellValue2) {
        super(CageOperator.SUBTRACT, cellValue1, cellValue2);
    }

    static SubtractionCageResult tryToCreate(int... cellValues) {
        if (canBeCreated(cellValues)) {
            return new SubtractionCageResult(cellValues[0], cellValues[1]);
        }
        throw new IllegalStateException(
                String.format("Cannot instantiate with specified values: %s", Arrays.toString(cellValues)));
    }

    public static boolean canBeCreated(int... cellValues) {
        return cellValues != null && cellValues.length == 2;
    }

    @Override
    public int getResult() {
        int lower = Math.min(getCellValue(0), getCellValue(1));
        int higher = Math.max(getCellValue(0), getCellValue(1));

        return higher - lower;

    }
}
