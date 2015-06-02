package net.mathdoku.plus.gridsolving.combogenerator;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;

import java.util.HashSet;
import java.util.Set;

public class CageComboGeneratorTest {
    public static Cage createTwoCellCage(CageOperator cageOperator) {
        int cageSize = 2;
        CageBuilder cageBuilder = new CageBuilder().setCageOperator(CageOperator.DIVIDE)
                .setCells(new int[cageSize])
                .setHideOperator(false)
                .setResult(2);
        return new Cage(cageBuilder);
    }

    public static Set<CageCombo> getExpectedCageCombos(int[][] combos) {
        Set<CageCombo> cageCombos = new HashSet<CageCombo>();
        for (int[] combo : combos) {
            CageCombo cageCombo = new CageCombo();
            for (int value : combo) {
                cageCombo.append(value);
            }
            cageCombos.add(cageCombo);
        }
        return cageCombos;
    }
}