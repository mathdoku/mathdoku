package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.puzzle.cage.Cage;

/* package private */ class MultiplyCageComboGenerator extends CageComboGenerator {
    private final int targetValue;

    public MultiplyCageComboGenerator(ComboGenerator comboGenerator, Cage cage) {
        super(comboGenerator, cage);
        targetValue = cage.getResult();
    }

    @Override
    protected boolean hasValidNumberOfCells(int numberOfCells) {
        return numberOfCells >= 2;
    }

    @Override
    protected boolean canExpandWithValue(CageCombo cageCombo, int cellValue) {
        return multiplyCageComboValues(cageCombo) * cellValue <= targetValue;
    }

    private int multiplyCageComboValues(CageCombo cageCombo) {
        int cageComboTotal = 1;
        for (int valueInCageCombo : cageCombo.getCellValues()) {
            cageComboTotal *= valueInCageCombo;
        }
        return cageComboTotal;
    }

    @Override
    protected boolean matchesTargetValue(CageCombo cageCombo) {
        return multiplyCageComboValues(cageCombo) == targetValue;
    }
}
