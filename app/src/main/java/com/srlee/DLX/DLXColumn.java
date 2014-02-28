package com.srlee.dlx;

public class DLXColumn extends LL2DNode {
	private int size; // Number of items in column

	public DLXColumn() {
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
