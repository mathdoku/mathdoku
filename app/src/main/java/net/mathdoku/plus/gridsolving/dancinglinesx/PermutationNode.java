package net.mathdoku.plus.gridsolving.dancinglinesx;

class PermutationNode extends Node {
    private final ConstraintNode constraintNode;
    private final int permutationIndex;

    public PermutationNode(ConstraintNode constraintNode, int permutationIndex) {
        this.constraintNode = constraintNode;
        this.permutationIndex = permutationIndex;
        constraintNode.getUp()
                .setDown(this);
        setUp(constraintNode.getUp());
        setDown(constraintNode);
        constraintNode.setUp(this);
        constraintNode.increaseNumberOfPermutations();
    }

    public ConstraintNode getConstraintNode() {
        return constraintNode;
    }

    public int getPermutationIndex() {
        return permutationIndex;
    }
}
