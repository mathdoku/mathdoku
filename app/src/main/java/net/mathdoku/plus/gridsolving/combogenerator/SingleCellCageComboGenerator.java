package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.puzzle.cage.Cage;

/* package private */ class SingleCellCageComboGenerator extends CageComboGenerator {
    private final int targetValue;

    public SingleCellCageComboGenerator(ComboGenerator comboGenerator, Cage cage) {
        super(comboGenerator, cage);
        targetValue = cage.getResult();
    }

    @Override
    protected boolean hasValidNumberOfCells(int numberOfCells) {
        return numberOfCells == 1;
    }

    @Override
    protected boolean canExpandWithValue(CageCombo cageCombo, int cellValue) {
        return cageCombo.getCellValues().isEmpty() && cellValue == targetValue;
    }

    @Override
    protected boolean matchesTargetValue(CageCombo cageCombo) {
        return cageCombo.getCellValue(0) == targetValue;
    }
}
