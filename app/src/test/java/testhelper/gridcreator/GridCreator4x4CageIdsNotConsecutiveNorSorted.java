package testhelper.gridcreator;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cage.CageBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The test grid in this class is a variant on the grid in the super class. In contrary to the superclass the cage ids
 * are not consecutive nor sorted. However, the resulting grid is identical to the grid in the superclass.
 */
public class GridCreator4x4CageIdsNotConsecutiveNorSorted extends GridCreator4x4 {
    private final int renumberFromCageId;
    private final int renumberToCageId;

    private GridCreator4x4CageIdsNotConsecutiveNorSorted(boolean hideOperator) {
        super();

        // Determine the cage id which has to be renumbered.
        int offsetToLastCage = 3;
        int numberOfCages = getNumberOfOriginalCages();
        renumberFromCageId = Math.max(0, numberOfCages - offsetToLastCage);

        // One of the cages will be renumbered to the current highest cage id
        // plus 1. As the cage id is zero based, the new cage id is equals to
        // the original number of cages.
        renumberToCageId = numberOfCages;
    }

    private int getNumberOfOriginalCages() {
        int count = 0;
        for (CageOperator cageOperator : super.getCageOperatorPerCage()) {
            if (cageOperator != null) {
                count++;
            }
        }
        return count;
    }

    public static GridCreator4x4CageIdsNotConsecutiveNorSorted create() {
        return new GridCreator4x4CageIdsNotConsecutiveNorSorted(false);
    }

    protected int[] getCageIdPerCell() {
        int[] cageIdPerCell = super.getCageIdPerCell();
        for (int i = 0; i < cageIdPerCell.length; i++) {
            if (cageIdPerCell[i] == renumberFromCageId) {
                cageIdPerCell[i] = renumberToCageId;
            }
        }
        return cageIdPerCell;
    }

    protected int[] getResultPerCage() {
        int[] resultPerCageOriginal = super.getResultPerCage();

        int[] resultPerCage = new int[resultPerCageOriginal.length + 1];
        for (int i = 0; i < resultPerCageOriginal.length; i++) {
            resultPerCage[i] = resultPerCageOriginal[i];
        }
        // The cage result of the renumbered cage is moved to the new (= last)
        // cage.
        resultPerCage[renumberToCageId] = resultPerCageOriginal[renumberFromCageId];
        resultPerCage[renumberFromCageId] = -1;

        return resultPerCage;
    }

    protected CageOperator[] getCageOperatorPerCage() {
        CageOperator[] cageOperatorPerCageOriginal = super.getCageOperatorPerCage();

        CageOperator[] cageOperatorPerCage = new CageOperator[cageOperatorPerCageOriginal.length + 1];
        for (int i = 0; i < cageOperatorPerCageOriginal.length; i++) {
            cageOperatorPerCage[i] = cageOperatorPerCageOriginal[i];
        }
        // The cage result of the renumbered cage is moved to the new (= last)
        // cage.
        cageOperatorPerCage[renumberToCageId] = cageOperatorPerCageOriginal[renumberFromCageId];
        cageOperatorPerCage[renumberFromCageId] = null;

        return cageOperatorPerCage;
    }

    public String getGridDefinition() {
        // Although the cages are numbered differently compared to the
        // superclass, it is essentially the same grid.
        return super.getGridDefinition();
    }

    @Override
    public List<Cage> getCages() {
        List<Cage> cages = new ArrayList<Cage>();

        for (int cageId = 0; cageId < getResultPerCage().length; cageId++) {
            if (getResultPerCage()[cageId] <= 0 && getCageOperatorPerCage()[cageId] == null) {
                // This cage id is missing in the list of cells.
                continue;
            }
            Cage cage = new CageBuilder().setId(cageId)
                    .setHideOperator(getHideOperator())
                    .setCells(getCells(cageId))
                    .setResult(getResultPerCage()[cageId])
                    .setCageOperator(getCageOperatorPerCage()[cageId])
                    .build();
            cages.add(cage);
        }

        return cages;
    }
}
