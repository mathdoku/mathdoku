package net.mathdoku.plus.gridsolving.dancinglinesx;

/*
    The ConstraintNode represents a single constraint. Constraints for MathDoku consists of:
    - column constraints (each digit is used once in each column). A column constraint should be interpreted as: "is
    digit <d> used in column <c>?".
    - row constraints (each digit is used once in each row). A row constraint should be interpreted as: "is
    digit <d> used in row <r>?".
    - cage constraints (the calculation of the cage). A cage constraint should be interpreted as: "is
    digit <d> used in column <c> of the cage?" or "is digit <d> used in row <r> of the cage?".

    The vertical list of nodes starting and ending at this nodes represents all permutations (i.e. a possible
    solution to fulfill a single cage) which fulfill this constraint.
 */

class ConstraintNode extends Node {
    private int numberOfPermutations;

    public ConstraintNode() {
        numberOfPermutations = 0;
        setUp(this);
        setDown(this);
    }

    public int getNumberOfPermutations() {
        return numberOfPermutations;
    }

    public void decreaseNumberOfPermutations() {
        numberOfPermutations--;
    }

    public void increaseNumberOfPermutations() {
        numberOfPermutations++;
    }
}
