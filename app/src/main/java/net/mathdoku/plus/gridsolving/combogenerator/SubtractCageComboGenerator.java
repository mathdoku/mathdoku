package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.puzzle.cage.Cage;

/* package private */ class SubtractCageComboGenerator extends CageComboGenerator {
    private final int targetValue;

    public SubtractCageComboGenerator(ComboGenerator comboGenerator, Cage cage) {
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
            return isTargetValueSubtractionOf(cageCombo.getCellValue(0), cellValue);
        }
        return cageCombo.isEmpty();
    }

    private boolean isTargetValueSubtractionOf(int cellValue1, int cellValue2) {
        return Math.abs(cellValue1 - cellValue2) == targetValue;
    }

    @Override
    protected boolean matchesTargetValue(CageCombo cageCombo) {
        return cageCombo.getMaxValue() - cageCombo.getMinValue() == targetValue;
    }
}
