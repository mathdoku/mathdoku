package net.mathdoku.plus.gridsolving.dancinglinesx;

class DancingLinesXNode extends Node {
    public DancingLinesXNode(ConstraintNode col, int ri) {
        RowIdx = ri;
        C = col;
        col.getUp()
                .setDown(this);
        setUp(col.getUp());
        setDown(col);
        col.setUp(this);
        col.increaseNumberOfPermutations();
    }

    public ConstraintNode getColumn() {
        return C;
    }

    public int getRowIdx() {
        return RowIdx;
    }

    private final ConstraintNode C; // Pointer to Column Header
    private final int RowIdx; // Index to row
}
