package com.srlee.dlx;

class DLXNode extends LL2DNode {
	public DLXNode(DLXColumn col, int ri) {
		RowIdx = ri;
		C = col;
		col.getUp().setDown(this);
		setUp(col.getUp());
		setDown(col);
		col.setUp(this);
		col.increaseSize();
	}

	public DLXColumn getColumn() {
		return C;
	}

	public int getRowIdx() {
		return RowIdx;
	}

	private final DLXColumn C; // Pointer to Column Header
	private final int RowIdx; // Index to row
}
