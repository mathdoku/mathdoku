package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.puzzle.cage.Cage;

import java.util.ArrayList;
import java.util.List;

public abstract class CageComboGenerator {
    private final ComboGenerator comboGenerator;
    private final int gridSize;
    private List<CageCombo> allCageCombos;
    private CageCombo cageCombo = new CageCombo();


    public CageComboGenerator(ComboGenerator comboGenerator) {
        if (comboGenerator == null) {
            throw new IllegalArgumentException("ComboGenerator should not be null");
        }
        gridSize = comboGenerator.getGridSize();
        this.comboGenerator = comboGenerator;
    }

    public List<CageCombo> getCombosForCage(Cage cage) {
        allCageCombos = new ArrayList<CageCombo>();

        getCombosRecursively(cage.getResult(), cage.getNumberOfCells());

        return allCageCombos;
    }

    private void getCombosRecursively(int targetValue, int numberOfCellsRemaining) {
        if (targetValue <= 0 || numberOfCellsRemaining <= 0) {
            // Invalid combo
            return;
        }
        if (numberOfCellsRemaining == 1 && targetValue > gridSize) {
            // Unreachable targetValue
            return;
        }
        if (numberOfCellsRemaining == 1) {
            tryToAddTargetValueToCageCombo(targetValue);
        } else {
            expandCageComboWithNextValue(targetValue, numberOfCellsRemaining);
        }
    }

    private void tryToAddTargetValueToCageCombo(int targetValue) {
        cageCombo.append(targetValue);
        if (comboGenerator.satisfiesConstraints(cageCombo)) {
            // Clone the current cage combination as this instance will be manipulated in order to find other
            // combo's.
            allCageCombos.add(cageCombo.clone());
        }
        cageCombo.removeLastValue();
    }

    private void expandCageComboWithNextValue(int targetValue, int numberOfCellsRemaining) {
        for (int cellValue = 1; cellValue <= gridSize; cellValue++) {
            if (canExpandWithValue(targetValue, cellValue)) {
                cageCombo.append(cellValue);
                getCombosRecursively(getNextTargetValue(targetValue, cellValue), numberOfCellsRemaining - 1);
                cageCombo.removeLastValue();
            }
        }
    }

    protected abstract boolean canExpandWithValue(int targetValue, int cellValue);

    protected abstract int getNextTargetValue(int targetValue, int cellValue);
}
