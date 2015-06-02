package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.puzzle.cage.Cage;

/* package private */ class AddCageComboGenerator extends CageComboGenerator {
    private final int targetValue;

    public AddCageComboGenerator(ComboGenerator comboGenerator, Cage cage) {
        super(comboGenerator, cage);
        targetValue = cage.getResult();
    }

    @Override
    protected boolean hasValidNumberOfCells(int numberOfCells) {
        return numberOfCells >= 2;
    }

    @Override
    protected boolean canExpandWithValue(CageCombo cageCombo, int cellValue) {
        return addValuesInCageCombo(cageCombo) + cellValue <= targetValue;
    }

    private int addValuesInCageCombo(CageCombo cageCombo) {
        int cageComboTotal = 0;
        for (int valueInCageCombo : cageCombo.getCellValues()) {
            cageComboTotal += valueInCageCombo;
        }
        return cageComboTotal;
    }

    protected boolean matchesTargetValue(CageCombo cageCombo) {
        return addValuesInCageCombo(cageCombo) == targetValue;
    }
}
