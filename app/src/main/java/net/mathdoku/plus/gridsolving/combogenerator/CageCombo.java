package net.mathdoku.plus.gridsolving.combogenerator;

import java.util.ArrayList;
import java.util.List;

public class CageCombo {
    private final List<Integer> cellValues;

    public CageCombo() {
        cellValues = new ArrayList<Integer>();
    }

    public void append(Integer cellValue) {
        cellValues.add(cellValue);
    }

    public void removeLastValue() {
        if (!cellValues.isEmpty()) {
            cellValues.remove(cellValues.size() - 1);
        }
    }

    public CageCombo clone() {
        CageCombo cageCombo = new CageCombo();
        for (Integer cellValue : cellValues) {
            cageCombo.append(cellValue);
        }
        return cageCombo;
    }


    public int getCellValue(int index) {
        return new Integer(cellValues.get(index));
    }

    public List<Integer> getCellValues() {
        return new ArrayList<Integer>(cellValues);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CageCombo{");
        sb.append("cellValues=")
                .append(cellValues);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CageCombo)) {
            return false;
        }

        CageCombo cageCombo = (CageCombo) o;

        if (!cellValues.equals(cageCombo.cellValues)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return cellValues.hashCode();
    }
}
