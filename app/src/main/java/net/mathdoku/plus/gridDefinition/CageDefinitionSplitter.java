package net.mathdoku.plus.griddefinition;

import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.InvalidGridException;

class CageDefinitionSplitter {
    private final String cageDefinition;
    private final String[] cageDefinitionElements;
    private static final int CAGE_ID = 0;
    private static final int RESULT_ID = 1;
    private static final int CAGE_OPERATOR_ID = 2;

    public CageDefinitionSplitter(String cageDefinition) {
        if (cageDefinition == null) {
            throw new InvalidGridException("Cage definition cannot be null.");
        }

        this.cageDefinition = cageDefinition;
        cageDefinitionElements = cageDefinition.split(GridDefinitionDelimiter.LEVEL2);
        if (cageDefinitionElements == null || cageDefinitionElements.length != 3) {
            throw new InvalidGridException(
                    String.format("Cage definition '%s' has invalid number of elements.", cageDefinition));
        }
    }

    public int getCageId() {
        return Integer.valueOf(cageDefinitionElements[CAGE_ID]);
    }

    public int getResult() {
        return Integer.valueOf(cageDefinitionElements[RESULT_ID]);
    }

    public CageOperator getCageOperator() {
        CageOperator cageOperator = null;
        try {
            cageOperator = CageOperator.fromId(cageDefinitionElements[CAGE_OPERATOR_ID]);
        } catch (IllegalArgumentException e) {
            throw new InvalidGridException(String.format("Invalid cage operator '%s' in cage definition '%s'.",
                                                         cageDefinitionElements[CAGE_OPERATOR_ID], cageDefinition), e);
        }

        return cageOperator;
    }
}
