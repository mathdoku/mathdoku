package net.mathdoku.plus.gridsolving.combogenerator;

/* package private */ class AddCageComboGenerator extends CageComboGenerator {

    public AddCageComboGenerator(ComboGenerator comboGenerator) {
        super(comboGenerator);
    }

    @Override
    protected boolean canExpandWithValue(int targetValue, int cellValue) {
        return cellValue <= targetValue;
    }

    @Override
    protected int getNextTargetValue(int targetValue, int cellValue) {
        return targetValue - cellValue;
    }

}
