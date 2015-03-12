package net.mathdoku.plus.gridsolving.dancinglinesx;

class DancingLinesXNode extends DancingLinesX2DNode {
    public DancingLinesXNode(DancingLinesXColumn col, int ri) {
        RowIdx = ri;
        C = col;
        col.getUp()
                .setDown(this);
        setUp(col.getUp());
        setDown(col);
        col.setUp(this);
        col.increaseSize();
    }

    public DancingLinesXColumn getColumn() {
        return C;
    }

    public int getRowIdx() {
        return RowIdx;
    }

    private final DancingLinesXColumn C; // Pointer to Column Header
    private final int RowIdx; // Index to row
}
