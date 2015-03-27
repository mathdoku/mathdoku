package net.mathdoku.plus.gridsolving.dancinglinesx;

/*
    The node is a holder for two dimensional structure of double linked nodes. E.g. the nodes can be traversed
    vertically (both upwards and downwards) and horizontally (both leftwards and rightwards).
 */
class Node {
    private Node left;
    private Node right;
    private Node up;
    private Node down;

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public Node getUp() {
        return up;
    }

    public void setUp(Node up) {
        this.up = up;
    }

    public Node getDown() {
        return down;
    }

    public void setDown(Node down) {
        this.down = down;
    }
}
