package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.puzzle.cage.Cage;

/* package private */ class DivideCageComboGenerator extends CageComboGenerator {
    private final int targetValue;

    public DivideCageComboGenerator(ComboGenerator comboGenerator, Cage cage) {
        super(comboGenerator, cage);
        targetValue = cage.getResult();
    }

    @Override
    protected boolean hasValidNumberOfCells(int numberOfCells) {
        return numberOfCells == 2;
    }

    @Override
    protected boolean canExpandWithValue(CageCombo cageCombo, int cellValue) {
        if (cageCombo.size() == 1) {
            return isTargetValueDivisionOf(cageCombo.getCellValue(0), cellValue);
        }
        return cageCombo.isEmpty();
    }

    private boolean isTargetValueDivisionOf(int cellValue1, int cellValue2) {
        // Avoid integer division to prevent rounding errors
        return cellValue1 * targetValue == cellValue2 || cellValue2 * targetValue == cellValue1;
    }

    @Override
    protected boolean matchesTargetValue(CageCombo cageCombo) {
        return cageCombo.getMaxValue() == targetValue * cageCombo.getMinValue() ;
    }
}
