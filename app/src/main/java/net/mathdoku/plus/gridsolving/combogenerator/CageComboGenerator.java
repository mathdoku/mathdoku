package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.puzzle.cage.Cage;

import java.util.HashSet;
import java.util.Set;

public abstract class CageComboGenerator {
    private final ComboGenerator comboGenerator;
    private final int gridSize;
    private Set<CageCombo> allCageCombos;
    private final Cage cage;
    private final CageCombo cageCombo = new CageCombo();

    private static class InvalidNumberOfCells extends RuntimeException {
    }

    public static CageComboGenerator create(ComboGenerator comboGenerator, Cage cage) {
        CageComboGenerator cageComboGenerator = null;
        switch (cage.getOperator()) {
            case NONE:
                cageComboGenerator = new SingleCellCageComboGenerator(comboGenerator, cage);
                break;
            case ADD:
                cageComboGenerator = new AddCageComboGenerator(comboGenerator, cage);
                break;
            case SUBTRACT:
                cageComboGenerator = new SubtractCageComboGenerator(comboGenerator, cage);
                break;
            case MULTIPLY:
                cageComboGenerator = new MultiplyCageComboGenerator(comboGenerator, cage);
                break;
            case DIVIDE:
                cageComboGenerator = new DivideCageComboGenerator(comboGenerator, cage);
                break;
        }
        if (!cageComboGenerator.hasValidNumberOfCells(cage.getNumberOfCells())) {
            throw new InvalidNumberOfCells();
        }
        return cageComboGenerator;
    }

    protected abstract boolean hasValidNumberOfCells(int numberOfCells);

    public CageComboGenerator(ComboGenerator comboGenerator, Cage cage) {
        if (comboGenerator == null) {
            throw new IllegalArgumentException("ComboGenerator should not be null");
        }
        gridSize = comboGenerator.getGridSize();
        this.comboGenerator = comboGenerator;
        this.cage = cage;
    }

    public Set<CageCombo> getCombos() {
        allCageCombos = new HashSet<CageCombo>();

        expandCageComboWithNextValue();

        return allCageCombos;
    }

    private void expandCageComboWithNextValue() {
        for (int cellValue = 1; cellValue <= gridSize; cellValue++) {
            if (canExpandWithValue(cageCombo, cellValue)) {
                expandWithValue(cellValue);
            }
        }
    }

    protected abstract boolean canExpandWithValue(CageCombo cageCombo, int cellValue);

    private void expandWithValue(int cellValue) {
        cageCombo.append(cellValue);
        if (isCageComboComplete()) {
            if (isValidCageCombo()) {
                // Clone the current cage combination as this instance will be manipulated in order to find other
                // combo's.
                allCageCombos.add(cageCombo.clone());
            }
        } else {
            expandCageComboWithNextValue();
        }
        cageCombo.removeLastValue();
    }

    private boolean isCageComboComplete() {
        return cageCombo.size() == cage.getNumberOfCells();
    }

    private boolean isValidCageCombo() {
        return matchesTargetValue(cageCombo) && comboGenerator.satisfiesConstraints(cageCombo);
    }

    protected abstract boolean matchesTargetValue(CageCombo cageCombo);
}
