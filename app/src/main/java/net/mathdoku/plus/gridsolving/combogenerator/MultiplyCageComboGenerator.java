package net.mathdoku.plus.gridsolving.combogenerator;

/* package private */ class MultiplyCageComboGenerator extends CageComboGenerator {

    public MultiplyCageComboGenerator(ComboGenerator comboGenerator) {
        super(comboGenerator);
    }

    @Override
    protected boolean canExpandWithValue(int targetValue, int cellValue) {
        return targetValue % cellValue == 0;
    }

    @Override
    protected int getNextTargetValue(int targetValue, int cellValue) {
        return targetValue / cellValue;
    }
}
