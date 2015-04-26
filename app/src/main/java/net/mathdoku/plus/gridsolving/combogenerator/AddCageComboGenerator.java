package net.mathdoku.plus.gridsolving.combogenerator;

/* package private */ class AddCageComboGenerator extends CageComboGenerator {
    private CageCombo cageCombo = new CageCombo();

    public AddCageComboGenerator(ComboGenerator comboGenerator) {
        super(comboGenerator);
    }

    @Override
    protected void getCombosRecursively(int targetValue, int numberOfCellsRemaining) {
        if (targetValue <= 0 || numberOfCellsRemaining <= 0) {
            // Invalid combo
            return;
        }
        if (numberOfCellsRemaining == 1 && targetValue <= gridSize) {
            cageCombo.append(targetValue);
            if (comboGenerator.satisfiesConstraints(cageCombo)) {
                // Clone the current cage combination as this instance will be manipulated in order to find other
                // combo's.
                add(cageCombo.clone());
                cageCombo.removeLastValue();
            }
        } else {
            for (int cellValue = 1; cellValue <= gridSize; cellValue++) {
                cageCombo.append(cellValue);
                getCombosRecursively(targetValue - cellValue, numberOfCellsRemaining - 1);
                cageCombo.removeLastValue();
            }
        }
    }
}
