package net.mathdoku.plus.griddefinition;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.util.Util;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts the definition of this grid to a string. This definitions only consists of information
 * needed to rebuild the puzzle. It does not include information about how it was created or about
 * the current status of solving. This definition is unique regardless of grid size and or the
 * version of the grid generator used.
 * <p/>
 * The id's of the cages are reindexed so the list of top-left-cells of the cages are sorted in
 * order of increasing cell numbers. In this way to identical grids in which only the cage id's are
 * different result in identical grid definitions.
 */
public class GridDefinitionCreator {
    private final List<Cell> cells;
    private final List<Cage> cages;
    private final GridGeneratingParameters gridGeneratingParameters;
    private final List<Integer> cageIdMapping;

    public GridDefinitionCreator(List<Cell> cells, List<Cage> cages,
                                 GridGeneratingParameters gridGeneratingParameters) {
        this.cells = cells;
        this.cages = cages;
        this.gridGeneratingParameters = gridGeneratingParameters;
        validateParameters();
        cageIdMapping = setCageIdMapping();
    }

    private void validateParameters() {
        if (Util.isListNullOrEmpty(cells)) {
            throw new InvalidParameterException("Parameter cells cannot be null or empty list.");
        }
        if (Util.isListNullOrEmpty(cages)) {
            throw new InvalidParameterException("Parameter cages cannot be null or empty list.");
        }
        if (gridGeneratingParameters == null) {
            throw new InvalidParameterException(
                    "Parameter gridGeneratingParameters cannot be null.");
        }
    }

    private List<Integer> setCageIdMapping() {
        List<Integer> mapping = new ArrayList<Integer>();

        if (cells != null) {
            for (Cell cell : cells) {
                if (!mapping.contains(cell.getCageId())) {
                    mapping.add(cell.getCageId());
                }
            }
        }

        return mapping;
    }

    public String invoke() {
        StringBuilder definitionString = new StringBuilder();

        definitionString.append(toStringPuzzleComplexityId());
        definitionString.append(GridDefinitionDelimiter.LEVEL1);
        definitionString.append(toStringCageIdPerCell());
        definitionString.append(toStringCageDefinitions());

        return definitionString.toString();
    }

    private String toStringPuzzleComplexityId() {
        return Integer.toString(gridGeneratingParameters.getPuzzleComplexity()
                                        .getId());
    }

    private StringBuilder toStringCageIdPerCell() {
        StringBuilder cageIdPerCell = new StringBuilder();

        for (Cell cell : cells) {
            // Note: with a maximum of 81 cells in a 9x9 grid we can never have
            // a cage-id > 99.
            cageIdPerCell.append(String.format("%02d", getNewCageId(cell.getCageId())));
        }

        return cageIdPerCell;
    }

    private int getNewCageId(int oldCageId) {
        return cageIdMapping.indexOf(oldCageId);
    }

    private StringBuilder toStringCageDefinitions() {
        StringBuilder cageDefinitions = new StringBuilder();
        for (int cageId : cageIdMapping) {
            cageDefinitions.append(GridDefinitionDelimiter.LEVEL1);
            cageDefinitions.append(toStringCageDefinition(getCage(cageId)));
        }

        return cageDefinitions;
    }

    private Cage getCage(int id) {
        for (Cage cage : cages) {
            if (cage.getId() == id) {
                return cage;
            }
        }

        return null;
    }

    private StringBuilder toStringCageDefinition(Cage cage) {
        StringBuilder cageDefinition = new StringBuilder();

        cageDefinition.append(getNewCageId(cage.getId()));
        cageDefinition.append(GridDefinitionDelimiter.LEVEL2);
        cageDefinition.append(cage.getResult());
        cageDefinition.append(GridDefinitionDelimiter.LEVEL2);
        cageDefinition.append(toStringHideOperators(cage.getOperator()));

        return cageDefinition;
    }

    private int toStringHideOperators(CageOperator cageOperator) {
        return gridGeneratingParameters.isHideOperators() ? CageOperator.NONE.getId() : cageOperator.getId();
    }
}
