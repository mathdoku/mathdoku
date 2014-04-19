package net.mathdoku.plus.gridsolving.dancinglinesx;

/**
 * Two dimensional nodes which are used in the double linked lists.
 */
class DancingLinesX2DNode {
	DancingLinesX2DNode() {
		L = R = U = D = null;
	}

	public void setLeft(DancingLinesX2DNode left) {
		L = left;
	}

	public void setRight(DancingLinesX2DNode right) {
		R = right;
	}

	public void setUp(DancingLinesX2DNode up) {
		U = up;
	}

	public void setDown(DancingLinesX2DNode down) {
		D = down;
	}

	public DancingLinesX2DNode getLeft() {
		return L;
	}

	public DancingLinesX2DNode getRight() {
		return R;
	}

	public DancingLinesX2DNode getUp() {
		return U;
	}

	public DancingLinesX2DNode getDown() {
		return D;
	}

	private DancingLinesX2DNode L; // Pointer to left node
	private DancingLinesX2DNode R; // Pointer to right node
	private DancingLinesX2DNode U; // Pointer to node above
	private DancingLinesX2DNode D; // Pointer to node below
}
