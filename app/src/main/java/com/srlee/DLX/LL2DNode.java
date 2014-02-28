package com.srlee.dlx;

public class LL2DNode {
	LL2DNode() {
		L = R = U = D = null;
	}

	public void setLeft(LL2DNode left) {
		L = left;
	}

	public void setRight(LL2DNode right) {
		R = right;
	}

	public void setUp(LL2DNode up) {
		U = up;
	}

	public void setDown(LL2DNode down) {
		D = down;
	}

	public LL2DNode getLeft() {
		return L;
	}

	public LL2DNode getRight() {
		return R;
	}

	public LL2DNode getUp() {
		return U;
	}

	public LL2DNode getDown() {
		return D;
	}

	private LL2DNode L; // Pointer to left node
	private LL2DNode R; // Pointer to right node
	private LL2DNode U; // Pointer to node above
	private LL2DNode D; // Pointer to node below
}
