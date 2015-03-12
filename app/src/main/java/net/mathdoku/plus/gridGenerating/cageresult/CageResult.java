package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;

public abstract class CageResult {
    @SuppressWarnings("unused")
    private static final String TAG = CageResult.class.getName();

    private int[] cellValues;
    private CageOperator cageOperator;

    CageResult(CageOperator cageOperator, int... cellValues) {
        this.cellValues = cellValues;
        this.cageOperator = cageOperator;
    }

    // Protected constructor for NullCageResult
    CageResult() {
    }

    public static boolean canBeCreated(CageOperator cageOperator, int... cellValues) {
        boolean canBeCreated = false;
        switch (cageOperator) {
            case NONE:
                canBeCreated = SingeCellCageResult.canBeCreated(cellValues);
                break;
            case ADD:
                canBeCreated = AdditionCageResult.canBeCreated(cellValues);
                break;
            case SUBTRACT:
                canBeCreated = SubtractionCageResult.canBeCreated(cellValues);
                break;
            case MULTIPLY:
                canBeCreated = MultiplicationCageResult.canBeCreated(cellValues);
                break;
            case DIVIDE:
                canBeCreated = DivisionCageResult.canBeCreated(cellValues);
                break;
            default:
                throwExceptionForInvalidOperator(cageOperator, cellValues);
                break;
        }
        return canBeCreated;
    }

    private static void throwExceptionForInvalidOperator(CageOperator cageOperator,
                                                         int[] cellValues) {
        throw new IllegalArgumentException(
                String.format("Operator '%s' not allowed for cell values '%s'.",
                              cageOperator.toString(), Arrays.toString(cellValues)));
    }

    public static CageResult create(CageOperator cageOperator, int... cellValues) {
        if (cellValues == null) {
            throw new IllegalArgumentException("Parameter cellValues cannot be null.");
        }

        CageResult cageResult = NullCageResult.create();
        switch (cageOperator) {
            case NONE:
                cageResult = SingeCellCageResult.tryToCreate(cellValues);
                break;
            case ADD:
                cageResult = AdditionCageResult.tryToCreate(cellValues);
                break;
            case SUBTRACT:
                cageResult = SubtractionCageResult.tryToCreate(cellValues);
                break;
            case MULTIPLY:
                cageResult = MultiplicationCageResult.tryToCreate(cellValues);
                break;
            case DIVIDE:
                cageResult = DivisionCageResult.tryToCreate(cellValues);
                break;
            default:
                throwExceptionForInvalidOperator(cageOperator, cellValues);
                break;
        }
        return cageResult;
    }

    public abstract int getResult();

    public CageOperator getCageOperator() {
        return cageOperator;
    }

    int getCellValue(int index) {
        return cellValues[index];
    }

    int[] getCellValues() {
        return cellValues;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isNull() {
        return false;
    }

    public boolean isValid() {
        return !isNull();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CageResult{");
        sb.append("cellValues=")
                .append(Arrays.toString(cellValues));
        sb.append(", cageOperator=")
                .append(cageOperator);
        sb.append('}');
        return sb.toString();
    }
}
