package net.mathdoku.plus.gridsolving.dancinglinesx;

class DancingLinesXColumn extends DancingLinesX2DNode {
    private int size;

    public DancingLinesXColumn() {
        size = 0;
        setUp(this);
        setDown(this);
    }

    public int getSize() {
        return size;
    }

    public void decreaseSize() {
        size--;
    }

    public void increaseSize() {
        size++;
    }
}
