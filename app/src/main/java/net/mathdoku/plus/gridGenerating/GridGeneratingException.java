package net.mathdoku.plus.gridgenerating;

public class GridGeneratingException extends RuntimeException {
	public GridGeneratingException() {
		super();
	}

	public GridGeneratingException(String errorMessage) {
		super(errorMessage);
	}

	public GridGeneratingException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
