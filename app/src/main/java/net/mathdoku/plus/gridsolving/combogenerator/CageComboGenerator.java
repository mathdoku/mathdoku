package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.puzzle.cage.Cage;

import java.util.ArrayList;
import java.util.List;

public abstract class CageComboGenerator {
    protected final ComboGenerator comboGenerator;
    protected final int gridSize;
    private List<CageCombo> allCageCombos;


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

    protected void add(CageCombo cageCombo) {
        allCageCombos.add(cageCombo);
    }

    abstract protected void getCombosRecursively(int targetValue, int numberOfCellsRemaining);
}
